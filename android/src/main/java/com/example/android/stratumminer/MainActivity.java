package com.example.android.stratumminer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stratumminer.*;
import com.example.android.stratumminer.connection.IMiningConnection;
import com.example.android.stratumminer.connection.StratumMiningConnection;
import com.example.android.stratumminer.stratum.StratumSocket;
import com.example.android.stratumminer.worker.CpuMiningWorker;
import com.example.android.stratumminer.worker.IMiningWorker;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import static android.R.id.edit;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.android.stratumminer.Constants.DEFAULT_BACKGROUND;
import static com.example.android.stratumminer.Constants.DEFAULT_SCREEN;
import static com.example.android.stratumminer.Constants.PREF_BACKGROUND;
import static com.example.android.stratumminer.Constants.PREF_NEWS_RUN_ONCE;
import static com.example.android.stratumminer.Constants.PREF_PASS;
import static com.example.android.stratumminer.Constants.PREF_SCREEN;
import static com.example.android.stratumminer.Constants.PREF_THREAD;
import static com.example.android.stratumminer.Constants.PREF_TITLE;
import static com.example.android.stratumminer.Constants.PREF_URL;
import static com.example.android.stratumminer.Constants.PREF_USER;

public class MainActivity extends Activity {

    EditText et_serv;
    EditText et_user;
    EditText et_pass;
    CheckBox cb_service;
    CheckBox cb_screen_awake;

    int  baseThreadCount;
    boolean mBound = false;
    MinerService mService;
    public int curScreenPos = 0;
    String unit = " h/s";
    Handler statusHandler = new Handler() {};
    final Runnable rConsole = new Runnable() {
        public void run() {
            TextView txt_console = (TextView) findViewById(R.id.status_textView_console);
            txt_console.setText(mService.cString);
            txt_console.invalidate();
        }
    };

    final Runnable rSpeed = new Runnable() {
        public void run() {
            TextView tv_speed = (TextView) findViewById(R.id.status_textView_speed);
            DecimalFormat df = new DecimalFormat("#.##");
            tv_speed.setText(df.format(mService.speed)+unit);
        }
    };
    final Runnable rAccepted = new Runnable() {
        public void run() {
            TextView txt_accepted = (TextView) findViewById(R.id.status_textView_accepted);
            txt_accepted.setText(String.valueOf(mService.accepted));
        }
    };
    final Runnable rRejected = new Runnable() {
        public void run() {
            TextView txt_rejected = (TextView) findViewById(R.id.status_textView_rejected);
            txt_rejected.setText(String.valueOf(mService.rejected));
        }
    };
    final Runnable rStatus = new Runnable() {
        public void run() {
            TextView txt_status = (TextView) findViewById(R.id.status_textView_status);
            txt_status.setText(mService.status);
        }
    };
    final Runnable rBtnStart= new Runnable() {
        public void run() {
            final Button b = (Button) findViewById(R.id.status_button_startstop);
            b.setText(getString(R.string.main_button_start));
            if (firstRunFlag) {
                b.setEnabled(true);
                b.setClickable(true);
            } else if (StartShutdown) {
                b.setEnabled(false);
                b.setClickable(false);
                if (!ShutdownStarted) {
                    ShutdownStarted = true;
                    CpuMiningWorker worker = (CpuMiningWorker)mService.imw;
                    ThreadStatusAsyncTask threadWaiter = new ThreadStatusAsyncTask();
                    threadWaiter.execute(worker);
                }
            }
        }
    };
    final Runnable rBtnStop= new Runnable() {
        public void run() {
            Button b = (Button) findViewById(R.id.status_button_startstop);
            b.setText(getString(R.string.main_button_stop));
            b.setEnabled(true);
        }
    };

    public volatile  boolean firstRunFlag = true;
    public volatile  boolean ShutdownStarted = false;
    public volatile  boolean StartShutdown = false;
    
