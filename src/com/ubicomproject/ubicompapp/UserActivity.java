package com.ubicomproject.ubicompapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.os.Build;

public class UserActivity extends ActionBarActivity {
    
	private String SERVER_URL = "http://192.169.1.102/cgi-bin/gateway.py";
	private String FAN_CONTROL_STATUS = "FAN_OFF";
	
	private String HEATER_CONTROL_STATUS = "HEATER_OFF";
	
	// Declare RadioButstons
    RadioGroup fanRadioGroup;
    RadioButton fanOnRadio;
    RadioButton fanOffRadio;
    
    RadioGroup heaterRadioGroup;
    RadioButton heaterOnRadio;
    RadioButton heaterOffRadio;
    
    private Context context;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);

		context=this;
		
		fanRadioGroup = (RadioGroup) findViewById(R.id.userFanRadioGroup);
		fanOnRadio = (RadioButton) findViewById(R.id.userFanOnRadio);
		fanOffRadio = (RadioButton) findViewById(R.id.userFanOffRadio);
		
		heaterRadioGroup = (RadioGroup) findViewById(R.id.userHeaterRadioGroup);
		heaterOnRadio = (RadioButton) findViewById(R.id.userHeaterOnRadio);
		heaterOffRadio = (RadioButton) findViewById(R.id.userHeaterOffRadio);
		
		
	   //action listener for the fan radio group 
		fanRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
		
				//FAN_CONTROL_STATUS = (fanOnRadio.isChecked())?"ON":"OFF";
				FAN_CONTROL_STATUS = (fanOffRadio.isChecked())?"FAN_OFF":"FAN_ON";
				
				
				new SendStatusToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,FAN_CONTROL_STATUS);


			}
		});
	    //action listener for the heater radio group 
		heaterRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
		
				//FAN_CONTROL_STATUS = (fanOnRadio.isChecked())?"ON":"OFF";
				HEATER_CONTROL_STATUS = (heaterOffRadio.isChecked())?"HEATER_OFF":"HEATER_ON";
				
				
				new SendStatusToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,HEATER_CONTROL_STATUS);


			}
		});
	     
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user, menu);
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
			String result = SendControlMessage(params[0]);
			return result;
		}
		
		
		private String SendControlMessage(String clientMessage){
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
					String statusString = statusJSONObject.getString("message");
					
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
