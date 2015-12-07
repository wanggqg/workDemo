package com.webview;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.util.Log;

public class UnityWrapper {

	private static final String TAG = "UnityWrapper";
	private Method mSendMessage = null;
	private String mUnityObjName;

	public UnityWrapper() {
		CommonUtils.unityWraper = this;
	}

	/**
	 * 获取当前unity的activity实例
	 */
	private Activity getUnityCurrentActivity() {
		Activity activity = null;

		try {
			Class<?> unityPlayer = Class.forName("com.unity3d.player.UnityPlayer");
			Field currentActivity = unityPlayer.getField("currentActivity");
			mSendMessage = unityPlayer.getMethod("UnitySendMessage", String.class, String.class, String.class);
			activity = (Activity) currentActivity.get(null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.d("Unity","not in unity environment!");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			Log.d("Unity","Unity is not offical!");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			Log.d("Unity", "Unity does not have UnitySendMessage method!");
		}

		if (activity == null) {
			throw new IllegalStateException("Please call init function after unity has been launched!");
		}

		return activity;
	}

	public void init(String gameId, String userId, String serverUrl) {
		Log.v(TAG, "init");
	}

	public void deinit() {
		Log.v(TAG, "deinit");
	}

	public void setUnityObjName(String name) {
		mUnityObjName = name;
		Log.v(TAG, "setUnityObjName");
	}

	public void sendUnityMessage(String target, String info) {
		if (mSendMessage != null) {
			Log.d(TAG, "UnitySendMessage:" + target + ", " + info);
			try {
				if(mUnityObjName != null){
					mSendMessage.invoke(null, mUnityObjName, target, info);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public void showFullWebView(String url) {
		CommonUtils.showFullWebView(getUnityCurrentActivity(), url);
	}

	public void showWebView(String url) {
		Log.d("Unity", "Android-showWebView "+url);
		CommonUtils.showWebView(getUnityCurrentActivity(), url);
	}
}
