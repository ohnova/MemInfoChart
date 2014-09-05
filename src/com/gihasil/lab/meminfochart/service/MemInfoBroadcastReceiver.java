package com.gihasil.lab.meminfochart.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gihasil.lab.meminfochart.activity.HistoryViewActivity;
import com.gihasil.lab.meminfochart.db.MemInfoDBHandler;
import com.gihasil.lab.meminfochart.utils.AppInfo;
import com.gihasil.lab.meminfochart.utils.MemInfoLog;
import com.gihasil.lab.meminfochart.utils.SharedPreferencesUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.PowerManager;

public class MemInfoBroadcastReceiver extends BroadcastReceiver {
	private MemInfoDBHandler mDbHandler;
	private Context mContext;
	private Cursor mCursor = null;
		
	static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mStartingService;
    
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		mContext = context;
		//Do nothing
		if ("com.gihasil.lab.meminfochart.version".equals(action)) {
			Intent i = new Intent(context.getApplicationContext(), HistoryViewActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			context.startActivity(i);
		} else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
			SharedPreferencesUtils pref = new SharedPreferencesUtils(context);
			if (pref.getValue(AppInfo.PREF_SERVICE_START, false)) {
				MemCheckServiceHelper helper = new MemCheckServiceHelper(context);
				helper.startMemCheckService();
			}
		} else {
			mDbHandler = MemInfoDBHandler.open(mContext);

			new InsertDataTask().execute();
		}
	}	
	
	private class InsertDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			synchronized (mStartingServiceSync) {
				if (mStartingService == null) {
					PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
					mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MemInfoChart WakeLock");
					mStartingService.setReferenceCounted(false);
				}
				mStartingService.acquire();
			}
			
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
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
			
			if (rowCounts > AccumMemoryService.MAX_RECORD_NUMBER) {
				MemInfoLog.D("rowCounts : " + rowCounts);
				mDbHandler.deleteOldRecord(AccumMemoryService.MAX_RECORD_NUMBER);
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {	
			MemInfoLog.D("Fire!!");
			mContext.sendBroadcast(new Intent(AccumMemoryService.START_LIST_VIEW_INTENT));
			super.onPostExecute(result);
	        synchronized (mStartingServiceSync) {
	            if (mStartingService != null) {
	                    mStartingService.release();
	            }
	        }
		}
		
	}
}
