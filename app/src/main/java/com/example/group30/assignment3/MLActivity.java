package com.example.group30.assignment3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import umich.cse.yctung.androidlibsvm.LibSVM;



/**
 * Created by Dishank on 4/16/2018.
 */

public class MLActivity extends AppCompatActivity {


    private Button btn_train;
    private Button btn_plotGraph;
    private Button btn_result, btn_predict;
    private TextView tv_score;
    private EditText et_params;
    private EditText et_gamma, et_degree, et_nfold;
    private Spinner sp_svmType, sp_kernelType;
    //private svm_parameter parameter;
    private double model_accuracy = 0;
    //private svm_problem model;
    private String trainDataPath;
    private String modelPath;
    private String dataPredictPath;
    private String parameters;
    private String processId = Integer.toString(android.os.Process.myPid());
    private String result;
    private Process SVMprocess;
    private WebView graphPlot;
    private Cursor rowData;
    private HashMap<String, Integer> spinnerMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml);
        //executing the asynctask to convert the database file into dataset.txt
        //the file data is converted into proper format to provide in the SVM algorithm
        new FileConvert().execute("");

        String[] svmType_array = getResources().getStringArray(R.array.svmType_array);
        String[] kernelType_array = getResources().getStringArray(R.array.kernelType_array);

        sp_svmType = findViewById(R.id.sp_svmType);
        sp_kernelType = findViewById(R.id.sp_kernelType);
        et_degree = findViewById(R.id.et_degree);
        et_gamma = findViewById(R.id.et_gamma);
        et_nfold = findViewById(R.id.et_nfold);

        sp_svmType.setEnabled(false);
        et_gamma.setEnabled(false);
        et_gamma.setText("Gamma 0.0067");
        et_degree.setText("Degree 3");
        et_degree.setEnabled(false);
        sp_kernelType.setEnabled(false);

        spinnerMap = new HashMap<>();

        for (int i=0;i<5;i++){
            spinnerMap.put(svmType_array[i],i);
            spinnerMap.put(kernelType_array[i],i);
        }

        //Initializing the paths to the folders to create the model files and reading the dataset.txt file

        String storage_folder = "/Android/Data/CSE535_ASSIGNMENT3/";
        //parameters = "-s 0 -t 1 -d 3 -g 0.0067 -v 4 ";
        String appFolder = Environment.getExternalStorageDirectory().getPath()
                + storage_folder;
        initializeFilePaths(appFolder);

        graphPlot = findViewById(R.id.wv_graphView);
