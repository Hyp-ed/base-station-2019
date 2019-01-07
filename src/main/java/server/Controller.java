package server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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

    @MessageMapping("/data")
    @SendTo("/topic/pod_stats")
    public String podStats() {
        if (server != null && server.isConnected()) {
            JSONObject data = new JSONObject();
            data.put("cmd", server.getCmd());
            data.put("data", server.getData());

            return data.toString();
        }

        return "server not started or summin";
    }
}
