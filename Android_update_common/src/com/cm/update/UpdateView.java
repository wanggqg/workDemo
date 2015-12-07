package com.cm.update;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cm.config.GameRes;
import com.cm.interfaces.IAlertBoxCallback;
import com.cm.interfaces.IDownloadCompleteCallback;
import com.cm.utils.DownLoadManager;
import com.cm.utils.KeepLog;
import com.cm.utils.NetWorkManager;
import com.cm.utils.UpdateManager;
import com.unity3d.player.UnityPlayer;

public class UpdateView {
	private int recordCurrent = 0;
	static UpdateView _updateView = null;
	Activity _activity = null;
	public static UpdateView getInstance() {
		if (_updateView == null) {
			_updateView = new UpdateView();
		}
		return _updateView;
	}

	public void initContext(Activity context) {
		_activity =context;
	}

	// =UI相关函数================================================================

	public void initView() {
		_activity.setContentView(GameRes.getInstance().GetViewLayoutID("activity_update"));
		SetTipTextById(GameRes.getInstance().GetResTextID("cm_start_game"));
	}

	public void ShowTip(boolean show) {
		int v0, v1;
		if (show == true) {
			v0 = View.VISIBLE;
			v1 = View.INVISIBLE;
		} else {
			v0 = View.INVISIBLE;
			v1 = View.VISIBLE;
		}

		View v = _activity.findViewById(GameRes.getInstance().GetViewObjID("textView1"));
		v.setVisibility(v0);

		v = _activity.findViewById(GameRes.getInstance().GetViewObjID("myprogress"));
		v.setVisibility(v0);

		v = _activity.findViewById(GameRes.getInstance().GetViewObjID("splash"));
		v.setVisibility(v1);

		v = _activity.findViewById(GameRes.getInstance().GetViewObjID("iv"));
		v.setVisibility(v0);
	}

	void SetTipTextById(int id) {
		TextView t = (TextView) _activity.findViewById(GameRes.getInstance().GetViewObjID("textView1"));
		if (t != null)
			t.setText(id);
	}

	public void SetVersionText(String text) {
		TextView t = (TextView) _activity.findViewById(GameRes.getInstance().GetViewObjID("textView3"));
		if (t != null)
			t.setText(text);
	}

	public void SetSplashText(int id) {
		TextView view = (TextView) _activity.findViewById(GameRes.getInstance().GetViewObjID("textView2"));
		view.setVisibility(1);
		view.setText(id);
	}

	public void setProgress(int current, int total) {
		current += recordCurrent;
		ShowTip(true);
		TextView view = (TextView) _activity.findViewById(GameRes.getInstance().GetViewObjID("textView1"));
		String tipText = _activity.getString(GameRes.getInstance().GetResTextID("cm_update_lable"));

		float len = (float) total / (1024.0f * 1024.0f);
		float a = (float) current * 100 / total;
		float round = (float) (Math.round(a * 10)) / 10;

		tipText = tipText.replace("{ver}", UpdateManager.getInstance().mUpdateXml.mNewResversion );
		tipText = String.format(tipText, len);
		tipText += "%.1f%%";
		tipText = String.format(tipText, round);
		view.setText(tipText);
		showCustomProgress(current, total);
	}

	void showCustomProgress(final int current, int total) {
		ProgressBar mybar = (ProgressBar) _activity.findViewById(GameRes.getInstance().GetViewObjID("myprogress"));
		mybar.setProgress(current);
		mybar.setMax(total);
		ImageView iv = (ImageView) _activity.findViewById(GameRes.getInstance().GetViewObjID("iv"));
		int myBarWidth = mybar.getMeasuredWidth();
		double cur = (((double) current / (double) total) * myBarWidth);

		MarginLayoutParams margin = new MarginLayoutParams(iv.getLayoutParams());
		margin.setMargins((int) (0 + cur), margin.topMargin, margin.rightMargin, margin.bottomMargin);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
		layoutParams.gravity = Gravity.TOP;
		iv.setLayoutParams(layoutParams);
		iv.invalidate();
	}

