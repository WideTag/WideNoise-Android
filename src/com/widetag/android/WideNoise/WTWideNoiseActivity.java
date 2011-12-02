package com.widetag.android.WideNoise;



import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class WTWideNoiseActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
        //tabHost.setup();
        Resources res = getResources(); 
        
        intent = new Intent().setClass(this, WTListenActivity.class);
        Drawable listenIcon = res.getDrawable(R.drawable.ic_tab_listen);
        
        spec = tabHost.newTabSpec("listenActivity").setIndicator("Listen", listenIcon).setContent(intent);
        tabHost.addTab(spec);
       
        
        intent = new Intent().setClass(this, WTMapActivity.class);
        Drawable mapIcon = res.getDrawable(R.drawable.ic_tab_map);
        spec = tabHost.newTabSpec("mapActivity").setIndicator("Map", mapIcon).setContent(intent);
        //tabHost.getTabWidget().getChildAt(0).setBackgroundResource(Color.GREEN); 
        //spec = tabHost.newTabSpec("mapActivity").setIndicator("Map", null).setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, WTShareActivity.class);
        Drawable shareIcon =  res.getDrawable(R.drawable.ic_tab_share);
        spec = tabHost.newTabSpec("shareActivity").setIndicator("Share", shareIcon).setContent(intent);
        //spec = tabHost.newTabSpec("shareActivity").setIndicator("Share", null).setContent(intent);
        tabHost.addTab(spec);
        Drawable aboutIcon =  res.getDrawable(R.drawable.ic_tab_about);
        spec = tabHost.newTabSpec("about").setIndicator("About", aboutIcon).setContent(R.id.tabview4);
        //spec = tabHost.newTabSpec("about").setIndicator("About", null).setContent(R.id.tabview4);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);

    }
}