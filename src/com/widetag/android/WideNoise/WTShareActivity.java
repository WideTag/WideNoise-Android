package com.widetag.android.WideNoise;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class WTShareActivity extends Activity 
{
	private static final String PERSONAL_PAGE_URL = "http://widenoise.com/profile?id=%s";	
	private static final String WIDGET_URL = "http://www.widetag.com/widenoise/widget";
	
	Button linkTwitterButton;
	Button linkFacebookButton;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		
		linkTwitterButton = (Button) findViewById(R.id.link_twitter_button); 
		linkFacebookButton = (Button) findViewById(R.id.link_facebook_button); 
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		WTSocialNetworkManager snm = WTSocialNetworkManager.getInstance(this);
		snm.facebookAuthorizeCallback(requestCode, resultCode, data);
		if (snm.isFacebookAuthorized())
		{
			linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_logged));
		}
		else
		{
			linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_not_logged));
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		WTSocialNetworkManager snm = WTSocialNetworkManager.getInstance(this);
		
		if  (snm.isTwitterAuthorized())
		{
			linkTwitterButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_twitter_logged));
		}
		else
		{
			linkTwitterButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_twitter_not_logged));
		}
		
		if (snm.isFacebookAuthorized())
		{
			linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_logged));
		}
		else
		{
			linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_not_logged));
		}

	}
	
	public void link_twitter_callback( View view)
	{
		WTSocialNetworkManager snm = WTSocialNetworkManager.getInstance(this);
		if (snm.isTwitterAuthorized())
		{
			snm.logoutTwitter();
			linkTwitterButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_twitter_not_logged));
		}
		else
		{
			snm.authorizeTwitter(this);
			if (snm.isTwitterAuthorized())
			{
				linkTwitterButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_twitter_logged));
			}
		}
	}

	public void link_facebook_callback( View view)
	{
		WTSocialNetworkManager snm = WTSocialNetworkManager.getInstance(this);
		if (snm.isFacebookAuthorized())
		{
			snm.logoutFacebook(this);
			linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_not_logged));
		}
		else 
		{
			snm.authorizeFacebook(this, new DialogListener() {
				public void onFacebookError(FacebookError e) {
					linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_not_logged));
					Toast t = new Toast(WTShareActivity.this);
					t.setText(e.getLocalizedMessage());
					t.show();					
				}
				
				public void onError(DialogError e) {
					linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_not_logged));
					Toast t = new Toast(WTShareActivity.this);
					t.setText(e.getLocalizedMessage());
					t.show();					
				}
				
				public void onComplete(Bundle values) {
					linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_logged));
				}
				
				public void onCancel() {
					linkFacebookButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_facebook_not_logged));
				}
			});
		}
	}


	public void email_widget_callback( View view)
	{
		Uri uri = Uri.parse(WIDGET_URL); 
		Intent intent = new Intent(Intent.ACTION_VIEW, uri); 
		startActivity(intent);
	}
	
	public void open_personal_page_callback( View view)
	{
		TelephonyManager tm = (TelephonyManager)(getSystemService(Context.TELEPHONY_SERVICE));
		String ID = tm.getDeviceId();
		String personalPage = String.format(PERSONAL_PAGE_URL, ID);
		Uri uri = Uri.parse(personalPage); 
		Intent intent = new Intent(Intent.ACTION_VIEW, uri); 
		startActivity(intent);
	}
}
