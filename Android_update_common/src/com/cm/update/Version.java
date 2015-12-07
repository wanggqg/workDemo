package com.cm.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cm.utils.ShareObjectManager;
import com.cm.utils.VersionUtil;
import com.unity3d.player.UnityPlayer;

public class Version {
	private static String adIdConfig = "ok_adid.cfg";
	private Activity context;
	public Version(){
		context = UnityPlayer.currentActivity;
		ShareObjectManager.getInstance().initContext(context);
	}
	public String GetClientVersion(){
		String sVersion = null;
		if(context != null){
			sVersion = ShareObjectManager.getInstance().getString("version", "0.0.0");
		}
		Log.d("updater","sVersion="+sVersion );
		return sVersion;
	}

	public String GetLanchVersion(){
		String version = "0";
		if(context != null){
			PackageManager pm = context.getPackageManager();  
			PackageInfo info;
			try {
				info = pm.getPackageInfo(context.getPackageName(), 0);
				version = info.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return VersionUtil.modifyBundleVersion(version);
	}
	public String GetIsShowSpecial(){
		String bIsShowSpecial = "0";
		if(context != null){
			bIsShowSpecial = ShareObjectManager.getInstance().getString("mIsShowSpecial", "0");
			Log.d("updater", "bIsShowSpecial="+bIsShowSpecial);
		}
		return bIsShowSpecial;
		
	}
	public String GetUpdateInfor(){
		String updateInfor = "";
		if(context != null){
			updateInfor = ShareObjectManager.getInstance().getString("updateinfourl", "");
			Log.d("updater", "updateInfor="+updateInfor);
		}
		return updateInfor;
	}
	/**
	 * 获取广告id
	 * @return
	 */
	public String GetADID(){
		String sAdId = "";
		Activity context = UnityPlayer.currentActivity;
		if (context ==null) {
			sAdId = "";
		}
		try{
			AssetManager manager = context.getAssets();
			InputStream stream = manager.open(adIdConfig);
			InputStreamReader isReader = new InputStreamReader(stream);
			BufferedReader breader = new BufferedReader(isReader);

			String line = breader.readLine();
			if(line != null){
				String[] params = line.split("=");
				if(params.length>1){
					sAdId = params[1];
				}
			}
			breader.close();
			isReader.close();
			stream.close();
		}
		catch(IOException e){
			Log.d("updater", "can't open "+adIdConfig);
		}
		Log.d("updater", "sAdId="+sAdId);
		return sAdId;
	}
	/**
	 * 获取设备信息
	 * @return
	 */
	public String GetModleInfo(){
		String PhoneModel = android.os.Build.BRAND+ android.os.Build.PRODUCT;
		String Type = "";
		String PhoneOs = android.os.Build.VERSION.RELEASE;
		Activity context = UnityPlayer.currentActivity;
		if (context ==null) {
			Type = "nonet";
		}
		ConnectivityManager cm = (ConnectivityManager) context
.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) {
			Type = "nonet";
		} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			Type = "wifi";
		} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			int subType = info.getSubtype();
			if (subType == TelephonyManager.NETWORK_TYPE_CDMA
					|| subType == TelephonyManager.NETWORK_TYPE_GPRS
					|| subType == TelephonyManager.NETWORK_TYPE_EDGE) {
				Type = "2g";
			} else if (subType == TelephonyManager.NETWORK_TYPE_UMTS
					|| subType == TelephonyManager.NETWORK_TYPE_HSDPA
					|| subType == TelephonyManager.NETWORK_TYPE_EVDO_A
					|| subType == TelephonyManager.NETWORK_TYPE_EVDO_0
					|| subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
				Type = "3g";
			} else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {
				Type = "4g";
			}
		}
		String message = PhoneModel+"###"+Type+"###android-"+PhoneOs;
		Log.d("updater", "GetModleInfo: "+message);
		return message;
	}
}
