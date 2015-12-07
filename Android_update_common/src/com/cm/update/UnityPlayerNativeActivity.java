package com.cm.update;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.oksdk.helper.OKUnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

public class UnityPlayerNativeActivity extends OKUnityPlayerActivity
{
	protected UnityPlayer mUnityPlayer;		// don't change the name of this variable; referenced from native code
	protected boolean isStarting = true;
	protected long timeEclapse=0;
	Context mContext = null;
	// UnityPlayer.init() should be called before attaching the view to a layout - it will load the native code.
	// UnityPlayer.quit() should be the last thing called - it will unload the native code.
	protected void onCreate (Bundle savedInstanceState)
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setTheame();
		String path = this.getIntent().getStringExtra("apkpath");
		
		if(path ==null){
			Log.d("updater", "apkpath is null,can't init Unity enviroment");
			android.os.Process.killProcess(android.os.Process.myPid());
			return;
		}
		
		isStarting = true;
		
		getWindow().takeSurface(null);
		setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
		getWindow().setFormat(PixelFormat.RGB_565);

		Boolean bUpdate = this.getIntent().getBooleanExtra ("bUpdateServerList",false);
		
		//Log.d("updater", "apkpath:"+path);
		Log.d("updater", "bUpdateServerList:"+bUpdate);
		
		mUnityPlayer = new UnityPlayer(this, path);

		if (mUnityPlayer.getSettings ().getBoolean ("hide_status_bar", true)){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			getWindow().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		int glesMode = mUnityPlayer.getSettings().getInt("gles_mode", 1);
		boolean trueColor8888 = false;
		mUnityPlayer.init(glesMode, trueColor8888);

		View playerView = mUnityPlayer.getView();
		setContentView(playerView);
		playerView.requestFocus();
		mContext = this;
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	@Override
	public void onBackPressed() {
		Log.d("updater", "UnityPlayerNativeActivity:onBackPressed");
		
		if(isStarting){
			long now_t = System.currentTimeMillis();
			long ts = now_t - timeEclapse;
			if(ts<30000){
				return;
			}
			else{
				isStarting = false;
			}
		}
		else{
			super.onBackPressed();
		}
	}

	protected void onDestroy ()
	{
		Log.d("updater", "UnityPlayerNativeActivity:onDestroy");
		mUnityPlayer.quit();
		super.onDestroy();
	}

	// onPause()/onResume() must be sent to UnityPlayer to enable pause and resource recreation on resume.
	protected void onPause()
	{
		Log.d("updater", "UnityPlayerNativeActivity:onPause");
		super.onPause();
		mUnityPlayer.pause();
	}
	protected void onResume()
	{
		Log.d("updater", "UnityPlayerNativeActivity:onResume");
		super.onResume();
		mUnityPlayer.resume();
	}
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		//Log.d("updater", "UnityPlayerNativeActivity:dispatchKeyEvent:"+event.getKeyCode()+",key="+event.getNumber());
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.onKeyMultiple(event.getKeyCode(), event.getRepeatCount(), event);
		return super.dispatchKeyEvent(event);
	}
	public void startSelectPhotoActivity(String objName,String id){
		Log.d("updater", "startSelectPhotoActivity");
		Intent intent = new Intent(mContext, SelectPhotoActivity.class);
		intent.putExtra("name", objName);
		intent.putExtra("id", id);
		this.startActivity(intent);
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
