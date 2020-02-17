import javax.swing.table.AbstractTableModel;

public class FramePlanTableModel extends AbstractTableModel {
    private DataModel dataModel;

    public String[] columnHeaders = {"Number", "Type", "Exposure", "Binning", "Complete"};

    public static FramePlanTableModel create(DataModel dataModel) {
        FramePlanTableModel newModel = new FramePlanTableModel();
        newModel.dataModel = dataModel;
        return newModel;
    }

    @Override
    public int getRowCount() {
        return this.dataModel.getSavedFrameSets().size();
    }

    @Override
    public int getColumnCount() {
        return this.columnHeaders.length;
    }

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

    @Override
    public String getColumnName(int column) {
        return this.columnHeaders[column];
    }

    public void deleteRow(int index) {
        this.dataModel.getSavedFrameSets().remove(index);
        this.fireTableRowsDeleted(index, index);
    }

    public void appendRow(FrameSet newFrameSet) {
        this.dataModel.getSavedFrameSets().add(newFrameSet);
        int newRowIndex = this.dataModel.getSavedFrameSets().size() - 1;
        this.fireTableRowsInserted(newRowIndex, newRowIndex);
    }

    public void insertRow(int selectedRow, FrameSet newFrameSet) {
        this.dataModel.getSavedFrameSets().add(selectedRow, newFrameSet);
        this.fireTableRowsInserted(selectedRow, selectedRow);
    }

    public void replaceRow(int replacementPoint, FrameSet changedFrameSet) {
        this.dataModel.getSavedFrameSets().set(replacementPoint, changedFrameSet);
        this.fireTableRowsUpdated(replacementPoint, replacementPoint);
    }

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
