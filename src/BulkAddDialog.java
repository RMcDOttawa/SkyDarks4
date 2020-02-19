import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
/*
 * Created by JFormDesigner on Sun Feb 16 13:17:42 EST 2020
 */



/**
 * @author Richard McDonald
 */
public class BulkAddDialog extends JDialog {
    private static double SECONDS_IN_DAY = 24.0 * 60 * 60;

	private boolean     saveClicked = false;

    private int         numBiasValue = 0;
	private boolean     numBiasValid = true;
	private int         numDarksValue = 0;
	private boolean     numDarksValid = true;

	//  Which binning settings are selected?  Remember zero-based.  1x1 = item[0]
	private boolean[]   biasBinningSelected = {false, false, false, false};
    private boolean[]   darkBinningSelected = {false, false, false, false};

	//  Recording that the various window components are valid
    private boolean exposureListValid = true;
    private ArrayList<Double> exposureLengthsList = new ArrayList<>();


	public BulkAddDialog(MainWindow owner) {
		super(owner);
        initComponents();
		enableSaveButton();

		//  Catch Paste into the exposure times field so we can enable the Save button and ensure only clean pastes happen
        this.exposureLengths.registerKeyboardAction(ae -> handlePasteToExposuresField(this.exposureLengths),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                JComponent.WHEN_FOCUSED);
	}

    private void handlePasteToExposuresField(JTextArea exposureLengths) {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                String pasteableString = (String) clipboard.getData(DataFlavor.stringFlavor);
                exposureLengths.paste();
                this.validateExposureLengths(pasteableString, exposureLengths);
                this.enableSaveButton();
            }

        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    public boolean getSaveClicked() {
		return this.saveClicked;
	}

	//  Validate the "number of bias frames" field.  It can be empty, or a non-negative integer.
    private void numBiasFramesActionPerformed() {
        String fieldContents = numBiasFrames.getText().trim();
        boolean isValid;
        if (fieldContents.length() == 0) {
            this.numBiasValue = 0;
            isValid = true;
        } else {
            ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(fieldContents,
                    0, 1000);
            isValid = validation.left;
            if (isValid) {
                this.numBiasValue = validation.right;
            }
        }
        this.numBiasValid = isValid;
        this.colourFieldValidity(this.numBiasFrames, isValid);
        this.enableSaveButton();
    }

    private void numBiasFramesFocusLost() {
        this.numBiasFramesActionPerformed();
    }

    private void numDarkFramesActionPerformed() {
        String fieldContents = numDarkFrames.getText().trim();
        boolean isValid;
        if (fieldContents.length() == 0) {
            this.numDarksValue = 0;
            isValid = true;
        } else {
            ImmutablePair<Boolean, Integer> validation = Validators.validIntInRange(fieldContents,
                    0, 1000);
            isValid = validation.left;
            if (isValid) {
                this.numDarksValue = validation.right;
            }
        }
        this.numDarksValid = isValid;
        this.colourFieldValidity(this.numDarkFrames, isValid);
        this.enableSaveButton();
	}

    private void numDarkFramesFocusLost() {
        this.numDarkFramesActionPerformed();
    }

    private void biasBinned1x1ActionPerformed() {
		this.biasBinningSelected[0] = biasBinned1x1.isSelected();
		this.enableSaveButton();
	}

    private void biasBinned2x2ActionPerformed() {
        this.biasBinningSelected[1] = biasBinned2x2.isSelected();
		this.enableSaveButton();
	}

    private void biasBinned3x3ActionPerformed() {
        this.biasBinningSelected[2] = biasBinned3x3.isSelected();
		this.enableSaveButton();
    }

    private void biasBinned4x4ActionPerformed() {
        this.biasBinningSelected[3] = biasBinned4x4.isSelected();
		this.enableSaveButton();
	}

    private void darkBinned1x1ActionPerformed() {
        this.darkBinningSelected[0] = darkBinned1x1.isSelected();
		this.enableSaveButton();
    }

    private void darkBinned2x2ActionPerformed() {
        this.darkBinningSelected[1] = darkBinned2x2.isSelected();
		this.enableSaveButton();
    }

    private void darkBinned3x3ActionPerformed() {
        this.darkBinningSelected[2] = darkBinned3x3.isSelected();
		this.enableSaveButton();
	}

    private void darkBinned4x4ActionPerformed() {
        this.darkBinningSelected[3] = darkBinned4x4.isSelected();
		this.enableSaveButton();
	}

