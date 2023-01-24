package com.example.android.stratumminer;

import static com.example.android.stratumminer.Constants.DEFAULT_PASS;
import static com.example.android.stratumminer.Constants.DEFAULT_PRIORITY;
import static com.example.android.stratumminer.Constants.DEFAULT_RETRYPAUSE;
import static com.example.android.stratumminer.Constants.DEFAULT_THREAD;
import static com.example.android.stratumminer.Constants.DEFAULT_URL;
import static com.example.android.stratumminer.Constants.DEFAULT_USER;
import static com.example.android.stratumminer.Constants.MSG_ACCEPTED_UPDATE;
import static com.example.android.stratumminer.Constants.MSG_CONSOLE_UPDATE;
import static com.example.android.stratumminer.Constants.MSG_REJECTED_UPDATE;
import static com.example.android.stratumminer.Constants.MSG_SPEED_UPDATE;
import static com.example.android.stratumminer.Constants.MSG_STATUS_UPDATE;
import static com.example.android.stratumminer.Constants.MSG_TERMINATED;
import static com.example.android.stratumminer.Constants.PREF_PASS;
import static com.example.android.stratumminer.Constants.PREF_THREAD;
import static com.example.android.stratumminer.Constants.PREF_TITLE;
import static com.example.android.stratumminer.Constants.PREF_URL;
import static com.example.android.stratumminer.Constants.PREF_USER;
import static com.example.android.stratumminer.Constants.STATUS_NOT_MINING;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;
import com.example.android.stratumminer.connection.IMiningConnection;
import com.example.android.stratumminer.connection.StratumMiningConnection;
import com.example.android.stratumminer.worker.CpuMiningWorker;
import com.example.android.stratumminer.worker.IMiningWorker;

public class MinerService extends Service {

  IMiningConnection mc;
  IMiningWorker imw;
  SingleMiningChief smc;
  Console console;
  Boolean running = false;
  float speed = 0;
  int accepted = 0;
  int rejected = 0;
  String status = STATUS_NOT_MINING;
  String cString = "";

  Handler serviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
          Bundle bundle = msg.getData();

          if (msg.arg1 == MSG_CONSOLE_UPDATE) {
            cString = bundle.getString("console");
          } else if (msg.arg1 == MSG_SPEED_UPDATE) {
            speed = bundle.getFloat("speed");
          } else if (msg.arg1 == MSG_STATUS_UPDATE) {
            status = bundle.getString("status");
          } else if (msg.arg1 == MSG_ACCEPTED_UPDATE) {
            accepted = (int) bundle.getLong("accepted");
          } else if (msg.arg1 == MSG_REJECTED_UPDATE) {
            rejected = (int) bundle.getLong("rejected");
          } else if (msg.arg1 == MSG_TERMINATED) {
            running = false;
          }
          super.handleMessage(msg);
        }
      };
  // Binder given to clients
  private final IBinder mBinder = new LocalBinder();

  public class LocalBinder extends Binder {
    MinerService getService() {
      return MinerService.this;
    }
  }

  public MinerService() {}

  public void startMiner() {
    console = new Console(serviceHandler);
    SharedPreferences settings = getSharedPreferences(PREF_TITLE, 0);
    String url, user, pass;
    speed = 0;
    accepted = 0;
    rejected = 0;

    console.write("Service: Start mining");
    url = settings.getString(PREF_URL, "stratum+tcp://us2.litecoin.org:8080");
    user = settings.getString(PREF_USER, "Ariasa.test");
    pass = settings.getString(PREF_PASS, "1234");

    try {
      mc = new StratumMiningConnection(url, user, pass);
      int nThread = settings.getInt(PREF_THREAD, DEFAULT_THREAD);
      imw = new CpuMiningWorker(nThread, DEFAULT_RETRYPAUSE, DEFAULT_PRIORITY, console);
      smc = new SingleMiningChief(mc, imw, console, serviceHandler);
      smc.startMining();
      running = true;
    } catch (Exception e) {
    	for (StackTraceElement t : e.getStackTrace())
    			console.write("Error: "+t.toString());
    }

  }

  public void stopMiner() {
    console.write("Service: Stopping mining");
    Toast.makeText(this, "Worker cooling down, this can take a few minutes", Toast.LENGTH_LONG).show();
    running = false;
    try {
      smc.stopMining();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
}
