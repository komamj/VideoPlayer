package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.SurfaceHolder;

import com.koma.video.videoplaylibrary.util.KomaLogUtils;

import java.io.IOException;

/**
 * Created by koma on 6/8/17.
 */

public class KomaVideoControllerPresenter extends SimpleOnGestureListener implements
        KomaVideoControllerContract.Presenter, AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener, SurfaceHolder.Callback {
    private static final String TAG = KomaVideoControllerPresenter.class.getSimpleName();

    private static final float ADJUST_LOWER = .2f;
    private static final float ADJUST_RAISE = 1.0f;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private int mCurrentBufferPercentage;

    private int mAudioSession;
    private SurfaceHolder mSurfaceHolder = null;
    private AudioManager mAudiomanager;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;
    @NonNull
    private KomaVideoControllerContract.View mView;

    private MediaPlayer mMediaPlayer = null;

    private Uri mUri;

    private boolean mPausedByTransientLossOfFocus = false; // recording audio focus loss

    public KomaVideoControllerPresenter(KomaVideoControllerContract.View view) {
        mView = view;
        mView.setPresenter(this);

        init();
    }

    private void init() {
        mAudiomanager = (AudioManager) mView.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void subscribe() {
        KomaLogUtils.i(TAG, "subscribe");
    }

    @Override
    public void unSubscribe() {
        KomaLogUtils.i(TAG, "unSubscribe");
    }

    @Override
    public SimpleOnGestureListener getGestureListener() {
        return this;
    }

    @Override
    public SurfaceHolder.Callback getCallback() {
        return this;
    }

    @Override
    public void setVideoPath(String path) {
        setVideoUri(Uri.parse(path));
    }

    @Override
    public void setVideoUri(Uri uri) {
        mUri = uri;

        mSeekWhenPrepared = 0;
    }

    @Override
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        this.mOnPreparedListener = l;
    }

    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        this.mOnCompletionListener = l;
    }

    @Override
    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        this.mOnErrorListener = l;
    }

    @Override
    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        this.mOnInfoListener = l;
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);

        try {
            mMediaPlayer = new MediaPlayer();
            // TODO: create SubtitleController in MediaPlayer, but we need
            // a context for the subtitle renderers

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mView.getContext(), mUri);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            //attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            //mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            KomaLogUtils.e(TAG, "Unable to open content: " + mUri + "," + ex.toString());
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            //mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } finally {
        }
    }

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            abandonAudioFocus();
        }
    }

    private int requestAudioFocus() {
        AudioManager am = (AudioManager) mView.getContext().getSystemService(Context.AUDIO_SERVICE);
        return am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    private void abandonAudioFocus() {
        AudioManager am = (AudioManager) mView.getContext().getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(this);
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mPausedByTransientLossOfFocus = false;
            if (requestAudioFocus() == AudioManager.AUDIOFOCUS_GAIN) {
                mMediaPlayer.start();
                mCurrentState = STATE_PLAYING;
            }
            //// TODO: 6/12/17 update mediacontroller
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
                //// TODO: 6/12/17 update mediacontroller
            }
        }
        mTargetState = STATE_PAUSED;
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(pos);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = pos;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        KomaLogUtils.i(TAG, "onAudioFocusChange : " + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying()) {
                    mPausedByTransientLossOfFocus = true;
                    pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (isPlaying()) {
                    mMediaPlayer.setVolume(ADJUST_LOWER, ADJUST_LOWER);
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mPausedByTransientLossOfFocus && !isPlaying()) {
                    start();
                } else if (isPlaying()) {
                    mMediaPlayer.setVolume(ADJUST_RAISE, ADJUST_RAISE);
                }
                break;
            default:
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        KomaLogUtils.i(TAG, "onPrepared");

        mCurrentState = STATE_PREPARED;

        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mMediaPlayer);
        }
        //// TODO: 6/12/17 enable mediacontroller
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        KomaLogUtils.i(TAG, "onVideoSizeChanged");
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        if (videoWidth != 0 && videoHeight != 0) {
            mSurfaceHolder.setFixedSize(videoWidth, videoHeight);
            mView.setVideoSize(videoWidth, videoHeight);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        KomaLogUtils.i(TAG, "onCompletion");
        mCurrentState = STATE_PLAYBACK_COMPLETED;
        mTargetState = STATE_PLAYBACK_COMPLETED;

        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mp);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        KomaLogUtils.e(TAG, "Error: " + what + "," + extra);
        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;
        /* If an error handler has been supplied, use it and finish. */
        if (mOnErrorListener != null) {
            if (mOnErrorListener.onError(mMediaPlayer, what, extra)) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        KomaLogUtils.i(TAG, "onInfo " + what + "," + extra);

        if (mOnInfoListener != null) {
            mOnInfoListener.onInfo(mp, what, extra);
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        KomaLogUtils.i(TAG, "onBufferingUpdate percent : " + percent);

        mCurrentBufferPercentage = percent;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        KomaLogUtils.i(TAG, "surfaceCreated");
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        KomaLogUtils.i(TAG, "surfaceChanged width : " + width + "," + "height : " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        KomaLogUtils.i(TAG, "surfaceDestroyed");

        mSurfaceHolder = null;

        release(true);
    }
}
