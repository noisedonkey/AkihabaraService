package com.nexd.itsinghua.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

/*
 * Wifi Collector
 * Collect & Store WiFi Strength Information
 * 
 */
class WiFiSingleCollector {
	List<List<ScanResult>> datamap;
	WifiManager manager;
	boolean started = false;	
	WifiReceiver receiver;
	WifiLock locker;
	Handler parentHandler;
	Context context;

	
	public WiFiSingleCollector(Context context,Handler handler) {
		// TODO Auto-generated method stub
		super();
		this.parentHandler = handler;
		this.datamap = new ArrayList<List<ScanResult>>();
		this.manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		this.context = context;

		this.locker = this.manager.createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, "locker");
	}
	
	protected void register() {
		context.registerReceiver(receiver=new WifiReceiver(), new IntentFilter(  
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	protected void unregister() {
		// TODO Auto-generated method stub
		if (context != null) {
			context.unregisterReceiver(receiver);
		}
	}
	
	protected void lock() {
		if (!this.locker.isHeld()) {
			this.locker.acquire();
		}
	}
	
	protected void unlock() {
		if (this.locker.isHeld()) {
			this.locker.release();
		}
	}

	private int parseInt(int value) {
//		return value == Integer.MAX_VALUE ? -1 : value;
		return value;
	}
	@SuppressLint("NewApi")
	protected String getCellString(long synctime) {
		StringBuilder sb_cell = new StringBuilder();
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && manager.getAllCellInfo() != null) {
			for (CellInfo ci : manager.getAllCellInfo()) {
				if (ci instanceof CellInfoGsm) {
					CellInfoGsm gsm = (CellInfoGsm)ci;
					int cid = gsm.getCellIdentity().getCid(), lac = gsm.getCellIdentity().getLac(), mcc = gsm.getCellIdentity().getMcc(), mnc = gsm.getCellIdentity().getMnc(), psc = -1; 
					sb_cell.append("g" + " " + parseInt(cid) + " " + parseInt(lac) + " " + parseInt(mcc) + " " + parseInt(mnc) + " " + parseInt(psc) + " " + gsm.getCellSignalStrength().getDbm());
				} else if (ci instanceof CellInfoCdma) {
					CellInfoCdma cdma = (CellInfoCdma)ci;
					int cid = cdma.getCellIdentity().getBasestationId(), lat = cdma.getCellIdentity().getLatitude(), lng = cdma.getCellIdentity().getLongitude(), nid = cdma.getCellIdentity().getNetworkId(), sid=cdma.getCellIdentity().getSystemId();
					sb_cell.append("c" + " " + parseInt(cid) + " " + lat + " " + lng + " " + parseInt(nid) + " " + parseInt(sid) + " " + cdma.getCellSignalStrength().getDbm() + " " + cdma.getCellSignalStrength().getCdmaDbm() + " " + cdma.getCellSignalStrength().getCdmaEcio() + " " + cdma.getCellSignalStrength().getEvdoDbm() + " " + cdma.getCellSignalStrength().getEvdoEcio());
				} else if (ci instanceof CellInfoWcdma) {
					CellInfoWcdma wcdma = (CellInfoWcdma)ci;
					int cid = wcdma.getCellIdentity().getCid(), lac = wcdma.getCellIdentity().getLac(), mcc = wcdma.getCellIdentity().getMcc(), mnc = wcdma.getCellIdentity().getMnc(), psc = wcdma.getCellIdentity().getPsc();
					sb_cell.append("w" + " " + parseInt(cid) + " " + parseInt(lac) + " " + parseInt(mcc) + " " + parseInt(mnc) + " " + parseInt(psc) + " " + wcdma.getCellSignalStrength().getDbm());
				} else if (ci instanceof CellInfoLte) {
					CellInfoLte lte = (CellInfoLte)ci;
					int cid = lte.getCellIdentity().getCi(), tac = lte.getCellIdentity().getTac(), mcc = lte.getCellIdentity().getMcc(), mnc = lte.getCellIdentity().getMnc(), pci = lte.getCellIdentity().getPci();
					sb_cell.append("l" + " " + parseInt(cid) + " " + tac + " " + parseInt(mcc) + " " + parseInt(mnc) + " " + parseInt(pci) + " " + lte.getCellSignalStrength().getDbm() + " " + lte.getCellSignalStrength().getTimingAdvance());
				}
				sb_cell.append("\n");
			}
			if (synctime==-1) synctime = System.currentTimeMillis();
			sb_cell.append("----------"+synctime + "---" +format.format(new Date(synctime))+"\n");
		} else {
//			CellLocation cl = manager.getCellLocation();
//			List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();
//			System.out.println(list.get(0).);
			if (synctime==-1) synctime = System.currentTimeMillis();
			sb_cell.append("----------"+synctime + "---" +format.format(new Date(synctime))+"\n");
		}
		return sb_cell.toString();
	}
	
	@SuppressLint("NewApi")
	public String getDataStringWithTS(long synctime) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return getDataString(synctime);
		}
		StringBuilder sb = new StringBuilder();
		if (datamap != null) {
			try {
				synchronized (datamap) {
					if (datamap.size() > 0) {
						for (ScanResult result : datamap.get(datamap.size() - 1)) {
							sb.append(result.SSID + "\t" + result.BSSID + "\t" + result.frequency + "\t" + result.level + "\t" + result.timestamp);
							sb.append("\n");
						}
					}
					if(synctime == -1) synctime=System.currentTimeMillis();
					sb.append("----------"+synctime + "---" +format.format(new Date(synctime))+"\n");
				}
			}catch(Exception e) {

			}
		}
		return sb.toString();
	}
	
	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	public String getDataString(long synctime) {
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
					if(synctime == -1) synctime=System.currentTimeMillis();
					sb.append("----------"+synctime + "---" +format.format(new Date(synctime))+"\n");
				}
			}catch(Exception e) {

			}
		}
		return sb.toString();
	}
	

	public void scan() {
		started = true;
		register();
		lock();
		manager.startScan();
	}


	public List<List<ScanResult>> getDatamap() {
		return datamap;
	}

	public boolean isStarted() {
		return started;
	}
	
	List<ScanResult> lastVisibleResults = null;
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private boolean containsResult(List<ScanResult> list, ScanResult result) {
		for (ScanResult li : list) {
			if (li.BSSID.equals(result.BSSID) && li.timestamp == result.timestamp) {
				return true;
			}
		}
		return false;
	}
	private List<ScanResult> cutCachedResults(List<ScanResult> results) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return results;
		}
		if (lastVisibleResults != null && lastVisibleResults.size() > 0 && results != null && results.size() > 0) {
			if (results.size() == lastVisibleResults.size()) {
				boolean flag = true;
				for (ScanResult result : lastVisibleResults) {
					if (!containsResult(results,result)) {
						flag = false;
						break;
					}
				}
				if (flag) {
					results.clear();
				}
			}
		}
		if (results.size() > 0) {
			lastVisibleResults = results;
		}
		return results;
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
        	started = false;
        	
        	List<ScanResult> results = manager.getScanResults();
        	
        	results = cutCachedResults(results);
        	
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
        	unregister();
        	unlock();
        }
    }

}
