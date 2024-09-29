package Matrix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.ejml.data.DMatrixSparseCSC;

public class BlockHelper {

    public static DMatrixSparseCSC revolutionToTheHorizon(DMatrixSparseCSC A) {
        if (isHorizon(A)) {
            return A;
        }
        int block_size = A.numCols;

        int[] tmp_col = new int[A.numRows + 1];
        int[] new_nr = new int[A.nz_length];
        double[] nz_values = new double[A.nz_length];
        Arrays.fill(nz_values, 1.0);
        HashMap <Integer, LinkedList<Integer>> tmp_row = new HashMap<>();
        int k = 0;

        for (int col = 0; col < A.getNumCols(); col++) {
                int colStart = A.col_idx[col];
                int colEnd = A.col_idx[col + 1];

                for (int idx = colStart; idx < colEnd; idx++) {
                    int row = A.nz_rows[idx];

                    tmp_row.computeIfAbsent(col + block_size * (row / block_size), key -> new LinkedList<>()).add(row % block_size); 
                    tmp_col[col + 1 + block_size * (row / block_size)] += 1;
                }
            }
            

        Set<Integer> keys = tmp_row.keySet();

        Set<Integer> sortedKeys = new TreeSet<>(keys);
                

        for (int q : sortedKeys) {
            LinkedList<Integer> list = tmp_row.get(q);
            for (int i = 0; i < list.size(); i++) {
                new_nr[k++] = list.get(i);
            }
        }

        for (int i = 1; i < tmp_col.length; i++) {
            tmp_col[i] += tmp_col[i - 1];
        }

        DMatrixSparseCSC matrix1 = new DMatrixSparseCSC(A.numCols, A.numRows);
        matrix1.nz_rows = new_nr;
        matrix1.col_idx = tmp_col;

        matrix1.nz_values = nz_values;
        matrix1.nz_length = tmp_col[tmp_col.length - 1];

        return matrix1;

    }

    public static boolean isHorizon(DMatrixSparseCSC a) {
        return a.numCols > a.numRows;
    }

    public static DMatrixSparseCSC revolutionToTheVertical(DMatrixSparseCSC A) {
        if (!isHorizon(A)) {
            return A;
        }
        
        int block_size = A.numRows;

        int[] tmp_col = new int[A.numRows + 1];
        int[] new_nr = new int[A.nz_length];
        double[] nz_values = new double[A.nz_length];
        Arrays.fill(nz_values, 1.0);
        HashMap <Integer, LinkedList<Integer>> tmp_row = new HashMap<>();
        int k = 0;

        for (int col = 0; col < A.getNumCols(); col++) {
                int colStart = A.col_idx[col];
                int colEnd = A.col_idx[col + 1];

                for (int idx = colStart; idx < colEnd; idx++) {
                    int row = A.nz_rows[idx];

                    tmp_row.computeIfAbsent(col % block_size, key -> new LinkedList<>()).add(row + block_size * (col / block_size)); 
                    tmp_col[(col % block_size) + 1] += 1;
                }
            }

        Set<Integer> keys = tmp_row.keySet();

        Set<Integer> sortedKeys = new TreeSet<>(keys);
                

        for (int q : sortedKeys) {
            LinkedList<Integer> list = tmp_row.get(q);
            for (int i = 0; i < list.size(); i++) {
                new_nr[k++] = list.get(i);
            }
        }
        

        for (int i = 1; i < tmp_col.length; i++) {
            tmp_col[i] += tmp_col[i - 1];
        } 

        DMatrixSparseCSC matrix1 = new DMatrixSparseCSC(A.numCols, A.numRows);
        matrix1.nz_rows = new_nr;
        matrix1.col_idx = tmp_col;

        matrix1.nz_values = nz_values;
        matrix1.nz_length = tmp_col[tmp_col.length - 1];

        return matrix1;
    }

