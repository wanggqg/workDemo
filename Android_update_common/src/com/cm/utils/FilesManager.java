package com.cm.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.cm.config.UpdateConfig;
import com.cm.exceptions.UpdateApkException;
import com.cm.interfaces.ICopyProgressCallback;
import com.cm.interfaces.IExtractFiles;
import com.cm.update.StorageMgr;
import com.cm.update.UpdateView;

public class FilesManager {

	static FilesManager _filesManager = null;
	Activity _activity = null;
	ReentrantLock lock = new ReentrantLock();

	public static FilesManager getInstance() {
		if (_filesManager == null) {
			_filesManager = new FilesManager();
		}
		return _filesManager;
	}

	public void initContext(Activity context) {
		_activity = context;
	}

	public void removeOldVersionSDFilesByApkFilelist() {
		if (StorageMgr.HasBundleData(_activity, UpdateConfig.sFileList)) {
			FileListManager.getInstance().removeFilesByApkFilelist();
		}
	}

	public void removeOldVersionSDFiles() {
		// delete assets
		String assetsPath = StorageMgr.GetStreamingAssetPath();
		Log.d("Updater", "RemoveOldVersionSDFiles from  " + assetsPath);
		StorageMgr.DeleteFolder(new File(assetsPath));

	}

	public void ExtractToSD(String fileinapk, String dest) {
		try {
			if (StorageMgr.HasBundleData(_activity, fileinapk) == false) {
				Log.d("Updater", "ExtractToSD failed, file not in apk (" + fileinapk + ")");
				return;
			}

			ContextWrapper context = (ContextWrapper) _activity;
			AssetManager manager = context.getAssets();

			InputStream is = null;
			try {
				is = manager.open(fileinapk);
			} catch (Exception e) {
				if (is != null)
					is.close();
				is = null;
			}

			if (is == null) {
				Log.e("Updater", "could not open file " + fileinapk);
				return;
			}

			String dest_file = dest + fileinapk;
			String dest_dir = dest_file.substring(0, dest_file.lastIndexOf('/'));

			// Log.d("updater", "ExtractToSD:dirpath="+dest_dir);
			// Log.d("updater", "ExtractToSD:filepath=" + dest_file);

			File folder = new File(dest_dir);
			if (!folder.exists()) {
				Boolean bRet = folder.mkdirs();
				if (!bRet) {
					Log.e("updater", "CreateDir failed : " + dest_dir);
					return;
				}
			}
			// 这里需要添加判断，如果sd上面已经存在该文件，则尝试将此文件删除，然后重新创建

			File targetfile = new File(dest_file);
			if (targetfile.exists() && targetfile.isFile()) {
				if (targetfile.delete()) {
					Log.d("updater", "Delete old targetfile:" + dest_file);
				}
			}
			if (targetfile.exists()) {
				Log.d("updater", "targetfile has exist:" + dest_file);
			} else {
				if (targetfile.createNewFile()) {
					// Log.d("updater", "Create targetfile success:"+dest_file);
				} else {
					Log.d("updater", "Create targetfile fail:" + dest_file);
				}
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(targetfile);
			} catch (Exception e) {
				if (fos != null)
					fos.close();
				fos = null;
				return;
			}

			int bufSize = 1024 * 1024;
			byte[] buf = new byte[bufSize];
			while (true) {
				int len;

				try {
					len = is.read(buf);
				} catch (Exception e) {
					Log.e("updater", "could not read from file " + fileinapk);

					if (is != null)
						is.close();
					is = null;
					if (fos != null)
						fos.close();
					fos = null;
					return;
				}

				if (len == -1)
					break;

				try {
					fos.write(buf, 0, len);
				} catch (Exception e) {
					Log.e("updater", "could not write to file " + targetfile);

					if (is != null)
						is.close();
					is = null;
					if (fos != null)
						fos.close();
					fos = null;
					return;
				}
			}

			if (is != null)
				is.close();
			is = null;
			if (fos != null)
				fos.close();
			fos = null;
			Log.d("updater", "ExtractToSD " + fileinapk + " to " + dest + "  ok");
		} catch (Exception e) {
			Log.e("updater", "something is wrong with ExtractToSD " + fileinapk + " to " + dest);
			e.printStackTrace();
		}
	}

