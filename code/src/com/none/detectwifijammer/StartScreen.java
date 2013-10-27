package com.none.detectwifijammer;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;



public class StartScreen extends Activity {
	
	Handler kalmanLooper = new Handler();
	Runnable kalmanRun = new Runnable () {
		@Override
		public void run(){
			
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
		if (tButton.getText().equals("Start Sampling")){
			tButton.setText("Stop Sampling");
			
		}
		else{
			tButton.setText("Start Sampling");
		}
		
	}

}
