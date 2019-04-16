package com.nedap.university.packets;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
  private ReliableSender sender;
  private ReliableReceiver receiver;
  
  // maps with treads
  private HashMap<Short, ReliableSender> sendingMap;
  private HashMap<Short, ReliableReceiver> receivingMap;
  
  /**
   * Constructor
   */
  public PacketDealer(DatagramSocket socket, InetAddress address, int port) {
    this.address = address;
    this.port = port;
    sendQueue = new SendQueue(socket);
    sendQueue.start();
    sendingMap = new HashMap<Short, ReliableSender>();
    receivingMap = new HashMap<Short, ReliableReceiver>();
  }
  
  public void readPackage(DatagramPacket dataPacket) {
    System.out.println("Received a packet:");
    Packet packet = new Packet (dataPacket);    
    if (Arrays.equals(packet.getChecksum(), packet.calculateChecksum())) {
      System.out.println("  Checksum correct");
      this.checkFlags(packet);
    } else {
      this.packetIncorrect(packet);
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
      this.packetIncorrect(packet);
      
    }  
  }
  private void packetIncorrect(Packet packet) {
    System.out.println("  Packet checksum or flags incorrect");
    System.out.println("  SYN: " + packet.hasSynchronizeFlag());
    System.out.println("  ACK: " + packet.hasAcknowledgementFlag());
    System.out.println("  FIN: " + packet.hasFinalFlag());
    System.out.println("  AFL: " + packet.hasAvailableFilesListFlag());
    System.out.println("  DFL: " + packet.hasDownloadingFilesListFlag());
    System.out.println("  PFL: " + packet.hasPausedFilesListFlag());
    System.out.println("  DOWN: " + packet.hasDownloadingFlag());
    System.out.println("  UP: " + packet.hasUploadingFLag());
    System.out.println("  PAU: " + packet.hasPauseFlag());
    System.out.println("  RES: " + packet.hasResumeFlag());
    System.out.println("  STAT: " + packet.hasStatisticsFlag());
    System.out.println("  EXIT: " + packet.hasExitFlag());
    }

  private void availableFilesListPacketDealer (Packet packet) {
    Packet returnPacket = new Packet(address, port);
    returnPacket.setAvailableFilesListFlag();
    returnPacket.setAcknowlegdementFlag();
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {
      // read content
      receiver = new ReliableReceiver(sendQueue, packet);
      receiver.start();

      // return acknowledgement
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.stopTimeout(packet.getSeqNumber()-1);
      sender = new ReliableSender(returnDataPacket, sendQueue);
      sender.start();
    } else if (packet.hasSynchronizeFlag()) {
      returnPacket.setSynchronizeFlag();
      
      // TODO: add list 
      List<?> list = new LinkedList<String>();
      
      sender = new ReliableSender(returnPacket, list, sendQueue, packet.getSeqNumber() + 1);
      sender.start();
    } else if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.getSeqNumber() - 1);
    } else {
      this.packetIncorrect(packet);
    }
  }
  
  private void downloadingPacketDealer(Packet packet) {
    // TODO Auto-generated method stub
    short filenumber = packet.getFileNumber();
    boolean downloading = true;
    
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {    
      receiver = new ReliableReceiver(sendQueue, filenumber, downloading);
      this.receivingMap.put(filenumber, receiver);
      receiver.start();
      receiver.addToReadingQueue(packet.getContent(), packet.getSeqNumber(), packet.hasFinalFlag());
    } else if (packet.hasSynchronizeFlag()) {;
      String filename = new String(packet.getContent());
      
      sender = new ReliableSender(filename, sendQueue, filenumber, downloading);
      this.sendingMap.put(filenumber, sender);
      sender.start();
    } else if (packet.hasAcknowledgementFlag()) {
      sender = sendingMap.get(filenumber);
      sender.changeLastAcknowledgeReceived(packet.getAckNumber());
    } else {
      receiver = receivingMap.get(filenumber);
      receiver.addToReadingQueue(packet.getContent(), packet.getSeqNumber(), packet.hasFinalFlag());
    }
  }

  private void statisticsPacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    returnPacket.setStatisticsFlag();
    returnPacket.setAcknowlegdementFlag();
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {
      // read content
      receiver = new ReliableReceiver(sendQueue, packet);
      receiver.start();
      
      // return acknowledgement
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.stopTimeout(packet.getSeqNumber()-1);
      sender = new ReliableSender(returnDataPacket, sendQueue);
      sender.start();
    } else if (packet.hasSynchronizeFlag()) {
      returnPacket.setSynchronizeFlag();
      
      // TODO: add statistics in content
      List<?> list = new LinkedList<String>();
      
      sender = new ReliableSender(returnPacket, list, sendQueue, packet.getSeqNumber() + 1);
      sender.start();
    } else if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.getSeqNumber() - 1);
    } else {
      this.packetIncorrect(packet);
    }
  }
  
  private void pausePacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.getSeqNumber() - 1);
    } else {
      // TODO read filenumber and pause that file
      returnPacket.setPauseFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sender = new ReliableSender(returnDataPacket, sendQueue);
      sender.start();
    }   
  }

  private void resumePacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.getSeqNumber() - 1);
    } else {
      // TODO read filenumber and resume that file
      returnPacket.setResumeFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sender = new ReliableSender(returnDataPacket, sendQueue);
      sender.start();
    }
  }

  private void downloadingFilesListPacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    returnPacket.setDownloadingFilesListFlag();
    returnPacket.setAcknowlegdementFlag();
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {
      // read content
      receiver = new ReliableReceiver(sendQueue, packet);
      receiver.start();

      // return acknowledgement
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.stopTimeout(packet.getSeqNumber()-1);
      sender = new ReliableSender(returnDataPacket, sendQueue);
      sender.start(); 
      } else if (packet.hasSynchronizeFlag()) {
      returnPacket.setSynchronizeFlag();
      
      // TODO: add list 
      List<?> list = new LinkedList<String>();
      
      sender = new ReliableSender(returnPacket, list, sendQueue, packet.getSeqNumber() + 1);
      sender.start();
    } else if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.getSeqNumber() - 1);
    } else {
      this.packetIncorrect(packet);
    }
    
  }

  private void pausedFilesListPacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    returnPacket.setPausedFilesListFlag();
    returnPacket.setAcknowlegdementFlag();
    if (packet.hasSynchronizeFlag() && packet.hasAcknowledgementFlag()) {
      // read content
      receiver = new ReliableReceiver(sendQueue, packet);
      receiver.start();

      // return acknowledgement
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sendQueue.stopTimeout(packet.getSeqNumber()-1);
      sender = new ReliableSender(returnDataPacket, sendQueue);
      sender.start(); 
    } else if (packet.hasSynchronizeFlag()) {
      returnPacket.setSynchronizeFlag();
      
      // TODO: add list 
      List<?> list = new LinkedList<String>();
      
      sender = new ReliableSender(returnPacket, list, sendQueue, packet.getSeqNumber() + 1);
      sender.start();
    } else if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.getSeqNumber() - 1);
    } else {
      this.packetIncorrect(packet);
    }
  }

  private void uploadingPacketDealer(Packet packet) {
    // TODO Auto-generated method stub
    short filenumber = packet.getFileNumber();
    boolean downloading = false;
    
    if (packet.hasSynchronizeFlag()) {      
      receiver = new ReliableReceiver(sendQueue, filenumber, downloading);
      this.receivingMap.put(filenumber, receiver);
      receiver.start();
      receiver.addToReadingQueue(packet.getContent(), packet.getSeqNumber(), packet.hasFinalFlag());
    } else if (packet.hasAcknowledgementFlag()) {
      sender = sendingMap.get(filenumber);
      sender.changeLastAcknowledgeReceived(packet.getAckNumber());
    } else {
      receiver = receivingMap.get(filenumber);
      receiver.addToReadingQueue(packet.getContent(), packet.getSeqNumber(), packet.hasFinalFlag());
    }
    
  }

  private void exitPacketDealer(Packet packet) {
    Packet returnPacket = new Packet(address, port);
    if (packet.hasAcknowledgementFlag()) {
      sendQueue.stopTimeout(packet.getSeqNumber() - 1);
    } else {
      // TODO exit
      returnPacket.setExitFlag();
      returnPacket.setAcknowlegdementFlag();
      DatagramPacket returnDataPacket = returnPacket.makePacket();
      sender = new ReliableSender(returnDataPacket, sendQueue);
      sender.start();
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
    
    Random random = new Random(); 
    int sequenceNumber = random.nextInt();
    packet.setSeqNumber(sequenceNumber);
    
    sender = new ReliableSender(packet.makePacket(), sendQueue, sequenceNumber);
    sender.start();
  }
  
  public void downloadFile(String filename, short filenumber) {
    // TODO file number
    Packet packet = new Packet(address, port);
    packet.setDownloadingFlag();
    packet.setSynchronizeFlag();
    packet.setFileNumber(filenumber);
    packet.setContent(filename.getBytes());
    
    Random random = new Random(); 
    int sequenceNumber = random.nextInt();
    packet.setSeqNumber(sequenceNumber);
    
    sender = new ReliableSender(packet.makePacket(), sendQueue, sequenceNumber);
    sender.start();
  }

  public void queryStatistics() {
    Packet packet = new Packet(address, port);
    packet.setStatisticsFlag();
    packet.setSynchronizeFlag();
    
    Random random = new Random(); 
    int sequenceNumber = random.nextInt();
    packet.setSeqNumber(sequenceNumber);
    
    sender = new ReliableSender(packet.makePacket(), sendQueue, sequenceNumber);
    sender.start();
  }
  
  public void pauseFile() {
    // TODO add file number
    Packet packet = new Packet(address, port);
    packet.setPauseFlag();
    
    Random random = new Random(); 
    int sequenceNumber = random.nextInt();
    packet.setSeqNumber(sequenceNumber);
    
    sender = new ReliableSender(packet.makePacket(), sendQueue, sequenceNumber);
    sender.start();
  }
  
  public void resumeFile() {
    // TODO add file number
    Packet packet = new Packet(address, port);
    packet.setResumeFlag();
    
    Random random = new Random(); 
    int sequenceNumber = random.nextInt();
    packet.setSeqNumber(sequenceNumber);
    
    sender = new ReliableSender(packet.makePacket(), sendQueue, sequenceNumber);
    sender.start();
  }
  
  public void queryDownloadingFilesList() {
    Packet packet = new Packet(address, port);
    packet.setDownloadingFilesListFlag();
    packet.setSynchronizeFlag();
    
    Random random = new Random(); 
    int sequenceNumber = random.nextInt();
    packet.setSeqNumber(sequenceNumber);
    
    sender = new ReliableSender(packet.makePacket(), sendQueue, sequenceNumber);
    sender.start();
  }
  
  public void queryPausedFilesList() {
    Packet packet = new Packet(address, port);
    packet.setPausedFilesListFlag();
    packet.setSynchronizeFlag();
    
    Random random = new Random(); 
    int sequenceNumber = random.nextInt();
    packet.setSeqNumber(sequenceNumber);
    
    sender = new ReliableSender(packet.makePacket(), sendQueue, sequenceNumber);
    sender.start();
  }
  
  public void uploadFile(String filename, short filenumber) {
    // TODO file number
    boolean downloading = false;
    sender = new ReliableSender(filename, sendQueue, filenumber, downloading);
    this.sendingMap.put(filenumber, sender);
    sender.start();
  }
  
  public void exitServer() {
    Packet packet = new Packet(address, port);
    packet.setExitFlag();
    
    Random random = new Random(); 
    int sequenceNumber = random.nextInt();
    packet.setSeqNumber(sequenceNumber);
    
    sender = new ReliableSender(packet.makePacket(), sendQueue, sequenceNumber);
    sender.start();
  }
  
}
