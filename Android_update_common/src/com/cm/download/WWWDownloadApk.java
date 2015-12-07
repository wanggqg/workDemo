package com.cm.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.cm.interfaces.IDownloadApkCallback;

import android.util.Log;

public class WWWDownloadApk extends java.lang.Thread
{
	private String mUrl;
	private IDownloadApkCallback mCallBack;
	private int mContinue;
	private int downloadSize;

	private boolean mStop = false;
	private boolean bComplete = false;
	
	private int retCode;
	private String errInfo;
	private boolean isNoraml;

	
	public WWWDownloadApk(String url, IDownloadApkCallback callback) {
		mUrl = url;
		mCallBack = callback;
	}
	public void setTimeout(int seconds){}
	
	public void stopDownLoad(){
		mStop = true;
	}
	
	public boolean finish(){
		return bComplete;
	}
	public void SetContinue(int continu) {
		mContinue = continu;
	}
	
	public int getDownloadSize(){
		return downloadSize;
	}
	
	public int getResponseCode(){
		return retCode;
	}
	
	public String getErrorInfo(){
		return errInfo;
	}
	
	public boolean bNoraml(){
		return isNoraml;
	}
	
	public void run()
	{		
		URL url = null;
		HttpURLConnection connection = null;
		try 
		{
			url = new URL(mUrl);
			connection = (HttpURLConnection) url.openConnection();
		} 
		catch (MalformedURLException e) 
		{
			Log.d("updater", "WWWDownload:"+e.toString());
			if (mCallBack != null)
				mCallBack.ErrorCallback(mUrl, e.toString());
			return;
		} 
		catch (IOException e) 
		{
			Log.d("updater", "WWWDownload:"+e.toString());
			if (mCallBack != null)
				mCallBack.ErrorCallback(mUrl, e.toString());
			return;
		}
		
		if (mCallBack != null)	{
			mCallBack.ConnectReadyCallback(this);
		}

		if (mContinue > 0)	{
			String sProperty = "bytes=" + mContinue +"-";
			connection.setRequestProperty("RANGE", sProperty);
		}
		
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		
		try{
			retCode = connection.getResponseCode();
			int retCodeWeight =(int) Math.floor(retCode/100);
			isNoraml = (retCodeWeight == 2);
			if(!isNoraml){
				errInfo = "URL Request Error:"+retCode;
				Log.e("updater", "response-code : "+retCode);
				mCallBack.ErrorCallback(mUrl, retCode+"");
				connection.disconnect();
				return;
			}
		}
		catch(IOException e){
			errInfo = e.toString();
			mCallBack.ErrorCallback(mUrl, retCode+"");
			connection.disconnect();
			return;
		}


		int contentLength = connection.getContentLength() > 0 ? connection.getContentLength() : 0;
//		Log.d("updater","downloadcontentLength="+ contentLength +",mContinue="+mContinue);
		
		downloadSize = contentLength;
		
		if (mCallBack != null)	{
			if (mCallBack.ConnectCallback(this) == false){
				mCallBack.ErrorCallback(mUrl, "ConnectCallback is false");
				connection.disconnect();
				return; 
			}
		}
		
		contentLength += mContinue;
		
		
		int totalWritten = mContinue;
		int bufSize = 32 * 1024;
		InputStream is = null;
		try 
		{
			long startTime = System.currentTimeMillis();
			byte[] buffer = new byte[bufSize];

			is = connection.getInputStream();
			if (mCallBack != null)
			{
				for (int bytesRead = 0; bytesRead != -1; bytesRead = is.read(buffer))
				{
					if (mCallBack.ReadCallback(buffer, bytesRead) == false){
						mCallBack.ErrorCallback(mUrl, mUrl + " aborted");
						is.close();
						connection.disconnect();
						return;
					}
					totalWritten += bytesRead;
					mCallBack.ProgressCallback(totalWritten, contentLength, System.currentTimeMillis(), startTime);
				}
				is.close();
				connection.disconnect();
			}
		} 
		catch (IOException e) 
		{
			try {
				is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			connection.disconnect();
			Log.d("updater","WWWDownload:"+ e.toString());
			if (mCallBack != null)
				mCallBack.ErrorCallback(mUrl, e.toString());
			return;
		}
		
		if (mCallBack != null){
			bComplete = true;
			Log.d("updater", "down:" + totalWritten + " total:" + contentLength);
			mCallBack.ProgressCallback(totalWritten, totalWritten, 0, 0);
			mCallBack.DoneCallback(mUrl);
		}
	}

}
