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

public class ClientHandler implements Runnable {
    // Maintain data about the client serviced by this thread
    ClientConnectionData client;
    public static final ArrayList<ClientConnectionData> clientList = new ArrayList<>();

    public ClientHandler(ClientConnectionData client) {
        this.client = client;
    }

    /**
     * Broadcasts a message to all clients connected to the server.
     */
    public void broadcast(String msg) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    c.getOut().println(msg);
                    // c.getOut().flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void broadcastOne(String msg, String name) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getName() == name) c.getOut().println(msg);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void broadcastAllButOne(String msg, String name) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getName() != name) c.getOut().println(msg);
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
            BufferedReader in = client.getInput();
            String incoming = "";

            broadcastOne("SUBMITNAME", client.getName());
            boolean validated = false;
            String username = "";

            while (!validated && (incoming = in.readLine()) != null) {
                if (incoming.startsWith("NAME")) {
                    String name = incoming.substring(4).trim();
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
                    broadcastOne("RESUBMITNAME", client.getName());
                } else {
                    for (ClientConnectionData c : clientList) {
                        System.out.println(c.getUserName());
                        validated = validated && !username.equals(c.getUserName());
                    }
                    if (!validated) broadcastOne("DIFFERENTNAME", client.getName());
                }
            }

            client.setUserName(username);
            broadcastOne("CONFIRMNAME", client.getName());
            //notify all that client has joined
            broadcast(String.format("WELCOME %s", client.getUserName()));

            incoming = "";

            while( (incoming = in.readLine()) != null) {
                if (incoming.startsWith("CHAT")) {
                    String chat = incoming.substring(4).trim();
                    if (chat.length() > 0) {
                        String msg = String.format("CHAT %s %s", client.getUserName(), chat);
                        broadcast(msg);    
                    }
                } else if (incoming.startsWith("QUIT")){
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
            broadcast(String.format("EXIT %s", client.getUserName()));
            try {
                client.getSocket().close();
            } catch (IOException ex) {}

        }
    }
    
}