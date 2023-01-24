package com.example.android.stratumminer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
  public static void setReceiver(Receiver r) {
  		this._receiver = r;
  }
  public static void send(int lvl, String s) {
    if (s != null) {
	    	int i = 20;
	      while (--i > 0) {
		        messages[i] = messages[i - 1];
		      	levels[i] = levels[i - 1];
	      }
	      messages[0] = logDateFormat.format(new Date()) + s;
	      levels[0] = lvl;
    }
    if (this._receiver != null)
  			_receiver.receive(levels, messages);
  }
  
  public static interface Receiver {
  	public static final int MAX_LENGTH = 20;
  	public void receive(int[] lvls, String[] msgs);
  }
}
