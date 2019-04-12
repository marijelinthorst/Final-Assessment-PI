package com.nedap.university.packets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ReliableSender extends Thread {
  private File file;
  private int filenumber;
  private DatagramPacket packet;
  private List<?> list;
  private boolean isFinished;
  private SendQueue sendQueue;
  
  // Sliding window sender
  private final static int SENDWINDOWSIZE = 10;
  private int firstFrameSeqNumber;
  private int lastAcknowledgeReceived;
  private int lastFrameSend;
  private int contentLength = 494;

  /**
   *  constructors for each type of command
   */
  public ReliableSender(File file, SendQueue sendQueue, int filenumber) {
    this.file = file;
    this.filenumber = filenumber;
    this.sendQueue = sendQueue;
  }
  // what type does pi give?
  public ReliableSender(List<?> list, SendQueue sendQueue) {
    this.list = list;
  }
  public ReliableSender(DatagramPacket packet, SendQueue sendQueue) {
    this.packet = packet;
  }
  
  /**
   *  run: directs to specific run
   */
  public void run() {
    if (file != null) {
      this.runFileReliableTransfer();
    } else if (packet != null) {
      this.runPacketReliableTransfer();
    } else if (list != null) {
      this.runListReliableTransfer();
    } else {
      // receive file or list?
      System.out.println("What?");
    }
  }

  public void runFileReliableTransfer() {
    byte[] fileBytes = this.readFileToByte(file);
    
    // make random starting sequence number
    Random random = new Random(); 
    firstFrameSeqNumber = random.nextInt();
    lastFrameSend = firstFrameSeqNumber - 1;
    
    // calculate window, stop when fin is reached
    // start sending window
    // window empty --> isFinished = true;
    while (!isFinished) {
      
    }
    
    int i = 0;
    int totalNoOfPackets = fileBytes.length/contentLength;
    
    
    
    
    while (i <= totalNoOfPackets) {
      if (i == 0) {
        Packet packet = new Packet(sendQueue.getAddress(), sendQueue.getPort());
        packet.setDownloadingFlag();
        packet.setSynchronizeFlag();
        packet.setAcknowlegdementFlag();
        packet.setFileNumber(filenumber);
        packet.setSeqNumber(lastFrameSend + 1);
        sendQueue.addToQueue(packet.makePacket());
        i++;
      } else if (lastFrameSend + 1 - lastAcknowledgeReceived < SENDWINDOWSIZE) {
        Packet packet = new Packet(sendQueue.getAddress(), sendQueue.getPort());
        packet.setDownloadingFlag();
        if (i == totalNoOfPackets) {
          packet.setFinalFlag();
        }
        packet.setFileNumber(filenumber);
        packet.setSeqNumber(lastFrameSend + 1);
        byte[] content = Arrays.copyOfRange(fileBytes, i-1, i-1+contentLength);
        packet.setContent(content);
        sendQueue.addToQueue(packet.makePacket());
      } else {
        
      }
    }
    
    
  }
  
  //-------------------------- file writer / reader ----------------------------
  // Method which write bytes into a file 
  public void writeByteToFile(byte[] bytes, File file) { 
    try { 
      // Initialize a pointer in file using OutputStream 
      OutputStream os = new FileOutputStream(file); 

      // Starts writing the bytes in it 
      os.write(bytes); 
      System.out.println("Successfully byte inserted"); 

      // Close the file 
      os.close(); 
    } catch (Exception e) { 
      System.out.println("Exception: " + e); 
    } 
 }
 
  //Method which write a file into bytes 
  public byte[] readFileToByte (File file) {
    byte[] fileContent = null;
    try {
      fileContent = Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      System.out.println("Exception: " + e);
      e.printStackTrace();
    }
    return fileContent;
  }
  
  //-------------------------------------------------------------------------------
  public void runPacketReliableTransfer() {
    while (!isFinished) {
      // TODO
    }
  }
  
  // -------------------------------------------------------------------------------
  public void runListReliableTransfer() {
    while (!isFinished) {
      // TODO
    }
  }
  
  //-------------------------------------------------------------------------------
  public short returnFilenumber() {
    return filenumber;
  }
}
