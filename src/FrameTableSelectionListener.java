import com.sun.tools.javac.Main;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//  Selection listener to learn when the rows selected in the frame plan table change.
//  This is used to tell the main window to enable and disable certain controls.

public class FrameTableSelectionListener  implements ListSelectionListener {
    private MainWindow parent = null;

    public FrameTableSelectionListener(MainWindow theParent) {
        this.parent = theParent;
    }

    @Override
    public void valueChanged(ListSelectionEvent theEvent) {
        this.parent.framePlanTableSelectionChanged();
    }
}
