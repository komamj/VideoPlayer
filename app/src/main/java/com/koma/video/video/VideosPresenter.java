package com.koma.video.video;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.Formatter;

import com.koma.video.KomaVideoApplication;
import com.koma.video.data.VideoRepository;
import com.koma.video.data.model.Video;
import com.koma.video.videoplaylibrary.util.KomaLogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koma on 5/27/17.
 */

public class VideosPresenter implements VideosConstract.Presenter {
    private static final String TAG = VideosPresenter.class.getSimpleName();
    private static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    private static final String VIDEOS_SELECTION = MediaStore.Video.Media.TITLE + " != ''";
    private static final String VIDEOS_SORT_ORDER = MediaStore.Video.Media.TITLE + " COLLATE UNICODE";

    private static final String[] VIDEOS_PROJECTION = new String[]{
            MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE, MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION, MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.WIDTH
    };

    private VideosConstract.View mView;

    private VideoRepository mRepository;


    public VideosPresenter(VideosConstract.View view, VideoRepository repository) {
        mView = view;
        mView.setPresenter(this);

        mRepository = repository;


    }


    public static List<Video> getAllVideos() {
        ContentResolver resolver = KomaVideoApplication.getContext().getContentResolver();
        ArrayList<Video> videoList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = resolver.query(VIDEO_URI, VIDEOS_PROJECTION, VIDEOS_SELECTION,
                    null, VIDEOS_SORT_ORDER);
            if (cursor != null) {
                if (!cursor.moveToFirst()) {
                    cursor.close();
                    return videoList;
                }
                do {
                    Video info = new Video();
                    info.setmId(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)));
                    info.setmFilePath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                    info.setmMineType(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)));
                    info.setmVideoName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
                    info.setmCreateTime(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)));
                    info.setmDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
                    long videoSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                    info.setmVideoSize(Formatter.formatFileSize(VideoApplication.getContext(), videoSize));

                    videoList.add(info);

                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            KomaLogUtils.e(TAG, "getAllVideos error : " + e.toString());
        } finally {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return videoList;
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unSubscribe() {

    }

    @Override
    public void loadVideos() {

    }
}