	//  Key has been typed in the exposure lengths field.  We'll do two things:
    //  1. Check if it was the Tab key.  If so, tab out of this field, don't enter a tab character.
    //  2. Otherswise, validate the field.  This just means ensuring it is a list of integer or float
    //      numbers separated by whitespace or commas

    private void exposureLengthsKeyTyped(KeyEvent keyEvent) {
        if (keyEvent.getExtendedKeyCode() == KeyEvent.VK_TAB) {
            // Tab key has been pressed.  Tab to next or previous field (shift key = previous)
            if (keyEvent.isShiftDown()) {
                exposureLengths.transferFocusBackward();
            } else {
                exposureLengths.transferFocus();
            }
            // Consume the tab keypress so it doesn't go into the text
            keyEvent.consume();
        } else {
            // That wasn't a tab key.  Make sure the field is still a valid list of exposure times
            //  We validate the text already entered plus the key just typed
            String textToValidate = exposureLengths.getText() + keyEvent.getKeyChar();
            this.validateExposureLengths(textToValidate, exposureLengths);
        }
		this.enableSaveButton();
	}

    private void validateExposureLengths(String textToValidate, JTextArea exposureLengths) {
        ImmutablePair<Boolean, ArrayList<Double>> parseResults = this.parseExposures(textToValidate);
        this.exposureListValid = parseResults.left;
        this.exposureLengthsList = parseResults.right;
        this.colourFieldValidity(exposureLengths, parseResults.left);
    }

    //  Parse and validate the exposures list string.  Rules:
    //  It can be empty, or
    //  It can be a list of numbers separated by commas or white space
    //  Numbers are integers or floating point numbers, non-negative
    //  Multiple white spaces are OK and are ignored.
    //
    //  We return a validity indicator, and a list of parsed values
    
    private ImmutablePair<Boolean, ArrayList<Double>> parseExposures(String text) {
        String[] tokens = text.trim().split("[\\s,]+");
        ArrayList<Double> values = new ArrayList<>(tokens.length);
        boolean allValid = true;
        for (String token: tokens) {
            ImmutablePair<Boolean, Double> validation = Validators.validFloatInRange(token,
                    0.0, SECONDS_IN_DAY);
            if (validation.left) {
                values.add(validation.right);
            } else {
                allValid = false;
            }
        }
	    return ImmutablePair.of(allValid, values);
    }

    // Set background colour of field to reflect validity: red if invalid
    private void colourFieldValidity(JTextComponent theField, boolean isValid) {
        Color backgroundColor = Color.RED;
        if (isValid) {
            backgroundColor = Color.WHITE;
        }
        theField.setBackground(backgroundColor);
    }

    //	Enable the save button only if there is something actionable described in the form.
	//	i.e. at least some bias or dark frames (number of frames and binnings) and, for darks,
	//	a valid exposures field.

	private void enableSaveButton() {

		boolean biasGiven = this.numBiasValid && this.numBiasValue > 0;
		boolean anyBiasBinning = this.countTrue(this.biasBinningSelected) > 0;

		boolean darksGiven = this.numDarksValid && this.numDarksValue > 0;
		boolean anyDarkBinning = this.countTrue(this.darkBinningSelected) > 0;

		this.saveButton.setEnabled( (biasGiven && anyBiasBinning)
        || (darksGiven && anyDarkBinning && this.exposureListValid && this.exposureLengthsList.size() > 0));
	}

	private int countTrue(boolean[] theArray) {
	    int count = 0;
	    for (boolean theBool: theArray) {
	        if (theBool) {
	            count += 1;
            }
        }
	    return count;
    }

    private void saveButtonActionPerformed() {
	    // Before we close, we re-parse the exposures list, because I've found that
        // certain cut-paste operations in the field can get missed and the pre-parsed
        //  list may not be accurate
        ImmutablePair<Boolean, ArrayList<Double>> parseResults = this.parseExposures(exposureLengths.getText());
        this.exposureListValid = parseResults.left;
        assert(this.exposureListValid);  // Otherwise Save should have been disabled
        this.exposureLengthsList = parseResults.right;

        // Close the window, and flag to the caller that this was a save operation
        this.saveClicked = true;
        this.setVisible(false);
    }

