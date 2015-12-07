package com.cm.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

public class ContinueFile {
	private File mFile;
	private String mFileName;
	private int mPosition;
	private OutputStream os;

	public File getFile(){
		return mFile;
	}
	public ContinueFile(String filename) throws Exception {
		if (filename == null)
			throw new Exception("file name cant be null");
		
		mFileName = filename;
		mFileName = mFileName.replace("\\", "/");
		mFile = new File(mFileName);
		
		os = new FileOutputStream(mFileName);
	}

	public int GetPosition() {
		return mPosition;
	}

	static void ShowStack(String error) {
		RuntimeException e = new RuntimeException(error);
		e.fillInStackTrace();
		Log.d("updater", error, e);
	}

	public boolean WriteData(byte[] data, int length) {
		try {
			os.write(data, 0, length);
			return true;
		} catch (IOException e) {
			Log.d("updater", e.toString());
			ShowStack(e.toString());
		}

		return false;
	}

	public void Succeed() {
		try {
			os.close();
		} catch (IOException e) {
			Log.d("updater", e.toString());
		}
	}

	public static boolean Exist(String path) {
		File file = new File(path);
		if (file.isFile() == true) {
			File logFile = new File(path + ".nm");
			if (logFile.exists() == false)
				return true;
		}
		return false;
	}

	public static void Delete(String path) {
		File file = new File(path);
		if (file.exists() && file.isFile() == true) {
			file.delete();
			Log.d("updater", "delete file:" + path);
		}
	}

	public static void DeleteOnExit(String path) {
		File file = new File(path);
		if ((file.exists() == true) && (file.isFile() == true)) {
			file.deleteOnExit();
			Log.d("updater", "delete file on exit:" + path);
		}
	}

	public static String getFileTmpName(String fn) {
		return fn + ".tmp";
	}
}
