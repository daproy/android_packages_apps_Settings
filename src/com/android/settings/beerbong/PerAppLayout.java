package com.android.settings.beerbong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        addPreferencesFromResource(R.xml.per_app_layout_list);

        final PreferenceScreen prefSet = getPreferenceScreen();

        mAppList = (PreferenceCategory) prefSet
                .findPreference("per_app_layout_list");

        Applications.AppInfo[] items = Applications
                .getApplicationList(mContext);

        mAppList.removeAll();

        for (int i = 0; i < items.length; i++) {
            
//            if (items[i].pack.equals("com.android.systemui")) {
//                continue;
//            }
            
            Preference pref = new Preference(mContext);
            Applications.AppInfo bAppInfo = items[i];

            pref.setKey(bAppInfo.pack);
            pref.setTitle(bAppInfo.name);
            pref.setIcon(bAppInfo.icon);
            pref.setLayoutResource(R.layout.simple_preference2);

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

    private static final String[] DEPRECATED_CONTAINERS = {"xlarge", "xhdpi", "large", "hdpi", "normal", "mdpi", "small", "ldpi"};
    private static final String[] DEPRECATED_SIZES = {"720", "720", "600", "600", "360", "360", "360", "360"};
    private final Pattern pattern1 = Pattern.compile("^res/(.*)/");
    private final Pattern pattern2 = Pattern.compile("^[a-z]+-[sw]+(\\d+)dp-?[sw]*(\\d*)");
    
    private String[] getLayouts(String filename) {
        ArrayList<String> containers = new ArrayList<String>();
        boolean lessThan360 = false;      
        try {   
            HashSet<String> hash = new HashSet<String>();            
            ZipFile zip = new ZipFile(filename);
            Enumeration<? extends ZipEntry> zippedFiles = zip.entries();

            // Find all folders in res directory
            while (zippedFiles.hasMoreElements()) {
                Matcher matcher = pattern1.matcher(zippedFiles.nextElement().getName());
                if (matcher.find()){
                    hash.add(matcher.group(1));
                }
            }

            // Search for matches
            Iterator<String> itr = hash.iterator();
            while(itr.hasNext()) {
                String match = itr.next();
                for(int i=0; i<DEPRECATED_CONTAINERS.length; i++)
                    match = match.replaceFirst("layout-"+DEPRECATED_CONTAINERS[i], "layout-sw"+DEPRECATED_SIZES[i]+"dp");

                Matcher matcher = pattern2.matcher(match);
                if (matcher.find()) {
                    String result = matcher.group(2).equals("") ? matcher.group(1) : matcher.group(2);
                    if (Integer.parseInt(result) <= 360){
                        lessThan360 = true;
                    }
                    containers.add(result);
                }
            }
        } catch (IOException e) {
            // We're dead here, hopefully will never happen
        }

        // Add minimal default if not already present
        if (!lessThan360){
            containers.add("360");
        }
    
        // Kill duplicates and sort
        HashSet<String> hash = new HashSet<String>(containers);
        containers.clear();
        containers.addAll(hash);
        Collections.sort(containers, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return Integer.parseInt(s1) > Integer.parseInt(s2) ? 1 : -1;
            }
        });

        return containers.toArray(new String[containers.size()]);
    }
}
