package com.cm.interfaces;

public interface IDownloadFileCallback {
	public void DoneCallback(String url,byte[] data);
	public void FailCallback(String url,String error);
}
