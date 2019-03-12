package main.java.cs455.scaling.server;

import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerStatistics implements Runnable {
  private int clientConnections;
  private int messagesProcessed;
  private long lastTimePrinted;
  private ConcurrentHashMap<SocketChannel, Integer> throughput;

  public ServerStatistics () {
    this.clientConnections = 0;
    this.messagesProcessed = 0;
    throughput = new ConcurrentHashMap<>();
    this.lastTimePrinted = System.currentTimeMillis();
  }

  public synchronized void incrementClientConnections() {
    //System.out.println("Incrementing client connections");
    this.clientConnections++;
  }

  private synchronized int getClientConnections() {
    return clientConnections;
  }

  private synchronized int getMessagesProcessed() {
    return messagesProcessed;
  }

  public synchronized void incrementMessagesProcessed(int amount) {
    this.messagesProcessed += amount;
  }

  private synchronized void resetMessagesProcessed() {
    this.messagesProcessed = 0;
  }

  private void resetLastTimePrinted() {
    this.lastTimePrinted = System.currentTimeMillis();
  }

  public synchronized void incrementClientMessage(SocketChannel clientChannel) {
    if (!throughput.containsKey(clientChannel)) {
      throughput.put(clientChannel, 1);
    }
    else {
      int incrementedValue = throughput.get(clientChannel) + 1;
      throughput.replace(clientChannel, incrementedValue);
    }
  }

  public synchronized void addClientToThroughput(SocketChannel clientChannel) {
    throughput.putIfAbsent(clientChannel, 0);
  }

  private synchronized void resetClientMessages() {
    throughput.forEach((socketChannel, value) -> {throughput.replace(socketChannel, 0);});
  }

  private synchronized double getMeanClientThroughput() {
    if (getClientConnections() == 0) {
      return 0;
    }

    int totalSum = 0;
    for (SocketChannel entry: throughput.keySet()) {
      totalSum += throughput.get(entry);
    }
    //System.out.println(totalSum);
    return (1.0*totalSum) / getClientConnections();
  }

  private synchronized double getStandardDeviationForThroughput() {
    if (getClientConnections() == 0) {
      return 0.0;
    }

    double mean = getMeanClientThroughput();

    double differencesSum = 0;
    for (SocketChannel entry: throughput.keySet()) {
      differencesSum += Math.pow(throughput.get(entry) - mean, 2);
    }
    differencesSum /= getClientConnections();

    return Math.sqrt(differencesSum);
  }

  @Override
  public String toString() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();
    synchronized (this) {
      return String.format("%s Server Throughput: %d messages/s, Active Client Connections: %d, Mean Per-\n"
              + "client Throughput: %.2f messages/s, Std. Dev. Of Per-client Throughput: %.2f messages/s\n",
          dateFormat.format(date).toString(), getMessagesProcessed() / 20, getClientConnections(), getMeanClientThroughput() / 20.0,
          getStandardDeviationForThroughput());
    }
  }

  @Override
  public void run() {
    while (true) {
      // TODO: Change to 20 seconds instead of 2 seconds
      if (System.currentTimeMillis() >= (lastTimePrinted + 20000)) {
        System.out.println(this);


        resetMessagesProcessed();
        resetClientMessages();
        resetLastTimePrinted();
      }
    }
  }


}
