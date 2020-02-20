
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.net.InetAddress;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Iterator;

public class RmNetUtils {

    static final int CONNECT_TIMEOUT = 3000;

    //  Specifications from wikipedia:
    //      A domain name is a series of tokens separated by dots.
    //      Each token is from 1 to 63 characters, and the entire name is max 253 characters.
    //      Each token can contain letters, digits, hyphens.  May not start with hyphen.

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

    public static boolean validateIpAddress(String proposedAddress) {
//        System.out.println("validateIpAddress: " + proposedAddress);
        byte[] theAddress = parseIP4Address(proposedAddress);
        return theAddress != null;
    }

    //  Parse a string to an IP4 address.
    //  Format is 4 dot-separated numbers between 0 and 255
    //  e.g. 192.168.1.10.
    //  Returns a 4-byte array if valid, null if not

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

    //  Get the all-subnet broadcast address corresponding to the given IP address.
    //  This is done by simply replacing the last byte with "255"

    public static byte[] broadCastAddressForIp(byte[] ipAddressBytes) {
//        System.out.println("broadCastAddressForIp: " + formatBytesToDecimalString(ipAddressBytes, "."));
        assert(ipAddressBytes.length == 4);
        byte[] bytesCopy = ipAddressBytes.clone();
        bytesCopy[3] = (byte) 255;
        return bytesCopy;
    }

    //  Send WOL magic packet to given broadcast range with given MAC.  Return success indicator

    public static void sendWakeOnLan(byte[] broadcastAddressBytes, byte[] macAddressBytes) throws IOException {
        boolean success = true;
//        System.out.println("SendWakeOnLan(" + formatBytesToDecimalString(broadcastAddressBytes, ".")
//                + "," + formatBytesToHexString(macAddressBytes, ":") + ") STUB");
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
//            System.out.println("  Packet: " + packet + ", socket: " + socket);
        socket.send(packet);
        socket.close();
    }

    public static String formatBytesToHexString(byte[] bytes, String separator) {
//        System.out.println("formatBytesToHexString");
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


    public static String formatBytesToDecimalString(byte[] bytes, String separator) {
//        System.out.println("formatBytesToDecimalString");
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

    //  Parse Mac address.  12 byte code tokens separated by ":", "-", or "."
    //  return 12-byte array, or NULL if not valid

    public static byte[] parseMacAddress(String proposedMacAddress) {
//        System.out.println("parseMacAddress(" + proposedMacAddress + ")");
        byte[] result = null;
        String[] tokens = proposedMacAddress.toUpperCase().split("[:\\.\\-]");
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

    public static boolean testConnectionIP(byte[] ipAddressBytes, int port) {
//        System.out.println("testConnection(" + formatBytesToDecimalString(ipAddressBytes,".")
//                + ", " + port + ")");
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

    //  Get IP address byte set from a give string.
    //  The string might be an IP address in numeric dot-notation, or it might
    //  be a host name to be looked up.
    //  Return null if neither approach can turn into an IP address

    public static byte[] parseIP4FromString(String theString) {
        byte[] resultIpAddress = parseIP4Address(theString);
        if (resultIpAddress == null) {
            //  Try getting address by looking up host name
            try {
                InetAddress addressFromHost =  InetAddress.getByName(theString);
                resultIpAddress = addressFromHost.getAddress();
            } catch (UnknownHostException e) {
                resultIpAddress = null;
            }
        }
        return resultIpAddress;
    }

    //  Test connection to given server and port.  Server might be a name or an IP address.

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
