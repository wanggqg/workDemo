package com.cm.update;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.cm.config.GameRes;
import com.cm.interfaces.IAlertBoxCallback;
import com.cm.interfaces.ICheckUpdateCallBack;
import com.cm.interfaces.IOperateResult;
import com.cm.utils.DownLoadManager;
import com.cm.utils.FileListManager;
import com.cm.utils.FilesManager;
import com.cm.utils.KeepLog;
import com.cm.utils.NetWorkManager;
import com.cm.utils.ResManager;
import com.cm.utils.ShareObjectManager;
import com.cm.utils.UpdateManager;

public class UpdateActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTheame();
		setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}
		Log.d("updater", "onCreate");
		// 初始化管理类的Context环境
		UpdateView.getInstance().initContext(this);
		GameRes.getInstance().initContext(this);
		NetWorkManager.getInstance().initContext(this);
		FilesManager.getInstance().initContext(this);
		FileListManager.getInstance().initContext(this);
		ShareObjectManager.getInstance().initContext(this);
		DownLoadManager.getInstance().initContext(this);
		StorageMgr.initContext(this);
		KeepLog.getInstance().initContext(this);
		UpdateManager.getInstance().initContext(this);
		ResManager.getInstance().initContext(this);

		// 初始化更新UI界面
		UpdateView.getInstance().initView();
		//读取配置文件
		UpdateManager.getInstance().readConfig();

		EventStat.getInstance().initTalkingdata(this);
		// 启动前先检测测网络是否正常
		if (NetWorkManager.getInstance().getNetWorkStatus()) {
			UpdateManager.getInstance().CheckUpdate(new ICheckUpdateCallBack() {
				@Override
				public void DoneCallback() {
					StartUnity();
				}
			});
		} else {
			// 这里提示玩家手机网络异常，请检查网络的Toast提示
			UpdateView.getInstance().showAlert("cm_nonet", "cm_setnet", "cm_ok", "cm_no", new IAlertBoxCallback() {
				@Override
				public void buttonYESClickCallback() {
					Intent intent = new Intent(Settings.ACTION_SETTINGS);
					startActivity(intent);
					android.os.Process.killProcess(android.os.Process.myPid());
				}

				@Override
				public void buttonCancelClickCallback() {
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			});

		}
	}

	// =Activity事件相关==============================================================
	@Override
	public void onBackPressed() {
		Log.d("updater", "onBackPressed");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("updater", "onKeyDown");
		return false;
	}

	// =========================================================================
	void StartUnity() {

		// 这里需要判断资源完整性
		if (ResManager.getInstance().isGameResFull() == false) {
			UpdateView.getInstance().showErrorAlert("cm_getresource_error");
			return;
		}

		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTUNITY, 0, "StartUnity");

		try {
			Class<?> activity = Class.forName("com.lk.sdk.MainActivity");
			if (activity != null) {
				StartActivity(activity);
				return;
			}
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		}

		Log.d("updater", "StartUnityPlayer");

		StartActivity(UnityPlayerNativeActivity.class);
	}

	void StartActivity(Class<?> activityClass) {
		Intent intent = new Intent(UpdateActivity.this, activityClass);
		String apkpath = StorageMgr.GetAssetPath();
		intent.putExtra("apkpath", apkpath);
		ShareObjectManager.getInstance().putString("version", UpdateManager.getInstance().mClientVersion.clientVersion);
		ShareObjectManager.getInstance().putString("updateinfourl", UpdateManager.getInstance().mUpdateXml.mUpdateInfoUrl);
		ShareObjectManager.getInstance().putString("mIsShowSpecial", UpdateManager.getInstance().mUpdateXml.isShowSpecial());
		Log.d("updater", "apkpath:" + apkpath);

		invalidActivity();
		startActivity(intent);

		finish();
	}

	public void invalidActivity() {
		Log.d("updater", "UpdateActivity deinit");
		System.gc();
	}

	public Handler mHandler = new Handler() {
		int current = 0;
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case IOperateResult.UpdateProgress: 
				int read = msg.arg1;
				current += read;
				UpdateView.getInstance().setProgress(current, msg.arg2);
				break;
			case IOperateResult.DownloadApking: 
				UpdateView.getInstance().setProgress(msg.arg1, msg.arg2);
				break;
			case IOperateResult.ExtractFilesToSDing:
				UpdateView.getInstance().showCopyFilesProgress(msg.arg1, msg.arg2);
				break;
			}
		}
	};
	
	@Override
	protected void onPause() {
		Log.d("updater", "onPause");
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTUNITY, KeepLog.EVENT_STEP_ACTIVITY_ONPAUSE, "onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d("updater", "onResume");
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTUNITY, KeepLog.EVENT_STEP_ACTIVITY_ONRESUME, "onResume");
		super.onResume();
	}
	/**
	 * 设置不同sdk版本，显示不同的ui主题
	 *
	 */
	private void setTheame() {
		int sdkInt = Build.VERSION.SDK_INT;
		Log.d("Unity", "sdkInt" + sdkInt);
		if (sdkInt >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && sdkInt <= Build.VERSION_CODES.KITKAT) {
			Log.d("Unity", "sdkInt >= Build.VERSION_CODES.ICE_CREAM_SANDWICH&& sdkInt <= Build.VERSION_CODES.KITKAT");
			setTheme(android.R.style.Theme_Holo);
		} else if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
			Log.d("Unity", "sdkInt >= Build.VERSION_CODES.ICE_CREAM_SANDWICH&& sdkInt <= Build.VERSION_CODES.KITKAT");
			setTheme(android.R.style.Theme_Material);
		} else {
			setTheme(16973831);
		}
	}
}
