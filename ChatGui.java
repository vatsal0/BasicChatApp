package ChatApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class ChatGui extends Application {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    private Stage stage;
    private TextArea messageArea;
    private TextArea namesArea;
    private TextField textInput;
    private Button sendButton;
    private Button randomButton;
    
    String namesList = "";
    ServerListener socketListener;
    String serverAddress;
    int serverPort;

    private volatile String username = "";

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        List<String> args = getParameters().getUnnamed();
        if (args.size() == 2){
            serverAddress = args.get(0);
            serverPort = Integer.parseInt(args.get(1));
        } else {
            throw new Exception("Please enter ip and port");
        }


        this.stage = primaryStage;
        BorderPane borderPane = new BorderPane();

        messageArea = new TextArea();
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        borderPane.setCenter(messageArea);
        messageArea.setPrefWidth(600);

        namesArea = new TextArea();
        namesArea.setWrapText(true);
        namesArea.setEditable(false);
        borderPane.setRight(namesArea);
        namesArea.setPrefWidth(200);

        //At first, can't send messages - wait for WELCOME!
        textInput = new TextField();
        sendButton = new Button("Send");
        randomButton = new Button("Send Random");
        textInput.setOnAction(e -> sendMessage());
        sendButton.setOnAction(e -> sendMessage());
        randomButton.setOnAction(e -> sendRandomMessage());
        randomButton.setDisable(true);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(new Label("Message: "), textInput, sendButton, randomButton);
        HBox.setHgrow(textInput, Priority.ALWAYS);
        borderPane.setBottom(hbox);

        Scene scene = new Scene(borderPane, 800, 500);
        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();

        socketListener = new ServerListener();
        
        //Handle GUI closed event
        stage.setOnCloseRequest(e -> {
            socketListener.appRunning = false;
            try {
                out.writeObject(new ChatMessage(ChatMessage.HEADER_QUIT));
                socket.close(); 
            } catch (IOException ex) {}
        });

        new Thread(socketListener).start();
    }

    private void sendRandomMessage() {
        String line = textInput.getText().trim();
        if (line.length() == 0)
            return;
        textInput.clear();
        try { 
            ArrayList<String> names = new ArrayList<String>();
            names.add(".");
            out.writeObject(new ChatMessage(ChatMessage.HEADER_PCHAT, line, names));
        } catch (Exception ex) {ex.printStackTrace();}
    }

    private void sendMessage() {
        String line = textInput.getText().trim();
        if (line.length() == 0)
            return;
        textInput.clear();
        try { 
            if (socketListener.state == 1) {
                out.writeObject(new ChatMessage(ChatMessage.HEADER_NAME, line));
            } else if (socketListener.state == 2) {
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
                                message += contents[i] + " ";
                            }
                        } else {
                            message += contents[i] + " ";
                        }
                    }

                    if (message.length() > 0) {
                        out.writeObject(new ChatMessage(ChatMessage.HEADER_PCHAT, message, names));
                    }
                    Platform.runLater(() -> {
                        messageArea.appendText("You: " + line + "\n");
                    });
                } else if (line.toLowerCase().startsWith("/whoishere")) {
                    Platform.runLater(() -> {
                        messageArea.appendText("List of chat members:\n");
                        messageArea.appendText(namesList + "\n");
                    });
                } else {
                    out.writeObject(new ChatMessage(ChatMessage.HEADER_CHAT, line));
                    Platform.runLater(() -> {
                        messageArea.appendText("You: " + line + "\n");
                    });
                }
            }
        } catch (Exception ex) {ex.printStackTrace();}
    }

    class ServerListener implements Runnable {

        volatile boolean appRunning = false;
        public int state = 0; //0: off, 1: awaiting name verification 2: active

        public final String[] WelcomeMessages = {
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

        public void run() {
            try {
                // Set up the socket for the Gui
                socket = new Socket(serverAddress, serverPort);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                appRunning = true;
                //Ask the gui to show the username dialog and update username
                //Send to the server
                // Platform.runLater(() -> {
                //     try { out.writeObject(new ChatMessage(ChatMessage.HEADER_NAME, getName())); } catch (Exception ex) {ex.printStackTrace();}
                // });

                while (appRunning) {
                    ChatMessage incoming = (ChatMessage) in.readObject();
                    Platform.runLater(() -> {
                        if (incoming.getHeader() == ChatMessage.HEADER_SUBMIT) {
                            state = 1;
                            messageArea.appendText("Welcome to the chat! Please enter a username:\n");
                        } else if (incoming.getHeader() == ChatMessage.HEADER_RESUBMIT) {
                            messageArea.appendText("Invalid username! Must be alphanumeric and have at least one character. Enter username:\n");
                        } else if (incoming.getHeader() == ChatMessage.HEADER_DIFFERENT) {
                            messageArea.appendText("Username is already in use!. Enter another username:\n");
                        } else if (incoming.getHeader() == ChatMessage.HEADER_CONFIRM) {
                            state = 2;
                            messageArea.appendText("List of chat members:\n");
                            messageArea.appendText(namesList + "\n");
                            stage.setTitle("Chat - " + incoming.getText());
                            randomButton.setDisable(false);
                        } else if (incoming.getHeader() == ChatMessage.HEADER_WELCOME) {
                            String name = incoming.getText().trim();
                            messageArea.appendText(String.format(WelcomeMessages[rand.nextInt(WelcomeMessages.length)], name) + "\n");
                        } else if (incoming.getHeader() == ChatMessage.HEADER_EXIT) {
                            String name = incoming.getText().trim();
                            messageArea.appendText(String.format("%s has left the chat.", name) + "\n");
                        } else if (incoming.getHeader() == ChatMessage.HEADER_CHAT) {
                            String text = incoming.getText().trim();
                            String sender = incoming.getSender().trim();
                            Date date = new Date();  
                            messageArea.appendText(String.format("%s @ %s: %s", sender, formatter.format(date), text) + "\n");
                        } else if (incoming.getHeader() == ChatMessage.HEADER_PCHAT) {
                            String text = incoming.getText().trim();
                            String sender = incoming.getSender().trim();
                            Date date = new Date();  
                            messageArea.appendText(String.format("\u0007%s [privately] @ %s: %s", sender, formatter.format(date), text) + "\n");
                        } else if (incoming.getHeader() == ChatMessage.HEADER_NAMELIST) {
                            namesList = incoming.getText().trim();
                            namesArea.setText("Connected users:\n" + namesList);
                        } else {
                            messageArea.appendText("" + incoming.getHeader() + "\n");
                        }
                    });
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (Exception e) {
                if (appRunning)
                    e.printStackTrace();
            } 
            finally {
                Platform.runLater(() -> {
                    stage.close();
                });
                try {
                    if (socket != null)
                        socket.close();
                }
                catch (IOException e){
                }
            }
        }
    }
}