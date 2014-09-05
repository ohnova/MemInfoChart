package com.gihasil.lab.meminfochart.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.gihasil.lab.meminfochart.utils.MemInfoLog;
import com.gihasil.lab.meminfochart.utils.MemInfoUtils;
import com.gihasil.lab.meminfochart.R;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AlwaysOnTopService extends Service { // 항상 보이게 할 뷰
	private GraphicalView mChartView;
	private WindowManager.LayoutParams mParams; // layout params 객체. 뷰의 위치 및 크기
	private WindowManager mWindowManager; // 윈도우 매니저
	private String mPackageName;
	private Handler mHdler;
	private Context mContext;
	private static XYMultipleSeriesDataset mDataset;
	private static TimeSeries mTotalPssSeries;
	private static TimeSeries mDalvikPssSeries;
	private static TimeSeries mOthersPssSeries;
	private static TimeSeries mNativePssSeries;
	private static XYSeriesRenderer mTotalPssRendererSeries;
	private static XYSeriesRenderer mDalvikPssRendererSeries;
	private static XYSeriesRenderer mOthersPssRendererSeries;
	private static XYSeriesRenderer mNativePssRendererSeries;

	private int mScreenHeight;
	private int mScreenWidth;
	private int mStatusBarHeight;

	private float mLandscapeFloatingMovePreX;
	private float mLandscapeFloatingMovePreY;
	private float mPortraitFloatingMovePreX;
	private float mPortraitFloatingMovePreY;
	private static float mLandscapeFloatingMoveLastX = 0;
	private static float mLandscapeFloatingMoveLastY = 0;
	private static float mPortraitFloatingMoveLastX = 0;
	private static float mPortraitFloatingMoveLastY = 0;

	private static final int FOCUSING_TEST_INTERVER = 500;
	private static final int MAX_ROW_COUNT = 50;

	private static final float FLOATING_POSITION_X = 0.5f;
	private static final float PORTRAIT_FLOATING_POSITION_Y = 0.4f;
	private static PointF mLandscapeFloatingPosition = new PointF(
			FLOATING_POSITION_X, 0f);
	private static PointF mPortraitFloatingPosition = new PointF(
			FLOATING_POSITION_X, PORTRAIT_FLOATING_POSITION_Y);

	private Button mCloseButton = null;
	// XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
	XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

	private TextView mTitleText;
	private ImageView mPackageIcn;
	private LinearLayout mLayout;
	private View mAot;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mChartView = null;
		mContext = this;
		initView(intent);
		setRepeat();
		initChart();
		return super.onStartCommand(intent, flags, startId);
	}

	private void initChart() {
		mDataset = new XYMultipleSeriesDataset();
		mTotalPssSeries = new TimeSeries("Total");
		mDalvikPssSeries = new TimeSeries("Dalvik");
		mOthersPssSeries = new TimeSeries("Others");
		mNativePssSeries = new TimeSeries("Native");
		mTotalPssRendererSeries = new XYSeriesRenderer();
		mDalvikPssRendererSeries = new XYSeriesRenderer();
		mOthersPssRendererSeries = new XYSeriesRenderer();
		mNativePssRendererSeries = new XYSeriesRenderer();

		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.BLACK);
		renderer.setXLabels(6);
		renderer.setYLabels(10);
		renderer.setShowGrid(true);
		renderer.setLabelsTextSize(renderer.getLabelsTextSize() * 2);
		renderer.setAxisTitleTextSize(renderer.getAxisTitleTextSize() * 2);
		renderer.setChartTitleTextSize(renderer.getChartTitleTextSize() * 2);
		renderer.setYLabelsPadding(-10.0f);
		renderer.setPointSize(renderer.getPointSize() * 2);
		renderer.setLegendTextSize(renderer.getLegendTextSize() * 2);

		int[] margin = { 0, 60, 50, 0 };
		// Returns the margin sizes. An array containing the margins in this
		// order: top, left, bottom, right
		renderer.setMargins(margin);

		renderer.addSeriesRenderer(mTotalPssRendererSeries);
		mTotalPssRendererSeries.setFillPoints(true);
		mTotalPssRendererSeries.setPointStyle(PointStyle.CIRCLE);
		mTotalPssRendererSeries.setColor(Color.GREEN);

		renderer.addSeriesRenderer(mDalvikPssRendererSeries);
		mDalvikPssRendererSeries.setFillPoints(true);
		mDalvikPssRendererSeries.setPointStyle(PointStyle.DIAMOND);
		mDalvikPssRendererSeries.setColor(Color.RED);

		renderer.addSeriesRenderer(mOthersPssRendererSeries);
		mOthersPssRendererSeries.setFillPoints(true);
		mOthersPssRendererSeries.setPointStyle(PointStyle.SQUARE);
		mOthersPssRendererSeries.setColor(Color.YELLOW);

		renderer.addSeriesRenderer(mNativePssRendererSeries);
		mNativePssRendererSeries.setFillPoints(true);
		mNativePssRendererSeries.setPointStyle(PointStyle.TRIANGLE);
		mNativePssRendererSeries.setColor(Color.CYAN);
	}

	private void initView(Intent intent) {
		try {
			mPackageName = intent.getExtras().getString("package");
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			return;
		}

		// mPopupView = new TextView(this); // 뷰 생성
		// mPopupView.setText(mPackageName); // 텍스트 설정
		// mPopupView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // 텍스트 크기
		// 18sp
		// mPopupView.setTextColor(Color.BLUE); // 글자 색상
		// mPopupView.setBackgroundColor(Color.argb(127, 0, 255, 255)); // 텍스트뷰
		// 배경
		// 색
		// 최상위 윈도우에 넣기 위한 설정
		mParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,// 항상 최 상위. 터치 이벤트 받을 수 있음. 
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // 포커스를 가지지 않음
				PixelFormat.TRANSLUCENT);

		mParams.gravity = Gravity.LEFT | Gravity.TOP; // 왼쪽 상단에 위치하게 함.
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mAot = inflater.inflate(R.layout.concent_chart, null);

		mTitleText = ((TextView) mAot.findViewById(R.id.package_name));
		// mPackageIcn = ((ImageView) mAot.findViewById(R.id.package_icn));

		Point pt = new Point();
		wm.getDefaultDisplay().getSize(pt);
		mScreenHeight = pt.y;
		mScreenWidth = pt.x;

		mParams.height = mScreenHeight / 2;
		mParams.width = mScreenWidth / 3 * 2;
		// makeButton();
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE); // 윈도우
																			// 매니저
		mTitleText.setText(mPackageName);
		mLayout = (LinearLayout) mAot.findViewById(R.id.aot_linear);

		mLayout.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == false) {
					mLayout.setAlpha((float) 0.5);
				} else {
					mLayout.setAlpha((float) 1);
				}
			}
		});

		mWindowManager.addView(mAot, mParams);
		// mWindowManager.addView(mPopupView, mParams); // 윈도우에 뷰 넣기. permission
		// 필요.
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

	}

	@Override
	public void onDestroy() {
		if (mWindowManager != null) {
			if (mLayout != null) {
				mLayout.removeAllViews();
			}
			if (mAot != null)
				mWindowManager.removeView(mAot);
		}
		if (mHdler != null) {
			mHdler = null;
		}

		super.onDestroy();
	}

	private boolean isPortrait() {
		final Configuration config = this.getResources().getConfiguration();
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			return true;
		}
		return false;
	}

	private float getMovePrePositionX() {
		if (isPortrait() == true) {
			return mPortraitFloatingMovePreX;
		}
		return mLandscapeFloatingMovePreX;
	}

	private float getMovePrePositionY() {
		if (isPortrait() == true) {
			return mPortraitFloatingMovePreY;
		}
		return mLandscapeFloatingMovePreY;
	}

	private void setMovePrePositionX(float x) {
		if (isPortrait() == true) {
			mPortraitFloatingMovePreX = x;
		} else {
			mLandscapeFloatingMovePreX = x;
		}
	}

	private void setMovePrePositionY(float y) {
		if (isPortrait() == true) {
			mPortraitFloatingMovePreY = y;
		} else {
			mLandscapeFloatingMovePreY = y;
		}
	}

	private float getPositionX() {
		if (isPortrait() == true) {
			return mPortraitFloatingPosition.x;
		}
		return mLandscapeFloatingPosition.x;
	}

	private float getPositionY() {
		if (isPortrait() == true) {
			return mPortraitFloatingPosition.y;
		}
		return mLandscapeFloatingPosition.y;
	}

	private void setMoveLastPositionX(float x) {
		if (isPortrait() == true) {
			mPortraitFloatingMoveLastX = x;
		} else {
			mLandscapeFloatingMoveLastX = x;
		}
	}

	private void setMoveLastPositionY(float y) {
		if (isPortrait() == true) {
			mPortraitFloatingMoveLastY = y;
		} else {
			mLandscapeFloatingMoveLastY = y;
		}
	}

	public float getMoveLastPositionX() {
		if (isPortrait() == true) {
			return mPortraitFloatingMoveLastX;
		}
		return mLandscapeFloatingMoveLastX;
	}

	public float getMoveLastPositionY() {
		if (isPortrait() == true) {
			return mPortraitFloatingMoveLastY;
		}
		return mLandscapeFloatingMoveLastY;
	}

	private void setPositionX(float x) {
		if (isPortrait() == true) {
			mPortraitFloatingPosition.x = x;
		} else {
			mLandscapeFloatingPosition.x = x;
		}
	}

	private void setPositionY(float y) {
		if (isPortrait() == true) {
			mPortraitFloatingPosition.y = y;
		} else {
			mLandscapeFloatingPosition.y = y;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		Point pt = new Point();
		wm.getDefaultDisplay().getSize(pt);
		mScreenHeight = pt.y;
		mScreenWidth = pt.x;
		super.onConfigurationChanged(newConfig);
	}

	private OnTouchListener mViewTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: // 사용자 터치 다운이면
				setMovePrePositionX(event.getRawX());
				setMovePrePositionY(event.getRawY());
				break;
			case MotionEvent.ACTION_MOVE:
				float gapX = getMovePrePositionX() - event.getRawX();
				float gapY = getMovePrePositionY() - event.getRawY();
				int moveX = (int) (((mScreenWidth - mChartView.getWidth()) * getPositionX()) - gapX);
				int moveY = (int) (((mScreenHeight - mChartView.getHeight()) * getPositionY()) - gapY);
				if (moveX <= 0) {
					moveX = 0;
				} else if (moveX >= (mScreenWidth - mChartView.getWidth())) {
					moveX = (mScreenWidth - mChartView.getWidth());
				}
				if (moveY < 0) {
					moveY = 0;
				} else if (moveY >= mScreenHeight - mChartView.getHeight()
						- mStatusBarHeight) {
					moveY = mScreenHeight - mChartView.getHeight()
							- mStatusBarHeight;
				}
				setMoveLastPositionX(moveX < 0 ? 0 : moveX);
				setMoveLastPositionY(moveY);
				// mChartView.update(moveX, moveY, mChartView.getWidth(),
				// mChartView.getHeight());
				mParams.x = (int) getMoveLastPositionX();
				mParams.y = (int) getMoveLastPositionY();
				mWindowManager.updateViewLayout(mAot, mParams);
				break;
			case MotionEvent.ACTION_UP:
				setPositionX(getMoveLastPositionX()
						/ (mScreenWidth - mChartView.getWidth()));
				setPositionY(getMoveLastPositionY()
						/ (mScreenHeight - mChartView.getHeight()));
				if (getPositionX() < 0) {
					setPositionX(0);
				} else if (getPositionX() >= 1) {
					setPositionX(1);
				}
				if (getPositionY() < 0) {
					setPositionY(0);
				} else if (getPositionY() >= 1) {
					setPositionY(1);
				}
				// mIsMoving = false; // DS5-IME-P15981,RM#1510 Floating Move
				break;
			}
			return true;
		}
	};

	List<Date[]> x = new ArrayList<Date[]>();
	List<double[]> values = new ArrayList<double[]>();
	Date[] xlists = new Date[MAX_ROW_COUNT];
	double[] valueLists = new double[MAX_ROW_COUNT];
	double[] DalvikLists = new double[MAX_ROW_COUNT];
	double[] OtherLists = new double[MAX_ROW_COUNT];
	double[] NativeLists = new double[MAX_ROW_COUNT];
	double[] xBoundary = new double[2];
	double[] yBoundary = new double[2];

	class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			updateView();
			if (mHdler != null)
				mHdler.sendMessageDelayed(new Message(), FOCUSING_TEST_INTERVER);
		}

		private void updateView() {
			ActivityManager activityManager = (ActivityManager) mContext
					.getSystemService(ACTIVITY_SERVICE);
			MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
			activityManager.getMemoryInfo(memoryInfo);

			List<RunningAppProcessInfo> runningAppProcesses = activityManager
					.getRunningAppProcesses();

			android.os.Debug.MemoryInfo[] memoryInfoArray = null;

			for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
				if (runningAppProcessInfo.processName.equals(mPackageName)) {
					int pids[] = new int[1];
					pids[0] = runningAppProcessInfo.pid;
					memoryInfoArray = activityManager
							.getProcessMemoryInfo(pids);
					MemInfoLog.I(mPackageName + " memoryInfo.TotalPss "
							+ (memoryInfoArray[0].getTotalPss()) + " kB");
				}
			}

			if (memoryInfoArray == null) {
				Toast.makeText(mContext, R.string.err_not_running,
						Toast.LENGTH_SHORT).show();
				if (mHdler != null)
					mHdler = null;
				return;
			}

			Long time = System.currentTimeMillis();

			String[] titles = new String[] { "Total", "Dalvik", "Other Device",
					"Native" };

			for (int index = 0; index < MAX_ROW_COUNT - 1; index++) {
				if (xlists[index + 1] == null) {
					xlists[index + 1] = new Date(time);
				}
				xlists[index] = xlists[index + 1];
				DalvikLists[index] = DalvikLists[index + 1];
				OtherLists[index] = OtherLists[index + 1];
				NativeLists[index] = NativeLists[index + 1];
				valueLists[index] = valueLists[index + 1];
			}

			xlists[MAX_ROW_COUNT - 1] = new Date(time);
			DalvikLists[MAX_ROW_COUNT - 1] = memoryInfoArray[0].dalvikPss;
			OtherLists[MAX_ROW_COUNT - 1] = memoryInfoArray[0].otherPss;
			NativeLists[MAX_ROW_COUNT - 1] = memoryInfoArray[0].nativePss;
			valueLists[MAX_ROW_COUNT - 1] = memoryInfoArray[0].getTotalPss();

			x.clear();
			values.clear();

			for (int i = 0; i < titles.length; i++) {
				x.add(xlists);
			}
			values.add(valueLists);
			values.add(DalvikLists);
			values.add(OtherLists);
			values.add(NativeLists);

			int length = renderer.getSeriesRendererCount();

			for (int i = 0; i < length; i++) {
				((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
						.setFillPoints(true);
			}

			yBoundary = MemInfoUtils.getMostSmallBigValue(valueLists);
			double y1 = 0;
			double y2 = yBoundary[MemInfoUtils.MAX_VALUE_INDEX]
					+ yBoundary[MemInfoUtils.MAX_VALUE_INDEX] / 10;

			// setChartSettings(renderer, mPackageName, "Time", "Memory (kB)",
			setChartSettings(renderer, "", "Time", "Memory (kB)",
					x.get(0)[0].getTime(),
					x.get(0)[xlists.length - 1].getTime(), y1, y2,
					Color.LTGRAY, Color.LTGRAY);
			Date date = new Date(time);

			mTotalPssSeries.add(date, memoryInfoArray[0].getTotalPss());
			mDalvikPssSeries.add(date, memoryInfoArray[0].dalvikPss);
			mOthersPssSeries.add(date, memoryInfoArray[0].otherPss);
			mNativePssSeries.add(date, memoryInfoArray[0].nativePss);
			if (mChartView != null) {
				mChartView.invalidate();
			} else {
				mDataset.addSeries(mTotalPssSeries);
				mDataset.addSeries(mDalvikPssSeries);
				mDataset.addSeries(mOthersPssSeries);
				mDataset.addSeries(mNativePssSeries);

				mChartView = ChartFactory.getTimeChartView(mContext, mDataset,
						renderer, "h:mm:ss");
				// mWindowManager.addView(mChartView, mParams);
				mLayout.addView(mChartView);
				mLayout.setOnTouchListener(mViewTouchListener);
				mChartView.setOnTouchListener(mViewTouchListener);
			}
		}
	}

	public void setRepeat() {
		mHdler = new MyHandler();
		mHdler.sendMessageDelayed(new Message(), FOCUSING_TEST_INTERVER);
	}

	protected XYMultipleSeriesDataset buildDataset(String[] titles,
			List<double[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		addXYSeries(dataset, titles, xValues, yValues, 0);
		return dataset;
	}

	public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles,
			List<double[]> xValues, List<double[]> yValues, int scale) {
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			XYSeries series = new XYSeries(titles[i], scale);
			double[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
	}

	/**
	 * Builds an XY multiple series renderer.
	 * 
	 * @param colors
	 *            the series rendering colors
	 * @param styles
	 *            the series point styles
	 * @return the XY multiple series renderers
	 */
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, colors, styles);
		return renderer;
	}

	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors,
			PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}
	}

	/**
	 * Sets a few of the series renderer settings.
	 * 
	 * @param renderer
	 *            the renderer to set the properties to
	 * @param title
	 *            the chart title
	 * @param xTitle
	 *            the title for the X axis
	 * @param yTitle
	 *            the title for the Y axis
	 * @param xMin
	 *            the minimum value on the X axis
	 * @param xMax
	 *            the maximum value on the X axis
	 * @param yMin
	 *            the minimum value on the Y axis
	 * @param yMax
	 *            the maximum value on the Y axis
	 * @param axesColor
	 *            the axes color
	 * @param labelsColor
	 *            the labels color
	 */
	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	/**
	 * Builds an XY multiple time dataset using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param xValues
	 *            the values for the X axis
	 * @param yValues
	 *            the values for the Y axis
	 * @return the XY multiple time dataset
	 */
	protected XYMultipleSeriesDataset buildDateDataset(String[] titles,
			List<Date[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			TimeSeries series = new TimeSeries(titles[i]);
			Date[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
		return dataset;
	}

	/**
	 * Builds a category series using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param values
	 *            the values
	 * @return the category series
	 */
	protected CategorySeries buildCategoryDataset(String title, double[] values) {
		CategorySeries series = new CategorySeries(title);
		int k = 0;
		for (double value : values) {
			series.add("Project " + ++k, value);
		}

		return series;
	}

	/**
	 * Builds a multiple category series using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param values
	 *            the values
	 * @return the category series
	 */
	protected MultipleCategorySeries buildMultipleCategoryDataset(String title,
			List<String[]> titles, List<double[]> values) {
		MultipleCategorySeries series = new MultipleCategorySeries(title);
		int k = 0;
		for (double[] value : values) {
			series.add(2007 + k + "", titles.get(k), value);
			k++;
		}
		return series;
	}

	/**
	 * Builds a category renderer to use the provided colors.
	 * 
	 * @param colors
	 *            the colors
	 * @return the category renderer
	 */
	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
		DefaultRenderer renderer = new DefaultRenderer();
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setMargins(new int[] { 20, 30, 15, 0 });
		for (int color : colors) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	/**
	 * Builds a bar multiple series dataset using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param values
	 *            the values
	 * @return the XY multiple bar dataset
	 */
	protected XYMultipleSeriesDataset buildBarDataset(String[] titles,
			List<double[]> values) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			CategorySeries series = new CategorySeries(titles[i]);
			double[] v = values.get(i);
			int seriesLength = v.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(v[k]);
			}
			dataset.addSeries(series.toXYSeries());
		}
		return dataset;
	}

	/**
	 * Builds a bar multiple series renderer to use the provided colors.
	 * 
	 * @param colors
	 *            the series renderers colors
	 * @return the bar multiple series renderer
	 */
	protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	public void onClickClose(View v) {
		stopSelf();
	}
}
