package com.android.settings.beerbong;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * @author beerbong
 * @version 1.0
 */

public class DpiGroups extends SettingsPreferenceFragment {

    protected static final String PREFS_NAME = "custom_dpi_groups_preference";
    protected static final String PROPERTY_CUSTOM_DPI_LIST = "custom_dpi_groups";
    protected static final String PROPERTY_AUTO_BACKUP = "auto_backup";
    protected static final String DEFAULT_GROUPS = "120|160|213|240|320|480";

    private CheckBoxPreference mAutoBackup;
    private Preference mBackup;
    private Preference mRestore;
    private PreferenceCategory mCategory;
    private CustomDpiGroupPreference mCustomDpi;
    private Preference mRestoreDefault;

    private Context mContext;

    private Properties properties;

    private String mGroupsString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        Utils.setContext(mContext);

        addPreferencesFromResource(R.xml.dpi_groups_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mAutoBackup = (CheckBoxPreference) prefSet.findPreference("dpi_groups_auto_backup");
        mBackup = prefSet.findPreference("dpi_groups_backup");
        mRestore = prefSet.findPreference("dpi_groups_restore");
        mCategory = (PreferenceCategory) prefSet.findPreference("dpi_groups_category");
        mCustomDpi = (CustomDpiGroupPreference) prefSet.findPreference("customdpigroup");
        mRestoreDefault = prefSet.findPreference("dpi_groups_restore_default");

        boolean isAutoBackup = mContext.getSharedPreferences(PREFS_NAME, 0).getBoolean(PROPERTY_AUTO_BACKUP, false);

        mAutoBackup.setChecked(isAutoBackup);

        mRestore.setEnabled(Applications.backupExists());

        mCustomDpi.setDpiGroups(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGroups();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mBackup) {
            Applications.backup(mContext);
        } else if (preference == mRestore) {
            Applications.restore(mContext);
        } else if (preference == mAutoBackup) {
            SharedPreferences settings = mContext.getSharedPreferences(DpiGroups.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(DpiGroups.PROPERTY_AUTO_BACKUP, ((CheckBoxPreference) preference).isChecked());
            editor.commit();
            updateGroups();
        } else if (preference == mRestoreDefault) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(R.string.dpi_groups_restore_default_title);
            alert.setMessage(R.string.dpi_groups_restore_default_summary);

            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    SharedPreferences settings = mContext.getSharedPreferences(DpiGroups.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(DpiGroups.PROPERTY_CUSTOM_DPI_LIST, DEFAULT_GROUPS);
                    editor.commit();
                    updateGroups();
                }
            });
            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

            alert.show();
        }
        mRestore.setEnabled(Applications.backupExists());
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateProperties() {
        try {
            properties = new Properties();
            properties.load(new FileInputStream("/system/etc/beerbong/properties.conf"));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected void updateGroups() {

        mRestore.setEnabled(Applications.backupExists());

        updateProperties();

        mGroupsString = mContext.getSharedPreferences(PREFS_NAME, 0)
                .getString(PROPERTY_CUSTOM_DPI_LIST, DEFAULT_GROUPS);
        String[] groupsStringArray = mGroupsString.split("\\|");
        ArrayList<Integer> mGroupsList = new ArrayList<Integer>();
        for (String s : groupsStringArray) {
            if (s != null && s != "") {
                mGroupsList.add(Integer.parseInt(s));
            }
        }

        Map<String, Integer> hashMap = new HashMap();
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            String packageName = (String) it.next();
            if (packageName.endsWith(".dpi")) {

                String dpi = properties.getProperty(packageName);

                if (!"0".equals(dpi) && mGroupsList.indexOf(Integer.parseInt(dpi)) < 0) {
                    Applications.removeApplication(mContext, packageName.substring(0, packageName.indexOf(".dpi")));
                } else {

                    if (hashMap.get(dpi) == null)
                        hashMap.put(dpi, 0);
                    int count = hashMap.get(dpi);
                    count++;
                    hashMap.put(dpi, count);
                }
            }
        }

        mCategory.removeAll();

        for (int i = 0; i < mGroupsList.size(); i++) {

            int dpi = mGroupsList.get(i);
            int count = hashMap.get(String.valueOf(dpi)) == null ? 0 : hashMap.get(String.valueOf(dpi));

            DpiGroupPreference pGroup = new DpiGroupPreference(mContext, this, dpi);
            pGroup.setOrder(Preference.DEFAULT_ORDER);
            pGroup.setTitle(dpi + " " + mContext.getResources().getString(R.string.dpi_group_title));
            pGroup.setSummary(count + " " + getResources().getString(R.string.dpi_groups_apps));

            mCategory.addPreference(pGroup);
        }

    }
}
