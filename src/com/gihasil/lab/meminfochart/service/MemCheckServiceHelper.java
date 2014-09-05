package com.gihasil.lab.meminfochart.service;

import java.util.Calendar;

import com.gihasil.lab.meminfochart.activity.MainActivity;
import com.gihasil.lab.meminfochart.db.MemInfoDBHandler;
import com.gihasil.lab.meminfochart.utils.AppInfo;
import com.gihasil.lab.meminfochart.utils.MemInfoLog;
import com.gihasil.lab.meminfochart.utils.SharedPreferencesUtils;
import com.gihasil.lab.meminfochart.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class MemCheckServiceHelper {
	private Context mContext = null;

	private static final int THIRTY_SEC = 0;
	private static final int ONE_MIN = 1;
	private static final int TWO_MIN = 2;
	private static final int FIVE_MIN = 3;
	private static final int TEN_MIN = 4;

	public static int mInterval;
	private AlarmManager mAlarms;
	private PendingIntent mPi;

	private SharedPreferencesUtils mPrefUtils = null;
	private OnSharedPreferenceChangeListener mListener;

	public MemCheckServiceHelper(Context c) {
		mContext = c;

//		Intent intent = new Intent(mContext, AccumMemoryService.class);
//		mPi = PendingIntent.getService(mContext,
//				MainActivity.GATHERING_ALARM_CODE, intent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
		
		
		Intent intent = new Intent(mContext, MemInfoBroadcastReceiver.class);
		mPi = PendingIntent.getBroadcast(mContext,
				MainActivity.GATHERING_ALARM_CODE, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		mAlarms = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		
		if (needToUpdate(mContext)) {
			mContext.sendBroadcast(intent);
		}
		
		initPref();
		
		if (mPrefUtils.getValue(AppInfo.PREF_SERVICE_START, false)) {
			if (NotificationLoader.isNotificationVisible(mContext) == false) {
				NotificationLoader.launchNotification(mContext);
			}
		}
	}

	public void initPref() {
		final SharedPreferences mySharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		loadPref(mySharedPreferences);

		mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if ("list_preference".equals(key)) {
					loadPref(mySharedPreferences);
					stopMemCheckService();
					startMemCheckService();
				}
			}
		};

		mySharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		mPrefUtils = new SharedPreferencesUtils(mContext);
	}

	private void loadPref(final SharedPreferences mySharedPreferences) {
		String interval[] = mContext.getResources().getStringArray(
				R.array.entryvalues_interval_preference);
		String interval_preferences = mySharedPreferences.getString(
				"list_preference", "five_min");
		for (int i = 0; i < interval.length; i++) {
			if (interval_preferences.equals(interval[i])) {
				switch (i) {
				case THIRTY_SEC:
					mInterval = 30 * 1000;
					break;
				case ONE_MIN:
					mInterval = 60 * 1000;
					break;
				case TWO_MIN:
					mInterval = 2 * 60 * 1000;
					break;
				case FIVE_MIN:
					mInterval = 5 * 60 * 1000;
					break;
				case TEN_MIN:
					mInterval = 10 * 60 * 1000;
					break;
				default:
					mInterval = 60 * 1000;
					break;
				}
			}
		}
	}

	public void startMemCheckService(boolean bForced) {
		if ((mPrefUtils.getValue(AppInfo.PREF_SERVICE_START, false) == false) ||
				needToUpdate(mContext)) {	
			startMemService();
		}
	}	
	
	public boolean startMemCheckService() {	
		if (mPrefUtils.getValue(AppInfo.PREF_SERVICE_START, false)) {
			return false;
		}

		Calendar cal = Calendar.getInstance();
		mAlarms.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				mInterval, mPi);			
		
		//mAlarms.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), mInterval, mPi);
		
		startMemService();
		
		return true;
	}

	private void startMemService() {
//		Intent intent = new Intent(mContext, AccumMemoryService.class);
//		intent.putExtra(AppInfo.INTERVAL_TIME, (long)mInterval);
//		mContext.startService(intent);
		
		MemInfoLog.D("mInterval : " + mInterval);
		
		String description = null;
		if (mInterval == 30000) {
			description = mContext.getString(R.string.start_service, "0.5");
		} else {
			description = mContext.getString(R.string.start_service,
					Integer.toString(mInterval / 60000));
		}
		mPrefUtils.put(AppInfo.PREF_SERVICE_START, true);	
		NotificationLoader.launchNotification(mContext);
		Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show();
	}

	public boolean stopMemCheckService() {
		if (mPrefUtils.getValue(AppInfo.PREF_SERVICE_START, false)) {
			mPrefUtils.put(AppInfo.PREF_SERVICE_START, false);			
			
//			Intent intent = new Intent(mContext, AccumMemoryService.class);
//			mContext.stopService(intent);

			mAlarms.cancel(mPi);
			NotificationLoader.eraseNotification(mContext);
			
			MemInfoLog.D("");
			return true;
		} else {
			return false;
		}
	}	
	
	public static boolean needToUpdate(Context context) {
		return (MemInfoDBHandler.open(context).dbSelectPackageCusor(context.getPackageName()).getCount() < 2);
	}
}
