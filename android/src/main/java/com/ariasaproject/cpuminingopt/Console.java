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
  static {
    for (int i = 0; i < 20; i++) {
      messages[i] = "";
      levels[i] = 0;
    }
  }

  public static void setReceiver(Receiver r) {
  		_receiver = r;
  }
  private static native String outLogs();
  private static native void write(int lvl, String s);
  public static void send(int lvl, String s) {
    write(lvl, s);
    if (_receiver != null)
  			_receiver.receive(outLogs());
  }
  
  public static interface Receiver {
  	public static final int MAX_LENGTH = 20;
  	public void receive(String msgs);
  }
  
}
