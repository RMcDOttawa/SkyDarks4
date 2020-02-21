import java.util.TimerTask;

public class CoolingMonitorTask extends TimerTask {
    private SkyXSessionThread parent = null;
    private TheSkyXServer server;

    public CoolingMonitorTask(SkyXSessionThread parent, TheSkyXServer server) {
        super();
        this.parent = parent;
        this.server = server;
    }

    @Override
    public void run() {
        parent.fireCoolingMonitor(server);
    }
}
