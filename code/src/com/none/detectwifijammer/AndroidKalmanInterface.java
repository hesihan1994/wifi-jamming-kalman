package com.none.detectwifijammer;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;


public class AndroidKalmanInterface implements Runnable {
	Context applicationContext;
	Handler alertUser;
	
	private WifiManager deviceWifi;
	private WifiInfo deviceWifiInfo;
	
	private WifiKalmanFilter theFilter;
	
	private String currentMessage ="Not Jammed";
	
	private int detectionThreshold;
	private float estimatedRssi;
	
	public boolean isWifiConnected(Context currentContext){
		applicationContext = currentContext;
		deviceWifi = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
		deviceWifiInfo = deviceWifi.getConnectionInfo();
		if (WifiInfo.getDetailedStateOf(deviceWifiInfo.getSupplicantState())== NetworkInfo.DetailedState.CONNECTED)
			return true;
		return false;
	}
	
	public void startTesting(int theThreshold, Handler callbackFunction){
		theFilter.clearFilter();
		detectionThreshold = theThreshold;
		alertUser = callbackFunction;
		theFilter.initialiseFilter(60, getRssi());
		estimatedRssi = 0;
	}
	
	public void endTesting(){
		theFilter.clearFilter();
	}
	
	@Override
	public void run() {
		float currentRssi = getRssi();
		if ((currentRssi - estimatedRssi) < detectionThreshold){
			checkStatusandAlert();
		}
		estimatedRssi = theFilter.updateFilter(currentRssi);
	}
	
	private float getRssi(){
		return (float)deviceWifiInfo.getRssi();
	}
	
	private boolean isWifiConnected(){
		if (WifiInfo.getDetailedStateOf(deviceWifiInfo.getSupplicantState())== NetworkInfo.DetailedState.CONNECTED)
			return true;
		return false;
	}
	
	private void checkStatusandAlert(){
		if (isWifiConnected()){
			if (!currentMessage.equals("Not Jammed")){
				currentMessage = "Not Jammed";
				String changeAlert = "Not Jammed";
				Message changeMessage = new Message();
				changeMessage.obj = changeAlert;
				changeMessage.setTarget(alertUser);
				changeMessage.notify();
			}
		}
		else{
			if (!currentMessage.equals("Jammed")){
				currentMessage = "Jammed";
				String changeAlert = "Jammed";
				Message changeMessage = new Message();
				changeMessage.obj = changeAlert;
				changeMessage.setTarget(alertUser);
				changeMessage.notify();
			}
		}
	}
}
