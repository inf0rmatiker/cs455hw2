package main.java.cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import main.java.cs455.scaling.message.DataPacket;

public class Server {

  private int portNum, threadPoolSize, batchSize, batchTime;
  private ThreadPoolManager threadPoolManager;
  private Map<String, ClientConnection> clientConnections;

  public Server(int portNum, int threadPoolSize, int batchSize, int batchTime) {
    this.portNum = portNum;
    this.threadPoolSize = threadPoolSize;
    this.batchSize = batchSize;
    this.batchTime = batchTime;
    this.threadPoolManager = new ThreadPoolManager(threadPoolSize);
  }

  /**
   * Initializes a (selectable, non-blocking) ServerSocketChannel, registers it with the Selector
   * object, and listens for incoming activity on the Selector object. Once activity has been
   * flagged, iterates over the set of flagged activity and handles each case appropriately.
   * @throws IOException
   */
  public void initializeServerAndListenForConnections() throws IOException {
    // Open a Selector
    Selector selector = Selector.open();

    // Create server's input channel
    ServerSocketChannel serverSocket = ServerSocketChannel.open();
    serverSocket.bind(new InetSocketAddress(this.getHostName(), this.portNum));
    serverSocket.configureBlocking(false); // We don't want it to block

    // Register our serverSocket to the selector
    SelectionKey serverSocketKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);

    // Loop on selector, listening for available I/O on our selectable channels.
    while (true) {
      System.out.println("Listening for new messages or connections...");

      // Block until there is new activity
      selector.select();
      System.out.println("Activity on selector!\n");

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
          registerConnection(selector, serverSocket);
        }

        // If the key is for an OP_READ channel, read the data/respond
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
   * @param selector The multiplexer for selectable channels
   * @param serverSocketChannel The ServerSocketChannel which contains the incoming connection
   * @throws IOException
   */
  public void registerConnection(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
    // Grab incoming socket from the serverSocketChannel
    SocketChannel client = serverSocketChannel.accept();

    // Configure client to be a selectable channel and register it with the selector
    client.configureBlocking(false);
    client.register(selector, SelectionKey.OP_READ);
    System.out.println("\t\tNew client registered\n");
  }

  /**
   * Reads and handles data on an incoming selectable SocketChannel.
   * @param selectionKey The SelectionKey object associated with the SocketChannel
   * @throws IOException
   */
  public void readAndQueue(SelectionKey selectionKey) throws IOException {
    // Initialize a buffer to read data
    ByteBuffer buffer = ByteBuffer.allocate(8004);

    // Grab SocketChannel from the key
    SocketChannel client = (SocketChannel) selectionKey.channel();
    // Read from it
    int bytesRead = client.read(buffer);

    // Handle a closed connection
    if (bytesRead == -1) {
      client.close();
    }
    else {
      System.out.printf("\t\tReceived %d bytes.\n\n", bytesRead);

      // Construct a DataPacket from the buffer byte array.
      byte[] totalMessageBytes = buffer.array();
      DataPacket packet = new DataPacket(totalMessageBytes);

      // Construct a new Task from the fields held in the DataPacket.
      Task task = new Task(packet.getTotalMessageBytes(), packet.getLength());

      System.out.println(task);
      //this.threadPoolManager.addToTaskQueue(task);

      // Clear the buffer so we can read another item
      buffer.clear();
    }

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
