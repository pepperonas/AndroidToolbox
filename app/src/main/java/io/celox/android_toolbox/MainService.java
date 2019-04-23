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
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.pepperonas.aespreferences.AesPrefs;
import com.pepperonas.jbasx.base.Si;

import java.text.DecimalFormat;

import io.celox.android_toolbox.models.ClipDataAdvanced;
import io.celox.android_toolbox.utils.Const;
import io.celox.android_toolbox.utils.Database;
import io.celox.android_toolbox.utils.Log;

/**
 * @author Martin Pfeffer
 * <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 * @see <a href="https://celox.io">https://celox.io</a>
 */
public class MainService extends Service {

    private static final String TAG = "MainService";

    private static final int NOTIFICATION_ID = 1;

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;

    private long mTmpLastRx;
    private long mTmpLastTx;
    private long mTmpLastRxMobile;
    private long mTmpLastTxMobile;

    private long mTsServiceStarted;

    private Database mDb;

    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                updateNotification();
            } finally {
                mHandler.postDelayed(mRunnable, 1000);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mTsServiceStarted = System.currentTimeMillis();
        mDb = new Database(MainService.this);

        String channelId = getString(R.string.channel_id_network_notification);
        String channelName = getString(R.string.channel_name_network_notification);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName,
                    NotificationManager.IMPORTANCE_NONE);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(new ClipboardListener());
        }

        mNotificationBuilder = new NotificationCompat.Builder(MainService.this, channelId);
        Notification notification = mNotificationBuilder
                .setOngoing(true)
                .setSmallIcon(R.drawable.kbytes_0)
                .setContentTitle(getString(R.string.network_notification_content_title))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setShowWhen(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        Intent notificationIntent = new Intent(MainService.this, ClipboardDialogActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainService.this, 0, notificationIntent, 0);
        mNotificationBuilder.setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, notification);

        mHandler.post(mRunnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

        int imageResourceId = 0;
        try {
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
            // not found...
            if (imageResourceId == 0) {
                imageResourceId = resolveDrawableId("kbytes_" + 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String maxRx;
        String maxTx;

        if ((mTsServiceStarted + 3000) < System.currentTimeMillis()) {
            if (AesPrefs.getLongRes(R.string.MAX_RX, 0L) < rx_ivl) {
                AesPrefs.putLongRes(R.string.MAX_RX, rx_ivl);
            }
            if (AesPrefs.getLongRes(R.string.MAX_TX, 0L) < tx_ivl) {
                AesPrefs.putLongRes(R.string.MAX_TX, tx_ivl);
            }
        }

        if (AesPrefs.getLongRes(R.string.MAX_RX, 0L) > Si.MEGA) {
            float f = (float) AesPrefs.getLongRes(R.string.MAX_RX, 0L) / (float) Si.MEGA;
            maxRx = df.format(f) + " " + getString(R.string._unit_megabytes_per_second);
        } else {
            maxRx = AesPrefs.getLongRes(R.string.MAX_RX, 0L) / 1024 + " " + getString(R.string._unit_kilobytes_per_second);
        }
        if (AesPrefs.getLongRes(R.string.MAX_TX, 0L) > Si.MEGA) {
            float f = (float) AesPrefs.getLongRes(R.string.MAX_TX, 0L) / (float) Si.MEGA;
            maxTx = df.format(f) + " " + getString(R.string._unit_megabytes_per_second);
        } else {
            maxTx = AesPrefs.getLongRes(R.string.MAX_TX, 0L) / 1024 + " " + getString(R.string._unit_kilobytes_per_second);
        }

        final int finalImageResourceId = imageResourceId;
        final String finalMaxRx = maxRx;
        final String finalMaxTx = maxTx;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mNotificationBuilder != null) {
                    mNotificationBuilder.setSmallIcon(finalImageResourceId);
                    mNotificationBuilder.setContentTitle(down + "  |  " + up);
                    mNotificationBuilder.setContentText("Max: " + finalMaxRx + " | " + finalMaxTx
                            + "\tClips: " + mDb.getClipDataCount());
                    mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                } else {
                    String channelId = getString(R.string.channel_id_network_notification);
                    mNotificationBuilder = new NotificationCompat.Builder(MainService.this, channelId);
                }
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

        public void onPrimaryClipChanged() {
            if (!AesPrefs.getBooleanRes(R.string.CLIPBOARD_ENABLED, true)) {
                return;
            }
            try {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    ClipData cd = clipboardManager.getPrimaryClip();
                    if (cd != null) {
                        ClipData.Item item = cd.getItemAt(0);
                        String content = item.getText().toString();
                        ClipDataAdvanced.Type type = getTypeByContent(content);

                        Log.d(TAG, "onPrimaryClipChanged: content=" + content + " type=" + type.name());

                        mDb.insertClipboardText(type, content, System.currentTimeMillis());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "onPrimaryClipChanged: Error while getting clip-data", e);
            }
        }

        private ClipDataAdvanced.Type getTypeByContent(String content) {
            // TODO: 2019-04-22 add types
            // most specific on top
            if (content.toLowerCase().contains("google") && content.toLowerCase().contains("drive")) {
                return ClipDataAdvanced.Type.GOOGLE_DRIVE;
            }
            if (content.toLowerCase().contains("dropbox")) {
                return ClipDataAdvanced.Type.DROPBOX;
            }
            if (content.toLowerCase().contains("facebook")) {
                return ClipDataAdvanced.Type.FACEBOOK;
            }
            if (content.toLowerCase().contains("instagram")) {
                return ClipDataAdvanced.Type.INSTAGRAM;
            }
            if (content.toLowerCase().contains("amazon") || content.toLowerCase().contains("amzn")) {
                return ClipDataAdvanced.Type.AMAZON;
            }
            if (content.toLowerCase().contains("google") && content.toLowerCase().contains("play")) {
                return ClipDataAdvanced.Type.GOOGLE_PLAY;
            }
            if (content.toLowerCase().contains("xing")) {
                return ClipDataAdvanced.Type.XING;
            }
            if (content.toLowerCase().contains("youtube") || content.toLowerCase().contains("youtu")) {
                return ClipDataAdvanced.Type.YOUTUBE;
            }
            if (content.toLowerCase().contains("spotify")) {
                return ClipDataAdvanced.Type.SPOTIFY;
            }
            if (content.contains("http")) {
                return ClipDataAdvanced.Type.URL;
            }
            return ClipDataAdvanced.Type.DEFAULT;
        }
    }
}
