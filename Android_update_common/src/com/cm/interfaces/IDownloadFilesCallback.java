package com.cm.interfaces;

import com.cm.download.WWWDownloadFiles;

public interface IDownloadFilesCallback {
	public void ProgressCallback(int read, int length, long now_time, long start_time);

	/**
	 * 当前线程下载完一个文件后回调方法
	 * 
	 * @param buffer
	 * @param url
	 */
	public boolean DoneCallback(String url);

	/**
	 * 当前线程停止回调方法
	 */
	public void CurrentThreadStopCallback();

	public void ErrorCallback(String url, String error);

	public void TimeoutCallback(String url);

	public boolean ConnectCallback(WWWDownloadFiles download);

	public boolean ReadCallback(byte[] buffer, int length);

	public boolean ConnectReadyCallback(WWWDownloadFiles download, String path);
}
