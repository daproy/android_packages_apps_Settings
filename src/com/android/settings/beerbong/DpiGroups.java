package com.android.settings.beerbong;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
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

    protected static final String PROPERTY_CUSTOM_DPI_LIST = "custom_dpi_groups";
    protected static final String DEFAULT_GROUPS = "120|160|213|240|320|480";

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
        mCategory = (PreferenceCategory) prefSet
                .findPreference("dpi_groups_category");
        mCustomDpi = (CustomDpiGroupPreference) prefSet
                .findPreference("customdpigroup");
        mRestoreDefault = prefSet.findPreference("dpi_groups_restore_default");

        mCustomDpi.setDpiGroups(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGroups();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mRestoreDefault) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(R.string.dpi_groups_restore_default_title);
            alert.setMessage(R.string.dpi_groups_restore_default_summary);

            alert.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            dialog.dismiss();
                            SharedPreferences settings = mContext
                                    .getSharedPreferences(
                                            Applications.PREFS_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(
                                    DpiGroups.PROPERTY_CUSTOM_DPI_LIST,
                                    DEFAULT_GROUPS);
                            editor.commit();
                            updateGroups();
                        }
                    });
            alert.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            dialog.dismiss();
                        }
                    });

            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateProperties() {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(
                    "/system/etc/beerbong/properties.conf"));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected void updateGroups() {

        updateProperties();

        mGroupsString = mContext.getSharedPreferences(Applications.PREFS_NAME,
                0).getString(PROPERTY_CUSTOM_DPI_LIST, DEFAULT_GROUPS);
        String[] groupsStringArray = mGroupsString.split("\\|");
        ArrayList<Integer> mGroupsList = new ArrayList<Integer>();
        for (String s : groupsStringArray) {
            if (s != null && !s.equals("")) {
                mGroupsList.add(Integer.parseInt(s));
            }
        }

        Map<String, Integer> hashMap = new HashMap();
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            String packageName = (String) it.next();

            String dpi = properties.getProperty(packageName);

            if (hashMap.get(dpi) == null) {
                hashMap.put(dpi, 0);
            }

            if (!"0".equals(dpi) && !Applications.isPartOfSystem(packageName)
                    && mGroupsList.indexOf(Integer.parseInt(dpi)) < 0) {
                mGroupsString += "|" + dpi;
                mGroupsList.add(Integer.parseInt(dpi));
            }
            int count = hashMap.get(dpi);
            count++;
            hashMap.put(dpi, count);
        }

        SharedPreferences settings = mContext.getSharedPreferences(Applications.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PROPERTY_CUSTOM_DPI_LIST, mGroupsString);
        editor.commit();

        mCategory.removeAll();

        Collections.sort(mGroupsList);

        for (int i = 0; i < mGroupsList.size(); i++) {

            int dpi = mGroupsList.get(i);
            int count = hashMap.get(String.valueOf(dpi)) == null ? 0 : hashMap
                    .get(String.valueOf(dpi));

            DpiGroupPreference pGroup = new DpiGroupPreference(mContext, this,
                    dpi);
            pGroup.setOrder(Preference.DEFAULT_ORDER);
            pGroup.setTitle(dpi
                    + " "
                    + mContext.getResources().getString(
                            R.string.dpi_group_title));
            pGroup.setSummary(count + " "
                    + getResources().getString(R.string.dpi_groups_apps));

            mCategory.addPreference(pGroup);
        }

    }
}
