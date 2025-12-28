package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        if (numThreads <= 0) {
            throw new IllegalArgumentException("Number of threads must be positive.");
        }
        workers = new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = 0.5 + Math.random(); // [0.5, 1.5)
            workers[i] = new TiredThread(i, fatigueFactor);
            idleMinHeap.add(workers[i]);
            workers[i].start();
        }
       
    }

       public void submit(Runnable task) {
        if (task == null) throw new IllegalArgumentException("task cannot be null");

        final TiredThread worker;

        synchronized (this) {
            while (idleMinHeap.isEmpty()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for an idle worker", e);
                }
            }

            // Take least-fatigued idle worker
            worker = idleMinHeap.poll();
            inFlight.incrementAndGet();
        }

        // Wrap to ensure worker is returned to idle heap even if task throws
        Runnable wrapped = () -> {
            try {
                task.run();
            } finally {
                synchronized (this) {
                    idleMinHeap.add(worker);
                    inFlight.decrementAndGet();
                    
                    this.notifyAll();
                }
            }
        };

        // newTask is non-blocking; if it fails, we must roll back inFlight + idle heap
        try {
            worker.newTask(wrapped);
        } catch (RuntimeException e) {
            synchronized (this) {
                // Put the worker back + fix counter
                idleMinHeap.add(worker);
                inFlight.decrementAndGet();
                this.notifyAll();
            }
            throw e;
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
     
        for (Runnable r : tasks) {
            submit(r);
        }
        synchronized (this) {
            while (inFlight.get() > 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for tasks to finish", e);
                }
            }
    }
    }

    public void shutdown() throws InterruptedException {
        // TODO
        synchronized (this) {
            while (inFlight.get() > 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for tasks to finish", e);
                }
            }
        }
        for (TiredThread worker : workers) {
            worker.shutdown();
        }
        for (TiredThread worker : workers) {
            worker.join();
        }
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        StringBuilder stats = new StringBuilder();
        for (TiredThread worker : workers) {
            stats.append("Worker ")
              .append(worker.getWorkerId())
              .append(" name=")
              .append(worker.getName())
              .append(" fatigue=")
              .append(worker.getFatigue())
              .append(" used=")
              .append(worker.getTimeUsed())
              .append(" idle=")
              .append(worker.getTimeIdle())
              .append("\n");
        }
        

        return stats.toString();
    }
}
