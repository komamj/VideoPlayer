package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.media.AudioManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.koma.video.videoplaylibrary.util.KomaLogUtils;
import com.koma.video.videoplaylibrary.util.Utils;

/**
 * Created by koma on 5/23/17.
 */

public class KomaVideoPlayerView extends FrameLayout {
    private static final String TAG = KomaVideoPlayerView.class.getSimpleName();
    /**
     * 调节音量亮度进度条的修正比例，如此不需要手势划满屏幕高度即可完整调节进度条
     */
    private static final float ADJUSTED_PERCENT = 1.2f;

    private int mMaxVolume;
    private static final int MAX_ADJUST_PROGRESS = 100;
    private static final int MS_PER_SECOND = 1000;
    private static final String SLASH = "  /  ";
    private Context mContext;

    private GestureDetector mGestureDetector;
    private KomaMediaController mController;
    private KomaVideoView1 mPlayerView;
    private KomaVerticalSlideView mSlideBrightView, mSlideVolumeView;
    private TextView mSpeedView, mRetreatView;
    private boolean isProgressing = false;
    private boolean isInGesture = false;
    /**
     * 进度调节的目标位置
     */
    private long mProgressEndPoint;
    /**
     * 当前调整的音量
     */
    private int mVolume;

    /**
     * 当前调整的亮度
     */
    private int mBrightness;
    private AudioManager mAudioManager;
    private boolean isBrightnessSliding = false;

    private boolean isVolumeSliding = false;

    public KomaVideoPlayerView(Context context) {
        super(context);

        mContext = context;
    }

    public KomaVideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KomaVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KomaVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;

