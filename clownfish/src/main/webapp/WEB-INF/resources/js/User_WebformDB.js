/* global axios, bootstrap */

var webform = angular.module("webformApp", []);

webform.controller('WebformCtrl', function($scope, $http) {
    $scope.recordEdit = [];
    $scope.mediaList = [];
    $scope.tes = "das";
    $scope.libNames = [];
    $scope.changedPw = false;
    
    $scope.init = function(datasourcename, tablename, page, limit, attributlist, pklist, orderlist) {
        $scope.datasourcename = datasourcename;
        $scope.tablename = tablename;
        $scope.page = page;
        $scope.limit = limit;
        $scope.attributlist = attributlist;
        $scope.primarykeylist = pklist;
        $scope.orderlist = orderlist;
        $scope.getList();
    };

    $scope.add = () => {
        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            datasource: $scope.datasourcename,
            tablename: $scope.tablename,
            valuemap: $scope.getInputInformation('forms')
        };

        axios.post('/insertdb',
                attributmap
            )
            .then(function(response) {
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });
    };

    //Update Content
    $scope.update = (id) => {
        console.log("UPD: " + id);
        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            datasource: $scope.datasourcename,
            tablename: $scope.tablename,
            valuemap: $scope.getInputInformation('forms2'),
            conditionmap: $scope.getInputInformation('forms2')
        };

        axios.post('/updatedb',
                attributmap
            )
            .then(function(response) {
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });
    };

    $scope.flick = () => {
        var toastElList = [].slice.call(document.querySelectorAll('.toast'));
        var toastList = toastElList.map(function (toastEl) {
            return new bootstrap.Toast(toastEl);
        });

        toastList.forEach(toast => toast.show());
    };

    $scope.testes = () => {
        $scope.changedPw = true;
    };

    //Delete content
    $scope.deleteI = (id) => {
        console.log(id);
        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            datasource: $scope.datasourcename,
            tablename: $scope.tablename,
            conditionmap: $scope.attributlist
        };

        axios.post('/deletedb',
                attributmap
            )
            .then(function(response) {
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });
    };

    //Give a List of the whole content
    $scope.getList = async () => {
        var req = {
            method: "POST",
            url: "/readdb",
            headers: {
                "Content-Type": "application/json"
            },
            data: {
                apikey: `+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=`,
                datasource: $scope.datasourcename,
                page: $scope.page,
                pagination: $scope.limit,
                tablename: $scope.tablename,
                conditionmap: $scope.attributlist,
                valuemap: $scope.orderlist
            }
        };

        await $http(req).then(
            function(res) {
                console.log(res.data.result);
                $scope.contentList = res.data.result;
                return res.data.result;
            },
            function(e) {
                return false;
            }
        );
    };

    $scope.edit = (id) => {
        $scope.recordEdit = [];
        console.log($scope);
        var x = $scope.contentList[id];
        x.id = id;
        $scope.recordEdit.push(x);
    };

    $scope.formatDate = (dateToFormat, action) => {
        var date = new Date(dateToFormat)
        return `${date.getFullYear()}-${(date.getMonth() + 1) < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1)}-${date.getDate() < 10 ? "0" + date.getDate() : date.getDate()}`;
    }

    $scope.getTodaysDate = () => {
        var date = new Date()
        return `${date.getFullYear()}-${(date.getMonth() + 1) < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1)}-${date.getDate() < 10 ? "0" + date.getDate() : date.getDate()}`;
    }

    $scope.getInputInformation = (formID) => {
        var formEl = document.forms.tester;
        var kvpairs = [];
        var form = document.forms[formID];

        for (var i = 0; i < form.elements.length; i++) {
            var e = form.elements[i];
            
            if ((e.type === "select-one") && (e.value === "NOVALUE")) {
                e.value = null;
            }
            if(e.type === "password") {
                if(e.value === undefined || e.value === null || e.value.length < 3) {
                    continue;
                }
            }
            var x = {};
            if (e.type === "checkbox") {
                x[e.id] = e.checked;
            } else if (e.type === "date") {
                if(e.value !== undefined && e.value !== null) {
                    var date = new Date();
                    var time = (date.getHours() < 10 ? "0" + date.getHours() : date.getHours()) + 
                            ":" + (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes()) + 
                            ":" + (date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds());

                    date = new Date(e.value);
                    var newDate = (date.getDate() < 10 ? "0" + date.getDate() : date.getDate()) 
                            + "." + ((date.getMonth() + 1) < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1)) 
                            + "." + (date.getFullYear() < 10 ? "0" + date.getFullYear() : date.getFullYear());

                    x[e.id] = newDate + " " + time;
                } else {
                    continue;
                }
            } else {
                x[e.id] = e.value;
            }

            kvpairs.push(x);
        }
        var attributemap = Object.assign({}, ...kvpairs);

        return attributemap;
    };

    $scope.clickTester = () => {
        return 1;
    };

});