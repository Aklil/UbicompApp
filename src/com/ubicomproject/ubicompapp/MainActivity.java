package com.ubicomproject.ubicompapp;

//import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.ActionBar;
//import android.support.v4.app.Fragment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
//import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
//import android.view.View;
//import android.view.ViewGroup;
//import android.os.Build;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	private String SERVER_URL = "http://192.168.0.240/cgi-bin/gateway.py";
	
	private String FAN_CONTROL_STATUS = "OFF";
	
	private String TOILET_CLEANED_MESSAGE = "CLEANED";
	
	private boolean ALERT_RECEIVED = false;
	
	public static boolean ACTIVITY_IS_ALIVE;
	
	private Context context;
	
	// Declare RadioButstons
    RadioGroup fanRadioGroup;
    RadioButton fanOnRadio;
    RadioButton fanOffRadio;
    Button startButton;
	Button stopButton;
	Button alertButton;
	
	CleanAlertReceiver cleanAlertReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		unlockScreen();
		setContentView(R.layout.activity_main);
		context = this;
		//find radio buttons
		fanRadioGroup = (RadioGroup) findViewById(R.id.fanRadioGroup);
		fanOnRadio = (RadioButton) findViewById(R.id.fanOnRadio);
		fanOffRadio = (RadioButton) findViewById(R.id.fanOffRadio);
		
		startButton = (Button) findViewById(R.id.startButton);
		stopButton = (Button) findViewById(R.id.stopButton);
		alertButton = (Button) findViewById(R.id.alertButton);
		
		//hide alertButton 
		if(!WebService.ALERT_STATUS){
//			alertButton.setVisibility(View.INVISIBLE);
		}
		
		//registered alert receiver
		cleanAlertReceiver = new CleanAlertReceiver();
		
		

	     
	    //action listener for the radio group 
		fanRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
		
				//FAN_CONTROL_STATUS = (fanOnRadio.isChecked())?"ON":"OFF";
				FAN_CONTROL_STATUS = (fanOffRadio.isChecked())?"OFF":"ON";
				
