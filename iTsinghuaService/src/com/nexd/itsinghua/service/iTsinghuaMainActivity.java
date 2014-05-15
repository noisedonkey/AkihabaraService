package com.nexd.itsinghua.service;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class iTsinghuaMainActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ToggleButton btn = (ToggleButton) findViewById(R.id.toggleButton1);
		btn.setTextOn("开着");
		btn.setTextOff("关了");
		btn.setChecked(isServiceRunning(WiFiService.class));
		btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
//				Intent intent = new Intent(MainActivity.this,WiFiService.class);
//				intent.putExtra("command", arg1?WiFiService.START_SCAN:WiFiService.STOP_SCAN);
//				startService(intent);
				
				if (arg1) {
					startTimer();
					BootBroadcastReceiver.fileFlag(BootBroadcastReceiver.USER_STARTUP);
				} else {
					stopTimer();
					BootBroadcastReceiver.fileFlag(BootBroadcastReceiver.USER_SHUTDOWN);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private boolean isServiceRunning(Class<? extends Service> c) {  
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);  
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {  
	        if (c.getName().equals(service.service.getClassName())) {  
	            return true;  
	        }  
	    }  
	    return false;  
	}  
	

	private void startTimer() {
		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(iTsinghuaMainActivity.this,WiFiService.class);
		intent.putExtra("command", WiFiService.SINGLE_SCAN);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DateUtils.SECOND_IN_MILLIS * 1, 15000L, pintent);
		
	}

	private void stopTimer() {

		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		PendingIntent intent = PendingIntent.getService(this, 0, new Intent(this,WiFiService.class), 0);
		am.cancel(intent);
		Intent i = new Intent(iTsinghuaMainActivity.this,WiFiService.class);
		stopService(i);

	}
	
}
