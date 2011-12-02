package com.widetag.android.WideNoise;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import com.google.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;
import android.location.Location;

public class WTNoise
{
	public WTNoise() 
	{
		identifier = "";
		location = new Location("");
		measurementDate = new Date();
	}
	
	private final ArrayList<Float> samples = new ArrayList<Float>();
	private final Hashtable <String,Float> perceptions = new Hashtable<String,Float>();
	private String identifier;
	private Location location;
	private Date measurementDate;
	private double measurementDuration;
	float averageLevelInDB ;

	private float lookUpTable[][] = 
		{
			{0      , 0},
			{0.003f , 50.0f},
			{0.0046f, 55.0f},
			{0.009f , 60.0f},
			{0.016f , 65.0f},
			{0.031f , 70.0f},
			{0.052f , 75.0f},
			{0.085f , 80.0f},
			{0.15f  , 85.0f},
			{0.25f  , 90.0f},
			{0.5f   , 95.0f},
			{0.8f   , 100.0f},
			{0.9f   , 110.0f},
			{1.0f   , 120.0f}
		}; 
	

	public float interpolate(float x)
	{
		if ( x <= lookUpTable[0][0])
		{
			return lookUpTable[0][1];
		}
		
		for (int i = 0; i < lookUpTable.length - 1; i++)
		{
			float x0 = lookUpTable[i][0];
			float y0 = lookUpTable[i][1];
			float x1 = lookUpTable[i + 1][0];
			float y1 = lookUpTable[i + 1][1];
			
			if ( x <= x1 )
			{
				float y = ((y1 - y0) / (x1 - x0)) * (x - x0) + y0;
				return y;
			}
		}
		
		return lookUpTable[lookUpTable.length - 1][1];
	}


	public float getAverageLevel()
	{
		float averageLevel = 0.0f;
		int samplesCount = samples.size();
		if (samplesCount > 0)
		{
			for (int i = 0 ; i < samplesCount; ++i)
			{
				averageLevel += samples.get(i);
			}
			return averageLevel / samplesCount;
		}
		
		return 0.0f;
	}
	
	public float getAverageLevelInDB() 
	{
		return samples.isEmpty()?averageLevelInDB:interpolate(getAverageLevel());
	}


	public int getIcon()
	{
		
		float db = getAverageLevelInDB();
		int imgId = 0;
		if (db <= 30)
		{
			imgId = (R.drawable.icon_1); 
		}
		else
		if (db <= 60)
		{
			imgId = (R.drawable.icon_2);
		}
		else
		if (db <= 70)
		{
			imgId = (R.drawable.icon_3);
		}
		else
		if (db <= 90)
		{
			imgId = (R.drawable.icon_4);
		}
		else
		if (db <= 100)
		{
			imgId = (R.drawable.icon_5);
		}
		else
		if (db <= 115)
		{
			imgId = (R.drawable.icon_6);
		}
		else
		{
			imgId = (R.drawable.icon_7);
		}

		return imgId;
	}
	
	public String getDescription()
	{
		float avg = getAverageLevelInDB();
		String description = null;
		if (avg <= 10)
		{
			description = new String("silence");
		}
		else 
		if (avg <= 30)
		{
			description = new String("feather noise");
		}
		else
		if (avg <= 60)
		{
			description = new String("sleeping cat noise");
		}
		else
		if (avg <= 70)
		{
			description = new String("television noise");
		}
		else
		if (avg <= 90)
		{
			description = new String("car noise");
		}
		else
		if (avg <= 100)
		{
			description = new String("dragster noise");
		}
		else
		if (avg <= 115)
		{
			description = new String("t-rex noise");
		}
		else
		{
			description = new String("rock concert noise");
		}
		
		return description;
	}

	public void addSample(float level)
	{
		if (level <0.0)
		{
			level = 0.0f;
		}
		else if (level > 1.0)
		{
			level = 1.0f;
		}
		samples.add(level);
	}

	// public float getRawSampleAtIndex(int index) 
	// {}
	
	public Integer hash()
	{
		int prime = 31;
		int result = 1; 
		result = prime * result + identifier.hashCode();
		return result;
	}

	public void setFeelinglevel(float level)
	{
		perceptions.put("feeling", level / 10.0f);
	}
	
	public void setDisturbanceLevel(float level)
	{
		perceptions.put("distrubance", level / 10.0f);
	}
	
	public void setIsolationLevel(float level)
	{
		perceptions.put("isolation", level / 10.0f);
	}
	
	public void setArtificiality(float level)
	{
		perceptions.put("aritificality", level / 10.0f);
	}

	public void setID(String newID)
	{
		identifier = newID;
	}
	
	public void setAverageLevelInDB(float value) {
		assert samples.isEmpty();
		averageLevelInDB = value;
	}
	
	//////////////////////////////////////////////////////
	
	
	public String getTitle()
	{
		return String.format("%ddb - %d\"",(int) getAverageLevel(), measurementDuration );
	}
	
	public String getSubTitle()
	{
		Format formatter;
		formatter = new SimpleDateFormat("yyyy MM dd 'at' HH:mm");
		String output = formatter.format(measurementDate);
		return output;
	}
	
	public ArrayList<Float> getSamples()
	{
		return samples;
	}
	
	public double getMeasurementDuration()
	{
		return measurementDuration;
	}
	
	public void setMeasurementDuration(double duration)
	{
		measurementDuration = duration;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public void setLocation(Location newLocation)
	{
		location = newLocation;
	}
	
	public Date getMeasurementDate()
	{
		return measurementDate;
	}
	
	public void setMeasurementDate(Date newMeasurementDate)
	{
		measurementDate = newMeasurementDate;
	}
	
	public float getSampleAtIndex(int i) 
	{
		return interpolate(samples.get(i));
	}
	
	public Map<String,Float> getPerceptions()
	{
		return perceptions;
	}
	
	public String getID()
	{
		return identifier;
	}
}


