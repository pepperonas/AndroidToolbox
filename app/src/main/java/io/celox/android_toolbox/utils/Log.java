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

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.celox.android_toolbox.R;

/**
 * @author Martin Pfeffer
 * <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 * @see <a href="https://celox.io">https://celox.io</a>
 */
public class Log {

    private static final int LOG_ID_MAIN = 0;

    private static Context sContext;

    private static long sStartTime;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Locale.getDefault());

    /**
     * The constant printWriter.
     */
    private static PrintWriter printWriter = null;

    /**
     * Init.
     */
    public static void init(Context context) {
        sContext = context;

        String ts = Utils.getReadableTimeStamp();

        if (printWriter == null) {
            sStartTime = System.currentTimeMillis();

            try {
                File dir = new File(Environment.getExternalStorageDirectory() +
                        File.separator + Const.APP_DIR +
                        File.separator + Const.LOG_DIR);
                dir.mkdirs();
                printWriter = new PrintWriter(new FileWriter(new File(dir, "log-" + ts + ".log"), true));
            } catch (Exception e) {
                android.util.Log.e(Log.class.getName(), "initExternal() -> IOException", e);
            }
        }
    }

    private static synchronized int log(int priority, String tag, String msg) {
        int res = android.util.Log.println(priority, tag, msg);

        if (sContext != null) {
            if (sContext.getResources().getBoolean(R.bool.disable_log_writer)) {
                return res;
            }
        }

        if (printWriter == null) {
            init(sContext);
        }

        try {
            printWriter.print("+");
            printWriter.print(String.format("%-10s", Utils.formatTimePeriod((int) ((System.currentTimeMillis() - sStartTime) / 1000))));
            printWriter.print(String.format("%-25s", tag));
            printWriter.print(String.format("%-20s", sdf.format(new Date(System.currentTimeMillis()))));
            printWriter.print(msg + "\r\n");
            printWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * V int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    public static int v(String tag, String msg) {
        return log(android.util.Log.VERBOSE, tag, msg);
    }

    /**
     * V int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    public static int v(String tag, String msg, Throwable tr) {
        //        android.util.Log.v(tag, msg);
        return log(android.util.Log.VERBOSE, tag,
                msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

    /**
     * D int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    public static int d(String tag, String msg) {
        //        android.util.Log.d(tag, msg);
        return log(android.util.Log.DEBUG, tag, msg);
    }

    /**
     * D int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    public static int d(String tag, String msg, Throwable tr) {
        //        android.util.Log.d(tag, msg);
        return log(android.util.Log.DEBUG, tag,
                msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

    /**
     * int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    public static int i(String tag, String msg) {
        //        android.util.Log.i(tag, msg);
        return log(android.util.Log.INFO, tag, msg);
    }

    /**
     * int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    public static int i(String tag, String msg, Throwable tr) {
        //        android.util.Log.i(tag, msg);
        return log(android.util.Log.INFO, tag,
                msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

    /**
     * W int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    public static int w(String tag, String msg) {
        //        android.util.Log.w(tag, msg);
        return log(android.util.Log.WARN, tag, msg);
    }

    /**
     * W int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    public static int w(String tag, String msg, Throwable tr) {
        //        android.util.Log.w(tag, msg);
        return log(android.util.Log.WARN, tag,
                msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

    /**
     * E int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    public static int e(String tag, String msg) {
        //        android.util.Log.e(tag, msg);
        return log(android.util.Log.ERROR, tag, msg);
    }

    /**
     * E int.
     *
     * @param tag  the tag
     * @param msg  the msg
     * @param msg2 the msg
     */
    public static void ee(String tag, String msg, String msg2) {
        try {
            String ts = Utils.getReadableTimeStamp();
            File dir = new File(Environment.getExternalStorageDirectory() +
                    File.separator + Const.APP_DIR +
                    File.separator + Const.LOG_DIR);
            dir.mkdirs();
            PrintWriter printWriter = new PrintWriter(new FileWriter(new File(dir, "crash-" + ts + ".log"), true));
            printWriter.print("-------------------------------\r\n");
            printWriter.print(ts + ":\r\n");
            printWriter.print(tag + "\r\n");
            printWriter.print(msg + "\r\n");
            printWriter.print(msg2 + "\r\n");
            printWriter.print("-------------------------------\r\n");
            printWriter.flush();
        } catch (IOException e) {
            android.util.Log.e(Log.class.getName(), "ee -> IOException", e);
        }
    }

    /**
     * E int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    public static int e(String tag, String msg, Throwable tr) {
        //        android.util.Log.e(tag, msg);
        return log(android.util.Log.ERROR, tag,
                msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

    /**
     * Wtf int.
     *
     * @param tag the tag
     * @param msg the msg
     * @return the int
     */
    public static int wtf(String tag, String msg) {
        //        android.util.Log.wtf(tag, msg);
        return log(LOG_ID_MAIN, tag, msg);
    }

    /**
     * Wtf int.
     *
     * @param tag the tag
     * @param msg the msg
     * @param tr  the tr
     * @return the int
     */
    public static int wtf(String tag, String msg, Throwable tr) {
        //        android.util.Log.wtf(tag, msg);
        return log(LOG_ID_MAIN, tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

}