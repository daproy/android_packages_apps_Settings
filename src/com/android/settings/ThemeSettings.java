/*
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.os.Bundle;
import android.content.ContentResolver;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class ThemeSettings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.theme_settings);
        disablePref();
    }

    private void disablePref() {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean enabled = Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1;

        PreferenceScreen screen = getPreferenceScreen();

        if (enabled) {
            Preference pref = getPreferenceManager().findPreference("lockscreen_wallpaper_settings");
            screen.removePreference(pref);
        }
    }
}
