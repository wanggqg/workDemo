package com.cm.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.cm.interfaces.IDownloadCallback;

public class WWWDownloadFile extends java.lang.Thread {
	private String mUrl;
	private IDownloadCallback mCallBack;
	private int retCode;
	private boolean isNoraml = true;
	private boolean mStop = false;
	private int timeout;
	public WWWDownloadFile(String url,int timeout, IDownloadCallback callback) {
		this.timeout = timeout;
		mUrl = url;
		mCallBack = callback;
	}

	public void stopDownLoad() {
		mStop = true;
	}
	@Override
	public void run() {
		URL url = null;
		HttpURLConnection connection = null;
		try {
			mUrl = mUrl+"?t="+System.currentTimeMillis();
			url = new URL(mUrl);
			Log.d("updater", "downloadfile: "+mUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches(false);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			if (mCallBack != null) {
				mCallBack.ErrorCallback(mUrl, e.toString());
			}
			return;
		} catch (IOException e) {
			e.printStackTrace();
			if (mCallBack != null)
				mCallBack.ErrorCallback(mUrl, e.toString());
			return;
		}
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		try {
			retCode = connection.getResponseCode();
			int retCodeWeight = (int) Math.floor(retCode / 100);
			isNoraml = (retCodeWeight == 2);
		} catch (IOException e) {
			e.printStackTrace();
			if (mCallBack != null) {
				mCallBack.ErrorCallback(mUrl, e.toString());
			}
			connection.disconnect();
			return;
		}
		if (!isNoraml) {
			if (mCallBack != null) {
				mCallBack.ErrorCallback(mUrl, retCode + "");
			}
			connection.disconnect();
			return;
		}
		int contentLength = connection.getContentLength() > 0 ? connection.getContentLength() : 0;
//		Log.d("updater", "downloadcontentLength=" + contentLength);

		if (mStop) {
			return;
		}
		byte[] buffer = new byte[32 * 1024];
		InputStream is = null;
		try {
			is = connection.getInputStream();
			if (mCallBack != null) {
				if (!mCallBack.ConnectReadyCallback(this, contentLength)) {
					mCallBack.ErrorCallback(mUrl, "create file error");
					is.close();
					connection.disconnect();
					return;
				}
			}
			mCallBack.ConnectReadyCallback(this, contentLength);
			for (int bytesRead = 0; bytesRead != -1; bytesRead = is.read(buffer)) {
				if (mCallBack.ReadCallback(buffer, bytesRead) == false) {// buffer是数据源
					mCallBack.ErrorCallback(mUrl, mUrl + " aborted");
					is.close();
					connection.disconnect();
					return;
				}
			}
			is.close();
			connection.disconnect();
		} catch (IOException e) {
			try {
				if (is!=null) is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			connection.disconnect();
			e.printStackTrace();
			if (mCallBack != null) {
				mCallBack.ErrorCallback(mUrl, e.toString());
			}
			return;
		}
		if (mCallBack != null) {
			mCallBack.DoneCallback(mUrl);
		}
	}
}
