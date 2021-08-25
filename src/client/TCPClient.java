package client;
import java.io.*;
import java.net.*;

public class TCPClient {
    boolean run = true;
    Socket socket;
    DataOutputStream outToServer;
    BufferedReader inFromServer;

    // all possible commands
    private final String[] rfcCommands = new String[]{"USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL",
            "NAME", "TOBE", "DONE", "RETR", "SEND", "STOP", "STOR"};

    // current stored command
    private String rfcCommand = "";

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
                        case "ACCT":
                            //auth("ACCT",commandArgs);
                            break;
                        case "PASS":
                           // auth("PASS",commandArgs);
                            break;
                        case "TYPE":
                           type(cmdString);
                            break;
                        case "LIST":
                           // list(commandArgs);
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

    /**
     *
     * @param cmd that represents the arguements set by user
     * @throws Exception on server error
     */
    private void type(String cmd) throws Exception {
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