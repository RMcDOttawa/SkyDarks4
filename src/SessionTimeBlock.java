//  Everything we need to know about the start and stop times of a session

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class SessionTimeBlock {

    //  Start the session
    boolean         startNow;           //  Start immediately?
    LocalDateTime   startDateTime;      //  If not, start at this date & time

    //  Stop the session
    boolean         endWhenDone;       //  Stop when all frames done, no matter what time?
    LocalDateTime   endDateTime;       //  If not, stop at this date & time

    //  Getters and Setters
    public boolean isStartNow() { return startNow; }
    public void setStartNow(boolean startNow) { this.startNow = startNow; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public boolean isStopWhenDone() { return endWhenDone; }
    public void setStopWhenDone(boolean stopWhenDone) { this.endWhenDone = stopWhenDone; }

    public LocalDateTime getStopDateTime() { return endDateTime; }
    public void setStopDateTime(LocalDateTime stopDateTime) { this.endDateTime = stopDateTime; }

    //  Static constructor factory

    private SessionTimeBlock() {};

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

    public String toString() {

        String startString = "";
        String endString = "";

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
