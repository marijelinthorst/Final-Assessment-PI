package com.nedap.university.packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SendQueue extends Thread {
  private int queueLength = 1000;
  private BlockingQueue<DatagramPacket> queue;
  private DatagramSocket socket;

  public SendQueue(DatagramSocket socket) {
    queue = new ArrayBlockingQueue<DatagramPacket>(queueLength);
    this.socket = socket;
  }
 
  public void run() {
    while (true) {
      try {
        socket.send(queue.take());
      } catch (InterruptedException e) {
        System.out.println("ERROR: couldn't take packet from queue");
        e.printStackTrace();
      } catch (IOException e) {
        System.out.println("ERROR: couldn't send to: " + socket.getInetAddress() + ". Port: " + socket.getPort());
        e.printStackTrace();
      }
    }
  }
  
  public void addToQueue(DatagramPacket packet) {
    queue.add(packet);
  }
  
  

}
