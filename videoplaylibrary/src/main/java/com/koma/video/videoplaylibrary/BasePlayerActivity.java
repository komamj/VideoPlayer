package com.koma.video.videoplaylibrary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by koma on 6/12/17.
 */

public class BasePlayerActivity extends AppCompatActivity {
    private KomaVideoControllerContract.Presenter mPresenter;

    private KomaVideoControllerContract.View mView;

    @Override
    protected void onCreate(Bundle savedIntsanceState) {
        super.onCreate(savedIntsanceState);

        init();
    }

    private void init() {
        mPresenter = new KomaVideoControllerPresenter(mView);
    }
}
