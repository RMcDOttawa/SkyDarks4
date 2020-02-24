/**
 * Enum describing what type of frame we have.  This program deals only with
 * Dark and Bias frames.
 * (Why?  Because Lights and Flats require managing rotator orientation and other
 * parameters, while Darks and Bias are independent of those factors, and so can
 * be collected and stored easily.)
 */
public enum FrameType  {
    DARK_FRAME {
        public String toString() {
            return "Dark";
        }
    },
    BIAS_FRAME {
        public String toString() {
            return "Bias";
        }
    }
}