import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Data model driving the "session frame table", a table that appears on the "Run Session"
 * tab in the main user interface window. This table contains frame sets that will be acquired
 * in the session - i.e. it omits any frame sets that are completed.
 */
public class SessionFrameTableModel extends AbstractTableModel {
    public String[] columnHeaders = {"#", "Typ", "Exp", "Bin", "Done"};
    private ArrayList<FrameSet> sessionFramesets;

    /**
     * Static constructor, given the full frameset list
     * @param allFrameSets      Frame sets to be considered for inclusion in session table
     * @return (object)         The table model constructed
     */
    public static SessionFrameTableModel of (ArrayList<FrameSet> allFrameSets) {
        SessionFrameTableModel newModel = new SessionFrameTableModel();

        //  Pull out just the framesets that are not complete
        ArrayList<FrameSet> sessionList = new ArrayList<>(allFrameSets.size());
        allFrameSets.forEach((nextSet) -> {
            if (nextSet.getNumberOfFrames() > nextSet.getNumberComplete()) {
                sessionList.add(nextSet);
            }
        });

        //  Remember this reduced list to drive the session table
        newModel.sessionFramesets = sessionList;

        return newModel;
    }

    /**
     * Tell the table how many rows there are
     * @return  (int)       Number of rows in the table
     */
    @Override
    public int getRowCount() {
        return this.sessionFramesets.size();
    }

    /**
     * Tell the table how many columns there are (this is a fixed property of the data
     * we are displaying.)
     * @return  (int)       Number of columns in the table
     */
    @Override
    public int getColumnCount() {
        return this.columnHeaders.length;
    }

    /**
     * Get value to be displayed in a given table cell
     * @param rowIndex          zero-based row index of the cell
     * @param columnIndex       zero-based column index of the cell
     * @return (String)         string to be displayed in the indexed cell
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FrameSet row = this.sessionFramesets.get(rowIndex);
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
                result = String.format("%dx%d", row.getBinning(), row.getBinning());
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
     * Return the header text to be placed over a column
     * @param column        Zero-based column index
     * @return (String)     Column header text
     */
    @Override
    public String getColumnName(int column) {
        return this.columnHeaders[column];
    }

    /**
     * Return the list of framesets driving this data model
     * @return (array)      List of frame sets
     */
    public ArrayList<FrameSet> getSessionFramesets() {
        return this.sessionFramesets;
    }
}
