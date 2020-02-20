//  Class for sending Javascript command strings to the server running TheSkyX

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TheSkyXServer {
    private static final int SOCKET_TIMEOUT = 5 * 1000;

    private String serverAddress;
    private int portNumber;
    private Socket socket;

    //  Constructor, taking address and port number, create socket for connection
    public TheSkyXServer(String serverAddress, Integer portNumber) throws IOException {
        super();
        // todo TheSkyXServer constructor
        System.out.println("TheSkyXServer constructor");
        this.serverAddress = serverAddress;
        this.portNumber = portNumber;
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(serverAddress, portNumber), this.SOCKET_TIMEOUT);
    }

    // Close the server connection

    public void closeSocket() throws IOException {
        this.socket.close();
        this.socket = null;
    }
}
