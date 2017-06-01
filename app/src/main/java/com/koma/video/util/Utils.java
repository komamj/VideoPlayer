package com.koma.video.util;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by koma on 6/1/17.
 */

public class Utils {
    public static Uri getVideoUri(long id) {
        return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
    }
}
