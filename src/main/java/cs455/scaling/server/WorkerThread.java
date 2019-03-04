package main.java.cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;

public class WorkerThread implements Runnable {

  private boolean isDone = false;
  private LinkedBlockingQueue<Task> taskQueue; // Shared queue between worker threads and threadpool manager

  public WorkerThread(LinkedBlockingQueue<Task> taskQueue) {
    this.taskQueue = taskQueue;
  }

  public void run() {
    while (!isDone) {

    }
  }

  public synchronized void setDone() {
    this.isDone = true;
  }

  public synchronized boolean isDone() {
    return this.isDone;
  }

}
