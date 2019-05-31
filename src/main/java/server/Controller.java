package server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
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
            System.out.println("Server started");
        }

        return String.valueOf(server.isConnected());
    }

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private TaskScheduler scheduler;

    @MessageMapping("/pullData")
    @SendTo("/topic/isPodConnected")
    public void podStats() {
        Thread checkToScheduleThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!server.isConnected()) {
                    try {
                        Thread.sleep(200);
                    }
                    catch (InterruptedException e) {
                        System.out.println("Error putting thread to sleep while checking if pod is connected");
                    }
                }

                template.convertAndSend("/topic/isPodConnected", "CONNECTED");

                scheduler.scheduleAtFixedRate(() -> pingPodConnectionStatus(), 100);
                scheduler.scheduleAtFixedRate(() -> pingData(), 100);

                return;  // end thread
            }
        });

        checkToScheduleThread.start();

        // don't return anything so that frontend knows as soon as it receives something from /topic/isPodConnected pod is connected
        return;
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/sendMessageStatus")
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

    // this method gets scheduled to run every 100ms (resposible for sending pod status to frontend)
    public void pingPodConnectionStatus() {
        if (!server.isConnected()) {
            template.convertAndSend("/topic/isPodConnected", "DISCONNECTED");
        }
        else {
            template.convertAndSend("/topic/isPodConnected", "CONNECTED");
        }
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
