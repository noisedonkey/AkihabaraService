package com.nexd.itsinghua.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;

/*
 * Wifi Collector
 * Collect & Store WiFi Strength Information
 * 
 */
class WifiCollector {
	List<List<ScanResult>> datamap;
	WifiManager manager;
	boolean started = false;	
	WifiReceiver receiver;
	WifiLock locker;
	Handler parentHandler;
	Context context;

	
	public WifiCollector(Context context,Handler handler) {
		// TODO Auto-generated method stub
		super();
		this.parentHandler = handler;
		this.datamap = new ArrayList<List<ScanResult>>();
		this.manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		this.context = context;
	}
	
	public void register() {
		context.registerReceiver(receiver=new WifiReceiver(), new IntentFilter(  
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	public void unregister() {
		// TODO Auto-generated method stub
		if (context != null) {
			context.unregisterReceiver(receiver);
		}
	}

	public String getDataString() {
		StringBuilder sb = new StringBuilder();
		if (datamap != null) {
			try {
				synchronized (datamap) {
					if (datamap.size() > 0) {
						for (ScanResult result : datamap.get(datamap.size() - 1)) {
							sb.append(result.SSID + "\t" + result.BSSID + "\t" + result.frequency + "\t" + result.level);
							sb.append("\n");
						}
					}
					
					sb.append("--------------------------------------"+System.currentTimeMillis()+"\n");
				}
			}catch(Exception e) {

			}
		}
		return sb.toString();
	}
	
	/*
	 * Scan Until User Stop
	 */
	public void startScan() {
		started = true;
		globalTimer = new Timer();
		this.locker = this.manager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY,"locker");
		this.locker.acquire();
		this.scan();
	}

	/*
	 * Stop Scan
	 */
	public void stopScan() {
		started = false;
		globalTimer.cancel();
	    if (locker.isHeld())  
	    {  
	        locker.release();  
	    }  
	}

	public void scan() {
		manager.startScan();
	}


	public List<List<ScanResult>> getDatamap() {
		return datamap;
	}

	public boolean isStarted() {
		return started;
	}
	
	private final long countDelay() {
		return 1000;
	}
	
	/*
	 * Receiver Class
	 */
    private final class WifiReceiver extends BroadcastReceiver {
        /*
         * Callback Function
         * @base:see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */  
        @Override  
        public void onReceive(Context context, Intent intent) {
        	if (!started) {
        		return;
        	}
        	
        	List<ScanResult> results = manager.getScanResults();
//        	for (ScanResult result : results) {
//        		System.out.println(result.SSID + " " + result.BSSID + " " + result.level + " " + result.frequency + " " + result.capabilities);
//        	}
        	
        	synchronized(datamap){
        		if (datamap.size() == 0) {
        			datamap.add(results);
        		} else {
        			datamap.set(0, results);
        		}
        	}
        	parentHandler.sendEmptyMessage(1);
        	long delay = countDelay();
        	if (started) {
        		globalTimer.schedule(new DelayTask(), delay);
        	} else if (parentHandler != null){
        	}
        }


    }
    
    private Timer globalTimer;
	class DelayTask extends TimerTask {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			scan();
		}
	};

	@Override
	public void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if (context != null) {
			context.unregisterReceiver(receiver);
		}
	}
}
