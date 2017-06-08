package com.koma.video.videoplaylibrary;

import android.view.GestureDetector;

/**
 * Created by koma on 6/8/17.
 */

public interface KomaVideoControllerContract {
    interface View extends BaseView<Presenter> {
        void showSystemUI(boolean forceShow);
    }

    interface Presenter extends BasePresenter {
        GestureDetector.SimpleOnGestureListener getGestureListener();
    }
}
