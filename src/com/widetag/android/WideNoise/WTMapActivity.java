package com.widetag.android.WideNoise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class WTMapActivity extends MapActivity {
	private static final String MAP_KEY = "";
	
	
	static float locationAccuracy = 1000.0f;
	WTMapView mapView;
	ImageView overlayImageView;
	TextView overlayLabel;

	RelativeLayout mapContainer;
	RelativeLayout topBorder;
	RelativeLayout bottomBorder;

	String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public void onMapMoved() 
	{
		
		class SendingRequestAsyncTask extends AsyncTask<WTMapActivity, Void, Void>
		{
			@Override
			protected Void doInBackground(WTMapActivity... self) 
			{
				WTMapActivity activity = self[0];
				
				GeoPoint center = activity.mapView.getMapCenter();
				float latitude = center.getLatitudeE6() / 1.0E6f;
				float longitude = center.getLongitudeE6() / 1.0E6f;
				float latitudeDelta = activity.mapView.getLatitudeSpan() / 1.0E6f;
				float longitudeDelta = activity.mapView.getLongitudeSpan() / 1.0E6f;
				
				WTRequestFactory reqFactory = new WTReferenceRequestFactory(activity);
				HttpGet request = reqFactory.requestForFetchingNoiseReportsInMapRect(latitude, longitude, latitudeDelta, longitudeDelta);
				DefaultHttpClient httpclient = new DefaultHttpClient();
				try 
				{
					HttpResponse response = httpclient.execute(request);
					double averageLevel = 0.0;
					ArrayList<WTNoise> noises = new ArrayList<WTNoise>();
					InputStream instream = response.getEntity().getContent();
					String result = convertStreamToString(instream);
					instream.close();
					try 
					{
						JSONObject jsonResponse = new JSONObject(result);
						averageLevel = jsonResponse.getDouble("average_db");
						JSONObject data = jsonResponse.getJSONObject("data");
						Iterator<String> ikey = data.keys();
						while (ikey.hasNext()) 
						{
							String noiseID = ikey.next();
							JSONObject noise = data.getJSONObject(noiseID);
							WTNoise n = new WTNoise();
							n.setID(noiseID);
							n.setAverageLevelInDB((float)noise.getDouble("average_db"));
							n.setMeasurementDate(new Date((long) (noise.getDouble("timestamp"))*1000));
							n.setMeasurementDuration(noise.getDouble("duration"));
							JSONArray coordinates = noise.getJSONArray("geo_coord");
							Location l = new Location("");
							l.setLongitude(coordinates.getDouble(0));
							l.setLatitude(coordinates.getDouble(1));
							n.setLocation(l);
							noises.add(n);
						}
					} 
					catch (IllegalStateException e1) 
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
					catch (JSONException e1) 
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		
					UpdateOverlay(activity, averageLevel, noises);
		
				} 
				catch (ClientProtocolException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				return null;
				
			}
			
			private void UpdateOverlay(Activity activity,double averageLevel, ArrayList<WTNoise> noises) 
			{
				final double level = averageLevel;
				final ArrayList<WTNoise> list = noises;
				activity.runOnUiThread(new Runnable() 
				{
					public void run() 
					{
						setLevelAndPins(level, list);
					}
				});
			}
		};
		SendingRequestAsyncTask srat = new SendingRequestAsyncTask();
		srat.execute(WTMapActivity.this);
	}

	private LocationManager locationManager;

	private void showLocationAlert() {
		runOnUiThread(new Runnable() {
			public void run() {
				new AlertDialog.Builder(WTMapActivity.this)
						.setCancelable(false)
						.setMessage("You need to turn on location services")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(
											android.content.DialogInterface dialog,
											int arg1) {
										dialog.dismiss();
									}
								}).create().show();
			}
		});
	}
	
	CustomItemizedOverlay overlay;
	
	private void setLevelAndPins(double averageLevel, ArrayList<WTNoise> noises) {
		overlay.setPins(noises);

		String description;
		float hue = 0.f;
		if (averageLevel <= 30) {
			overlayImageView.setImageResource(R.drawable.icon_1);
			description = "Feather";
			hue = 30;
		} else if (averageLevel <= 60) {
			overlayImageView.setImageResource(R.drawable.icon_2);
			description = "Sleeping Cat";
			hue = 60;
		} else if (averageLevel <= 70) {
			overlayImageView.setImageResource(R.drawable.icon_3);
			description = "TV";
			hue = 70;
		} else if (averageLevel <= 90) {
			overlayImageView.setImageResource(R.drawable.icon_4);
			description = "Car";
			hue = 90;
		} else if (averageLevel <= 100) {
			overlayImageView.setImageResource(R.drawable.icon_5);
			description = "Dragster";
			hue = 90;
		} else if (averageLevel <= 115) {
			overlayImageView.setImageResource(R.drawable.icon_6);
			description = "T-rex";
			hue = 115;
		} else {
			overlayImageView.setImageResource(R.drawable.icon_7);
			description = "Rock Concert";
			hue = 120;
		}

		int borderColor = Color.HSVToColor(new float[] {
				120.0f - hue, 1.f, 1.f });
		topBorder.setBackgroundColor(borderColor);
		bottomBorder.setBackgroundColor(borderColor);
		overlayLabel.setText(description);
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mapView = new WTMapView(this, MAP_KEY);
		
		mapView.setSatellite(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setClickable(true);
		
		mapContainer = (RelativeLayout) findViewById(R.id.map_container);
		mapContainer.addView(mapView);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria lmCriteria = new Criteria();
		lmCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		lmCriteria.setAltitudeRequired(false);
		lmCriteria.setBearingRequired(false);
		lmCriteria.setCostAllowed(false);
		lmCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		lmCriteria.setSpeedRequired(false);

		String bestProvider = locationManager.getBestProvider(lmCriteria, true);
		if (bestProvider != null) {
			lmCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
			bestProvider = (bestProvider != null) ? bestProvider : locationManager.getBestProvider(lmCriteria, true);
		}
		if (bestProvider != null) {
			Location lastLocation = locationManager.getLastKnownLocation(bestProvider);
			if (lastLocation != null) {
				WTMapActivity.this.mapView.getController().animateTo(new GeoPoint((int)(lastLocation.getLatitude()* 1E6), (int) (lastLocation.getLongitude() * 1E6)));
				WTMapActivity.this.mapView.getController().setZoom(19);
				WTMapActivity.this.onMapMoved();
			}
			locationManager.requestLocationUpdates(bestProvider, 1000L, locationAccuracy, new LocationListener() {
				public void onLocationChanged(Location location) {
					WTMapActivity.this.locationManager.removeUpdates(this);
					WTMapActivity.this.mapView.getController().animateTo(new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
					WTMapActivity.this.mapView.getController().setZoom(19);
					WTMapActivity.this.onMapMoved();
				}

				public void onProviderDisabled(String provider) {}

				public void onProviderEnabled(String provider) {}

				public void onStatusChanged(String provider, int status, Bundle extras) {}

			});
		} else {
			showLocationAlert();
		}

		overlayImageView = (ImageView) findViewById(R.id.map_info_noise_icon);
		overlayLabel = (TextView) findViewById(R.id.map_info_noise_description);
		topBorder = (RelativeLayout) findViewById(R.id.top_map_info_border);
		bottomBorder = (RelativeLayout) findViewById(R.id.bottom_map_info_border);
		
		overlay = new CustomItemizedOverlay(getResources().getDrawable(R.drawable.da_marker_red), this, mapView);
		mapView.getOverlays().add(overlay);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	static Drawable boundToMyCenter(Drawable defaultMarker) {
		int h = defaultMarker.getIntrinsicHeight();
    	int w = defaultMarker.getIntrinsicWidth();
    	int t = -5*h/8;
    	int l = -w/2;
    		
    	defaultMarker.setBounds(l, t, l+w, t+h);
    	return defaultMarker;
	}
	
	public class CustomOverlayItem extends OverlayItem {
		private int imageResource;
		CustomOverlayItem(GeoPoint g, String text1, String text2, int imgres) {
			super(g, text1, text2);
			imageResource = imgres;
		}
		
		public int getImageResource() {
			return imageResource;
		}
	}

	public class CustomItemizedOverlay extends ItemizedOverlay<CustomOverlayItem> {
		View popup;
		MapView map;
		int yoffset;
		
	    private ArrayList<CustomOverlayItem> mOverlays = new ArrayList<CustomOverlayItem>();
	    public CustomItemizedOverlay(Drawable defaultMarker, Context mContext, MapView view) {
	        super(boundToMyCenter(defaultMarker));
	        popup = getLayoutInflater().inflate(R.layout.balloon_overlay, map, false);
	        yoffset = defaultMarker.getBounds().top - defaultMarker.getIntrinsicHeight()/8;
	        map = view;
	        populate();
	    }

		@Override
	    protected CustomOverlayItem createItem(int i) {
	        return mOverlays.get(i);
	    }
	
	    @Override
	    public int size() {
	        return mOverlays.size();
	    }
	    
	    @Override
	    public boolean onTap(com.google.android.maps.GeoPoint arg0, com.google.android.maps.MapView arg1)
	    {
	    	map.removeView(popup);
	    	return super.onTap(arg0, arg1);
	    }

	    @Override
	    protected boolean onTap(int i) {
	    	map.removeView(popup);
	    	CustomOverlayItem item = createItem(i);
	    	MapView.LayoutParams mapParams = new MapView.LayoutParams(
    			ViewGroup.LayoutParams.WRAP_CONTENT, 
    			ViewGroup.LayoutParams.WRAP_CONTENT, 
    			item.getPoint(),                         
    			0,                         
    			yoffset,                         
    			MapView.LayoutParams.BOTTOM_CENTER
			);
	    	((ImageView)popup.findViewById(R.id.balloon_icon)).setImageResource(item.getImageResource());
	    	((TextView)popup.findViewById(R.id.balloon_item_title)).setText(item.getTitle());
	    	((TextView)popup.findViewById(R.id.balloon_item_snippet)).setText(item.getSnippet());
	    	map.addView(popup, mapParams); 	    
	    	return true;
	    }

	    public void setPins(ArrayList<WTNoise> noises) {
	    	ArrayList<CustomOverlayItem> newOverlays = new ArrayList<CustomOverlayItem>();
	    	for (WTNoise noise: noises)
	    	{
	    		String title = String.format("%.0fdb - %.0f\"", noise.getAverageLevelInDB(), noise.getMeasurementDuration());
	    		String snippet = noise.getMeasurementDate().toLocaleString();
	    		newOverlays.add(new CustomOverlayItem(new GeoPoint((int)(noise.getLocation().getLatitude()*1.0E6), (int)(noise.getLocation().getLongitude()*1.0E6)), title, snippet, noise.getIcon()));
	    	}
	        setLastFocusedIndex(-1);
	        mOverlays = newOverlays;
	        populate();
	    }
	}
}
