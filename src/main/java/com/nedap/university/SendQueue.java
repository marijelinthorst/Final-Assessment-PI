package com.nedap.university;

import java.net.DatagramPacket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SendQueue extends Thread {
  private int queueLength = 1000;
  private BlockingQueue<DatagramPacket> queue;

  public SendQueue() {
    queue = new ArrayBlockingQueue<DatagramPacket>(queueLength);
  }
 
  public void run() {
    while (true) {
      
    }
  }
  
  public void addToQueue(DatagramPacket packet) {
    queue.add(packet);
  }
  
  

}
