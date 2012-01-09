package com.widetag.android.WideNoise;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public interface WTRequestFactory {

	HttpPost requestForReportingNoise(WTNoise noise, Date date);
	
	HttpGet requestForFetchingNoiseReportsInMapRect(float latitude, float longitude, float latitudeDelta, float longitudeDelta); 
	
	public HttpPost requestForAssigningTags(ArrayList<String> tags, WTNoise noise, Date date);
	
}
