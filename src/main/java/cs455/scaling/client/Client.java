package main.java.cs455.scaling.client;

import main.java.cs455.scaling.server.Server;

public class Client {


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

  }

}
