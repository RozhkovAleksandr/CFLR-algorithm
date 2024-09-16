import java.util.ArrayList;
import java.util.List;

import org.ejml.data.DMatrixSparseCSC;

public class AsistantMatrix {

    List<AbstractMatrix> order;
    List<AbstractMatrix> orderNow;

    AbstractMatrix tmp1;
    AbstractMatrix tmp2;
    AbstractMatrix tmp3;
    AbstractMatrix tmp11;
    AbstractMatrix tmp22;
    AbstractMatrix tmp33;
    AbstractMatrix tmp111;
    AbstractMatrix tmp222;
    AbstractMatrix tmp333;

    public AsistantMatrix(int optim, int n, int block_size) {
        order = new ArrayList<>();
        orderNow = new ArrayList<>();
        switch (optim) {
            case 4:
                tmp1 = new CellBlockMatrix(new DMatrixSparseCSC(n, n));
                tmp2 = new CellBlockMatrix(new DMatrixSparseCSC(n, n));
                tmp3 = new CellBlockMatrix(new DMatrixSparseCSC(n, n));
                tmp11 = new VectorBlockMatrix(new DMatrixSparseCSC(n * block_size, n));
                tmp22 = new VectorBlockMatrix(new DMatrixSparseCSC(n * block_size, n));
                tmp33 = new VectorBlockMatrix(new DMatrixSparseCSC(n * block_size, n));
                tmp111 = new VectorBlockMatrix(new DMatrixSparseCSC(n, n * block_size));
                tmp222 = new VectorBlockMatrix(new DMatrixSparseCSC(n, n *  block_size));
                tmp333 = new VectorBlockMatrix(new DMatrixSparseCSC(n, n * block_size));

                order.add(tmp1);
                order.add(tmp2);
                order.add(tmp3);
                order.add(tmp11);
                order.add(tmp22);
                order.add(tmp33);
                order.add(tmp111);
                order.add(tmp222);
                order.add(tmp333);

                orderNow.add(tmp1);
                orderNow.add(tmp2);
                orderNow.add(tmp3);
                break;
            case 2:
                tmp1 = new FormatMatrix(new DMatrixSparseCSC(n, n));
                tmp2 = new FormatMatrix(new DMatrixSparseCSC(n, n));
                tmp3 = new FormatMatrix(new DMatrixSparseCSC(n, n));
                orderNow.add(tmp1);
                orderNow.add(tmp2);
                orderNow.add(tmp3);
                break;
            default:
                tmp1 = new BaseMatrix(new DMatrixSparseCSC(n, n));
                tmp2 = new BaseMatrix(new DMatrixSparseCSC(n, n));
                tmp3 = new BaseMatrix(new DMatrixSparseCSC(n, n));
                orderNow.add(tmp1);
                orderNow.add(tmp2);
                orderNow.add(tmp3);
                break;
        }
    }

    public AbstractMatrix getMatrix(int index) {
        return orderNow.get(index);
    }

    public void putMatrix(String direction, int index, CellBlockMatrix m) {
        // orderNow.set(index, m);
        order.set(index, m);
        orderNow.set(index, order.get(index));
        
    }

    public AbstractMatrix getMatrix(String direction, int index) {
        switch (direction) {
            case "vertical":
                orderNow.set(index, order.get(index + 3));
                return orderNow.get(index);
            case "horizon":
                orderNow.set(index, order.get(index + 6));
                return orderNow.get(index);
            default:
                orderNow.set(index, order.get(index));
                return orderNow.get(index);
        }
    }
}
