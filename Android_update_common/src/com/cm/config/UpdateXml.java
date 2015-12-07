package com.cm.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

import com.cm.update.StorageMgr;
import com.cm.utils.UpdateManager;

public class UpdateXml {

	class DefineValue {
		public String mVersion;
	}

	public String mNewResversion = "0.0.0";
	public String mNewApkVersion = "0.0.0";

	String mCheck_version_name = "0.0.0";

	String mPlatformName = null;

	public String mClient_isMini = "0";

	public String mClient_addr = null;
	public String mClientmd5 = "";

	public String mUpdateUrl = null;
	public String mFileListUrl = null;
	public String mUpdateInfoUrl = null;

	public UpdateXml() {
	}

	public String getFileListUrl() {
		return mUpdateUrl + mNewResversion + "/assets/fileList.txt";
	}

	public String getFilesUrl() {
		return mUpdateUrl + mNewResversion + "/";
	}

	public Boolean parseUpdate(byte[] buffer) {
		mPlatformName = UpdateManager.getInstance().mGameConfig.mPlatformName;
		// 这里需要对appversion resversion 进行修正

		ArrayList<String> platForms = new ArrayList<String>();
		ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;

		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		}

		if (builder == null) {
			return false;
		}

		Document doc = null;
		try {
			doc = builder.parse(stream);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		}

		if (doc == null) {
			return false;
		}

		Element root = doc.getDocumentElement();

		if (root == null) {
			Log.d("updater", "root is null");
			return false;
		}

		if ("update".equals(root.getNodeName()) == false) {
			Log.d("updater", "update Format Config Error,Please Check");
			return false;
		}

		NodeList collegeNodes = root.getChildNodes();
		if (collegeNodes == null) {
			return false;
		}

		// 获取Common字段值
		for (int i = 0; i < collegeNodes.getLength(); i++) {
			Node college = collegeNodes.item(i);
			if ("common".equals(college.getNodeName())) {
				// ------------------------------------------------------------------------------------------------------------------------
				if (college.getAttributes().getNamedItem("version") != null) {
					mNewApkVersion = college.getAttributes().getNamedItem("version").getNodeValue();
				}
				if (college.getAttributes().getNamedItem("res_version") != null) {
					mNewResversion = college.getAttributes().getNamedItem("res_version").getNodeValue();
				}
				if (college.getAttributes().getNamedItem("check_version_name") != null) {
					mCheck_version_name = college.getAttributes().getNamedItem("check_version_name").getNodeValue();
				}
				if (college.getAttributes().getNamedItem("updateserver") != null) {
					mUpdateUrl = college.getAttributes().getNamedItem("updateserver").getNodeValue();
				}
				if (college.getAttributes().getNamedItem("updateinfourl") != null) {
					mUpdateInfoUrl = college.getAttributes().getNamedItem("updateinfourl").getNodeValue();
				}
				// mUpdateUrl = "http://113.208.129.53:14572/zhz/update/";
				// ------------------------------------------------------------------------------------------------------------------------
				break;
			}
		}

