package com.android.settings.beerbong;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class WidgetListPreference extends SettingsPreferenceFragment {

    private PreferenceCategory mWidgetList;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        addPreferencesFromResource(R.xml.widget_list);

        final PreferenceScreen prefSet = getPreferenceScreen();

        mWidgetList = (PreferenceCategory) prefSet.findPreference("widget_list");

        Applications.AppInfo[] items = Applications.getApplicationList(mContext);

        mWidgetList.removeAll();

        for (int i = 0; i < items.length; i++) {
            Preference pref = new Preference(mContext);
            Applications.AppInfo bAppInfo = items[i];

            pref.setKey(bAppInfo.pack);
            pref.setTitle(bAppInfo.name);
            pref.setIcon(bAppInfo.icon);
            pref.setLayoutResource(R.layout.simple_preference);

            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(final Preference preference) {
                    Applications.addWidget(mContext, preference.getKey());
                    getActivity().getFragmentManager().popBackStackImmediate();
                    return false;
                }
            });
            mWidgetList.addPreference(pref);
        }

    }

}
