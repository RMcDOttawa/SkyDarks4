//  Class for sending Javascript command strings to the server running TheSkyX

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TheSkyXServer {
    private static final int SOCKET_TIMEOUT = 5 * 1000;

    private String serverAddress;
    private int portNumber;
    private InetSocketAddress inetSocketAddress;

    //  Constructor, taking address and port number, create socket for trial connection
    public TheSkyXServer(String serverAddress, Integer portNumber) throws IOException {
        super();
        this.serverAddress = serverAddress;
        this.portNumber = portNumber;
        //  Trial connection
        Socket socket = new Socket();
        this.inetSocketAddress = new InetSocketAddress(serverAddress, portNumber);
        socket.connect(this.inetSocketAddress, this.SOCKET_TIMEOUT);
        socket.close();
    }

    //  Ask the server for the path set via the camera AutoSave button

    public String getCameraAutosavePath() throws IOException {
        String commandWithReturn = "var path=ccdsoftCamera.AutoSavePath;"
                + "var Out;Out=path+\"\\n\";";
        String returnValue = this.sendCommandWithReturn(commandWithReturn);
        return returnValue;
    }

    //  Send to the server a command packet that gets a return value, and return it

    private String sendCommandWithReturn(String commandToSend) throws IOException {
        String commandPacket =  "/* Java Script */"
                + "/* Socket Start Packet */"
                + commandToSend
                + "/* Socket End Packet */";
        return sendCommandPacket(commandPacket);
    }

    //  Send given command packet to server, retrieve server response

    String sendCommandPacket(String commandPacket) throws IOException {

        //  Create socket and connect
        Socket socket = new Socket();
        socket.connect(this.inetSocketAddress, this.SOCKET_TIMEOUT);

        //  Send the command to the server
        PrintStream toServerStream = new PrintStream(socket.getOutputStream());
        toServerStream.println(commandPacket);

        //  Read the response
        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader response = new BufferedReader(inputStreamReader);
        String serverAnswer = response.readLine();

        response.close();
        inputStreamReader.close();
        toServerStream.close();
        socket.close();

        return serverAnswer;
    }

    //  Send "Connect to camera" command to server.  No response expected

    public void connectToCamera() throws IOException {
        String command = "ccdsoftCamera.Connect();";
        this.sendCommandNoReturn(command);
    }

    private void sendCommandNoReturn(String commandToSend) throws IOException {
        System.out.println("Send command: " + commandToSend);
        String commandPacket =  "/* Java Script */"
                + "/* Socket Start Packet */"
                + commandToSend
                + "var Out;"
                + "Out=\"0\\n\";"
                + "/* Socket End Packet */";
        this.sendCommandPacket(commandPacket);
   }

   // Turn camera cooling on or off.  If on, set temperature target;
    public void setCameraCooling(boolean coolingOn, Double temperatureTarget) throws IOException {
        String command  = "";
        if (coolingOn) {
            command = "ccdsoftCamera.TemperatureSetPoint=" + temperatureTarget + ";";
        }
        command += "ccdsoftCamera.RegulateTemperature=" + boolToJS(coolingOn) + ";"
                + "ccdsoftCamera.ShutDownTemperatureRegulationOnDisconnect=false;";
        this.sendCommandNoReturn(command);
    }

    //  Convert a boolean to the text used by JavaScript

    public static String boolToJS(boolean theBool) {
        return theBool ? "true" : "false";
    }
}
