package main.java.cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import main.java.cs455.scaling.hash.Hash;
import main.java.cs455.scaling.message.DataPacket;
import java.util.Random;

public class SenderThread implements Runnable {

  private Client client;
  private static ByteBuffer byteBuffer;
  private int messageRate; // Messages to send per second
  private Hash hasher; // Hasher instance
  private ClientStatistics clientStatistics;

  public SenderThread(Client client, ClientStatistics clientStatistics) {
    this.client = client;
    this.messageRate = client.getMessageRate();
    this.hasher = new Hash();
    byteBuffer = ByteBuffer.allocate(8000);
    this.clientStatistics = clientStatistics;
  }

  /**
   * Randomly generates a byte array of length 8 KB.
   * @return The randomly generated byte array.
   */
  public byte[] getMessageContents() {
    byte[] randomlyGenerateBytes = new byte[8000];
    new Random().nextBytes(randomlyGenerateBytes);
    return randomlyGenerateBytes;
  }

  public void run() {
    while (true) {

      try {
        // Randomly generate a 8KB byte[] and record its length
        byte[] messageContents = getMessageContents();
        //int length = messageContents.length;

        // Calculate hash of randomly generated bytes and add the resulting string to the
        // taskHashes ConcurrentHashMap.
        String hash = hasher.SHA1FromBytes(messageContents);
        client.addTaskHash(hash);
//        //System.out.printf("Incomplete Hash: \t%s\n", hash);
//        if (!client.isInHashMap(hash)) {
//          System.out.println("FALSCH");
//        }

        // Create a DataPacket message containing byte length and messageContents
        //DataPacket message = new DataPacket(length, messageContents);

        // Send the messageContents over the SocketChannel using the byteBuffer
        byteBuffer = ByteBuffer.wrap(messageContents);
        client.sendMessageToServer(byteBuffer);
        byteBuffer.clear();
        clientStatistics.incrementSentCount();
      } catch (IOException e) {
        System.err.println(e.getMessage());
      }

      // Sleep for 1/messageRate seconds
      long timeToSleep = 1000/this.messageRate;
      try {
        Thread.sleep(timeToSleep);
      } catch (InterruptedException e) {
        System.err.println(e.getMessage());
      }
    }


  }
}
