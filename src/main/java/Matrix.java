
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
                new Edge(2, 3, "b")
        );

        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("AB", "S");

        contextFreePathQuerying(grammar, edges);
    }

    public static HashMap<String, DMatrixSparseCSC> contextFreePathQuerying(Grammar grammar, List<Edge> edges) {
        int n = getNumbersNodes(grammar, edges);

        HashMap<String, DMatrixSparseCSC> labels = makeMatrix(grammar, edges, n);

        boolean changed;
        do {
            changed = false;
            for (String key1 : labels.keySet()) {
                for (String key2 : labels.keySet()) {
                    String combinedKey = key1 + key2;

                    Grammar.Production production = grammar.getProductionRulesByLHS(combinedKey);
                    if (production != null) {

                        DMatrixSparseCSC matrix1 = labels.get(key1);
                        DMatrixSparseCSC matrix2 = labels.get(key2);
                        DMatrixSparseCSC keyMatrix = labels.get(grammar.getRHSByLHS(combinedKey));

                        DMatrixSparseCSC tmp = new DMatrixSparseCSC(n, n);

                        CommonOps_DSCC.mult(matrix1, matrix2, tmp);

                        DMatrixSparseCSC tmp2 = new DMatrixSparseCSC(n, n);

                        CommonOps_DSCC.add(1.0, tmp, 1.0, keyMatrix, tmp2, null, null);

                        if (keyMatrix.nz_length != tmp2.nz_length) {
                            labels.put(grammar.getRHSByLHS(combinedKey), tmp2);
                            changed = true;
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
