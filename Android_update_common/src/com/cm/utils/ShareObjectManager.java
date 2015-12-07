package com.cm.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ShareObjectManager {

	Activity _activity = null;
	
	private SharedPreferences sp;
	private Editor edit;
	
	static ShareObjectManager _shareObjectManager = null;
	
	public static ShareObjectManager getInstance(){
		if(_shareObjectManager == null){
			_shareObjectManager = new ShareObjectManager();
		}
		return _shareObjectManager;
	}
	
	public void initContext(Activity context){
		_activity = context;
		initSharePreference();
	}
	
	private void initSharePreference() {
		sp = _activity.getSharedPreferences("recordFileList", Activity.MODE_PRIVATE);
		edit = sp.edit();
	}
	
	public boolean getBoolean(String key,boolean value){
		return sp.getBoolean(key, value);
	}
	
	public void putBoolean(String key,boolean value){
		edit.putBoolean(key, value);
		edit.commit();
	}
	
	public void putString(String key,String value){
		edit.putString(key, value);
		edit.commit();
	}
	
	public String getString(String key,String value){
		return sp.getString(key, value);
	}
}
