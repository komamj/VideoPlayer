package com.koma.video.videoplaylibrary.util;

import android.util.Log;

/**
 * Created by koma on 5/23/17.
 */

public class KomaLogUtils {
    private static final boolean IS_DEBUG = true;
    private static final String TAG = "KomaVideo";

    public static void i(String tag, String msg) {
        if (IS_DEBUG) {
            Log.i(TAG, buildString(tag, msg));
        }
    }

    public static void e(String tag, String msg) {
        if (IS_DEBUG) {
            Log.e(TAG, buildString(tag, msg));
        }
    }

    private static String buildString(String tag, String msg) {
        StringBuilder sb = new StringBuilder(tag);
        sb.append("----");
        sb.append(msg);
        return sb.toString();
    }
}
