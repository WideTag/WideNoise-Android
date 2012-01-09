package com.widetag.android.WideNoise;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;


public class WTSocialNetworkManager 
{
	private static final String CONSUMER_SECRET = "";
	private static final String CONSUMER_KEY = "";
	final static String FACEBOOK_APP_ID = "";
	final static String FACEBOOK_APP_HASH = "";
	private static final String FACEBOOK_TOKEN_FILENAME = "facebook_token";
	private static final String TWITTER_TOKEN_FILENAME = "twitter_token";


	private AsyncTwitter asyncTwitter;
	private Facebook facebook;
	AccessToken twitterToken;
	
	private final Context appContext;
	
	
	static private WTSocialNetworkManager instance ;
	private WTSocialNetworkManager(Context context)
	{
		appContext = context.getApplicationContext();
		
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();       
	    configurationBuilder.setOAuthConsumerKey(CONSUMER_KEY);      
	    configurationBuilder.setOAuthConsumerSecret(CONSUMER_SECRET);      
	    Configuration configuration = configurationBuilder.build();
	    asyncTwitter = new AsyncTwitterFactory(configuration).getInstance();   
	    
	    facebook = new Facebook(FACEBOOK_APP_ID);
	    
	    readFacebookToken();
	    readTwitterToken();

	}
	
	synchronized public void loginToTwitter(String username, String password )
	{
	    try 
	    {
	    	twitterToken = asyncTwitter.getOAuthAccessToken(username, password);
	    	writeTwitterToken();	   
	    	asyncTwitter.verifyCredentials();
		} 
	    catch (TwitterException e) 
	    {
			e.printStackTrace();
		}
	}
	
	synchronized static public WTSocialNetworkManager getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new WTSocialNetworkManager(context);
		}
		return instance;
	}
	
	public boolean isTwitterAuthorized()
	{
		return (twitterToken != null);
	}
	
	public boolean isFacebookAuthorized()
	{
		return facebook.isSessionValid();
	}
	
	public void logoutTwitter()
	{
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();       
	    configurationBuilder.setOAuthConsumerKey(CONSUMER_KEY);      
	    configurationBuilder.setOAuthConsumerSecret(CONSUMER_SECRET);      
	    Configuration configuration = configurationBuilder.build();
	    asyncTwitter = new AsyncTwitterFactory(configuration).getInstance();   
	    twitterToken = null;
		clearTwitterToken();
	}
	
	public boolean updateTwitterStatus(String newStatus)
	{
		if (twitterToken == null)
		{
			return false;
		}
		StatusUpdate sus = new StatusUpdate(newStatus);
		asyncTwitter.updateStatus(sus);
		return true;
		
	}
	
	private void setFacebookToken(String token) {
		facebook.setAccessToken(token);
		if (facebook.isSessionValid()) {
			writeFacebookToken();
		} else {
			clearFacebookToken();
		}
	}
	
	private void writeFacebookToken() {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(appContext.openFileOutput(FACEBOOK_TOKEN_FILENAME, Context.MODE_PRIVATE)));
			bw.write(facebook.getAccessToken());	
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void logoutFacebook(Activity activity) {
		if (facebook.isSessionValid())
		{
			try {
				facebook.logout(activity);
			} 
			catch (MalformedURLException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		clearFacebookToken();
	}

	private void clearFacebookToken() {
		facebook.setAccessToken(null);
		appContext.deleteFile(FACEBOOK_TOKEN_FILENAME);
	}
	
	private void clearTwitterToken() {
		appContext.deleteFile(TWITTER_TOKEN_FILENAME);
	}
	
	private void readFacebookToken() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(appContext.openFileInput(FACEBOOK_TOKEN_FILENAME)));
			facebook.setAccessToken(br.readLine());				
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readTwitterToken() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(appContext.openFileInput(TWITTER_TOKEN_FILENAME)));
			String token = br.readLine();
			String tokenSecret = br.readLine();
			br.close();
			twitterToken = new AccessToken(token, tokenSecret);
			asyncTwitter.setOAuthAccessToken(twitterToken);
	    	asyncTwitter.verifyCredentials();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeTwitterToken() {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(appContext.openFileOutput(TWITTER_TOKEN_FILENAME, Context.MODE_PRIVATE)));
			bw.write(twitterToken.getToken());
			bw.write('\n');
			bw.write(twitterToken.getTokenSecret());	
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void authorizeTwitter(Activity activity) {
		Intent intent = new Intent(activity, WTTwitterLoginActivity.class);
		activity.startActivity(intent);
	}
	

	public void authorizeFacebook(Activity activity, final DialogListener dl) {
		try {
			facebook.authorize(activity, new String[] {"publish_stream"}, new DialogListener()
			{

				public void onCancel() {
					dl.onCancel();
				}

				public void onComplete(Bundle arg0) {					
					setFacebookToken(facebook.getAccessToken());
					dl.onComplete(arg0);
				}

				public void onError(DialogError arg0) {					
					dl.onError(arg0);
				}

				public void onFacebookError(FacebookError arg0) {					
					dl.onFacebookError(arg0);
				}
				
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendFacebook(Bundle parameters) {
		try 
		{
			facebook.request("me/feed", parameters, "POST");			
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void facebookAuthorizeCallback(int requestCode, int resultCode, Intent data) {
		facebook.authorizeCallback(requestCode, resultCode, data);
	}
}
