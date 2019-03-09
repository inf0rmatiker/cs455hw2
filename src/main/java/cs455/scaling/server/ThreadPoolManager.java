package main.java.cs455.scaling.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager {

  public List<WorkerThread> threadPool; // List of WorkerThreads which are available
  public List<List<Task>> taskQueue;
  private int threadPoolSize;
  public int batchSize;
  private int batchTime;
  private boolean isDone = false;
  private long lastTimeRemoved = System.currentTimeMillis();

  public ThreadPoolManager() {
    this(10, 10, 10, null);
  }

  public ThreadPoolManager(int threadPoolSize, int batchSize, int batchTime, Server server) {
    this.taskQueue = new LinkedList<>();
    this.threadPool = new ArrayList<>(threadPoolSize);
    this.threadPoolSize = threadPoolSize;
    this.batchSize = batchSize;
    this.batchTime = batchTime;

    for (int i = 0; i < threadPoolSize; i++) {
      threadPool.add(new WorkerThread(this, server, taskQueue));
    }
  }

  private int getBatchTimeMilliseconds() {
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
      if ((!taskQueue.isEmpty()) && (isFull(taskQueue.get(0)) || batchTimePassed())) {

        // Get the batch on the tail of the queue
        List<Task> lastBatch = taskQueue.get(taskQueue.size() - 1);

        // If the batch on the tail of the queue is full, create a new one
        if (isFull(lastBatch)) {
          addTaskToNewBatch(task);
        }
        else { // Otherwise, add the task to the non-full lastBatch
          lastBatch.add(task);
        }

        // Notify a WorkerThread to remove a batch from the head of the queue
        System.out.println("Notifying a worker");
        taskQueue.notify();
      }
      else if (taskQueue.isEmpty()) { // If the taskQueue has no batches in it, just add the task to a new batch
        List<Task> newBatch = new LinkedList<>();
        newBatch.add(task);
        taskQueue.add(newBatch);
      }
      else { // The taskQueue is not empty, and the head isn't full, and batchTime hasn't passed.
        List<Task> lastBatch = taskQueue.get(taskQueue.size() - 1);
        lastBatch.add(task);
      }
    }
  }

  public boolean isFull(List<Task> batch) {
    return batch.size() == this.batchSize;
  }

  public boolean batchTimePassed() {
    long difference = System.currentTimeMillis() - this.lastTimeRemoved;
    if (difference >= getBatchTimeMilliseconds()) {
      System.out.println("Batch time has passed!");
    }
    return (difference >= getBatchTimeMilliseconds());
    //return false;
  }

  private void addTaskToNewBatch(Task task) {
    synchronized (taskQueue) {
      List<Task> newBatch = new LinkedList<>();
      newBatch.add(task);
      taskQueue.add(newBatch);
    }
  }

  public List<List<Task>> getTaskQueue() {
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
