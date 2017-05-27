package com.koma.video;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by koma on 5/27/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
    }

    public abstract int getLayoutId();
}
