package com.koma.video.videoplaylibrary;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.koma.video.videoplaylibrary.util.KomaLogUtils;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by koma on 5/23/17.
 */

public class KomaMediaController extends FrameLayout implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = MediaController.class.getSimpleName();

    private static final int DEFAULT_TIME_OUT = 3000;

    private MediaPlayerControl mPlayer;
    private SeekBar mSeekBar;
    private ImageButton mPauseButton, mNextButton, mPrevButton, mLockButton;
    private TextView mCurrentTime, mEndTime;
    private boolean mShowing;
    private boolean mLockShowing;
    private boolean mDragging;
    private Context mContext;

    private AccessibilityManager mAccessibilityManager;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private Toolbar mToolbar;
    private View mControlLayout;
    private boolean mLocked;

    public KomaMediaController(Context context) {
        super(context);

        mContext = context;

        init();
    }

    public KomaMediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KomaMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KomaMediaController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;

        init();
    }

    private void init() {
        mAccessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(mContext).inflate(R.layout.media_controller, this);

        initViews();

        mSeekBar.setOnSeekBarChangeListener(this);
    }

    private void initViews() {
        mPauseButton = (ImageButton) findViewById(R.id.ib_pause);
        mNextButton = (ImageButton) findViewById(R.id.ib_next);
        mPrevButton = (ImageButton) findViewById(R.id.ib_prev);
        mLockButton = (ImageButton) findViewById(R.id.ib_lock);
        mCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        mEndTime = (TextView) findViewById(R.id.tv_end_time);
        mSeekBar = (SeekBar) findViewById(R.id.sb_progress);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mControlLayout = (LinearLayout) findViewById(R.id.play_control_layout);
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(DEFAULT_TIME_OUT);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && !mLocked) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();

            }
            ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            if (mToolbar.getVisibility() != View.VISIBLE) {
                mToolbar.setVisibility(View.VISIBLE);
            }
            if (mControlLayout.getVisibility() != View.VISIBLE) {
                mControlLayout.setVisibility(View.VISIBLE);
            }
            mShowing = true;
        }

        if (mLockButton.getVisibility() != VISIBLE) {
            mLockButton.setVisibility(View.VISIBLE);
        }
        mLockShowing = true;

        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        post(mShowProgress);

        if (timeout != 0 && !mAccessibilityManager.isTouchExplorationEnabled()) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    public boolean isLockShowing() {
        return mLockShowing;
    }

    public void lock() {
        mLocked = true;

        if (mShowing) {
            try {
                removeCallbacks(mShowProgress);
                ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                if (mToolbar.getVisibility() != View.GONE) {
                    mToolbar.setVisibility(View.GONE);
                }
                if (mControlLayout.getVisibility() != View.GONE) {
                    mControlLayout.setVisibility(View.GONE);
                }
            } catch (IllegalArgumentException ex) {
                KomaLogUtils.e(TAG, "already removed");
            }
            mShowing = false;
        }

        show();
    }

    public void unLock() {
        mLocked = false;

        show();
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mLockShowing) {
            if (mLockButton.getVisibility() != View.GONE) {
                mLockButton.setVisibility(View.GONE);
            }
            mLockShowing = false;
        }
        if (mShowing) {
            try {
                removeCallbacks(mShowProgress);
                ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                if (mToolbar.getVisibility() != View.GONE) {
                    mToolbar.setVisibility(View.GONE);
                }
                if (mControlLayout.getVisibility() != View.GONE) {
                    mControlLayout.setVisibility(View.GONE);
                }
            } catch (IllegalArgumentException ex) {
                KomaLogUtils.e(TAG, "already removed");
            }
            mShowing = false;
        }
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            KomaLogUtils.i(TAG, "fade out mShowing : " + mShowing);
            if (mShowing) {
                hide();
            }
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPlayer.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

   /* @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                show(0); // show until hide is called
                break;
            case MotionEvent.ACTION_UP:
                show(DEFAULT_TIME_OUT); // start timeout
                break;
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                break;
        }
        return true;
    }*/

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(DEFAULT_TIME_OUT);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(DEFAULT_TIME_OUT);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(DEFAULT_TIME_OUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(DEFAULT_TIME_OUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(DEFAULT_TIME_OUT);
        return super.dispatchKeyEvent(event);
    }

    public void updatePausePlay() {
        if (mPauseButton == null)
            return;

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.ic_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    public void setClickListeners(OnClickListener listener) {
        mPrevButton.setOnClickListener(listener);
        mNextButton.setOnClickListener(listener);
        mPauseButton.setOnClickListener(listener);
    }

    public void setNextEnabled(boolean enabled) {
        mNextButton.setEnabled(enabled);
    }

    public void setPrevEnabled(boolean enabled) {
        mNextButton.setEnabled(enabled);
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
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) {
            // We're not interested in programmatically generated changes to
            // the progress bar's position.
            return;
        }
        long duration = mPlayer.getDuration();
        long newposition = (duration * progress) / 1000L;
        mPlayer.seekTo((int) newposition);
        mCurrentTime.setText(stringForTime((int) newposition));
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

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
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
    public void onStopTrackingTouch(SeekBar seekBar) {
        mDragging = false;
        setProgress();
        updatePausePlay();
        show(DEFAULT_TIME_OUT);

        // Ensure that progress is properly updated in the future,
        // the call to show() does not guarantee this because it is a
        // no-op if we are already showing.
        post(mShowProgress);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mPauseButton.setEnabled(enabled);
        mNextButton.setEnabled(enabled);
        mPrevButton.setEnabled(enabled);
        mSeekBar.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return KomaMediaController.class.getName();
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        /**
         * Get the audio session id for the player used by this VideoView. This can be used to
         * apply audio effects to the audio track of a video.
         *
         * @return The audio session, or 0 if there was an error.
         */
        int getAudioSessionId();
    }
}
