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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pepperonas.jbasx.base.Si;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {

    private static final String TAG = "MainService";

    private static final int NOTIFICATION_ID = 1;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;

    private long mTmpLastRx;
    private long mTmpLastTx;
    private long mTmpLastRxMobile;
    private long mTmpLastTxMobile;
    //    private RemoteViews mRemoteViews;

    public class MyBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String channelId = getString(R.string.channel_id_network_notification);
        String channelName = getString(R.string.channel_name_network_notification);
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(chan);

        mNotificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = mNotificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.network_notification_content_title))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        //        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_network);
        //        mRemoteViews.setImageViewResource(R.id.image, R.drawable.ic_launcher);
        //        mRemoteViews.setTextViewText(R.id.title, getString(R.string.network_notification_title));
        //        mNotificationBuilder.setContent(mRemoteViews);

        startForeground(NOTIFICATION_ID, notification);

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateUi();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);

        return START_STICKY;
    }

    public void updateUi() {
        long rx_ivl = (long) ((TrafficStats.getTotalRxBytes() - mTmpLastRx) / (float) Const.NETWORK_UPDATE_INTERVAL_SEC);
        long tx_ivl = (long) ((TrafficStats.getTotalTxBytes() - mTmpLastTx) / (float) Const.NETWORK_UPDATE_INTERVAL_SEC);
        long rxm_ivl = (long) ((TrafficStats.getMobileRxBytes() - mTmpLastRxMobile) / (float) Const.NETWORK_UPDATE_INTERVAL_SEC);
        long txm_ivl = (long) ((TrafficStats.getMobileTxBytes() - mTmpLastTxMobile) / (float) Const.NETWORK_UPDATE_INTERVAL_SEC);

        mTmpLastRx = TrafficStats.getTotalRxBytes();
        mTmpLastTx = TrafficStats.getTotalTxBytes();
        mTmpLastRxMobile = TrafficStats.getMobileRxBytes();
        mTmpLastTxMobile = TrafficStats.getMobileTxBytes();

        final String down = "Down: " + (rx_ivl / 1024) + " kB";
        final String up = "Up: " + (tx_ivl / 1024) + " kB";

        final long totalTraffic = (rx_ivl + tx_ivl);

        int imageResourceId;
        if (totalTraffic > Si.MEGA) {
            float f = totalTraffic / (float) Si.MEGA;
            String fStr = String.valueOf(f);
            imageResourceId = resolveDrawableId("mbytes__" + fStr.split("\\.")[0] + "_" + fStr.split("\\.")[1].charAt(0));
        } else if (totalTraffic != 0) {
            imageResourceId = resolveDrawableId("kbytes_" + totalTraffic / (int) Si.KILO);
        } else {
            imageResourceId = resolveDrawableId("kbytes_" + 0);
        }

        try {
            if (imageResourceId == 0) {
                Log.e(TAG, "update: imageResourceId invalid.");
                imageResourceId = resolveDrawableId("kbytes_" + 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //                mNotificationBuilder.setSmallIcon(R.drawable.ic_launcher_background);

        final Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        final int finalImageResourceId = imageResourceId;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mNotificationBuilder.setSmallIcon(finalImageResourceId);
                mNotificationBuilder.setContentTitle(down + "  |  " + up);
                mNotificationBuilder.setContentText(sdf.format(date));
                //                mRemoteViews.setTextViewText(R.id.tv_notification_circle_value, String.valueOf(totalTraffic / 1024));
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
}
