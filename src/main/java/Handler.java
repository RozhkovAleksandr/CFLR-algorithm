
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

import Matrix.AbstractMatrix;
import Matrix.BaseMatrix;
import Matrix.BlockHelper;
import Matrix.CellBlockMatrix;
import Matrix.FastMatrixCell;
import Matrix.FastMatrixVector;
import Matrix.FormatMatrix;
import Matrix.LazyMatrix;
import Matrix.VectorBlockMatrix;

public class Handler {

    static int block_size = 1;
    static int n = 0;

    public static HashMap<String, AbstractMatrix> makeMatrix(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        HashMap<String, AbstractMatrix> labels = createMatrixForKey(grammar, edges, optimizations);

        edges.parallelStream().forEach(edge -> processEdge(edge, labels, grammar));

        return labels;
    }

    private static void processEdge(Edge edge, HashMap<String, AbstractMatrix> labels, Grammar grammar) {
        boolean endsWithI;

        int from = edge.getStart();
        int to = edge.getFinish();
        String label = edge.getLabel();

        if (from <= n && to <= n) {
            AbstractMatrix matrix = labels.get(label);
            synchronized (matrix) {
                if (edge.hasN()) {
                    if (((to + n * edge.getN()) < (matrix.getNumCols()))) {
                        matrix.set(from, to + n * edge.getN(), 1);
                    } else {
                        matrix.set(from + n * edge.getN(), to, 1);
                    }
                } else {
                    matrix.set(from, to, 1);
                }
            }

            String rhs = grammar.getRHSByLHS(label);

            if (rhs != null) {
                AbstractMatrix rhsMatrix = labels.get(rhs);
                synchronized (rhsMatrix) {
                    endsWithI = rhs.endsWith("_i");
                    if (edge.hasN() && endsWithI) {
                        if (((to + n * edge.getN()) < (rhsMatrix.getNumCols()))) {
                            rhsMatrix.set(from, to + n * edge.getN(), 1);
                        } else {
                            rhsMatrix.set(from + n * edge.getN(), to, 1);
                        }
                    } else {
                        rhsMatrix.set(from, to, 1);
                    }
                }
            }
        }
    }

    private static HashMap<String, AbstractMatrix> createMatrixForKey(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        boolean endsWithI;
        HashMap<String, AbstractMatrix> labels = new HashMap<>();
        for (String key : grammar.getLetters()) {
            AbstractMatrix matrix;

            if (optimizations.isOpt5()) {
                endsWithI = key.endsWith("_i");
                if (endsWithI) {
                    if (!grammar.isVertical(key)) {
                        matrix = !edges.equals(Arrays.asList())
                                ? new VectorBlockMatrix(new DMatrixSparseCSC(n, n * block_size))
                                : new FastMatrixVector(new DMatrixSparseCSC(n, n * block_size));
                    } else {
                        matrix = !edges.equals(Arrays.asList())
                                ? new VectorBlockMatrix(new DMatrixSparseCSC(n * block_size, n))
                                : new FastMatrixVector(new DMatrixSparseCSC(n * block_size, n));
                    }
                } else {
                    matrix = !edges.equals(Arrays.asList())
                            ? new CellBlockMatrix(new DMatrixSparseCSC(n, n))
                            : new FastMatrixCell(new DMatrixSparseCSC(n, n));
                }
            } else if (optimizations.isOpt4()) {
                endsWithI = key.endsWith("_i");
                if (endsWithI) {
                    if (!grammar.isVertical(key)) {
                        matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n, n * block_size));
                    } else {
                        matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n * block_size, n));
                    }
                } else {
                    matrix = new CellBlockMatrix(new DMatrixSparseCSC(n, n));
                }
            } else if (optimizations.isOpt3()) {
                matrix = !edges.equals(Arrays.asList())
                        ? new BaseMatrix(new DMatrixSparseCSC(n, n))
                        : new LazyMatrix(new DMatrixSparseCSC(n, n));
            } else if (optimizations.isOpt2()) {
                matrix = new FormatMatrix(new DMatrixSparseCSC(n, n));
            } else {
                matrix = new BaseMatrix(new DMatrixSparseCSC(n, n));
            }

            labels.put(key, matrix);
        }

        return labels;
    }

    public static List<Edge> readEdgesFromFile(String filename, Grammar grammar) {
        List<Edge> edges = new ArrayList<>();

        int maxN = -1;
        int maxAB;
        int counter = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 3 && parts.length <= 4) {
                    int start = Integer.parseInt(parts[0]);
                    int finish = Integer.parseInt(parts[1]);
                    maxAB = Math.max(start, finish);
                    maxN = Math.max(maxAB, maxN);
                    String label = parts[2];
                    grammar.addLetters(label);
                    grammar.addNonTerminal(label);
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

        n = maxN + 1;
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

    public static void checkingEpsilonCases(Grammar.Production a, HashMap<String, AbstractMatrix> labels, Grammar grammar) {

        if (a.getLHS() != null && !grammar.isNonTerminal(a.getLHS())) {
            boolean endsWithI = a.getLHS().endsWith("_i");
            boolean endsWithI2 = a.getRHS().endsWith("_i");

            switch (endsWithI ? (endsWithI2 ? 1 : 2) : (endsWithI2 ? 3 : 0)) {
                case 1:
                    if (labels.get(a.getLHS()).getNumCols() > labels.get(a.getRHS()).getNumCols()) {
                        CommonOps_DSCC.add(1.0, labels.get(a.getRHS()).copy().matrix, 1.0, BlockHelper.revolutionToTheVertical(labels.get(a.getLHS()).matrix), labels.get(a.getRHS()).matrix, null, null);
                    } else {
                        if (labels.get(a.getLHS()).getNumCols() < labels.get(a.getRHS()).getNumCols()) {
                            CommonOps_DSCC.add(1.0, labels.get(a.getRHS()).copy().matrix, 1.0, BlockHelper.revolutionToTheHorizon(labels.get(a.getLHS()).matrix), labels.get(a.getRHS()).matrix, null, null);
                        } else {
                            CommonOps_DSCC.add(1.0, labels.get(a.getRHS()).copy().matrix, 1.0, labels.get(a.getLHS()).matrix, labels.get(a.getRHS()).matrix, null, null);
                        }
                    }
                    break;
                case 0:
                    CommonOps_DSCC.add(1.0, labels.get(a.getRHS()).copy().matrix, 1.0, labels.get(a.getLHS()).matrix, labels.get(a.getRHS()).matrix, null, null);
                    break;
                case 2:
                    CommonOps_DSCC.add(1.0, labels.get(a.getRHS()).copy().matrix, 1.0,BlockHelper.reverse(labels.get(a.getLHS()).matrix), labels.get(a.getRHS()).matrix, null, null);
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect grammar. there is no support for the A_i <- a rule");
            }
        }
    }
}
