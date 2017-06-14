package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.koma.video.videoplaylibrary.util.KomaLogUtils;
import com.koma.video.videoplaylibrary.videoview.KomaVideoView;

import java.util.Formatter;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by koma on 6/8/17.
 */

public class KomaVideoControllerView extends FrameLayout implements KomaVideoControllerContract.View {
    private static final String TAG = KomaVideoControllerView.class.getSimpleName();

    private static final int DEFAULT_TIME_OUT = 3000;

    @BindView(R2.id.ib_lock)
    ImageButton mLockButton;
    @BindView(R2.id.tv_volume)
    TextView mVolumeView;
    @BindView(R2.id.tv_brightness)
    TextView mBrightnessView;
    @BindView(R2.id.tv_progress)
    TextView mProgressView;
    @BindView(R2.id.video_view)
    KomaVideoView mVideoView;
    @BindView(R2.id.media_controller)
    View mMediaController;
    @BindView(R2.id.sb_progress)
    SeekBar mProgress;
    @BindView(R2.id.tv_current_time)
    TextView mCurrentTime;
    @BindView(R2.id.tv_end_time)
    TextView mEndTime;
    @BindView(R2.id.ib_pause)
    ImageButton mPauseButton;

    private KomaVideoControllerContract.Presenter mPresenter;
    private GestureDetector mGestureDetector;
    private AccessibilityManager mAccessibilityManager;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private int mLastSystemUiVis;

    private boolean mIsLocked;
    private boolean mDragging;
    private boolean mShowing;

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

        LayoutInflater.from(getContext()).inflate(R.layout.video_player_layout, this);

        ButterKnife.bind(this, this);

        init();
    }

    private void init() {
        //mVideoView.addCallback(mPresenter.getCallback());

        mProgress.setOnSeekBarChangeListener(mSeekListener);

        mAccessibilityManager = (AccessibilityManager) getContext().getSystemService(
                Context.ACCESSIBILITY_SERVICE);

        mFormatBuilder = new StringBuilder();

        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
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
        int diff = mLastSystemUiVis ^ visible;
        mLastSystemUiVis = visible;
        if ((diff & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0
                && (visible & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            // show bars
            KomaLogUtils.i(TAG, "show bars");
            mLockButton.setVisibility(VISIBLE);

            if (!mShowing) {
                mMediaController.setVisibility(VISIBLE);
                mShowing = true;
            }

        } else {
            KomaLogUtils.i(TAG, "hide bars");
            mLockButton.setVisibility(GONE);

            if (mShowing) {
                mMediaController.setVisibility(GONE);
                mShowing = false;
            }
        }
    }

    @Override
    public void addCallback(SurfaceHolder.Callback callback) {
        //mVideoView.addCallback(callback);
    }

    @Override
    public void showSystemUI(boolean forceShow) {
        if (forceShow) {
            setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }

    @Override
    public void updateLockButton() {

    }

    @Override
    public void updatePausePlay() {
        if (mPresenter != null) {
            if (mPresenter.isPlaying()) {
                mPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                mPauseButton.setImageResource(R.drawable.ic_play);
            }
        }
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        mVideoView.setVideoSize(videoWidth, videoHeight);
    }

    @Override
    public void show() {
        show(DEFAULT_TIME_OUT);
    }

    @Override
    public void show(int timeout) {
        showSystemUI(true);

        if (timeout != 0 && !mAccessibilityManager.isTouchExplorationEnabled()) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            showSystemUI(false);
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPresenter.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private int setProgress() {
        if (mPresenter == null || mDragging) {
            return 0;
        }
        int position = mPresenter.getCurrentPosition();
        int duration = mPresenter.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPresenter.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @OnClick(R2.id.ib_lock)
    void lock() {
        if (mIsLocked) {
            mIsLocked = false;
            mLockButton.setImageResource(R.drawable.ic_lock_off);
        } else {
            mIsLocked = true;
            mLockButton.setImageResource(R.drawable.ic_lock_on);
        }
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            removeCallbacks(mShowProgress);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPresenter.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPresenter.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newposition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(DEFAULT_TIME_OUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            post(mShowProgress);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
}
