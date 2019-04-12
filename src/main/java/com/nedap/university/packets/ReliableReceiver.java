package com.nedap.university.packets;

public class ReliableReceiver extends Thread {
//sliding window receiver
 private final static int RECEIVEWINDOWSIZE = 5;
 private int lastFrameReceived;
 private int lastAcknowledgeSend;
 
//TODO Path of a file 
static final String FILEPATH = ""; 
//new File(FILEPATH + "");

}