//				String fanControlMessage = FAN_CONTROL_STATUS;				
//				JSONObject jsonData = new JSONObject();	
//				try {
//					jsonData.put("fanControlMessage", fanControlMessage);
//					jsonData.put("requestType", "FAN");
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
//				new SendStatusToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonData);
//				new SendStatusToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,FAN_CONTROL_STATUS);


			}
		});
	     
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

		alertButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				stopAlert();
				
			}
		});
	}
   

	
	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter intentFilter = new IntentFilter("CLEANING_TIME");
		LocalBroadcastManager.getInstance(context).registerReceiver(cleanAlertReceiver, intentFilter);
	}


	@Override
	protected void onResume() {
		super.onResume();
		ACTIVITY_IS_ALIVE = true;
		
	}
   
	
	@Override
	protected void onPause() {
		super.onPause();
		stopAlert();
		ACTIVITY_IS_ALIVE = false;
		LocalBroadcastManager.getInstance(context).unregisterReceiver(cleanAlertReceiver);

	}

    private void stopAlert(){
    	//stop music and vibration
		Intent intent = new Intent("STOP_ALERT");
		intent.putExtra("ActivityLocalBroadcast", "Stop Alert");
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		ALERT_RECEIVED = false;
		setAlertButton();
			
		WebService.ALERT_STATUS = false;
    }
    
    private void setAlertButton(){
    	if(ALERT_RECEIVED){
    		alertButton.setBackgroundResource(R.drawable.circular_active_button);
		    alertButton.setText(R.string.alert_active_button);
		    alertButton.setTextColor(R.color.alert_active_color_text);
    	}else{
    		alertButton.setBackgroundResource(R.drawable.circular_inactive_button);
            alertButton.setTextColor(R.color.alert_inactive_color_text);
    		alertButton.setText(R.string.alert_inactive_button);
    	}
    	
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		switch(item.getItemId()){
		
			case R.id.menu_toilet_cleaned:
				Toast.makeText(this,"Toilet Cleaned", Toast.LENGTH_LONG).show();
				
				
//				JSONObject jsonData = new JSONObject();	
//				try {
//					jsonData.put("toiletCleanStatus", "CLEANED_MESSAGE");
//					jsonData.put("toiletCleanMessage", TOILET_CLEANED_MESSAGE);				
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}		
				
//				new SendStatusToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,TOILET_CLEANED_MESSAGE);
				
			    return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
    private void unlockScreen(){
    	Window window = this.getWindow();
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
    }
	//Local Broadcast receiver from service this should be registered in the oncreate and unregistered in onpause
	private class CleanAlertReceiver extends BroadcastReceiver{
       
		@Override
		public void onReceive(Context context, Intent intent) {
			//Actions to be done
			Log.i("MyLog","CleanAlertReceiver in onReceive");
//			if(intent.getStringExtra("ServiceLocalBroadcast") !=null){
				Log.i("MyLog","LocalBroadcast: "+intent.getStringExtra("ServiceLocalBroadcast"));
				
				startButton.setBackgroundColor(Color.GREEN);
				ALERT_RECEIVED = true;
				setAlertButton();
			    			  					
		}
		
	}
	
	//http send radio button status
	private class SendStatusToServer extends AsyncTask<String , String , String>{
       
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
//		    dialog = new ProgressDialog(context);
//	        dialog.setCancelable(true);
//            dialog.setMessage("setting..");
//	        dialog.show();

	              
		}


		@Override
		protected String doInBackground(String... params) {

			Log.i("MyLog","Trying to set Radio button status");
			
			//send	status to server
			String result = SendFanControlMessage(params[0]);
			return result;
		}
		
		
		private String SendFanControlMessage(String clientMessage){
			    String postResult = null;
				try {
				
					Thread.sleep(1000);
					
//					//name should correspond to db name
//					String fanControlMessage = FAN_CONTROL_STATUS;
////					String data = URLEncoder.encode("fanControlMessage","UTF-8") +  URLEncoder.encode(fanControlMessage,"UTF-8");
//					
					JSONObject jsonData = new JSONObject();	
					jsonData.put("clientMessage", clientMessage);
					jsonData.put("requestType", "CLIENT");
					
//					List<NameValuePair> data = new ArrayList<NameValuePair> ();
//					
//					data.add(new NameValuePair("fanControlMessage",fanControlMessage));
//						
						
					DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
					//postmethod
					HttpPost httpPost = new HttpPost(SERVER_URL);
					httpPost.setHeader("Content-type","application/json");
					
					httpPost.setEntity(new  StringEntity(jsonData.toString()));
					//for json data??
					
					HttpResponse response = httpClient.execute(httpPost);
				
					// Get hold of the response entity (-> the data):
					HttpEntity entity = response.getEntity();
					Log.i("MyLog","Checking post response"+entity.toString());
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
					if(inputStream != null)inputStream.close();
					
//					HERE check the returned json data ....check the key value
					JSONObject jsonObject;
					
					jsonObject = new JSONObject(result);
					JSONObject statusJSONObject = jsonObject.getJSONObject("status");
					//get the json string
					String statusString = statusJSONObject.getString("fan");
					
					if(entity !=null){
						postResult = statusString;
						Log.i("MyLog","Checking result if entitiy is not null:---"+result);
						Log.i("MyLog","Checking result if entitiy is not null:---"+statusString);
					}else{
						postResult ="error";
						Log.i("MyLog","entitiy null");
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
			
			return postResult;
			
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i("MyLog","Checking result in onPostExecute"+result);
			 Toast.makeText(context, result, Toast.LENGTH_LONG).show();
//			 if(dialog.isShowing()){
//	             dialog.cancel();
//
//			 }
		}
	}

}
