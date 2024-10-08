## CFLR-algorithm

This repository implements a CFLR (Context-Free Language Recognition) algorithm.


### Getting Started

1. Clone the repository: \
 git clone https://github.com/your-username/CFLR-algorithm.git
2. Build the project: \
 cd CFLR-algorithm \
./gradlew build
3. Run the algorithm: \
./gradlew run --args='<grammar_file> <graph_file> <optimization_level> <start_symbol>'
Arguments:

* <grammar_file>: The path to the file containing the context-free grammar.
* <graph_file>: The path to a file containing a set of adjacent vertices and their symbol. (Vertices must start from zero and go sequentially)

* <optimization_level>: Integer value representing the optimization level to be applied. Possible values:

    * 0: No optimizations
    * 1: Incremental calculations
    * 2: Matrix formats
    * 3: Delayed matrix addition
    * 4: Production patterns
    * **5: All optimizations**

* <start_symbol>: Nonterminal symbol from the grammar that represents the starting point of the recognition process.


### Output

The program will display the result of the recognition process, indicating how many paths have been found. Additionally, all reachable pairs of vertices will be saved in a file named **reachability_pairs.txt**. It will also display the execution time of the algorithm.

