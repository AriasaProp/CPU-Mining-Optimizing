package com.ariasaproject.cpuminingopt;

import static com.ariasaproject.cpuminingopt.Constants.DEFAULT_PRIORITY;
import static com.ariasaproject.cpuminingopt.Constants.DEFAULT_RETRYPAUSE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_ACCEPTED_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_REJECTED_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_SPEED_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_STARTED;
import static com.ariasaproject.cpuminingopt.Constants.MSG_STATUS_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_TERMINATED;
import static com.ariasaproject.cpuminingopt.Constants.PREF_BACKGROUND;
import static com.ariasaproject.cpuminingopt.Constants.PREF_PASS;
import static com.ariasaproject.cpuminingopt.Constants.PREF_SCREEN;
import static com.ariasaproject.cpuminingopt.Constants.PREF_THREAD;
import static com.ariasaproject.cpuminingopt.Constants.PREF_URL;
import static com.ariasaproject.cpuminingopt.Constants.PREF_USER;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.core.text.HtmlCompat;
import com.ariasaproject.cpuminingopt.connection.IMiningConnection;
import com.ariasaproject.cpuminingopt.connection.StratumMiningConnection;
import java.text.DecimalFormat;

public class MainActivity extends Activity {
  static {
    System.loadLibrary("ext");
  }

  IMiningConnection mc;
  IMiningWorker imw;
  SingleMiningChief smc;

  EditText et_serv;
  EditText et_user;
  EditText et_pass;
  CheckBox cb_background_run;
  CheckBox cb_keep_awake;

