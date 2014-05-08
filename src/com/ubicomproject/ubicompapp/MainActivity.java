package com.ubicomproject.ubicompapp;

//import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.ActionBar;
//import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
//import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
//import android.view.View;
//import android.view.ViewGroup;
//import android.os.Build;

public class MainActivity extends Activity {

	
    Button startButton;
	Button stopButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
		//find the buttons
		startButton = (Button) findViewById(R.id.startButton);
		stopButton = (Button) findViewById(R.id.stopButton);
		
	
		//action listeners for start button
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i("MyLog","in startButton action Listener");
				
				Intent startServiceIntent = new Intent(getApplicationContext(),WebService.class);
				
				//pass message in intent
				
				startServiceIntent.putExtra("MessageFromActivity", "Service Started");
				
				startService(startServiceIntent);
			}
		});
	
		//action listeners for stop button
		stopButton.setOnClickListener(new View.OnClickListener() {
					
			@Override
			public void onClick(View v) {
				Log.i("MyLog","trying to stop servicer");
				Intent stopServiceIntent = new Intent(getApplicationContext(),WebService.class);
				stopService(stopServiceIntent);

			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		//just checking
		if(this.getIntent().getStringExtra("MessageFromService") !=null){
			Log.i("MyLog","Service starting new Activity"+this.getIntent().getStringExtra("MessageFromService"));
			
			startButton.setBackgroundColor(Color.GREEN);
		}
			
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
