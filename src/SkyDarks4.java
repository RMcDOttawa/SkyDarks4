//  Main program.
//  Does very little other than set up the Swing main window, MainWindow, and open
//  it.  The real "main" is the event loop in that window's controller class.
/*
 * Main class of this program.
 * SkyDarks4 is a program to orchestrate the use of TheSkyX to capture a library of dark and bias
 * calibration frames. TheSkyX is running on the local network and this program connects to it
 * in its "TCP Server" mode. This program has a list of all the sets of Dark and Bias frames desired,
 * and arranges for their acquisition, possibly over a series of nights during specified time periods.
 */

import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.swing.*;

public class SkyDarks4 {

    /**
     * Main program called from operating system
     * @param args      Array of string arguments to the program.  Arg[0] is the program name.
     */
    public static void main(String[] args) {
        //  If we are running on a Mac, use the system menu bar instead of windows-style window menu
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("mac os x")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        //  Create and open the main window
        try {
            //  If we were given command arguments, assume that was a data file and
            //  try to open it as a data model.  Otherwise create a new data model
            ImmutablePair<DataModel, String> modelInfo = makeDataModel(args);
            DataModel loadedDataModel = modelInfo.left;
            String windowTitle = modelInfo.right;
            MainWindow mainWindow = new MainWindow();
            mainWindow.loadDataModel(loadedDataModel, windowTitle);
            mainWindow.setFilePath(makeFilePath(windowTitle, args));
            mainWindow.makeNotDirty();
            mainWindow.setVisible(true);
        } catch (Exception e) {
            System.out.println("Uncaught exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * If the program is called with a file name argument, convert that to an absolute path name
     * @param windowTitle           Title to be assigned to the main window
     * @param args                  command-line arguments passed in to the program
     * @return (String)             Absolute path name of the specified file
     */
    private static String makeFilePath(String windowTitle, String[] args) {
        if (windowTitle.equals(CommonUtils.UNSAVED_WINDOW_TITLE)) {
            return "";
        } else if (args.length > 0) {
            return CommonUtils.simpleFileNameFromPath(args[0]);
        } else {
            return "";
        }
    }

    /**
     * Get a suitable data model.  If a file name argument is provided, try to load it.
     * Otherwise create a new default model.  Provide a suitable file name in either case.
     * @param args              Command-line arguments from application invocation
     * @return  (Pair)          Data model and window title string
     */
    private static ImmutablePair<DataModel, String> makeDataModel(String[] args) {
        DataModel resultModel;
        String resultName;
        if (args.length == 0) {
            // No command arguments, make a fresh data model
            resultModel = DataModel.newInstance();
            resultName = CommonUtils.UNSAVED_WINDOW_TITLE;
        } else {
            //  Try to make a data model from this file
            resultModel = DataModel.tryLoadFromFile(args[0]);
            if (resultModel == null) {
                JOptionPane.showMessageDialog(null,
                        "File provided does not exist or is not a valid\ndata file. Creating an empty file instead.");
                resultModel = DataModel.newInstance();
                resultName = CommonUtils.UNSAVED_WINDOW_TITLE;
            } else {
                resultName = CommonUtils.simpleFileNameFromPath(args[0]);
            }
        }
        return ImmutablePair.of(resultModel, resultName);
    }
}
