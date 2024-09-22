import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class MatrixTest {
    private Optimizations optStandart() {
        Optimizations optimizations = new Optimizations("5");

        return optimizations;
    }

    @Test
    public void testSimpleWords() {

        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/SimpleWords/grammarSimple.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/SimpleWords/pathSimple.g", grammar);

        System.out.println("Grammar: " + grammar);
        System.out.println("Edges: " + edges);

        Optimizations optimizations = optStandart();

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        assertEquals(1, matrixS.nz_length());   
    }

    @Test
    public void testSimpleWords0() {

        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/SimpleWords/grammarSimple.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/SimpleWords/pathSimple.g", grammar);

        System.out.println("Grammar: " + grammar);
        System.out.println("Edges: " + edges);

        Optimizations optimizations = new Optimizations("0");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        assertEquals(1, matrixS.nz_length());   
    }

    @Test
    public void testSimpleWords1() {

        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/SimpleWords/grammarSimple.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/SimpleWords/pathSimple.g", grammar);

        System.out.println("Grammar: " + grammar);
        System.out.println("Edges: " + edges);

        Optimizations optimizations = new Optimizations("1");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        assertEquals(1, matrixS.nz_length());   
    }

    @Test
    public void testSimpleWords2() {

        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/SimpleWords/grammarSimple.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/SimpleWords/pathSimple.g", grammar);

        System.out.println("Grammar: " + grammar);
        System.out.println("Edges: " + edges);

        Optimizations optimizations = new Optimizations("2");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        assertEquals(1, matrixS.nz_length());   
    }

    @Test
    public void testSimpleWords3() {

        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/SimpleWords/grammarSimple.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/SimpleWords/pathSimple.g", grammar);

        System.out.println("Grammar: " + grammar);
        System.out.println("Edges: " + edges);

        Optimizations optimizations = new Optimizations("3");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        assertEquals(1, matrixS.nz_length());   
    }

    @Test
    public void testGraphWithCycle() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/GraphWithCycle/an_bn.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/GraphWithCycle/tree.g", grammar);

        Optimizations optimizations = optStandart();

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        boolean hasNonZeroElements = matrixS.nz_length() == 20;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testIndexedSimple() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/IndexedSimple/an_bn_indexed.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/IndexedSimple/indexed_tree.g", grammar);

        Optimizations optimizations = optStandart();

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        boolean hasNonZeroElements = matrixS.nz_length() == 12;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testIndexedSimple4() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/IndexedSimple/an_bn_indexed.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/IndexedSimple/indexed_tree.g", grammar);

        Optimizations optimizations = new Optimizations("4");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        boolean hasNonZeroElements = matrixS.nz_length() == 12;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testGraphWithEpsilon() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/GraphWithEpsilon/c_alias.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/GraphWithEpsilon/c_prog.g", grammar);

        Optimizations optimizations = optStandart();

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        boolean hasNonZeroElements = matrixS.nz_length() == 156;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testGraphWithEpsilon3() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/GraphWithEpsilon/c_alias.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/GraphWithEpsilon/c_prog.g", grammar);

        Optimizations optimizations = new Optimizations("3");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        boolean hasNonZeroElements = matrixS.nz_length() == 156;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testGraphWithEpsilon4() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/GraphWithEpsilon/c_alias.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/GraphWithEpsilon/c_prog.g", grammar);

        Optimizations optimizations = new Optimizations("4");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        boolean hasNonZeroElements = matrixS.nz_length() == 156;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testIndexedSimple2() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/IndexedSimple2/an_bn_indexed2.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/IndexedSimple2/indexed_tree.g", grammar);

        Optimizations optimizations = optStandart();

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "S");
        boolean hasNonZeroElements = matrixS.nz_length() == 8;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testCycleSimple() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/CycleSimple/transitive.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/CycleSimple/loop.g", grammar);

        Optimizations optimizations = optStandart();

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "A");
        boolean hasNonZeroElements = matrixS.nz_length() == 9;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testCycleSimple2() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/CycleSimple/transitive.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/CycleSimple/loop.g", grammar);

        Optimizations optimizations = new Optimizations("2");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "A");
        boolean hasNonZeroElements = matrixS.nz_length() == 9;
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testManyRules() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/ManyRules/java_points_to.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/ManyRules/java_prog.g", grammar);

        Optimizations optimizations = optStandart();

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "PT");
        boolean hasNonZeroElements = matrixS.nz_length() == 6;
        System.out.println(matrixS.nz_length());
        assertTrue(hasNonZeroElements);
    }

    @Test
    public void testManyRules4() {
        Grammar grammar = new Grammar();
        Matrix.parseGrammarFile("src/test/java/values/ManyRules/java_points_to.cnf", grammar);

        List<Edge> edges = Matrix.readEdgesFromFile("src/test/java/values/ManyRules/java_prog.g", grammar);

        Optimizations optimizations = new Optimizations("4");

        AbstractMatrix matrixS = Matrix.contextFreePathQuerying(grammar, edges, optimizations, "PT");
        boolean hasNonZeroElements = matrixS.nz_length() == 6;
        System.out.println(matrixS.nz_length());
        assertTrue(hasNonZeroElements);
    }
}