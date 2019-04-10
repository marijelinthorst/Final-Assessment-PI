package com.nedap.university.packets;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {

  /**
   * Header
   * 
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
   *  - DFL: downloading files list --> server of client verantwoordelijk?
   *  - PFL: paused files list --> server of client verantwoordelijk?
   *  - EXIT: exit, shut down of server
   *  - DOWN: download
   *  - UP: upload
   *
   * TOTAL:
   * - 140 bits: 16 bytes, 4 empty bits
   *
   * questions:
   * - pause / resume uploading files, can be combination of UP/DOWN + PAU/RES
   * - remove files
   */
  
  // variables
  byte[] data;
  InetAddress address;
  int port;
  
  // lengths
  final static int PACKETLENGTH = 512;
  final static int FNL = 2;
  final static int SNL = 4;
  final static int ANL = 4;
  final static int WSL = 2;
  final static int CSL = 2;
  final static int FL = 2;
  static int contentLength;
  
  // header
  byte[] fileNumber;
  byte[] seqNumber;
  byte[] ackNumber;
  byte[] windowSize;
  byte[] checksum;
  byte[] content;
  
  // Flags
  byte syn = 0;
  byte ack = 0;
  byte fin = 0;
  byte afl = 0;
  
  byte dfl = 0;
  byte pfl = 0;
  byte down = 0;
  byte up = 0;
  byte pau = 0;
  byte res = 0;
  byte stat = 0;
  byte exit = 0;
   
  final static byte SYN = 8;
  final static byte ACK = 4;
  final static byte FIN = 2;
  final static byte AFL = 1;
  
  final static byte DFL = 80;
  final static byte PFL = 40;
  final static byte DOWN = 20;
  final static byte UP = 10;
  final static byte PAU = 8;
  final static byte RES = 4;
  final static byte STAT = 2;
  final static byte EXIT = 1;
  
  
  
  /**
   * constructors
   */
  public Packet (DatagramPacket packet) {
    this.data = packet.getData();
    this.address = packet.getAddress();
    this.port = packet.getPort();
    
    this.fileNumber = new byte[FNL];
    this.seqNumber = new byte[SNL];
    this.ackNumber = new byte[ANL];
    this.windowSize = new byte[WSL];
    this.checksum = new byte[CSL];
    contentLength = PACKETLENGTH - FNL - SNL - ANL - WSL - CSL;
    this.content = new byte[contentLength];
  }
  
  public Packet (InetAddress address, int port) {
    data = new byte[PACKETLENGTH];
    this.address = address;
    this.port = port;
    
    this.fileNumber = new byte[FNL];
    this.seqNumber = new byte[SNL];
    this.ackNumber = new byte[ANL];
    this.windowSize = new byte[WSL];
    this.checksum = new byte[CSL];
    contentLength = PACKETLENGTH - FNL - SNL - ANL - WSL - CSL;
    this.content = new byte[contentLength];
  }
  
  //--------------------------- Set flags ---------------------------------
  
  public void setSynchronizeFlag() {
    syn = SYN;
  }
  
  public void setAcknowlegdementFlag() {
    ack = ACK;
  }
  
  public void setFinalFlag() {
    fin = FIN;
  }
  
  public void setAvailableFilesList() {
    afl = AFL;
  }
  
  public void setDownloadingFilesList() {
    dfl = DFL;
  }
  
  public void setPausedFilesList() {
    pfl = PFL;
  }
  
  public void setDownloadingFlag() {
    down = DOWN;
  }
  
  public void setUploadingFlag() {
    up = UP;
  }
  
  public void setPauseFlag() {
    pau = PAU;
  }
  
  public void setResumeFlag() {
    res = RES;
  }
  
  public void setStatisticsFlag() {
    stat = STAT;
  }
  
  public void setExitFlag() {
    exit = EXIT;
  }
  
  //------------------------ Set header parts ---------------------------------
  
  public void setFileNumber (int fileNumber) {
    byte[] byteArray = BigInteger.valueOf(fileNumber).toByteArray();
    int start = this.fileNumber.length - byteArray.length;
    for (int i = start; i< this.fileNumber.length; i++) {
      data[i] = byteArray[i - start];
    }
  }
  
  public void setSeqNumber (int seqNumber) {
    byte[] byteArray = BigInteger.valueOf(seqNumber).toByteArray();
    int start = this.seqNumber.length - byteArray.length;
    for (int i = start; i< this.seqNumber.length; i++) {
      data[FNL+i] = byteArray[i - start];
    }
  }
  
  public void setAckNumber (int ackNumber) {
    byte[] byteArray = BigInteger.valueOf(ackNumber).toByteArray();
    int start = this.ackNumber.length - byteArray.length;
    for (int i = start; i< this.ackNumber.length; i++) {
      data[FNL+SNL+i] = byteArray[i - start];
    }
  }
  
  public void setWindowSize (int windowSize) {
    byte[] byteArray = BigInteger.valueOf(windowSize).toByteArray();
    int start = this.windowSize.length - byteArray.length;
    for (int i = start; i< this.windowSize.length; i++) {
      data[FNL+SNL+ANL+i] = byteArray[i - start];
    }
  }
  
  public void setChecksum (int checksum) {
    byte[] byteArray = BigInteger.valueOf(checksum).toByteArray();
    int start = this.checksum.length - byteArray.length;
    for (int i = start; i< 2 && i< this.checksum.length; i++) {
      data[FNL+SNL+ANL+WSL+i] = byteArray[i - start];
    }
  }
  
  public void setContent (byte[] content) {
    int start = contentLength - content.length;
    for (int i = start; i < content.length; i++) {
      data[FNL+SNL+ANL+WSL+CSL+FL+i] = content[i - start];
    }
  }
  
  
  // --------------- Make packet ---------------------------------
  public DatagramPacket makePacket() {
    // set flags
    this.flagsToByte();   
    
    // make the datagram packet
    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
    
    return packet;
  }
  
  public void flagsToByte() {
    byte [] flags = new byte[2];
    flags[0] = (byte) (syn | ack | fin | afl);
    flags[1] = (byte) (dfl | pfl | down | up | pau | res | stat | exit);
    
    data[14] = flags[0];
    data[15] = flags[1];
  }
   
  
  //--------------------------- Read flags ---------------------------------------
  
  public boolean hasSynchronizeFlag() {
    return (data[14] & SYN) == SYN;
  }
  public boolean hasAcknowledgementFlag() {
    return (data[14] & ACK) == ACK;
  }
  public boolean hasFinalFlag() {
    return (data[14] & FIN) == FIN;
  }
  public boolean hasAvailableFilesListFlag() {
    return (data[14] & AFL) == AFL;
  }
  
  public boolean hasDownloadingFilesListFlag() {
    return (data[15] & DFL) == DFL;
  }
  public boolean hasPausedFilesListFlag() {
    return (data[15] & PFL) == PFL;
  }
  public boolean hasDownloadingFlag() {
    return (data[15] & DOWN) == DOWN;
  }
  public boolean hasUploadingFLag() {
    return (data[15] & UP) == UP;
  }
  public boolean hasPauseFlag() {
    return (data[15] & PAU) == PAU;
  }
  public boolean hasResumeFlag() {
    return (data[15] & RES) == RES;
  }
  public boolean hasStatisticsFlag() {
    return (data[15] & STAT) == STAT;
  }
  public boolean hasExitFlag() {
    return (data[15] & EXIT) == EXIT;
  }
  
  public boolean hasFLag (byte flag) {
    
    return (data[15] & flag) == flag;
  }
  
  //--------------------------- Read header parts ---------------------------------------
  
  public int getFileNumber() {
    for (int i = 0; i<fileNumber.length; i++) {
      fileNumber[i] = data[i];
    }
     return new BigInteger(fileNumber).intValue();  
  }
  
  public int getSeqNumber() {
    for (int i = 0; i<seqNumber.length; i++) {
      seqNumber[i] = data[2+i];
    }
     return new BigInteger(seqNumber).intValue();
  }
  
  public int getAckNumber() {
    for (int i = 0; i<ackNumber.length; i++) {
      ackNumber[i] = data[6+i];
    }
     return new BigInteger(ackNumber).intValue();
  }
  
  public int getWindowSize() {
    for (int i = 0; i<windowSize.length; i++) {
      windowSize[i] = data[10+i];
    }
     return new BigInteger(windowSize).intValue();   
  }
  
  public int getChecksum() {
    for (int i = 0; i<checksum.length; i++) {
      checksum[i] = data[12+i];
    }
     return new BigInteger(checksum).intValue();   
  }
  
  public byte[] getContent() {
    byte [] content = new byte[496];
    for (int i = 0; i < content.length; i++) {
      content[i] = data[16+i];
    }
     return content;   
  }
  
  // Checksum
  public int calculateChecksum() {
    // TODO
    return 0;
  }
}
