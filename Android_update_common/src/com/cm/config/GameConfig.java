package com.cm.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.util.Log;

public class GameConfig {

	String mPlatformName = "";
	String mUpdateXmlUrl = "";
	String mServerListUrl = "";
	String mGameId = "";
	String mGameKey = "";
	public String mTalkingdataTractEnable = "1";

	/**
	 * 读取gameConfig文件
	 */
	public boolean readGameConfig(ContextWrapper context) {
		AssetManager manager = context.getAssets();
		boolean bReadGameConfigSuccess = true;
		try {
			InputStreamReader isReader = new InputStreamReader(manager.open(UpdateConfig.sGameConfig));
			BufferedReader breader = new BufferedReader(isReader);
			String line = null;
			while (true) {
				try {
					line = breader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				if (line == null) {
					break;
				}
				String[] s = line.split("=");
				if (s.length == 2) {
					for (int i = 0; i < s.length; i++)
						s[i] = s[i].trim();

					if (s[0].equals("PlatformCode") == true) {
						mPlatformName = s[1];
					} else if (s[0].equals("Update") == true) {
						mUpdateXmlUrl = s[1];
					} else if (s[0].equals("ServerListURL") == true) {
						mServerListUrl = s[1];
					} else if (s[0].equals("GameID") == true) {
						mGameId = s[1];
					} else if (s[0].equals("GameKey") == true) {
						mGameKey = s[1];
					} else if (s[0].equals("TalkingdataTractEnable") == true) {
						mTalkingdataTractEnable = s[1];
					}
				}
			}
			breader.close();
			isReader.close();
		} catch (IOException e) {
			Log.e("updater", "can't read " + UpdateConfig.sGameConfig);
			e.printStackTrace();
			bReadGameConfigSuccess = false;
		}
		Log.d("updater", "mUpdateUrl:" + mUpdateXmlUrl);
		Log.d("updater", "mPlatformName:" + mPlatformName);
		return bReadGameConfigSuccess;
	}

	public String GetServerlistUrl() {
		return mServerListUrl;
	}

	public String getUpdateXmlUrl() {
		return mUpdateXmlUrl;
	}

}
