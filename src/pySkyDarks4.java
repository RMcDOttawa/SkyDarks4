//  Main program.
//  Does very little other than set up the Swing main window, MainWindow, and open
//  it.  The real "main" is the event loop in that window's controller class.

import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.swing.*;

public class pySkyDarks4 {


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

    private static String makeFilePath(String windowTitle, String[] args) {
        if (windowTitle.equals(CommonUtils.UNSAVED_WINDOW_TITLE)) {
            return "";
        } else if (args.length > 0) {
            return CommonUtils.simpleFileNameFromPath(args[0]);
        } else {
            return "";
        }
    }

    //  Get a data model.  If a file name argument is provided, try to load it.
    //  Otherwise create a new default model.  Provide a suitable file name in either case.

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
