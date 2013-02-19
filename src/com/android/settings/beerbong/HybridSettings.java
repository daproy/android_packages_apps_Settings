package com.android.settings.beerbong;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class HybridSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    // private static final String PREF_UI_MODE = "ui_mode";

    // private PreferenceScreen mDpiScreen;
    // private ListPreference mUimode;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Utils.setContext(mContext);
//        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.hybrid_settings);

        // mDpiScreen = (PreferenceScreen) findPreference("system_dpi");
        // updateDensityTextSummary();
        //
        // mUimode = (ListPreference) findPreference(PREF_UI_MODE);
        //
        // int uiMode =
        // Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
        // Settings.System.UI_MODE, 0);
        // mUimode.setValue(String.valueOf(uiMode));
        // mUimode.setSummary(mUimode.getEntry());
        // mUimode.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // updateDensityTextSummary();
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        // if (PREF_UI_MODE.equals(key)) {
        // int uiMode = Integer.valueOf((String) objValue);
        // int index = mUimode.findIndexOfValue((String) objValue);
        // Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
        // Settings.System.UI_MODE,
        // uiMode);
        // mUimode.setSummary(mUimode.getEntries()[index]);
        // Utils.reboot();
        // }

        return true;
    }

    // private void updateDensityTextSummary() {
    // String prop = Utils.getProperty("qemu.sf.lcd_density");
    // if (prop == null)
    // prop = Utils.getProperty("ro.sf.lcd_density");
    // mDpiScreen.setSummary(getResources().getString(R.string.system_dpi_summary)
    // + " " + prop);
    // }
}