package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        if (vector == null) throw new IllegalArgumentException("vector cannot be null");
        if (orientation == null) throw new IllegalArgumentException("orientation cannot be null");

        this.vector = vector;
        this.orientation = orientation;

    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
           readLock();
        try {
            return vector[index];
        } finally {
            readUnlock();
        }
    }

    public int length() {
        readLock();
        try{
            return vector.length;
        } finally {
            readUnlock();
        }
        
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        readLock();
        try {
            return orientation;
        } finally {
            readUnlock();
        }
        
    }

    public void writeLock() {
        
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        
        lock.writeLock().unlock();
    }

    public void readLock() {
        
        lock.readLock().lock(); 
    }

    public void readUnlock() {
       
        lock.readLock().unlock();
    }

    public void transpose() {
        writeLock();
        try {
            if (orientation == VectorOrientation.ROW_MAJOR) {
                orientation = VectorOrientation.COLUMN_MAJOR;
            } else {
                orientation = VectorOrientation.ROW_MAJOR;
            }
        } finally {
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        if (this.getOrientation() != other.getOrientation()) {
            throw new IllegalArgumentException("Vectors must have the same orientation to add.");
        }
        writeLock();
        other.readLock();
        try {
            if (this.length() != other.length()) {
                throw new IllegalArgumentException("Vectors must be of the same length to add.");
            }
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] += other.vector[i];
            }
        } finally {
            other.readUnlock();
            writeUnlock();
        }
    }

    public void negate() {
        writeLock();
        try {
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] = -this.vector[i];
            }
        } finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        if (other == null) {
            throw new IllegalArgumentException("Other vector cannot be null.");
        }
        if (this.length() != other.length()) {
            throw new IllegalArgumentException("Vectors must be of the same length to compute dot product.");
        }
        readLock();
        other.readLock();
        try {
            if (this.orientation != VectorOrientation.ROW_MAJOR || other.orientation != VectorOrientation.COLUMN_MAJOR) {
                throw new IllegalArgumentException("Dot product requires row · column ");
            }
            double result = 0.0;
            for (int i = 0; i < this.length(); i++) {
                result += this.vector[i] * other.vector[i];
            }
            return result;
        } finally {
            other.readUnlock(); 
            readUnlock();
        }
        
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null.");
        }
        readLock();
        try {
            if (this.orientation != VectorOrientation.ROW_MAJOR) {
                throw new IllegalArgumentException("vecMatMul requires the vector to be ROW_MAJOR.");
            }
        } finally {
            readUnlock();
        }

        if (matrix.getOrientation() != VectorOrientation.COLUMN_MAJOR) {
            throw new IllegalArgumentException("vecMatMul requires matrix to be COLUMN_MAJOR.");
        }
        
        int matrixCols = matrix.length();
        double[] result = new double[matrixCols];
        writeLock();
        try {
            for (int j = 0; j < matrixCols; j++) {
                SharedVector colVector = matrix.get(j);
                result[j] = this.dot(colVector);
            }
            this.vector = result;
        } finally {
            writeUnlock();
        }

    }
}
