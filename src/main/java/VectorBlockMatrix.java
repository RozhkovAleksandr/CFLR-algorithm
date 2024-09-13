
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class VectorBlockMatrix extends BlockMatrix {

    public VectorBlockMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AbstractMatrix result) {
        if (!isVector(other)) {
            CommonOps_DSCC.mult(BlockHelper.revolutionToTheVertical(matrix), other.matrix, result.matrix);
        } else {
            CommonOps_DSCC.mult(BlockHelper.toDiagMatrix(matrix), BlockHelper.revolutionToTheVertical(other.matrix), result.matrix);
        }
    }

    @Override
    public void add(AbstractMatrix other, AbstractMatrix result) {
        if (!isVector(other)) {
            throw new IllegalArgumentException("The matrix is not blocky.");
        }

        if (matrix.numCols == other.matrix.numCols) {
            CommonOps_DSCC.add(1, matrix, 1, other.matrix, result.matrix, null, null);
        } else {
            if (matrix.numCols > other.matrix.numCols) {
                CommonOps_DSCC.add(1, matrix, 1, BlockHelper.revolutionToTheHorizon(other.matrix), result.matrix, null, null);
            } else {
                CommonOps_DSCC.add(1, matrix, 1, BlockHelper.revolutionToTheVertical(other.matrix), result.matrix, null, null);
            }
        }
    }

    @Override
    public void subtraction(AbstractMatrix other, AbstractMatrix tmp) {
        if (!isVector(other)) {
            throw new IllegalArgumentException("The matrix is not blocky.");
        }

        if (other.matrix.numCols == matrix.numCols) {
            CommonOps_DSCC.add(1, other.matrix.copy(), 1, matrix, other.matrix, null, null);
        } else {
            if (other.matrix.numCols > matrix.numCols) {
                CommonOps_DSCC.changeSign(BlockHelper.revolutionToTheHorizon(matrix), tmp.matrix);
                CommonOps_DSCC.add(1, other.matrix.copy(), 1, matrix, other.matrix, null, null); 
            }else {
                CommonOps_DSCC.changeSign(BlockHelper.revolutionToTheVertical(matrix), tmp.matrix);
                CommonOps_DSCC.add(1, other.matrix.copy(), 1, matrix, other.matrix, null, null);
            }
        }
    }

    private boolean isVector(AbstractMatrix m) {
        return ((m.matrix.numCols == this.matrix.numCols && m.matrix.numRows == this.matrix.numRows)
                || (m.matrix.numCols == this.matrix.numRows && m.matrix.numRows == this.matrix.numCols));
    }
}
