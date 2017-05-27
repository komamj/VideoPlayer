package com.koma.video.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.koma.video.R;
import com.koma.video.base.BaseActivity;
import com.koma.video.videoplaylibrary.KomaGestureController;
import com.koma.video.videoplaylibrary.KomaMediaController;
import com.koma.video.videoplaylibrary.KomaVideoView;
import com.koma.video.videoplaylibrary.util.KomaLogUtils;
import com.koma.video.videoplaylibrary.util.Utils;

public class PlayVideoActivity extends BaseActivity implements View.OnClickListener,
        MediaPlayer.OnCompletionListener, KomaVideoView.PauseListener {
    private static final String TAG = PlayVideoActivity.class.getSimpleName();

    private KomaVideoView mVideoView;
    private KomaMediaController mController;

    private int mPlayTime;
    /**
     * 当前视频的uri.
     */
    private Uri mContentUri;
    private String mVideoTitle;
    private Toolbar mToolbar;
    private boolean mIsLock, mPauseState;
    private ImageButton mLockButton;
    private KomaGestureController mGestureControllerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        super.onCreate(savedInstanceState);

        KomaLogUtils.i(TAG, "onCreate");

        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(this);
        mLockButton = (ImageButton) findViewById(R.id.ib_lock);
        mGestureControllerView = (KomaGestureController) findViewById(R.id.gesture_controller);
        mVideoView = (KomaVideoView) findViewById(R.id.player_view);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setPauseListener(this);
        mController = (KomaMediaController) findViewById(R.id.media_controller);
        mController.setClickListeners(this);
        mController.hide();
        mVideoView.setMediaController(mController);
        mLockButton.setOnClickListener(this);

        handleIntent();

        mVideoView.setVideoURI(mContentUri);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        KomaLogUtils.i(TAG, "onWindowFocusChanged hasFocus : " + hasFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        KomaLogUtils.i(TAG, "onSaveInstanceState");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        KomaLogUtils.i(TAG, "onRestoreInstanceState");
    }

    /**
     * Gets the data from intent.
     *
     * @return the data from intent
     */
    private void handleIntent() {

        Intent intent = getIntent();
        if (intent.getAction() == null) {
            finish();
            return;
        }

        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            /** 从其他应用发出的intent*/
            mContentUri = intent.getData();
            if (mContentUri == null) {
                mContentUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample);
            }
            mVideoTitle = Utils.getFileNameFromUri(this, mContentUri);
        }
        if (mContentUri == null) {
            mContentUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample);
            mVideoTitle = Utils.getFileNameFromUri(this, mContentUri);
        }
        KomaLogUtils.i(TAG, "mVideoTitle : " + mVideoTitle + "uri : " + mContentUri);
    }

    @Override
    public void onStart() {
        super.onStart();

        KomaLogUtils.i(TAG, "onStart");

        //监听耳机拔出事件
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mHeadsetReceiver, intentFilter);

        initPlayer();

        initBrightness();
    }

    private void initPlayer() {
        mVideoView.seekTo(mPlayTime);

        if (mPauseState) {
            mVideoView.pause();
        } else {
            mVideoView.start();
        }
        mVideoView.requestFocus();
    }

    private void initBrightness() {
       /* mIsAutoBrightness = BrightnessManager.isAutoBrightness(this);
        if (mIsAutoBrightness) {
            BrightnessManager.setBrightnessMode(this, false);
            if (mFromCamera) {
                if (BrightnessManager.getBrightness(this) < BrightnessManager.BRIGHTNESS_MANUAL_CAMAERA) {
                    BrightnessManager.setBrightness(this, BrightnessManager.BRIGHTNESS_MANUAL_CAMAERA);
                }
            }
        }
        mSystemBrightness = BrightnessManager.getBrightness(this);
        if (!mFromCamera) {
            if (mSystemBrightness + BrightnessManager.BRIGHTNESS_MANUAL_INCREASE >=
                    BrightnessManager.BRIGHTNESS_MANUAL_MAX) {
                BrightnessManager.setBrightness(this, BrightnessManager.BRIGHTNESS_MANUAL_MAX);
            } else {
                BrightnessManager.setBrightness(this, mSystemBrightness +
                        BrightnessManager.BRIGHTNESS_MANUAL_INCREASE);
            }
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();

        KomaLogUtils.i(TAG, "onResume");

        updateControllerState();
    }

    private void updateControllerState() {
        mToolbar.setTitle(mVideoTitle);
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

    @Override
    public void onPause() {
        super.onPause();

        KomaLogUtils.i(TAG, "onPause");

        savePlayTime();
    }

    private void savePlayTime() {
        mPlayTime = mVideoView.getCurrentPosition();

    }

    @Override
    public void onStop() {
        super.onStop();

        KomaLogUtils.i(TAG, "onStop");

        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }

        resetBrightness();

        unregisterReceiver(mHeadsetReceiver);
    }

    private void resetBrightness() {
        /*if (mIsAutoBrightness) {
            BrightnessManager.setBrightnessMode(this, true);
        }
        BrightnessManager.setBrightness(this, mSystemBrightness);*/
    }

    private BroadcastReceiver mHeadsetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if ((mVideoView != null) && mVideoView.isPlaying()) {
                    mVideoView.pause();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        KomaLogUtils.i(TAG, "onDestroy");

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_play_video;
    }

    private boolean isInMultiWindow() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.N && isInMultiWindowMode();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_next:
                if (Utils.isRTL()) {
                    //play prev
                } else {
                    //play next
                }
                break;
            case R.id.ib_pause:
                if (mVideoView != null) {
                    if (mVideoView.isPlaying()) {
                        mPauseState = true;
                        mVideoView.pause();
                    } else {
                        mPauseState = false;
                        mVideoView.start();
                        if (mController != null) {
                            mController.updatePausePlay();
                        }
                    }
                }

                break;
            case R.id.ib_prev:
                if (Utils.isRTL()) {
                    //play next
                } else {
                    //play prev
                }
                break;
            case R.id.ib_lock:
                mIsLock = !mIsLock;
                mLockButton.setImageResource(mIsLock ? R.drawable.ic_lock_on : R.drawable.ic_lock_off);
                mGestureControllerView.setEnabled(!mIsLock);
                if (mIsLock) {
                    mController.lock();
                } else {
                    mController.unLock();
                }
                break;
            default:
                this.finish();
                break;
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        this.finish();
    }

    @Override
    public void updatePauseState() {
        mPauseState = true;
    }
}
