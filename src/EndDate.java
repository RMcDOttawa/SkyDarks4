import java.io.Serializable;

/**
 * enum describing what kind of end date has been specified for this session
 */
public enum EndDate  implements Serializable {
    WHEN_DONE,          // Go until all frames complete, no matter how long that takes
    TODAY_TOMORROW,     // End today or tomorrow at the given time.  (Today if the time
                        //    is still in the future, otherwise that time tomorrow.)
    GIVEN_DATE          // End on the specified date
}
