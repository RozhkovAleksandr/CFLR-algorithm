

import org.ejml.data.DMatrixSparseCSC;

public class BlockMatrix extends AbstractMatrix {

    public BlockMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AbstractMatrix result) {
        throw new UnsupportedOperationException("Unimplemented method 'multiply'");
    }

    @Override
    public void add(AbstractMatrix other, AbstractMatrix result) {
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public void subtraction(AbstractMatrix other, AbstractMatrix tmp) {
        throw new UnsupportedOperationException("Unimplemented method 'subtraction'");
    }
}