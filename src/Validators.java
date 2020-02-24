import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Utility methods for validating strings as various kinds of numbers in given ranges
 */
public class Validators {

    /**
     * Attempt to convert a string to an integer, and validate both the format and the range
     * @param theString     String to be converted
     * @param minimum       Minimum acceptable value
     * @param maximum       Maximum acceptable value
     * @return (pair)       Validity flag and converted value
     */
    public static ImmutablePair<Boolean, Integer> validIntInRange(String theString, int minimum, int maximum) {
        int convertedValue = 0;
        boolean isValid = true;
        try {
            convertedValue = Integer.parseInt(theString);
            if ((convertedValue < minimum) || (convertedValue > maximum)) {
                isValid = false;
            }
        } catch (NumberFormatException e) {
            isValid = false;
        }
        return ImmutablePair.of(isValid, convertedValue);
    }

    /**
     * Attempt to convert a string to a double, and validate both the format and the range
     * @param theString     String to be converted
     * @param minimum       Minimum acceptable value
     * @param maximum       Maximum acceptable value
     * @return (pair)       Validity flag and converted value
     */
    public static ImmutablePair<Boolean, Double> validFloatInRange(String theString, double minimum, double maximum) {
        double convertedValue = 0.0;
        boolean isValid = true;
        try {
            convertedValue = Double.parseDouble(theString);
            if ((convertedValue < minimum) || (convertedValue > maximum)) {
                isValid = false;
            }
        } catch (NumberFormatException e) {
            isValid = false;
        }
        return ImmutablePair.of(isValid, convertedValue);
    }
}
