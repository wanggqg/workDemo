package com.cm.update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.selectphoto.R;

public class SelectPhotoActivity extends Activity {
	public static final int NONE = 0;
	public static final int PHOTOHRAPH = 1;// 拍照
	public static final int PHOTOZOOM = 2; // 缩放
	public static final int PHOTORESOULT = 3;// 结果
	public static final int UpLoadError = 4;
	public static final int UpLoadOk = 5;
	public static final int NoExixt = 6;
	public static final int DELESIGNOK = 7;
	public static final String IMAGE_UNSPECIFIED = "image/*";
	protected String filename;
	private String dirname = "/photo";// 文件夹名称，要更换名称直接修改即可
	private String id = "111";
	private String filePath = "";
	// private Handler mHandler;
	private ClipImageLayout mClipImageLayout;

	private Uri imageUri = null;// The Uri to store the big bitmap

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(GetResourceId("layout", "activity_select"));
		// mView = (CropImageView) findViewById(R.id.cropimage);
		// id = this.getIntent().getStringExtra("id").trim();
		mClipImageLayout = (ClipImageLayout) findViewById(R.id.id_clipImageLayout);
		Log.d("updater", "android id is " + id);
		File dir = new File(Environment.getExternalStorageDirectory(), dirname);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		// uploadManager = new UploadManager(this, null);
		dialog();
	}

	private void dialog() {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(GetResourceId("layout", "activity_selecte_image"));
		final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		// 初始化view
		OnClickListener ol = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				if (id == GetResourceId("id", "btn_camera")) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					// 图片名称 时间命名
					Date date = new Date(System.currentTimeMillis());
					filename = format.format(date);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory() + dirname, filename + ".jpg")));
					startActivityForResult(intent, PHOTOHRAPH);
					dialog.dismiss();
				} else if (id == GetResourceId("id", "btn_local")) {
					// 图片名称 时间命名
					Date date = new Date(System.currentTimeMillis());
					filename = format.format(date);
					Intent intent = new Intent(Intent.ACTION_PICK, null);
					intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
					startActivityForResult(intent, PHOTOZOOM);
					dialog.dismiss();
				} else if (id == GetResourceId("id", "btn_cancle")) {
					dialog.dismiss();
					SelectPhotoActivity.this.finish();
				}
			}
		};
		Button btn_camera = (Button) dialog.findViewById(GetResourceId("id", "btn_camera"));
		Button btn_local = (Button) dialog.findViewById(GetResourceId("id", "btn_local"));
		Button btn_cancle = (Button) dialog.findViewById(GetResourceId("id", "btn_cancle"));
		btn_camera.setOnClickListener(ol);
		btn_local.setOnClickListener(ol);
		btn_cancle.setOnClickListener(ol);
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.BOTTOM);
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialogWindow.setAttributes(lp);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.id_action_clip:
			Bitmap photo = mClipImageLayout.clip();

			String filePath = path() + "/" + id + ".jpg";
			Log.d("updater", "保存的本地路径：" + filePath);
			File picture = new File(filePath);
			if (picture.exists()) {
				boolean delete = picture.delete();
				Log.d("updater", "onActivityResult dele: " + delete);
			} else {
				try {
					picture.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream out = null;
			try {
				// 最后保存图片到本地
				out = new FileOutputStream(picture);
				photo.compress(Bitmap.CompressFormat.PNG, 75, out);// (0 -
																	// 100)压缩文件
				float sizeOfBitmap = getBitmapSize(photo);
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 上传到服务器
			// UpLoadPhoto(photo);
			Log.d("wgq", "UpLoadPhoto== ");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		if (filename != null) {
			File file = new File(filePath);
			if (file.exists() && !file.isDirectory()) {
			}
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("wgq", requestCode + "==" + resultCode);
		if (resultCode == NONE) {
			Log.d("updater", "selectPhoto cancel");
			finish();
			return;
		}
		// 拍照
		if (requestCode == PHOTOHRAPH) {
			// 设置文件保存路径这里放在SD卡根目录下
			File picture = new File(Environment.getExternalStorageDirectory() + dirname + "/" + filename + ".jpg");
			imageUri = Uri.fromFile(picture);
			// 广播刷新相册
			Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intentBc.setData(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + dirname, filename + ".jpg")));
			sendBroadcast(intentBc);
			startPhotoZoomFromCamera(Environment.getExternalStorageDirectory() + dirname + "/" + filename + ".jpg");
		}

		if (data == null) {
			return;
		}
		// 读取相册缩放图片
		if (requestCode == PHOTOZOOM) {
			Uri uri = data.getData();

			startPhotoZoomFromAlbum(uri);
		}
		// 处理结果
		if (requestCode == PHOTORESOULT) {
			String filePath = path() + "/" + id + ".jpg";
			Log.d("updater", "保存的本地路径：" + filePath);
			File picture = new File(filePath);
			if (picture.exists()) {
				boolean delete = picture.delete();
				Log.d("updater", "onActivityResult dele: " + delete);
			} else {
				try {
					picture.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Bitmap photo = null;
			photo = data.getParcelableExtra("data");
			Uri data2 = data.getData();
			// return;
			if (photo == null) {
				try {
					photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream out = null;
			try {
				// 最后保存图片到本地
				out = new FileOutputStream(picture);
				photo.compress(Bitmap.CompressFormat.PNG, 75, out);// (0 -
																	// 100)压缩文件
				float sizeOfBitmap = getBitmapSize(photo);
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 上传到服务器
			// UpLoadPhoto(photo);
			Log.d("wgq", "UpLoadPhoto== ");
			// finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	public void startPhotoZoomFromAlbum(Uri uri) {
		if (uri == null) {
			return;
		}
		mClipImageLayout.setVisibility(View.VISIBLE);
		String realFilePath = getRealFilePath(this, uri);
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(realFilePath);
		} catch (IOException ex) {
			Log.d("wgq", "cannot read exif" + ex);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}
			}
		}
		Bitmap bitmap = comp(realFilePath);
		Log.d("wgq", "startPhotoZoomFromAlbum degree: "+degree);
		if (degree != 0 && bitmap !=null) {
			// 下面的方法主要作用是把图片转一个角度，也可以放大缩小等
			Matrix m = new Matrix();
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			m.setRotate(degree); // 旋转angle度
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 从新生成图片
		}
		if (bitmap == null) {
			return;
		}
		mClipImageLayout.setImage(bitmap);
	}

	public void startPhotoZoomFromCamera(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return;
		}
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filePath);
		} catch (IOException ex) {
			Log.d("wgq", "cannot read exif" + ex);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}
			}
		}
		mClipImageLayout.setVisibility(View.VISIBLE);
		if (filePath != null) {
			Bitmap bitmap = comp(filePath);
			Log.d("wgq", "startPhotoZoomFromCamera degree: "+degree);
			if (degree != 0) {
				// 下面的方法主要作用是把图片转一个角度，也可以放大缩小等
				Matrix m = new Matrix();
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				m.setRotate(degree); // 旋转angle度
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 从新生成图片
			}
			mClipImageLayout.setImage(bitmap);
		}
	}

	int GetResourceId(String type, String name) {
		Resources localResources = getResources();
		String packageName = getPackageName();
		return localResources.getIdentifier(name, type, packageName);
	}

	private String path() {
		File filesDir = getExternalFilesDir(null);
		if (filesDir.exists() && filesDir.isDirectory()) {
			String parent = filesDir.getParent() + "/files";
			return parent;
		}
		return "";
	}
	private Bitmap comp(String imagePath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imagePath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }
	private Bitmap compressImage(Bitmap image) {
		if (image == null) {
			Log.e("wgq", "compressImage image is null ");
			return null;
		}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩        
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
	@SuppressLint("NewApi")
	public int getBitmapSize(Bitmap bitmap){
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){//API 12
	        return bitmap.getByteCount();
	    }
	    return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
	}
	public static String getRealFilePath( final Context context, final Uri uri ) {
	    if ( null == uri ) return null;
	    final String scheme = uri.getScheme();
	    String data = null;
	    if ( scheme == null )
	        data = uri.getPath();
	    else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
	        data = uri.getPath();
	    } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
	        Cursor cursor = context.getContentResolver().query( uri, new String[] { ImageColumns.DATA }, null, null, null );
	        if ( null != cursor ) {
	            if ( cursor.moveToFirst() ) {
	                int index = cursor.getColumnIndex( ImageColumns.DATA );
	                if ( index > -1 ) {
	                    data = cursor.getString( index );
	                }
	            }
	            cursor.close();
	        }
	    }
	    return data;
	}
}
