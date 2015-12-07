package com.cm.utils;

import android.app.Activity;
import android.os.Message;
import android.util.Log;

import com.cm.config.ClientVersion;
import com.cm.config.GameConfig;
import com.cm.config.GameDefine;
import com.cm.config.UpdateXml;
import com.cm.interfaces.IAlertBoxCallback;
import com.cm.interfaces.ICheckUpdateCallBack;
import com.cm.interfaces.ICopyProgressCallback;
import com.cm.interfaces.IDownloadCompleteCallback;
import com.cm.interfaces.IDownloadFinishCallback;
import com.cm.interfaces.IOperateResult;
import com.cm.update.StorageMgr;
import com.cm.update.UpdateActivity;
import com.cm.update.UpdateView;

public class UpdateManager {

	private ICheckUpdateCallBack iCheckUpdateCallBack; // 更新检查完毕后，回调接口iCheckUpdateCallBack

	public UpdateXml mUpdateXml = new UpdateXml(); // 资源服务器updateXml文件
	public GameDefine mGameDefine = new GameDefine(); // gameDefine.txt文件
	public GameConfig mGameConfig = new GameConfig(); // gameConfig.txt文件
	public ClientVersion mClientVersion = new ClientVersion(); // ClientVersion.txt文件

	static UpdateManager _updateManager = null;
	Activity _activity = null;

	public static UpdateManager getInstance() {
		if (_updateManager == null) {
			_updateManager = new UpdateManager();
		}
		return _updateManager;
	}

	public void initContext(Activity context) {
		_activity = context;
	}

	public void readConfig() {
		// 读取配置文件GameConfig
		mGameConfig.readGameConfig(_activity);
		mGameDefine.readGameDefine(_activity);
		mClientVersion.readClientVersion();
	}

	// =================================================================
	// 初始化配置文件
	// =================================================================

	private boolean isNeedUpdateApp() {
		// updateXml中的appVersion大于gameDefine中的appBundleVersion,则需要进行安装包更新
		return VersionUtil.LargerThan(mUpdateXml.mNewApkVersion, mGameDefine.appBundleVersion);
	}

	private boolean isNeedUpdateRes() {
		// updateXml中的资源版本号大于clientVersion中的资源版本号，需要进行资源更新
		String mNewResversion = mUpdateXml.mNewResversion;
		String clientVersion = mClientVersion.clientVersion;
		return VersionUtil.LargerThan(mNewResversion,clientVersion );
	}

	public void CheckUpdate(final ICheckUpdateCallBack iCheckUpdateCallBack) {
		// 检查是否需要更新后的，回调接口iCheckUpdateCallBack
		this.iCheckUpdateCallBack = iCheckUpdateCallBack;
		// ========================================================
		DeviceInfo sysinfo = new DeviceInfo();
		sysinfo.printAndroidSystemInfo();

		if (sysinfo.supportCurrentAndroidSystem() == false) {
			UpdateView.getInstance().showErrorAlert("cm_system_error");
			return;
		}
		// ========================================================
		String mountedPath = StorageMgr.getMountedPath(_activity);// 存储卡的路径

		if (mountedPath == null || mountedPath.length() <= 0) {
			Log.e("updater", "cm_storage_limit mountedPath is null");
			UpdateView.getInstance().showErrorAlert("cm_storage_limit");
			return;
		}

		Log.d("updater", "PrintMountPath mountedPathString=" + mountedPath);
		// ========================================================

		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, 0, "EVENT_STARTAPP");
		// 更新UpdateView界面的版本号显示
		String disPlayResVersion = mClientVersion.clientVersion;
		// ========================================================
		// gameDefine字段中如果appBundleVersion > appSdVersion,
		// 说明是整包覆盖安装，需要清除掉SD之前的数据，然后重新将包内特定的文件拷贝至SD
		if (VersionUtil.LargerThan(mGameDefine.appBundleVersion, mGameDefine.appSdVersion)) {
			// 这里需要判断当前安装包，是否是MINI包（资源不完整包）
			if (ResManager.getInstance().isAPKMini()) {
				ResManager.getInstance().setResStatus(false);
				
				// 新安装的apk文件中如果有filelist，则将外部的文件删除，因为apk中的文件应该是较新的
				FilesManager.getInstance().removeOldVersionSDFilesByApkFilelist();
				
			} else {
				// 当前安装包为完整包时，需要将SD上面的资源删除
				FilesManager.getInstance().removeOldVersionSDFiles();
				ResManager.getInstance().setResStatus(true);
			}
			try{
				FilesManager.getInstance().extractConfigInApkToSD();
			}catch(Exception e){
				Log.d("updater", "extractConfigInApkToSD: "+e.toString());
			}
			//删除掉files文件夹下的apk文件，这个方法是为了旧版本下载apk放在了files下，新版本的apk下载都放在了assets文件夹下
			FilesManager.getInstance().removeOldAPK();
			mGameDefine.updateGameDefine(mGameDefine.appBundleVersion);
		}
		// ========================================================
		//显示界面左上角的资源版本号
		if (VersionUtil.LargerThan(mGameDefine.appBundleVersion, mClientVersion.clientVersion)) {
			disPlayResVersion = mGameDefine.appBundleVersion;
		}
		UpdateView.getInstance().SetVersionText("V" + disPlayResVersion);

