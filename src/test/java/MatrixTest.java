import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class MatrixTest {
    private Optimizations optStandart() {
        Optimizations optimizations = new Optimizations(true, false, false, false, false);

        return optimizations;
    }

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

        Optimizations optimizations = optStandart();

        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);

        AbstractMatrix matrixS = result.get("S");
        assertEquals(1, matrixS.nz_length());   
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

        Optimizations optimizations = optStandart();

        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);

        AbstractMatrix matrixS = result.get("S");
        assertEquals(1, matrixS.nz_length());
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

        Optimizations optimizations = optStandart();

        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);

        AbstractMatrix matrixS = result.get("S");
        boolean hasNonZeroElements = matrixS.nz_length() == 0;
        assertTrue(hasNonZeroElements);  
    }

    @Test
    public void testLeftMany() {
        List<Edge> edges = Arrays.asList(
            new Edge(1, 2, "a"),
            new Edge(2, 3, "a"),
            new Edge(4, 5, "b"),
            new Edge(5, 5, "b"),
            new Edge(1, 5, "b"),
            new Edge(2, 5, "b"),
            new Edge(3, 5, "b")
        );
        
        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("AB", "S");

        Optimizations optimizations = optStandart();

        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);

        AbstractMatrix matrixS = result.get("S");
        boolean elem = matrixS.nz_length() == 2;
        assertTrue(elem);
    }

    @Test
    public void testMultiplePaths() {
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

        Optimizations optimizations = optStandart();

        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);

        AbstractMatrix matrixS = result.get("S");
        boolean hasNonZeroElements = matrixS.nz_length() == 3;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testGraphWithCycle() {
        List<Edge> edges = Arrays.asList(
                new Edge(1, 2, "a"),
                new Edge(2, 3, "b"),
                new Edge(3, 2, "a"), 
                new Edge(3, 4, "c")
        );

        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("c", "C");
        grammar.addProductionRules("AB", "S");

        Optimizations optimizations = optStandart();

        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);
        AbstractMatrix matrixS = result.get("S");
        boolean hasNonZeroElements = matrixS.nz_length() == 2;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testNoPaths() {
        List<Edge> edges = Arrays.asList(
                new Edge(1, 2, "a"),
                new Edge(3, 4, "b")
        );

        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("AB", "S");

        Optimizations optimizations = optStandart();
  
        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);

        AbstractMatrix matrixS = result.get("S");
        boolean hasNonZeroElements = matrixS.nz_length() == 0;
        assertTrue(hasNonZeroElements); 
    }

    @Test
    public void testSelfLoop() {
        List<Edge> edges = Arrays.asList(
                new Edge(1, 1, "a")
        );

        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("AA", "S");

        Optimizations optimizations = optStandart();

        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);

        AbstractMatrix matrixS = result.get("S");
        boolean hasNonZeroElements = matrixS.nz_length() == 1;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testCyclicGraph() {
        List<Edge> edges = Arrays.asList(
                new Edge(1, 2, "a"),
                new Edge(2, 1, "b")
        );

        Grammar grammar = new Grammar();
        grammar.addProductionRules("a", "A");
        grammar.addProductionRules("b", "B");
        grammar.addProductionRules("AB", "S");

        Optimizations optimizations = optStandart();

        HashMap<String, AbstractMatrix> result = Matrix.contextFreePathQuerying(grammar, edges, optimizations);

        AbstractMatrix matrixS = result.get("S");
        boolean hasNonZeroElements = matrixS.nz_length() != 0;
        assertTrue(hasNonZeroElements);
    }
}