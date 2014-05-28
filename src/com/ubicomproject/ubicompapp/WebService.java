package com.ubicomproject.ubicompapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import android.hardware.Camera.PreviewCallback;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class WebService extends Service{

   private static final String TAG = "MyLog";
   public static boolean ALERT_STATUS = false;   // to check alert button 
   private ServerConnection serverConnection;
   static String serverUrl = "http://192.169.1.102/cgi-bin/gateway.py";
   private boolean MEDIA_PLAYER_DATA_SOURCE_SETTED = false;
   private boolean MEDIA_PLAYER_PREPARED = false;
   private boolean MEDIA_PLAYER_STARTED = false;
   private Context context;
   private StopAlertReceiver stopAlertReceiver;
   
   private MyAsynTask myAsynTask ;
   private final ReentrantLock lock = new ReentrantLock();
   private final Condition tryAgain = lock.newCondition();
   
   private MediaPlayer mediaPlayer = new MediaPlayer();;

   
	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i(TAG,"In Service onCreate");

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG,"In Service onStartCommand");
		//Log.i(TAG,"Logging intent extra message"+intent.getStringExtra("MessageFromActivity"));
		if(intent!=null){
			Log.i(TAG,"Logging intent in onStartCommand"+intent.toString());

		}
		 
		context = this;
		
		//register alert stop receiver
		stopAlertReceiver = new StopAlertReceiver();
		IntentFilter intentFilter = new IntentFilter("STOP_ALERT");
		LocalBroadcastManager.getInstance(context).registerReceiver(stopAlertReceiver, intentFilter);
//		if(!MEDIA_PLAYER_DATA_SOURCE_SETTED) setMediaDataSource();
		if(!MEDIA_PLAYER_PREPARED)prepareMediaPlayer();
		
		//starting the serverconnection thread
	    serverConnection =new ServerConnection();
	    serverConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		return startId;
	  
	
	  //starting the myAsynctask thread
		
		
//		new MyAsynTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//		Log.i(TAG,"new thread initialized");
		
