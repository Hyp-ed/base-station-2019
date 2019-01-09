package server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.ResponseEntity;
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

    // @RequestMapping("/")
    // public String index() {
        // return "HOMEPAGE!!!";
    // }

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

    @MessageMapping("/data")
    @SendTo("/topic/pod_stats")
    public String podStats() {
        if (server != null && server.isConnected()) {
            ScheduledFuture scheduledFuture = scheduler.scheduleAtFixedRate(() -> pingData(), 100); // don't really need this ScheduledFuture object, maybe to cancel() or something

            return "hopefully this works :)";
        }

        return "server not started or summin";
    }

    @Autowired
    private SimpMessagingTemplate template;

    // this method gets scheduled to run every 100ms (resposible for sending data to frontend)
    public void pingData() {
        JSONObject data = new JSONObject();
        data.put("cmd", server.getCmd());
        data.put("data", server.getData());

        template.convertAndSend("/topic/pod_stats", data.toString());
    }
}
