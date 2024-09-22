
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

class BaseMatrix extends AbstractMatrix {

    public BaseMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        if (other instanceof LazyMatrix) {
            if (asistant != null && asistant.getMatrix(n) != null) {
                asistant.getMatrix(n).matrix.zero();
                ((LazyMatrix) other).multiplyOther(matrix, asistant.getMatrix(n));
            }
        } else if (other != null && other.matrix != null) {
            CommonOps_DSCC.mult(this.matrix, other.matrix, asistant.getMatrix(n).matrix);
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        CommonOps_DSCC.add(1.0, this.matrix, 1.0, other.matrix, asistant.getMatrix(n).matrix, null, null);
    }

    @Override
    public AbstractMatrix copy() {
        BaseMatrix copy = new BaseMatrix(new DMatrixSparseCSC(matrix.getNumCols(), matrix.getNumCols()));
        copy.matrix = this.matrix.copy();
        return copy;
    }

    @Override
    public void subtraction(AbstractMatrix other, AsistantMatrix asistant, int n) {
        CommonOps_DSCC.changeSign(this.matrix, asistant.getMatrix(n).matrix);
        CommonOps_DSCC.add(1.0, other.copy().matrix, 1.0, asistant.getMatrix(n).matrix, other.matrix, null, null);
    }

    @Override
    public AbstractMatrix removeNonPositiveElements() {
        AbstractMatrix positiveMatrix = new BaseMatrix(new DMatrixSparseCSC(matrix.getNumCols(), matrix.getNumCols()));
        DMatrixSparseCSC tmp = this.matrix.copy();

        for (int col = 0; col < tmp.getNumCols(); col++) {
            int colStart = tmp.col_idx[col];
            int colEnd = tmp.col_idx[col + 1];

            for (int idx = colStart; idx < colEnd; idx++) {
                int row = tmp.nz_rows[idx];
                double value = tmp.nz_values[idx];

                if (value > 0) {
                    positiveMatrix.set(row, col, 1);
                }
            }
        }

        return positiveMatrix;
    }
}
