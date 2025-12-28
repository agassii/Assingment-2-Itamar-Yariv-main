package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

    private static final double EPS = 1e-9;

    @Test
    void constructor_valid_edge_invalid() {
        // ✅ valid
        SharedVector v = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        assertEquals(3, v.length());
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
        assertEquals(2.0, v.get(1), EPS);

        // ✅ edge: length=1
        SharedVector one = new SharedVector(new double[]{42}, VectorOrientation.COLUMN_MAJOR);
        assertEquals(1, one.length());
        assertEquals(42.0, one.get(0), EPS);

        // ❌ invalid: null vector
        assertThrows(IllegalArgumentException.class,
                () -> new SharedVector(null, VectorOrientation.ROW_MAJOR));

        // ❌ invalid: null orientation
        assertThrows(IllegalArgumentException.class,
                () -> new SharedVector(new double[]{1}, null));
    }

    @Test
    void get_valid_edge_invalid() {
        SharedVector v = new SharedVector(new double[]{10, 20, 30}, VectorOrientation.ROW_MAJOR);

        // ✅ valid
        assertEquals(10.0, v.get(0), EPS);

        // ✅ edge: last index
        assertEquals(30.0, v.get(2), EPS);

        // ❌ invalid: out of bounds
        assertThrows(IndexOutOfBoundsException.class, () -> v.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> v.get(3));
    }

    @Test
    void transpose_valid_edge_invalid() {
        // ✅ valid
        SharedVector v = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());

        // ✅ edge: transpose twice returns to original
        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());

        // ❌ invalid: אין “invalid” טבעי ל-transpose אצלך (הוא תמיד חוקי)
        // אז אנחנו בודקים עקביות: לא אמור לזרוק חריגה
        assertDoesNotThrow(v::transpose);
    }

    @Test
    void add_valid_edge_invalid() {
        // ✅ valid
        SharedVector a = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector b = new SharedVector(new double[]{10, 20, 30}, VectorOrientation.ROW_MAJOR);
        a.add(b);
        assertEquals(11.0, a.get(0), EPS);
        assertEquals(22.0, a.get(1), EPS);
        assertEquals(33.0, a.get(2), EPS);

        // ✅ edge: length=1
        SharedVector x = new SharedVector(new double[]{5}, VectorOrientation.COLUMN_MAJOR);
        SharedVector y = new SharedVector(new double[]{-2}, VectorOrientation.COLUMN_MAJOR);
        x.add(y);
        assertEquals(3.0, x.get(0), EPS);

        // ❌ invalid: orientation mismatch
        SharedVector r = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector c = new SharedVector(new double[]{3, 4}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> r.add(c));

        // ❌ invalid: length mismatch (same orientation)
        SharedVector p = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector q = new SharedVector(new double[]{4, 5}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> p.add(q));
    }

    @Test
    void negate_valid_edge_invalid() {
        // ✅ valid
        SharedVector v = new SharedVector(new double[]{1, -2, 3}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assertEquals(-1.0, v.get(0), EPS);
        assertEquals(2.0, v.get(1), EPS);
        assertEquals(-3.0, v.get(2), EPS);

        // ✅ edge: negate twice returns to original values
        v.negate();
        assertEquals(1.0, v.get(0), EPS);
        assertEquals(-2.0, v.get(1), EPS);
        assertEquals(3.0, v.get(2), EPS);

        // ❌ invalid: אין “invalid” טבעי ל-negate אצלך (הוא תמיד חוקי)
        assertDoesNotThrow(v::negate);
    }

    @Test
    void dot_valid_edge_invalid() {
        // ✅ valid: row · column
        SharedVector row = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.COLUMN_MAJOR);
        assertEquals(1 * 4 + 2 * 5 + 3 * 6, row.dot(col), EPS);

        // ✅ edge: length=1
        SharedVector r1 = new SharedVector(new double[]{7}, VectorOrientation.ROW_MAJOR);
        SharedVector c1 = new SharedVector(new double[]{-3}, VectorOrientation.COLUMN_MAJOR);
        assertEquals(-21.0, r1.dot(c1), EPS);

        // ❌ invalid: null other
        assertThrows(IllegalArgumentException.class, () -> row.dot(null));

        // ❌ invalid: length mismatch
        SharedVector col2 = new SharedVector(new double[]{1, 2}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> row.dot(col2));

        // ❌ invalid: orientation mismatch (row·row or col·col)
        SharedVector rowB = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> row.dot(rowB));
    }

    @Test
    void vecMatMul_valid_edge_invalid() {
        // ✅ valid: row vector * COLUMN_MAJOR matrix
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);

        double[][] m = {
                {10, 20, 30},
                {1,  2,  3}
        }; // 2x3

        SharedMatrix M = new SharedMatrix();
        M.loadColumnMajor(m); // vecMatMul דורש COLUMN_MAJOR

        v.vecMatMul(M);
        // result = [1*10+2*1, 1*20+2*2, 1*30+2*3] = [12, 24, 36]
        assertEquals(3, v.length());
        assertEquals(12.0, v.get(0), EPS);
        assertEquals(24.0, v.get(1), EPS);
        assertEquals(36.0, v.get(2), EPS);

        // ✅ edge: 1x1 matrix
        SharedVector v1 = new SharedVector(new double[]{5}, VectorOrientation.ROW_MAJOR);
        SharedMatrix one = new SharedMatrix();
        one.loadColumnMajor(new double[][]{{7}});
        v1.vecMatMul(one);
        assertEquals(1, v1.length());
        assertEquals(35.0, v1.get(0), EPS);

        // ❌ invalid: null matrix
        SharedVector vv = new SharedVector(new double[]{1}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> vv.vecMatMul(null));

        // ❌ invalid: vector not ROW_MAJOR
        SharedVector colVec = new SharedVector(new double[]{1, 2}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> colVec.vecMatMul(M));

        // ❌ invalid: matrix not COLUMN_MAJOR
        SharedMatrix rowMajorMatrix = new SharedMatrix(m); // ctor עושה loadRowMajor
        SharedVector rowVec = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> rowVec.vecMatMul(rowMajorMatrix));
    }
}
