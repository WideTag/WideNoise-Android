package com.widetag.android.WideNoise;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.content.Context;

public class WTTagsManager 
{
	final static String STORED_TAGS_FILE_NAME = "UserTags";
	
	private ArrayList<String> selectedTags;
	private ArrayList<String> storedTags;
	private Context appContext;
	
	private static WTTagsManager instance;
	
	
	private WTTagsManager(Context context)
	{
		super();
		appContext = context;
		selectedTags = new ArrayList<String> ();
		storedTags = new ArrayList<String> ();
	
	}
	
	synchronized public void setSelectedTags(ArrayList<String> tags)
	{
		this.selectedTags = tags;
	}
	
	synchronized public boolean addTagToStore(String newTag)
	{
		if (!storedTags.contains(newTag))
		{
			storedTags.add(newTag);
			
			if (!saveStoredTags())
			{
				storedTags.remove(newTag);
				return false;
			}
			return true;
			
		}
		return false;
	}
	
	synchronized public ArrayList<String> getSelectedTags()
	{
		return selectedTags;
	}
	
	synchronized public ArrayList<String> getStoredTags()
	{
		return storedTags;
	}
	
	synchronized static WTTagsManager getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new WTTagsManager(context.getApplicationContext());
			instance.readStoredTags();
		}
		return instance;
	}
	
	private boolean saveStoredTags()
	{
		try 
		{
			BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(appContext.openFileOutput(STORED_TAGS_FILE_NAME,Context.MODE_PRIVATE)));
			for (int i = 0; i < storedTags.size(); ++i)
			{
				buffer.write(storedTags.get(i));
				if (i != storedTags.size() - 1)
				{
					buffer.write("\n");
				}
			}
			buffer.close();
			return true;
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			return false;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean readStoredTags()
	{
		try 
		{
			FileInputStream fis = appContext.openFileInput(STORED_TAGS_FILE_NAME);
			if (fis != null)
			{
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader buffer = new BufferedReader(isr);
				String singleTag = null;
				ArrayList<String> readTags = new ArrayList<String>();
				do 
				{
					try 
					{
						singleTag = buffer.readLine();
						if (singleTag != null)
						{
							readTags.add(singleTag);
						}
						
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
						singleTag = null;
						return false;
					}
					
				}
				while(singleTag != null);
				
				if (readTags.size() > 0)
				{
					storedTags = null;
					storedTags = readTags;
				}
				try 
				{
					buffer.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
					return false;
				}	
			}
			
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			return false;
		}
		catch (Exception ee)
		{
		}
		
		return true;
	}
	
	
}
