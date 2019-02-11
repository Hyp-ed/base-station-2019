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

import org.json.*;

@RestController
public class Controller {
    private Server server;

    // starts base station server, and returns if it has connected to pod client or not
    @RequestMapping(path = "/server", method = RequestMethod.POST)
    public String postServer() {
        if (server == null) {
            server = new Server();
            Thread serverThread = new Thread(server);
            serverThread.start();
        }

        return String.valueOf(server.isConnected());
    }

    @Autowired
    private TaskScheduler scheduler;
    private ScheduledFuture scheduledFuture;

    @MessageMapping("/pullData")
    @SendTo("/topic/podStats")
    public String podStats() {
        // check if scheduledFuture is null bc we don't want to schedule pingData more than once per 100 ms
        if (server != null && server.isConnected() && scheduledFuture == null) {
            scheduledFuture = scheduler.scheduleAtFixedRate(() -> pingData(), 100); // don't really need this ScheduledFuture object, maybe to cancel() or something
            return "{\"status\":\"should be working\"}";
        }

        return "{\"status\":\"error\", \"errorMessage\":\"error: base-station server probably not connected to pod (pod not started)\"}";
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/podStats") // error messages get sent to same destination as pod stats do, probably should change this
    public String sendMessage(int msg) {
        if (server != null && server.isConnected()) {
            server.sendMessage(msg);
            return "{\"status\":\"sent msg: < " + msg + "> to server\"}";
        }

        return "{\"status\":\"error\", \"errorMessage\":\"could not send message\"}";
    }

    @Autowired
    private SimpMessagingTemplate template;

    // this method gets scheduled to run every 100ms (resposible for sending data to frontend)
    public void pingData() {
        JSONObject data = new JSONObject();
        data.put("cmd", server.getCmd());
        data.put("data", server.getData());

        template.convertAndSend("/topic/podStats", data.toString());
    }
}
