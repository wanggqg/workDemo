package com.webview;


public final class Log {
    static boolean LOG = true;

//    public static void setLoggable(boolean loggable) {
//        LOG = loggable;
//    }

    public static void i(String tag, String string) {
        if (LOG) android.util.Log.i(tag, string);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (LOG) android.util.Log.i(tag, msg, tr);
    }

    public static void e(String tag, String string) {
        if (LOG) android.util.Log.e(tag, string);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (LOG) android.util.Log.e(tag, msg, tr);
    }

    public static void d(String tag, String string) {
        if (LOG) android.util.Log.d(tag, string);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (LOG) android.util.Log.d(tag, msg, tr);
    }
    public static void v(String tag, String string) {
        if (LOG) android.util.Log.v(tag, string);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (LOG) android.util.Log.v(tag, msg, tr);
    }
    public static void w(String tag, String string) {
        if (LOG) android.util.Log.w(tag, string);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (LOG) android.util.Log.w(tag, msg, tr);
    }
}
