package ChatApp;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatMessage implements Serializable {
    public static final long serialVersionUID = 4L; //🔪

    public static final int HEADER_SUBMIT = 0;
    public static final int HEADER_RESUBMIT = 1;
    public static final int HEADER_DIFFERENT = 2;
    public static final int HEADER_CONFIRM = 3;
    public static final int HEADER_NAME = 4;
    public static final int HEADER_WELCOME = 5;
    public static final int HEADER_EXIT = 6;
    public static final int HEADER_CHAT = 7;
    public static final int HEADER_PCHAT = 8;
    public static final int HEADER_QUIT = 9;

    private int header;
    private String sender;
    private String text;
    private ArrayList<String> recipients;

    public ChatMessage(int header) {
        this.header = header;
    }

    public ChatMessage(int header, String text) {
        this.header = header;
        this.text = text;
    }

    public ChatMessage(int header, String text, ArrayList<String> recipients) {
        this.header = header;
        this.text = text;
        this.recipients = recipients;
    }

    public ChatMessage(int header, String text, String sender) {
        this.header = header;
        this.text = text;
        this.sender = sender;
    }

    public int getHeader() {
        return header;
    }

    public String getText() {
        return text;
    }

    public ArrayList<String> getRecipients() {
        return recipients;
    }

    public String getSender() {
        return sender;
    }

    public void setHeader(int header) {
        this.header = header;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setRecipients(ArrayList<String> recipients) {
        this.recipients = recipients;
    }

    public void setSender(String sender) {
        this.sender = sender;
    } 
}