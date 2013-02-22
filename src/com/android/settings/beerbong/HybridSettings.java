package com.android.settings.beerbong;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.ExtendedPropertiesUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class HybridSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private PreferenceScreen mDpiScreen;
    private ListPreference mUimode;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Utils.setContext(mContext);
        // ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.hybrid_settings);

        mDpiScreen = (PreferenceScreen) findPreference("system_dpi");
        updateDensityTextSummary();

        mUimode = (ListPreference) findPreference("ui_mode");

        String prop = ExtendedPropertiesUtils.getProperty("android.layout");
        if (!"0".equals(prop)) {
            mUimode.setValue(prop);
            mUimode.setSummary(mUimode.getEntry());
        }
        mUimode.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        updateDensityTextSummary();
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if ("ui_mode".equals(key)) {
            String layout = (String) objValue;
            int index = mUimode.findIndexOfValue(layout);
            Applications.addSystemLayout(mContext, layout);
            mUimode.setSummary(mUimode.getEntries()[index]);
        }

        return true;
    }

    private void updateDensityTextSummary() {
        String prop = ExtendedPropertiesUtils.getProperty("android.dpi",
                ExtendedPropertiesUtils
                        .getProperty(ExtendedPropertiesUtils.BEERBONG_PREFIX
                                + "rom_default_dpi"));
        mDpiScreen.setSummary(getResources().getString(
                R.string.system_dpi_summary)
                + " " + prop);
    }
}