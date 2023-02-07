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

public class MainActivity extends Activity {
  static {
    System.loadLibrary("ext");
  }
  TextView console;
  EditText ed_uri, ed_user,ed_pass;
  Button btn_mining;
  Handler mHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_main);
    super.onCreate(savedInstanceState);
		ed_uri = (EditText)findViewById(R.id.edt_uri);
		ed_user = (EditText)findViewById(R.id.edt_user);
		ed_pass = (EditText)findViewById(R.id.edt_pass);
		btn_mining = (Button)findViewById(R.id.btn_mining);
		console = (TextView)findViewById(R.id.txview_console);
    mHandler = new Handler() {
		  //for main Ui Thread Update
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					default:
						break;
		      case 1: //request start
		      	btn_mining.setEnabled(false);
		      	btn_mining.setClickable(false);
		      	btn_mining.setText(R.string.state_button_onstart);
		      	ed_uri.setEnabled(false);
		      	ed_uri.setClickable(false);
		      	ed_user.setEnabled(false);
		      	ed_user.setClickable(false);
		      	ed_pass.setEnabled(false);
		      	ed_pass.setClickable(false);
						MainActivity.this.startMining();
						break;
					case 2: //after start
		      	btn_mining.setText(R.string.state_button_stop);
		      	btn_mining.setOnClickListener(new View.OnClickListener() {
				        @Override
				        public void onClick(View v) {
				        		sendMessage(obtainMessage(3));
				        }
				    });
		      	btn_mining.setClickable(true);
						btn_mining.setEnabled(true);
						break;
		      case 3: //request stop
						btn_mining.setEnabled(false);
		      	btn_mining.setClickable(false);
		      	btn_mining.setText(R.string.state_button_onstop);
						MainActivity.this.stopMining();
						break;
					case 4: //after stop
		      	btn_mining.setText(R.string.state_button_start);
		      	btn_mining.setOnClickListener(new View.OnClickListener() {
				        @Override
				        public void onClick(View v) {
				        		sendMessage(obtainMessage(1));
				        }
				    });
						btn_mining.setEnabled(true);
		      	btn_mining.setClickable(true);
		      	ed_uri.setEnabled(true);
		      	ed_uri.setClickable(true);
		      	ed_user.setEnabled(true);
		      	ed_user.setClickable(true);
		      	ed_pass.setEnabled(true);
		      	ed_pass.setClickable(true);
						break;
					case 9: //console tag
		        console.setText(Html.fromHtml((String) msg.obj), TextView.BufferType.SPANNABLE);
						break;
				}
			}
    };
    mHandler.sendMessage(mHandler.obtainMessage(4));
  }
  //used to call from Native
  private void receiveMessage(int flag, String msg) {
  	mHandler.removeCallbacksAndMessages(flag);
  	mHandler.sendMessage(mHandler.obtainMessage(flag,msg));
  }
  private native void startMining();
  private native void stopMining();
}
