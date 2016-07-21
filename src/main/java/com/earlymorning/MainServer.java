/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.earlymorning;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dario.barrotta
 */
import java.net.*;
import java.io.*;

/*
 Class that implements the server.
 Start the ServerSocket and MainServer himself as a Thread that has to accept every clients trying to connect to him adding them into a list of ServerThread.
 Adding a client will start a ServerThread that open the streams with that single client.
 ServerThread than accept everything the client send to him and pass the input to the MainServer to handle it.
 The MainServer manage the input and use the logic of a chat to send (throght ServerThread) a broadcast msg or single ones 

 */
public class MainServer implements Runnable {

    private ServerThread[] clients = new ServerThread[20];
    private ServerSocket server = null;
    private Thread thread = null;
    private int numClient = 0;

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
        MainServer server = new MainServer(5555);
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
        String user = "Unknown";
        if (findClient(ID) != -1 && clients[findClient(ID)].getUsername() != "") { // Set Username in the method
            user = clients[findClient(ID)].getUsername();
        }
        if (".bye".equals(input)) {                                                // Manage the disconnection of a Client
            for (int i = 0; i < numClient; i++) {
                if (i != findClient(ID)) {
                    clients[i].send(user + " has disconnected");
                }
            }
            clients[findClient(ID)].send(".bye");
            remove(ID);

        } else if (input.startsWith("/")) {                                       // Manage the particular action of clients
            if (input.contains("/username= ")) {                                  // Such as input the username
                user = input.replace("/username= ", "");
                clients[findClient(ID)].setUsername(user);
                String users = "";
                for (int i = 0; i < numClient - 1; i++) {
                    clients[i].send("The new user: " + user + " just joined the Chat!");
                    users += clients[i].getUsername() + ", ";
                }
                clients[findClient(ID)].send("Server: Hi " + user + "! For private msg use /nameOfTheReceiver msgToSend");
                if (numClient > 1) {
                    clients[findClient(ID)].send("The other connected user are: " + users.substring(0, users.length() - 2));
                }
            } else {                                                               // Send a private msg to only 1 other user
                String receiver = input.substring(1, input.indexOf(' '));
                input = input.substring(input.indexOf(' '), input.length());
                clients[findClient(receiver)].send("/" + user + ":" + input);
            }
        } else {                                                                   // Send a broadcast msg from client to all clients
            for (int i = 0; i < numClient; i++) {
                clients[i].send(user + ": " + input);
            }
        }
    }

    public synchronized void remove(int ID) {
            int pos = findClient(ID);
            if (pos >= 0) {
                System.out.println("Removing client thread " + ID);
                if (pos < numClient - 1) {
                    for (int i = pos + 1; i < numClient; i++) {
                        clients[i - 1] = clients[i];
                    }
                }
                numClient--;
                try {
                    clients[pos].close();
                } catch (IOException ioe) {
                    System.out.println("Error closing thread: " + ioe);
                }
                clients[pos].stop();
            }
    }

    private void addClient(Socket socket) {
        clients[numClient] = new ServerThread(this, socket);
        clients[numClient].start();
        numClient++;
    }

    private int findClient(int ID) {
        for (int i = 0; i < numClient; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    private int findClient(String user) {
        for (int i = 0; i < numClient; i++) {
            if (clients[i].getUsername().equals(user)) {
                return i;
            }
        }
        return -1;
    }
}
