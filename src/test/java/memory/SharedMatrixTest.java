package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    private static final double EPS = 1e-9;

    @Test
    void loadRowMajor_thenReadRowMajor_shouldReturnSameMatrix() {
        double[][] data = {
                {1, 2, 3},
                {4, 5, 6}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(data);

        double[][] out = m.readRowMajor();

        assertEquals(2, out.length);
        assertEquals(3, out[0].length);

        assertEquals(1.0, out[0][0], EPS);
        assertEquals(6.0, out[1][2], EPS);
    }

    @Test
    void loadColumnMajor_thenReadRowMajor_shouldConvertCorrectly() {
        // Represents:
        // [ [1, 2],
        //   [3, 4],
        //   [5, 6] ]
        double[][] columnMajor = {
                {1, 3, 5},
                {2, 4, 6}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(columnMajor);

        double[][] out = m.readRowMajor();

        assertEquals(3, out.length);
        assertEquals(2, out[0].length);

        assertEquals(1.0, out[0][0], EPS);
        assertEquals(2.0, out[0][1], EPS);
        assertEquals(3.0, out[1][0], EPS);
        assertEquals(4.0, out[1][1], EPS);
        assertEquals(5.0, out[2][0], EPS);
        assertEquals(6.0, out[2][1], EPS);
    }

    @Test
    void loadRowMajor_raggedArray_shouldThrow() {
        double[][] ragged = {
                {1, 2, 3},
                {4, 5}
        };

        SharedMatrix m = new SharedMatrix();
        assertThrows(IllegalArgumentException.class, () -> m.loadRowMajor(ragged));
    }

    @Test
    void loadColumnMajor_raggedArray_shouldThrow() {
        double[][] ragged = {
                {1, 2},
                {3}
        };

        SharedMatrix m = new SharedMatrix();
        assertThrows(IllegalArgumentException.class, () -> m.loadColumnMajor(ragged));
    }

    @Test
    void get_outOfBounds_shouldThrow() {
        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(new double[][]{
                {1, 2},
                {3, 4}
        });

        assertThrows(IndexOutOfBoundsException.class, () -> m.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> m.get(100));
    }
}
