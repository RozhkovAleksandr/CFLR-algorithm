package Matrix;

import java.util.Arrays;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class CellBlockMatrix extends AbstractMatrix {

    public CellBlockMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        switch (other.getClass().getSimpleName()) {
            case "FastMatrixVector":
                ((FastMatrixVector) other).multiplyOther(matrix, asistant, n, production);
                break;
            case "FastMatrixCell":
                ((FastMatrixCell) other).multiplyOther(matrix, asistant, n, production);
                break;
            default:
                if (isCell(other)) {
                    CommonOps_DSCC.mult(matrix, other.matrix, asistant.getMatrix("cell", n).matrix);
                } else {
                    CommonOps_DSCC.mult(matrix, BlockHelper.revolutionToTheHorizon(other.matrix), asistant.getMatrix("horizon", n).matrix);
                    if (!production.endsWith("_i")) {
                        asistant.putMatrix(n, BlockHelper.reverseVectorBlockMatrix(asistant.getMatrix(n)));
                    }
                }
                break;
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        if (isCell(other)) {
            CommonOps_DSCC.add(1.0, this.matrix, 1.0, other.matrix, asistant.getMatrix("cell", n).matrix, null, null);
        } else {
            CommonOps_DSCC.add(1.0, this.matrix, 1.0, BlockHelper.reverse(other.matrix), asistant.getMatrix("cell", n).matrix, null, null);
        }
    }

    @Override
    public void subtraction(AbstractMatrix other, AsistantMatrix asistant, int n) {
        if (!isCell(other)) {
            throw new IllegalArgumentException("The 'other' matrix is not a cell.");
        }

        CommonOps_DSCC.changeSign(this.matrix, asistant.getMatrix(n).matrix);
        CommonOps_DSCC.add(1.0, other.copy().matrix, 1.0, asistant.getMatrix(n).matrix, other.matrix, null, null);
    }

    @Override
    public AbstractMatrix removeNonPositiveElements() {
        AbstractMatrix positiveMatrix = new CellBlockMatrix(this.matrix.copy());

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
