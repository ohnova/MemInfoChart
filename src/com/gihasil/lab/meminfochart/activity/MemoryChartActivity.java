package com.gihasil.lab.meminfochart.activity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.achartengine.GraphicalView;

import com.gihasil.lab.meminfochart.chart.MemoryLineChart;
import com.gihasil.lab.meminfochart.db.MemInfoDB;
import com.gihasil.lab.meminfochart.db.MemInfoDBHandler;
import com.gihasil.lab.meminfochart.R;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MemoryChartActivity extends Activity {		
	private static final int MENU_ITEM_EXPORT_TXT_FILE = 1;
	//private IAChart memChart = new MemoryLineChart();
	private MemInfoDBHandler mDbHandler;
	private Cursor mCursor;
	private String mPackageName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mPackageName = getIntent().getStringExtra("name");
		GraphicalView mGraphicalView = new MemoryLineChart().createView(getApplicationContext(), mPackageName);
		if (mGraphicalView != null) {
			setContentView(mGraphicalView);
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_EXPORT_TXT_FILE, 0, R.string.export_txt_file);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_EXPORT_TXT_FILE:
			saveFile();
			break;
		}
		return true;
	}
	
	private void saveFile() {
		String dirPath = "/sdcard/MemInfoChart";//Environment.getExternalStorageDirectory() + "/MemInfoChart/Data";
		File file = new File(dirPath);
		
		if (!file.exists())	{
			file.mkdir();
		}
		
		mDbHandler = MemInfoDBHandler.open(getApplicationContext());
		mCursor = mDbHandler.dbSelectPackageCusor(mPackageName);
		int rowCounts = mCursor.getCount();
		
		if (rowCounts == 0) return; 
		
		long time = System.currentTimeMillis(); 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");    
	    Date day = new Date(time);
	    String strTime = sdf.format(day);
	    String filename = dirPath+"/"+mPackageName+"_"+strTime+".csv";
		//File saveFile = new File(filename);
		
	    SimpleDateFormat dd = new SimpleDateFormat("yyyy-MM-dd");
	    SimpleDateFormat tt = new SimpleDateFormat("HH.mm.ss");
	    
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			fw.append(mPackageName+"\n");
			fw.append("Total,Dalvik,OtherDev,Native,Day,Time\n");
			Date date = null;
			if (mCursor.moveToFirst()) {
				do {
					fw.append(Long.toString(mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_PSSMEM))));
					fw.append(',');
					fw.append(Long.toString(mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_DALVIK))));
					fw.append(',');
					fw.append(Long.toString(mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_OTHERDEV))));
					fw.append(',');
					fw.append(Long.toString(mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_NATIVE))));
					fw.append(',');				
					date = new Date(mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_TIMESTAMP)));
					fw.append(dd.format(date));
					fw.append(',');
					fw.append(tt.format(date));
					fw.append('\n');
				} while (mCursor.moveToNext());
			}
			Toast.makeText(getApplicationContext(), "Saved to \n" + filename, Toast.LENGTH_LONG).show();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
		
		if (mCursor != null && mCursor.isClosed()) {
			mCursor.close();
		}
		
	}
}
