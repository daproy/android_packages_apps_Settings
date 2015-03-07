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
package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.Spannable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.temasek.SeekBarPreference;

import java.util.Locale;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "StatusBarSettings";

    private static final String KEY_STATUS_BAR_CLOCK = "clock_style_pref";

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String KEY_CARRIERLABEL_PREFERENCE = "carrier_options";
    private static final String KEY_STATUS_BAR_NETWORK_ARROWS= "status_bar_show_network_activity";
    private static final String KEY_STATUS_BAR_GREETING = "status_bar_greeting";
    private static final String KEY_STATUS_BAR_GREETING_TIMEOUT = "status_bar_greeting_timeout";

    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;

    private PreferenceScreen mClockStyle;
    private PreferenceScreen mCarrierLabel;
    private SwitchPreference mNetworkArrows;
    private SwitchPreference mStatusBarGreeting;
    private SeekBarPreference mStatusBarGreetingTimeout;

    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarBatteryShowPercent;

    private String mCustomGreetingText = "";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.status_bar_settings);

        PreferenceCategory mCategory = (PreferenceCategory) findPreference("status_bar");
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mClockStyle = (PreferenceScreen) prefSet.findPreference(KEY_STATUS_BAR_CLOCK);

        mCarrierLabel = (PreferenceScreen) prefSet.findPreference(KEY_CARRIERLABEL_PREFERENCE);
        if (Utils.isWifiOnly(getActivity()) || 
        TelephonyManager.getDefault().isMultiSimEnabled()) {
            mCategory.removePreference(mCarrierLabel);
        }

        // Network arrows
        mNetworkArrows = (SwitchPreference) prefSet.findPreference(KEY_STATUS_BAR_NETWORK_ARROWS);
        mNetworkArrows.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
            Settings.System.STATUS_BAR_SHOW_NETWORK_ACTIVITY, 1) == 1);
        mNetworkArrows.setOnPreferenceChangeListener(this);
        int networkArrows = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_NETWORK_ACTIVITY, 1);
        updateNetworkArrowsSummary(networkArrows);

        updateClockStyleDescription();

        // Greeting
        mStatusBarGreeting = (SwitchPreference) prefSet.findPreference(KEY_STATUS_BAR_GREETING);
        mCustomGreetingText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_GREETING);
        boolean greeting = mCustomGreetingText != null && !TextUtils.isEmpty(mCustomGreetingText);
        mStatusBarGreeting.setChecked(greeting);

        mStatusBarGreetingTimeout =
                (SeekBarPreference) prefSet.findPreference(KEY_STATUS_BAR_GREETING_TIMEOUT);
        int statusBarGreetingTimeout = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_GREETING_TIMEOUT, 400);
        mStatusBarGreetingTimeout.setValue(statusBarGreetingTimeout / 1);
        mStatusBarGreetingTimeout.setOnPreferenceChangeListener(this);

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryShowPercent =
                (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        int batteryStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int batteryShowPercent = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        enableStatusBarBatteryDependents(batteryStyle);
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);

        if (TelephonyManager.getDefault().getPhoneCount() <= 1) {
            removePreference(Settings.System.STATUS_BAR_MSIM_SHOW_EMPTY_ICONS);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) objValue);
            int index = mStatusBarBattery.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);

            enableStatusBarBatteryDependents(batteryStyle);
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            int batteryShowPercent = Integer.valueOf((String) objValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
            mStatusBarBatteryShowPercent.setSummary(
                    mStatusBarBatteryShowPercent.getEntries()[index]);
            return true;
        } else if (preference == mNetworkArrows) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_NETWORK_ACTIVITY,
                    (Boolean) objValue ? 1 : 0);
            int networkArrows = Settings.System.getInt(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_NETWORK_ACTIVITY, 1);
            updateNetworkArrowsSummary(networkArrows);
            return true;
        } else if (preference == mStatusBarGreetingTimeout) {
            int timeout = (Integer) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_GREETING_TIMEOUT, timeout * 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
       if (preference == mStatusBarGreeting) {
           boolean enabled = mStatusBarGreeting.isChecked();
           if (enabled) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle(R.string.status_bar_greeting_title);
                alert.setMessage(R.string.status_bar_greeting_dialog);

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                input.setText(mCustomGreetingText != null ? mCustomGreetingText :
                        getResources().getString(R.string.status_bar_greeting_main));
                alert.setView(input);
                alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = ((Spannable) input.getText()).toString();
                        Settings.System.putString(getActivity().getContentResolver(),
                                Settings.System.STATUS_BAR_GREETING, value);
                        updateCheckState(value);
                    }
                });
                alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
            } else {
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_GREETING, "");
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateCheckState(String value) {
        if (value == null || TextUtils.isEmpty(value)) mStatusBarGreeting.setChecked(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockStyleDescription();
    }

    private void updateClockStyleDescription() {
        if (mClockStyle == null) {
            return;
        }
        if (Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
            mClockStyle.setSummary(getString(R.string.enabled));
        } else {
            mClockStyle.setSummary(getString(R.string.disabled));
        }
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN ||
                batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT) {
            mStatusBarBatteryShowPercent.setEnabled(false);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
        }
    }

    private void updateNetworkArrowsSummary(int value) {
        String summary = value != 0
                ? getResources().getString(R.string.enabled)
                : getResources().getString(R.string.disabled);
        mNetworkArrows.setSummary(summary);
    }
}
