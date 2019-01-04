package server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

@RestController
public class Controller {
    private Server server;

    // @RequestMapping("/")
    // public String index() {
        // return "HOMEPAGE!!!";
    // }

    // returns true if server has connected, false if not/server isn't even running
    @RequestMapping(path = "/server", method = RequestMethod.POST)
    public String postServer() {
        if (server == null) {
            server = new Server();
            Thread serverThread = new Thread(server);
            serverThread.start();
        }

        return String.valueOf(server.isConnected());
    }

    @RequestMapping(path = "/server", method = RequestMethod.GET)
    public ResponseEntity<String> getServer() {
        if (server != null && server.isConnected()) {
            return ResponseEntity.ok(server.getCmd() + " --- " + server.getData());
        }

        return ResponseEntity.badRequest().body(null);
    }

    @MessageMapping("/data")
    @SendTo("/topic/pod_stats")
    public String podStats(String whatever) {
        if (server != null && server.isConnected()) {
            return server.getCmd() + " --- " + server.getData();
        }

        return "server not started or summin";
    }
}
