package com.koma.video.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by koma on 5/31/17.
 */

public class VideoImageView extends android.support.v7.widget.AppCompatImageView {
    public VideoImageView(Context context) {
        super(context);
    }

    public VideoImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthMeasureSpec, 9 * widthMeasureSpec / 16);
    }
}
