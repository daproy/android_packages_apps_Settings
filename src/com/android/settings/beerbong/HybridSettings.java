package com.android.settings.beerbong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.ExtendedPropertiesUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class HybridSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private PreferenceScreen mDpiScreen;
    private ListPreference mUimode;
    private Preference mNavbarHeight;
    private CheckBoxPreference mAutoBackup;
    private Preference mBackup;
    private Preference mRestore;

    private Context mContext;

    private int mNavbarHeightProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Utils.setContext(mContext);

        addPreferencesFromResource(R.xml.hybrid_settings);

        mDpiScreen = (PreferenceScreen) findPreference("system_dpi");

        mUimode = (ListPreference) findPreference("ui_mode");

        int prop = ExtendedPropertiesUtils
                .getActualProperty("com.android.systemui.layout");
        mUimode.setValue(String.valueOf(prop));
        mUimode.setSummary(mUimode.getEntry());
        mUimode.setOnPreferenceChangeListener(this);

        mNavbarHeight = findPreference("navbar_height");

        mAutoBackup = (CheckBoxPreference) findPreference("dpi_groups_auto_backup");
        mBackup = findPreference("dpi_groups_backup");
        mRestore = findPreference("dpi_groups_restore");

        boolean isAutoBackup = mContext.getSharedPreferences(Applications.PREFS_NAME, 0)
                .getBoolean(Applications.PROPERTY_AUTO_BACKUP, false);

        mAutoBackup.setChecked(isAutoBackup);

        mRestore.setEnabled(Applications.backupExists());

        updateSummaries();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mNavbarHeight) {
            showNavbarHeightDialog();
        } else if (preference == mBackup) {
            Applications.backup(mContext);
        } else if (preference == mRestore) {
            Applications.restore(mContext);
        } else if (preference == mAutoBackup) {
            SharedPreferences settings = mContext.getSharedPreferences(
                    Applications.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Applications.PROPERTY_AUTO_BACKUP,
                    ((CheckBoxPreference) preference).isChecked());
            editor.commit();
        }
        updateSummaries();
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if ("ui_mode".equals(key)) {
            String layout = (String) objValue;
            Applications.addSystemLayout(mContext, layout);
        }

        updateSummaries();
        return true;
    }

    private void updateSummaries() {
        int dpi = ExtendedPropertiesUtils
                .getActualProperty("com.android.systemui.dpi");
        mDpiScreen.setSummary(getResources().getString(
                R.string.system_dpi_summary)
                + " " + dpi);

        int layout = ExtendedPropertiesUtils
                .getActualProperty("com.android.systemui.layout");
        int index = mUimode.findIndexOfValue(String.valueOf(layout));
        mUimode.setSummary(mUimode.getEntries()[index]);

        mRestore.setEnabled(Applications.backupExists());
    }

    private void showNavbarHeightDialog() {
        Resources res = getResources();
        String cancel = res.getString(R.string.cancel);
        String ok = res.getString(R.string.ok);
        String title = res.getString(R.string.navbar_height_title);
        int savedProgress = ExtendedPropertiesUtils
                .getActualProperty("com.android.systemui.navbar.dpi") / 5;

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);
        SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);
        final TextView seektext = (TextView) alphaDialog
                .findViewById(R.id.seek_text);
        OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress,
                    boolean fromUser) {
                mNavbarHeightProgress = seekbar.getProgress() * 5;
                seektext.setText(mNavbarHeightProgress + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) {
            }
        };
        seektext.setText((savedProgress * 5) + "%");
        seekbar.setMax(20);
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
                                "com.android.systemui.navbar.dpi",
                                mNavbarHeightProgress, true);
                    }
                }).create().show();
    }
}