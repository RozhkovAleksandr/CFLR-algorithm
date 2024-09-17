
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class CellBlockMatrix extends AbstractMatrix {

    public CellBlockMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        if (isCell(other)) {
            CommonOps_DSCC.mult(matrix, other.matrix, asistant.getMatrix("cell", n).matrix);
        }
        else {
            CommonOps_DSCC.mult(matrix, BlockHelper.revolutionToTheHorizon(other.matrix), asistant.getMatrix("horizon", n).matrix);
            if (!production.endsWith("_i")) {
                asistant.putMatrix("horizon", n, BlockHelper.reverseVectorBlockMatrix(asistant.getMatrix(n)));
            }
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        if (isCell(other)) {
            CommonOps_DSCC.add(1.0, this.matrix, 1.0, other.matrix, asistant.getMatrix("cell", n).matrix, null, null);
        }
        else {
            CommonOps_DSCC.add(1.0, this.matrix, 1.0, BlockHelper.reverse(other.matrix), asistant.getMatrix("cell", n).matrix, null, null);
        }
    }

    @Override
    public void subtraction(AbstractMatrix other, AbstractMatrix tmp) {
        if (!isCell(other)) {
            throw new IllegalArgumentException("The 'other' matrix is not a cell.");
        }

        CommonOps_DSCC.changeSign(this.matrix, tmp.matrix);
        CommonOps_DSCC.add(1.0, other.copy().matrix, 1.0, tmp.matrix, other.matrix, null, null);
    }

    @Override
    public AbstractMatrix removeNonPositiveElements() {
        AbstractMatrix positiveMatrix = new CellBlockMatrix(new DMatrixSparseCSC(matrix.numRows, matrix.numCols));
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

    @Override
    public AbstractMatrix copy() {
        CellBlockMatrix copy = new CellBlockMatrix(new DMatrixSparseCSC(matrix.numRows, matrix.numCols));
        copy.matrix = this.matrix.copy();
        return copy;
    }

    private boolean isCell(AbstractMatrix m) {
        return (m.matrix.numCols == this.matrix.numCols && m.matrix.numRows == this.matrix.numRows);
    }
}
