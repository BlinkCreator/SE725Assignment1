package server;

import java.io.*;
import java.net.Socket;

public class ServerInstance extends Thread{
    protected Socket socket;

    // Different flags for continuing with different activities
    boolean run = true;

    String cmd;

    //Data Streams for ASCII and Binary
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    DataInputStream binFromClient;
    DataOutputStream binToClient;

    // Variables for admin usage
    long mbSent = 0;

    ServerInstance(Socket socket) throws IOException {
        this.socket = socket;

        try {
            socket.setReuseAddress(true);
            this.binToClient = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            this.binFromClient = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            this.inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.outToClient = new DataOutputStream(socket.getOutputStream());

            System.out.println("Server instance created...");
            sendToClient("Server Connected...");
            System.out.println("FROM Client: "+ readFromClient());
        } catch (Exception e) {
            e.printStackTrace();
            socket.close();
            run = false;
        }
    }
    // USER=1, ACCT=2, PASS=3, TYPE=4, LIST=5, CDIR=6, KILL=7, NAME=8, DONE=9, RETR=10, STOR=11
    @Override
    public void run() {
        while (run) {
            try {
                String[] cmd = readFromClient().split(" ");
                if(cmd[0] == null) {
                    cmd[0] = "ERROR: NULL";
                }
                    switch (cmd[0]){
                        case "DONE":
                            done();
                            break;
                        case "ACCT":
                            //auth("ACCT",commandArgs);
                            break;
                        case "PASS":
                            // auth("PASS",commandArgs);
                            break;
                        case "TYPE":
                            type(cmd[1]);
                            break;
                        case "LIST":
                            // list(commandArgs);
                            break;
                        case "CDIR":
                            // cdir(commandArgs);
                            break;
                        case "KILL":
                            // kill(commandArgs);
                            break;
                        case "name":
                            //name
                            break;
                        case "RETR":
                            //retr
                            break;
                        case "STOR":
                            //stor
                            break;
                        default:
                            System.out.println(cmd[0]);
                            sendToClient("From Server: -Non RFC command");
                    }

            } catch (Exception e) {
                e.printStackTrace();
                try { // If err closes server thread
                    socket.close();
                    run = false;
                    break;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * @param s that represents the second argument for the TYPE cmd
     * @throws Exception socket error
     */
     private void type(String s) throws Exception {
         System.out.println(s);
         switch (s) {
             case "A" -> sendToClient("+Using Ascii mode");
             case "B" -> sendToClient("+Using Binary mode");
             case "C" -> sendToClient("+Using Continuous mode");
             default -> sendToClient("-invalid mode");
         }
     }


    /**
     * closes client and sends to close server
     * @throws Exception which will close server anyway
     */
    private void done() throws Exception {
        sendToClient("+Closing connection. A total of " + mbSent/1000 + "kB was transferred.\n");
        System.out.println("Server instance closed <>");
        socket.close();
        run = false;
    }

    /**
     * Used to read ASCII from client
     * @return text string representing text from ASCII values
     * @throws Exception socket error
     */
    private String readFromClient() throws Exception {
        return inFromClient.readLine();
    }

    /**
     * Sends ASCII to client
     * Writes bytes from text in ASCII form
     * @param text to send to client
     */
    private void sendToClient(String text) throws Exception {
        outToClient.writeBytes(text + "\n");
    }

}