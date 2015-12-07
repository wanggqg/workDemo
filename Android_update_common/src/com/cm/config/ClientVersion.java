package com.cm.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

import com.cm.update.StorageMgr;
import com.cm.utils.UpdateManager;

public class ClientVersion {

	public String clientVersion = "0.0.0";
	String clientVersionPath = StorageMgr.GetStreamingAssetPath() + UpdateConfig.sClientVersion;

	public void readClientVersion() {
		File file = new File(clientVersionPath);
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
							clientVersion = s[1];
						}
					}
				}
				breader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			clientVersion = UpdateManager.getInstance().mGameDefine.appBundleVersion;
		}
		Log.d("updater", "readClientVersion: " + clientVersion);
	}

	public void updateClientVersion(String version) {
		clientVersion = version;
		File file = new File(clientVersionPath);
		if (!file.exists() || file.isDirectory()) {
			try {
				boolean createClientVersionFile = file.createNewFile();
				Log.d("Unuty", "createClientVersionFile: " + createClientVersionFile);
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
}
