package com.nedap.university.packets;

import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReliableReceiver extends Thread {
  // file receiving
  private short filenumber = 0;
  private SendQueue sendQueue;
  private boolean downloading;
  private ConcurrentLinkedQueue<byte[]> dataQueue;
  private ConcurrentLinkedQueue<Integer> sequenceNumberQueue;
  
  // list receiving
  private Packet packet;
   
  //sliding window receiver
  private final static int RECEIVEWINDOWSIZE = 20;
  private int lastFrameReceived;
  private int lastAcknowledgeSend;
  private HashMap<Integer, byte[]> map;
  private boolean isFinished = false;
  private boolean needToReceive = true;
 
  //TODO Path of a file 
  //private static final String HOME = System.getProperty("user.home");
  //static final Path FOLDERPATH = Paths.get(HOME + "/Desktop/Receiving/"); 
  private FileDealer fileDealer;
  
  
  /**
   *  constructors for each type of command
   */
  public ReliableReceiver(SendQueue sendQueue, short filenumber, boolean downloading, Path folderPath) {
    this.filenumber = filenumber;
    this.sendQueue = sendQueue;
    this.downloading = downloading;
    this.dataQueue = new ConcurrentLinkedQueue<byte[]>();
    this.sequenceNumberQueue = new ConcurrentLinkedQueue<Integer>();
    map = new HashMap<Integer, byte[]>();
    fileDealer = new FileDealer(folderPath);
    fileDealer.start();
  }
  // this is for receiving list
  public ReliableReceiver(SendQueue sendQueue, Packet packet) {
    this.sendQueue = sendQueue;
    this.packet = packet;
  }
 
  /**
   *  run: directs to specific run
   */
  public void run() {
    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
    }
    if (filenumber != 0) {
      this.runFileReliableTransfer();
    } else if (sendQueue != null) {
      this.runListReliableTransfer();
    } else {
      System.out.println("What?");
    }
  }

  private void runFileReliableTransfer() {
    int i = 0;
    while (needToReceive) {
      // receive the packet
      if (!dataQueue.isEmpty() && !sequenceNumberQueue.isEmpty()) {
        // get data and sequenceNumber
        byte[] data = dataQueue.remove();
        int sequenceNumber = this.sequenceNumberQueue.remove();
        
        // initialize return packet (acknowledge)
        Packet packet = new Packet(sendQueue.getAddress(), sendQueue.getPort());
        packet.setFileNumber(filenumber);
        packet.setAcknowlegdementFlag();
        if (downloading) {
          packet.setDownloadingFlag();
        } else {
          packet.setUploadingFlag();
        }
        
        if (i == 0) { // this is first packet (down+syn+ack/up+syn), get data and send down+ack/up+ack to SendingQueue
          // data
          lastFrameReceived = sequenceNumber;
          i++;
        } else if (sequenceNumber - lastFrameReceived < RECEIVEWINDOWSIZE && !map.containsKey(sequenceNumber)) { // packet in receiving window
          map.put(sequenceNumber, data);
          i++;
        }
        
        // whenever packet arrived, send acknowledge back with lastFrameReceived + 1 (= acknowledge number)
        lastAcknowledgeSend = lastFrameReceived + 1;
        packet.setAckNumber(lastAcknowledgeSend);
        System.out.println("SENDING ACK PACKET NUMBER: " + i + " ACK NO: " + lastAcknowledgeSend);
        sendQueue.addAcknowlegdementToQueue(packet.makePacket());
      }
      
      // arrange data  bytes in order by reading next sequence number from map
      if (!map.isEmpty() && map.containsKey(lastFrameReceived + 1)) {
        fileDealer.addToWritingQueue(map.get(lastFrameReceived + 1));
        lastFrameReceived++;
      } else if (isFinished) {
        Packet packet = new Packet(sendQueue.getAddress(), sendQueue.getPort());
        packet.setFileNumber(filenumber);
        packet.setAcknowlegdementFlag();
        if (downloading) {
          packet.setDownloadingFlag();
        } else {
          packet.setUploadingFlag();
        }
        lastAcknowledgeSend = lastFrameReceived + 1;
        packet.setAckNumber(lastAcknowledgeSend);
        System.out.println("SENDING ACK PACKET NUMBER: " + i + " ACK NO: " + lastAcknowledgeSend);
        sendQueue.addAcknowlegdementToQueue(packet.makePacket());
        needToReceive = false;
      }
    }  
  }
  
  public synchronized void addToReadingQueue (byte[] data, int sequenceNumber, boolean finished) {
   this.dataQueue.add(data);
   this.sequenceNumberQueue.add(sequenceNumber);
   this.isFinished = finished;
   if (finished) {
     System.out.print("Received last packet: ");
   }
  }
  
  //-------------------------------------------------------------------------------
  private void runListReliableTransfer() {
    // get filenames length
    byte[] content = packet.getContent();
    byte[] filenamesLengthBytes = new byte[4];
    System.arraycopy(content, 0, filenamesLengthBytes, 0, filenamesLengthBytes.length); 
    int filenamesLength = new BigInteger(filenamesLengthBytes).intValue();
    
    // get filenames
    byte[] filenameBytes = new byte[filenamesLength];
    System.arraycopy(content, filenamesLengthBytes.length, filenameBytes, 0, filenameBytes.length); 
    String filenames = new String(filenameBytes);
    
    System.out.println(filenames);
    
    Packet returnPacket = new Packet(sendQueue.getAddress(), sendQueue.getPort());
    returnPacket.setAvailableFilesListFlag();
    returnPacket.setAcknowlegdementFlag();
    returnPacket.setAckNumber(packet.getSeqNumber()+1);
    sendQueue.addAcknowlegdementToQueue(returnPacket.makePacket());
  }
  
  //-------------------------------------------------------------------------------
  public short returnFilenumber() {
    return filenumber;
  }
}
