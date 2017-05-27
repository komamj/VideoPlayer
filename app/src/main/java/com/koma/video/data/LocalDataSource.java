/*
 * Copyright (C) 2017, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * LocalDataSource.java
 *
 * Description
 *
 * Author MaoJun
 *
 * Ver 1.0, Feb 15, 2017, MaoJun, Create file
 */
package com.koma.video.data;

import com.koma.video.data.model.Video;

import java.util.ArrayList;

import io.reactivex.Observable;


public class LocalDataSource implements VideoDataSource {
    @Override
    public Observable<ArrayList<String>> getDetails(String data) {
        return null;
    }

    @Override
    public Observable<ArrayList<Video>> getAllVideos() {
        return null;
    }
}
