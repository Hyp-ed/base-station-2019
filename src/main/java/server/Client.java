package server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        System.out.println("Waiting to connect to server...");
        Socket socket = getSocket();
        System.out.println("Connected to server");
        PrintWriter out = getPrintWriter(socket);

        Thread readWorker = new Thread(new MessageReader(socket));
        readWorker.start();

        for (int x = 0; x < 100000000; x++) {
            out.println("1"); // velocity
            out.println("123");

            out.println("2"); // acceleration
            out.println("234");

            out.println("3"); // brake temp
            out.println("345");

            out.println("1"); // velocity
            out.println("124");

            out.println("2"); // acceleration
            out.println("235");

            out.println("3"); // brake temp
            out.println("346");
        }

        out.println("UNUSED COMMAND"); // doesn't matter, as we're sending END below anyways
        out.println("END");

        try {
            readWorker.join();
            out.close();
            socket.close();
        }
        catch (IOException e) {
            System.out.println("Error closing PrintWriter/socket");
        }
        catch (InterruptedException e) {
            System.out.println("Error joing thread");
        }
    }

    private static class MessageReader implements Runnable {
        private BufferedReader in = null;

        public MessageReader(Socket s) {
            in = getBufferedReader(s);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        System.out.println("Server decided to end connection");
                        System.exit(0); // terminate program
                    }
                    else {
                        System.out.println("FROM SERVER: " + input);
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Something went wrong reading message");
            }
        }
    }

    private static Socket getSocket() {
        try {
            return new Socket("localhost", 9090);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed getting client socket");
        }
    }

    private static PrintWriter getPrintWriter(Socket s) {
        try {
            return new PrintWriter(s.getOutputStream(), true);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed getting PrintWriter");
        }
    }

    private static BufferedReader getBufferedReader(Socket s) {
        try {
            return new BufferedReader(new InputStreamReader(s.getInputStream()));
        }
        catch (IOException e) {
            throw new RuntimeException("Error getting new BufferedReader");
        }
    }
}
