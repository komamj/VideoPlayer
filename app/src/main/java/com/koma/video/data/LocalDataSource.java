package com.koma.video.data;

import com.koma.video.data.model.Video;
import com.koma.video.video.VideosPresenter;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;


public class LocalDataSource implements VideoDataSource {
    @Override
    public Flowable<List<Video>> getAllVideos() {
        return Flowable.create(new FlowableOnSubscribe<List<Video>>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<List<Video>> e) throws Exception {
                e.onNext(VideosPresenter.getAllVideos());
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST);
    }
}
