package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toolbar;

import com.koma.video.videoplaylibrary.util.KomaLogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by koma on 6/8/17.
 */

public class KomaVideoControllerView extends FrameLayout implements KomaVideoControllerContract.View {
    private static final String TAG = KomaVideoControllerView.class.getSimpleName();
    @BindView(R2.id.ib_lock)
    ImageButton mLockButton;
    private KomaVideoControllerContract.Presenter mPresenter;
    private GestureDetector mGestureDetector;

    private int mLastSystemUiVis;

    public KomaVideoControllerView(@NonNull Context context) {
        super(context);
    }

    public KomaVideoControllerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KomaVideoControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KomaVideoControllerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setPresenter(KomaVideoControllerContract.Presenter presenter) {
        mPresenter = presenter;

        mGestureDetector = new GestureDetector(getContext(), mPresenter.getGestureListener());
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.media_controller, this);

        ButterKnife.bind(this, this);
    }

    @Override
    public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);

        KomaLogUtils.i(TAG, "onWindowFocusChanged focus : " + focus);
    }

    @Override
    public void onWindowSystemUiVisibilityChanged(int visible) {
        super.onWindowSystemUiVisibilityChanged(visible);

        KomaLogUtils.i(TAG, "onWindowSystemUIVisibilityChanged visible : " + visible);
        int diff = mLastSystemUiVis ^ visible;
        mLastSystemUiVis = visible;
        if ((diff & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0
                && (visible & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            // show bars
            KomaLogUtils.i(TAG, "show bars");
        } else {
            KomaLogUtils.i(TAG, "hide bars");
        }
    }

    @Override
    public void showSystemUI(boolean forceShow) {
        int flags = 0;
        if (forceShow) {
            flags = SYSTEM_UI_FLAG_LOW_PROFILE;
            flags |= (SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setSystemUiVisibility(flags);
    }

    @Override
    public void updateLockButton(boolean isLocked) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
}