    private void cancelButtonActionPerformed() {
        this.saveClicked = false;
        this.setVisible(false);
    }

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        label1 = new JLabel();
        label2 = new JLabel();
        numBiasFrames = new JTextField();
        label3 = new JLabel();
        biasBinned1x1 = new JCheckBox();
        biasBinned2x2 = new JCheckBox();
        biasBinned3x3 = new JCheckBox();
        biasBinned4x4 = new JCheckBox();
        label4 = new JLabel();
        numDarkFrames = new JTextField();
        label5 = new JLabel();
        darkBinned1x1 = new JCheckBox();
        darkBinned2x2 = new JCheckBox();
        darkBinned3x3 = new JCheckBox();
        darkBinned4x4 = new JCheckBox();
        exposureLengths = new JTextArea();
        label6 = new JLabel();
        saveButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setModal(true);
        setMinimumSize(new Dimension(570, 545));
        var contentPane = getContentPane();

        //---- label1 ----
        label1.setText("Bulk Entry of Frame Set Specifications");
        label1.setFont(new Font("Lucida Grande", Font.PLAIN, 24));
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        label1.setVerticalAlignment(SwingConstants.TOP);

        //---- label2 ----
        label2.setText("Bias Frames: ");

        //---- numBiasFrames ----
        numBiasFrames.setMinimumSize(new Dimension(11, 30));
        numBiasFrames.setToolTipText("If you want bias frames, binned as shown below, enter the number of frames of each binning as a single integer.");
        numBiasFrames.addActionListener(e -> numBiasFramesActionPerformed());
        numBiasFrames.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                numBiasFramesFocusLost();
            }
        });

        //---- label3 ----
        label3.setText("each, binned:");

        //---- biasBinned1x1 ----
        biasBinned1x1.setText("1 x 1");
        biasBinned1x1.setToolTipText("Take bias frames binned 1 x 1.");
        biasBinned1x1.addActionListener(e -> biasBinned1x1ActionPerformed());

        //---- biasBinned2x2 ----
        biasBinned2x2.setText("2 x 2");
        biasBinned2x2.setToolTipText("Take bias frames binned 2 x 2.");
        biasBinned2x2.addActionListener(e -> biasBinned2x2ActionPerformed());

        //---- biasBinned3x3 ----
        biasBinned3x3.setText("3 x 3");
        biasBinned3x3.setToolTipText("Take bias frames binned 3 x 3.");
        biasBinned3x3.addActionListener(e -> biasBinned3x3ActionPerformed());

        //---- biasBinned4x4 ----
        biasBinned4x4.setText("4 x 4");
        biasBinned4x4.setToolTipText("Take bias frames binned 4 x 4.");
        biasBinned4x4.addActionListener(e -> biasBinned4x4ActionPerformed());

        //---- label4 ----
        label4.setText("Dark Frames: ");

        //---- numDarkFrames ----
        numDarkFrames.setToolTipText("If you want Dark frames of the binning and exposure lengths shown, enter the number of frames wanted of each combination, as a single integer.  ");
        numDarkFrames.addActionListener(e -> numDarkFramesActionPerformed());
        numDarkFrames.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                numDarkFramesFocusLost();
            }
        });

        //---- label5 ----
        label5.setText("each, binned:");

        //---- darkBinned1x1 ----
        darkBinned1x1.setText("1 x 1");
        darkBinned1x1.setToolTipText("Take dark frames, binned 1 x 1, of the exposure times below.");
        darkBinned1x1.addActionListener(e -> darkBinned1x1ActionPerformed());

        //---- darkBinned2x2 ----
        darkBinned2x2.setText("2 x 2");
        darkBinned2x2.setToolTipText("Take dark frames, binned 2 x 2, of the exposure times below.");
        darkBinned2x2.addActionListener(e -> darkBinned2x2ActionPerformed());

        //---- darkBinned3x3 ----
        darkBinned3x3.setText("3 x 3");
        darkBinned3x3.setToolTipText("Take dark frames, binned 3 x 3, of the exposure times below.");
        darkBinned3x3.addActionListener(e -> darkBinned3x3ActionPerformed());

        //---- darkBinned4x4 ----
        darkBinned4x4.setText("4 x 4");
        darkBinned4x4.setToolTipText("Take dark frames, binned 4 x 4, of the exposure times below.");
        darkBinned4x4.addActionListener(e -> darkBinned4x4ActionPerformed());

        //---- exposureLengths ----
        exposureLengths.setLineWrap(true);
        exposureLengths.setWrapStyleWord(true);
        exposureLengths.setToolTipText("Enter all the exposure times desired for dark frames, separated by blanks or commas. Times are in seconds.");
        exposureLengths.setTabSize(0);
        exposureLengths.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                exposureLengthsKeyTyped(e);
            }
        });

        //---- label6 ----
        label6.setText("<html>Exposure lengths, in seconds, separated by blanks:</html>");

        //---- saveButton ----
        saveButton.setText("Save");
        saveButton.setToolTipText("Close this window and add the frames described here to the plan.");
        saveButton.addActionListener(e -> saveButtonActionPerformed());

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("Close and abandon this window; don't add anything to the plan.");
        cancelButton.addActionListener(e -> cancelButtonActionPerformed());

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(label1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addComponent(label2)
                                    .addGap(18, 18, 18)
                                    .addComponent(numBiasFrames, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(label3, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE))
                                .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addComponent(label4)
                                    .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                            .addGap(99, 99, 99)
                                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addComponent(biasBinned3x3)
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                    .addComponent(darkBinned2x2)
                                                    .addComponent(darkBinned3x3)
                                                    .addComponent(darkBinned4x4)
                                                    .addGroup(contentPaneLayout.createSequentialGroup()
                                                        .addGap(2, 2, 2)
                                                        .addComponent(biasBinned4x4))
                                                    .addComponent(darkBinned1x1))
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                    .addComponent(biasBinned1x1)
                                                    .addComponent(biasBinned2x2))))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                            .addGap(12, 12, 12)
                                            .addComponent(numDarkFrames, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(label5, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)))))
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGap(7, 7, 7)
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addComponent(saveButton)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cancelButton))
                                .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addComponent(label6, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(exposureLengths)))))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addGap(4, 4, 4)
                    .addComponent(label1)
                    .addGap(31, 31, 31)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label2)
                        .addComponent(numBiasFrames, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                        .addComponent(label3))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(biasBinned1x1)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(biasBinned2x2)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(biasBinned3x3)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(biasBinned4x4)
                    .addGap(19, 19, 19)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label4)
                        .addComponent(label5)
                        .addComponent(numDarkFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(darkBinned1x1)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(darkBinned2x2)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(darkBinned3x3)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(darkBinned4x4)
                    .addGap(18, 18, 18)
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(label6, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 67, Short.MAX_VALUE))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(exposureLengths, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(saveButton)
                                .addComponent(cancelButton))))
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JLabel label1;
    private JLabel label2;
    private JTextField numBiasFrames;
    private JLabel label3;
    private JCheckBox biasBinned1x1;
    private JCheckBox biasBinned2x2;
    private JCheckBox biasBinned3x3;
    private JCheckBox biasBinned4x4;
    private JLabel label4;
    private JTextField numDarkFrames;
    private JLabel label5;
    private JCheckBox darkBinned1x1;
    private JCheckBox darkBinned2x2;
    private JCheckBox darkBinned3x3;
    private JCheckBox darkBinned4x4;
    private JTextArea exposureLengths;
    private JLabel label6;
    private JButton saveButton;
    private JButton cancelButton;

    // JFormDesigner - End of variables declaration  //GEN-END:variables

    // Generate the list of framesets described by the form's settings
    //      Bias frames if quantity and binnings are given
    //      Dark frames if quantity, binnings, and one or more exposure times are given
    public ArrayList<FrameSet> generateFramesToAdd() {
        int estimatedNumberNeeded = this.countTrue(this.biasBinningSelected)
                + (this.countTrue(this.darkBinningSelected) * this.exposureLengthsList.size());
        ArrayList<FrameSet> resultList = new ArrayList<>(estimatedNumberNeeded);

        //  Bias frames
        if (this.numBiasValid && (this.numBiasValue > 0)) {
            for (int biasBinIndex = 0; biasBinIndex <= 3; biasBinIndex++) {
                if (this.biasBinningSelected[biasBinIndex]) {
                    resultList.add(FrameSet.of(this.numBiasValue, FrameType.BIAS_FRAME,
                            0.0, biasBinIndex + 1, 0));
                }
            }
        }

        //  Dark frames
        if (this.numDarksValid && this.exposureListValid && (this.numDarksValue > 0)) {
            //  Set up frames for each exposure in the exposures list
            for (Double exposureTime : this.exposureLengthsList) {
                //  Do this exposure length for each selected binning
                for (int darkBinIndex = 0; darkBinIndex <= 3; darkBinIndex++) {
                    if (this.darkBinningSelected[darkBinIndex]) {
                        resultList.add(FrameSet.of(this.numDarksValue, FrameType.DARK_FRAME,
                                exposureTime, darkBinIndex + 1, 0));
                    }
                }
            }
        }

        return resultList;
    }

}
