package com.koma.video;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.koma.video.base.BaseActivity;
import com.koma.video.video.VideosFragment;

import butterknife.BindView;

/**
 * Created by koma on 5/27/17.
 */

public class MainActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(mToolbar);
    }

    public void init() {
        VideosFragment videosFragment = (VideosFragment) getSupportFragmentManager()
                .findFragmentById(R.id.content_main);

        if (videosFragment == null) {
            videosFragment = new VideosFragment();

            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, videosFragment)
                    .commit();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.acitivity_main;
    }
}
