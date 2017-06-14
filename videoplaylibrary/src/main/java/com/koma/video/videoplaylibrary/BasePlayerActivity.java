package com.koma.video.videoplaylibrary;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.koma.video.videoplaylibrary.util.KomaLogUtils;
import com.koma.video.videoplaylibrary.videoview.KomaContract;
import com.koma.video.videoplaylibrary.videoview.KomaPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by koma on 6/12/17.
 */

public class BasePlayerActivity extends AppCompatActivity {
    private static final String TAG = BasePlayerActivity.class.getSimpleName();

    private KomaContract.Presenter mPresenter;
    @BindView(R2.id.video_view)
    KomaContract.View mView;

    @Override
    protected void onCreate(Bundle savedIntsanceState) {
        super.onCreate(savedIntsanceState);

        setContentView(R.layout.activity_base_player);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        mPresenter = new KomaPresenter(mView);

        mPresenter.setVideoUri(Uri.parse("content://media/external/video/media/305"));

        mPresenter.subscribe();
    }

    @Override
    protected void onStart() {
        super.onStart();

        KomaLogUtils.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();

        KomaLogUtils.i(TAG, "onResume");

        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        KomaLogUtils.i(TAG, "onPause");

        mPresenter.stopPlayback();
    }

    @Override
    protected void onStop() {
        super.onStop();

        KomaLogUtils.i(TAG, "onStop");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        KomaLogUtils.i(TAG, "onConfigurationChanged newConfig : " + newConfig.toString());
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);

        KomaLogUtils.i(TAG, "onMultiWindowModeChanged: " + isInMultiWindowMode);
    }
}
