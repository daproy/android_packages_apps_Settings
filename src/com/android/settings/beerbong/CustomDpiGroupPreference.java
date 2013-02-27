package com.android.settings.beerbong;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.SeekBarDialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;

public class CustomDpiGroupPreference extends SeekBarDialogPreference implements SeekBar.OnSeekBarChangeListener {

    private DpiGroups mDpiGroups;
    private SeekBar mSeekBar;
    private TextView mText;
    private Context mContext;

    private ArrayList<Integer> mGroupsList;

    private int mCustomDpi = -1;

    private static final int SEEK_BAR_RANGE = 480 - 120;

    public CustomDpiGroupPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        setDialogLayoutResource(R.layout.preference_dialog_dpigroup);

        loadGroups();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(SEEK_BAR_RANGE);
        mSeekBar.setProgress(213 - 120);

        mSeekBar.setOnSeekBarChangeListener(this);

        mText = (TextView) view.findViewById(R.id.text_dpi);
        mText.setText(mContext.getResources().getString(R.string.custom_dpi) + " 213");
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        mCustomDpi = mSeekBar.getProgress() + 120;
        mText.setText(mContext.getResources().getString(R.string.custom_dpi) + " " + mCustomDpi);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            // if dpi group exists show toast and return, otherwise create group
            if (mGroupsList.indexOf(mCustomDpi) >= 0) {
                Toast.makeText(mContext, R.string.customdpigroup_group_exists, Toast.LENGTH_SHORT).show();
            } else {
                mGroupsList.add(mCustomDpi);
                Collections.sort(mGroupsList);
                saveGroups();
            }
        }
    }

    public void setDpiGroups(DpiGroups dpiGroups) {
        mDpiGroups = dpiGroups;
    }

    private void loadGroups() {
        String list = mContext.getSharedPreferences(Applications.PREFS_NAME, 0).getString(
                DpiGroups.PROPERTY_CUSTOM_DPI_LIST, DpiGroups.DEFAULT_GROUPS);
        String[] groupsStringArray = list.split("\\|");
        mGroupsList = new ArrayList<Integer>();
        for (String s : groupsStringArray) {
            if (s != null && !s.equals("")) {
                mGroupsList.add(Integer.parseInt(s));
            }
        }
    }

    private void saveGroups() {

        String groups = "";

        for (int s : mGroupsList)
            groups += s + "|";

        SharedPreferences settings = mContext.getSharedPreferences(Applications.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(DpiGroups.PROPERTY_CUSTOM_DPI_LIST, groups);
        editor.commit();

        mDpiGroups.updateGroups();
    }
}
