import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class SessionFrameTableModel extends AbstractTableModel {
    public String[] columnHeaders = {"#", "Typ", "Exp", "Bin", "Done"};
    private ArrayList<FrameSet> sessionFramesets;

    //  Constructor, given the full frameset list
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

    @Override
    public int getRowCount() {
        return this.sessionFramesets.size();
    }

    @Override
    public int getColumnCount() {
        return this.columnHeaders.length;
    }

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

    @Override
    public String getColumnName(int column) {
        return this.columnHeaders[column];
    }

    public ArrayList<FrameSet> getSessionFramesets() {
        return this.sessionFramesets;
    }
}
