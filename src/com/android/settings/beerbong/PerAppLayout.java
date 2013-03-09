package com.android.settings.beerbong;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.ExtendedPropertiesUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PerAppLayout extends SettingsPreferenceFragment {

    private PreferenceCategory mAppList;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        Utils.setContext(mContext);

        addPreferencesFromResource(R.xml.per_app_layout_list);

        final PreferenceScreen prefSet = getPreferenceScreen();

        mAppList = (PreferenceCategory) prefSet
                .findPreference("per_app_layout_list");

        Applications.BeerbongAppInfo[] items = Applications
                .getApplicationList(mContext);

        mAppList.removeAll();

        for (int i = 0; i < items.length; i++) {
            
            if (items[i].pack.equals("com.android.systemui")) {
                continue;
            }
            
            Preference pref = new Preference(mContext);
            Applications.BeerbongAppInfo bAppInfo = items[i];

            pref.setKey(bAppInfo.pack);
            pref.setTitle(bAppInfo.name);
            pref.setIcon(bAppInfo.icon);
            pref.setLayoutResource(com.android.internal.R.layout.preference);

            int currentLayout = ExtendedPropertiesUtils
                    .getActualProperty(bAppInfo.pack + ".layout");

            pref.setSummary(currentLayout + "dp");

            ApplicationInfo appInfo = ExtendedPropertiesUtils
                    .getAppInfoFromPackageName(bAppInfo.pack);
            final String[] layouts = getLayouts(
            /*
             * ExtendedPropertiesUtils.getActualProperty(ExtendedPropertiesUtils.
             * BEERBONG_PREFIX + "rom_default_layout"),
             */appInfo.sourceDir);

            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(final Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            mContext);
                    builder.setTitle(R.string.per_app_layout_alert_title);
                    builder.setItems(layouts,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int item) {
                                    Applications.addApplicationLayout(mContext,
                                            preference.getKey(),
                                            Integer.parseInt(layouts[item]));
                                    preference.setSummary(layouts[item] + "dp");
                                }
                            });
                    builder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return false;
                }
            });
            mAppList.addPreference(pref);
        }
    }

    private String[] getLayouts(String apkPath) {
        try {
            ZipFile zip = new ZipFile(new File(apkPath));
            List<String> foundLayouts = new ArrayList<String>();
            foundLayouts.add("360");
            try {
                Enumeration<ZipEntry> zipFileEntries = (Enumeration<ZipEntry>) zip
                        .entries();
                while (zipFileEntries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                    String currentEntry = entry.getName();
                    int index = -1;
                    if (currentEntry.startsWith("res/layout")
                            && (index = currentEntry.indexOf("w")) >= 0) {
                        while (index >= 0) {
                            int i = index + 1;
                            String layout = "";
                            while (i < currentEntry.length()
                                    && Character
                                            .isDigit(currentEntry.charAt(i))) {
                                layout += currentEntry.charAt(i);
                                i++;
                            }
                            if (!"".equals(layout)
                                    && foundLayouts.indexOf(layout) < 0) {
                                foundLayouts.add(layout);
                            }
                            currentEntry = currentEntry.substring(index + 1);
                            index = currentEntry.indexOf("w");
                        }
                    }
                }
            } finally {
                zip.close();
            }
            return foundLayouts.toArray(new String[foundLayouts.size()]);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
