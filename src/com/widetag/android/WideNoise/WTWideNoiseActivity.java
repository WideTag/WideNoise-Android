package com.widetag.android.WideNoise;



import twitter4j.Twitter;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

public class WTWideNoiseActivity extends TabActivity {
	private Twitter twitter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
        Resources res = getResources(); 
        
        intent = new Intent().setClass(this, WTListenActivity.class);
        Drawable listenIcon = res.getDrawable(R.drawable.ic_tab_listen);
        
        spec = tabHost.newTabSpec("listenActivity").setIndicator("Listen", listenIcon).setContent(intent);
        tabHost.addTab(spec);
       
        
        intent = new Intent().setClass(this, WTMapActivity.class);
        Drawable mapIcon = res.getDrawable(R.drawable.ic_tab_map);
        spec = tabHost.newTabSpec("mapActivity").setIndicator("Map", mapIcon).setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, WTShareActivity.class);
        Drawable shareIcon =  res.getDrawable(R.drawable.ic_tab_share);
        spec = tabHost.newTabSpec("shareActivity").setIndicator("Share", shareIcon).setContent(intent);
        tabHost.addTab(spec);
        Drawable aboutIcon =  res.getDrawable(R.drawable.ic_tab_about);
        spec = tabHost.newTabSpec("about").setIndicator("About", aboutIcon).setContent(R.id.tabview4);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
    }
    
    public Twitter getTwitter()
    {
    	 return twitter;
    }
    
    public void aboutWidetagClick(View v) {
    	Uri uri = Uri.parse("http://www.widetag.com");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    public void aboutEveryAwareClick(View v) {
    	Uri uri = Uri.parse("http://www.everyaware.eu/");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    public void aboutISIClick(View v) {
    	Uri uri = Uri.parse("http://http://www.isi.it/");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    public void aboutSapUniClick(View v) {
    	Uri uri = Uri.parse("http://www.phys.uniroma1.it/DipWeb/home.html");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    public void aboutCSPClick(View v) {
    	Uri uri = Uri.parse("http://www.csp.it/");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    public void aboutL3SClick(View v) {
    	Uri uri = Uri.parse("http://www.everyaware.eu/consortium/l3s-research-center/");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
    
    public void aboutCodemachineClick(View v) {
    	Uri uri = Uri.parse("http://www.codemachine.it");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    }
} 