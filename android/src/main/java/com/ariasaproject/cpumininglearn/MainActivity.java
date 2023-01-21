package com.ariasaproject.cpumininglearn;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    super.onCreate(b);
    mining_switch = (Button) findViewById(R.id.mining_switch);
    uri_value = (EditText) findViewById(R.id.uri_value);
    username_value = (EditText) findViewById(R.id.username_value);
    password_value = (EditText) findViewById(R.id.password_value);
    final ViewGroup ctr = (ViewGroup) findViewById(R.id.log_container);
    co =
        new ConsoleMessage() {
          final DateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss|");

          @Override
          public void sendLog(ConsoleMessage.Message lvl, String msg) {
            int i = 0;
            TextView vt = (TextView) ctr.getChildAt(0);
            int color = vt.getCurrentTextColor(), color1;
            Editable eT = vt.getText(), eT1;
            vt.setText(logDateFormat.format(new Date()) + msg);
            switch (lvl) {
              default:
              case DEBUG:
                vt.setTextColor(0xffa3a3a3);
                break;
              case INFO:
                vt.setTextColor(0xffffffff);
                break;
              case SUCCESS:
                vt.setTextColor(0xff00ff00);
                break;
              case WARNING:
                vt.setTextColor(0xffffff00);
                break;
              case ERROR:
                vt.setTextColor(0xffff0000);
                break;
            }
            while (i < 20) {
              vt = (TextView) ctr.getChildAt(++i);
              eT1 = vt.getTetxt();
              vt.setText(eT);
              color1 = vt.getCurrentTextColor();
              vt.setTextColor(color);
              eT = eT1;
              color = color1;
            }
          }
        };
    if (b != null && b.containsKey(PREF_URI)) {
      uri_value.setText(b.getString(PREF_URI));
      username_value.setText(b.getString(PREF_USERNAME));
      password_value.setText(b.getString(PREF_PASSWORD));
    }
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
