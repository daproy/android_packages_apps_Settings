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

public class DpiGroupFragment extends SettingsPreferenceFragment {

    private PreferenceCategory mAppList;
    private Context mContext;

    private int mDpi = -1;

    public DpiGroupFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // mDpi = getArguments().getInt("dpi");

        mContext = getActivity();

        Utils.setContext(mContext);

        addPreferencesFromResource(R.xml.dpi_group);

        PreferenceScreen prefSet = getPreferenceScreen();

        PreferenceCategory tit = (PreferenceCategory) prefSet.findPreference("dpi_group_fragment_title");
        tit.setTitle(getDpi() + " DPI");

        mAppList = (PreferenceCategory) prefSet.findPreference("app_list");

        updateList();
    }

    public int getDpi() {
        return mDpi;
    }

    public void setDpi(int dpi) {
        mDpi = dpi;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {

        Applications.BeerbongAppInfo[] items = Applications.getApplicationList(mContext, getDpi());

        mAppList.removeAll();

        for (int i = 0; i < items.length; i++) {
            Preference pref = new Preference(mContext);
            Applications.BeerbongAppInfo bAppInfo = items[i];

            pref.setKey(bAppInfo.pack);
            pref.setTitle(bAppInfo.name);
            pref.setIcon(bAppInfo.icon);
            pref.setLayoutResource(R.layout.simple_preference);

            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                public boolean onPreferenceClick(final Preference preference) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setTitle(R.string.dpi_groups_alert_remove_app);

                    String title = (String) preference.getTitle();

                    String summary = mContext.getResources().getString(R.string.dpi_groups_remove_app,
                            new Object[] { title });

                    alert.setMessage(summary);

                    alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            Applications.removeApplication(mContext, preference.getKey());
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
            mAppList.addPreference(pref);
        }
    }
}
