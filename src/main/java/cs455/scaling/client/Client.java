package main.java.cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import main.java.cs455.scaling.hash.Hash;
import main.java.cs455.scaling.server.Server;
import java.util.List;
import java.util.LinkedList;
import main.java.cs455.scaling.server.Task;

public class Client {

  private LinkedList<String> taskHashes;
  private String serverHost;
  private int serverPort;
  private int messageRate;
  private Hash hasher;
  private SenderThread sender;
  private static SocketChannel client;
  private static ByteBuffer buffer;

  public Client() {
    this("denver", 5003, 2);
  }

  public Client(String serverHost, int serverPort, int messageRate) {
    this.serverHost = serverHost;
    this.messageRate = messageRate;
    this.serverPort = serverPort;
    this.taskHashes = new LinkedList<>();
    this.hasher = new Hash();
  }

  public String getServerHost() {
    return serverHost;
  }

  public int getServerPort() {
    return serverPort;
  }

  public int getMessageRate() {
    return messageRate;
  }

  /**
   * Establishes a connection to the server and create a buffer to read/write.
   * @throws IOException
   */
  public void establishServerConnection() throws IOException {
    // Connect to the server
    client = SocketChannel.open(new InetSocketAddress(this.serverHost, this.serverPort));

    // Create byte buffer
    buffer = ByteBuffer.allocate(256);
  }

  /**
   * Starts a sender thread to send messages. Reads any responses coming back from the server.
   * @throws IOException
   */
  public void doWork() throws IOException {
    buffer = ByteBuffer.wrap("PLEASE SEND THIS BACK TO ME.".getBytes());

    String response = null;

    client.write(buffer);
    buffer.clear();
    client.read(buffer); // blocking call

    response = new String(buffer.array()).trim();
    System.out.println("Server responded with: " + response);
    buffer.clear();
  }

  public void initializeSenderThread(Socket socket) {
    this.sender = new SenderThread(this, socket);
  }

  public synchronized void removeTaskHash(String hash) {
    taskHashes.remove(hash);
  }

  public synchronized void addTaskHash(String hash) {
    taskHashes.add(hash);
  }

  public void startSenderThread() {
    Thread senderThread = new Thread(sender, "Client Sender Thread");
    senderThread.start();
  }

  private static void printClientUsageMessage() {
    String message = "\nUsage:\n\njava main.java.cs455.scaling.client.Client <server-host> <server-port> <message-rate>\n";
    System.out.printf("%s\n\n", message);
  }

  public static void main(String[] args) {
    if (args.length != 3) {
      Client.printClientUsageMessage();
      System.exit(1);
    }

    // Parse args
    String serverHost = args[0];
    int serverPort = Integer.parseInt(args[1]);
    int messageRate = Integer.parseInt(args[2]); // Should be between 2 - 4

    Client client = new Client(serverHost, serverPort, messageRate);

    try {
      client.establishServerConnection();
    } catch (IOException e) {
      System.err.println("Unable to connect to the server...\n" + e.getMessage());
    }

    try {
      client.doWork();
    } catch (IOException e) {
      System.err.println("Unable to perform task...\n" + e.getMessage());
    }
  }

}
