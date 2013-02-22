package com.android.settings.beerbong;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.ExtendedPropertiesUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DpiPreferences extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String DPI_PREF = "system_dpi_window";
    private static final String CUSTOM_DPI_PREF = "custom_dpi_text";

    private ListPreference mDpiWindow;
    private EditTextPreference mCustomDpi;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        Utils.setContext(mContext);

        addPreferencesFromResource(R.xml.dpi_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver cr = getActivity().getApplicationContext()
                .getContentResolver();

        String prop = ExtendedPropertiesUtils.getProperty("android.dpi",
                ExtendedPropertiesUtils
                        .getProperty(ExtendedPropertiesUtils.BEERBONG_PREFIX
                                + "rom_default_dpi"));

        mDpiWindow = (ListPreference) prefSet.findPreference(DPI_PREF);
        mDpiWindow.setValue(prop);
        mDpiWindow.setOnPreferenceChangeListener(this);

        mCustomDpi = (EditTextPreference) findPreference(CUSTOM_DPI_PREF);
        mCustomDpi.setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference == mCustomDpi) {
            String prop = ExtendedPropertiesUtils.getProperty("android.dpi",
                    ExtendedPropertiesUtils
                    .getProperty(ExtendedPropertiesUtils.BEERBONG_PREFIX
                            + "rom_default_dpi"));
            mCustomDpi.getEditText().setText(prop);
            mCustomDpi.getEditText().setSelection(prop.length());
            mCustomDpi.getDialog().findViewById(android.R.id.button1)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            int value = 213;
                            try {
                                value = Integer.parseInt(mCustomDpi
                                        .getEditText().getText().toString());
                            } catch (Throwable t) {
                            }
                            if (value < 120)
                                value = 120;
                            else if (value > 480)
                                value = 480;
                            Applications.addSystem(mContext, value);
                            mCustomDpi.getDialog().dismiss();
                        }
                    });
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDpiWindow) {
            Applications.addSystem(mContext, Integer.parseInt(newValue.toString()));
            return true;
        }
        return false;
    }
}
