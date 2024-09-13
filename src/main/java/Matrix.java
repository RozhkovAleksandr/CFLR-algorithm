
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

    static int block_size = 0;
    static int n = 0;

    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        grammar.addProductionRules("A a");
        grammar.addProductionRules("B b");
        grammar.addProductionRules("S A B");

        String filename = "C:\\Users\\Conff\\vscode\\JavaEducation\\CFLR-algorithm\\src\\main\\java\\filename.txt";
        List<Edge> edges = readEdgesFromFile(filename, grammar);


        Optimizations optimizations = new Optimizations(false, false, false, false, false);

        contextFreePathQuerying(grammar, edges, optimizations);
    }

    public static HashMap<String, AbstractMatrix> contextFreePathQuerying(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        HashMap<String, AbstractMatrix> labels = makeMatrix(grammar, edges, optimizations);
        HashMap<String, AbstractMatrix> old = makeMatrix(grammar, Arrays.asList(), optimizations);

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
            // OPTIMIZATION 444444444444444444444444444444
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
                    String production = grammar.getRHSByLHS(combinedKey);
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
            if (optimizations.isOpt3() && !optimizations.isOpt4()) {
                entry.getValue().toOne(tmp1);
                tmp1.print();
            } else {
                entry.getValue().print();
            }
        }

        return current;
    }

    private static HashMap<String, AbstractMatrix> makeMatrix(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        HashMap<String, AbstractMatrix> labels = new HashMap<>();
        for (String key : grammar.getLetters()) {
            AbstractMatrix matrix;
            if (optimizations.isOpt4()) {
                if (grammar.getindexedLetters(key)) {
                    // заюзай функцию isindexwed и надо еще написать прием в виде файлика
                    // blocksize
                    matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n, n));
                }
                else {
                    matrix = new CellBlockMatrix(new DMatrixSparseCSC(n, n));
                }
            } else {
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

    private static List<Edge> readEdgesFromFile(String filename, Grammar grammar) {
        List<Edge> edges = new ArrayList<>();

        // для поиска размеров будущих блоков
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
                        grammar.addindexedLetters(label);
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
        block_size = counter;

        return edges;
    }
}
