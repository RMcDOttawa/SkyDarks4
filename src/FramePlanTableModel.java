import javax.swing.table.AbstractTableModel;

/**
 * Data model used to drive the "Frame Plan" table in the main user interface.
 * This is a table with one row per frame set to be acquired, and columns giving
 * the number of frames in the set, their type, exposure and binning, and
 * the number that have already been completed in previous sessions
 */
public class FramePlanTableModel extends AbstractTableModel {
    private DataModel dataModel;

    public String[] columnHeaders = {"Number", "Type", "Exposure", "Binning", "Complete"};

    /**
     * Static creator for the table data model
     * @param dataModel     Program's main data model, for access to the frame sets list
     * @return
     */
    public static FramePlanTableModel create(DataModel dataModel) {
        FramePlanTableModel newModel = new FramePlanTableModel();
        newModel.dataModel = dataModel;
        return newModel;
    }

    /**
     * Return the number of rows to be displayed in the table.
     * This is the number of elements stored in the data model frameset array
     * @return
     */
    @Override
    public int getRowCount() {
        return this.dataModel.getSavedFrameSets().size();
    }

    /**
     * Return the number of columns to be displayed in the table.
     * This is a fixed property of this table.
     * @return
     */
    @Override
    public int getColumnCount() {
        return this.columnHeaders.length;
    }

    /**
     * Return the value to be displayed at the specified table cell
     * @param rowIndex          Zero-based row index
     * @param columnIndex       Zero-based column index
     * @return (String)         Returns string to be displayed in this cell
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FrameSet row = this.dataModel.getSavedFrameSets().get(rowIndex);
        String result = "INVALID";
        switch (columnIndex) {
            case 0:
                result = String.valueOf(row.getNumberOfFrames());
                break;
            case 1:
                result = String.valueOf(row.getFrameType());
                break;
            case 2:
                result = String.valueOf(row.getExposureSeconds());
                break;
            case 3:
                result = String.format("%d x %d", row.getBinning(), row.getBinning());
                break;
            case 4:
                result = String.valueOf(row.getNumberComplete());
                break;
            default:
                System.out.println("Invalid column number for frame table " + columnIndex);
        }
        return result;
    }

    /**
     * Get the header text to be displayed in the given column
     * @param column        Zero-based column index
     * @return
     */
    @Override
    public String getColumnName(int column) {
        return this.columnHeaders[column];
    }

    /**
     * Delete the specified row from the table and tell the UI to update appropriately
     * @param index     Zero-based row index to delete
     */
    public void deleteRow(int index) {
        this.dataModel.getSavedFrameSets().remove(index);
        this.fireTableRowsDeleted(index, index);
    }

    /**
     * Add the specified frame set to the bottom of the table and tell the UI to update.
     * @param newFrameSet       New frame set to be added to table
     */
    public void appendRow(FrameSet newFrameSet) {
        this.dataModel.getSavedFrameSets().add(newFrameSet);
        int newRowIndex = this.dataModel.getSavedFrameSets().size() - 1;
        this.fireTableRowsInserted(newRowIndex, newRowIndex);
    }

    /**
     * Insert the specified frame set into the table, above the specified row, and tell the UI to update.
     * @param selectedRow       Zero-based row index above which to insert new frame set
     * @param newFrameSet       Frame set to be inserted
     */
    public void insertRow(int selectedRow, FrameSet newFrameSet) {
        this.dataModel.getSavedFrameSets().add(selectedRow, newFrameSet);
        this.fireTableRowsInserted(selectedRow, selectedRow);
    }

    /**
     * Replace the row at the specified index with a new frame set, and update the UI
     * @param replacementPoint      Zero-based row index to be replaced
     * @param changedFrameSet       New frame set to go at that row
     */
    public void replaceRow(int replacementPoint, FrameSet changedFrameSet) {
        this.dataModel.getSavedFrameSets().set(replacementPoint, changedFrameSet);
        this.fireTableRowsUpdated(replacementPoint, replacementPoint);
    }

    /**
     * Set all the "Number Completed" values in the table to zero, and update the UI
     */
    public void resetCompletedCounts() {
        int numFrameSets = this.dataModel.getSavedFrameSets().size();
        if (numFrameSets > 0) {
            for (int index = 0; index < numFrameSets; index++) {
                FrameSet thisFrameSet = this.dataModel.getSavedFrameSets().get(index);
                thisFrameSet.setNumberComplete(0);
            }
            this.fireTableRowsUpdated(0, numFrameSets - 1);
        }
    }
}
