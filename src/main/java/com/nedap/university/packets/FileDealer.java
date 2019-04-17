package com.nedap.university.packets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.CRC32;

public class FileDealer extends Thread {
  /// variables
  private boolean hasFilenameLength = false;
  private boolean hasFilename = false;
  private boolean hasChecksum = false;
  private boolean hasFileLength = false;
  private boolean hasFile = false;
  private boolean canDetermineNOPackets = false;
  
  private ConcurrentLinkedQueue<byte[]> dataQueue;
  
  // header parts
  private byte[] filenameLengthBytes;
  private int filenameLength;
  private byte[] filenameBytes;
  private String filename;
  private byte[] checksumBytes;
  private byte[] fileLengthBytes;
  private int fileLength;
  private byte[] buffer;
  private int bufferPointer;
  
  // other
  private CRC32 crc;
  private File file;
  private OutputStream os;
  private Path folderPath;
  private int contentLength;
  
  // constructor
  public FileDealer (Path folderPath) {
    this.dataQueue = new ConcurrentLinkedQueue<byte[]>();
    this.filenameLengthBytes = new byte[4];
    this.checksumBytes = new byte[4];
    this.fileLengthBytes = new byte[4];
    this.crc = new CRC32();
    this.folderPath = folderPath;
  }
  
  public FileDealer (Path folderPath, String filename, int contentLength) {
    this.dataQueue = new ConcurrentLinkedQueue<byte[]>();
    this.filename = filename;
    this.contentLength = contentLength;
    this.buffer = new byte[contentLength];
    this.bufferPointer = 0;
    this.crc = new CRC32();
    this.file = folderPath.resolve(filename).toFile();
    
    
    // nodig?
    this.filenameLengthBytes = new byte[4];
    this.checksumBytes = new byte[4];
    this.fileLengthBytes = new byte[4];
  }
  
  public void run() {
    if (filename == null) {
      this.runFileWriter();
    } else {
      this.runFileReader();
    }
  }
  
  //-------------------------- file writer ----------------------------
  public void runFileWriter() {
    while (!hasFile) {
      if (!hasFilenameLength) {
        if (!dataQueue.isEmpty()) {
          buffer = readFomWritingQueue();
          this.filenameLengthBytes = Arrays.copyOf(buffer, filenameLengthBytes.length);
          bufferPointer = 4;
          
          // add to checksum
          crc.update(filenameLengthBytes);
          
          this.filenameLength = new BigInteger(filenameLengthBytes).intValue();
          hasFilenameLength = true;  
        }
      } else if (!hasFilename) {
        filenameBytes = new byte[filenameLength];
        for (int i = 0; i < filenameLength; i++) {
          if (bufferPointer < buffer.length) {
            filenameBytes[i] = buffer[bufferPointer];
            bufferPointer++;
          } else {
            if (!dataQueue.isEmpty()) {
              buffer = readFomWritingQueue(); 
              bufferPointer = 0;
              i--;
            }
          } 
        }
        // add to checksum
        crc.update(filenameBytes);
        
        filename = new String(filenameBytes);
        hasFilename = true;
      } else if (!hasChecksum) {
        for (int i = 0; i < checksumBytes.length; i++) {
          if (bufferPointer < buffer.length) {
            checksumBytes[i] = buffer[bufferPointer];
            bufferPointer++;
          } else {
            if (!dataQueue.isEmpty()) {
              buffer = readFomWritingQueue(); 
              bufferPointer = 0;
              i--;
            }
          } 
        } 
        hasChecksum = true;
      } else if (!hasFileLength) {
        for (int i = 0; i < fileLengthBytes.length; i++) {
          if (bufferPointer < buffer.length) {
            fileLengthBytes[i] = buffer[bufferPointer];
            bufferPointer++;
          } else {
            if (!dataQueue.isEmpty()) {
              buffer = readFomWritingQueue(); 
              bufferPointer = 0;
              i--;
            }
          } 
        }
        // add to checksum
        crc.update(fileLengthBytes);
        
        fileLength = new BigInteger(fileLengthBytes).intValue();
        hasFileLength = true;
      } else {
        // open file
        try {
          file = folderPath.resolve(filename).toFile();
          os = new FileOutputStream(file);
        } catch (FileNotFoundException e){
          System.out.println("ERROR: File not found " + e);
        }
        
        // write to file
        byte [] bytesToWrite = new byte[buffer.length-bufferPointer];
        int i = 0;
        int copyEnd = Math.min(buffer.length, bufferPointer + fileLength);
       
        while (i < fileLength) {
          int bytesRequested = Math.min(buffer.length, fileLength - i);
          copyEnd = Math.min(buffer.length, bufferPointer+bytesRequested);
          
          bytesToWrite = Arrays.copyOfRange(buffer, bufferPointer, copyEnd);
          i += copyEnd - bufferPointer;
          
         // System.out.println("i = " + i + " ; copyEnd = " + copyEnd + " ; bytesRequested = " + bytesRequested + 
           //   " ; bufferPointer = " + bufferPointer + ";buffer length =  " + buffer.length );
          this.writeBytesToFile(bytesToWrite);
          bufferPointer = copyEnd;
          
          if (!dataQueue.isEmpty()) {
            buffer = readFomWritingQueue(); 
            bufferPointer = 0;
          }          
        }
        
        this.writeBytesToFile(bytesToWrite);
        // Close the file 
        try {
          //os.flush();
          os.close();
        } catch (IOException e) {
          System.out.println("ERROR: " + e);
        }
        hasFile = true;
      } 
    }
    
    // has now the whole file, check checksum
    if (Arrays.equals(checksumBytes, this.getChecksum())) {
      System.out.println("Successfully made file");
    } else {
      System.out.println("Unsuccessfully made file: file checksum incorrect");
    }
  }
   
