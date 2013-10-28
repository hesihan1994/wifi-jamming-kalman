package com.none.detectwifijammer;

import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class StartScreen extends Activity {
	/* Based on the kalman filter for dummies described at
	 * http://bilgin.esme.org/BitsBytes/KalmanFilterforDummies.aspx
	 */
	
	// Known lowest connection threshold
	private double RSSIThreshold = 0;
	private double maxChange = 100;

	// Kalman filter stuff
	// inputs
	private double RSSI_Un = 0, //user has no control over RSSI values
			RSSI_zk =0, //current RSSI value
			RSSI_x_hat =0, //  Current predicted RSSI value
			RSSI_x_hat_m1 = 0, // previous predicted RSSI val
			RSSI_Pk_mk =0, // Process error
			RSSI_R = 0, // RSSI variance
			RSSI_Kk =0 ;
	
	// RSSI distribution stuff
	private float RSSIsd = 0;
	private double RSSImean = 0;
	private double numSamples = 0;
	
	
	private WifiManager appWifi = null;
	// Not connected to wifi handler 
	Handler buttonLoop = new Handler();
	Runnable buttonRun = new Runnable () {
		@Override
		public void run(){
			Button tButton = (Button) findViewById(R.id.buttonBtest);
			tButton.setText("Start Sampling");
			tButton.setEnabled(true);
		}
	};
	
	/* Kalman filter loop
	 * predict: using implementation from 
	 * evaluate
	 * if connected, change threshold
	 * if below known threshold and not connected, not jammed
	 * if above known threshold and not connected, jammed 
	*/
	Handler kalmanLooper = new Handler();
	Runnable kalmanRun = new Runnable () {
		@Override
		public void run(){
			WifiInfo current = appWifi.getConnectionInfo();
			RSSI_zk =current.getRssi();
			calculateRunningSd();
			RSSI_Kk = (RSSI_Pk_mk/(RSSI_Pk_mk+RSSI_R));
			RSSI_x_hat = RSSI_x_hat_m1+ RSSI_Kk*(RSSI_zk-RSSI_x_hat_m1);
			RSSI_Pk_mk = (1-RSSI_Kk)*RSSI_Pk_mk;
			checkConnectionStatus();
			RSSI_x_hat_m1 = RSSI_x_hat; 
			
			//change this to sample with every beacon
			kalmanLooper.postDelayed(this, 200);
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start_screen, menu);
		return true;
	}
	
	
	public void startStop(View v){
		Button tButton = (Button) findViewById(R.id.buttonBtest);
		Context context = this.getApplicationContext();
		if (tButton.getText().equals("Start Sampling")){
			WifiManager wifiManage = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wi = wifiManage.getConnectionInfo();
			if (WifiInfo.getDetailedStateOf(wi.getSupplicantState())== NetworkInfo.DetailedState.CONNECTED){
				tButton.setText("Stop Sampling");
				appWifi = wifiManage;
				RSSIThreshold = wi.getRssi();
				RSSImean = RSSIThreshold;
				RSSI_zk = RSSImean; 
				numSamples = 1;
		       	kalmanLooper.postDelayed(kalmanRun, 200);
			}
			else {
				tButton.setText("Wifi Network not connected");
				tButton.setEnabled(false);
				buttonLoop.postDelayed(buttonRun, 5000);
			}
		}
		else{
			tButton.setText("Start Sampling");
			RSSI_zk =0; //current RSSI value
			RSSI_x_hat =0; //  Current predicted RSSI value
			RSSI_x_hat_m1 = 0; // previous predicted RSSI val
			RSSI_Pk_mk =0; // Process error
			RSSI_R = 0; // RSSI variance
			RSSI_Kk =0;
			numSamples =0;
			RSSIThreshold =0;
			RSSImean = 0;
			kalmanLooper.removeCallbacks(kalmanRun);
		}
		
	}
	
	private void calculateRunningSd(){
		 numSamples++;
		 double tmpM = RSSImean;
		 RSSImean+= (RSSI_zk - tmpM)/numSamples;
		 RSSIsd += (RSSI_zk - tmpM)* (RSSI_zk* RSSImean);
		 RSSI_R = Math.sqrt(RSSIsd/ (numSamples-1)); 
		/* Stack exchange example: http://stackoverflow.com/questions/895929/
		 * how-do-i-determine-the-standard-deviation-stddev-of-a-set-of-values
		 * double tmpM = M;
		 * M += (value - tmpM) / k;
		 * S += (value - tmpM) * (value - M);
		 * k++;
		 * }
		 * return Math.Sqrt(S / (k-1)); */
	}
	
	private void checkConnectionStatus(){
		WifiInfo wi = appWifi.getConnectionInfo();
		TextView warn_text= (TextView) findViewById(R.id.textView1);
		if (((RSSI_x_hat- RSSI_zk) >  RSSIThreshold )){  
				if (WifiInfo.getDetailedStateOf(wi.getSupplicantState())== NetworkInfo.DetailedState.CONNECTED)
					RSSIThreshold =  RSSI_zk;
				warn_text.setText("Not Jammed");
		}
		else{
			if (WifiInfo.getDetailedStateOf(wi.getSupplicantState())!= NetworkInfo.DetailedState.CONNECTED){
				//jammer may be active
				warn_text.setText("Jammed");
			}
			else{
				warn_text.setText("Not Jammed");
			}
		}
	}

}
