package server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
public class Controller {

    private Server server;
    private boolean serverRunning = false;

    @RequestMapping("/")
    public String index() {
        return "HOMEPAGE!!!";
    }

    @RequestMapping(path="/server", method=RequestMethod.POST)
    public boolean initiateServer() {
        if (!serverRunning) {
            server = new Server();
            Thread serverThread = new Thread(server);
            serverThread.start();
            serverRunning = true;
        }

        return serverRunning;
    }
}
