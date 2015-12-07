package com.cm.interfaces;

import com.cm.download.WWWDownloadFile;



public interface IDownloadCallback 
{
	public void ProgressCallback(int read, int length, long now_time, long start_time);
	public void DoneCallback(String url);
	public boolean ConnectReadyCallback(WWWDownloadFile www,int len);
	public void ErrorCallback(String url, String error);
	public boolean ReadCallback(byte[] buffer, int length);
}
