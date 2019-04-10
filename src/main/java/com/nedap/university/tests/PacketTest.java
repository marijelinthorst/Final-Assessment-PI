package com.nedap.university.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nedap.university.packets.Packet;

class PacketTest {
  
  private Packet packet;
  InetAddress address;
  int port = 8080;

  @BeforeEach
  void setUp() throws Exception {
    address = InetAddress.getByName("localhost");
    packet = new Packet(address, port);
  }

  @Test
  void testSyn() {
    assertFalse(packet.hasSyn());
    packet.setSynchronizeFlag();
    packet.flagsToByte();
    assertTrue(packet.hasSyn());
  }

  @Test
  void testAck() {
    assertFalse(packet.hasAcknowledgementFlag());
    packet.setAcknowlegdementFlag();
    packet.flagsToByte();
    assertTrue(packet.hasAcknowledgementFlag());
    }

  @Test
  void testFin() {
    assertFalse(packet.hasFinalFlag());
    packet.setFinalFlag();
    packet.flagsToByte();
    assertTrue(packet.hasFinalFlag());
  }

  @Test
  void testAfl() {
    assertFalse(packet.hasAvailableFilesListFlag());
    packet.setAvailableFilesList();
    packet.flagsToByte();
    assertTrue(packet.hasAvailableFilesListFlag());}

  @Test
  void testDfl() {
    assertFalse(packet.hasDownloadingFilesListFlag());
    packet.setDownloadingFilesList();
    packet.flagsToByte();
    assertTrue(packet.hasDownloadingFilesListFlag());}

  @Test
  void testPfl() {
    assertFalse(packet.hasPausedFilesListFlag());
    packet.setPausedFilesList();
    packet.flagsToByte();
    assertTrue(packet.hasPausedFilesListFlag());}

  @Test
  void testDown() {
    assertFalse(packet.hasDownloadingFlag());
    packet.setDownloadingFlag();
    packet.flagsToByte();
    assertTrue(packet.hasDownloadingFlag());}

  @Test
  void testUp() {
    assertFalse(packet.hasUploadingFLag());
    packet.setUploadingFlag();
    packet.flagsToByte();
    assertTrue(packet.hasUploadingFLag());}

  @Test
  void testPau() {
    assertFalse(packet.hasPauseFlag());
    packet.setPauseFlag();
    packet.flagsToByte();
    assertTrue(packet.hasPauseFlag());}

  @Test
  void testRes() {
    assertFalse(packet.hasResumeFlag());
    packet.setResumeFlag();
    packet.flagsToByte();
    assertTrue(packet.hasResumeFlag());}

  @Test
  void testStat() {
    assertFalse(packet.hasStatisticsFlag());
    packet.setStatisticsFlag();
    packet.flagsToByte();
    assertTrue(packet.hasStatisticsFlag());}

  @Test
  void testSetExit() {
    assertFalse(packet.hasExitFlag());
    packet.setExitFlag();
    packet.flagsToByte();
    assertTrue(packet.hasExitFlag());
  }

  @Test
  void testDifferentFlags() {
    assertFalse(packet.hasExitFlag());
    assertFalse(packet.hasSyn());
    assertFalse(packet.hasResumeFlag());
    assertFalse(packet.hasAvailableFilesListFlag());
    packet.setExitFlag();
    packet.setSynchronizeFlag();
    packet.setResumeFlag();
    packet.setAvailableFilesList();
    packet.flagsToByte();
    assertTrue(packet.hasExitFlag());
    assertTrue(packet.hasSyn());
    assertTrue(packet.hasResumeFlag());
    assertTrue(packet.hasAvailableFilesListFlag());
    assertFalse(packet.hasPauseFlag());
    assertFalse(packet.hasAcknowledgementFlag());
    assertFalse(packet.hasStatisticsFlag());
  }
  
  @Test
  void testFileName() {
    assertEquals(packet.getFileNumber(), 0);
    packet.setFileNumber(5);
    assertEquals(packet.getFileNumber(), 5);
  }

  @Test
  void testSeqNumber() {
    assertEquals(packet.getSeqNumber(), 0);
    packet.setSeqNumber(9234);
    assertEquals(packet.getSeqNumber(), 9234);
  }
  
  
  @Test
  void testAckNumber() {
    assertEquals(packet.getAckNumber(), 0);
    packet.setAckNumber(98234);
    assertEquals(packet.getAckNumber(), 98234);
    }

  @Test
  void testWindowSize() {
    assertEquals(packet.getWindowSize(), 0);
    packet.setWindowSize(3458);
    assertEquals(packet.getWindowSize(), 3458);
    }

  @Test
  void testChecksum() {
    assertEquals(packet.getChecksum(), 0);
    packet.setChecksum(101);
    assertEquals(packet.getChecksum(), 101);
    }

  @Test
  void testContent() {
    Arrays.equals(packet.getContent(), new byte[496]);
    byte[] content = new String("hello").getBytes();
    packet.setContent(content);
    Arrays.equals(packet.getContent(), new String("hello").getBytes());
    }

  @Test
  void testMakeAndReadPacket() {
    packet.setExitFlag();
    packet.setSynchronizeFlag();
    packet.setSeqNumber(9234);
    packet.setChecksum(101);
    
    DatagramPacket newDataPacket = packet.makePacket();
    assertTrue(newDataPacket.getPort() == port);
    assertTrue(newDataPacket.getAddress().equals(address));
    
    Packet newPacket = new Packet (newDataPacket); 
    assertTrue(newPacket.hasExitFlag());
    assertTrue(newPacket.hasSyn());
    assertEquals(newPacket.getSeqNumber(), 9234);
    assertEquals(newPacket.getChecksum(), 101);
  }
}
