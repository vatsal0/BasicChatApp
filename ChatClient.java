package ChatApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ChatClient {
    private static Socket socket;
    private static ObjectInputStream socketIn;
    private static ObjectOutputStream out;
    
    public static void main(String[] args) throws Exception {
        Scanner userInput = new Scanner(System.in);
        
        System.out.println("What's the server IP? ");
        String serverip = "ec2-13-58-169-82.us-east-2.compute.amazonaws.com";//userInput.nextLine();
        System.out.println("What's the server port? ");
        int port = 59003;//userInput.nextInt();
        //userInput.nextLine();

        socket = new Socket(serverip, port);
        //socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //out = new PrintWriter(socket.getOutputStream(), true);
        out = new ObjectOutputStream(socket.getOutputStream());
        socketIn = new ObjectInputStream(socket.getInputStream());
        // start a thread to listen for server messages
        ServerListener listener = new ServerListener(socketIn);
        Thread t = new Thread(listener);
        t.start();

        String line = userInput.nextLine().trim();
        while(!line.toLowerCase().startsWith("/quit")) {
            if (listener.state == 1) {
                out.writeObject(new ChatMessage(ChatMessage.HEADER_NAME, line));
                line = userInput.nextLine().trim();
            } else if (listener.state == 2) {
                if (line.startsWith("@")) {
                    boolean readingMentions = true;
                    ArrayList<String> names = new ArrayList<String>();
                    String message = "";

                    String[] contents = line.split(" ");

                    for (int i = 0; i < contents.length; i++) {
                        if (readingMentions) {
                            if (contents[i].startsWith("@")) {
                                names.add(contents[i].substring(1));
                            } else {
                                readingMentions = false;
                                message += contents[i];
                            }
                        } else {
                            message += contents[i];
                        }
                    }

                    if (message.length() > 0) {
                        out.writeObject(new ChatMessage(ChatMessage.HEADER_PCHAT, message, names));
                        line = userInput.nextLine().trim();
                    }
                } else if (line.toLowerCase().startsWith("/whoishere")) {
                    System.out.println("List of chat members:");
                    System.out.println(ServerListener.namesList);
                    line = userInput.nextLine().trim();
                } else {
                    out.writeObject(new ChatMessage(ChatMessage.HEADER_CHAT, line));
                    line = userInput.nextLine().trim();
                }
            }
        }
        out.writeObject(new ChatMessage(ChatMessage.HEADER_QUIT));
        out.close();
        userInput.close();
        socketIn.close();
        socket.close();
        
    }
}
