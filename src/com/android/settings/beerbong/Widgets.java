package com.android.settings.beerbong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Widgets extends SettingsPreferenceFragment {

    private PreferenceCategory mWidgetList;
    private Context mContext;

    public Widgets() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        addPreferencesFromResource(R.xml.widgets);

        PreferenceScreen prefSet = getPreferenceScreen();

        mWidgetList = (PreferenceCategory) prefSet.findPreference("widget_list");

        updateList();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {

        Applications.AppInfo[] items = Applications.getWidgetList(mContext);

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
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setTitle(R.string.widget_alert_remove_widget);

                    String title = (String) preference.getTitle();

                    String summary = mContext.getResources().getString(R.string.widget_remove_widget,
                            new Object[] { title });

                    alert.setMessage(summary);

                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            Applications.removeWidget(mContext, preference.getKey());
                            updateList();
                        }
                    });
                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });

                    alert.show();

                    return false;
                }
            });
            mWidgetList.addPreference(pref);
        }
    }
}