//		return startId;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG,"In Service onDestroy");
		super.onDestroy();
		serverConnection.cancel(true);
		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
		mediaPlayer = null;
		MEDIA_PLAYER_DATA_SOURCE_SETTED = false;
		LocalBroadcastManager.getInstance(context).unregisterReceiver(stopAlertReceiver);
	}
	private void setMediaDataSource(){
		try {
			Uri mediaSource = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.alert);
//	        String url = "http://www.brothershouse.narod.ru/music/pepe_link_-_guitar_vibe_113_club_mix.mp3"; // your URL here
			mediaPlayer.setDataSource(context, mediaSource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 MEDIA_PLAYER_DATA_SOURCE_SETTED = true;
	}
	private void prepareMediaPlayer(){
		
		try {
			Uri mediaSource = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.alert);
//	        String url = "http://www.brothershouse.narod.ru/music/pepe_link_-_guitar_vibe_113_club_mix.mp3"; // your URL here
			mediaPlayer.setDataSource(context, mediaSource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		try {
			mediaPlayer.prepareAsync();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer player) {
            	MEDIA_PLAYER_PREPARED = true;
            	Log.i(TAG,"Media player prepared");
            }

        });
	}
	private class ServerConnection extends AsyncTask<String,String,String>{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
		
//			mediaPlayer = MediaPlayer.create(context, R.raw.alert);
			while(true){
				try {
					if(this.isCancelled()){
						Log.i(TAG,"Thread cancelled");
						break;
					}
					Thread.sleep(10000);
					
					DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
					//postmethod
					HttpPost httpPost = new HttpPost(serverUrl);
					
					Log.i(TAG,"Trying to make connection to server");
					
					//for json data??
					httpPost.setHeader("Content-type","application/json");
					
					
				    JSONObject jsonRequestData = new JSONObject();	
				    jsonRequestData.put("requestType", "CLIENT");
				    jsonRequestData.put("clientMessage", "CLEAN");
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
					Log.i(TAG,"result before json"+result);
					if(inputStream != null)inputStream.close();
					
					//the code below assumes data is in json format : json parser
					JSONObject jsonObject;
					
					jsonObject = new JSONObject(result);
					JSONObject statusJSONObject = jsonObject.getJSONObject("status");
					//get the json string
					String statusString = statusJSONObject.getString("clean");
					
					Log.i(TAG,"result after json"+statusString);

					//Here check the requested value
					if(statusString.equals("YES")){
						
						//notify user
						ALERT_STATUS = true;
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
		Log.i(TAG,"In notify User");
		String newActivityStarted = null;
		//first check if Activity is on resume or onpause
		if(CleaningActivity.CLEANING_ACTIVITY_IS_ALIVE){
			//send localbroadcast
			sendLocalBroadCast();
		}
		else if(MainActivity.MAIN_ACTIVITY_IS_ALIVE){
			Log.i(TAG,"Main Activity is Alive");
			if(!MEDIA_PLAYER_PREPARED)prepareMediaPlayer();
			sendLocalBroadCast();
			Intent intent = new Intent(context, CleaningActivity.class);
		    intent.setAction(Intent.ACTION_VIEW);
		    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
		    startActivity(intent);
			
		}
		else{
			//create a new activity
			newActivityStarted = "New Activity Started";
			startNewActivity(newActivityStarted);
			sendLocalBroadCast();	
		}	
	    //start media player
	   while(!MEDIA_PLAYER_PREPARED){Log.i(TAG,"Media player not prepared in the while loop");}
	   mediaPlayer.setLooping(true); // Set looping 
	   mediaPlayer.setVolume(100,100); 
  	   mediaPlayer.start();
  	   MEDIA_PLAYER_STARTED = true;
	}
	private void startNewActivity(String newActivityStarted){
		//create a new activity
		Log.i(TAG,"Service trying to start new activity");
		Intent intent = new Intent(context, CleaningActivity.class);
	    intent.setAction(Intent.ACTION_VIEW);
	    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TOP);	
	    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
	    newActivityStarted = "New Activity Started";
	    intent.putExtra("MessageFromService",newActivityStarted);
	    startActivity(intent);
	}
	private void sendLocalBroadCast(){
		//send localbroadcast
		Log.i(TAG,"Service trying to send local broadcast message");

		Intent intent = new Intent("CLEANING_TIME");
		intent.putExtra("ServiceLocalBroadcast", "cleaning time");
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	private class StopAlertReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//Actions to be done
			Log.i(TAG,"StopAlertReceiver in onReceive");
			if(intent.getStringExtra("ActivityLocalBroadcast") !=null){
				Log.i(TAG,"LocalBroadcast: "+intent.getStringExtra("ActivityLocalBroadcast"));
				
				if( MEDIA_PLAYER_STARTED){
					
					Log.i(TAG,"Stopping Media player already started");
					mediaPlayer.stop();
					mediaPlayer.reset();
					prepareMediaPlayer();
//					mediaPlayer.release();
//					mediaPlayer = null;
					MEDIA_PLAYER_PREPARED = false;
					//mediaPlayer.release();
					MEDIA_PLAYER_STARTED=false;
					
				}
				
			}
			
		}
		
	}
	// The separate thread
	//params: first: what is passed  second:for update third: the returned
	private class MyAsynTask extends AsyncTask<String,String,String>{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
		}

		protected String doInBackground(String... params) {
			try {
				lock.lockInterruptibly();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(this.isCancelled()){
				
				return null;
			}
			int i=0;
			try{
			  while(i<5){
				Thread.sleep(1000);
				
				Log.i(TAG,"Executing in Thread");
				i++;
			  }
			}catch(Exception e){
				Log.i(TAG,"Exception Occured in MyAsyncTask"+e.toString());
			}
			
		
//			try {
//				tryAgain.await();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		    lock.unlock();
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			Log.i(TAG,"in Post Execute UI thread");
			
			ALERT_STATUS = true;
		    
			notifyUser();
		    
			this.cancel(true);
		    
		    //TODO  vibration here
		    
		    
		    
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			
			Log.i(TAG,"Thread Cancelled");
		}

	}
    
	
}



