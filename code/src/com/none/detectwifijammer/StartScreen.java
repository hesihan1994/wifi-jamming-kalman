package com.none.detectwifijammer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartScreen extends Activity {
	
	private AndroidKalmanInterface kalmanInterface;
	private Handler mainLoop = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			String recievedString = msg.obj.toString();
			boolean notHandled = true;
			if (recievedString.equals("Jammed") || recievedString.equals("Not Jammed")){
				updateDisplayMessage(recievedString);
				notHandled = false;
			}
			
			if (recievedString.equals("changedNetwork")){
				setNetworkName();
				notHandled = false;
			}
			
			if (notHandled) {
				super.handleMessage(msg);
			}
		}
	};
	 
	private Runnable enableButton = new Runnable(){
		@Override
		public void run() {
			Button startStop = (Button) findViewById(R.id.buttonStartStop);
			startStop.setText("Start Sampling");
			startStop.setEnabled(true);
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
		Button startStop = (Button) findViewById(R.id.buttonStartStop);
		if (startStop.getText().equals("Start Sampling")){
			if (kalmanInterface.isWifiConnected(this.getApplicationContext())){
				kalmanInterface.startTesting(getThreshold(),mainLoop);
				startStop.setText("Stop Sampling");
				setNetworkName();
				mainLoop.postDelayed(kalmanInterface, 200);
			}
			else {
				startStop.setText("Wifi Network not connected");
				startStop.setEnabled(false);
				mainLoop.postDelayed(enableButton, 5000);
			}
		}
		else{
			startStop.setText("Start Sampling");
			mainLoop.removeCallbacks(kalmanInterface);
		}
	}
	
	private int getThreshold(){
		TextView detectionThreshold = (TextView) findViewById(R.id.displayThreshold);
		String currentThresholdText = detectionThreshold.getText().toString();
		if (Integer.parseInt(currentThresholdText)> 0)
			return Integer.parseInt(currentThresholdText);
		return 150;
	}
	
	private void updateDisplayMessage(String recievedString){
		TextView alertText = (TextView) findViewById(R.id.displayStatus);
		alertText.setText(recievedString);
	}
	
	private void setNetworkName(){
		TextView networkText = (TextView) findViewById(R.id.displayNetwork);
		networkText.setText(kalmanInterface.wifiNetworkName());
	}
	
	
}
