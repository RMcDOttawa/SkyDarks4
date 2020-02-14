import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.JTableHeader;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;
import net.miginfocom.swing.*;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
/*
 * Created by JFormDesigner on Wed Feb 12 19:55:56 EST 2020
 */



/**
 * @author Richard McDonald
 */
public class MainWindow extends JFrame {
    private DataModel dataModel;
    public DataModel getDataModel() {
        System.out.println("getDataModel called");
        return dataModel;
    }

    private HashMap<JTextField,Boolean> textFieldValidity = new HashMap<>();
    private FramePlanTableModel framePlanTableModel;
    private static int SESSION_TAB_INDEX = 4;
    private SessionFrameTableModel sessionFrameTableModel;

    public MainWindow() {
        initComponents();
    }

    private void makeDirty() {
        System.out.println("makeDirty stub");
    }

    private static int SECONDS_IN_DAY = 24 * 60 * 60;
    private static double WATER_BOILS = 100.0;
    private static double ABSOLUTE_ZERO = -273.15;

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
        }
    }

    private void startDateNowButtonActionPerformed(ActionEvent e) {
//        System.out.println("startDateNowButtonActionPerformed");
        if (this.dataModel.getStartDateType() != StartDate.NOW) {
            this.makeDirty();
            this.dataModel.setStartDateType(StartDate.NOW);
        }
    }

    private void startDateTodayButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getStartDateType() != StartDate.TODAY) {
            this.makeDirty();
            this.dataModel.setStartDateType(StartDate.TODAY);
        }    }

    private void startDateGivenButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getStartDateType() != StartDate.GIVEN_DATE) {
            this.makeDirty();
            this.dataModel.setStartDateType(StartDate.GIVEN_DATE);
        }
    }

    private void startSunsetButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getStartTimeType() != StartTime.SUNSET) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.SUNSET);
        }
    }

    private void startCivilButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getStartTimeType() != StartTime.CIVIL_DUSK) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.CIVIL_DUSK);
        }
    }

    private void startNauticalButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getStartTimeType() != StartTime.NAUTICAL_DUSK) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.NAUTICAL_DUSK);
        }
    }

    private void startAstronomicalButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getStartTimeType() != StartTime.ASTRONOMICAL_DUSK) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.ASTRONOMICAL_DUSK);
        }
    }

    private void startGivenTimeButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getStartTimeType() != StartTime.GIVEN_TIME) {
            this.makeDirty();
            this.dataModel.setStartTimeType(StartTime.GIVEN_TIME);
        }
    }

    private void startDatePickerPropertyChange(PropertyChangeEvent e) {
        if (this.dataModel == null) {
//            System.out.println("State change before data model set, ignoring");
            ;
        } else {
            LocalDate newDate = startDatePicker.getDate();
            this.makeDirty();
            this.dataModel.setGivenStartDate(newDate);
        }
    }

    private void startTimePickerPropertyChange(PropertyChangeEvent e) {
        if (this.dataModel == null) {
//            System.out.println("State change before data model set, ignoring");
            ;
        } else {
            LocalTime newTime = startTimePicker.getTime();
            this.makeDirty();
            this.dataModel.setGivenStartTime(newTime);
        }
    }

    private void endDateDoneButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getEndDateType() != EndDate.WHEN_DONE) {
            this.makeDirty();
            this.dataModel.setEndDateType(EndDate.WHEN_DONE);
        }
    }

    private void endDateTodayButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getEndDateType() != EndDate.TODAY_TOMORROW) {
            this.makeDirty();
            this.dataModel.setEndDateType(EndDate.TODAY_TOMORROW);
        }
    }

    private void endDateGivenButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getEndDateType() != EndDate.GIVEN_DATE) {
            this.makeDirty();
            this.dataModel.setEndDateType(EndDate.GIVEN_DATE);
        }
    }

    private void endDatePickerPropertyChange(PropertyChangeEvent e) {
        if (this.dataModel == null) {
//            System.out.println("State change before data model set, ignoring");
            ;
        } else {
            LocalDate newDate = endDatePicker.getDate();
            this.makeDirty();
            this.dataModel.setGivenEndDate(newDate);
        }
    }

    private void endSunriseButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getEndTimeType() != EndTime.SUNRISE) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.SUNRISE);
        }
    }

    private void endCivilButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getEndTimeType() != EndTime.CIVIL_DAWN) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.CIVIL_DAWN);
        }
    }

    private void endNauticalButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getEndTimeType() != EndTime.NAUTICAL_DAWN) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.NAUTICAL_DAWN);
        }
    }

    private void endAstronomicalButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getEndTimeType() != EndTime.ASTRONOMICAL_DAWN) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.ASTRONOMICAL_DAWN);
        }
    }

    private void endGivenTimeButtonActionPerformed(ActionEvent e) {
        if (this.dataModel.getEndTimeType() != EndTime.GIVEN_TIME) {
            this.makeDirty();
            this.dataModel.setEndTimeType(EndTime.GIVEN_TIME);
        }
    }

    private void endTimePickerPropertyChange(PropertyChangeEvent e) {
        if (this.dataModel == null) {
//            System.out.println("State change before data model set, ignoring");
            ;
        } else {
            LocalTime newTime = endTimePicker.getTime();
            this.makeDirty();
            this.dataModel.setGivenEndTime(newTime);
        }
    }

    private void locationNameActionPerformed(ActionEvent e) {
        String newName = locationName.getText().trim();
        if (!newName.equals(this.dataModel.getLocationName())) {
            this.makeDirty();
            this.dataModel.setLocationName(newName);
        }
    }

    private void timeZoneOffsetActionPerformed(ActionEvent e) {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(timeZoneOffset.getText(),
                -24, +24);
        if (validation.left) {
            int offset = validation.right;
            if (offset != this.dataModel.getTimeZone()) {
                this.makeDirty();
                this.dataModel.setTimeZone(offset);
            }
        }
        this.recordTextFieldValidity(timeZoneOffset, validation.left);
    }

    private void latitudeActionPerformed(ActionEvent e) {
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

    private void longitudeActionPerformed(ActionEvent e) {
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

    private void warmUpCheckboxActionPerformed(ActionEvent e) {
        boolean checkBoxState = warmUpCheckbox.isSelected();
        if (this.dataModel.getWarmUpWhenDone() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setWarmUpWhenDone(checkBoxState);
        }
    }

    private void disconnectCheckboxActionPerformed(ActionEvent e) {
        boolean checkBoxState = disconnectCheckbox.isSelected();
        if (this.dataModel.getDisconnectWhenDone() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setDisconnectWhenDone(checkBoxState);
        }
    }

    private void warmUpSecondsActionPerformed(ActionEvent e) {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(warmUpSeconds.getText(),
                0, SECONDS_IN_DAY);
        if (validation.left) {
            int seconds = validation.right;
            if (seconds != this.dataModel.getWarmUpWhenDoneSeconds()) {
                this.makeDirty();
                this.dataModel.setWarmUpWhenDoneSeconds(seconds);
            }
        }
        this.recordTextFieldValidity(warmUpSeconds, validation.left);
    }

    private void temperatureRegulatedCheckboxActionPerformed(ActionEvent e) {
        boolean checkBoxState = temperatureRegulatedCheckbox.isSelected();
        if (this.dataModel.getTemperatureRegulated() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setTemperatureRegulated(checkBoxState);
        }
    }

    private void targetTemperatureActionPerformed(ActionEvent e) {
        ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(targetTemperature.getText(),
                ABSOLUTE_ZERO, WATER_BOILS);
        if (validation.left) {
            double temperature = validation.right;
            if (temperature != this.dataModel.getTemperatureTarget()) {
                this.makeDirty();
                this.dataModel.setTemperatureTarget(temperature);
            }
        }
        this.recordTextFieldValidity(targetTemperature, validation.left);
    }

    private void temperatureWithinActionPerformed(ActionEvent e) {
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

    private void coolingCheckIntervalActionPerformed(ActionEvent e) {
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

    private void coolingTimeoutActionPerformed(ActionEvent e) {
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

    private void coolingRetryCountActionPerformed(ActionEvent e) {
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

    private void coolingRetryDelayActionPerformed(ActionEvent e) {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(coolingRetryDelay.getText(),
                0, SECONDS_IN_DAY);
        if (validation.left) {
            int delay = validation.right;
            if (delay != this.dataModel.getTemperatureFailRetryDelaySeconds()) {
                this.makeDirty();
                this.dataModel.setTemperatureFailRetryDelaySeconds(delay);
            }
        }
        this.recordTextFieldValidity(coolingRetryDelay, validation.left);
    }

    private void abortOnTempRiseCheckboxActionPerformed(ActionEvent e) {
        boolean boxState = abortOnTempRiseCheckbox.isSelected();
        if (this.dataModel.getTemperatureAbortOnRise() != boxState) {
            this.makeDirty();
            this.dataModel.setTemperatureAbortOnRise(boxState);
        }
    }

    private void abortOnTempRiseThresholdActionPerformed(ActionEvent e) {
        ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(abortOnTempRiseThreshold.getText(),
                0.1, WATER_BOILS);
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

    private void serverAddressActionPerformed(ActionEvent e) {
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

    private void portNumberActionPerformed(ActionEvent e) {
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

    private void sendWOLcheckboxActionPerformed(ActionEvent e) {
        boolean checkBoxState = sendWOLcheckbox.isSelected();
        if (this.dataModel.getSendWakeOnLanBeforeStarting() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setSendWakeOnLanBeforeStarting(checkBoxState);
        }
    }

    private void wolSecondsBeforeActionPerformed(ActionEvent e) {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(wolSecondsBefore.getText(),
                0, SECONDS_IN_DAY);
        if (validation.left) {
            int seconds = validation.right;
            if (seconds != this.dataModel.getSendWolSecondsBefore()) {
                this.makeDirty();
                this.dataModel.setSendWolSecondsBefore(seconds);
            }
        }
        this.recordTextFieldValidity(wolSecondsBefore, validation.left);
    }

    private void wolMacAddressActionPerformed(ActionEvent e) {
        String proposedMacAddress = wolMacAddress.getText().trim();
        byte[] macAddressBytes = RmNetUtils.parseMacAddress(proposedMacAddress);
        boolean valid = macAddressBytes != null;
        if (valid && !proposedMacAddress.equals(this.dataModel.getWolMacAddress())) {
            this.makeDirty();
            this.dataModel.setWolMacAddress(proposedMacAddress);
        }
        this.recordTextFieldValidity(wolMacAddress, valid);
    }

    private void wolBroadcastAddressActionPerformed(ActionEvent e) {
        String proposedBroadcastAddress = wolBroadcastAddress.getText().trim();
        boolean valid = RmNetUtils.validateIpAddress(proposedBroadcastAddress);
        if (valid && !proposedBroadcastAddress.equals(this.dataModel.getWolBroadcastAddress())) {
            this.makeDirty();
            this.dataModel.setWolBroadcastAddress(proposedBroadcastAddress);
        }
        this.recordTextFieldValidity(wolBroadcastAddress, valid);
    }

    private void autosaveCheckboxActionPerformed(ActionEvent e) {
        boolean checkBoxState = autosaveCheckbox.isSelected();
        if (this.dataModel.getAutoSaveAfterEachFrame() != checkBoxState) {
            this.makeDirty();
            this.dataModel.setAutoSaveAfterEachFrame(checkBoxState);
        }
    }

    private void testConnectionButtonActionPerformed(ActionEvent e) {
        System.out.println("testConnectionButtonActionPerformed");
        // TODO testConnectionButtonActionPerformed
    }

    private void sendWOLbuttonActionPerformed(ActionEvent e) {
        System.out.println("sendWOLbuttonActionPerformed");
        // TODO sendWOLbuttonActionPerformed
    }

    private void addFramesetButtonActionPerformed(ActionEvent e) {
        System.out.println("addFramesetButtonActionPerformed");
        // TODO addFramesetButtonActionPerformed
    }

    private void deleteFramesetButtonActionPerformed(ActionEvent e) {
        System.out.println("deleteFramesetButtonActionPerformed");
        // TODO deleteFramesetButtonActionPerformed
    }

    private void editFramesetButtonActionPerformed(ActionEvent e) {
        System.out.println("editFramesetButtonActionPerformed");
        // TODO editFramesetButtonActionPerformed
    }

    private void bulkAddButtonActionPerformed(ActionEvent e) {
        System.out.println("bulkAddButtonActionPerformed");
        // TODO bulkAddButtonActionPerformed
    }

    private void resetCompletedButtonActionPerformed(ActionEvent e) {
        System.out.println("resetCompletedButtonActionPerformed");
        // TODO resetCompletedButtonActionPerformed
    }

    private void moveUpButtonActionPerformed(ActionEvent e) {
        System.out.println("moveUpButtonActionPerformed");
        // TODO moveUpButtonActionPerformed
    }

    private void moveDownButtonActionPerformed(ActionEvent e) {
        System.out.println("moveDownButtonActionPerformed");
        // TODO moveDownButtonActionPerformed
    }

    private void beginSessionButtonActionPerformed(ActionEvent e) {
        System.out.println("beginSessionButtonActionPerformed");
        // TODO beginSessionButtonActionPerformed
    }

    private void cancelSessionButtonActionPerformed(ActionEvent e) {
        System.out.println("cancelSessionButtonActionPerformed");
        // TODO cancelSessionButtonActionPerformed
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
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
        panel3 = new JPanel();
        label9 = new JLabel();
        label11 = new JLabel();
        locationName = new JTextField();
        label13 = new JLabel();
        timeZoneOffset = new JTextField();
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
        label37 = new JLabel();
        sendWOLbutton = new JButton();
        label38 = new JLabel();
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
        label43 = new JLabel();
        label32 = new JLabel();
        label41 = new JLabel();
        scrollPane2 = new JScrollPane();
        list1 = new JList();
        scrollPane3 = new JScrollPane();
        sessionFramesetTable = new JTable();
        progressBar1 = new JProgressBar();
        beginSessionButton = new JButton();
        cancelSessionButton = new JButton();
        label45 = new JLabel();
        label46 = new JLabel();

        //======== this ========
        setMinimumSize(new Dimension(800, 600));
        var contentPane = getContentPane();
        contentPane.setLayout(new GridLayout());

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("File");
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
                        "[grow,fill]"));

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
                    startDateNowButton.addActionListener(e -> startDateNowButtonActionPerformed(e));
                    panel6.add(startDateNowButton, "cell 1 2 2 1");

                    //---- startDateTodayButton ----
                    startDateTodayButton.setText("Today");
                    startDateTodayButton.setToolTipText("Start later today, at the time given below.");
                    startDateTodayButton.addActionListener(e -> startDateTodayButtonActionPerformed(e));
                    panel6.add(startDateTodayButton, "cell 1 3 2 1");

                    //---- startDateGivenButton ----
                    startDateGivenButton.setText("This Date:");
                    startDateGivenButton.setMinimumSize(new Dimension(48, 23));
                    startDateGivenButton.setToolTipText("Start on this future date, at the time given below.");
                    startDateGivenButton.addActionListener(e -> startDateGivenButtonActionPerformed(e));
                    panel6.add(startDateGivenButton, "cell 1 4");

                    //---- startDatePicker ----
                    startDatePicker.setSettings(null);
                    startDatePicker.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startDatePicker.addPropertyChangeListener(e -> startDatePickerPropertyChange(e));
                    panel6.add(startDatePicker, "cell 2 4");

                    //---- label4 ----
                    label4.setText("Time");
                    panel6.add(label4, "cell 0 5");

                    //---- startSunsetButton ----
                    startSunsetButton.setText("Sunset");
                    startSunsetButton.setToolTipText("Start at sunset on the specified day.");
                    startSunsetButton.addActionListener(e -> startSunsetButtonActionPerformed(e));
                    panel6.add(startSunsetButton, "cell 1 5 2 1");

                    //---- startCivilButton ----
                    startCivilButton.setText("Civil Dusk");
                    startCivilButton.setToolTipText("Start at civil dusk on the specified day.");
                    startCivilButton.addActionListener(e -> startCivilButtonActionPerformed(e));
                    panel6.add(startCivilButton, "cell 1 6 2 1");

                    //---- startNauticalButton ----
                    startNauticalButton.setText("Nautical Dusk");
                    startNauticalButton.setToolTipText("Start at nautical dusk on the specified day.");
                    startNauticalButton.addActionListener(e -> startNauticalButtonActionPerformed(e));
                    panel6.add(startNauticalButton, "cell 1 7 2 1");

                    //---- startAstronomicalButton ----
                    startAstronomicalButton.setText("Astronomical Dusk");
                    startAstronomicalButton.setToolTipText("Start at astronomical dusk on the specified day.");
                    startAstronomicalButton.addActionListener(e -> startAstronomicalButtonActionPerformed(e));
                    panel6.add(startAstronomicalButton, "cell 1 8 2 1");

                    //---- startGivenTimeButton ----
                    startGivenTimeButton.setText("This Time:");
                    startGivenTimeButton.setMinimumSize(new Dimension(48, 23));
                    startGivenTimeButton.setToolTipText("Start at the given time on the specified day.");
                    startGivenTimeButton.addActionListener(e -> startGivenTimeButtonActionPerformed(e));
                    panel6.add(startGivenTimeButton, "cell 1 9");

                    //---- startTimePicker ----
                    startTimePicker.addPropertyChangeListener(e -> startTimePickerPropertyChange(e));
                    panel6.add(startTimePicker, "cell 2 9");
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
                    endDateDoneButton.addActionListener(e -> endDateDoneButtonActionPerformed(e));
                    panel5.add(endDateDoneButton, "cell 1 2 2 1");

                    //---- endDateTodayButton ----
                    endDateTodayButton.setText("Today / Tomorrow");
                    endDateTodayButton.setToolTipText("Stop today or tomorrow at the time specified below. (Tomorrow if that time has passed today.)");
                    endDateTodayButton.addActionListener(e -> endDateTodayButtonActionPerformed(e));
                    panel5.add(endDateTodayButton, "cell 1 3 2 1");

                    //---- endDateGivenButton ----
                    endDateGivenButton.setText("This Date:");
                    endDateGivenButton.setToolTipText("Stop on this future date, at the time specified below.");
                    endDateGivenButton.addActionListener(e -> endDateGivenButtonActionPerformed(e));
                    panel5.add(endDateGivenButton, "cell 1 4");

                    //---- endDatePicker ----
                    endDatePicker.addPropertyChangeListener(e -> endDatePickerPropertyChange(e));
                    panel5.add(endDatePicker, "cell 2 4");

                    //---- label7 ----
                    label7.setText("Time");
                    panel5.add(label7, "cell 0 5");

                    //---- endSunriseButton ----
                    endSunriseButton.setText("Sunrise");
                    endSunriseButton.setToolTipText("Stop at sunrise on the date given above.");
                    endSunriseButton.addActionListener(e -> endSunriseButtonActionPerformed(e));
                    panel5.add(endSunriseButton, "cell 1 5 2 1");

                    //---- endCivilButton ----
                    endCivilButton.setText("Civil Dawn");
                    endCivilButton.setToolTipText("Stop at civil dawn on the date given above.");
                    endCivilButton.addActionListener(e -> endCivilButtonActionPerformed(e));
                    panel5.add(endCivilButton, "cell 1 6 2 1");

                    //---- endNauticalButton ----
                    endNauticalButton.setText("Nautical Dawn");
                    endNauticalButton.setToolTipText("Stop at nautical dawn on the date given above.");
                    endNauticalButton.addActionListener(e -> endNauticalButtonActionPerformed(e));
                    panel5.add(endNauticalButton, "cell 1 7 2 1");

                    //---- endAstronomicalButton ----
                    endAstronomicalButton.setText("Astronomical Dawn");
                    endAstronomicalButton.setToolTipText("Stop at astronomical dawn on the date given above.");
                    endAstronomicalButton.addActionListener(e -> endAstronomicalButtonActionPerformed(e));
                    panel5.add(endAstronomicalButton, "cell 1 8 2 1");

                    //---- endGivenTimeButton ----
                    endGivenTimeButton.setText("This Time:");
                    endGivenTimeButton.setToolTipText("Stop at the given time on the date given above.");
                    endGivenTimeButton.addActionListener(e -> endGivenTimeButtonActionPerformed(e));
                    panel5.add(endGivenTimeButton, "cell 1 9");

                    //---- endTimePicker ----
                    endTimePicker.addPropertyChangeListener(e -> endTimePickerPropertyChange(e));
                    panel5.add(endTimePicker, "cell 2 9");
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
                    locationName.addActionListener(e -> locationNameActionPerformed(e));
                    panel3.add(locationName, "cell 1 2");

                    //---- label13 ----
                    label13.setText("Time Zone: ");
                    panel3.add(label13, "cell 0 3");

                    //---- timeZoneOffset ----
                    timeZoneOffset.setToolTipText("Time zone as offset from UTC.  e.g. EST = -5");
                    timeZoneOffset.addActionListener(e -> timeZoneOffsetActionPerformed(e));
                    panel3.add(timeZoneOffset, "cell 1 3");

                    //---- label12 ----
                    label12.setText("Latitude: ");
                    panel3.add(label12, "cell 0 4");

                    //---- latitude ----
                    latitude.setToolTipText("Latitude of observing site.");
                    latitude.addActionListener(e -> latitudeActionPerformed(e));
                    panel3.add(latitude, "cell 1 4");

                    //---- label10 ----
                    label10.setText("Longitude: ");
                    panel3.add(label10, "cell 0 5");

                    //---- longitude ----
                    longitude.setToolTipText("Longitude of observing site.");
                    longitude.addActionListener(e -> longitudeActionPerformed(e));
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
                    warmUpCheckbox.addActionListener(e -> warmUpCheckboxActionPerformed(e));
                    panel4.add(warmUpCheckbox, "cell 0 2");

                    //---- warmUpSeconds ----
                    warmUpSeconds.setToolTipText("How long to leave CCD warming up before disconnecting.");
                    warmUpSeconds.addActionListener(e -> warmUpSecondsActionPerformed(e));
                    panel4.add(warmUpSeconds, "cell 1 2");

                    //---- label15 ----
                    label15.setText("seconds");
                    panel4.add(label15, "cell 2 2");

                    //---- disconnectCheckbox ----
                    disconnectCheckbox.setText("Disconnect Camera (after warmup)");
                    disconnectCheckbox.setToolTipText("Disconnect camera when done.");
                    disconnectCheckbox.addActionListener(e -> disconnectCheckboxActionPerformed(e));
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
                    temperatureRegulatedCheckbox.addActionListener(e -> temperatureRegulatedCheckboxActionPerformed(e));
                    panel1.add(temperatureRegulatedCheckbox, "cell 2 1 3 1");

                    //---- label16 ----
                    label16.setText("Target Temperature: ");
                    panel1.add(label16, "cell 2 2");

                    //---- targetTemperature ----
                    targetTemperature.setMinimumSize(new Dimension(100, 26));
                    targetTemperature.setToolTipText("Target temperature setpoint for camera.");
                    targetTemperature.addActionListener(e -> targetTemperatureActionPerformed(e));
                    panel1.add(targetTemperature, "cell 3 2");

                    //---- label17 ----
                    label17.setText("\u00b0 C");
                    panel1.add(label17, "cell 4 2");

                    //---- label18 ----
                    label18.setText("Within +/-:");
                    panel1.add(label18, "cell 2 3");

                    //---- temperatureWithin ----
                    temperatureWithin.setToolTipText("How close to target is good enough to begin session?");
                    temperatureWithin.addActionListener(e -> temperatureWithinActionPerformed(e));
                    panel1.add(temperatureWithin, "cell 3 3");

                    //---- label23 ----
                    label23.setText("\u00b0 C");
                    panel1.add(label23, "cell 4 3");

                    //---- label19 ----
                    label19.setText("Cooling Check Interval: ");
                    panel1.add(label19, "cell 2 4");

                    //---- coolingCheckInterval ----
                    coolingCheckInterval.setToolTipText("While cooling, check camera temperature this often.");
                    coolingCheckInterval.addActionListener(e -> coolingCheckIntervalActionPerformed(e));
                    panel1.add(coolingCheckInterval, "cell 3 4");

                    //---- label24 ----
                    label24.setText("seconds");
                    panel1.add(label24, "cell 4 4");

                    //---- label20 ----
                    label20.setText("Max Time to Try Cooling: ");
                    panel1.add(label20, "cell 2 5");

                    //---- coolingTimeout ----
                    coolingTimeout.setToolTipText("If camera doesn't reach target temperature in this time, assume it never will.");
                    coolingTimeout.addActionListener(e -> coolingTimeoutActionPerformed(e));
                    panel1.add(coolingTimeout, "cell 3 5");

                    //---- label25 ----
                    label25.setText("seconds");
                    panel1.add(label25, "cell 4 5");

                    //---- label21 ----
                    label21.setText("Cooling Retry Count: ");
                    panel1.add(label21, "cell 2 6");

                    //---- coolingRetryCount ----
                    coolingRetryCount.setToolTipText("If camera fails to reach target temperature, wait a bit and retry this many times.");
                    coolingRetryCount.addActionListener(e -> coolingRetryCountActionPerformed(e));
                    panel1.add(coolingRetryCount, "cell 3 6");

                    //---- label26 ----
                    label26.setText("times");
                    panel1.add(label26, "cell 4 6");

                    //---- label22 ----
                    label22.setText("Cooling Retry Delay: ");
                    panel1.add(label22, "cell 2 7");

                    //---- coolingRetryDelay ----
                    coolingRetryDelay.setToolTipText("How long to wait after a failed cooling attempt.");
                    coolingRetryDelay.addActionListener(e -> coolingRetryDelayActionPerformed(e));
                    panel1.add(coolingRetryDelay, "cell 3 7");

                    //---- label27 ----
                    label27.setText("seconds");
                    panel1.add(label27, "cell 4 7");

                    //---- abortOnTempRiseCheckbox ----
                    abortOnTempRiseCheckbox.setText("Abort if Temp Rises: ");
                    abortOnTempRiseCheckbox.setToolTipText("If temperature rises above target durring acquisition, abort the session.");
                    abortOnTempRiseCheckbox.addActionListener(e -> abortOnTempRiseCheckboxActionPerformed(e));
                    panel1.add(abortOnTempRiseCheckbox, "cell 2 8");

                    //---- abortOnTempRiseThreshold ----
                    abortOnTempRiseThreshold.setToolTipText("How much temperature needs to rise to abort session.");
                    abortOnTempRiseThreshold.addActionListener(e -> abortOnTempRiseThresholdActionPerformed(e));
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
                sendWOLcheckbox.addActionListener(e -> sendWOLcheckboxActionPerformed(e));
                serverTab.add(sendWOLcheckbox, "cell 3 4 2 1");

                //---- label30 ----
                label30.setText("IP Address or Host Name: ");
                serverTab.add(label30, "cell 0 5");

                //---- serverAddress ----
                serverAddress.setToolTipText("IPv4 address or host name of server.");
                serverAddress.addActionListener(e -> serverAddressActionPerformed(e));
                serverTab.add(serverAddress, "cell 1 5");

                //---- label34 ----
                label34.setText("Seconds before start to send WOL: ");
                serverTab.add(label34, "cell 3 5");

                //---- wolSecondsBefore ----
                wolSecondsBefore.setToolTipText("How long before acquisition to send the wakeup command.");
                wolSecondsBefore.addActionListener(e -> wolSecondsBeforeActionPerformed(e));
                serverTab.add(wolSecondsBefore, "cell 4 5");

                //---- label31 ----
                label31.setText("Port Number: ");
                serverTab.add(label31, "cell 0 6");

                //---- portNumber ----
                portNumber.setToolTipText("Port number where TheSkyX is listening.");
                portNumber.addActionListener(e -> portNumberActionPerformed(e));
                serverTab.add(portNumber, "cell 1 6");

                //---- label35 ----
                label35.setText("Server MAC address: ");
                serverTab.add(label35, "cell 3 6");

                //---- wolMacAddress ----
                wolMacAddress.setToolTipText("MAC address of computer where TheSkyX runs.");
                wolMacAddress.addActionListener(e -> wolMacAddressActionPerformed(e));
                serverTab.add(wolMacAddress, "cell 4 6");

                //---- label36 ----
                label36.setText("Network broadcast address: ");
                serverTab.add(label36, "cell 3 7");

                //---- wolBroadcastAddress ----
                wolBroadcastAddress.setToolTipText("Address to broadcast wakeup to whole LAN. Use 255.255.255.255 except in very special circumstances.");
                wolBroadcastAddress.addActionListener(e -> wolBroadcastAddressActionPerformed(e));
                serverTab.add(wolBroadcastAddress, "cell 4 7");

                //---- testConnectionButton ----
                testConnectionButton.setText("Test Connection");
                testConnectionButton.setMinimumSize(new Dimension(100, 29));
                testConnectionButton.setToolTipText("Try to connect to TheSkyX to see if it works.");
                testConnectionButton.addActionListener(e -> testConnectionButtonActionPerformed(e));
                serverTab.add(testConnectionButton, "cell 0 9,alignx left,growx 0");

                //---- label37 ----
                label37.setText("message");
                serverTab.add(label37, "cell 1 9");

                //---- sendWOLbutton ----
                sendWOLbutton.setText("Send WOL Now");
                sendWOLbutton.setToolTipText("Send the Wakeup command now.");
                sendWOLbutton.addActionListener(e -> sendWOLbuttonActionPerformed(e));
                serverTab.add(sendWOLbutton, "cell 3 9,alignx left,growx 0");

                //---- label38 ----
                label38.setText("message");
                serverTab.add(label38, "cell 4 9");
            }
            mainTabFrame.addTab("TheSkyX Server", null, serverTab, "Information for connecting to the TheSkyX server");

            //======== framesPlanTab ========
            {

                //======== scrollPane1 ========
                {

                    //---- framesetTable ----
                    framesetTable.setFillsViewportHeight(true);
                    framesetTable.setToolTipText("The complete set of frames to be acquired, and what has already been completed.");
                    scrollPane1.setViewportView(framesetTable);
                }

                //---- addFramesetButton ----
                addFramesetButton.setText("+");
                addFramesetButton.setToolTipText("Add a new frame set above the selected row or to the end of the table.");
                addFramesetButton.addActionListener(e -> addFramesetButtonActionPerformed(e));

                //---- deleteFramesetButton ----
                deleteFramesetButton.setText("-");
                deleteFramesetButton.setToolTipText("Remove the selected frame set(s) from the table");
                deleteFramesetButton.addActionListener(e -> deleteFramesetButtonActionPerformed(e));

                //---- editFramesetButton ----
                editFramesetButton.setText("Edit");
                editFramesetButton.setToolTipText("Edit the specification of the selected frame set.");
                editFramesetButton.addActionListener(e -> editFramesetButtonActionPerformed(e));

                //---- bulkAddButton ----
                bulkAddButton.setText("Bulk Add");
                bulkAddButton.setToolTipText("Rapidly add multiple frame sets in a standard pattern.");
                bulkAddButton.addActionListener(e -> bulkAddButtonActionPerformed(e));

                //---- resetCompletedButton ----
                resetCompletedButton.setText("Reset Completed");
                resetCompletedButton.setToolTipText("Set all the \"Completed\" counts back to zero, causing all frame sets to be re-acquired.");
                resetCompletedButton.addActionListener(e -> resetCompletedButtonActionPerformed(e));

                //---- moveUpButton ----
                moveUpButton.setText("Up");
                moveUpButton.setToolTipText("Move the selected frame set(s) up one row.");
                moveUpButton.addActionListener(e -> moveUpButtonActionPerformed(e));

                //---- moveDownButton ----
                moveDownButton.setText("Down");
                moveDownButton.setToolTipText("Move the selected frame set(s) down one row.");
                moveDownButton.addActionListener(e -> moveDownButtonActionPerformed(e));

                //---- autosaveCheckbox ----
                autosaveCheckbox.setText("Auto-save after each completed frame");
                autosaveCheckbox.setToolTipText("During acquisition, save this plan after each acquired frame.");
                autosaveCheckbox.addActionListener(e -> autosaveCheckboxActionPerformed(e));

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
                            .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
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
                    "[fill]" +
                    "[fill]" +
                    "[grow,fill]" +
                    "[fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]" +
                    "[grow]" +
                    "[]" +
                    "[]"));

                //---- label44 ----
                label44.setText("Acquisition Session");
                label44.setHorizontalAlignment(SwingConstants.CENTER);
                label44.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
                runSessionTab.add(label44, "cell 0 0 8 1");

                //---- label42 ----
                label42.setText("Camera Autosave Path: ");
                runSessionTab.add(label42, "cell 0 1");

                //---- label43 ----
                label43.setText("(Displayed when connected)");
                label43.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
                label43.setToolTipText("The path on the server where TheSkyX will be auto-saving acquired images.");
                runSessionTab.add(label43, "cell 1 1 2 1");

                //---- label32 ----
                label32.setText("Console Log:");
                label32.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                runSessionTab.add(label32, "cell 0 2 3 1");

                //---- label41 ----
                label41.setText("Images Being Acquired:");
                label41.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
                runSessionTab.add(label41, "cell 7 2");

                //======== scrollPane2 ========
                {

                    //---- list1 ----
                    list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    list1.setVisibleRowCount(22);
                    list1.setFocusable(false);
                    list1.setToolTipText("Messages on progress of the session.");
                    list1.setPrototypeCellValue("10:30 AM THis is a typical line in the console log.");
                    scrollPane2.setViewportView(list1);
                }
                runSessionTab.add(scrollPane2, "cell 0 3 6 1,aligny top,grow 100 0");

                //======== scrollPane3 ========
                {

                    //---- sessionFramesetTable ----
                    sessionFramesetTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    sessionFramesetTable.setEnabled(false);
                    sessionFramesetTable.setFillsViewportHeight(true);
                    sessionFramesetTable.setToolTipText("The frames that will be acquired this session. The row we are actively working on is highlighted.");
                    scrollPane3.setViewportView(sessionFramesetTable);
                }
                runSessionTab.add(scrollPane3, "cell 7 3 3 1,align left top,grow 0 0");
                runSessionTab.add(progressBar1, "cell 0 4 10 1");

                //---- beginSessionButton ----
                beginSessionButton.setText("Begin Session");
                beginSessionButton.setMinimumSize(new Dimension(100, 29));
                beginSessionButton.setToolTipText("Begin the acquisition session.");
                beginSessionButton.addActionListener(e -> beginSessionButtonActionPerformed(e));
                runSessionTab.add(beginSessionButton, "cell 0 5");

                //---- cancelSessionButton ----
                cancelSessionButton.setText("Cancel Session");
                cancelSessionButton.setToolTipText("Cancel the acquisition session that is in progress.");
                cancelSessionButton.addActionListener(e -> cancelSessionButtonActionPerformed(e));
                runSessionTab.add(cancelSessionButton, "cell 3 5,alignx trailing,growx 0");

                //---- label45 ----
                label45.setText("Cooler Power: ");
                runSessionTab.add(label45, "cell 7 5,alignx right,growx 0");

                //---- label46 ----
                label46.setText("100%");
                label46.setToolTipText("The camera reports that its cooler is running at this power level.");
                runSessionTab.add(label46, "cell 8 5");
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
            startGivenTimeButton, ELProperty.create("${selected}"),
            startTimePicker, BeanProperty.create("enabled")));
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
            warmUpCheckbox, BeanProperty.create("selected"),
            warmUpSeconds, BeanProperty.create("enabled")));
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
            temperatureRegulatedCheckbox, ELProperty.create("${selected}"),
            abortOnTempRiseThreshold, BeanProperty.create("enabled")));
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
            this, ELProperty.create("${dataModel.netAddress eq \"x\"}"),
            testConnectionButton, BeanProperty.create("enabled")));
        bindingGroup.bind();
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JMenuBar menuBar1;
    private JMenu menu1;
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
    private JPanel panel3;
    private JLabel label9;
    private JLabel label11;
    private JTextField locationName;
    private JLabel label13;
    private JTextField timeZoneOffset;
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
    private JLabel label37;
    private JButton sendWOLbutton;
    private JLabel label38;
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
    private JLabel label43;
    private JLabel label32;
    private JLabel label41;
    private JScrollPane scrollPane2;
    private JList list1;
    private JScrollPane scrollPane3;
    private JTable sessionFramesetTable;
    private JProgressBar progressBar1;
    private JButton beginSessionButton;
    private JButton cancelSessionButton;
    private JLabel label45;
    private JLabel label46;
    private BindingGroup bindingGroup;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

	public void loadDataModel(DataModel dataModel) {
        this.dataModel = dataModel;

		// Start-End tab pane

		switch (dataModel.getStartDateType()) {
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
		switch (dataModel.getStartTimeType()) {
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
        switch (dataModel.getEndDateType()) {
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
        switch (dataModel.getEndTimeType()) {
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
        if (dataModel.getGivenStartDate() != null) {
			startDatePicker.setDate(dataModel.getGivenStartDate());
		}
        if (dataModel.getGivenStartTime() != null) {
        	startTimePicker.setTime(dataModel.getGivenStartTime());
		}
        if (dataModel.getGivenEndDate() != null) {
        	endDatePicker.setDate(dataModel.getGivenEndDate());
		}
        if (dataModel.getGivenEndTime() != null) {
        	endTimePicker.setTime(dataModel.getGivenEndTime());
		}

        locationName.setText(dataModel.getLocationName());
        timeZoneOffset.setText(String.valueOf(dataModel.getTimeZone()));
        latitude.setText(String.valueOf(dataModel.getLatitude()));
        longitude.setText(String.valueOf(dataModel.getLongitude()));

        warmUpCheckbox.setSelected(dataModel.getWarmUpWhenDone());
        warmUpSeconds.setText(String.valueOf(dataModel.getWarmUpWhenDoneSeconds()));
        disconnectCheckbox.setSelected(dataModel.getDisconnectWhenDone());

		// Temperature tab pane

		temperatureRegulatedCheckbox.setSelected(dataModel.getTemperatureRegulated());
		targetTemperature.setText(String.valueOf(dataModel.getTemperatureTarget()));
		temperatureWithin.setText(String.valueOf(dataModel.getTemperatureWithin()));
		coolingCheckInterval.setText(String.valueOf(dataModel.getTemperatureSettleSeconds()));
		coolingTimeout.setText(String.valueOf(dataModel.getMaxCoolingWaitTime()));
		coolingRetryCount.setText(String.valueOf(dataModel.getTemperatureFailRetryCount()));
		coolingRetryDelay.setText(String.valueOf(dataModel.getTemperatureFailRetryDelaySeconds()));
		abortOnTempRiseCheckbox.setSelected(dataModel.getTemperatureAbortOnRise());
		abortOnTempRiseThreshold.setText(String.valueOf(dataModel.getTemperatureAbortRiseLimit()));

		// Server tab pane

		serverAddress.setText(dataModel.getNetAddress());
		portNumber.setText(String.valueOf(dataModel.getPortNumber()));
		sendWOLcheckbox.setSelected(dataModel.getSendWakeOnLanBeforeStarting());
		wolSecondsBefore.setText(String.valueOf(dataModel.getSendWolSecondsBefore()));
		wolMacAddress.setText(dataModel.getWolMacAddress());
		wolBroadcastAddress.setText(dataModel.getWolBroadcastAddress());

		// Frames Plan tab pane

        autosaveCheckbox.setSelected(dataModel.getAutoSaveAfterEachFrame());
        this.framePlanTableModel = FramePlanTableModel.create(dataModel);
        framesetTable.setModel(this.framePlanTableModel);

        // Make table column headers bold and 1 point larger than default

        JTableHeader header = framesetTable.getTableHeader();
        Font headerFont = header.getFont();
        Font newFont = new Font(header.getName(), Font.BOLD, headerFont.getSize() + 1);
        header.setFont(newFont);

        // Run Session tab pane
        //  Because information on the "Run Session" tab is computed from other data in the
        //  model, and could change, that tab is populated in the responder for clicking on the
        //  tab, not here. That way it is not calculated until needed, and is always recalculated as needed

//		enableContextSensitiveControls();
	}


    @Override
    public void setMaximizedBounds(Rectangle bounds) {
        super.setMaximizedBounds(bounds);
    }

    public static void main(String[] args) {
        //  If we are running on a Mac, use the system menu bar instead of windows-style window menu
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("mac os x")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        //  Create and open the main window
		DataModel dataModel = DataModel.newInstance();
        MainWindow mainWindow = new MainWindow();
		mainWindow.loadDataModel(dataModel);
		mainWindow.setVisible(true);
    }

    //  Record the validity of the given text field.
    //  In a dict indexed by the text field, record the validity state so we can, later, quickly check
    //  if all the fields are valid.  Also colour the field red if it is not valid.

    private void recordTextFieldValidity(JTextField theField, boolean isValid) {
	    //  Record validity in map
        if (this.textFieldValidity.containsKey(theField)) {
            this.textFieldValidity.replace(theField, Boolean.valueOf(isValid));
        } else {
            this.textFieldValidity.put(theField, Boolean.valueOf(isValid));
        }

        //  Set background colour
	    Color backgroundColor = Color.RED;
	    if (isValid) {
	        backgroundColor = Color.WHITE;
        }
	    theField.setBackground(backgroundColor);
    }

}

// TODO Bind enabled fields as appropriate
// TODO Put calculated time in display field for start and end