import java.io.Serializable;

/**
 * Enum giving the different options for specifying when the program should start
 */
public enum StartTime  implements Serializable {
    SUNSET,                 //  Start at sunset on the specified date
    CIVIL_DUSK,             // Start at civil dusk on the specified date
    NAUTICAL_DUSK,          // Start at nautical dusk on the specified date
    ASTRONOMICAL_DUSK,      // Start at astronomical dusk on the specified date
    GIVEN_TIME              // Start at the given time on the specified date
}