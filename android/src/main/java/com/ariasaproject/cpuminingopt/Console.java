package com.ariasaproject.cpuminingopt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.ariasaproject.cpuminingopt.*;

public class Console {
  private static final DateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss | ");
  private static Receiver _receiver;
  
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
  	public void receive(String msgs);
  }
  
}
