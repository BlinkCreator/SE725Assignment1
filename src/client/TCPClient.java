package client;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TCPClient {
    boolean run = true;
    Socket socket;
    DataOutputStream outToServer;
    BufferedReader inFromServer;

    String clientDir = System.getProperty("user.dir") + File.separator +"src"+ File.separator +"client"+File.separator+"dir"; // default directory


TCPClient(Socket socket) throws IOException {

        try {
            this.socket = socket;
            socket.setReuseAddress(true);
            //sets ASCII streams
            this.outToServer = new DataOutputStream(socket.getOutputStream());
            this.inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Client Created");
            sendToServer("Client Connected...");
            System.out.println("FROM SERVER: "+ readFromServer());

        } catch (Exception e) {
            e.printStackTrace();
            socket.close();
            run = false;
        }
    }

    public void main(){
        // USER=1, ACCT=2, PASS=3, TYPE=4, LIST=5, CDIR=6, KILL=7, NAME=8, DONE=9, RETR=10, STOR=11
        // Gets ip and port from terminal input
        // Initializes socket and streams
        while (run) {
            try{
                System.out.print("Input command master: ");
                String cmdString = readCommand();
                String[] cmd = cmdString.split(" ");

            if(cmd[0] == null) {
                cmd[0] = "ERROR. NULL";
            }
                    switch (cmd[0]){
                        case "DONE":
                            done();
                            break;
                        case "USER":
                        case "ACCT":
                        case "PASS":
                        case "TYPE":
                        case "KILL":
                        case "CDIR":
                            processCMD(cmdString);
                            break;
                            //cdir(cmdString);
                        case "LIST":
                            list(cmdString);
                            break;
                        case "NAME":
                            name(cmdString);
                            break;
                        case "RETR":
                            retr(cmdString);
                            break;
                        case "STOR":
                            stor(cmdString);
                            break;
                        default:
                            //if command not found
                            System.out.println("Input error: Invalid Command");
                            System.out.println("Commands available: "
                                    + "\"USER\", \"ACCT\", \"PASS\", \"TYPE\", \"LIST\","
                                    + "\"CDIR\", \"KILL\", \"NAME\", \"DONE\", \"RETR\", \"STOR\"");
                            break;
                    }

            }catch(Exception e){
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

    private void stor(String s) throws Exception{
        String[] args = s.split(" ");
        String retrievePath = clientDir +File.separator + args[2];
        File file = new File(retrievePath);
        if(!file.exists()){
           System.out.println("-File does not exist at " + retrievePath );
           return;
        }
        sendToServer(s);
        System.out.println(readFromServer());//response from STOR cmd
        System.out.println(readFromServer());//ok waiting for file


        boolean isProcessed = false;

        while (!isProcessed) {
            String cmdString = readCommand();
            String[] sizeArgs = cmdString.split(" ");
            String response = "";
            if(sizeArgs[0].equals("SIZE")){
                sendToServer(cmdString);
                byte[] bytes = Files.readAllBytes(Paths.get(retrievePath));
                String content = new String(bytes);
                int byteSize = content.getBytes().length;
                outToServer.write(byteSize);
                outToServer.writeBytes(content);
                outToServer.flush();
                System.out.println(readFromServer());
                isProcessed = true;

            }
        }
    }
    public void retr(String s) throws  Exception {
        sendToServer(s);
        String[] args = s.split(" ");
        String response = readFromServer();
        if (response.equals("-File doesn't exist")) {
            System.out.println(response);
            return;
        }
        boolean isProcessed = false;
        int byteSize = Integer.parseInt(response);
        System.out.println(byteSize);
        while (!isProcessed) {
            String cmdString = readCommand();
            String response2 = null;
            if(cmdString.equals("SEND")){
                sendToServer(cmdString);

                byte[] inBytes = new byte[byteSize];
                for (int i = 0; i < byteSize; i++) {
                    inBytes[i] = (byte) inFromServer.read();//retrieveSend.read();
                }
                FileOutputStream createdFile = new FileOutputStream(clientDir + File.separator + args[1]);
                createdFile.write(inBytes);
                createdFile.close();

                response2 = readFromServer();
                System.out.println(response2);
            }else if(cmdString.equals("STOP")){
                System.out.println(readFromServer());
               isProcessed = true;
            }
            if (response2.equals("+File Sent")){
                isProcessed = true;
            }
        }
    }

    private void list(String s) throws  Exception {
        sendToServer(s);
        boolean processList = false;
        while(!processList){
            String response = readFromServer();
            System.out.println(response);
            if(response == null || response.equals("") || response.equals("-Please Login")){
               processList = true;
            }
        }
    }

    public void name(String s) throws  Exception {
        sendToServer(s);
        String response = readFromServer();
        System.out.println(response);
        if (response.equals("+File Exists")) {
            boolean isProcessed = false;
            String response2 = readFromServer();
            System.out.println(response2);
            while (!isProcessed) {
                String cmdString = readCommand();
                sendToServer(cmdString);
                response2 = readFromServer();
                System.out.println(response2);
                if (!response2.equals("Please send TOBE followed by the new name")) {
                    isProcessed = true;
                }
            }
        }
    }

    /**
     * might get it's own functionality depending
     * if I implement account password
     * @param s
     * @throws Exception
     */
    public void cdir(String s) throws Exception{
        sendToServer(s);
        System.out.println(readFromServer());
        // accessing restricted goes here
    }

    /**
     * Typical send and receive response for client
     * @param cmd
     * @throws Exception
     */
    private void processCMD(String cmd) throws Exception{
        sendToServer(cmd);
        System.out.println(readFromServer());
    }

    /**
     * closes client and sends to close server
     * @throws Exception which will close server anyway
     */
    public void done() throws Exception{
        sendToServer("DONE");
        System.out.println(readFromServer());
        socket.close();
        run = false;
    }

    /**
     *
     * @return command that has been read from user
     * @throws Exception server error
     */
    public String readCommand() throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String input = bufferedReader.readLine();
        String command = input;
        return command;
    }

    /**
     * Used to read ASCII from server
     * @return text string representing text from ASCII values
     * @throws Exception server error
     */
    private String readFromServer() throws Exception {
        return inFromServer.readLine();
    }

    /**
     * Sends ASCII to server
     * Writes bytes from text in ASCII form
     * @param text text wished to send to server
     */
    private void sendToServer(String text) throws Exception {
        outToServer.writeBytes(text + "\n");
    }
}