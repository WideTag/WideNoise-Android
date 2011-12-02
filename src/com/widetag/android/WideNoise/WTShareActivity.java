package com.widetag.android.WideNoise;

import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import twitter4j.TwitterException;
import twitter4j.User;

public class WTShareActivity extends Activity 
{
	private static final String[] PERMS = new String[] { "user_events" };
	
	Button linkTwitterButton;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		
		linkTwitterButton = (Button) findViewById(R.id.link_twitter_button); 
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		WTSocialNetworkManager.getInstance().getFacebook().authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		WTSocialNetworkManager snm = WTSocialNetworkManager.getInstance();
		
		if  (snm.isTwitterAuthorized())
		{
			linkTwitterButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_twitter_logged));
		}
		
		if (snm.isFacebookAuthorized())
		{
			linkTwitterButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_twitter_not_logged));
		}
		  
	}
	
	public void link_twitter_callback( View view)
	{
		WTSocialNetworkManager snm = WTSocialNetworkManager.getInstance();
		if (snm.isTwitterAuthorized())
		{
			// alert: chiedere se si conferma il log out
			// assegnare questo stesso oggetto come listener
			snm.logoutFromTwitter();
			linkTwitterButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_twitter_not_logged));
		}
		else
		{
			Intent intent = new Intent(this, WTTwitterLoginActivity.class);
			WTShareActivity.this.startActivity(intent);
			if (snm.isTwitterAuthorized())
			{
				
			}
		}
	}
	
	public void link_facebook_callback( View view)
	{
		Facebook facebook = WTSocialNetworkManager.getInstance().getFacebook();
		if (facebook.isSessionValid())
		{
			try {
				facebook.logout(this);
			} 
			catch (MalformedURLException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			facebook.authorize(this,PERMS, new DialogListener()
			{

				public void onCancel() {
					int i = 1;
				}

				public void onComplete(Bundle arg0) {
					
					int i = 1;
				}

				public void onError(DialogError arg0) {
					
					int i = 1;
				}

				public void onFacebookError(FacebookError arg0) {
					
					int i = 1;
				}
				
			});
		}
	}

	public void email_widget_callback( View view)
	{
		
	}
	
	public void open_personal_page_callback( View view)
	{
		
	}
}
