package com.example.upresent;

import java.io.IOException;
import java.io.InputStream;
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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Remote extends Activity {
	public ImageButton prev;
	public ImageButton next;
	public TextView slideInfo;
	public ImageView slideImg;
	public TextView presName;
	public TextView resetPoll;
	public TextView endPres;

	private int presID;
	private int numS = 5;
	private int currSlide = 1;
	private String pName;
	private Bitmap[] slideImgs;
	private Boolean poll = false;

	public static final String PRES_KEY = "PresKey";
	public static final String PRESN_KEY = "PresNKey";

	private String apiInfo;
	private String rootURL = "http://upresent.org/";
	private String getSlides = "http://upresent.org/api/index.php/getSlides/";
	private String reset = "http://upresent.org/api/index.php/resetPoll";
	private String setSlide = "http://upresent.org/api/index.php/setCurrentSlide";
	private String end = "http://upresent.org/api/index.php/finishPresentation";
	private String getSlideInfo = "http://upresent.org/api/index.php/getCurrentSlide/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		presID = intent.getIntExtra(PRES_KEY, presID);
		pName = intent.getStringExtra(PRESN_KEY);

		setContentView(R.layout.present);

		prev = (ImageButton) findViewById(R.id.previous);
		next = (ImageButton) findViewById(R.id.next);
		slideInfo = (TextView) findViewById(R.id.slideNumInfo);
		slideImg = (ImageView) findViewById(R.id.currSlideImg);
		presName = (TextView) findViewById(R.id.presName);
		endPres = (TextView) findViewById(R.id.endPres);
		resetPoll = (TextView) findViewById(R.id.resetPoll);

		presName.setText(pName);

		apiInfo = getSlides;
		apiInfo += presID;
		new GetSlides().execute(apiInfo);

		prev.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
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
		resetPoll.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (poll) {
					resetPoll();
				}
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
		this.finish();
	}

	private void updateSlide() {
		slideInfo.setText("Slide: " + currSlide + " of " + numS);
		slideImg.setImageBitmap(slideImgs[currSlide - 1]);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.accumulate("presId", presID);
			jsonObject.accumulate("currSlide", currSlide);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		new UpdateSlide().execute(jsonObject);
	}

	private void resetPoll() {
		Toast.makeText(getApplicationContext(), "Resetting Poll",
				Toast.LENGTH_SHORT).show();
		String json = "{\"presId\":\"" + presID + "\",\"slide\":" + currSlide
				+ "}";
		new ResetPoll().execute(json);
	}

	private void endPresentation() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.accumulate("presId", presID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		new EndPresentation().execute(jsonObject);
		onBackPressed();
	}

	private class GetSlides extends AsyncTask<String, Integer, JSONObject> {
		protected JSONObject doInBackground(String... url) {
			String result = loadJsonFromNetwork(url[0]);
			JSONObject resultJSON = null;
			JSONObject resultSlides = null;
			try {
				resultJSON = new JSONObject(result);
				resultSlides = resultJSON.getJSONObject("slides");
				String numSlides = resultJSON.getString("numSlides");
				numS = Integer.parseInt(numSlides);
				slideImgs = new Bitmap[numS];
				for (int i = 0; i < numS; i++) {
					String key = "" + (i + 1);
					String link = rootURL;
					link += resultSlides.getString(key);
					slideImgs[i] = loadImageFromNetwork(link);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return resultJSON;
		}

		protected void onPostExecute(JSONObject result) {
			updateSlide();
			RelativeLayout rL = (RelativeLayout) findViewById(R.id.onLoad);
			rL.setVisibility(View.VISIBLE);
			rL = (RelativeLayout) findViewById(R.id.onLoading);
			rL.setVisibility(View.GONE);
		}
	}

	private class UpdateSlide extends
			AsyncTask<JSONObject, Integer, JSONObject> {
		protected JSONObject doInBackground(JSONObject... obj) {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(setSlide);
				String jsonS = "";
				jsonS = obj[0].toString();
				StringEntity se = new StringEntity(jsonS);
				httpPost.setEntity(se);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				httpclient.execute(httpPost);
				String temp = getSlideInfo + presID;
				JSONObject result = new JSONObject(loadJsonFromNetwork(temp));
				temp = result.getString("poll");
				if (temp == "false") {
					poll = false;
				} else {
					poll = true;
				}

			} catch (Exception e) {
				Log.d("InputStream", e.getLocalizedMessage());
			}

			return null;
		}

		protected void onPostExecute(JSONObject result) {
			if (poll) {
				resetPoll.setTextColor(Color.parseColor("#FFFF9F00"));
			} else {
				resetPoll.setTextColor(Color.parseColor("#BBEDEDED"));
			}
		}
	}

	private class EndPresentation extends
			AsyncTask<JSONObject, Integer, JSONObject> {
		protected JSONObject doInBackground(JSONObject... obj) {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(end);
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

	private class ResetPoll extends AsyncTask<String, Integer, JSONObject> {
		protected JSONObject doInBackground(String... json) {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(reset);
				StringEntity se = new StringEntity(json[0]);
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
}