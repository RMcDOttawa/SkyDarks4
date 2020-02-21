import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

public class SkyXSessionThread implements Runnable {

    private static final int COOLING_MONITOR_INTERVAL = 5;
    private CoolingMonitorTask coolingMonitorTask = null;
    private Timer coolingMonitorTimer = null;

    private HashMap<Integer, Double> downloadTimes = null;

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
        TheSkyXServer server = null;
        try {
            //  Wait for start, and wake server if desired
            this.waitForStartTime(this.sessionTimeBlock.isStartNow(), this.sessionTimeBlock.getStartDateTime(),
                    this.dataModel.getSendWakeOnLanBeforeStarting(), this.dataModel.getSendWolSecondsBefore());
            this.optionalWakeOnLan(this.dataModel);

            //  Set up server, display autosave path
            this.console("Connecting to server.", 1);
            server = new TheSkyXServer(this.dataModel.getNetAddress(), this.dataModel.getPortNumber());
            this.displayCameraPath(server);

            //  Connect to camera and start it cooling
            this.connectToCamera(server);
            this.startCoolingCamera(server, this.dataModel.getTemperatureRegulated(), this.dataModel.getTemperatureTarget());

            // While the camera is cooling, measure download times for the binnings we'll be using
            this.measureDownloadTimes(server);

            // We're out of things to do until the camera reaches its target temperature
            // Wait for it.  Note that it can fail - if ambient is higher than the cooler can handle
            if (this.waitForCoolingTarget(server)) {
                // todo Acquire frames until done or time
                this.startCoolingMonitor(server);
                simulateWork();
            }
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
            this.cleanUpFromCancel(server);
        }
        finally {
            this.stopCoolingMonitor();
            this.parent.skyXSessionThreadEnded();
        }
    }


    // For all fo the binning values we'll be using, take a bias frame and time it.
    //  Since bias frames are zero-length, any time that passes is the download time for the
    //  camera at that binning value.  Record these in a hashmap so we have them availble for
    //  reference when estimating the time that actual exposures will take.

    private void measureDownloadTimes(TheSkyXServer server) throws IOException {
        //  Create empty table
        this.downloadTimes = new HashMap<>(4);
        this.console("Measuring download times.", 1);

        //  Loop through all planned framesets.  For any binning not already in table, time it
        ArrayList<FrameSet> frameSets = this.sessionTableModel.getSessionFramesets();
        for (FrameSet frameSet : frameSets) {
            Integer binning = frameSet.getBinning();
            if (!this.downloadTimes.containsKey(binning)) {
                // We don't have a time for this binning value.  Time it now.
                Double downloadTime = this.timeDownload(server, binning);
                this.downloadTimes.put(binning, downloadTime);
            }
        }
    }

    //  Time how long the camera download takes for a bias frame of given binning

    private Double timeDownload(TheSkyXServer server, Integer binning) throws IOException {
        LocalDateTime timeBefore = LocalDateTime.now();
        this.exposeFrame(server, FrameType.BIAS_FRAME, 0.0, binning, false, false);
        LocalDateTime timeAfter = LocalDateTime.now();
        Duration timeTaken = Duration.between(timeAfter, timeBefore).abs();
        double downloadSeconds = timeTaken.getSeconds();
        this.console(String.format("Binned %d x %d: %.1f seconds.", binning, binning, downloadSeconds), 2);
        return downloadSeconds;
    }

    private void exposeFrame(TheSkyXServer server, FrameType frameType, double exposureSeconds, Integer binning,
                             boolean asynchronous, boolean autoSave) throws IOException {
//        System.out.println("exposeFrame(" + frameType + "," + exposureSeconds + ","
//                + binning + "," + asynchronous + "," + autoSave + ")");
        server.exposeFrame(frameType, exposureSeconds, binning, asynchronous, autoSave);
    }

    //  User clicked Cancel and interrupted the thread.  We don't know exactly what we were doing
    //  at the time. Clean up any operations such as camera exposures in progress.

    private void cleanUpFromCancel(TheSkyXServer server) {
        // todo cleanUpFromCancel
        System.out.println("cleanUpFromCancel");

        // Abort any camera operation in progress
        if (server != null) {
            try {
                server.abortImageInProgress();
            } catch (IOException e) {
                // Stop any exception here so we don't get into a cancellation loop
            }
        }

        // If cooling monitor timer is running, stop it.
        if (this.coolingMonitorTimer != null) {
            this.stopCoolingMonitor();
        }
    }

    private void simulateWork() throws InterruptedException {
        this.console("Simulating work.", 1);
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
        }
    }

    //  Set up a periodic timer that will prompt us to get the camera power and temperature
    //  and pass them up to the main window for displaying in the UI.

    private void startCoolingMonitor(TheSkyXServer server) {
        this.coolingMonitorTask = new CoolingMonitorTask(this, server);
        this.coolingMonitorTimer = new Timer();
        this.coolingMonitorTimer.scheduleAtFixedRate(this.coolingMonitorTask,
                COOLING_MONITOR_INTERVAL * 1000,
                COOLING_MONITOR_INTERVAL * 1000);
    }

    //  this method is called by the cooling monitor.  Get temp and power and pass to UI

    public void fireCoolingMonitor(TheSkyXServer server) {
        try {
            ImmutablePair<Double, Double> temperatureInfo = server.getCameraTemperatureAndPower();
            double temperature = temperatureInfo.left;
            double coolerPower = temperatureInfo.right;
            this.parent.reportCoolingStatus(temperature, coolerPower);
        } catch (IOException e) {
            // Ignore this exception
        }
    }

    private void stopCoolingMonitor() {
        if (this.coolingMonitorTimer != null) {
            this.coolingMonitorTimer.cancel();
            this.coolingMonitorTimer = null;
        }
        if (this.coolingMonitorTask != null) {
            this.coolingMonitorTask.cancel();
            this.coolingMonitorTask = null;
        }
        this.parent.hideCoolingStatus();
    }

    //  Assuming the camera is temperature-regulated, wait for the cooling target temperature to be
    //  reached.  This can fail - if the ambient temperature is so high that it is beyond the ability
    //  of the camera's cooler to lower the temperature to the target.  If this happens, we can optionally
    //  give the cooler a rest and then try again. The idea is that the ambient temperature is falling
    //  as night goes on, so a later attempt may succeed.  Note that the very first cooling attempt will
    //  be given a little more time than what is specified, since the camera has been cooling while we
    //  measured the download times.  This matters only if it times out, which is rare, so we're not
    //  worrying about it.

    private boolean waitForCoolingTarget(TheSkyXServer server) throws InterruptedException, IOException {
        boolean success = true;
        if (this.dataModel.getTemperatureRegulated()) {
            this.console(String.format("Waiting for camera to cool to target of %.1f",
                    this.dataModel.getTemperatureTarget()), 1);
            int totalAttempts = 1 + this.dataModel.getTemperatureFailRetryCount();
            while (totalAttempts > 0) {
                totalAttempts -= 1;
                success = this.oneCoolingAttempt(server);
                if (success) {
                    break;
                } else {
                    // Failed to cool to target.  Turn off cooling.
                    this.stopCooling(server);
                    //  If more attempts are remaining, wait then try again
                    if (totalAttempts > 0) {
                        this.console("Cooling failed to reach target temp "
                                + this.dataModel.getTemperatureTarget() + ".", 1);
                        this.console("Waiting " + this.casualFormatInterval(this.dataModel.getTemperatureFailRetryDelaySeconds())
                                + " before next attempt.", 2);
                        this.sleepWithProgressBar(this.dataModel.getTemperatureFailRetryDelaySeconds());
                        this.startCoolingCamera(server, true, this.dataModel.getTemperatureTarget());
                    }
                }
            }
            if (!success) {
                this.console("Failed to cool to target temperature in "
                + this.dataModel.getTemperatureFailRetryCount() + " tries.", 1);
            }
        }
        return success;
    }

    //  Turn off the camera's cooler circuit

    private void stopCooling(TheSkyXServer server) throws IOException {
        server.setCameraCooling(false, 0.0);
    }

    //  Make one attempt to cool to the target.  The cooling is already underway.  At given intervals
    //  we will get the temperature from the camera, and consider cooling done if we are within a
    //  certain distance of the target.  We'll run a progress bar against the total time we're allowed
    //  to wait.  If we don't reach the temperature in that limit, we return false so the caller can
    //  decide whether to try again.

    private boolean oneCoolingAttempt(TheSkyXServer server) throws InterruptedException, IOException {
        boolean success = false;

        int maxSecondsToWait = this.dataModel.getMaxCoolingWaitTime();
        this.parent.startProgressBar(0, maxSecondsToWait);
        int totalTimeWaited = 0;
        int waitInterval = this.dataModel.getTemperatureSettleSeconds();
        while (totalTimeWaited < maxSecondsToWait) {
            //  Wait a brief while before next temperature check
            Thread.sleep(waitInterval * 1000);
            //  Update progress bar to reflect that we've waited
            totalTimeWaited += waitInterval;
            parent.updateProgressBar(totalTimeWaited);
            ImmutablePair<Double, Double> tempAndPower = server.getCameraTemperatureAndPower();
            double currentTemperature = tempAndPower.left;
            double coolerPower = tempAndPower.right;
            this.console(String.format("Camera temperature %.1f, cooler power %.0f%%", currentTemperature, coolerPower), 2);
            double temperatureDifference = Math.abs(currentTemperature - this.dataModel.getTemperatureTarget());
            if (temperatureDifference <= this.dataModel.getTemperatureWithin()) {
                success = true;
                break;
            }
        }
        this.parent.stopProgressBar();
        if (success) {
            this.console("Target temperature reached.", 1);
        }
        return success;
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
