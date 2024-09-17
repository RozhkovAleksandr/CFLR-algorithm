import org.ejml.data.DMatrixSparseCSC;

public class FastMatrixCell extends AbstractMatrix {

    public FastMatrixCell(DMatrixSparseCSC matrix) {
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


    
}
