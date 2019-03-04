package main.java.cs455.scaling.client;

import java.net.Socket;
import java.nio.ByteBuffer;

public class SenderThread implements Runnable {

  private Socket socket;
  private Client client;

  public SenderThread(Client client, Socket socket) {
    this.client = client;
    this.socket = socket;
  }

  public void run() {

  }
}
