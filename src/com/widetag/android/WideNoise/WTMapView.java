package com.widetag.android.WideNoise;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.MapView;

public class WTMapView extends MapView {

	public WTMapView(Context context, String apiKey) {
		super(context, apiKey);
	}
	
	 @Override
	public boolean onTouchEvent(MotionEvent event) 
	 {
		 if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			 ((WTMapActivity)getContext()).onMapMoved();
		 }
		return super.onTouchEvent(event);
	}

}
