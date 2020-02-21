import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
/*
 * Created by JFormDesigner on Sat Feb 15 13:59:58 EST 2020
 */



/**
 * @author Richard McDonald
 */
public class AddFramesetDialog extends JDialog {
    private FrameSet    frameSet;
    private boolean     numberOfFramesValid = true;
    private boolean     exposureValid = true;
    private boolean     numberCompleteValid = true;
    private boolean     saveClicked = true;

    public boolean getSaveClicked() {
        return this.saveClicked;
    }

    public FrameSet getFrameSet() {
        return this.frameSet;
    }



    //  This version of the constructor opens the dialog to create a new frame set

    public AddFramesetDialog(Window owner) {
        super(owner);
        initComponents();
        this.frameSet = new FrameSet();
        this.loadFields();
        // When creating a new frame, we don't offer the "completed" fields
        this.completedLabel.setVisible(false);
        this.numberCompleted.setVisible(false);

        //  Set Save button as default for this dialog
        JRootPane root = this.saveButton.getRootPane();
        root.setDefaultButton(this.saveButton);
    }

    //  This version of the constructor, called with an existing frame set,
    //  opens the dialog to edit that frame set

    public AddFramesetDialog(Window owner, FrameSet frameSetToEdit) {
        super(owner);
        initComponents();
        //  We keep a copy, not the original, so the field updates don't change the original.
        //  This protects the ability to click Cancel and have nothing changed.
        this.frameSet = frameSetToEdit.copy();
        this.loadFields();
        // When editing an existing frame, we offer the "completed" fields
        this.completedLabel.setVisible(true);
        this.numberCompleted.setVisible(true);
        // Change the label and title to reflect that this is an edit, not a new set
        this.dialogTitle.setText("Edit Existing Frame Set");
        this.setTitle("Edit Frame Set");
    }

    private void loadFields() {
        this.numberOfFrames.setText(String.valueOf(this.frameSet.getNumberOfFrames()));
        this.exposureSeconds.setText(String.valueOf(this.frameSet.getExposureSeconds()));
        this.numberCompleted.setText(String.valueOf(this.frameSet.getNumberComplete()));

        // Frame type
        if (this.frameSet.getFrameType() == FrameType.DARK_FRAME) {
            this.darkButton.setSelected(true);
        } else {
            this.biasButton.setSelected(true);
        }

        // Binning
        switch (this.frameSet.getBinning()) {
            case 1:
                this.binning1x1.setSelected(true);
                break;
            case 2:
                this.binning2x2.setSelected(true);
                break;
            case 3:
                this.binning3x3.setSelected(true);
                break;
            case 4:
                this.binning4x4.setSelected(true);
                break;
        }

        this.enableSaveButton();
    }

    //  The Save button is enabled only when all 3 text fields are valid.
    //  (No need to check radio buttons, as they can't be invalid)

    private void enableSaveButton() {
        this.saveButton.setEnabled(this.exposureValid && this.numberOfFramesValid && this.numberCompleteValid);
    }

    // Validate entry in Number of Frames field: must be a positive integer.
    //  If valid, store it in the frame set.

