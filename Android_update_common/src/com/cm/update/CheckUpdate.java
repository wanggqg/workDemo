package com.cm.update;

import android.app.Activity;
import android.util.Log;

import com.cm.download.WWWRetryDownload;
import com.cm.interfaces.IDownloadFileCallback;
import com.cm.utils.DownLoadManager;
import com.cm.utils.UpdateManager;
import com.cm.utils.VersionUtil;
import com.unity3d.player.UnityPlayer;

public class CheckUpdate {
	/**
	 * 游戏中返回登录调用此方法
	 */
	public String GetNeedUpdate(final String gameObject, final String method) {
		Log.d("Unity", "====GetNeedUpdate");

		Activity activity = UnityPlayer.currentActivity;
		UpdateManager.getInstance().initContext(activity);
		DownLoadManager.getInstance().initContext(activity);
		UpdateView.getInstance().initContext(activity);

		// 首先去下载update.xml
		UpdateManager.getInstance().readConfig();

		String updateUrl = UpdateManager.getInstance().mGameConfig.getUpdateXmlUrl();
		downloadUpdateXml(updateUrl);

		return "0";
	}

	private void downloadUpdateXml(String updateUrl) {
		WWWRetryDownload wwwRetryDownload = new WWWRetryDownload(updateUrl, "", new IDownloadFileCallback() {
			@Override
			public void FailCallback(String url, String error) {
				Log.e("updater", "GetNeedUpdate downloadUpdateXml FailCallback");
			}

			@Override
			public void DoneCallback(String url, byte[] data) {
				if(UpdateManager.getInstance().mUpdateXml.parseUpdate(data)){
					checkGameResNeedUpdate();
				}else{
					Log.e("updater", "GetNeedUpdate downloadUpdateXml DoneCallback paserupdate error");
				}
			}
		});

		wwwRetryDownload.downLoadFile(1,6000);
	}

	private void checkGameResNeedUpdate() {
		String res_version = UpdateManager.getInstance().mUpdateXml.mNewResversion;
		String client_version = UpdateManager.getInstance().mClientVersion.clientVersion;
		if (VersionUtil.LargerThan(res_version, client_version)) {
			UpdateView.getInstance().ShowRestart();
		}
	}
}
