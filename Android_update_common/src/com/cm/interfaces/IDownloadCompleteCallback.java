package com.cm.interfaces;

public interface IDownloadCompleteCallback {
	public void DoneCallback(String url,byte[] data);
	public void FailCallback(String url);
}
