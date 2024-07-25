import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ejml.data.DMatrixSparseCSC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class MatrixTest {

    @Test
    public void testSimpleWords() {
        List<Edge> edges = Arrays.asList(
            new Edge(1, 2, "a"),
            new Edge(2, 3, "b")
        );

        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("AB", "S");

        HashMap<String, DMatrixSparseCSC> result = Matrix.contextFreePathQuerying(grammar, edges);

        DMatrixSparseCSC matrixS = result.get("S");
        assertEquals(1, matrixS.get(0, 2));
    }

    @Test
    public void testDifficultWords() {

        List<Edge> edges = Arrays.asList(
            new Edge(1, 2, "apple"),
            new Edge(2, 3, "pineapple")
        );
        
        Grammar grammar = new Grammar();
        grammar.addProductionRules("apple", "A");
        grammar.addProductionRules("pineapple", "B");
        grammar.addProductionRules("AB", "S");

        HashMap<String, DMatrixSparseCSC> result = Matrix.contextFreePathQuerying(grammar, edges);

        DMatrixSparseCSC matrixS = result.get("S");
        assertEquals(1, matrixS.get(0, 2));
    }

    @Test
    public void testEmpty() {
        List<Edge> edges = Arrays.asList(
            new Edge(1, 2, "a"),
            new Edge(2, 3, "a"),
            new Edge(4, 5, "b")
        );
        
        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("AB", "S");

        HashMap<String, DMatrixSparseCSC> result = Matrix.contextFreePathQuerying(grammar, edges);

        DMatrixSparseCSC matrixS = result.get("S");
        boolean hasNonZeroElements = matrixS.getNumCols() != 0;
        assertTrue(hasNonZeroElements);  
    }
}