package main.java.cs455.scaling.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Task {

  private byte[] taskBytes;
  private int taskLength;
  private SelectionKey key;
  private String hash;
  private SocketChannel client;

  public Task(byte[] taskBytes, int taskLength, SocketChannel client) {
    this.taskLength = taskLength;
    this.taskBytes = taskBytes;
    this.client = client;
  }

  public byte[] getTaskBytes() {
    return taskBytes;
  }

  public int getTaskLength() {
    return taskLength;
  }

  public SocketChannel getClient() {
    return client;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Task)) return false;
    Task other = (Task) o;

    return Arrays.equals(this.taskBytes, other.getTaskBytes());
  }

  @Override
  public String toString() {
    return String.format("Task of length %d", taskLength);
  }

}
