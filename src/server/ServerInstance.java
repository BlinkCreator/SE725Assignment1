package server;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.stream.Collectors;

import static java.lang.Runtime.getRuntime;

public class ServerInstance extends Thread{
    protected Socket socket;

    // Different flags for continuing with different activities
    boolean run = true;

    // variables for Cmds
    String serverDir = System.getProperty("user.dir") + File.separator +"src"+ File.separator +"server"+File.separator+"dir"; // default directory
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
                    if(cmd == null) {
                        cmd[0] = "ERROR: NULL";
                    }
                    if(cmd.length < 2){
                        cmd = new String[]{cmd[0], ""};
                    }
                    if(cmd.length < 3){
                        cmd = new String[]{cmd[0],cmd[1], ""};
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
                            case "DONE":
                                done();
                                break;
                            case "TYPE":
                                type(cmd[1]);
                                break;
                            case "LIST":
                                list(cmd[1], cmd[2]);
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
                                stor(cmd[1],cmd[2]);
                                break;
                            default:
                                System.out.println(cmd[0]);
                                sendToClient("-Non RFC command");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                try { // If err closes server thread
                    sendToClient("DONE");
                    socket.close();
                    run = false;
                    break;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void  stor(String storMode, String fileName) throws Exception{
        String filePath =serverDir+ File.separator + fileName;
        File file = new File(filePath);
        if(file.exists()) {
            switch (storMode) {
                case "NEW":
                    sendToClient("+File exists, will create new generation of file");
                    break;
                case "OLD":
                    sendToClient("+Will write over old file");
                    break;
                case "APP":
                    sendToClient("+Will append to file");
                    break;
            }
        }else{
            if(storMode.equals("NEW")){
               sendToClient("+File does not exist, will create new file");
            }else{
                sendToClient("+Will create file");
            }
        }
        // SIZE portion of the STOR CMD
        sendToClient("+ok, waiting for file");
        boolean isProcessed = false;
        while(!isProcessed) {
            String[] cmd = readFromClient().split(" ");
            if (cmd[0].equals("SIZE")) {
                int numOfBytes = Integer.parseInt(cmd[1]);

                int actualByteSize = inFromClient.read(); //read size of file to be sent from client
                if (numOfBytes > actualByteSize) { //if requested size to be stored is greater than the size of the actual file set size variable to be recieved as actual file size
                    numOfBytes= actualByteSize;
                }

                if(numOfBytes > getRuntime().getRuntime().freeMemory()){
                    sendToClient("-Not enough room, don't send it");
                    return;
                }
                byte[] inBytes = new byte[numOfBytes];
                for (int i = 0; i < numOfBytes; i++) {
                    inBytes[i] = (byte) inFromClient.read();//retrieveSend.read();
                }
                //default output stream
                FileOutputStream createdFile;
                if(file.exists() && storMode.equals("NEW")){
                    filePath =serverDir+ File.separator;
                    while(file.exists()){//If stormode is new and file exists append copy to the end
                        filePath = filePath + "copy of ";
                        file = new File(filePath + fileName);
                    }
                    filePath = filePath + fileName;
                    createdFile = new FileOutputStream(filePath);
                }else if(file.exists() && storMode.equals("APP")){// If file exists append
                    System.out.println(filePath);
                    createdFile = new FileOutputStream(filePath,true);
                }else{
                    createdFile = new FileOutputStream(filePath);
                }
                System.out.println(createdFile);
                createdFile.write(inBytes);
                createdFile.close();
                sendToClient("+Saved " + "<" + fileName + ">");
                isProcessed = true;
            }
        }
    }
    /**
     *
     * @param s V for verbose or F
     * @param dirPathFromRoot directory, if null uses current server dir
     * @throws Exception
     */
    private void list(String s, String dirPathFromRoot) throws  Exception{
        String dir = "";
        if(s == null || s.equals("")){
            dir = serverDir;
        }else{
            dir = serverDir + File.separator + dirPathFromRoot;
            serverDir = dir;

        }

        File file = new File(dir);
        if (!file.isDirectory()){
            sendToClient("-Can't list directory because: Invalid Directory-" + dir);
            sendToClient("");
            return;
        }

        List<File> files = Files.list(Paths.get(dir))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        sendToClient("+" + dir + " <"+accounts.getUser()+">");
        if (s.equals("F")) {
            for (int i = 0; i < files.size(); i++) {
                sendToClient(files.get(i).getName());
            }
        }
        if (s.equals("V")) {
            for (int i = 0; i < files.size(); i++) {
                long fileSize = files.get(i).length();
                java.util.Date date = new java.util.Date(files.get(i).lastModified());
                sendToClient(files.get(i).getName() + ": " + date + ": " + fileSize  + ": " + accounts.getUser());
            }
        }
        sendToClient("");
    }

    /**
     * Transfers specified file from
     * server to client dir if it exists
     * @param s file name
     * @throws Exception
     */
    private void retr(String s) throws Exception{
        String retrievePath =serverDir+ File.separator + s;
        File file = new File(retrievePath);

        if (file.exists()) {
            byte[] bytes = Files.readAllBytes(Paths.get(retrievePath));
            String content = new String(bytes);

            //remember to delete this debugging only
            System.out.println("Content: " + content);
            int byteSize = content.getBytes().length;
            sendToClient(byteSize + "");

            // SEND portion of the RETR CMD
            boolean isProcessed = false;
            while(!isProcessed) {
                String[] cmd = readFromClient().split(" ");
                if (cmd[0].equals("SEND")) {
                    outToClient.writeBytes(content);
                    outToClient.flush();
                    sendToClient("+File Sent");
                    isProcessed = true;
                }
                if(cmd[0].equals("STOP")){

                    sendToClient("+ok, RETR aborted");
                    isProcessed = true;
                }
            }
        } else {
            sendToClient("-File doesn't exist");
        }
    }


    /**
     * Kills specified file
     * @param s suplementry directory
     * @throws Exception
     */
    private void kill(String s) throws Exception{
        File file = new File(serverDir + File.separator + s );
        if (file.delete()) {
            sendToClient("+File deleted successfully");
        } else {
            sendToClient("-Not deleted because file does not exist");
        }
    }

    /**
     * renames file if exists
     * @param s directed directory
     * @throws Exception
     */
    public void name(String s) throws  Exception{
        File file = new File(serverDir + File.separator + s);
        if (file.exists()) {
            sendToClient("+File Exists");
            boolean isProcessed = false;
            while(!isProcessed){
                sendToClient("Please send TOBE followed by the new name");
                String[] cmd = readFromClient().split(" ");
                if(cmd[0].equals("TOBE")) {
                    if (cmd[1] != "") {
                        File fileRename = new File(serverDir + File.separator + cmd[1]);
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
     * ROOT reverts it to root
     * @param s directory
     * @throws Exception
     */
    public void cdir(String s) throws Exception{
        String dir = "";
        if(s.equals("ROOT")){
            dir = System.getProperty("user.dir") + File.separator +"src"+ File.separator +"server"+File.separator+"dir"; // default directory
        }else{
            dir = serverDir + File.separator + s;
        }
        serverDir = dir;
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
     * @param s accountName
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
     * @param s password
     * @throws Exception socket error
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
     * @param s userName
     * @throws Exception if socket error
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