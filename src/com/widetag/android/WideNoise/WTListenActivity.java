package com.widetag.android.WideNoise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ViewFlipper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

public class WTListenActivity extends Activity implements WTLedViewDataSource, WTNoiseRecordDelegate, WTTagsViewControllerDelegate
{
	static int innerFlipRToLDuration = 450;
	static int innerFlipLToRDuration = 450;
	
	static int mainFlipRToLDuration = 550;
	static int mainFlipLToRDuration = 550;
	
	
	static int record_duration = 5;
	
	static float locationAccuracy = 1000.0f;
	
	private int page = 0;
	
	static int pager_images[] = {R.drawable.pager_1, R.drawable.pager_2, R.drawable.pager_3, R.drawable.pager_4, R.drawable.pager_5};
	
	private WTNoiseRecorder noiseRecorder;	
	
	private WTLedView ledView;
	private ImageView stopView;
	private RelativeLayout screenGraphContainer;
	
	
	// Interface buttons
	private Button takeNoiseButton;
	private Button extendButton;
	private Button restartButton;
	private Button qualifyNoiseButton;
	private Button sendReportButton;

	private Button addTag;
	private Button shareResult;
	private Button takeNewSample;
	
	// Icons changing during execution
	private ImageView noiseMeterImage;
	private ImageView guessTextImage;
	private ImageView matchTextImage;
	private ImageView stopImage;
	private ImageView recImage;
	private ImageView locImage;
	private ImageView statusScreen;
	
	// Text labels
	private TextView leftdbLabel;
	private TextView rightdbLabel;
	private TextView leftDescription;
	private TextView rightDescription;
	
	
	// qualitication noise seekbars
	private SeekBar loveSeekBar;
	private SeekBar calmSeekBar;
	private SeekBar aloneSeekBar;
	private SeekBar natureSeekBar;
	private SeekBar guessNoiseSeekBar;
	
	LocationManager locationManager;
	Location currentLocation;
	
	private class ImgAndDescription
	{
		public int img;
		public String description;
		
		public ImgAndDescription()
		{
			description = new String("");
		}
	}
	
	
	private static int natural_pos = 0, artificial_pos = 1, lovable_pos = 2, hurting_pos = 3, indoor_pos = 4, outdoor_pos = 5, single_pos = 6, multiple_pos = 7; 
	
	
	
	private boolean noiseTypeButtonFlags[];  // "true" if the button is selected
	
