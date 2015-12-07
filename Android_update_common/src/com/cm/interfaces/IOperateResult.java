package com.cm.interfaces;

public interface IOperateResult 
{
	public static final int DownloadUpdateOK = 1;
	public static final int DownloadUpdateError = 2;
	public static final int DownloadUpdateTimeout = 3;
	
	public static final int DownloadServerListError = 4;
	public static final int DownloadServerListOK = 5;
	
	public static final int DownloadPatchTask = 6;
	public static final int DownloadApkTask = 7;
	
	public static final int DownloadPackageing = 8;
//	public static final int DownloadPatchSize = 6;
//	public static final int DownloadPatchOK = 7;
	public static final int DownloadPatchError = 9;
	public static final int DownloadBigPatchOK = 10;
	public static final int DownloadApkOK = 11;
	public static final int DownloadError = 12;
	public static final int DownloadTimeout = 13;
	
	public static final int Apk2SDError = 14;
	public static final int Apk2SDOK = 15;
	
	public static final int Extracting = 16;
	public static final int ExtracingFile = 17;
	public static final int ExtractOK = 18;
	public static final int ExtractZipError = 19;

	public static final int ExtractRawPatch = 20;
	public static final int ExtractUpdatePatch = 21;

	public static final int DownloadFileListOK = 22;
	public static final int DownloadFileListError = 23;
	public static final int DownloadFileListnormal = 38;
	public static final int DownloadFilesOk = 24;
	public static final int DownloadFilesIng = 25;
	public static final int DownloadFilesError = 26;
	public static final int DownloadFileListTimeOut = 27;
	public static final int DownloadFilesTimeOut = 28;
	public static final int RefreshFileListError = 29;
	
	public static final int ExtractFilesToSDing = 30;
	public static final int ExtractFilesComplete = 31;
	public static final int ExtractFilesError = 32;
	public static final int PaserOK = 33;
	
	public static final int DownloadApking = 34;
	public static final int DownloadApkIsOK = 35;
	public static final int DownloadApkError = 36;
	public static final int DownloadApkTimeOut = 37;
	
	public static final int DownloadFileOK = 40;
	public static final int UnzipError = 41;
	
	public static final int DownloadCurrentThreadFilesOk = 42;
	public static final int RefreshFileList = 43;
	
	public static final int UpdateProgress = 44;
	//public static final int StartUnity = 45;
	
}
