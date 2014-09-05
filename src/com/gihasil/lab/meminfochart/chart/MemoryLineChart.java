/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gihasil.lab.meminfochart.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.gihasil.lab.meminfochart.db.MemInfoDB;
import com.gihasil.lab.meminfochart.db.MemInfoDBHandler;
import com.gihasil.lab.meminfochart.utils.MemInfoUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Average temperature demo chart.
 */
public class MemoryLineChart extends AbstractChart {
	/**
	 * Returns the chart name.
	 * 
	 * @return the chart name
	 */
	private MemInfoDBHandler mDbHandler;
	private Cursor mCursor;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDesc() {
		// TODO Auto-generated method stub
		return null;
	}

	public GraphicalView createView(Context context, String packageName) {
		mDbHandler = MemInfoDBHandler.open(context);
		mCursor = mDbHandler.dbSelectPackageCusor(packageName);
		int rowCounts = mCursor.getCount();
		
		if (rowCounts == 0) return null; 
			
		String[] titles = new String[] { "Total" , "Dalvik", "Other Device", "Native"};
		
		List<Date[]> x = new ArrayList<Date[]>();
		List<double[]> values = new ArrayList<double[]>();
		Date[] xlists = new Date[rowCounts];
		double[] valueLists = new double[rowCounts];
		double[] DalvikLists = new double[rowCounts];
		double[] OtherLists = new double[rowCounts];
		double[] NativeLists = new double[rowCounts];
		double[] xBoundary = new double[2];
		double[] yBoundary = new double[2];
		int index = 0;
		if (mCursor.moveToFirst()) {
			do {
				xlists[index] = new Date(mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_TIMESTAMP)));
				DalvikLists[index] = mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_DALVIK));
				OtherLists[index] = mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_OTHERDEV));
				NativeLists[index] = mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_NATIVE));
				valueLists[index++] = mCursor.getLong(mCursor.getColumnIndex(MemInfoDB.DB_COLUMN_PSSMEM));
			} while (mCursor.moveToNext());
		}
		
		for (int i = 0; i < titles.length; i++) {
			x.add(xlists);
		}		
		values.add(valueLists);
		values.add(DalvikLists);
		values.add(OtherLists);
		values.add(NativeLists);
		
		int[] colors = new int[] { Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN };
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE , PointStyle.DIAMOND, PointStyle.SQUARE, PointStyle.TRIANGLE};
		
		//		PointStyle.DIAMOND, PointStyle.TRIANGLE, PointStyle.SQUARE };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}
//		xBoundary = MemLeakUtils.getMostSmallBigValue(xlists);
		yBoundary = MemInfoUtils.getMostSmallBigValue(valueLists);
		
		//yBoundary = MathHelper.minmax(values);
		
		double y1 = 0;//yBoundary[MemLeakUtils.MIN_VALUE_INDEX] - 1000;
		double y2 = yBoundary[MemInfoUtils.MAX_VALUE_INDEX] + 1000;
		
		setChartSettings(renderer, packageName, "Time(dd hh:mm)", "Memory (kB)",
		        x.get(0)[0].getTime(), x.get(0)[xlists.length-1].getTime(),
		        y1, y2, 
		        Color.LTGRAY, Color.LTGRAY);
		
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.BLACK);
		renderer.setXLabels(8);
		renderer.setYLabels(10);
		renderer.setShowGrid(true);
//		renderer.setXLabelsAlign(Align.RIGHT);
//		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setZoomButtonsVisible(false);
//		renderer.setPanLimits(new double[] { xBoundary[MemLeakUtils.MIN_VALUE_INDEX], xBoundary[MemLeakUtils.MAX_VALUE_INDEX],
//				yBoundary[MemLeakUtils.MIN_VALUE_INDEX], yBoundary[MemLeakUtils.MAX_VALUE_INDEX] });
//		renderer.setZoomLimits(new double[] { xBoundary[MemLeakUtils.MIN_VALUE_INDEX], xBoundary[MemLeakUtils.MAX_VALUE_INDEX],
//				yBoundary[MemLeakUtils.MIN_VALUE_INDEX], yBoundary[MemLeakUtils.MAX_VALUE_INDEX] });

		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		float textSize = (float)(Math.min(size.x, size.y) / 40);
		int[] margins = renderer.getMargins();
		margins[1] = (int)(size.x / 20);
		margins[2] = (int)(size.y / 20);
		renderer.setMargins(margins);
		
		renderer.setLabelsTextSize(textSize);
		renderer.setLegendTextSize(textSize);
		renderer.setAxisTitleTextSize(textSize);
		renderer.setYLabelsPadding(-textSize);
		renderer.setChartTitleTextSize(textSize);
		
//		XYMultipleSeriesDataset dataset = buildDataset(titles, x, values);
		//XYSeries series = dataset.getSeriesAt(0);
		//series.addAnnotation("Vacation", 6, 30);
		if (mCursor != null && mCursor.isClosed()) {
			mCursor.close();
		}
		return ChartFactory.getTimeChartView(context, buildDateDataset(titles, x, values),
		        renderer, "MM/dd HH:mm");
	}
	
	/**
	 * Executes the chart demo.
	 * 
	 * @param context
	 *            the context
	 * @return the built intent
	 */
	public Intent execute(Context context, String packageName) {
		return null;
	}

}
