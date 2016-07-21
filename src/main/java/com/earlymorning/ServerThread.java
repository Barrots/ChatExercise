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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {

    private MainServer server = null;
    private Socket socket = null;
    private int ID = -1;
    private String username = "";
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;

    public ServerThread(MainServer server, Socket socket) {
        super();
        
            this.server = server;
            this.socket = socket;
            this.ID = socket.getPort();
        try {
            streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void run() {
        System.out.println("ServerThread running. Client: " + ID);
        while (socket.isConnected()) {
            try {
                server.handle(ID, streamIn.readUTF());
            } catch (IOException ioe) {
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }
    
    public void send(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.out.println(ID + " ERROR sending: " + ioe.getMessage());
            server.remove(ID);
            stop();
        }
    }

    public void close() throws IOException {
            socket.close();
            streamIn.close();
            streamOut.close();
    }
    
    
    public int getID() {
        return ID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
