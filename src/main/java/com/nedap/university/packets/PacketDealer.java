package com.nedap.university.packets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.nio.file.Files;

public class PacketDealer {
  /**
   * Header
   * 
   * source port? (16 bits)
   * filename (16 bits)
   * sequence number (32 bits)
   *    --> starts random number with flag SYN
   *    --> acknowledgement number is sequence number +1
   * Acknowledgement number (32 bits)
   * Window size (16 bits)
   *    --> receive window
   *    --> important when sending more files at once
   * Checksum (16 bits)
   *    --> UPD checksum not enough
   * Flags (12 bits)
   *  - AFL: available files list
   *  - SYN: synchronise file transfer (starting sequence number)
   *  - ACK: acknowledge packet
   *  - FIN: last package of file, should include file checksum
   *  - STAT: statistics on file
   *  - PAU: pause file
   *  - RES: resume paused file
   *  - DFL: downloading files list
   *  - PFL: paused files list
   *  - EXIT: exit, shut down of server
   *  - DOWN: download
   *  - UP: upload
   *
   * TOTAL:
   * - 140 bits: 18 bytes, 4 empty bits
   *
   * questions:
   * - pause / resume uploading files, can be combination of UP/DOWN + PAU/RES
   * - remove files
   */
  
  //Path of a file 
  static final String FILEPATH = ""; 
  
  /**
   * Constructor
   */
  public PacketDealer() {
    
  }
  
  public void readPackage(DatagramPacket dataPacket) {
    Packet packet = new Packet (dataPacket);
    
    if (packet.getChecksum() == packet.calculateChecksum()) {
      this.checkFlags(packet);
    } else {
      System.out.println("Packet checksum incorrect");
      // TODO Pakket onleesbaar, stuur laatste pakketje opnieuw? Of ack?
    }
    // TODO
    // krijg pakketje
    // lees pakketje
    // deel in op info
    // new File(FILEPATH + "");
  }
  
  private void checkFlags(Packet packet) {
    
    if (packet.hasSynchronizeFlag()) {
      
    } else if (packet.hasAcknowledgementFlag()){
      
    } else if (packet.hasFinalFlag()){
      
    } else if (packet.hasAvailableFilesListFlag()){
      this.aflFlag(packet);
    } else if (packet.hasDownloadingFilesListFlag()){
      
    } else if (packet.hasPausedFilesListFlag()){
         
    } else if (packet.hasPauseFlag()){
      
    } else if (packet.hasResumeFlag()){
      
    } else if (packet.hasStatisticsFlag()){
      
    } else if (packet.hasExitFlag()){
      
    } else {
      System.out.println("Packet flags incorrect");
      // TODO Pakket onleesbaar, stuur laatste pakketje opnieuw? Of ack?
    }   
  }
  
  private void aflFlag (Packet packet) {
    // Client: afl
    // Server: afl + fin
    // Client: afl + ack
    //if ()
  }
  
  
  // -------------------------- file writer / reader ----------------------------

  // Method which write the bytes into a file 
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
}
