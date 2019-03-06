package main.java.cs455.scaling.server;

public class Task {

  private byte[] taskBytes;
  private int taskLength;

  public Task(byte[] taskBytes, int taskLength) {
    this.taskLength = taskLength;
    this.taskBytes = taskBytes;
  }

  @Override
  public String toString() {
    return String.format("Task of length %d", taskLength);
  }

}