        init();
    }

    private void init() {
        mGestureDetector = new GestureDetector(mContext, new GestureListener());
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(mContext).inflate(com.koma.video.videoplaylibrary.R.layout.video_player, this);
        initViews();
    }

    private void initViews() {
        mController = (KomaMediaController) findViewById(com.koma.video.videoplaylibrary.R.id.media_controller);
        mPlayerView = (KomaVideoView1) findViewById(com.koma.video.videoplaylibrary.R.id.player_view);
        mSlideBrightView = (KomaVerticalSlideView) findViewById(com.koma.video.videoplaylibrary.R.id.slide_bright);
        mSlideBrightView.setIndicatorImage(com.koma.video.videoplaylibrary.R.drawable.ic_brightness_up);
        mSlideVolumeView = (KomaVerticalSlideView) findViewById(com.koma.video.videoplaylibrary.R.id.slide_volume);
        mSlideVolumeView.setIndicatorImage(com.koma.video.videoplaylibrary.R.drawable.ic_volume_up);
        mSpeedView = (TextView) findViewById(com.koma.video.videoplaylibrary.R.id.tv_speed);
        mRetreatView = (TextView) findViewById(com.koma.video.videoplaylibrary.R.id.tv_retreat);
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        KomaLogUtils.i(TAG,"onTouchEvent");
       /* switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mController.show(0); // show until hide is called
                break;
            case MotionEvent.ACTION_UP:
                mController.show(); // start timeout
                break;
            case MotionEvent.ACTION_CANCEL:
                mController.hide();
                break;
            default:
                break;
        }*/
        // return super.onTouchEvent(motionEvent);

        if (mGestureDetector.onTouchEvent(motionEvent)) {
            isInGesture = true;
        } else if (MotionEvent.ACTION_UP == (motionEvent.getAction() & MotionEvent.ACTION_MASK)) {
            if (isInGesture) {
                endGesture();
            }
        }
        return true;
    }

    /**
     * 手势结束
     */
    public void endGesture() {
        if (!isInGesture) {
            return;
        }
        if (mPlayerView != null && isProgressing) {
            mPlayerView.seekTo((int) mProgressEndPoint);
        }
        isProgressing = false;
        isBrightnessSliding = false;
        isVolumeSliding = false;
        // 隐藏
        removeCallbacks(mHideGestureView);
        postDelayed(mHideGestureView, 500);

        isInGesture = false;
    }

    private Runnable mHideGestureView = new Runnable() {
        @Override
        public void run() {
            mSlideBrightView.setVisibility(View.GONE);
            mSlideVolumeView.setVisibility(View.GONE);
            mSpeedView.setVisibility(View.GONE);
            mRetreatView.setVisibility(View.GONE);
        }
    };

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SCROLL_VERTICAL_LEFT = 2;

        private static final int SCROLL_VERTICAL_RIGHT = 3;

        private static final int SCROLL_HORIZONAL = 4;

        private static final int UNKNOW = 1;

        private int mMode;

        @Override
        public boolean onDown(MotionEvent e) {
            mMode = UNKNOW;
            return super.onDown(e);
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isEnabled()) {
                return false;
            }
            mController.hide();
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getY();
            int x = (int) e2.getX();

            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(dm);

            int windowWidth = dm.widthPixels;
            int windowHeight = dm.heightPixels;


            float dx = mOldX - x;
            float dy = mOldY - y;

            switch (mMode) {
                case SCROLL_VERTICAL_LEFT:
                case SCROLL_VERTICAL_RIGHT:
                    if (Utils.isRTL() ^ (mMode == SCROLL_VERTICAL_LEFT)) {
                        mSlideBrightView.setVisibility(View.GONE);
                        mSlideVolumeView.setVisibility(View.VISIBLE);
                        mSpeedView.setVisibility(View.GONE);
                        mRetreatView.setVisibility(View.GONE);
                        onVolumeSlide((mOldY - y) / windowHeight);
                    } else {
                        mSlideBrightView.setVisibility(View.VISIBLE);
                        mSlideVolumeView.setVisibility(View.GONE);
                        mSpeedView.setVisibility(View.GONE);
                        mRetreatView.setVisibility(View.GONE);
                        onBrightnessSlide((mOldY - y) / windowHeight);
                    }
                    break;

                case SCROLL_HORIZONAL:
                    onProgressSlide((x - mOldX) / windowWidth);
                    break;

                case UNKNOW:
                    if (Math.abs(dy) > (Math.abs(dx) * 3)) {
                        /** 如果起始点的x轴坐标小于屏幕坐标的一半，则说明是在左边屏幕滑动 */
                        if (mOldX > (windowWidth * 0.5)) {
                            mMode = SCROLL_VERTICAL_LEFT;
                        } else if (mOldX < (windowWidth * 0.5)) {
                            mMode = SCROLL_VERTICAL_RIGHT;
                        }
                    } else if (Math.abs(dx) > (Math.abs(dy) * 3)) {
                        mMode = SCROLL_HORIZONAL;
                    }
                    break;

                default:
                    mMode = UNKNOW;
                    break;
            }

            /** 防止隐藏 */
            removeCallbacks(mHideGestureView);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            KomaLogUtils.i(TAG, "onSingleTapConfirmed");
            if (isEnabled()) {
                if (mController.isShowing()) {
                    mController.hide();
                } else {
                    mController.show();
                    if (mRetreatView.getVisibility() != View.GONE) {
                        mRetreatView.setVisibility(View.GONE);
                    }
                    if (mSpeedView.getVisibility() != View.GONE) {
                        mSpeedView.setVisibility(View.GONE);
                    }
                    if (mSlideBrightView.getVisibility() != GONE) {
                        mSlideBrightView.setVisibility(View.GONE);
                    }
                    if (mSlideVolumeView.getVisibility() != GONE) {
                        mSlideVolumeView.setVisibility(View.GONE);
                    }
                }

            } else {
                if (mController.isLockShowing()) {
                    mController.hide();
                } else {
                    mController.show();
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            if (!isEnabled()) {
                return super.onDoubleTap(motionEvent);
            }
            if (mPlayerView != null) {
                if (mPlayerView.isPlaying()) {
                    mPlayerView.pause();
                } else {
                    mPlayerView.start();
                }
                return true;
            }
            return super.onDoubleTap(motionEvent);
        }
    }

    /**
     * 滑动调整进度
     */
    private void onProgressSlide(float percent) {
        if (mPlayerView == null) {
            return;
        }
        if (!isProgressing) {
            if (mSlideBrightView.getVisibility() != GONE) {
                mSlideBrightView.setVisibility(View.GONE);
            }
            if (mSlideVolumeView.getVisibility() != GONE) {
                mSlideVolumeView.setVisibility(View.GONE);
            }
            isProgressing = true;
        }


        int progressStartPoint = mPlayerView.getCurrentPosition();

        /** 如果视频长度小于100秒，则从屏幕最左端到最右端，手势调整的大小为视频的长度 */
        long duration = (long) (percent * Math.min(mPlayerView.getDuration(), MAX_ADJUST_PROGRESS * MS_PER_SECOND));
        mProgressEndPoint = progressStartPoint + duration;

        /** 调整后的进度不小于0，不超过视频长度 */
        mProgressEndPoint = Math.max(mProgressEndPoint, 0);
        mProgressEndPoint = Math.min(mProgressEndPoint, mPlayerView.getDuration());
        duration = mProgressEndPoint - progressStartPoint;

        //  int drawId = (Math.abs(duration) < MS_PER_SECOND) ? 0 : (duration > 0 ? R.mipmap.ic_progress_forward : R.mipmap.ic_progress_backward);


//      String str0 = (Math.abs(duration) < MS_PER_SECOND) ? NOMOVE : (duration > 0 ? FORWARD : BACKWARD);
//      String str1 = VideoUtils.formatDuration(Math.abs(duration));
        String str2 = Utils.formatDuration(mProgressEndPoint);
        String str3 = Utils.formatDuration(mPlayerView.getDuration());

        SpannableStringBuilder builder = new SpannableStringBuilder(str2 + SLASH + str3);
        int color2 = getResources().getColor(com.koma.video.videoplaylibrary.R.color.current_time_color);
        int color3 = getResources().getColor(com.koma.video.videoplaylibrary.R.color.total_time_color);
        builder.setSpan(new ForegroundColorSpan(color2), 0, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(color3), str2.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (duration > 0) {
            if (mSpeedView.getVisibility() != View.VISIBLE) {
                mSpeedView.setVisibility(View.VISIBLE);
            }
            mSpeedView.setText(builder);
            if (mRetreatView.getVisibility() != View.GONE) {
                mRetreatView.setVisibility(View.GONE);
            }
        } else {
            if (mSpeedView.getVisibility() != View.GONE) {
                mSpeedView.setVisibility(View.GONE);
            }
            if (mRetreatView.getVisibility() != View.VISIBLE) {
                mRetreatView.setVisibility(View.VISIBLE);
            }
            mRetreatView.setText(builder);
        }
    }

    /**
     * 滑动改变声音大小
     */
    public void onVolumeSlide(float percent) {
        percent = percent * ADJUSTED_PERCENT;
        if (!isVolumeSliding) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0) {
                mVolume = 0;
            }
            isVolumeSliding = true;
        }
        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
        mSlideVolumeView.refresHeight(index, mMaxVolume);
    }

    /**
     * 滑动改变亮度
     */
    private void onBrightnessSlide(float percent) {
        percent *= ADJUSTED_PERCENT;
        if (!isBrightnessSliding) {
            //  mBrightness = BrightnessManager.getBrightness(getContext());
            isBrightnessSliding = true;
        }
        // int brightness = (int) (BrightnessManager.BRIGHTNESS_MANUAL_MAX * percent);
        // brightness += mBrightness;
        // brightness = Math.max(brightness, BrightnessManager.BRIGHTNESS_MANUAL_MIN);
        // brightness = Math.min(brightness, BrightnessManager.BRIGHTNESS_MANUAL_MAX);
        // BrightnessManager.setBrightness(mContext, brightness);

        //mSlideBrightView.refresHeight(brightness, BrightnessManager.BRIGHTNESS_MANUAL_MAX);
    }
}
