package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
     if (computationRoot == null) {
            throw new IllegalArgumentException("computationRoot cannot be null");
        }

        // Keep resolving until root becomes a MATRIX node
        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX) {
            ComputationNode next = computationRoot.findResolvable();
            if (next == null) {
                // Should not happen if the tree is well-formed
                throw new IllegalStateException("No resolvable node found, but root is not a MATRIX");
            }

            loadAndCompute(next);

            // After tasks finish, M1 holds the output
            double[][] result = leftMatrix.readRowMajor();
            next.resolve(result);
        }

        // Clean shutdown once entire computation finishes
        try {
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during executor shutdown", e);
        }

        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        ComputationNodeType type = node.getNodeType();
        if (type == ComputationNodeType.MATRIX) return;

        List<ComputationNode> children = node.getChildren();
        if (children == null) throw new IllegalArgumentException("Node has no children");

        // Arity checks (per assignment)
        if ((type == ComputationNodeType.NEGATE || type == ComputationNodeType.TRANSPOSE) && children.size() != 1) {
            throw new IllegalArgumentException("Illegal operation: unary operator with " + children.size() + " operands");
        }
        if ((type == ComputationNodeType.ADD || type == ComputationNodeType.MULTIPLY) && children.size() != 2) {
            throw new IllegalArgumentException("Illegal operation: binary operator with " + children.size() + " operands");
        }

        // Load operands from MATRIX children into M1/M2
        if (type == ComputationNodeType.NEGATE || type == ComputationNodeType.TRANSPOSE) {
            double[][] a = children.get(0).getMatrix();
            leftMatrix.loadRowMajor(a);   // store as rows
            rightMatrix.loadRowMajor(new double[0][0]); // optional clear
        } else {
            double[][] a = children.get(0).getMatrix();
            double[][] b = children.get(1).getMatrix();

            if (type == ComputationNodeType.ADD) {
                // Addition expects same dimensions; store both as rows
                leftMatrix.loadRowMajor(a);
                rightMatrix.loadRowMajor(b);
            } else { // MULTIPLY
                // For multiplication: left as rows, right as columns (efficient row * matrix)
                leftMatrix.loadRowMajor(a);
                rightMatrix.loadColumnMajor(b);
            }
        }

        // Dimension checks (error-handling logic)
        switch (type) {
            case ADD: {
                // same #rows and same #cols
                if (leftMatrix.length() != rightMatrix.length()) {
                    throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
                }
                if (leftMatrix.length() > 0) {
                    int colsA = leftMatrix.get(0).length();
                    int colsB = rightMatrix.get(0).length();
                    if (colsA != colsB) {
                        throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
                    }
                }
                executor.submitAll(createAddTasks());
                break;
            }

            case NEGATE: {
                executor.submitAll(createNegateTasks());
                break;
            }

            case TRANSPOSE: {
                executor.submitAll(createTransposeTasks());
                break;
            }

            case MULTIPLY: {
                // A is (m x k) stored as m row vectors length k
                // B is (k x n) stored as n column vectors length k
                if (leftMatrix.length() == 0 || rightMatrix.length() == 0) {
                    // define empty multiplication as mismatch (or allow 0x0); choose consistent policy
                    // If your SharedMatrix allows empty, you may allow returning empty.
                    throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
                }

                int kA = leftMatrix.get(0).length();
                int kB = rightMatrix.get(0).length();
                if (kA != kB) {
                    throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
                }

                executor.submitAll(createMultiplyTasks());
                break;
            }

            default:
                throw new IllegalArgumentException("Unsupported operator: " + type);
        }
    }

        
    
    public List<Runnable> createAddTasks() {
      int n = leftMatrix.length();
    java.util.ArrayList<Runnable> tasks = new java.util.ArrayList<>(n);

    for (int i = 0; i < n; i++) {
        SharedVector l = leftMatrix.get(i);
        SharedVector r = rightMatrix.get(i);
        tasks.add(() -> l.add(r));
    }
    return tasks;
}

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        int n = leftMatrix.length();
        java.util.ArrayList<Runnable> tasks = new java.util.ArrayList<>(n);
        for (int i = 0; i < n; i++) {
                final int idx = i;
            tasks.add(() -> {
                leftMatrix.get(idx).vecMatMul(rightMatrix);
            });
            
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        int n = leftMatrix.length();
        java.util.ArrayList<Runnable> tasks = new java.util.ArrayList<>(n);
        for (int i = 0; i < n; i++) {
                final int idx = i;
            tasks.add(() -> {
                leftMatrix.get(idx).negate();
            });
            
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        int n = leftMatrix.length();
        java.util.ArrayList<Runnable> tasks = new java.util.ArrayList<>(n);
        for (int i = 0; i < n; i++) {
                final int idx = i;
            tasks.add(() -> {
                leftMatrix.get(idx).transpose();
            });
            
        }
        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity

        return executor.getWorkerReport();
    }
}
