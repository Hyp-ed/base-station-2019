package server;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import telemetrydata.TelemetryData.*;

public class Server implements Runnable {
    private static final int PORT = 9090;
    private static final int SPACE_X_PORT = 3000;
    private static final String SPACE_X_IP = "localhost"; // change to actual ip

    private Socket client; // TCP socket to pod
    private DatagramSocket spaceXSocket; // UDP socket to SpaceX
    private InetAddress spaceXAddress;

    private TestMessage msg;
    private boolean connected = false;

    public Server() {
        try {
            spaceXSocket = new DatagramSocket();
            spaceXAddress = InetAddress.getByName(SPACE_X_IP);
            System.out.println("SPACEX ADDRESS: " + spaceXAddress);
        }
        catch (SocketException e) {
            System.out.println("SpaceX socket initialization failed");
        }
        catch (UnknownHostException e) {
            System.out.println("Couldn't resolve SpaceX hostname");
        }
    }

    @Override
    public void run() {
        ServerSocket listener = getServerSocket(PORT);
        System.out.println("Server now listening on port " + PORT);
        System.out.println("Waiting to connect to client...");

        try {
            client = getClientServerFromListener(listener);
            connected = true;
            System.out.println("Connected to client");

            Thread readWorker = new Thread(new MessageReader());
            Thread udpWorker = new Thread(new SpaceXSender());

            readWorker.start();
            udpWorker.start();

            try {
                readWorker.join();
                udpWorker.join();
            }
            catch (InterruptedException e) {
                System.out.println("Problem joining readWorker/udpWorker threads");
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
            sendWorker.join();
        }
        catch (InterruptedException e) {
            System.out.println("Problem joining sendWorker thread");
        }
        catch (NullPointerException e) {
            System.out.println("Could not send message, client probably not running");
        }
    }

    private class MessageSender implements Runnable {
        private TestMessage.Builder msgBuilder;

        public MessageSender(int content) {
            switch (content) {
                case 4:
                    msgBuilder = TestMessage.newBuilder().setCommand(TestMessage.Command.FINISH);
                    break;
                case 5:
                    msgBuilder = TestMessage.newBuilder().setCommand(TestMessage.Command.EM_STOP);
                    break;
                // IMPLEMENT DEFAULT CASE, honestly idk what to do here since we can't "cancel" this runnable from here
            }
        }

        @Override
        public void run() {
            try {
                TestMessage msg = msgBuilder.build();
                msg.writeDelimitedTo(Server.this.client.getOutputStream());
                System.out.println("Sent \"" + msg.getCommand() + "\" to client");
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
                    System.out.println("Client probably disconnected");
                    connected = false;
                    System.exit(0);
                }
                catch (IOException e) {
                    System.out.println("Exception: " + e);
                    break;
                }

                switch (cmd) {
                    case VELOCITY:
                        logger.info("VELOCITY: " + data);
                        break;
                    case ACCELERATION:
                        logger.info("ACCELERATION: " + data);
                        break;
                    case BRAKE_TEMP:
                        logger.info("BRAKE_TEMP: " + data);
                        break;
                    default:
                        logger.info("ERROR: we should never reach this state");
                        throw new RuntimeException("UNREACHABLE");
                }
            }
        }
    }

    private class SpaceXSender implements Runnable {
        private ByteBuffer buffer;

        public SpaceXSender() {
            buffer = ByteBuffer.allocate(34); // 34 bytes as specified by SpaceX
        }

        @Override
        public void run() {
            while (connected) { // need to make sure packets sent between 10Hz and 50Hz
                byte teamID = 2;
                byte status = 1;

                buffer.put(teamID);
                buffer.put(status);
                buffer.putInt(2);
                buffer.putInt(1);
                buffer.putInt(2);
                buffer.putInt(1);
                buffer.putInt(2);
                buffer.putInt(1);
                buffer.putInt(2);
                buffer.putInt(1);

                byte[] bufferArray = buffer.array();
                DatagramPacket packet = new DatagramPacket(bufferArray, bufferArray.length, spaceXAddress, SPACE_X_PORT);

                try {
                    spaceXSocket.send(packet);
                }
                catch (IOException e) {
                    System.out.println("Failure sending to SpaceX socket");
                }

                buffer.clear();
            }
        }
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

    public String getCmd() {
        return String.valueOf(msg.getCommand());
    }

    public int getData() {
        return msg.getData();
    }

    public boolean isConnected() {
        return connected;
    }
}
