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
import com.ariasaproject.cpuminingopt.Constants.*;

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
          		case MSG_TERMINATED:
            			runOnUiThread(rBtnStart);
          				break;
          		case MSG_STARTED:
          				runOnUiThread(rBtnStop);
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
                    ThreadStatusAsyncTask threadWaiter = new ThreadStatusAsyncTask();
                    threadWaiter.execute(imw);
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
				if (b.getText().equals(getString(R.string.status_button_start))) {
						String url = et_serv.getText().toString();
						String user = et_user.getText().toString();
						String pass = et_pass.getText().toString();
						Spinner threadList = (Spinner)findViewById(R.id.spinner1);
						int threads = Integer.parseInt(threadList.getSelectedItem().toString());
						if(cb_screen_awake.isChecked()) {
								getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						}
						Console.send(0, "Try to start mining");
						//start mining0
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
						} catch (Exception e) {
								for (StackTraceElement t : e.getStackTrace())
										Console.send(0, "Error: "+t.toString());
						}
						firstRunFlag = false;
						b.setText(getString(R.string.main_button_stop));
				} else{
						Console.send(0, "Service: Stopping mining");
						Toast.makeText(this, "Worker cooling down, this can take a few minutes", Toast.LENGTH_LONG).show();
						try {
								smc.stopMining();
						} catch (Exception e) {
								for (StackTraceElement t : e.getStackTrace())
										Console.send(0, "Error: "+t.toString());
						}
						StartShutdown = true;
						b.setText(getString(R.string.status_button_start));
				}
		}
		
		@Override
		protected void onSaveInstanceState(Bundle savedInstanceState) {
				savedInstanceState.putString("console", ((TextView) findViewById(R.id.status_textView_console)).getText().toString());
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
    		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
								
        if (settings.getBoolean(PREF_BACKGROUND, DEFAULT_BACKGROUND)) {
            TextView tv_background = (TextView) findViewById(R.id.status_textView_background);
            tv_background.setText("RUN IN BACKGROUND");
        }
        super.onResume();
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
