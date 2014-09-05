package com.gihasil.lab.meminfochart.db;

import com.gihasil.lab.meminfochart.utils.MemInfoLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MemInfoDBHandler {
	public static final String TAG = "DBHandler";

	private MemInfoDB mMemLeakDBhelper;
	private SQLiteDatabase db;
	
	public static final String DB_APP_NAME 			= MemInfoDB.DB_APP_NAME;
	public static final String DB_AUTHORITY 			= MemInfoDB.DB_AUTHORITY;
	public static final Uri DB_CONTENT_URI 			= MemInfoDB.DB_CONTENT_URI;
	public static final String DB_NAME 				= MemInfoDB.DB_NAME;
	public static final String DEFAULT_SORT_ORDER		= MemInfoDB.DEFAULT_SORT_ORDER;
	public static final int DB_VERSION 				= MemInfoDB.DB_VERSION;
	public static final String DB_TABLE	 			= MemInfoDB.DB_TABLE;

	public static final String DB_COLUMN_ID 			= MemInfoDB.DB_COLUMN_ID;
	public static final String DB_COLUMN_PACKAGENAME 	= MemInfoDB.DB_COLUMN_PACKAGENAME;
	public static final String DB_COLUMN_PSSMEM   	 	= MemInfoDB.DB_COLUMN_PSSMEM;
	public static final String DB_COLUMN_TIMESTAMP 	= MemInfoDB.DB_COLUMN_TIMESTAMP;
	public static final String DB_COLUMN_NATIVE 		= MemInfoDB.DB_COLUMN_NATIVE;
	public static final String DB_COLUMN_DALVIK 		= MemInfoDB.DB_COLUMN_DALVIK;
	public static final String DB_COLUMN_OTHERDEV 		= MemInfoDB.DB_COLUMN_OTHERDEV;
	
	private static final String[] from = new String[] { DB_COLUMN_ID, DB_COLUMN_PACKAGENAME, DB_COLUMN_PSSMEM, DB_COLUMN_TIMESTAMP, 
			DB_COLUMN_DALVIK, DB_COLUMN_OTHERDEV, DB_COLUMN_NATIVE};
	//private final String[] packageOnly = new String[] { DB_COLUMN_ID, DB_COLUMN_PACKAGENAME};
	
    private MemInfoDBHandler(Context context) {
    	MemInfoLog.setLogTag(TAG);
    	this.mMemLeakDBhelper =  new MemInfoDB(context);
    	this.db = mMemLeakDBhelper.getWritableDatabase();
    }

    public static MemInfoDBHandler open(Context context) throws SQLException {
        return new MemInfoDBHandler(context);        
	}

	public void close() {
		mMemLeakDBhelper.close();
	}

	public long insert(Context context, String pakagename, long pssmem, long timestamp, long dalvik, long otherdev, long nativ) {
		//MemLeakLog.V("insert()\n pakagename :" + pakagename+"\n pssmem :"+pssmem+"\n timestamp :"+timestamp);		
		ContentValues values = new ContentValues();
		values.put(DB_COLUMN_PACKAGENAME, pakagename);
		values.put(DB_COLUMN_PSSMEM, pssmem);
		values.put(DB_COLUMN_TIMESTAMP, timestamp);
		values.put(DB_COLUMN_DALVIK, dalvik);
		values.put(DB_COLUMN_OTHERDEV, otherdev);
		values.put(DB_COLUMN_NATIVE, nativ);

		return db.insert(DB_TABLE, null, values);
	}
	
	//delete All
	public boolean deleteAll() {
		MemInfoLog.V("deleteAll()");
		return db.delete(DB_TABLE, null, null) == 1;
	}
	
	public boolean delete(long rowID) {
		MemInfoLog.V("delete() rowID :"+rowID);
		return db.delete(DB_TABLE, DB_COLUMN_ID + "=" + rowID, null) > 0;
	}

	public void deleteOldRecord(int limit) {
		MemInfoLog.V("deleteOldRecord()");
		// DELETE FROM mytable WHERE ROWID IN (SELECT ROWID FROM mytable ORDER BY ROWID DESC LIMIT -1 OFFSET limit)
		String rawQuery = "DELETE FROM "+DB_TABLE+ " WHERE "+DB_COLUMN_ID+
				" IN (SELECT "
				+DB_COLUMN_ID+
				" FROM "
				+DB_TABLE+
				" ORDER BY "
				+DB_COLUMN_ID+
				" DESC LIMIT -1 OFFSET "
				+limit+")";
		db.execSQL(rawQuery);
	}
//	public boolean update(Context context, long id, int group, int type, String voice, String content) {
//		MemLeakLog.V("update()\n id :" + id + "\n group :" + group+"\n voice :"+voice+"\n content :"+content);
//		
//		ContentValues values = new ContentValues();
//		values.put(DB_COLUMN_ID, id);
//		values.put(DB_COLUMN_GROUP, group);
//		values.put(DB_COLUMN_TYPE, type);
//		values.put(DB_COLUMN_VOICE, voice);
//		values.put(DB_COLUMN_CONTENT, content);
//		return db.update(DB_TABLE, values, DB_COLUMN_ID + "=" + id, null) > 0;
//	}
	
	public Cursor dbAllCusor() throws SQLException {
		MemInfoLog.V("dbAllCusor()");
		Cursor cursor = db.rawQuery("SELECT * FROM meminfochart_table", null);		
	
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	
	public Cursor dbSelectAllCusor() throws SQLException {
		MemInfoLog.V("dbSelectAllCusor()");
		Cursor cursor = db.rawQuery("SELECT * FROM meminfochart_table GROUP BY packagename ORDER BY pssmem DESC", null);		
	
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	
	public Cursor dbSelectPackageCusor(String packageName) throws SQLException {
		MemInfoLog.V("dbSelectPackageCusor()");
		
		Cursor cursor = db.query(false, DB_TABLE, from , DB_COLUMN_PACKAGENAME + "=?",
				new String[] { packageName }, null, null, null, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
//	public Cursor dbSelectCusor(String order) throws SQLException {
//		MemLeakLog.V("dbSelectGroupCusor()");
//		
//		Cursor cursor = db.query(true, DB_TABLE, from , DB_COLUMN_VOICE + "=?",
//				new String[] { order }, null, null, null, null);
//		
//		if (cursor != null) {
//			cursor.moveToFirst();
//		}
//		return cursor;
//	}
	
//	public Cursor dbSelectCusor(String[] order) throws SQLException {
//		MemLeakLog.V("dbSelectGroupCusor()");
//		
//		Cursor cursor = db.query(true, DB_TABLE, from , DB_COLUMN_VOICE + "=?",
//				 order , null, null, null, null);
//		
//		if (cursor != null) {
//			cursor.moveToFirst();
//		}
//		return cursor;
//	}	
		
//	public ArrayList<MemLeakProfile> dbSelectAllList() throws SQLException {
//		MemLeakLog.V("dbSelectAllList()");
//		
//		ArrayList<MemLeakProfile> selectList = new ArrayList<MemLeakProfile>();
//		
//		Cursor cursor = db.query(true, DB_TABLE, from, null, null, null,null, null, null);
//		if (cursor.moveToFirst()) {
//			do {
//				selectList.add(new MemLeakProfile(
//						cursor.getLong(cursor.getColumnIndex(DB_COLUMN_ID)), 
//						cursor.getString(cursor.getColumnIndex(DB_COLUMN_PACKAGENAME)), 
//						cursor.getLong(cursor.getColumnIndex(DB_COLUMN_PSSMEM)), 
//						cursor.getLong(cursor.getColumnIndex(DB_COLUMN_TIMESTAMP)))); 
////				MemLeakLog.V("id :" + cursor.getLong(cursor.getColumnIndex(DB_COLUMN_ID)) +
////						"\n group :" + cursor.getInt(cursor.getColumnIndex(DB_COLUMN_GROUP))+
////						"\n type :" + cursor.getInt(cursor.getColumnIndex(DB_COLUMN_TYPE))+
////						"\n voice :"+cursor.getString(cursor.getColumnIndex(DB_COLUMN_VOICE))+
////						"\n content :"+cursor.getString(cursor.getColumnIndex(DB_COLUMN_CONTENT)));
//
//			} while (cursor.moveToNext());
//		}
//		cursor.close();
//		return selectList;
//	}
	
//	public ArrayList<MemLeakProfile> dbSelectPackageList(int group) throws SQLException {
//		MemLeakLog.V("dbSelectGroupList()");
//		
//		ArrayList<MemLeakProfile> selectList = new ArrayList<MemLeakProfile>();
//		
//		Cursor cursor = db.query(true, DB_TABLE, from,  DB_COLUMN_GROUP + "=?",
//				new String[] {String.valueOf(group)}, null,null, null, null);
//		if (cursor.moveToFirst()) {
//			do {
//				selectList.add(new MemLeakProfile(
//						cursor.getLong(cursor.getColumnIndex(DB_COLUMN_ID)), 
//						cursor.getInt(cursor.getColumnIndex(DB_COLUMN_GROUP)), 
//						cursor.getInt(cursor.getColumnIndex(DB_COLUMN_TYPE)),
//						cursor.getString(cursor.getColumnIndex(DB_COLUMN_VOICE)), 
//						cursor.getString(cursor.getColumnIndex(DB_COLUMN_CONTENT))));
//				MemLeakLog.V("id :" + cursor.getLong(cursor.getColumnIndex(DB_COLUMN_ID)) +
//						"\n group :" + cursor.getInt(cursor.getColumnIndex(DB_COLUMN_GROUP))+
//						"\n type :" + cursor.getInt(cursor.getColumnIndex(DB_COLUMN_TYPE))+
//						"\n voice :"+cursor.getString(cursor.getColumnIndex(DB_COLUMN_VOICE))+
//						"\n content :"+cursor.getString(cursor.getColumnIndex(DB_COLUMN_CONTENT)));
//
//			} while (cursor.moveToNext());
//		}
//		cursor.close();
//		return selectList;
//	}
	
//	
//	public MemLeakProfile dbSelectList(String[] order) throws SQLException {
//		MemLeakLog.V("dbSelectGroupCusor()");
//
//		Cursor cursor = db.query(true, DB_TABLE, from, DB_COLUMN_VOICE + "=?",
//				order, null, null, null, null);
//		MemLeakProfile gVoiceProfile = null;
//		if (cursor.moveToFirst()) {
//			gVoiceProfile = new MemLeakProfile(cursor.getLong(cursor
//					.getColumnIndex(DB_COLUMN_ID)), cursor.getInt(cursor
//					.getColumnIndex(DB_COLUMN_GROUP)), cursor.getInt(cursor
//					.getColumnIndex(DB_COLUMN_TYPE)), cursor.getString(cursor
//					.getColumnIndex(DB_COLUMN_VOICE)), cursor.getString(cursor
//					.getColumnIndex(DB_COLUMN_CONTENT)));
//			MemLeakLog.V("id :"+ cursor.getLong(cursor.getColumnIndex(DB_COLUMN_ID))
//							+ "\n group :"
//							+ cursor.getInt(cursor.getColumnIndex(DB_COLUMN_GROUP))
//							+ "\n type :"
//							+ cursor.getInt(cursor.getColumnIndex(DB_COLUMN_TYPE))
//							+ "\n voice :"
//							+ cursor.getString(cursor.getColumnIndex(DB_COLUMN_VOICE))
//							+ "\n content :"
//							+ cursor.getString(cursor.getColumnIndex(DB_COLUMN_CONTENT)));
//
//		}
//		cursor.close();
//		return gVoiceProfile;
//	}
//	
}
