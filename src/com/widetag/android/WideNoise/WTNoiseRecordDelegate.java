package com.widetag.android.WideNoise;

public interface WTNoiseRecordDelegate 
{
	public void hasFinishedRecording(WTNoiseRecorder recorder);
	public void noiseUpdated(int numOfRecordedSamples, WTNoise noise);
}
