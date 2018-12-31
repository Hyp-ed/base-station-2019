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

    public void sendMessage(int message) {
        try {
            Thread sendWorker = new Thread(new MessageSender(message));
            sendWorker.start();
        }
        catch (NullPointerException e) {
            System.out.println("Could not send message, client probably not running");
        }
    }

    private class MessageSender implements Runnable {
        private TestMessage.Builder msgBuilder;

        public MessageSender(int content) {
            msgBuilder = TestMessage.newBuilder().setData(content);
        }

        @Override
        public void run() {
            try {
                TestMessage msg = msgBuilder.build();
                msg.writeDelimitedTo(Server.this.client.getOutputStream());
                System.out.println("Sent \"" + msg.getData() + "\" to client");
            }
            catch (IOException e) {
                System.out.println("Error sending message to client");
            }
        }
    }

    private class MessageReader implements Runnable {
        private Logger logger = null;

        public MessageReader() {
            logger = Server.getLogger(Server.class.getName());
        }

        @Override
        public void run() {
            TestMessage msg;
            TestMessage.Command cmd = TestMessage.Command.ERROR; // default value
            int data = 0; // default value

            logger.info("******BEGIN******");

            while (true) {
                try {
                    msg = TestMessage.parseDelimitedFrom(Server.this.client.getInputStream());
                    cmd = msg.getCommand();
                    data = msg.getData();
                }
                catch (NullPointerException e) {
                    System.out.println("Exception: " + e);
                    System.out.println("Client probably disconnected");
                    System.exit(0);
                }
                catch (IOException e) {
                    System.out.println("Exception: " + e);
                    break;
                }

                switch (cmd) {
                    case VELOCITY:
                        Server.this.velocity.set(String.valueOf(data));
                        logger.info("VELOCITY: " + data);
                        break;
                    case ACCELERATION:
                        Server.this.acceleration.set(String.valueOf(data));
                        logger.info("ACCELERATION: " + data);
                        break;
                    case BRAKE_TEMP:
                        Server.this.brakeTemp.set(String.valueOf(data));
                        logger.info("BRAKE_TEMP: " + data);
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