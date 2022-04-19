var webform = angular.module("webformApp", []);

webform.controller('WebformCtrl', function($scope, $http) {
    $scope.recordEdit = [];
    $scope.tes = "das"
    
    $scope.add = () => {
        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            classname: document.getElementById('classname').innerText,
            attributmap: $scope.getInputInformation('forms'),
            contentname: document.getElementById('contentname').value,
        };

        axios.post('/insertcontent',
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

        axios.post('/updatecontent',
                attributmap
            )
            .then(function(response) {
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });
    }

    //Delete content
    $scope.deleteI = (id) => {
        // 
        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            classname: document.getElementById('classname').innerText,
            contentname: $scope.contentList[id].content.name,
        };

        axios.post('/deletecontent',
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
            var x = {};
            if (e.type == "checkbox") {
                x[e.id] = e.checked
            } else if (e.type == "date") {
                var date = new Date();
                x[e.id] = date + " " + date.getHours() > 10 ? "0" + date.getHours() : date.getHours() + ":" + date.getMinutes() > 10 ? "0" + date.getMinutes() : date.getMinutes() + ":" + date.getSeconds() > 10 ? "0" + date.getSeconds() : date.getSeconds()
            } else {
                x[e.id] = e.value;
            }

            kvpairs.push(x);
        }
        var attributemap = Object.assign({}, ...kvpairs);
        return attributemap;
    }

    $scope.getList();
});