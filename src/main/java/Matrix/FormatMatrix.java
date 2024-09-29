package Matrix;

import java.util.Arrays;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class FormatMatrix extends AbstractMatrix {

    public FormatMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        if (other instanceof LazyMatrix) {
            ((LazyMatrix) other).multiplyOther(matrix, asistant.getMatrix(n));
        } else if (other != null) {
            multHelper(this.matrix, other.matrix, asistant.getMatrix(n).matrix);
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        CommonOps_DSCC.add(1.0, this.matrix, 1.0, other.matrix, asistant.getMatrix(n).matrix, null, null);
    }

    @Override
    public AbstractMatrix copy() {
        FormatMatrix copy = new FormatMatrix(new DMatrixSparseCSC(matrix.getNumCols(), matrix.getNumCols()));
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
        AbstractMatrix positiveMatrix;
        positiveMatrix = new FormatMatrix(this.matrix.copy());

        for (int i = 0; i < positiveMatrix.matrix.nz_length; i++) {
            if (positiveMatrix.matrix.nz_values[i] <= 0) {
                positiveMatrix.matrix.nz_values[i] = 0;
            }
        }

        CommonOps_DSCC.removeZeros(positiveMatrix.matrix, 0);

        double[] new_values = new double[positiveMatrix.matrix.nz_length];
        Arrays.fill(new_values, 1.0);

        positiveMatrix.matrix.nz_values = new_values;

        return positiveMatrix;
    }

    private void multHelper(DMatrixSparseCSC m, DMatrixSparseCSC other, DMatrixSparseCSC result) {
        if (m.nz_length >= other.nz_length) {
            CommonOps_DSCC.mult(m, other, result);
        } else {
            DMatrixSparseCSC tmp1 = new DMatrixSparseCSC(m.numCols, m.numRows);
            DMatrixSparseCSC tmp2 = new DMatrixSparseCSC(other.numCols, other.numRows);
            DMatrixSparseCSC tmp3 = new DMatrixSparseCSC(other.numRows, m.numCols);
            CommonOps_DSCC.transpose(m, tmp1, null);
            CommonOps_DSCC.transpose(other, tmp2, null);
            CommonOps_DSCC.mult(tmp2, tmp1, tmp3);
            CommonOps_DSCC.transpose(tmp3, result, null);
        }
    }
}
