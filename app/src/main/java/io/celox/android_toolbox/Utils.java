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

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Callable;

/**
 * @author Martin Pfeffer
 * <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 * @see <a href="https://celox.io">https://celox.io</a>
 */
public class Utils {

    private static final String TAG = "Utils";

    /**
     * Run on background thread.
     *
     * @param callable the callable
     */
    public static void runOnBackgroundThread(final Callable callable) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Exception e) {
                    Log.e(TAG, "runOnBackgroundThread: ", e);
                }
            }
        });
    }

    /**
     * Run on main ui thread.
     *
     * @param callable the callable
     */
    public static void runOnMainUiThread(final Callable callable) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Exception e) {
                    Log.e(TAG, "runOnMainUiThread: ", e);
                }
            }
        });
    }

    /**
     * Run delayed.
     *
     * @param callable the callable
     * @param delay    the delay
     */
    public static void runDelayed(final Callable callable, long delay) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, delay);
    }

    public static void runThreadSafe(final Callable callable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        Log.e(TAG, "runThreadSafe: ", e);
                    }
                }
            });
        }
    }
}
