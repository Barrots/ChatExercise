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

public class ClientThread extends Thread {

    private Socket socket = null;
    private MainClient client = null;
    private DataInputStream streamIn = null;

    public ClientThread(MainClient client, Socket socket) {
        try {
            this.client = client;
            this.socket = socket;
            streamIn = new DataInputStream(socket.getInputStream());
            start();
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void run() {
        while (true) {
            try {
                client.receive(streamIn.readUTF());
            } catch (IOException ioe) {
                System.out.println("Listening error: " + ioe.getMessage());
                client.stop();
            }
        }
    }
}
