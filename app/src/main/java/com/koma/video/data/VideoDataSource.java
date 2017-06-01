package com.koma.video.data;

import com.koma.video.data.model.Video;

import java.util.List;

import io.reactivex.Flowable;


public interface VideoDataSource {
    Flowable<List<Video>> getAllVideos();
}
