package com.cm.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.os.Message;
import android.util.Log;

import com.cm.config.UpdateConfig;
import com.cm.download.ApkContinueFile;
import com.cm.download.ContinueFile;
import com.cm.download.WWWDownloadApk;
import com.cm.download.WWWDownloadFiles;
import com.cm.download.WWWRetryDownload;
import com.cm.interfaces.IAlertBoxCallback;
import com.cm.interfaces.IDownloadApkCallback;
import com.cm.interfaces.IDownloadCompleteCallback;
import com.cm.interfaces.IDownloadFileCallback;
import com.cm.interfaces.IDownloadFilesCallback;
import com.cm.interfaces.IDownloadFinishCallback;
import com.cm.interfaces.IOperateResult;
import com.cm.update.StorageMgr;
import com.cm.update.UpdateActivity;
import com.cm.update.UpdateView;

public class DownLoadManager {
	/**
	 * 线程数
	 */
	private static final int THREADNUM = 3;

	static DownLoadManager _downLoadManager = null;
	Activity _activity = null;

	public WWWDownloadFiles wwwFiles = null;
	private List<String> countNums;

	private ArrayList<String> errorFileList;

	private ArrayList<String> mUrl;

	private ReentrantLock lock;

	public static DownLoadManager getInstance() {
		if (_downLoadManager == null) {
			_downLoadManager = new DownLoadManager();
		}
		return _downLoadManager;
	}

	public void initContext(Activity context) {
		_activity = context;
		lock = new ReentrantLock();
	}

	public void checkDownloadTask() {
		if (wwwFiles != null) {
			if (wwwFiles.finish()) {
				wwwFiles = null;
			}
		}
	}

