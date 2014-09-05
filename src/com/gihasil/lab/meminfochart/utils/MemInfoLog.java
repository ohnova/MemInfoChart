package com.gihasil.lab.meminfochart.utils;

import android.util.Log;

public class MemInfoLog
{
	private static final boolean LOG_ON = true;
	private static String TAG = "MemInfoChart";
	private static boolean mCallerDisplay = false;
	
	// display who has called the logging method 
	public static void setCaller(boolean caller_display)
	{
		mCallerDisplay = caller_display;
	}
	
	// set email log tag
	public static void setTag(String tag)
	{
		TAG = tag;
	}
	
	// display current stack trace
	public static void DisplayStackTrace()
	{
		if(Thread.currentThread() == null) return;
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		
		Line();
		for(int i = 0 ; i < stack.length ; i++)
		{
			Log.d(TAG , i + " : " + stack[i].getClassName() + ".class " + stack[4].getMethodName() + "()");
		}
		Line();
	}
	
	private static String GetLoggingLoacation(boolean caller_display)
	{
		String className= "";
		String methodName = "";
		String lineNumber = "";
		String caller ="";
		if(Thread.currentThread() == null) return ""; 
		
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		if(stack.length > 3){
			if(stack[4] !=null)
			{
				className = stack[4].getClassName();
				className = className.substring(className.lastIndexOf(".")+1, className.length());
				className = className + ".";
				methodName = stack[4].getMethodName() + "()";
				lineNumber = "["+String.valueOf(stack[4].getLineNumber()) + "]";
			}
			if(stack.length > 4)
			{
				if(stack[5] !=null && caller_display)
				{
					String callerClassName = stack[5].getClassName();
					callerClassName = callerClassName.substring(callerClassName.lastIndexOf(".")+1, callerClassName.length());
					callerClassName = callerClassName + ".";
					String callerMethodName = stack[5].getMethodName() + "()";
					caller = "__called by__" + callerClassName+ callerMethodName+ "[" + stack[5].getLineNumber() + "]";
				}
			}
		}
		
		
		String position = className + methodName + lineNumber + caller ;
		
		
		
/*		if(DISPLAY_LOGGING_CLASS == false && DISPLAY_LOGGING_METHOD == false)
		{
			position = "";
		}
		else if(DISPLAY_LOGGING_CLASS == true && DISPLAY_LOGGING_METHOD == false)
		{
			position = className;
		}
		else if(DISPLAY_LOGGING_CLASS == false && DISPLAY_LOGGING_METHOD == true)
		{
			position = methodName;
		}
		else */
		// {
			
		// }
		
		
		return position;
	}
	
	public static void E(String msg)
	{
		if( LOG_ON == false ) return;
		Log.e(TAG, GetLoggingLoacation(mCallerDisplay) + " :: " + msg);
	}
	
	public static void EE(String msg)
	{
		Log.e(TAG, GetLoggingLoacation(true) + " :: " + msg);
	}
	
	public static void W(String msg)
	{
		if( LOG_ON == false ) return;
		Log.w(TAG, GetLoggingLoacation(mCallerDisplay) + " :: " + msg);
	}
	
	public static void WW(String msg)
	{
		Log.w(TAG, GetLoggingLoacation(true) + " :: " + msg);
	}

	public static void I(String msg)
	{
		if( LOG_ON == false ) return;
		Log.i(TAG, GetLoggingLoacation(mCallerDisplay) + " :: " + msg);
	}
	
	public static void II(String msg)
	{
		Log.i(TAG, GetLoggingLoacation(true) + " :: " + msg);
	}
	
	public static void D(String msg)
	{
		if( LOG_ON == false ) return;
		Log.d(TAG, GetLoggingLoacation(mCallerDisplay) + " :: " + msg);
	}
		
	public static void DD(String msg)
	{
		Log.d(TAG, GetLoggingLoacation(true) + " :: " + msg);
	}
		
	
	public static void V(String msg)
	{
		if( LOG_ON == false ) return;
		Log.v(TAG, GetLoggingLoacation(mCallerDisplay) + " :: " + msg);
	}
	
	public static void VV(String msg)
	{
		Log.v(TAG, GetLoggingLoacation(true) + " :: " + msg);
	}
	
	public static void C()
	{
		if( LOG_ON == false ) return;
		Log.v(TAG, GetLoggingLoacation(mCallerDisplay) + " :: ");
	}
	
	public static void CC()
	{
		Log.v(TAG, GetLoggingLoacation(true) + " :: ");
	}
	
	public static void setLogTag(String tag)
	{
		TAG = tag;
	}
	
	public static void Line()
	{
		if( LOG_ON == false ) return;
		Log.i(TAG , "-*--DBugApps--*-" );
	}
	
	public static void M(String msg)
	{
		if( LOG_ON == false ) return;
		Log.i(TAG, msg);
	}
	
	
}