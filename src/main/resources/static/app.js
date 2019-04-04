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
    var percentage = (jsonData.data / 1000) * 100; // pretend value we receive is out of 1000

    switch (jsonData.cmd) {
        case "VELOCITY":
            document.getElementById("progressBarBatteryHp1Battery").style.width = percentage + "%";
            break;
        case "ACCELERATION":
            document.getElementById("progressBarBatteryHp1Voltage").style.width = percentage + "%";
            break;
        case "BRAKE_TEMP":
            document.getElementById("progressBarBatteryHp1Temperature").style.width = percentage + "%";
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

    $( "#stop" ).click(function() { sendMessage(1); });
    $( "#launch" ).click(function() { sendMessage(2); });
    $( "#reset" ).click(function() { sendMessage(3); });
    $( "#run_length" ).click(function() { sendMessage(4); });
    $( "#svp_go" ).click(function() { sendMessage(5); });
    $( "#svp_stop" ).click(function() { sendMessage(6); });
});