	// =======================================================================
	public void downLoadServerList() {
		String serverListUrl = UpdateManager.getInstance().mGameConfig.GetServerlistUrl();
		String serverListPath = StorageMgr.GetStreamingAssetPath() + UpdateConfig.sServerList;
		Log.d("updater", "DownLoadServerList：" + serverListUrl);
		WWWRetryDownload wwwRetryDownload = new WWWRetryDownload(serverListUrl, serverListPath, new IDownloadFileCallback() {

			@Override
			public void FailCallback(String url, String error) {
				Log.e("updater", "download serverList error: " + url + "error info: " + error);
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADSERVERLIST_ERROR, url + "#" + error);
			}

			@Override
			public void DoneCallback(String url, byte[] data) {
				Log.d("updater", "download serverList ok: " + url);
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADSERVERLIST_OK, "SERVERLIST_OK");
			}
		});
		wwwRetryDownload.downLoadFile(3, 6000);
	}

	// =======================================================================
	public void downLoadApk(String apkUrl) {
		downLoadApk(apkUrl, new IDownloadCompleteCallback() {

			@Override
			public void FailCallback(String url) {
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADAPK_ERROR, "");
				UpdateView.getInstance().showErrorAlert("cm_getRes_error");
			}

			@Override
			public void DoneCallback(String path, byte[] data) {
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADAPK_OK, "DOWNLOADAPK_OK");
				VersionUtil.InstallApk(path, _activity);
			}
		});
	}

	public void downLoadApk(final String apkUrl, final IDownloadCompleteCallback iDownloadCompleteCallback) {
		Log.d("updater", "DownLoadApk: " + apkUrl);
		final String apkPath = UpdateManager.getInstance().mUpdateXml.getApkPath();
		Log.d("updater", "apkPAth: " + apkPath);
		WWWDownloadApk wwwApk = new WWWDownloadApk(apkUrl, new IDownloadApkCallback() {
			ApkContinueFile mFile = null;

			@Override
			public void TimeoutCallback() {
				Log.e("updater", "DownLoadApk Timeout");
				iDownloadCompleteCallback.FailCallback(apkUrl);
			}

			@Override
			public boolean ReadCallback(byte[] buffer, int length) {
				if (mFile != null) {
					mFile.WriteData(buffer, length);
					return true;
				}
				return false;
			}

			@Override
			public void ProgressCallback(int read, int length, long now_time, long start_time) {
				Message msg = new Message();
				msg.what = IOperateResult.DownloadApking;
				msg.arg1 = read;
				msg.arg2 = length;
				UpdateActivity mActivity = (UpdateActivity) _activity;
				if (mActivity != null) {
					mActivity.mHandler.sendMessage(msg);
				}
			}

			@Override
			public void ErrorCallback(String url, String error) {
				Log.e("updater", "DownLoadApk ErrorCallback: "+url+"#"+error);
				iDownloadCompleteCallback.FailCallback(apkUrl);
			}

			@Override
			public void DoneCallback(String url) {
				iDownloadCompleteCallback.DoneCallback(apkPath, null);
			}

			@Override
			public boolean ConnectReadyCallback(WWWDownloadApk download) {
				try {
					mFile = new ApkContinueFile(apkPath);
					download.SetContinue(mFile.GetPosition());
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}

			@Override
			public boolean ConnectCallback(WWWDownloadApk download) {
				if (download.bNoraml()) {
					return true;
				}
				Log.e("updater", "DownLoadApk ConnectCallback");
				return false;
			}
		});
		wwwApk.start();
	}

	// -----------------------------------------------------------------------------------------------------------------------------

	// 获取版本信息的文件
	public void downloadUpdateXml(final IDownloadCompleteCallback downloadcompleteCallBack) {

		final String updateXmlUrl = UpdateManager.getInstance().mGameConfig.getUpdateXmlUrl();
		Log.d("updater", "downloadUpdateXmlUrl = " + updateXmlUrl);
		WWWRetryDownload wwwRetryDownload = new WWWRetryDownload(updateXmlUrl, "", new IDownloadFileCallback() {

			@Override
			public void FailCallback(String url, String error) {
				Log.e("updater", "download Update error ErrorCallback");
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADUPDATE_ERROR, url + "#" + error);
				downloadcompleteCallBack.FailCallback(url);
			}

			@Override
			public void DoneCallback(String url, byte[] data) {
				Log.d("updater", "DownLoad update.xml success");
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADUPDATE_OK, "UPDATE_OK");
				// 这里下载update.xml完成，需要做进一步处理
				if (downloadcompleteCallBack != null) {
					downloadcompleteCallBack.DoneCallback(updateXmlUrl, data);
				}
			}
		});
		wwwRetryDownload.downLoadFile(1, 6000);
	}

	public void downloadFileList(final IDownloadCompleteCallback iDownloadCompleteCallback) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADFILELIST_BEGIN, "FILELIST_BEGIN");
		final String fileListUrl = UpdateManager.getInstance().mUpdateXml.getFileListUrl();
		Log.d("updater", "downloadFileList = " + fileListUrl);
		WWWRetryDownload wwwRetryDownload = new WWWRetryDownload(fileListUrl, "", new IDownloadFileCallback() {

			@Override
			public void FailCallback(String url, String error) {
				Log.e("updater", "DownloadFileListError ErrorCallback");
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADFILELIST_ERROR, url + "#" + error);
				iDownloadCompleteCallback.FailCallback(url);
			}

			@Override
			public void DoneCallback(String url, byte[] data) {
				Log.d("updater", "DownloadFileList is ok: " + url);
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADFILELIST_OK, "FILELIST_OK");
				iDownloadCompleteCallback.DoneCallback(url, data);
			}
		});
		wwwRetryDownload.downLoadFile(1, 6000);
	}

	/**
	 * 移出错误集合
	 * 
	 * @param url
	 */
	private void removeErrorList(String url) {
		lock.lock();
		try {
			if (errorFileList != null) {
				errorFileList.remove(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	private void addErrorList(String url, String error) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADFILES_ERROR, url + "#" + error);
		lock.lock();
		try {
			if (errorFileList != null) {
				if (!errorFileList.contains(url)) {
					errorFileList.add(url);
					mUrl.add(url);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * 将开启多个线程现在多个文件
	 */
	public void downLoadFileFromNet(int length, IDownloadFinishCallback iDownloadFinishCallback) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADFILES_BEGIN, "");
		mUrl = new ArrayList<String>();
		countNums = Collections.synchronizedList(new ArrayList<String>());
		Set<String> keySet = FileListManager.getInstance().addFileListUrlAndSize.keySet();
		for (String string : keySet) {
			mUrl.add(string);
		}
		Log.d("updater", "need download: " + mUrl.size());
		errorFileList = new ArrayList<String>();
		for (int i = 0; i < THREADNUM; i++) {
			countNums.add("" + i);
		}
		
		for (int i = 0; i < THREADNUM; i++) {
			DownloadFileFromNet(lock, i, UpdateManager.getInstance().mUpdateXml.getFilesUrl(), mUrl, length, iDownloadFinishCallback);
		}
	}

	public String getFileUrl() {
		synchronized (this) {
			if (mUrl != null && mUrl.size() > 0) {
				return mUrl.remove(0);
			}
			return "";
		}
	}

	private void DownloadFileFromNet(ReentrantLock lock, final int thread, String updateUrl, final ArrayList<String> urlList, final int length, final IDownloadFinishCallback iDownloadFinishCallback) {
		final WWWDownloadFiles wwwFiles = new WWWDownloadFiles(lock, updateUrl, urlList, new IDownloadFilesCallback() {
			ContinueFile mFile = null;

			@Override
			public void ProgressCallback(int read, int total, long now_time, long start_time) {
				Message msg = Message.obtain();
				msg.what = IOperateResult.UpdateProgress;
				msg.arg1 = read;
				msg.arg2 = length;
				UpdateActivity mActivity = (UpdateActivity) _activity;
				if (mActivity != null) {
					mActivity.mHandler.sendMessage(msg);
				}
			}

			@Override
			public boolean DoneCallback(String url) {
				if (mFile != null)
					mFile.Succeed();
				if (FileListManager.getInstance().addFileList != null) {
					try {
						// 下载完成后解压
						FilesManager.getInstance().unZipFile(url);
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("updater", "unZipFile is error：" + url);
						addErrorList(url, "unzip error");
						return false;
					}
					// // 这里下载完成，获取md5与服务器比较
					// String fileHashcode =
					// FilesManager.getInstance().getFileMD5(url);
					// if (!TextUtils.isEmpty(fileHashcode)) {
					// boolean bOk =
					// FilesManager.getInstance().checkFileMd5(url,
					// fileHashcode);
					// if (!bOk) {
					// Log.e("updater", "check file md5 is fail：" + url);
					// errorFileList.add(url);
					// return;
					// }
					// }
					if (!FilesManager.getInstance().refreshFileList(url, FileListManager.getInstance().addFileList.get(url))) {
						Log.e("updater", "refreshFileList url: " + url);
						addErrorList(url, "refreshFileList error");
						return false;
					}

					removeErrorList(url);
					return true;

				}
				return true;
			}

			@Override
			public void CurrentThreadStopCallback() {
				synchronized (DownLoadManager.this) {
					if (countNums != null || countNums.size() != 0) {
						Log.d("updater", "thread: " + thread + " is compeleted and countNums size:" + countNums.size());
						countNums.remove("" + thread);
						for (int i = 0; i < countNums.size(); i++) {
							Log.d("updater", "no compeleted :" + countNums.get(i));
						}
					}
					if (countNums != null && countNums.size() == 0) {
						if (mUrl != null && mUrl.size() == 0 && errorFileList != null && errorFileList.size() == 0) {
							// 如果下载完成，加入一个完成的标记
							ResManager.getInstance().setResStatus(true);
							Log.d("updater", "all threads is compeleted mUrl size: " + mUrl.size());
							// if (errorFileList.size() > 0) {
							// for (int i = 0; i < errorFileList.size(); i++) {
							// Log.e("updater", "error file: " +
							// errorFileList.get(i));
							// }
							// Log.e("updater",
							// "iDownloadFinishCallback FailCallback");
							// iDownloadFinishCallback.FailCallback("");
							// } else {
							Log.d("updater", "iDownloadFinishCallback DoneCallback");
							iDownloadFinishCallback.DoneCallback("");
							// }
						} else {
							iDownloadFinishCallback.FailCallback("");
						}
					}
				}
			}

			@Override
			public void ErrorCallback(String url, String error) {
				Log.e("updater", "ErrorCallback url: " + url + "error: " + error);
				addErrorList(url, error);
			}

			@Override
			public void TimeoutCallback(String url) {
				Log.e("updater", "TimeoutCallback url: " + url);
				addErrorList(url, "timeout");
			}

			@Override
			public boolean ConnectReadyCallback(WWWDownloadFiles download, String path) {
				boolean createFile = StorageMgr.createFile(path);
				if (createFile) {
					try {
						mFile = new ContinueFile(path);
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
					return true;
				} else {
					Log.d("updater", "create file error: " + path);
				}
				return false;
			}

			@Override
			public boolean ConnectCallback(WWWDownloadFiles download) {
				if (download.bNoraml()) {
					return true;
				}
				return false;
			}

			@Override
			public boolean ReadCallback(byte[] buffer, int length) {
				if (mFile != null) {
					mFile.WriteData(buffer, length);
					return true;
				}
				return false;
			}
		});
		wwwFiles.start();
	}
}
