

import org.ejml.data.DMatrixSparseCSC;

public class BlockMatrix extends AbstractMatrix {

    public BlockMatrix(DMatrixSparseCSC matrix) {
        super(matrix);
    }

    @Override
    public void multiply(AbstractMatrix other, AsistantMatrix asistant, int n, String production) {
        throw new UnsupportedOperationException("Unimplemented method 'multiply'");
    }

    @Override
    public void add(AbstractMatrix other, AsistantMatrix asistant, int n) {
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public void subtraction(AbstractMatrix other, AbstractMatrix tmp) {
        throw new UnsupportedOperationException("Unimplemented method 'subtraction'");
    }

    @Override
    public AbstractMatrix copy() {
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }
}