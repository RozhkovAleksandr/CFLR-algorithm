package Matrix;

import java.util.HashSet;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class FastMatrixVector extends AbstractMatrix {

    private final HashSet<DMatrixSparseCSC> matrices = new HashSet<>();
    private final double b;

    public FastMatrixVector(DMatrixSparseCSC matrix) {
        super(matrix);
        this.matrices.add(matrix);
        this.b = 10;
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        DMatrixSparseCSC tmp = new DMatrixSparseCSC(Math.max(matrix.numCols, matrix.numRows), Math.min(matrix.numCols, matrix.numRows));

        asistant.getMatrix("vertical", n).matrix.zero();

        if (isVector(other.matrix)) {
            for (DMatrixSparseCSC m : matrices) {
                multHelper(BlockHelper.toDiagMatrix(m), BlockHelper.revolutionToTheVertical(other.matrix), tmp);
                CommonOps_DSCC.add(1.0, BlockHelper.revolutionToTheVertical(tmp), 1.0, asistant.getMatrix("vertical", n).copy().matrix, asistant.getMatrix("vertical", n).matrix, null, null);
            }
        } else {
            for (DMatrixSparseCSC m : matrices) {
                multHelper(BlockHelper.revolutionToTheVertical(m), other.matrix, tmp);
                CommonOps_DSCC.add(1.0, BlockHelper.revolutionToTheVertical(tmp), 1.0, asistant.getMatrix("vertical", n).copy().matrix, asistant.getMatrix("vertical", n).matrix, null, null);
            }
        }

        if (!production.endsWith("_i")) {
            asistant.putMatrix(n, BlockHelper.reverseVectorBlockMatrix(asistant.getMatrix(n)));
        }
    }

    public void multiplyOther(DMatrixSparseCSC other, AsistantMatrix asistant, int n, String production) {
        DMatrixSparseCSC tmp;

        if (isVector(other)) {
            tmp = new DMatrixSparseCSC(Math.max(matrix.numCols, matrix.numRows), Math.min(matrix.numCols, matrix.numRows));
            asistant.getMatrix("vertical", n).matrix.zero();
            for (DMatrixSparseCSC m : matrices) {

                multHelper(BlockHelper.toDiagMatrix(other), BlockHelper.revolutionToTheVertical(m), tmp);
                CommonOps_DSCC.add(1.0, BlockHelper.revolutionToTheVertical(tmp), 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
            }
        } else {
            tmp = new DMatrixSparseCSC(Math.min(matrix.numCols, matrix.numRows), Math.max(matrix.numCols, matrix.numRows));
            asistant.getMatrix("horizon", n).matrix.zero();
            for (DMatrixSparseCSC m : matrices) {
                multHelper(other, BlockHelper.revolutionToTheHorizon(m), tmp);
                CommonOps_DSCC.add(1.0, BlockHelper.revolutionToTheHorizon(tmp), 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
            }
        }

        if (!production.endsWith("_i")) {
            asistant.putMatrix(n, BlockHelper.reverseVectorBlockMatrix(asistant.getMatrix(n)));
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        boolean f = true;
        boolean check = false;

        if (matrix.numRows > matrix.numCols) {
            while (f) {
                f = false;
                for (DMatrixSparseCSC m : matrices) {
                    if (m.nz_length * b >= other.nz_length() || other.nz_length() * b >= m.nz_length) {
                        CommonOps_DSCC.add(1.0, m, 1.0, BlockHelper.revolutionToTheVertical(other.matrix), asistant.getMatrix("vertical", n).matrix, null, null);
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
        } else {
            while (f) {
                f = false;

                for (DMatrixSparseCSC m : matrices) {

                    if (m.nz_length * b >= other.nz_length() || other.nz_length() * b >= m.nz_length) {
                        CommonOps_DSCC.add(1.0, m, 1.0, BlockHelper.revolutionToTheHorizon(other.matrix), asistant.getMatrix("horizon", n).matrix, null, null);
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
        }

        if (check) {
            matrices.add(other.matrix.copy());
        }
    }

    @Override
    public void subtraction(AbstractMatrix other, AsistantMatrix asistant, int n) {
        if (!isVector(other.matrix)) {
            throw new IllegalArgumentException("The matrix is not blocky.");
        }
        if (matrix.numCols != other.matrix.numCols) {
            if (matrix.numCols < matrix.numRows) {
                for (DMatrixSparseCSC m : matrices) {

                    CommonOps_DSCC.changeSign(m, asistant.getMatrix("vertical", n).matrix);
                    CommonOps_DSCC.add(1.0, BlockHelper.revolutionToTheVertical(other.matrix), 1.0, asistant.getMatrix(n).matrix, other.matrix, null, null);
                }
            } else {
                for (DMatrixSparseCSC m : matrices) {

                    CommonOps_DSCC.changeSign(m, asistant.getMatrix("horizon", n).matrix);
                    CommonOps_DSCC.add(1.0, BlockHelper.revolutionToTheHorizon(other.matrix), 1.0, asistant.getMatrix(n).matrix, other.matrix, null, null);
                }
            }

        } else {
            if (matrix.numCols < matrix.numRows) {
                for (DMatrixSparseCSC m : matrices) {
                    CommonOps_DSCC.changeSign(m, asistant.getMatrix("vertical", n).matrix);
                    CommonOps_DSCC.add(1.0, other.matrix.copy(), 1.0, asistant.getMatrix(n).matrix, other.matrix, null, null);
                }
            } else {
                for (DMatrixSparseCSC m : matrices) {

                    CommonOps_DSCC.changeSign(m, asistant.getMatrix("horizon", n).matrix);
                    CommonOps_DSCC.add(1.0, other.matrix.copy(), 1.0, asistant.getMatrix(n).matrix, other.matrix, null, null);
                }
            }
        }
    }

    @Override
    public void toOne(AsistantMatrix asistant, int n) {
        if (matrix.numRows > matrix.numCols) {
            asistant.getMatrix("vertical", n).matrix.zero();
        } else {
            asistant.getMatrix("horizon", n).matrix.zero();
        }
        for (DMatrixSparseCSC m : matrices) {
            CommonOps_DSCC.add(1.0, m, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
        }
    }

    private boolean isVector(DMatrixSparseCSC m) {
        return ((m.numCols == this.matrix.numCols && m.numRows == this.matrix.numRows)
                || (m.numCols == this.matrix.numRows && m.numRows == this.matrix.numCols));
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
