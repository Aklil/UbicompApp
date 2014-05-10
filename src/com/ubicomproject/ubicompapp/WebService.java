package com.ubicomproject.ubicompapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import android.app.Service;
//import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class WebService extends Service{

  
   static String serverUrl = "";
	
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
		//Log.i("MyLog","Logging intent extra message"+intent.getStringExtra("MessageFromActivity"));
		Log.i("MyLog","Logging intent in onStartCommand"+intent.toString());
		
		//starting the serverconnection thread
		new ServerConnection().execute();
		return startId;
	
	  //starting the myAsynctask thread
		//new MyAsynTask().execute();
		//return startId;
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();
		
		Log.i("MyLog","In Service onDestroy");
	}
	
	private class ServerConnection extends AsyncTask<String,String,String>{
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			//Here an http request is done for the server data and if the number of toilet users reach the maximum number of users
			//if max number reaches notify the activity with an alarm sound, vibration and change the UI
			
			//server is checked in 2 mins interval but here simulate with seconds
			
			while(true){
				try {
					Thread.sleep(2000);
					
					DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
					//postmethod
					HttpPost httpPost = new HttpPost(serverUrl);
					
					//getmethod
					HttpGet httpGet = new HttpGet(serverUrl);
					
					//for json data??
					//httpPost.setHeader("Content-type","application/json");
						
					//try reading 
					InputStream inputStream = null;
					String result = null;
					
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity entity =  response.getEntity();
					inputStream = entity.getContent();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
					
					//manipulate the string 
					StringBuilder stringBuilder = new StringBuilder();
					String line = null;
					
					while((line = reader.readLine())!=null){
						stringBuilder.append(line+"\n");
					}
					result = stringBuilder.toString();
					if(inputStream != null)inputStream.close();
					
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
		}
		
		
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
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TOP);		    
		    intent.putExtra("MessageFromService", "New Activity Started");
		    getApplicationContext().startActivity(intent);
		}

	}

	
}



