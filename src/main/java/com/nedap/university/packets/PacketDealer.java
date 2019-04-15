package com.nedap.university.packets;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

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
  
  
  
  private InetAddress address;
  private int port;
  private SendQueue sendQueue;
  
  /**
   * Constructor
   */
  public PacketDealer(DatagramSocket socket, InetAddress address, int port) {
    this.address = address;
    this.port = port;
    sendQueue = new SendQueue(socket);
    sendQueue.start();
  }
  
  public void readPackage(DatagramPacket dataPacket) {
    System.out.println("Received a packet:");
    Packet packet = new Packet (dataPacket);    
    if (Arrays.equals(packet.getChecksum(), packet.calculateChecksum())) {
      System.out.println("  Checksum correct");
      this.checkFlags(packet);
    } else {
      this.packetIncorrect();
    }
  }
  
  /**
   * Check the flags of the packet to match 1 of the 9 different actions. For each action one certain flag is set and a lot of other flags 
   * cannot be set. The remaining flags make combinations to determine what to respond, this is determined in the methods below.
   * @param packet
   * TODO: correct flags responsibility Packet?
   */
  private void checkFlags(Packet packet) {    
    if (checkAvailableFilesListCorrectFlags(packet)) {
      System.out.println("  AFL packet");
      this.availableFilesListPacketDealer(packet);
    } else if (checkDownloadingCorrectFlags(packet)) {
      System.out.println("  DOWN packet");
      this.downloadingPacketDealer(packet);
    } else if (checkStatisticsCorrectFlags(packet)) {
      System.out.println("  STAT packet");
      this.statisticsPacketDealer(packet);
    } else if (checkPauseCorrectFlags(packet)) {
      System.out.println("  PAU packet");
      this.pausePacketDealer(packet);
    } else if (checkResumeCorrectFlags(packet)) {
      System.out.println("  RES packet");
      this.resumePacketDealer(packet);
    } else if (checkDownloadingFilesListCorrectFlags(packet)) {
      System.out.println("  DFL packet");
      this.downloadingFilesListPacketDealer(packet);
    } else if (checkPausedFilesListCorrectFlags(packet)) {
      System.out.println("  PFL packet");
      this.pausedFilesListPacketDealer(packet);
    } else if (checkUploadingCorrectFlags(packet)) {
      System.out.println("  UP packet");
      this.uploadingPacketDealer(packet);
    } else if (checkExitCorrectFlags(packet)) {
      System.out.println("  EXIT packet");
      this.exitPacketDealer(packet);
    } else {
      this.packetIncorrect();
      
    }  
  }
  private void packetIncorrect() {
    System.out.println("  Packet checksum or flags incorrect");
    // TODO Pakket onleesbaar, stuur laatste pakketje opnieuw? Of ack?
  }

  private void availableFilesListPacketDealer (Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {
      // do something with content
      returnPacket.setAvailableFilesListFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    } else if (packet.hasSynchronizeFlag()) {
      returnPacket.setAvailableFilesListFlag();
      returnPacket.setSynchronizeFlag();
      returnPacket.setAcknowlegdementFlag();
      // TODO: add list in content
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    } else if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.makePacket());
    } else {
      this.packetIncorrect();
    }
  }
  
  private void downloadingPacketDealer(Packet packet) {
    // TODO Auto-generated method stub
    
  }

  private void statisticsPacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {
      // TODO do something with content of packet
      returnPacket.setStatisticsFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    } else if (packet.hasSynchronizeFlag()) {
      returnPacket.setStatisticsFlag();
      returnPacket.setSynchronizeFlag();
      returnPacket.setAcknowlegdementFlag();
      // TODO: add statistics in content
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    } else if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.makePacket());
    } else {
      this.packetIncorrect();
    }
  }
  
  private void pausePacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.makePacket());
    } else {
      // TODO read filenumber and pause that file
      returnPacket.setPauseFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    }   
  }

  private void resumePacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.makePacket());
    } else {
      // TODO read filenumber and resume that file
      returnPacket.setResumeFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    }
  }

  private void downloadingFilesListPacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {
      // TODO do something with content of packet
      returnPacket.setDownloadingFilesListFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    } else if (packet.hasSynchronizeFlag()) {
      returnPacket.setDownloadingFilesListFlag();
      returnPacket.setSynchronizeFlag();
      returnPacket.setAcknowlegdementFlag();
      // TODO: add list in content
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    } else if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.makePacket());
    } else {
      this.packetIncorrect();
    }
    
  }

  private void pausedFilesListPacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {
      // TODO do something with content of packet
      returnPacket.setPausedFilesListFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    } else if (packet.hasSynchronizeFlag()) {
      returnPacket.setPausedFilesListFlag();
      returnPacket.setSynchronizeFlag();
      returnPacket.setAcknowlegdementFlag();
      // TODO: add list in content
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    } else if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.makePacket());
    } else {
      this.packetIncorrect();
    }
  }

  private void uploadingPacketDealer(Packet packet) {
    // TODO Auto-generated method stub
    
  }

  private void exitPacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.makePacket());
    } else {
      // TODO exit
      returnPacket.setExitFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.addToQueue(returnDataPacket);
    }
  }
  
  // -------------------------- flag booleans ----------------------------  
  private boolean checkAvailableFilesListCorrectFlags(Packet packet) {
    return packet.hasAvailableFilesListFlag() && !packet.hasFinalFlag() && !packet.hasDownloadingFilesListFlag() && 
        !packet.hasPausedFilesListFlag() && !packet.hasDownloadingFlag() && !packet.hasUploadingFLag() && 
        !packet.hasPauseFlag() && !packet.hasResumeFlag() && !packet.hasStatisticsFlag() && !packet.hasExitFlag();
  }
  
  private boolean checkDownloadingCorrectFlags(Packet packet) {
    return packet.hasDownloadingFlag() && !packet.hasAvailableFilesListFlag() && !packet.hasDownloadingFilesListFlag() && 
        !packet.hasPausedFilesListFlag() && !packet.hasUploadingFLag() && !packet.hasPauseFlag() && 
        !packet.hasResumeFlag() && !packet.hasStatisticsFlag() && !packet.hasExitFlag();
  }

  private boolean checkStatisticsCorrectFlags(Packet packet) {
    return packet.hasStatisticsFlag() && !packet.hasFinalFlag() && !packet.hasAvailableFilesListFlag() && 
        !packet.hasDownloadingFilesListFlag() && !packet.hasPausedFilesListFlag() && !packet.hasDownloadingFlag() && 
        !packet.hasUploadingFLag() && !packet.hasPauseFlag() && !packet.hasResumeFlag() && !packet.hasExitFlag();
  }

  private boolean checkPauseCorrectFlags(Packet packet) {
    return packet.hasPauseFlag() && !packet.hasSynchronizeFlag() && !packet.hasFinalFlag() && 
        !packet.hasAvailableFilesListFlag() && !packet.hasDownloadingFilesListFlag() && !packet.hasPausedFilesListFlag() && 
        !packet.hasDownloadingFlag() && !packet.hasUploadingFLag() && !packet.hasResumeFlag() && !packet.hasStatisticsFlag() && 
        !packet.hasExitFlag();
  }

  private boolean checkResumeCorrectFlags(Packet packet) {
    return packet.hasResumeFlag() && !packet.hasSynchronizeFlag() && !packet.hasFinalFlag() && 
        !packet.hasAvailableFilesListFlag() && !packet.hasDownloadingFilesListFlag() && !packet.hasPausedFilesListFlag() && 
        !packet.hasDownloadingFlag() && !packet.hasUploadingFLag() && !packet.hasPauseFlag() && !packet.hasStatisticsFlag() && 
        !packet.hasExitFlag();
  }

  private boolean checkDownloadingFilesListCorrectFlags(Packet packet) {
    return packet.hasDownloadingFilesListFlag() && !packet.hasFinalFlag() && !packet.hasAvailableFilesListFlag() && 
        !packet.hasPausedFilesListFlag() && !packet.hasDownloadingFlag() && !packet.hasUploadingFLag() && 
        !packet.hasPauseFlag() && !packet.hasResumeFlag() && !packet.hasStatisticsFlag() && !packet.hasExitFlag();
  }

  private boolean checkPausedFilesListCorrectFlags(Packet packet) {
    return packet.hasPausedFilesListFlag() && !packet.hasFinalFlag() && !packet.hasAvailableFilesListFlag() && 
        !packet.hasDownloadingFilesListFlag() && !packet.hasDownloadingFlag() && !packet.hasUploadingFLag() && 
        !packet.hasPauseFlag() && !packet.hasResumeFlag() && !packet.hasStatisticsFlag() && !packet.hasExitFlag();
  }

  private boolean checkUploadingCorrectFlags(Packet packet) {
    return packet.hasUploadingFLag() && !packet.hasAvailableFilesListFlag() && !packet.hasDownloadingFilesListFlag() && 
        !packet.hasPausedFilesListFlag() && !packet.hasDownloadingFlag() && !packet.hasPauseFlag() && 
        !packet.hasResumeFlag() && !packet.hasStatisticsFlag() && !packet.hasExitFlag();
  }

  private boolean checkExitCorrectFlags(Packet packet) {
    return packet.hasExitFlag() && !packet.hasSynchronizeFlag() && !packet.hasFinalFlag() && 
        !packet.hasAvailableFilesListFlag() && !packet.hasDownloadingFilesListFlag() && !packet.hasPausedFilesListFlag() && 
        !packet.hasDownloadingFlag() && !packet.hasUploadingFLag() && !packet.hasPauseFlag() && !packet.hasResumeFlag() && 
        !packet.hasStatisticsFlag();
  }
  
  // ------------------------ methods for client ----------------
  
  public void queryAvailableFilesList() {
    Packet packet = new Packet(address, port);
    packet.setAvailableFilesListFlag();
    packet.setSynchronizeFlag();
    sendQueue.addToQueue(packet.makePacket());
  }
  
  public void downloadFile() {
    // TODO add filenumber, sequence number
    Packet packet = new Packet(address, port);
    packet.setDownloadingFlag();
    packet.setSynchronizeFlag();
    sendQueue.addToQueue(packet.makePacket());
  }

  public void queryStatistics() {
    Packet packet = new Packet(address, port);
    packet.setStatisticsFlag();
    packet.setSynchronizeFlag();
    sendQueue.addToQueue(packet.makePacket());
  }
  
  public void pauseFile() {
    // TODO add filenumber
    Packet packet = new Packet(address, port);
    packet.setPauseFlag();
    sendQueue.addToQueue(packet.makePacket());
  }
  
  public void resumeFile() {
    // TODO add filenumber
    Packet packet = new Packet(address, port);
    packet.setResumeFlag();
    sendQueue.addToQueue(packet.makePacket());
  }
  
  public void queryDownloadingFilesList() {
    Packet packet = new Packet(address, port);
    packet.setDownloadingFilesListFlag();
    packet.setSynchronizeFlag();
    sendQueue.addToQueue(packet.makePacket());
  }
  
  public void queryPausedFilesList() {
    Packet packet = new Packet(address, port);
    packet.setPausedFilesListFlag();
    packet.setSynchronizeFlag();
    sendQueue.addToQueue(packet.makePacket());
  }
  
  public void uploadFile() {
    // TODO filenumber, sequence number
    Packet packet = new Packet(address, port);
    packet.setUploadingFlag();
    packet.setSynchronizeFlag();
    sendQueue.addToQueue(packet.makePacket());
  }
  
  public void exitServer() {
    Packet packet = new Packet(address, port);
    packet.setExitFlag();
    sendQueue.addToQueue(packet.makePacket());
  }
  
}