    public static DMatrixSparseCSC toDiagMatrix(DMatrixSparseCSC A) {
        if (A.numRows > A.numCols) {
            int block_size = A.numCols;

            int[] tmp_col = new int[A.numRows + 1];

            for (int col = 0; col < A.getNumCols(); col++) {
                    int colStart = A.col_idx[col];
                    int colEnd = A.col_idx[col + 1];

                    for (int idx = colStart; idx < colEnd; idx++) {
                        int row = A.nz_rows[idx];

                        tmp_col[col + 1 + block_size * (row / block_size)] += 1;
                    }
                }
            

            for (int i = 1; i < tmp_col.length; i++) {
                tmp_col[i] += tmp_col[i - 1];
            } 

            DMatrixSparseCSC matrix1 = new DMatrixSparseCSC(A.numRows, A.numRows);
            matrix1.nz_rows = A.nz_rows;
            matrix1.col_idx = tmp_col;

            matrix1.nz_values = A.nz_values;
            matrix1.nz_length = tmp_col[tmp_col.length - 1];

            return matrix1;
        } else {
            int block_size = A.numRows;

            int[] new_nr = new int[A.nz_length];
            HashMap <Integer, LinkedList<Integer>> tmp_row = new HashMap<>();
            int k = 0;

            for (int col = 0; col < A.getNumCols(); col++) {
                    int colStart = A.col_idx[col];
                    int colEnd = A.col_idx[col + 1];

                    for (int idx = colStart; idx < colEnd; idx++) {
                        int row = A.nz_rows[idx];

                        tmp_row.computeIfAbsent(col, key -> new LinkedList<>()).add(row + block_size * (col / block_size)); 
                    }
                }
            
            Set<Integer> keys = tmp_row.keySet();

            Set<Integer> sortedKeys = new TreeSet<>(keys);
                

            for (int q : sortedKeys) {
                LinkedList<Integer> list = tmp_row.get(q);
                for (int i = 0; i < list.size(); i++) {
                    new_nr[k++] = list.get(i);
                }
            }

            DMatrixSparseCSC matrix1 = new DMatrixSparseCSC(A.numCols, A.numCols);
            matrix1.nz_rows = new_nr;
            matrix1.col_idx = A.col_idx;

            matrix1.nz_values = A.nz_values;
            matrix1.nz_length = A.col_idx[A.col_idx.length - 1];
            return matrix1;
        }
    }

    public static DMatrixSparseCSC reverse(DMatrixSparseCSC A) {
        if (A.numCols > A.numRows) {
            int block_size = A.numRows;

            int[] tmp_col = new int[A.numRows + 1];
            int[] new_nr = new int[A.nz_length];
            HashMap <Integer, LinkedList<Integer>> tmp_row = new HashMap<>();
            int k = 0;

            for (int col = 0; col < A.getNumCols(); col++) {
                    int colStart = A.col_idx[col];
                    int colEnd = A.col_idx[col + 1];

                    for (int idx = colStart; idx < colEnd; idx++) {
                        int row = A.nz_rows[idx];

                        tmp_row.computeIfAbsent(col % block_size, key -> new LinkedList<>()).add(row % block_size); 
                        tmp_col[(col % block_size) + 1] += 1;
                    }
                }

            Set<Integer> keys = tmp_row.keySet();

            Set<Integer> sortedKeys = new TreeSet<>(keys);
                    

            for (int q : sortedKeys) {
                LinkedList<Integer> list = tmp_row.get(q);
                for (int i = 0; i < list.size(); i++) {
                    new_nr[k++] = list.get(i);
                }
            }
            

            for (int i = 1; i < tmp_col.length; i++) {
                tmp_col[i] += tmp_col[i - 1];
            } 

            double[] nz_values = new double[tmp_col[tmp_col.length - 1]];
            Arrays.fill(nz_values, 1.0);

            DMatrixSparseCSC matrix1 = new DMatrixSparseCSC(block_size, block_size);
            matrix1.nz_rows = new_nr;
            matrix1.col_idx = tmp_col;

            matrix1.nz_values = nz_values;
            matrix1.nz_length = tmp_col[tmp_col.length - 1];

            return matrix1;
        } else {
            int block_size = A.numCols;

            int[] tmp_col = new int[A.numCols + 1];
            int[] new_nr = new int[A.nz_length];
            HashMap <Integer, LinkedList<Integer>> tmp_row = new HashMap<>();
            int k = 0;

            for (int col = 0; col < A.getNumCols(); col++) {
                    int colStart = A.col_idx[col];
                    int colEnd = A.col_idx[col + 1];

                    for (int idx = colStart; idx < colEnd; idx++) {
                        int row = A.nz_rows[idx];

                        tmp_row.computeIfAbsent(col % block_size, key -> new LinkedList<>()).add(row % block_size); 
                        tmp_col[(col % block_size) + 1] += 1;
                    }
                }
                

            Set<Integer> keys = tmp_row.keySet();

            Set<Integer> sortedKeys = new TreeSet<>(keys);
                    

            for (int q : sortedKeys) {
                LinkedList<Integer> list = tmp_row.get(q);
                for (int i = 0; i < list.size(); i++) {
                    new_nr[k++] = list.get(i);
                }
            }

            for (int i = 1; i < tmp_col.length; i++) {
                tmp_col[i] += tmp_col[i - 1];
            }

            DMatrixSparseCSC matrix1 = new DMatrixSparseCSC(block_size, block_size);
            matrix1.nz_rows = new_nr;
            matrix1.col_idx = tmp_col;

            double[] nz_values = new double[tmp_col[tmp_col.length - 1]];
            Arrays.fill(nz_values, 1.0);

            matrix1.nz_values = nz_values;
            matrix1.nz_length = tmp_col[tmp_col.length - 1];

            return matrix1;
        }
    }

