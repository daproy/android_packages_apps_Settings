package com.android.settings.beerbong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.util.ExtendedPropertiesUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DpiPreferences extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String DPI_PREF = "system_dpi_window";
    private static final String CUSTOM_DPI_PREF = "custom_dpi_text";

    private ListPreference mDpiWindow;
    private Preference mCustomDpi;
    private Context mContext;

    private int mCustomDpiProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        Utils.setContext(mContext);

        addPreferencesFromResource(R.xml.dpi_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        String prop = ExtendedPropertiesUtils.getProperty("android.dpi",
                ExtendedPropertiesUtils
                        .getProperty(ExtendedPropertiesUtils.BEERBONG_PREFIX
                                + "rom_default_dpi"));

        mDpiWindow = (ListPreference) prefSet.findPreference(DPI_PREF);
        mDpiWindow.setValue(prop);
        mDpiWindow.setOnPreferenceChangeListener(this);

        mCustomDpi = findPreference(CUSTOM_DPI_PREF);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mCustomDpi) {
            showCustomDpiDialog();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDpiWindow) {
            Applications.addSystem(mContext,
                    Integer.parseInt(newValue.toString()));
            return true;
        }
        return false;
    }

    private void showCustomDpiDialog() {
        Resources res = getResources();
        String cancel = res.getString(R.string.cancel);
        String ok = res.getString(R.string.ok);
        String title = res.getString(R.string.system_dpi_custom_title);
        int savedProgress = ExtendedPropertiesUtils
                .getActualProperty("com.android.systemui.dpi");
        savedProgress = (savedProgress - 120) / 5;

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);
        SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);
        final TextView seektext = (TextView) alphaDialog
                .findViewById(R.id.seek_text);
        OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress,
                    boolean fromUser) {
                mCustomDpiProgress = 120 + (seekbar.getProgress() * 5);
                seektext.setText(String.valueOf(mCustomDpiProgress));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) {
            }
        };
        seektext.setText(String.valueOf(120 + (savedProgress * 5)));
        seekbar.setMax(72);
        seekbar.setProgress(savedProgress);
        seekbar.setOnSeekBarChangeListener(seekBarChangeListener);
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(alphaDialog)
                .setNegativeButton(cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // nothing
                            }
                        })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Applications.addProperty(mContext,
                                "com.android.systemui.dpi",
                                mCustomDpiProgress, true);
                    }
                }).create().show();
    }
}
