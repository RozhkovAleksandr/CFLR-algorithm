
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class VectorBlockMatrix extends BlockMatrix {

    public VectorBlockMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        if (!isVector(other)) {
            CommonOps_DSCC.mult(BlockHelper.revolutionToTheVertical(matrix), other.matrix, asistant.getMatrix("vertical", n).matrix);
        } else {
            CommonOps_DSCC.mult(BlockHelper.toDiagMatrix(matrix), BlockHelper.revolutionToTheVertical(other.matrix), asistant.getMatrix("vertical", n).matrix);
        }

        if (!production.endsWith("_i")) {
            asistant.putMatrix("vertical", n, BlockHelper.reverseVectorBlockMatrix(asistant.getMatrix(n)));
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        if (!isVector(other)) {
            throw new IllegalArgumentException("The matrix is not blocky.");
        }

        if (matrix.numCols == other.matrix.numCols && matrix.numCols < matrix.numRows) {
            CommonOps_DSCC.add(1, matrix, 1, other.matrix, asistant.getMatrix("vertical", n).matrix, null, null);
        } else {
            if (matrix.numCols == other.matrix.numCols && matrix.numCols > matrix.numRows) {
                CommonOps_DSCC.add(1, matrix, 1, other.matrix, asistant.getMatrix("horizon", n).matrix, null, null);
            } else { 
                if (matrix.numCols >= other.matrix.numCols) {
                    CommonOps_DSCC.add(1, matrix, 1, BlockHelper.revolutionToTheHorizon(other.matrix), asistant.getMatrix("horizon", n).matrix, null, null);
                } else {
                    CommonOps_DSCC.add(1, matrix, 1, BlockHelper.revolutionToTheVertical(other.matrix), asistant.getMatrix("vertical", n).matrix, null, null);
                }
            }
        }
    }

    @Override
    public void subtraction(AbstractMatrix other, AbstractMatrix tmp) {
        if (!isVector(other)) {
            throw new IllegalArgumentException("The matrix is not blocky.");
        }

        // change tmp -> matrix in add
        if (other.matrix.numCols == matrix.numCols) {
            CommonOps_DSCC.changeSign(matrix, tmp.matrix);
            CommonOps_DSCC.add(1, other.matrix.copy(), 1, tmp.matrix, other.matrix, null, null);
        } else {


            // change tmp -> matrix in add
            if (other.matrix.numCols >= matrix.numCols) {
                CommonOps_DSCC.changeSign(BlockHelper.revolutionToTheHorizon(matrix), tmp.matrix);
                CommonOps_DSCC.add(1, other.matrix.copy(), 1, tmp.matrix, other.matrix, null, null);
            } else {
                CommonOps_DSCC.changeSign(BlockHelper.revolutionToTheVertical(matrix), tmp.matrix);
                CommonOps_DSCC.add(1, other.matrix.copy(), 1, tmp.matrix, other.matrix, null, null);
            }
        }
    }

    private boolean isVector(AbstractMatrix m) {
        return ((m.matrix.numCols == this.matrix.numCols && m.matrix.numRows == this.matrix.numRows)
                || (m.matrix.numCols == this.matrix.numRows && m.matrix.numRows == this.matrix.numCols));
    }

    @Override
    public AbstractMatrix copy() {
        VectorBlockMatrix copy = new VectorBlockMatrix(new DMatrixSparseCSC(matrix.numCols, matrix.numCols));
        copy.matrix = this.matrix.copy();
        return copy;
    }

    @Override
    public AbstractMatrix removeNonPositiveElements() {
        AbstractMatrix positiveMatrix = new VectorBlockMatrix(new DMatrixSparseCSC(matrix.numRows, matrix.numCols));
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
