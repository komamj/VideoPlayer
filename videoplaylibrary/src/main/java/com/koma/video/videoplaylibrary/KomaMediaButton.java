package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by koma on 5/26/17.
 */

public class KomaMediaButton extends android.support.v7.widget.AppCompatImageButton {
    private static float ACTIVE_ALPHA = 1.0f;
    private static float INACTIVE_ALPHA = 0.4f;

    public KomaMediaButton(Context context) {
        super(context);
    }

    public KomaMediaButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KomaMediaButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        setAlpha(enabled ? ACTIVE_ALPHA : INACTIVE_ALPHA);
    }
}