  String unit = " h/s";
  final Handler statusHandler =
      new Handler() {
        final DecimalFormat df = new DecimalFormat("#.##");
        int acc, recj;
        @Override
        public void handleMessage(Message msg) {
          final Bundle bundle = msg.getData();
          if ((msg.arg1 & MSG_STARTED) == MSG_STARTED) {
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            if (settings.getBoolean(PREF_SCREEN, false)) {
              getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            Button b = (Button) findViewById(R.id.status_button_startstop);
            b.setText(R.string.main_button_stop);
            b.setEnabled(true);
            b.setClickable(true);
          }
          if ((msg.arg1 & MSG_TERMINATED) == MSG_TERMINATED) {
            if (imw != null) {
              CpuMiningWorker w = (CpuMiningWorker) imw;
              long lastTime = System.currentTimeMillis();
              long currTime;
              while (w.getThreadsStatus()) {
                currTime = System.currentTimeMillis();
                long deltaTime = currTime - lastTime;
                if (deltaTime > 15000.0) {
                  Console.send(0, "Still cooling down...");
                  lastTime = currTime;
                }
              }
              imw = null;
            }
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            if (settings.getBoolean(PREF_SCREEN, false)) {
              getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            Button b = (Button) findViewById(R.id.status_button_startstop);
            b.setText(R.string.main_button_start);
            b.setEnabled(true);
            b.setClickable(true);
          }
          if ((msg.arg1 & MSG_SPEED_UPDATE) == MSG_SPEED_UPDATE) {
            TextView tv_speed = (TextView) findViewById(R.id.status_textView_speed);
            tv_speed.setText(df.format(bundle.getFloat("speed")) + unit);
          }
          if ((msg.arg1 & MSG_STATUS_UPDATE) == MSG_STATUS_UPDATE) {
            TextView txt_status = (TextView) findViewById(R.id.status_textView_status);
            txt_status.setText(bundle.getString("status"));
          }
          if ((msg.arg1 & MSG_RESULT_UPDATE) == MSG_ACCEPTED_UPDATE) {
            TextView txt_result = (TextView) findViewById(R.id.status_textView_result);
            txt_result.setText(bundle.getString("result"));
          }
          if ((msg.arg1 & MSG_SIGNAL_UPDATE) == MSG_SIGNAL_UPDATE) {
            TextView txt_signal = (TextView) findViewById(R.id.status_textView_signal);
            txt_signal.setText(bundle.getString("signal"));
          }
          super.handleMessage(msg);
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_main);
    super.onCreate(savedInstanceState);
    final TextView txt_console = (TextView) findViewById(R.id.status_textView_console);
    Console.setReceiver(
        new Console.Receiver() {
          @Override
          public void receive(String msgs) {
            statusHandler.post(
                new Runnable() {
                  @Override
                  public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                      txt_console.setText(
                          HtmlCompat.fromHtml(msgs, HtmlCompat.FROM_HTML_MODE_LEGACY),
                          TextView.BufferType.SPANNABLE);
                    else txt_console.setText(Html.fromHtml(msgs), TextView.BufferType.SPANNABLE);
                  }
                });
          }
        });

    et_serv = (EditText) findViewById(R.id.server_et);
    et_user = (EditText) findViewById((R.id.user_et));
    et_pass = (EditText) findViewById(R.id.password_et);
    cb_background_run = (CheckBox) findViewById(R.id.settings_checkBox_background);
    cb_keep_awake = (CheckBox) findViewById(R.id.settings_checkBox_keepscreenawake);
    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
    et_serv.setText(settings.getString(PREF_URL, "stratum+tcp://us2.litecoinpool.org:8080"));
    et_user.setText(settings.getString(PREF_USER, "Ariasa.test"));
    et_pass.setText(settings.getString(PREF_PASS, "1234"));
    cb_keep_awake.setChecked(settings.getBoolean(PREF_SCREEN, false));
    cb_background_run.setChecked(settings.getBoolean(PREF_BACKGROUND, false));
    // set number of Threads posibility use
    try {
      Spinner threadList = (Spinner) findViewById(R.id.spinner1);
      String[] threadsAvailable = new String[Runtime.getRuntime().availableProcessors()];
      for (int i = 0; i <= Runtime.getRuntime().availableProcessors(); i++) {
        threadsAvailable[i] = Integer.toString(i + 1);
        ArrayAdapter threads =
            new ArrayAdapter<CharSequence>(
                this, android.R.layout.simple_spinner_item, threadsAvailable);
        threadList.setAdapter(threads);
      }
    } catch (Exception e) {
    }
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
  }

  public void startstopMining(View v) {
    final Button b = (Button) v;
    b.setEnabled(false);
    b.setClickable(false);
    if (b.getText().equals(getString(R.string.main_button_start))) {
      b.setText(R.string.main_button_onstart);
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  String url = et_serv.getText().toString();
                  String user = et_user.getText().toString();
                  String pass = et_pass.getText().toString();
                  Spinner threadList = (Spinner) findViewById(R.id.spinner1);
                  int threads = Integer.parseInt(threadList.getSelectedItem().toString());
                  Console.send(1, "Try to start mining");
                  try {
                    mc = new StratumMiningConnection(url, user, pass);
                    imw = new CpuMiningWorker(threads, DEFAULT_RETRYPAUSE, DEFAULT_PRIORITY);
                    smc = new SingleMiningChief(mc, imw, statusHandler);
                    smc.startMining();
                    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_URL, url);
                    editor.putString(PREF_USER, user);
                    editor.putString(PREF_PASS, pass);
                    editor.putInt(PREF_THREAD, threads);
                    editor.putBoolean(PREF_BACKGROUND, cb_background_run.isChecked());
                    editor.putBoolean(PREF_SCREEN, cb_keep_awake.isChecked());
                    editor.commit();
                    final Message msg = statusHandler.obtainMessage();
                    msg.arg1 = MSG_STARTED;
                    statusHandler.sendMessage(msg);
                  } catch (Exception e) {
                    Console.send(4, "Error start mining : " + e.toString());
                    final Message msg = statusHandler.obtainMessage();
                    msg.arg1 = MSG_TERMINATED;
                    statusHandler.sendMessage(msg);
                  }
                }
              })
          .start();
    } else {
      b.setText(R.string.main_button_onstop);
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  Console.send(1, "try stopping mining");
                  try {
                    smc.stopMining();
                  } catch (Exception e) {
                    Console.send(4, "Exception: " + e.getMessage());
                  }
                  final Message msg = statusHandler.obtainMessage();
                  msg.arg1 = MSG_TERMINATED;
                  statusHandler.sendMessage(msg);
                }
              })
          .start();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    // savedInstanceState.putString("console", ((TextView)
    // findViewById(R.id.status_textView_console)).getText().toString());
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
    if (settings.getBoolean(PREF_BACKGROUND, false)) {
      TextView tv_background = (TextView) findViewById(R.id.status_textView_background);
      tv_background.setText("RUN IN BACKGROUND");
    }
  }

  @Override
  protected void onStop() {
    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
    if (!settings.getBoolean(PREF_BACKGROUND, false) && (imw != null)) {
      Console.send(0, "Try to stop mining");
      try {
        smc.stopMining();
      } catch (Exception e) {
        Console.send(4, "Error: " + e.getMessage());
      }
    }
    super.onStop();
  }
}
