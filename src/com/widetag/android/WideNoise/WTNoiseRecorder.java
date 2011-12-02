package com.widetag.android.WideNoise;

import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class WTNoiseRecorder 
{
	final int samples_per_packet = 256; 
	
	private double recordingDuration;
	private WTNoise recordedNoise;
	private WTNoiseRecordDelegate delegate;
	private AudioRecord audioRecorder;
	public static int samplingRate = 8000;
	public static int maxNumOfRecords = 3;
	public static int maxDuration = 5;
	public static int bufferDim = samplingRate * maxDuration * maxNumOfRecords;
	public static int resamplingNumOfSamples = 20;
	private short buffer[];
	private double buffer_rms[];
	private int buffer_rms_position;
	
	private double listOfDuration[];
	private int currentRecord;
	private int lastAddedRecord;
	private int numOfRecordedIntervals;
	
	private int numOfRecordToComplete = 0;
	
	private Thread thread;
	
	private int tableSize = 14;
	
	private WTNoiseRecorder()
	{
		// NOT ALLOWED
	}
	
	private int getNumOfRecordedSamples()
	{
		return (int)((double)(numOfRecordedIntervals) * (double)samplingRate / resamplingNumOfSamples);
	}
	
	
	private int numberOfSamples()
	{
		return (int)(recordingDuration * resamplingNumOfSamples);
	}
	
	private void resetRecordVars()
	{
		listOfDuration = null;
		currentRecord = -1;
		lastAddedRecord = -1;
		numOfRecordedIntervals = 0;
	}
	
	public WTNoiseRecorder(WTNoiseRecordDelegate newDelegate)
	{
		//
		audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferDim * 2 );
		//audioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffersizebytes );
		buffer = new short[bufferDim];
		buffer_rms = new double[(bufferDim+	samples_per_packet-1)/samples_per_packet];
		buffer_rms_position = 0;
		delegate = newDelegate;
		recordedNoise = new WTNoise();
		resetRecordVars();
		audioRecorder.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() 
		{

			public void onPeriodicNotification(AudioRecord ar)
			{
				WTNoiseRecorder.this.numOfRecordedIntervals++;

				if (ar.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
				{
					//float level = (float) Math.pow(10.0, 0.05*WTNoiseRecorder.this.averagePowerForChannel());
					float level = (float) WTNoiseRecorder.this.averagePowerForChannel();
					WTNoiseRecorder.this.getRecordedNoise().addSample(level);
					WTNoiseRecorder.this.delegate.noiseUpdated(WTNoiseRecorder.this.getNumOfRecordedSamples(), recordedNoise);
				}
				else
				{
					WTNoiseRecorder.this.audioRecorder.stop();
					WTNoiseRecorder.this.resetRecordVars();
				}
			}
			
			public void onMarkerReached(AudioRecord ar)
			{
				if (WTNoiseRecorder.this.currentRecord >= WTNoiseRecorder.this.lastAddedRecord)
				{
					//double md = WTNoiseRecorder.this.recordedNoise.getMeasurementDuration();
					//int index = WTNoiseRecorder.this.currentRecord;
					//WTNoiseRecorder.this.recordedNoise.setMeasurementDuration(md + WTNoiseRecorder.this.listOfDuration[index]);
					WTNoiseRecorder.this.audioRecorder.stop();
					WTNoiseRecorder.this.currentRecord = -1;
					WTNoiseRecorder.this.delegate.hasFinishedRecording(WTNoiseRecorder.this);
					
				}
				else
				{
					currentRecord++;
					WTNoiseRecorder.this.audioRecorder.setNotificationMarkerPosition((int)(WTNoiseRecorder.this.listOfDuration[currentRecord] * samplingRate));
				}
			}
		});
	}
	
	public boolean recordForDuration(double duration)
	{
		if (duration <= 0)
		{
			return false;
		}
		
		if (duration > maxDuration)
		{	
			duration = maxDuration;
		}
		
		if (lastAddedRecord == maxNumOfRecords) // too many records
		{
			return false;
		}
		
		
		if (audioRecorder.getState() == AudioRecord.RECORDSTATE_STOPPED) // this is the first record
		{
			lastAddedRecord = -1;
			listOfDuration = new double[maxNumOfRecords];
			currentRecord = 0;
			listOfDuration[0] = duration;
			lastAddedRecord = 0;
			buffer_rms_position = 0;
			audioRecorder.setPositionNotificationPeriod( samplingRate / resamplingNumOfSamples );
			audioRecorder.setNotificationMarkerPosition((int)(duration * samplingRate));
			numOfRecordedIntervals = 0;
			
			
			thread = new Thread(new Runnable() 
				{         
					public void run() 
					{   
						int addedRead = 0;
						audioRecorder.startRecording();
						audioRecorder.read(buffer,0,(int)(WTNoiseRecorder.this.listOfDuration[0] * samplingRate ));
						
						while(audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) 
						{
							if (lastAddedRecord > addedRead)
							{
								double totalDuration = 0;
								for (int i = 0; i < currentRecord; i++)
								{
									totalDuration = WTNoiseRecorder.this.listOfDuration[i];
								}
								audioRecorder.read(buffer,(int)(totalDuration * samplingRate),(int)(WTNoiseRecorder.this.listOfDuration[currentRecord] * samplingRate));
							}
						}
					}     
				});
			thread.start(); 
		}
		else
		{
			listOfDuration[++lastAddedRecord] = duration;
			
		}
		recordedNoise.setMeasurementDuration(this.getTotalDuration());
		
		if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
		{
			return true;
		}
		
		return false;
	}
	
	public void stop()
	{
		// useless in android: recording duration is established before starting recording
	}
	
	public void clear()
	{
		if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
		{
			recordedNoise = new WTNoise();
		}
	}
	
	public WTNoise getRecordedNoise()
	{
		return recordedNoise;
	}
	
	public double getCurrentRecordingDuration()
	{
		if ((listOfDuration == null) || (currentRecord < 0))
		{
			return 0;
		}
		return listOfDuration[currentRecord];
	}
	
	
	public float averagePowerForChannel()
	{
		int buffer_rms_end_packet = getNumOfRecordedSamples()/samples_per_packet;
		for(int packet = buffer_rms_position; packet < buffer_rms_end_packet; packet++)
		{
			double amount = 0.0;
			int end = (packet+1)*samples_per_packet;
			int start =  packet*samples_per_packet;
			
			for (int i = start; i < end; i++)
			{
				double normSample = buffer[i] / 32768.0 ;
				normSample *= normSample;
				amount += normSample;
			}
			
			if (amount > 0.0)
			{
				amount = Math.sqrt(amount/samples_per_packet);
			} else {
				amount = 0.0;
			}
			buffer_rms[packet] = amount;
		}
		buffer_rms_position = buffer_rms_end_packet;
		
		double amount = buffer_rms[0];
//		int start_packet = Math.max(0, buffer_rms_end_packet-32);
		for(int i = 1; i < buffer_rms_end_packet; i++)
		{
			amount = amount*0.9 + buffer_rms[i]*0.1;
		}
//		if (start_packet != buffer_rms_end_packet)
//		{
//			return (float)Math.pow(10.0, (amount_db/(buffer_rms_end_packet - start_packet)-120.0)/20.0);
//		}
		//return (float)((amount>0.0)?20.0*Math.log(amount):-160.0);
		return (float)amount;
	}
	
	public double getTotalDuration()
	{
		double duration = 0;
		for (int i = 0; i <=lastAddedRecord; i++)
		{
			duration += listOfDuration[i];
		}
		return duration;
	}
	
}
