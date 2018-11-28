package types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Message {
    private String command;
    private String data;

    public Message() {
        this.command = null;
        this.data = null;
    }

    // we set command to null when we just want to send messages without a command prepended
    // e.g. when we send from server to client no need for command header
    public Message(String d) {
        this.command = null;
        this.data = d;
    }

    public Message(String c, String d) {
        this.command = c;
        this.data = d;
    }

    public void read(BufferedReader in) throws RuntimeException {
        try {
            // Data should be sent with newspaces to delimit
            this.command = in.readLine();
            this.data = in.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading message data/command");
        }
    }

    public void send(PrintWriter out) {
        if (this.command != null) {
            out.println(this.command);
        }

        if (this.data != null) {
            out.println(this.data);
        }
    }

    public String getCommand() {
        return this.command;
    }

    public String getData() {
        return this.data;
    }
}
