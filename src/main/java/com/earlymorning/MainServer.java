/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.earlymorning;

/**
 *
 * @author dario.barrotta
 */
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 Class that implements the server.
 Start the ServerSocket and MainServer himself as a Thread that has to accept every clients trying to connect to him adding them into a list of ServerThread.
 Adding a client will start a ServerThread that open the streams with that single client.
 ServerThread than accept everything the client send to him and pass the input to the MainServer to handle it.
 The MainServer manage the input and use the logic of a chat to send (through ServerThread) a broadcast msg or single ones

 */
public class MainServer implements Runnable {

    private List<ServerThread> clients = new ArrayList<>();
    private ServerSocket server = null;
    private Thread thread = null;
    public static final int port = 5555;

    public MainServer(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            thread = new Thread(this);
            thread.start();
        } catch (IOException ioe) {
            System.out.println("Error on Server starting");
        }
    }

    public static void main(String[] args) {
        MainServer server = new MainServer(port);
    }
    
    @Override
    public void run() {
        while (thread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addClient(server.accept());
            } catch (IOException ioe) {
                System.out.println("Server accept error: " + ioe);
                thread.stop();
                thread = null;
            }
        }
    }

    public synchronized void handle(int ID, String input) {

        int positionInTheList = findClient(ID);
        ServerThread clientToHandle = clients.get(positionInTheList);
        String user= "Unknown";
        if (usernameHasBeenSet(clientToHandle)){
             user = clientToHandle.getUsername();
        }

        if (".bye".equals(input)) {                                                // Manage the disconnection of a Client
            for (int i = 0; i < clients.size(); i++) {
                if (i != positionInTheList) {
                    clients.get(i).send(user + " has disconnected");
                }
            }
            clientToHandle.send(".bye");
            remove(ID);

        } else if (input.startsWith("/")) {                                       // Manage the particular action of clients
            if (input.contains("/username= ")) {                                  // Such as input the username
                user = input.replace("/username= ", "");
                clientToHandle.setUsername(user);
                List<String> connectedUsers = new ArrayList<>();

                for (ServerThread cl: clients){
                    if(!cl.equals(clientToHandle)){
                        cl.send("The new user: " + user + " just joined the Chat!");
                        connectedUsers.add(cl.getUsername());
                    }
                }
                clientToHandle.send("Server: Hi " + user + "! For private msg use /nameOfTheReceiver msgToSend ; type .bye for exit");

                if (connectedUsers.size() > 1) {
                    clientToHandle.send("The other connected user are: " + Arrays.toString(connectedUsers.toArray()));
                }
            } else {                                                               // Send a private msg to only 1 other user
                String receiver = input.substring(1, input.indexOf(' '));
                input = input.substring(input.indexOf(' '), input.length());
                clients.get(findClient(receiver)).send("/" + user + ":" + input);
            }
        } else {                                                                   // Send a broadcast msg from client to all clients
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).send(user + ": " + input);
            }
        }
    }

    public synchronized void remove(int ID) {
            int pos = findClient(ID);
            ServerThread clientToRemove = clients.get(pos);
            if (pos >= 0) {
                System.out.println("Removing client thread " + ID);

                try {
                    clientToRemove.close();
                } catch (IOException ioe) {
                    System.out.println("Error closing thread: " + ioe);
                }
                clients.remove(clientToRemove);
                clientToRemove.stop();

            }
    }

    private boolean usernameHasBeenSet(ServerThread sThread){

        return !("").equals(sThread.getUsername());
    }

    private void addClient(Socket socket) {
        clients.add(new ServerThread(this, socket)) ;
        clients.get(clients.size()-1).start();
    }

    private int findClient(int ID) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    private int findClient(String user) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getUsername().equals(user)) {
                return i;
            }
        }
        return -1;
    }
}
