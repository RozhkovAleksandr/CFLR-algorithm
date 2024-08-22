
import org.ejml.data.DMatrixSparseCSC;

class OptimizationMatrix {

    private DMatrixSparseCSC base;
    private DMatrixSparseCSC[] matrices;
    private int b;
    private int min_nz;


    public OptimizationMatrix(DMatrixSparseCSC base) {
        this(base, 10, 2);
    }

    public OptimizationMatrix(DMatrixSparseCSC base, int min_nz, int b) {
        this.base = base;
        this.matrices = new DMatrixSparseCSC[]{base};
        this.min_nz = min_nz;
        this.b = b;
    }

    public void addM(DMatrixSparseCSC deltaM) {

    }

    public void minusM(DMatrixSparseCSC deltaM) {

    }

    public void multLeft(DMatrixSparseCSC deltaM) {

    }

    public void multRight(DMatrixSparseCSC deltaM) {
    }
}