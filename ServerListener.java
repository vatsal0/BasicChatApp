package ChatApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerListener implements Runnable {
    private static BufferedReader socketIn;
    public static int state = 0; //0: off, 1: awaiting name verification 2: active

    public ServerListener(BufferedReader socketIn) {
        this.socketIn = socketIn;
    }

    @Override
    public void run() {
        try {
            String incoming = "";

            while( (incoming = socketIn.readLine()) != null) {
                if (incoming.startsWith("SUBMITNAME")) {
                    state = 1;
                    System.out.println("Welcome to the chat! Please enter a username:");
                } else if (incoming.startsWith("RESUBMITNAME")) {
                    System.out.println("Invalid username! Must be alphanumeric and have at least one character. Enter username:");
                } else {
                    System.out.println(incoming);
                }
                //handle different headers
                //WELCOME
                //CHAT
                //EXIT
                
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally{
            System.out.println("Client Listener exiting");
        }
    }
}