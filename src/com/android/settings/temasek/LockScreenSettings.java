/*
 * Copyright (C) 2015 crDroid Android
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

package com.android.settings.temasek;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.util.Helpers;

public class LockScreenSettings extends SettingsPreferenceFragment
        implements OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockScreenSettings";

    private static final String KEY_LOCKSCREEN_WEATHER = "lockscreen_weather";

    private SwitchPreference mLockscreenWeather;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.temasek_lockscreen);

        ContentResolver resolver = getActivity().getContentResolver();

        // Lockscreen weather
        mLockscreenWeather = (SwitchPreference) findPreference(KEY_LOCKSCREEN_WEATHER);
        mLockscreenWeather.setChecked(Settings.System.getIntForUser(resolver,
            Settings.System.LOCKSCREEN_WEATHER, 0, UserHandle.USER_CURRENT) == 1);
        mLockscreenWeather.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mLockscreenWeather) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_WEATHER, value ? 0 : 1, UserHandle.USER_CURRENT);
            Helpers.restartSystemUI();
        }
        return false;
    }
}
