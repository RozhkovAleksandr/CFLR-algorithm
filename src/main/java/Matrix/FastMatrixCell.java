package Matrix;

import java.util.HashSet;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class FastMatrixCell extends AbstractMatrix {

    private final HashSet<DMatrixSparseCSC> matrices = new HashSet<>();
    private final double b;

    public FastMatrixCell(DMatrixSparseCSC matrix) {
        super(matrix);
        this.matrices.add(matrix);
        this.b = 10;
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        DMatrixSparseCSC tmp;

        if (isCell(other.matrix)) {
            tmp = new DMatrixSparseCSC(matrix.numCols, matrix.numRows);

            asistant.getMatrix("cell", n).matrix.zero();

            for (DMatrixSparseCSC m : matrices) {
                multHelper(m, other.matrix, tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
            }
        } else {
            asistant.getMatrix("horizon", n).matrix.zero();

            tmp = asistant.getMatrix("horizon", n).matrix.copy();

            for (DMatrixSparseCSC m : matrices) {

                multHelper(m, BlockHelper.revolutionToTheHorizon(other.matrix), tmp);
                CommonOps_DSCC.add(1.0, BlockHelper.revolutionToTheHorizon(tmp), 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
            }

            if (!production.endsWith("_i")) {
                asistant.putMatrix(n, BlockHelper.reverseVectorBlockMatrix(asistant.getMatrix(n)));
            }
        }
    }

    public void multiplyOther(DMatrixSparseCSC other, AsistantMatrix asistant, int n, String production) {
        DMatrixSparseCSC tmp;

        if (isCell(other)) {
            tmp = new DMatrixSparseCSC(matrix.numCols, matrix.numRows);

            asistant.getMatrix("cell", n).matrix.zero();

            for (DMatrixSparseCSC m : matrices) {
                multHelper(other, m, tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix,
                        null, null);
            }
        } else {
            asistant.getMatrix("vertical", n).matrix.zero();

            tmp = asistant.getMatrix("vertical", n).matrix.copy();

            for (DMatrixSparseCSC m : matrices) {

                multHelper(BlockHelper.revolutionToTheVertical(other), m, tmp);

                CommonOps_DSCC.add(1.0, BlockHelper.revolutionToTheVertical(tmp), 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix,
                        null, null);
            }

            if (!production.endsWith("_i")) {
                asistant.putMatrix(n, BlockHelper.reverseVectorBlockMatrix(asistant.getMatrix(n)));
            }
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        boolean f = true;
        boolean check = false;

        while (f) {
            f = false;
            for (DMatrixSparseCSC m : matrices) {
                if (m.nz_length * b >= other.nz_length() || other.nz_length() * b >= m.nz_length) {
                    CommonOps_DSCC.add(1.0, m, 1.0, other.matrix, asistant.getMatrix("cell", n).matrix, null, null);
                    if (m.nz_length != asistant.getMatrix(n).matrix.nz_length) {

                        matrices.remove(m);

                        other.matrix = asistant.getMatrix(n).matrix.copy();

                        f = true;
                        check = true;
                    }
                    break;
                }
            }
        }
        if (check) {
            matrices.add(other.matrix.copy());
        }
    }

    @Override
    public void subtraction(AbstractMatrix other, AsistantMatrix asistant, int n) {
        if (!isCell(other.matrix)) {
            throw new IllegalArgumentException("The matrix is not cell.");
        }

        for (DMatrixSparseCSC m : matrices) {

            CommonOps_DSCC.changeSign(m, asistant.getMatrix("cell", n).matrix);
            CommonOps_DSCC.add(1.0, other.matrix.copy(), 1.0, asistant.getMatrix(n).matrix, other.matrix, null, null);
        }
    }

    @Override
    public void toOne(AsistantMatrix asistant, int n) {
        asistant.getMatrix("cell", n).matrix.zero();
        for (DMatrixSparseCSC m : matrices) {
            CommonOps_DSCC.add(1.0, m, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
        }
    }

    private boolean isCell(DMatrixSparseCSC m) {
        return (m.numCols == this.matrix.numCols && m.numRows == this.matrix.numRows);
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
