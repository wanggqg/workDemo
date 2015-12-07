package com.cm.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import com.cm.utils.ShareObjectManager;
import com.lk.event.utils.OKUtils;
import com.unity3d.player.UnityPlayer;

public class StorageMgr {

	private  static String resourcePath="";
	private static String mountedPathString = "";
	private static String installConfig = "install.cfg";


	public StorageMgr(){}
	
	public static void initContext(Activity activity)
	{
		String mountedPath = getMountedPath(activity);
		if(mountedPath !=null && mountedPath.length()>0){
			return;
		}

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == true)
		{
			File extdir = activity.getExternalFilesDir(null);
			if(extdir != null && extdir.exists()){
				String extpath = extdir.getPath() + "/"; 
				if(calcFreeSize(extpath)){
					mountedPathString = extpath;
					SaveMountedPath(activity,mountedPathString);
					return;
				}
			}

			File path = Environment.getExternalStorageDirectory();
			if(path != null && path.exists()){
				if(calcFreeSize(path.getPath())){
					mountedPathString = path.getPath() +"/"+ activity.getPackageName()+"/";//"/com.lk.zhenhuan/"; 
					Log.d("updater", "Destionation is OK from Env " + mountedPathString );

					SaveMountedPath(activity,mountedPathString);
					return;
				}
			}

			Log.d("updater", "Destionation from Env has not enougth space : " + mountedPathString );
			Log.d("updater", "Continue: use external storage space");
		}

		StorageManager sm = (StorageManager) activity.getSystemService(Activity.STORAGE_SERVICE);
		try {
			Method call_volume = sm.getClass().getMethod("getVolumePaths");
			String[] paths = (String[]) call_volume.invoke(sm);

			if (paths != null && paths.length > 0)
			{
				for(int i=0;i<paths.length;i++){
					if(calcFreeSize(paths[i])){
						mountedPathString = paths[i]  +"/"+ activity.getPackageName()+"/";// "/com.lk.zhenhuan/";
						Log.d("updater", "destination path is " + mountedPathString + " is  [" + i +"/" +  paths.length ); 
						break;
					}
				}
			}

			if (mountedPathString.length() == 0){
				Log.d("updater", "There is no fit space for the program about " + paths.length + " SD card");
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		if ( mountedPathString.length() > 0)	{
			SaveMountedPath(activity,mountedPathString);
		}

	}
	
	public static String getMountedPath(Activity activity) {
		if (activity==null) {
			return "";
		}
		//codePath = activity.getPackageCodePath();
		resourcePath = activity.getPackageResourcePath();
		Log.d("updater", "resourcePath="+StorageMgr.resourcePath);

		//这里应该从sharepreference目录中尝试读取installConfig文件，或者程序安装目录
		mountedPathString = ShareObjectManager.getInstance().getString(installConfig, mountedPathString);
		
		return mountedPathString;
	}
	
	private static void SaveMountedPath(Activity activity,String path){
		//这里应该将mountedPathString，写入sharepreference,下次程序启动时，从 sharepreference中读取程序安装目录
		ShareObjectManager.getInstance().putString(installConfig, path);
	}

	public static boolean calcFreeSize(String path){
		android.os.StatFs statfs = new android.os.StatFs(path);
		long nBlocSize = statfs.getBlockSize();  
		long nAvailaBlock = statfs.getAvailableBlocks(); 

		long nFreeSize = nAvailaBlock * nBlocSize;
		Log.d("updater", path+",getBlockSize="+nBlocSize+",getAvailableBlocks="+nAvailaBlock+",nFreeSize="+nFreeSize);
		
		return nFreeSize > 200 * 1024 * 1024;
	}

	public static void DeleteFolder(File f)	{
		//删除文件夹下面的文件，不删除文件夹
		if(f.exists() == false) 
			return;
		if(f.isFile() == true){
			f.delete();	
			return;
		}
		File[] files = f.listFiles();
		if(files !=null && files.length>0)
			for(int i = 0;i < files.length; i++)  	{
				DeleteFolder(files[i]);
			}
	}
	

	public static  boolean checkFilePath(String filePath){
		File targetFile = new File(filePath);
		String parentPath = targetFile.getParentFile().getAbsolutePath();
		File directory = new File(parentPath);
		
		if (!directory.exists() || !directory.isDirectory()) {
			boolean mkdirs = directory.mkdirs();
			if(!mkdirs){
				Log.d("updater", "create directory failed, "+parentPath);
			}
		}
		
		if(!targetFile.exists()){
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * 这个方法用来创建多级文件夹和文件
	 * 
	 * @param path
	 * @return 是否创建成功或者存在
	 */
	public static boolean createFile(String path) {
		String replace = path.replace("\\", "/");
		String substring = replace.substring(0, replace.lastIndexOf("/"));
		File file = new File(substring);
		if (!file.exists()) {
			synchronized (StorageMgr.class) {
				if (!file.mkdirs()) {
					Log.e("updater", "mkdirs error: "+substring);
					if (!file.exists()) return false;
				}
			}
		}
		File file2 = new File(replace);
		if (file2.exists()) {
			boolean delete = file2.delete();
			if (!delete) {
				Log.e("updater", "delete error: "+replace);
				return false;
			}
		}
		try {
			if (file2.createNewFile()) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("updater", "createNewFile error: "+replace);
			return false;
		}
		return false;
	}
	
	public static void CheckPath(String dir)	{
		if (dir != null)	{
			File pathFile = new File(dir);
			if (!pathFile.exists()) 	{
				pathFile.mkdirs();
			}
		}
	}

	public static boolean ExistFile(String filename)	{
		if (filename == null)
			return false;
		File file = new File(filename);
		if (file.isFile() == true)
			return true;
		else
			return false;
	}

	public static boolean HasBundleData(Context context, String path)
	{
		AssetManager manager = context.getAssets();
		try {
			InputStream islist = manager.open(path);
			islist.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static String GetRootPath(){
		return mountedPathString;
	}
	public static String GetAssetPath(){
		if(mountedPathString==null || mountedPathString.length()==0){
			getMountedPath(UnityPlayer.currentActivity);
		}
		return mountedPathString;
	}
	public static String GetStreamingAssetPath(){
		return GetAssetPath()+ "assets/";
	}
	public static String GetGameConfigPath(){
		return GetStreamingAssetPath() + "gameconfig/";
	}
	public static String GetMountedPath(){
		return mountedPathString;
	}

	public static String GetFilePath(String fn){
		String filePath = GetStreamingAssetPath() + fn;
		if(ExistFile(filePath)){
			return "file://"+filePath;
		}
		else{
			return "jar:file://"+resourcePath+"!/assets/"+fn;
		}
	}

	public static String GetApkPath(){
		return resourcePath;
	}
	public static String GetApkFilePath(String fn){
		return "jar:file://"+resourcePath+"!/assets/"+fn;
	}
	
	public static String GetDeviceID(){
		String sDeviceID = "";

		Activity activity = UnityPlayer.currentActivity;
		if(activity != null){
			sDeviceID = OKUtils.id(activity);
		}
		return sDeviceID;
	}
	
	
}
