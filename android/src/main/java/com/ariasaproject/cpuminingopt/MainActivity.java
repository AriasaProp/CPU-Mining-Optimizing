package com.ariasaproject.cpuminingopt;

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
import android.os.AsyncTask;

import android.transition.TransitionManager;
import android.transition.Scene;

public class MainActivity extends Activity {
  static {
    System.loadLibrary("ext");
  }
  Scene startS,onstartS,stopS,onstopS;
  Transition mtransition;
  private AsyncTask task = new AsyncTask<Runnable, Void, Runnable> {
	  @Override
	  protected Runnable doInBackground(Runnable... voids) {
	  	if (voids !=null) {
	  		voids[0].run();
	  		if (voids.length > 1)
	  			return voids[1];
  		}
  		return null;
	  }
	  @Override
	  protected void onPostExecute(Runnable ui) {
	  	if (ui != null)
	  		ui.run();
	  }
	}

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_main);
    super.onCreate(savedInstanceState);
    startS = Scene.getSceneForLayout((ViewGroup) findViewById(R.id.container), R.layout.activity_layout_start, this);
		onstartS = Scene.getSceneForLayout((ViewGroup) findViewById(R.id.container), R.layout.activity_layout_onstart, this);
		stopS = Scene.getSceneForLayout((ViewGroup) findViewById(R.id.container), R.layout.activity_layout_stop, this);
		onstopS = Scene.getSceneForLayout((ViewGroup) findViewById(R.id.container), R.layout.activity_layout_onstop, this);
		
		mtransition = TransitionInflater.from(this).inflateTransition(R.transition.mining_transition);
		TransitionManager.go(startS, mtransition);
  }
  
  public void startMining(View v) {
		TransitionManager.go(onstartS, mtransition);
  	task.execute(new Runnable[2]{
  		new Runnable(){
  			@Override
  			public void run(){
  				startMining();
  			}
  		},
  		new Runnable(){
  			@Override
  			public void run(){
					TransitionManager.go(stopS, mtransition);
  			}
  		}
  	});
  }
  
  public void stopMining(View v) {
		TransitionManager.go(onstopS, mtransition);
  	task.execute(new Runnable[2]{
  		new Runnable(){
  			@Override
  			public void run(){
  				startMining();
  			}
  		},
  		new Runnable(){
  			@Override
  			public void run(){
					TransitionManager.go(startS, mtransition);
  			}
  		}
  	});
  }
  
  private native void startMining();
  private native void stopMining();
}
