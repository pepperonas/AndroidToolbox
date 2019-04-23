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
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pepperonas.aespreferences.AesPrefs;
import com.pepperonas.andbasx.base.ToastUtils;
import com.pepperonas.jbasx.base.TextUtils;
import com.pepperonas.materialdialog.MaterialDialog;

import io.celox.android_toolbox.R;
import io.celox.android_toolbox.utils.Const;
import io.celox.android_toolbox.utils.Database;

//import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Martin Pfeffer (celox.io)
 * @see <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 */
public class DialogDecryptDatabase {

    @SuppressWarnings("unused")
    private static final String TAG = "DialogDecryptDatabase";

    public DialogDecryptDatabase(@NonNull final Activity activity, final CheckBoxPreference cbxEncrypt, final Database db) {
        new MaterialDialog.Builder(activity)
                .title(activity.getString(R.string.dialog_enter_password_title))
                .message(activity.getString(R.string.dialog_enter_password_to_decrypt_msg))
                .customView(R.layout.dialog_set_password)
                .icon(new IconicsDrawable(activity, CommunityMaterial.Icon.cmd_lock_open_outline)
                        .colorRes(R.color.dialog_icon)
                        .sizeDp(Const.NAV_DRAWER_ICON_SIZE))
                .positiveText(activity.getString(R.string.ok))
                .neutralText(R.string.reset)
                .negativeText(activity.getString(R.string.cancel))
                .buttonCallback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        EditText etInput = dialog.findViewById(R.id.et_set_password);
                        if (!TextUtils.isEmpty(etInput.getText().toString())) {
                            if (etInput.getText().toString().equals(AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, ""))) {
                                ToastUtils.toastShort(R.string.clipboard_decrypted);

                                AesPrefs.putRes(R.string.ENCRYPTION_PASSWORD, "");
                                db.decryptClipboard(etInput.getText().toString());
                            } else {
                                ToastUtils.toastShort(R.string.wrong_password);
                                cbxEncrypt.setChecked(true);
                            }
                        } else {
                            ToastUtils.toastShort(R.string.invalid_input);
                            cbxEncrypt.setChecked(true);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);

                        cbxEncrypt.setChecked(true);
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);
                        ToastUtils.toastLong(R.string.encryption_disabled);

                        // TODO: 2019-04-20 show warning 'this will delete all clips'

                        db.deleteAllClips();
                        cbxEncrypt.setChecked(false);
                        AesPrefs.putRes(R.string.ENCRYPTION_PASSWORD, "");

                        //                        activity.sendBroadcast(new Intent(MainService.BROADCAST_CLIP_DELETED));
                    }
                })
                .show();
    }

}
