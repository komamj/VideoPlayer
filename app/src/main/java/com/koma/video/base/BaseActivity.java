package com.koma.video.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by koma on 5/27/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());

        ButterKnife.bind(this);

        if (!needRequestStoragePermission()) {
            //// TODO: 5/27/17
            init();
        }
    }

    public abstract void init();

    public abstract int getLayoutId();

    private boolean needRequestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        boolean needRequest = false;
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };
        ArrayList<String> permissionList = new ArrayList<String>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
                needRequest = true;
            }
        }

        if (needRequest) {
            int count = permissionList.size();
            if (count > 0) {
                String[] permissionArray = new String[count];
                for (int i = 0; i < count; i++) {
                    permissionArray[i] = permissionList.get(i);
                }

                requestPermissions(permissionArray, PERMISSION_REQUEST_CODE);
            }
        }

        return needRequest;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (checkPermissionGrantResults(grantResults)) {
                    //// TODO: 4/8/17
                    init();
                } else {
                    finish();
                }
            }
        }
    }

    private boolean checkPermissionGrantResults(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
