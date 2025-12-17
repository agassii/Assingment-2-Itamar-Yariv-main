package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
    this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        validateRectangular(matrix);
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        this.vectors = newVectors;

        
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        validateRectangular(matrix);
        SharedVector[] newVectors = new SharedVector[matrix[0].length];
        for (int j = 0; j < matrix[0].length; j++) {
            double[] column = new double[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                column[i] = matrix[i][j];
            }
            newVectors[j] = new SharedVector(column, VectorOrientation.COLUMN_MAJOR);
        }
        this.vectors = newVectors;
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        SharedVector[] vecs = this.vectors;
        if(vecs.length == 0) {
            return new double[0][0];
        }
        acquireAllVectorReadLocks(vecs);
        try{
        VectorOrientation ori = getOrientation();
        if( ori == VectorOrientation.ROW_MAJOR) {
            double[][] result = new double[vecs.length][vecs[0].length()];
            for (int i = 0; i < vecs.length; i++) {
                if (vecs[i].getOrientation() != VectorOrientation.ROW_MAJOR) {
                        throw new IllegalStateException("Expected ROW_MAJOR vectors in a ROW_MAJOR matrix.");
                    }
                    if (vecs[i].length() != vecs[0].length()) {
                        throw new IllegalArgumentException("Inconsistent row lengths in matrix.");
                    }
                for (int j = 0; j < vecs[0].length(); j++) {
                    result[i][j] = vecs[i].get(j);
                }
            }
            return result;

           } else  {// ori == COLUMN_MAJOR
            double[][] result = new double[vecs[0].length()][vecs.length];
            for (int j = 0; j < vecs.length; j++) {
                 if (vecs[j].getOrientation() != VectorOrientation.COLUMN_MAJOR) {
                        throw new IllegalStateException("Expected COLUMN_MAJOR vectors in a COLUMN_MAJOR matrix.");
                    }
                    if (vecs[j].length() != vecs[0].length()) {
                        throw new IllegalArgumentException("Inconsistent column lengths in matrix.");
                    }
                for (int i = 0; i < vecs[0].length(); i++) {
                    result[i][j] = vecs[j].get(i);
                }
            }
            return result;
        }
    }
        finally {
            releaseAllVectorReadLocks(vecs);
       }     
       
        
    
}
    

    public SharedVector get(int index) {
        if (index < 0 || index >= vectors.length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return vectors[index];
    }

    public int length() {
        // TODO: return number of stored vectors
        return vectors.length;
        
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if (vectors.length == 0) {
           return VectorOrientation.ROW_MAJOR; // default orientation for empty matrix
        }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for (SharedVector vec : vecs) {
            vec.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        for (SharedVector vec : vecs) {
            vec.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for (SharedVector vec : vecs) {
            vec.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for (SharedVector vec : vecs) {
            vec.writeUnlock();
        }
    }

     private void validateRectangular(double[][] matrix) {
        if (matrix == null) throw new IllegalArgumentException("matrix cannot be null");
        if (matrix.length == 0) throw new IllegalArgumentException("matrix cannot be empty");
        if (matrix[0] == null || matrix[0].length == 0) throw new IllegalArgumentException("matrix rows cannot be empty");

        int width = matrix[0].length;
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) throw new IllegalArgumentException("matrix row cannot be null");
            if (matrix[i].length != width) throw new IllegalArgumentException("matrix must be rectangular");
        }
    }
}

