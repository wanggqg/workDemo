package com.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressLint("SetJavaScriptEnabled")
public class CommonUtils {
    public static  UnityWrapper unityWraper;
    public static WebViewDialog inst;
    
    private static class WebViewWrapper extends WebView {
    	
        public WebViewWrapper(Context context, String url) {
            super(context);

            getSettings().setJavaScriptEnabled(true);
            getSettings().setUseWideViewPort(true);
            getSettings().setLoadWithOverviewMode(true);

            setWebViewClient(new WebViewClient());
            
//            addJavascriptInterface(new Object() { 
//                @SuppressWarnings("unused")
//				public void clickOnCloseWindow(final int i) {
//                	Log.d("Unity", "clickOnCloseWindow");
//                	if(CommonUtils.inst != null){
//                		CommonUtils.inst.dismiss();
//                	}
//                    if(unityWraper != null){
//                    	unityWraper.sendUnityMessage("CloseWebView", "");
//                    }
//                }
//            }, "webview");

            
            loadUrl(url);

        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            return true;
        }

    }

    private static class WebViewDialog extends Dialog {
        private String mUrl = null;
        WebView webView = null;
        ImageView bkgImg = null;
        ImageView noticeTitle = null;
        ImageView closeBtn = null;
        RelativeLayout root = null;
        
        @Override
		public void onWindowFocusChanged(boolean hasFocus) {
			Log.d("Unity", "onWindowFocusChanged:"+hasFocus);
			if(!hasFocus){
				dismiss();
			}
			super.onWindowFocusChanged(hasFocus);
		}

		public WebViewDialog(Activity activity, String url) {
            super(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            setOwnerActivity(activity);
            mUrl = url;
        }

        public WebViewDialog(Activity activity, String url,int x,int y, int width, int height) {
            super(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            setOwnerActivity(activity);
            mUrl = url;
        }

        protected void deinit(){
        	root.removeAllViews();
        	webView.removeAllViews();
        	webView.destroyDrawingCache();
        	webView.destroy();
        	webView = null;
        	bkgImg.destroyDrawingCache();
            bkgImg = null;
            noticeTitle.destroyDrawingCache();
            noticeTitle = null;
            closeBtn.destroyDrawingCache();
            closeBtn = null;
        }
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            CommonUtils.inst = this;

            setCancelable(true);
            
            DisplayMetrics dm = new DisplayMetrics();
            getOwnerActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            int height = dm.heightPixels ;
            
            double bkgWidth = 612.0;
            double bkgHeight = 733.0;
            double scale = width/bkgWidth;
            
            //double bkgScaleWidth = bkgWidth*scale;
            double bkgScaleHeight = bkgHeight*scale;
            
            double webWidth = 559.0;
            double webHeight = 692.0;
            
            double webScaleWidth = webWidth*scale;
            double webScaleHeight = webHeight*scale;
            
            double mWidth = (int)webScaleWidth;
            double mHeight = (int)webScaleHeight;
            
            double titleWidth = 370.0;
            double titleHeight = 98;
            
            double titleScaleWidth = titleWidth*scale;
            double titleScaleHeight = titleHeight*scale;
            
            double btnCloseWidth = 221;
            double btnCloseHeight = 67;
            
            double btnCloseScaleWidth = btnCloseWidth*scale;
            double btnCloseScaleHeight = btnCloseHeight*scale;

            //Log.d("Unity", "width="+width+",height="+height+",scale="+scale);
           // Log.d("Unity", "bkgScaleWidth="+bkgScaleWidth+",bkgScaleHeight="+bkgScaleHeight);
            //Log.d("Unity", "webScaleWidth="+webScaleWidth+",webScaleHeight="+webScaleHeight);
          
            int topLogo = 28;
            double scaleTopLogo = topLogo*scale;
            double mY =  (height-bkgScaleHeight)*0.5+scaleTopLogo;

            root = new RelativeLayout(getOwnerActivity());
            root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            bkgImg = new ImageView(getOwnerActivity());
            RelativeLayout.LayoutParams bkgLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            bkgLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            bkgLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            bkgImg.setLayoutParams(bkgLayoutParams);
            bkgImg.setImageResource(getOwnerActivity().getResources().getIdentifier("notice", "drawable", getOwnerActivity().getPackageName()));
            
            root.addView(bkgImg);
            
            if(webView == null){
            	webView  = new WebViewWrapper(getOwnerActivity(), mUrl);
            }
           
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( (int)mWidth, (int)mHeight);
            layoutParams.topMargin = (int)mY;
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            webView.setLayoutParams(layoutParams);
            webView.setId(0x111222);
            //webView.setLayerType(layerType, paint);
            
            root.addView(webView);
           
            noticeTitle = new ImageView(getOwnerActivity());
            noticeTitle.setImageResource(getOwnerActivity().getResources().getIdentifier("pattern_gonggao", "drawable", getOwnerActivity().getPackageName()));
            RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            titleLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            titleLayoutParams.topMargin = (int)Math.round(mY - titleScaleHeight*0.72);
            titleLayoutParams.width = (int)titleScaleWidth;
            titleLayoutParams.height =(int)titleScaleHeight;
            noticeTitle.setLayoutParams(titleLayoutParams);
            root.addView(noticeTitle);
            
            closeBtn = new ImageView(getOwnerActivity());
            RelativeLayout.LayoutParams closeBtnLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            closeBtnLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 0x111222);
            closeBtnLayoutParams.topMargin = (int)Math.round(mY+mHeight+40*scale);
            closeBtnLayoutParams.width = (int)btnCloseScaleWidth;
            closeBtnLayoutParams.height = (int)btnCloseScaleHeight;
            closeBtn.setLayoutParams(closeBtnLayoutParams);
            closeBtn.setImageResource(getOwnerActivity().getResources().getIdentifier("button_bengongzhidaole", "drawable", getOwnerActivity().getPackageName()));
            
            //float btnScale = (float)btnCloseScale;
            //System.out.print("sacle="+btnScale);
            
           // closeBtn.setScaleX(btnScale);
           // closeBtn.setScaleY(btnScale);
            
            root.addView(closeBtn);
            
          
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	deinit();
                	dismiss();
                	System.gc();
                    if(unityWraper != null){
                    	unityWraper.sendUnityMessage("CloseWebView", "");
                    }
                }
            });
            
            setContentView(root);
        }
    }

    
    /**
     * 全屏显示一个webview界面
     * 
     * @param activity  需要显示webview的activity
     * @param url   显示url地址
     */
    public static void showFullWebView(final Activity activity, final String url) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Dialog webviewDialog = new WebViewDialog(activity, url);
                    webviewDialog.show();
                }
            });

        }
    }

    /**
     * 显示一个webview界面
     * 
     * @param activity  需要显示webview的activity
     * @param url   显示url地址
     * @param width 网页显示的宽度
     * @param height 网页显示的高度
     */
    public static void showWebView(final Activity activity, final String url) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
            	
                @Override
                public void run() {
                	Dialog webviewDialog = new WebViewDialog(activity, url,0,0,100,100);
                    webviewDialog.show();
                }
            });
        }
    }

}