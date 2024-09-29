package Matrix;

import java.util.Arrays;
import java.util.HashMap;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class FormatMatrix extends AbstractMatrix {

    public FormatMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        if (other instanceof LazyMatrix) {
            ((LazyMatrix) other).multiplyOther(matrix, asistant.getMatrix(n));
        } else if (other != null) {
            if (this.matrix.nz_length >= other.matrix.nz_length) {
                multColumnByColumn(this.matrix, other.matrix, asistant.getMatrix(n).matrix);
            } else {
                multRowByRow(this.matrix, other.matrix, asistant.getMatrix(n).matrix);
            }
        }
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        CommonOps_DSCC.add(1.0, this.matrix, 1.0, other.matrix, asistant.getMatrix(n).matrix, null, null);
    }

    @Override
    public AbstractMatrix copy() {
        FormatMatrix copy = new FormatMatrix(new DMatrixSparseCSC(matrix.getNumCols(), matrix.getNumCols()));
        copy.matrix = this.matrix.copy();
        return copy;
    }

    @Override
    public void subtraction(AbstractMatrix other, AsistantMatrix asistant, int n) {
        CommonOps_DSCC.changeSign(this.matrix, asistant.getMatrix(n).matrix);
        CommonOps_DSCC.add(1.0, other.copy().matrix, 1.0, asistant.getMatrix(n).matrix, other.matrix, null, null);
    }

    @Override
    public AbstractMatrix removeNonPositiveElements() {
        AbstractMatrix positiveMatrix;
        positiveMatrix = new FormatMatrix(this.matrix.copy());

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

    private static DMatrixSparseCSC multRowByRow(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC result) {

        HashMap<Integer, Integer> freq = transform(A.nz_rows);
        int counter;

        for (int tmp : freq.keySet()) {
            counter = 0;
            for (int k = 0; k < A.numCols; k++) {
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

        return result;
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
