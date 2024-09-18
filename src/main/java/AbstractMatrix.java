
import org.ejml.data.DMatrixSparseCSC;

abstract public class AbstractMatrix {

    protected DMatrixSparseCSC matrix;

    public AbstractMatrix(DMatrixSparseCSC matrix) {
        this.matrix = matrix;
    }

    public abstract void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production);

    public abstract void add(AbstractMatrix other, AsistantMatrix asistant, int n);

    public int nz_length() {
        return matrix.nz_length;
    }

    public double get(int row, int col) {
        return matrix.get(row, col);
    }

    public AbstractMatrix copy() {
        return null;
    }

    public AbstractMatrix removeNonPositiveElements() {
        return null;
    }

    public void set(int row, int col, double value) {
        matrix.set(row, col, value);
    }

    public int getNumRows() {
        return matrix.numRows;
    }

    public int getNumCols() {
        return matrix.numCols;
    }

    public void print() {
        matrix.print();
    }

    public int col_idx(int col) {
        return matrix.col_idx[col];
    }

    public int[] col_idx() {
        return matrix.col_idx;
    }

    public int nz_rows(int col) {
        return matrix.nz_rows[col];
    }

    public int[] nz_rows() {
        return matrix.nz_rows;
    }

    public double nz_values(int col) {
        return matrix.nz_values[col];
    }

    public abstract void subtraction(AbstractMatrix other, AsistantMatrix asistant, int n) ;
    
    public void toOne(AsistantMatrix asistant, int n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
