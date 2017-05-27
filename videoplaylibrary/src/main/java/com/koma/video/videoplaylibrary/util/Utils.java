package com.koma.video.videoplaylibrary.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import java.util.Locale;

/**
 * Created by koma on 5/27/17.
 */

public class Utils {
    /**
     * 阅读习惯是否是从右到左
     *
     * @return true:从右到左 false:从左到右
     */
    public static boolean isRTL() {
        final Locale locale = Locale.getDefault();
        return TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Format duration.
     *
     * @param duration the duration
     * @return the string
     */
    public static String formatDuration(long duration) {
        long ss = duration / 1000 % 60;
        long mm = duration / 60000 % 60;
        long hh = duration / 3600000;
        if (duration < 60 * 60 * 1000) {
            return String.format(Locale.getDefault(), "%02d:%02d", mm, ss);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hh, mm, ss);
        }
    }

    public static String getFileNameFromUri(Context context, Uri uri) {
        String filePath = getFilePathFromUri(context, uri);
        if (filePath != null && filePath.length() != 0) {
            return filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        } else {
            return "";
        }
    }

    private static final String URI_DOWNLOADS_START = "content://downloads/";
    private static final String URI_BLUETOOTH_START = "content://com.google.android.bluetooth.fileprovider";
    private static final String PATH_BLUETOOTH_START = "/storage/emulated/0/bluetooth/";
    private static final String URI_PROVIDERS_START = "content://com.android.providers.media.documents/document/";
    private static final String URI_EXTERNALSTORAGE_START = "content://com.android.externalstorage.documents/document/";
    private static final String VIDEO_ID_PREFIX = "video:";
    private static final String URI_CONTENT_START = "content";
    private static final String URI_FILE_START = "file";

    public static String getFilePathFromUri(Context context, Uri uri) {
        if (uri == null || uri.toString().length() == 0) {
            return null;
        }
        if (uri.toString().startsWith(URI_DOWNLOADS_START)) {
            String[] projection = new String[]{"mediaprovider_uri"};
            return getValueForFile(context, uri, projection, null, null, null, null);
        } else if (uri.toString().startsWith(URI_BLUETOOTH_START)) {
            String valueForFile = getValueForFile(context, uri, null, null, null, null, null);
            return PATH_BLUETOOTH_START + valueForFile;
        } else if ((uri.toString().startsWith(URI_PROVIDERS_START))
                || uri.toString().startsWith(URI_EXTERNALSTORAGE_START)) {
            String _id = getValueForFile(context, uri, null, null, null, null, null);
            _id = _id.substring(VIDEO_ID_PREFIX.length(), _id.length());
            String where = MediaStore.Video.Media._ID + "=?";
            String[] selectionArgs = new String[]{_id, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME};
            return getValueForFile(context, uri, null, where, selectionArgs, null, MediaStore.Video.Media.DISPLAY_NAME);
        } else if (URI_CONTENT_START.compareTo(uri.getScheme()) == 0) {
            return getValueForFile(context, uri, null, null, null, null, MediaStore.Video.Media.DISPLAY_NAME);
        } else if (URI_FILE_START.compareTo(uri.getScheme()) == 0) {
            return uri.getPath();
        }
        return null;
    }

    private static String getValueForFile(Context context, Uri uri, String[] projection,
                                          String selection, String[] selectionArgs, String sortOrder, String columnName) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
            if ((cursor != null) && cursor.moveToFirst()) {
                if (columnName == null) {
                    return cursor.getString(0);
                } else {
                    int column_index = cursor.getColumnIndex(columnName);
                    return cursor.getString(column_index);
                }
            }
        } catch (IllegalStateException e) {
            KomaLogUtils.e("getValueForFile", "error : " + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
