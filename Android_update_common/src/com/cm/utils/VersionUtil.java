package com.cm.utils;

import java.io.File;

import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class VersionUtil {
	public static boolean LargerThan(String v0, String v1)	{
		if(v0 == null){ return false;}
		if(v1 == null){ return true; }
		String[] sp0 = v0.split("\\.");
		String[] sp1 = v1.split("\\.");

		for (int i = 0; i < sp0.length; i++)
		{
			if (TextUtils.isEmpty(sp0[i])) {
				return false;
			}
			int n0 = Integer.parseInt(sp0[i]);
			int n1 = 0;
			if(i<sp1.length){
				n1 = Integer.parseInt(sp1[i]);
			}

			if (n0 > n1)
				return true;
			else if (n0 < n1)
				return false;
		}
		return false;
	}
	
	public static void InstallApk(String file, ContextWrapper context)   {
		Intent install = new Intent();
		install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		install.setAction(android.content.Intent.ACTION_VIEW);
		install.setDataAndType(Uri.fromFile(new File(file)), "application/vnd.android.package-archive");
		
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_INSTALLAPK, "INSTALLAPK");
		
		context.startActivity(install);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	public static String modifyBundleVersion(String vserion) {
		if (vserion.split("\\.").length == 4) {
			vserion = vserion.substring(0, vserion.lastIndexOf("."));
		}
		return vserion;
	}
}
