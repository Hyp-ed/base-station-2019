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
        BufferedReader in = getBufferedReader(socket);
        PrintWriter out = getPrintWriter(socket);

        System.out.println(getMessageFromServer(in));

        for (int x = 0; x < 2; x++) {
            out.println("CMD01"+Integer.toString(x*2));
            out.println("CMD02"+Integer.toString(x));
            out.println("CMD03-2");
            out.println("CMD04"+Integer.toString(x));
            out.println("CMD05"+Integer.toString(x));
            out.println("CMD06"+Integer.toString(x));
            out.println("CMD07"+Integer.toString(x));
            out.println("CMD0803");
            out.println("CMD091111");
            out.println("CMD1011111111");
            out.println("CMD1111111111");
            out.println("CMD1211");
            out.println("CMD13"+Integer.toString(x));
            out.println("CMD14"+Integer.toString(x));
            out.println("CMD15"+Integer.toString(x));
            out.println("CMD16"+Integer.toString(x));
            out.println("CMD17"+Integer.toString(x));
            out.println("CMD18"+Integer.toString(x));
            out.println("CMD19"+Integer.toString(x));
            out.println("CMD20"+Integer.toString(x));
            out.println("CMD21"+Integer.toString(x));
            out.println("CMD22"+Integer.toString(x));
            out.println("CMD23"+Integer.toString(x));
            out.println("CMD24"+Integer.toString(x));
            out.println("CMD25"+Integer.toString(x));
            out.println("CMD26"+Integer.toString(x));
            out.println("CMD27"+Integer.toString(x));
            out.println("CMD28"+Integer.toString(x));
            out.println("CMD29"+Integer.toString(x));
            out.println("CMD30"+Integer.toString(x));
        }

        out.println("."); //end connection

        try {
            out.close();
            socket.close();
        }
        catch (IOException e) {
            System.out.println("Error closing PrintWriter/socket");
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

    // idk why we need to catch an exception when reading
    // in server file we can read without having to do error handling
    private static String getMessageFromServer(BufferedReader b) {
        try {
            return "FROM SERVER: " + b.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading message from server");
        }
    }
}
