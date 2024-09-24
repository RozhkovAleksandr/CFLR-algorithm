
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import Matrix.AbstractMatrix;
import Matrix.AsistantMatrix;

public class Main {

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
        Handler.parseGrammarFile(fileGrammar, grammar);

        List<Edge> edges = Handler.readEdgesFromFile(filePath, grammar);

        Optimizations optimizations = new Optimizations(optimNumber);

        AbstractMatrix result = contextFreePathQuerying(grammar, edges, optimizations, ultimate);

        File file = new File("reachability_pairs.txt");
        if (file.exists()) {
            file.delete();
        }

        for (int col = 0; col < result.getNumCols(); col++) {
            int colStart = result.col_idx(col);
            int colEnd = result.col_idx(col + 1);

            for (int idx = colStart; idx < colEnd; idx++) {
                int row = result.nz_rows(idx);

                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write(row + "\t" + col + "\n");
                } catch (IOException e) {
                    System.err.println("Error writing to the file: " + e.getMessage());
                }
            }
        }

        System.out.print("Total achievable pairs : " + result.nz_length());
    }

    public static AbstractMatrix contextFreePathQuerying(Grammar grammar, List<Edge> edges, Optimizations optimizations, String ultimate) {
        HashMap<String, AbstractMatrix> labels = Handler.makeMatrix(grammar, edges, optimizations);
        HashMap<String, AbstractMatrix> old = Handler.makeMatrix(grammar, Arrays.asList(), optimizations);

        AbstractMatrix tmp;
        AsistantMatrix storage = new AsistantMatrix(optimizations.number, Handler.n, Handler.block_size);

        boolean changed;
        do {
            changed = false;
            for (Grammar.Production a : grammar.getProductions()) {
                if (a.getLHSL() == null) {
                    Handler.checkingEpsilonCases(a, labels, optimizations);
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
}
