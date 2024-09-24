package Matrix;

import java.util.HashMap;
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
                multHelper(m, other.matrix, tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix, null, null);
            }
        } else {
            asistant.getMatrix("horizon", n).matrix.zero();

            tmp = asistant.getMatrix("horizon", n).matrix.copy();

            for (DMatrixSparseCSC m : matrices) {

                multHelper(m, BlockHelper.revolutionToTheHorizon(other.matrix), tmp);
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
                multHelper(other, m, tmp);
                CommonOps_DSCC.add(1.0, tmp, 1.0, asistant.getMatrix(n).copy().matrix, asistant.getMatrix(n).matrix,
                        null, null);
            }
        } else {
            asistant.getMatrix("vertical", n).matrix.zero();

            tmp = asistant.getMatrix("vertical", n).matrix.copy();

            for (DMatrixSparseCSC m : matrices) {

                multHelper(BlockHelper.revolutionToTheVertical(other), m, tmp);

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

    private static void multColumnByColumn(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC result) {
        for (int j = 0; j < B.numRows; j++) {
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
    }
}
