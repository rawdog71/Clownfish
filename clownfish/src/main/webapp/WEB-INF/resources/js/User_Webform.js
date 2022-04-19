var webform = angular.module("webformApp", []);

webform.controller('WebformCtrl', function($scope, $http) {
    $scope.recordEdit = [];
    $scope.tes = "das"
    
    $scope.add = () => {
        console.log("asd")
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
                console.log(response);
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });

        console.log("Add");
    }

    //Update Content
    $scope.update = (name) => {
        console.log(name)
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
                console.log(response);
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });

        console.log("Update");
    }

    $scope.flick = () => {
        var toastElList = [].slice.call(document.querySelectorAll('.toast'))
        var toastList = toastElList.map(function (toastEl) {
            return new bootstrap.Toast(toastEl)
        })

        toastList.forEach(toast => toast.show());
   
        console.log(toastList); 
    }

    //Delete content
    $scope.deleteI = (id) => {
        // 
        console.log($scope.contentList[id].content)
        var attributmap = {
            apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
            classname: document.getElementById('classname').innerText,
            contentname: $scope.contentList[id].content.name,
        };

        axios.post('http://localhost:9000/deletecontent',
                attributmap
            )
            .then(function(response) {
                console.log(response);
                $scope.getList();
            })
            .catch(function(error) {
                console.log(error);
            });

        console.log("Delete");
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
                console.log($scope.contentList)
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
        console.log($scope.recordEdit)
    }

    $scope.getInputInformation = (formID) => {
        var formEl = document.forms.tester;
        var kvpairs = [];
        var form = document.forms[formID];

        console.log(form)

        for (var i = 0; i < form.elements.length; i++) {
            console.log(form.elements)
            var e = form.elements[i];
            var x = {};
            if (e.type == "checkbox") {
                x[e.id] = e.checked
            } else if (e.type == "date") {
                var date = new Date();
                console.log(date.getHours())
                x[e.id] = date + " " + date.getHours() > 10 ? "0" + date.getHours() : date.getHours() + ":" + date.getMinutes() > 10 ? "0" + date.getMinutes() : date.getMinutes() + ":" + date.getSeconds() > 10 ? "0" + date.getSeconds() : date.getSeconds()
            } else {
                x[e.id] = e.value;
            }

            kvpairs.push(x);
        }
        var attributemap = Object.assign({}, ...kvpairs);

        console.log(attributemap);
        return attributemap;
    }

    $scope.getList();
});