import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientHandler implements Runnable {
    // Maintain data about the client serviced by this thread
    ClientConnectionData client;
    public static final ArrayList<ClientConnectionData> clientList = new ArrayList<>();
    private Random rand = new Random();

    public ClientHandler(ClientConnectionData client) {
        this.client = client;
    }

    public void broadcast(ChatMessage msg) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    c.getOut().writeObject(msg);
                    // c.getOut().flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void broadcastOne(ChatMessage msg, String name) {
        try {
            System.out.println(name + "Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getName() == name) c.getOut().writeObject(msg);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void broadcastToUsername(ChatMessage msg, String name) {
        try {
            System.out.println("Broadcasting -- " + msg);
            System.out.println(msg.getText());
            System.out.println(name);
            synchronized (clientList) {
                if (name.equals(".")) {
                    (clientList.get(rand.nextInt(clientList.size()))).getOut().writeObject(msg);
                } else {
                    for (ClientConnectionData c : clientList){
                        if (c.getUserName().equals(name)) c.getOut().writeObject(msg);
                    }
                }
                
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void broadcastExceptUsername(ChatMessage msg, String name) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (!c.getUserName().equals(name)) c.getOut().writeObject(msg);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = client.getInput();
            ChatMessage incoming;

            broadcastOne(new ChatMessage(ChatMessage.HEADER_SUBMIT), client.getName());
            boolean validated = false;
            String username = "";

            while (!validated) {
                incoming = (ChatMessage) in.readObject();
                if (incoming.getHeader() == ChatMessage.HEADER_NAME) {
                    String name = incoming.getText();
                    if (name.length() > 0) {
                        boolean alphanumeric = true;
                        for (char c : name.toCharArray()) {
                            alphanumeric = alphanumeric && ((c >= 48 && c <= 57) || (c >= 65 && c <= 90) || (c >= 97 && c <= 122));
                        }
                        if (alphanumeric) {
                            validated = true;
                            username = name;
                        }
                    }
                }
                if (!validated) {
                    broadcastOne(new ChatMessage(ChatMessage.HEADER_RESUBMIT), client.getName());
                } else {
                    for (ClientConnectionData c : clientList) {
                        System.out.println(c.getUserName());
                        validated = validated && !username.equals(c.getUserName());
                    }
                    if (!validated) broadcastOne(new ChatMessage(ChatMessage.HEADER_DIFFERENT), client.getName());
                }
            }

            client.setUserName(username);
            String namesList = "";
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getUserName() != null) namesList += c.getUserName() + "\n";
                }
            }
            broadcast(new ChatMessage(ChatMessage.HEADER_NAMELIST, namesList));
            broadcastOne(new ChatMessage(ChatMessage.HEADER_CONFIRM, username), client.getName());
            //notify all that client has joined
            broadcast(new ChatMessage(ChatMessage.HEADER_WELCOME, client.getUserName()));

            while(true) {
                incoming = (ChatMessage) in.readObject();
                if (incoming.getHeader() == ChatMessage.HEADER_CHAT) {
                    String chat = incoming.getText();
                    if (chat.length() > 0) {
                        broadcastExceptUsername(
                            new ChatMessage(ChatMessage.HEADER_CHAT, chat, client.getUserName()), 
                            client.getUserName());    
                    }
                } else if (incoming.getHeader() == ChatMessage.HEADER_PCHAT) {
                    String chat = incoming.getText();
                    ArrayList<String> recipients = incoming.getRecipients();

                    if (chat.length() > 0) {
                        for (String recipient : recipients) {
                            if (client.getUserName().equals(recipient)) continue;
                            broadcastToUsername(
                                new ChatMessage(ChatMessage.HEADER_PCHAT, chat, client.getUserName()),
                                recipient);
                        }
                    }
                } else if (incoming.getHeader() == ChatMessage.HEADER_QUIT) {
                    break;
                }
            }
        } catch (Exception ex) {
            if (ex instanceof SocketException) {
                System.out.println("Caught socket ex for " + 
                    client.getName());
            } else {
                System.out.println(ex);
                ex.printStackTrace();
            }
        } finally {
            //Remove client from clientList, notify all
            synchronized (clientList) {
                clientList.remove(client); 
            }
            System.out.println(client.getName() + " has left.");
            broadcast(new ChatMessage(ChatMessage.HEADER_EXIT, client.getUserName()));  
            String namesList = "";
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getUserName() != null) namesList += c.getUserName() + "\n";
                }
            }
            broadcast(new ChatMessage(ChatMessage.HEADER_NAMELIST, namesList));
            try {
                client.getSocket().close();
            } catch (IOException ex) {}

        }
    }
    
}