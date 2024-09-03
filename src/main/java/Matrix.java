
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ejml.data.DMatrixSparseCSC;

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

        Optimizations optimizations = new Optimizations(true, true, false, false, false);

        contextFreePathQuerying(grammar, edges, optimizations);
    }

    public static HashMap<String, AbstractMatrix> contextFreePathQuerying(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        int n = getNumbersNodes(grammar, edges);

        HashMap<String, AbstractMatrix> labels = makeMatrix(grammar, edges, n, optimizations);
        HashMap<String, AbstractMatrix> old = makeMatrix(grammar, Arrays.asList(), n, optimizations);

        AbstractMatrix tmp1;
        AbstractMatrix tmp2;
        // delete tmp3
        AbstractMatrix tmp3;

        if (optimizations.isOpt2()) {
            tmp1 = new FormatMatrix(new DMatrixSparseCSC(n, n));
            tmp2 = new FormatMatrix(new DMatrixSparseCSC(n, n));
            // delete tmp3
            tmp3 = new FormatMatrix(new DMatrixSparseCSC(n, n));
        } else {
            tmp1 = new BaseMatrix(new DMatrixSparseCSC(n, n));
            tmp2 = new BaseMatrix(new DMatrixSparseCSC(n, n));
            // delete tmp3
            tmp3 = new BaseMatrix(new DMatrixSparseCSC(n, n));
        }

        boolean changed;
        do {
            changed = false;
            for (String key1 : labels.keySet()) {
                for (String key2 : labels.keySet()) {
                    String combinedKey = key1 + key2;
                    Grammar.Production production = grammar.getProductionRulesByLHS(combinedKey);
                    if (production != null) {

                        if (optimizations.isOpt1()) {
                            old.get(key1).multiply(labels.get(key2), tmp1);

                            if (!optimizations.isOpt3()) {
                                for (String key : labels.keySet()) {
                                    tmp3 = old.get(key).copy();

                                    labels.get(key).add(old.get(key), tmp2);

                                    old.put(key, tmp2.copy());

                                    if (tmp3.nz_length() != old.get(key).nz_length()) {
                                        changed = true;
                                    }
                                }
                            } else {
                                for (String key : labels.keySet()) {
                                    old.get(key).add(labels.get(key), tmp3);

                                    old.get(key).toOne(tmp3);
                                }
                            }

                            labels.get(key1).multiply(old.get(key2), tmp2);

                            tmp1.add(tmp2.copy(), tmp2);

                            old.get(grammar.getRHSByLHS(combinedKey)).subtraction(tmp2, tmp3);

                            tmp2 = tmp2.removeNonPositiveElements();

                            if (optimizations.isOpt3() && tmp2.nz_length() != 0) {
                                changed = true;
                            }


                            labels.put(grammar.getRHSByLHS(combinedKey), tmp2.copy());

                        } else {
                            AbstractMatrix keyMatrix = labels.get(grammar.getRHSByLHS(combinedKey));

                            labels.get(key1).multiply(labels.get(key2), tmp2);

                            tmp2.copy().add(keyMatrix, tmp2);

                            if (keyMatrix.nz_length() != tmp2.nz_length()) {
                                changed = true;
                                labels.put(grammar.getRHSByLHS(combinedKey), tmp2.copy());
                            }
                        }
                    }
                }
            }
        } while (changed);

        HashMap<String, AbstractMatrix> current;

        if (optimizations.isOpt1()) {
            current = old;
        } else {
            current = labels;
        }

        for (HashMap.Entry<String, AbstractMatrix> entry : current.entrySet()) {
            System.out.println("Matrix " + entry.getKey() + ":");
            if (optimizations.isOpt3()) {
                entry.getValue().toOne(tmp1);
                tmp1.print();
            }
            else {
                entry.getValue().print();
            }
        }

        return current;
    }

    private static HashMap<String, AbstractMatrix> makeMatrix(Grammar grammar, List<Edge> edges, int n, Optimizations optimizations) {
        HashMap<String, AbstractMatrix> labels = new HashMap<>();

        for (String key : grammar.getLetters()) {
            AbstractMatrix matrix;
            if (optimizations.isOpt3()) {
                if (!edges.equals(Arrays.asList())) {
                    matrix = new BaseMatrix(new DMatrixSparseCSC(n, n));
                }
                else {
                    matrix = new LazyMatrix(new DMatrixSparseCSC(n, n));
                }
            }
            else {
                if (optimizations.isOpt2()) {
                    matrix = new FormatMatrix(new DMatrixSparseCSC(n, n));
                } else {
                    matrix = new BaseMatrix(new DMatrixSparseCSC(n, n));
                }
            }
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
