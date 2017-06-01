package com.koma.video.video;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.koma.video.KomaVideoApplication;
import com.koma.video.data.VideoRepository;
import com.koma.video.data.model.Video;
import com.koma.video.videoplaylibrary.util.KomaLogUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Created by koma on 5/27/17.
 */

public class VideosPresenter implements VideosConstract.Presenter {
    private static final String TAG = VideosPresenter.class.getSimpleName();

    private static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    private static final String VIDEOS_SELECTION = MediaStore.Video.Media.TITLE + " != ''";
    private static final String VIDEOS_SORT_ORDER = MediaStore.Video.Media.DATE_ADDED + " DESC";

    private static final String[] VIDEOS_PROJECTION = new String[]{
            MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATE_ADDED,
    };

    private VideosConstract.View mView;

    private VideoRepository mRepository;

    private CompositeDisposable mDisposables;

    public VideosPresenter(VideosConstract.View view, VideoRepository repository) {
        mView = view;
        mView.setPresenter(this);

        mDisposables = new CompositeDisposable();

        mRepository = repository;
    }


    @Override
    public void subscribe() {
        loadVideos();
    }

    @Override
    public void unSubscribe() {
        if (mDisposables != null) {
            mDisposables.clear();
        }
    }

    @Override
    public void loadVideos() {
        if (mDisposables != null) {
            mDisposables.clear();
        }

        Disposable disposable = mRepository.getAllVideos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<List<Video>>() {

                    @Override
                    public void onNext(List<Video> videoList) {
                        if (mView != null && mView.isActive()) {
                            mView.showVideos(videoList);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        KomaLogUtils.e(TAG, "onError error : " + t.toString());
                    }

                    @Override
                    public void onComplete() {
                        KomaLogUtils.i(TAG, "onComplete");
                    }
                });

        mDisposables.add(disposable);
    }

    public static List<Video> getAllVideos() {
        ContentResolver resolver = KomaVideoApplication.getContext().getContentResolver();
        ArrayList<Video> videoList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = resolver.query(VIDEO_URI, VIDEOS_PROJECTION, VIDEOS_SELECTION,
                    null, VIDEOS_SORT_ORDER);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Video video = new Video();
                    video.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID)));
                    video.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                    video.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
                    videoList.add(video);

                } while (cursor.moveToNext());
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
}
