package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by koma on 6/12/17.
 */

public class KomaVideoView extends SurfaceView {
    public KomaVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public KomaVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initVideoView(context);
    }

    public KomaVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KomaVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView(context);
    }

    private void initVideoView(Context context) {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public void addCallback(SurfaceHolder.Callback callback) {
        getHolder().addCallback(callback);
    }
}
