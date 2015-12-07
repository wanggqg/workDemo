package com.cm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Set;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import com.cm.config.UpdateConfig;
import com.cm.update.StorageMgr;

public class FileListManager {

	static FileListManager _fileListManager = null;
	Activity _activity = null;
	public Hashtable<String, String> addFileListUrlAndSize;
	public Hashtable<String, String> addFileList;

	public static FileListManager getInstance() {
		if (_fileListManager == null) {
			_fileListManager = new FileListManager();
		}
		return _fileListManager;
	}

	public void initContext(Activity context) {
		_activity = context;
	}

	// =================================================

	public Hashtable<String, String> readFileList(InputStream open) {
		InputStreamReader isReader = new InputStreamReader(open);
		BufferedReader breader = new BufferedReader(isReader);
		String line = null;
		Hashtable<String, String> fileList = new Hashtable<String, String>();
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
				fileList.put(s[0], s[1]);
			}
		}

		try {
			breader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			isReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			open.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileList;
	}

	/**
	 * 读取fileList
	 * 
	 * @param data
	 *            fileList的源数据
	 * @return 返回 文件名，MD5和文件大小 的Hashtable
	 * @throws IOException
	 */
	public Hashtable<String, String> readFileList(byte[] data) {
		Hashtable<String, String> fileList = new Hashtable<String, String>();

		String s1 = new String(data);
		String[] split = s1.split("\n");

		for (int i = 0; i < split.length; i++) {
			String item = split[i];
			if (item == null || item.length() < 5) {
				Log.d("updater", "item is error,please chect:" + item);
				continue;
			}
			String[] itemKeyvalue = item.split("=");
			if (itemKeyvalue.length < 2) {
				Log.d("updater", "item is error,please chect:" + item);
				continue;
			}
			String key = itemKeyvalue[0];
			String value = itemKeyvalue[1];
			if (key == null || value == null) {
				Log.d("updater", "item is error,please chect:" + item);
				continue;
			}

			key = key.trim();
			value = value.trim();
			fileList.put(key, value);
		}

		return fileList;
	}

	public void removeFilesByApkFilelist() {
		String path = StorageMgr.GetStreamingAssetPath().replace("assets/", "");
		try {
			Hashtable<String, String> sdFileLists = null;
			Hashtable<String, String> apkFileLists = null;
			StringBuffer sbuf = new StringBuffer(1024 * 1024);
			AssetManager manager = _activity.getAssets();
			File sdfileList = new File(StorageMgr.GetStreamingAssetPath() + UpdateConfig.sFileList);
			if (!sdfileList.exists() || !sdfileList.isFile()) {
				return;
			}
			if (!StorageMgr.HasBundleData(_activity, UpdateConfig.sFileList)) {
				return;
			}
			Log.d("updater", "read fileList from sd");
			FileInputStream fileInputStream = new FileInputStream(sdfileList);
			sdFileLists = readFileList(fileInputStream);
			apkFileLists = readFileList(manager.open(UpdateConfig.sFileList));
			Set<String> keySet = apkFileLists.keySet();
			for (String key : keySet) {

				if (zipKeyIsInTable(sdFileLists, key)) {
					// 如果都存在，删除外部的
					if (!deleteFile(path, key)) {
						Log.e("updater", "deletefile from sd failure! " + key);
						continue;
					}

					removeZipKey(sdFileLists, key);
				}
			}

			keySet = apkFileLists.keySet();
			for (String key : keySet) {
				if (!zipKeyIsInTable(sdFileLists, key)) {
					sdFileLists.put(key, apkFileLists.get(key));
				}
			}

			// sdFileLists.putAll(apkFileLists);

			Set<String> keySet2 = sdFileLists.keySet();
			for (String string : keySet2) {
				Log.d("updater", "====" + string);
				sbuf.append(string + "=" + sdFileLists.get(string) + "\r\n");
			}
			FilesManager.getInstance().saveFileList(sbuf);

			fileInputStream.close();
		} catch (IOException e) {
			Log.e("updater", "removeFiles error: " + e.toString());
		}
	}

	private boolean deleteFile(String path, String key) {
		String filePath = path + key;
		filePath = filePath.replace("\\", "/");
		filePath = filePath.replace(".zip", "");
		File file = new File(filePath);
		if (file.exists() && !file.isDirectory()) {
			if (file.delete()) {
				Log.d("updater", "delete file is ok: " + filePath);
				return true;
			}
		} else {
			Log.e("updater", "delete file is fail: " + filePath);
		}
		return false;
	}

	// ============================================================
	/**
	 * 比对本地和远程的fileList文件
	 * 
	 * @throws IOException
	 */
	public int calcFileListDiff(byte[] data) {

		// 需要下载的集合
		addFileList = new Hashtable<String, String>();
		addFileListUrlAndSize = new Hashtable<String, String>();
		// 本地的集合
		Hashtable<String, String> sFileLists = null;
		// 远程的集合
		Hashtable<String, String> webFileLists = null;
		// 需要下载的大小
		int countSize = 0;

		AssetManager manager = _activity.getAssets();
		try {
			// 先读取本地的fileList
			File fileList = new File(StorageMgr.GetStreamingAssetPath() + UpdateConfig.sFileList);
			if (fileList.exists() && fileList.isFile()) {
				Log.d("updater", "read fileList from sd");
				FileInputStream fileInputStream = new FileInputStream(fileList);
				sFileLists = readFileList(fileInputStream);
			} else {
				Log.d("updater", "read fileList from apk");
				if (StorageMgr.HasBundleData(_activity, UpdateConfig.sFileList)) {
					sFileLists = readFileList(manager.open(UpdateConfig.sFileList));
				} else {
					// 如果sd卡上没有fileList.txt并且apk里也没有，在sd卡上创建一个，并且下载全部的资源
					sFileLists = new Hashtable<String, String>();
					boolean createFile = fileList.createNewFile();
					Log.d("updater", "mini包没有fileList时创建一个：" + createFile);
				}
			}
			// 读取远程的fileList
			webFileLists = readFileList(data);

			sameToFileList(sFileLists, webFileLists);

			// 这两步是需要下载的列表
			Set<String> webFileListKeys = webFileLists.keySet();
			for (String webFileListkey : webFileListKeys) {
				// 3、远程有的文件，本地没有的文件
				// if (!sFileLists.containsKey(webFileListkey)) {
				if (!zipKeyIsInTable(sFileLists, webFileListkey)) {
					addFileList.put(webFileListkey, webFileLists.get(webFileListkey));
					continue;
				}
				// 4、本地和远程都有但是hash值不同
				// if (zipKeyIsInTable(sFileLists, webFileListkey)) {
				else {
					// String sFileValue = sFileLists.get(webFileListkey);
					String sFileValue = zipKey2Value(sFileLists, webFileListkey);
					String wFileValue = zipKey2Value(webFileLists, webFileListkey);
					// 修改的,文件名相同md5值不一样
					if (!sFileValue.trim().equals(wFileValue.trim())) {
						// 文件名相同md5值不相同
						if (webFileListkey != null) {
							// Log.d("updater", "===sFileLists: "+sFileLists);
							addFileList.put(webFileListkey, webFileLists.get(webFileListkey));
						}
					}
				}
			}
			Set<String> keySet = addFileList.keySet();
			for (String key : keySet) {
				String kValue = addFileList.get(key).split("#")[1].trim();
				countSize += Integer.parseInt(kValue);
				addFileListUrlAndSize.put(key, kValue);
				// Log.d("updater", i + ",需要更新文件：" + key + " 更新大小：" + kValue +
				// "B");
			}

		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return countSize;
	}

	private void sameToFileList(final Hashtable<String, String> sFileLists, final Hashtable<String, String> webFileLists) {
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		AssetManager manager = _activity.getAssets();
		String assetPath = StorageMgr.GetStreamingAssetPath();
		StringBuffer sbuf = new StringBuffer();
		Set<String> sFileListKeys = sFileLists.keySet();
		for (String sFileListKey : sFileListKeys) {
			// 1、遍历本地fileList有的文件远程没有的文件
			sFileListKey = sFileListKey.trim();
			// if (!webFileLists.containsKey(sFileListKey)) {
			if (!zipKeyIsInTable(webFileLists, sFileListKey)) {
				if (isExits(manager, assetPath, sFileListKey)) {
					sbuf.append(sFileListKey).append("=").append(sFileLists.get(sFileListKey)).append("\n");
				}
				continue;
			}
			// 2、找出2者文件相同的
			if (zipKeyIsInTable(webFileLists, sFileListKey)) {
				if (zipKey2Value(webFileLists, sFileListKey).trim().equals(zipKey2Value(sFileLists, sFileListKey).trim())) {
					if (isExits(manager, assetPath, sFileListKey)) {
						sbuf.append(tryGetRightKey(sFileLists, webFileLists, sFileListKey)).append("=").append(sFileLists.get(sFileListKey)).append("\n");
					}
				} else {
					if (isExits(manager, assetPath, sFileListKey)) {
						if (VersionUtil.LargerThan("1.2.13", UpdateManager.getInstance().mClientVersion.clientVersion)) {
							Log.d("updater", "clientVersion: " + UpdateManager.getInstance().mClientVersion.clientVersion);
							Log.d("updater", "sFileListKey: " + sFileListKey);
							String zipKey2Value = zipKey2Value(webFileLists, sFileListKey);
							String fileMD5 = FilesManager.getInstance().getFileMD5(sFileListKey);
							if (zipKey2Value != null && fileMD5 != null) {
								if (zipKey2Value.trim().equals(fileMD5.trim())) {
									Log.d("updater", "not need to update, because remote md5 is same as the local real file: " + sFileListKey);
									sbuf.append(tryGetRightKey(sFileLists, webFileLists, sFileListKey)).append("=").append(sFileLists.get(sFileListKey)).append("\n");
								}
							}
						}
					}
				}
			}
		}
		// 这两步写到本地fileList
		FilesManager.getInstance().saveFileList(sbuf);
		// }
		// }).start();
	}

	private String removeZipKey(Hashtable<String, String> tbl, String key) {
		String r;
		if (key.endsWith(".zip")) {
			r = tbl.remove(key);
			if (r == null)
				return tbl.remove(key.replace(".zip", ""));
		} else {
			r = tbl.remove(key);
			if (r == null)
				return tbl.remove(key + ".zip");
		}

		return r;
	}

	private String tryGetRightKey(Hashtable<String, String> l, Hashtable<String, String> r, String lkey) {
		if (!lkey.endsWith(".zip")) {
			if (l.contains(lkey) && r.containsKey(lkey + ".zip")) {
				return lkey + ".zip";
			}

		} else {
			if (l.contains(lkey) && r.containsKey(lkey.replace(".zip", ""))) {
				return lkey.replace(".zip", "");
			}
		}

		return lkey;
	}

	private String zipKey2Value(Hashtable<String, String> tbl, String key) {
		String r = tbl.get(key);

		if (key.endsWith(".zip")) {
			if (r == null)
				r = tbl.get(key.replace(".zip", ""));

		} else {
			if (r == null)
				r = tbl.get(key + ".zip");
		}
		if (r != null) {
			String[] split = r.split("#");
			if (split.length == 2) {
				r = split[0];
			}
		}
		return r;
	}

	private boolean zipKeyIsInTable(Hashtable<String, String> tbl, String key) {
		if (key.endsWith(".zip"))
			return tbl.containsKey(key) || tbl.containsKey(key.replace(".zip", ""));
		else
			return tbl.containsKey(key) || tbl.containsKey(key + ".zip");
	}

	private boolean isExits(AssetManager manager, String assetPath, String sFileListKey) {
		// 需要判断这个文件是否真的在本地，在的话记录，不在，不记录
		if (sFileListKey != null) {
			String filePath = sFileListKey.replace("\\", "/");
			// 兼容zip资源文件
			filePath = filePath.replace(".zip", "");
			String[] splits = filePath.split("assets/");
			if (splits.length != 2) {
				return false;
			}
			filePath = splits[1];
			String path = assetPath + filePath;
			File file = new File(path);
			if (file.exists() && !file.isDirectory()) {
				return true;
			}
			try {
				InputStream is = manager.open(filePath);
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
