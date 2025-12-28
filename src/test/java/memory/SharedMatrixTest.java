package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {

    private static final double EPS = 1e-9;

    private static void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertEquals(expected.length, actual.length, "row count mismatch");
        if (expected.length == 0) return;
        assertEquals(expected[0].length, actual[0].length, "col count mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].length, actual[i].length, "row " + i + " length mismatch");
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], actual[i][j], EPS, "Mismatch at (" + i + "," + j + ")");
            }
        }
    }

    @Test
    void constructor_valid_edge_invalid() {
        // ✅ valid
        double[][] a = {{1, 2}, {3, 4}};
        SharedMatrix M = new SharedMatrix(a);
        assertEquals(2, M.length());
        assertEquals(VectorOrientation.ROW_MAJOR, M.getOrientation());
        assertMatrixEquals(a, M.readRowMajor());

        // ✅ edge: default ctor creates empty
        SharedMatrix empty = new SharedMatrix();
        assertEquals(0, empty.length());
        assertEquals(VectorOrientation.ROW_MAJOR, empty.getOrientation()); // default in your code
        assertMatrixEquals(new double[0][0], empty.readRowMajor());

        // ❌ invalid: constructor with non-rectangular matrix (via loadRowMajor inside ctor)
        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(new double[][]{
                {1, 2},
                {3}
        }));
    }

    @Test
    void loadRowMajor_valid_edge_invalid() {
        SharedMatrix M = new SharedMatrix();

        // ✅ valid
        double[][] a = {{1, 2, 3}, {4, 5, 6}};
        M.loadRowMajor(a);
        assertEquals(VectorOrientation.ROW_MAJOR, M.getOrientation());
        assertEquals(2, M.length());
        assertMatrixEquals(a, M.readRowMajor());

        // ✅ edge: 1x1
        double[][] one = {{9}};
        M.loadRowMajor(one);
        assertEquals(1, M.length());
        assertMatrixEquals(one, M.readRowMajor());

        // ❌ invalid: null
        assertThrows(IllegalArgumentException.class, () -> M.loadRowMajor(null));

        

        // ❌ invalid: non-rectangular
        assertThrows(IllegalArgumentException.class, () -> M.loadRowMajor(new double[][]{
                {1, 2},
                {3}
        }));
    }

    @Test
    void loadColumnMajor_valid_edge_invalid() {
        SharedMatrix M = new SharedMatrix();

        // ✅ valid
        double[][] a = {
                {1, 2, 3},
                {4, 5, 6}
        }; // 2x3
        M.loadColumnMajor(a);
        assertEquals(VectorOrientation.COLUMN_MAJOR, M.getOrientation());
        assertEquals(3, M.length()); // number of columns stored
        assertMatrixEquals(a, M.readRowMajor()); // should still read back same logical matrix

        // ✅ edge: 1x1
        double[][] one = {{7}};
        M.loadColumnMajor(one);
        assertEquals(1, M.length());
        assertEquals(VectorOrientation.COLUMN_MAJOR, M.getOrientation());
        assertMatrixEquals(one, M.readRowMajor());

        // ❌ invalid: null
        assertThrows(IllegalArgumentException.class, () -> M.loadColumnMajor(null));

        

        // ❌ invalid: non-rectangular
        assertThrows(IllegalArgumentException.class, () -> M.loadColumnMajor(new double[][]{
                {1, 2},
                {3}
        }));
    }

    @Test
    void get_valid_edge_invalid() {
        SharedMatrix M = new SharedMatrix();
        M.loadRowMajor(new double[][]{{1, 2}, {3, 4}});

        // ✅ valid
        SharedVector v0 = M.get(0);
        assertNotNull(v0);
        assertEquals(VectorOrientation.ROW_MAJOR, v0.getOrientation());
        assertEquals(2, v0.length());

        // ✅ edge: last index
        SharedVector v1 = M.get(1);
        assertEquals(2, v1.length());

        // ❌ invalid: out of bounds
        assertThrows(IndexOutOfBoundsException.class, () -> M.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> M.get(2));
    }

    @Test
    void readRowMajor_valid_edge_invalid() {
        SharedMatrix M = new SharedMatrix();

        // ✅ valid: row-major storage
        double[][] a = {{1, 2}, {3, 4}};
        M.loadRowMajor(a);
        assertMatrixEquals(a, M.readRowMajor());

        // ✅ edge: column-major storage but still reads same logical matrix
        double[][] b = {
                {10, 20, 30},
                {40, 50, 60}
        };
        M.loadColumnMajor(b);
        assertMatrixEquals(b, M.readRowMajor());

        // ❌ invalid: hard to create an "invalid" internal state without reflection,
        // because validateRectangular + your constructors prevent bad shapes.
        // לכן, במקרה הלא-תקין פה נבדוק invalid input דרך load* (נבדק כבר),
        // ובנוסף נוודא ש-empty matrix returns 0x0:
        SharedMatrix empty = new SharedMatrix();
        assertMatrixEquals(new double[0][0], empty.readRowMajor());
    }
}
