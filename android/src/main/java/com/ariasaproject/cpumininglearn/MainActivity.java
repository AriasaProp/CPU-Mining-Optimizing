package com.ariasaproject.cpumininglearn;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.Editable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
  static final String PREF_URI = "Uri";
  static final String PREF_USERNAME = "Username";
  static final String PREF_PASSWORD = "Password";

  EditText uri_value, username_value, password_value;
  Button mining_switch;
  ViewGroup log_container;

  ConsoleMessage co;

  @Override
  protected void onCreate(Bundle b) {
    setContentView(R.layout.main);
    super.onCreate(b);
    mining_switch = (Button) findViewById(R.id.mining_switch);
    uri_value = (EditText) findViewById(R.id.uri_value);
    username_value = (EditText) findViewById(R.id.username_value);
    password_value = (EditText) findViewById(R.id.password_value);
    log_container = (ViewGroup) findViewById(R.id.log_container);
    co = new ConsoleMessage() {
          @Override
          public void sendLog(ConsoleMessage.Message lvl, String msg) {
          	runOnUiThread(new Runnable(){
          		@Override
          		public void run (){
          			sendALog(lvl, msg);
          		}
          	});
          }
        };
    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    if (sharedPref.contains(PREF_URI)) {
      uri_value.setText(sharedPref.getString(PREF_URI, ""));
      username_value.setText(sharedPref.getString(PREF_USERNAME, ""));
      password_value.setText(sharedPref.getString(PREF_PASSWORD,  ""));
    }
    
    
    sendALog(ConsoleMessage.Message.DEBUG, "None");
    sendALog(ConsoleMessage.Message.DEBUG, "None");
    sendALog(ConsoleMessage.Message.DEBUG, "None");
    sendALog(ConsoleMessage.Message.DEBUG, "None!");
    sendALog(ConsoleMessage.Message.DEBUG, "Login App!");
    
  }

  final DateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss|");
  void sendALog(final ConsoleMessage.Message lvl, final String msg) {
  	final int j = log_container.getChildCount();
  	TextView cur = (TextView) log_container.getChildAt(j-1), next;
    for (int i = j-2; i >= 0; i--) {
      next = (TextView) log_container.getChildAt(i);
      cur.setTextColor(next.getCurrentTextColor());
      cur.setText(next.getText());
      cur = next;
    }
    switch (lvl) {
      default:
      case DEBUG:
        cur.setTextColor(0xffa3a3a3);
        break;
      case INFO:
        cur.setTextColor(0xffffffff);
        break;
      case SUCCESS:
        cur.setTextColor(0xff00ff00);
        break;
      case WARNING:
        cur.setTextColor(0xffffff00);
        break;
      case ERROR:
        cur.setTextColor(0xffff0000);
        break;
    }
    cur.setText(logDateFormat.format(new Date()) + msg);
  }
  boolean onMining = false;
  public void startstopMining(final View v) {
  	
    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor edit = sharedPref.edit();
    edit.putString(PREF_URI, uri_value.getText().toString());
    edit.putString(PREF_USERNAME, username_value.getText().toString());
    edit.putString(PREF_PASSWORD, password_value.getText().toString());
  	edit.apply();
  	
    new Thread(miningThread).start();
  }
  final Runnable miningThread = new Runnable() {
    @Override
    public void run() {
      try {
        co.sendLog(ConsoleMessage.Message.SUCCESS, "Begin Thread Run!");
        co.sendLog(ConsoleMessage.Message.DEBUG, "Wait for a sec");
        Thread.sleep(1000);
        co.sendLog(ConsoleMessage.Message.WARNING, "Thread being proccess!");
        co.sendLog(ConsoleMessage.Message.DEBUG, "Wait for 2 sec");
        Thread.sleep(2000);
        co.sendLog(ConsoleMessage.Message.INFO, "Information Update!");
        co.sendLog(ConsoleMessage.Message.DEBUG, "Wait for a sec");
        Thread.sleep(1000);
        co.sendLog(ConsoleMessage.Message.ERROR, "Thread Ended!");
      } catch (InterruptedException e) {
      }
    }
  };
}
