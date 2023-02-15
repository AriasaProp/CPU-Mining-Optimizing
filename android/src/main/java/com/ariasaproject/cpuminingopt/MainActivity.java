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
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
	static final String URI_PREF = "URI";
	static final String PORT_PREF = "PORT";
	static final String USER_PREF = "USER";
	static final String PASS_PREF = "PASS";
	static final String IPV_PREF = "IPV";
  static {
    System.loadLibrary("ext");
  }
  TextView console;
  EditText ed_uri, ed_port, ed_user,ed_pass;
  Switch swtch_ipv;
  Button btn_mining;
  Handler mHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_main);
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		ed_uri = (EditText)findViewById(R.id.edt_uri);
		ed_uri.setText(pref.getString(URI_PREF, "us2.litecoinpool.org"));
		ed_port = (EditText)findViewById(R.id.edt_port);
		ed_port.setText(String.valueOf(pref.getInt(PORT_PREF, 3333)));
		ed_user = (EditText)findViewById(R.id.edt_user);
		ed_user.setText(pref.getString(USER_PREF, "Ariasa.test"));
		ed_pass = (EditText)findViewById(R.id.edt_pass);
		ed_pass.setText(pref.getString(PASS_PREF, "1234"));
		swtch_ipv = (EditText)findViewById(R.id.swtch_IPv);
		swtch_ipv.setShowText(pref.getBoolean(IPV_PREF, false));
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
		      	btn_mining.setOnClickListener(null);
		      	btn_mining.setEnabled(false);
		      	btn_mining.setClickable(false);
		      	btn_mining.setText(R.string.state_button_onstart);
		      	ed_uri.setEnabled(false);
		      	ed_uri.setClickable(false);
		      	ed_port.setEnabled(false);
		      	ed_port.setClickable(false);
		      	ed_user.setEnabled(false);
		      	ed_user.setClickable(false);
		      	ed_pass.setEnabled(false);
		      	ed_pass.setClickable(false);
		      	swtch_ipv.setEnabled(false);
		      	swtch_ipv.setClickable(false);
		      	String[] d = new String[4];
		      	d[0] = ed_uri.getText().toString();
		      	d[1] = ed_port.getText().toString();
		      	d[2] = ed_user.getText().toString();
		      	d[3] = ed_pass.getText().toString();
		      	int flags = 0;
		      	if (swtch_ipv.getShowText()) {
		      		flags |= 1;
		      	}
						MainActivity.this.startMining(d, flags);
						break;
					case 2: //after start
						SharedPreferences.Editor pref_edit = getPreferences(Context.MODE_PRIVATE).edit();
						pref_edit.putString(URI_PREF, ed_uri.getText().toString());
						pref_edit.putInt(PORT_PREF, Integer.valueOf(ed_port.getText().toString()));
						pref_edit.putString(USER_PREF, ed_user.getText().toString());
						pref_edit.putString(PASS_PREF, ed_pass.getText().toString());
						pref_edit.putBoolean(IPV_PREF, swtch_ipv.getShowText());
						pref_edit.commit();
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
		      	btn_mining.setOnClickListener(null);
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
		      	ed_port.setEnabled(true);
		      	ed_port.setClickable(true);
		      	ed_user.setEnabled(true);
		      	ed_user.setClickable(true);
		      	ed_pass.setEnabled(true);
		      	ed_pass.setClickable(true);
		      	swtch_ipv.setEnabled(true);
		      	swtch_ipv.setClickable(true);
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
  private native void startMining(String[] datas, int flags);
  private native void stopMining();
}
