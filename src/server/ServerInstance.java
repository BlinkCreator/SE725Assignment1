package server;

import java.io.*;
import java.net.Socket;

public class ServerInstance extends Thread{
    protected Socket socket;

    // Different flags for continuing with different activities
    boolean run = true;

    // variables for Cmds
    String serverDir = System.getProperty("user.dir") + "/src/server/dir"; // default directory
    boolean isRetrieve = false; //for RETR cmd
    String fileName = ""; //for SEND cmd

    // Create account object to manage Accounts
    Accounts accounts = new Accounts();


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
                System.out.println(accounts.checkLogin());
                // remember to revert this to !accounts
                if(accounts.checkLogin()) {
                    switch (cmd[0]) {
                        case "USER":
                            user(cmd[1]);
                            //auth("ACCT",commandArgs);
                            break;
                        case "ACCT":
                            acct(cmd[1], true);
                            //auth("ACCT",commandArgs);
                            break;
                        case "PASS":
                            pass(cmd[1], true);
                            // auth("PASS",commandArgs);
                            break;
                        default:
                            System.out.println(cmd[0]);
                            sendToClient("-Please Login");
                    }
                }else{
                        switch (cmd[0]) {
                            case "DONE":
                                done();
                                break;
                            case "TYPE":
                                type(cmd[1]);
                                break;
                            case "LIST":
                                // list(commandArgs);
                                break;
                            case "CDIR":
                                cdir(cmd[1]);
                                break;
                            case "KILL":
                                kill(cmd[1]);
                                break;
                            case "NAME":
                                name(cmd[1]);
                                break;
                            case "RETR":
                                retr(cmd[1]);
                                break;
                            case "STOR":
                                //stor
                                break;
                            default:
                                System.out.println(cmd[0]);
                                sendToClient("-Non RFC command");
                    }
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

    private void retr(String s) throws Exception{

    }
    /**
     * Kills specified file
     * @param s
     * @throws Exception
     */
    private void kill(String s) throws Exception{
        File file = new File(serverDir + "/" + s );
        if (file.delete()) {
            sendToClient("+File deleted successfully");
        } else {
            sendToClient("-Not deleted because file does not exist");
        }
    }

    /**
     * renames file
     * @param s
     * @throws Exception
     */
    public void name(String s) throws  Exception{
        File file = new File(serverDir + "/" + s);
        if (file.exists()) {
            sendToClient("+File Exists");
            boolean isProcessed = false;
            while(!isProcessed){
                sendToClient("Please send TOBE followed by the new name");
                String[] cmd = readFromClient().split(" ");
                if(cmd[0].equals("TOBE")) {
                    if (cmd[1] != "") {
                        File fileRename = new File(serverDir + "/" + cmd[1]);
                        file.renameTo(fileRename);
                        sendToClient("+" + file + " renamed to " + fileRename);
                    }else{
                        sendToClient("-File wasn't renamed because Invalid file name provided");
                    }
                    isProcessed = true;
                }
            }
        } else {
            sendToClient("-Can't find " + file);
        }
    }

    /**
     * changes server directory
     * @param s
     * @throws Exception
     */
    public void cdir(String s) throws Exception{
        String dir = serverDir + "/" + s;
        File file = new File(dir);
        if (!file.isDirectory()){
            sendToClient("-Can't connect to directory because: Invalid Directory-" + dir);
            return;
        }
        // checks for restricted directories
//        if(s == "userAccounts" && !accounts.checkLogin()){
//            sendToClient("+directory ok, send account/password");
//            while(!accounts.checkLogin()) {
//                String[] cmd = readFromClient().split(" ");
//                while (cmd[0] != "ACCT" || cmd[0] != "PASS") {
//                    cmd = readFromClient().split(" ");
//                }
//                switch(cmd[0]){
//                    case "ACCT":
//                        acct(cmd[1],"CDIR");
//                    case "PASS":
//                        pass(cmd[1],"CDIR");
//                }
//            }
//
//        }
        serverDir = dir;
        sendToClient("!Changed working dir to " + dir);
    }

    /**
     *  takes in command and returns based
     *  on account authentication
     * @param s
     * @throws Exception
     */
    public void acct(String s, boolean forLogin) throws Exception{
        if(forLogin) {
            if (accounts.selectAccount(s)) {
                if (accounts.checkLogin()) {
                    sendToClient("! Account valid, logged-in");

                } else {
                    sendToClient("+Account valid, send password");
                }
            } else {
                sendToClient("-Invalid account, try again");
            }
        }
    }

    /**
     *  sends to server depending on
     *  password authentication
     *  if second argument is empty continues
     * @param s
     * @throws Exception
     */
    public void pass(String s,  boolean forLogin) throws Exception{
        if(forLogin) {
            if (accounts.enterPassword(s)) {
                if (accounts.checkLogin()) {
                    sendToClient("! Logged in");
                } else {
                    sendToClient("+Send account");
                }
            } else {
                sendToClient("-Wrong password, try again");
            }
        }
    }

    /**
     * takes in
     * @param s
     * @throws Exception
     */
    public void user(String s) throws Exception{
        if(accounts.selectUser(s)){
            sendToClient("+User-id valid, send account and password");
        }else{
            sendToClient("-Invalid user-id, try again");
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