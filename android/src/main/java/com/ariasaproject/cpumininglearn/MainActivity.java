package com.ariasaproject.cpumininglearn;

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

  ConsoleMessage co;

  @Override
  protected void onCreate(Bundle b) {
    setContentView(R.layout.main);
    mining_switch = (Button) findViewById(R.id.mining_switch);
    uri_value = (EditText) findViewById(R.id.uri_value);
    username_value = (EditText) findViewById(R.id.username_value);
    password_value = (EditText) findViewById(R.id.password_value);
    final ViewGroup ctr = (ViewGroup) findViewById(R.id.log_container);
    final int j = ctr.getChildCount();
    co = new ConsoleMessage() {
          final DateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss|");
          @Override
          public void sendLog(ConsoleMessage.Message lvl, String msg) {
          	TextView cur = (TextView) ctr.getChildAt(j-1), next;
            for (int i = j-2; i >= 0; i--) {
              next = (TextView) ctr.getChildAt(i);
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
        };
    /*
    if (b != null && b.containsKey(PREF_URI)) {
      uri_value.setText(b.getString(PREF_URI));
      username_value.setText(b.getString(PREF_USERNAME));
      password_value.setText(b.getString(PREF_PASSWORD));
    }
    */
    super.onCreate(b);
    
    co.sendLog(ConsoleMessage.Message.DEBUG, "None");
    co.sendLog(ConsoleMessage.Message.DEBUG, "None");
    co.sendLog(ConsoleMessage.Message.DEBUG, "None");
    co.sendLog(ConsoleMessage.Message.DEBUG, "None!");
    co.sendLog(ConsoleMessage.Message.DEBUG, "Login App!");
    
  }

  @Override
  protected void onSaveInstanceState(Bundle b) {
    super.onSaveInstanceState(b);
    b.putString(PREF_URI, uri_value.getText().toString());
    b.putString(PREF_USERNAME, username_value.getText().toString());
    b.putString(PREF_PASSWORD, password_value.getText().toString());
  }

  void sendAL(final ConsoleMessage.Message lvl, final String ls) {
    runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            co.sendLog(lvl, ls);
          }
        });
  }
  // URL m_url;
  public void startstopMining(final View v) {
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  sendAL(ConsoleMessage.Message.SUCCESS, "Begin Thread Run!");
                  sendAL(ConsoleMessage.Message.DEBUG, "Wait for a sec");
                  Thread.sleep(1000);
                  sendAL(ConsoleMessage.Message.WARNING, "Thread being proccess!");
                  sendAL(ConsoleMessage.Message.DEBUG, "Wait for 2 sec");
                  Thread.sleep(2000);
                  sendAL(ConsoleMessage.Message.INFO, "Information Update!");
                  sendAL(ConsoleMessage.Message.DEBUG, "Wait for a sec");
                  Thread.sleep(1000);
                  sendAL(ConsoleMessage.Message.ERROR, "Thread Ended!");
                } catch (InterruptedException e) {
                }
              }
            })
        .start();
  }
}
