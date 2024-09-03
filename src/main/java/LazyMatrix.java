
import java.util.HashSet;
import java.util.Iterator;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

class LazyMatrix extends AbstractMatrix {

    private final HashSet<DMatrixSparseCSC> matrices = new HashSet<>();
    private final int b;

    public LazyMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
        this.matrices.add(matrix);
        this.b = matrix.numCols * 1;
    }


    @Override
    public void multiply(AbstractMatrix other, AbstractMatrix result) {
        DMatrixSparseCSC tmp = new DMatrixSparseCSC(other.matrix.numCols, other.matrix.numCols);

        for (DMatrixSparseCSC m : matrices) {
            CommonOps_DSCC.mult(m, other.matrix, tmp);
            piecewiseAdd(tmp, result.copy().matrix, result.matrix);
        }
    }

    private void piecewiseAdd(DMatrixSparseCSC first, DMatrixSparseCSC second, DMatrixSparseCSC result) {
        for (int col = 0; col < first.getNumCols(); col++) {
            int colStart = first.col_idx[col];
            int colEnd = first.col_idx[col + 1];

            for (int idx = colStart; idx < colEnd; idx++) {
                int row = first.nz_rows[idx];
                double value = first.nz_values[idx];

                if (value > 0) {
                    result.set(row, col, 1);
                }
            }
        }



        for (int col = 0; col < second.getNumCols(); col++) {
            int colStart = second.col_idx[col];
            int colEnd = second.col_idx[col + 1];

            for (int idx = colStart; idx < colEnd; idx++) {
                int row = second.nz_rows[idx];
                double value = second.nz_values[idx];

                if (value > 0) {
                    result.set(row, col, 1);
                }
            }
        }
    }

    public void multiplyOther(DMatrixSparseCSC other, AbstractMatrix result) {
        DMatrixSparseCSC tmp = new DMatrixSparseCSC(other.numCols, other.numCols);

        for (DMatrixSparseCSC m : matrices) {
            CommonOps_DSCC.mult(other, m, tmp);
            piecewiseAdd(tmp, result.copy().matrix, result.matrix);
        }
    }

    @Override
    public void add(AbstractMatrix other, AbstractMatrix result) {
        int border;
        DMatrixSparseCSC tmp = new DMatrixSparseCSC(other.matrix.numCols, other.matrix.numCols);

        boolean f = true;
        while (f) {
            border = other.nz_length() * b;
            f = false;
            for (DMatrixSparseCSC m : matrices) {
                if (m.nz_length * b >= other.nz_length() || border >= m.nz_length) {
                    piecewiseAdd(m, other.matrix, tmp);
                    matrices.remove(m);

                    other.matrix = tmp.copy();

                    f = true;
                    break;
                }
            }
        }
        matrices.add(other.matrix.copy());
    }

    @Override
    public void subtraction(AbstractMatrix other, AbstractMatrix tmp) {
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
    public void toOne(AbstractMatrix tmp) {
        tmp.matrix.zero();
        for (DMatrixSparseCSC m : matrices) {
            piecewiseAdd(m, tmp.copy().matrix, tmp.matrix);
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
