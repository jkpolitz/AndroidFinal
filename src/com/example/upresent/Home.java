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
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
		postJSON(jsonObject, deletePres);
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
		super.onBackPressed();
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
			JSONObject resultObj = null;

			/*try {
				resultJSON = new JSONArray(result);
				for (int i = 0; i < resultJSON.length(); i++) {
					resultObj = resultJSON.getJSONObject(i);
					pres.add(new Presentation(resultObj.getString("presName"),
							resultObj.getInt("presId")));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}*/
			return resultJSON;
		}

		protected void onPostExecute(JSONArray result) {
			findViewById(R.id.pBar).setVisibility(View.GONE);
			findViewById(R.id.txtContainer).setVisibility(View.VISIBLE);
			adpt = new Adapter(((Home)context), R.layout.list_row, pres);
			ListView list = (ListView) findViewById(R.id.list);
			list.setAdapter(adpt);
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

	private void postJSON(JSONObject json, String url) {
		InputStream inputStream = null;
		String result = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			String jsonS = "";
			jsonS = json.toString();
			StringEntity se = new StringEntity(jsonS);
			httpPost.setEntity(se);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			HttpResponse httpResponse = httpclient.execute(httpPost);
			inputStream = httpResponse.getEntity().getContent();
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
				Log.d("JKP_193", result);
			} else
				result = "Did not work!";

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
	}

	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	}
}
