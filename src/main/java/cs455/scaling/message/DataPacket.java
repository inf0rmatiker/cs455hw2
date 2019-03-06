package main.java.cs455.scaling.message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataPacket {

  private int length;
  private byte[] packetBytes;
  private byte[] totalMessageBytes;

  public DataPacket(int length, byte[] packetBytes) throws IOException {

    this.length = length;
    this.packetBytes = packetBytes;
    this.packBytes();
  }

  public DataPacket(byte[] totalMessageBytes) throws IOException {
    this.totalMessageBytes = totalMessageBytes;
    this.unpackBytes();
  }

  public byte[] getPacketBytes() {
    return packetBytes;
  }

  public byte[] getTotalMessageBytes() { return totalMessageBytes; }

  public int getLength() { return length; }

  public void unpackBytes() throws IOException {
    ByteArrayInputStream baInputStream = new ByteArrayInputStream(this.totalMessageBytes);
    DataInputStream dataInput = new DataInputStream(new BufferedInputStream(baInputStream));

    // Read length of packetBytes[]
    this.length = dataInput.readInt();

    // Read packetBytes
    this.packetBytes = new byte[this.length];
    dataInput.readFully(this.packetBytes, 0, this.length);

    // Close the streams
    baInputStream.close();
    dataInput.close();
  }

  public void packBytes() throws IOException {
    ByteArrayOutputStream baOuputStream = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baOuputStream));

    // Write length
    dataOut.writeInt(length);

    // Write packetBytes
    dataOut.write(packetBytes);

    dataOut.flush();
    this.totalMessageBytes = baOuputStream.toByteArray();

    // Close the streams
    baOuputStream.close();
    dataOut.close();
  }

  @Override
  public String toString() {
    return String.format("Length: %d\n\tPacketBytes: %s\n",
        this.length, new String(this.packetBytes));
  }

}
