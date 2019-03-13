package main.java.cs455.scaling.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientStatistics implements Runnable {

  private int receivedCount;
  private int sentCount;
  private long lastTimePrinted;

  public ClientStatistics() {
    this.receivedCount = 0;
    this.sentCount = 0;
    this.lastTimePrinted = System.currentTimeMillis();
  }

  private synchronized int getReceivedCount() {
    return this.receivedCount;
  }

  private synchronized int getSentCount() {
    return this.sentCount;
  }

  public synchronized void incrementReceiveCount() {
    this.receivedCount++;
  }

  public synchronized void incrementSentCount() {
    this.sentCount++;
  }

  private void resetLastTimePrinted() {
    this.lastTimePrinted = System.currentTimeMillis();
  }

  private synchronized void resetSentCount() {
    this.sentCount = 0;
  }

  private synchronized void resetReceivedCount() {
    this.receivedCount = 0;
  }

  @Override
  public String toString() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();
    return String.format("%s Total Sent Count: %d, Total Received Count: %d\n",
        dateFormat.format(date).toString(), getSentCount(), getReceivedCount());
  }


  @Override
  public void run() {
    while (true) {
      synchronized (this) {
        if (System.currentTimeMillis() >= lastTimePrinted + 20000) {
          System.out.println(this);

          resetSentCount();
          resetReceivedCount();
          resetLastTimePrinted();
        }
      }
    }

  }

}
