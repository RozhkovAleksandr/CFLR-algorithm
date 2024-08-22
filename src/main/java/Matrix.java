
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class Matrix {

    public static void main(String[] args) {
        List<Edge> edges = Arrays.asList(
                new Edge(1, 2, "a"),
                new Edge(2, 3, "b"),
                new Edge(3, 2, "a"),
                new Edge(3, 4, "c")
        );

        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("c", "C");
        grammar.addProductionRules("AB", "S");

        Optimizations optimizations = new Optimizations(false, false, false, false, false);

        contextFreePathQuerying(grammar, edges, optimizations);
    }

    public static HashMap<String, DMatrixSparseCSC> contextFreePathQuerying(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        int n = getNumbersNodes(grammar, edges);

        HashMap<String, DMatrixSparseCSC> labels = makeMatrix(grammar, edges, n);
        HashMap<String, DMatrixSparseCSC> old = makeMatrix(grammar, Arrays.asList(), n);

        DMatrixSparseCSC tmp1 = new DMatrixSparseCSC(n, n);
        DMatrixSparseCSC tmp2 = new DMatrixSparseCSC(n, n);
        // delete tmp3
        DMatrixSparseCSC tmp3 = new DMatrixSparseCSC(n, n);

        int schitat = 0;

        boolean changed;
        do {
            changed = false;
            for (String key1 : labels.keySet()) {
                for (String key2 : labels.keySet()) {
                    String combinedKey = key1 + key2;
                    Grammar.Production production = grammar.getProductionRulesByLHS(combinedKey);
                    if (production != null) {

                        if (optimizations.isOpt1()) {
                            DMatrixSparseCSC old2 = old.get(key1);

                            System.out.println(++schitat);
        
                            DMatrixSparseCSC matrix1 = labels.get(key1);
                            DMatrixSparseCSC matrix2 = labels.get(key2);

                            if (optimizations.isOpt2()) {
                                tmp1 = multColumnByColumn(old2, matrix2);
                            }
                            else {
                                CommonOps_DSCC.mult(old2, matrix2, tmp1);
                            }

                            for (String key : labels.keySet()) {
                                tmp3 = old.get(key).copy();
                                
                                CommonOps_DSCC.add(1.0, labels.get(key), 1.0, old.get(key), tmp2, null, null);
                                
                                old.put(key, tmp2.copy());

                                if (tmp3.nz_length != old.get(key).nz_length) {
                                    changed = true;
                                }

                            }

                            if (optimizations.isOpt2()) {
                                tmp2 = multRowByRow(matrix1, old.get(key2));
                            }
                            else {
                                CommonOps_DSCC.mult(matrix1, old.get(key2), tmp2);
                            }

                            CommonOps_DSCC.add(1.0, tmp1, 1.0, tmp2.copy(), tmp2, null, null);

                            CommonOps_DSCC.changeSign(old.get(grammar.getRHSByLHS(combinedKey)), tmp3);

                            CommonOps_DSCC.add(1.0, tmp2.copy(), 1.0, tmp3, tmp2, null, null);

                            tmp2 = removeNonPositiveElements(tmp2.copy());

                            labels.put(grammar.getRHSByLHS(combinedKey), tmp2.copy());

                        } else {
                            DMatrixSparseCSC matrix1 = labels.get(key1);
                            DMatrixSparseCSC matrix2 = labels.get(key2);
                            DMatrixSparseCSC keyMatrix = labels.get(grammar.getRHSByLHS(combinedKey));

                            CommonOps_DSCC.mult(matrix1, matrix2, tmp2);

                            CommonOps_DSCC.add(1.0, tmp2.copy(), 1.0, keyMatrix, tmp2, null, null);

                            if (keyMatrix.getNonZeroLength() != tmp2.getNonZeroLength()) {
                                changed = true;
                                labels.put(grammar.getRHSByLHS(combinedKey), tmp2.copy());
                            }                      
                        }
                    }
                }
            }
        } while (changed);

        HashMap<String, DMatrixSparseCSC> current;

        if (optimizations.isOpt1()) {
            current = old;
        }
        else {
            current = labels;
        }

        for (HashMap.Entry<String, DMatrixSparseCSC> entry : current.entrySet()) {
            System.out.println("Matrix " + entry.getKey() + ":");
            entry.getValue().print();
        }

        return current; 
    }

    private static DMatrixSparseCSC removeNonPositiveElements(DMatrixSparseCSC matrix) {
        DMatrixSparseCSC positiveMatrix = new DMatrixSparseCSC(matrix.numRows, matrix.numCols);

        for (int col = 0; col < matrix.numCols; col++) {
            int colStart = matrix.col_idx[col];
            int colEnd = matrix.col_idx[col + 1];

            for (int idx = colStart; idx < colEnd; idx++) {
                int row = matrix.nz_rows[idx];
                double value = matrix.nz_values[idx];

                if (value > 0) {
                    positiveMatrix.set(row, col, 1);
                }
            }
        }

        return positiveMatrix;
    }

    // TODO ПОБЫСТРЕЕ
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

    private static HashMap<String, DMatrixSparseCSC> makeMatrix(Grammar grammar, List<Edge> edges, int n) {
        HashMap<String, DMatrixSparseCSC> labels = new HashMap<>();

        for (String key : grammar.getLetters()) {
            DMatrixSparseCSC matrix = new DMatrixSparseCSC(n, n);
            labels.put(key, matrix);
        }

        for (Edge edge : edges) {
            int from = edge.getStart();
            int to = edge.getFinish();
            String label = edge.getLabel();
            if (from <= n && to <= n) {
                labels.get(label).set(from - 1, to - 1, 1);

                String rhs = grammar.getRHSByLHS(label);
                if (rhs != null) {
                    labels.get(rhs).set(from - 1, to - 1, 1);
                }
            }
        }

        return labels;
    }

    private static int getNumbersNodes(Grammar grammar, List<Edge> edges) {
        for (Edge edge : edges) {
            grammar.addLetters(edge.getLabel());
        }

        Set<Integer> nums = new HashSet<>();
        for (Edge edge : edges) {
            nums.add(edge.getStart());
            nums.add(edge.getFinish());
        }

        return nums.size();
    }
}
