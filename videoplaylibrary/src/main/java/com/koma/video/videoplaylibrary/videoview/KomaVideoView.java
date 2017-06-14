package com.koma.video.videoplaylibrary.videoview;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.accessibility.AccessibilityManager;

import com.koma.video.videoplaylibrary.util.Constants;
import com.koma.video.videoplaylibrary.util.KomaLogUtils;

/**
 * Created by koma on 6/12/17.
 */

public class KomaVideoView extends SurfaceView implements KomaContract.View {
    private static final String TAG = KomaVideoView.class.getSimpleName();

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private boolean mShowing;

    private AccessibilityManager mAccessibilityManager;

    private GestureDetector mGestureDetector;
    @NonNull
    private KomaContract.Presenter mPresenter;

    private SurfaceHolder mSurfaceHolder = null;

    public KomaVideoView(Context context) {
        super(context);

        initVideoView();
    }

    public KomaVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

        initVideoView();
    }

    public KomaVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KomaVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initVideoView();
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;

        mAccessibilityManager = (AccessibilityManager) getContext().getSystemService(
                Context.ACCESSIBILITY_SERVICE);

        getHolder().addCallback(mSHCallback);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public void setVideoSize(int videoWidth, int videoHeight) {
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;

        if (mVideoWidth != 0 && mVideoHeight != 0) {
            getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        KomaLogUtils.i(TAG, "onMeasure");

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                KomaLogUtils.i(TAG, "mVideoWidth : " + mVideoWidth + "," + "mVideoHeight : "
                        + mVideoHeight + "," + "width : " + width + ", height : " + height);
                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    KomaLogUtils.i(TAG, "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    KomaLogUtils.i(TAG, "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            KomaLogUtils.i(TAG, "surfaceChanged width : " + w + ",height : " + h);

            mSurfaceWidth = w;
            mSurfaceHeight = h;

            if (mPresenter == null) {
                return;
            }

            boolean isValidState = (mPresenter.isTargetPlaying());
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);

            if (isValidState && hasValidSize) {
                if (mPresenter.getSeekWhenPrepared() != 0) {
                    mPresenter.seekTo(mPresenter.getSeekWhenPrepared());
                }
                mPresenter.start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            KomaLogUtils.i(TAG, "surfaceCreated");

            mSurfaceHolder = holder;

            if (mPresenter != null) {
                mPresenter.openVideo();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            KomaLogUtils.i(TAG, "surfaceDestroyed");
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            //// TODO: 6/13/17 hide controller
            if (mPresenter != null) {
                mPresenter.release(true);
            }
        }
    };

    @Override
    public void setPresenter(KomaContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();

        if (mVideoWidth != 0 && mVideoHeight != 0) {
            KomaLogUtils.i(TAG, "onPrepared, video size: " + mVideoWidth + "/" + mVideoHeight);

            getHolder().setFixedSize(mVideoWidth, mVideoHeight);

            if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                // We didn't actually change the size (it was already at the size
                // we need), so we won't get a "surface changed" callback, so
                // start the video here instead of in the callback.
                if (mPresenter.isTargetPlaying()) {
                    mPresenter.start();
                }
            }
        } else {
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mPresenter.isTargetPlaying()) {
                mPresenter.start();
            }
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();

        if (mVideoWidth != 0 && mVideoHeight != 0) {
            getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            requestLayout();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return mGestureDetector.onTouchEvent(motionEvent);
    }

    @Override
    public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);

        KomaLogUtils.i(TAG, "onWindowFocusChanged focus : " + focus);

        showSystemUI(false);
    }

    @Override
    public void onWindowSystemUiVisibilityChanged(int visible) {
        super.onWindowSystemUiVisibilityChanged(visible);

        KomaLogUtils.i(TAG, "onWindowSystemUIVisibilityChanged visible : " + visible);

        if (visible == (SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)) {
            mShowing = true;
            //// TODO: 6/13/17 show controller
        } else {
            mShowing = false;
            //// TODO: 6/13/17 hide controller
        }
    }

    @Override
    public void show() {
        show(Constants.DEFAULT_TIME_OUT);
    }

    @Override
    public void hide() {
        showSystemUI(false);
        mShowing = false;
    }

    @Override
    public boolean isShowing() {
        return this.mShowing;
    }

    @Override
    public void show(int timeout) {
        if (!mShowing) {
            showSystemUI(true);
            mShowing = true;
        }


        if (timeout != 0 && !mAccessibilityManager.isTouchExplorationEnabled()) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            if (mShowing) {
                hide();
            }
        }
    };

    private void showSystemUI(boolean forceShow) {
        if (forceShow) {
            setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
