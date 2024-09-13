
import java.util.HashMap;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

class FormatMatrix extends AbstractMatrix {

    public FormatMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AbstractMatrix result) {
        if (other instanceof LazyMatrix) {
            ((LazyMatrix) other).multiplyOther(matrix, result);
        } else {
            if (this.matrix.nz_length >= other.matrix.nz_length) {
                result.matrix = multColumnByColumn(this.matrix, other.matrix);
            } else {
                result.matrix = multRowByRow(this.matrix, other.matrix);
            }
        }

    }

    @Override
    public void add(AbstractMatrix other, AbstractMatrix result) {
        CommonOps_DSCC.add(1.0, this.matrix, 1.0, other.matrix, result.matrix, null, null);
    }

    @Override
    public AbstractMatrix copy() {
        FormatMatrix copy = new FormatMatrix(new DMatrixSparseCSC(matrix.getNumCols(), matrix.getNumCols()));
        copy.matrix = this.matrix.copy();
        return copy;
    }

    @Override
    public void subtraction(AbstractMatrix other, AbstractMatrix tmp) {
        CommonOps_DSCC.changeSign(this.matrix, tmp.matrix);
        other.copy().add(tmp, other);
    }

    @Override
    public AbstractMatrix removeNonPositiveElements() {
        AbstractMatrix positiveMatrix = new FormatMatrix(new DMatrixSparseCSC(matrix.getNumCols(), matrix.getNumCols()));
        DMatrixSparseCSC tmp = this.matrix.copy();

        for (int col = 0; col < tmp.getNumCols(); col++) {
            int colStart = tmp.col_idx[col];
            int colEnd = tmp.col_idx[col + 1];

            for (int idx = colStart; idx < colEnd; idx++) {
                int row = tmp.nz_rows[idx];
                double value = tmp.nz_values[idx];

                if (value > 0) {
                    positiveMatrix.set(row, col, 1);
                }
            }
        }

        return positiveMatrix;
    }

    private static DMatrixSparseCSC multRowByRow(DMatrixSparseCSC A, DMatrixSparseCSC B) {
        DMatrixSparseCSC result = new DMatrixSparseCSC(A.numRows, B.numCols);

        HashMap<Integer, Integer> freq = transform(A.nz_rows);
        int counter;

        for (int tmp : freq.keySet()) {
            counter = 0;
            for (int k = 0; k < A.numCols; k++) {
                if (A.get(tmp, k) == 1) {
                    counter++;
                    for (int j = 0; j < B.numCols; j++) {
                        if (B.get(k, j) == 1) {
                            result.set(tmp, j, 1);
                        }
                    }

                    if (counter == freq.get(tmp)) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    private static DMatrixSparseCSC multColumnByColumn(DMatrixSparseCSC A, DMatrixSparseCSC B) {
        DMatrixSparseCSC result = new DMatrixSparseCSC(A.numCols, A.numRows);

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

    private static HashMap<Integer, Integer> transform(int[] arr) {
        HashMap<Integer, Integer> freqHashMap = new HashMap<>();

        for (int num : arr) {
            freqHashMap.put(num, freqHashMap.getOrDefault(num, 0) + 1);
        }

        return freqHashMap;
    }
}