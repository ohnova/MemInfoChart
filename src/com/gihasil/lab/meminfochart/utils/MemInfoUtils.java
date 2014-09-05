package com.gihasil.lab.meminfochart.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.gihasil.lab.meminfochart.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class MemInfoUtils
{
	public static final int MAX_VALUE_INDEX = 0;
	public static final int MIN_VALUE_INDEX = 1;
	
	  public static double[] getMostSmallBigValue(double[] lists) {
		  double[] value = new double[2];
		  value[MAX_VALUE_INDEX] = 0;
		  value[MIN_VALUE_INDEX] = 999999999;
		  
		  for (int i = 0; i < lists.length; i++) {
			if (lists[i] > value[MAX_VALUE_INDEX])
				value[MAX_VALUE_INDEX] = lists[i];
			if (lists[i] < value[MIN_VALUE_INDEX])
				value[MIN_VALUE_INDEX] = lists[i];
		}
		  
		  return value;
	  }
	  
	public static String getSystemNewlineString() {
	  return System.getProperty("line.separator");
	}
	
	public static ArrayList<AppInfo> getAllAppPackages(Context context) {
	    ArrayList<AppInfo> apps = getInstalledApps(context, false); /* false = no system packages */
	    final int max = apps.size();
	    for (int i=0; i<max; i++) {
	        apps.get(i).prettyPrint();
	    }
	    return apps;
	}
	
	public static Drawable getIconDrawable(Context context, String packageName)
	{
		PackageInfo pInfo;
		Drawable iconDrawable = context.getResources().getDrawable(R.drawable.ic_launcher);
		try
		{
			pInfo =context.getPackageManager().getPackageInfo(packageName, 0 );
		    iconDrawable = pInfo.applicationInfo.loadIcon(context.getPackageManager());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return iconDrawable;
	}
	public static ArrayList<AppInfo> getInstalledApps(Context context , boolean getSysPackages) {
	    ArrayList<AppInfo> res = new ArrayList<AppInfo>();        
	    List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
	    for(int i=0;i<packs.size();i++) {
	        PackageInfo p = packs.get(i);
	        if ((!getSysPackages) && (p.versionName == null)) {
	            continue ;
	        }
	        String appName     = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
	        String pName        = p.packageName;
	        String versionName =  p.versionName;
	        int     versionCode = p.versionCode;
	        Drawable icon        = p.applicationInfo.loadIcon(context.getPackageManager());
	        
	        AppInfo newInfo = new AppInfo();
	        
	        newInfo.setAppname(appName);
	        newInfo.setPackagename(pName);
	        newInfo.setVersionName(versionName);
	        newInfo.setVersionCode(versionCode);
	        newInfo.setIcon(icon);
	        
	        res.add(newInfo);
	    }
	    return res; 
	}
  
	private static final String[] PROJECTION_NUM_DATA = {
		Data._ID,
		Data.MIMETYPE,
		Data.CONTACT_ID,
		Data.DISPLAY_NAME,
		Phone.NUMBER,
	};
  
  public static String findContactWithName(Context context, String searchText) {
	  MemInfoLog.I("searchText = " + searchText);
	  StringBuffer mBuffer = new StringBuffer();
		mBuffer.append("LENGTH(");
		mBuffer.append(Phone.NUMBER);
		mBuffer.append(")>4 OR ");
		mBuffer.append(Data.DISPLAY_NAME);
		mBuffer.append(" LIKE '%");
		mBuffer.append(searchText);
		mBuffer.append("%'");
		Uri baseUri= Phone.CONTENT_FILTER_URI;
		Uri uriSMS = Uri.withAppendedPath(baseUri, Uri.encode(searchText)); 
		Cursor cursor = context.getContentResolver().query(uriSMS,
			PROJECTION_NUM_DATA,
			mBuffer.toString(),
			null,
			"(CASE WHEN " + Data.DISPLAY_NAME + " LIKE '" + searchText + "%'"  + " THEN 0 ELSE 1 END), " + 
				Data.DISPLAY_NAME + " ASC");
		
		String number = null;
		String name = null;
		if(cursor != null) {
			MemInfoLog.I("cursor != null");
			String szName = null;
			while(cursor.moveToNext()) {
				
				String mimeType = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
				if(mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
					number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
					name = cursor.getString(cursor.getColumnIndex(Data.DISPLAY_NAME));
					break;
				} 
			}
			cursor.close();
		}
		else {
			MemInfoLog.I("cursor == null");
		}
		MemInfoLog.I("number = " + number);
		MemInfoLog.I("name = " + name);
		if(number != null) {
			return number + "@" + name;
		}
		else {
			return null;
		}
  }
  
  public static  String analyzeAlram(String hourToken, String minuteToken, String input){
      input = input.replace("\n", "");
      input = input.replace(" ", "");
      
      String hour = "", minute = "";
      StringTokenizer st = new StringTokenizer(input, hourToken);
      String result = "";
      try {
          hour = st.nextToken();
          minute = st.nextToken();
          //minute = minute.substring(0, minute.length()-1);
      } catch (NoSuchElementException e) {
          return "";
      }
      //Log.e(TAG, "hour : " + hour);
      //Log.e(TAG, "minute : " + minute);
      if(st.hasMoreTokens())
          return "";
      
      if(hour.equals("") || minute.equals(""))
          return "";
      
      if(hour.length()>2 || minute.length()>2)
          return "";
      
      if( !(analyzeTokenNumber(hour) && analyzeTokenNumber(minute)) )
          return "";

      if( Integer.parseInt(hour)<0 || Integer.parseInt(hour) > 23 )
          return "";
      
      if( Integer.parseInt(minute)<0 || Integer.parseInt(minute) > 59 )
          return "";      
                  
      result = hour + ":" + minute;
      //Log.e(TAG, result);
      return result;
  }
  
  public static  String analyzeAlramForAfterTime(int index, String Token, String input){
      input = input.replace("\n", "");
      input = input.replace(" ", "");
      
      String time = "";
      StringTokenizer st = new StringTokenizer(input, Token);
      String result = "";
      try {
          time = st.nextToken();
          //minute = st.nextToken();
          //minute = minute.substring(0, minute.length()-1);
      } catch (NoSuchElementException e) {
          return "";
      }
      //Log.e(TAG, "hour : " + hour);
      //Log.e(TAG, "minute : " + minute);
      if(st.hasMoreTokens()) {
          return "";
      }
      
      if(time.equals("0")) return "";
      
      if(time.equals("")){
          return "";
      }
      
      if(time.length()>2){
          return "";
      }

      if( !(analyzeTokenNumber(time)))
          return "";

      Calendar c = Calendar.getInstance();
      int hour = Integer.parseInt(String.format("%02d",c.get(Calendar.HOUR_OF_DAY)));
      int minute = Integer.parseInt(String.format("%02d",c.get(Calendar.MINUTE)));
      
      if(index==0) {  // ** hours after
          if( Integer.parseInt(time)<0 || Integer.parseInt(time) > 23 ) return "";
          hour = Integer.parseInt(time) + hour;
          if(hour < 0 || hour > 23) hour = hour - 24;

      }
      else if(index==1) { // ** minutes after
          if( Integer.parseInt(time)<0 || Integer.parseInt(time) > 59 ) return ""; 
          minute = Integer.parseInt(time) + minute;
          if(minute < 0 || minute > 59) {
              hour = hour + 1;
              minute = minute - 60;
              if(hour < 0 || hour > 23) hour = hour - 24;
          }
      }
      
      result = "" + hour + ":" + minute;
      //Log.e(TAG, result);
      return result;
  }
  
  public static  boolean analyzeTokenNumber(String input) {
      int i=0;
      byte[] bytes = input.getBytes();
      for(i=0; i<bytes.length; i++){
          if(!((bytes[i] > 0x2F) && (bytes[i] < 0x3A)))
              return false;
      }
      return true;
  }
}