import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.awt.desktop.QuitStrategy;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Created by JFormDesigner on Wed Feb 12 19:55:56 EST 2020
 */



/**
 * @author Richard McDonald
 */
public class MainWindow extends JFrame {
    private DataModel dataModel;
    private String filePath = "";  // Set when file is saved

    private HashMap<JTextField,Boolean> textFieldValidity = new HashMap<>();
    private FramePlanTableModel framePlanTableModel;
    private static int SESSION_TAB_INDEX = 4;
    private SessionFrameTableModel sessionFrameTableModel;
    private FrameTableSelectionListener frameTableSelectionListener;

    private boolean documentDirtyFlag = false;
    private SkyXSessionThread skyXSessionRunnable;
    private Thread skyXThread;
    private DefaultListModel<String> consoleListModel;

    // Lock used to serialize UI updates coming from the sub-thread.
    // Includes console messages, progress bar updates, highlighting and updating the session plan table.
    // All those events are locked with the same lock, so the UI updates happen atomically and in sequence

    private ReentrantLock consoleLock = null;

    public MainWindow() {
        this.frameTableSelectionListener = new FrameTableSelectionListener(this);

        //  Catch main Quit menu so we can check for unsaved data
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
            desktop.setQuitHandler((QuitEvent evt, QuitResponse res) -> quitMenuItemClicked());
        }

