package com.cm.utils;

/**
 * 修正平台日志新接口
 * */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

import com.cm.config.UpdateConfig;
import com.cm.update.EventStat;

public class KeepLog {

	public static int EVENT_STARTAPP = 1;
	public static int EVENT_ZIPACTION = 2;

	public static int EVENT_STARTUNITY = 3;
	
	
	public static int EVENT_GUIDER = 4;
	public static int EVENT_QUITGAME = 5;
	public static int EVENT_ERROR = 6;
	public static int EVENT_STORAGE_WARNING = 7;

//	public static int EVENT_STEP_ExtractRawPatchToSD = 1;
//	public static int EVENT_STEP_ExtractRawPatchOK = 2;
//	public static int EVENT_STEP_ExtractRawPatchERROR = 3;
//	public static int EVENT_STEP_UNZIPRawPatchOK = 4;
//	public static int EVENT_STEP_UNZIPRawPatchERROR = 5;
//
//	public static int EVENT_STEP_DOWNLOADPATCH_BEGIN = 6;
//	public static int EVENT_STEP_DOWNLOADPATCH_FINISH = 7;
//	public static int EVENT_STEP_DOWNLOADPATCH_ERROR = 8;
//	public static int EVENT_STEP_UNZIPUpdatePatchOK = 9;
//	public static int EVENT_STEP_UNZIPUpdatePatchERROR = 10;

	public static int EVENT_STEP_DOWNLOADSERVERLIST_ERROR = 11;
	public static int EVENT_STEP_DOWNLOADSERVERLIST_OK = 12;
	
	public static int EVENT_STEP_DOWNLOADUPDATE_ERROR = 13;
	public static int EVENT_STEP_DOWNLOADUPDATE_OK = 14;
	
	public static int EVENT_STEP_DOWNLOADFILELIST_ERROR = 15;
	public static int EVENT_STEP_DOWNLOADFILELIST_OK = 16;
	public static int EVENT_STEP_DOWNLOADFILELIST_BEGIN = 17;
	public static int EVENT_STEP_DOWNLOADFILELIST_NONEED = 33;

	
	public static int EVENT_STEP_DOWNLOADFILES_ERROR = 18;
	public static int EVENT_STEP_DOWNLOADFILES_OK = 19;
	public static int EVENT_STEP_DOWNLOADFILES_BEGIN = 20;
	
	public static int EVENT_STEP_COPYRESTOSD_BEGIN = 26;
	public static int EVENT_STEP_COPYRESTOSD_OK = 27;
	public static int EVENT_STEP_COPYRESTOSD_ERROR = 28;
	
	public static int EVENT_STEP_DOWNLOADAPK_BEGIN = 21;
	public static int EVENT_STEP_DOWNLOADAPK_OK = 22;
	public static int EVENT_STEP_DOWNLOADAPK_ERROR = 23;
	public static int EVENT_STEP_INSTALLAPK = 29;
	
	public static int EVENT_STEP_PASERUPDATE_OK = 24;
	public static int EVENT_STEP_PASERUPDATE_ERROR = 25;
	
	public static int EVENT_STEP_SHOWALERT = 32;
	public static int EVENT_STEP_ACTIVITY_ONPAUSE = 30;
	public static int EVENT_STEP_ACTIVITY_ONRESUME = 31;
	
//	public static int EVENT_STEP_UPDATE = 19;
//	public static int EVENT_STEP_SERVERLIST = 20;
	// pId (value range:1-99)*1000000
	// eId (value range :1-9999)*100
	// subId (value range :1-99)*1

//	private static String gameId;
	
	static KeepLog _keepLog = null;
	private Activity _activity = null;
	private String gameId = "";
	public static KeepLog getInstance(){
		if(_keepLog == null){
			_keepLog = new KeepLog();
		}
		return _keepLog;
	}
	
	public void initContext(Activity context){
		_activity = context;
	}

	public int getEventId(int pid, int eid, int sid) {
		return pid * 1000000 + eid * 100 + sid;
	}

	public void updateLog(int pid, int eid,String desc) {
		if (TextUtils.isEmpty(gameId)) {
			try {
				AssetManager manager = _activity.getAssets();
				InputStream stream = manager.open(UpdateConfig.sGameConfig);
				InputStreamReader isReader = new InputStreamReader(stream);
				BufferedReader breader = new BufferedReader(isReader);

				String line;
				while ((line = breader.readLine()) != null) {
					String[] params = line.split("=");
					if (params.length > 1) {
						String param = params[0];
						param = param.trim();
						if (param.equals("GameID")) {
							param = params[1];
							gameId = param.trim();// Integer.parseInt(param.trim());
							break;
						}
					}
				}
				breader.close();
				isReader.close();
				stream.close();
				Log.d("updater", "GameID=" + gameId);
			} catch (IOException e) {
				Log.d("updater", "can't find " + UpdateConfig.sGameConfig);
			} catch (NumberFormatException e) {
				Log.d("updater", e.toString());
			}
		}
		if (pid == EVENT_ERROR) {
			desc += "," + getDeviceInfo();
		}

		desc = "android:" + desc;
		Log.d("updater", desc);

		int eventId = getEventId(pid, 0, eid);
		EventStat.getInstance().onEvent(_activity,eventId+"",pid+"",desc,gameId,"");
	}

	public String getDeviceInfo() {
		Activity context = _activity;
		String info = "";
		info += "PhoneOs=" + android.os.Build.VERSION.RELEASE + ",";

		String ApplicationVersion = "";
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo pkginfo = manager.getPackageInfo(
					context.getPackageName(), 0);
			ApplicationVersion = pkginfo.versionName;
		} catch (Exception e) {
			Log.d("updater", "pkginfo is null");
		}

		info += "ApplicationVersion=" + ApplicationVersion + ",";

		String PhoneUnique = "";
		String androidId = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);

		if (androidId == null) {
			PhoneUnique = UUID.randomUUID().toString();
		} else {
			if (!"9774d56d682e549c".equals(androidId)) {
				PhoneUnique = androidId;
			} else {
				PhoneUnique = UUID.randomUUID().toString();
			}
		}

		info += "PhoneUnique=" + PhoneUnique + ",";
		info += "PhoneCpu=" + android.os.Build.CPU_ABI + ",";
		info += "PhoneModel=" + android.os.Build.BRAND
				+ android.os.Build.PRODUCT;

		return info;

	}
}
