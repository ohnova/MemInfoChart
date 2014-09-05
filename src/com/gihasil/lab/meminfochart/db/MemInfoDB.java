package com.gihasil.lab.meminfochart.db;

import com.gihasil.lab.meminfochart.utils.MemInfoLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class MemInfoDB extends SQLiteOpenHelper {	
  
  private static final String TAG = "MemInfoChart";

  public static final String DB_APP_NAME = "meminfochart";
  public static final String DB_AUTHORITY = "com.gihasil.lab.meminfochart";
  public static final Uri DB_CONTENT_URI = Uri.parse("content://" + DB_AUTHORITY + "/"
      + DB_APP_NAME);
  public static final String DB_NAME = "meminfochart.db";
  public static final String DEFAULT_SORT_ORDER = "_id DESC";
  public static final int DB_VERSION = 2;
  public static final String DB_TABLE = DB_APP_NAME + "_table";

  public static final String DB_COLUMN_ID = "_id";
  public static final String DB_COLUMN_PACKAGENAME = "packagename";
  public static final String DB_COLUMN_PSSMEM = "pssmem";
  public static final String DB_COLUMN_TIMESTAMP = "timestamp";
  public static final String DB_COLUMN_NATIVE = "native";
  public static final String DB_COLUMN_DALVIK = "dalvik";
  public static final String DB_COLUMN_OTHERDEV = "otherdev";

  public MemInfoDB(Context context) {
    super(context, DB_APP_NAME, null, DB_VERSION);
    MemInfoLog.setLogTag(TAG);
  }

  public static final String DB_CREATE = "CREATE TABLE " + DB_TABLE + " (" + DB_COLUMN_ID
      + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " + DB_COLUMN_PACKAGENAME + " NEXT NOT NULL" + ", "
      + DB_COLUMN_PSSMEM + " LONG" + ", " + DB_COLUMN_TIMESTAMP + " LONG" + ", "
      + DB_COLUMN_DALVIK + " LONG" + ", " + DB_COLUMN_OTHERDEV + " LONG" + ", "
      + DB_COLUMN_NATIVE + " LONG" + ");";

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(DB_CREATE);
    //initInsert(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
    onCreate(db);
  }

}
