package main.java.cs455.scaling.server;

public class Server {


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



  }

}
