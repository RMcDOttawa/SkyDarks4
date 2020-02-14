import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class DataModel {
    //  Location of the site (for calculating sun times)
    private String locationName = "EWHO";
    private int timeZone = -5;
    private double latitude = 45.309645;
    private double longitude = -75.886471;
    public static final double LATITUDE_NULL = -99999.0;
    public static final double LONGITUDE_NULL = -99999.0;

    //  Info about when the run starts
    private StartDate startDateType = StartDate.TODAY;          // What day?
    private StartTime startTimeType = StartTime.CIVIL_DUSK;     // What time on that day?
    private LocalDate givenStartDate = null;                    // If specific date given
    private LocalTime givenStartTime = null;                    // If specific time given

    //  Info about when the run stops
    private EndDate endDateType = EndDate.TODAY_TOMORROW;       // What day (or just when done)?
    private EndTime endTimeType = EndTime.CIVIL_DAWN;           // What time that day (if applicable)?
    private LocalDate givenEndDate = null;                      // If specific date given
    private LocalTime givenEndTime = null;                      // If specific time given

    //  What do we do when we're done?
    private boolean disconnectWhenDone      = true;             // Disconnect camera?
    private boolean warmUpWhenDone          = true;             // Allow camera to warm up first?
    private int warmUpWhenDoneSeconds   = 300;              //  Warm up for how long before disconnect?

    //  Do we want a "Wake on Lan" command sent to the host computer
    //  as part of starting up the run?

    private boolean sendWakeOnLanBeforeStarting = true;         // Send a WOL command?
    private int sendWolSecondsBefore = 15 * 60;             // Send WOL this many seconds before start
    private String wolMacAddress = "74-27-ea-5a-7c-66";         // MAC address to receive WOL command
    private String wolBroadcastAddress = "255.255.255.255";     //  Hits entire current sublan (rarely changed)

    //  Network address information
    private String netAddress = "localhost";
    private int portNumber = 3040;     //  The default from TheSkyX TCP server

    //  Info about temperature regulation for the run

    private boolean temperatureRegulated = true;                //  Use camera temperature regulation
    private double temperatureTarget = 0.0;                     //  Camera setpoint target temperature
    private double temperatureWithin = 0.1;                     //  Start when temp at target within this much
    private int temperatureSettleSeconds = 60;              //  Check temp every this often while cooling
    private int maxCoolingWaitTime = 30 * 60;               //  Try cooling for this long in one attempt
    private int temperatureFailRetryCount = 5;              //  If can't cool to target, wait and retry this often
    private int temperatureFailRetryDelaySeconds = 300;     //      Delay between cooling retries
    private boolean temperatureAbortOnRise = true;              //  Abort run if temperature rises
    private double temperatureAbortRiseLimit = 1.0;             //       this much

    //  List of frames to be collected during the run

    //  The list of FrameSets that drives the table in the UI .

    private ArrayList<FrameSet> savedFrameSets = new ArrayList<FrameSet>(100);

    //  Information dealing with saving the model to a file or reading from a file

    private transient static final String dataFileSuffix = "pskdk4";
    private boolean autoSaveAfterEachFrame = true;

    // Getters and Setters

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public int getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(int timeZone) {
        this.timeZone = timeZone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public StartDate getStartDateType() {
        return startDateType;
    }

    public void setStartDateType(StartDate startDateType) {
        this.startDateType = startDateType;
    }

    public StartTime getStartTimeType() {
        return startTimeType;
    }

    public void setStartTimeType(StartTime startTimeType) {
        this.startTimeType = startTimeType;
    }

    public LocalDate getGivenStartDate() {
        return givenStartDate;
    }

    public void setGivenStartDate(LocalDate givenStartDate) {
        this.givenStartDate = givenStartDate;
    }

    public LocalTime getGivenStartTime() {
        return givenStartTime;
    }

    public void setGivenStartTime(LocalTime givenStartTime) {
        this.givenStartTime = givenStartTime;
    }

    public EndDate getEndDateType() {
        return endDateType;
    }

    public void setEndDateType(EndDate endDateType) {
        this.endDateType = endDateType;
    }

    public EndTime getEndTimeType() {
        return endTimeType;
    }

    public void setEndTimeType(EndTime endTimeType) {
        this.endTimeType = endTimeType;
    }

    public LocalDate getGivenEndDate() {
        return givenEndDate;
    }

    public void setGivenEndDate(LocalDate givenEndDate) {
        this.givenEndDate = givenEndDate;
    }

    public LocalTime getGivenEndTime() {
        return givenEndTime;
    }

    public void setGivenEndTime(LocalTime givenEndTime) {
        this.givenEndTime = givenEndTime;
    }

    public boolean getDisconnectWhenDone() {
        return disconnectWhenDone;
    }

    public void setDisconnectWhenDone(boolean disconnectWhenDone) {
        this.disconnectWhenDone = disconnectWhenDone;
    }

    public boolean getWarmUpWhenDone() {
        return warmUpWhenDone;
    }

    public void setWarmUpWhenDone(boolean warmUpWhenDone) {
        this.warmUpWhenDone = warmUpWhenDone;
    }

    public int getWarmUpWhenDoneSeconds() {
        return warmUpWhenDoneSeconds;
    }

    public void setWarmUpWhenDoneSeconds(int warmUpWhenDoneSeconds) {
        this.warmUpWhenDoneSeconds = warmUpWhenDoneSeconds;
    }

    public boolean getSendWakeOnLanBeforeStarting() {
        return sendWakeOnLanBeforeStarting;
    }

    public void setSendWakeOnLanBeforeStarting(boolean sendWakeOnLanBeforeStarting) {
        this.sendWakeOnLanBeforeStarting = sendWakeOnLanBeforeStarting;
    }

    public int getSendWolSecondsBefore() {
        return sendWolSecondsBefore;
    }

    public void setSendWolSecondsBefore(int sendWolSecondsBefore) {
        this.sendWolSecondsBefore = sendWolSecondsBefore;
    }

    public String getWolMacAddress() {
        return wolMacAddress;
    }

    public void setWolMacAddress(String wolMacAddress) {
        this.wolMacAddress = wolMacAddress;
    }

    public String getWolBroadcastAddress() {
        return wolBroadcastAddress;
    }

    public void setWolBroadcastAddress(String wolBroadcastAddress) {
        this.wolBroadcastAddress = wolBroadcastAddress;
    }

    public String getNetAddress() {
        return netAddress;
    }

    public void setNetAddress(String netAddress) {
        this.netAddress = netAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public boolean getTemperatureRegulated() {
        return temperatureRegulated;
    }

    public void setTemperatureRegulated(boolean temperatureRegulated) {
        this.temperatureRegulated = temperatureRegulated;
    }

    public double getTemperatureTarget() {
        return temperatureTarget;
    }

    public void setTemperatureTarget(double temperatureTarget) {
        this.temperatureTarget = temperatureTarget;
    }

    public double getTemperatureWithin() {
        return temperatureWithin;
    }

    public void setTemperatureWithin(double temperatureWithin) {
        this.temperatureWithin = temperatureWithin;
    }

    public int getTemperatureSettleSeconds() {
        return temperatureSettleSeconds;
    }

    public void setTemperatureSettleSeconds(int temperatureSettleSeconds) {
        this.temperatureSettleSeconds = temperatureSettleSeconds;
    }

    public int getMaxCoolingWaitTime() {
        return maxCoolingWaitTime;
    }

    public void setMaxCoolingWaitTime(int maxCoolingWaitTime) {
        this.maxCoolingWaitTime = maxCoolingWaitTime;
    }

    public int getTemperatureFailRetryCount() {
        return temperatureFailRetryCount;
    }

    public void setTemperatureFailRetryCount(int temperatureFailRetryCount) {
        this.temperatureFailRetryCount = temperatureFailRetryCount;
    }

    public int getTemperatureFailRetryDelaySeconds() {
        return temperatureFailRetryDelaySeconds;
    }

    public void setTemperatureFailRetryDelaySeconds(int temperatureFailRetryDelaySeconds) {
        this.temperatureFailRetryDelaySeconds = temperatureFailRetryDelaySeconds;
    }

    public boolean getTemperatureAbortOnRise() {
        return temperatureAbortOnRise;
    }

    public void setTemperatureAbortOnRise(boolean temperatureAbortOnRise) {
        this.temperatureAbortOnRise = temperatureAbortOnRise;
    }

    public double getTemperatureAbortRiseLimit() {
        return temperatureAbortRiseLimit;
    }

    public void setTemperatureAbortRiseLimit(double temperatureAbortRiseLimit) {
        this.temperatureAbortRiseLimit = temperatureAbortRiseLimit;
    }

    public ArrayList<FrameSet> getSavedFrameSets() {
        return savedFrameSets;
    }

    public void setSavedFrameSets(ArrayList<FrameSet> savedFrameSets) {
        this.savedFrameSets = savedFrameSets;
    }

    public boolean getAutoSaveAfterEachFrame() {
        return autoSaveAfterEachFrame;
    }

    public void setAutoSaveAfterEachFrame(boolean autoSaveAfterEachFrame) {
        this.autoSaveAfterEachFrame = autoSaveAfterEachFrame;
    }

    //  Pseudo-properties (i.e. Get methods for non-existant attributes) that are used in bindings
    //  in the ui to enable and disable fields on more complex situations.

    public boolean getEnableStartTimePicker() {
        System.out.println("getEnableStartTimePicker");
        return true;
    }

    //  Creator static factories

    private DataModel () {}

    public static DataModel newInstance() {
        DataModel newModel = new DataModel();

//        The following lines are used to insert some initial framesets into the data model for testing.
//        In production, we don't insert any, so the user is presented with an initial empty list.

        FrameSet f1 =  FrameSet.of(10, FrameType.BIAS_FRAME, 0.0, 1, 0);
        FrameSet f2 =  FrameSet.of(10, FrameType.BIAS_FRAME, 0.0, 2, 0);
        FrameSet f3 =  FrameSet.of(12, FrameType.DARK_FRAME, 10.0, 1, 0);
        FrameSet f4 =  FrameSet.of(14, FrameType.DARK_FRAME, 20.0, 2, 0);
        FrameSet f5 =  FrameSet.of(16, FrameType.DARK_FRAME, 30.0, 3, 0);
//
        newModel.getSavedFrameSets().add(f1);
        newModel.getSavedFrameSets().add(f2);
        newModel.getSavedFrameSets().add(f3);
        newModel.getSavedFrameSets().add(f4);
        newModel.getSavedFrameSets().add(f5);

        return newModel;
    }

}

