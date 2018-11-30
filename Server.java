import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.beans.property.SimpleStringProperty;
import types.*;

public class Server implements Runnable {
    private static final int PORT = 9090;
    private Socket client = null;
    private SimpleStringProperty cmd = new SimpleStringProperty(this, "cmd", "COMMAND");
    private SimpleStringProperty data = new SimpleStringProperty(this, "data", "DATA");

    @Override
    public void run() {
        ServerSocket listener = getServerSocket(PORT);
        System.out.println("Server now listening on port " + PORT);
        System.out.println("Waiting to connect to client...");

        try {
            client = getClientServerFromListener(listener);
            System.out.println("Connected to client");

            Thread readWorker = new Thread(new MessageReader());
            readWorker.start();

            try {
                readWorker.join();
            }
            catch (InterruptedException e) {
                System.out.println("Problem joining threads");
            }

            closeClient(client);
        }
        finally  {
            closeServer(listener);
        }
    }

    public void sendMessage(String message) {
        try {
            Thread sendWorker = new Thread(new MessageSender(message));
            sendWorker.start();
        }
        catch (NullPointerException e) {
            System.out.println("Could not send message, client probably not running");
        }
    }

    private class MessageSender implements Runnable {
        private PrintWriter out = null;
        private Message msg;

        public MessageSender(String msgContent) {
            out = getPrintWriter(Server.this.client);
            msg = new Message(msgContent);
        }

        @Override
        public void run() {
            msg.send(this.out);
            System.out.println("Sent message to client");
        }
    }

    private class MessageReader implements Runnable {
        private BufferedReader in = null;
        private Logger logger = null;

        public MessageReader() {
            in = getBufferedReader(Server.this.client);
        }

        @Override
        public void run() {
            try {
                logger = Logger.getLogger(Server.class.getName());
                FileHandler fh = new FileHandler(System.getProperty("user.dir") + "/temp/server_log.log"); // make sure temp dir exists in current dir before running
                fh.setFormatter(new SimpleFormatter());
                logger.addHandler(fh);
                logger.setUseParentHandlers(false);

                logger.info("******BEGIN******");

                while (true) {
                    Message msg = new Message();
                    msg.read(in);

                    logger.info("COMMAND: " + msg.getCommand());
                    logger.info("DATA: " + msg.getData());

                    Server.this.cmd.set(msg.getCommand());
                    Server.this.data.set(msg.getData());
                    System.out.println("msg.command: " + Server.this.cmd.getValue());
                    System.out.println("msg.data: " + Server.this.data.getValue());

                    if (msg.getData().equals("END")) {
                        System.out.println("Client decided to end connection");
                        System.exit(0);
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Something went wrong while reading message");
            }
        }
    }

    public SimpleStringProperty dataProperty() {
        return this.data;
    }

    private static ServerSocket getServerSocket(int portNum) {
        try {
            return new ServerSocket(portNum);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to get new server socket");
        }
    }

    private static Socket getClientServerFromListener(ServerSocket lstn) {
        try {
            return lstn.accept();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to get new client socket");
        }
    }

    private static PrintWriter getPrintWriter(Socket clientSocket) {
        try {
            return new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed getting PrintWriter");
        }
    }

    private static BufferedReader getBufferedReader(Socket clientSocket) {
        try {
            return new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch (IOException e) {
            throw new RuntimeException("Error getting new BufferedReader for client socket");
        }
    }

    private static void closeClient(Socket clientSocket) {
        try {
            clientSocket.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Error closing client socket");
        }
    }

    private static void closeServer(ServerSocket serverSocket) {
        try {
            serverSocket.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Error closing server socket");
        }
    }
}
