package main.java.cs455.scaling.message;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataPacket {

  private int portNum;
  private String hostName;
  private int length;
  private byte[] packetBytes;
  private byte[] totalMessageBytes;

  public DataPacket(int portNum, String hostName, int length, byte[] packetBytes) throws IOException {
    this.portNum = portNum;
    this.hostName = hostName;
    this.length = length;
    this.packetBytes = packetBytes;
    this.packBytes();
  }

  public DataPacket(byte[] packetBytes) throws IOException {
    this.packetBytes = packetBytes;
    this.unpackBytes();
  }

  public byte[] getPacketBytes() {
    return packetBytes;
  }

  public int getLength() {
    return length;
  }

  public int getPortNum() {
    return portNum;
  }

  public String getHostName() {
    return hostName;
  }

  public void unpackBytes() throws IOException {

  }

  public void packBytes() throws IOException {
    ByteArrayOutputStream baOuputStream = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOuputStream));

    // Write portnum
    dataOut.writeInt(portNum);

    // Write hostname
    marshallString(hostName, dataOut);

    // Write length
    dataOut.writeInt(length);

    // Write packetBytes
    dataOut.write(packetBytes);

    dataOut.flush();
    this.totalMessageBytes = baOuputStream.toByteArray();

    baOuputStream.close();
    dataOut.close();
  }

  private void marshallString(String stringToMarshall, DataOutputStream dataOut) throws IOException {
    dataOut.writeInt(stringToMarshall.length());
    byte[] stringBytes = stringToMarshall.getBytes();
    dataOut.write(stringBytes);
  }

}
