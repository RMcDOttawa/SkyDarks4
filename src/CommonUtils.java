import java.nio.file.Path;
import java.nio.file.Paths;

public class CommonUtils {
    public static final double   SECONDS_IN_DAY = 24.0 * 60 * 60;
    public static final int      INT_SECONDS_IN_DAY = 24 * 60 * 60;
    public static final double   WATER_BOILS = 100.0;
    public static final double   ABSOLUTE_ZERO = -273.15;
    public static final String  DATA_FILE_SUFFIX = "pskdk4";

    public static final String   UNSAVED_WINDOW_TITLE = "(Unsaved File)";


    //  Given a full path, get just the file name, without the extension.

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
