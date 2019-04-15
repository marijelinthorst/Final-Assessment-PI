package com.nedap.university.packets;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;

public class ReliableSender extends Thread {
  private File file;
  private String filename;
  private short filenumber = 0;
  private DatagramPacket packet;
  private List<?> list;
  private boolean isFinished;
  private SendQueue sendQueue;
  private boolean downloading;
  
  // Sliding window sender variables
  private final static int SENDWINDOWSIZE = 10;
  private int firstFrameSeqNumber;
  private int lastAcknowledgeReceived;
  private int lastFrameSend;
  private int contentLength = 494;
  
  //TODO Path of a file 
  static final String FILEPATH = ""; 

  /**
   *  constructors for each type of command
   */
  public ReliableSender(String filename, SendQueue sendQueue, short filenumber, boolean downloading) {
    this.filename = filename;
    this.filenumber = filenumber;
    this.sendQueue = sendQueue;
    this.downloading = downloading;
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
    if (filename != null) {
      this.runFileReliableTransfer();
    } else if (packet != null) {
      this.runPacketReliableTransfer();
    } else if (list != null) {
      this.runListReliableTransfer();
    } else {
      System.out.println("What?");
    }
  }

  public void runFileReliableTransfer() {
    // get bytes to send
    byte[] fileBytes = this.readFileToByte(filename);
    
    // make random starting sequence number
    Random random = new Random(); 
    firstFrameSeqNumber = random.nextInt();
    lastFrameSend = firstFrameSeqNumber - 1;
    
    int i = 0;
    int totalNoOfPackets = fileBytes.length/contentLength;
    
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
      
      // sliding window, start with sending syn + ack and no content
      if (i == 0) { // start
        packet.setSynchronizeFlag();
        packet.setAcknowlegdementFlag();
        sendQueue.addToQueue(packet.makePacket(), lastFrameSend + 1);
        
        System.out.println("SENDING PACKET NUMBER: " + i);
        lastFrameSend++;
        i++;
      } else if (lastFrameSend + 1 - this.getLAR() < SENDWINDOWSIZE) { // it is in the window
        if (i == totalNoOfPackets) {
          packet.setFinalFlag();
        }
        byte[] content = Arrays.copyOfRange(fileBytes, (i-1)*contentLength, i*contentLength-1);
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
  
  private synchronized int getLAR () {
    return lastAcknowledgeReceived;
  }
  
  public synchronized void changeLAR (int ack) {
    lastAcknowledgeReceived = ack; 
  }
  
  //-------------------------- file reader ----------------------------
  private byte[] readFileToByte (String filename) {
    // filename and length to byte[]
    byte[] filenameBytes = filename.getBytes();
    int filenameLength = filenameBytes.length;
    byte[] filenameLengthBytes = ByteBuffer.allocate(4).putInt(filenameLength).array();
    
    // write file content and length to byte[]
    this.file = new File(FILEPATH + filename);
    byte[] fileBytes = null;
    try {
      fileBytes = Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      System.out.println("Exception: " + e);
      e.printStackTrace();
    }
    int fileLength = fileBytes.length;
    byte[] fileLengthBytes = ByteBuffer.allocate(4).putInt(fileLength).array();
    
    // calculate checksum and put into 4 bytes
    // dataWithoutChecksum = filenameLengthBytes + filenameBytes + fileLengthBytes + fileBytes
    byte[] dataWithoutChecksum = new byte[filenameLengthBytes.length + filenameBytes.length + fileLengthBytes.length + fileBytes.length];
    System.arraycopy(filenameLengthBytes, 0, dataWithoutChecksum, 0, filenameLengthBytes.length); 
    System.arraycopy(filenameBytes, 0, dataWithoutChecksum, filenameLengthBytes.length, filenameBytes.length); 
    System.arraycopy(fileLengthBytes, 0, dataWithoutChecksum, filenameLengthBytes.length + filenameBytes.length, fileLengthBytes.length);
    System.arraycopy(fileBytes, 0, dataWithoutChecksum, filenameLengthBytes.length + filenameBytes.length + fileLengthBytes.length, filenameBytes.length);
    
    CRC32 crc = new CRC32();
    crc.update(dataWithoutChecksum);
    long longChecksum = crc.getValue();
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(longChecksum);
    byte[] tooLong =  buffer.array();
    byte[] checksumBytes = new byte[4];
    System.arraycopy(tooLong,4,checksumBytes,0,checksumBytes.length);
    
    // bytesToSend = filenameLengthBytes + filenameBytes + checksumBytes + fileLengthBytes + fileBytes
    byte [] bytesToSend = new byte[filenameLengthBytes.length + filenameBytes.length + checksumBytes.length + fileLengthBytes.length + fileBytes.length]; 
    System.arraycopy(filenameLengthBytes, 0, bytesToSend, 0, filenameLengthBytes.length); 
    System.arraycopy(filenameBytes, 0, bytesToSend, filenameLengthBytes.length, filenameBytes.length);
    System.arraycopy(checksumBytes, 0, bytesToSend, filenameLengthBytes.length + filenameBytes.length, checksumBytes.length); 
    System.arraycopy(fileLengthBytes, 0, bytesToSend, filenameLengthBytes.length + filenameBytes.length + checksumBytes.length, fileLengthBytes.length);
    System.arraycopy(fileBytes, 0, bytesToSend, filenameLengthBytes.length + filenameBytes.length + checksumBytes.length + fileLengthBytes.length, filenameBytes.length);
    
    return bytesToSend;
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
