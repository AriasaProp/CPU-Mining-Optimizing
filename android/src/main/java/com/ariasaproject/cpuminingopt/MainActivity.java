package com.ariasaproject.cpuminingopt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.lang.Thread;

import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.Scene;

public class MainActivity extends Activity implements Handler.Callback {
  static {
    System.loadLibrary("ext");
  }
  Scene startS, onstartS, stopS, onstopS;
  TextView console;
  Transition mtransition;
  Handler mHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_main);
    super.onCreate(savedInstanceState);
		console = (TextView)findViewById(R.id.txview_console);
    startS = Scene.getSceneForLayout((ViewGroup) findViewById(R.id.container), R.layout.activity_layout_start, this);
		onstartS = Scene.getSceneForLayout((ViewGroup) findViewById(R.id.container), R.layout.activity_layout_onstart, this);
		stopS = Scene.getSceneForLayout((ViewGroup) findViewById(R.id.container), R.layout.activity_layout_stop, this);
		onstopS = Scene.getSceneForLayout((ViewGroup) findViewById(R.id.container), R.layout.activity_layout_onstop, this);
		mtransition = TransitionInflater.from(this).inflateTransition(R.transition.mining_transition);
		TransitionManager.go(startS, mtransition);
    //mHandler = new Handler(Looper.getMainLooper());
    mHandler = new Handler(){
		  //for main Ui Thread Update
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					default:
						break;
		      case 1: //request start
		      	TransitionManager.go(onstartS, mtransition);
		      	new Thread(new Runnable(){
				  		@Override
				  		public void run () {
								MainActivity.this.startMining();
								MainActivity.this.receiveMessage(2,"");
				  		}
						}).start();
						break;
					case 2: //after start
						TransitionManager.go(stopS, mtransition);
						break;
		      case 3: //request stop
						TransitionManager.go(onstopS, mtransition);
						new Thread(new Runnable(){
				  		@Override
				  		public void run () {
								MainActivity.this.stopMining();
						  	MainActivity.this.receiveMessage(4,"");
				  		}
						}).start();
						break;
					case 4: //after stop
						TransitionManager.go(startS, mtransition);
						break;
					case 9: //console tag
		        console.setText(Html.fromHtml((String) msg.obj), TextView.BufferType.SPANNABLE);
						break;
				}
			}
    };
  }
  public void startMining(View v) {
  	receiveMessage(1,"");
  }
  public void stopMining(View v) {
  	receiveMessage(3,"");
  }
  private void receiveMessage(int flag, String msg) {
  	mHandler.removeCallbacksAndMessages(flag);
  	Message m = mHandler.obtainMessage();
  	m.what = flag;
  	m.obj = msg;
  	m.setAsynchronous(true);
  	mHandler.sendMessage(m);
  }
  private native void startMining();
  private native void stopMining();
}
