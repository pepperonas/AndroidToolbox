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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.pepperonas.aespreferences.AesPrefs;
import com.pepperonas.materialdialog.MaterialDialog;

import io.celox.android_toolbox.R;

/**
 * @author Martin Pfeffer (celox.io)
 * @see <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 */
public class DialogAbout {

    private long mDelta = 0;

    public DialogAbout(final Context context) {

        String title = context.getString(R.string.app_name);
        if (AesPrefs.getBooleanRes(R.string.MADE_WITH_LOVE, false)) {
            title = context.getString(R.string.michi) + " " + context.getString(R.string.loved_edition);
        }

        new MaterialDialog.Builder(context)
                .title(title)
                .customView(R.layout.dialog_app_info)
                .positiveText(context.getString(R.string.ok))
                .positiveColor(R.color.grey_700)
                .showListener(new MaterialDialog.ShowListener() {
                    @Override
                    public void onShow(AlertDialog dialog) {
                        super.onShow(dialog);
                        TextView tvLibInfo = dialog.findViewById(R.id.tv_lib_info);
                        tvLibInfo.setText(HtmlCompat.fromHtml(context.getString(R.string.web_presentation_info),
                                HtmlCompat.FROM_HTML_MODE_LEGACY));
                        tvLibInfo.setMovementMethod(LinkMovementMethod.getInstance());
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                                .setOnTouchListener(new OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                            mDelta = System.currentTimeMillis();
                                        }
                                        if (event.getAction() == MotionEvent.ACTION_UP) {
                                            if ((System.currentTimeMillis() - mDelta) > 5000) {
                                                new DialogAndroidId(context);
                                            }
                                            v.callOnClick();
                                        }
                                        return true;
                                    }
                                });
                    }
                }).show();
    }

}