    public static CellBlockMatrix reverseVectorBlockMatrix(AbstractMatrix A) {
        if (A.matrix.numCols > A.matrix.numRows) {
            int block_size = A.matrix.numRows;

            int[] tmp_col = new int[A.matrix.numRows + 1];
            int[] new_nr = new int[A.matrix.nz_length];
            HashMap <Integer, LinkedList<Integer>> tmp_row = new HashMap<>();
            int k = 0;

            for (int col = 0; col < A.getNumCols(); col++) {
                    int colStart = A.matrix.col_idx[col];
                    int colEnd = A.matrix.col_idx[col + 1];

                    for (int idx = colStart; idx < colEnd; idx++) {
                        int row = A.matrix.nz_rows[idx];

                        tmp_row.computeIfAbsent(col % block_size, key -> new LinkedList<>()).add(row % block_size); 

                        tmp_col[(col % block_size) + 1] += 1;
                    }
                }

            Set<Integer> keys = tmp_row.keySet();

            Set<Integer> sortedKeys = new TreeSet<>(keys);
                    

            for (int q : sortedKeys) {
                LinkedList<Integer> list = tmp_row.get(q);
                for (int i = 0; i < list.size(); i++) {
                    new_nr[k++] = list.get(i);
                }
            }
            

            for (int i = 1; i < tmp_col.length; i++) {
                tmp_col[i] += tmp_col[i - 1];
            }

            double[] nz_values = new double[tmp_col[tmp_col.length - 1]];
            Arrays.fill(nz_values, 1.0);


            CellBlockMatrix matrix1 = new CellBlockMatrix(new DMatrixSparseCSC(block_size, block_size));
            matrix1.matrix.nz_rows = new_nr;
            matrix1.matrix.col_idx = tmp_col;

            matrix1.matrix.nz_values = nz_values;

            matrix1.matrix.nz_length = tmp_col[tmp_col.length - 1];

            return matrix1;
        } else {
            int block_size = A.matrix.numCols;

            int[] tmp_col = new int[A.matrix.numCols + 1];
            int[] new_nr = new int[A.matrix.nz_length];
            HashMap <Integer, LinkedList<Integer>> tmp_row = new HashMap<>();
            int k = 0;

            for (int col = 0; col < A.getNumCols(); col++) {
                    int colStart = A.matrix.col_idx[col];
                    int colEnd = A.matrix.col_idx[col + 1];

                    for (int idx = colStart; idx < colEnd; idx++) {
                        int row = A.matrix.nz_rows[idx];

                        tmp_row.computeIfAbsent(col % block_size, key -> new LinkedList<>()).add(row % block_size); 

                        tmp_col[(col % block_size) + 1] += 1;
                        
                    }
                }
                

            Set<Integer> keys = tmp_row.keySet();

            Set<Integer> sortedKeys = new TreeSet<>(keys);
                    

            for (int q : sortedKeys) {
                LinkedList<Integer> list = tmp_row.get(q);
                for (int i = 0; i < list.size(); i++) {
                    new_nr[k++] = list.get(i);
                }
            }

            for (int i = 1; i < tmp_col.length; i++) {
                tmp_col[i] += tmp_col[i - 1];
            }

            CellBlockMatrix matrix1 = new CellBlockMatrix(new DMatrixSparseCSC(block_size, block_size));
            matrix1.matrix.nz_rows = new_nr;
            matrix1.matrix.col_idx = tmp_col;

            double[] nz_values = new double[tmp_col[tmp_col.length - 1]];
            Arrays.fill(nz_values, 1.0);

            matrix1.matrix.nz_values = nz_values;

            matrix1.matrix.nz_length = tmp_col[tmp_col.length - 1];

            return matrix1;
        }
    }
}
