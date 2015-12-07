package com.cm.utils;

import java.io.File;

import android.app.Activity;
import android.util.Log;

import com.cm.config.UpdateConfig;
import com.cm.update.StorageMgr;
import com.cm.update.UpdateActivity;

public class ResManager {

	static ResManager _resManager = null;
	Activity _activity = null;

	public static ResManager getInstance() {
		if (_resManager == null) {
			_resManager = new ResManager();
		}
		return _resManager;
	}

	public void initContext(Activity context) {
		_activity = (UpdateActivity) context;
	}

	public void setResStatus(boolean status) {
		String resfilePath = StorageMgr.GetStreamingAssetPath() + UpdateConfig.sResFile;
		if (status == true) {
			// 创建SD上res.txt
			StorageMgr.createFile(resfilePath);
			Log.d("updater", "res.txt create");
		} else {
			// 如果SD上存在res.txt，则删除res.txt
			File resfile = new File(resfilePath);
			if (resfile.exists()) {
				if(resfile.delete()){
					Log.d("updater", "res.txt delete");
				}
			}
		}
	}

	public boolean isAPKMini() {
		// 判断APK包内是否存在MiniPackage.txt是否存在
		return StorageMgr.HasBundleData(_activity, UpdateConfig.sMiniPackage);
	}

	public boolean isGameResFull() {
		// 如果下载中，再次启动游戏，不允许进入游戏
		// boolean isUpdateIng =
		// ShareObjectManager.getInstance().getBoolean("isUpdateIng", false);
		// Log.d("updater", "isUpdateIng: " + isUpdateIng);
		// boolean isResFull = (isUpdateIng==false);
		// return isResFull;
		String resfilePath = StorageMgr.GetStreamingAssetPath() + UpdateConfig.sResFile;
		File file = new File(resfilePath);
		if (file.exists()) {
			return true;
		}
		return false;
	}

}
