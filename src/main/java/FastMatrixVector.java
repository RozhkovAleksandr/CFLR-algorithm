
import java.util.HashMap;
import java.util.HashSet;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class FastMatrixVector extends AbstractMatrix {

    private final HashSet<DMatrixSparseCSC> matrices = new HashSet<>();
    private final double b;

    public FastMatrixVector(DMatrixSparseCSC matrix) {
        super(matrix);
        this.matrices.add(matrix);
        this.b = 1.5;
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        DMatrixSparseCSC tmp = new DMatrixSparseCSC(Math.max(matrix.numCols, matrix.numRows), Math.min(matrix.numCols, matrix.numRows));

        asistant.getMatrix("vertical", n).matrix.zero();

        if (isVector(other.matrix)) {
            for (DMatrixSparseCSC m : matrices) {
                multHelper(BlockHelper.toDiagMatrix(m), BlockHelper.revolutionToTheVertical(other.matrix), tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix("vertical", n).copy().matrix, asistant.getMatrix("vertical", n).matrix, null, null);
            }
        } else {
            for (DMatrixSparseCSC m : matrices) {
                multHelper(BlockHelper.revolutionToTheVertical(m), other.matrix, tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix("vertical", n).copy().matrix, asistant.getMatrix("vertical", n).matrix, null, null);
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
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
            }
        } else {
            tmp = new DMatrixSparseCSC(Math.min(matrix.numCols, matrix.numRows), Math.max(matrix.numCols, matrix.numRows));
            asistant.getMatrix("horizon", n).matrix.zero();
            for (DMatrixSparseCSC m : matrices) {
                multHelper(other, BlockHelper.revolutionToTheHorizon(m), tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
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

                    for (int col = 0; col < m.numCols; col++) {
                        int colStart = m.col_idx[col];
                        int colEnd = m.col_idx[col + 1];

                        for (int idx = colStart; idx < colEnd; idx++) {
                            int row = m.nz_rows[idx];

                            if (m.get(row, col) > 0) {
                                other.matrix.remove(row % matrix.numCols, col + (matrix.numRows / (matrix.numRows / matrix.numCols)) * (row / matrix.numCols));
                            }
                        }
                    }
                }
            } else {
                for (DMatrixSparseCSC m : matrices) {

                    for (int col = 0; col < m.numCols; col++) {
                        int colStart = m.col_idx[col];
                        int colEnd = m.col_idx[col + 1];

                        for (int idx = colStart; idx < colEnd; idx++) {
                            int row = m.nz_rows[idx];

                            if (m.get(row, col) > 0) {
                                other.matrix.remove(row + (matrix.numCols / (matrix.numCols / matrix.numRows)) * (col / matrix.numRows), col % matrix.numRows);
                            }
                        }
                    }
                }
            }

        } else {

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
            multColumnByColumn(m, other, result);
        } else {
            multRowByRow(m, other, result);
        }
    }

    private static void multRowByRow(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC result) {
        HashMap<Integer, Integer> freq = transform(A.nz_rows);
        int counter;

        for (int tmp : freq.keySet()) {
            if (tmp < result.numRows) {
                counter = 0;
                for (int k = 0; k < A.numCols; k++) {
                    if (k < B.numRows) {
                        if (A.get(tmp, k) > 0) {
                            counter++;
                            for (int j = 0; j < B.numCols; j++) {
                                if (B.get(k, j) > 0) {
                                    result.set(tmp, j, 1);
                                }
                            }

                            if (counter == freq.get(tmp)) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static HashMap<Integer, Integer> transform(int[] arr) {
        HashMap<Integer, Integer> freqHashMap = new HashMap<>();

        for (int num : arr) {
            freqHashMap.put(num, freqHashMap.getOrDefault(num, 0) + 1);
        }

        return freqHashMap;
    }

    private static DMatrixSparseCSC multColumnByColumn(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC result) {
        for (int j = 0; j < B.numCols; j++) {
            int colStartB = B.col_idx[j];
            int colEndB = B.col_idx[j + 1];

            if (colStartB != colEndB) {
                for (int bi = colStartB; bi < colEndB; bi++) {
                    int rowB = B.nz_rows[bi];

                    int colStartA = A.col_idx[rowB];
                    int colEndA = A.col_idx[rowB + 1];

                    for (int ai = colStartA; ai < colEndA; ai++) {
                        int rowA = A.nz_rows[ai];

                        result.set(rowA, j, 1);
                    }
                }
            }
        }

        return result;
    }
}
