package com.koma.video.video;

import com.koma.video.base.BasePresenter;
import com.koma.video.base.BaseView;
import com.koma.video.data.model.Video;

import java.util.List;

/**
 * Created by koma on 5/27/17.
 */

public interface VideosConstract {
    interface View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showVideos(List<Video> videoList);

        void showNoVideos();

        boolean isActive();
    }

    interface Presenter extends BasePresenter {
        void loadVideos();
    }
}
