package com.ubicomproject.ubicompapp;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Build;

public class MainActivity extends Activity {

	public static boolean MAIN_ACTIVITY_IS_ALIVE;
	
	private Button toiletUserButton;
	private Button janitorButton;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
		final Context context = this;
		toiletUserButton = (Button) findViewById(R.id.toilerUserButton);
		janitorButton = (Button) findViewById(R.id.janitorButton);
		
		//action listeners for start button
		janitorButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("MyLog","in janitorButton action Listener");
                
				Intent startCleaningIntent = new Intent(context, CleaningActivity.class);

				//pass message in intent

//				startServiceIntent.putExtra("MessageFromActivity", "Activity Started");

				startActivity(startCleaningIntent);
				
				startWebService();
			}
		});
		
		
		//action listeners for start button
		toiletUserButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("MyLog","in janitorButton action Listener");
                
				Intent startUserIntent = new Intent(context, UserActivity.class);

				//pass message in intent

//				startServiceIntent.putExtra("MessageFromActivity", "Activity Started");

				startActivity(startUserIntent);
			}
		});
	}
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MAIN_ACTIVITY_IS_ALIVE = true;
	}
   
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MAIN_ACTIVITY_IS_ALIVE = false;

	}

	private void startWebService(){
    	Intent startWebServiceIntent = new Intent(getApplicationContext(),WebService.class);

		//pass message in intent

		startWebServiceIntent.putExtra("MessageFromActivity", "Service Started");

		startService(startWebServiceIntent);
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
