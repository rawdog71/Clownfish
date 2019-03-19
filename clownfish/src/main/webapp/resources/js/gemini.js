/* 
 * Copyright Rainer Sulzbach
 */

function generateJsonParam(name, value, jsonparam) {
    dict1 = {};
    dict1["name"] = name;
    dict1["value"] = value;
    jsonparam.push(dict1);
}

function generateJsonParamDBRead(tablename, orderby, orderdir, pagination, page, jsonparam) {
    dict1 = {};
    dict1["name"] = "db$table";
    dict1["value"] = tablename;
    jsonparam.push(dict1);
    dict2 = {};
    dict2["name"] = "db$table$"+tablename+"$orderby";
    dict2["value"] = orderby;
    jsonparam.push(dict2);
    dict3 = {};
    dict3["name"] = "db$table$"+tablename+"$orderdir";
    dict3["value"] = orderdir;
    jsonparam.push(dict3);
    dict4 = {};
    dict4["name"] = "db$table$"+tablename+"$pagination";
    dict4["value"] = pagination;
    jsonparam.push(dict4);
    dict5 = {};
    dict5["name"] = "db$table$"+tablename+"$page";
    dict5["value"] = page;
    jsonparam.push(dict5);
}

function getCurrentDate() {
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth() + 1; //January is 0!

    var yyyy = today.getFullYear();
    if (dd < 10) {
      dd = '0' + dd;
    } 
    if (mm < 10) {
      mm = '0' + mm;
    }
    var hour = today.getHours();
    if (hour < 10) {
      hour = '0' + hour;
    }
    var minute = today.getMinutes();
    if (minute < 10) {
      minute = '0' + minute;
    }
    var second = today.getSeconds();
    if (second < 10) {
      second = '0' + second;
    }
    var currentDate = dd + '.' + mm + '.' + yyyy + ' ' + hour + ":" + minute + ":" + second;
    
    return currentDate;
    
}