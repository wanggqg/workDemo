package com.cm.interfaces;

import com.cm.download.WWWDownloadApk;

public interface IDownloadApkCallback {
	public void ProgressCallback(int read, int length, long now_time, long start_time);
	public void DoneCallback(String url);
	public void ErrorCallback(String url, String error);
	public void TimeoutCallback();
	public boolean ConnectCallback(WWWDownloadApk download);
	public boolean ReadCallback(byte[] buffer, int length);
	public boolean ConnectReadyCallback(WWWDownloadApk download);
}
