package com.gihasil.lab.meminfochart.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import com.gihasil.lab.meminfochart.activity.MainActivity;
import com.gihasil.lab.meminfochart.db.MemInfoDBHandler;
import com.gihasil.lab.meminfochart.utils.AppInfo;
import com.gihasil.lab.meminfochart.utils.MemInfoLog;
import com.gihasil.lab.meminfochart.utils.SharedPreferencesUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class AccumMemoryService extends Service {
	//private static final long HOUR = 3600 * 1000;
	//private static final long DAY = HOUR * 24;

	public static final String START_LIST_VIEW_INTENT = "com.gihasil.lab.meminfochart.startlist";
	public static final int MAX_RECORD_NUMBER = 240000;
	
	private static final int SERVICE_ID = 0x3245;
	
	private MemInfoDBHandler mDbHandler;
	private Context mContext;
	private String mPackageName = null;
	private Cursor mCursor = null;
//	private static Timer mTimer = null;
//	private long mInterval = 0;
	
	private SharedPreferencesUtils mPrefUtils = null;
	
	@Override
	public void onCreate() {
		MemInfoLog.D("");
		mContext = this;

		startForeground(SERVICE_ID, NotificationLoader.launchNotification(mContext));
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		mDbHandler = MemInfoDBHandler.open(mContext);
		if(intent!=null){
			mPackageName  = intent.getStringExtra(MainActivity.INPUT_METHOD_SERVICE);		
			if(mPackageName!=null) makeMonkeyEvent(mPackageName, false);
		}
//		new InsertDataTask().execute();
//		
//		stopSelf();
		
//		TimerTask myTask = new TimerTask() {			
//			@Override
//			public void run() {
//				new InsertDataTask().execute();				
//			}
//		};
		
//		mInterval = intent.getLongExtra(AppInfo.INTERVAL_TIME, 1000 * 60 * 5); // default 5 min.
//		if (mTimer == null) {
//			mTimer = new Timer();
//		}
//		mTimer.schedule(myTask, 0, mInterval);
//		MemInfoLog.D("delay : " + mInterval);				
		
		mPrefUtils = new SharedPreferencesUtils(mContext);
		mPrefUtils.put(AppInfo.PREF_SERVICE_START, true);
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		MemInfoLog.D("");
		NotificationLoader.eraseNotification(mContext);		
		mPrefUtils.put(AppInfo.PREF_SERVICE_START, false);	
//		mTimer.cancel();
//		mTimer = null;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}
	
	private class InsertDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
			MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
			activityManager.getMemoryInfo(memoryInfo);

			MemInfoLog.I(" memoryInfo.availMem " + (memoryInfo.availMem/1048576L)+" MB");
			MemInfoLog.I(" memoryInfo.lowMemory " + memoryInfo.lowMemory);
			MemInfoLog.I(" memoryInfo.threshold " + (memoryInfo.threshold/1048576L)+" MB");
			MemInfoLog.I(" memory limit per process " + 
			((ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass()/1024+" kB");
			MemInfoLog.I(" Dalvik heap max memory " + Runtime.getRuntime().maxMemory()/1048576L+" MB");
			
			List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();

			Map<Integer, String> pidMap = new TreeMap<Integer, String>();
			
			for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses)
			{
			    pidMap.put(runningAppProcessInfo.pid, runningAppProcessInfo.processName);
			}

			Collection<Integer> keys = pidMap.keySet();
						
			long now = System.currentTimeMillis();//Math.round(new Date().getTime() / DAY) * DAY;
			
			for(int key : keys)
			{
			    int pids[] = new int[1];
			    pids[0] = key;
			    android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
			    for(android.os.Debug.MemoryInfo pidMemoryInfo: memoryInfoArray)
			    {
			    	mDbHandler.insert(mContext, pidMap.get(pids[0]), pidMemoryInfo.getTotalPss(), now, 
			    			pidMemoryInfo.dalvikPss, pidMemoryInfo.otherPss, pidMemoryInfo.nativePss);
//			    	MemLeakLog.I(String.format("** MEMINFO in pid %d [%s] **",pids[0],pidMap.get(pids[0])));
//			    	MemLeakLog.I(" pidMemoryInfo.getTotalPrivateDirty: " + pidMemoryInfo.getTotalPrivateDirty());
//			    	MemLeakLog.I(" pidMemoryInfo.getTotalPss: " + pidMemoryInfo.getTotalPss());
//			    	MemLeakLog.I(" pidMemoryInfo.getTotalSharedDirty: " + pidMemoryInfo.getTotalSharedDirty());
			    }
			}
			
			mCursor = mDbHandler.dbAllCusor();
			int rowCounts = mCursor.getCount();
			
			if (rowCounts > MAX_RECORD_NUMBER) {
				MemInfoLog.D("rowCounts : " + rowCounts);
				mDbHandler.deleteOldRecord(MAX_RECORD_NUMBER);
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {	
			mContext.sendBroadcast(new Intent(START_LIST_VIEW_INTENT));
			super.onPostExecute(result);
		}
		
	}
	
	public static void makeMonkeyEvent(String event, boolean isCustomized){
		// make command with package name (= event) if not customized 
		if(!isCustomized) event = "monkey -p " + event + " -v 1000";
		try {
			Process p = Runtime.getRuntime().exec(event);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
