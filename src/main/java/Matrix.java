
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ejml.data.DMatrixSparseCSC;

public class Matrix {

    static int block_size = 1;
    static int n = 0;

    // enter vertices starting from 0 and do not skip values
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Invalid argument format. You need to specify two files: file path, file grammar, optimizations number.");
            System.exit(1);
        }

        String filePath = args[1];
        String fileGrammar = args[0];
        String optimNumber = args[2];
        String ultimate = args[3];

        Grammar grammar = new Grammar();
        parseGrammarFile(fileGrammar, grammar);

        List<Edge> edges = readEdgesFromFile(filePath, grammar);

        Optimizations optimizations = new Optimizations(optimNumber);

        contextFreePathQuerying(grammar, edges, optimizations, ultimate);
    }

    public static AbstractMatrix contextFreePathQuerying(Grammar grammar, List<Edge> edges, Optimizations optimizations, String ultimate) {
        HashMap<String, AbstractMatrix> labels = makeMatrix(grammar, edges, optimizations);
        HashMap<String, AbstractMatrix> old = makeMatrix(grammar, Arrays.asList(), optimizations);

        AbstractMatrix tmp;
        AsistantMatrix storage = new AsistantMatrix(optimizations.number, n, block_size);

        boolean changed;
        do {
            changed = false;
            for (Grammar.Production a : grammar.getProductions()) {
                if (a.getLHSL() == null) {
                    checkingEpsilonCases(a, labels, optimizations, storage);
                    continue;
                }

                String key1 = a.getLHSL();
                String key2 = a.getLHSR();
                String production = a.getRHS();

                if (optimizations.isOpt1()) {
                    old.get(key1).multiply(labels.get(key2), storage, 0, production);

                    if (!optimizations.isOpt3() && !optimizations.isOpt5()) {
                        for (String key : labels.keySet()) {

                            labels.get(key).add(old.get(key), storage, 1);

                            if (storage.getMatrix(1).nz_length() != old.get(key).nz_length()) {
                                changed = true;
                            }

                            old.put(key, storage.getMatrix(1).copy());
                        }
                    } else {
                        for (String key : labels.keySet()) {
                            old.get(key).add(labels.get(key), storage, 2);
                        }
                    }

                    labels.get(key1).multiply(old.get(key2), storage, 1, production);

                    storage.getMatrix(0).add(storage.getMatrix(1), storage, 1);

                    old.get(production).subtraction(storage.getMatrix(1), storage, 2);

                    tmp = storage.getMatrix(1).removeNonPositiveElements();

                    if ((optimizations.isOpt3() && tmp.nz_length() != 0) || (optimizations.isOpt5() && tmp.nz_length() != 0)) {
                        changed = true;
                    }

                    labels.put(production, tmp.copy());

                } else {
                    AbstractMatrix keyMatrix = labels.get(production);

                    labels.get(key1).multiply(labels.get(key2), storage, 1, production);

                    storage.getMatrix(1).copy().add(keyMatrix, storage, 1);

                    if (keyMatrix.nz_length() != storage.getMatrix(1).nz_length()) {
                        changed = true;
                        labels.put(production, storage.getMatrix(1).copy());
                    }
                }
            }
        } while (changed);

        if (optimizations.isOpt3() || optimizations.isOpt5()) {
            old.get(ultimate).toOne(storage, 1);
            return storage.getMatrix(1);
        }

        if (optimizations.isOpt2() || optimizations.isOpt4()) {
            return old.get(ultimate);
        }

        return labels.get(ultimate);
    }

    private static HashMap<String, AbstractMatrix> makeMatrix(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        HashMap<String, AbstractMatrix> labels = new HashMap<>();
        boolean endsWithI;
        for (String key : grammar.getLetters()) {
            AbstractMatrix matrix;

            if (optimizations.isOpt5()) {
                endsWithI = key.endsWith("_i");
                if (endsWithI) {
                    if (grammar.isLhsR(key)) {
                        if (!edges.equals(Arrays.asList())) {
                            matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n, n * block_size));
                        } else {
                            matrix = new FastMatrixVector(new DMatrixSparseCSC(n, n * block_size));
                        }
                    } else {
                        if (!edges.equals(Arrays.asList())) {
                            matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n * block_size, n));
                        } else {
                            matrix = new FastMatrixVector(new DMatrixSparseCSC(n * block_size, n));
                        }
                    }
                } else {
                    if (!edges.equals(Arrays.asList())) {
                        matrix = new CellBlockMatrix(new DMatrixSparseCSC(n, n));
                    } else {
                        matrix = new FastMatrixCell(new DMatrixSparseCSC(n, n));
                    }
                }
            } else {
                if (optimizations.isOpt4()) {
                    endsWithI = key.endsWith("_i");
                    if (endsWithI) {
                        if (grammar.isLhsR(key)) {
                            matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n, n * block_size));
                        } else {
                            matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n * block_size, n));
                        }
                    } else {
                        matrix = new CellBlockMatrix(new DMatrixSparseCSC(n, n));
                    }
                } else {
                    if (optimizations.isOpt3()) {
                        if (!edges.equals(Arrays.asList())) {
                            matrix = new BaseMatrix(new DMatrixSparseCSC(n, n));
                        } else {
                            matrix = new LazyMatrix(new DMatrixSparseCSC(n, n));
                        }
                    } else {
                        if (optimizations.isOpt2()) {
                            matrix = new FormatMatrix(new DMatrixSparseCSC(n, n));
                        } else {
                            matrix = new BaseMatrix(new DMatrixSparseCSC(n, n));
                        }
                    }
                }
            }

            labels.put(key, matrix);
        }

        for (Edge edge : edges) {
            int from = edge.getStart();
            int to = edge.getFinish();
            String label = edge.getLabel();

            if (from <= n && to <= n) {
                if (edge.hasN()) {
                    if (grammar.isLhsR(label)) {
                        labels.get(label).set(from, to + n * edge.getN(), 1);
                    } else {
                        labels.get(label).set(from + n * edge.getN(), to, 1);
                    }
                } else {
                    labels.get(label).set(from, to, 1);
                }

                String rhs = grammar.getRHSByLHS(label);

                if (rhs != null) {
                    endsWithI = rhs.endsWith("_i");
                    if (edge.hasN() && endsWithI) {
                        if (grammar.isLhsR(label)) {
                            labels.get(rhs).set(from, to + n * edge.getN(), 1);
                        } else {
                            labels.get(rhs).set(from + n * edge.getN(), to, 1);
                        }
                    } else {
                        labels.get(rhs).set(from, to, 1);
                    }
                }
            }
        }

        return labels;
    }

    public static List<Edge> readEdgesFromFile(String filename, Grammar grammar) {
        List<Edge> edges = new ArrayList<>();

        Set<Integer> nums = new HashSet<>();
        int counter = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 3 && parts.length <= 4) {
                    int start = Integer.parseInt(parts[0]);
                    int finish = Integer.parseInt(parts[1]);
                    nums.add(start);
                    nums.add(finish);
                    String label = parts[2];
                    grammar.addLetters(label);
                    Edge edge = new Edge(start, finish, label);
                    if (parts.length == 4) {
                        edge.setN(Integer.parseInt(parts[3]));
                        counter = Math.max(Integer.parseInt(parts[3]), counter);
                    }
                    edges.add(edge);
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        n = nums.size();
        block_size = counter + 1;

        return edges;
    }

    public static void parseGrammarFile(String filePath, Grammar grammar) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().replaceAll("\\s+", " ");
                grammar.addProductionRules(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        grammar.addNewRules();
    }

    public static void checkingEpsilonCases(Grammar.Production a, HashMap<String, AbstractMatrix> labels, Optimizations opt, AsistantMatrix storage) {

        if (a.getLHS() != null) {
            boolean endsWithI = a.getLHS().endsWith("_i");
            boolean endsWithI2 = a.getRHS().endsWith("_i");
            if (endsWithI && !endsWithI2) {
                storage.getMatrix("cell", 1).matrix = BlockHelper.reverse(labels.get(a.getLHS()).matrix);

                for (int col = 0; col < storage.getMatrix(1).getNumCols(); col++) {
                    int colStart = storage.getMatrix(1).col_idx(col);
                    int colEnd = storage.getMatrix(1).col_idx(col + 1);

                    for (int idx = colStart; idx < colEnd; idx++) {
                        int row = storage.getMatrix(1).nz_rows(idx);

                        labels.get(a.getRHS()).set(row, col, 1);
                    }
                }
            } else {
                for (int col = 0; col < labels.get(a.getLHS()).getNumCols(); col++) {
                    int colStart = labels.get(a.getLHS()).col_idx(col);
                    int colEnd = labels.get(a.getLHS()).col_idx(col + 1);

                    for (int idx = colStart; idx < colEnd; idx++) {
                        int row = labels.get(a.getLHS()).nz_rows(idx);

                        labels.get(a.getRHS()).set(row, col, 1);
                    }
                }
            }
        }
    }
}
