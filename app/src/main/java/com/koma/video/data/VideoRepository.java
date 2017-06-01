package com.koma.video.data;

import com.koma.video.data.model.Video;

import java.util.List;

import io.reactivex.Flowable;


public class VideoRepository implements VideoDataSource {
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
    public Flowable<List<Video>> getAllVideos() {
        return mLocalDataSource.getAllVideos();
    }
}