		// ========================================================
		// 进行更新逻辑判断之前，需要先完成update.xml的下载
		DownLoadManager.getInstance().downLoadServerList();
		DownLoadManager.getInstance().downloadUpdateXml(new IDownloadCompleteCallback() {
			@Override
			public void DoneCallback(String url, byte[] data) {
				downloadUpdateXmlComplete(data);
			}

			@Override
			public void FailCallback(String url) {
				iCheckUpdateCallBack.DoneCallback();
			}
		});
	}

	/**
	 * 下载update.xml后解析
	 * 
	 * @param data
	 */
	private void downloadUpdateXmlComplete(byte[] data) {
		UpdateXml mUpdateXml = UpdateManager.getInstance().mUpdateXml;
		if (mUpdateXml.parseUpdate(data)) {
			Log.d("updater", "paser update is ok");
			KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_PASERUPDATE_OK, "PASERUPDATE_OK");
			startUpdate();
		} else {
			Log.e("updater", "updateXml parse error");
			KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_PASERUPDATE_ERROR, "PASERUPDATE_ERROR");
			iCheckUpdateCallBack.DoneCallback();
		}
	}

	private void downloadWebFileList() {
		DownLoadManager.getInstance().downloadFileList(new IDownloadCompleteCallback() {
			@Override
			public void FailCallback(String url) {
				UpdateView.getInstance().showErrorAlert("cm_getresource_error");
			}

			@Override
			public void DoneCallback(String url, byte[] data) {
				downloadWebFileListComplete(data);
			}
		});
	}

	private void downloadWebFileListComplete(byte[] data) {
		// 开始校验
		int downloadLength = FileListManager.getInstance().calcFileListDiff(data);
		if (downloadLength<0) {
			UpdateView.getInstance().showErrorAlert("cm_updateres_error");
			return;
		}
		Log.d("updater", "download files total size: " + downloadLength);
		// 开始下载所有文件
		if (downloadLength>0) {
			// 如果开始下载，加入一个资源不完整的标记
			ResManager.getInstance().setResStatus(false);
		}else{
			ResManager.getInstance().setResStatus(true);
		}
		DownLoadManager.getInstance().downLoadFileFromNet(downloadLength, new IDownloadFinishCallback() {
			@Override
			public void FailCallback(String url) {
				ResManager.getInstance().setResStatus(false);
				UpdateView.getInstance().showErrorAlert("cm_updateres_error");
			}

			@Override
			public void DoneCallback(String url) {
				ResManager.getInstance().setResStatus(true);
				// 刷新本地Clientversion.txt,保存最新资源版本号
				mClientVersion.updateClientVersion(mUpdateXml.mNewResversion);
				KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADFILES_OK, "DOWNLOADFILES_OK");
				iCheckUpdateCallBack.DoneCallback();
			}
		});
	}

	// 在update.xml下载完成后，执行StartUpdate来判断是否需要执行更新
	private void startUpdate() {
		//如果覆盖安装过，在比对版本号之前，需要重新读取
		mClientVersion.readClientVersion();
		Log.d("updater", "appversion: "+mGameDefine.appBundleVersion+" resversion: "+mClientVersion.clientVersion);
		// ========================================================
		if (isNeedUpdateApp()) {
			Log.d("updater", "need update apk");
			// 这里启动更新APK安装包的跳转SKipUrl
			UpdateView.getInstance().showAlert("", "cm_skip_url", "cm_yes", "cm_exit", new IAlertBoxCallback() {
				@Override
				public void buttonYESClickCallback() {
					// 判断如果跳转的是mini包，需要将sd卡上没有的apk里面的资源拉取到sd卡
					if (("0".equals(mUpdateXml.mClient_isMini))==false) {
						// 下载安装之前，需要备份下sd卡上没有的apk里面的资源到sd卡
						FilesManager.getInstance().startCopyFilesInApkToSD(new ICopyProgressCallback() {
							@Override
							public void ProgressCallback(int current, int total) {
								Message msg = Message.obtain();
								msg.arg1 = current;
								msg.arg2 = total;
								msg.what = IOperateResult.ExtractFilesToSDing;
								UpdateActivity mActivity =(UpdateActivity) _activity;
								if (mActivity!=null) {
									mActivity.mHandler.sendMessage(msg);
								}
							}

							@Override
							public void DoneCallback() {
								UpdateView.getInstance().skipUrl(mUpdateXml.mClient_addr);
							}
						});
					} else {
						UpdateView.getInstance().skipUrl(mUpdateXml.mClient_addr);
					}
				}

				@Override
				public void buttonCancelClickCallback() {
				}
			});
			return;
		}
		if (isNeedUpdateRes()) {
			Log.d("updater", "need update res");
			// 这里启动更新Res资源的Downloader
			downloadWebFileList();
			return;
		}

		if (ResManager.getInstance().isGameResFull() == false) {
			Log.d("updater", "check res full");
			// 这里资源不完整，需要启动强制资源更新逻辑
			downloadWebFileList();
			return;
		}
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADFILELIST_NONEED, "FILELIST_NONEED");
		iCheckUpdateCallBack.DoneCallback();
	}

}
