/*
 * Copyright (C) 2013 The ChameleonOS Open Source Project
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

package com.android.settings.chameleonos.labs.identicons;

import android.annotation.ChaosLab;
import android.annotation.ChaosLab.Classification;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

@ChaosLab(name="QuickStats", classification=Classification.NEW_CLASS)
public class IdenticonsSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "IdenticonsSettings";

    private static final String KEY_ENABLED = "identicons_enabled";
    private static final String KEY_STYLE = "identicons_style";
    private static final String KEY_CREATE = "identicons_create";
    private static final String KEY_REMOVE = "identicons_remove";

    private SwitchPreference mEnabledPref;
    private ImageListPreference mStylePref;

    private CharSequence mPreviousTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.identicons_prefs);

        mEnabledPref = (SwitchPreference) findPreference(KEY_ENABLED);
        mEnabledPref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.IDENTICONS_ENABLED, 0) == 1));
        mEnabledPref.setOnPreferenceChangeListener(this);

        PreferenceScreen prefSet = getPreferenceScreen();
        mStylePref = (ImageListPreference) prefSet.findPreference(KEY_STYLE);
        mStylePref.setOnPreferenceChangeListener(this);
        int style = Settings.System.getInt(getContentResolver(),
                Settings.System.IDENTICONS_STYLE, 0);
        mStylePref.setValue(String.valueOf(style));
        updateStyleSummary(style);

        Preference startServicePref = findPreference(KEY_CREATE);
        startServicePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().startService(new Intent(getActivity(), IdenticonCreationService.class));
                return true;
            }
        });

        startServicePref = findPreference(KEY_REMOVE);
        startServicePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().startService(new Intent(getActivity(), IdenticonRemovalService.class));
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        final ActionBar bar = getActivity().getActionBar();
        mPreviousTitle = bar.getTitle();
        bar.setTitle(R.string.identicons_title);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().getActionBar().setTitle(mPreviousTitle);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEnabledPref) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.IDENTICONS_ENABLED,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
            return true;
        } else if (preference == mStylePref) {
            int style = Integer.valueOf((String) newValue);
            updateStyleSummary(style);
            return true;
        }
        return false;
    }

    private void updateStyleSummary(int value) {
        mStylePref.setSummary(mStylePref.getEntries()[mStylePref.findIndexOfValue("" + value)]);
        Settings.System.putInt(getContentResolver(),
                Settings.System.IDENTICONS_STYLE, value);
    }
}
