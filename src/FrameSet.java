public class FrameSet {

    //  Properties of a Frame Set, and their default values

    private int         numberOfFrames  = 16;
    private FrameType   frameType       = FrameType.DARK_FRAME;
    private double      exposureSeconds = 300.0;
    private int         binning         = 1;
    private int         numberComplete  = 0;

    public FrameSet() {}

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
    }

    public double getExposureSeconds() {
        return exposureSeconds;
    }

    public void setExposureSeconds(double exposureSeconds) {
        this.exposureSeconds = exposureSeconds;
    }

    public int getBinning() {
        return binning;
    }

    public void setBinning(int binning) {
        this.binning = binning;
    }

    public int getNumberComplete() {
        return numberComplete;
    }

    public void setNumberComplete(int numberComplete) {
        this.numberComplete = numberComplete;
    }

    //  Static creator factory methods

    public static FrameSet of(int count,
                              FrameType frameType,
                              double exposureSeconds,
                              int binning,
                              int completed) {
        FrameSet newFrameSet = new FrameSet();
        newFrameSet.numberOfFrames = count;
        newFrameSet.frameType = frameType;
        newFrameSet.exposureSeconds = exposureSeconds;
        newFrameSet.binning = binning;
        newFrameSet.numberComplete = completed;
        return newFrameSet;

    }

}