    final ServiceConnection mConnection = new ServiceConnection() {
    		@Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MinerService.LocalBinder binder = (MinerService.LocalBinder) service;
            mService = binder.getService();
            updateThread.start();
        }
        public void onServiceDisconnected(ComponentName name) {
        		if (updateThread.isAlive()) updateThread.interrupt();
        }
    };
    final Thread updateThread = new Thread () {
    		@Override
        public void run() {
            try {
		        		for (;;) {
		                if(mService.running) { statusHandler.post(rBtnStop);
		                } else {statusHandler.post(rBtnStart);}
                    sleep(1000);
		                statusHandler.post(rConsole);
		                statusHandler.post(rSpeed);
		                statusHandler.post(rAccepted);
		                statusHandler.post(rRejected);
		                statusHandler.post(rStatus);
		        		}
            } catch (InterruptedException e) {}
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        et_serv = (EditText) findViewById(R.id.server_et);
        et_user = (EditText) findViewById((R.id.user_et));
        et_pass = (EditText) findViewById(R.id.password_et);
        cb_service = (CheckBox) findViewById(R.id.settings_checkBox_background) ;
        cb_service.setChecked(DEFAULT_BACKGROUND);
        cb_screen_awake = (CheckBox) findViewById(R.id.settings_checkBox_keepscreenawake) ;
        cb_screen_awake.setChecked(DEFAULT_SCREEN);
        SharedPreferences settings = getSharedPreferences(PREF_TITLE, 0);
        et_serv.setText(settings.getString(PREF_URL,""));
        et_user.setText(settings.getString(PREF_USER,""));
        et_pass.setText(settings.getString(PREF_PASS,""));
        
        //set number of Threads posibility use
        try {
            Spinner threadList = (Spinner)findViewById(R.id.spinner1);
            String[] threadsAvailable = new String[Runtime.getRuntime().availableProcessors()];
            for(int i = 0; i <= Runtime.getRuntime().availableProcessors();i++) {
                threadsAvailable[i] = Integer.toString(i + 1);
                ArrayAdapter threads = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, threadsAvailable);
                threadList.setAdapter(threads);
            }
        } catch (Exception e){}
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Intent intent = new Intent(getApplicationContext(), MinerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
		public void startstopMining(View v) {
				final Button b = (Button) v;
				if (b.getText().equals(getString(R.string.status_button_start))) {
				   String url = et_serv.getText().toString();
				   String user = et_user.getText().toString();
				   String pass = et_pass.getText().toString();
				   Spinner threadList = (Spinner)findViewById(R.id.spinner1);
				   int threads = Integer.parseInt(threadList.getSelectedItem().toString());
				   SharedPreferences settings = getSharedPreferences(PREF_TITLE, 0);
				   SharedPreferences.Editor editor = settings.edit();
				   settings = getSharedPreferences(PREF_TITLE, 0);
				   editor = settings.edit();
				   editor.putString(PREF_URL, url);
				   editor.putString(PREF_USER, user);
				   editor.putString(PREF_PASS, pass);
				   editor.putInt(PREF_THREAD, threads);
				   editor.putBoolean(PREF_BACKGROUND, cb_service.isChecked());
				   editor.putBoolean(PREF_SCREEN, cb_screen_awake.isChecked());
				   editor.commit();
				   if(settings.getBoolean(PREF_SCREEN,DEFAULT_SCREEN )) {
				       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				   }
				   mService.startMiner();
				   firstRunFlag = false;
				   b.setText(getString(R.string.main_button_stop));
				} else{
				   mService.stopMiner();
				   StartShutdown = true;
				   b.setText(getString(R.string.status_button_start));
				}
		}
		
		public void startMining() {
		   mService.startMiner();
		}
		public void stopMining() {
		   mService.stopMiner();
		}
		
		public void setButton(boolean flag) {
				final Button btn = (Button) findViewById(R.id.status_button_startstop);
        btn.setEnabled(flag);
        btn.setClickable(flag);
		}
		
    public class ThreadStatusAsyncTask extends AsyncTask<CpuMiningWorker,Integer,Boolean> {
        @Override
        protected Boolean doInBackground(CpuMiningWorker... params) {
            long lastTime = System.currentTimeMillis();
            long currTime;
            while (params[0].getThreadsStatus()) {
                currTime = System.currentTimeMillis();
                double deltaTime = (double)(currTime-lastTime)/1000.0;
                if (deltaTime>15.0) {
                    params[0].ConsoleWrite("Still cooling down...");
                    lastTime = currTime;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                final Button btn = (Button) findViewById(R.id.status_button_startstop);
				        btn.setEnabled(true);
				        btn.setClickable(true);
                ShutdownStarted = false;
                StartShutdown = false;
                firstRunFlag = true;
                Toast.makeText(MainActivity.this,"Cooldown finished",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        SharedPreferences settings = getSharedPreferences(PREF_TITLE, 0);
        if (settings.getBoolean(PREF_BACKGROUND, DEFAULT_BACKGROUND)) {
            TextView tv_background = (TextView) findViewById(R.id.status_textView_background);
            tv_background.setText("RUN IN BACKGROUND");
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if(updateThread.isAlive()) updateThread.interrupt();

        SharedPreferences settings = getSharedPreferences(PREF_TITLE, 0);
        if(!settings.getBoolean(PREF_BACKGROUND,DEFAULT_BACKGROUND )) {
            if (mService != null && mService.running) { mService.stopMiner(); }
            Intent intent = new Intent(getApplicationContext(), MinerService.class);
            stopService(intent);
        }

        try {
            unbindService(mConnection);
        } catch (RuntimeException e) {}

        super.onStop();
    }
}
