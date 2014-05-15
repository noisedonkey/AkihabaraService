/**
 * 
 */
package com.nexd.itsinghua.service;

import java.io.File;
import java.io.FileOutputStream;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.format.DateUtils;
import android.text.format.Time;

/**
 * @author Nexd
 *
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

	public static final int USER_STARTUP = 0x10;
	public static final int USER_SHUTDOWN = 0x11;
	public static final int BOOT_STARTUP = 0x12;
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
//		File flag = new File(path+flagname);
//		if (flag.exists() && flag.isFile()) {
			startTimer(context);
			fileFlag(BOOT_STARTUP);
//		}
	}

	private static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/tmp/";
	private static final String flagname = "0.run";
	public static void fileFlag(int reason) {

		try {
			new File(path).mkdirs();
			Time time = new Time();time.setToNow();
			FileOutputStream fos = new FileOutputStream(path+flagname,true);
			fos.write(((reason)+"\t"+time.format2445()+"\n").getBytes());
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void startTimer(Context context) {

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context,WiFiService.class);
		intent.putExtra("command", WiFiService.SINGLE_SCAN);
		PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DateUtils.SECOND_IN_MILLIS * 1, 15000L, pintent);

	}

}
