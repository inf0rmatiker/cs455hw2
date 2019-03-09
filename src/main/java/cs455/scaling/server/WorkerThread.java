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
  public List<List<Task>> taskQueue;
  public ThreadPoolManager manager;
  public List<List<Task>> completedTasks;
  private Hash hasher;
  private Server server;

  public WorkerThread(ThreadPoolManager manager, Server server, List<List<Task>> taskQueue) {
    this.hasher = new Hash();
    this.taskQueue = taskQueue;
    this.manager = manager;
    this.completedTasks = completedTasks;
    this.server = server;
  }

  public void run() {
    while (!isDone) {
      List<Task> batchToProcess = null;
      synchronized (taskQueue) {
        try {
          taskQueue.wait();
        } catch (InterruptedException e) {
          System.out.println(e.getMessage());
        }
        batchToProcess = removeBatchFromQueue();
      }

      for (Task task : batchToProcess) {
        String hash = hasher.SHA1FromBytes(task.getTaskBytes());
        if (batchToProcess.size() < manager.batchSize) {
          System.out.println(hash);

        }
        task.setTaskHash(hash);
      }

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
      if (manager.isFull(taskQueue.get(0)) || manager.batchTimePassed()) {
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Removes and returns the batch at the head of the queue
   */
  private List<Task> removeBatchFromQueue() {
    synchronized (taskQueue) {
      manager.updateRemovedTimestamp();
      return taskQueue.remove(0);
    }
  }



  public synchronized void setDone() {
    this.isDone = true;
  }

  public synchronized boolean isDone() {
    return this.isDone;
  }

}
