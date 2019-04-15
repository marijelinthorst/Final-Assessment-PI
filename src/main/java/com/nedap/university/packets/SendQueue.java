package com.nedap.university.packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SendQueue extends Thread {
  private ConcurrentLinkedQueue<DatagramPacket> queue;
  private DatagramSocket socket;
  private long delay = 5000; //time before retransmission in milliseconds 
  private HashMap<Integer,Timer> timers;

  public SendQueue(DatagramSocket socket) {
    this.timers = new HashMap<Integer,Timer>();
    this.queue = new ConcurrentLinkedQueue<DatagramPacket>();
    this.socket = socket;
  }
 
  public void run() {
    while (true) {
      try {
        if (!queue.isEmpty()) {
          System.out.println("Sending");
          socket.send(queue.remove());
        }  
      } catch (IOException e) {
        System.out.println("ERROR: couldn't send to: " + socket.getInetAddress() + ". Port: " + socket.getPort());
        e.printStackTrace();
      }
    }
  }
  
  public void addToQueue(DatagramPacket packet, int sequenceNumber) {
    Timer timer = new Timer();
    timer.schedule(this.getTask(packet, sequenceNumber), delay);
    timers.put(sequenceNumber, timer);
    queue.add(packet);
  }
  
  private TimerTask getTask(DatagramPacket packet, int sequenceNumber) {
    return new TimerTask(){
      @Override
      public void run() { 
        if (queue.contains(packet)) {
          timers.remove(sequenceNumber);
          Timer timer = new Timer();
          timer.schedule(getTask(packet, sequenceNumber), delay);
          timers.put(sequenceNumber, timer);
        } else {
          addToQueue(packet, sequenceNumber);
        }
      }
    };
  }
  
  public synchronized void stopTimeout(int sequenceNumber) {
    Timer thisTimer = timers.get(sequenceNumber);
    thisTimer.cancel();
    timers.remove(sequenceNumber);
  }
  
  public InetAddress getAddress() {
    return socket.getInetAddress();
  }
  
  public int getPort() {
    return socket.getPort();
  }

}
