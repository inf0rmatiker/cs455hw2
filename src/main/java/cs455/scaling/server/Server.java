package main.java.cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.java.cs455.scaling.message.DataPacket;

public class Server {

  private int portNum, threadPoolSize, batchSize, batchTime;
  private ThreadPoolManager threadPoolManager;
  private Selector selector;
  private Object completedTasksLock;

  public Server(int portNum, int threadPoolSize, int batchSize, int batchTime) {
    this.portNum = portNum;
    this.threadPoolSize = threadPoolSize;
    this.batchSize = batchSize;
    this.batchTime = batchTime;
    this.completedTasksLock = new Object();
    this.threadPoolManager = new ThreadPoolManager(threadPoolSize, batchSize, batchTime, this);
  }

  /**
   * Initializes a (selectable, non-blocking) ServerSocketChannel, registers it with the Selector
   * object, and listens for incoming activity on the Selector object. Once activity has been
   * flagged, iterates over the set of flagged activity and handles each case appropriately.
   */
  public void initializeServerAndListenForConnections() throws IOException {

    this.startThreadPool();

    // Open a Selector
    selector = Selector.open();

    // Create server's input channel
    ServerSocketChannel serverSocket = ServerSocketChannel.open();
    serverSocket.bind(new InetSocketAddress(this.getHostName(), this.portNum));
    serverSocket.configureBlocking(false); // We don't want it to block

    // Register our serverSocket to the selector
    SelectionKey serverSocketKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);

    // Loop on selector, listening for available I/O on our selectable channels.
    while (true) {
      // Block until there is new activity
      selector.select();
      System.out.println("Activity on server.");

      // Set of key(s) that are ready
      Set<SelectionKey> selectedKeys = selector.selectedKeys();

      // Loop over ready keys
      Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
      while (keyIterator.hasNext()) {
        // Grab current key
        SelectionKey key = keyIterator.next();

        // Verify key validity
        if (!key.isValid()) {
          continue;
        }

        // If the key is for an OP_ACCEPT channel, register it with selector
        if (key.isAcceptable()) {
          registerConnection(serverSocket);
        }

        // If the key is for an OP_READ channel, read the data
        if (key.isReadable()) {
          readAndQueue(key);
        }

        // Remove it from set of ready keys, we have already processed it.
        keyIterator.remove();
      }

    }
  }

  public String getHostName() throws IOException {
    return InetAddress.getLocalHost().getHostName();
  }

  /**
   * Registers a selectable socket with the selector object once a connection has been established.
   *
   * @param serverSocketChannel The ServerSocketChannel which contains the incoming connection
   */
  public void registerConnection(ServerSocketChannel serverSocketChannel) throws IOException {
    // Grab incoming socket from the serverSocketChannel
    SocketChannel client = serverSocketChannel.accept();
    //ByteBuffer buffer = ByteBuffer.allocate(8000);

    // Configure client to be a selectable channel and register it with the selector
    client.configureBlocking(false);
    client.register(selector, SelectionKey.OP_READ);
    System.out.println("\t\tNew client registered\n");
  }

  /**
   * Reads and handles data on an incoming selectable SocketChannel.
   *
   * @param selectionKey The SelectionKey object associated with the SocketChannel
   */
  public void readAndQueue(SelectionKey selectionKey) throws IOException {
    //ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
    ByteBuffer buffer = ByteBuffer.allocate(8000);

    // Grab SocketChannel from the key
    SocketChannel client = (SocketChannel) selectionKey.channel();
    int read = 0;

    // Handle a closed connection
    while (buffer.hasRemaining() && read != -1) {
      read = client.read(buffer);
    }

    if (read == -1) {
      client.close();
    } else {
      //System.out.printf("\t\tReceived %d bytes.\n\n", bytesRead);

      // Construct a DataPacket from the buffer byte array.
      byte[] totalMessageBytes = buffer.array();

      // Construct a new Task from the fields held in the DataPacket.
      Task task = new Task(totalMessageBytes, totalMessageBytes.length, selectionKey);

      this.threadPoolManager.addToTaskQueue(task);

      // Clear the buffer so we can read another item
      buffer.clear();
    }
  }

  /**
   * Sends the completed tasks in a batch back to the original client
   */
  public void sendTasksToClients(List<Task> batch) {
    for (Task task : batch) {
      if (task.isComplete()) {
        System.out.println("Task Complete...");
        SelectionKey key = task.getKey();
        SocketChannel clientChannel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.wrap(task.getHash().getBytes());
        while (buffer.hasRemaining()) {
          try {
            clientChannel.write(buffer);
          } catch (IOException e) {
            System.err.println("UH OH");
            System.err.println(e.getMessage());
          }

        }
        buffer.clear();
      }
    }
  }

  public void startThreadPool() {
    threadPoolManager.startWorkerThreads();
  }

  public int getPortNum() {
    return portNum;
  }

  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public int getBatchTime() {
    return batchTime;
  }

  /**
   * Prints a generic usage message of what arguments to use to run the Server class.
   */
  private static void printServerUsageMessage() {
    String message = "\nUsage:\n\njava main.java.cs455.scaling.server.Server <portnum> <thread-pool-size> <batch-size> <batch-time>\n";
    System.out.printf("%s\n\n", message);
  }

  public static void main(String[] args) {
    if (args.length != 4) {
      Server.printServerUsageMessage();
      System.exit(1);
    }

    // Parse command line args
    int portNum = Integer.parseInt(args[0]);
    int threadPoolSize = Integer.parseInt(args[1]);
    int batchSize = Integer.parseInt(args[2]);
    int batchTime = Integer.parseInt(args[3]);

    Server server = new Server(portNum, threadPoolSize, batchSize, batchTime);

    try {
      server.initializeServerAndListenForConnections();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

}
