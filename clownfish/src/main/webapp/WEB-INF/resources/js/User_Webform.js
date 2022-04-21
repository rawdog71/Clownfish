var webform = angular.module("webformApp", []);

webform.controller('WebformCtrl', function($scope, $http) {
    $scope.recordEdit = [];
    $scope.mediaList = [];
    $scope.tes = "das"
    $scope.libNames = [];

    $scope.add = () => {
        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            classname: document.getElementById('classname').innerText,
            attributmap: $scope.getInputInformation('forms'),
            contentname: document.getElementById('contentname').value,
        };

        axios.post('http://localhost:9000/insertcontent',
                attributmap
            )
            .then(function(response) {
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });
    }

    //Update Content
    $scope.update = (name) => {
        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            classname: document.getElementById('classname').innerText,
            contentname: name,
            attributmap: $scope.getInputInformation('forms2'),
        };

        axios.post('http://localhost:9000/updatecontent',
                attributmap
            )
            .then(function(response) {
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });
    }

    $scope.flick = () => {
        var toastElList = [].slice.call(document.querySelectorAll('.toast'))
        var toastList = toastElList.map(function (toastEl) {
            return new bootstrap.Toast(toastEl)
        })
        toastList.forEach(toast => toast.show());
    }

    //Delete content
    $scope.deleteI = (id) => {

        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            classname: document.getElementById('classname').innerText,
            contentname: $scope.contentList[id].content.name,
        };

        axios.post('http://localhost:9000/deletecontent',
                attributmap
            )
            .then(function(response) {
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });
    }

    //Give a List of the whole content
    $scope.getList = async () => {
        var classname = document.getElementById("classname").innerText
        var req = {
            method: "POST",
            url: "/GetContentHibernate",
            headers: {
                "Content-Type": "application/json",
            },
            data: {
                apikey: `+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=`,
                classname: `${classname}`,
            },
        };

        await $http(req).then(
            function(res) {
                $scope.contentList = JSON.parse(res.data.json);
                return JSON.parse(res.data.json);
            },
            function(e) {
                return false;
            }
        );
    }

    $scope.edit = (id) => {
        $scope.recordEdit = [];
        var x = $scope.contentList[id].keyvals[0];
        x.contentname = $scope.contentList[id].content.name
        $scope.recordEdit.push(x)
    }

    $scope.getInputInformation = (formID) => {
        var formEl = document.forms.tester;
        var kvpairs = [];
        var form = document.forms[formID];

        for (var i = 0; i < form.elements.length; i++) {
            var e = form.elements[i];
            if(e.value == "NOVALUE") {
                continue;
            }
            var x = {};
            if (e.type == "checkbox") {
                x[e.id] = e.checked
            } else if (e.type == "date") {
                var date = new Date()
                var hour = date.getHours() < 10 ? "0" + date.getHours() : date.getHours()
                var minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes()
                var seconds = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds()
                var newDate = e.value + " " + hour + ":" + minutes + ":" + seconds

                x[e.id] = e.value
            } else {
                x[e.id] = e.value;
            }

            kvpairs.push(x);
        }
        var attributemap = Object.assign({}, ...kvpairs);

        return attributemap;
    }

    $scope.getAssetlibs = async () => {
        await $http.get(`GetAssetLibraries?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=`).then(res => {
            for(let i = 0; i < res.data.length; i++) {
                $scope.libNames.push(res.data[i].assetlist)
            }
        });
    }

    $scope.clickTester = () => {
        console.log($scope.recordEdit)
        return 1;
    }

    $scope.getClasses = async () => {
        await $http
            .get(`/GetClasses?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=`)
            .then(
                async function(res) {

                    const index = res.data.findIndex(
                        (info) => info.clazz.name == document.getElementById('classname').innerText
                    );


                    for(let i = 0; i < res.data[index].attributlist.length; i++) {
                        if(res.data[index].attributlist[i].relationref) {
                            var req = {
                                method: "POST",
                                url: "/getdatalists",
                                headers: {
                                    "Content-Type": "application/json",
                                },
                                data: {
                                    apikey: `+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=`,
                                    classname: `${res.data[index].attributlist[i].relationref.name}`,
                                },
                            };

                            await $http(req).then(
                                function(res) {
                                    $scope.classNames = res.data.list
                                },
                                function(e) {
                                    return false;
                                }
                            );
                        }
                    }
                },
                function(e) {
                    /* console.log(e);*/
                }
            );
    }

    $scope.getMedia = () => {
        axios.get('asset_length')
            .then(function(response) {
                $scope.mediaList = response.data;
            })
            .catch(function(error) {
                console.log(error);
            });
    }

    $scope.getAssetlibs();
    $scope.getList();
    $scope.getClasses();
    $scope.getMedia();
});