//        et_params = findViewById(R.id.tv_params);
//        et_params.setText("-:SVM Classifier:-\n" +
//                "svm_type = C-SVC\t kernel_type = polynomial\n" +
//                "degree = 3\t gamma = 0.0067\n" +
//                "n_fold = 4");




        btn_train = findViewById(R.id.btn_train);
        btn_result = findViewById(R.id.btn_result);
        btn_plotGraph = findViewById(R.id.btn_plotGraph);
        btn_predict = findViewById(R.id.btn_pAct);
        btn_result.setEnabled(false);

        //button to go the activity to predict the accuracy of test set
        btn_predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MLActivity.this, PredictActivity.class);
                startActivity(intent);
            }
        });

        //To invoke the library method to train the SVM algorithm
        btn_train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!et_nfold.getText().toString().equals("")) {
                    btn_train.setEnabled(false);


                    parameters =
//                        "-s "+spinnerMap.get(sp_svmType.getSelectedItem())
//                        +" -t "+ spinnerMap.get(sp_kernelType.getSelectedItem())
//                        +" -d "+ et_degree.getText().toString()
//                        +" -g "+ et_gamma.getText().toString()
                            " -v " + et_nfold.getText().toString()
                    ;
                    Log.d("params ", parameters);
                    new TrainModel().execute(parameters, trainDataPath, modelPath);
                }else{
                    Toast.makeText(getApplicationContext(), "Select n_fold parameter", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                btn_result.setEnabled(false);
                displayResult();
            }
        });


        graphPlot.setWebContentsDebuggingEnabled(true);

        graphPlot.setWebChromeClient(new WebChromeClient());

        // enable JS
        WebSettings webSettings = graphPlot.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);

        //ploting the
        btn_plotGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String storage_folder = "/Android/Data/CSE535_ASSIGNMENT3";
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory().getPath()
                        + storage_folder + "/Group30.db", null);
                rowData = db.rawQuery("SELECT * FROM Test", null);
                final float[][][] dataArray = new float[rowData.getCount()][3][50];
                int column = 0;
                int row = 0;
                while (rowData.moveToNext()) {
                    column=0;
                    for (int i = 1; i <= 150; i+=3) {
                        dataArray[row][0][column] = rowData.getFloat(i);
                        dataArray[row][1][column] = rowData.getFloat(i+1);
                        dataArray[row][2][column] = rowData.getFloat(i+2);
                        Log.d("array","row "+ row +" "+dataArray[row][0][column]+" "+dataArray[row][1][column]+" "+ dataArray[row][2][column]);
                        column++;
                    }
                    row++;

                }
                Log.d("array", dataArray[rowData.getCount()-1][0][49]+" "+dataArray[rowData.getCount()-1][1][49]);

                graphPlot.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        Log.d("in js", "hello");
                        JSONObject jsonObj = null;
                        Gson gson = new Gson();
                        String json = gson.toJson(dataArray);

                        try {
                            jsonObj = new JSONObject("{\"phonetype\":\"N95\",\"cat\":\"WP\"}");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        graphPlot.evaluateJavascript("plotgraph(" + json + ")", null);
                    }
                });
                graphPlot.loadUrl("file:///android_asset/www/plotlyEX.html");

            }
        });

    }

    //dialog to display the accuracy of the training phase.
    public void displayResult() {

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View dialogView = inflater.inflate(R.layout.result_layout, null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle("SVM Train Results")
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

    public void initializeFilePaths(String appFolder) {

        trainDataPath = appFolder + "dataset.txt";
        String modelFilePath = Environment.getExternalStorageDirectory() + "/CSE535_ASSIGNMENT3/";
        modelPath = modelFilePath + "svm";
        //dataPredictPath = appFolder + "predict";

        /*File f = new File(Environment.getExternalStorageDirectory(), "/Android/Data/CSE535_ASSIGNMENT3/model");
        if (!f.exists()) {
            f.mkdirs();
            Log.d("success", "file created");
        }
        f = new File(Environment.getExternalStorageDirectory(), "/Android/Data/CSE535_ASSIGNMENT3/predict");
        if (!f.exists()) {
            f.mkdirs();
            Log.d("success", "file created");
        }*/

    }


    //asynctask to invoke the library function to train the svm model
    private class TrainModel extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "=========SVM Training Start==========", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... parameters) {
            LibSVM.getInstance().train(TextUtils.join(" ", parameters));
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
            Toast.makeText(getApplicationContext(), "=========SVM Training Done==========", Toast.LENGTH_SHORT).show();
//            btn_result.setEnabled(false);
            try {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(SVMprocess.getInputStream()));
                StringBuilder log = new StringBuilder();
                log.append("-:SVM Classifier:-\n" +
                        "svm_type = C-SVC\t kernel_type = polynomial\n" +
                        "degree = 3\t gamma = 0.0067\n" +
                        "n_fold = "+ et_nfold.getText().toString()+"\n");
                String line = "";
                bufferedReader.readLine();
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
                btn_train.setEnabled(true);
                btn_result.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //asynctask to convert the file into .txt from .db
    class FileConvert extends AsyncTask<String, Void, Boolean> {

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
                Log.d("rows ",""+rows);
                writer = new FileWriter(csvFile);

                for (int i = 20; i < rows; i++) {
                    StringBuilder row;
                    int temp = i + 1;
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
            }
        }
    }






}
