package com.cm.interfaces;

public interface IExtractorCallback 
{
	public void ProgressCallback(int current, int total);
	public void ProgressFileCallback(String file);
	public void FinishCallback();
	public void ErrorCallback(String error);
}
