package com.cm.download;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class ApkContinueFile 
{
	private RandomAccessFile mFile;
	private String mFileName;
	private int mPosition;
	
	private long mSaveTime = 0;
	
	public ApkContinueFile(String filename) throws Exception
	{
		if (filename == null)
			throw new Exception("file name cant be null");
		
		mFileName = filename;
		mFile = new RandomAccessFile(filename, "rw");
		mPosition = ReadPosition(filename + ".nm");
		if (mPosition > 0)
			mFile.seek(mPosition);
		mSaveTime = 0;
	}
	
	public int GetPosition()
	{
		return mPosition;
	}
	
	static void ShowStack(String error)
	{
		RuntimeException e = new RuntimeException(error);
        e.fillInStackTrace();
        Log.d("updater", error, e);
	}
	
	private static int ReadPosition(String filename)
	{
		DataInputStream dis = null;
		int pos = 0;
		File file = new File(filename);
		try 
		{
			if (file.isFile() == true)
			{
				dis = new DataInputStream(new FileInputStream(file));
				pos = dis.readInt();
				dis.close();
			}
			else 
			{
				// write a default 0 position 
				DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
				dos.writeInt(0);
				dos.flush();
				dos.close();
			}
		} 
		catch (FileNotFoundException e) 
		{
			Log.d("updater", e.toString());
		}
		catch (IOException e)
		{
			Log.d("updater", e.toString());
			file.delete();
		}
		finally
		{
			if (dis != null)
			{
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return pos;
	}
	
	private boolean WritePosition(String filename, int position)
	{
		if (System.currentTimeMillis() - mSaveTime > 3000)	// every 3 minute save file
		{
			try 
			{
				DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
				dos.writeInt(position);
				dos.flush();
				dos.close();
				mSaveTime = System.currentTimeMillis();
				return true;
			} 
			catch (FileNotFoundException e) 
			{
				Log.d("updater", e.toString());
			}
			catch (IOException e)
			{
				Log.d("updater", e.toString());
				ShowStack(e.toString());
			}
			return false;
		}
		else {
			return true;
		}
	}
	
	public void Close() 
	{
		try 
		{
			mFile.close();
		} 
		catch (IOException e)
		{
			Log.d("updater", e.toString());
		}
		mFile = null;
	}
	
	public boolean WriteData(byte[] data, int length)
	{
		try {
			mFile.write(data, 0, length);
			mPosition += length;
			return WritePosition(mFileName + ".nm", mPosition);
		} catch (IOException e) {
			Log.d("updater", e.toString());
			ShowStack(e.toString());
		}
		
		return false;
	}
	
	public void Succeed()
	{
		try 
		{
			mFile.close();
		} 
		catch (IOException e) 
		{
			Log.d("updater", e.toString());
		}

		File logFile = new File(mFileName + ".nm");
		if (logFile.isFile() == true)
			logFile.delete();
	}
	
	public static boolean Exist(String path)
	{
		File file = new File(path);
		if (file.isFile() == true)
		{
			File logFile = new File(path + ".nm");
			if (logFile.exists() == false)
				return true;
		}
		return false;
	}
	
//	public static void Delete(String path)
//	{
//		File file = new File(path);
//		if (file.exists() && file.isFile() == true){
//			file.delete();
//			Log.d("updater","delete file:"+path);
//		}
//	}
	
	public static void DeleteOnExit(String path){
		File file = new File(path);
		if ((file.exists() == true) && (file.isFile() == true)){
			file.deleteOnExit();
			Log.d("updater","delete file on exit:"+path);
		}
	}
	
	public static String getFileTmpName(String fn){
		return fn+".tmp";
	}
}
