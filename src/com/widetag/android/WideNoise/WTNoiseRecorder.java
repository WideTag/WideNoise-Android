package com.widetag.android.WideNoise;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class WTNoiseRecorder 
{
	final static int samples_per_packet = 256; 
	
	private WTNoise recordedNoise;
	private WTNoiseRecordDelegate delegate;
	private AudioRecord audioRecorder;
	public static int samplingRate = 8000;
	private final static int maxNumOfRecords = 3;
	private final static int recordDuration = 5;
	private final static int maxDuration = recordDuration*maxNumOfRecords;
	public static int bufferDim = samplingRate * maxDuration;
	public static int resamplingNumOfSamples = 20;
	private short buffer[];
	private double buffer_rms[];
	private int buffer_rms_position;
	
	private int currentRecord;
	private int numOfRecordedIntervals;
	private int totalRecords;
	private int lastEnqueued;

	private int getNumOfRecordedSamples()
	{
		return (int)((double)(numOfRecordedIntervals) * (double)samplingRate / resamplingNumOfSamples);
	}
	
	
	public void resetRecordVars()
	{
		
		numOfRecordedIntervals = 0;
		totalRecords = 0;
		lastEnqueued = 0;
	}
	
	public WTNoiseRecorder(WTNoiseRecordDelegate newDelegate)
	{
		audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferDim * 2 );
		buffer = new short[bufferDim];
		buffer_rms = new double[(bufferDim+	samples_per_packet-1)/samples_per_packet];
		buffer_rms_position = 0;
		delegate = newDelegate;
		recordedNoise = new WTNoise();
		
		numOfRecordedIntervals = 0;
		totalRecords = 0;
		lastEnqueued = 0;

		resetRecordVars();
		audioRecorder.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() 
		{

			public void onPeriodicNotification(AudioRecord ar)
			{
				numOfRecordedIntervals++;

				float level = (float)averagePowerForChannel();
				getRecordedNoise().addSample(level);
				delegate.noiseUpdated(getNumOfRecordedSamples(), recordedNoise);
			}
			
			public void onMarkerReached(AudioRecord ar)
			{
				currentRecord++;
				if (currentRecord == totalRecords)
				{
					audioRecorder.stop();
					delegate.hasFinishedRecording(WTNoiseRecorder.this);
				}
				else
				{
					final int lastEnqueuedPosition = lastEnqueued*recordDuration*samplingRate;
					final int samples = recordDuration * samplingRate;
					int nextEnqueuedPosition = lastEnqueuedPosition + samples;
					audioRecorder.setNotificationMarkerPosition(nextEnqueuedPosition);
					audioRecorder.setPositionNotificationPeriod( samplingRate / resamplingNumOfSamples );
					audioRecorder.startRecording();
					new Thread(new Runnable() 
					{         
						public void run() 
						{ 
							audioRecorder.read(buffer, lastEnqueuedPosition, samples);
						}
					}).start();
					
					lastEnqueued++;
				}
			}
		});
	}
	
	synchronized public boolean record()
	{
		if (totalRecords == 0)
		{
			
			currentRecord = 0;
			lastEnqueued = 1;
			buffer_rms_position = 0;
			audioRecorder.setPositionNotificationPeriod( samplingRate / resamplingNumOfSamples );
			audioRecorder.setNotificationMarkerPosition(recordDuration * samplingRate);
			numOfRecordedIntervals = 0;
			
			audioRecorder.startRecording();
			
			new Thread(new Runnable() 
			{         
				public void run() 
				{ 
					audioRecorder.read(buffer,0, recordDuration * samplingRate);
				}
			}).start();
			
		}
		
		totalRecords++;
		
		recordedNoise.setMeasurementDuration(this.getTotalDuration());
		
		return (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING);
	}
	
	public void stop()
	{
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
	
	synchronized public double getTotalDuration()
	{
		return totalRecords*recordDuration;
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
		for(int i = 1; i < buffer_rms_end_packet; i++)
		{
			amount = amount*0.9 + buffer_rms[i]*0.1;
		}
		return (float)amount;
	}
	
	public boolean canEnqueueMoreBuffers() {
		return totalRecords < maxNumOfRecords;
	}
	
}
