package com.gihasil.lab.meminfochart.utils;

import android.graphics.drawable.Drawable;

public class AppInfo {
	
	   private String appname = "";
	    private String mPackageName = "";
	    private String versionName = "";
	    private int versionCode = 0;
	    private Drawable icon;
	    
	    public static final String PREF_SERVICE_START = "service_started"; 
	    public static final String INTERVAL_TIME = "interval_time";
	    
		public void prettyPrint() {
	    	MemInfoLog.D(appname + "\t" + mPackageName + "\t" + versionName + "\t" + versionCode);
	    }
	    
	    public String getAppname() {
			return appname;
		}


		public void setAppname(String appname) {
			this.appname = appname;
		}


		public String getPackagename() {
			return mPackageName;
		}


		public void setPackagename(String pname) {
			this.mPackageName = pname;
		}


		public String getVersionName() {
			return versionName;
		}


		public void setVersionName(String versionName) {
			this.versionName = versionName;
		}


		public int getVersionCode() {
			return versionCode;
		}


		public void setVersionCode(int versionCode) {
			this.versionCode = versionCode;
		}


		public Drawable getIcon() {
			return icon;
		}


		public void setIcon(Drawable icon) {
			this.icon = icon;
		}

}
