package main.java.cs455.scaling.server;

import java.util.LinkedList;
import java.util.List;
import main.java.cs455.scaling.hash.Hash;

public class Batch {

  private final int capacity;
  private List<Task> tasks;
  private Hash hasher;

  public Batch(int capacity) {
    this.capacity = capacity;
    this.tasks = new LinkedList<>();
    this.hasher = new Hash();
  }

  // ACHTUNG! This method returns tasks by REFERENCE, and is NOT thread-safe.
  // The only time this should be used is AFTER all tasks have been completed and are ready to
  // be sent back to their respective clients.
  public List<Task> getTasks() {
    return this.tasks;
  }

  public int getCapacity() {
    return this.capacity;
  }

  public synchronized void addTaskToBatch(Task task) {
    tasks.add(task);
  }

  public synchronized boolean isFull() {
    if (tasks.size() > capacity) {
      System.err.println("How did you manage to overflow your batch, you absolute moron?");
    }
    return tasks.size() == capacity;
  }

  public synchronized int size() {
    return tasks.size();
  }

  public synchronized void processTasks() {
    for (Task task : tasks) {
      String hash = hasher.SHA1FromBytes(task.getTaskBytes());
      task.setTaskHash(hash);
    }
  }

  // Sagt "Richtig", nur wann this.tasks "task" hat. Andererseits sagt es "Falsch".
  public synchronized boolean batchContains(Task other) {
    for (Task task: tasks) {
      if (task.equals(other)) {
        return true;
      }
    }
    return false;
  }


}
