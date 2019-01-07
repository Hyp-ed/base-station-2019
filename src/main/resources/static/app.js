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
        stompClient.subscribe('/topic/pod_stats', function (data) {
            showData(data.body);
            console.log('data: ' + data);
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
    stompClient.send("/app/data");
}

function showData(message) {
    document.getElementById("data").innerHTML = message;
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#pull" ).click(function() { pullData(); });
});
