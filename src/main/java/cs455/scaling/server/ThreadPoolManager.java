package main.java.cs455.scaling.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages WorkerThreads by assigning incoming tasks to process to available workers.
 */
public class ThreadPoolManager {

  public List<WorkerThread> threadPool; // List of WorkerThreads which are available
  public List<Batch> taskQueue;         // Queue of Batches containing tasks to be processed
  private int batchSize;
  private double batchTime;
  private long lastTimeRemoved;         // Timestamp in milliseconds of the last batch processed

  public ThreadPoolManager(int threadPoolSize, int batchSize, double batchTime, Server server) {
    this.taskQueue = new LinkedList<>();
    this.threadPool = new ArrayList<>(threadPoolSize);
    this.batchSize = batchSize;
    this.batchTime = batchTime;

    for (int i = 0; i < threadPoolSize; i++) {
      threadPool.add(new WorkerThread(this, server, taskQueue));
    }

    this.lastTimeRemoved = System.currentTimeMillis();
  }

  /**
   * Converts the double batchTime, specified in seconds, to milliseconds.
   * @return the batchTime in milliseconds
   */
  private double getBatchTimeMilliseconds() {
    return batchTime * 1000;
  }

  /**
   * Updates the lastTimeRemoved field to the current timestamp.
   */
  public synchronized void updateRemovedTimestamp() {
    lastTimeRemoved = System.currentTimeMillis();
  }

  /**
   * Adds the given Task to a Batch in the taskQueue, and notifies a WorkerThread to process a Batch
   * if there is a full Batch on the queue or batchTime has expired since last Batch processed.
   * @param task Task to add to a taskQueue Batch.
   */
  public void addToTaskQueue(Task task) {
    synchronized (taskQueue) {
      if (taskQueue.isEmpty() || taskQueue.get(taskQueue.size() - 1).isFull()) {
        addTaskToNewBatch(task);
      }
      else {
        taskQueue.get(taskQueue.size() - 1).addTaskToBatch(task);
      }

      if (!taskQueue.isEmpty()) {
        if (taskQueue.get(0).isFull() || batchTimePassed()) {
          // Notify an available WorkerThread to process a Batch
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

  /**
   * Creates a new Batch and adds it to the taskQueue.
   * @param task The initial Task object to put into the new Batch
   */
  private void addTaskToNewBatch(Task task) {
    synchronized (taskQueue) {
      Batch newBatch = new Batch(batchSize);
      newBatch.addTaskToBatch(task);
      taskQueue.add(newBatch);
    }
  }

  /**
   * Removes and returns the batch at the head of the queue if the queue is non-empty.
   * @return the Batch on the head of the taskQueue
   */
  public Batch removeBatchFromQueue() {
    synchronized (taskQueue) {
      if (!taskQueue.isEmpty()) {
        Batch removedBatch = taskQueue.remove(0);
        updateRemovedTimestamp();
        return removedBatch;
      }
      else return null;
    }
  }

}
