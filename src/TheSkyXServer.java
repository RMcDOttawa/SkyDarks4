// Class for sending commands, encoded in Javascript, to TheSkyX server


import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TheSkyXServer {
    private static final int SOCKET_TIMEOUT = 5 * 1000;

    //  We use a lock for server commands since more than one task may be asking the server to act
    private ReentrantLock serverLock = null;

    private InetSocketAddress inetSocketAddress;

    /**
     * Constructor, taking address and port number, create socket for trial connection
     * @param serverAddress         String giving server name or IP address
     * @param portNumber            Port number where server is listening
     * @throws IOException          I/O error from network
     */
    public TheSkyXServer(String serverAddress, Integer portNumber) throws IOException {
        super();
        this.serverLock = new ReentrantLock();
        //  Trial connection
        Socket socket = new Socket();
        this.inetSocketAddress = new InetSocketAddress(serverAddress, portNumber);
        socket.connect(this.inetSocketAddress, SOCKET_TIMEOUT);
        socket.close();
    }

    /**
     * Ask the server for the path set via the camera AutoSave button
     * @return (String)         Absolute path to file save area on TheSkyX machine
     * @throws IOException      I/O error from network
     */
    public String getCameraAutosavePath() throws IOException {
        String commandWithReturn = "var path=ccdsoftCamera.AutoSavePath;"
                + "var Out;Out=path+\"\\n\";";
        return this.sendCommandWithReturn(commandWithReturn);
    }

    /**
     * Send to the server a command packet that gets a return value, and return it
     * @param commandToSend         Command to be sent to server
     * @return (String)             String returned from server
     * @throws IOException          I/O error from network
     */
    private String sendCommandWithReturn(String commandToSend) throws IOException {
        String commandPacket =  "/* Java Script */"
                + "/* Socket Start Packet */"
                + commandToSend
                + "/* Socket End Packet */";
        return sendCommandPacket(commandPacket);
    }

    /**
     * Low-level send given command packet to server, retrieve server response
     * @param commandPacket         Command to be sent to server
     * @return (String)             String returned from server
     * @throws IOException          I/O error from network
     */

    String sendCommandPacket(String commandPacket) throws IOException {

        //  Create socket and connect
        Socket socket = new Socket();
        socket.connect(this.inetSocketAddress, SOCKET_TIMEOUT);

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

    /**
     * Send "Connect to camera" command to server.  No response expected
     * @throws IOException          I/O error from network
     */
    public void connectToCamera() throws IOException {
        String command = "ccdsoftCamera.Connect();";
        this.sendCommandNoReturn(command);
    }

    /**
     * Send "Disconnect from camera" command to server.  No response expected
     * @throws IOException          I/O error from network
     */
    public void disconnectFromCamera() throws IOException {
        String command = "ccdsoftCamera.Disconnect();";
        this.sendCommandNoReturn(command);
    }

    /**
     * Send to the server a command that is not expecting a returned string
     * @param commandToSend     Command to send to server
     * @throws IOException      I/O error from network
     */
    private void sendCommandNoReturn(String commandToSend) throws IOException {
        String commandPacket =  "/* Java Script */"
                + "/* Socket Start Packet */"
                + commandToSend
                + "var Out;"
                + "Out=\"0\\n\";"
                + "/* Socket End Packet */";
        try {
            this.serverLock.lock();
            this.sendCommandPacket(commandPacket);
        } finally {
            this.serverLock.unlock();
        }

    }

    /**
     * Turn camera cooling on or off.  If on, set temperature target;
     * @param coolingOn                 True for cooling on, false for off
     * @param temperatureTarget         Target temperature if turning cooling on, ignored if not
     * @throws IOException              I/O error from network
     */
    public void setCameraCooling(boolean coolingOn, Double temperatureTarget) throws IOException {
        String command  = "";
        if (coolingOn) {
            command = "ccdsoftCamera.TemperatureSetPoint=" + temperatureTarget + ";";
        }
        command += "ccdsoftCamera.RegulateTemperature=" + boolToJS(coolingOn) + ";"
                + "ccdsoftCamera.ShutDownTemperatureRegulationOnDisconnect=false;";
        this.sendCommandNoReturn(command);
    }

    /**
     * Convert a Java boolean to the text used by JavaScript
     * @param theBool           Boolean value to convert
     * @return (String)         JavaScript representation of the boolean value
     */

    public static String boolToJS(boolean theBool) {
        return theBool ? "true" : "false";
    }

    /**
     * Get and return the camera's temperature and the cooler's power level (as percent)
     * @return (Pair)                           Two doubles: temperature and cooler power
     * @throws IOException                      I/O error from network
     * @throws NumberFormatException            Invalid number format parsing server response
     * @throws ArrayIndexOutOfBoundsException   Missing information in parsing server response
     */
    public ImmutablePair<Double, Double> getCameraTemperatureAndPower()
            throws IOException, NumberFormatException, ArrayIndexOutOfBoundsException {
        String commandWithReturn = "var temp=ccdsoftCamera.Temperature;"
                + "var power=ccdsoftCamera.ThermalElectricCoolerPower;"
                + "var Out;"
                + "Out=temp+\",\"+power+\"\\n\";";
        String returnValue = this.sendCommandWithReturn(commandWithReturn);
        double temperature;
        double power;
        String[] parts = returnValue.split(",");
        temperature = Double.parseDouble(parts[0]);
        power = Double.parseDouble(parts[1]);
        return ImmutablePair.of(temperature, power);
    }

    /**
     * Expose a frame with the given specifications.
     * Note that we don't actually receive the resulting image back here - the images are very
     * large and would take a long time to transmit.  We just cause the image to be acquired, and
     * it is saved where TheSkyX has its AutoSave path set.
     * @param frameType             Dark or Bias frame
     * @param exposureSeconds       Exposure time if Dark; ignored if Bias
     * @param binning               Integer n x n binning value
     * @param asynchronous          True if asynchronous (i.e. don't wait for camera), false for synchronous
     * @param autoSave              Should acquired image be auto-saved to server's autosave directory?
     * @throws IOException          I/O error from network
     */
    public void exposeFrame(FrameType frameType, double exposureSeconds,
                            Integer binning, boolean asynchronous, boolean autoSave) throws IOException {
//        System.out.println("exposeFrame(" + frameType + "," + exposureSeconds + ","
//                + binning + "," + asynchronous + "," + autoSave + ")");
        String command = "ccdsoftCamera.Autoguider=false;"        //  Use main camera
                + "ccdsoftCamera.Asynchronous=" + boolToJS(asynchronous) + ";"   //  Wait for camera?
                + "ccdsoftCamera.Frame=" + (frameType == FrameType.DARK_FRAME ? "3" : "2") + ";"
                + "ccdsoftCamera.ImageReduction=0;"       // No autodark or calibration
                + "ccdsoftCamera.ToNewWindow=false;"      // Reuse window, not new one
                + "ccdsoftCamera.ccdsoftAutoSaveAs=0;"    //  0 = FITS format
                + "ccdsoftCamera.AutoSaveOn=" + boolToJS(autoSave) + ";"
                + "ccdsoftCamera.BinX=" + binning + ";"
                + "ccdsoftCamera.BinY=" + binning + ";"
                + "ccdsoftCamera.ExposureTime=" + exposureSeconds + ";"
                + "var cameraResult = ccdsoftCamera.TakeImage();"
                + "var Out;Out=cameraResult+\"\\n\";";

        String result = this.sendCommandWithReturn(command);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            System.out.println("Error returned from camera: " + result);
        }
    }

    /**
     * One of the peculiarities of the TheSkyX tcp interface.  Sometimes you get "success" back
     * from the socket, but the returned string contains an error encoded in the text message.
     * The "success" meant that the server was successful in sending this error text to you, not
     * that all is well.  Awkward.  We check for that, and return a better "success" indicator
     * and a message of any failure we found.
     *
     *      0:  No error
     *      1:  Camera was aborted
     *      2:  CFITSIO error (bad file save name or location)
     *      3:  Some other TYPE ERROR
     * @param returnedText      Server response text to be checked
     * @return (int)            Error code as described above
     */
    private int errorCheckResult(String returnedText) {
        String returnedTextUpper = returnedText.toUpperCase();
        int result = 0;
        if (returnedTextUpper.contains("TYPEERROR: PROCESS ABORTED"))
            result = 1;
        else if (returnedTextUpper.contains("TYPEERROR: CFITSIO ERROR"))
            result = 2;
        else if (returnedTextUpper.contains("TYPEERROR:"))
            result = 3;
        return result;
    }

    /**
     *
     *   Send a camera abort command to the server, stopping any image acquisition in progress
    */

    public void abortImageInProgress() throws IOException {
        String command = "ccdsoftCamera.Abort();";
        this.sendCommandNoReturn(command);
    }

    /**
     * Ask server if the recently-started asynchronous exposure is complete.
     * @return Indicator of completion
     */
    public boolean exposureIsComplete() throws IOException {
        String commandWithReturn = "var path=ccdsoftCamera.IsExposureComplete;"
                + "var Out;Out=path+\"\\n\";";
        String returnString = this.sendCommandWithReturn(commandWithReturn);
        return returnString.trim().equals("1");
    }
}
