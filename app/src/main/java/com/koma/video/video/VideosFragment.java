package com.koma.video.video;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.koma.video.R;
import com.koma.video.base.BaseFragment;
import com.koma.video.data.model.Video;
import com.koma.video.videoplaylibrary.util.KomaLogUtils;

import java.util.List;

/**
 * Created by koma on 5/27/17.
 */

public class VideosFragment extends BaseFragment implements VideosConstract.View {
    private static final String TAG = VideosFragment.class.getSimpleName();
    @NonNull
    private VideosConstract.Presenter mPresenter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        KomaLogUtils.i(TAG, "onViewCreated");
    }

    @Override
    public int getLayoutId() {
        return R.layout.videos_fragment_layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        KomaLogUtils.i(TAG, "onStart");

        if (mPresenter != null) {
            mPresenter.subscribe();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        KomaLogUtils.i(TAG, "onStop");

        if (mPresenter != null) {
            mPresenter.unSubscribe();
        }
    }

    @Override
    public void setPresenter(@NonNull VideosConstract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void showVideos(List<Video> videoList) {

    }

    @Override
    public void showNoVideos() {

    }

    @Override
    public boolean isActive() {
        return false;
    }
}