    private void numberOfFramesActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(this.numberOfFrames.getText(),
                1, 1000);
        if (validation.left) {
            this.frameSet.setNumberOfFrames(validation.right);
        }
        this.numberOfFramesValid = validation.left;
        this.colourFieldValidity(this.numberOfFrames, validation.left);
        this.enableSaveButton();
    }

    // Set background colour of field to reflect validity: red if invalid
    private void colourFieldValidity(JTextField theField, boolean isValid) {
        Color backgroundColor = Color.RED;
        if (isValid) {
            backgroundColor = Color.WHITE;
        }
        theField.setBackground(backgroundColor);
    }

    private void numberOfFramesFocusLost() {
        this.numberOfFramesActionPerformed();
    }

    // Validate entry in Number Complete field: must be a positive integer.
    //  If valid, store it in the frame set.  Extra validation:  the number complete can't
    //  be larger than the total number of frame sets being acquired.

    private void numberCompletedActionPerformed() {
        ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(this.numberCompleted.getText(),
                0, this.frameSet.getNumberOfFrames());
        if (validation.left) {
            this.frameSet.setNumberComplete(validation.right);
        }
        this.numberCompleteValid = validation.left;
        this.colourFieldValidity(this.numberCompleted, validation.left);
        this.enableSaveButton();
    }

    private void numberCompletedFocusLost() {
        this.numberCompletedActionPerformed();
    }

    private void binningActionPerformed() {
        if (this.binning1x1.isSelected()) {
            this.frameSet.setBinning(1);
        } else if (this.binning2x2.isSelected()) {
            this.frameSet.setBinning(2);
        } else if (this.binning3x3.isSelected()) {
            this.frameSet.setBinning(3);
        } else {
            assert(this.binning4x4.isSelected());
            this.frameSet.setBinning(4);
        }
    }

    private void biasDarkButtonActionPerformed() {
        if (this.biasButton.isSelected()) {
            this.frameSet.setFrameType(FrameType.BIAS_FRAME);
        } else {
            this.frameSet.setFrameType(FrameType.DARK_FRAME);
        }
    }

    //  Close the dialog and tell the caller that it was a "save" event, so they can
    //  fetch the frameset we've created and deal with it.

    private void saveButtonActionPerformed() {
        this.saveClicked = true;
        this.setVisible(false);
    }

    //  Close the dialog and tell the caller it was cancelled.

    private void cancelButtonActionPerformed() {
        this.saveClicked = false;
        this.setVisible(false);
    }

    // Validate exposure field and store if ok

    private void exposureSecondsActionPerformed() {
        ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(this.exposureSeconds.getText(),
                0, CommonUtils.SECONDS_IN_DAY);
        if (validation.left) {
            this.frameSet.setExposureSeconds(validation.right);
        }
        this.exposureValid = validation.left;
        this.colourFieldValidity(this.exposureSeconds, validation.left);
        this.enableSaveButton();
    }

    private void exposureSecondsFocusLost() {
        exposureSecondsActionPerformed();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        dialogTitle = new JLabel();
        label2 = new JLabel();
        numberOfFrames = new JTextField();
        label3 = new JLabel();
        biasButton = new JRadioButton();
        darkButton = new JRadioButton();
        label4 = new JLabel();
        exposureSeconds = new JTextField();
        label5 = new JLabel();
        binning1x1 = new JRadioButton();
        binning2x2 = new JRadioButton();
        binning3x3 = new JRadioButton();
        binning4x4 = new JRadioButton();
        completedLabel = new JLabel();
        numberCompleted = new JTextField();
        vSpacer1 = new JPanel(null);
        saveButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setMinimumSize(new Dimension(285, 420));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setResizable(false);
        setTitle("Add Frameset");
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
                    "[fill]" +
                    "[fill]" +
                    "[fill]" +
                    "[fill]" +
                    "[fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]"));

                //---- dialogTitle ----
                dialogTitle.setText("Define New Frame Set");
                dialogTitle.setFont(new Font(".SF NS Text", Font.PLAIN, 14));
                contentPanel.add(dialogTitle, "cell 1 0 5 1,alignx center,growx 0");

                //---- label2 ----
                label2.setText("Number of Frames: ");
                contentPanel.add(label2, "cell 1 6 2 1");

                //---- numberOfFrames ----
                numberOfFrames.setToolTipText("How many frames of this type should be acquired?");
                numberOfFrames.addActionListener(e -> numberOfFramesActionPerformed());
                numberOfFrames.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        numberOfFramesFocusLost();
                    }
                });
                contentPanel.add(numberOfFrames, "cell 3 6 3 1");

                //---- label3 ----
                label3.setText("Frame Type: ");
                contentPanel.add(label3, "cell 1 7 2 1");

                //---- biasButton ----
                biasButton.setText("Bias");
                biasButton.setToolTipText("Zero-length bias frames to calibrate dark current.");
                biasButton.addActionListener(e -> biasDarkButtonActionPerformed());
                contentPanel.add(biasButton, "cell 3 7");

                //---- darkButton ----
                darkButton.setText("Dark");
                darkButton.setToolTipText("Dark frames of same length as your light frames.");
                darkButton.addActionListener(e -> biasDarkButtonActionPerformed());
                contentPanel.add(darkButton, "cell 3 8");

                //---- label4 ----
                label4.setText("Exposure Seconds: ");
                contentPanel.add(label4, "cell 1 9 2 1");

                //---- exposureSeconds ----
                exposureSeconds.setToolTipText("Exposure length (seconds) for dark frames.");
                exposureSeconds.addActionListener(e -> exposureSecondsActionPerformed());
                exposureSeconds.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        exposureSecondsFocusLost();
                    }
                });
                contentPanel.add(exposureSeconds, "cell 3 9 3 1");

                //---- label5 ----
                label5.setText("Binning: ");
                contentPanel.add(label5, "cell 1 10 2 1");

                //---- binning1x1 ----
                binning1x1.setText("1 x 1");
                binning1x1.setToolTipText("Bin the acquired frames 1 x 1");
                binning1x1.addActionListener(e -> binningActionPerformed());
                contentPanel.add(binning1x1, "cell 3 10");

                //---- binning2x2 ----
                binning2x2.setText("2 x 2");
                binning2x2.setToolTipText("Bin the acquired frames 2 x 2");
                binning2x2.addActionListener(e -> binningActionPerformed());
                contentPanel.add(binning2x2, "cell 3 11");

                //---- binning3x3 ----
                binning3x3.setText("3 x 3");
                binning3x3.setToolTipText("Bin the acquired frames 3 x 3");
                binning3x3.addActionListener(e -> binningActionPerformed());
                contentPanel.add(binning3x3, "cell 3 12");

                //---- binning4x4 ----
                binning4x4.setText("4 x 4");
                binning4x4.setToolTipText("Bin the acquired frames 4 x 4");
                binning4x4.addActionListener(e -> binningActionPerformed());
                contentPanel.add(binning4x4, "cell 3 13");

                //---- completedLabel ----
                completedLabel.setText("Completed: ");
                contentPanel.add(completedLabel, "cell 1 14 2 1");

                //---- numberCompleted ----
                numberCompleted.setToolTipText("Change the number of frames already completed, causing more or fewer additional frames to be acquired.");
                numberCompleted.addActionListener(e -> numberCompletedActionPerformed());
                numberCompleted.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        numberCompletedFocusLost();
                    }
                });
                contentPanel.add(numberCompleted, "cell 3 14 3 1");
                contentPanel.add(vSpacer1, "cell 2 15");

                //---- saveButton ----
                saveButton.setText("Save");
                saveButton.setToolTipText("Save this frame set to the plan and close this dialog.");
                saveButton.addActionListener(e -> saveButtonActionPerformed());
                contentPanel.add(saveButton, "cell 1 16");

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.setToolTipText("Close this dialog without adding this frame set to the plan.");
                cancelButton.addActionListener(e -> cancelButtonActionPerformed());
                contentPanel.add(cancelButton, "cell 5 16");
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

        //---- frameTypeGroup ----
        var frameTypeGroup = new ButtonGroup();
        frameTypeGroup.add(biasButton);
        frameTypeGroup.add(darkButton);

        //---- binningGroup ----
        var binningGroup = new ButtonGroup();
        binningGroup.add(binning1x1);
        binningGroup.add(binning2x2);
        binningGroup.add(binning3x3);
        binningGroup.add(binning4x4);

        //---- bindings ----
        bindingGroup = new BindingGroup();
        bindingGroup.addBinding(Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
            darkButton, BeanProperty.create("selected"),
            exposureSeconds, BeanProperty.create("editable")));
        bindingGroup.bind();
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel dialogTitle;
    private JLabel label2;
    private JTextField numberOfFrames;
    private JLabel label3;
    private JRadioButton biasButton;
    private JRadioButton darkButton;
    private JLabel label4;
    private JTextField exposureSeconds;
    private JLabel label5;
    private JRadioButton binning1x1;
    private JRadioButton binning2x2;
    private JRadioButton binning3x3;
    private JRadioButton binning4x4;
    private JLabel completedLabel;
    private JTextField numberCompleted;
    private JPanel vSpacer1;
    private JButton saveButton;
    private JButton cancelButton;
    private BindingGroup bindingGroup;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
