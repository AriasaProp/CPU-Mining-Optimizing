package com.ariasaproject.cpuminingopt;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.ariasaproject.cpuminingopt.*;
import com.ariasaproject.cpuminingopt.connection.IMiningConnection;
import com.ariasaproject.cpuminingopt.connection.StratumMiningConnection;
import com.ariasaproject.cpuminingopt.stratum.StratumSocket;
import com.ariasaproject.cpuminingopt.worker.CpuMiningWorker;
import com.ariasaproject.cpuminingopt.worker.IMiningWorker;

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
import static com.ariasaproject.cpuminingopt.Constants.*;

import static com.ariasaproject.cpuminingopt.Constants.MSG_UIUPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_STARTED;
import static com.ariasaproject.cpuminingopt.Constants.MSG_TERMINATED;
import static com.ariasaproject.cpuminingopt.Constants.MSG_SPEED_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_STATUS_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_ACCEPTED_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_REJECTED_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_CONSOLE_UPDATE;

import static com.ariasaproject.cpuminingopt.Constants.PREF_URL;
import static com.ariasaproject.cpuminingopt.Constants.PREF_USER;
import static com.ariasaproject.cpuminingopt.Constants.PREF_PASS;
import static com.ariasaproject.cpuminingopt.Constants.PREF_THREAD;
import static com.ariasaproject.cpuminingopt.Constants.PREF_THROTTLE;
import static com.ariasaproject.cpuminingopt.Constants.PREF_SCANTIME;
import static com.ariasaproject.cpuminingopt.Constants.PREF_RETRYPAUSE;
import static com.ariasaproject.cpuminingopt.Constants.PREF_SERVICE;
import static com.ariasaproject.cpuminingopt.Constants.PREF_PRIORITY;
import static com.ariasaproject.cpuminingopt.Constants.PREF_BACKGROUND;
import static com.ariasaproject.cpuminingopt.Constants.PREF_SCREEN;

