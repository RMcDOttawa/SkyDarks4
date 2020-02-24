import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.net.*;

/**
 * Utility methods for handling network addresses and testing connections
 */
public class RmNetUtils {

    static final int CONNECT_TIMEOUT = 3000;

    //  Specifications from wikipedia:
    //      A domain name is a series of tokens separated by dots.
    //      Each token is from 1 to 63 characters, and the entire name is max 253 characters.
    //      Each token can contain letters, digits, hyphens.  May not start with hyphen.

    /**
     * Test if a string is a valid host name (syntactically valid, not looked up on network)
     * Specifications from wikipedia:
     *      A domain name is a series of tokens separated by dots.
     *     Each token is from 1 to 63 characters, and the entire name is max 253 characters.
     *      Each token can contain letters, digits, hyphens.  May not start with hyphen.
     * @param proposedHostName      The name to be tested
     * @return (boolean)            Host name is valid
     */
    public static boolean validateHostName(String proposedHostName) {
        String hostNameTrimmed = proposedHostName.trim();
//        System.out.println("validateHostName: " + hostNameTrimmed);
        boolean valid = false;
        if ((hostNameTrimmed.length() > 0) && (hostNameTrimmed.length() <= 253)) {
            String[] tokens = hostNameTrimmed.split("\\.");
            valid = true;
            for (int tokenIndex = 0; valid && (tokenIndex < tokens.length); tokenIndex++) {
                valid = false;
                String thisToken = tokens[tokenIndex].toUpperCase();
//                System.out.println("   Validating token: " + thisToken);
                if (thisToken.length() > 0 && thisToken.length() <= 63) {
                    // Length OK.  Check for valid characters
                    if (thisToken.matches("^[A-Z0-9\\-]+")) {
                        // Valid characters.  Can't begin with a hyphen
                        if (!thisToken.startsWith("-")) {
                            //  All is well.
                            valid = true;
                        }
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Determine if the given string is a valid IPv4 address
     *  e.g. 192.168.1.123
     * @param proposedAddress       String to validate
     * @return (boolean)            indicator of validity
     */
    public static boolean validateIpAddress(String proposedAddress) {
        byte[] theAddress = parseIP4Address(proposedAddress);
        return theAddress != null;
    }

    /**
     * Parse a string to an IP4 address.
     * Format is 4 dot-separated numbers between 0 and 255
     * e.g. 192.168.1.10.
     * Returns a 4-byte array if valid, null if not
     * @param inputString   String to be parsed as IP address
     * @return (array)      Array of 4 bytes parsed as IP address
     */
    public static byte[] parseIP4Address(String inputString) {
        byte[] result = null;

        byte[] addressBytes = new byte[4];
        String[] tokens = inputString.split("\\.");
        if (tokens.length == 4) {
            boolean valid = true;
            for (int i = 0; (i < tokens.length) && valid; i++) {
                String thisToken = tokens[i];
                try {
                    int tokenParsed = Integer.parseInt(thisToken);
                    if ((tokenParsed >= 0) && (tokenParsed <= 255)) {
                        addressBytes[i] = (byte) tokenParsed;
                    } else {
                        //  Number is out of acceptable range
                        valid = false;
                    }
                } catch (NumberFormatException e) {
                    valid = false;
                }
            }
            if (valid) {
                result =  addressBytes;
            }
        }
        return result;
    }

    /**
     * Send WOL magic packet to given broadcast range with given MAC.  Return success indicator
     * @param broadcastAddressBytes     Parsed byte array of sublan broadcast address
     * @param macAddressBytes           Parsed byte array of target MAC address
     * @throws IOException              I/O error returned from network
     */
    public static void sendWakeOnLan(byte[] broadcastAddressBytes, byte[] macAddressBytes) throws IOException {
        assert(broadcastAddressBytes.length == 4);
        assert(macAddressBytes.length == 6);

        //  Make up magic packet
        byte[] magicPacket = new byte[6 + 16 * macAddressBytes.length];
        for (int i = 0; i < 6; i++) {
            magicPacket[i] = (byte) 0xff;
        }
        for (int i = 6; i < magicPacket.length; i += macAddressBytes.length) {
            System.arraycopy(macAddressBytes, 0, magicPacket, i, macAddressBytes.length);
        }

        InetAddress address = InetAddress.getByAddress(broadcastAddressBytes);
        DatagramPacket packet = new DatagramPacket(magicPacket, magicPacket.length, address, 9);
        DatagramSocket socket = new DatagramSocket();

        socket.send(packet);
        socket.close();
    }

    /**
     * Format a bytes array to printable hexadecimal for testing purposes.
     * @param bytes         Bytes array to format
     * @param separator     Character that should separate bytes
     * @return (String)
     */
    @SuppressWarnings("unused")
    public static String formatBytesToHexString(byte[] bytes, String separator) {
        StringBuilder builder = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; i++) {
            byte thisByte = bytes[i];
            String thisByteAsString = String.format("%x", thisByte);
            if (i > 0) {
                builder.append(separator);
            }
            builder.append(thisByteAsString);
        }
        return builder.toString();
    }

    /**
     * Format a bytes array to a string of decimal numbers with a separator
     * e.g. an IP address.
     * @param bytes         Bytes array to be formatted
     * @param separator     Separator ("." for traditional IP address format)
     * @return (String)
     */
    @SuppressWarnings("unused")
    public static String formatBytesToDecimalString(byte[] bytes, String separator) {
        StringBuilder builder = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; i++) {
            byte thisByte = bytes[i];
            String thisByteAsString = String.valueOf(thisByte < 0 ? thisByte + 256 : thisByte);
            if (i > 0) {
                builder.append(separator);
            }
            builder.append(thisByteAsString);
        }
        return builder.toString();
    }

    /**
     * Parse Mac address.  12 byte code tokens separated by ":", "-", or "."
     * return 12-byte array, or NULL if not valid
     * @param proposedMacAddress    String to be parsed
     * @return                      Byte array if valid, null if not valid
     */

    public static byte[] parseMacAddress(String proposedMacAddress) {
//        System.out.println("parseMacAddress(" + proposedMacAddress + ")");
        byte[] result = null;
        String[] tokens = proposedMacAddress.toUpperCase().split("[:.\\-]");
//        System.out.println("   Tokens parsed: " + tokens.toString());
        if (tokens.length == 6) {
            result = new byte[6];
            for (int i = 0; (i < 6) && (result != null); i++) {
                String byteString = tokens[i];
//                System.out.println("      Converting " + byteString);
                try {
                    long convertedByte = Integer.parseInt(byteString,16);
                    if ((convertedByte >= 0) && (convertedByte <= 255)) {
                        result[i] = (byte) convertedByte;
                    } else {
//                        System.out.println("         Token value " + convertedByte
//                                + " is out of range for a single byte");
                        result = null;
                    }
                } catch (NumberFormatException e) {
//                    System.out.println("         Token isn't a valid hex string for a byte");
                    result = null;
                }
            }
        }
        return result;
    }

    /**
     * Attempt to connect to the given server address and port, report if successful
     * @param ipAddressBytes    Parsed bytes of IPv4 address
     * @param port              Port number (integer)
     * @return                  Success indicator
     */
    @SuppressWarnings("unused")
    public static boolean testConnectionIP(byte[] ipAddressBytes, int port) {
        boolean success;
        try {
            InetAddress address = InetAddress.getByAddress(ipAddressBytes);
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            Socket socket = new Socket();
            socket.connect(socketAddress, CONNECT_TIMEOUT);
            success = true;
            socket.close();
        } catch (IOException e) {
            success = false;
        }
        return success;
    }

    /**
     * Get IP address byte set from a give string.
     * The string might be an IP address in numeric dot-notation, or it might
     * be a host name to be looked up.
     * Return null if neither approach can turn into an IP address
     * @param theString         String to be parsed
     * @return                  Bytes of IP address, or null if not valid
     */

    public static byte[] parseIP4FromString(String theString) {
        byte[] resultIpAddress = parseIP4Address(theString);
        if (resultIpAddress == null) {
            //  Try getting address by looking up host name
            try {
                InetAddress addressFromHost =  InetAddress.getByName(theString);
                resultIpAddress = addressFromHost.getAddress();
            } catch (UnknownHostException e) {
                //resultIpAddress = null;
            }
        }
        return resultIpAddress;
    }

    /**
     * Test connection to given server and port.  Server might be a name or an IP address.
     * @param addressString     Address to connect to
     * @param port              Port to connect to
     * @return                  Pair (success indicator, message)
     */
    public static ImmutablePair<Boolean, String> testConnection(String addressString, int port) {
        boolean success = false;
        String message = "";

        byte[] ipBytes = parseIP4FromString(addressString);
        if (ipBytes != null) {
            // We have a valid address, now try to connect
            try {
                InetAddress address = InetAddress.getByAddress(ipBytes);
                InetSocketAddress socketAddress = new InetSocketAddress(address, port);
                Socket socket = new Socket();
                socket.connect(socketAddress, CONNECT_TIMEOUT);
                success = true;
                socket.close();
            } catch (IOException e) {
                message = "Connection Failed";
            }
        } else {
            message = "Bad Address";
        }

        return ImmutablePair.of(success, message);
    }


}
