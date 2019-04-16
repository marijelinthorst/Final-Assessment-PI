package com.nedap.university.tests;

public class TestThread extends Thread {

  private String toPrint;
  
  public TestThread (String toPrint) {
    this.toPrint = toPrint;
  }
  
  public void run() {
    while (true) {
      System.out.println(toPrint);
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        continue;
      }
    }
  }
}
