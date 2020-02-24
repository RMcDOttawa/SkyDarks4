import java.io.Serializable;

/**
 * Enum giving the kinds of "Start Dates" that can be specified for this program
 */
public enum StartDate  implements Serializable {
    NOW,            // Start immediately - not on some future date or time
    TODAY,          // Start today, at the time specified
    GIVEN_DATE      // Start on the given future date, at the time specified
}

