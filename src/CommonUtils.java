import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Common constants and utility functions for use around the program
 */
public class CommonUtils {
    public static final int     SECONDS_IN_HOUR = 60 * 60;
    public static final int     SECONDS_IN_MINUTE = 60;
    public static final double  SECONDS_IN_DAY = 24.0 * 60 * 60;
    public static final int     INT_SECONDS_IN_DAY = 24 * 60 * 60;
    public static final double  WATER_BOILS = 100.0;
    public static final double  ABSOLUTE_ZERO = -273.15;
    public static final String  DATA_FILE_SUFFIX = "pskdk4";

    public static final int     SESSION_TAB_INDEX = 4;
    public static final String   UNSAVED_WINDOW_TITLE = "(Unsaved File)";


    /**
     * Given a full path, get just the file name, without the extension.
     * Funny, I thought there was a built-in function with exactly this function somewhere
     * in a Java library, but I couldn't find it after looking for as long as i cared to.
     * @param fullPath
     * @return
     */
    public static String simpleFileNameFromPath(String fullPath) {
        Path path = Paths.get(fullPath);
        Path fileNamePath = path.getFileName();
        String fileNameString = fileNamePath.toString();
        if (fileNameString.endsWith("." + DATA_FILE_SUFFIX)) {
            fileNameString = fileNameString.substring(0,
                    fileNameString.length() - (1 + DATA_FILE_SUFFIX.length()));
        }
        return fileNameString;
    }
}
