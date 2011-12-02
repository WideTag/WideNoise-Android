package com.widetag.android.WideNoise;

import java.util.Map;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.conf.*;

import android.os.Bundle;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;


public class WTSocialNetworkManager extends TwitterAdapter
{
	final static String FACEBOOK_APP_ID = "###FACEBOOK-APP-ID-KEY-HERE###";
	final static String FACEBOOK_APP_SECRET = "###FACEBOOK-APP-SECRET-KEY-HERE###";

	private AsyncTwitter asyncTwitter;
	private Facebook facebook;
	private AsyncFacebookRunner mAsyncRunner;
	
	
	static private WTSocialNetworkManager instance ;
    AccessToken twitterToken;
	private WTSocialNetworkManager()
	{
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();       
	    configurationBuilder.setOAuthConsumerKey("###TWITTER-CONSUMER-KEY-HERE###");      
	    configurationBuilder.setOAuthConsumerSecret("###TWITTER-CONSUMER-SECRET-KEY-HERE###");      
	    Configuration configuration = configurationBuilder.build();
	    asyncTwitter = new AsyncTwitterFactory(configuration).getInstance();   
	    asyncTwitter.addListener(this);
	    twitterToken = null;
	    
	    facebook = new Facebook(FACEBOOK_APP_ID);
	    mAsyncRunner = new AsyncFacebookRunner(facebook);

	}
	
	synchronized public boolean loginToTwitter(String username, String password )
	{
	    try 
	    {
	    	twitterToken = asyncTwitter.getOAuthAccessToken(username, password);
	    	return true;
		} 
	    catch (TwitterException e) 
	    {
			e.printStackTrace();
			return false;
		}
	}
	
	synchronized static public WTSocialNetworkManager getInstance()
	{
		if (instance == null)
		{
			instance = new WTSocialNetworkManager();
		}
		return instance;
	}
	
	synchronized Facebook getFacebook()
	{
		return facebook;
	}
	
	public boolean isTwitterAuthorized()
	{
		return (twitterToken != null);
	}
	
	public boolean isFacebookAuthorized()
	{
		return false;
	}
	
	public void logoutFromTwitter()
	{
		twitterToken = null;
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
	
	public void updateFacebookStatus(Map newStatus)
	{
		
	}
	
	@Override
	public void updatedStatus(Status statuses)
	{
		int i = 0;
		int j = i;
	}
	
}
