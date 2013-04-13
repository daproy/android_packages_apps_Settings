package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SeekBarDialogPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.hybrid.Utils;

import com.android.settings.cyanogenmod.colorpicker.ColorPickerPreference;

public class PieControl extends SettingsPreferenceFragment
                        implements Preference.OnPreferenceChangeListener {

    private static final int DEFAULT_POSITION = 1 << 1; // this equals Position.BOTTOM.FLAG
    private static final String PIE_CONTROL = "pie_control_checkbox";
    private static final String PIE_SIZE = "pie_control_size";
    private static final String[] TRIGGER = {
        "pie_control_trigger_left",
        "pie_control_trigger_bottom",
        "pie_control_trigger_right",
        "pie_control_trigger_top"
    };
 
    private CheckBoxPreference mPieControl;
    private SeekBarDialogPreference mPieSize;
    private CheckBoxPreference[] mTrigger = new CheckBoxPreference[4];
    ColorPickerPreference mPieColor;
    ColorPickerPreference mPieSelectedColor;
    ColorPickerPreference mPieOutlineColor;

    private ContentObserver mPieTriggerObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updatePieTriggers();
            refreshSettings();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.pie_control);

        prefs = getPreferenceScreen();

        mPieControl = (CheckBoxPreference) prefs.findPreference(PIE_CONTROL);
        mPieControl.setOnPreferenceChangeListener(this);
        mPieSize = (SeekBarDialogPreference) prefs.findPreference(PIE_SIZE);
        mPieColor = (ColorPickerPreference) prefs.findPreference("pie_color");
        mPieColor.setOnPreferenceChangeListener(this);
        mPieSelectedColor = (ColorPickerPreference) prefs.findPreference("pie_selected_color");
        mPieSelectedColor.setOnPreferenceChangeListener(this);
        mPieOutlineColor = (ColorPickerPreference) prefs.findPreference("pie_outline_color");
        mPieOutlineColor.setOnPreferenceChangeListener(this);

        for (int i = 0; i < TRIGGER.length; i++) {
            mTrigger[i] = (CheckBoxPreference) prefs.findPreference(TRIGGER[i]);
            mTrigger[i].setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mPieControl) {
            boolean newState = (Boolean) newValue;

            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_CONTROLS, newState ? 1 : 0);
            propagatePieControl(newState);
        } else if (preference == mPieColor) {
           String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_COLOR, intHex);
            Utils.restartUI(getActivity());
        } else if (preference == mPieSelectedColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_SELECTED_COLOR, intHex);
            Utils.restartUI(getActivity());
        } else if (preference == mPieOutlineColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_OUTLINE_COLOR, intHex);
            Utils.restartUI(getActivity());
        } else {
            int triggerSlots = 0;
            for (int i = 0; i < mTrigger.length; i++) {
                boolean checked = preference == mTrigger[i]
                        ? (Boolean) newValue : mTrigger[i].isChecked();
                if (checked) {
                    triggerSlots |= 1 << i;
                }
            }
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_POSITIONS, triggerSlots);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPieControl.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_CONTROLS, 0) == 1);
        propagatePieControl(mPieControl.isChecked());

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.PIE_POSITIONS), true,
                mPieTriggerObserver);

        updatePieTriggers();
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mPieTriggerObserver);
    }

    private void propagatePieControl(boolean value) {
        for (int i = 0; i < mTrigger.length; i++) {
            mTrigger[i].setEnabled(value);
        }
        mPieSize.setEnabled(value);
    }

    private void updatePieTriggers() {
        int triggerSlots = Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_POSITIONS, DEFAULT_POSITION);

        for (int i = 0; i < mTrigger.length; i++) {
            if ((triggerSlots & (0x01 << i)) != 0) {
                mTrigger[i].setChecked(true);
            } else {
                mTrigger[i].setChecked(false);
            }
        }
    }

}
