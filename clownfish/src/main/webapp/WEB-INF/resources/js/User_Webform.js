function add() {
    var attributmap = {
        apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
        classname: document.getElementById('classname').innerText,
        attributmap: getInputInformation(),
        contentname: document.getElementById('contentname').value,
    };

    axios.post('http://localhost:9000/insertcontent',
            attributmap
        )
        .then(function(response) {
            console.log(response);
        })
        .catch(function(error) {
            console.log(error);
        });

    console.log("Add");
}

function add2() {
    var attributmap = {
        email: "admin",
        password: "test",
    };

    axios.post('http://localhost:3000/loginpost',
            attributmap
        )
        .then(function(response) {
            console.log(response);
        })
        .catch(function(error) {
            console.log(error);
        });

    console.log("Add");
}


//Update Content
function update() {
    var attributmap = {
        apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
        classname: document.getElementById('classname').innerText,
        contentname: document.getElementById('contentname').value,
        attributmap: getInputInformation(),
    };

    axios.post('http://localhost:9000/updatecontent',
            attributmap
        )
        .then(function(response) {
            console.log(response);
        })
        .catch(function(error) {
            console.log(error);
        });

    console.log("Update");
}

//Delete content
function deleteI() {
    // 
    var attributmap = {
        apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
        classname: document.getElementById('classname').innerText,
        contentname: document.getElementById('contentname').value,
    };

    axios.post('http://localhost:9000/deletecontent',
            attributmap
        )
        .then(function(response) {
            console.log(response);
        })
        .catch(function(error) {
            console.log(error);
        });

    console.log("Delete");
}

//Give a List of the whole content
function read() {

}

function getInputInformation() {
    var formEl = document.forms.tester;
    var kvpairs = [];
    var form = document.forms.forms;

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

read();