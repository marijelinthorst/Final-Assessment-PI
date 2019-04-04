package com.nedap.university;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientMain {


  // booleans which determine status of client
  private boolean isFinished = false;

  // TODO give name??
  private static InetAddress host;
  private static int clientPort = 8080;
  private static int serverPort;
  private static DatagramSocket socket;
  private static InetAddress broadcastIP;
  private Scanner userIn;
  
  // Packet info
  private int packetlength = 512;

  /** main */
  public static void main(String[] args) {
    try {
      socket = new DatagramSocket();
      socket.setBroadcast(true);
      broadcastIP = InetAddress.getByName("255.255.255.255");
    } catch (SocketException e) {
      System.out.println("ERROR: couldn't construct a DatagramSocket object!");
      e.printStackTrace();
    } catch (UnknownHostException e) {
      System.out.println("ERROR: no valid hostname!");
      e.printStackTrace();
    }
    
    String broadcastMessage = "Hello"; // TODO: misschien eigen header hier
    byte[] broadcast = broadcastMessage.getBytes();
    DatagramPacket broadcastPacket = new DatagramPacket(broadcast, broadcast.length, broadcastIP, clientPort);
    byte[] responseBuffer = new byte[512];
    DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
    
    // send broadcast message and receive response from the server
    try {
      socket.send(broadcastPacket);     
      socket.receive(responsePacket);
    } catch (IOException e) {
      System.out.println("ERROR: couldn't send or receive broadcast message!");
      e.printStackTrace();
    }
    
    // get info from response packet from the server, data is useless at this point
    host = responsePacket.getAddress();
    serverPort = responsePacket.getPort();
    
    
    // construct new client object and start the user and server input threads
    ClientMain client = new ClientMain();
    client.startUserInput();
    client.startServerInput();
  }
  
  /**
   * constructor
   * 
   * @throws IOException
   */
  private ClientMain() {
    userIn = new Scanner(System.in);
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
    return false;
  }
  
  public void showPrompt() {
    // TODO determine intuitive TUI
    System.out.println("What do you want to do:");
    System.out.println("1. Upload file to the server");
    System.out.println("2. See a list of files the server has available for downloading");
    System.out.println("3. See a list of files currently being downloading and/or uploading");
    System.out.println("4. See uploading/downloading statistics");
    System.out.println("5. Select a file to download, pause or resume downloading");
    System.out.println("6. Exit");
    System.out.println("Please enter the number of the action you wish to do");
  }

  public void dispatchUILine(String input) {
    switch (input) {
      case "1":
        System.out.println("1");
        break;
      case "2":
        System.out.println("2");
        break;
      case "3":
        System.out.println("3");
        break;
      case "4":
        System.out.println("4");
        break;
      case "5":
        System.out.println("5");
        break;
      case "6":
        System.out.println("6");
        break;
      default:
        System.out.println("That number is not  a valid choice!");
        break;
    }
    // TODO add actions for each case
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
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length, broadcastIP, port);
        socket.receive(receivePacket);
        dispatchServerLine(receivePacket);
      } catch (IOException e) {
        System.out.println("Sorry, cannot reach server");
        this.shutdown();
      }
    }
  }

  private void dispatchServerLine(DatagramPacket inputLine) {
    // TODO Auto-generated method stub

  }

  // ---------------- shutdown ---------------------
  private void shutdown() {
    // TODO Auto-generated method stub

  }
}
