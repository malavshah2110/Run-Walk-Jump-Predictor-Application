package com.example.group30.assignment3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import umich.cse.yctung.androidlibsvm.LibSVM;

public class PredictActivity extends AppCompatActivity {

    private Button btn_predict, btn_record, btn_result;
    private  Process SVMprocess;
    private String result;
    private ArrayList<Integer> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);
        btn_predict = findViewById(R.id.btn_predict);
        btn_record = findViewById(R.id.btn_record);
        btn_result = findViewById(R.id.btn_pResults);

        btn_predict.setEnabled(false);
        btn_result.setEnabled(false);


        int i=0;
        arrayList = new ArrayList<>();
        while(i<20){
            final int random = new Random().nextInt(59) + 1;
            if(!arrayList.contains(random)){
                i++;
                arrayList.add(random);
            }
        }



        final String trainDataPath = Environment.getExternalStorageDirectory().getPath()
                + "/Android/Data/CSE535_ASSIGNMENT3/"+"dataset.txt";
        final String modelPath = Environment.getExternalStorageDirectory() + "/CSE535_ASSIGNMENT3/svm";
        final String result = Environment.getExternalStorageDirectory() + "/CSE535_ASSIGNMENT3/result";

        //button to shuffle the 20 test records
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TestFileConvert().execute("");
            }
        });

        //button to invoke the predict method of library
        btn_predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PredictModel().execute(trainDataPath, modelPath, result);
            }
        });

        //button to show the test accuracy result
        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayResult();
            }
        });

    }

    //dialog to display the test accuracy
    public void displayResult() {

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View dialogView = inflater.inflate(R.layout.result_layout, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle("SVM Prediction Results")
                .setView(dialogView)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final TextView tv_result = dialogView.findViewById(R.id.tv_result_data);
        tv_result.setText(result);
        final AlertDialog alertDialogCreater = alertDialog.create();
        alertDialogCreater.show();

    }

    //asyntask to convert the test data to proper format.
    class TestFileConvert extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            String storage_folder = "/Android/Data/CSE535_ASSIGNMENT3";
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory().getPath()
                    + storage_folder + "/Group30.db", null);
            File csvDirectory = new File(Environment.getExternalStorageDirectory() + storage_folder);
            //File csvFile = new File(csvDirectory, "dataset.csv");
            File csvFile = new File(csvDirectory, "dataset.txt");
            //CSVWriter writer = null;
            Cursor cursor = null;
            FileWriter writer = null;


            try {

                cursor = db.rawQuery("SELECT COUNT(*) FROM Test", null);
                cursor.moveToFirst();
                int rows = cursor.getInt(0);
                writer = new FileWriter(csvFile);

                for (int i = 10; i < 30; i++) {
                    Log.d("selected rows", " " +i);
                    StringBuilder row;
                    int temp = i+1;
                    Cursor rowCursor = db.rawQuery("SELECT * FROM Test WHERE Activity_ID=" + temp, null);

                    rowCursor.moveToFirst();
                    String label = rowCursor.getString(151);
                    Log.d("data", label);
                    row = new StringBuilder(label + " ");
                    for (int j = 1; j <= 150; j++) {
                        row.append(j).append(":").append(rowCursor.getString(j)).append(" ");
                    }

                    String finalRow = row.toString().trim();
                    finalRow += "\n";

                    writer.append(finalRow);
                    writer.flush();

                }


                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Toast.makeText(getApplicationContext(), "File converted", Toast.LENGTH_LONG).show();
                btn_predict.setEnabled(true);
            }
        }
    }

    //asynctask to invoke the predict method of svm library
    private class PredictModel extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "=====SVM Prediction Start=====", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... parameters) {
            LibSVM.getInstance().predict(TextUtils.join(" ", parameters));
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"logcat", "-t", "100"});
                SVMprocess = process;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "=====SVM Prediction Done=====", Toast.LENGTH_SHORT).show();
//            btn_result.setEnabled(false);
            try {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(SVMprocess.getInputStream()));
                StringBuilder log = new StringBuilder();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                     Log.d("while", line);
                    if (line.contains("LibSVM")) {

                        if (line.contains("=======")) {
                            log.append("==================\n");
                        } else if (line.contains("NDK")) {
                            log.append(line.substring(line.lastIndexOf("NDK:"))).append("\n");
                        } else if (line.contains("End of SVM")) {
                            log.append(line.substring(line.indexOf("End"))).append("\n");
                            break;
                        } else {
//                            int indexOfProcessId = line.lastIndexOf(processId);
//                            String newLine = line.substring(indexOfProcessId);
//                            log.append(newLine).append("\n\n");
                        }
                    }

                }
                Log.d("result", log.toString());
                result = log.toString();
                btn_result.setEnabled(true);

//                btn_train.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
