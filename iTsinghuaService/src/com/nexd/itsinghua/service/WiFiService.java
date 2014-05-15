package com.nexd.itsinghua.service;

import java.io.File;
import java.io.FileOutputStream;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.Time;
import android.util.Log;

public class WiFiService extends Service {

//	public final static int START_SCAN = 0x1;
//	public final static int STOP_SCAN = 0x2;
	public final static int SINGLE_SCAN = 0x3;
	
	private boolean lastWifiStatus = false;
    private WakeLock wakeLock;
    private String TAG = WiFiService.class.getName();
	private static final String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/tmp/";
	
	WiFiSingleCollector collector;
	Handler handler;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
//		getCellLocation();
		handler = new WiFiHandler();
		collector = new WiFiSingleCollector(this, this.handler);
		new File(root).mkdirs();
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
//		Log.v(TAG,"aaaaaaaaaa");
		if (intent != null) {
			int command = intent.getIntExtra("command", 0);
			Log.v(TAG,"Scan Request");
			if (command == SINGLE_SCAN && !collector.isStarted()) {
				if (checkTime()) {
					acquireWakeLock();
					lastWifiStatus = toggleWiFi(true);
					collector.scan();
				}
			}
		} else {
//			Log.v(TAG,"CAOCAOCAOCAOCAOWOBEISHALEWOXUELIUMANDIA");
		}
		return START_STICKY;//super.onStartCommand(intent, flags, startId);
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


//	private void getCellLocation() {
//		// TODO Auto-generated method stub
//		// Acquire a reference to the system Location Manager
//		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//
//		// Define a listener that responds to location updates
//		LocationListener locationListener = new LocationListener() {
//			
//			@Override
//		    public void onLocationChanged(Location location) {
//		      // Called when a new location is found by the network location provider.
//				Log.v(TAG,location.getAltitude() + " " + location.getLatitude());
//		    }
//
//
//		    public void onProviderEnabled(String provider) {}
//
//		    public void onProviderDisabled(String provider) {}
//
//			@Override
//			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
//				// TODO Auto-generated method stub
//				Log.v(TAG,arg0 + " " + arg1);
//			}
//		  };
//
//		// Register the listener with the Location Manager to receive location updates
//		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
//	}
	
	protected boolean checkTime() {
		Time time = new Time();
		time.setToNow();
		return (time.hour > 6 && time.hour < 23);
	}

	
    public boolean toggleWiFi(boolean status) {
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        boolean originStatus = wifiManager.isWifiEnabled();
        if (status == true && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        } else if (status == false && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        return originStatus;
    }
    
	private void acquireWakeLock() { 
		
		if (wakeLock != null && wakeLock.isHeld()) {
			return;
		}
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
		int wakeFlags; 
		wakeFlags = PowerManager.PARTIAL_WAKE_LOCK; 
		wakeLock = pm.newWakeLock(wakeFlags, TAG); 
		wakeLock.setReferenceCounted(false);
		wakeLock.acquire();   
//		Log.v(TAG, "acquireWakeLock"); 
		
	} 
	
	private void releaseWakeLock() {
		// TODO Auto-generated method stub
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}
		wakeLock = null;
//		Log.v(TAG, "releaseWakeLock");
	}
    
	@SuppressLint("HandlerLeak")
	class WiFiHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == 1) {
				try {
					Time time = new Time();
					time.setToNow();
					long synctime = System.currentTimeMillis();
					String model = Build.MODEL.replace(" ", "");
					String fname = root+time.format("%Y%m%d") + "_" + model +".wdc";
					FileOutputStream fos;
					fos = new FileOutputStream(new File(fname),true);
					fos.write(collector.getDataStringWithTS(synctime).getBytes("utf-8"));
					fos.close();
					fname = root+time.format("%Y%m%d") + "_" + model +".cdc";
					fos = new FileOutputStream(new File(fname),true);
					fos.write(collector.getCellString(synctime).getBytes("utf-8"));
					fos.close();
//					Log.w(TAG,collector.getDataStringWithTS(synctime));
					releaseWakeLock();
					toggleWiFi(lastWifiStatus);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				super.handleMessage(msg);
			}
		}
	}
	
	
}
