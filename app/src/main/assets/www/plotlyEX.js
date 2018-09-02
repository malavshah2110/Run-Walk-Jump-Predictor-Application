var message;

//function to plot graph the first time it is loaded
//@param msg 3d array with the x,y,z data points
function plotgraph(msg){

//       var x = msg[0][0];
//       var y = msg[0][1];
//       var z= msg[0][2];
//    var j=0;
//    console.log(msg[0][0][0]);
    message = msg;
    var charts = [];
    var layout = {
      title:'',
      height: 400,
      width: 368
    };

    for (var dataItems = 0; dataItems<msg.length; dataItems++){

        var lineColor = 0;
        var hideLegend = false;
        var x = msg[dataItems][0];
        var y = msg[dataItems][1];
        var z = msg[dataItems][2];
        if(dataItems < 20){
            if(dataItems == 0){
                hideLegend = true;
            }

            lineColor = 'rgb(128, 0, 128)'
        } else if(dataItems < 40){
            lineColor = 'rgb(55, 128, 191)'
        } else {
            lineColor = 'rgb(219, 64, 82)'
        }

        var trace = {
        type: 'scatter3d',
                          mode: 'lines',
                          x: x,
                          y: y,
                          z: z,
                          opacity: 1,
                          line: {
                            width: 6,
                            color: lineColor,
                            reversescale: false
                          }


        };
        charts.push(trace);

//        Plotly.plot('graph', [{
//              type: 'scatter3d',
//              mode: 'lines',
//              x: x,
//              y: y,
//              z: z,
//              opacity: 1,
//              line: {
//                width: 6,
//                color: 0,
//                reversescale: false
//              }
//            }], {
//              height: 400,
//              width: 368
//            });


    }


    Plotly.newPlot('graph', charts, layout);


}

//function to select plots according to checkboxes
function checkPlot(){

    document.getElementById("graph").innerHTML = "";
    var runCheck = document.getElementById('runPlot');
    var walkCheck = document.getElementById('walkPlot');
     var jogCheck = document.getElementById('jogPlot');
    console.log(walkCheck.checked+ " "+ jogCheck.checked+ " "+ runCheck.checked);

    refreshPlot(walkCheck.checked, jogCheck.checked, runCheck.checked )


}

//function to refresh plot according to selected checkbox values
function refreshPlot(walk, jog, run){

    var charts = [];
        var layout = {
          title:'',
          height: 400,
          width: 368
        };

        for (var dataItems = 0; dataItems<60; dataItems++){



            var lineColor = 0;
            var x = message[dataItems][0];
            var y = message[dataItems][1];
            var z = message[dataItems][2];
            if(dataItems < 20){

                lineColor = 'rgb(128, 0, 128)'
            } else if(dataItems < 40){
                lineColor = 'rgb(55, 128, 191)'
            } else {
                lineColor = 'rgb(219, 64, 82)'
            }

            var trace = {
            type: 'scatter3d',
                              mode: 'lines',
                              x: x,
                              y: y,
                              z: z,
                              opacity: 1,
                              line: {
                                width: 6,
                                color: lineColor,
                                reversescale: false
                              }


            };

            if( walk && dataItems< 20)
            {
               charts.push(trace);
            }
            if( jog && dataItems<40 && dataItems >=20){
                charts.push(trace);
            }
            if( run && dataItems<60 && dataItems >=40){
                charts.push(trace);
            }

//            Plotly.plot('graph', [{
//                  type: 'scatter3d',
//                  mode: 'lines',
//                  x: x,
//                  y: y,
//                  z: z,
//                  opacity: 1,
//                  line: {
//                    width: 6,
//                    color: lineColor,
//                    reversescale: false
//                  }
//                }], {
//                  height: 400,
//                  width: 368
//                });
//
//
//        }

    }
    Plotly.newPlot('graph', charts, layout);

}
