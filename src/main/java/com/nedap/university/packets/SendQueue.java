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
  private long delay = 10000; //time before retransmission in milliseconds 
  private HashMap<Integer,TimerTask> timers;
  private InetAddress address;
  private int port;
  private Timer timer;

  public SendQueue(DatagramSocket socket, InetAddress address, int port) {
    this.timers = new HashMap<Integer,TimerTask>();
    this.queue = new ConcurrentLinkedQueue<DatagramPacket>();
    this.socket = socket;
    this.address = address;
    this.port = port;
    timer = new Timer();
  }
 
  public void run() {
    while (true) {
      try {
        if (!queue.isEmpty()) {
          //System.out.println("Sending");
          socket.send(queue.remove());
        }  
      } catch (IOException e) {
        System.out.println("ERROR: couldn't send to: " + socket.getInetAddress() + ". Port: " + socket.getPort());
        e.printStackTrace();
      }
    }
  }
  
  public void addToQueue(DatagramPacket packet, int sequenceNumber) {
    TimerTask task = this.getTask(packet, sequenceNumber);
    timer.schedule(task, delay);
    timers.put(sequenceNumber, task);
    queue.add(packet);
  }
  
  public void addAcknowlegdementToQueue(DatagramPacket packet) {
    queue.add(packet);
  }
  
  private TimerTask getTask(DatagramPacket packet, int sequenceNumber) {
    return new TimerTask(){
      @Override
      public void run() { 
        if (queue.contains(packet)) {
          TimerTask thisTimer = timers.get(sequenceNumber);
          thisTimer.cancel();
          timers.remove(sequenceNumber);
          
          TimerTask task = getTask(packet, sequenceNumber);
          timer.schedule(task, delay);
          timers.put(sequenceNumber, task);
        } else {
          addToQueue(packet, sequenceNumber);
        }
      }
    };
  }
  
  public synchronized void stopTimeout(int sequenceNumber) {
    TimerTask thisTimer = timers.get(sequenceNumber);
    thisTimer.cancel();
    timers.remove(sequenceNumber);
  }
  
  public InetAddress getAddress() {
    return address;
  }
  
  public int getPort() {
    return port;
  }

}
