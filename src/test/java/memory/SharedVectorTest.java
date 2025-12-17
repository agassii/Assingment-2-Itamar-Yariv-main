package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

    @Test
    void transpose_togglesOrientation() {
        SharedVector v = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());

        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());

        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    @Test
    void negate_negatesAllEntries() {
        SharedVector v = new SharedVector(new double[]{1, -2, 3.5}, VectorOrientation.ROW_MAJOR);
        v.negate();

        assertEquals(-1.0, v.get(0), 1e-9);
        assertEquals(2.0, v.get(1), 1e-9);
        assertEquals(-3.5, v.get(2), 1e-9);
    }

    @Test
    void add_sameOrientationAndLength_addsInPlace() {
        SharedVector a = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{10, 20, 30}, VectorOrientation.ROW_MAJOR);

        a.add(b);

        assertEquals(11.0, a.get(0), 1e-9);
        assertEquals(22.0, a.get(1), 1e-9);
        assertEquals(33.0, a.get(2), 1e-9);
    }

    @Test
    void add_mismatchedLength_throws() {
        SharedVector a = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{10, 20}, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }

    @Test
    void add_mismatchedOrientation_throws() {
        SharedVector a = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{10, 20, 30}, VectorOrientation.COLUMN_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }

    @Test
    void dot_rowDotColumn_correct() {
        SharedVector row = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.COLUMN_MAJOR);

        double res = row.dot(col); // 1*4 + 2*5 + 3*6 = 32
        assertEquals(32.0, res, 1e-9);
    }

    @Test
    void dot_wrongOrientation_throws() {
        SharedVector a = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> a.dot(b));
    }

    @Test
    void vecMatMul_rowVectorTimesColumnMajorMatrix_correct() {
        // v = [1,2] (row)
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);

        // M = [[3,4,5],
        //      [6,7,8]]  (2x3)
        double[][] M = {
                {3, 4, 5},
                {6, 7, 8}
        };

        SharedMatrix mat = new SharedMatrix();
        mat.loadColumnMajor(M);

        // v*M = [1*3+2*6, 1*4+2*7, 1*5+2*8] = [15,18,21]
        v.vecMatMul(mat);

        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
        assertEquals(3, v.length());
        assertEquals(15.0, v.get(0), 1e-9);
        assertEquals(18.0, v.get(1), 1e-9);
        assertEquals(21.0, v.get(2), 1e-9);
    }
}
