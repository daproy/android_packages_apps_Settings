package com.android.settings.beerbong;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.android.settings.R;

public class DpiGroupPreference extends Preference {

    private DpiGroups mDpiGroups;
    private Context mContext;

    private int mDpi = -1;

    public DpiGroupPreference(Context context, DpiGroups dpiGroups, int dpi) {
        super(context);
        mContext = context;
        mDpiGroups = dpiGroups;
        mDpi = dpi;
    }

    public int getDpi() {
        return mDpi;
    }

    protected View onCreateView(ViewGroup parent) {
        View layout = super.onCreateView(parent);

        layout.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                FragmentTransaction ft = mDpiGroups.getFragmentManager().beginTransaction();
                DpiGroupFragment fragment = new DpiGroupFragment();
                fragment.setDpi(mDpi);
                ft.addToBackStack("dpi_group");
                ft.replace(mDpiGroups.getId(), fragment);
                ft.commit();
            }
        });

        layout.setOnLongClickListener(new OnLongClickListener() {

            public boolean onLongClick(View v) {

                final int dpi = DpiGroupPreference.this.getDpi();

                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setTitle(R.string.dpi_groups_delete_title);
                alert.setMessage(R.string.dpi_groups_delete_summary);

                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();

                        String list = mContext.getSharedPreferences(DpiGroups.PREFS_NAME, 0).getString(
                                DpiGroups.PROPERTY_CUSTOM_DPI_LIST, DpiGroups.DEFAULT_GROUPS);
                        String[] groupsStringArray = list.split("\\|");
                        String groups = "";
                        for (String s : groupsStringArray) {
                            if (s != null && s != "" && Integer.parseInt(s) != dpi) {
                                groups += s + "|";
                            }
                        }

                        SharedPreferences settings = mContext.getSharedPreferences(DpiGroups.PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(DpiGroups.PROPERTY_CUSTOM_DPI_LIST, groups);
                        editor.commit();
                        mDpiGroups.updateGroups();
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();

                return true;
            }
        });

        return layout;
    }
}