	public View.OnClickListener noiseTypeButtonListener = new View.OnClickListener() 
	{ 
		public void onClick(View v) 
		{
			
		}
	}; // it's a variable definition!
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listen);
		
		// create ledView
		screenGraphContainer = (RelativeLayout) findViewById(R.id.screen_graph);
		ledView = new WTLedView(this);
		
		Resources r = getResources(); 
		float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 268, r.getDisplayMetrics());
		float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, r.getDisplayMetrics());
		
		// RECUPERA LA LOCAZIONE CORRENTE E IMPOSTA LA CALLBACK
		//
		//	TO DO
		//
		
		currentLocation = new Location("");
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		
		
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int) width, (int) height);
		ledView.setLayoutParams(params);
		ledView.setEnabled(true);
		ledView.setDataSource(this);
		screenGraphContainer.addView(ledView);
		
		// retrieve Button objects from xml
		takeNoiseButton = (Button) findViewById(R.id.take_noise_sample_button);
		extendButton = (Button) findViewById(R.id.extend_sampling_button);
		restartButton = (Button) findViewById(R.id.restart_button);
		sendReportButton = (Button) findViewById(R.id.send_report_button);
		qualifyNoiseButton = (Button) findViewById(R.id.qualify_noise_button);
		
		addTag = (Button) findViewById(R.id.add_tag);
		shareResult = (Button) findViewById(R.id.share_result);
		takeNewSample = (Button) findViewById(R.id.take_new_sample);
		
		// retrieve ImageView objects from xml
		noiseMeterImage = (ImageView) findViewById(R.id.noise_meter);
		guessTextImage = (ImageView) findViewById(R.id.guess_text);
		matchTextImage = (ImageView) findViewById(R.id.match);
		stopImage = (ImageView) findViewById(R.id.stop);
		recImage = (ImageView) findViewById(R.id.rec);
		locImage = (ImageView) findViewById(R.id.loc);
		statusScreen = (ImageView) findViewById(R.id.status_screen);
		
		// rettrieve textView
		leftdbLabel = (TextView) findViewById(R.id.leftdB);
		rightdbLabel = (TextView) findViewById(R.id.rightdB);
		
		leftDescription = (TextView) findViewById(R.id.leftDescription);
		rightDescription = (TextView) findViewById(R.id.rightDescription);		
		noiseRecorder = new WTNoiseRecorder(this);
		
		guessNoiseSeekBar = (SeekBar) findViewById(R.id.guess_noise_level_seekbar);
		guessNoiseSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) 
			{
				Integer db = new Integer(seekBar.getProgress());
				
				ImgAndDescription iad = getNoiseDescriptionAndImage(db.floatValue());
				leftdbLabel.setVisibility(View.VISIBLE);
				leftDescription.setVisibility(View.VISIBLE);
				leftdbLabel.setText(db.toString() + "db");
				leftDescription.setText(iad.description);
				guessTextImage.setVisibility(View.GONE);
				
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		
		});
		
		loveSeekBar = (SeekBar) findViewById(R.id.love_seekbar);
		calmSeekBar = (SeekBar) findViewById(R.id.calm_seekbar);
		aloneSeekBar = (SeekBar) findViewById(R.id.alone_seekbar);
		natureSeekBar = (SeekBar) findViewById(R.id.nature_seekbar);
		
		setPage(0);
		
	}
	
	
	protected void setStatus(int page)
	{
		
		switch (page)
		{
			case 0:
			{
				takeNoiseButton.setEnabled(true);
				noiseMeterImage.setImageResource(R.drawable.noise_meter_off);
				stopImage.setVisibility(View.VISIBLE);
				recImage.setVisibility(View.GONE);
				locImage.setVisibility(View.GONE);
				guessTextImage.setVisibility(View.GONE);
				matchTextImage.setVisibility(View.GONE);
				
				leftdbLabel.setText("");
				rightdbLabel.setText("");
				leftDescription.setText("");
				rightDescription.setText("");
				guessNoiseSeekBar.setProgress(0);
				leftdbLabel.setVisibility(View.GONE);
				leftDescription.setVisibility(View.GONE);
			}
			break;
			case 1:
			{
				extendButton.setEnabled(false);
			}
			break;
			case 2:
			{
				restartButton.setEnabled(true);
				qualifyNoiseButton.setEnabled(true);
				stopImage.setVisibility(View.VISIBLE);
				recImage.setVisibility(View.GONE);
				locImage.setVisibility(View.VISIBLE);
				guessTextImage.setVisibility(View.GONE);
			}
			break;
			case 3:
			{
				loveSeekBar.setProgress(loveSeekBar.getMax() / 2);
				calmSeekBar.setProgress(loveSeekBar.getMax() / 2);
				aloneSeekBar.setProgress(loveSeekBar.getMax() / 2);
				natureSeekBar.setProgress(loveSeekBar.getMax() / 2);
				sendReportButton.setEnabled(true);
			}
			break;
			case 4:
			{
				addTag.setEnabled(false);
				shareResult.setEnabled(false);
				takeNewSample.setEnabled(false);
			}
			default:
				break;
		}
		this.page = page;
		
	}

	
	protected void setPage(int page)
	{
		ImageView pager = (ImageView) findViewById(R.id.pager);
		int newPage = 0;
		if ((page < pager_images.length - 1) && (page >= 0))
		{	
			pager.setImageResource(pager_images[page]);
			newPage = page;
		}
		else
		if (page > pager_images.length - 1)
		{
			pager.setImageResource(pager_images[0]);
			newPage = 0;
		}
		else
		{	
			pager.setImageResource(pager_images[pager_images.length - 1]);
			newPage = pager_images.length - 1;
		}
		
		setStatus(newPage);
	}
	
	protected void flipToNextView(ViewFlipper vf)
	{
		Animation in = inFromRightAnimation(innerFlipRToLDuration);
		if (page == 0)
		{
			in.setAnimationListener(from0to1);
		}
		else if (page == 3)
		{
			in.setAnimationListener(from3to4);
		}
		vf.setInAnimation(in);
        vf.setOutAnimation(outToLeftAnimation(innerFlipRToLDuration));
		vf.showNext();
	}
	
	protected void flipToPreviousView(ViewFlipper vf)
	{
		vf.setInAnimation(inFromLeftAnimation(innerFlipLToRDuration));
        vf.setOutAnimation(outToRightAnimation(innerFlipLToRDuration));
		vf.showPrevious();
	}
	
	private void showLocationAlert() 
	{
		new AlertDialog.Builder(WTListenActivity.this)
			.setCancelable(false)
			.setMessage("You need to turn on location services")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() 
			{  
			    public void onClick(android.content.DialogInterface dialog, int arg1) 
			    {
			        dialog.dismiss();                      
			    }
			}).create().show();  
	}

	////////////////////////////////////////////////////////////////////
	
	//	Buttons callbacks 
	
	////////////////////////////////////////////////////////////////////
	
	public void take_noise_sample_callback( View view)
	{
		
		takeNoiseButton.setEnabled(false);
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.inner_listen_view_flipper);
		flipToNextView(vf);
		
		setPage(1);

		Criteria lmCriteria = new Criteria();
		lmCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		lmCriteria.setAltitudeRequired(false);
		lmCriteria.setBearingRequired(false);
		lmCriteria.setCostAllowed(false);
		lmCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		lmCriteria.setSpeedRequired(false);

		String bestProvider = locationManager.getBestProvider(lmCriteria, true);
		if (bestProvider == null) {
			lmCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
			bestProvider = (bestProvider != null) ? bestProvider
					: locationManager.getBestProvider(lmCriteria, true);
		}
		if (bestProvider != null) {
			Location lastLocation = locationManager
					.getLastKnownLocation(bestProvider);
			if (lastLocation != null) {
				currentLocation.setLatitude(lastLocation.getLatitude());
				currentLocation.setLongitude(lastLocation.getLongitude());
			}

			// FARE RICHIESTA SINCRONA
			locationManager.requestLocationUpdates(bestProvider, 1000L,
					locationAccuracy, new LocationListener() {

						public void onLocationChanged(Location location) {
							if (location != null) {
								// SE LA PRECISIONE è TRA 0 E 100 AGGIORNA
								WTListenActivity.this.currentLocation
										.setLatitude(location.getLatitude());
								WTListenActivity.this.currentLocation
										.setLongitude(location.getLongitude());
								WTListenActivity.this.locationManager
										.removeUpdates(this);
							}
						}

						public void onProviderDisabled(String provider) {
							WTListenActivity.this.currentLocation
									.setLatitude(0.0);
							WTListenActivity.this.currentLocation
									.setLongitude(0.0);
							WTListenActivity.this.locationManager
									.removeUpdates(this);
							showLocationAlert();
						}

						public void onProviderEnabled(String provider) {
							// TODO Auto-generated method stub

						}

						public void onStatusChanged(String provider,
								int status, Bundle extras) {
							// TODO Auto-generated method stub

						}

					});
		} else {
			showLocationAlert();
		}
	}
	
	public void extend_sampling_callback( View view)
	{
		extendButton.setEnabled(false);
	}
	
	public void restart_callback(View view)
	{
		restartButton.setEnabled(false);
		noiseRecorder.clear();
		
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.inner_listen_view_flipper);
		flipToNextView(vf);
		
		setPage(0);
	}
		
	public void qualify_noise_callback(View view)
	{
		qualifyNoiseButton.setEnabled(false);
		
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.main_listen_view_flipper);
		flipToNextView(vf);
		
		setPage(3);
	}
	
	
	public static String convertStreamToString(InputStream is) 
	{

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try 
		{

			while ((line = reader.readLine()) != null) 
			{
				sb.append(line + "\n");
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			try 
			{
				is.close();
			} 
			catch (IOException e) 
			{
	
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	protected void handleConnectionError()
	{
		statusScreen.setBackgroundResource(R.drawable.status_screen);
		new AlertDialog.Builder(WTListenActivity.this)
			.setCancelable(false)
			.setMessage("Error sending report.")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() 
			{  
			    public void onClick(android.content.DialogInterface dialog, int arg1) 
			    {
			        dialog.dismiss();                      
			    }
			}).create().show();  
	}
	
	public void send_report_callback(View view)
	{
		sendReportButton.setEnabled(false);
		
		statusScreen.setBackgroundResource(R.drawable.status_screen_sending);
		ViewFlipper mainVf = (ViewFlipper) findViewById(R.id.main_listen_view_flipper);
		flipToNextView(mainVf);
		
		setPage(4);
		
		final WTNoise recordedNoise = noiseRecorder.getRecordedNoise();
		
		recordedNoise.setLocation(currentLocation);
		
		recordedNoise.setFeelinglevel(loveSeekBar.getProgress());
		recordedNoise.setDisturbanceLevel(calmSeekBar.getProgress());
		recordedNoise.setIsolationLevel(aloneSeekBar.getProgress());
		recordedNoise.setArtificiality(natureSeekBar.getProgress());
		
		
		
		class SendingReportAsyncTask extends AsyncTask<WTListenActivity, Void, Void>
		{

			@Override
			protected Void doInBackground(WTListenActivity... self) 
			{
				WTListenActivity activity = self[0];
				try 
				{
					WTReferenceRequestFactory requestFactory = new WTReferenceRequestFactory(activity);
					Calendar rightNow = Calendar.getInstance();
					Date currentDate = rightNow.getTime();
					HttpPost httpPost = requestFactory.requestForReportingNoise(noiseRecorder.getRecordedNoise(), currentDate);
	
					DefaultHttpClient httpclient = new DefaultHttpClient();
					HttpResponse response = httpclient.execute(httpPost); 
			
				
					InputStream instream = response.getEntity().getContent();
					String result= convertStreamToString(instream);
					instream.close();
					try 
					{
						JSONObject jsonResponse = new JSONObject(result);
						if (jsonResponse.has("id")) 
						{
							recordedNoise.setID(jsonResponse.getString("id"));
							sendOk(activity);
						} 
						else 
						{
							sendError(activity);
						}
					} 
					catch (JSONException e) 
					{
						sendError(activity);
						e.printStackTrace();
					}
				} 
				catch (ClientProtocolException e) 
				{
					sendError(activity);
				} 
				catch (IOException e) 
				{
					sendError(activity);
				}
				return null;
			}

			private void sendOk(Activity activity) 
			{
				activity.runOnUiThread(new Runnable() 
				{
					public void run() {
						statusScreen.setBackgroundResource(R.drawable.status_screen_done);
				}});
			}

			private void sendError(Activity activity) 
			{
				activity.runOnUiThread(new Runnable() 
				{
					public void run() {
						statusScreen.setBackgroundResource(R.drawable.status_screen);
						handleConnectionError();
				}});
			}
		}
		
		SendingReportAsyncTask srat = new SendingReportAsyncTask();
		srat.execute(this);
		takeNewSample.setEnabled(true);
		noiseRecorder.clear();

	}
	
	public void change_prediction_callback(View view)
	{
		Integer db = new Integer(((SeekBar)view).getProgress());
		String description = new String();
		ImgAndDescription iad = getNoiseDescriptionAndImage(db.floatValue());
		
		leftdbLabel.setText(db.toString() + "db");
		leftDescription.setText(iad.description);
	}
	
	
	public void add_tag_callback(View view)
	{
		Button but = (Button)view;
		but.setEnabled(false);
	}
	
	public void share_result_callback(View view)
	{
		Button but = (Button)view;
		but.setEnabled(false);
	}
	
	public void take_new_sample_callback(View view)
	{
		Button but = (Button)view;
		but.setEnabled(false);
		
		ViewFlipper mainVf = (ViewFlipper) findViewById(R.id.main_listen_view_flipper);
		flipToNextView(mainVf);
		
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.inner_listen_view_flipper);
		flipToNextView(vf);
		setPage(0);

	}
	////////////////////////////////////////////////////////////////////
	
	//   Animation functions
	
	////////////////////////////////////////////////////////////////////
	
	// for the next movement
	public static Animation inFromRightAnimation(int duration) 
	{

    	Animation inFromRight = new TranslateAnimation(
    	Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
    	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
    	);
    	inFromRight.setDuration(duration);
    	inFromRight.setInterpolator(new AccelerateInterpolator());
    	return inFromRight;
    }
	
    public static Animation outToLeftAnimation(int duration) 
    {
    	Animation outtoLeft = new TranslateAnimation(
    	 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
    	 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
    	);
    	outtoLeft.setDuration(duration);
    	outtoLeft.setInterpolator(new AccelerateInterpolator());
    	return outtoLeft;
    }
    
    // for the previous movement
    public static Animation inFromLeftAnimation(int duration) 
    {
    	Animation inFromLeft = new TranslateAnimation(
    	Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
    	Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
    	);
    	inFromLeft.setDuration(duration);
    	inFromLeft.setInterpolator(new AccelerateInterpolator());
    	return inFromLeft;
    }
    
    public static Animation outToRightAnimation(int duration) 
    {
    	Animation outtoRight = new TranslateAnimation(
    	 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
    	 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
    	);
    	outtoRight.setDuration(duration);
    	outtoRight.setInterpolator(new AccelerateInterpolator());
    	return outtoRight;
    }
    
    
    // listener for first animation to postpone noise recording: otherwise animation is spoiled 
    Animation.AnimationListener from0to1 = new Animation.AnimationListener()
    {

		public void onAnimationEnd(Animation animation) 
		{
			extendButton.setEnabled(false);
			stopImage.setVisibility(View.GONE);
			recImage.setVisibility(View.VISIBLE);
			locImage.setVisibility(View.VISIBLE);
			guessTextImage.setVisibility(View.VISIBLE);
			matchTextImage.setVisibility(View.GONE);
			noiseRecorder.recordForDuration(record_duration);
		}

		public void onAnimationRepeat(Animation animation) 
		{
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation animation) 
		{
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    
    Animation.AnimationListener from3to4 = new Animation.AnimationListener()
    {

		public void onAnimationEnd(Animation animation) 
		{
//			WTListenActivity.this.runOnUiThread(new Runnable() 
//			{
//				public void run() 
//				{

//					
//				}
//			});
//					
	
		}
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
    }; 
    
	////////////////////////////////////////////////////////////////////
	
	//   WTLedViewDataSource
	
	////////////////////////////////////////////////////////////////////    

	public float ledViewValue(WTLedView ledView, int columnIndex) 
	{
		WTNoise noise = this.noiseRecorder.getRecordedNoise();
		int samplesPerSecond = this.noiseRecorder.resamplingNumOfSamples;
		double currentRecDur = noise.getMeasurementDuration();
		int totalSamples = (int)(samplesPerSecond  * currentRecDur);
		
		if (totalSamples == 0)
		{
			return 0;
		}
		float numOfCols = ((float) (ledView.getNumberOfCols()));
		float len = totalSamples / numOfCols ;
		
		float level = 0.0f;
		
		int j = (int)(len * columnIndex);
		if (j < noise.getSamples().size())
		{
			level = ((Float)noise.getSampleAtIndex(j)).floatValue();
		}
		else
		{
			level = 0;
		}
		
		float value = 0;
		
		if (level <= 0)
		{
			value = 0;
		}
		else
		if (level <= 30)
		{
			value = 0.07f;
		}
		else
		if (level <= 100)
		{
			value = 0.07f + ((level - 30.0f) / 70.0f)*0.97f;
		}
		else
		{
			value = 1.0f;
		}
		
		return value;
	}

	////////////////////////////////////////////////////////////////////
	
	//   WTNoiseRecordDelegate
	
	////////////////////////////////////////////////////////////////////    
	
	protected ImgAndDescription getNoiseDescriptionAndImage(float db)
	{
		ImgAndDescription result = new ImgAndDescription();
		if (db <= 30)
		{
			result.img = R.drawable.noise_meter_1;
			result.description = "Feather";
		}
		else
		if (db <= 60)
		{
			result.img = R.drawable.noise_meter_2;
			result.description = "Sleeping Cat";
		}
		else
		if (db <= 70)
		{
			result.img = R.drawable.noise_meter_3;
			result.description = "TV";
		}
		else
		if (db <= 90)
		{
			result.img = R.drawable.noise_meter_4;
			result.description = "Car";
		}
		else
		if (db <= 100)
		{
			result.img = R.drawable.noise_meter_5;
			result.description = "Dragster";
		}
		else
		if (db < 115)
		{
			result.img = R.drawable.noise_meter_6;
			result.description = "T-rex";
		}
		else
		{
			result.img = R.drawable.noise_meter_7;
			result.description = "Rock Concert";
		}
		
		return result;
	}
	
	////////////////////////////////////////////////////////////////////
	
	//   WTNoiseRecordDelegate
	
	////////////////////////////////////////////////////////////////////    
		
	public void hasFinishedRecording(WTNoiseRecorder recorder) 
	{
		WTNoise recordedNoise = noiseRecorder.getRecordedNoise();
		//recordedNoise.setMeasurementDuration(noiseRecorder.getTotalDuration());
		
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.inner_listen_view_flipper);
		flipToNextView(vf);
		setPage(2);
		
		
		
		final float db = recordedNoise.getAverageLevelInDB();
		String description = new String();
		 
		Integer num = new Integer(0);
		ImgAndDescription iad = getNoiseDescriptionAndImage(db);
		final Integer imageNum = new Integer(iad.img) ;
		final String passingDescrption = iad.description;
		
				
		this.runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				WTListenActivity.this.noiseMeterImage.setImageResource(imageNum.intValue());
				rightdbLabel.setText(Integer.toString((int)db) + "db");
				rightDescription.setText(passingDescrption);
				if ((int)db == guessNoiseSeekBar.getProgress())
				{
					matchTextImage.setImageResource(R.drawable.perfect);
				}
				else
				if (Math.abs((int)db - guessNoiseSeekBar.getProgress()) <= 5 )
				{
					matchTextImage.setImageResource(R.drawable.good);
				}
				else
				{
					matchTextImage.setImageResource(R.drawable.no_match);
				}
				matchTextImage.setVisibility(View.VISIBLE);
			}
		});
		
	}

	public void noiseUpdated(int numOfRecordedSamples, final WTNoise noise) 
	{
		if (noise.getSamples().size() > (noiseRecorder.getCurrentRecordingDuration() * noiseRecorder.resamplingNumOfSamples / 2))
		{
			this.runOnUiThread(new Runnable() 
			{
				public void run() 
				{
					WTListenActivity.this.extendButton.setEnabled(true);
					WTListenActivity.this.extendButton.setClickable(true);
					WTListenActivity.this.ledView.invalidate();
				}
			});
		}
	}
	

	
	
}
