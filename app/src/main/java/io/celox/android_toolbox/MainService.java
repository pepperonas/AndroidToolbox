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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pepperonas.aespreferences.AesPrefs;
import com.pepperonas.jbasx.base.Si;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.celox.android_toolbox.utils.Const;
import io.celox.android_toolbox.utils.Database;

public class MainService extends Service {

    private static final String TAG = "MainService";

    private static final int NOTIFICATION_ID = 1;

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;

    private long mTmpLastRx;
    private long mTmpLastTx;
    private long mTmpLastRxMobile;
    private long mTmpLastTxMobile;

    private String mClipboardText = "";
    private Database mDb;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDb = new Database(this);

        String channelId = getString(R.string.channel_id_network_notification);
        String channelName = getString(R.string.channel_name_network_notification);
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_NONE);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(notificationChannel);

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(new ClipboardListener());
        }

        mNotificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = mNotificationBuilder
                .setOngoing(true)
                .setSmallIcon(R.drawable.kbytes_0)
                .setContentTitle(getString(R.string.network_notification_content_title))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateNotification();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);

        return START_STICKY;
    }

    public void updateNotification() {
        long rx_ivl = (long) ((TrafficStats.getTotalRxBytes() - mTmpLastRx)
                / (float) Const.NETWORK_UPDATE_INTERVAL_SEC);
        long tx_ivl = (long) ((TrafficStats.getTotalTxBytes() - mTmpLastTx)
                / (float) Const.NETWORK_UPDATE_INTERVAL_SEC);
        long rxm_ivl = (long) ((TrafficStats.getMobileRxBytes() - mTmpLastRxMobile)
                / (float) Const.NETWORK_UPDATE_INTERVAL_SEC);
        long txm_ivl = (long) ((TrafficStats.getMobileTxBytes() - mTmpLastTxMobile)
                / (float) Const.NETWORK_UPDATE_INTERVAL_SEC);

        mTmpLastRx = TrafficStats.getTotalRxBytes();
        mTmpLastTx = TrafficStats.getTotalTxBytes();
        mTmpLastRxMobile = TrafficStats.getMobileRxBytes();
        mTmpLastTxMobile = TrafficStats.getMobileTxBytes();

        String sRx;
        String sTx;
        float fRx;
        float fTx;
        String unitRx = getString(R.string._unit_kilobytes_per_second);
        String unitTx = getString(R.string._unit_kilobytes_per_second);

        DecimalFormat df = new DecimalFormat("#.##");

        if (rx_ivl > Si.MEGA) {
            unitRx = getString(R.string._unit_megabytes_per_second);
            fRx = (float) rx_ivl / (float) Si.MEGA;
            sRx = df.format(fRx);
        } else {
            sRx = String.valueOf((rx_ivl / 1024));
        }

        if (tx_ivl > Si.MEGA) {
            unitTx = getString(R.string._unit_megabytes_per_second);
            fTx = (float) tx_ivl / (float) Si.MEGA;
            sTx = df.format(fTx);
        } else {
            sTx = String.valueOf((tx_ivl / 1024));
        }

        final String down = getString(R.string.down) + " " + sRx + " " + unitRx;
        final String up = getString(R.string.up) + " " + sTx + " " + unitTx;

        final long totalTraffic = (rx_ivl + tx_ivl);

        int imageResourceId;
        if (totalTraffic > Si.MEGA) {
            float f = totalTraffic / (float) Si.MEGA;
            String fStr = String.valueOf(f);
            imageResourceId = resolveDrawableId("mbytes__" +
                    fStr.split("\\.")[0] + "_" + fStr.split("\\.")[1].charAt(0));
        } else if (totalTraffic != 0) {
            imageResourceId = resolveDrawableId("kbytes_" + totalTraffic / (int) Si.KILO);
        } else {
            imageResourceId = resolveDrawableId("kbytes_" + 0);
        }

        try {
            if (imageResourceId == 0) {
                imageResourceId = resolveDrawableId("kbytes_" + 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        final int finalImageResourceId = imageResourceId;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mNotificationBuilder.setSmallIcon(finalImageResourceId);
                mNotificationBuilder.setContentTitle(down + "  |  " + up);
                mNotificationBuilder.setContentText(sdf.format(date) + " Clip: " + mClipboardText);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            }
        }, 1000);
    }

    private int resolveDrawableId(@NonNull String source) {
        try {
            String uri = "@drawable/" + source;
            return getResources().getIdentifier(uri, null, getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {

        private static final long DELTA_TIME_MS = 1000;

        private long mLastAddedClip = 0;

        public void onPrimaryClipChanged() {
            //            SharedPreferences preferences = getSharedPreferences(
            //                    Const.PREF_FILE_NAME, Context.MODE_PRIVATE);
            //            boolean clipboardEnabled = preferences.getBoolean(
            //                    Const.P_CLIPBOARD_ENABLED, true);
            if (!AesPrefs.getBooleanRes(R.string.CLIPBOARD_ENABLED, true)) {
                return;
            }
            try {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData cd = null;
                if (clipboardManager != null) {
                    cd = clipboardManager.getPrimaryClip();
                }
                ClipData.Item item;
                if (cd != null) {
                    item = cd.getItemAt(0);
                    mClipboardText = item.getText().toString();
                    Log.d(TAG, "onPrimaryClipChanged " + mClipboardText);
                    if ((System.currentTimeMillis() - mLastAddedClip) > DELTA_TIME_MS) {
                        mLastAddedClip = System.currentTimeMillis();
                        mDb.insertClipboardText(mClipboardText);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "onPrimaryClipChanged Error while getting clip-data");
            }
        }
    }
}
