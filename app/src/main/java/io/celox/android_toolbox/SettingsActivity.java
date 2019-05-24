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

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.pepperonas.aespreferences.AesPrefs;
import com.pepperonas.andbasx.base.ToastUtils;

import java.util.Objects;

import io.celox.android_toolbox.dialogs.DialogAbout;
import io.celox.android_toolbox.dialogs.DialogDecryptDatabase;
import io.celox.android_toolbox.dialogs.DialogNote;
import io.celox.android_toolbox.dialogs.DialogSetPassword;
import io.celox.android_toolbox.utils.Database;
import io.celox.android_toolbox.utils.Utils;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Database mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mDb = new Database(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public Database getDatabase() {
        return mDb;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements
            Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

        private static final String TAG = "SettingsFragment";

        private long mLastClickedAbout;
        private long mLastClickedBuild;
        private int mHiddenCounterAbout;
        private int mHiddenCounterBuild;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            PreferenceCategory preferenceCategory = findPreference(getString(R.string.P_CAT_MAIN));

            Objects.requireNonNull(findPreference(getString(R.string.P_RESET_MAX_VALUES)))
                    .setOnPreferenceClickListener(this);

            CheckBoxPreference cbxP = findPreference(getString(R.string.CBX_AUTOSTART_ENABLED));
            if (cbxP != null) {
                cbxP.setChecked(AesPrefs.getBooleanRes(R.string.AUTOSTART_ENABLED, true));
                cbxP.setOnPreferenceChangeListener(this);
            }

            cbxP = findPreference(getString(R.string.CBX_REMOTE_VIEWS_ENABLED));
            if (cbxP != null) {
                if (preferenceCategory != null) {
                    preferenceCategory.removePreference(cbxP);
                }
                cbxP.setChecked(AesPrefs.getBooleanRes(R.string.REMOTE_VIEWS_ENABLED, false));
                cbxP.setOnPreferenceChangeListener(this);
            }

            cbxP = findPreference(getString(R.string.CBX_CLIPBOARD_ENABLED));
            if (cbxP != null) {
                cbxP.setChecked(AesPrefs.getBooleanRes(R.string.CLIPBOARD_ENABLED, true));
                cbxP.setOnPreferenceChangeListener(this);
            }

            cbxP = findPreference(getString(R.string.CBX_ENCRYPT_CLIPBOARD));
            if (cbxP != null) {
                cbxP.setChecked(!AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, "").equals(""));
                cbxP.setOnPreferenceClickListener(this);
            }

            Preference p = findPreference(getString(R.string.P_ABOUT));
            Objects.requireNonNull(p).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    processHitOnAbout();
                    return false;
                }
            });

            p = findPreference(getString(R.string.P_BUILD_VERSION));
            String buildVersion = Utils.getBuildVersion();
            Objects.requireNonNull(p).setSummary(buildVersion);
            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    processHitOnBuild();
                    return false;
                }
            });
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals(getString(R.string.CBX_AUTOSTART_ENABLED))) {
                AesPrefs.putBooleanRes(R.string.AUTOSTART_ENABLED, (Boolean) newValue);
                return true;
            }
            if (preference.getKey().equals(getString(R.string.CBX_REMOTE_VIEWS_ENABLED))) {
                AesPrefs.putBooleanRes(R.string.REMOTE_VIEWS_ENABLED, (Boolean) newValue);
                return true;
            }
            if (preference.getKey().equals(getString(R.string.CBX_CLIPBOARD_ENABLED))) {
                AesPrefs.putBooleanRes(R.string.CLIPBOARD_ENABLED, (Boolean) newValue);
                return true;
            }
            if (preference.getKey().equals(getString(R.string.LIST_NOTIFICATION_IMPORTANCE))) {
                AesPrefs.putIntRes(R.string.NOTIFICATION_IMPORTANCE, (Integer) newValue);
            }

            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(getString(R.string.CBX_ENCRYPT_CLIPBOARD))) {
                CheckBoxPreference cbxP = findPreference(getString(R.string.CBX_ENCRYPT_CLIPBOARD));
                onClickEncryptDatabase(cbxP);
                return true;
            } else if (preference.getKey().equals(getString(R.string.P_RESET_MAX_VALUES))) {
                AesPrefs.putLongRes(R.string.MAX_RX, 0L);
                AesPrefs.putLongRes(R.string.MAX_TX, 0L);
            }
            return false;
        }

        private void onClickEncryptDatabase(CheckBoxPreference cbxP) {
            SettingsActivity settingsActivity = (SettingsActivity) getActivity();
            if (settingsActivity != null) {
                if (cbxP.isChecked()) {
                    if (AesPrefs.getRes(R.string.CBX_ENCRYPT_CLIPBOARD, "").equals("")) {
                        new DialogSetPassword(getContext(), cbxP, settingsActivity.getDatabase());
                    }
                } else {
                    new DialogDecryptDatabase(getActivity(), cbxP, settingsActivity.getDatabase());
                }
            }

        }

        private void processHitOnAbout() {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mHiddenCounterAbout < 2) {
                        new DialogAbout(Objects.requireNonNull(getActivity()));
                    }
                }
            }, 1000);
            if ((mLastClickedAbout + 1000) > System.currentTimeMillis() || mLastClickedAbout == 0) {
                mHiddenCounterAbout++;
            } else {
                //                mHiddenCounterAbout = 0;
            }
            if (mHiddenCounterAbout == 7) {
                new DialogNote(getActivity());
            }
            mHiddenCounterAbout = mHiddenCounterAbout > 7 ? 0 : mHiddenCounterAbout;
            mLastClickedAbout = System.currentTimeMillis();
        }

        private void processHitOnBuild() {
            if ((mLastClickedBuild + 1000) > System.currentTimeMillis() || mLastClickedBuild == 0) {
                mHiddenCounterBuild++;
            } else {
                mHiddenCounterBuild = 0;
            }
            if (mHiddenCounterBuild == 7) {
                // toggle
                AesPrefs.putBooleanRes(R.string.MADE_WITH_LOVE,
                        !AesPrefs.getBooleanRes(R.string.MADE_WITH_LOVE, false));
                if (AesPrefs.getBooleanRes(R.string.MADE_WITH_LOVE, false)) {
                    ToastUtils.toastLong(R.string.made_with_love);
                } else {
                    ToastUtils.toastLong(R.string.reset_made_with_love);
                }
            }
            mHiddenCounterBuild = mHiddenCounterBuild > 7 ? 0 : mHiddenCounterBuild;
            mLastClickedBuild = System.currentTimeMillis();
        }

    }

}