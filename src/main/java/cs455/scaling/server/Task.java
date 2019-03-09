package main.java.cs455.scaling.server;

import java.nio.channels.SelectionKey;

public class Task {

  private byte[] taskBytes;
  private int taskLength;
  private SelectionKey key;
  private String hash;

  public Task(byte[] taskBytes, int taskLength, SelectionKey key) {
    this.taskLength = taskLength;
    this.taskBytes = taskBytes;
    this.key = key;
  }

  public byte[] getTaskBytes() {
    return taskBytes;
  }

  public int getTaskLength() {
    return taskLength;
  }

  public SelectionKey getKey() {
    return key;
  }

  public String getHash() {
    return hash;
  }

  public boolean isComplete() {
    return hash != null;
  }

  public void setTaskHash(String hash) {
    this.hash = hash;
  }

  @Override
  public String toString() {
    return String.format("Task of length %d", taskLength);
  }

}
