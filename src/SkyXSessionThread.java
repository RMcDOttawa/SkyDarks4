
public class SkyXSessionThread implements Runnable {
    MainWindow parent;
    DataModel   dataModel;
    SessionTimeBlock sessionTimeBlock;
    SessionFrameTableModel sessionTableModel;

    public SkyXSessionThread(MainWindow parent, DataModel dataModel, SessionTimeBlock timeBlock, SessionFrameTableModel sessionTableModel) {
        this.parent = parent;
        this.dataModel = dataModel;
        this.sessionTimeBlock = timeBlock;
        this.sessionTableModel = sessionTableModel;
    }

    @Override
    public void run() {
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                break;
            }
            this.console("Session " + String.valueOf(i), 2);
        }
        // Wait for start time
        // Optional wake-on-LAN to server
        // Connect to server, display camera path
        // Connect to camera
        // Start cooling camera
        // Measure download times
        // Wait for cooling target
        // Acquire frames until done or time
        // Optional warmup
        // Optional disconnect
        this.parent.skyXSessionThreadEnded();
    }

    private void console (String message, int messageLevel) {
        parent.console(message, messageLevel);
    }
}
