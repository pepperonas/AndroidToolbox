/*
 * Copyright (c) 2019 Martin Pfeffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.celox.android_toolbox;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.pepperonas.aespreferences.AesPrefs;
import com.pepperonas.andbasx.base.ToastUtils;

import io.celox.android_toolbox.dialogs.DialogAbout;
import io.celox.android_toolbox.utils.Database;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_INITIAL_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.bringToFront();

        Intent serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);

        ensureRuntimePermissions();

        if (getResources().getBoolean(R.bool.wipe_database)) {
            new Database(this).wipe();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                new DialogAbout(this);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            if (requestCode == PERMISSION_INITIAL_REQUEST) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // store that permissions granted
                    AesPrefs.putBooleanRes(R.string.PERMISSIONS_GRANTED, true);

                } else {
                    Log.w(TAG, "onRequestPermissionsResult: " + getString(R.string.permissions_missing));
                    ToastUtils.toastLong(R.string.permissions_missing);
                    ensureRuntimePermissions();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onRequestPermissionsResult: " + e.getMessage());
        }
    }

    private void ensureRuntimePermissions() {
        try {
            AesPrefs.initBooleanRes(R.string.PERMISSIONS_GRANTED, false);
        } catch (Exception e) {
            android.util.Log.e(TAG, "ensureRuntimePermissions: ");
        }

        try {
            if (!AesPrefs.getBooleanRes(R.string.PERMISSIONS_GRANTED, false)) {
                String[] permissions = new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
                ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_INITIAL_REQUEST);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "ensureRuntimePermissions: ");
            String[] permissions = new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            ActivityCompat.requestPermissions(this, permissions, PERMISSION_INITIAL_REQUEST);
        }
    }

}
