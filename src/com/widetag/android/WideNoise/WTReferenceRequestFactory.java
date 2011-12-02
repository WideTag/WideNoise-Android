package com.widetag.android.WideNoise;

import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.app.Activity;
import android.provider.Settings.Secure; 
import org.apache.http.NameValuePair;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Settings.Secure;
import org.apache.http.HttpResponse;


public class WTReferenceRequestFactory implements WTRequestFactory
{
	private Context context;
	private final static String TAGS_URL = "http://www.widetag.com/widenoise/v2/api/noise/%s/tag/";
	private final static String REPORTING_URL = "http://www.widetag.com/widenoise/v2/api/noise/";
	private final static String MAP_URL = "http://www.widetag.com/widenoise/v2/api/noise/";
	private final static String KEY = "###API-SECRET-KEY-HERE###";
	private WTReferenceRequestFactory()
	{
		// cannot be called from outside
	}
	
	public WTReferenceRequestFactory(Context context)
	{
		this.context = context; 
	}
	
	public HttpPost requestForReportingNoise(WTNoise noise, Date date) 
	{
		TelephonyManager tm = (TelephonyManager)(context.getSystemService(Context.TELEPHONY_SERVICE));
		String ID = tm.getDeviceId();
		String model = android.os.Build.MODEL;
		 
		JSONObject jobj = new JSONObject();
		
		try 
		{
			jobj.put("timestamp", new Formatter(Locale.US).format("%d", (date.getTime()/1000)).toString());
			jobj.put("duration", new Formatter(Locale.US).format("%.0f", noise.getMeasurementDuration()).toString());
			
			ArrayList<Double> geoCoord = new ArrayList<Double>();
			geoCoord.add(noise.getLocation().getLongitude());
			geoCoord.add(noise.getLocation().getLatitude());
			
			jobj.put("geo_coord", new JSONArray(geoCoord) );
			
			jobj.put("average_raw", new Formatter(Locale.US).format("%f", noise.getAverageLevel()).toString());
			jobj.put("average_db", new Formatter(Locale.US).format("%f", noise.getAverageLevelInDB()).toString());
			jobj.put("uid", ID);
			
			JSONObject perceptions = new JSONObject();
			for(String k: noise.getPerceptions().keySet())
			{
				perceptions.put(k, noise.getPerceptions().get(k).toString());
			}
			jobj.put("perceptions", perceptions);
			
			int numOfSamples = noise.getSamples().size(); // debug
			double duration = noise.getMeasurementDuration(); // debug
			int interval = (int) (numOfSamples / (2 * duration));
			ArrayList<String> samples = new ArrayList<String>();
			int i = 0;
			while ( i < noise.getSamples().size())
			{
				samples.add((noise.getSamples().get(i)).toString());
				i+= interval;
			}
			jobj.put("samples", new JSONArray(samples) );
			jobj.put("device", model);
			jobj.put("hash", "");
			
			Mac mac = Mac.getInstance("HMACSHA256");

			SecretKeySpec sk = new SecretKeySpec(KEY.getBytes(), "HMACSHA256");      
			mac.init(sk);
			String jsonString = jobj.toString();

			String resultStr = new String(Base64.encode(mac.doFinal(jsonString.getBytes("UTF-8")), Base64.DEFAULT));
			 
			
			jobj.put("hash", resultStr);
			
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		} 
		catch (InvalidKeyException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalStateException e) 
		{
			e.printStackTrace();
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		
		HttpPost httpost = new HttpPost(REPORTING_URL);
		
		StringEntity se;
		try 
		{
			String jsonString = jobj.toString();
			jsonString = jsonString.replace("\\/", "/");
			jsonString = jsonString.replace("\\n", "");
			se = new StringEntity(jsonString);
			httpost.setEntity(se);
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}

		return httpost;
		
	}

	public HttpGet requestForFetchingNoiseReportsInMapRect(float latitude, float longitude,float latitudeDelta, float longitudeDelta) 
	{
    	HttpGet request = new HttpGet(new Formatter(Locale.US).format("%s?lat=%f&lon=%f&lat_delta=%f&lon_delta=%f", MAP_URL, latitude, longitude, latitudeDelta, longitudeDelta).toString());
    	return request;  	
	}

	public HttpPost requestForAssigningTags(ArrayList<String> tags, WTNoise noise, Date date) 
	{
		TelephonyManager tm = (TelephonyManager)(context.getSystemService(Context.TELEPHONY_SERVICE));
		String ID = tm.getDeviceId();
		JSONObject jobj = new JSONObject();
		try 
		{
			jobj.put("uid", ID);
			jobj.put("tags", new JSONArray(tags) );
			jobj.put("timestamp", new Formatter(Locale.US).format("%d", (date.getTime()/1000)).toString());
			jobj.put("hash", "");
			
			Mac mac = Mac.getInstance("HMACSHA256");
			SecretKeySpec sk = new SecretKeySpec(KEY.getBytes(), "HMACSHA256");      
			mac.init(sk);
			String jsonString = jobj.toString();
			String resultStr = new String(Base64.encode(mac.doFinal(jsonString.getBytes("UTF-8")), Base64.DEFAULT));
			
			jobj.put("hash", resultStr);
			
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		} 
		catch (InvalidKeyException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalStateException e) 
		{
			e.printStackTrace();
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}

		String url = String.format(TAGS_URL, noise.getID());
		Log.w("TAG URL", url);
		HttpPost httpost = new HttpPost(url);
		
		StringEntity se;
		try 
		{
			String jsonString = jobj.toString();
			jsonString = jsonString.replace("\\/", "/");
			jsonString = jsonString.replace("\\n", "");
			Log.w("TAG JSON", jsonString);
			se = new StringEntity(jsonString);
			httpost.setEntity(se);
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}

		return httpost;
	}

}
