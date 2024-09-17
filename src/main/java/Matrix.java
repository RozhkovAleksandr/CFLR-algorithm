
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

    // обязательное условие. вводить вершины начиная с 0 и не пропускать значения
    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        String filePath = "C:\\Users\\Conff\\vscode\\JavaEducation\\CFLR-algorithm\\src\\main\\java\\values\\grammarFile.txt";
        parseGrammarFile(filePath, grammar);

        String filename = "C:\\Users\\Conff\\vscode\\JavaEducation\\CFLR-algorithm\\src\\main\\java\\values\\\\filename.txt";
        List<Edge> edges = readEdgesFromFile(filename, grammar);

        Optimizations optimizations = new Optimizations(true, false, true, false, false);

        contextFreePathQuerying(grammar, edges, optimizations);
    }

    public static HashMap<String, AbstractMatrix> contextFreePathQuerying(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        HashMap<String, AbstractMatrix> labels = makeMatrix(grammar, edges, optimizations);
        HashMap<String, AbstractMatrix> old = makeMatrix(grammar, Arrays.asList(), optimizations);

        AbstractMatrix tmp2;
        // delete tmp3
        AbstractMatrix tmp3;
        // Check optimization !
        AsistantMatrix help4 = new AsistantMatrix(3, n, block_size);

        boolean changed;
        do {
            changed = false;
            for (Grammar.Production a : grammar.getProductions()) {
                if (a.getLHSL() == null) {
                    continue;
                }

                String key1 = a.getLHSL();
                String key2 = a.getLHSR();
                String production = a.getRHS();

                if (optimizations.isOpt1()) {
                    old.get(key1).multiply(labels.get(key2), help4, 0, production);

                    if (!optimizations.isOpt3()) {
                        for (String key : labels.keySet()) {
                            tmp3 = old.get(key).copy();

                            labels.get(key).add(old.get(key), help4, 1);

                            old.put(key, help4.getMatrix(1).copy());

                            if (tmp3.nz_length() != old.get(key).nz_length()) {
                                changed = true;
                            }
                        }
                    } else {
                        for (String key : labels.keySet()) {
                            old.get(key).add(labels.get(key), help4, 2);

                            old.get(key).toOne(help4.getMatrix(2));
                        }
                    }

                    labels.get(key1).multiply(old.get(key2), help4,1, production);

                    help4.getMatrix(0).add(help4.getMatrix(1), help4,1);

                    old.get(production).subtraction(help4.getMatrix(1), help4.getMatrix(2));
                    
                    tmp2 = help4.getMatrix(1).removeNonPositiveElements();

                    if (optimizations.isOpt3() && tmp2.nz_length() != 0) {             
                        changed = true;
                    }

                    labels.put((production), tmp2.copy());

                } else {
                    AbstractMatrix keyMatrix = labels.get(production);

                    labels.get(key1).multiply(labels.get(key2), help4, 1, production);

                    help4.getMatrix(1).copy().add(keyMatrix, help4, 1);

                    if (keyMatrix.nz_length() != help4.getMatrix(1).nz_length()) {
                        changed = true;
                        labels.put(production, help4.getMatrix(1).copy());
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
                entry.getValue().toOne(help4.getMatrix(0));
                help4.getMatrix(0).print();
            } else {
                entry.getValue().print();
            }
        }

        return current;
    }

    private static HashMap<String, AbstractMatrix> makeMatrix(Grammar grammar, List<Edge> edges, Optimizations optimizations) {
        HashMap<String, AbstractMatrix> labels = new HashMap<>();
        boolean endsWithI;
        for (String key : grammar.getLetters()) {
            AbstractMatrix matrix;

            // if (optimizations.isOpt5()) {
            //     endsWithI = key.endsWith("_i");
            //     if (endsWithI) {
            //         if (grammar.isLhsR(key)) {
            //             matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n, n * block_size));
            //         } else {
            //             matrix = new VectorBlockMatrix(new DMatrixSparseCSC(n * block_size, n));
            //         }      
            //     } else {
            //         matrix = new CellBlockMatrix(new DMatrixSparseCSC(n, n));
            //     }
            // }



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

            labels.put(key, matrix);
        }

        for (Edge edge : edges) {
            int from = edge.getStart();
            int to = edge.getFinish();
            String label = edge.getLabel();

            if (from <= n && to <= n) {
                if (edge.hasN()) {
                    if (grammar.isLhsR(label)) {
                        labels.get(label).set(from , to + n * edge.getN(), 1);
                    } else {
                        labels.get(label).set(from + n * edge.getN(), to, 1);
                    }
                } else {
                    labels.get(label).set(from , to , 1);
                }
                
                String rhs = grammar.getRHSByLHS(label);

                if (rhs != null) {
                    endsWithI = rhs.endsWith("_i");
                    if (edge.hasN() && endsWithI) {
                        if (grammar.isLhsR(label)) {
                            labels.get(rhs).set(from, to + n * edge.getN() , 1);
                        } else {
                            labels.get(rhs).set(from + n * edge.getN(), to  , 1);
                        }
                    } else {
                        labels.get(rhs).set(from , to , 1);
                    }
                }
            }
        }

        // labels.get("a").print();
        
        return labels;
    }

    private static List<Edge> readEdgesFromFile(String filename, Grammar grammar) {
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
    }
}
