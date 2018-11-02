import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 9090;

    public static void main(String[] args) {
        ServerSocket listener = getServerSocket(PORT);
        System.out.println("Server now listening on port " + PORT);
        System.out.println("Waiting to connect to client...");

        try {
            Socket client = getClientServerFromListener(listener);
            System.out.println("Connected to client");

            // System.out.print("Enter message to be sent to client: ");
            // out.println(consoleIn.readLine());
            Thread readWorker = new Thread(new MessageReader(client));
            readWorker.start();
            Thread sendWorker = new Thread(new MessageSender(client));
            sendWorker.start();

            try {
                readWorker.join();
                sendWorker.join();
            }
            catch (java.lang.InterruptedException e) {
                System.out.println("idk man");
            }

            closeClient(client);
        }
        finally  {
            closeServer(listener);
        }
    }

    private static class MessageSender implements Runnable {
        private PrintWriter out = null;
        private BufferedReader consoleIn = null;

        public MessageSender(Socket client) {
            out = getPrintWriter(client);
            consoleIn = getBufferedReader();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    System.out.println("Enter <1> to send message to client");
                    String userInput = consoleIn.readLine();
                    if (userInput.equals("1")) {
                        out.println("TEST MESSAGE FROM SERVER!!!!!!!");
                        System.out.println("Sent message to client");
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Something went wrong");
            }
        }
    }

    private static class MessageReader implements Runnable {
        private BufferedReader in = null;

        public MessageReader(Socket client) {
            in = getBufferedReader(client);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals(".")) {
                        System.out.println("Client decided to end connection");
                        break;
                    }

                    // System.out.println("FROM CLIENT: " + input);
                }
            }
            catch (IOException e) {
                System.out.println("Something went wrong");
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

    private static BufferedReader getBufferedReader() {
        return new BufferedReader(new InputStreamReader(System.in));
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
