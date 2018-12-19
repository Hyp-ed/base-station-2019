package server;

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
import protoTypes.MessageProtos.*;
import types.*;

public class Server implements Runnable {
    private static final int PORT = 9090;
    private Socket client = null;
    private SimpleStringProperty velocity = new SimpleStringProperty(this, "velocity", "0");
    private SimpleStringProperty acceleration = new SimpleStringProperty(this, "acceleration", "0");
    private SimpleStringProperty brakeTemp = new SimpleStringProperty(this, "brakeTemp", "25");

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
        // private PrintWriter out = null;
        // private Message msg;
        private TestMessage.Builder msg;

        public MessageSender(String msgContent) {
            // Neil neil = new Neil();
            // System.out.println("Swag: " + neil.getSwag());
            // System.out.println("Age: " + Neil.getAge());

            // out = getPrintWriter(Server.this.client);
            // msg = new Message(msgContent);
            msg = TestMessage.newBuilder().setData(123);
        }

        @Override
        public void run() {
            // msg.send(this.out);
            try {
                msg.build().writeDelimitedTo(Server.this.client.getOutputStream());
                System.out.println("Sent message to client");
            }
            catch (IOException e) {
                System.out.println("rip my g");
            }
            // System.out.println("Sent \"" + msg.getData() + "\" to client");
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
            logger = Server.getLogger(Server.class.getName());
            logger.info("******BEGIN******");

            while (true) {
                Message msg = new Message();
                msg.read(in);
                String rawCommand = msg.getCommand();
                String rawData = msg.getData();

                if (rawData == null || rawData.equals("END")) {
                    System.out.println("Client decided to end connection");
                    System.exit(0);
                }

                switch (rawCommand) {
                    case "1": // velocity
                        Server.this.velocity.set(rawData);
                        logger.info("VELOCITY: " + rawData);
                        break;
                    case "2": // acceleration
                        Server.this.acceleration.set(rawData);
                        logger.info("ACCELERATION: " + rawData);
                        break;
                    case "3": // brake temp
                        Server.this.brakeTemp.set(rawData);
                        logger.info("BRAKE_TEMP: " + rawData);
                        break;
                    default:
                        logger.info("ERROR: we should never reach this state");
                        throw new RuntimeException("UNREACHABLE");
                }
            }
        }
    }

    public SimpleStringProperty getVelocityProperty() {
        return this.velocity;
    }

    public SimpleStringProperty getAccelerationProperty() {
        return this.acceleration;
    }

    public SimpleStringProperty getBrakeTempProperty() {
        return this.brakeTemp;
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

    private static Logger getLogger(String name) {
        try {
            Logger logger = Logger.getLogger(name);
            FileHandler fh = new FileHandler(System.getProperty("user.dir") + "/temp/server_log.log"); // make sure temp dir exists in current dir before running
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
            return logger;
        }
        catch (IOException e) {
            System.out.println("Error creating new logger");
            return null;
        }
    }
}
