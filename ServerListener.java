import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Random;
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ServerListener implements Runnable {
    private static ObjectInputStream socketIn;
    public static int state = 0; //0: off, 1: awaiting name verification 2: active
    public static String namesList = "";

    private static final String[] WelcomeMessages = {
        "%s just joined the server - glhf!",
        "%s just joined. Everyone, look busy!",
        "%s just joined. Can I get a heal?",
        "%s joined your party.",
        "%s joined. You must construct additional pylons.",
        "Ermagherd. %s is here.",
        "Welcome, %s. Stay awhile and listen.",
        "Welcome, %s. We were expecting you ( ͡° ͜ʖ ͡°)",
        "Welcome, %s. We hope you brought pizza.",
        "Welcome %s. Leave your weapons by the door.",
        "A wild %s appeared.",
        "Swoooosh. %s just landed.",
        "Brace yourselves. %s just joined the server.",
        "%s just joined. Hide your bananas.",
        "%s just arrived. Seems OP - please nerf.",
        "%s just slid into the server.",
        "A %s has spawned in the server.",
        "Big %s showed up!",
        "Where's %s? In the server!",
        "%s hopped into the server. Kangaroo!!",
        "%s just showed up. Hold my beer.",
        "Challenger approaching - %s has appeared!",
        "It's a bird! It's a plane! Nevermind, it's just %s.",
        "It's %s! Praise the sun! [T]/",
        "Never gonna give %s up. Never gonna let %s down.",
        "Ha! %s has joined! You activated my trap card!",
        "Cheers, love! %s's here!",
        "Hey! Listen! %s has joined!",
        "We've been expecting you %s",
        "It's dangerous to go alone, take %s!",
        "%s has joined the server! It's super effective!",
        "Cheers, love! %s is here!",
        "%s is here, as the prophecy foretold.",
        "%s has arrived. Party's over.",
        "Ready player %s",
        "%s is here to kick butt and chew bubblegum. And %s is all out of gum.",
        "Hello. Is it %s you're looking for?",
        "%s has joined. Stay a while and listen!",
        "Roses are red, violets are blue, %s joined this server with you"
    };
    private Random rand = new Random();
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");  

    public ServerListener(ObjectInputStream socketIn) {
        this.socketIn = socketIn;
    }

    @Override
    public void run() {
        try {
            ChatMessage incoming;

            while(true) {
                incoming = (ChatMessage) socketIn.readObject();
                if (incoming.getHeader() == ChatMessage.HEADER_SUBMIT) {
                    state = 1;
                    System.out.println("Welcome to the chat! Please enter a username:");
                } else if (incoming.getHeader() == ChatMessage.HEADER_RESUBMIT) {
                    System.out.println("Invalid username! Must be alphanumeric and have at least one character. Enter username:");
                } else if (incoming.getHeader() == ChatMessage.HEADER_DIFFERENT) {
                    System.out.println("Username is already in use!. Enter another username:");
                } else if (incoming.getHeader() == ChatMessage.HEADER_CONFIRM) {
                    state = 2;
                    System.out.println("List of chat members:");
                    System.out.println(ServerListener.namesList);
                } else if (incoming.getHeader() == ChatMessage.HEADER_WELCOME) {
                    String name = incoming.getText().trim();
                    System.out.println(String.format(WelcomeMessages[rand.nextInt(WelcomeMessages.length)], name));
                } else if (incoming.getHeader() == ChatMessage.HEADER_EXIT) {
                    String name = incoming.getText().trim();
                    System.out.println(String.format("%s has left the chat.", name));
                } else if (incoming.getHeader() == ChatMessage.HEADER_CHAT) {
                    String text = incoming.getText().trim();
                    String sender = incoming.getSender().trim();
                    Date date = new Date();  
                    System.out.println(String.format("%s @ %s: %s", sender, formatter.format(date), text));
                } else if (incoming.getHeader() == ChatMessage.HEADER_PCHAT) {
                    String text = incoming.getText().trim();
                    String sender = incoming.getSender().trim();
                    Date date = new Date();  
                    System.out.println(String.format("\u0007%s [privately] @ %s: %s", sender, formatter.format(date), text));
                } else if (incoming.getHeader() == ChatMessage.HEADER_NAMELIST) {
                    namesList = incoming.getText().trim();
                } else {
                    System.out.println(incoming);
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally{
            System.out.println("Client Listener exiting");
        }
    }
}