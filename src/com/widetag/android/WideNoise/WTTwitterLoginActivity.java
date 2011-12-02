package com.widetag.android.WideNoise;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class WTTwitterLoginActivity extends Activity
{
	EditText userName;
	EditText password;
	Button loginButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter);
		
		userName = (EditText) findViewById(R.id.twitter_username_text);
		password = (EditText) findViewById(R.id.twitter_password_text);
		loginButton = (Button) findViewById(R.id.twitter_login_button);
		
		loginButton.setEnabled(false);
		
		TextWatcher textWatcher = new TextWatcher() 
		{
			public void afterTextChanged(Editable s) 
			{
				String userNameText = WTTwitterLoginActivity.this.userName.getText().toString();
				String passwordText = WTTwitterLoginActivity.this.password.getText().toString();
				if (!userNameText.contentEquals("") &&
					!passwordText.contentEquals(""))
				{
					loginButton.setEnabled(true);
					}
				else
				{
					loginButton.setEnabled(false);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}};
		userName.addTextChangedListener(textWatcher);
		password.addTextChangedListener(textWatcher);
	}
	
	public void cancel_button_callback(View view)
	{
		finish();
	}
	
	public void login_button_callback(View view)
	{
		String userNameText = userName.getText().toString();
		String passwordText = password.getText().toString();
		Button thisButton = (Button)view;
		thisButton.setEnabled(false);
		boolean logged = WTSocialNetworkManager.getInstance().loginToTwitter(userNameText, passwordText);
		if (logged)
		{
			// messaggio di successo?
		}
		else
		{
			//messaggio di errore
			// ritorno dalla navigazione
		}
		finish();
	}
		
}
