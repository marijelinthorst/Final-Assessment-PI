package com.nedap.university.packets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.CRC32;

public class ReliableReceiver extends Thread {
  // file receiving
  private short filenumber = 0;
  private SendQueue sendQueue;
  private boolean downloading;
  private ConcurrentLinkedQueue<byte[]> dataQueue;
  private ConcurrentLinkedQueue<Integer> sequenceNumberQueue;
  
  // list receiving
  private List<?> list;
   
  //sliding window receiver
  private final static int RECEIVEWINDOWSIZE = 20;
  private int lastFrameReceived;
  private int lastAcknowledgeSend;
  private HashMap<Integer, byte[]> map;
  private byte[] allData;
  private boolean isFinished = false;
  private boolean needToReceive = true;
 
  //TODO Path of a file 
  static final Path FILEPATH = Paths.get("/Users/marije.linthorst/Desktop/Receiving/"); 
  
  /**
   *  constructors for each type of command
   */
  public ReliableReceiver(SendQueue sendQueue, short filenumber, boolean downloading) {
    this.filenumber = filenumber;
    this.sendQueue = sendQueue;
    this.downloading = downloading;
    this.dataQueue = new ConcurrentLinkedQueue<byte[]>();
    this.sequenceNumberQueue = new ConcurrentLinkedQueue<Integer>();
    map = new HashMap<Integer, byte[]>();
  }
  // this is for receiving list
  public ReliableReceiver(SendQueue sendQueue, Packet packet) {
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
      if (!dataQueue.isEmpty()) {
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
        }
        
        // whenever packet arrived, send acknowledge back with lastFrameReceived + 1 (= acknowledge number)
        lastAcknowledgeSend = lastFrameReceived + 1;
        packet.setAckNumber(lastAcknowledgeSend);
        sendQueue.addAcknowlegdementToQueue(packet.makePacket());
      }
      
      // arrange data  bytes in order by reading next sequence number from map
      if (!map.isEmpty() && map.containsKey(lastFrameReceived + 1)) {
        byte[] temp = Arrays.copyOf(allData, allData.length); 
        byte[] bytesToAdd = map.get(lastFrameReceived + 1);
        allData = Arrays.copyOf(temp, temp.length + bytesToAdd.length);
        System.arraycopy(bytesToAdd, 0, allData, temp.length, bytesToAdd.length);
        lastFrameReceived++;
      } else if (isFinished) {
        needToReceive = false;
        this.writeByteToFile(allData);
      }
    }  
  }
  
  public synchronized void addToReadingQueue (byte[] data, int sequenceNumber, boolean finished) {
   this.dataQueue.add(data);
   this.sequenceNumberQueue.add(sequenceNumber);
   this.isFinished = finished;
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
        File file = FILEPATH.resolve(filename).toFile();
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
  public short returnFilenumber() {
    return filenumber;
  }
}
