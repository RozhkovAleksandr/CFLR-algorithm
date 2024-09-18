import java.util.HashSet;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class FastMatrixCell extends AbstractMatrix {

    private final HashSet<DMatrixSparseCSC> matrices = new HashSet<>();
    private final double b;

    public FastMatrixCell(DMatrixSparseCSC matrix) {
        super(matrix);
        this.matrices.add(matrix);
        this.b = 1.5;
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        DMatrixSparseCSC tmp;

        if (isCell(other.matrix)) {
            tmp = new DMatrixSparseCSC(matrix.numCols, matrix.numRows);
            
            asistant.getMatrix("cell", n).matrix.zero();

            for (DMatrixSparseCSC m : matrices) {
                CommonOps_DSCC.mult(m, other.matrix, tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
            }
        } else {
            tmp = new DMatrixSparseCSC(Math.min(matrix.numCols, matrix.numRows), Math.max(matrix.numRows, matrix.numCols));
            
            asistant.getMatrix("horizon", n).matrix.zero();

            for (DMatrixSparseCSC m : matrices) {
                CommonOps_DSCC.mult(m, BlockHelper.revolutionToTheHorizon(other.matrix), tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
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
                CommonOps_DSCC.mult(other, m, tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix,
                        null, null);
            }
        } else {
            tmp = new DMatrixSparseCSC(Math.max(matrix.numCols, matrix.numRows), Math.min(matrix.numRows, matrix.numCols));

            asistant.getMatrix("vertical", n).matrix.zero();

            for (DMatrixSparseCSC m : matrices) {

                CommonOps_DSCC.mult(BlockHelper.revolutionToTheVertical(other), m, tmp);

                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix,
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
        while (f) {
            f = false;
            for (DMatrixSparseCSC m : matrices) {
                if (m.nz_length * b >= other.nz_length() || other.nz_length() * b >= m.nz_length) { 
                    CommonOps_DSCC.add(1.0, m, 1.0, other.matrix, asistant.getMatrix("cell", n).matrix, null, null);
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
        if (!isCell(other.matrix)) {
            throw new IllegalArgumentException("The matrix is not cell.");
        }
        
        for (DMatrixSparseCSC m : matrices) {

            for (int col = 0; col < m.numCols; col++) {
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
        asistant.getMatrix("cell", n).matrix.zero();
        for (DMatrixSparseCSC m : matrices) {
            CommonOps_DSCC.add(1.0, m, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
        }
    }

    private boolean isCell(DMatrixSparseCSC m) {
        return (m.numCols == this.matrix.numCols && m.numRows == this.matrix.numRows);
    }
}