  private void writeBytesToFile (byte[] bytes) { 
    // update checksum
    crc.update(bytes);
    
    //Write the bytes in file
      try { 
        os.write(bytes);  
      } catch (Exception e) { 
        System.out.println("Exception: " + e); 
      } 
  }
  
  
  
  //-------------------------- file writer ---------------------------- 
  public void runFileReader() {
    // get bytes of all parts except file and checksum
    filenameBytes = filename.getBytes();
    filenameLength = filenameBytes.length;
    filenameLengthBytes = ByteBuffer.allocate(4).putInt(filenameLength).array();
    try {
      fileLength = (int) Files.size(file.toPath());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    fileLengthBytes = ByteBuffer.allocate(4).putInt(fileLength).array();
    
    
    // add first two parts to packets and add them to crc
    this.addBytesToPackets(filenameLengthBytes);
    this.addBytesToPackets(filenameBytes);
    crc.update(filenameLengthBytes);
    crc.update(filenameBytes);
    
    // add parts after checksum to crc
    crc.update(fileLengthBytes);
    updateFileChecksum(file, crc);
    
    // get checksum, add checksum and file length to packets
    checksumBytes = this.getChecksum(); 
    this.addBytesToPackets(checksumBytes);    
    this.addBytesToPackets(fileLengthBytes);

    canDetermineNOPackets = true;
    readFileToPackets();
  }
  
  public void addBytesToPackets (byte[] bytes) {
    for (int i = 0; i < bytes.length; i++) {
      if (bufferPointer < buffer.length) {
        buffer[bufferPointer] = bytes[i];
        bufferPointer++;
        if (bufferPointer == buffer.length) {
          this.addToWritingQueue(buffer);
          buffer = new byte[contentLength];
          bufferPointer = 0;
        }
      } else {
        this.addToWritingQueue(buffer);
        buffer = new byte[contentLength];
        bufferPointer = 0;
        i--;
      }
    }
  }
  
  public static void updateFileChecksum (File file, CRC32 crc) {
    InputStream inputStream;
    try {
        inputStream = new FileInputStream(file);
        int filePart;
        while ((filePart = inputStream.read()) != -1) {
            crc.update(filePart);
        }
    } catch (FileNotFoundException e) {
      System.out.println("ERROR: " + e);
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("ERROR: " + e);
      e.printStackTrace();
    }
}
  
  private void readFileToPackets () {
    try {
    
      BufferedInputStream inputStream;
      inputStream = new BufferedInputStream(new FileInputStream(file));
      
      int bytesRead = 0;
      int toRead = contentLength - bufferPointer;
      byte[] rawDataPart = new byte[toRead];
      
      while ((bytesRead = inputStream.read(rawDataPart)) >= 0) {
        byte[] toWrite = Arrays.copyOf(rawDataPart, bytesRead);
        addBytesToPackets(toWrite);      
        toRead = contentLength - bufferPointer;
        rawDataPart = new byte[toRead];
      }
      
      this.addToWritingQueue(buffer);
      inputStream.close();
      
    } catch (FileNotFoundException e) {
      System.out.println("ERROR: File not found " + e);
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("ERROR: " + e);
      e.printStackTrace();
    }
  }
  
  public int getTotalNumberOfPackets() {
    while (!canDetermineNOPackets) {
      
    }
    int totalNoOfPackets = (int) (fileLength + filenameLengthBytes.length + filenameBytes.length + fileLengthBytes.length + checksumBytes.length) /contentLength + 1;
    return totalNoOfPackets;
  }
  
  
  // ----------------------- methods in common ---------------------------- 
  public synchronized void addToWritingQueue(byte[] data) {
    this.dataQueue.add(data);
  }
 
  public synchronized byte[] readFomWritingQueue() {
    while (dataQueue.isEmpty()) {
      
    }
    return this.dataQueue.remove();
  }
  
  private byte[] getChecksum() {
    long longChecksum = crc.getValue();
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(longChecksum);
    byte[] tooLong =  buffer.array();
    byte[] checksumBytesToCompare = new byte[4];
    System.arraycopy(tooLong,4,checksumBytesToCompare,0,checksumBytesToCompare.length);
    return checksumBytesToCompare;
  }
}
