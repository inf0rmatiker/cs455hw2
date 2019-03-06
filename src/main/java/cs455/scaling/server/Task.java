package main.java.cs455.scaling.server;

import java.nio.channels.SelectionKey;

public class Task {

  private byte[] taskBytes;
  private int taskLength;
  private SelectionKey key;

  public Task(byte[] taskBytes, int taskLength, SelectionKey key) {
    this.taskLength = taskLength;
    this.taskBytes = taskBytes;
    this.key = key;
  }

  @Override
  public String toString() {
    return String.format("Task of length %d", taskLength);
  }

}
