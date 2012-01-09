package com.widetag.android.WideNoise;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

public class WTLedView extends View
{

	
	static private int pixel_width = 2;
	static private int pixel_pitch = 1;
	static private int col_width = pixel_width + pixel_pitch;
	
	private int ledColor;
	
	private WTLedViewDataSource dataSource;

	public WTLedView(Context context) 
	{
		super(context);
		ledColor = Color.rgb(255, 172, 83);
	
	}
	
	public void onCreate(Bundle savedInstanceState)
	{
		
	}
	
	protected void onDraw(Canvas canvas)
	{

		Paint paint = new Paint();
		paint.setStrokeWidth(2);
		DashPathEffect dashPath = new DashPathEffect(new float[]{pixel_width,pixel_pitch}, 1);
		paint.setPathEffect(dashPath);
		paint.setColor(ledColor);
		int height = getHeight();
		if (dataSource != null)
		{
			for (int i = 0; i < getNumberOfCols(); i++)
			{
				float value = dataSource.ledViewValue(this, i); 
				canvas.drawLine(col_width * i, height, col_width * i, height - col_width * (int) (getNumberOfRows() * value), paint);
			}
		}
	}
	
	public int getNumberOfCols()
	{
		return getWidth() / col_width;
	}
	
	public int getNumberOfRows()
	{
		return getHeight() / col_width;
	}

	public void setLedColor(int newColor)
	{

	}
	
	public void setDataSource(WTLedViewDataSource newDataSource)
	{
		dataSource = newDataSource;
	}
	
}
