package com.nedap.university;

import java.net.DatagramPacket;

public class PackageReader {
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
  
  /**
   * Constructor
   */
  public PackageReader() {
    
  }
  
  public void readPackage(DatagramPacket packet) {
    // TODO
  }

}
