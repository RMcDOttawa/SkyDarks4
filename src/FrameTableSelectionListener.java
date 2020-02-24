import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Selection listener to learn when the rows selected in the frame plan table change.
 * This is used to tell the main window to enable and disable certain controls.
 * Yes, this tiny object would make more sense as a Lambda.  Earlier in the application
 * development there was a testing reason to have it separate, and I left it that way.
 */
public class FrameTableSelectionListener  implements ListSelectionListener {
    private MainWindow parent;

    /**
     * Creator for this object
     * @param theParent     Main window controller, to be informed of changes
     */
    public FrameTableSelectionListener(MainWindow theParent) {
        this.parent = theParent;
    }

    /**
     * Receive notice that the selection has changed, and tell the parent
     * @param theEvent
     */
    @Override
    public void valueChanged(ListSelectionEvent theEvent) {
        this.parent.framePlanTableSelectionChanged();
    }
}
