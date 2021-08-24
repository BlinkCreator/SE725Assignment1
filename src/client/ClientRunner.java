package client;

import java.io.IOException;
import java.net.Socket;

public class ClientRunner {
    public static void main(String[] args) throws Exception {
        Socket socket1 = new Socket("localhost", 6789);
        TCPClient client1 = new TCPClient(socket1);
        client1.main();
    }
}
