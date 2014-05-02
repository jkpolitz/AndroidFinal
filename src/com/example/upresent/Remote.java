package com.example.upresent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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

import com.example.upresent.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class Remote extends Activity {
	public ImageButton prev;
	public ImageButton next;
	public TextView slideInfo;
	public ImageView slideImg;
	public TextView presName;
	public Button endPres;

	private int presID;
	private int numS = 5;
	private int currSlide = 1;
	private String pName;
	private Bitmap[] slideImgs;

	public static final String PRES_KEY = "PresKey";

	private String apiInfo;
	private String rootURL = "http://upresent.org/";
	private String getSlides = "http://upresent.org/api/index.php/getSlides/";
	private String getPresInfo = "http://upresent.org/api/index.php/getPresInfo/";
	private String setSlide = "http://upresent.org/api/index.php/setCurrentSlide";
	private String end = "http://upresent.org/api/index.php/finishPresentation";

	// private String slideURLs = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("JKP", "In remote");
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		presID = intent.getIntExtra(PRES_KEY, presID);
		String temp = "" + presID;
		Log.d("JKP_75", temp);

		setContentView(R.layout.present);

		prev = (ImageButton) findViewById(R.id.previous);
		next = (ImageButton) findViewById(R.id.next);
		slideInfo = (TextView) findViewById(R.id.slideNumInfo);
		slideImg = (ImageView) findViewById(R.id.currSlideImg);
		presName = (TextView) findViewById(R.id.presName);
		endPres = (Button) findViewById(R.id.endPres);

		apiInfo = getSlides;
		apiInfo += presID;
		Log.d("JKP", apiInfo);
		// IMPLEMENT LOADING BITMAP FROM SLIDE LINKS
		new GetSlides().execute(apiInfo);

		prev.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// UPDATE SLIDES
				if (currSlide > 1) {
					currSlide--;
					updateSlide();
				} else {
					Toast.makeText(getApplicationContext(),
							"You are already at the first slide.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		next.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// UPDATE SLIDES
				if (currSlide < numS) {
					currSlide++;
					updateSlide();
				} else {
					Toast.makeText(getApplicationContext(),
							"You are already at the last slide.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		slideImg.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// UPDATE SLIDES
				if (currSlide < numS) {
					currSlide++;
					updateSlide();
				} else {
					Toast.makeText(getApplicationContext(),
							"You are already at the last slide.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		endPres.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Ending Presentation.",
						Toast.LENGTH_SHORT).show();
				endPresentation();
			}
		});

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

	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_OK);
		super.onBackPressed();
	}

	private void updateSlide() {
		slideInfo.setText(currSlide + "|" + numS);
		slideImg.setImageBitmap(slideImgs[currSlide - 1]);
		// post slide change to server
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.accumulate("presId", presID);
			jsonObject.accumulate("currSlide", currSlide);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		postJSON(jsonObject, setSlide);

	}

	private void endPresentation() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.accumulate("presId", presID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		postJSON(jsonObject, end);
		onBackPressed();
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

	private class GetSlides extends AsyncTask<String, Integer, JSONObject> {
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
				JSONObject resultSlides = resultJSON.getJSONObject("slides");
				String numSlides = resultJSON.getString("numSlides");
				numS = Integer.parseInt(numSlides);
				slideImgs = new Bitmap[numS];
				for (int i = 0; i < numS; i++) {
					String key = "" + (i + 1);
					String link = rootURL;
					link += resultSlides.getString(key);
					slideImgs[i] = loadImageFromNetwork(link);
				}
				slideImg.setImageBitmap(slideImgs[0]);
				slideInfo.setText(currSlide + "|" + numS);
				Toast.makeText(getApplicationContext(), numSlides,
						Toast.LENGTH_SHORT).show();
				// JSONObject json = resultArray.getJSONObject(0);
				// verified = resultJSON.getBoolean("registered");
			} catch (Exception e) {
				e.printStackTrace();
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

	public Bitmap loadImageFromNetwork(String imgUrl) {
		Bitmap img = null;
		URL url;
		try {
			url = new URL(imgUrl.replaceAll(" ", "%20"));
			img = BitmapFactory.decodeStream(url.openStream());
		} catch (MalformedURLException e) {
			Log.e("JKP", "URL is bad");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("JKP", "Failed to decode Bitmap");
			e.printStackTrace();
		}
		return img;
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