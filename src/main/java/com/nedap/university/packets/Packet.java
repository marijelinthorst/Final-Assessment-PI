package com.nedap.university.packets;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

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
  final static int CSL = 4;
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
  
  int flagFirst = FNL + SNL + ANL + WSL + CSL;
  int flagSecond = flagFirst + 1;
  
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
  
  public void setAvailableFilesListFlag() {
    afl = AFL;
  }
  
  public void setDownloadingFilesListFlag() {
    dfl = DFL;
  }
  
  public void setPausedFilesListFlag() {
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
  
  // TODO niet nodig?
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
    
    // add checksum
    byte[] checksum = this.calculateChecksum();
    data[12] = checksum[0];
    data[13] = checksum[1];
    data[14] = checksum[2];
    data[15] = checksum[3];
    
    // make the datagram packet
    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
    
    return packet;
  }
  
  public void flagsToByte() {
    byte [] flags = new byte[2];
    flags[0] = (byte) (syn | ack | fin | afl);
    flags[1] = (byte) (dfl | pfl | down | up | pau | res | stat | exit);
    
    data[flagFirst] = flags[0];
    data[flagSecond] = flags[1];
  }
   
  
  //--------------------------- Read flags ---------------------------------------
  
  public boolean hasSynchronizeFlag() {
    return (data[flagFirst] & SYN) == SYN;
  }
  public boolean hasAcknowledgementFlag() {
    return (data[flagFirst] & ACK) == ACK;
  }
  public boolean hasFinalFlag() {
    return (data[flagFirst] & FIN) == FIN;
  }
  public boolean hasAvailableFilesListFlag() {
    return (data[flagFirst] & AFL) == AFL;
  }
  
  public boolean hasDownloadingFilesListFlag() {
    return (data[flagSecond] & DFL) == DFL;
  }
  public boolean hasPausedFilesListFlag() {
    return (data[flagSecond] & PFL) == PFL;
  }
  public boolean hasDownloadingFlag() {
    return (data[flagSecond] & DOWN) == DOWN;
  }
  public boolean hasUploadingFLag() {
    return (data[flagSecond] & UP) == UP;
  }
  public boolean hasPauseFlag() {
    return (data[flagSecond] & PAU) == PAU;
  }
  public boolean hasResumeFlag() {
    return (data[flagSecond] & RES) == RES;
  }
  public boolean hasStatisticsFlag() {
    return (data[flagSecond] & STAT) == STAT;
  }
  public boolean hasExitFlag() {
    return (data[flagSecond] & EXIT) == EXIT;
  }
  
  public boolean hasFLag (byte flag) { 
    return (data[flagSecond] & flag) == flag;
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
      seqNumber[i] = data[FNL+i];
    }
     return new BigInteger(seqNumber).intValue();
  }
  
  public int getAckNumber() {
    for (int i = 0; i<ackNumber.length; i++) {
      ackNumber[i] = data[FNL+SNL+i];
    }
     return new BigInteger(ackNumber).intValue();
  }
  
  public int getWindowSize() {
    for (int i = 0; i<windowSize.length; i++) {
      windowSize[i] = data[FNL+SNL+ANL+i];
    }
     return new BigInteger(windowSize).intValue();   
  }
  
  public byte[] getChecksum() {
    for (int i = 0; i<checksum.length; i++) {
      checksum[i] = data[FNL+SNL+ANL+WSL+i];
    }
     return checksum;   
  }
  
  public byte[] getContent() {
    byte [] content = new byte[contentLength];
    for (int i = 0; i < content.length; i++) {
      content[i] = data[FNL+SNL+ANL+WSL+CSL+FL+i];
    }
     return content;   
  }
  
  // Checksum
  public byte[] calculateChecksum() {
    // calculate checksum of:
    byte[] dataUntilChecksum = Arrays.copyOfRange(data, 0, FNL+SNL+ANL+WSL);
    byte[] dataAfterChecksum = Arrays.copyOfRange(data, FNL+SNL+ANL+WSL+CSL, PACKETLENGTH);
    
    byte[] dataWithoutChecksum = new byte[PACKETLENGTH - CSL];
    System.arraycopy(dataUntilChecksum, 0, dataWithoutChecksum, 0, dataUntilChecksum.length); 
    System.arraycopy(dataAfterChecksum, 0, dataWithoutChecksum, dataUntilChecksum.length, dataAfterChecksum.length);
    
    // calculate checksum
    CRC32 crc = new CRC32();
    crc.update(dataWithoutChecksum);
    long checksum = crc.getValue();
    
    // from long to byte[2]
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(checksum);
    byte[] tooLong =  buffer.array();
    byte[] correct = new byte[4];
    System.arraycopy(tooLong,4,correct,0,correct.length);
    
    return correct;
  }
}
