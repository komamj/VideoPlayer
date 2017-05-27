package com.koma.video.data;

import com.koma.video.data.model.Video;

import java.util.ArrayList;

import io.reactivex.Observable;


public interface VideoDataSource {
    Observable<ArrayList<String>> getDetails(String data);

    Observable<ArrayList<Video>> getAllVideos();
}
