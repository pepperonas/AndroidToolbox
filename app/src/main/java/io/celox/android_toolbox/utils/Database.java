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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final String CREATED = "created";

    // clipboard
    private static final String TBL_CB_TEXT = "text";

    /**
     * Instantiates a new Database helper.
     *
     * @param context the context
     */
    public Database(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CLIPBOARD + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                TBL_CB_TEXT + " TEXT DEFAULT NULL " +
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

    public void insertClipboardText(String text) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TBL_CB_TEXT, text);
        try {
            db.insert(TABLE_CLIPBOARD, null, contentValues);
        } catch (Exception e) {
            android.util.Log.e(TAG, "insertClipboardText: " + e.getMessage());
        }
    }

    /**
     * Delete all.
     */
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CLIPBOARD + ";");
    }

}

