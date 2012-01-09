package com.widetag.android.WideNoise;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class WTTagActivity extends Activity
{

		
	class ClickableText extends TextView
	{
		private boolean selected;
		ImageView selectedImgView;
		TableRow tableRow;
		
		public ClickableText(WTTagActivity activity, ImageView selectedImgView, TableRow tableRow)
		{
			super(activity);
			selected = false;
			this.selectedImgView = selectedImgView; 
			this.tableRow = tableRow;
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			if (!selected)
			{
				this.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
				selectedImgView.setBackgroundDrawable(getResources().getDrawable(R.drawable.checkmark));
				selected = true;
				int bckndColor = Color.rgb(255, 236, 215);
				tableRow.setBackgroundColor(bckndColor);
			}
			else
			{
				tableRow.setBackgroundColor(Color.WHITE);
				this.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
				selectedImgView.setBackgroundDrawable(null);
				selected = false;
			}
			return super.onTouchEvent(event);
		}
		
		public boolean isSelected()
		{
			return selected;
		}

	}

	
	
	private ArrayList<ClickableText> tagTextList;
	private ArrayList<String> localCopyOfStoredTags;
	private EditText newTagEditText;
	private TableLayout table;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag);
		newTagEditText = (EditText) findViewById(R.id.new_tag_generator);
		table = (TableLayout) findViewById(R.id.tag_table);

		localCopyOfStoredTags = WTTagsManager.getInstance(this).getStoredTags();
		tagTextList = new ArrayList<ClickableText>();

		for (int i = 0; i < localCopyOfStoredTags.size(); i++)
		{
			AddRow(i ,localCopyOfStoredTags.get(i) );
		}
		
	}
	
	@Override
	public void onResume()
	{
		

		super.onResume();
	
	}
	
	public void tag_cancel_button_callback(View view)
	{
		WTTagsManager.getInstance(this).getSelectedTags().clear();
		finish();
	}
	
	public void tag_done_button_callback(View view)
	{
		ArrayList<String> selectedTags = new ArrayList<String>();
		selectedTags.clear();
		
		for (ClickableText text: tagTextList)
		{
			if (text.isSelected())
			{
				selectedTags.add(text.getText().toString());
			}
		}
		WTTagsManager.getInstance(this).setSelectedTags(selectedTags);
		
		setResult(RESULT_OK);
		finish();
	}
	
	
	public void add_tag_button_callback(View view)
	{
		String textToInsert = newTagEditText.getText().toString();
		if (!textToInsert.contentEquals(""))
		{
			if (!localCopyOfStoredTags.contains(textToInsert))
			{
				WTTagsManager.getInstance(this).addTagToStore(textToInsert);
				
				AddRow(0,newTagEditText.getText().toString());
				newTagEditText.setText("");
			}
		}
	}
	
	private void AddRow(int position, String textString)
	{
		TableRow tr = new TableRow(WTTagActivity.this);
		
		Resources r = getResources();
		int trHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40 , r.getDisplayMetrics());
		LayoutParams trParams = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,trHeight);
		tr.setLayoutParams(trParams);
		tr.setBackgroundColor(Color.WHITE);
		
		
		final ImageView img = new ImageView(this);
		ClickableText text = new ClickableText(WTTagActivity.this, img, tr);
		text.setBackgroundColor(Color.TRANSPARENT);
		text.setHeight(trHeight);
		text.setWidth(tr.getWidth() - trHeight);
		
		
		text.setTextSize(trHeight /3);
		text.setTextColor(Color.BLACK);
		text.setGravity(Gravity.CENTER_VERTICAL);
		
		
		text.setText(textString);
		
		tr.addView(text);
		tagTextList.add(text);
		
		LayoutParams dummyParams = new LayoutParams(trHeight,trHeight);
		ImageView dummyImg = new ImageView(this);
		dummyImg.setLayoutParams(dummyParams);
		
		int checkDim = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20 , r.getDisplayMetrics());
		
		int borderSize = (trHeight - checkDim)/2;
		img.layout(borderSize , borderSize,trHeight - borderSize , trHeight - borderSize);
		
		RelativeLayout imgL = new RelativeLayout(this);
		imgL.setBackgroundColor(Color.TRANSPARENT);
		imgL.addView(dummyImg);
		imgL.addView(img);
		tr.addView(imgL);

		table.addView(tr,position);
	}

	
}
