package com.gihasil.lab.meminfochart.db;

public class MemInfoProfile {
	long mId;
	String mPackageName;
	long mTimestamp;
	long mPssMemory;
	long mDalvikMemory;
	long mOtherDevMem;
	long mNativeMem;

	
	public long getId() {
		return mId;
	}


	public MemInfoProfile(long mId, String mPackageName, long mTimestamp, long mPssMemory, long mDalvikMem, long mOtherDev, long mNativeMem) {
		this.mId = mId;
		this.mPackageName = mPackageName;
		this.mTimestamp = mTimestamp;
		this.mPssMemory = mPssMemory;
		this.mDalvikMemory = mDalvikMem;
		this.mOtherDevMem = mOtherDev;
		this.mNativeMem = mNativeMem;
	}


	public String getPackageName() {
		return mPackageName;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public long getPssMemory() {
		return mPssMemory;
	}

	public long getDalvikMemory() {
		return mDalvikMemory;
	}

	public long getOtherDevMem() {
		return mOtherDevMem;
	}

	public long getNativeMem() {
		return mNativeMem;
	}

}
