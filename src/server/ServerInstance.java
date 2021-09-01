package server;

import java.io.*;
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
    String serverDir = System.getProperty("user.dir") + File.separator +"server"+File.separator+"dir"; // default directory

    // Create account object to manage Accounts
    Accounts accounts = new Accounts();

    //Data Streams for ASCII and Binary
    BufferedReader inFromClient;
    DataOutputStream outToClient;

    ServerInstance(Socket socket) throws IOException {
        this.socket = socket;

        try {
            socket.setReuseAddress(true);
            this.inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.outToClient = new DataOutputStream(socket.getOutputStream());

            System.out.println("Server Instance Created");
            sendToClient("(listening for connection)");
            System.out.println(readFromClient());
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

                // remember to revert this to !accounts
                if(!accounts.checkLogin()) {
                    switch (cmd[0]) {
                        case "USER" -> user(cmd[1]);
                        case "ACCT" -> acct(cmd[1], true);
                        case "PASS" -> pass(cmd[1], true);
                        case "DONE" -> done();
                        // auth("PASS",commandArgs);
                        default -> sendToClient("-Please Login");
                    }
                }else {
                    switch (cmd[0]) {
                        case "USER" -> user(cmd[1]);

                        //auth("ACCT",commandArgs);
                        case "ACCT" -> acct(cmd[1], true);

                        //auth("ACCT",commandArgs);
                        case "PASS" -> pass(cmd[1], true);

                        // auth("PASS",commandArgs);
                        case "DONE" -> done();
                        case "TYPE" -> type(cmd[1]);
                        case "LIST" -> list(cmd[1], cmd[2]);
                        case "CDIR" -> cdir(cmd[1]);
                        case "KILL" -> kill(cmd[1]);
                        case "NAME" -> name(cmd[1]);
                        case "RETR" -> retr(cmd[1]);
                        case "STOR" -> stor(cmd[1], cmd[2]);
                        default -> {
                            sendToClient("-Non RFC command");
                        }
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

    /**
     * Stors file at serverDirectory
     * according to various types described
     * @param storMode APP NEW OR OLD that is the type of storing of the file
     * @param fileName NAME of file on client side
     * @throws Exception if socket error
     */
    private void  stor(String storMode, String fileName) throws Exception{
        String filePath =serverDir+ File.separator + fileName;
        File file = new File(filePath);
        if(file.exists()) {
            switch (storMode) {
                case "NEW" -> sendToClient("+File exists, will create new generation of file");
                case "OLD" -> sendToClient("+Will write over old file");
                case "APP" -> sendToClient("+Will append to file");
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
                int actualByteSize = Integer.parseInt(readFromClient()); //read size of file to be sent from client
                if (numOfBytes > actualByteSize) { //if requested size to be stored is greater than the size of the actual file set size variable to be recieved as actual file size
                    numOfBytes= actualByteSize;
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
                    createdFile = new FileOutputStream(filePath,true);
                }else{
                    createdFile = new FileOutputStream(filePath);
                }
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
     * @throws Exception if socket error
     */
    private void list(String s, String dirPathFromRoot) throws  Exception{
        String dir;
        if(dirPathFromRoot == null || dirPathFromRoot.equals("")){
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
            for (File value : files) {
                sendToClient(value.getName());
            }
        }
        if (s.equals("V")) {
            for (File value : files) {
                long fileSize = value.length();
                Date date = new Date(value.lastModified());
                sendToClient(value.getName() + ": " + date + ": " + fileSize + ": " + accounts.getUser());
            }
        }
        sendToClient("");
    }

    /**
     * Transfers specified file from
     * server to client dir if it exists
     * @param s file name
     * @throws Exception if socket error
     */
    private void retr(String s) throws Exception{
        String retrievePath =serverDir+ File.separator + s;
        File file = new File(retrievePath);

        if (file.exists()) {
            byte[] bytes = Files.readAllBytes(Paths.get(retrievePath));
            String content = new String(bytes);

            //remember to delete this debugging only
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
     * @throws Exception if socket error
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
     * @throws Exception if socket error
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
                    if (!cmd[1].equals("")) {
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
     * @throws Exception if socket error
     */
    public void cdir(String s) throws Exception{
        if(s.equals("ROOT")){
            serverDir = System.getProperty("user.dir") + File.separator +"server"+File.separator+"dir"; // default directory
        }else{
            serverDir = serverDir + File.separator + s;
        }
        File file = new File(serverDir);
        if (!file.isDirectory()){
            sendToClient("-Can't connect to directory because: Invalid Directory-" + serverDir);
            return;
        }
        sendToClient("!Changed working dir to " + serverDir);
    }

    /**
     *  takes in command and returns based
     *  on account authentication
     * @param s accountName
     * @throws Exception if socket error
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
        System.out.println("Instance Closed");
        accounts.selectUser("LOGGED OUT");
        sendToClient("+Closing connection.\n");
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