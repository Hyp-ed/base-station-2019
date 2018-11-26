package types;

import java.io.BufferedReader;
import java.io.IOException;

public class Message {
    public String command;
    public String data;

    public Message(String c, String d) {
        this.command = c;
        this.data = d;
    }

    public Message(BufferedReader in) {
        try {
            // Data should be sent with newspaces to delimit
            this.command = in.readLine();
            this.data = in.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading message data/command");
        }
    }
}