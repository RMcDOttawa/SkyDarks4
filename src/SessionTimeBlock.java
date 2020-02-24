//  Everything we need to know about the start and stop times of a session

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * "Convenience" class holding all the information about an acquisition session's
 * start and end times.
 */
public class SessionTimeBlock {

    //  Start the session
    boolean         startNow;           //  Start immediately?
    LocalDateTime   startDateTime;      //  If not, start at this date & time

    //  Stop the session
    boolean         endWhenDone;       //  Stop when all frames done, no matter what time?
    LocalDateTime   endDateTime;       //  If not, stop at this date & time

    //  Getters and Setters
    public boolean isStartNow() { return startNow; }
    @SuppressWarnings("unused")
    public void setStartNow(boolean startNow) { this.startNow = startNow; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    @SuppressWarnings("unused")
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public boolean isStopWhenDone() { return endWhenDone; }
    @SuppressWarnings("unused")
    public void setStopWhenDone(boolean stopWhenDone) { this.endWhenDone = stopWhenDone; }

    public LocalDateTime getStopDateTime() { return endDateTime; }
    @SuppressWarnings("unused")
    public void setStopDateTime(LocalDateTime stopDateTime) { this.endDateTime = stopDateTime; }

    /**
     * Null constructor - don't use this, use the static factory that follows
     */
    private SessionTimeBlock() {}

    /**
     * Static constructor factory for this object
     * @param startNow              Is the session supposed to start immediately?
     * @param startDateTime         If not, when should it start?
     * @param endWhenDone           Is the session supposed to run until all complete?
     * @param endDateTime           If not, when should it end?
     * @return                      new instance
     */
    public static SessionTimeBlock of( boolean         startNow,
                                LocalDateTime   startDateTime,
                                boolean         endWhenDone,
                                LocalDateTime   endDateTime) {
        SessionTimeBlock timeBlock = new SessionTimeBlock();
        timeBlock.startNow = startNow;
        timeBlock.startDateTime = startDateTime;
        timeBlock.endWhenDone = endWhenDone;
        timeBlock.endDateTime = endDateTime;
        return timeBlock;
    }

    /**
     * Make a readable string representing all this information
     * @return (String)
     */
    public String toString() {

        String startString;
        String endString;

        if (this.startNow) {
            startString = "now";
        } else {
            startString = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(startDateTime);
        }

        if (this.endWhenDone) {
            endString = "when complete";
        } else {
            endString = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(endDateTime);
        }

        return "Start " + startString + "; End " + endString + ".";
    }

}
