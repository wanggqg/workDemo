package com.cm.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import android.text.TextUtils;
import android.util.Log;

import com.cm.interfaces.IDownloadFilesCallback;
import com.cm.update.StorageMgr;

public class WWWDownloadFiles extends java.lang.Thread {
	private IDownloadFilesCallback mCallBack;
	// private int mContinue;
	private int downloadSize;
	private int downloadFilesSize;
	private boolean mStop = false;
	private boolean bComplete = false;
	private int retCode;
	private String errInfo;
	private String mUpdateUrl;
	private boolean isNoraml;
	private URL url;
	private boolean connectReadyCallback = false;
	private String fileUrl;
	private ReentrantLock mLock;
	ArrayList<String> mUrl = null;

	public WWWDownloadFiles(ReentrantLock lock, String updateUrl, ArrayList<String> urlList, IDownloadFilesCallback callback) {
		mLock = lock;
		mUpdateUrl = updateUrl;
		mUrl = urlList;
		mCallBack = callback;
	}

	public boolean finish() {
		return bComplete;
	}

	// public void SetContinue(int continu) {
	// mContinue = continu;
	// }

	public int getDownloadSize() {
		return downloadSize;
	}

	public int getResponseCode() {
		return retCode;
	}

	public String getErrorInfo() {
		return errInfo;
	}

	public boolean bNoraml() {
		return isNoraml;
	}

	/**
	 * 获取需要下载文件的url
	 * 
	 * @return url
	 */
	public String getUrl() {
		String mFileUrl = "";
		// 保证在取得时候安全
		mLock.lock();
		if (mUrl != null && mUrl.size() > 0) {
			mFileUrl = mUrl.remove(0);
		}
		mLock.unlock();
		return mFileUrl;
	}

	public void run() {
		while (!mStop) {
			url = null;
			HttpURLConnection connection = null;
			try {
				if (mUrl != null && mUrl.size() == 0) {
					// 如果获取不到停止当前线程
					mStop = true;
					if (mCallBack != null) {
						mCallBack.CurrentThreadStopCallback();
					}
					return;
				}
				fileUrl = getUrl();
				if (TextUtils.isEmpty(fileUrl)) continue;
				
				String fileWebUrl = mUpdateUrl + fileUrl.replace('\\', '/');
				fileWebUrl = fileWebUrl + "?t=" + System.currentTimeMillis();
				url = new URL(fileWebUrl);
				// Log.d("wgq", "downLoadFile Url is: " + url);
				connection = (HttpURLConnection) url.openConnection();
				connection.setUseCaches(false);
			} catch (MalformedURLException e) {
				Log.d("updater", "WWWDownload:" + e.toString());
				if (mCallBack != null)
					mCallBack.ErrorCallback(fileUrl, e.toString());
				continue;
			} catch (IOException e) {
				Log.d("updater", "WWWDownload:" + e.toString());
				if (mCallBack != null)
					mCallBack.ErrorCallback(fileUrl, e.toString());
				continue;
			}

			if (mCallBack != null) {

				connectReadyCallback = mCallBack.ConnectReadyCallback(this, StorageMgr.GetAssetPath() + fileUrl);
				if (!connectReadyCallback) {
					mCallBack.ErrorCallback(fileUrl, fileUrl + " aborted");
					continue;
				}
			}
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			try {
				retCode = connection.getResponseCode();
				int retCodeWeight = (int) Math.floor(retCode / 100);
				isNoraml = (retCodeWeight == 2);
				if (!isNoraml) {
					Log.e("updater", "wwwFiles response-code : " + retCode);
					errInfo = "URL Request Error:" + retCode;
					mCallBack.TimeoutCallback(fileUrl);
					connection.disconnect();
					continue;
				}
			} catch (IOException e) {
				errInfo = e.toString();
				Log.e("updater", fileUrl + ": " + e.toString());
				mCallBack.ErrorCallback(fileUrl, e.toString());
				connection.disconnect();
				continue;
			}
			int contentLength = downloadFilesSize;
			// Log.d("updater", "downloadcontentLength=" + contentLength +
			// ",mContinue=" + mContinue);

			downloadSize = contentLength;
			// if (mCallBack != null) {
			// if (mCallBack.ConnectCallback(this) == false) {
			// connection.disconnect();
			// continue;
			// }
			// }
			// contentLength += mContinue;
			int bufSize = 32 * 1024;
			InputStream is = null;
			try {
				long startTime = System.currentTimeMillis();
				byte[] buffer = new byte[bufSize];
				is = connection.getInputStream();
				if (mCallBack != null) {
					for (int bytesRead = 0; bytesRead != -1; bytesRead = is.read(buffer)) {
						if (!mCallBack.ReadCallback(buffer, bytesRead)) {
							mCallBack.ErrorCallback(fileUrl, fileUrl + " aborted");
							is.close();
							connection.disconnect();
							continue;
						}
						mCallBack.ProgressCallback(bytesRead, contentLength, System.currentTimeMillis(), startTime);
					}
					is.close();
					connection.disconnect();
				}
			} catch (IOException e) {
				Log.d("updater", "WWWDownload:" + e.toString());
				if (mCallBack != null)
					mCallBack.ErrorCallback(fileUrl, e.toString());
				try {
					if (is != null)
						is.close();
				} catch (IOException ex) {
					Log.d("updater", "is.close is error" + ex.toString());
				}

				connection.disconnect();
				continue;
			}

			if (mCallBack != null) {
				mCallBack.DoneCallback(fileUrl);
			}
		}
	}
}