	public void Delete(String path) {
		File file = new File(path);
		if (file.exists() && file.isFile() == true) {
			file.delete();
			Log.d("updater", "delete file:" + path);
		}
	}

	public String getFileMD5(String url) {
		synchronized (_filesManager) {
			MessageDigest digest = null;
			try {
				FileInputStream in = null;
				byte buffer[] = new byte[1024];
				int len;
				digest = MessageDigest.getInstance("MD5");
				String replace = url.replace("\\", "/");
				replace = replace.replace("assets/", "");
				replace = replace.replace(".zip", "");
				String path = StorageMgr.GetStreamingAssetPath() + replace;
				File file = new File(path);
				if (file == null || !file.isFile()) {
					Log.d("updater", "replace: " + replace);
					in = (FileInputStream) _activity.getAssets().open(replace);
				} else {
					in = new FileInputStream(file);
				}
				while ((len = in.read(buffer, 0, 1024)) != -1) {
					digest.update(buffer, 0, len);
				}
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			BigInteger bigInt = new BigInteger(1, digest.digest());
			return bigInt.toString(16);
		}
	}

	public boolean checkFileMd5(String url, String hashcode) {
		String string = FileListManager.getInstance().addFileList.get(url);
		String[] value = string.split("#");
		String[] split = url.split("assets\\\\");
		if (value.length != 2 || split.length != 2) {
			return false;
		}
		// 判断要下载的文件是否存在
		String path = StorageMgr.GetStreamingAssetPath() + split[1];
		String replace = path.replace("\\", "/");
		replace = replace.replace(".zip", "");
		File file = new File(replace);
		if (!file.exists() || file.isDirectory()) {
			return false;
		}
		// 判断下载的文件MD5和服务器的是否一致，这里java获取的MD5首位有0不能补，所以用contains
		if (!value[0].contains(hashcode)) {
			Log.e("updater", "url: " + url + " value[0]: " + value[0] + " hashcode: " + hashcode);
			return false;
		}
		return true;
	}

	/**
	 * 解压文件
	 * 
	 * @param url
	 * @throws Exception
	 * @throws UpdateApkException
	 */

	public boolean unZipFile(String url) throws Exception {
		url = StorageMgr.GetAssetPath() + url;
		url = url.replace("\\", "/");
		// Log.d("updater", "unZipFile: " + url);
		if (url != null && url.contains(".zip")) {
			String[] split = url.split(".zip");
			String path = "";
			if (split.length == 1) {
				path = split[0];
			}
			if (!TextUtils.isEmpty(path)) {
				ZipInputStream inZip = new ZipInputStream(new FileInputStream(url));
				ZipEntry zipEntry;
				String szName = "";
				while ((zipEntry = inZip.getNextEntry()) != null) {
					szName = zipEntry.getName();
					if (zipEntry.isDirectory()) {
						szName = szName.substring(0, szName.length() - 1);
						File folder = new File(path + File.separator + szName);
						folder.mkdirs();
					} else {
						File file = new File(path);
						file.createNewFile();
						FileOutputStream out = new FileOutputStream(file);
						int len;
						byte[] buffer = new byte[1024];
						while ((len = inZip.read(buffer)) != -1) {
							out.write(buffer, 0, len);
							out.flush();
						}
						out.close();
					}
				}
				inZip.close();
				File file = new File(url);
				file.delete();
			}
		}
		return true;
	}

	/**
	 * 以APK包内fileList.txt为基准，遍历判断SD卡上，该文件是否存在，如果不存在，则将APK包内的该文件，拷贝至SD上面。
	 */
	public void copyFilesInApkToSD(IExtractFiles callBack) {
		String[] splits;
		String path = "";
		File file;
		String assetPath = StorageMgr.GetStreamingAssetPath();
		// 记录需要拷贝的文件
		int fileNum = 0;
		int count = 0;
		AssetManager manager = _activity.getAssets();
		String fileListPath = StorageMgr.GetStreamingAssetPath() + UpdateConfig.sFileList;
		File fileList = new File(fileListPath);
		// 本地fileList存的集合
		Hashtable<String, String> sFileLists = null;
		try {
			// 如果外面有fileList，以外部的fileList为准
			if (fileList.exists() && !fileList.isDirectory()) {
				Log.d("updater", "read fileList.txt from sd");
				FileInputStream fileInputStream = new FileInputStream(new File(StorageMgr.GetStreamingAssetPath() + UpdateConfig.sFileList));
				sFileLists = FileListManager.getInstance().readFileList(fileInputStream);
			} else {
				Boolean hasFileListInApk = StorageMgr.HasBundleData(_activity, UpdateConfig.sFileList);
				// sd卡上有没有fileList.txt,没有将apk里的拷贝出去
				FilesManager.getInstance().ExtractToSD(UpdateConfig.sFileList, StorageMgr.GetStreamingAssetPath());
				if (hasFileListInApk) {
					sFileLists = FileListManager.getInstance().readFileList(manager.open(UpdateConfig.sFileList));
				} else {
					// 如果APK包内，SD卡，均没有FileList.txt，则表明安装的是APK壳包，直接启动资源下载
					callBack.completeCallback();
					return;
				}
			}
			Set<String> files = sFileLists.keySet();
			fileNum = files.size();
			for (String filePath : files) {
				count++;
				filePath = filePath.replace("\\", "/");
				// 兼容zip资源文件
				filePath = filePath.replace(".zip", "");
				splits = filePath.split("assets/");

				if (splits.length == 2) {
					filePath = splits[1];
				}

				path = assetPath + filePath;
				file = new File(path);

				if (!file.exists() || file.isDirectory()) {
					boolean copyFile = copyFile(_activity, filePath, file);
					if (copyFile) {
						callBack.doneCallback(count, fileNum);
					} else {
						Log.e("updater", "copy file to sd: " + file.getName());
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			callBack.extractFilesError();
			KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_COPYRESTOSD_ERROR, "COPYRESTOSD_ERROR");
		}
		callBack.completeCallback();
	}

	/**
	 * 拷贝文件
	 * 
	 * @param context
	 *            上下文
	 * @param filePath
	 *            源路径
	 * @param targetFile
	 *            目标路径文件对象
	 * @throws IOException
	 */

	private boolean copyFile(ContextWrapper context, String filePath, File targetFile) {
		// 判断当前文件所在文件夹是否存在，不存在创建
		String parentPath = targetFile.getParentFile().getAbsolutePath();
		File directory = new File(parentPath);

		if (!directory.exists() || !directory.isDirectory()) {
			boolean mkdirs = directory.mkdirs();
			if (!mkdirs) {
				Log.d("updater", "create directory failed, " + parentPath);
				return false;
			}
		}

		try {
			// 新建文件输入流并对它进行缓冲
			InputStream input = context.getAssets().open(filePath);
			BufferedInputStream inbuff = new BufferedInputStream(input);
			// 新建文件输出流并对它进行缓冲
			FileOutputStream out = new FileOutputStream(targetFile);
			BufferedOutputStream outbuff = new BufferedOutputStream(out);
			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len = 0;
			while ((len = inbuff.read(b)) != -1) {
				outbuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outbuff.flush();
			// 关闭流
			inbuff.close();
			outbuff.close();
			out.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void startCopyFilesInApkToSD(final ICopyProgressCallback iCopyProgressCallback) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_COPYRESTOSD_BEGIN, "COPYRESTOSD_BEGIN");
		new Thread(new Runnable() {
			@Override
			public void run() {
				copyFilesInApkToSD(new IExtractFiles() {
					@Override
					public void doneCallback(int current, int total) {
						iCopyProgressCallback.ProgressCallback(current, total);
					}

					@Override
					public void completeCallback() {
						KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_COPYRESTOSD_OK, "COPYRESTOSD_OK");
						iCopyProgressCallback.DoneCallback();
					}

					@Override
					public void extractFilesError() {
						UpdateView.getInstance().showErrorAlert("cm_copyrawpatch_error");
					}
				});
			}
		}).start();
	}

	public void extrafilesExtractToSD() {
		if (StorageMgr.HasBundleData(_activity, UpdateConfig.sExtraFiles) == false) {
			Log.e("updater", UpdateConfig.sExtraFiles + " not exist");
			return;
		}
		Log.d("updater", "开始移动extraconfig");
		try {
			InputStream inputStream = _activity.getAssets().open(UpdateConfig.sExtraFiles);
			if (inputStream != null) {
				Log.d("updater", UpdateConfig.sExtraFiles + " open sucess");

				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				String s = null;

				while ((s = br.readLine()) != null) {
					Log.d("updater", "读取到的路径 ： " + s);
					s = s.replace("\\", "/");
					s = s.replace("\r", "");
					String filename = s.replace("assets/extraconfig", "extraconfig");
					Log.d("updater", "移动文件： " + filename);

					// VersionUtil.ExtractToSD(UpdateActivity.this, filename,
					// StorageMgr.GetStreamingAssetPath());
					FilesManager.getInstance().ExtractToSD(filename, StorageMgr.GetStreamingAssetPath());

				}

				br.close();
				inputStream.close();

			} else {
				Log.d("updater", UpdateConfig.sExtraFiles + " not opened");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 拷贝APK包内的文件的配置文件到SD卡
	public void extractConfigInApkToSD() {

		FilesManager.getInstance().ExtractToSD(UpdateConfig.sGameConfig, StorageMgr.GetStreamingAssetPath());
		FilesManager.getInstance().ExtractToSD(UpdateConfig.sServerList, StorageMgr.GetStreamingAssetPath());
		FilesManager.getInstance().ExtractToSD(UpdateConfig.sServiceConfig, StorageMgr.GetStreamingAssetPath());

		// 判断assets/extraconfig/extrafiles.txt是否存在，并且里面是否有内容，有的话放到sd卡
		FilesManager.getInstance().extrafilesExtractToSD();
	}

	public boolean excludeFiles(String filename) {

		if (filename != null) {
			if (filename.contains("gameconfig")) {
				return true;
			}
			if (filename.contains("extraconfig")) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 刷新fileList
	 * 
	 * @param url
	 *            key文件路径
	 * @param md5AndSize
	 *            value 文件MD5和大小
	 * @return 刷新是否成功
	 */
	public boolean refreshFileList(String url, String md5AndSize) {
		lock.lock();
		boolean refreshSuccess = false;
		if (url == null || url.equals("")) {
			return refreshSuccess;
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(StorageMgr.GetStreamingAssetPath() + UpdateConfig.sFileList, true));
			StringBuffer sb = new StringBuffer(256);
			sb.append(url).append("=").append(md5AndSize).append("\r\n");
			bw.write(sb.toString());
			bw.close();
			refreshSuccess = true;

		} catch (FileNotFoundException e) {
			refreshSuccess = false;
			e.printStackTrace();
		} catch (IOException e) {
			refreshSuccess = false;
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return refreshSuccess;
	}

	public void saveFileList(StringBuffer sbuf) {
		// Log.d("wgq", "sbuf size"+sbuf.length());
		// lock.lock();
		// Log.d("wgq", "saveFileList lock");
		try {
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(StorageMgr.GetStreamingAssetPath() + UpdateConfig.sFileList));
			bw.write(sbuf.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// finally{
		// Log.d("wgq", "saveFileList unlock");
		// lock.unlock();
		// }
	}

	/**
	 * 这个功能是为了旧版下载apk没有删除掉用的
	 */
	public void removeOldAPK() {
		String path = StorageMgr.GetAssetPath();
		Log.d("updater", "removeOldAPK: " + path);
		File dir = new File(path);
		File[] listFiles = dir.listFiles();
		if (listFiles != null) {
			for (int i = 0; i < listFiles.length; i++) {
				if (listFiles[i].getName().endsWith(".apk") && !listFiles[i].isDirectory()) {
					Log.d("updater", "delete apk path: " + listFiles[i].getName());
					listFiles[i].delete();
				}
			}
		}
	}
}
