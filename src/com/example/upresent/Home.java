package com.example.upresent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

import com.example.upresent.R;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity {


	private static final int PRES_REQUEST = 6349;
	private Presentation [] pres = new Presentation[2];

	public String getPres = "http://upresent.org/api/index.php/getPresentations/jackjp";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		Log.d("JKP", "content set");
		new GetPresentations().execute(getPres);
		
		Adapter adpt = new Adapter(this, R.layout.list_row, pres);
		ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(adpt);
		Log.d("JKP", "pulled presentations");
	}

	private void launchRemote(int presID) {
		Log.d("JKP", "Launching remote");
		Intent intent = new Intent(this, Remote.class);
		intent.putExtra(Remote.PRES_KEY, presID);
		startActivityForResult(intent, PRES_REQUEST);
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

	private class GetPresentations extends
			AsyncTask<String, Integer, JSONArray> {

		protected JSONArray doInBackground(String... url) {
			String result = loadJsonFromNetwork(url[0]);
			Log.d("JKP_107", result);
			JSONArray resultJSON = null;
			try {
				resultJSON = new JSONArray(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return resultJSON;
		}

		protected void onPostExecute(JSONArray result) {
			try {
				JSONArray resultJSON = result;
				JSONObject resultObj;
				for (int i = 0; i < resultJSON.length(); i++) {
					resultObj = resultJSON.getJSONObject(i);
					pres[i] = new Presentation(resultObj.getString("presName"), resultObj.getInt("presId"));
					Toast.makeText(getApplicationContext(),
							pres[i].toString(), Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			findViewById(R.id.pBar).setVisibility(View.GONE);
			findViewById(R.id.txtContainer).setVisibility(View.VISIBLE);

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
}
