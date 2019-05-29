package server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import java.util.concurrent.ScheduledFuture;
import telemetrydata.TelemetryData.*;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.InvalidProtocolBufferException;

import org.json.*;

@RestController
public class Controller {

    @Autowired
    private Server server;

    // starts base station server, and returns if it has connected to pod client or not
    @RequestMapping(path = "/server", method = RequestMethod.POST)
    public String postServer() {
        if (server != null) {
            Thread serverThread = new Thread(server);
            serverThread.start();
            System.out.println("******** Server started");
        }

        return String.valueOf(server.isConnected());
    }

    @MessageMapping("/pullData")
    @SendTo("/topic/podStats")
    public void podStats() {
        // check if scheduledFuture is null bc we don't want to schedule pingData more than once per 100 ms
        // if (server != null && server.isConnected() && scheduledFuture == null) {
            // scheduledFuture = scheduler.scheduleAtFixedRate(() -> pingData(), 100); // don't really need this ScheduledFuture object, maybe to cancel() or something
            // return "{\"status\":\"should be working\"}";
        // }
// 
        // if (scheduledFuture != null) {
            // return "{\"status\":\"ScheduledFuture already running\"}";
        // }

        // return "{\"status\":\"error\", \"errorMessage\":\"error: base-station server probably not connected to pod (pod not started)\"}";
        getReadyToPingData();
        return;
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/sendMessageStatus") // TODO: error messages get sent to same destination as pod stats do, probably should change this
    public String sendMessage(String msg) {
        try {
            if (server != null && server.isConnected()) {
                server.sendMessage(new JSONObject(msg));
                return "{\"status\":\"sent msg\", \"message\":" + msg + "}";
            }
        }
        catch (JSONException e) {
            return "{\"status\":\"error\", \"errorMessage\":\"poorly formed json attempted to be sent to server (probs entered nothing in run_length box)\"}";
        }

        return "{\"status\":\"error\", \"errorMessage\":\"could not send message\"}";
    }

    @Autowired private SimpMessagingTemplate template;
    @Autowired private TaskScheduler scheduler;
    private ScheduledFuture scheduledFuture;  // not autowired since it is initialized using scheduleAtFixedRate() below

    public void getReadyToPingData() {
        while (!server.isConnected()) {
            template.convertAndSend("topic/podStats", "Pod not connected");  // isn't received on GUI, idk why
        }

        scheduledFuture = scheduler.scheduleAtFixedRate(() -> pingData(), 100); // don't really need this ScheduledFuture object, maybe to cancel() or something
    }

    // this method gets scheduled to run every 100ms (resposible for sending data to frontend)
    public void pingData() {
        ClientToServer msg = server.getProtoMessage();
        JsonFormat.Printer protoJsonPrinter = JsonFormat.printer();
        String msgJson;

        try {
            msgJson = protoJsonPrinter.print(msg);
        }
        catch (InvalidProtocolBufferException e) {
            System.out.println("Error: " + e);
            msgJson = "{\"status\":\"error\", \"errorMessage\":\"empty msgJson\"}";
        }

        template.convertAndSend("/topic/podStats", msgJson);
    }
}