	// ==UI弹框相关显示=========================================================================
	/**
	 * 
	 * @param title
	 *            标题
	 * @param message
	 *            信息
	 * @param positiveMsg
	 *            确认按钮信息
	 * @param negativeMsg
	 *            取消按钮信息
	 * @param back
	 *            回调
	 */
	public void showAlert(final String title, final String message, final String positiveMsg, final String negativeMsg, final IAlertBoxCallback back) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_SHOWALERT, message);
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
				if (!TextUtils.isEmpty(title)) {
					builder.setTitle(GameRes.getInstance().GetResTextID(title));
				}
				if (!TextUtils.isEmpty(message)) {
					builder.setMessage(GameRes.getInstance().GetResTextID(message));
				}
				builder.setPositiveButton(GameRes.getInstance().GetResTextID(positiveMsg), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						back.buttonYESClickCallback();
					}
				});
				builder.setNegativeButton(GameRes.getInstance().GetResTextID(negativeMsg), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						System.exit(0);
					}
				});
				builder.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK  
		                        && event.getRepeatCount() == 0) {  
							dialog.dismiss();
							System.exit(0);
		                }  
		                return false;  
					}
				});
				builder.setCancelable(false);
				builder.create().show();
			}
		});
	}

	/**
	 * 
	 * @param title
	 *            标题
	 * @param positiveMsg
	 *            确认按钮信息
	 * @param back
	 *            回调
	 */
	public void showAlert(final String title, final String message, final String positiveMsg, final IAlertBoxCallback back) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_SHOWALERT, message);
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
				if (!TextUtils.isEmpty(title)) {
					builder.setTitle(GameRes.getInstance().GetResTextID(title));
				}
				if (!TextUtils.isEmpty(message)) {
					builder.setMessage(GameRes.getInstance().GetResTextID(message));
				}
				builder.setPositiveButton(GameRes.getInstance().GetResTextID(positiveMsg), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						back.buttonYESClickCallback();
					}
				});
				builder.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK  
		                        && event.getRepeatCount() == 0) {  
							dialog.dismiss();
							System.exit(0);
		                }  
		                return false;  
					}
				});
				builder.setCancelable(false);
				builder.create().show();
			}
		});
	}

	/**
	 * 显示错误提示框，引导退出
	 * 
	 * @param message
	 */
	public void showErrorAlert(final String message) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_SHOWALERT, message);
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
				builder.setMessage(GameRes.getInstance().GetResTextID(message));
				builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						System.exit(0);
					}
				});
				builder.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK  
		                        && event.getRepeatCount() == 0) {  
							dialog.dismiss();
							System.exit(0);
		                }  
		                return false;  
					}
				});
				builder.setCancelable(false);
				builder.create().show();
			}
		});
	}

	public void showMemoryWarningAlert(float memoryAvailable) {
		int textId = GameRes.getInstance().GetResTextID("cm_memory_warning");
		String textMsg = String.format(_activity.getString(textId), memoryAvailable);

		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(textMsg);

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_continue"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// _activity.extractPatchInApkToSD();
			}
		});

		builder.create().show();
	}

	public void ShowCriticalError(int id, int error) {
		String s = _activity.getString(id);
		s = s + "[error:" + error + "]";
		ShowCriticalError(s);
	}

	public void ShowCriticalError(int id) {
		String s = _activity.getString(id);
		ShowCriticalError(s);
	}

	public void ShowCriticalErrorByResId(String textName) {
		ShowCriticalError(GameRes.getInstance().GetResTextID(textName));
	}

	public void ShowCriticalError(String str) {
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(str);

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});
		builder.create().show();
	}

	void ShowNetError(String titile, String msg, IDownloadCompleteCallback icall) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_ERROR, 0, "ShowNetError");
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(GameRes.getInstance().GetResTextID("cm_net_error"));

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_retry"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// DownLoadManager.getInstance().downloadUpdateXml();
				// icall.DoneCallback(url, data);
			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});

		builder.create().show();

	}

	void showUpdateVersionError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(GameRes.getInstance().GetResTextID("cm_update_error"));

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_retry"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// showNum++;
				// DownLoadManager.getInstance().downloadUpdateXml();
			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});
		builder.create().show();
	}

	void ShowUpdateError() {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_ERROR, 0, "ShowUpdateError");
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(GameRes.getInstance().GetResTextID("cm_update_error"));

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_retry"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// DownLoadManager.getInstance().downloadFileListAndCheck();
			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});

		builder.create().show();
	}

	void ShowApkError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(GameRes.getInstance().GetResTextID("cm_serverlist_error"));

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_retry"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// DownLoadManager.getInstance().DownLoadApk(mConfig.mClient_addr);

			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});

		builder.create().show();
	}

	void ShowUpdateFilesError() {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_ERROR, 0, "ShowUpdateFilesError");
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		Log.e("updater", "ShowUpdateFilesError");
		builder.setMessage(GameRes.getInstance().GetResTextID("cm_updatefile_error"));

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_retry"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// DownLoadManager.getInstance().downloadFileListAndCheck();
			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});

		builder.create().show();
	}

	void ShowUpdateFilesTimeOut() {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_ERROR, 0, "ShowUpdateFilesError");
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);

		Log.e("updater", "ShowUpdateFilesError");
		builder.setMessage(GameRes.getInstance().GetResTextID("cm_getupdatefile_error"));

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_retry"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// DownLoadManager.getInstance().downloadFileListAndCheck();
			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});

		builder.create().show();
	}

	void ShowRefreshFileListError() {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_ERROR, 0, "ShowRefreshFileListError");
		Log.e("updater", "ShowRefreshFileListError");

		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(GameRes.getInstance().GetResTextID("cm_getupdatefile_error"));

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_retry"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// DownLoadManager.getInstance().downloadFileListAndCheck();
			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});

		builder.create().show();
	}

	/**
	 * 显示升级提示框
	 * 
	 * @param str
	 */
	void ShowUpdateAlert(int id) {
		String s = _activity.getString(id);
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(s);

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_yes"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// skipUrl(mConfig.mClient_addr);
			}
		});
		builder.create().show();
	}

	public boolean skipUrl(String mAddr) {
		KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADAPK_BEGIN, "DOWNLOADAPK_BEGIN");
		// 这里更具url类型，完成跳转逻辑
		if ("".equals(mAddr)) {
			KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADAPK_ERROR, "APKURL_ERROR");
			showErrorAlert("cm_getApk_error");
			return false;
		}
		try {
			new URL(mAddr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			KeepLog.getInstance().updateLog(KeepLog.EVENT_STARTAPP, KeepLog.EVENT_STEP_DOWNLOADAPK_ERROR, "APKURL_ERROR");
			showErrorAlert("cm_getApk_error");
			return false;
		}

		// 如果是apk的路径，启动自己的更新器下载，不跳转浏览器了
		String fileExt = mAddr.substring(mAddr.length() - 4);
		Log.d("updater", "fileExt: " + fileExt);
		if (fileExt.equals(".apk")) {
			Log.d("updater", "DownLoadApk ");
			DownLoadManager.getInstance().downLoadApk(mAddr);
		} else {
			Log.d("Unity", "skip url ");
			Uri uri = Uri.parse(mAddr);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			_activity.startActivity(it);
		}
		return true;
	}

	public void showRequestAgreementAlert(final IAlertBoxCallback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		builder.setMessage(GameRes.getInstance().GetResTextID("cm_skip_url"));

		builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_yes"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				callback.buttonYESClickCallback();

			}
		});

		builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.exit(0);
			}
		});

		builder.create().show();
	}

	/**
	 * 非wifi模式要提示将要更新的大小并且提示是否更新
	 * 
	 * @param downloadSize
	 */
	protected void showMobileAlertFileList(final int downloadSize) {
		if (NetWorkManager.getInstance().isMobileNetWork()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
			float len = (float) downloadSize / (1024.0f * 1024.0f);
			builder.setMessage(String.format(_activity.getString(GameRes.getInstance().GetResTextID("cm_net_tips")), len));

			builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_yes"), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Log.d("updater", "phone net downloading...");
					// DownLoadManager.getInstance().downLoadFileFromNet(downloadSize);
				}
			});

			builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_exit"), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			});

			builder.create().show();
		} else {
			Log.d("updater", "wifi model downloading...");
			// DownLoadManager.getInstance().downLoadFileFromNet(downloadSize);
		}
	}

	/**
	 * copy文件时的ui显示
	 * 
	 * @param current
	 * @param total
	 */
	public void showCopyFilesProgress(int current, int total) {
		TextView view2 = (TextView) _activity.findViewById(GameRes.getInstance().GetViewObjID("textView2"));
		view2.setVisibility(View.INVISIBLE);

		TextView view = (TextView) _activity.findViewById(GameRes.getInstance().GetViewObjID("textView1"));
		view.setVisibility(View.VISIBLE);

		String tipText = _activity.getString(GameRes.getInstance().GetResTextID("cm_extract_files"));
		tipText = tipText.replace("{ver}", current + "/" + total);
		view.setText(tipText);

		ProgressBar mybar = (ProgressBar) _activity.findViewById(GameRes.getInstance().GetViewObjID("myprogress"));
		mybar.setVisibility(View.VISIBLE);
		mybar.setProgress(current);
		mybar.setMax(total);
	}

	// 弹框提示玩家有资源更新，需要重新启动程序
	public void ShowRestart() {
		final Activity currentActivity = UnityPlayer.currentActivity;
		if (currentActivity != null) {
			currentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
					Log.d("Unity", "ShowRestart");
					builder.setMessage(GameRes.getInstance().GetResTextID("cm_restart"));

					builder.setPositiveButton(GameRes.getInstance().GetResTextID("cm_yes"), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							restart(currentActivity);
						}
					});

					builder.setNegativeButton(GameRes.getInstance().GetResTextID("cm_cancel"), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
			});
		}
	}

	public void restart(Activity context) {
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		context.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
