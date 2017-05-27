package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by koma on 5/23/17.
 */

public class KomaVerticalSlideView extends LinearLayout {
    private Context mContext;

    private ImageView mIndicator;
    private View mPercentView;
    private View mContainer;

    public KomaVerticalSlideView(Context context) {
        super(context);

        mContext = context;

        init();
    }

    public KomaVerticalSlideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KomaVerticalSlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KomaVerticalSlideView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;

        init();
    }

    private void init() {
        setOrientation(VERTICAL);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(mContext).inflate(R.layout.vertical_slide_layout, this);

        initViews();
    }

    private void initViews() {
        mIndicator = (ImageView) findViewById(R.id.iv_indicator);
        mPercentView = (View) findViewById(R.id.iv_percent);
        mContainer = (FrameLayout) findViewById(R.id.iv_full);
    }

    public void setIndicatorImage(Bitmap bitmap) {
        if (mIndicator != null) {
            mIndicator.setImageBitmap(bitmap);
        }
    }

    public void setIndicatorImage(int resId) {
        if (mIndicator != null) {
            mIndicator.setImageResource(resId);
        }
    }

    public void setIndicatorImage(Drawable drawable) {
        if (mIndicator != null) {
            mIndicator.setImageDrawable(drawable);
        }
    }

    public void refresHeight(int current, int total) {
        // 变更进度条
        ViewGroup.LayoutParams lp = mPercentView.getLayoutParams();
        lp.height = mContainer.getLayoutParams().height * current / total;
        mPercentView.setLayoutParams(lp);
    }

}
