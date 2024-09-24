package Matrix;

import java.util.HashSet;
import java.util.Iterator;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class LazyMatrix extends AbstractMatrix {

    private final HashSet<DMatrixSparseCSC> matrices = new HashSet<>();
    private final double b;

    public LazyMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
        this.matrices.add(matrix);
        this.b = 10;
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        DMatrixSparseCSC tmp = new DMatrixSparseCSC(other.matrix.numCols, other.matrix.numCols);

        asistant.getMatrix(n).matrix.zero();

        for (DMatrixSparseCSC m : matrices) {
            CommonOps_DSCC.mult(m, other.matrix, tmp);
            CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
        }
    }

    public void multiplyOther(DMatrixSparseCSC other, AbstractMatrix result) {
        DMatrixSparseCSC tmp = new DMatrixSparseCSC(other.numCols, other.numCols);

        for (DMatrixSparseCSC m : matrices) {
            CommonOps_DSCC.mult(other, m, tmp);

            CommonOps_DSCC.add(1.0, tmp, 1.0, result.copy().matrix, result.matrix, null, null);
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        boolean f = true;
        while (f) {
            f = false;
            for (DMatrixSparseCSC m : matrices) {
                if (m.nz_length * b >= other.nz_length() || other.nz_length() * b >= m.nz_length) {
                    CommonOps_DSCC.add(1.0, m, 1.0, other.matrix, asistant.getMatrix(n).matrix, null, null);
                    matrices.remove(m);

                    other.matrix = asistant.getMatrix(n).matrix.copy();

                    f = true;
                    break;
                }
            }
        }
        matrices.add(other.matrix.copy());
    }

    @Override
    public void subtraction(AbstractMatrix other, AsistantMatrix asistant, int n) {
        for (DMatrixSparseCSC m : matrices) {

            for (int col = 0; col < m.getNumCols(); col++) {
                int colStart = m.col_idx[col];
                int colEnd = m.col_idx[col + 1];

                for (int idx = colStart; idx < colEnd; idx++) {
                    int row = m.nz_rows[idx];

                    if (m.get(row, col) > 0) {
                        other.matrix.remove(row, col);
                    }
                }
            }
        }
    }

    @Override
    public void toOne(AsistantMatrix asistant, int n) {
        asistant.getMatrix(n).matrix.zero();
        for (DMatrixSparseCSC m : matrices) {
            CommonOps_DSCC.add(1.0, m, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
        }
    }

    @Override
    public void set(int row, int col, double value) {
        Iterator<DMatrixSparseCSC> iterator = matrices.iterator();
        if (iterator.hasNext()) {
            iterator.next().set(row, col, value);
        }
    }

    @Override
    public AbstractMatrix copy() {
        return null;
    }

    @Override
    public AbstractMatrix removeNonPositiveElements() {
        return null;
    }
}
