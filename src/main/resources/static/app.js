var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#data").show();
    }
    else {
        $("#data").hide();
    }
}

function startBaseStationServer() {
    var request = new XMLHttpRequest;
    request.onload = function() {
        console.log("SERVER CONNECTED TO POD CLIENT: " + request.responseText);
    };
    request.open("POST", "http://localhost:8080/server");
    request.send();
}

function connect() {
    startBaseStationServer();

    var socket = new SockJS('/connecthere');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/podStats', function (data) {
            showData(data.body);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function pullData() {
    stompClient.send("/app/pullData");
}

function sendMessage(msg) {
    stompClient.send("/app/sendMessage", {}, msg);
}

function showData(message) {
    var jsonData = JSON.parse(message);
    switch (jsonData.cmd) {
        case "VELOCITY":
            document.getElementById("velocity").innerHTML = jsonData.data;
            break;
        case "ACCELERATION":
            document.getElementById("acceleration").innerHTML = jsonData.data;
            break;
        case "BRAKE_TEMP":
            document.getElementById("brake_temp").innerHTML = jsonData.data;
            break;
        default: // probably an received an error
            if (jsonData.status == "error") {
                $( "#alert_placeholder" ).html('<div class="alert alert-warning alert-dismissible fade show" role="alert">' + jsonData.errorMessage + '<button type="button" class="close" data-dismiss="alert"><span>&times;</span></button></div>');
            }
            else {
                console.log("Got some weird JSON data");
            }
    }
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#pullData" ).click(function() { pullData(); });
    $( "#send1" ).click(function() { sendMessage(1); });
    $( "#send2" ).click(function() { sendMessage(2); });
});
