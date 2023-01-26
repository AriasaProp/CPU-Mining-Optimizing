package com.ariasaproject.cpuminingopt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Console {
  static final int MSG_UIUPDATE = 1;
  static final int MSG_STATUPDATE = 2;
  static final int MSG_CONSOLE_UPDATE = 7;
  private static final DateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss | ");
  private static Receiver _receiver;
  private static String[] messages = new String[20];
  private static int[] levels = new int[20];
  static {
    for (int i = 0; i < 20; i++) {
      messages[i] = "";
      levels[i] = 0;
    }
  }
	static {
		System.loadLibrary("ext");
	}

  public static void setReceiver(Receiver r) {
  		_receiver = r;
  }
  private static native void initialize();
  private static native String outLogs();
  private static native void write(int lvl, String s);
  private static native void destroy();
  public static void send(int lvl, String s) {
  	/*
    if (s != null) {
	    	int i = 20;
	      while (--i > 0) {
		        messages[i] = messages[i - 1];
		      	levels[i] = levels[i - 1];
	      }
	      messages[0] = logDateFormat.format(new Date()) + s;
	      levels[0] = lvl;
    }
    */
    write(lvl, s);
    if (_receiver != null)
  			_receiver.receive(outLogs());
  }
  
  public static interface Receiver {
  	public static final int MAX_LENGTH = 20;
  	public void receive(String msgs);
  }
  
}
