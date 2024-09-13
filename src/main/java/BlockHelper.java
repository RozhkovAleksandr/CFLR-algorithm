
import org.ejml.data.DMatrixSparseCSC;

public class BlockHelper {

    public static DMatrixSparseCSC revolutionToTheHorizon(DMatrixSparseCSC tmp) {
        if (isHorizon(tmp)) {
            return tmp;
        }

        DMatrixSparseCSC ans = new DMatrixSparseCSC(tmp.numCols, tmp.numRows);

        for (int col = 0; col < tmp.getNumCols(); col++) {
            int colStart = tmp.col_idx[col];
            int colEnd = tmp.col_idx[col + 1];

            for (int idx = colStart; idx < colEnd; idx++) {
                int row = tmp.nz_rows[idx];
                double value = tmp.nz_values[idx];

                if (value > 0) {
                    ans.set(col, row, 1);
                }
            }
        }

        return ans;
    }

    public static boolean isHorizon(DMatrixSparseCSC a) {
        return a.numCols > a.numRows;
    }

    public static DMatrixSparseCSC revolutionToTheVertical(DMatrixSparseCSC tmp) {
        if (!isHorizon(tmp)) {
            return tmp;
        }

        DMatrixSparseCSC ans = new DMatrixSparseCSC(tmp.numCols, tmp.numRows);

        for (int col = 0; col < tmp.getNumCols(); col++) {
            int colStart = tmp.col_idx[col];
            int colEnd = tmp.col_idx[col + 1];

            for (int idx = colStart; idx < colEnd; idx++) {
                int row = tmp.nz_rows[idx];
                double value = tmp.nz_values[idx];

                if (value > 0) {
                    ans.set(col, row, 1);
                }
            }
        }

        return ans;
    }

    public static DMatrixSparseCSC toDiagMatrix(DMatrixSparseCSC tmp) {
        int maxim = Math.max(tmp.numCols, tmp.numRows);
        int minim = Math.min(tmp.numCols, tmp.numRows);

        DMatrixSparseCSC ans = new DMatrixSparseCSC(maxim, maxim);

        if (tmp.numCols < tmp.numRows) {
            for (int col = 0; col < tmp.getNumCols(); col++) {
                int colStart = tmp.col_idx[col];
                int colEnd = tmp.col_idx[col + 1];

                for (int idx = colStart; idx < colEnd; idx++) {
                    int row = tmp.nz_rows[idx];
                    double value = tmp.nz_values[idx];

                    if (value > 0) {
                        ans.set(row, col + (row / minim * minim), 1);
                    }
                }
            }
        } else {
            for (int col = 0; col < tmp.getNumCols(); col++) {
                int colStart = tmp.col_idx[col];
                int colEnd = tmp.col_idx[col + 1];

                for (int idx = colStart; idx < colEnd; idx++) {
                    int row = tmp.nz_rows[idx];
                    double value = tmp.nz_values[idx];

                    if (value > 0) {
                        ans.set(row + (col / minim * minim), col, 1);
                    }
                }
            }
        }

        return ans;
    }

    public static DMatrixSparseCSC reverse(DMatrixSparseCSC tmp) {
        DMatrixSparseCSC ans;

        if (tmp.numCols > tmp.numRows) {
            int minimum = tmp.numRows;
            ans = new DMatrixSparseCSC(minimum, minimum);

            for (int col = 0; col < tmp.getNumCols(); col++) {
                int colStart = tmp.col_idx[col];
                int colEnd = tmp.col_idx[col + 1];

                for (int idx = colStart; idx < colEnd; idx++) {
                    int row = tmp.nz_rows[idx];
                    double value = tmp.nz_values[idx];

                    if (value > 0) {
                        ans.set(row, col % minimum, 1);
                    }
                }
            }
        } else {
            int minimum = tmp.numCols;
            ans = new DMatrixSparseCSC(minimum, minimum);

            for (int col = 0; col < tmp.getNumCols(); col++) {
                int colStart = tmp.col_idx[col];
                int colEnd = tmp.col_idx[col + 1];

                for (int idx = colStart; idx < colEnd; idx++) {
                    int row = tmp.nz_rows[idx];
                    double value = tmp.nz_values[idx];

                    if (value > 0) {
                        ans.set(row % minimum, col, 1);
                    }
                }
            }

        }
        return ans;

    }
}
