package main.java.cs455.scaling.server;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServerTest {

  Server server;

  @Test
  public void testConstructor() {
    server = new Server(5003, 100, 20, 2);
    assertEquals(5003, server.getPortNum());
    assertEquals(100, server.getThreadPoolSize());
    assertEquals(20, server.getBatchSize());
    assertEquals(2, server.getBatchTime());
  }

}