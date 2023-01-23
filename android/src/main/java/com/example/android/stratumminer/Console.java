package com.example.android.stratumminer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Console {
  final int MSG_UIUPDATE = 1;
  final int MSG_STATUPDATE = 2;
  final int MSG_CONSOLE_UPDATE = 7;
  private static final DateFormat logDateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");
  StringBuilder sb = new StringBuilder();
  boolean c_new = false;
  String[] console_a = new String[20];
  Handler sHandler = new Handler();

  public Console(Handler h) {
    console_a = new String[20];
    for (int i = 0; i < 20; i++) {
      console_a[i] = "";
    }
    sHandler = h;
  }

  public void write(String s) {
    Message msg = new Message();
    Bundle bundle = new Bundle();

    if (s != null) {
      for (int i = 19; i > 0; i--) {
        console_a[i] = console_a[i - 1];
      }
      console_a[0] = logDateFormat.format(new Date()) + s;
    }
    msg.arg1 = MSG_CONSOLE_UPDATE;
    sb = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      sb.append(console_a[i] + '\n');
    }
    bundle.putString("console", sb.toString());
    msg.setData(bundle);
    sHandler.sendMessage(msg);
  }
}
