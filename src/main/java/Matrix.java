
import java.util.ArrayList;
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
                new Edge(1, 3, "a"),
                new Edge(2, 4, "b"),
                new Edge(3, 4, "b"),
                new Edge(4, 5, "c")
        );

        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("c", "C");
        grammar.addProductionRules("AB", "S");
        grammar.addProductionRules("BC", "S");

        Optimizations optimizations = new Optimizations(false, false, false, false, false);

        contextFreePathQuerying(grammar, edges, optimizations);
    }

    public static HashMap<String, DMatrixSparseCSC> contextFreePathQuerying(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        int n = getNumbersNodes(grammar, edges);

        List<Edge> edges1 = Arrays.asList();

        HashMap<String, DMatrixSparseCSC> labels = makeMatrix(grammar, edges, n);
        HashMap<String, DMatrixSparseCSC> old = makeMatrix(grammar, edges1, n);

        DMatrixSparseCSC tmp1 = new DMatrixSparseCSC(n, n);
        DMatrixSparseCSC tmp2 = new DMatrixSparseCSC(n, n);

        boolean changed;
        do {
            changed = false;
            for (String key1 : labels.keySet()) {
                for (String key2 : labels.keySet()) {
                    String combinedKey = key1 + key2;
                    Grammar.Production production = grammar.getProductionRulesByLHS(combinedKey);
                    if (production != null) {

                        boolean res;

                        if (optimizations.isOpt1()) {
                            DMatrixSparseCSC matrix1 = labels.get(key1);
                            DMatrixSparseCSC matrix2 = labels.get(key2);
                            DMatrixSparseCSC old2 = old.get(key1);

                            CommonOps_DSCC.changeSign(old.get(key2), tmp1);

                            CommonOps_DSCC.add(1.0, matrix2, 1.0, tmp1.copy(), tmp1, null, null);

                            if (optimizations.isOpt2()) {
                                tmp1 = multColumnByColumn(old2, tmp1.copy());
                            } else {
                                CommonOps_DSCC.mult(old2, tmp1.copy(), tmp1);
                            }

                            CommonOps_DSCC.changeSign(old2, tmp2);
                            CommonOps_DSCC.add(1.0, matrix1, 1.0, tmp2.copy(), tmp2, null, null);

                            if (optimizations.isOpt2()) {
                                tmp2 = multRowByRow(tmp2.copy(), matrix2);
                            } else {
                                CommonOps_DSCC.mult(tmp2.copy(), matrix2, tmp2);
                            }

                            CommonOps_DSCC.add(1.0, tmp1, 1.0, tmp2, tmp2, null, null);

                            CommonOps_DSCC.removeZeros(tmp2, 0);
                            CommonOps_DSCC.removeZeros(old.get(grammar.getRHSByLHS(combinedKey)), 0);

                            res = tmp2.nz_length > old.get(grammar.getRHSByLHS(combinedKey)).nz_length;

                        } else {
                            DMatrixSparseCSC matrix1 = labels.get(key1);
                            DMatrixSparseCSC matrix2 = labels.get(key2);
                            DMatrixSparseCSC keyMatrix = labels.get(grammar.getRHSByLHS(combinedKey));

                            CommonOps_DSCC.mult(matrix1, matrix2, tmp2);

                            CommonOps_DSCC.add(1.0, tmp2.copy(), 1.0, keyMatrix, tmp2, null, null);

                            res = keyMatrix.getNonZeroLength() != tmp2.getNonZeroLength();
                        }

                        if (res) {
                            labels.put(grammar.getRHSByLHS(combinedKey), tmp2.copy());
                            changed = true;

                            for (String key : labels.keySet()) {
                                old.put(key, labels.get(key).copy());
                            }
                        }
                    }
                }
            }
        } while (changed);

        for (HashMap.Entry<String, DMatrixSparseCSC> entry : labels.entrySet()) {
            System.out.println("Matrix " + entry.getKey() + ":");
            entry.getValue().print();

        }

        return labels;
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
                            result.set(k, j, 1);
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
