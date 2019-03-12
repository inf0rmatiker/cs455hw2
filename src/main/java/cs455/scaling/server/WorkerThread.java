package main.java.cs455.scaling.server;

import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import main.java.cs455.scaling.hash.Hash;

public class WorkerThread implements Runnable {

  private boolean isDone = false;
  public List<Batch> taskQueue;
  public ThreadPoolManager manager;
  private Hash hasher;
  private Server server;

  public WorkerThread(ThreadPoolManager manager, Server server, List<Batch> taskQueue) {
    this.hasher = new Hash();
    this.taskQueue = taskQueue;
    this.manager = manager;
    this.server = server;
  }

  public void run() {
    while (!isDone) {
      Batch batchToProcess = null;
      synchronized (taskQueue) {
        try {
          taskQueue.wait();
          if (taskQueue.isEmpty()) {
            taskQueue.wait();
          }
          batchToProcess = manager.removeBatchFromQueue();
        } catch (InterruptedException e) {
          System.out.println(e.getMessage());
        }

      }

      // Complete (process) the tasks by hashing them.
      batchToProcess.processTasks();

      // Send all the completed tasks back to the clients
      server.sendTasksToClients(batchToProcess);
    }
  }

  /**
   * Checks and sees if 1. the queue is non-empty, 2. the queue head is full or batchTime has
   * passed
   */
  public boolean isBatchAvailable() {
    synchronized (taskQueue) {
      if (taskQueue.isEmpty()) {
        return false;
      }
      if ((taskQueue.get(0).isFull()) || manager.batchTimePassed()) {
        return true;
      } else {
        return false;
      }
    }
  }

  public synchronized void setDone() {
    this.isDone = true;
  }

  public synchronized boolean isDone() {
    return this.isDone;
  }

}