import static com.ariasaproject.cpuminingopt.Constants.DEFAULT_PRIORITY;
import static com.ariasaproject.cpuminingopt.Constants.DEFAULT_THREAD;
import static com.ariasaproject.cpuminingopt.Constants.DEFAULT_SCANTIME;
import static com.ariasaproject.cpuminingopt.Constants.DEFAULT_RETRYPAUSE;
import static com.ariasaproject.cpuminingopt.Constants.DEFAULT_THROTTLE;

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
    CheckBox cb_service;
    CheckBox cb_screen_awake;

    int  baseThreadCount;
    String unit = " h/s";
    final Handler statusHandler = new Handler() {
      final DecimalFormat df = new DecimalFormat("#.##");
      @Override
      public void handleMessage(Message msg) {
          final Bundle bundle = msg.getData();
          switch (msg.arg1) {
          		case MSG_STARTED:
	          			{
		          				Button b = (Button) findViewById(R.id.status_button_startstop);
		          				b.setText(getString(R.string.main_button_stop));
							        b.setEnabled(true);
							        b.setClickable(true);
	          			}
          				break;
          		case MSG_TERMINATED:
          				if (imw != null) {
			                CpuMiningWorker w = (CpuMiningWorker)imw;
			                long lastTime = System.currentTimeMillis();
					            long currTime;
					            while (w.getThreadsStatus()) {
					                currTime = System.currentTimeMillis();
					                long deltaTime = currTime-lastTime;
					                if (deltaTime>15000.0) {
					                    w.ConsoleWrite("Still cooling down...");
					                    lastTime = currTime;
					                }
					            }
					            imw = null;
          				}
          				
          				{
		          				Button b = (Button) findViewById(R.id.status_button_startstop);
		          				b.setText(getString(R.string.main_button_start));
							        b.setEnabled(true);
							        b.setClickable(true);
          				}
          				break;
          		case MSG_SPEED_UPDATE:
			            TextView tv_speed = (TextView) findViewById(R.id.status_textView_speed);
			            tv_speed.setText(df.format(bundle.getFloat("speed"))+unit);
          				break;
          		case MSG_STATUS_UPDATE:
			            TextView txt_status = (TextView) findViewById(R.id.status_textView_status);
			            txt_status.setText(bundle.getString("status"));
          				break;
          		case MSG_ACCEPTED_UPDATE:
			            TextView txt_accepted = (TextView) findViewById(R.id.status_textView_accepted);
			            txt_accepted.setText(String.valueOf(bundle.getLong("accepted")));
          				break;
          		case MSG_REJECTED_UPDATE:
			            TextView txt_rejected = (TextView) findViewById(R.id.status_textView_rejected);
			            txt_rejected.setText(String.valueOf(bundle.getLong("rejected")));
          				break;
          		case MSG_CONSOLE_UPDATE:
  								TextView txt_console = (TextView) findViewById(R.id.status_textView_console);
			            txt_console.setText(bundle.getString("console"));
			            txt_console.invalidate();
          				break;
	          	default:
	          			break;
          }
          super.handleMessage(msg);
      }
    };
    

    public volatile  boolean firstRunFlag = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        
        TextView txt_console = (TextView) findViewById(R.id.status_textView_console);
        if (savedInstanceState != null)
        		txt_console.setText(savedInstanceState.getString("console", "Welcome to CPU Mining Opt"));
        
        Console.setReceiver(new Console.Receiver() {
		  			@Override
		  			public void receive(String msgs) {
		  					final Message msg = statusHandler.obtainMessage();
		  					msg.arg1 = MSG_CONSOLE_UPDATE;
		  					Bundle b = new Bundle();
		  					b.putString("console", msgs);
		  					msg.setData(b);
      					statusHandler.sendMessage(msg);
		  			}
		  	});
        
        et_serv = (EditText) findViewById(R.id.server_et);
        et_user = (EditText) findViewById((R.id.user_et));
        et_pass = (EditText) findViewById(R.id.password_et);
        cb_service = (CheckBox) findViewById(R.id.settings_checkBox_background) ;
        cb_screen_awake = (CheckBox) findViewById(R.id.settings_checkBox_keepscreenawake) ;
				SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        et_serv.setText(settings.getString(PREF_URL,"stratum+tcp://us2.litecoinpool.org:8080"));
        et_user.setText(settings.getString(PREF_USER,"Ariasa.test"));
        et_pass.setText(settings.getString(PREF_PASS,"1234"));
        cb_screen_awake.setChecked(settings.getBoolean(PREF_SCREEN, false));
        cb_service.setChecked(settings.getBoolean(PREF_BACKGROUND, false));
        
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
    }
		public void startstopMining(View v) {
				final Button b = (Button) v;
        b.setEnabled(false);
        b.setClickable(false);
				if (b.getText().equals(getString(R.string.main_button_start))) {
						b.setText(R.string.main_button_onstart);
						new Thread(new Runnable(){
								@Override
								public void run() {
										String url = et_serv.getText().toString();
										String user = et_user.getText().toString();
										String pass = et_pass.getText().toString();
										Spinner threadList = (Spinner)findViewById(R.id.spinner1);
										int threads = Integer.parseInt(threadList.getSelectedItem().toString());
										if(cb_screen_awake.isChecked()) {
												getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
										}
										Console.send(0, "Try to start mining");
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
												editor.putBoolean(PREF_BACKGROUND, cb_service.isChecked());
												editor.putBoolean(PREF_SCREEN, cb_screen_awake.isChecked());
												editor.commit();
												firstRunFlag = false;
												final Message msg = statusHandler.obtainMessage();
												msg.arg1 = MSG_STARTED;
												statusHandler.sendMessage(msg);
										} catch (Exception e) {
												for (StackTraceElement t : e.getStackTrace())
														Console.send(0, "Error: "+t.toString());
												final Message msg = statusHandler.obtainMessage();
												msg.arg1 = MSG_TERMINATED;
												statusHandler.sendMessage(msg);
										}
								}
						}).start();
				} else {
						b.setText(R.string.main_button_onstop);
						Toast.makeText(this, "Worker cooling down, this can take a few minutes", Toast.LENGTH_LONG).show();
						new Thread(new Runnable() {
								@Override
								public void run(){
										Console.send(0, "Service: Stopping mining");
										try {
												smc.stopMining();
										} catch (Exception e) {
												for (StackTraceElement t : e.getStackTrace())
														Console.send(0, "Error: "+t.toString());
										}
										final Message msg = statusHandler.obtainMessage();
										msg.arg1 = MSG_TERMINATED;
										statusHandler.sendMessage(msg);
								}
						}).start();
				}
		}
		
		@Override
		protected void onSaveInstanceState(Bundle savedInstanceState) {
				savedInstanceState.putString("console", ((TextView) findViewById(R.id.status_textView_console)).getText().toString());
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
				if(!settings.getBoolean(PREF_BACKGROUND,false)) {
            Console.send(0, "Try to stop mining");
						Toast.makeText(this, "Worker cooling down, this can take a few minutes", Toast.LENGTH_LONG).show();
						try {
								smc.stopMining();
						} catch (Exception e) {
								for (StackTraceElement t : e.getStackTrace())
										Console.send(0, "Error: "+t.toString());
						}
        }
        super.onStop();
    }
}
