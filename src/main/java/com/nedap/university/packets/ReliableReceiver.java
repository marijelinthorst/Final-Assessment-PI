package com.nedap.university.packets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.CRC32;

public class ReliableReceiver extends Thread {
  private short filenumber = 0;
  private SendQueue sendQueue;
  private boolean downloading;
  private List<?> list;
  private DatagramPacket packet;
  private ConcurrentLinkedQueue<byte[]> dataQueue;
  private ConcurrentLinkedQueue<Integer> sequenceNumberQueue; 
  
  //sliding window receiver
  private final static int RECEIVEWINDOWSIZE = 20;
  private int lastFrameReceived;
  private int lastAcknowledgeSend;
  private int firstFrameSeqNumber;
  private HashMap<Integer, byte[]> map;
  private int contentLength = 494;
  private byte[] allData;
 
  //TODO Path of a file 
  static final String FILEPATH = ""; 
  
  /**
   *  constructors for each type of command
   */
  public ReliableReceiver(SendQueue sendQueue, short filenumber, boolean downloading) {
    this.filenumber = filenumber;
    this.sendQueue = sendQueue;
    this.downloading = downloading;
    this.dataQueue = new ConcurrentLinkedQueue<byte[]>();
    this.sequenceNumberQueue = new ConcurrentLinkedQueue<Integer>();
  }
  // what type does pi give?
  public ReliableReceiver(SendQueue sendQueue) {
    this.sendQueue = sendQueue;
    this.dataQueue = new ConcurrentLinkedQueue<byte[]>();
    this.sequenceNumberQueue = new ConcurrentLinkedQueue<Integer>();
  }
  public ReliableReceiver(DatagramPacket packet, SendQueue sendQueue) {
    this.packet = packet;
    this.sendQueue = sendQueue;
    this.dataQueue = new ConcurrentLinkedQueue<byte[]>();
    this.sequenceNumberQueue = new ConcurrentLinkedQueue<Integer>();
  }
  
  /**
   *  run: directs to specific run
   */
  public void run() {
    if (filenumber != 0) {
      this.runFileReliableTransfer();
    } else if (packet != null) {
      this.runPacketReliableTransfer();
    } else if (sendQueue != null) {
      this.runListReliableTransfer();
    } else {
      System.out.println("What?");
    }
  }

  private void runFileReliableTransfer() {
    int i = 0;
    while (true) {
      if (!dataQueue.isEmpty()) {
        byte[] data = dataQueue.remove();
        int sequenceNumber = this.sequenceNumberQueue.remove();
        
        Packet packet = new Packet(sendQueue.getAddress(), sendQueue.getPort());
        packet.setFileNumber(filenumber);
        if (downloading) {
          packet.setDownloadingFlag();
        } else {
          packet.setUploadingFlag();
        }
        
        if (i == 0) { // this is first packet
          lastFrameReceived = sequenceNumber;
          firstFrameSeqNumber = sequenceNumber;
          int acknowledgeNumber = sequenceNumber +1;
          i++;
        } else if (sequenceNumber - lastFrameReceived < RECEIVEWINDOWSIZE) {
          map.put(sequenceNumber, data);
        }
        
        if (!map.isEmpty() && map.containsKey(lastFrameReceived + 1)) {
          byte[] temp = Arrays.copyOf(allData, allData.length); 
          byte[] bytesToAdd = map.get(lastFrameReceived + 1);
          allData = Arrays.copyOf(temp, temp.length + bytesToAdd.length);
          System.arraycopy(bytesToAdd, 0, allData, temp.length, bytesToAdd.length);
          lastFrameReceived++;
          i++;
          // TODO: hier gebleven, alle waardes aanpassen en ack terug sturen
          // System.out.println("");
          // lastFrameReceived alleen veranderen als ie van buffer naar byte[] gaat
        } 
        // altijd ack sturen
      }  
    }  
  }
  
  public synchronized void addToReadingQueue (byte[] data, int sequenceNumber) {
   this.dataQueue.add(data);
   this.sequenceNumberQueue.add(sequenceNumber);
  }
  
  //-------------------------- file reader ---------------------------- 
  private void writeByteToFile (byte[] bytes) { 
    // bytes = filenameLengthBytes[4] + filenameBytes + checksumBytes[4] + fileLengthBytes[4] + fileBytes
    
    // get filename length
    byte[] filenameLengthBytes = new byte[4];
    System.arraycopy(bytes, 0, filenameLengthBytes, 0, filenameLengthBytes.length); 
    int filenameLength = new BigInteger(filenameLengthBytes).intValue();
    
    // get filename
    byte[] filenameBytes = new byte[filenameLength];
    System.arraycopy(bytes, filenameLengthBytes.length, filenameBytes, 0, filenameBytes.length); 
    String filename = new String(filenameBytes);
    
    // get checksum
    byte[] checksumBytes = new byte[4];
    System.arraycopy(bytes, filenameLengthBytes.length + filenameBytes.length, checksumBytes, 0, checksumBytes.length); 
    
    // get file length
    byte[] fileLengthBytes = new byte[4];
    System.arraycopy(bytes, filenameLengthBytes.length + filenameBytes.length + checksumBytes.length, fileLengthBytes, 0, fileLengthBytes.length);
    int fileLength = new BigInteger(fileLengthBytes).intValue();
    
    // get fileBytes
    byte[] fileBytes = new byte[fileLength];
    System.arraycopy(bytes, filenameLengthBytes.length + filenameBytes.length + checksumBytes.length + fileLengthBytes.length, fileBytes, 0, fileBytes.length);
    
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
    byte[] checksumBytesToCompare = new byte[4];
    System.arraycopy(tooLong,4,checksumBytesToCompare,0,checksumBytesToCompare.length);
    
    
    if (Arrays.equals(checksumBytes, checksumBytesToCompare)) {
      try { 
        File file = new File(FILEPATH + filename);
        // Initialise a pointer in file using OutputStream 
        OutputStream os = new FileOutputStream(file); 

        // Starts writing the bytes in it 
        os.write(fileBytes); 
        System.out.println("Successfully made file"); 

        // Close the file 
        os.close(); 
      } catch (Exception e) { 
        System.out.println("Exception: " + e); 
      } 
    } else {
      System.out.println("File checksum incorrect"); 
    } 
  }
  
  //-------------------------------------------------------------------------------
  private void runListReliableTransfer() {
    // TODO Auto-generated method stub
    
  }
  
  //-------------------------------------------------------------------------------
  private void runPacketReliableTransfer() {
    // TODO Auto-generated method stub
    
  }
  
  //-------------------------------------------------------------------------------
  public short returnFilenumber() {
    return filenumber;
  }
}
