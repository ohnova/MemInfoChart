package com.gihasil.lab.meminfochart.service;

import com.gihasil.lab.meminfochart.activity.MainActivity;
import com.gihasil.lab.meminfochart.utils.MemInfoLog;
import com.gihasil.lab.meminfochart.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationLoader {	
	private static final int NOTIFI_ID = 0x473;
	
	public NotificationLoader() {
		//mContext = c;
		//bmBigPicture = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
	}

	public static Notification launchNotification(Context c) {
		NotificationManager nm;
		
		nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		
		PendingIntent intent = PendingIntent.getActivity(c,0,
				new Intent(c, MainActivity.class), Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		Notification notification =
                new Notification.Builder(c)
                .setContentTitle(c.getResources().getString(R.string.app_name))
                .setContentText("memory is being collected")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(intent)
                .build();
                
		nm.notify(NOTIFI_ID, notification);
		MemInfoLog.D(""); 
		
		return notification;
	}
	
	public static void eraseNotification(Context c) {
		MemInfoLog.D("");
		NotificationManager nm;
		nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFI_ID);
	}
	
	public static boolean isNotificationVisible(Context c) {
	    Intent notificationIntent = new Intent(c, MainActivity.class);
	    PendingIntent test = PendingIntent.getActivity(c, NOTIFI_ID, notificationIntent, PendingIntent.FLAG_NO_CREATE);
	    return test != null;
	}
}
