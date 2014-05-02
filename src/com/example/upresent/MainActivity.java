package com.example.upresent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity {
	public TextView userTV;
	public TextView passTV;
	public Button login;
	String loginURL = "http://upresent.org/api/index.php/verify/";
	private String creds;
	boolean verified = false;
	private static final int LOGIN_REQUEST = 309;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		login = (Button) findViewById(R.id.login);
		userTV = (TextView) findViewById(R.id.userN);
		passTV = (TextView) findViewById(R.id.pass);
		
		login.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.pBar).setVisibility(View.VISIBLE);
				creds = loginURL;
				creds+=userTV.getText().toString();
				creds+="/";
				creds+=passTV.getText().toString();
				Log.d("JKP", creds);
				new Login().execute(creds);
			}
		});
		
		
	}
	private void loggedIn(String userName) {
		Log.d("JKP", "Launching remote");
		Intent intent = new Intent(this, Home.class);
		intent.putExtra(Home.LOGIN_KEY, userName);
		startActivityForResult(intent, LOGIN_REQUEST);
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		     if(resultCode == RESULT_OK){
		 		userTV.setText("");
		 		passTV.setText("");
		 		userTV.requestFocus();
		     }
		     if (resultCode == RESULT_CANCELED) {    
		         //Write your code if there's no result
		     }
		}
	
	private class Login extends AsyncTask<String, Integer, JSONObject> {
		protected JSONObject doInBackground(String... url) {
			String result = loadJsonFromNetwork(url[0]);
			JSONObject resultJSON = null;
			try {
				resultJSON = new JSONObject(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return resultJSON;
		}
		
		protected void onPostExecute(JSONObject result) {
			try {
				JSONObject resultJSON = result;
				//JSONArray resultArray = resultJSON.getJSONArray("registered");
				//JSONObject json = resultArray.getJSONObject(0);
				verified = resultJSON.getBoolean("registered");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(verified) {
				loggedIn(userTV.getText().toString());
				Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "Username and Password Do Not Match", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public String loadJsonFromNetwork(String jsonUrl) {
		JSONObject tempJSON = null;
		String json = null;
		HttpClient httpC = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(jsonUrl);

		try {
			HttpResponse resp = httpC.execute(httpGet);
			StatusLine statusLine = resp.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode == 200) {
				HttpEntity entity = resp.getEntity();
				InputStream stream = entity.getContent();
				Scanner scanner = new Scanner(stream);
				json = scanner.useDelimiter("\\A").next();
				Log.d("JKP", json);
				scanner.close();
			} else {
				Log.d("JKP", "Failed");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}
}
