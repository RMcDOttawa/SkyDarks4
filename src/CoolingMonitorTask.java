import java.util.TimerTask;

public class CoolingMonitorTask extends TimerTask {
    private SkyXSessionThread parent = null;

    public CoolingMonitorTask(SkyXSessionThread parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.fireCoolingMonitor();
    }
}
