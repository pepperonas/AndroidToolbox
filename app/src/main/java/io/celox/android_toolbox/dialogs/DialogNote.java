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

package io.celox.android_toolbox.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pepperonas.materialdialog.MaterialDialog;

import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;
import io.celox.android_toolbox.R;

/**
 * @author Martin Pfeffer <a href="mailto:martin.pfeffer@kjtech.com">martin.pfeffer@kjtech.com</a>, <a
 * href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 * @see <a href="http://www.kjtech.de">www.kjtech.de</a>
 * Dialog zeigt während einer Schicht ein Eingabefeld an. In dem Eingabefeld werden Notizen des Nutzers
 * gespeichert. Erst nach Ende der Schicht wird der Inhalt zurückgesetzt. Die Daten werden NICHT an das
 * Backend übertragen.
 */
public class DialogNote {

    private static final String TAG = "DialogNote";

    public DialogNote(Activity activity) {
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title(R.string.enter_name)
                .customView(R.layout.dialog_notes)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .showListener(new MaterialDialog.ShowListener() {
                    @Override
                    public void onShow(AlertDialog dialog) {
                        super.onShow(dialog);
                        EditText editText = dialog.findViewById(R.id.et_notes);
                    }
                })
                .buttonCallback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText editText = dialog.findViewById(R.id.et_notes);

                        AsyncHttpClient client = new AsyncHttpClient();
                        RequestParams params = new RequestParams();
                        params.add("name", editText.getText().toString());
                        client.post("https://gooooo.xyz/celoncgr/android-toolbox-post-name.php", params,
                                new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        String response = new String(responseBody, StandardCharsets.UTF_8);
                                        Log.d(TAG, "onSuccess: " + response);
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers,
                                                          byte[] responseBody, Throwable error) {
                                        String response = new String(responseBody, StandardCharsets.UTF_8);
                                        Log.d(TAG, "onFailure: " + response);
                                    }
                                });

                        super.onPositive(dialog);
                    }
                })
                .build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
