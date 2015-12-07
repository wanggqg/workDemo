package com.cm.download;

import java.nio.ByteBuffer;

import android.text.TextUtils;
import android.util.Log;

import com.cm.interfaces.IDownloadCallback;
import com.cm.interfaces.IDownloadFileCallback;

public class WWWRetryDownload {
	/**
	 * 重试次数
	 */
	private int connectNum = 0;
	private IDownloadFileCallback mCallback;
	private String mUrl = "";
	private String filePath = "";
	private WWWDownloadFile wwwDownloadFile;

	public WWWRetryDownload(String url, String path, IDownloadFileCallback mCallback) {
		this.mUrl = url;
		this.filePath = path;
		this.mCallback = mCallback;
	}

	public void downLoadFile(final int num,int timeout) {
		wwwDownloadFile = new WWWDownloadFile(mUrl,timeout, new IDownloadCallback() {
			ContinueFile mFile = null;
			ByteBuffer mBuffer;

			@Override
			public void ErrorCallback(String url, String error) {
				if (connectNum < num) {
					connectNum++;
					wwwDownloadFile.run();
					return;
				}
				mCallback.FailCallback(mUrl, error);
			}

			@Override
			public boolean ConnectReadyCallback(WWWDownloadFile download, int length) {
				if (!TextUtils.isEmpty(filePath)) {
					try {
						mFile = new ContinueFile(filePath);
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
				int totalLength = 0;
				if (length < 1024 * 1024) {
					totalLength = 1024 * 1024;
				} else {
					totalLength = length + 64 * 1024;
				}
				mBuffer = ByteBuffer.allocate(totalLength);
				return true;
			}

			@Override
			public boolean ReadCallback(byte[] buffer, int length) {
				if (length > 0) {
					while (mBuffer.position() + length > mBuffer.capacity()) {
						Log.e("updater", "filelist size over " + mBuffer.capacity());
						ByteBuffer iBuffer = ByteBuffer.allocate(mBuffer.capacity() + 64 * 1024);
						Log.d("updater", "mBuffer: " + mBuffer.position() + "  length: " + length);
						iBuffer.put(mBuffer.array(), 0, mBuffer.position());
						Log.d("updater", "iBuffer: " + iBuffer.position());
						mBuffer = iBuffer;
					}
					mBuffer.put(buffer, 0, length);
				}
				if (mFile != null) {
					Log.d("updater", buffer.length + "");
					mFile.WriteData(buffer, length);
				}
				return true;
			}

			@Override
			public void ProgressCallback(int read, int length, long now_time, long start_time) {
			}

			@Override
			public void DoneCallback(String url) {
				byte[] data = new byte[mBuffer.position()];
				System.arraycopy(mBuffer.array(), 0, data, 0, mBuffer.position());
				mCallback.DoneCallback(mUrl, data);
			}
		});
		wwwDownloadFile.start();
	}
}
