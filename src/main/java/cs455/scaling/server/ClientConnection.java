package main.java.cs455.scaling.server;

import java.net.Socket;

public class ClientConnection {

  private int portNum;
  private String hostName;
  private Socket socket;

  public ClientConnection(int portNum, String hostName, Socket socket) {
    this.portNum = portNum;
    this.hostName = hostName;
    this.socket = socket;
  }

  public void setPortNum(int portNum) {
    this.portNum = portNum;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof  ClientConnection)) return false;

    ClientConnection other = (ClientConnection) o;
    return other.hostName.equals(this.hostName) && other.portNum == this.portNum;
  }

  @Override
  public String toString() {
    return String.format("ClientConnection with hostName %s, portNum %d", hostName, portNum);
  }
}
