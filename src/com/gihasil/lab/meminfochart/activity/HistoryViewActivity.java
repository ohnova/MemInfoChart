package com.gihasil.lab.meminfochart.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.gihasil.lab.meminfochart.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class HistoryViewActivity extends Activity {
	private TextView mHistory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_activity);
		
		mHistory = (TextView) findViewById(R.id.textViewHistory);
		mHistory.setText(readRawTextFile(getApplicationContext(), R.raw.readme));
	}

	public static String readRawTextFile(Context ctx, int resId)
	{
	    InputStream inputStream = ctx.getResources().openRawResource(resId);

	    InputStreamReader inputreader = new InputStreamReader(inputStream);
	    BufferedReader buffreader = new BufferedReader(inputreader);
	    String line;
	    StringBuilder text = new StringBuilder();

	    try {
	        while (( line = buffreader.readLine()) != null) {
	            text.append(line);
	            text.append('\n');
	        }
	    } catch (IOException e) {
	        return null;
	    }
	    return text.toString();
	}
}
