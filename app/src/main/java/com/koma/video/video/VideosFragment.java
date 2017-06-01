package com.koma.video.video;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.koma.video.R;
import com.koma.video.base.BaseFragment;
import com.koma.video.data.VideoRepository;
import com.koma.video.data.model.Video;
import com.koma.video.videoplaylibrary.util.KomaLogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by koma on 5/27/17.
 */

public class VideosFragment extends BaseFragment implements VideosConstract.View {
    private static final String TAG = VideosFragment.class.getSimpleName();
    @NonNull
    private VideosConstract.Presenter mPresenter;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private VideosAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new VideosPresenter(this, VideoRepository.getInstance());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        KomaLogUtils.i(TAG, "onViewCreated");

        initViews();
    }

    private void initViews() {
        mAdapter = new VideosAdapter(mContext, new ArrayList<Video>());

        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
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
        KomaLogUtils.i(TAG, "showVideos size : " + videoList.size());
        if (mAdapter != null) {
            mAdapter.replaceData(videoList);
        }
    }

    @Override
    public void showNoVideos() {

    }

    @Override
    public boolean isActive() {
        return this.isAdded();
    }
}
