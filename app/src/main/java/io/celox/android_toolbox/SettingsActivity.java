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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.pepperonas.aespreferences.AesPrefs;

import io.celox.android_toolbox.dialogs.DialogDecryptDatabase;
import io.celox.android_toolbox.dialogs.DialogSetPassword;
import io.celox.android_toolbox.utils.Database;

public class SettingsActivity extends AppCompatActivity {

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

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            CheckBoxPreference cbxP = findPreference(getString(R.string.CBX_ENCRYPT_CLIPBOARD));
            if (cbxP != null) {
                cbxP.setChecked(!AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, "").equals(""));
                cbxP.setOnPreferenceClickListener(this);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(getString(R.string.CBX_ENCRYPT_CLIPBOARD))) {
                CheckBoxPreference cbxP = findPreference(getString(R.string.CBX_ENCRYPT_CLIPBOARD));
                onClickEncryptDatabase(cbxP);
                return true;
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
    }

}