		// ==获取Platform字段值==========================================================
		for (int i = 0; i < collegeNodes.getLength(); i++) {

			Node college = collegeNodes.item(i);
			if ("os".equals(college.getNodeName())) {
				String value = college.getAttributes().getNamedItem("value").getNodeValue();
				// ===AndRoid平台=========================================================
				if ("android".equals(value)) {

					NodeList childs = college.getChildNodes();
					if (childs != null && childs.getLength() > 0) {

						for (int j = 0; j < childs.getLength(); j++) {

							Node item = childs.item(j);
							if (item.getAttributes() != null) {
								String namedItem = item.getAttributes().getNamedItem("platform_code").getNodeValue();

								if (namedItem.equals(mPlatformName)) {
									platForms.add(namedItem);
									// ------------------------------------------------------------------------------------------------------
									if (item.getAttributes().getNamedItem("version") != null) {
										String nodeValue = item.getAttributes().getNamedItem("version").getNodeValue();
										if (!"0".equals(nodeValue)) {
											mNewApkVersion = nodeValue;
										}
									}
									if (item.getAttributes().getNamedItem("client_addr") != null) {
										mClient_addr = item.getAttributes().getNamedItem("client_addr").getNodeValue();
									}
									if (item.getAttributes().getNamedItem("client_ismini") != null) {
										mClient_isMini = item.getAttributes().getNamedItem("client_ismini").getNodeValue();
									}else{
										if (item.getAttributes().getNamedItem("ismini") != null) {
											mClient_isMini = item.getAttributes().getNamedItem("ismini").getNodeValue();
										}
									}
									if (item.getAttributes().getNamedItem("clientmd5") != null) {
										mClientmd5 = item.getAttributes().getNamedItem("clientmd5").getNodeValue();
									}
									// ----------------------------------------------------------------------------------------------------
									break;
								}
							}
						}

						// 如果都没有，用默认的Platform=default
						if (!platForms.contains(mPlatformName)) {
							Log.d("updater", "mPlatformName is default");

							for (int j = 0; j < childs.getLength(); j++) {
								Node item = childs.item(j);
								if (item.getAttributes() != null) {
									String namedItem = item.getAttributes().getNamedItem("platform_code").getNodeValue();

									if (namedItem.equals("default")) {
										// -----------------------------------------------------------------
										if (item.getAttributes().getNamedItem("version") != null) {
											String nodeValue = item.getAttributes().getNamedItem("version").getNodeValue();
											if (!"0".equals(nodeValue)) {
												mNewApkVersion = nodeValue;
											}
										}
										if (item.getAttributes().getNamedItem("client_addr") != null) {
											mClient_addr = item.getAttributes().getNamedItem("client_addr").getNodeValue();
										}
										if (item.getAttributes().getNamedItem("client_ismini") != null) {
											mClient_isMini = item.getAttributes().getNamedItem("client_ismini").getNodeValue();
										}
										// -----------------------------------------------------------------
										break;
									}
								}
							}
						}

					}// endif

				}
				// ===AndRoid平台=========================================================
			}
		}
		// ==获取Platform字段值结束=======================================================
		/**
		 * <update> <resversion > <item version="1.1.2" res_version="1.2.2"/>
		 * <itemversion="1.2.12" res_version="1.2.13"/> <itemversion="1.2.13"
		 * res_version="1.2.14"/> </resversion> </update>
		 */

		// ==根据appVersion,修正resVersion
		String appBundleVersion = UpdateManager.getInstance().mGameDefine.appBundleVersion;
		for (int i = 0; i < collegeNodes.getLength(); i++) {
			Node college = collegeNodes.item(i);
			if ("resversion".equals(college.getNodeName())) {
				// --section resversion
				// begin------------------------------------------------------------------------------------------
				NodeList childs = college.getChildNodes();
				if (childs != null && childs.getLength() > 0) {
					for (int j = 0; j < childs.getLength(); j++) {
						Node item = childs.item(j);
						if (item.getAttributes() != null) {
							String appversion = item.getAttributes().getNamedItem("version").getNodeValue().trim();
							Log.d("updater", "resversion item" + appversion);
							if (appBundleVersion.equals(appversion)) {
								mNewResversion = item.getAttributes().getNamedItem("res_version").getNodeValue().trim();
								break;
							}
						}
					}
				}
				// --section resversion
				// end--------------------------------------------------------------------------------------------
				break;
			}
		}
		Log.d("updater", "mNewApkVersion: " + mNewApkVersion);
		Log.d("updater", "mNewResversion: " + mNewResversion);
		Log.d("updater", "mCheck_version_name:" + mCheck_version_name);

		Log.d("updater", "mUpdateInfoUrl:" + mUpdateInfoUrl);
		Log.d("updater", "mUpdateUrl:" + mUpdateUrl);

		Log.d("updater", "mClient_addr:" + mClient_addr);
		Log.d("updater", "mClient_ismini:" + mClient_isMini);
		return true;
	}

	/**
	 * 是否显示审核区
	 * 
	 * @return 1 是显示审核区 0是玩家区
	 */
	public String isShowSpecial() {
		String appBundleVersion = UpdateManager.getInstance().mGameDefine.appBundleVersion;
		if ("0.0.0".equals(mCheck_version_name)) {
			return "0";
		}
		if (mCheck_version_name.contains(",")) {
			String[] check_version = mCheck_version_name.split(",");
			for (int i = 0; i < check_version.length; i++) {
				if (check_version[i].equals(appBundleVersion)) {
					return "1";
				}
			}
		} else {
			if (mCheck_version_name.equals(appBundleVersion)) {
				return "1";
			}
		}
		return "0";
	}

	public String getApkPath() {
		String apkPath = StorageMgr.GetStreamingAssetPath() + mNewApkVersion + ".apk";
		return apkPath;
	}
}
