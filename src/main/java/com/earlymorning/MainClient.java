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
/*
Class that implements one client ( that will be called multiple times).
MainClient connect to the server, create a stream of data and start ClientThread.
ClientThread will wait for every message from the socket and re-send them to this class.
Every time ClientThread send a msg to MainClient it is shown in the console.
MainClient (started as a Thread) will send throught the socket all the messages written in the console.

*/

public class MainClient implements Runnable { 

    private Socket socket = null;
    private Thread thread = null;
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;
    private ClientThread client = null;
    private boolean firstRun = true;

    public MainClient(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            console = new DataInputStream(System.in);
            streamOut = new DataOutputStream(socket.getOutputStream());
            if (thread == null) {
                client = new ClientThread(this, socket);
                thread = new Thread(this);
                firstRun = true;
                thread.start();
            }
        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    public static void main(String args[]) {
        MainClient client = new MainClient("localhost", 5555);
    }
    
    @Override
    public void run() {
        while (thread != null) {
            try {
                if (firstRun) {  // Manage the first time a client connect and the setting of the username
                    System.out.println("Insert your Username");
                    streamOut.writeUTF("/username= " + console.readLine());
                    firstRun = false;
                } else {
                    streamOut.writeUTF(console.readLine());
                    streamOut.flush();
                }
            } catch (IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
        }
    }

    public void receive(String msg) {
        if (".bye".equals(msg)) {    // Handle the exit of a client
            System.out.println("Good bye. ");
            stop();
        } else {
            System.out.println(msg);
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
                console.close();
                streamOut.close();
                socket.close();
        } catch (IOException ioe) {
            System.out.println("Error closing ...");
        }
        client.stop();
    }

}
