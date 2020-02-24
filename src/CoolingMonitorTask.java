import java.util.TimerTask;

/**
 * Timer task run as a sub-thread under the SessionThread, controlled by a timer.
 * This is used to periodically fetch the cooler state from the camera and update
 * the user interface with camera temperature and cooler power
 */
public class CoolingMonitorTask extends TimerTask {
    private SkyXSessionThread parent = null;
    private TheSkyXServer server;

    /**
     * Constructor for this timer task
     * @param parent        The main session thread that spawns this timer
     * @param server        The socket server for communicating with TheSkyX
     */
    public CoolingMonitorTask(SkyXSessionThread parent, TheSkyXServer server) {
        super();
        this.parent = parent;
        this.server = server;
    }

    /**
     * The main (and only) method in this task, which is invoked every time the timer fires
     */
    @Override
    public void run() {
        parent.fireCoolingMonitor(server);
    }
}
