package com.cm.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetWorkManager {

	static NetWorkManager _networkManager = null;
	Activity _activity = null;
	
	public static NetWorkManager getInstance(){
		if(_networkManager == null){
			_networkManager = new NetWorkManager();
		}
		return _networkManager;
	}
	
	public void initContext(Activity context){
		_activity = context;
	}
	/**
	 * 判断如果没有网络，引导进入网络设置界面
	 * @return
	 */
	public boolean getNetWorkStatus() {
		boolean netSataus = false;
		ConnectivityManager cwjManager = (ConnectivityManager) _activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		cwjManager.getActiveNetworkInfo();
		if (cwjManager.getActiveNetworkInfo() != null) {
			netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
		}
		return netSataus;
	}
	
	public boolean isMobileNetWork() {
		ConnectivityManager connectivityManager = (ConnectivityManager) _activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		}
		NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null && mNetworkInfo.isAvailable() && mNetworkInfo.isConnected()) {
			if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				return true;
			}
		} else {
			Log.d("updater", "the network can't connect to intenet");
		}
		return false;
	}
	
}
