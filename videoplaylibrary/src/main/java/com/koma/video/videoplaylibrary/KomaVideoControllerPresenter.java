package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.view.GestureDetector.SimpleOnGestureListener;

import com.koma.video.videoplaylibrary.util.KomaLogUtils;

/**
 * Created by koma on 6/8/17.
 */

public class KomaVideoControllerPresenter extends SimpleOnGestureListener implements
        KomaVideoControllerContract.Presenter {
    private static final String TAG = KomaVideoControllerPresenter.class.getSimpleName();
    private AudioManager mAudiomanager;
    @NonNull
    private KomaVideoControllerContract.View mView;

    public KomaVideoControllerPresenter(KomaVideoControllerContract.View view) {
        mView = view;
        mView.setPresenter(this);

        init();
    }

    private void init() {
        mAudiomanager = (AudioManager) mView.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void subscribe() {
        KomaLogUtils.i(TAG, "subscribe");
    }

    @Override
    public void unSubscribe() {
        KomaLogUtils.i(TAG, "unSubscribe");
    }

    @Override
    public SimpleOnGestureListener getGestureListener() {
        return this;
    }
}
