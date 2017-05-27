package com.koma.video.data;

import com.koma.video.data.model.Video;

import java.util.ArrayList;

import io.reactivex.Observable;


public class VideoRepository implements VideoDataSource {
    private static final String TAG = VideoRepository.class.getSimpleName();

    private static VideoRepository mRepostory;

    private LocalDataSource mLocalDataSource;

    private VideoRepository() {
        mLocalDataSource = new LocalDataSource();
    }

    public synchronized static VideoRepository getInstance() {
        if (mRepostory == null) {
            synchronized (VideoRepository.class) {
                if (mRepostory == null) {
                    mRepostory = new VideoRepository();
                }
            }
        }
        return mRepostory;
    }

    @Override
    public Observable<ArrayList<String>> getDetails(String data) {
        return mLocalDataSource.getDetails(data);
    }

    @Override
    public Observable<ArrayList<Video>> getAllVideos() {
        return mLocalDataSource.getAllVideos();
    }
}
