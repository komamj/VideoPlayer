package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.view.GestureDetector;

/**
 * Created by koma on 6/8/17.
 */

public interface KomaVideoControllerContract {
    interface View extends BaseView<Presenter> {
        Context getContext();

        void showSystemUI(boolean forceShow);

        void updateLockButton(boolean isLocked);
    }

    interface Presenter extends BasePresenter {
        GestureDetector.SimpleOnGestureListener getGestureListener();

    }
}
