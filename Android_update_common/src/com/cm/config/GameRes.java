package com.cm.config;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;

public class GameRes {

	static GameRes _gameRes = null;
	Activity _activity = null;
	
	public static GameRes getInstance(){
		if(_gameRes == null){
			_gameRes = new GameRes();
		}
		return _gameRes;
	}
	
	public void initContext(Activity context){
		_activity = context;
	}
	
	int GetResourceId(String type, String name) {
		if(_activity == null){
			Log.e("Updater", "GameText activity is null, please check it");
			return 0;
		}
		
		Resources localResources = _activity.getResources();
		String packageName = _activity.getPackageName();

		return localResources.getIdentifier(name, type, packageName);
	}
	
	public int GetResTextID(String name){
		return GetResourceId("string", name);
	}
	
	public int GetViewObjID(String name){
		return GetResourceId("id", name);
	}
	public int GetViewLayoutID(String name){
		return GetResourceId("layout", name);
	}
}
