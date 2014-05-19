package com.ubicomproject.ubicompapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
//import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

public class WebService extends Service{

   public static boolean ALERT_STATUS = false;   // to check alert button 
   private ServerConnection serverConnection;
   static String serverUrl = "http://192.168.0.240/cgi-bin/gateway.py";
   private Context context;
   private StopAlertReceiver stopAlertReceiver;
   private MediaPlayer mediaPlayer;
   
	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i("MyLog","In Service onCreate");
		
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MyLog","In Service onStartCommand");
		//Log.i("MyLog","Logging intent extra message"+intent.getStringExtra("MessageFromActivity"));
		Log.i("MyLog","Logging intent in onStartCommand"+intent.toString());
		 
		context = this;
		
		//register alert stop receiver
		stopAlertReceiver = new StopAlertReceiver();
		IntentFilter intentFilter = new IntentFilter("STOP_ALERT");
		LocalBroadcastManager.getInstance(context).registerReceiver(stopAlertReceiver, intentFilter);
		
		Uri mediaSource = Uri.parse("android.resource://"+context.getPackageName()+R.raw.alert);
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(context,mediaSource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//mediaPlayer.setOnPreparedListener((OnPreparedListener) this);
		mediaPlayer.prepareAsync();
		
		//starting the serverconnection thread
//	    serverConnection =new ServerConnection();
//	    serverConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//		serverConnection.execute();
//		return startId;
//	  
	
	  //starting the myAsynctask thread
		new MyAsynTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		return startId;
	}

	@Override
	public void onDestroy() {
		Log.i("MyLog","In Service onDestroy");
		super.onDestroy();
		serverConnection.cancel(true);
		mediaPlayer.stop();
		mediaPlayer.release();
	}
	
	private class ServerConnection extends AsyncTask<String,String,String>{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			//Here an http request is done for the server data and if the number of toilet users reach the maximum number of users
			//if max number reaches notify the activity with an alarm sound, vibration and change the UI
			
			//server is checked in 2 mins interval but here simulate with seconds
		
			while(true){
				try {
					if(this.isCancelled()){
						
						break;
					}
					Thread.sleep(10000);
					
					DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
					//postmethod
					HttpPost httpPost = new HttpPost(serverUrl);
					
					//getmethod
					HttpGet httpGet = new HttpGet(serverUrl);
					
					//for json data??
					httpPost.setHeader("Content-type","application/json");
					
					
				    JSONObject jsonRequestData = new JSONObject();	
				 
				    jsonRequestData.put("requestType", "CLEAN");
					httpPost.setEntity(new  StringEntity(jsonRequestData.toString()));
					
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity entity =  response.getEntity();
					
					
					//try reading 
					InputStream inputStream = null;
					String result = null;
					inputStream = entity.getContent();			
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
					
					//manipulate the string 
					StringBuilder stringBuilder = new StringBuilder();
					String line = null;
					
					while((line = reader.readLine())!=null){
						stringBuilder.append(line+"\n");
					}
					result = stringBuilder.toString();
					Log.i("MyLog","result before json"+result);
					if(inputStream != null)inputStream.close();
					
					//the code below assumes data is in json format : json parser
					JSONObject jsonObject;
					
					jsonObject = new JSONObject(result);
					JSONObject statusJSONObject = jsonObject.getJSONObject("status");
					//get the json string
					String statusString = statusJSONObject.getString("clean");
					
					Log.i("MyLog","result after json"+statusString);

					//Here check the requested value
					if(statusString.equals("YES")){
						
						//notify user
						
						notifyUser();
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
				    
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		 }
			return null;
		}
	
		
	}
	private void notifyUser(){
		//change activity UI 
		//start media player and vibration 
		Log.i("MyLog","In notify User");
		String newActivityStarted = null;
		//first check if Activity is on resume or onpause
		if(MainActivity.ACTIVITY_IS_ALIVE){
			//send localbroadcast
			sendLocalBroadCast();
		}
		else{
			//create a new activity
			newActivityStarted = "New Activity Started";
			startNewActivity(newActivityStarted);
			sendLocalBroadCast();	
		}	
	    //start media player
	   mediaPlayer.setLooping(true); // Set looping 
	   mediaPlayer.setVolume(100,100); 
  	   mediaPlayer.start();
	    
	}
	private void startNewActivity(String newActivityStarted){
		//create a new activity
		Log.i("MyLog","Service trying to start new activity");
		Intent intent = new Intent(context, MainActivity.class);
	    intent.setAction(Intent.ACTION_VIEW);
	    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TOP);	
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    newActivityStarted = "New Activity Started";
	    intent.putExtra("MessageFromService",newActivityStarted);
	    getApplicationContext().startActivity(intent);
	}
	private void sendLocalBroadCast(){
		//send localbroadcast
		Log.i("MyLog","Service trying to send local broadcast message");

		Intent intent = new Intent("CLEANING_TIME");
		intent.putExtra("ServiceLocalBroadcast", "cleaning time");
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	private class StopAlertReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//Actions to be done
			Log.i("MyLog","StopAlertReceiver in onReceive");
			Log.i("MyLog","LocalBroadcast: "+intent.getStringExtra("ServiceLocalBroadcast"));
			
			mediaPlayer.stop();
			//mediaPlayer.release();		
		}
		
	}
	// The separate thread
	//params: first: what is passed  second:for update third: the returned
	private class MyAsynTask extends AsyncTask<String,String,String>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mediaPlayer = MediaPlayer.create(context, R.raw.alert);
		}

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
			
			Log.i("MyLog","in Post Execute UI thread");
			
			ALERT_STATUS = true;
		    
			notifyUser();
		    
		 
		    
		    //TODO  vibration here
		    
		    
		    
		}

	}
    
	
}



