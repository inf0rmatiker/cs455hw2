package main.java.cs455.scaling.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager {

  public List<WorkerThread> threadPool; // List of WorkerThreads which are available
  public List<Batch> taskQueue;
  private int threadPoolSize;
  private int batchSize;
  private double batchTime;
  private boolean isDone = false;
  private long lastTimeRemoved = System.currentTimeMillis();

  public ThreadPoolManager(int threadPoolSize, int batchSize, double batchTime, Server server) {
    this.taskQueue = new LinkedList<>();
    this.threadPool = new ArrayList<>(threadPoolSize);
    this.threadPoolSize = threadPoolSize;
    this.batchSize = batchSize;
    this.batchTime = batchTime;

    for (int i = 0; i < threadPoolSize; i++) {
      threadPool.add(new WorkerThread(this, server, taskQueue));
    }
  }

  private double getBatchTimeMilliseconds() {
    return batchTime * 1000;
  }

  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public synchronized void updateRemovedTimestamp() {
    lastTimeRemoved = System.currentTimeMillis();
  }

  public void addToTaskQueue(Task task) {
    // If the task list on the top of the queue is full, or batch-time has passed, notify a
    // Worker thread to pull off a job.
    synchronized (taskQueue) {

      if (taskQueue.isEmpty() || taskQueue.get(taskQueue.size() - 1).isFull()) {

        addTaskToNewBatch(task);
      }
      else {
        taskQueue.get(taskQueue.size() - 1).addTaskToBatch(task);


      }
      if (!taskQueue.isEmpty()) {
        if (taskQueue.get(0).isFull() || batchTimePassed()) {
          //System.err.println("In isFull() conditional");
          taskQueue.notify();
        }
      }
    }
  }

  /**
   * Checks to see if batchTime has passed since the last time a batch has been processed.
   * @return true if the batchTime has passed.
   */
  public boolean batchTimePassed() {
    long difference = System.currentTimeMillis() - this.lastTimeRemoved;
    return (difference >= getBatchTimeMilliseconds());
  }

  private void addTaskToNewBatch(Task task) {
    synchronized (taskQueue) {
      Batch newBatch = new Batch(batchSize);
      newBatch.addTaskToBatch(task);
      taskQueue.add(newBatch);
    }
  }

  /**
   * Removes and returns the batch at the head of the queue
   */
  public Batch removeBatchFromQueue() {
    synchronized (taskQueue) {
      if (taskQueue.size() > 0) {
        Batch removedBatch = taskQueue.remove(0);
        updateRemovedTimestamp();
        return removedBatch;
      }
      else return null;
    }
  }


  public List<Batch> getTaskQueue() {
    return this.taskQueue;
  }

  public synchronized boolean isDone() {
    return isDone;
  }

  // Sets all worker threads' isDone field to true
  public synchronized void setDone() {
    this.isDone = true;
    for (WorkerThread worker: threadPool) {
      worker.setDone();
    }
  }

  // Kicks off all worker threads
  public void startWorkerThreads() {
    for (WorkerThread worker: threadPool) {
      Thread thread = new Thread(worker, "Worker Thread");
      thread.start();
    }
  }

}
