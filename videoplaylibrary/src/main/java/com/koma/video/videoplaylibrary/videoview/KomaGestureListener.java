package com.koma.video.videoplaylibrary.videoview;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.koma.video.videoplaylibrary.util.KomaLogUtils;

/**
 * Created by koma on 6/14/17.
 */

public class KomaGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = KomaGestureListener.class.getSimpleName();

    private int mWindowWidth;
    private int mWindowHeight;
    private static int sTouchSlop;
    private int mCurrentVolume;
    private float mCurrentBrightness;
    private float mDownX;

    private boolean mChangeVolume;
    private boolean mChangeBrightness;
    private boolean mChangePosition;

    private Context mContext;

    private AudioManager mAudioManager;

    public KomaGestureListener(Context context) {
        mContext = context;

        mWindowWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        mWindowHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        sTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        KomaLogUtils.i(TAG, "onDown");

        mChangeBrightness = false;
        mChangePosition = false;
        mChangeVolume = false;

        mDownX = event.getX();

        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        KomaLogUtils.i(TAG, "current volume : " + mCurrentVolume);

        WindowManager.LayoutParams layoutParams = ((AppCompatActivity) mContext)
                .getWindow().getAttributes();
        if (layoutParams.screenBrightness < 0) {
            try {
                mCurrentBrightness = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
                KomaLogUtils.i(TAG, "current system brightness: " + mCurrentBrightness);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            mCurrentBrightness = layoutParams.screenBrightness * 255;
            KomaLogUtils.i(TAG, "current activity brightness: " + mCurrentBrightness);
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //KomaLogUtils.i(TAG, "onScroll distanceX : " + distanceX + ", distanceY : " + distanceY);
        if (Math.abs(distanceX) <= sTouchSlop && Math.abs(distanceY) <= sTouchSlop) {
            KomaLogUtils.i(TAG, "the scroll distance is too short,so not handle it ");
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        if (!mChangeBrightness && !mChangeVolume && !mChangePosition) {
            if (Math.abs(distanceX) > sTouchSlop) {
                mChangePosition = true;
            } else if (Math.abs(distanceY) > sTouchSlop) {
                if (mDownX > mWindowWidth * .5f) {
                    mChangeVolume = true;
                } else {
                    mChangeBrightness = true;
                }
            }
        }

        if (mChangeBrightness) {
            int dY = (int) (255 * distanceY * 3 / mWindowHeight);
            Window window = ((AppCompatActivity) mContext).getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            if (((mCurrentBrightness + dY) / 255) >= 1) {
                params.screenBrightness = 1;
            } else if (((mCurrentBrightness + dY) / 255) <= 0) {
                params.screenBrightness = 0.01f;
            } else {
                params.screenBrightness = (mCurrentBrightness + dY) / 255;
            }
            KomaLogUtils.i(TAG, "changeBrightness : " + params.screenBrightness);
            window.setAttributes(params);
        }
        if (mChangeVolume) {
            float percent = distanceY / mWindowHeight;
            KomaLogUtils.i(TAG, "percent: " + percent);
            percent = percent * 1.2f;
            int dY = (int) (percent * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            KomaLogUtils.i(TAG, "dY : " + dY);
            int index = dY + mCurrentVolume;
            if (index > mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                index = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            } else if (index < 0) {
                index = 0;
            }
            KomaLogUtils.i(TAG, "changeVolume dY :" + dY + ",percent : " + distanceY / mWindowHeight);

            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        }

        if (mChangePosition) {

        }

        return super.onScroll(e1, e2, distanceX, distanceX);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        KomaLogUtils.i(TAG, "onSingleTapConfirmed");

        return super.onSingleTapConfirmed(event);
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        KomaLogUtils.i(TAG, "onDoubleTap");

        return super.onDoubleTap(motionEvent);
    }
}
