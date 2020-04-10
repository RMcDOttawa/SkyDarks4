import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TimeZone;
import javax.swing.*;
import net.miginfocom.swing.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
/*
 * Created by JFormDesigner on Sat Feb 15 13:59:58 EST 2020
 */

// todo change visible row count as window resizes


/**
 * Controller class for the dialog used to pick time zone from available values.
 * Display all the possible values of time zone (obtained from time zone class).  Filter this
 * to a smaller list if and as user types in the search field
 * @author Richard McDonald
 */
public class TimeZonePicker extends JDialog {
    private boolean     saveClicked = true;
    private String[]    allValidTimeZones = TimeZone.getAvailableIDs();
    private String      selectedZone = "";

    public boolean getSaveClicked() {
        return this.saveClicked;
    }
    public String getSelectedZone() { return this.selectedZone; }


    /**
     * Constructor for "Add Frame Set Dialog".  Used for new frame sets (not editing),
     * initializes fields with defaults, and hides the "Completed" field since that
     * isn't intended to be set by the user on creation of a new frame set.
     * @param owner     The main window controller creating this dialog
     */
    public TimeZonePicker(Window owner) {
        super(owner);
        initComponents();
        this.loadFields();

        //  Set Save button as default for this dialog
//        JRootPane root = this.okButton.getRootPane();
//        root.setDefaultButton(this.okButton);
    }


    /**
     * Load the window fields from the values in the frame set stored inside us
     */
    private void loadFields() {
        // Set the maximum rows in the zone list to fit in the window
        this.timeZoneListResized();
        // Load the data model
        this.timeZoneList.setListData(this.allValidTimeZones);
        this.enableOkButton();
    }

    /**
     * Enable the Ok button when appropriate.
     * The Save button is enabled only when a line in the list is selected
     */
    private void enableOkButton() {
        this.okButton.setEnabled(!this.timeZoneList.isSelectionEmpty());
    }

    /**
     * Close the dialog and set a flag that will tell the caller it was cancelled.
     */
    private void cancelButtonClicked() {
        this.saveClicked = false;
        this.setVisible(false);
    }


    private void searchFieldKeyReleased(KeyEvent e) {
        String searchFieldText = this.searchField.getText();
        if (searchFieldText.length() == 0) {
            this.timeZoneList.setListData(this.allValidTimeZones);
        } else {
            ArrayList<String> filtered = new ArrayList<>();
            for (String timeZone : this.allValidTimeZones) {
                if (containsIgnoreCase(timeZone, searchFieldText)) {
                    filtered.add(timeZone);
                }
            }
            this.timeZoneList.setListData(filtered.toArray());
        }
    }

    private void okButtonClicked() {
        this.saveClicked = true;
        this.selectedZone = (String) this.timeZoneList.getSelectedValue();
        this.setVisible(false);
    }

    /**
     * The user has clicked in the time zone list, changing the selection.
     * Check if the OK button should be enabled.
     */
    private void timeZoneListValueChanged() {
        this.enableOkButton();
    }

    /**
     * The window, and hence the Time Zone JList, has been resized.  We need to calculate the
     * number of rows that will fit in the new size and change the visibleRows property to the new value
     * in order for the list to continue to contain the maximum number of rows that will fit.
     */
    private void timeZoneListResized() {
        JList timeZoneList = this.timeZoneList;
        JScrollPane scrollPane = this.timeZoneScrollPane;
        double rowHeight = timeZoneList.getFixedCellHeight();  // Known by supplied prototype text
        Dimension dimension = scrollPane.getViewport().getViewSize();
        int numRowsThatFit = (int) Math.round(dimension.height / rowHeight);
        timeZoneList.setVisibleRowCount(numRowsThatFit);
    }

    // Detect double-click on a list entry, treat it like selecting and clicking Ok

    private void timeZoneListMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            this.okButtonClicked();
        }
    }

    /**
     * Automatically-generated method from JFormDesigner
     */
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        searchField = new JTextField();
        timeZoneScrollPane = new JScrollPane();
        panel1 = new JPanel();
        timeZoneList = new JList();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setMinimumSize(new Dimension(285, 420));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setTitle("Select Time Zone");
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new MigLayout(
                    "insets dialog,hidemode 3,gap 0 6",
                    // columns
                    "[62,fill]" +
                    "[fill]" +
                    "[157,grow,fill]" +
                    "[139,right]",
                    // rows
                    "[top]" +
                    "[257:316,grow,top]" +
                    "[bottom]"));

                //---- label1 ----
                label1.setText("Search:");
                contentPanel.add(label1, "cell 0 0");

                //---- searchField ----
                searchField.setToolTipText("Type to filter displayed time zones");
                searchField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        searchFieldKeyReleased(e);
                    }
                });
                contentPanel.add(searchField, "cell 1 0 3 1");

                //======== timeZoneScrollPane ========
                {

                    //======== panel1 ========
                    {
                        panel1.setLayout(new BorderLayout());

                        //---- timeZoneList ----
                        timeZoneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        timeZoneList.setToolTipText("Click to select time zone to use.");
                        timeZoneList.setVisibleRowCount(14);
                        timeZoneList.setPrototypeCellValue("Canada/Toronto");
                        timeZoneList.setMaximumSize(new Dimension(101, 32767));
                        timeZoneList.addListSelectionListener(e -> timeZoneListValueChanged());
                        timeZoneList.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                timeZoneListMouseClicked(e);
                            }
                        });
                        panel1.add(timeZoneList, BorderLayout.CENTER);
                    }
                    timeZoneScrollPane.setViewportView(panel1);
                }
                contentPanel.add(timeZoneScrollPane, "cell 0 1 4 1");

                //---- okButton ----
                okButton.setText("Ok");
                okButton.addActionListener(e -> okButtonClicked());
                contentPanel.add(okButton, "tag ok,cell 0 2");

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.addActionListener(e -> cancelButtonClicked());
                contentPanel.add(cancelButton, "tag cancel,cell 3 2");
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    /**
     * Automatically-generated instance variables from JFormDesigner
     */
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JTextField searchField;
    private JScrollPane timeZoneScrollPane;
    private JPanel panel1;
    private JList timeZoneList;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
