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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pepperonas.aespreferences.AesPrefs;
import com.pepperonas.aespreferences.Crypt;
import com.pepperonas.andbasx.AndBasx;
import com.pepperonas.andbasx.base.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import io.celox.android_toolbox.R;
import io.celox.android_toolbox.models.ClipDataAdvanced;

/**
 * @author Martin Pfeffer (celox.io)
 * @see <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 */
public class Database extends SQLiteOpenHelper {

    private static final String TAG = "Database";

    @SuppressWarnings("WeakerAccess")
    public static String DATABASE_NAME = "android-toolbox.db";

    private static final String TABLE_CLIPBOARD = "tbl_clipboard";
    private static final String ID = "id";
    private static final String TS = "ts";
    private static final String CREATED = "created";

    // clipboard
    private static final String TBL_CB_TYPE = "type";
    private static final String TBL_CB_CONTENT = "content";
    private static final String TBL_CB_IV = "iv";

    /**
     * Instantiates a new Database helper.
     *
     * @param context the context
     */
    public Database(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void wipe() {
        if (AndBasx.getContext() != null) {
            AndBasx.getContext().deleteDatabase(DATABASE_NAME);
            ToastUtils.toastShort("Database deleted");
        } else {
            Log.w(TAG, "deleteDatabase: Missing context while deleting database...");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CLIPBOARD + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TS + " INTEGER DEFAULT NULL, " +
                TBL_CB_TYPE + " INTEGER DEFAULT NULL, " +
                TBL_CB_CONTENT + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE, " +
                TBL_CB_IV + " INTEGER DEFAULT NULL, " +
                CREATED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIPBOARD);

        onCreate(db);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    public void insertClipboardText(ClipDataAdvanced.Type type, String content, long iv) {
        SQLiteDatabase db = getWritableDatabase();

        if (!AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, "").equals("")) {
            content = Crypt.encrypt(AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, ""), content, iv);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(TS, System.currentTimeMillis());
        contentValues.put(TBL_CB_TYPE, type.ordinal());
        contentValues.put(TBL_CB_CONTENT, content);
        contentValues.put(TBL_CB_IV, iv);
        try {
            db.insertWithOnConflict(TABLE_CLIPBOARD, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            android.util.Log.e(TAG, "insertClipboardText: " + e.getMessage());
        }
    }

    public List<ClipDataAdvanced> getClipData(boolean encrypt) {
        String selectQuery = "SELECT * FROM " + TABLE_CLIPBOARD + " ORDER BY " + TS + " DESC;";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        List<ClipDataAdvanced> results = new ArrayList<>();
        if (AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, "").equals("")
                || AesPrefs.getLongRes(R.string.LOGOUT_TIME, 0) < System.currentTimeMillis()
                || encrypt) {
            Log.i(TAG, "getClipData: will get unencrypted data...");
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                ClipDataAdvanced.Type type;
                if (c.getInt(2) == 0) {
                    type = ClipDataAdvanced.Type.DEFAULT;
                } else {
                    type = ClipDataAdvanced.Type.DEFAULT;
                }
                results.add(new ClipDataAdvanced(c.getLong(1), type, c.getString(3), c.getLong(4)));
            }
            c.close();
            return results;
        } else {
            Log.i(TAG, "getClipData: will get encrypted data...");
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                try {
                    ClipDataAdvanced.Type type;
                    if (c.getInt(2) == 0) {
                        type = ClipDataAdvanced.Type.DEFAULT;
                    } else {
                        type = ClipDataAdvanced.Type.DEFAULT;
                    }
                    results.add(new ClipDataAdvanced(c.getLong(1), type,
                            Crypt.decrypt(AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, ""),
                                    c.getString(3), c.getLong(4)), c.getLong(4)));
                } catch (Exception e) {
                    Log.e(TAG, "getClipData error while decrypting...", e);
                }
            }
            c.close();
            return results;
        }
    }

    /**
     * Delete all.
     */
    @SuppressWarnings("unused")
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CLIPBOARD + ";");
    }

    public int getClipDataCount() {
        String selectQuery = "SELECT COUNT(*) as count FROM " + TABLE_CLIPBOARD + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToPosition(-1);
        c.moveToNext();
        int ctr = c.getInt(0);
        c.close();
        return ctr;
    }

    public void deleteClipData(long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CLIPBOARD + " WHERE " + TS + " = '" + timestamp + "';");
    }

    public void deleteAllClips() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CLIPBOARD + ";");
    }

    public void encryptClipboard(String s) {
        List<ClipDataAdvanced> data = getClipData(true);
        for (ClipDataAdvanced cda : data) {
            encryptClipDataEntry(cda, s);
        }
    }

    public void decryptClipboard(String s) {
        List<ClipDataAdvanced> data = getClipData(false);
        for (ClipDataAdvanced cda : data) {
            decryptClipDataEntry(cda, s);
        }
    }

    private void encryptClipDataEntry(ClipDataAdvanced clipDataAdvanced, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        long iv = System.currentTimeMillis();
        try {
            String encrypted = Crypt.encrypt(password, clipDataAdvanced.getContent(), iv);
            Log.i(TAG, "encryptClipDataEntry: " + encrypted);
            ContentValues contentValues = new ContentValues();
            contentValues.put(TBL_CB_CONTENT, encrypted);
            contentValues.put(TBL_CB_IV, iv);
            db.update(TABLE_CLIPBOARD, contentValues, TS + " = " + clipDataAdvanced.getTimestamp(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decryptClipDataEntry(ClipDataAdvanced clipDataAdvanced, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        String decrypted = Crypt.decrypt(password, clipDataAdvanced.getContent(), clipDataAdvanced.getIv());
        ContentValues contentValues = new ContentValues();
        contentValues.put(TBL_CB_CONTENT, decrypted);
        contentValues.put(TBL_CB_IV, clipDataAdvanced.getIv());
        try {
            db.updateWithOnConflict(TABLE_CLIPBOARD, contentValues, TS + " = " + clipDataAdvanced.getTimestamp(),
                    null, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

