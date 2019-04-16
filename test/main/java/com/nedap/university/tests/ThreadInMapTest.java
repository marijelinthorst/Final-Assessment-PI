package com.nedap.university.tests;

import java.util.Arrays;
import java.util.HashMap;

public class ThreadInMapTest {
  private static TestThread test;
  private  static HashMap<Integer, TestThread> map;
  
  public static void main(String[] args) {

    map = new HashMap<Integer, TestThread>();
    
    test = new TestThread("1: Ik ben aan");
    System.out.println(test);
    map.put(1, test);
    test.start();
    
    test = new TestThread("2: Ik ben aan");
    System.out.println(test);
    map.put(2, test);
    test.start();
    
    System.out.println(map.get(1));
    System.out.println(map.get(2));
    
    
    String filename = "Plattegrond.jpg";
    byte[] filenameBytes = filename.getBytes();
    
    System.out.println(filename);
    System.out.println(Arrays.toString(filenameBytes));
    
    String newFilename = new String (filenameBytes);
    System.out.println(newFilename);
  }
}

