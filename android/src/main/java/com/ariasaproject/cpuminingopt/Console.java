package com.ariasaproject.cpuminingopt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.ariasaproject.cpuminingopt.*;

public class Console {
  private static Receiver _receiver;
  
  public static void setReceiver(Receiver r) {
  		_receiver = r;
  }
  private static native String write(int lvl, String s);
  public static void send(int lvl, String s) {
    if (_receiver != null)
  			_receiver.receive(write(lvl, s));
  	
  }
  
  public static interface Receiver {
  	public void receive(String msgs);
  }
}
