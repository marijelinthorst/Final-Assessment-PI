package com.nedap.university.packets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.CRC32;

public class FileDealer {
  /// variables
  private boolean hasFilenameLength;
  private boolean hasFilename;
  private boolean hasFileLength;
  private boolean hasFile;
  
  private ConcurrentLinkedQueue<byte[]> dataQueue;
  
  
  // constructor
  public FileDealer () {
    this.dataQueue = new ConcurrentLinkedQueue<byte[]>();
  }
  
  // methods
  public synchronized void addToWritingQueue (byte[] data) {
    this.dataQueue.add(data);
  }
  
  public synchronized byte[] readFomWritingQueue () {
     return this.dataQueue.remove();
  }
  
  public void dothething() {
    if (allData.length != 512) {
      byte[] temp = Arrays.copyOf(allData, allData.length); 
      byte[] bytesToAdd = map.get(lastFrameReceived + 1);
      allData = Arrays.copyOf(temp, temp.length + bytesToAdd.length);
      System.arraycopy(bytesToAdd, 0, allData, temp.length, bytesToAdd.length);
    } else {
      allData = map.get(lastFrameReceived + 1);
    }
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
}
