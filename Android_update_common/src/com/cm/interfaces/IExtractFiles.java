package com.cm.interfaces;

public interface IExtractFiles {
	public void doneCallback(int current,int total);
	public void completeCallback();
	public void extractFilesError();
}
