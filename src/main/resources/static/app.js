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

    stompClient = Stomp.client('ws://localhost:8080/connecthere');
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/podStats', function (data) {
            showData(data.body);
        });
        stompClient.subscribe('/topic/sendMessageStatus', function (data) {
            showSendMessageStatus(data.body);
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

function showSendMessageStatus(message) {
    var jsonData = JSON.parse(message);

    switch (jsonData['status']) { // array syntax because status is javascript keyword
        case 'error':
            $( "#alert_placeholder" ).html('<div class="alert alert-warning alert-dismissible fade show" role="alert">' + jsonData.errorMessage + '<button type="button" class="close" data-dismiss="alert"><span>&times;</span></button></div>');
            console.log("Sent poorly formatted JSON (probably sent run_length with nothing in textbox)");
            break;
        case 'sent msg':
            console.log(`Sent >>> ${JSON.stringify(jsonData.message)} <<< to server`);
            break;
        default:
            console.log("sendMessage returned unknown json");
            break;
    }
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
        default:
            console.log("Got some weird JSON data");
            break;
    }
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#pullData" ).click(function() { pullData(); });

    $( "#stop" ).click(function() { sendMessage('{"command":"STOP"}'); });
    $( "#launch" ).click(function() { sendMessage('{"command":"LAUNCH"}'); });
    $( "#reset" ).click(function() { sendMessage('{"command":"RESET"}'); });
    $( "#run_length" ).click(function() { sendMessage(`{"command":"RUN_LENGTH", "run_length":${document.getElementById("run_length_value").value}}`); }); // template literal, enclosed in back-ticks not single quotes
    $( "#svp_go" ).click(function() { sendMessage('{"command":"SERVICE_PROPULSION", "state":true}'); });
    $( "#svp_stop" ).click(function() { sendMessage('{"command":"SERVICE_PROPULSION", "state":false}'); });
});
