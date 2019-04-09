package com.nedap.university;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

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
  DatagramSocket socket;
  
  // header
  byte[] filename;
  byte[] seqNumber;
  byte[] ackNumber;
  byte[] windowSize;
  byte[] checksum;
  
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
  public Packet (DatagramSocket socket, DatagramPacket packet) {
    this.data = packet.getData();
    this.socket = socket;
  }
  
  public Packet (DatagramSocket socket) {
    data = new byte[512];
    this.socket = socket;
  }
  
  //--------------------------- Set flags ---------------------------------
  
  public void setSyn() {
    syn = SYN;
  }
  
  public void setAck() {
    ack = ACK;
  }
  
  public void setFin() {
    fin = FIN;
  }
  
  public void setAfl() {
    afl = AFL;
  }
  
  public void setDfl() {
    dfl = DFL;
  }
  
  public void setPfl() {
    pfl = PFL;
  }
  
  public void setDown() {
    down = DOWN;
  }
  
  public void setUp() {
    up = UP;
  }
  
  public void setPau() {
    pau = PAU;
  }
  
  public void setRes() {
    res = RES;
  }
  
  public void setStat() {
    stat = STAT;
  }
  
  public void setExit() {
    exit = EXIT;
  }
  
  //------------------------ Set header parts ---------------------------------
  
  public void setFileName (String filename) {
    this.filename = filename.getBytes();
    data[0] = this.filename[0];
    data[1] = this.filename[1];
  }
  
  public void setSeqNumber (int seqNumber) {
    byte[] byteArray = BigInteger.valueOf(seqNumber).toByteArray();
    this.seqNumber = byteArray;
    data[2] = this.seqNumber[0];
    data[3] = this.seqNumber[1];
    data[4] = this.seqNumber[2];
    data[5] = this.seqNumber[3];
  }
  
  public void setAckNumber (int ackNumber) {
    byte[] byteArray = BigInteger.valueOf(ackNumber).toByteArray();
    this.ackNumber = byteArray;
    data[6] = this.ackNumber[0];
    data[7] = this.ackNumber[1];
    data[8] = this.ackNumber[2];
    data[9] = this.ackNumber[3];
  }
  
  public void setWindowSize (int windowSize) {
    byte[] byteArray = BigInteger.valueOf(windowSize).toByteArray();
    this.windowSize = byteArray;
    data[10] = this.windowSize[0];
    data[11] = this.windowSize[1];
  }
  
  public void setChecksum (int checksum) {
    byte[] byteArray = BigInteger.valueOf(checksum).toByteArray();
    this.checksum = byteArray;
    data[12] = this.checksum[0];
    data[13] = this.checksum[1];
    
  }
  
  public void setContent (byte[] content) {
    for (int i = 0; i < content.length; i++) {
      data[16+i] = content[i];
    }
  }
  
  
  // --------------- Make packet ---------------------------------
  public DatagramPacket makePacket() {
    // set flags
    this.flagsToByte();   
    
    // make the datagram packet
    DatagramPacket packet = new DatagramPacket(data, data.length, socket.getInetAddress(), socket.getPort());
    
    return packet;
  }
  
  private void flagsToByte() {
    byte [] flags = new byte[2];
    flags[0] = (byte) (syn | ack | fin | afl);
    flags[1] = (byte) (dfl | pfl | down | up | pau | res | stat | exit);
    
    data[14] = flags[0];
    data[15] = flags[1];
  }
   
  
  //--------------------------- Read flags ---------------------------------------
  
  public boolean hasSyn() {
    return (data[14] & SYN) == SYN;
  }
  public boolean hasAck() {
    return (data[14] & ACK) == ACK;
  }
  public boolean hasFin() {
    return (data[14] & FIN) == FIN;
  }
  public boolean hasAfl() {
    return (data[14] & AFL) == AFL;
  }
  
  public boolean hasDfl() {
    return (data[15] & DFL) == DFL;
  }
  public boolean hasPfl() {
    return (data[15] & PFL) == PFL;
  }
  public boolean hasDown() {
    return (data[15] & DOWN) == DOWN;
  }
  public boolean hasUp() {
    return (data[15] & UP) == UP;
  }
  public boolean hasPau() {
    return (data[15] & PAU) == PAU;
  }
  public boolean hasRes() {
    return (data[15] & RES) == RES;
  }
  public boolean hasStat() {
    return (data[15] & STAT) == STAT;
  }
  public boolean hasExit() {
    return (data[15] & EXIT) == EXIT;
  }
  
  //--------------------------- Read header parts ---------------------------------------
  
  public String getFilename() {
    filename[0] = data[0];
    filename[1] = data[1];
     return Arrays.toString(filename);   
  }
  
  public int getSeqNumber() {
    seqNumber[0] = data[2];
    seqNumber[1] = data[3];
    seqNumber[2] = data[4];
    seqNumber[3] = data[5];
     return new BigInteger(seqNumber).intValue();
  }
  
  public int getAckNumber() {
    ackNumber[0] = data[6];
    ackNumber[1] = data[7];
    ackNumber[2] = data[8];
    ackNumber[3] = data[9];
     return new BigInteger(ackNumber).intValue();
  }
  
  public int getWindowSize() {
    windowSize[0] = data[10];
    windowSize[1] = data[11];
     return new BigInteger(windowSize).intValue();   
  }
  
  public int getChecksum() {
    checksum[0] = data[12];
    checksum[1] = data[13];
     return new BigInteger(checksum).intValue();   
  }
  
  public byte[] getContent() {
    byte [] content = new byte[496];
    for (int i = 0; i < content.length; i++) {
      content[i] = data[16+i];
    }
     return content;   
  }
}
