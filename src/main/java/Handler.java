
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

import Matrix.AbstractMatrix;
import Matrix.BaseMatrix;
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
                   if (grammar.isLhsR(label) && ((to + n * edge.getN()) < (matrix.getNumCols()))) {
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
                       if (grammar.isLhsR(label) && ((to + n * edge.getN()) < (rhsMatrix.getNumCols()))) {
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
                    if (grammar.isLhsR(key)) {
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
                    if (grammar.isLhsR(key)) {
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

    public static void checkingEpsilonCases(Grammar.Production a, HashMap<String, AbstractMatrix> labels, Optimizations opt) {

        if (a.getLHS() != null) {
            boolean endsWithI = a.getLHS().endsWith("_i");
            boolean endsWithI2 = a.getRHS().endsWith("_i");

            switch (endsWithI ? (endsWithI2 ? 1 : 2) : (endsWithI2 ? 3 : 0)) {
                case 1:
                    if (labels.get(a.getLHS()).getNumCols() > labels.get(a.getRHS()).getNumCols()) {
                        for (int col = 0; col < labels.get(a.getLHS()).getNumCols(); col++) {
                            int colStart = labels.get(a.getLHS()).col_idx(col);
                            int colEnd = labels.get(a.getLHS()).col_idx(col + 1);

                            for (int idx = colStart; idx < colEnd; idx++) {
                                int row = labels.get(a.getLHS()).nz_rows(idx);

                                labels.get(a.getRHS()).set(row + (labels.get(a.getLHS()).getNumCols() / (labels.get(a.getLHS()).getNumCols() / labels.get(a.getLHS()).getNumRows())) * (col / labels.get(a.getLHS()).getNumRows()), col % labels.get(a.getLHS()).getNumRows(), 1);
                            }
                        }
                    } else {
                        if (labels.get(a.getLHS()).getNumCols() < labels.get(a.getRHS()).getNumCols()) {
                            for (int col = 0; col < labels.get(a.getLHS()).getNumCols(); col++) {
                                int colStart = labels.get(a.getLHS()).col_idx(col);
                                int colEnd = labels.get(a.getLHS()).col_idx(col + 1);

                                for (int idx = colStart; idx < colEnd; idx++) {
                                    int row = labels.get(a.getLHS()).nz_rows(idx);

                                    labels.get(a.getRHS()).set(row % labels.get(a.getLHS()).getNumCols(), col + (labels.get(a.getLHS()).getNumRows() / (labels.get(a.getLHS()).getNumRows() / labels.get(a.getLHS()).getNumCols())) * (row / labels.get(a.getLHS()).getNumCols()), 1);
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

                    break;
                case 0:
                    for (int col = 0; col < labels.get(a.getLHS()).getNumCols(); col++) {
                        int colStart = labels.get(a.getLHS()).col_idx(col);
                        int colEnd = labels.get(a.getLHS()).col_idx(col + 1);

                        for (int idx = colStart; idx < colEnd; idx++) {
                            int row = labels.get(a.getLHS()).nz_rows(idx);

                            labels.get(a.getRHS()).set(row, col, 1);
                        }
                    }
                    break;
                case 2:
                    int minimum = Math.min(labels.get(a.getLHS()).getNumCols(), labels.get(a.getLHS()).getNumRows());

                    for (int col = 0; col < labels.get(a.getLHS()).getNumCols(); col++) {
                        int colStart = labels.get(a.getLHS()).col_idx(col);
                        int colEnd = labels.get(a.getLHS()).col_idx(col + 1);

                        for (int idx = colStart; idx < colEnd; idx++) {
                            int row = labels.get(a.getLHS()).nz_rows(idx);

                            labels.get(a.getRHS()).set(row % minimum, col % minimum, 1);
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect grammar. there is no support for the A_i <- a rule");
            }
        }
    }
}
