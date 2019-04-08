package com.nedap.university;

import java.net.DatagramPacket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SendQueue extends Thread {
  private int queueLength = 1000;
  private BlockingQueue<DatagramPacket> queue;
  
  public void run() {
    SendQueue sendQueue = new SendQueue();
    sendQueue.startQueue();
  }

  public SendQueue() {
    queue = new ArrayBlockingQueue<DatagramPacket>(queueLength);
  }
  
  private void startQueue() {
    while (true) {
      
    }
    // TODO Auto-generated method stub
    
  }
  
  public void addToQueue(DatagramPacket packet) {
    queue.add(packet);
  }
  
  

}
