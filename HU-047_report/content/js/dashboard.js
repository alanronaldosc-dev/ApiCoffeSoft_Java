/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 98.92561983471074, "KoPercent": 1.0743801652892562};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.981198347107438, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.9521531100478469, 500, 1500, "A1 GET ventas/reporte hoy"], "isController": false}, {"data": [0.9975, 500, 1500, "C2 GET campanas"], "isController": false}, {"data": [0.9975, 500, 1500, "B2 GET segmentos"], "isController": false}, {"data": [0.9601990049751243, 500, 1500, "A5 GET ventas"], "isController": false}, {"data": [0.9876847290640394, 500, 1500, "A4 GET top-productos mes"], "isController": false}, {"data": [0.9903846153846154, 500, 1500, "A2 GET ventas/reporte mes"], "isController": false}, {"data": [1.0, 500, 1500, "D1 GET auditoria"], "isController": false}, {"data": [1.0, 500, 1500, "POST login setup"], "isController": false}, {"data": [0.9875, 500, 1500, "C1 GET campanas vigentes"], "isController": false}, {"data": [0.9735576923076923, 500, 1500, "A3 GET top-productos hoy"], "isController": false}, {"data": [0.93, 500, 1500, "B1 GET segmentos resumen"], "isController": false}, {"data": [1.0, 500, 1500, "E2 GET inventario"], "isController": false}, {"data": [1.0, 500, 1500, "E1 GET productos"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 2420, 26, 1.0743801652892562, 70.01859504132216, 4, 5006, 15.0, 63.90000000000009, 118.0, 1680.3199999999997, 39.37071924772642, 53.21740540147559, 14.983014259684058], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["A1 GET ventas/reporte hoy", 209, 7, 3.349282296650718, 176.6267942583732, 6, 5006, 17.0, 86.0, 1044.5, 4573.9000000000015, 3.5434539350988437, 3.39465655305856, 1.370320076464006], "isController": false}, {"data": ["C2 GET campanas", 200, 0, 0.0, 29.31500000000001, 4, 1047, 13.0, 49.70000000000002, 90.79999999999995, 356.7000000000003, 4.163804051381342, 6.47341411113193, 1.5370292299044406], "isController": false}, {"data": ["B2 GET segmentos", 200, 0, 0.0, 32.949999999999996, 5, 1348, 14.0, 54.50000000000003, 85.94999999999999, 464.3500000000006, 4.190148959795521, 1.7718110347572855, 1.5876736292975215], "isController": false}, {"data": ["A5 GET ventas", 201, 1, 0.4975124378109453, 116.2288557213931, 5, 2071, 19.0, 94.2000000000001, 1060.3999999999994, 1365.82, 3.8357314606312736, 23.426430229285142, 1.4084326457005458], "isController": false}, {"data": ["A4 GET top-productos mes", 203, 2, 0.9852216748768473, 56.02955665024635, 6, 1775, 18.0, 70.0, 118.59999999999997, 1643.680000000006, 3.8401876584313874, 1.9206295696814346, 1.5375751366766297], "isController": false}, {"data": ["A2 GET ventas/reporte mes", 208, 1, 0.4807692307692308, 49.90865384615387, 6, 2016, 17.5, 52.099999999999994, 118.54999999999998, 1030.3599999999992, 3.8950581449785586, 5.382331323383457, 1.5062920170034269], "isController": false}, {"data": ["D1 GET auditoria", 199, 0, 0.0, 25.793969849246228, 4, 288, 13.0, 67.0, 99.0, 252.0, 4.178039051018266, 5.153186837339912, 1.5463640628280495], "isController": false}, {"data": ["POST login setup", 1, 0, 0.0, 146.0, 146, 146, 146.0, 146.0, 146.0, 146.0, 6.8493150684931505, 5.819242294520548, 1.618685787671233], "isController": false}, {"data": ["C1 GET campanas vigentes", 200, 1, 0.5, 46.86, 5, 1541, 14.0, 42.0, 82.79999999999995, 1423.810000000001, 4.1790296293200715, 6.497085126833549, 1.5793793618621756], "isController": false}, {"data": ["A3 GET top-productos hoy", 208, 2, 0.9615384615384616, 81.75961538461533, 4, 2043, 17.0, 87.1, 350.9999999999959, 1764.3499999999983, 3.888068489821859, 1.951628128680113, 1.5567461726825804], "isController": false}, {"data": ["B1 GET segmentos resumen", 200, 12, 6.0, 170.27500000000003, 5, 2207, 13.5, 93.50000000000009, 2064.2999999999997, 2196.99, 4.017597075189329, 2.0127219722384044, 1.5536801189208735], "isController": false}, {"data": ["E2 GET inventario", 194, 0, 0.0, 19.984536082474218, 4, 169, 12.0, 42.5, 63.25, 162.35000000000008, 4.158360662765524, 2.7126805886009473, 1.5431416521981438], "isController": false}, {"data": ["E1 GET productos", 197, 0, 0.0, 27.263959390862915, 4, 263, 12.0, 58.0, 112.19999999999999, 241.44000000000023, 4.200336879810665, 3.6342758549924308, 1.5546168725080487], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["The operation lasted too long: It took 2,197 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,071 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,916 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,193 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,153 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,016 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 1,682 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,196 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 1,775 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 1,541 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,126 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 4,267 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,092 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,051 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 4,608 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,130 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 5,006 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,207 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,043 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,087 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,065 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 1,674 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 1,808 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 3,278 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,165 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}, {"data": ["The operation lasted too long: It took 2,999 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, 3.8461538461538463, 0.04132231404958678], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 2420, 26, "The operation lasted too long: It took 2,197 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,071 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,916 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,193 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,153 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["A1 GET ventas/reporte hoy", 209, 7, "The operation lasted too long: It took 3,278 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 4,267 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,916 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 4,608 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,165 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["A5 GET ventas", 201, 1, "The operation lasted too long: It took 2,071 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["A4 GET top-productos mes", 203, 2, "The operation lasted too long: It took 1,775 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 1,674 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "", "", "", "", "", ""], "isController": false}, {"data": ["A2 GET ventas/reporte mes", 208, 1, "The operation lasted too long: It took 2,016 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["C1 GET campanas vigentes", 200, 1, "The operation lasted too long: It took 1,541 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["A3 GET top-productos hoy", 208, 2, "The operation lasted too long: It took 2,043 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 1,808 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "", "", "", "", "", ""], "isController": false}, {"data": ["B1 GET segmentos resumen", 200, 12, "The operation lasted too long: It took 1,682 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,196 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,197 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,207 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1, "The operation lasted too long: It took 2,126 milliseconds, but should not have lasted longer than 1,500 milliseconds.", 1], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
