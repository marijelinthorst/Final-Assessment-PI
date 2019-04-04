package com.nedap.university;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

  private static boolean keepAlive = true;
  private static boolean running = false;

  // booleans which determine status of client
  private boolean isFinished = false;

  // ??
  private BufferedReader in;

  /**
   * constructor
   * 
   * @throws IOException
   */
  private ClientMain(Socket sock) throws IOException {
    this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    // TODO moet eerst socket vinden
  }

  /** main */
  public static void main(String[] args) {
    running = true;
    System.out.println("Hello, Nedap University!");

    initShutdownHook();

    while (keepAlive) {
      try {
        // do useful stuff
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    System.out.println("Stopped");
    running = false;
  }

  private static void initShutdownHook() {
    final Thread shutdownThread = new Thread() {
      @Override
      public void run() {
        keepAlive = false;
        while (running) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    };
    Runtime.getRuntime().addShutdownHook(shutdownThread);
  }

  // ---------------- user input ---------------------------------
  public void startUserInput(Scanner userIn) {
    Thread userInTread = new Thread() {
      public void run() {
        userEventLoop(userIn);
      }
    };
    userInTread.start();
  }

  public void userEventLoop(Scanner userIn) {
    while (!isFinished) {
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) { // has slept a bit
      }

      boolean hasInput = false;
      try {
        hasInput = System.in.available() > 0;
      } catch (IOException e) { // has no input :)
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

  public void showPrompt() {
    // TODO
  }

  public boolean shouldAskInput() {
    // TODO
    return false;
  }

  public void dispatchUILine(String input) {
    // TODO
  }

  // ---------------- server input ---------------------
  public void startServerInput() {
    Thread serverInTread = new Thread() {
      public void run() {
        // serverEventLoop();
      }
    };
    serverInTread.start();
  }

  public void serverEventLoop() {
    while (!isFinished) {
      try {
        String inputLine = in.readLine();
        dispatchServerLine(inputLine);
      } catch (IOException e) {
        System.out.println("Sorry, cannot reach server");
        this.shutdown();
      }
    }
  }

  private void dispatchServerLine(String inputLine) {
    // TODO Auto-generated method stub

  }

  // ---------------- shutdown ---------------------
  private void shutdown() {
    // TODO Auto-generated method stub

  }
}
