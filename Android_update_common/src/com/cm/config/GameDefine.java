package com.cm.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.cm.update.StorageMgr;
import com.cm.utils.VersionUtil;

public class GameDefine {
	private String gameDefinePath = StorageMgr.GetStreamingAssetPath() + UpdateConfig.sGameDefine;
	public String appBundleVersion = "0.0.0";
	public String appSdVersion = "0.0.0";

	public void updateGameDefine(String version) {
		File file = new File(gameDefinePath);
		if (!file.exists() || file.isDirectory()) {
			try {
				boolean createGameDefineFile = file.createNewFile();
				Log.d("Unuty", "createGameDefine: " + createGameDefineFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileWriter writer = new FileWriter(file);
			writer.write("Version = " + version);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public String readGameDefine(ContextWrapper context) {
		//获取BundleVersion
		PackageManager pm = context.getPackageManager();
		PackageInfo info;
		try {
			info = pm.getPackageInfo(context.getPackageName(), 0);
			appBundleVersion = info.versionName;
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		// 这里测试加了一位，1.0.4.8，去掉最后一位版本号
		appBundleVersion = VersionUtil.modifyBundleVersion(appBundleVersion);
		Log.d("updater", "readGameDefine appBundleVersion: "+appBundleVersion);
		//获取appSdVersion
		File file = new File(gameDefinePath);
		if (file.exists() && !file.isDirectory()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isReader = new InputStreamReader(fis);
				BufferedReader breader = new BufferedReader(isReader);
				String line;
				while ((line = breader.readLine()) != null) {
					String[] s = line.split("=");
					if (s.length == 2) {
						for (int i = 0; i < s.length; i++)
							s[i] = s[i].trim();
						if (s[0].equals("Version") == true) {
							appSdVersion = s[1];
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.d("updater", "getGameDefineVersion: " + appSdVersion);
		return appSdVersion;
	}
	// private boolean writeGameDefine() {
	// Log.d("updater", "写入gameDefin to sd" + mConfig.GetVersion());
	// String path = StorageMgr.GetStreamingAssetPath() +
	// UpdateConfig.sGameDefine;
	// File file = new File(path);
	// if (file != null) {
	// FileWriter writer = new FileWriter(file);
	// String getVersion = mConfig.GetVersion();
	// writer.write("Version = " + getVersion);
	// writer.flush();
	// writer.close();
	// }
	// return true;
	// }

	// DefineValue ReadGameDefine(InputStream stream) {
	// DefineValue dfv = new DefineValue();
	//
	// InputStreamReader isReader = new InputStreamReader(stream);
	// BufferedReader breader = new BufferedReader(isReader);
	//
	// String line = null;
	//
	// while (true) {
	// try {
	// line = breader.readLine();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// if(line == null){
	// break;
	// }
	//
	// String[] s = line.split("=");
	// if (s.length == 2) {
	// for (int i = 0; i < s.length; i++)
	// s[i] = s[i].trim();
	//
	// if (s[0].equals("Version") == true) {
	// dfv.mVersion = s[1];
	// }
	// }
	// }
	//
	// try {
	// breader.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// try {
	// isReader.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// return dfv;
	// }

}
