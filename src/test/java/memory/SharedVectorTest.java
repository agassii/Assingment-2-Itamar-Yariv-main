package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharedVectorTest {

    private static final double EPS = 1e-9;

    @Test
    void constructor_nullVector_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new SharedVector(null, VectorOrientation.ROW_MAJOR));
    }

    @Test
    void constructor_nullOrientation_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new SharedVector(new double[]{1, 2, 3}, null));
    }

    @Test
    void add_validVectors_shouldModifyThisVector() {
        SharedVector v1 = new SharedVector(
                new double[]{1, 2, 3},
                VectorOrientation.ROW_MAJOR
        );
        SharedVector v2 = new SharedVector(
                new double[]{4, -1, 0.5},
                VectorOrientation.ROW_MAJOR
        );

        v1.add(v2);

        assertEquals(5.0, v1.get(0), EPS);
        assertEquals(1.0, v1.get(1), EPS);
        assertEquals(3.5, v1.get(2), EPS);
    }

    @Test
    void add_differentLengths_shouldThrow() {
        SharedVector v1 = new SharedVector(
                new double[]{1, 2, 3},
                VectorOrientation.ROW_MAJOR
        );
        SharedVector v2 = new SharedVector(
                new double[]{1, 2},
                VectorOrientation.ROW_MAJOR
        );

        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }

    @Test
    void add_differentOrientation_shouldThrow() {
        SharedVector v1 = new SharedVector(
                new double[]{1, 2, 3},
                VectorOrientation.ROW_MAJOR
        );
        SharedVector v2 = new SharedVector(
                new double[]{1, 2, 3},
                VectorOrientation.COLUMN_MAJOR
        );

        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }

    @Test
    void negate_shouldFlipSigns() {
        SharedVector v = new SharedVector(
                new double[]{2, -3, 0},
                VectorOrientation.ROW_MAJOR
        );

        v.negate();

        assertEquals(-2.0, v.get(0), EPS);
        assertEquals(3.0, v.get(1), EPS);
        assertEquals(0.0, v.get(2), EPS);
    }

    @Test
    void vectorPlusMinusVector_shouldBecomeZeroVector() {
        SharedVector v = new SharedVector(
                new double[]{1, -2, 3.5},
                VectorOrientation.ROW_MAJOR
        );
        SharedVector u = new SharedVector(
                new double[]{1, -2, 3.5},
                VectorOrientation.ROW_MAJOR
        );

        u.negate();
        v.add(u);

        for (int i = 0; i < v.length(); i++) {
            assertEquals(0.0, v.get(i), EPS);
        }
    }

    @Test
    void dot_rowWithColumn_shouldWork() {
        SharedVector row = new SharedVector(
                new double[]{1, 2, 3},
                VectorOrientation.ROW_MAJOR
        );
        SharedVector col = new SharedVector(
                new double[]{4, 5, 6},
                VectorOrientation.COLUMN_MAJOR
        );

        double result = row.dot(col);

        // 1*4 + 2*5 + 3*6 = 32
        assertEquals(32.0, result, EPS);
    }

    @Test
    void dot_wrongOrientation_shouldThrow() {
        SharedVector v1 = new SharedVector(
                new double[]{1, 2, 3},
                VectorOrientation.ROW_MAJOR
        );
        SharedVector v2 = new SharedVector(
                new double[]{4, 5, 6},
                VectorOrientation.ROW_MAJOR
        );

        assertThrows(IllegalArgumentException.class, () -> v1.dot(v2));
    }

    @Test
    void vecMatMul_identityMatrix_shouldKeepVector() {
        SharedVector v = new SharedVector(
                new double[]{7, 8},
                VectorOrientation.ROW_MAJOR
        );

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{
                {1, 0},
                {0, 1}
        });

        v.vecMatMul(m);

        assertEquals(7.0, v.get(0), EPS);
        assertEquals(8.0, v.get(1), EPS);
    }

    @Test
    void vecMatMul_dimensionMismatch_shouldThrow() {
        SharedVector v = new SharedVector(
                new double[]{1, 2, 3},
                VectorOrientation.ROW_MAJOR
        );

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{
                {1, 0},
                {0, 1}
        });

        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(m));
    }
}
