import luckycatlabs.Location;
import luckycatlabs.SunriseSunsetCalculator;
import org.xml.sax.InputSource;

import java.beans.PropertyChangeSupport;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Object representing the data model for the program.  This object contains all of the
 * persistent data needed for a session.  Saving it to a file and restoring it later gives
 * a complete recreation of the session.
 *
 * Note that the instance variables are set up as Java Beans properties so the XMLEncoder
 * and XMLDecoder services can operate on them.
 *
 * Also note that this doesn't work for the LocalDate and LocalTime objects, so those
 * are kludge-converted to strings when saving and restoring the model to a file
 */
public class DataModel  implements Serializable {
    //  Version control of the data model, to allow backward-compatibility of saved files

    @SuppressWarnings("unused")
    private Integer modelVersion = 1;

    //  Location of the site (for calculating sun times)
    private String locationName = "EWHO";
    private String timeZone = "EST";
    private Double latitude = 45.;
    private Double longitude = -75.;
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

    //  LocalDate and LocalTime objects don't XML Serialize, so we will store string
    //  representations of them when saving the file
    private String givenStartDateAsString = "";
    private String givenStartTimeAsString = "";
    private String givenEndDateAsString = "";
    private String givenEndTimeAsString = "";

    //  What do we do when we're done?
    private Boolean disconnectWhenDone      = true;             // Disconnect camera?
    private Boolean warmUpWhenDone          = true;             // Allow camera to warm up first?
    private Integer warmUpWhenDoneSeconds   = 300;              //  Warm up for how long before disconnect?

    //  Do we want a "Wake on Lan" command sent to the host computer
    //  as part of starting up the run?

    private Boolean sendWakeOnLanBeforeStarting = true;         // Send a WOL command?
    private Integer sendWolSecondsBefore = 15 * 60;             // Send WOL this many seconds before start
    private String wolMacAddress = "E4:54:E8:D4:11:36";         // MAC address to receive WOL command
    private String wolBroadcastAddress = "255.255.255.255";     //  Hits entire current sublan (rarely changed)

    //  Network address information
    private String netAddress = "localhost";
    private Integer portNumber = 3040;     //  The default from TheSkyX TCP server

    //  Info about temperature regulation for the run

    private Boolean temperatureRegulated = true;                //  Use camera temperature regulation
    private Double temperatureTarget = 0.0;                     //  Camera set point target temperature
    private Double temperatureWithin = 0.1;                     //  Start when temp at target within this much
    private Integer temperatureSettleSeconds = 60;              //  Check temp every this often while cooling
    private Integer maxCoolingWaitTime = 30 * 60;               //  Try cooling for this long in one attempt
    private Integer temperatureFailRetryCount = 5;              //  If can't cool to target, wait and retry this often
    private Integer temperatureFailRetryDelaySeconds = 300;     //      Delay between cooling retries
    private Boolean temperatureAbortOnRise = true;              //  Abort run if temperature rises
    private Double temperatureAbortRiseLimit = 1.0;             //       this much

    //  List of frames to be collected during the run

    //  The list of FrameSets that drives the table in the UI .

    private ArrayList<FrameSet> savedFrameSets = new ArrayList<>(100);

    //  Information dealing with saving the model to a file or reading from a file

    private Boolean autoSaveAfterEachFrame = true;

