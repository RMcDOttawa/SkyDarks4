import java.io.Serializable;

/**
 * enum giving the different ways the end time for the session might be specified
 */
public enum EndTime   implements Serializable {
    SUNRISE,                // End at sunrise on the specified date
    CIVIL_DAWN,             // End at civil dawn on the specified date
    NAUTICAL_DAWN,          // End at nautical dawn on the specified date
    ASTRONOMICAL_DAWN,      // End at astronomical dawn on the specified date
    GIVEN_TIME              // End at the specified time on the specified date
}