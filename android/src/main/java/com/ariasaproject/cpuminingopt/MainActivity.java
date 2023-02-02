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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.AsyncTask;

import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.Scene;

public class MainActivity extends Activity {
  static {
    System.loadLibrary("ext");
  }
  Scene startS,onstartS,stopS,onstopS;
  Transition mtransition;

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
  Runnable[] runs = new Runnable[2];
  public void startMining(View v) {
		TransitionManager.go(onstartS, mtransition);
  	AsyncTask t = new AsyncTask<Void, Void, Void>() {
		  @Override
		  protected Void doInBackground(Void... voids) {
				startMining();
	  		return null;
		  }
		  @Override
		  protected void onPostExecute(Void ui) {
		  	runOnUiThread(new Runnable(){
		  		@Override
		  		public void run () {
						TransitionManager.go(stopS, mtransition);
		  		}
		  	});
		  }
		};
  	t.execute();
  }
  
  public void stopMining(View v) {
		TransitionManager.go(onstopS, mtransition);
		AsyncTask t = new AsyncTask<Void, Void, Void>() {
		  @Override
		  protected Void doInBackground(Void... voids) {
				stopMining();
	  		return null;
		  }
		  @Override
		  protected void onPostExecute(Void ui) {
		  	runOnUiThread(new Runnable(){
		  		@Override
		  		public void run () {
						TransitionManager.go(startS, mtransition);
		  		}
		  	});
		  }
		};
  	t.execute();
  }
  
  private native void startMining();
  private native void stopMining();
}
