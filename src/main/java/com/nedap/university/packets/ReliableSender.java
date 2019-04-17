package com.nedap.university.packets;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ReliableSender extends Thread {
  // file sending
  private String filename;
  private short filenumber = 0;
  private boolean downloading;
  
  // list sending
  private String[] fileNames;
  
  // Single packet sending
  private DatagramPacket singlePacket;
  private int singleSequenceNumber = 0;
  
  // general sending
  private boolean isFinished;
  private SendQueue sendQueue;
  boolean firstAckReceived = false;;
  
  // Sliding window sender variables
  private final static int SENDWINDOWSIZE = 10;
  private int firstFrameSeqNumber;
  private int lastAcknowledgeReceived;
  private int lastFrameSend;
  private int contentLength = 494;
  private FileDealer fileDealer;
  
  //TODO Path of a file 
  //private static final String HOME = System.getProperty("user.home");
  //private static final Path FOLDERPATH = Paths.get(HOME + "/Desktop/Sending/"); 

  /**
   *  constructors for each type of command
   */
  public ReliableSender(String filename, SendQueue sendQueue, short filenumber, boolean downloading, int sequenceNumber, Path folderPath) {
    this.filename = filename;
    this.filenumber = filenumber;
    this.sendQueue = sendQueue;
    this.downloading = downloading;
    this.firstFrameSeqNumber = sequenceNumber;
    fileDealer = new FileDealer(folderPath, filename, contentLength);
    fileDealer.start();
    lastAcknowledgeReceived = sequenceNumber - 3;
    
  }
  // what type does pi give?
  public ReliableSender(String[] fileNames, SendQueue sendQueue , int sequenceNumber) {
    this.fileNames = fileNames;
    this.sendQueue = sendQueue;
    this.singleSequenceNumber = sequenceNumber;
    
  }
  public ReliableSender(DatagramPacket packet, SendQueue sendQueue) {
    this.singlePacket = packet;
    this.sendQueue = sendQueue;
  }
  
  public ReliableSender(DatagramPacket packet, SendQueue sendQueue, int sequenceNumber) {
    this.singlePacket = packet;
    this.sendQueue = sendQueue;
    this.singleSequenceNumber = sequenceNumber;
  }
  
  /**
   *  run: directs to specific run
   */
  public void run() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      //continue;
    }
    if (filename != null) {
      this.runFileReliableTransfer();
    } else if (fileNames != null) {
      this.runListReliableTransfer();
    } else if (singleSequenceNumber != 0) {
      this.runPacketReliableTransfer();
    } else if (singlePacket != null) {
      this.runAckPacketReliableTransfer();
    } else {
      System.out.println("What?");
    }
  }

  public void runFileReliableTransfer() {
    
    //starting sequence number
    lastFrameSend = firstFrameSeqNumber;
    
    int i = 0;
    int totalNoOfPackets = fileDealer.getTotalNumberOfPackets();
    
    // start loop
    while (i <= totalNoOfPackets) { // since sendQueue takes care of retransmissions, if last packet is send then loop can stop
      // initialise packet with all flags in common
      Packet packet = new Packet(sendQueue.getAddress(), sendQueue.getPort());
      packet.setFileNumber(filenumber);
      packet.setSeqNumber(lastFrameSend + 1);
      if (downloading) {
        packet.setDownloadingFlag();
      } else {
        packet.setUploadingFlag();
      }
      
      // sliding window, start with sending down+syn+ack/up+syn and no content
      if (i == 0) { // start
        packet.setSynchronizeFlag();
        if (downloading) {
          packet.setAcknowlegdementFlag();
          packet.setAckNumber(lastFrameSend + 1);
        }
        sendQueue.addToQueue(packet.makePacket(), lastFrameSend + 1);
        
        System.out.println("SENDING PACKET NUMBER: " + i);
        lastFrameSend++;
        i++;
      } else if (lastFrameSend + 1 - this.getLastAcknowledgeReceived() < SENDWINDOWSIZE && firstAckReceived) { // it is in the window
        if (i == totalNoOfPackets) {
          packet.setFinalFlag();
          System.out.print("Sending last packet: ");
        }
        byte[] content = fileDealer.readFomWritingQueue();
        packet.setContent(content);
        sendQueue.addToQueue(packet.makePacket(), lastFrameSend + 1);
        
        System.out.println("SENDING PACKET NUMBER: " + i + " SEQ NO: " + (lastFrameSend + 1));
        lastFrameSend++;
        i++;
      } else { // all packets in window are send
        /** TODO if computer cannot handle this loop
        try {
          Thread.sleep(250);
          continue;
        } catch (InterruptedException e) {
        }
        */
      }
    }    
  }
  
  private synchronized int getLastAcknowledgeReceived() {
    return lastAcknowledgeReceived;
  }
  
  public synchronized void changeLastAcknowledgeReceived (int lastAcknowledgeReceived) {
    firstAckReceived = true;
    System.out.println("RECEIVED ACKNOWLEDGE NUMBER: " + lastAcknowledgeReceived);
    if (this.lastAcknowledgeReceived < lastAcknowledgeReceived) {
      this.lastAcknowledgeReceived = lastAcknowledgeReceived;
    } 
  }
  
  //-------------------------------------------------------------------------------
  public void runAckPacketReliableTransfer() {
    while (!isFinished) {
      sendQueue.addAcknowlegdementToQueue(singlePacket);
      isFinished = true;
    }
  }
  
  //-------------------------------------------------------------------------------
  public void runPacketReliableTransfer() {
    while (!isFinished) {
      sendQueue.addToQueue(singlePacket, singleSequenceNumber);
      isFinished = true;
    }
  }
  
  // -------------------------------------------------------------------------------
  public void runListReliableTransfer() {
    while (!isFinished) {
      Packet packet = new Packet(sendQueue.getAddress(), sendQueue.getPort());
      packet.setAvailableFilesListFlag();
      packet.setAcknowlegdementFlag();
      packet.setSynchronizeFlag();
      packet.setAckNumber(singleSequenceNumber);
      
      String toSend = "";
      for (String current: fileNames) {
        current = current + "\n";
        toSend = toSend + current;
      }
      byte[] namesToSend = toSend.getBytes();
      
      // set filenames length and filenames
      int namesToSendLength = namesToSend.length;
      byte[] namesToSendLengthBytes = ByteBuffer.allocate(4).putInt(namesToSendLength).array();
      byte[] content = new byte[namesToSend.length + namesToSendLengthBytes.length];     
      System.arraycopy(namesToSendLengthBytes, 0, content, 0, namesToSendLengthBytes.length);
      System.arraycopy(namesToSend, 0, content, namesToSendLengthBytes.length, namesToSend.length);
      packet.setContent(content);
      
      sendQueue.addToQueue(packet.makePacket(), singleSequenceNumber);
      isFinished = true;
    }
  }
  
  //-------------------------------------------------------------------------------
  public synchronized short returnFilenumber() {
    return filenumber;
  }
}
