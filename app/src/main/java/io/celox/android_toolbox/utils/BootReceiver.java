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

package io.celox.android_toolbox.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.pepperonas.aespreferences.AesPrefs;

import io.celox.android_toolbox.MainService;
import io.celox.android_toolbox.R;

/**
 * @author Martin Pfeffer
 * <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 * @see <a href="https://celox.io">https://celox.io</a>
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        AesPrefs.init(context, "aes_config", "fSXwTkTKEH299YBcMKa6UeW", AesPrefs.LogMode.NONE);

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (AesPrefs.getBooleanRes(R.string.AUTOSTART_ENABLED, true)) {
                Log.d(TAG, "onReceive " + "--- AUTO START ---");
                Intent serviceIntent = new Intent(context, MainService.class);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                    ContextCompat.startForegroundService(context, serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}