    //  Java beans change support methods (see JSR 295)

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);


    // Getters and Setters

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String newName) {
        String oldName = this.locationName;
        this.locationName = newName;
        changeSupport.firePropertyChange("location", oldName, newName);
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String newTimeZone) {
        String oldTimeZone = this.timeZone;
        this.timeZone = newTimeZone;
        changeSupport.firePropertyChange("timeZone", oldTimeZone, newTimeZone);
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double newLatitude) {
        Double oldLatitude = this.latitude;
        this.latitude = newLatitude;
        changeSupport.firePropertyChange("latitude", oldLatitude, newLatitude);
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double newLongitude) {
        Double oldLongitude = this.longitude;
        this.longitude = newLongitude;
        changeSupport.firePropertyChange("longitude", oldLongitude, newLongitude);
    }

    public StartDate getStartDateType() {
        return startDateType;
    }

    public void setStartDateType(StartDate newStartDateType) {
        StartDate oldStartDateType = this.startDateType;
        this.startDateType = newStartDateType;
        changeSupport.firePropertyChange("startDateType", oldStartDateType, newStartDateType);
    }

    public StartTime getStartTimeType() {
        return startTimeType;
    }

    public void setStartTimeType(StartTime newStartTimeType) {
        StartTime oldStartTimeType = this.startTimeType;
        this.startTimeType = newStartTimeType;
        changeSupport.firePropertyChange("startTimeType", oldStartTimeType, newStartTimeType);
    }

    public LocalDate getGivenStartDate() {
        return givenStartDate;
    }

    public void setGivenStartDate(LocalDate newGivenDate) {
        LocalDate oldGivenDate = this.givenStartDate;
        this.givenStartDate = newGivenDate;
        changeSupport.firePropertyChange("givenStartDate", oldGivenDate, newGivenDate);
    }

    public LocalTime getGivenStartTime() {
        return givenStartTime;
    }

    public void setGivenStartTime(LocalTime newGivenStartTime) {
        LocalTime oldGivenStartTime = this.givenStartTime;
        this.givenStartTime = newGivenStartTime;
        changeSupport.firePropertyChange("givenStartTime", oldGivenStartTime, newGivenStartTime);
    }

    public EndDate getEndDateType() {
        return endDateType;
    }

    public void setEndDateType(EndDate newEndDateType) {
        EndDate oldEndDateType = this.endDateType;
        this.endDateType = newEndDateType;
        changeSupport.firePropertyChange("endDateType", oldEndDateType, newEndDateType);
    }

    public EndTime getEndTimeType() {
        return endTimeType;
    }

    public void setEndTimeType(EndTime newEndTimeType) {
        EndTime oldEndTimeType = this.endTimeType;
        this.endTimeType = newEndTimeType;
        changeSupport.firePropertyChange("endTimeType", oldEndTimeType, newEndTimeType);
    }

    public LocalDate getGivenEndDate() {
        return givenEndDate;
    }

    public void setGivenEndDate(LocalDate newGivenEndDate) {
        LocalDate oldGivenEndDate = this.givenEndDate;
        this.givenEndDate = newGivenEndDate;
        changeSupport.firePropertyChange("givenEndDate", oldGivenEndDate, newGivenEndDate);
    }

    public LocalTime getGivenEndTime() {
        return givenEndTime;
    }

    public void setGivenEndTime(LocalTime newGivenEndTime) {
        LocalTime oldGivenEndTime = this.givenEndTime;
        this.givenEndTime = newGivenEndTime;
        changeSupport.firePropertyChange("givenEndTime", oldGivenEndTime, newGivenEndTime);
    }

    @SuppressWarnings("unused")
    public String getGivenStartDateAsString() {
        return givenStartDateAsString;
    }

    @SuppressWarnings("unused")
    public void setGivenStartDateAsString(String givenStartDateAsString) {
        this.givenStartDateAsString = givenStartDateAsString;
    }

    @SuppressWarnings("unused")
    public String getGivenStartTimeAsString() {
        return givenStartTimeAsString;
    }

    @SuppressWarnings("unused")
    public void setGivenStartTimeAsString(String givenStartTimeAsString) {
        this.givenStartTimeAsString = givenStartTimeAsString;
    }

    @SuppressWarnings("unused")
    public String getGivenEndDateAsString() {
        return givenEndDateAsString;
    }

    @SuppressWarnings("unused")
    public void setGivenEndDateAsString(String givenEndDateAsString) {
        this.givenEndDateAsString = givenEndDateAsString;
    }

    @SuppressWarnings("unused")
    public String getGivenEndTimeAsString() {
        return givenEndTimeAsString;
    }

    @SuppressWarnings("unused")
    public void setGivenEndTimeAsString(String givenEndTimeAsString) {
        this.givenEndTimeAsString = givenEndTimeAsString;
    }

    public Boolean getDisconnectWhenDone() {
        return disconnectWhenDone;
    }

    public void setDisconnectWhenDone(Boolean newDisconnectWhenDone) {
        Boolean oldDisconnectWhenDone = this.disconnectWhenDone;
        this.disconnectWhenDone = newDisconnectWhenDone;
        changeSupport.firePropertyChange("disconnectWhenDone",
                oldDisconnectWhenDone, newDisconnectWhenDone);
    }

    public Boolean getWarmUpWhenDone() {
        return this.warmUpWhenDone;
    }

    public void setWarmUpWhenDone(Boolean newWarmUpWhenDone) {
        Boolean oldWarmUpWhenDone = this.warmUpWhenDone;
        this.warmUpWhenDone = newWarmUpWhenDone;
        changeSupport.firePropertyChange("warmUpWhenDone", oldWarmUpWhenDone, newWarmUpWhenDone);
    }

    public Integer getWarmUpWhenDoneSeconds() {
        return warmUpWhenDoneSeconds;
    }

    public void setWarmUpWhenDoneSeconds(Integer newWarmUpWhenDoneSeconds) {
        Integer oldWarmUpWhenDoneSeconds = this.warmUpWhenDoneSeconds;
        this.warmUpWhenDoneSeconds = newWarmUpWhenDoneSeconds;
        changeSupport.firePropertyChange("warmUpWhenDoneSeconds",
                oldWarmUpWhenDoneSeconds, newWarmUpWhenDoneSeconds);
    }

    public Boolean getSendWakeOnLanBeforeStarting() {
        return sendWakeOnLanBeforeStarting;
    }

    public void setSendWakeOnLanBeforeStarting(Boolean newSendWakeOnLanBeforeStarting) {
        Boolean oldSendWakeOnLanBeforeStarting = this.sendWakeOnLanBeforeStarting;
        this.sendWakeOnLanBeforeStarting = newSendWakeOnLanBeforeStarting;
        changeSupport.firePropertyChange("sendWakeOnLanBeforeStarting",
                oldSendWakeOnLanBeforeStarting, newSendWakeOnLanBeforeStarting);
    }

    public Integer getSendWolSecondsBefore() {
        return sendWolSecondsBefore;
    }

    public void setSendWolSecondsBefore(Integer newSendWolSecondsBefore) {
        Integer oldSendWolSecondsBefore = this.sendWolSecondsBefore;
        this.sendWolSecondsBefore = newSendWolSecondsBefore;
        changeSupport.firePropertyChange("sendWolSecondsBefore",
                oldSendWolSecondsBefore, newSendWolSecondsBefore);
    }

    public String getWolMacAddress() {
        return wolMacAddress;
    }

    public void setWolMacAddress(String newWolMacAddress) {
        String oldWolMacAddress = this.wolMacAddress;
        this.wolMacAddress = newWolMacAddress;
        changeSupport.firePropertyChange("wolMacAddress", oldWolMacAddress, newWolMacAddress);
    }

    public String getWolBroadcastAddress() {
        return wolBroadcastAddress;
    }

    public void setWolBroadcastAddress(String newWolBroadcastAddress) {
        String oldWolBroadcastAddress = this.wolBroadcastAddress;
        this.wolBroadcastAddress = newWolBroadcastAddress;
        changeSupport.firePropertyChange("wolBroadcastAddress",
                oldWolBroadcastAddress, newWolBroadcastAddress);
    }

    public String getNetAddress() {
        return netAddress;
    }

    public void setNetAddress(String newNetAddress) {
        String oldNetAddress = this.netAddress;
        this.netAddress = newNetAddress;
        changeSupport.firePropertyChange("netAddress", oldNetAddress, newNetAddress);
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer newPortNumber) {
        Integer oldPortNumber = this.portNumber;
        this.portNumber = newPortNumber;
        changeSupport.firePropertyChange("portNumber", oldPortNumber, newPortNumber);
    }

    public Boolean getTemperatureRegulated() {
        return temperatureRegulated;
    }

    public void setTemperatureRegulated(Boolean newTemperatureRegulated) {
        Boolean oldTemperatureRegulated = this.temperatureRegulated;
        this.temperatureRegulated = newTemperatureRegulated;
        changeSupport.firePropertyChange("temperatureRegulated",
                oldTemperatureRegulated, newTemperatureRegulated);
    }

    public Double getTemperatureTarget() {
        return temperatureTarget;
    }

    public void setTemperatureTarget(Double newTemperatureTarget) {
        Double oldTemperatureTarget = this.temperatureTarget;
        this.temperatureTarget = newTemperatureTarget;
        changeSupport.firePropertyChange("temperatureTarget",
                oldTemperatureTarget, newTemperatureTarget);
    }

    public Double getTemperatureWithin() {
        return temperatureWithin;
    }

    public void setTemperatureWithin(Double newTemperatureWithin) {
        Double oldTemperatureWithin = this.temperatureWithin;
        this.temperatureWithin = newTemperatureWithin;
        changeSupport.firePropertyChange("temperatureWithin", oldTemperatureWithin, newTemperatureWithin);
    }

    public Integer getTemperatureSettleSeconds() {
        return temperatureSettleSeconds;
    }

    public void setTemperatureSettleSeconds(Integer newTemperatureSettleSeconds) {
        Integer oldTemperatureSettleSeconds = this.temperatureSettleSeconds;
        this.temperatureSettleSeconds = newTemperatureSettleSeconds;
        changeSupport.firePropertyChange("temperatureSettleSeconds",
                oldTemperatureSettleSeconds,
                newTemperatureSettleSeconds);
    }

    public Integer getMaxCoolingWaitTime() {
        return maxCoolingWaitTime;
    }

    public void setMaxCoolingWaitTime(Integer newMaxCoolingWaitTime) {
        Integer oldMaxCoolingWaitTime = this.maxCoolingWaitTime;
        this.maxCoolingWaitTime = newMaxCoolingWaitTime;
        changeSupport.firePropertyChange("maxCoolingWaitTime", oldMaxCoolingWaitTime, newMaxCoolingWaitTime);
    }

    public Integer getTemperatureFailRetryCount() {
        return temperatureFailRetryCount;
    }

    public void setTemperatureFailRetryCount(Integer newTemperatureFailRetryCount) {
        Integer oldTemperatureFailRetryCount = this.temperatureFailRetryCount;
        this.temperatureFailRetryCount = newTemperatureFailRetryCount;
        changeSupport.firePropertyChange("temperatureFailRetryCount",
                oldTemperatureFailRetryCount,
                newTemperatureFailRetryCount);
    }

    public Integer getTemperatureFailRetryDelaySeconds() {
        return temperatureFailRetryDelaySeconds;
    }

    public void setTemperatureFailRetryDelaySeconds(Integer newTemperatureFailRetryDelaySeconds) {
        Integer oldTemperatureFailRetryDelaySeconds = this.temperatureFailRetryDelaySeconds;
        this.temperatureFailRetryDelaySeconds = newTemperatureFailRetryDelaySeconds;
        changeSupport.firePropertyChange("temperatureFailRetryDelaySeconds",
                oldTemperatureFailRetryDelaySeconds,
                newTemperatureFailRetryDelaySeconds);
    }

    public Boolean getTemperatureAbortOnRise() {
        return temperatureAbortOnRise;
    }

    public void setTemperatureAbortOnRise(Boolean newTemperatureAbortOnRise) {
        Boolean oldTemperatureAbortOnRise = this.temperatureAbortOnRise;
        this.temperatureAbortOnRise = newTemperatureAbortOnRise;
        changeSupport.firePropertyChange("temperatureAbortOnRise",
                oldTemperatureAbortOnRise,
                newTemperatureAbortOnRise);
    }

    public Double getTemperatureAbortRiseLimit() {
        return temperatureAbortRiseLimit;
    }

    public void setTemperatureAbortRiseLimit(Double newTemperatureAbortRiseLimit) {
        Double oldTemperatureAbortRiseLimit = this.temperatureAbortRiseLimit;
        this.temperatureAbortRiseLimit = newTemperatureAbortRiseLimit;
        changeSupport.firePropertyChange("temperatureAbortRiseLimit",
                oldTemperatureAbortRiseLimit,
                newTemperatureAbortRiseLimit);
    }

    public ArrayList<FrameSet> getSavedFrameSets() {
        return savedFrameSets;
    }

    @SuppressWarnings("unused")
    public void setSavedFrameSets(ArrayList<FrameSet> savedFrameSets) {
        this.savedFrameSets = savedFrameSets;
    }

    public Boolean getAutoSaveAfterEachFrame() {
        return autoSaveAfterEachFrame;
    }

    public void setAutoSaveAfterEachFrame(Boolean newAutoSaveAfterEachFrame) {
        Boolean oldAutoSaveAfterEachFrame = this.autoSaveAfterEachFrame;
        this.autoSaveAfterEachFrame = newAutoSaveAfterEachFrame;
        changeSupport.firePropertyChange("autoSaveAfterEachFrame",
                oldAutoSaveAfterEachFrame,
                newAutoSaveAfterEachFrame);
    }

    //  Creator static factories

    /**
     * Naked creator function.  Don't use this - use the newInstance static constructor
     */
    private DataModel () {}

    private static final boolean CREATE_TESTING_FRAMESETS = false;

    /**
     * Create a new instance of the data model with default values.
     * @return
     */
    public static DataModel newInstance() {
        DataModel newModel = new DataModel();

//        Optionally insert some initial framesets into the data model for testing.
//        In production, we don't insert any, so the user is presented with an initial empty list.
        if (CREATE_TESTING_FRAMESETS) {
            FrameSet f1 = FrameSet.of(10, FrameType.BIAS_FRAME, 0.0, 1, 0);
            FrameSet f2 = FrameSet.of(10, FrameType.BIAS_FRAME, 0.0, 2, 0);
            FrameSet f3 = FrameSet.of(12, FrameType.DARK_FRAME, 10.0, 1, 0);
            FrameSet f4 = FrameSet.of(14, FrameType.DARK_FRAME, 20.0, 2, 0);
            FrameSet f5 = FrameSet.of(16, FrameType.DARK_FRAME, 30.0, 3, 0);

            newModel.getSavedFrameSets().add(f1);
            newModel.getSavedFrameSets().add(f2);
            newModel.getSavedFrameSets().add(f3);
            newModel.getSavedFrameSets().add(f4);
            newModel.getSavedFrameSets().add(f5);
        }

        return newModel;
    }

    /**
     * Get session start time, one of the 4 dusk categories or the given time
     * Dusk categories are only available if lat/long are known
     * @return      Time the session should start
     *              (Includes the lead time if wake-on-lan option selected)
     */
    public LocalTime appropriateStartTime() {
        LocalTime result = null;
        LocalDate startDate = LocalDate.now();  // Date right now
        if (this.startDateType == StartDate.GIVEN_DATE) {
            startDate = this.givenStartDate;
        }
        Location loc = new Location(this.latitude, this.longitude);
        SunriseSunsetCalculator calc = new SunriseSunsetCalculator(loc, String.valueOf(this.timeZone));
        switch (this.startTimeType) {
            case SUNSET:
                if ((this.latitude != LATITUDE_NULL) && (this.longitude != LONGITUDE_NULL)) {
                    String resultString = calc.getOfficialSunsetForDate(localDateToCalendar(startDate));
                    result = LocalTime.parse(resultString);
                }
                break;
            case CIVIL_DUSK:
                if ((this.latitude != LATITUDE_NULL) && (this.longitude != LONGITUDE_NULL)) {
                    String resultString = calc.getCivilSunsetForDate(localDateToCalendar(startDate));
                    result = LocalTime.parse(resultString);
                }
                break;
            case NAUTICAL_DUSK:
                if ((this.latitude != LATITUDE_NULL) && (this.longitude != LONGITUDE_NULL)) {
                    String resultString = calc.getNauticalSunsetForDate(localDateToCalendar(startDate));
                    result = LocalTime.parse(resultString);
                }
                break;
            case ASTRONOMICAL_DUSK:
                if ((this.latitude != LATITUDE_NULL) && (this.longitude != LONGITUDE_NULL)) {
                    String resultString = calc.getAstronomicalSunsetForDate(localDateToCalendar(startDate));
                    result = LocalTime.parse(resultString);
                }
                break;
            case GIVEN_TIME:
                result = this.givenStartTime;
                break;
        }
        return result;
    }

    /**
     * Get session end time, one of the 4 dawn categories or the given time
     * Dusk categories are only available if lat/long are known
     * @return      Time the session should end
     */
    public LocalTime appropriateEndTime() {
        LocalTime result = null;
        LocalDate stopDate = LocalDate.now();  // Date right now
        if (this.endDateType == EndDate.GIVEN_DATE) {
            stopDate = this.givenEndDate;
        }
        Location loc = new Location(this.latitude, this.longitude);
        SunriseSunsetCalculator calc = new SunriseSunsetCalculator(loc, this.timeZone);
        switch (this.endTimeType) {
            case SUNRISE:
                if ((this.latitude != LATITUDE_NULL) && (this.longitude != LONGITUDE_NULL)) {
                    String resultString = calc.getOfficialSunriseForDate(localDateToCalendar(stopDate));
                    result = LocalTime.parse(resultString);
                }
                break;
            case CIVIL_DAWN:
                if ((this.latitude != LATITUDE_NULL) && (this.longitude != LONGITUDE_NULL)) {
                    String resultString = calc.getCivilSunriseForDate(localDateToCalendar(stopDate));
                    result = LocalTime.parse(resultString);
                }
                break;
            case NAUTICAL_DAWN:
                if ((this.latitude != LATITUDE_NULL) && (this.longitude != LONGITUDE_NULL)) {
                    String resultString = calc.getNauticalSunriseForDate(localDateToCalendar(stopDate));
                    result = LocalTime.parse(resultString);
                }
                break;
            case ASTRONOMICAL_DAWN:
                if ((this.latitude != LATITUDE_NULL) && (this.longitude != LONGITUDE_NULL)) {
                    String resultString = calc.getAstronomicalSunriseForDate(localDateToCalendar(stopDate));
                    result = LocalTime.parse(resultString);
                }
                break;
            case GIVEN_TIME:
                result = this.givenEndTime;
                break;
        }
        return result;
    }

    /**
     * Utiltiy method to convert LocalDate to the Calendar object used by the sunset calculation
     * methods in the luckycatlabs package
     * @param localDate
     * @return
     */
    private static Calendar localDateToCalendar(LocalDate localDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        if (localDate != null) {
            int theYear = localDate.getYear();
            int theMonth = localDate.getMonthValue() - 1;
            int theDay = localDate.getDayOfMonth();
            //assuming start of day
            //noinspection MagicConstant
            calendar.set(theYear, theMonth, theDay);
        }
//        System.out.println("Local date " + localDate + " converted to calendar " + calendar);
        return calendar;
    }

    /**
     * Serialize this data model to XML.  Convert the LocalDate and LocalTime objects to strings
     * since the native objects break the serialization.
     * @return
     */
    public String serialize() {
        // LocalDate and LocalTime values don't serialize well, and are causing errors.
        // So, we temporarily set them to null and convert them to serializable strings
        this.setDateAndTimeStrings();
        LocalDate saveStartDate = this.givenStartDate;
        LocalDate saveEndDate = this.givenEndDate;
        LocalTime saveStartTime = this.givenStartTime;
        LocalTime saveEndTime = this.givenEndTime;
        this.givenStartDate = null;
        this.givenEndDate = null;
        this.givenStartTime = null;
        this.givenEndTime = null;

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        BufferedOutputStream outputStream = new BufferedOutputStream(byteStream);
        XMLEncoder encoder = new XMLEncoder(outputStream);
        encoder.writeObject(this);
        encoder.close();
        try {
            outputStream.close();
            byteStream.close();
        } catch (IOException e) {
            System.out.println("Exception serializing data model: " + e.getMessage());
            e.printStackTrace();
        }
        String resultString = byteStream.toString();

        this.givenStartDate = saveStartDate;
        this.givenEndDate = saveEndDate;
        this.givenStartTime = saveStartTime;
        this.givenEndTime = saveEndTime;

        return resultString;
    }

    private static final String dateFormatterPattern = "yyyy-MM-dd";
    private static final String timeFormatterPattern = "HH:mm:ss";

    /**
     * LocalDate and LocalTime objects don't seem to serialize, so we convert them to and
     * from strings when writing and reading files.
     */
    private void setDateAndTimeStrings() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormatterPattern);

        if (this.givenStartDate == null) {
            this.givenStartDateAsString = null;
        } else {
            this.givenStartDateAsString = this.givenStartDate.format(dateFormatter);
        }
        if (this.givenEndDate == null) {
            this.givenEndDateAsString = null;
        } else {
            this.givenEndDateAsString = this.givenEndDate.format(dateFormatter);
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormatterPattern);
        if (this.givenStartTime == null) {
            this.givenStartTimeAsString = null;
        } else {
            this.givenStartTimeAsString = this.givenStartTime.format(timeFormatter);
        }
        if (this.givenEndTime == null) {
            this.givenEndTimeAsString = null;
        } else {
            this.givenEndTimeAsString = this.givenEndTime.format(timeFormatter);
        }

    }

    //

    /**
     * De-serializer
     *
     * Create a new instance of data model by decoding the provided xml string
     * This includes translating the encoded start and end dates and times back to local dates
     * See serialize above to see what that's about.    * @param serialized
     * @return
     */
    public static DataModel newFromXml(String serialized) {
        DataModel newModel = null;
        Object decodedObject = null;
        InputSource inputSource = new InputSource(new StringReader(serialized));
        XMLDecoder decoder;
        try {
            decoder = new XMLDecoder(inputSource);
            decodedObject = decoder.readObject();
        } catch (Exception e) {
            // An exception means the input string was corrupted.
            // Just drop out and leave the model as null
        }
        if (decodedObject instanceof DataModel) {
            newModel = (DataModel) decodedObject;
            newModel.restoreDatesAndTimes();
        }
        return newModel;
    }

    /**
     * Before the data model was encoded to XML and saved to a file, we saved the LocalDate and LocalTime
     * attributes in string format and then set the objects to null, because they don't serialize well.
     * Now we restore those objects from the saved strings.
     */
    private void restoreDatesAndTimes() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormatterPattern);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormatterPattern);

        // Given start date
        if (this.givenStartDateAsString == null) {
            this.setGivenStartDate(null);
        } else {
            this.setGivenStartDate(LocalDate.parse(this.givenStartDateAsString, dateFormatter));
        }

        // Given end data
        if (this.givenEndDateAsString == null) {
            this.setGivenEndDate(null);
        } else {
            this.setGivenEndDate(LocalDate.parse(this.givenEndDateAsString, dateFormatter));
        }

        // Given start time
        if (this.givenStartTimeAsString == null) {
            this.setGivenStartTime(null);
        } else {
            this.setGivenStartTime(LocalTime.parse(this.givenStartTimeAsString, timeFormatter));
        }

        // Given end time
        if (this.givenEndTimeAsString == null) {
            this.setGivenEndTime(null);
        } else {
            this.setGivenEndTime(LocalTime.parse(this.givenEndTimeAsString, timeFormatter));
        }

    }

    /**
     * Try to create a data model by reading xml encoding from file with given name
     * If it fails (missing file or invalid contents) return null
     * @param fullPath      Absolute path to be opened and read
     * @return
     */
    public static DataModel tryLoadFromFile(String fullPath) {
        DataModel result;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fullPath));
            String encodedData = new String(encoded, StandardCharsets.US_ASCII);
            result = DataModel.newFromXml(encodedData);
        } catch (IOException e) {
            result = null;
        }
        return result;
    }
}

