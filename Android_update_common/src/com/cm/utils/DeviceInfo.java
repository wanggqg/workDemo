package com.cm.utils;

import java.io.File;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class DeviceInfo {

	public DeviceInfo(){}
	/**
	 * 获取android当前可用内存大小 
	 * @param activity
	 * @return
	 */
	public float getAvailMemory(Activity activity) {

		ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		float MB = 1024*1024;
		return  mi.availMem/MB;
	}
	/**
	 * 获取设备信息
	 */
	public void printAndroidSystemInfo(){
		int sdkInt = android.os.Build.VERSION.SDK_INT;
		String device_model = android.os.Build.MODEL; // 设备型号
		String version_release = android.os.Build.VERSION.RELEASE; // 设备的系统版本
		String msg = "AndroidSdkAPILevel:"+sdkInt +",device_model:"+device_model+",version_release:"+version_release;
		Log.d("updater", msg);
	}
	/**
	 * 判断android sdk必须大于9
	 * @return
	 */
	public boolean supportCurrentAndroidSystem(){
		return android.os.Build.VERSION.SDK_INT >=9;
	}

	public void readSystem() {
    	File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();
		Log.d("", "block大小:"+ blockSize+",block数目:"+ blockCount+",总大小:"+blockSize*blockCount/1024+"KB");
		Log.d("", "可用的block数目：:"+ availCount+",可用大小:"+ availCount*blockSize/1024+"KB");
    }
	
	public void readSDCard() {
    	String state = Environment.getExternalStorageState();
    	if(Environment.MEDIA_MOUNTED.equals(state)) {
    		File sdcardDir = Environment.getExternalStorageDirectory();
    		StatFs sf = new StatFs(sdcardDir.getPath());
    		long blockSize = sf.getBlockSize();
    		long blockCount = sf.getBlockCount();
    		long availCount = sf.getAvailableBlocks();
    		Log.d("", "block大小:"+ blockSize+",block数目:"+ blockCount+",总大小:"+blockSize*blockCount/1024+"KB");
    		Log.d("", "可用的block数目：:"+ availCount+",剩余空间:"+ availCount*blockSize/1024+"KB");
    	} 	
    }
	
}
