package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.nedap.university.packets.PacketDealer;

public class Client {


  // booleans which determine status of client
  private boolean isFinished = false;

  // TODO give name??
  private static InetAddress serverAddress;
  private static int clientPort = 9090;
  private static int serverPort =8080;
  private static DatagramSocket socket;
  private static InetAddress broadcastIP;
  private Scanner userIn;
  
  // Packet info
  private int packetlength = 512;
  private PacketDealer dealer;

  /** main */
  public static void main(String[] args) {
    
    System.out.println("Start client");
    // make socket, broadcastIP, a packet to send and a package to receive
    try {
      socket = new DatagramSocket(clientPort);
      socket.setBroadcast(true);
      broadcastIP = InetAddress.getByName("localhost");
    } catch (SocketException e) {
      System.out.println("ERROR: couldn't construct a DatagramSocket object!");
      e.printStackTrace();
    } catch (UnknownHostException e) {
      System.out.println("ERROR: no valid hostname!");
      e.printStackTrace();
    }
    
    String broadcastMessage = "SYN";
    byte[] broadcast = broadcastMessage.getBytes();
    DatagramPacket broadcastPacket = new DatagramPacket(broadcast, broadcast.length, broadcastIP, serverPort);
    byte[] responseBuffer = new byte[512];
    DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
    System.out.println("Client made broadcast message");
    
    // send broadcast message and receive response from the server
    try {
      socket.send(broadcastPacket);
      System.out.println("Client send broadcast message");
      socket.receive(responsePacket);
      System.out.println("Client received message");
    } catch (IOException e) {
      System.out.println("ERROR: couldn't send or receive broadcast message!");
      e.printStackTrace();
    }
    
    // get info from response packet from the server
    serverAddress = responsePacket.getAddress();
    serverPort = responsePacket.getPort(); 
      
    // Send ack
    String ackMessage = "ACK";
    byte[] ack = ackMessage.getBytes();
    DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, serverAddress, serverPort);
    try {
      socket.send(ackPacket);
      System.out.println("Client send ack");
    } catch (IOException e) {
      System.out.println("ERROR: couldn't send ack message!");
      e.printStackTrace();
    }
    
    // construct new client object and start the user and server input threads
    Client client = new Client();
    System.out.println("Client constructed");
    client.startUserInput();
    client.startServerInput();
  }
  
  /**
   * constructor
   */
  public Client() {
    userIn = new Scanner(System.in);
    dealer = new PacketDealer(socket, serverAddress, serverPort);
  }


  // ---------------- user input ---------------------------------
  public void startUserInput() {
    Thread userInTread = new Thread() {
      public void run() {
        userEventLoop();
      }
    };
    userInTread.start();
  }

  public void userEventLoop() {
    while (!isFinished) {
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) { // has slept a bit
      }

      boolean hasInput = false;
      try {
        hasInput = System.in.available() > 0;
      } catch (IOException e) { // has no input (this is to be able to exit at any time)
      }

      if (shouldAskInput() || hasInput) {
        if (shouldAskInput()) {
          showPrompt();
        }
        if (userIn.hasNext()) {
          String inputLine = userIn.nextLine();
          dispatchUILine(inputLine);
        }
      }
    }
    userIn.close();
  }
  
  public boolean shouldAskInput() {
    // TODO
    return true;
  }
  
  public void showPrompt() {
    // TODO determine intuitive TUI
    System.out.println("What do you want to do:");
    System.out.println("1. See a list of files the server has available for downloading");
    System.out.println("2. Download a file from the server");
    System.out.println("3. See uploading/downloading statistics");
    System.out.println("4. Pause a file");
    System.out.println("5. Resume a file");
    System.out.println("6. See a list of files currently being downloaded");
    System.out.println("7. See a list of files currently paused"); 
    System.out.println("8. Upload file to the server");
    System.out.println("9. Exit");
    System.out.println("Please enter the number of the action you wish to do");
  }

  public void dispatchUILine(String input) {
    switch (input) {
      case "1":
        System.out.println("1");
        dealer.queryAvailableFilesList();
        break;
      case "2":
        System.out.println("2");
        // TODO select a file
        dealer.downloadFile();
        break;
      case "3":
        System.out.println("3");
        dealer.queryStatistics();
        break;
      case "4":
        System.out.println("4");
        // TODO select a file
        dealer.pauseFile();
        break;
      case "5":
        System.out.println("5");
        // TODO select a file
        dealer.resumeFile();
        break;
      case "6":
        System.out.println("6");
        dealer.queryDownloadingFilesList();
        break;
      case "7":
        System.out.println("7");
        dealer.queryPausedFilesList();
        break;
      case "8":
        System.out.println("8");
        // TODO select a file
        dealer.uploadFile();
        break;
      case "9":
        System.out.println("9");
        // TODO sluit zelf ook af
        dealer.exitServer();
        break;
      default:
        System.out.println("That number is not  a valid choice!");
        break;
    }
  }

  // ---------------- server input ---------------------
  public void startServerInput() {
    Thread serverInTread = new Thread() {
      public void run() {
         serverEventLoop();
      }
    };
    serverInTread.start();
  }

  public void serverEventLoop() {
    while (!isFinished) {
      try {
        byte[] buffer = new byte[packetlength];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivePacket);
        // TODO cannot receive until packet is read. Change this?
        dealer.readPackage(receivePacket);
      } catch (IOException e) {
        System.out.println("Sorry, cannot reach server");
        this.shutdown();
      }
    }
  }

  // ---------------- shutdown ---------------------
  private void shutdown() {
    System.out.println("Shutting down");
    this.isFinished = true;
    // TODO
  }
}
