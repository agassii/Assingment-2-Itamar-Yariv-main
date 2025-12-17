package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {

    private static void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertEquals(expected.length, actual.length, "row count mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], 1e-9, "row " + i + " mismatch");
        }
    }

    @Test
    void constructor_buildsRowMajor() {
        double[][] A = {
                {1, 2},
                {3, 4}
        };

        SharedMatrix m = new SharedMatrix(A);

        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
        assertEquals(2, m.length()); // stored vectors == rows
        assertEquals(VectorOrientation.ROW_MAJOR, m.get(0).getOrientation());
        assertEquals(2, m.get(0).length());
    }

    @Test
    void loadRowMajor_thenReadRowMajor_returnsSameMatrix() {
        double[][] A = {
                {1, 2, 3},
                {4, 5, 6}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(A);

        double[][] out = m.readRowMajor();
        assertMatrixEquals(A, out);
    }

    @Test
    void loadColumnMajor_thenReadRowMajor_returnsSameMatrix() {
        double[][] A = {
                {1, 2, 3},
                {4, 5, 6}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(A);

        assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
        assertEquals(3, m.length()); // stored vectors == columns
        assertEquals(VectorOrientation.COLUMN_MAJOR, m.get(0).getOrientation());
        assertEquals(2, m.get(0).length()); // each column has 2 rows

        double[][] out = m.readRowMajor();
        assertMatrixEquals(A, out);
    }

    @Test
    void emptyMatrix_hasDeterministicDefaults() {
        SharedMatrix m = new SharedMatrix();
        assertEquals(0, m.length());
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
        assertMatrixEquals(new double[0][0], m.readRowMajor());
    }

    @Test
    void loadNonRectangular_throws() {
        double[][] bad = {
                {1, 2, 3},
                {4, 5}
        };

        SharedMatrix m = new SharedMatrix();
        assertThrows(IllegalArgumentException.class, () -> m.loadRowMajor(bad));
        assertThrows(IllegalArgumentException.class, () -> m.loadColumnMajor(bad));
    }
}
