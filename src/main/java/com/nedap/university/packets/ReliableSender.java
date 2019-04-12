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
  private int filenumber;
  private DatagramPacket packet;
  private List<?> list;
  private boolean isFinished;
  private SendQueue sendQueue;
  
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
  public ReliableSender(String filename, SendQueue sendQueue, int filenumber) {
    this.filename = filename;
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
    if (filename != null) {
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
    byte[] fileBytes = this.readFileToByte(filename);
    
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
  public int returnFilenumber() {
    return filenumber;
  }
}
