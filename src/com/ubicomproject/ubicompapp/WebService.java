package com.ubicomproject.ubicompapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class WebService extends Service{


	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i("MyLog","In Service onCreate");
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MyLog","In Service onStartCommand");
		Log.i("MyLog","Logging intent extra message"+intent.getStringExtra("MessageFromActivity"));
		
		//starting the myAsynctask thread
		
		new MyAsynTask().execute();
		return startId;
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();
		
		Log.i("MyLog","In Service onDestroy");
	}
	
	// The separate thread
	//params: first: what is passed  second:for update third: the returned
	private class MyAsynTask extends AsyncTask<String,String,String>{

		protected String doInBackground(String... params) {
			int i=0;
			try{
			  while(i<5){
				Thread.sleep(1000);
				
				Log.i("MyLog","Executing in Thread");
				i++;
			  }
			}catch(Exception e){
				Log.i("MyLog","Exception Occured in MyAsyncTask"+e.toString());
			}
			
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			//this is the UI thread
			
			//Here try to create the Activity
			
			Log.i("MyLog","in Post Execute UI thread");
			
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		    intent.setAction(Intent.ACTION_VIEW);
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//		    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		    
		    intent.putExtra("MessageFromService", "New Activity Started");
		    getApplicationContext().startActivity(intent);
		}

	}

	
}



