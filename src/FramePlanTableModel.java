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
}
