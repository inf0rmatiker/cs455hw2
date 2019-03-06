package main.java.cs455.scaling.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager {

  private List<WorkerThread> threadPool;
  private LinkedBlockingQueue<Task> taskQueue;
  private int threadPoolSize;
  private boolean isDone = false;

  public ThreadPoolManager() {
    this(10);
  }

  public ThreadPoolManager(int threadPoolSize) {
    this.taskQueue = new LinkedBlockingQueue<>();
    this.threadPool = new ArrayList<>(threadPoolSize);
    this.threadPoolSize = threadPoolSize;

    for (int i = 0; i < threadPoolSize; i++) {
      threadPool.add(new WorkerThread(taskQueue));
    }
  }

  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public synchronized void addToTaskQueue(Task task) {

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
