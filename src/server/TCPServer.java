package server;

/**
 **/

import java.io.*;
import java.net.*;

class TCPServer {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Welcome Socket...");
        ServerSocket welcomeSocket = new ServerSocket(6789);

        while (true) {
            System.out.println("Creating New Server instance");
            Socket connectionSocket = welcomeSocket.accept();
            new ServerInstance(connectionSocket).start(); // starts a server instance for each client connection
        }
    }
}