        initComponents();
    }

    private void quitMenuItemClicked() {
        //  If the acquisition subtask is running, stop it
//        if (skyXThread != null) {
//            skyXThread.interrupt();
//            skyXThread = null;
//        }
        if (this.protectedSaveProceedNoCancel()) {
            System.exit(0);
        }
    }

    public void setFilePath(String thePath) {
        this.filePath = thePath;
    }

    private void makeDirty() {
        this.documentDirtyFlag = true;
    }

    public void makeNotDirty() {
        this.documentDirtyFlag = false;
    }

    private boolean isDirty() {
        return this.documentDirtyFlag;
    }

    //  This listener method is invoked whenever the Tab is changed in the main tab view.
    //  We're only interested in when the Run Session tab has become active, so we can
    //  populate its data view.
    private void mainTabFrameStateChanged(ChangeEvent event) {
        JTabbedPane thePane = (JTabbedPane) event.getSource();
        int tabIndex = thePane.getSelectedIndex();
        if (tabIndex == SESSION_TAB_INDEX) {
            //  Set up the table model that drives the session table.  This will
            //  be just framesets that are incomplete, listed in a compact form
            SessionFrameTableModel model = SessionFrameTableModel.of(this.dataModel.getSavedFrameSets());
            sessionFramesetTable.setModel(model);
            this.sessionFrameTableModel = model;
            JTableHeader header = sessionFramesetTable.getTableHeader();
            Font headerFont = header.getFont();
            Font newFont = new Font(header.getName(), Font.BOLD, headerFont.getSize() + 1);
            header.setFont(newFont);
            this.enableBeginButton();
        }
    }

    private void startDateNowButtonActionPerformed() {
        if (this.dataModel.getStartDateType() != StartDate.NOW) {
            this.makeDirty();
            this.dataModel.setStartDateType(StartDate.NOW);
        }
        this.displayStartTime();
    }

    private void startDateTodayButtonActionPerformed() {
        if (this.dataModel.getStartDateType() != StartDate.TODAY) {
            this.makeDirty();
            this.dataModel.setStartDateType(StartDate.TODAY);
        }
        this.displayStartTime();
    }

    private void startDateGivenButtonActionPerformed() {
        if (this.dataModel.getStartDateType() != StartDate.GIVEN_DATE) {
            this.makeDirty();
            this.dataModel.setStartDateType(StartDate.GIVEN_DATE);
        }
        this.displayStartTime();
    }

    private void startSunsetButtonActionPerformed() {
        if (this.dataModel.getStartTimeType() != StartTime.SUNSET) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.SUNSET);
        }
        this.displayStartTime();
    }

    private void startCivilButtonActionPerformed() {
        if (this.dataModel.getStartTimeType() != StartTime.CIVIL_DUSK) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.CIVIL_DUSK);
        }
        this.displayStartTime();
    }

    private void startNauticalButtonActionPerformed() {
        if (this.dataModel.getStartTimeType() != StartTime.NAUTICAL_DUSK) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.NAUTICAL_DUSK);
        }
        this.displayStartTime();
    }

    private void startAstronomicalButtonActionPerformed() {
        if (this.dataModel.getStartTimeType() != StartTime.ASTRONOMICAL_DUSK) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.ASTRONOMICAL_DUSK);
        }
        this.displayStartTime();
    }

    private void startGivenTimeButtonActionPerformed() {
        if (this.dataModel.getStartTimeType() != StartTime.GIVEN_TIME) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.GIVEN_TIME);
        }
        this.displayStartTime();
    }

    private void startDatePickerPropertyChange() {
        if (this.dataModel != null) {
            LocalDate newDate = startDatePicker.getDate();
            if (newDate != this.dataModel.getGivenStartDate()) {
                this.makeDirty();
                this.dataModel.setGivenStartDate(newDate);
            }
            this.displayStartTime();
        }
    }

    private void startTimePickerPropertyChange() {
        if (this.dataModel != null) {
            LocalTime newTime = startTimePicker.getTime();
            if (newTime != this.dataModel.getGivenStartTime()) {
                this.makeDirty();
                this.dataModel.setGivenStartTime(newTime);
            }
            this.displayStartTime();
        }
    }

    private void endDateDoneButtonActionPerformed() {
        if (this.dataModel.getEndDateType() != EndDate.WHEN_DONE) {
            this.makeDirty();
            this.dataModel.setEndDateType(EndDate.WHEN_DONE);
        }
        this.displayEndTime();
    }

    private void endDateTodayButtonActionPerformed() {
        if (this.dataModel.getEndDateType() != EndDate.TODAY_TOMORROW) {
            this.makeDirty();
            this.dataModel.setEndDateType(EndDate.TODAY_TOMORROW);
        }
        this.displayEndTime();
    }

    private void endDateGivenButtonActionPerformed() {
        if (this.dataModel.getEndDateType() != EndDate.GIVEN_DATE) {
            this.makeDirty();
            this.dataModel.setEndDateType(EndDate.GIVEN_DATE);
        }
        this.displayEndTime();
    }

    private void endDatePickerPropertyChange() {
        if (this.dataModel != null) {
            LocalDate newDate = endDatePicker.getDate();
            if (newDate != this.dataModel.getGivenEndDate()) {
                this.makeDirty();
                this.dataModel.setGivenEndDate(newDate);
            }
            this.displayEndTime();
        }
    }

    private void endSunriseButtonActionPerformed() {
        if (this.dataModel.getEndTimeType() != EndTime.SUNRISE) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.SUNRISE);
        }
        this.displayEndTime();
    }

    private void endCivilButtonActionPerformed() {
        if (this.dataModel.getEndTimeType() != EndTime.CIVIL_DAWN) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.CIVIL_DAWN);
        }
        this.displayEndTime();
    }

    private void endNauticalButtonActionPerformed() {
        if (this.dataModel.getEndTimeType() != EndTime.NAUTICAL_DAWN) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.NAUTICAL_DAWN);
        }
        this.displayEndTime();
    }

    private void endAstronomicalButtonActionPerformed() {
        if (this.dataModel.getEndTimeType() != EndTime.ASTRONOMICAL_DAWN) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.ASTRONOMICAL_DAWN);
        }
        this.displayEndTime();
    }

    private void endGivenTimeButtonActionPerformed() {
        if (this.dataModel.getEndTimeType() != EndTime.GIVEN_TIME) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.GIVEN_TIME);
        }
        this.displayEndTime();
    }

    private void endTimePickerPropertyChange() {
        if (this.dataModel != null) {
            LocalTime newTime = endTimePicker.getTime();
            if (newTime != this.dataModel.getGivenEndTime()) {
                this.makeDirty();
                this.dataModel.setGivenEndTime(newTime);
            }
            this.displayEndTime();
        }
    }

    private void locationNameActionPerformed() {
        String newName = locationName.getText().trim();
        if (!newName.equals(this.dataModel.getLocationName())) {
            this.makeDirty();
            this.dataModel.setLocationName(newName);
        }
    }

    // Text field naming the time zone has changed.  To validate, we'll
    // compare what was typed against the list of all possible names from
    // the time zone class.

    private void timeZoneNameActionPerformed() {
        String oldTimeZone = this.dataModel.getTimeZone();
        String proposedTimeZone = timeZoneName.getText().trim();
        boolean valid = false;
        if (proposedTimeZone.length() > 0) {
            valid = this.validateTimeZone(proposedTimeZone);
            if (valid && !oldTimeZone.equals(proposedTimeZone)) {
                this.makeDirty();
                this.dataModel.setTimeZone(proposedTimeZone);
            }
        }
        this.recordTextFieldValidity(timeZoneName, valid);
    }

    // Validate proposed time zone name against names the built-in class will accept
    // Do the comparison case-insensitive.

    private boolean validateTimeZone(String proposedZone) {
        String[] validZones = TimeZone.getAvailableIDs();
        boolean valid = false;
        for (String zone : validZones) {
            if (proposedZone.equalsIgnoreCase(zone)) {
                valid = true;
                break;
            }
        }
        return valid;
    }

    private void latitudeActionPerformed() {
        ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(latitude.getText(),
                -90.0, +90.0);
        if (validation.left) {
            double latitude = validation.right;
            if (latitude != this.dataModel.getLatitude()) {
                this.makeDirty();
                this.dataModel.setLatitude(latitude);
            }
        }
        this.recordTextFieldValidity(latitude, validation.left);
    }

    private void longitudeActionPerformed() {
        ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(longitude.getText(),
                -180.0, +180.0);
        if (validation.left) {
            double longitude = validation.right;
            if (longitude != this.dataModel.getLongitude()) {
                this.makeDirty();
                this.dataModel.setLongitude(longitude);
            }
        }
        this.recordTextFieldValidity(longitude, validation.left);
    }

    private void warmUpCheckboxActionPerformed() {
        boolean checkBoxState = warmUpCheckbox.isSelected();
        if (this.dataModel.getWarmUpWhenDone() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setWarmUpWhenDone(checkBoxState);
        }
    }

    private void disconnectCheckboxActionPerformed() {
        boolean checkBoxState = disconnectCheckbox.isSelected();
        if (this.dataModel.getDisconnectWhenDone() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setDisconnectWhenDone(checkBoxState);
        }
    }

    private void warmUpSecondsActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(warmUpSeconds.getText(),
                0, CommonUtils.INT_SECONDS_IN_DAY);
        if (validation.left) {
            int seconds = validation.right;
            if (seconds != this.dataModel.getWarmUpWhenDoneSeconds()) {
                this.makeDirty();
                this.dataModel.setWarmUpWhenDoneSeconds(seconds);
            }
        }
        this.recordTextFieldValidity(warmUpSeconds, validation.left);
    }

    private void temperatureRegulatedCheckboxActionPerformed() {
        boolean checkBoxState = temperatureRegulatedCheckbox.isSelected();
        if (this.dataModel.getTemperatureRegulated() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setTemperatureRegulated(checkBoxState);
        }
    }

    private void targetTemperatureActionPerformed() {
        ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(targetTemperature.getText(),
                CommonUtils.ABSOLUTE_ZERO, CommonUtils.WATER_BOILS);
        if (validation.left) {
            double temperature = validation.right;
            if (temperature != this.dataModel.getTemperatureTarget()) {
                this.makeDirty();
                this.dataModel.setTemperatureTarget(temperature);
            }
        }
        this.recordTextFieldValidity(targetTemperature, validation.left);
    }

    private void temperatureWithinActionPerformed() {
        ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(temperatureWithin.getText(),
                0.0, 100.0);
        if (validation.left) {
            double tolerance = validation.right;
            if (tolerance != this.dataModel.getTemperatureWithin()) {
                this.makeDirty();
                this.dataModel.setTemperatureWithin(tolerance);
            }
        }
        this.recordTextFieldValidity(temperatureWithin, validation.left);
    }

    private void coolingCheckIntervalActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(coolingCheckInterval.getText(),
                1, 60*60*24);
        if (validation.left) {
            int seconds = validation.right;
            if (seconds != this.dataModel.getTemperatureSettleSeconds()) {
                this.makeDirty();
                this.dataModel.setTemperatureSettleSeconds(seconds);
            }
        }
        this.recordTextFieldValidity(coolingCheckInterval, validation.left);
    }

    private void coolingTimeoutActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(coolingTimeout.getText(),
                1, 60*60*24);
        if (validation.left) {
            int timeout = validation.right;
            if (timeout != this.dataModel.getMaxCoolingWaitTime()) {
                this.makeDirty();
                this.dataModel.setMaxCoolingWaitTime(timeout);
            }
        }
        this.recordTextFieldValidity(coolingTimeout, validation.left);
    }

    private void coolingRetryCountActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(coolingRetryCount.getText(),
                0, 100);
        if (validation.left) {
            int retries = validation.right;
            if (retries != this.dataModel.getTemperatureFailRetryCount()) {
                this.makeDirty();
                this.dataModel.setTemperatureFailRetryCount(retries);
            }
        }
        this.recordTextFieldValidity(coolingRetryCount, validation.left);
    }

    private void coolingRetryDelayActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(coolingRetryDelay.getText(),
                0, CommonUtils.INT_SECONDS_IN_DAY);
        if (validation.left) {
            int delay = validation.right;
            if (delay != this.dataModel.getTemperatureFailRetryDelaySeconds()) {
                this.makeDirty();
                this.dataModel.setTemperatureFailRetryDelaySeconds(delay);
            }
        }
        this.recordTextFieldValidity(coolingRetryDelay, validation.left);
    }

    private void abortOnTempRiseCheckboxActionPerformed() {
        boolean boxState = abortOnTempRiseCheckbox.isSelected();
        if (this.dataModel.getTemperatureAbortOnRise() != boxState) {
            this.makeDirty();
            this.dataModel.setTemperatureAbortOnRise(boxState);
        }
    }

    private void abortOnTempRiseThresholdActionPerformed() {
        ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(abortOnTempRiseThreshold.getText(),
                0.1, CommonUtils.WATER_BOILS);
        if (validation.left) {
            double threshold = validation.right;
            if (threshold != this.dataModel.getTemperatureAbortRiseLimit()) {
                this.makeDirty();
                this.dataModel.setTemperatureAbortRiseLimit(threshold);
            }
        }
        this.recordTextFieldValidity(abortOnTempRiseThreshold, validation.left);
    }

    //  Validate and store the server address.
    //  It might be an IP address or a host name, allow both.
    //  It can also be blank.

    private void serverAddressActionPerformed() {
        String proposedAddress = serverAddress.getText().trim();
        boolean valid;
        if (proposedAddress.length() == 0) {
            valid = true;
        } else if (RmNetUtils.validateIpAddress(proposedAddress)) {
            valid = true;
        } else {
            valid = RmNetUtils.validateHostName(proposedAddress);
        }
        String oldAddress = this.dataModel.getNetAddress();
        if (valid && (!proposedAddress.equals(oldAddress))) {
            this.makeDirty();
            this.dataModel.setNetAddress(proposedAddress);
        }
        this.recordTextFieldValidity(serverAddress, valid);
    }

    private void portNumberActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(portNumber.getText(),
                0, 65535);
        if (validation.left) {
            int portNumber = validation.right;
            if (portNumber != this.dataModel.getPortNumber()) {
                this.makeDirty();
                this.dataModel.setPortNumber(portNumber);
            }
        }
        this.recordTextFieldValidity(portNumber, validation.left);
    }

    private void sendWOLcheckboxActionPerformed() {
        boolean checkBoxState = sendWOLcheckbox.isSelected();
        if (this.dataModel.getSendWakeOnLanBeforeStarting() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setSendWakeOnLanBeforeStarting(checkBoxState);
        }
    }

    private void wolSecondsBeforeActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(wolSecondsBefore.getText(),
                0, CommonUtils.INT_SECONDS_IN_DAY);
        if (validation.left) {
            int seconds = validation.right;
            if (seconds != this.dataModel.getSendWolSecondsBefore()) {
                this.makeDirty();
                this.dataModel.setSendWolSecondsBefore(seconds);
            }
        }
        this.recordTextFieldValidity(wolSecondsBefore, validation.left);
    }

    private void wolMacAddressActionPerformed() {
        String proposedMacAddress = wolMacAddress.getText().trim();
        byte[] macAddressBytes = RmNetUtils.parseMacAddress(proposedMacAddress);
        boolean valid = macAddressBytes != null;
        if (valid && !proposedMacAddress.equals(this.dataModel.getWolMacAddress())) {
            this.makeDirty();
            this.dataModel.setWolMacAddress(proposedMacAddress);
        }
        this.recordTextFieldValidity(wolMacAddress, valid);
    }

    private void wolBroadcastAddressActionPerformed() {
        String proposedBroadcastAddress = wolBroadcastAddress.getText().trim();
        boolean valid = RmNetUtils.validateIpAddress(proposedBroadcastAddress);
        if (valid && !proposedBroadcastAddress.equals(this.dataModel.getWolBroadcastAddress())) {
            this.makeDirty();
            this.dataModel.setWolBroadcastAddress(proposedBroadcastAddress);
        }
        this.recordTextFieldValidity(wolBroadcastAddress, valid);
    }

    private void autosaveCheckboxActionPerformed() {
        boolean checkBoxState = autosaveCheckbox.isSelected();
        if (this.dataModel.getAutoSaveAfterEachFrame() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setAutoSaveAfterEachFrame(checkBoxState);
        }
    }

    // Manually-established listener for selection changes in the frames plan table
    //  We'll use this to enable and disable various editing buttons that are allowed
    //  only with certain kinds of selections

    public void framePlanTableSelectionChanged() {
        int[] selectedRows = this.framesetTable.getSelectedRows();
        Arrays.sort(selectedRows);  // Ensure they're in ascending order
        int numSelected = selectedRows.length;

        //  Add and Bulk Add buttons:  zero or one rows selected
        this.addFramesetButton.setEnabled((numSelected == 0) || (numSelected == 1));
        this.bulkAddButton.setEnabled((numSelected == 0) || (numSelected == 1));

        //  Delete button: one or more rows selected
        this.deleteFramesetButton.setEnabled(numSelected >= 1);

        //  Edit button: exactly one row selected
        this.editFramesetButton.setEnabled(numSelected == 1);

        //  Move up button:  one or more rows selected, not including row 0
        boolean enableMoveUp = false;
        if (numSelected > 0) {
            enableMoveUp = (selectedRows[0] != 0);
        }
        this.moveUpButton.setEnabled(enableMoveUp);

        //  Move down button:  one or more rows selected, not including last row
        boolean enableMoveDown = false;
        if (numSelected > 0) {
            enableMoveDown = (selectedRows[selectedRows.length - 1] != this.framesetTable.getRowCount() - 1);
        }
        this.moveDownButton.setEnabled(enableMoveDown);
    }

    private void testConnectionButtonActionPerformed() {
        System.out.println("testConnectionButtonActionPerformed");
        String addressString = this.dataModel.getNetAddress().trim();
        int port = this.dataModel.getPortNumber();
        ImmutablePair<Boolean, String> result = RmNetUtils.testConnection(addressString, port);
        if (result.left) {
            this.testConnectionMessage.setText("Success");
        } else {
            this.testConnectionMessage.setText(result.right);
        }
    }

    //  Send Wake On Lan packet to the network broadcast address, with the given MAC address
    //  in the packet, to attempt to wake the server.  This is done immediately, and is
    //  primarily for testing.  The delayed wake-on-lan feature is implemented in the session thread.

    private void sendWOLbuttonActionPerformed() {
        String addressString = wolBroadcastAddress.getText().trim(); // Could be IP or host name
        String macAddressString = wolMacAddress.getText().trim();

        byte[] broadcastAddressBytes = RmNetUtils.parseIP4FromString(addressString);
        if (broadcastAddressBytes != null) {
            //  Have a valid broadcast address
            byte[] macAddressBytes = RmNetUtils.parseMacAddress(macAddressString);
            if (macAddressBytes != null) {
                //  Have a valid Mac address
                try {
                    RmNetUtils.sendWakeOnLan(broadcastAddressBytes, macAddressBytes);
                    LocalTime timeNow = LocalTime.now();
                    wolTestMessage.setText("Sent "
                            + LocalTime.of(timeNow.getHour(), timeNow.getMinute(), timeNow.getSecond()).toString());
                } catch (IOException ioEx) {
                    wolTestMessage.setText(ioEx.getMessage());
                }
            } else {
                wolTestMessage.setText("Bad MAC address");
            }
        } else {
            wolTestMessage.setText("Bad broadcast address");
        }
    }

    // User has clicked "+" in the framesets table area.  We will open a separate dialog
    // window in which they can specify the parameters of a frameset to be added.  The new
    // frameset will go above the selected row or, if nothing selected, at the end of the list

    private void addFramesetButtonActionPerformed() {
        AddFramesetDialog addDialog = new AddFramesetDialog(this);
        addDialog.setVisible(true);
        if (addDialog.getSaveClicked()) {
            FrameSet newFrameSet = addDialog.getFrameSet();
            //  Insert new frameset into the plan at selection point or at end
            int[] selectedRows = this.framesetTable.getSelectedRows();
            // There has to be zero or one row or button would have been disabled
            assert(selectedRows.length < 2);
            if (selectedRows.length == 0) {
                // Nothing selected: append new frame set to the bottom
                this.framePlanTableModel.appendRow(newFrameSet);
            } else {
                // Insert new frame set above selected row
                int insertionPoint = selectedRows[0];
                this.framePlanTableModel.insertRow(insertionPoint, newFrameSet);
                //  Move selection so originally-selected row (now +1) is still selected
                this.framesetTable.setRowSelectionInterval(insertionPoint + 1, insertionPoint + 1);
            }
            this.makeDirty();
        }
    }

    // 1 or more rows in the frame set table are selected, and the delete button has been clicked.
    // We delete those rows from the table model and tell the table it needs to be updated
    private void deleteFramesetButtonActionPerformed() {
        int[] selectedRowIndices = this.framesetTable.getSelectedRows();

        //  Process the list in descending order so the indices don't change
        //  as rows are deleted.
        Arrays.sort(selectedRowIndices);
        ArrayUtils.reverse(selectedRowIndices);
        for (int index: selectedRowIndices) {
            this.framePlanTableModel.deleteRow(index);
        }
        this.makeDirty();
    }

    // With one row selected, user has clicked "Edit".  Open a dialog to allow them to change
    // the frame set.  We use the same dialog as "Add".

    private void editFramesetButtonActionPerformed() {
        //  There has to be exactly one row selected or the button would have been disabled.
        //  Get the frameset from that row.
        int selectedRowIndex = this.framesetTable.getSelectedRow();
        FrameSet frameSetToEdit = this.dataModel.getSavedFrameSets().get(selectedRowIndex);

        //  Open and run edit dialog
        AddFramesetDialog editDialog = new AddFramesetDialog(this, frameSetToEdit);
        editDialog.setVisible(true);
        if (editDialog.getSaveClicked()) {
            FrameSet changedFrameSet = editDialog.getFrameSet();
            //  Insert new frame set into the plan at selection point or at end
            // Replace the frame set at the selected row
            this.framePlanTableModel.replaceRow(selectedRowIndex, changedFrameSet);
            this.makeDirty();
        }
    }

    // Set up double-click to act as Edit if exactly one row selected

    private void framesetTableMouseClicked(MouseEvent mouseEvent) {
        if ((mouseEvent.getClickCount() == 2) && (mouseEvent.getButton() == MouseEvent.BUTTON1)) {
            int[] selectedRows = this.framesetTable.getSelectedRows();
            if (selectedRows.length == 1) {
                this.editFramesetButtonActionPerformed();
            }
        }
    }

    //  User has clicked "Bulk Add".  We open a dialog in which they can describe a large
    //  set of bias and dark frames quickly. If they click "OK" we will then insert all those
    //  frames into the plan.

    private void bulkAddButtonActionPerformed() {
        System.out.println("bulkAddButtonActionPerformed");
        //  Open the bulk-add dialog and wait for the user to close the window when done
        BulkAddDialog bulkAddDialog = new BulkAddDialog(this);
        bulkAddDialog.setVisible(true);
        if (bulkAddDialog.getSaveClicked()) {
            System.out.println("Bulk add save clicked");
            //  User clicked "Save" not "Cancel", so we want to do bulk entry
            //  Get the complete list of frame sets represented by the filled-in form
            ArrayList<FrameSet> frameSetsToAdd = bulkAddDialog.generateFramesToAdd();

            //  The frame sets will be added above the selected row, or to the end if no row is selected
            int[] selectedRows = this.framesetTable.getSelectedRows();
            // There has to be zero or one row or the button would have been disabled
            assert(selectedRows.length < 2);
            //  Do append or insert on each of the generated Frame Sets
            for (FrameSet newFrameSet : frameSetsToAdd) {
                if (selectedRows.length == 0) {
                    // Nothing selected: append new frame set to the bottom
                    this.framePlanTableModel.appendRow(newFrameSet);
                } else {
                    // Insert new frame set above selected row
                    int insertionPoint = selectedRows[0];
                    this.framePlanTableModel.insertRow(insertionPoint, newFrameSet);
                    //  Move selection so originally-selected row (now +1) is still selected
                    this.framesetTable.setRowSelectionInterval(insertionPoint + 1, insertionPoint + 1);
                }
            }
            this.makeDirty();
        } else {
            System.out.println("Cancel clicked");
        }
    }

    //  User has clicked "Reset Completed".  This could cause them to lose the book keeping for their
    //  in-progress acquisition plan, so we'll do an "are you sure" dialog.  Then, if they are sure,
    //  set the Completed count to all the framesets in the plan back to zero.

    private void resetCompletedButtonActionPerformed() {
        //  Set up and display dialog asking the user if they want to save first
        int response = JOptionPane.showConfirmDialog(this,
                "This will reset ALL the Completed counts in the plan,\ncausing ALL "
                + "the frame sets to be re-acquired.\nAre you sure you want to do this?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION);
        if (response == 0) {
            this.framePlanTableModel.resetCompletedCounts();
        }
    }

    //  Move Up button has been clicked.  "Up" is in the visual sense in the user interface.
    //  In terms of data structure, it means moving elements to lower indices.
    //  We know that
    //      1 or more rows are selected
    //      the first row is not selected
    //  Move every selected row up one position
    private void moveUpButtonActionPerformed() {
        int[] selectedRows = this.framesetTable.getSelectedRows();
        assert(selectedRows.length > 0);    // Row(s) are selected
        assert(selectedRows[0] != 0);       // First row not selected
        //	Moving rows de-selects them, so we'll build up a list of the new, moved, row
        //	indices for re-selection after
        ArrayList<Integer> newSelection = new ArrayList<>(selectedRows.length);
        for (int indexToMove: selectedRows) {
            FrameSet frameBeingMoved = this.dataModel.getSavedFrameSets().get(indexToMove);
            this.framePlanTableModel.deleteRow(indexToMove);
            this.framePlanTableModel.insertRow(indexToMove - 1, frameBeingMoved);
            newSelection.add(indexToMove - 1);
        }
        //	Re-select the rows in their new positions
        ListSelectionModel selectionModel = framesetTable.getSelectionModel();
        selectionModel.clearSelection();
        for (int newRowToSelect: newSelection) {
            selectionModel.addSelectionInterval(newRowToSelect, newRowToSelect);
        }
    }

    //  Move Down button has been clicked.  "Down" is in the visual sense in the user interface.
    //  In terms of data structure, it means moving elements to higher indices.
    //  We know that
    //      1 or more rows are selected
    //      the last row is not selected
    //  Move every selected row down one position
    private void moveDownButtonActionPerformed() {
        int[] selectedRows = this.framesetTable.getSelectedRows();
        //  Process the rows in reverse order so we don't have to constantly adjust indices
        ArrayUtils.reverse(selectedRows);
        assert(selectedRows.length > 0);    // Row(s) are selected
        assert(selectedRows[0] != this.framePlanTableModel.getRowCount() - 1);       // Last row not selected
        //	Moving rows de-selects them, so we'll build up a list of the new, moved, row
        //	indices for re-selection after
        ArrayList<Integer> newSelection = new ArrayList<>(selectedRows.length);
        for (int indexToMove: selectedRows) {
            FrameSet frameBeingMoved = this.dataModel.getSavedFrameSets().get(indexToMove);
            this.framePlanTableModel.deleteRow(indexToMove);
            this.framePlanTableModel.insertRow(indexToMove + 1, frameBeingMoved);
            newSelection.add(indexToMove + 1);
        }
        //	Re-select the rows in their new positions
        ListSelectionModel selectionModel = framesetTable.getSelectionModel();
        selectionModel.clearSelection();
        for (int newRowToSelect: newSelection) {
            selectionModel.addSelectionInterval(newRowToSelect, newRowToSelect);
        }
    }

    /**
     * User has clicked "Begin Session".
     *
     * The button would not have been enabled unless a minimum necessary set of information was
     * available, so we can proceed. Spawn the sub-thread that does the actual processing.
     */
    private void beginSessionButtonActionPerformed() {
        boolean proceed = true;
        if (this.dataModel.getAutoSaveAfterEachFrame()) {
            // We'll be doing auto-saves after each acquired frame.  However, the first one might be
            // many hours from now, so now is the right time to ensure that a file to save into is known
            proceed = this.protectedSaveProceed();
        }
        if (proceed) {
            this.restrictUiForSession(true);
            SessionTimeBlock timeBlock = getStartAndEndTimes();

            initializeConsoleList();
            spawnProcessingTask(timeBlock, this.sessionFrameTableModel);
        }
    }



    //  The session console is a JList displaying console lines.  The list is fed by a
    //  list model, which we will initialize to an empty list here.

    private void initializeConsoleList() {
        this.consoleListModel = new DefaultListModel<>();
        this.lvConsole.setModel(this.consoleListModel);
    }

    //  Add a line to the console pane in the session pane, and scroll to keep it visible
    //  We'll do a thread-lock on this code since requests will be coming from the sub-thread and
    //  we want to ensure we don't try to run the code more than once in parallel.

    private static final String INDENTATION_BLANKS = "    ";

    public void console(String message, int messageLevel) {
//        System.out.println("Console: " + message + "," + messageLevel);
        this.consoleLock.lock();
        try {
            assert (messageLevel > 0);
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
            String indentation = (messageLevel == 1) ? "" : StringUtils.repeat(INDENTATION_BLANKS, messageLevel - 1);
            this.consoleListModel.addElement(time + ": " + indentation + message);
            //  Ensure the line we just added is visible, scrolling if necessary
            this.lvConsole.ensureIndexIsVisible(this.consoleListModel.getSize() - 1);
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
    }

    //  Receive the server's camera autosave path from the server and display it in the UI

    public void displayAutosavePath(String autosavePath) {
        this.consoleLock.lock();
        try {
            //  Turn off the italic font
            Font pathLabelFont = this.autosavePath.getFont();
            this.autosavePath.setFont(pathLabelFont.deriveFont(Font.PLAIN));
            //  Set the path display
            this.autosavePath.setText("<html>" + autosavePath + "</html>");
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
    }

    //  A periodic timer in the session thread has just updated us on the state of the cooled camera.
    //  Display this info in a UI field

    public void reportCoolingStatus(double temperature, double coolerPower) {
        this.consoleLock.lock();
        try {
            this.coolingMessage.setText(String.format("Cooler power %.0f%%, temperature %.1f", coolerPower, temperature));
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
    }

    public void hideCoolingStatus() {
        this.consoleLock.lock();
        try {
            this.coolingMessage.setText(" ");
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
    }

    //  Also called from the processing thread, this set of methods displays a progress bar on the
    //  UI.

    //  Initialize progress bar to given max value, and set it visible.

    public void startProgressBar(int minValue, int maxValue) {
        this.consoleLock.lock();
        try {
            this.progressBar.setMinimum(minValue);
            this.progressBar.setMaximum(maxValue);
            this.progressBar.setVisible(true);
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
    }

    //  Update the progress bar with the given value of progress toward the established maximum

    public void updateProgressBar(int progressValue) {
        this.consoleLock.lock();
        try {
            this.progressBar.setValue(progressValue);
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
    }

    //  End the progress bar, setting it back to invisible

    public void stopProgressBar() {
        this.consoleLock.lock();
        try {
            this.progressBar.setVisible(false);
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
    }


    //  While the acquisition sub-task is running, the UI is restricted so that the user can not
    //  visit the other tabs, and the only button working in the Session tab is the Cancel button.
    //  Enter or leave this state.

    private void restrictUiForSession(boolean sessionRunning) {

        // Tabs
        for (int tabIndex = 0; tabIndex < this.mainTabFrame.getTabCount(); tabIndex++) {
            if (tabIndex != CommonUtils.SESSION_TAB_INDEX) {
                this.mainTabFrame.setEnabledAt(tabIndex, !sessionRunning);
                if (sessionRunning) {
                    this.mainTabFrame.setBackgroundAt(tabIndex, Color.gray);
                } else {
                    this.mainTabFrame.setBackgroundAt(tabIndex, Color.white);
                }
            }
        }

        //  Begin and Cancel buttons
        this.beginSessionButton.setEnabled(!sessionRunning);
        this.cancelSessionButton.setEnabled(sessionRunning);
    }

    private void cancelSessionButtonActionPerformed() {
        //  Send an Interrupt signal to the thread
        if (this.skyXThread != null) {
            console("Session cancelled.", 1);
            this.skyXThread.interrupt();
        }
        this.skyXThread = null;
        this.skyXSessionRunnable = null;
    }

    //  Get details of start and stop times.
    //  Start Time
    //      If "now", record that as boolean flag.
    //      Otherwise, use the specified date and time.
    //      If "today' and the time has passed, treat this as "now"
    //  Stop Time
    //      If "when done", record that as boolean flag.
    //      Otherwise, use specified date and time.
    //      If date & time is  earlier than the start date & time, advance one day

    private SessionTimeBlock getStartAndEndTimes() {
//        System.out.println("getStartAndEndTimes");

        boolean startNow = false;
        LocalDate today = LocalDate.now();
        LocalTime rightNow = LocalTime.now();
        LocalDate startDate = null;
        LocalTime startTime = null;

        //  Start date and time - see above rules

        LocalDateTime startDateTime;
        switch (this.dataModel.getStartDateType()) {
            case NOW:
                startNow = true;
                startDate = today;
                startTime = rightNow;
                break;
            case TODAY:
                startDate = today;
                startTime = this.dataModel.appropriateStartTime();
                break;
            case GIVEN_DATE:
                startNow = false;
                startDate = this.dataModel.getGivenStartDate();
                startTime = this.dataModel.appropriateStartTime();
                break;
        }
        if (!startNow) {
            if ((startDate == today) && (startTime.isBefore(LocalTime.now()))) {
                //  We've missed the start time.  Just start now
                startNow = true;
                startDate = today;
                startTime = rightNow;
            }
        }
        startDateTime = LocalDateTime.of(startDate, startTime);

        //  Get end date and time - see above rules

        boolean stopWhenDone = false;
        LocalDate endDate = null;
        LocalTime endTime = null;
        switch (this.dataModel.getEndDateType()) {
            case WHEN_DONE:
                stopWhenDone = true;
                endDate = LocalDate.MAX;
                endTime = LocalTime.MAX;
                break;
            case TODAY_TOMORROW:
                endTime = this.dataModel.appropriateEndTime();
                if ((endTime != null) && (endTime.isAfter(startTime))) {
                    endDate = LocalDate.now();
                } else {
                    //  The stated time (often morning) has already passed, so we
                    //  assume they meant *tomorrow* morning
                    endDate = today.plusDays(1);
                }
                break;
            case GIVEN_DATE:
                stopWhenDone = false;
                endDate = this.dataModel.getGivenEndDate();
                endTime = this.dataModel.appropriateEndTime();
                break;
        }
        assert endTime != null;
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        return SessionTimeBlock.of(startNow, startDateTime, stopWhenDone, endDateTime);
    }

    //  Start the separate thread that manages the file acquisition.  This is run as a separate
    //  thread so that the user interface, managed from here, remains responsive.

    private void spawnProcessingTask(SessionTimeBlock timeBlock, 
                                     SessionFrameTableModel sessionTableModel) {
        this.consoleLock = new ReentrantLock();
        console(timeBlock.toString(), 1);
        this.skyXSessionRunnable = new SkyXSessionThread(this, this.dataModel, timeBlock, sessionTableModel);
        this.skyXThread = new Thread(skyXSessionRunnable);
        this.skyXThread.start();
    }

    //  The processing sub-thread tells us when it is done via message to this method

    public void skyXSessionThreadEnded() {
        this.console("Session ended.", 1);
        this.sessionFramesetTable.clearSelection();
        this.restrictUiForSession(false);
        this.consoleLock = null;
   }

    private void serverAddressFocusLost() {
        this.serverAddressActionPerformed();
    }

    private void locationNameFocusLost() {
        locationNameActionPerformed();
    }

    private void timeZoneNameFocusLost() {
        timeZoneNameActionPerformed();
    }

    private void latitudeFocusLost() {
        latitudeActionPerformed();
    }

    private void longitudeFocusLost() {
        longitudeActionPerformed();
    }

    private void targetTemperatureFocusLost() {
        targetTemperatureActionPerformed();
    }

    private void temperatureWithinFocusLost() {
        temperatureWithinActionPerformed();
    }

    private void coolingCheckIntervalFocusLost() {
        coolingCheckIntervalActionPerformed();
    }

    private void coolingTimeoutFocusLost() {
        coolingTimeoutActionPerformed();
    }

    private void coolingRetryCountFocusLost() {
        coolingRetryCountActionPerformed();
    }

    private void coolingRetryDelayFocusLost() {
        coolingRetryDelayActionPerformed();
    }

    private void abortOnTempRiseThresholdFocusLost() {
        abortOnTempRiseThresholdActionPerformed();
    }

    private void portNumberFocusLost() {
        portNumberActionPerformed();
    }

    private void wolSecondsBeforeFocusLost() {
        wolSecondsBeforeActionPerformed();
    }

    private void wolMacAddressFocusLost() {
        wolMacAddressActionPerformed();
    }

    private void wolBroadcastAddressFocusLost() {
        wolBroadcastAddressActionPerformed();
    }

    private void warmUpSecondsFocusLost() {
        this.warmUpSecondsActionPerformed();
    }

    //  OPEN menu has been invoked.
    //  Use a file dialog to get the file to be opened.  Open and decode it to a new data model
    //  then update the displayed window to reflect that new data model.

    private void openMenuItemActionPerformed() {
        if (protectedSaveProceed()) {
            FileDialog fileDialog = new FileDialog(this, "Plan File", FileDialog.LOAD);
            fileDialog.setMultipleMode(false);
            fileDialog.setVisible(true);
            String selectedFile = fileDialog.getFile();
            String selectedDirectory = fileDialog.getDirectory();
            String fullPath = selectedDirectory + selectedFile;
            if (selectedFile != null) {
                this.readFromFile(fullPath);
            }
        }
    }

    // We're about to do something that will erase the current frame set plan.  If it is "dirty", i.e.
    // contains changes not yet saved to disk, ask the user if they want to do a save.  If they do, do the
    // save.  There are 3 possible outcomes on a dirty document
    //  1. Do a save
    //  2. Don't do a save, losing the unsaved changes
    //  3. Cancel, don't do the operation that caused this

    private boolean protectedSaveProceed() {
        boolean proceed = true;
        if (this.isDirty()) {
            Object[] options = { "Cancel", "Discard", "Save"};
            int result = JOptionPane.showOptionDialog(null,
                    "Your frame set plan has unsaved changes. "
                            + "Save these or discard them?", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[2]);

            switch (result) {
                case 0:
                    // Cancel was selected
                    proceed = false;
                    break;
                case 1:
                    // Discard selected - no need to save
                    break;
                case 2:
                    // Save selected - do a save then proceed
                    this.saveMenuItemActionPerformed();
            }
        }
        return proceed;
    }

    private boolean protectedSaveProceedNoCancel() {
        if (this.isDirty()) {
            Object[] options = { "Discard", "Save"};
            int result = JOptionPane.showOptionDialog(null,
                    "Your frame set plan has unsaved changes. "
                            + "Save these or discard them?", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[1]);

            switch (result) {
                case 0:
                    // Discard selected - no need to save
                    break;
                case 1:
                    // Save selected - do a save then proceed
                    this.saveMenuItemActionPerformed();
            }
        }
        return true;
    }

    //  Given full path name of an existing file, read it, decode it, and change over to that data model

    private void readFromFile(String fullPath) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fullPath));
            String encodedData = new String(encoded, StandardCharsets.US_ASCII);
            DataModel newDataModel = DataModel.newFromXml(encodedData);
            if (newDataModel != null) {
                this.dataModel = null;
                this.loadDataModel(newDataModel, CommonUtils.simpleFileNameFromPath(fullPath));
                this.filePath = fullPath;
                this.makeNotDirty();
            }
        } catch (IOException e) {
            System.out.println("Unable to read file.");
            JOptionPane.showMessageDialog(null, "IO error, unable to read file");
        }
    }

    // NEW menu invoked. Create a new default data model and load it.

    private void newMenuItemActionPerformed() {
        if (protectedSaveProceed()) {
            DataModel newDataModel = DataModel.newInstance();
            this.dataModel = null;
            this.loadDataModel(newDataModel, CommonUtils.UNSAVED_WINDOW_TITLE);
            this.filePath = "";
            this.makeNotDirty();
        }
    }

    //  User has selected the "Save As" menu item.  Prompt for a new name and location
    //  for the file, then write out serialized data model.

    private void saveAsMenuItemActionPerformed() {

        // The following code block is the native Java Swing file chooser.  However, it
        // doesn't come up looking like the standard system file dialog, and there is
        // a strange bug preventing it from drilling into some directories.  To keep the user
        // experience closer to what they're used to, we'll use the AWT file dialog instead, below.

//        OutputFileChooser fileChooser = new OutputFileChooser("pySkyDarks4 FrameSet plans",
//                dataFileSuffix);
/*
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "pySkyDark4 Files", dataFileSuffix);
        fileChooser.setFileFilter(filter);
        // Get file name for saving
        fileChooser.setDialogTitle("Where should we save the FrameSet plan?");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.rescanCurrentDirectory();
        int userSelection = fileChooser.showOpenDialog(this);
//        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            writeToFile(fileToSave);
        }
*/
        FileDialog fileDialog = new FileDialog(this, "Save Plan File", FileDialog.SAVE);
        fileDialog.setMultipleMode(false);
        fileDialog.setVisible(true);
        String selectedFile = fileDialog.getFile();
        if (selectedFile != null) {
            String selectedDirectory = fileDialog.getDirectory();
            String fullPath = selectedDirectory + selectedFile;
            if (!fullPath.endsWith(("." + CommonUtils.DATA_FILE_SUFFIX))) {
                fullPath += "." + CommonUtils.DATA_FILE_SUFFIX;
            }
            File newFile = new File(fullPath);
            this.writeToFile(newFile);
        }
    }

    //  Write the current data model, serialized to XML, to the given file
    //  Go through a temporary file so there is no data loss in the event of a crash
    private void writeToFile(File fileToSave) {
        // Write serialized data model to file
        String serialized = this.dataModel.serialize();

        // Write to temporary file then delete and rename old copy
        // This way, if system crashes, either old or new file will still exist - no data loss
        String fileNameWithExtension = fileToSave.getName();
        assert(fileNameWithExtension.endsWith("." + CommonUtils.DATA_FILE_SUFFIX));
        String justFileName = CommonUtils.simpleFileNameFromPath(fileToSave.getAbsolutePath());
        String directory = fileToSave.getParent();
        try {
            File tempFile = File.createTempFile(justFileName, CommonUtils.DATA_FILE_SUFFIX, new File(directory));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.getAbsolutePath()));
            writer.write(serialized);
            writer.close();

            //  Content is now in temporary file.   Delete original file name and rename temporary.
            boolean deleteResult = fileToSave.delete();
            if (!tempFile.renameTo(fileToSave)) {
                JOptionPane.showMessageDialog(null,
                        "Unable to rename temporary file after writing.");
            }

            // Set title of main window
            this.setTitle(justFileName);
            //  un-dirty the document
            this.makeNotDirty();
            //  Remember the file path for future saves
            this.filePath = fileToSave.getAbsolutePath();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFound Exception. Not sure how this can happen, probably can't.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Unable to write to file.");
            JOptionPane.showMessageDialog(null, "IO error, unable to save file");
        }

    }

    //  SAVE menu invoked.  If we already have a file defined, just re-save it.
    //  Otherwise, treat this like a Save-As so the file gets prompted.

    private void saveMenuItemActionPerformed() {
        if (this.filePath.equals("")) {
            this.saveAsMenuItemActionPerformed();
        } else {
            this.writeToFile(new File(this.filePath));
        }
    }

    // User has clicked the system close button on the window.
    //  Do an "unsaved data protection" then exit the program

    private void thisWindowClosing() {
        if (protectedSaveProceed()) {
            this.setVisible(false);
            System.exit(0);
        }
    }

    // The window, and hence the Console JList, has been resized.  We need to calculate the
    //  number of rows that will fit in the new size and change the visibleRows property to the new value
    //  in order for the console to continue to contain the maximum number of rows that will fit.
    //  Also re-adjust scrolling to ensure last row is still visible.

    private void lvConsoleComponentResized() {
        JList consoleList = this.lvConsole;
        JScrollPane scrollPane = this.consoleScrollPane;
        double rowHeight = consoleList.getFixedCellHeight();  // Known by supplied prototype text
        Dimension dimension = scrollPane.getViewport().getViewSize();
        int numRowsThatFit = (int) Math.round(dimension.height / rowHeight);
        consoleList.setVisibleRowCount(numRowsThatFit);
        if (this.consoleListModel != null) {
            int numRowsInConsole = this.consoleListModel.getSize();
            consoleList.ensureIndexIsVisible(numRowsInConsole - 1);
        }
    }

    @SuppressWarnings({"Convert2MethodRef", "SpellCheckingInspection"})
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        newMenuItem = new JMenuItem();
        openMenuItem = new JMenuItem();
        saveAsMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        mainTabFrame = new JTabbedPane();
        startEndTab = new JPanel();
        label1 = new JLabel();
        panel6 = new JPanel();
        label2 = new JLabel();
        label3 = new JLabel();
        startDateNowButton = new JRadioButton();
        startDateTodayButton = new JRadioButton();
        startDateGivenButton = new JRadioButton();
        startDatePicker = new DatePicker();
        label4 = new JLabel();
        startSunsetButton = new JRadioButton();
        startCivilButton = new JRadioButton();
        startNauticalButton = new JRadioButton();
        startAstronomicalButton = new JRadioButton();
        startGivenTimeButton = new JRadioButton();
        startTimePicker = new TimePicker();
        startTimeDisplay = new JLabel();
        panel5 = new JPanel();
        label8 = new JLabel();
        label6 = new JLabel();
        endDateDoneButton = new JRadioButton();
        endDateTodayButton = new JRadioButton();
        endDateGivenButton = new JRadioButton();
        endDatePicker = new DatePicker();
        label7 = new JLabel();
        endSunriseButton = new JRadioButton();
        endCivilButton = new JRadioButton();
        endNauticalButton = new JRadioButton();
        endAstronomicalButton = new JRadioButton();
        endGivenTimeButton = new JRadioButton();
        endTimePicker = new TimePicker();
        endTimeDisplay = new JLabel();
        panel3 = new JPanel();
        label9 = new JLabel();
        label11 = new JLabel();
        locationName = new JTextField();
        label13 = new JLabel();
        timeZoneName = new JTextField();
        label12 = new JLabel();
        latitude = new JTextField();
        label10 = new JLabel();
        longitude = new JTextField();
        panel4 = new JPanel();
        label14 = new JLabel();
        warmUpCheckbox = new JCheckBox();
        warmUpSeconds = new JTextField();
        label15 = new JLabel();
        disconnectCheckbox = new JCheckBox();
        vSpacer6 = new JPanel(null);
        temperatureTab = new JPanel();
        panel1 = new JPanel();
        label5 = new JLabel();
        temperatureRegulatedCheckbox = new JCheckBox();
        label16 = new JLabel();
        targetTemperature = new JTextField();
        label17 = new JLabel();
        label18 = new JLabel();
        temperatureWithin = new JTextField();
        label23 = new JLabel();
        label19 = new JLabel();
        coolingCheckInterval = new JTextField();
        label24 = new JLabel();
        label20 = new JLabel();
        coolingTimeout = new JTextField();
        label25 = new JLabel();
        label21 = new JLabel();
        coolingRetryCount = new JTextField();
        label26 = new JLabel();
        label22 = new JLabel();
        coolingRetryDelay = new JTextField();
        label27 = new JLabel();
        abortOnTempRiseCheckbox = new JCheckBox();
        abortOnTempRiseThreshold = new JTextField();
        label28 = new JLabel();
        serverTab = new JPanel();
        label39 = new JLabel();
        label29 = new JLabel();
        label33 = new JLabel();
        sendWOLcheckbox = new JCheckBox();
        label30 = new JLabel();
        serverAddress = new JTextField();
        label34 = new JLabel();
        wolSecondsBefore = new JTextField();
        label31 = new JLabel();
        portNumber = new JTextField();
        label35 = new JLabel();
        wolMacAddress = new JTextField();
        label36 = new JLabel();
        wolBroadcastAddress = new JTextField();
        testConnectionButton = new JButton();
        testConnectionMessage = new JLabel();
        sendWOLbutton = new JButton();
        wolTestMessage = new JLabel();
        framesPlanTab = new JPanel();
        scrollPane1 = new JScrollPane();
        framesetTable = new JTable();
        addFramesetButton = new JButton();
        deleteFramesetButton = new JButton();
        hSpacer1 = new JPanel(null);
        editFramesetButton = new JButton();
        bulkAddButton = new JButton();
        hSpacer2 = new JPanel(null);
        resetCompletedButton = new JButton();
        hSpacer3 = new JPanel(null);
        moveUpButton = new JButton();
        moveDownButton = new JButton();
        autosaveCheckbox = new JCheckBox();
        label40 = new JLabel();
        runSessionTab = new JPanel();
        label44 = new JLabel();
        label42 = new JLabel();
        autosavePath = new JLabel();
        label32 = new JLabel();
        label41 = new JLabel();
        consoleScrollPane = new JScrollPane();
        lvConsole = new JList<>();
        scrollPane3 = new JScrollPane();
        sessionFramesetTable = new JTable();
        progressBar = new JProgressBar();
        beginSessionButton = new JButton();
        cancelSessionButton = new JButton();
        coolingMessage = new JLabel();

        //======== this ========
        setMinimumSize(new Dimension(800, 600));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thisWindowClosing();
            }
        });
        var contentPane = getContentPane();
        contentPane.setLayout(new GridLayout());

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("File");

                //---- newMenuItem ----
                newMenuItem.setText("New");
                newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                newMenuItem.addActionListener(e -> newMenuItemActionPerformed());
                menu1.add(newMenuItem);

                //---- openMenuItem ----
                openMenuItem.setText("Open");
                openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                openMenuItem.addActionListener(e -> openMenuItemActionPerformed());
                menu1.add(openMenuItem);
                menu1.addSeparator();

                //---- saveAsMenuItem ----
                saveAsMenuItem.setText("Save As\u2026");
                saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
                saveAsMenuItem.addActionListener(e -> saveAsMenuItemActionPerformed());
                menu1.add(saveAsMenuItem);

                //---- saveMenuItem ----
                saveMenuItem.setText("Save");
                saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                saveMenuItem.addActionListener(e -> saveMenuItemActionPerformed());
                menu1.add(saveMenuItem);
            }
            menuBar1.add(menu1);
        }
        setJMenuBar(menuBar1);

        //======== mainTabFrame ========
        {
            mainTabFrame.setPreferredSize(new Dimension(800, 600));
            mainTabFrame.addChangeListener(e -> mainTabFrameStateChanged(e));

            //======== startEndTab ========
            {
                startEndTab.setLayout(new MigLayout(
                    "hidemode 3,alignx center",
                    // columns
                    "[331,grow,fill]para" +
                    "[330:332,grow,fill]",
                    // rows
                    "[]unrel" +
                    "[]para" +
                    "[]"));

                //---- label1 ----
                label1.setText("Session Start and End");
                label1.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
                startEndTab.add(label1, "cell 0 0 2 1,alignx center,growx 0");

                //======== panel6 ========
                {
                    panel6.setBorder(LineBorder.createBlackLineBorder());
                    panel6.setLayout(new MigLayout(
                        "fill,insets 4 4 4 16,hidemode 3,align left top,gap 4 4",
                        // columns
                        "[47,left]" +
                        "[115,grow,left]" +
                        "[grow,fill]",
                        // rows
                        "[]" +
                        "[]" +
                        "[grow,fill]" +
                        "[]" +
                        "[]" +
                        "[grow,fill]" +
                        "[grow,fill]" +
                        "[grow,fill]" +
                        "[grow,fill]" +
                        "[grow,fill]" +
                        "[]"));

                    //---- label2 ----
                    label2.setText("Session Start");
                    label2.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                    panel6.add(label2, "cell 0 0 3 1");

                    //---- label3 ----
                    label3.setText("Day");
                    panel6.add(label3, "cell 0 2");

                    //---- startDateNowButton ----
                    startDateNowButton.setText("Now");
                    startDateNowButton.setToolTipText("Start acquisition as soon as Proceed is clicked (on Run Session tab)");
                    startDateNowButton.addActionListener(e -> startDateNowButtonActionPerformed());
                    panel6.add(startDateNowButton, "cell 1 2 2 1");

                    //---- startDateTodayButton ----
                    startDateTodayButton.setText("Today");
                    startDateTodayButton.setToolTipText("Start later today, at the time given below.");
                    startDateTodayButton.addActionListener(e -> startDateTodayButtonActionPerformed());
                    panel6.add(startDateTodayButton, "cell 1 3 2 1");

                    //---- startDateGivenButton ----
                    startDateGivenButton.setText("This Date:");
                    startDateGivenButton.setMinimumSize(new Dimension(48, 23));
                    startDateGivenButton.setToolTipText("Start on this future date, at the time given below.");
                    startDateGivenButton.addActionListener(e -> startDateGivenButtonActionPerformed());
                    panel6.add(startDateGivenButton, "cell 1 4");

                    //---- startDatePicker ----
                    startDatePicker.setSettings(null);
                    startDatePicker.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startDatePicker.addPropertyChangeListener(e -> startDatePickerPropertyChange());
                    panel6.add(startDatePicker, "cell 2 4");

                    //---- label4 ----
                    label4.setText("Time");
                    panel6.add(label4, "cell 0 5");

                    //---- startSunsetButton ----
                    startSunsetButton.setText("Sunset");
                    startSunsetButton.setToolTipText("Start at sunset on the specified day.");
                    startSunsetButton.addActionListener(e -> startSunsetButtonActionPerformed());
                    panel6.add(startSunsetButton, "cell 1 5 2 1");

                    //---- startCivilButton ----
                    startCivilButton.setText("Civil Dusk");
                    startCivilButton.setToolTipText("Start at civil dusk on the specified day.");
                    startCivilButton.addActionListener(e -> startCivilButtonActionPerformed());
                    panel6.add(startCivilButton, "cell 1 6 2 1");

                    //---- startNauticalButton ----
                    startNauticalButton.setText("Nautical Dusk");
                    startNauticalButton.setToolTipText("Start at nautical dusk on the specified day.");
                    startNauticalButton.addActionListener(e -> startNauticalButtonActionPerformed());
                    panel6.add(startNauticalButton, "cell 1 7 2 1");

                    //---- startAstronomicalButton ----
                    startAstronomicalButton.setText("Astronomical Dusk");
                    startAstronomicalButton.setToolTipText("Start at astronomical dusk on the specified day.");
                    startAstronomicalButton.addActionListener(e -> startAstronomicalButtonActionPerformed());
                    panel6.add(startAstronomicalButton, "cell 1 8 2 1");

                    //---- startGivenTimeButton ----
                    startGivenTimeButton.setText("This Time:");
                    startGivenTimeButton.setMinimumSize(new Dimension(48, 23));
                    startGivenTimeButton.setToolTipText("Start at the given time on the specified day.");
                    startGivenTimeButton.addActionListener(e -> startGivenTimeButtonActionPerformed());
                    panel6.add(startGivenTimeButton, "cell 1 9");

                    //---- startTimePicker ----
                    startTimePicker.addPropertyChangeListener(e -> startTimePickerPropertyChange());
                    panel6.add(startTimePicker, "cell 2 9");

                    //---- startTimeDisplay ----
                    startTimeDisplay.setText("text");
                    panel6.add(startTimeDisplay, "cell 1 10");
                }
                startEndTab.add(panel6, "cell 0 1,aligny top,growy 0");

                //======== panel5 ========
                {
                    panel5.setBorder(LineBorder.createBlackLineBorder());
                    panel5.setLayout(new MigLayout(
                        "fill,insets 4 16 4 4,hidemode 3,align left top,gap 4 4",
                        // columns
                        "[43,left]" +
                        "[fill]" +
                        "[grow,fill]",
                        // rows
                        "[]" +
                        "[]" +
                        "[grow,fill]" +
                        "[]" +
                        "[]" +
                        "[grow,fill]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]"));

                    //---- label8 ----
                    label8.setText("Session End");
                    label8.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                    panel5.add(label8, "cell 0 0 3 1");

                    //---- label6 ----
                    label6.setText("Day");
                    panel5.add(label6, "cell 0 2");

                    //---- endDateDoneButton ----
                    endDateDoneButton.setText("When Done");
                    endDateDoneButton.setToolTipText("Run until all frames are acquired, no matter how long that takes.");
                    endDateDoneButton.addActionListener(e -> endDateDoneButtonActionPerformed());
                    panel5.add(endDateDoneButton, "cell 1 2 2 1");

                    //---- endDateTodayButton ----
                    endDateTodayButton.setText("Today / Tomorrow");
                    endDateTodayButton.setToolTipText("Stop today or tomorrow at the time specified below. (Tomorrow if that time has passed today.)");
                    endDateTodayButton.addActionListener(e -> endDateTodayButtonActionPerformed());
                    panel5.add(endDateTodayButton, "cell 1 3 2 1");

                    //---- endDateGivenButton ----
                    endDateGivenButton.setText("This Date:");
                    endDateGivenButton.setToolTipText("Stop on this future date, at the time specified below.");
                    endDateGivenButton.addActionListener(e -> endDateGivenButtonActionPerformed());
                    panel5.add(endDateGivenButton, "cell 1 4");

                    //---- endDatePicker ----
                    endDatePicker.addPropertyChangeListener(e -> endDatePickerPropertyChange());
                    panel5.add(endDatePicker, "cell 2 4");

                    //---- label7 ----
                    label7.setText("Time");
                    panel5.add(label7, "cell 0 5");

                    //---- endSunriseButton ----
                    endSunriseButton.setText("Sunrise");
                    endSunriseButton.setToolTipText("Stop at sunrise on the date given above.");
                    endSunriseButton.addActionListener(e -> endSunriseButtonActionPerformed());
                    panel5.add(endSunriseButton, "cell 1 5 2 1");

                    //---- endCivilButton ----
                    endCivilButton.setText("Civil Dawn");
                    endCivilButton.setToolTipText("Stop at civil dawn on the date given above.");
                    endCivilButton.addActionListener(e -> endCivilButtonActionPerformed());
                    panel5.add(endCivilButton, "cell 1 6 2 1");

                    //---- endNauticalButton ----
                    endNauticalButton.setText("Nautical Dawn");
                    endNauticalButton.setToolTipText("Stop at nautical dawn on the date given above.");
                    endNauticalButton.addActionListener(e -> endNauticalButtonActionPerformed());
                    panel5.add(endNauticalButton, "cell 1 7 2 1");

                    //---- endAstronomicalButton ----
                    endAstronomicalButton.setText("Astronomical Dawn");
                    endAstronomicalButton.setToolTipText("Stop at astronomical dawn on the date given above.");
                    endAstronomicalButton.addActionListener(e -> endAstronomicalButtonActionPerformed());
                    panel5.add(endAstronomicalButton, "cell 1 8 2 1");

                    //---- endGivenTimeButton ----
                    endGivenTimeButton.setText("This Time:");
                    endGivenTimeButton.setToolTipText("Stop at the given time on the date given above.");
                    endGivenTimeButton.addActionListener(e -> endGivenTimeButtonActionPerformed());
                    panel5.add(endGivenTimeButton, "cell 1 9");

                    //---- endTimePicker ----
                    endTimePicker.addPropertyChangeListener(e -> endTimePickerPropertyChange());
                    panel5.add(endTimePicker, "cell 2 9");

                    //---- endTimeDisplay ----
                    endTimeDisplay.setText("text");
                    panel5.add(endTimeDisplay, "cell 1 10");
                }
                startEndTab.add(panel5, "cell 1 1,aligny top,growy 0");

                //======== panel3 ========
                {
                    panel3.setBorder(LineBorder.createBlackLineBorder());
                    panel3.setLayout(new MigLayout(
                        "fill,insets 04 4 4 16,hidemode 3,align left top,gap 4 4",
                        // columns
                        "[fill]" +
                        "[grow,fill]",
                        // rows
                        "[grow,fill]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]"));

                    //---- label9 ----
                    label9.setText("Location (for Dusk/Dawn Calculation)");
                    label9.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                    panel3.add(label9, "cell 0 0 2 1");

                    //---- label11 ----
                    label11.setText("Name: ");
                    panel3.add(label11, "cell 0 2");

                    //---- locationName ----
                    locationName.setToolTipText("Arbitrary name for this site");
                    locationName.addActionListener(e -> locationNameActionPerformed());
                    locationName.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            locationNameFocusLost();
                        }
                    });
                    panel3.add(locationName, "cell 1 2");

                    //---- label13 ----
                    label13.setText("Time Zone: ");
                    panel3.add(label13, "cell 0 3");

                    //---- timeZoneName ----
                    timeZoneName.setToolTipText("Time zone identifier. Standard abbreviation, or Continent/City, or offset from GMT as Etc/GMT-5. Search \"java TimeZone class\" to see all the valid values.");
                    timeZoneName.addActionListener(e -> timeZoneNameActionPerformed());
                    timeZoneName.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            timeZoneNameFocusLost();
                        }
                    });
                    panel3.add(timeZoneName, "cell 1 3");

                    //---- label12 ----
                    label12.setText("Latitude: ");
                    panel3.add(label12, "cell 0 4");

                    //---- latitude ----
                    latitude.setToolTipText("Latitude of observing site.");
                    latitude.addActionListener(e -> latitudeActionPerformed());
                    latitude.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            latitudeFocusLost();
                        }
                    });
                    panel3.add(latitude, "cell 1 4");

                    //---- label10 ----
                    label10.setText("Longitude: ");
                    panel3.add(label10, "cell 0 5");

                    //---- longitude ----
                    longitude.setToolTipText("Longitude of observing site.");
                    longitude.addActionListener(e -> longitudeActionPerformed());
                    longitude.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            longitudeFocusLost();
                        }
                    });
                    panel3.add(longitude, "cell 1 5");
                }
                startEndTab.add(panel3, "cell 0 2,aligny top,growy 0");

                //======== panel4 ========
                {
                    panel4.setBorder(LineBorder.createBlackLineBorder());
                    panel4.setLayout(new MigLayout(
                        "fill,insets 04 16 4 4,hidemode 3,align left top,gap 4 4",
                        // columns
                        "[fill]" +
                        "[grow,fill]" +
                        "[fill]",
                        // rows
                        "[grow,fill]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]"));

                    //---- label14 ----
                    label14.setText("When Done");
                    label14.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                    panel4.add(label14, "cell 0 0");

                    //---- warmUpCheckbox ----
                    warmUpCheckbox.setText("Warm up CCD for ");
                    warmUpCheckbox.setToolTipText("When finished, turn off CCD cooling so it can warm up gently.");
                    warmUpCheckbox.addActionListener(e -> warmUpCheckboxActionPerformed());
                    panel4.add(warmUpCheckbox, "cell 0 2");

                    //---- warmUpSeconds ----
                    warmUpSeconds.setToolTipText("How long to leave CCD warming up before disconnecting.");
                    warmUpSeconds.addActionListener(e -> warmUpSecondsActionPerformed());
                    warmUpSeconds.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            warmUpSecondsFocusLost();
                        }
                    });
                    panel4.add(warmUpSeconds, "cell 1 2");

                    //---- label15 ----
                    label15.setText("seconds");
                    panel4.add(label15, "cell 2 2");

                    //---- disconnectCheckbox ----
                    disconnectCheckbox.setText("Disconnect Camera (after warmup)");
                    disconnectCheckbox.setToolTipText("Disconnect camera when done.");
                    disconnectCheckbox.addActionListener(e -> disconnectCheckboxActionPerformed());
                    panel4.add(disconnectCheckbox, "cell 0 3 3 1");

                    //---- vSpacer6 ----
                    vSpacer6.setPreferredSize(new Dimension(10, 36));
                    panel4.add(vSpacer6, "cell 0 5 3 1");
                }
                startEndTab.add(panel4, "cell 1 2,aligny top,growy 0");
            }
            mainTabFrame.addTab("Start/End", null, startEndTab, "Controls when the data acquisition starts and ends.");

            //======== temperatureTab ========
            {
                temperatureTab.setLayout(new MigLayout(
                    "hidemode 3,alignx center",
                    // columns
                    "[75,fill]" +
                    "[label]",
                    // rows
                    "[]para"));

                //======== panel1 ========
                {
                    panel1.setBorder(LineBorder.createBlackLineBorder());
                    panel1.setLayout(new MigLayout(
                        "hidemode 3,alignx center",
                        // columns
                        "[56,fill]" +
                        "[grow,fill]" +
                        "[left]" +
                        "[68,grow,fill]" +
                        "[614,fill]",
                        // rows
                        "[]para" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]"));

                    //---- label5 ----
                    label5.setText("Camera Temperature Settings");
                    label5.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
                    panel1.add(label5, "cell 2 0 3 1,alignx center,growx 0");

                    //---- temperatureRegulatedCheckbox ----
                    temperatureRegulatedCheckbox.setText("Camera is temperature-regulated");
                    temperatureRegulatedCheckbox.setToolTipText("Camera is temperature-controlled and should be set as specified below.");
                    temperatureRegulatedCheckbox.addActionListener(e -> temperatureRegulatedCheckboxActionPerformed());
                    panel1.add(temperatureRegulatedCheckbox, "cell 2 1 3 1");

                    //---- label16 ----
                    label16.setText("Target Temperature: ");
                    panel1.add(label16, "cell 2 2");

                    //---- targetTemperature ----
                    targetTemperature.setMinimumSize(new Dimension(100, 26));
                    targetTemperature.setToolTipText("Target temperature setpoint for camera.");
                    targetTemperature.addActionListener(e -> targetTemperatureActionPerformed());
                    targetTemperature.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            targetTemperatureFocusLost();
                        }
                    });
                    panel1.add(targetTemperature, "cell 3 2");

                    //---- label17 ----
                    label17.setText("\u00b0 C");
                    panel1.add(label17, "cell 4 2");

                    //---- label18 ----
                    label18.setText("Within +/-:");
                    panel1.add(label18, "cell 2 3");

                    //---- temperatureWithin ----
                    temperatureWithin.setToolTipText("How close to target is good enough to begin session?");
                    temperatureWithin.addActionListener(e -> temperatureWithinActionPerformed());
                    temperatureWithin.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            temperatureWithinFocusLost();
                        }
                    });
                    panel1.add(temperatureWithin, "cell 3 3");

                    //---- label23 ----
                    label23.setText("\u00b0 C");
                    panel1.add(label23, "cell 4 3");

                    //---- label19 ----
                    label19.setText("Cooling Check Interval: ");
                    panel1.add(label19, "cell 2 4");

                    //---- coolingCheckInterval ----
                    coolingCheckInterval.setToolTipText("While cooling, check camera temperature this often.");
                    coolingCheckInterval.addActionListener(e -> coolingCheckIntervalActionPerformed());
                    coolingCheckInterval.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            coolingCheckIntervalFocusLost();
                        }
                    });
                    panel1.add(coolingCheckInterval, "cell 3 4");

                    //---- label24 ----
                    label24.setText("seconds");
                    panel1.add(label24, "cell 4 4");

                    //---- label20 ----
                    label20.setText("Max Time to Try Cooling: ");
                    panel1.add(label20, "cell 2 5");

                    //---- coolingTimeout ----
                    coolingTimeout.setToolTipText("If camera doesn't reach target temperature in this time, assume it never will.");
                    coolingTimeout.addActionListener(e -> coolingTimeoutActionPerformed());
                    coolingTimeout.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            coolingTimeoutFocusLost();
                        }
                    });
                    panel1.add(coolingTimeout, "cell 3 5");

                    //---- label25 ----
                    label25.setText("seconds");
                    panel1.add(label25, "cell 4 5");

                    //---- label21 ----
                    label21.setText("Cooling Retry Count: ");
                    panel1.add(label21, "cell 2 6");

                    //---- coolingRetryCount ----
                    coolingRetryCount.setToolTipText("If camera fails to reach target temperature, wait a bit and retry this many times.");
                    coolingRetryCount.addActionListener(e -> coolingRetryCountActionPerformed());
                    coolingRetryCount.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            coolingRetryCountFocusLost();
                        }
                    });
                    panel1.add(coolingRetryCount, "cell 3 6");

                    //---- label26 ----
                    label26.setText("times");
                    panel1.add(label26, "cell 4 6");

                    //---- label22 ----
                    label22.setText("Cooling Retry Delay: ");
                    panel1.add(label22, "cell 2 7");

                    //---- coolingRetryDelay ----
                    coolingRetryDelay.setToolTipText("How long to wait after a failed cooling attempt.");
                    coolingRetryDelay.addActionListener(e -> coolingRetryDelayActionPerformed());
                    coolingRetryDelay.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            coolingRetryDelayFocusLost();
                        }
                    });
                    panel1.add(coolingRetryDelay, "cell 3 7");

                    //---- label27 ----
                    label27.setText("seconds");
                    panel1.add(label27, "cell 4 7");

                    //---- abortOnTempRiseCheckbox ----
                    abortOnTempRiseCheckbox.setText("Abort if Temp Rises: ");
                    abortOnTempRiseCheckbox.setToolTipText("If temperature rises above target durring acquisition, abort the session.");
                    abortOnTempRiseCheckbox.addActionListener(e -> abortOnTempRiseCheckboxActionPerformed());
                    panel1.add(abortOnTempRiseCheckbox, "cell 2 8");

                    //---- abortOnTempRiseThreshold ----
                    abortOnTempRiseThreshold.setToolTipText("How much temperature needs to rise to abort session.");
                    abortOnTempRiseThreshold.addActionListener(e -> abortOnTempRiseThresholdActionPerformed());
                    abortOnTempRiseThreshold.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            abortOnTempRiseThresholdFocusLost();
                        }
                    });
                    panel1.add(abortOnTempRiseThreshold, "cell 3 8");

                    //---- label28 ----
                    label28.setText("\u00b0 C");
                    panel1.add(label28, "cell 4 8");
                }
                temperatureTab.add(panel1, "cell 1 0");
            }
            mainTabFrame.addTab("Temperature", null, temperatureTab, "Controls the CCD temperature regulation.");

            //======== serverTab ========
            {
                serverTab.setLayout(new MigLayout(
                    "hidemode 3,alignx center",
                    // columns
                    "[fill]" +
                    "[grow,fill]" +
                    "[fill]" +
                    "[fill]" +
                    "[grow,fill]",
                    // rows
                    "[]" +
                    "[33]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]"));

                //---- label39 ----
                label39.setText("Network Settings");
                label39.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
                serverTab.add(label39, "cell 0 0 5 1,alignx center,growx 0");

                //---- label29 ----
                label29.setText("Server Address");
                label29.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                serverTab.add(label29, "cell 0 2 2 1");

                //---- label33 ----
                label33.setText("Wake on LAN");
                label33.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                serverTab.add(label33, "cell 3 2 2 1");

                //---- sendWOLcheckbox ----
                sendWOLcheckbox.setText("Send Wake on LAN packet before starting.");
                sendWOLcheckbox.setToolTipText("Wake the server some time before starting acquisition.");
                sendWOLcheckbox.addActionListener(e -> sendWOLcheckboxActionPerformed());
                serverTab.add(sendWOLcheckbox, "cell 3 4 2 1");

                //---- label30 ----
                label30.setText("IP Address or Host Name: ");
                serverTab.add(label30, "cell 0 5");

                //---- serverAddress ----
                serverAddress.setToolTipText("IPv4 address or host name of server.");
                serverAddress.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        serverAddressFocusLost();
                    }
                });
                serverAddress.addActionListener(e -> serverAddressActionPerformed());
                serverTab.add(serverAddress, "cell 1 5");

                //---- label34 ----
                label34.setText("Seconds before start to send WOL: ");
                serverTab.add(label34, "cell 3 5");

                //---- wolSecondsBefore ----
                wolSecondsBefore.setToolTipText("How long before acquisition to send the wakeup command.");
                wolSecondsBefore.addActionListener(e -> wolSecondsBeforeActionPerformed());
                wolSecondsBefore.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        wolSecondsBeforeFocusLost();
                    }
                });
                serverTab.add(wolSecondsBefore, "cell 4 5");

                //---- label31 ----
                label31.setText("Port Number: ");
                serverTab.add(label31, "cell 0 6");

                //---- portNumber ----
                portNumber.setToolTipText("Port number where TheSkyX is listening.");
                portNumber.addActionListener(e -> portNumberActionPerformed());
                portNumber.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        portNumberFocusLost();
                    }
                });
                serverTab.add(portNumber, "cell 1 6");

                //---- label35 ----
                label35.setText("Server MAC address: ");
                serverTab.add(label35, "cell 3 6");

                //---- wolMacAddress ----
                wolMacAddress.setToolTipText("MAC address of computer where TheSkyX runs.");
                wolMacAddress.addActionListener(e -> wolMacAddressActionPerformed());
                wolMacAddress.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        wolMacAddressFocusLost();
                    }
                });
                serverTab.add(wolMacAddress, "cell 4 6");

                //---- label36 ----
                label36.setText("Network broadcast address: ");
                serverTab.add(label36, "cell 3 7");

                //---- wolBroadcastAddress ----
                wolBroadcastAddress.setToolTipText("Address to broadcast wakeup to whole LAN. Use 255.255.255.255 except in very special circumstances.");
                wolBroadcastAddress.addActionListener(e -> wolBroadcastAddressActionPerformed());
                wolBroadcastAddress.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        wolBroadcastAddressFocusLost();
                    }
                });
                serverTab.add(wolBroadcastAddress, "cell 4 7");

                //---- testConnectionButton ----
                testConnectionButton.setText("Test Connection");
                testConnectionButton.setMinimumSize(new Dimension(100, 29));
                testConnectionButton.setToolTipText("Try to connect to TheSkyX to see if it works.");
                testConnectionButton.addActionListener(e -> testConnectionButtonActionPerformed());
                serverTab.add(testConnectionButton, "cell 0 9,alignx left,growx 0");

                //---- testConnectionMessage ----
                testConnectionMessage.setText(" ");
                serverTab.add(testConnectionMessage, "cell 1 9");

                //---- sendWOLbutton ----
                sendWOLbutton.setText("Send WOL Now");
                sendWOLbutton.setToolTipText("Send the Wakeup command now.");
                sendWOLbutton.addActionListener(e -> sendWOLbuttonActionPerformed());
                serverTab.add(sendWOLbutton, "cell 3 9,alignx left,growx 0");

                //---- wolTestMessage ----
                wolTestMessage.setText("   ");
                serverTab.add(wolTestMessage, "cell 4 9");
            }
            mainTabFrame.addTab("TheSkyX Server", null, serverTab, "Information for connecting to the TheSkyX server");

            //======== framesPlanTab ========
            {

                //======== scrollPane1 ========
                {

                    //---- framesetTable ----
                    framesetTable.setFillsViewportHeight(true);
                    framesetTable.setToolTipText("The complete set of frames to be acquired, and what has already been completed.");
                    framesetTable.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            framesetTableMouseClicked(e);
                        }
                    });
                    scrollPane1.setViewportView(framesetTable);
                }

                //---- addFramesetButton ----
                addFramesetButton.setText("+");
                addFramesetButton.setToolTipText("Add a new frame set above the selected row or to the end of the table.");
                addFramesetButton.addActionListener(e -> addFramesetButtonActionPerformed());

                //---- deleteFramesetButton ----
                deleteFramesetButton.setText("-");
                deleteFramesetButton.setToolTipText("Remove the selected frame set(s) from the table");
                deleteFramesetButton.addActionListener(e -> deleteFramesetButtonActionPerformed());

                //---- editFramesetButton ----
                editFramesetButton.setText("Edit");
                editFramesetButton.setToolTipText("Edit the specification of the selected frame set.");
                editFramesetButton.addActionListener(e -> editFramesetButtonActionPerformed());

                //---- bulkAddButton ----
                bulkAddButton.setText("Bulk Add");
                bulkAddButton.setToolTipText("Rapidly add multiple frame sets in a standard pattern.");
                bulkAddButton.addActionListener(e -> bulkAddButtonActionPerformed());

                //---- resetCompletedButton ----
                resetCompletedButton.setText("Reset Completed");
                resetCompletedButton.setToolTipText("Set all the \"Completed\" counts back to zero, causing all frame sets to be re-acquired.");
                resetCompletedButton.addActionListener(e -> resetCompletedButtonActionPerformed());

                //---- moveUpButton ----
                moveUpButton.setText("Up");
                moveUpButton.setToolTipText("Move the selected frame set(s) up one row.");
                moveUpButton.addActionListener(e -> moveUpButtonActionPerformed());

                //---- moveDownButton ----
                moveDownButton.setText("Down");
                moveDownButton.setToolTipText("Move the selected frame set(s) down one row.");
                moveDownButton.addActionListener(e -> moveDownButtonActionPerformed());

                //---- autosaveCheckbox ----
                autosaveCheckbox.setText("Auto-save after each completed frame");
                autosaveCheckbox.setToolTipText("During acquisition, save this plan after each acquired frame.");
                autosaveCheckbox.addActionListener(e -> autosaveCheckboxActionPerformed());

                //---- label40 ----
                label40.setText("Frame Sets to be Acquired");
                label40.setHorizontalAlignment(SwingConstants.CENTER);
                label40.setFont(new Font("Lucida Grande", Font.PLAIN, 24));

                GroupLayout framesPlanTabLayout = new GroupLayout(framesPlanTab);
                framesPlanTab.setLayout(framesPlanTabLayout);
                framesPlanTabLayout.setHorizontalGroup(
                    framesPlanTabLayout.createParallelGroup()
                        .addGroup(framesPlanTabLayout.createSequentialGroup()
                            .addGroup(framesPlanTabLayout.createParallelGroup()
                                .addComponent(scrollPane1, GroupLayout.Alignment.TRAILING)
                                .addGroup(framesPlanTabLayout.createSequentialGroup()
                                    .addGroup(framesPlanTabLayout.createParallelGroup()
                                        .addGroup(framesPlanTabLayout.createSequentialGroup()
                                            .addGap(9, 9, 9)
                                            .addComponent(autosaveCheckbox, GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE))
                                        .addGroup(framesPlanTabLayout.createSequentialGroup()
                                            .addGap(156, 156, 156)
                                            .addComponent(hSpacer1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addGap(1, 1, 1)
                                            .addComponent(editFramesetButton)
                                            .addGap(96, 96, 96)
                                            .addComponent(hSpacer2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addGap(1, 1, 1)
                                            .addComponent(resetCompletedButton)
                                            .addGap(1, 1, 1)
                                            .addComponent(hSpacer3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(framesPlanTabLayout.createSequentialGroup()
                                            .addGap(237, 237, 237)
                                            .addComponent(bulkAddButton))
                                        .addGroup(framesPlanTabLayout.createSequentialGroup()
                                            .addGap(80, 80, 80)
                                            .addComponent(deleteFramesetButton))
                                        .addGroup(framesPlanTabLayout.createSequentialGroup()
                                            .addGap(10, 10, 10)
                                            .addComponent(addFramesetButton)))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(moveUpButton)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(moveDownButton)
                                    .addGap(1, 1, 1))
                                .addComponent(label40, GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE))
                            .addContainerGap())
                );
                framesPlanTabLayout.setVerticalGroup(
                    framesPlanTabLayout.createParallelGroup()
                        .addGroup(framesPlanTabLayout.createSequentialGroup()
                            .addGap(27, 27, 27)
                            .addComponent(label40)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                            .addGroup(framesPlanTabLayout.createParallelGroup()
                                .addGroup(framesPlanTabLayout.createSequentialGroup()
                                    .addGap(4, 4, 4)
                                    .addGroup(framesPlanTabLayout.createParallelGroup()
                                        .addComponent(editFramesetButton)
                                        .addComponent(resetCompletedButton)
                                        .addGroup(framesPlanTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(moveDownButton)
                                            .addComponent(moveUpButton))
                                        .addComponent(bulkAddButton)
                                        .addComponent(deleteFramesetButton)
                                        .addComponent(addFramesetButton)
                                        .addGroup(framesPlanTabLayout.createSequentialGroup()
                                            .addGap(8, 8, 8)
                                            .addGroup(framesPlanTabLayout.createParallelGroup()
                                                .addComponent(hSpacer1, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(hSpacer2, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(hSpacer3, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE)))))
                                .addGroup(framesPlanTabLayout.createSequentialGroup()
                                    .addGap(30, 30, 30)
                                    .addComponent(autosaveCheckbox)))
                            .addGap(12, 12, 12))
                );
            }
            mainTabFrame.addTab("Frames Plan", null, framesPlanTab, "Lists the frames to be acquired, and progress to date.");

            //======== runSessionTab ========
            {
                runSessionTab.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[grow,fill]" +
                    "[fill]" +
                    "[115,grow,fill]" +
                    "[grow,fill]" +
                    "[fill]" +
                    "[52,fill]" +
                    "[grow,fill]" +
                    "[fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[27]" +
                    "[]" +
                    "[grow]" +
                    "[]" +
                    "[]"));

                //---- label44 ----
                label44.setText("Acquisition Session");
                label44.setHorizontalAlignment(SwingConstants.CENTER);
                label44.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
                runSessionTab.add(label44, "cell 0 0 7 1");

                //---- label42 ----
                label42.setText("Camera Autosave Path: ");
                runSessionTab.add(label42, "cell 0 1");

                //---- autosavePath ----
                autosavePath.setText("(Displayed when connected)");
                autosavePath.setFont(new Font("Lucida Grande", Font.ITALIC, 10));
                autosavePath.setToolTipText("The path on the server where TheSkyX will be auto-saving acquired images.");
                autosavePath.setHorizontalAlignment(SwingConstants.LEFT);
                autosavePath.setPreferredSize(new Dimension(560, 30));
                runSessionTab.add(autosavePath, "cell 2 1 6 1,width 500:800:800,height 30:30:50");

                //---- label32 ----
                label32.setText("Console Log:");
                label32.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                runSessionTab.add(label32, "cell 0 2 3 1");

                //---- label41 ----
                label41.setText("Images Being Acquired:");
                label41.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                runSessionTab.add(label41, "cell 6 2");

                //======== consoleScrollPane ========
                {
                    consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    consoleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                    consoleScrollPane.setAutoscrolls(true);
                    consoleScrollPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                    consoleScrollPane.setFocusable(false);

                    //---- lvConsole ----
                    lvConsole.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    lvConsole.setVisibleRowCount(40);
                    lvConsole.setFocusable(false);
                    lvConsole.setToolTipText("Messages on progress of the session.");
                    lvConsole.setPreferredSize(null);
                    lvConsole.setPrototypeCellValue("Prototype text with Ascenders, decsenders (like \"g\" and \"q\"), and digits like 1, 2, 34, 9.");
                    lvConsole.setMaximumSize(new Dimension(32767, 56));
                    lvConsole.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            lvConsoleComponentResized();
                        }
                    });
                    consoleScrollPane.setViewportView(lvConsole);
                }
                runSessionTab.add(consoleScrollPane, "cell 0 3 6 1,aligny top,grow 100 0");

                //======== scrollPane3 ========
                {

                    //---- sessionFramesetTable ----
                    sessionFramesetTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    sessionFramesetTable.setEnabled(false);
                    sessionFramesetTable.setFillsViewportHeight(true);
                    sessionFramesetTable.setToolTipText("The frames that will be acquired this session. The row we are actively working on is highlighted.");
                    sessionFramesetTable.setMaximumSize(new Dimension(32767, 32767));
                    scrollPane3.setViewportView(sessionFramesetTable);
                }
                runSessionTab.add(scrollPane3, "cell 6 3 3 1,align left top,grow 0 0");

                //---- progressBar ----
                progressBar.setVisible(false);
                runSessionTab.add(progressBar, "cell 0 4 10 1");

                //---- beginSessionButton ----
                beginSessionButton.setText("Begin Session");
                beginSessionButton.setMinimumSize(new Dimension(100, 29));
                beginSessionButton.setToolTipText("Begin the acquisition session.");
                beginSessionButton.addActionListener(e -> beginSessionButtonActionPerformed());
                runSessionTab.add(beginSessionButton, "cell 0 5");

                //---- cancelSessionButton ----
                cancelSessionButton.setText("Cancel Session");
                cancelSessionButton.setToolTipText("Cancel the acquisition session that is in progress.");
                cancelSessionButton.setEnabled(false);
                cancelSessionButton.addActionListener(e -> cancelSessionButtonActionPerformed());
                runSessionTab.add(cancelSessionButton, "cell 3 5,alignx trailing,growx 0");

                //---- coolingMessage ----
                coolingMessage.setText("During acquisition, camera temperature displayed here.");
                runSessionTab.add(coolingMessage, "cell 6 5 2 1,alignx right,growx 0");
            }
            mainTabFrame.addTab("Run Session", null, runSessionTab, "Console to start and monitor the acquisition process");
        }
        contentPane.add(mainTabFrame);
        pack();
        setLocationRelativeTo(getOwner());

        //---- startDayRadioGroup ----
        var startDayRadioGroup = new ButtonGroup();
        startDayRadioGroup.add(startDateNowButton);
        startDayRadioGroup.add(startDateTodayButton);
        startDayRadioGroup.add(startDateGivenButton);

        //---- startTimeRadioGroup ----
        var startTimeRadioGroup = new ButtonGroup();
        startTimeRadioGroup.add(startSunsetButton);
        startTimeRadioGroup.add(startCivilButton);
        startTimeRadioGroup.add(startNauticalButton);
        startTimeRadioGroup.add(startAstronomicalButton);
        startTimeRadioGroup.add(startGivenTimeButton);

        //---- endDayRadioGroup ----
        var endDayRadioGroup = new ButtonGroup();
        endDayRadioGroup.add(endDateDoneButton);
        endDayRadioGroup.add(endDateTodayButton);
        endDayRadioGroup.add(endDateGivenButton);

        //---- endTimeRadioGroup ----
        var endTimeRadioGroup = new ButtonGroup();
        endTimeRadioGroup.add(endSunriseButton);
        endTimeRadioGroup.add(endCivilButton);
        endTimeRadioGroup.add(endNauticalButton);
        endTimeRadioGroup.add(endAstronomicalButton);
        endTimeRadioGroup.add(endGivenTimeButton);

        //---- bindings ----
        bindingGroup = new BindingGroup();
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            startDateNowButton, ELProperty.create("${not selected}"),
            startSunsetButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            startDateNowButton, ELProperty.create("${not selected}"),
            startCivilButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            startDateNowButton, ELProperty.create("${not selected}"),
            startNauticalButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            startDateNowButton, ELProperty.create("${not selected}"),
            startAstronomicalButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            startDateNowButton, ELProperty.create("${not selected}"),
            startGivenTimeButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            startDateGivenButton, BeanProperty.create("selected"),
            startDatePicker, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            endDateDoneButton, ELProperty.create("${not selected}"),
            endSunriseButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            endDateDoneButton, ELProperty.create("${not selected}"),
            endCivilButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            endDateDoneButton, ELProperty.create("${not selected}"),
            endNauticalButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            endDateDoneButton, ELProperty.create("${not selected}"),
            endAstronomicalButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            endDateDoneButton, ELProperty.create("${not selected}"),
            endGivenTimeButton, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            endDateGivenButton, BeanProperty.create("selected"),
            endDatePicker, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            endGivenTimeButton, BeanProperty.create("selected"),
            endTimePicker, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            temperatureRegulatedCheckbox, ELProperty.create("${selected}"),
            targetTemperature, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            temperatureRegulatedCheckbox, ELProperty.create("${selected}"),
            temperatureWithin, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            temperatureRegulatedCheckbox, ELProperty.create("${selected}"),
            coolingCheckInterval, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            temperatureRegulatedCheckbox, ELProperty.create("${selected}"),
            coolingTimeout, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            temperatureRegulatedCheckbox, ELProperty.create("${selected}"),
            coolingRetryCount, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            temperatureRegulatedCheckbox, ELProperty.create("${selected}"),
            coolingRetryDelay, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            temperatureRegulatedCheckbox, ELProperty.create("${selected}"),
            abortOnTempRiseCheckbox, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            sendWOLcheckbox, ELProperty.create("${selected}"),
            wolSecondsBefore, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            sendWOLcheckbox, ELProperty.create("${selected}"),
            wolMacAddress, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            sendWOLcheckbox, BeanProperty.create("selected"),
            wolBroadcastAddress, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            warmUpCheckbox, BeanProperty.create("selected"),
            warmUpSeconds, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            startGivenTimeButton, BeanProperty.create("selected"),
            startTimePicker, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            this, ELProperty.create("${dataModel.temperatureRegulated and dataModel.temperatureAbortOnRise}"),
            abortOnTempRiseThreshold, BeanProperty.create("enabled")));
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            this, ELProperty.create("${dataModel.netAddress != \"\"}"),
            testConnectionButton, BeanProperty.create("enabled")));
        bindingGroup.bind();
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem newMenuItem;
    private JMenuItem openMenuItem;
    private JMenuItem saveAsMenuItem;
    private JMenuItem saveMenuItem;
    private JTabbedPane mainTabFrame;
    private JPanel startEndTab;
    private JLabel label1;
    private JPanel panel6;
    private JLabel label2;
    private JLabel label3;
    private JRadioButton startDateNowButton;
    private JRadioButton startDateTodayButton;
    private JRadioButton startDateGivenButton;
    private DatePicker startDatePicker;
    private JLabel label4;
    private JRadioButton startSunsetButton;
    private JRadioButton startCivilButton;
    private JRadioButton startNauticalButton;
    private JRadioButton startAstronomicalButton;
    private JRadioButton startGivenTimeButton;
    private TimePicker startTimePicker;
    private JLabel startTimeDisplay;
    private JPanel panel5;
    private JLabel label8;
    private JLabel label6;
    private JRadioButton endDateDoneButton;
    private JRadioButton endDateTodayButton;
    private JRadioButton endDateGivenButton;
    private DatePicker endDatePicker;
    private JLabel label7;
    private JRadioButton endSunriseButton;
    private JRadioButton endCivilButton;
    private JRadioButton endNauticalButton;
    private JRadioButton endAstronomicalButton;
    private JRadioButton endGivenTimeButton;
    private TimePicker endTimePicker;
    private JLabel endTimeDisplay;
    private JPanel panel3;
    private JLabel label9;
    private JLabel label11;
    private JTextField locationName;
    private JLabel label13;
    private JTextField timeZoneName;
    private JLabel label12;
    private JTextField latitude;
    private JLabel label10;
    private JTextField longitude;
    private JPanel panel4;
    private JLabel label14;
    private JCheckBox warmUpCheckbox;
    private JTextField warmUpSeconds;
    private JLabel label15;
    private JCheckBox disconnectCheckbox;
    private JPanel vSpacer6;
    private JPanel temperatureTab;
    private JPanel panel1;
    private JLabel label5;
    private JCheckBox temperatureRegulatedCheckbox;
    private JLabel label16;
    private JTextField targetTemperature;
    private JLabel label17;
    private JLabel label18;
    private JTextField temperatureWithin;
    private JLabel label23;
    private JLabel label19;
    private JTextField coolingCheckInterval;
    private JLabel label24;
    private JLabel label20;
    private JTextField coolingTimeout;
    private JLabel label25;
    private JLabel label21;
    private JTextField coolingRetryCount;
    private JLabel label26;
    private JLabel label22;
    private JTextField coolingRetryDelay;
    private JLabel label27;
    private JCheckBox abortOnTempRiseCheckbox;
    private JTextField abortOnTempRiseThreshold;
    private JLabel label28;
    private JPanel serverTab;
    private JLabel label39;
    private JLabel label29;
    private JLabel label33;
    private JCheckBox sendWOLcheckbox;
    private JLabel label30;
    private JTextField serverAddress;
    private JLabel label34;
    private JTextField wolSecondsBefore;
    private JLabel label31;
    private JTextField portNumber;
    private JLabel label35;
    private JTextField wolMacAddress;
    private JLabel label36;
    private JTextField wolBroadcastAddress;
    private JButton testConnectionButton;
    private JLabel testConnectionMessage;
    private JButton sendWOLbutton;
    private JLabel wolTestMessage;
    private JPanel framesPlanTab;
    private JScrollPane scrollPane1;
    private JTable framesetTable;
    private JButton addFramesetButton;
    private JButton deleteFramesetButton;
    private JPanel hSpacer1;
    private JButton editFramesetButton;
    private JButton bulkAddButton;
    private JPanel hSpacer2;
    private JButton resetCompletedButton;
    private JPanel hSpacer3;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JCheckBox autosaveCheckbox;
    private JLabel label40;
    private JPanel runSessionTab;
    private JLabel label44;
    private JLabel label42;
    private JLabel autosavePath;
    private JLabel label32;
    private JLabel label41;
    private JScrollPane consoleScrollPane;
    private JList<String> lvConsole;
    private JScrollPane scrollPane3;
    private JTable sessionFramesetTable;
    private JProgressBar progressBar;
    private JButton beginSessionButton;
    private JButton cancelSessionButton;
    private JLabel coolingMessage;
    private BindingGroup bindingGroup;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

	public void loadDataModel(DataModel dataModelInput, String windowTitle) {
        this.dataModel = dataModelInput;
        this.setTitle(windowTitle);
        this.clearFieldValidityWarnings();

		// Start-End tab pane

        //  Important to set these date fields before the radio buttons, they interact.  If the
        //  radio buttons are set first, it wipes out the stored date values.
        if (dataModelInput.getGivenStartDate() == null) {
            startDatePicker.setDate(null);
        } else {
            startDatePicker.setDate(dataModelInput.getGivenStartDate());
        }
        if (dataModelInput.getGivenStartTime() == null) {
            startTimePicker.setTime(null);
        } else {
            startTimePicker.setTime(dataModelInput.getGivenStartTime());
        }
        if (dataModelInput.getGivenEndDate() == null) {
            endDatePicker.setDate(null);
        } else {
            endDatePicker.setDate(dataModelInput.getGivenEndDate());
        }
        if (dataModelInput.getGivenEndTime() == null) {
            endTimePicker.setTime(null);
        } else {
            endTimePicker.setTime(dataModelInput.getGivenEndTime());
        }

        switch (dataModelInput.getStartDateType()) {
			case NOW:
				startDateNowButton.setSelected(true);
				break;
			case TODAY:
				startDateTodayButton.setSelected(true);
				break;
			case GIVEN_DATE:
				startDateGivenButton.setSelected(true);
				break;
		}
		switch (dataModelInput.getStartTimeType()) {
            case SUNSET:
                startSunsetButton.setSelected(true);
                break;
            case CIVIL_DUSK:
                startCivilButton.setSelected(true);
                break;
            case NAUTICAL_DUSK:
                startNauticalButton.setSelected(true);
                break;
            case ASTRONOMICAL_DUSK:
                startAstronomicalButton.setSelected(true);
                break;
            case GIVEN_TIME:
                startGivenTimeButton.setSelected(true);
                break;
        }
        switch (dataModelInput.getEndDateType()) {
            case WHEN_DONE:
                endDateDoneButton.setSelected(true);
                break;
            case TODAY_TOMORROW:
                endDateTodayButton.setSelected(true);
                break;
            case GIVEN_DATE:
                endDateGivenButton.setSelected(true);
                break;
        }
        switch (dataModelInput.getEndTimeType()) {
            case SUNRISE:
                endSunriseButton.setSelected(true);
                break;
            case CIVIL_DAWN:
                endCivilButton.setSelected(true);
                break;
            case NAUTICAL_DAWN:
                endNauticalButton.setSelected(true);
                break;
            case ASTRONOMICAL_DAWN:
                endAstronomicalButton.setSelected(true);
                break;
            case GIVEN_TIME:
                endGivenTimeButton.setSelected(true);
                break;
        }

        locationName.setText(dataModelInput.getLocationName());
        timeZoneName.setText(String.valueOf(dataModelInput.getTimeZone()));
        latitude.setText(String.valueOf(dataModelInput.getLatitude()));
        longitude.setText(String.valueOf(dataModelInput.getLongitude()));

        warmUpCheckbox.setSelected(dataModelInput.getWarmUpWhenDone());
        warmUpSeconds.setText(String.valueOf(dataModelInput.getWarmUpWhenDoneSeconds()));
        disconnectCheckbox.setSelected(dataModelInput.getDisconnectWhenDone());

		// Temperature tab pane

		temperatureRegulatedCheckbox.setSelected(dataModelInput.getTemperatureRegulated());
		targetTemperature.setText(String.valueOf(dataModelInput.getTemperatureTarget()));
		temperatureWithin.setText(String.valueOf(dataModelInput.getTemperatureWithin()));
		coolingCheckInterval.setText(String.valueOf(dataModelInput.getTemperatureSettleSeconds()));
		coolingTimeout.setText(String.valueOf(dataModelInput.getMaxCoolingWaitTime()));
		coolingRetryCount.setText(String.valueOf(dataModelInput.getTemperatureFailRetryCount()));
		coolingRetryDelay.setText(String.valueOf(dataModelInput.getTemperatureFailRetryDelaySeconds()));
		abortOnTempRiseCheckbox.setSelected(dataModelInput.getTemperatureAbortOnRise());
		abortOnTempRiseThreshold.setText(String.valueOf(dataModelInput.getTemperatureAbortRiseLimit()));

		// Server tab pane

		serverAddress.setText(dataModelInput.getNetAddress());
		portNumber.setText(String.valueOf(dataModelInput.getPortNumber()));
		sendWOLcheckbox.setSelected(dataModelInput.getSendWakeOnLanBeforeStarting());
		wolSecondsBefore.setText(String.valueOf(dataModelInput.getSendWolSecondsBefore()));
		wolMacAddress.setText(dataModelInput.getWolMacAddress());
		wolBroadcastAddress.setText(dataModelInput.getWolBroadcastAddress());

		// Frames Plan tab pane

        autosaveCheckbox.setSelected(dataModelInput.getAutoSaveAfterEachFrame());
        this.framePlanTableModel = FramePlanTableModel.create(dataModelInput);
        framesetTable.setModel(this.framePlanTableModel);

        // Make table column headers bold

        JTableHeader header = framesetTable.getTableHeader();
        Font headerFont = header.getFont();
        Font newFont = new Font(header.getName(), Font.BOLD, headerFont.getSize());
        header.setFont(newFont);

        // Run Session tab pane
        //  Because information on the "Run Session" tab is computed from other data in the
        //  model, and could change, that tab is populated in the responder for clicking on the
        //  tab, not here. That way it is not calculated until needed, and is always recalculated as needed

        //  Listen for changes to the frames plan table

        ListSelectionModel selectionModel = this.framesetTable.getSelectionModel();
        selectionModel.addListSelectionListener(this.frameTableSelectionListener);
        this.framePlanTableSelectionChanged();

        this.displayStartTime();
        this.displayEndTime();

        //  Cooler Power message should initially be empty.
//        this.coolingMessage.setText(" ");
	}

	//  The Begin button on the session tab is enabled only if the session is ready to run.
    //  We check the following:
    //      1. No invalid text fields recorded in the validity dictionary;
    //      2. Server name and port are given
    //      3. At least one incomplete frame set is in the session table

    private void enableBeginButton() {
        boolean okToBegin = true;
        String toolTip = "Begin the acquisition session.";
        //  Any invalid text fields?
        if (this.anyInvalidTextFields()) {
            toolTip = "Disabled because there are invalid text fields.";
            okToBegin = false;
        }
        //  Do we have server information?
        if (okToBegin && !this.serverInfoReady()) {
            toolTip = "Disabled because there is incomplete server information,";
            okToBegin = false;
        }
        //  Is there at least one work item in the work list?
        if (okToBegin && this.countSessionFrameSets() == 0) {
            toolTip = "Disabled because there are no frame sets to be acquired.";
            okToBegin = false;
        }

        this.beginSessionButton.setToolTipText(toolTip);
        this.beginSessionButton.setEnabled(okToBegin);
    }

    private int countSessionFrameSets() {
	    int result = 0;
	    if (this.sessionFrameTableModel != null) {
	        result = this.sessionFrameTableModel.getRowCount();
        }
	    return result;
    }

    private boolean serverInfoReady() {
        // The server name can be blank - ensure it isn't.
        return this.dataModel.getNetAddress().trim().length() > 0;
    }

    // Determine if any text input fields are still invalid.  (We've been recording valid/invalid flags
    // for fields in a dictionary as we process their input.)

    private boolean anyInvalidTextFields() {
        boolean anyInvalid = false;
        for (HashMap.Entry<JTextField,Boolean> entry : this.textFieldValidity.entrySet()) {
            boolean isValid = entry.getValue();
            if (!isValid) {
                anyInvalid = true;
                break;
            }
        }
        return anyInvalid;
    }

    //  Set the Start Time Display field to the time that will be used with the current settings
    //  Sunset, dusk, given time, etc.  If start is "Now", blank the field.
    private void displayStartTime() {
        if (this.dataModel.getStartDateType() == StartDate.NOW) {
            startTimeDisplay.setText(" ");
        } else {
            LocalTime time = this.dataModel.appropriateStartTime();
            if (time == null) {
                startTimeDisplay.setText(" ");
            } else {
                startTimeDisplay.setText(time.toString());
            }
        }
    }

    //  Set the End Time Display field to the time that will be used with the current settings
    //  Sunrise, dawn, given time, etc.  If end is "When Done", blank the field.
    private void displayEndTime() {
        if (this.dataModel.getEndDateType() == EndDate.WHEN_DONE) {
            endTimeDisplay.setText(" ");
        } else {
            LocalTime time = this.dataModel.appropriateEndTime();
            if (time == null) {
                endTimeDisplay.setText(" ");
            } else {
                endTimeDisplay.setText(time.toString());
            }
        }
    }


    @Override
    public void setMaximizedBounds(Rectangle bounds) {
        super.setMaximizedBounds(bounds);
    }

    //  Record the validity of the given text field.
    //  In a dict indexed by the text field, record the validity state so we can, later, quickly check
    //  if all the fields are valid.  Also colour the field red if it is not valid.

    private void recordTextFieldValidity(JTextField theField, boolean isValid) {
	    //  Record validity in map
        if (this.textFieldValidity.containsKey(theField)) {
            this.textFieldValidity.replace(theField, isValid);
        } else {
            this.textFieldValidity.put(theField, isValid);
        }

        //  Set background colour
	    Color backgroundColor = Color.RED;
	    if (isValid) {
	        backgroundColor = Color.WHITE;
        }
	    theField.setBackground(backgroundColor);
    }

    //  We're loading a new data model, which can only be valid since we don't save invalid ones.
    //  So, clear any field validity warnings in the table, by setting the validity to True
    //  and resetting the field colour.

    private void clearFieldValidityWarnings() {
        this.textFieldValidity.forEach((key,value) -> this.recordTextFieldValidity(key,true));
    }

    //  We've been notified from the acquisition thread that it is starting a new frame set, and
    //  given the row index in the session table.  Highlight that row to show where we are.
    public void startRowIndex(int rowIndex) {
        assert (rowIndex >= 0) && (rowIndex < this.sessionFrameTableModel.getRowCount());
        this.sessionFramesetTable.clearSelection();
        this.sessionFramesetTable.setRowSelectionInterval(rowIndex, rowIndex);
    }

    //

    /**
     * The acquisition thread is telling us that a frame has been acquired.
     * Increment the "completed" count in the frameset, which will update it in the session table in the UI.
     * If "auto save after each frame" is on, do a save.
     * @param frameSet      The frame set which just acquired one more image
     */

    public void oneFrameAcquired(FrameSet frameSet) {
        this.consoleLock.lock();
        try {
            frameSet.setNumberComplete(frameSet.getNumberComplete() + 1);
            if (this.dataModel.getAutoSaveAfterEachFrame()) {
                this.saveMenuItemActionPerformed();
            }
        }
        finally {
            //  Use try-finally to ensure unlock happens even if some kind of exception occurs
            this.consoleLock.unlock();
        }
    }
}

// todo make frame table narrower and console wider
