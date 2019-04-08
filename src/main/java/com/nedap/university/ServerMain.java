package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerMain {
  private static DatagramSocket socket;
  private static InetAddress clientAddress;
  private static int clientPort;
  private static int serverPort = 8080;
  
  // state
  private boolean isFinished = true;
  private int packetlength = 512;
  
  // package
  private PackageMaker maker;
  private PackageReader reader;
  private SendQueue queue;

  public static void main(String[] args) {
    
    // receive broadcast message from client
    byte[] buffer = new byte[512];
    DatagramPacket bufferPacket = new DatagramPacket(buffer, buffer.length);
    
    try {
      socket = new DatagramSocket();
      socket.setBroadcast(true);
      socket.receive(bufferPacket);
    } catch (IOException e) {
      System.out.println("ERROR: couldn't send or receive broadcast message!");
      e.printStackTrace();
    }
    
    // get info from response packet from the server, data is useless at this point
    clientAddress = bufferPacket.getAddress();
    clientPort = bufferPacket.getPort(); //TODO: does this read des or source port?
    
    // send response + ack and wait for ack
    String responseMessage = "Hello + Ack"; // TODO: misschien eigen header hier voor port en ack
    byte[] response = responseMessage.getBytes();
    DatagramPacket responsePacket = new DatagramPacket(response, response.length, clientAddress, clientPort);
    try {
      socket.send(responsePacket);
      socket.receive(bufferPacket);
      if (new String(bufferPacket.getData()).equals("Ack")) {
        ServerMain server = new ServerMain();
        server.startClientInputLoop();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * constructor
   */
  private ServerMain() {
    maker = new PackageMaker(serverPort);
    reader = new PackageReader();
    queue = new SendQueue();
    queue.start();
  }

  //---------------- Client input ---------------------
  private void startClientInputLoop() {
    while (!isFinished ) {
      try {
        byte[] buffer = new byte[packetlength];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivePacket);
        reader.readPackage(receivePacket);
      } catch (IOException e) {
        System.out.println("Sorry, cannot reach client");
        this.shutdown();
      }
    }
    
  }

  //---------------- shutdown ---------------------
  private void shutdown() {
    // TODO Auto-generated method stub
    
  }
}
