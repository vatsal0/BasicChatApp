package ChatApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerListener implements Runnable {
    private static BufferedReader socketIn;

    public ServerListener(BufferedReader socketIn) {
        this.socketIn = socketIn;
    }

    @Override
    public void run() {
        try {
            String incoming = "";

            while( (incoming = socketIn.readLine()) != null) {
                //handle different headers
                //WELCOME
                //CHAT
                //EXIT
                System.out.println(incoming);
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally{
            System.out.println("Client Listener exiting");
        }
    }
}