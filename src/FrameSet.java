import java.io.Serializable;

/**
 * Description of a "Frame Set".  A frame set is a collection of a given number of
 * frames with identical specifications.  e.g. 32 dark frames, 10 seconds, binned 1x1.
 */
public class FrameSet implements Serializable {

    //  Properties of a Frame Set, and their default values

    private Integer     numberOfFrames  = 16;
    private FrameType   frameType       = FrameType.DARK_FRAME;
    private Double      exposureSeconds = 300.0;
    private Integer     binning         = 1;
    private Integer     numberComplete  = 0;

    public FrameSet() {}

    public Integer getNumberOfFrames() {
        return numberOfFrames;
    }

    public void setNumberOfFrames(Integer numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
    }

    public Double getExposureSeconds() {
        return exposureSeconds;
    }

    public void setExposureSeconds(Double exposureSeconds) {
        this.exposureSeconds = exposureSeconds;
    }

    public Integer getBinning() {
        return binning;
    }

    public void setBinning(Integer binning) {
        this.binning = binning;
    }

    public Integer getNumberComplete() {
        return numberComplete;
    }

    public void setNumberComplete(Integer numberComplete) {
        this.numberComplete = numberComplete;
    }

    /**
     * Static creator factory methods
     * @param count                 Number of frames in this set
     * @param frameType             Bias or Dark frames
     * @param exposureSeconds       If dark, exposure time in seconds
     * @param binning               Binning - 1x1, 2x2, etc.
     * @param completed             How many are already complete?
     * @return
     */
    public static FrameSet of(Integer count,
                              FrameType frameType,
                              Double exposureSeconds,
                              Integer binning,
                              Integer completed) {
        FrameSet newFrameSet = new FrameSet();
        newFrameSet.numberOfFrames = count;
        newFrameSet.frameType = frameType;
        newFrameSet.exposureSeconds = exposureSeconds;
        newFrameSet.binning = binning;
        newFrameSet.numberComplete = completed;
        return newFrameSet;
    }

    /**
     * Make a copy of ourselves, return as a new frame set
     * @return
     */
    public FrameSet copy() {
        FrameSet newFrameSet = new FrameSet();
        newFrameSet.numberOfFrames = this.numberOfFrames;
        newFrameSet.frameType = this.frameType;
        newFrameSet.exposureSeconds = this.exposureSeconds;
        newFrameSet.binning = this.binning;
        newFrameSet.numberComplete = this.numberComplete;
        return newFrameSet;
    }

    /**
     * Render to string
     * @return
     */
    public String toString() {
        return "FrameSet "
                + "(#=" + numberOfFrames
                + ", " + this.getFrameType()
                + ", " + exposureSeconds
                + " secs, " + binning
                + " x " + binning
                + ", " + numberComplete
                + " done)";
    }
    
}


