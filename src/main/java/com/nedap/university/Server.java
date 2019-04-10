package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.nedap.university.packets.PacketDealer;
import com.nedap.university.packets.SendQueue;

public class Server {
  private static DatagramSocket socket;
  private static InetAddress clientAddress;
  private static int clientPort = 9090;
  private static int serverPort = 8080;
  
  // state
  private boolean isFinished = true;
  private int packetlength = 512;
  
  // package
  //private Packet maker;
  private PacketDealer dealer;
  private SendQueue queue;

  public static void main(String[] args) {
    System.out.println("Start server");
    
    // receive broadcast message from client
    byte[] buffer = new byte[512];
    DatagramPacket bufferPacket = new DatagramPacket(buffer, buffer.length);    
    try {
      socket = new DatagramSocket(serverPort);
      socket.setBroadcast(true);
      System.out.println("Server created socket");
      socket.receive(bufferPacket);
      System.out.println("Server received broadcast");
    } catch (IOException e) {
      System.out.println("ERROR: couldn't send or receive broadcast message!");
      e.printStackTrace();
    }
    
    // get info from response packet from the server
    clientAddress = bufferPacket.getAddress();
    clientPort = bufferPacket.getPort(); 
    
    // send response + ack and wait for ack 
    String responseMessage = "SYN + ACK";
    byte[] response = responseMessage.getBytes();
    DatagramPacket responsePacket = new DatagramPacket(response, response.length, clientAddress, clientPort);
    try {
      socket.send(responsePacket);
      System.out.println("Server send syn+ack");
      socket.receive(bufferPacket);
      System.out.println("Server received message");  
      Server server = new Server();
      System.out.println("Server constructed");
      server.startClientInputLoop();
    } catch (IOException e) {
      System.out.println("ERROR: couldn't send SYN + ACK message!");
      e.printStackTrace();
    }
  }
  
  /**
   * constructor
   */
  public Server() {
    //maker = new Packet(clientAddress, clientPort);
    dealer = new PacketDealer();
    queue = new SendQueue(socket);
    queue.start();
  }

  //---------------- Client input ---------------------
  public void startClientInputLoop() {
    while (!isFinished ) {
      try {
        byte[] buffer = new byte[packetlength];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivePacket);
        dealer.readPackage(receivePacket);
      } catch (IOException e) {
        System.out.println("Sorry, cannot reach client");
        this.shutdown();
      }
    }
  }

  //---------------- shutdown ---------------------
  private void shutdown() {
    this.isFinished = true;
    // TODO Auto-generated method stub
    
  }
}
