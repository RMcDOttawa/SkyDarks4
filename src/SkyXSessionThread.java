import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SkyXSessionThread implements Runnable {
    MainWindow parent;
    DataModel   dataModel;
    SessionTimeBlock sessionTimeBlock;
    SessionFrameTableModel sessionTableModel;

    public SkyXSessionThread(MainWindow parent, DataModel dataModel, SessionTimeBlock timeBlock, SessionFrameTableModel sessionTableModel) {
        this.parent = parent;
        this.dataModel = dataModel;
        this.sessionTimeBlock = timeBlock;
        this.sessionTableModel = sessionTableModel;
    }

    @Override
    public void run() {
        try {
            //  Wait for start, and wake server if desired
            this.waitForStartTime(this.sessionTimeBlock.isStartNow(), this.sessionTimeBlock.getStartDateTime(),
                    this.dataModel.getSendWakeOnLanBeforeStarting(), this.dataModel.getSendWolSecondsBefore());
            this.optionalWakeOnLan(this.dataModel);

            //  Set up server, display autosave path
            this.console("Connecting to server.", 1);
            TheSkyXServer server = new TheSkyXServer(this.dataModel.getNetAddress(), this.dataModel.getPortNumber());
            this.displayCameraPath(server);

            //  Connect to camera and start it cooling
            this.connectToCamera(server);
            this.startCoolingCamera(server, this.dataModel.getTemperatureRegulated(), this.dataModel.getTemperatureTarget());

            // todo Measure download times
            // todo Wait for cooling target
            // todo Acquire frames until done or time
            simulateWork();
            // todo Optional warmup
            // todo Optional disconnect
        }
        catch (IOException ioEx) {
            String theMessage = ioEx.getMessage();
            if (!theMessage.endsWith(".")) {
                theMessage += ".";
            }
            this.console("Server Error: " + theMessage, 1);
        }
        catch (InterruptedException intEx) {
            //  Thread has been interrupted by user clicking Cancel
            this.cleanUpFromCancel();
        }
        this.parent.skyXSessionThreadEnded();
    }

    //  User clicked Cancel and interrupted the thread.  We don't know exactly what we were doing
    //  at the time. Clean up any operations such as camera exposures in progress.

    private void cleanUpFromCancel() {
        // todo cleanUpFromCancel
        System.out.println("cleanUpFromCancel");
    }

    private void simulateWork() throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            Thread.sleep((int)(0.5 * 1000));
            this.console("Session " + i, 2);
        }
    }

    private void console (String message, int messageLevel) {
        parent.console(message, messageLevel);
    }

    //  Wait until an appropriate start time.  That's either now (no wait), or some future time given in the
    //  provided time descriptor.  If we're asked to send a Wake On Lan before starting, the wait time is
    //  reduced by the amount of time that is to follow the Wake On Lan packet.

    private void waitForStartTime(boolean startNow,
                                  LocalDateTime startDateTime,
                                  boolean sendWolBeforeStart,
                                  int sendWolSecondsBefore) throws InterruptedException {
//        System.out.println("waitForStartTime " + startNow + ", "
//                + startDateTime.toString() + ", "
//                + ", Wake " + sendWolBeforeStart
//                + ", " + sendWolSecondsBefore);
        int secondsToWait = this.calcSecondsToWait(startNow, startDateTime, sendWolBeforeStart, sendWolSecondsBefore);
        if (secondsToWait > 0) {
            this.console("Waiting " + this.casualFormatInterval(secondsToWait), 1);
            this.sleepWithProgressBar(secondsToWait);
        }
    }

    //  Calculate how long to wait, in seconds, to reach the desired starting time, less any lead
    //  time needed to send a WOL and allow the server to start up.

    private int calcSecondsToWait(boolean startNow, LocalDateTime startDateTime, boolean sendWolBeforeStart, int sendWolSecondsBefore) {

        long waitTime = 0;
        if (!startNow) {
            ZonedDateTime zonedStartTime = ZonedDateTime.of(startDateTime, ZoneId.systemDefault());
            ZonedDateTime now = ZonedDateTime.now();
            waitTime = zonedStartTime.toEpochSecond() - now.toEpochSecond();
        }

        //  If wake-on-lan is wanted in advance, reduce wait time by that much to leave time for it
        //  Don't go less than zero, of course.  If the start time is soon, this may mean a bit of
        //  delay while the wait-on-lan lead time transpires.
        if (sendWolBeforeStart) {
            waitTime = Math.max(waitTime - sendWolSecondsBefore, 0);
        }
        return (int) waitTime;
    }

    //  Optionally send Wake On Lan packet to the server, then wait while it starts up

    private void optionalWakeOnLan(DataModel dataModel) throws IOException, InterruptedException {
        if (dataModel.getSendWakeOnLanBeforeStarting()) {
            this.console("Sending Wake-on-LAN", 1);
            byte[] broadcastAddress = RmNetUtils.parseIP4Address(dataModel.getWolBroadcastAddress());
            if (broadcastAddress.length == 0) {
                this.console("Invalid or missing broadcast address", 2);
            } else {
                byte[] macAddress = RmNetUtils.parseMacAddress(dataModel.getWolMacAddress());
                if (macAddress.length == 0) {
                    this.console("Invalid or missing MAC address", 2);
                } else {
                    RmNetUtils.sendWakeOnLan(broadcastAddress, macAddress);
                    this.console("Wake on LAN packet sent.", 2);
                    int waitSeconds = dataModel.getSendWolSecondsBefore();
                    if (waitSeconds > 0) {
                        this.console("Waiting " + this.casualFormatInterval(waitSeconds)
                        + " for server to start.", 2);
                        this.sleepWithProgressBar(waitSeconds);
                    }
                }
            }
        }
    }

    //  As a test of server connectivity and to provide some feedback to the user, we will
    //  ask the server for the path set in it's autosave parameter, and display that in the interface

    private void displayCameraPath(TheSkyXServer server) throws IOException {
        String autosavePath = server.getCameraAutosavePath();
        if (autosavePath != null) {
            this.parent.displayAutosavePath(autosavePath);
        }
    }

    //  Tell TheSkyX to establish a connection to the camera.

    private void connectToCamera(TheSkyXServer server) throws IOException {
        server.connectToCamera();
    }

    //  If the camera is temperature-regulated, turn on the cooler and start it cooling toward the
    //  target.  Cooling is asynchronous - we'll do some other chores and them come back and check progress.

    private void startCoolingCamera(TheSkyXServer server, Boolean temperatureRegulated, Double temperatureTarget) throws IOException {
        if (temperatureRegulated) {
            // Turn on camera cooling and set target
            server.setCameraCooling(true, temperatureTarget);
            this.console("Start cooling camera to target " + temperatureTarget + ".", 1);
            // Tell the UI we have started cooling so it can start displaying temperature
            this.parent.startedCooling();
        }
    }

    // Format a time interval, given in seconds, to casual language such as "1 hour, 20 minutes"

    private String casualFormatInterval(int secondsToWait) {
        String hoursString = "";
        String minutesString = "";
        String secondsString = "";


        if (secondsToWait > CommonUtils.SECONDS_IN_HOUR) {
            int hours = secondsToWait / CommonUtils.SECONDS_IN_HOUR;
            secondsToWait -= hours * CommonUtils.SECONDS_IN_HOUR;
            hoursString = hours + " hour" + ((hours > 1) ? "s" : "");
        }

        if (secondsToWait > CommonUtils.SECONDS_IN_MINUTE) {
            int minutes = secondsToWait / CommonUtils.SECONDS_IN_MINUTE;
            secondsToWait -= minutes * CommonUtils.SECONDS_IN_MINUTE;
            minutesString = minutes + " minute" + ((minutes > 1) ? "s" : "");
        }

        if (secondsToWait > 0) {
            secondsString = secondsToWait + " second" + ((secondsToWait > 1) ? "s" : "");
        }

        String result = hoursString;
        if (!minutesString.equals("")) {
            result += (!result.equals("") ? ", " : "") + minutesString;
        }
        if (!secondsString.equals("")) {
            result += (!result.equals("") ? ", " : "") + secondsString;
        }

        return result;
    }

    //  Sleep for the given number of seconds.  Do it in small bits, sending progress updates to
    //  the progress bar in the UI.

    //  Rather than counting elapsed seconds, we'll calculate the end time and watch the clock. That
    //  way the wait is not polluted by processing time, delays from garbage collection, etc.

    private void sleepWithProgressBar(int secondsToSleep) throws InterruptedException {
        if (secondsToSleep > 0) {
            this.parent.startProgressBar(0, secondsToSleep);

            int updateIntervalSeconds = this.calcUpdateInterval(secondsToSleep);
            int elapsedSeconds = 0;
            ZonedDateTime startingTimeNow = ZonedDateTime.now();
            ZonedDateTime endingTime = startingTimeNow.plusSeconds(secondsToSleep);

            while (ZonedDateTime.now().compareTo(endingTime) < 0) {
                Thread.sleep(updateIntervalSeconds * 1000);
                elapsedSeconds += updateIntervalSeconds;
                this.parent.updateProgressBar(elapsedSeconds);
            }

            this.parent.stopProgressBar();
        }
    }

    //  Get a good update interval for a progress bar.  Shorter interval, more frequent updates, for brief
    //  time periods, longer intervals for longer time periods.
    private int calcUpdateInterval(int secondsToSleep) {
        int interval;
        if (secondsToSleep < 20)
            interval = 1;
        else if (secondsToSleep < (3*60))
            interval = 2;
        else if (secondsToSleep < (10*60))
            interval = 5;
        else
            interval = 10;
        return interval;
    }

}
