package com.example.upresent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.upresent.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity {

	String userName = "";
	public static final String LOGIN_KEY = "Login";
	Context context = this;

	private static final int PRES_REQUEST = 6349;
	ArrayList<Presentation> pres = new ArrayList<Presentation>();
	Adapter adpt;
	ListView list;
	private Boolean empty = false;

	public String getPres = "http://upresent.org/api/index.php/getPresentations/";
	public String deletePres = "http://upresent.org/api/index.php/deletePresentation";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		Intent intent = getIntent();
		userName = intent.getStringExtra(LOGIN_KEY);

		TextView userN = (TextView) findViewById(R.id.userName);
		userN.setText("Welcome, " + userName);

		getPres += userName;
		Log.d("JKP", "content set" + getPres);
		new GetPresentations().execute(getPres);

		Log.d("JKP", "pulled presentations");

		TextView logout = (TextView) findViewById(R.id.logout);
		TextView refresh = (TextView) findViewById(R.id.refresh);
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pres.clear();
				Toast.makeText(getApplicationContext(), "Refreshing UPresents",
						Toast.LENGTH_SHORT).show();
				new GetPresentations().execute(getPres);
			}
		});

	}

	void launchRemote(int presID, String name) {
		Log.d("JKP", "Launching remote");
		Intent intent = new Intent(this, Remote.class);
		intent.putExtra(Remote.PRES_KEY, presID);
		intent.putExtra(Remote.PRESN_KEY, name);
		startActivityForResult(intent, PRES_REQUEST);
	}

	void deletePresentation(String pName, int pos) {
		Log.d("JKP", "Deleting Presentation");
		// post slide change to server
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.accumulate("title", pName);
			jsonObject.accumulate("username", userName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Toast.makeText(getApplicationContext(), "Deleting: " + pName,
				Toast.LENGTH_SHORT).show();
		new DeletePresentation().execute(jsonObject);
		pres.remove(pos);
		adpt.notifyDataSetChanged();
	}

	void refresh() {
		new GetPresentations().execute(getPres);
		adpt.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_OK);
		pres.clear();
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
			JSONArray resultJSON = null;
			if (result != "") {
				empty = false;
				JSONObject resultObj = null;

				try {
					resultJSON = new JSONArray(result);
					for (int i = 0; i < resultJSON.length(); i++) {
						resultObj = resultJSON.getJSONObject(i);
						pres.add(new Presentation(resultObj
								.getString("presName"), resultObj
								.getInt("presId")));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				empty = true;
				/*Toast.makeText(
						getApplicationContext(),
						"You have no UPresents. Please visit UPresent.com to create one!",
						Toast.LENGTH_SHORT).show();*/
			}
			return resultJSON;
		}

		protected void onPostExecute(JSONArray result) {
			findViewById(R.id.pBar).setVisibility(View.GONE);
			if (!empty) {
				findViewById(R.id.txtContainer).setVisibility(View.VISIBLE);
				adpt = new Adapter(((Home) context), R.layout.list_row, pres);
				ListView list = (ListView) findViewById(R.id.list);
				list.setAdapter(adpt);
			}
		}

		public String loadJsonFromNetwork(String jsonUrl) {
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
					json = getResponseTextTraditionalWay(stream);
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

		private String getResponseTextTraditionalWay(InputStream stream)
				throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream));
			StringBuilder sb = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				sb.append(line + "\n");
				line = reader.readLine();
			}
			return sb.toString();
		}
	}
	private class DeletePresentation extends AsyncTask<JSONObject, Integer, JSONObject> {
		protected JSONObject doInBackground(JSONObject... obj) {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(deletePres);
				String jsonS = "";
				jsonS = obj[0].toString();
				StringEntity se = new StringEntity(jsonS);
				httpPost.setEntity(se);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				httpclient.execute(httpPost);
			} catch (Exception e) {
				Log.d("InputStream", e.getLocalizedMessage());
			}
			return null;
		}
	}

}
