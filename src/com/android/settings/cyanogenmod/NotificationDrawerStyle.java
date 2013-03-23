/*
 * Copyright (C) 2013 JellyBeer/BeerGang Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SeekBarPreference;
import com.android.settings.cyanogenmod.colorpicker.ColorPickerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationDrawerStyle extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NotificationDrawerStyle";

    private static final String PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";
    private static final String PREF_NOTIFICATION_WALLPAPER_LANDSCAPE = "notification_wallpaper_landscape";
    private static final String PREF_NOTIFICATION_WALLPAPER_ALPHA = "notification_wallpaper_alpha";
    private static final String PREF_NOTIFICATION_ALPHA = "notification_alpha";

    private ListPreference mNotificationWallpaper;
    private ListPreference mNotificationWallpaperLandscape;
    SeekBarPreference mWallpaperAlpha;
    SeekBarPreference mNotifAlpha;

    private File customnavTemp;
    private File customnavTempLandscape;

    private static final int REQUEST_PICK_WALLPAPER = 201;
    private static final int REQUEST_PICK_WALLPAPER_LANDSCAPE = 202;
    private static final String WALLPAPER_NAME = "notification_wallpaper.jpg";
    private static final String WALLPAPER_NAME_LANDSCAPE = "notification_wallpaper_landscape.jpg";

    private ContentResolver mResolver;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolver = getContentResolver();
        mActivity = getActivity();

        addPreferencesFromResource(R.xml.notification_bg_pref);

        PreferenceScreen prefSet = getPreferenceScreen();

        customnavTemp = new File(getActivity().getFilesDir()+"/notification_wallpaper_temp.jpg");
        customnavTempLandscape = new File(getActivity().getFilesDir()+"/notification_wallpaper_temp_landscape.jpg");

        mNotificationWallpaper = (ListPreference) findPreference(PREF_NOTIFICATION_WALLPAPER);
        mNotificationWallpaper.setOnPreferenceChangeListener(this);

        mNotificationWallpaperLandscape = (ListPreference) findPreference(PREF_NOTIFICATION_WALLPAPER_LANDSCAPE);
        mNotificationWallpaperLandscape.setOnPreferenceChangeListener(this);

        float wallpaperTransparency;
        try{
            wallpaperTransparency = Settings.System.getFloat(getActivity().getContentResolver(), Settings.System.NOTIF_WALLPAPER_ALPHA);
        }catch (Exception e) {
            wallpaperTransparency = 0;
            Settings.System.putFloat(getActivity().getContentResolver(), Settings.System.NOTIF_WALLPAPER_ALPHA, 0.1f);
        }
        mWallpaperAlpha = (SeekBarPreference) findPreference(PREF_NOTIFICATION_WALLPAPER_ALPHA);
        mWallpaperAlpha.setInitValue((int) (wallpaperTransparency * 100));
        mWallpaperAlpha.setProperty(Settings.System.NOTIF_WALLPAPER_ALPHA);
        mWallpaperAlpha.setOnPreferenceChangeListener(this);

        float notifTransparency;
        try{
            notifTransparency = Settings.System.getFloat(getActivity().getContentResolver(), Settings.System.NOTIF_ALPHA);
        }catch (Exception e) {
            notifTransparency = 0;
            Settings.System.putFloat(getActivity().getContentResolver(), Settings.System.NOTIF_ALPHA, 0);
        }
        mNotifAlpha = (SeekBarPreference) findPreference(PREF_NOTIFICATION_ALPHA);
        mNotifAlpha.setInitValue((int) (notifTransparency * 100));
        mNotifAlpha.setProperty(Settings.System.NOTIF_ALPHA);
        mNotifAlpha.setOnPreferenceChangeListener(this);


        updateCustomBackgroundSummary();
    }


    @Override
    public void onResume() {
        super.onResume();
        updateCustomBackgroundSummary();
    }


    private void updateCustomBackgroundSummary() {
        int resId;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND);
        if (value == null) {
            resId = R.string.notification_background_default_wallpaper;
            mNotificationWallpaper.setValueIndex(2);
            mNotificationWallpaperLandscape.setEnabled(false);
        } else if (value.isEmpty()) {
            resId = R.string.notification_background_custom_image;
            mNotificationWallpaper.setValueIndex(1);
            mNotificationWallpaperLandscape.setEnabled(true);
        } else {
            resId = R.string.notification_background_color_fill;
            mNotificationWallpaper.setValueIndex(0);
            mNotificationWallpaperLandscape.setEnabled(false);
        }
        mNotificationWallpaper.setSummary(getResources().getString(resId));

        value = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE);
        if (value == null) {
            resId = R.string.notification_background_default_wallpaper;
            mNotificationWallpaperLandscape.setValueIndex(1);
        } else {
            resId = R.string.notification_background_custom_image;
            mNotificationWallpaperLandscape.setValueIndex(0);
        }
        mNotificationWallpaperLandscape.setSummary(getResources().getString(resId));
    }

    public void deleteWallpaper (boolean orientation) {
      File wallpaperToDelete = new File(getActivity().getFilesDir()+"/notification_wallpaper.jpg");
      File wallpaperToDeleteLandscape = new File(getActivity().getFilesDir()+"/notification_wallpaper_landscape.jpg");

      if (wallpaperToDelete.exists() && !orientation) {
         wallpaperToDelete.delete();
      }

      if (wallpaperToDeleteLandscape.exists() && orientation) {
         wallpaperToDeleteLandscape.delete();
      }

      if (orientation) {
         Settings.System.putString(getContentResolver(),
            Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE, null);
      }
    }

    public void observerResourceHelper() {
       float helper;
       float first = Settings.System.getFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, 0.1f);
        if (first < 0.9f) {
            helper = first + 0.1f;
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, helper);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, first);
        }else {
            helper = first - 0.1f;
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, helper);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, first);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
          if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_WALLPAPER) {
              FileOutputStream wallpaperStream = null;
              Settings.System.putString(getContentResolver(),
                      Settings.System.NOTIFICATION_BACKGROUND,"");
              try {
                 wallpaperStream = getActivity().getApplicationContext().openFileOutput(WALLPAPER_NAME,
                         Context.MODE_WORLD_READABLE);
                 Uri selectedImageUri = Uri.fromFile(customnavTemp);
                 Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                 bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                 wallpaperStream.close();
                 customnavTemp.delete();
               } catch (Exception e) {
                     Log.e(TAG, e.getMessage(), e);
               }
            }else if (requestCode == REQUEST_PICK_WALLPAPER_LANDSCAPE) {
              FileOutputStream wallpaperStream = null;
              Settings.System.putString(getContentResolver(),
                      Settings.System.NOTIFICATION_BACKGROUND_LANDSCAPE,"");
              try {
                 wallpaperStream = getActivity().getApplicationContext().openFileOutput(WALLPAPER_NAME_LANDSCAPE,
                         Context.MODE_WORLD_READABLE);
                 Uri selectedImageUri = Uri.fromFile(customnavTempLandscape);
                 Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                 bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                 wallpaperStream.close();
                 customnavTempLandscape.delete();
               } catch (Exception e) {
                     Log.e(TAG, e.getMessage(), e);
               }
            }
        }
        observerResourceHelper();
        updateCustomBackgroundSummary();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWallpaperAlpha) {
            float valNav = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_WALLPAPER_ALPHA, valNav / 100);
            return true;
        } else if (preference == mNotifAlpha) {
            float valNav = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NOTIF_ALPHA, valNav / 100);
            return true;
        }else if (preference == mNotificationWallpaper) {
            int indexOf = mNotificationWallpaper.findIndexOfValue(newValue.toString());
            switch (indexOf) {
            //Displays color dialog when user has chosen color fill
            case 0:
                final ColorPickerView colorView = new ColorPickerView(mActivity);
                int currentColor = Settings.System.getInt(getContentResolver(),
                        Settings.System.NOTIFICATION_BACKGROUND, -1);
                if (currentColor != -1) {
                    colorView.setColor(currentColor);
                }
                colorView.setAlphaSliderVisible(false);
                new AlertDialog.Builder(mActivity)
                .setTitle(R.string.notification_drawer_custom_background_dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_BACKGROUND, colorView.getColor());
                        updateCustomBackgroundSummary();
                        deleteWallpaper(false);
                        deleteWallpaper(true);
                        observerResourceHelper();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setView(colorView).show();
                break;
            //Launches intent for user to select an image/crop it to set as background
            case 1:
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();
                Rect rect = new Rect();
                Window window = getActivity().getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int titleBarHeight = contentViewTop - statusBarHeight;
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                boolean isPortrait = getResources()
                        .getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT;
                intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
                intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);
                intent.putExtra("outputX", isPortrait ? width : height);
                intent.putExtra("outputY", isPortrait ? height : width);
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", true);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                try {
                     customnavTemp.createNewFile();
                     customnavTemp.setWritable(true, false);
                     intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(customnavTemp));
                     startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
            //Sets background to default
            case 2:
                Settings.System.putString(getContentResolver(),
                        Settings.System.NOTIFICATION_BACKGROUND, null);
                deleteWallpaper(false);
                deleteWallpaper(true);
                observerResourceHelper();
                updateCustomBackgroundSummary();
                break;
            }
            return true;
        }else if (preference == mNotificationWallpaperLandscape) {

            int indexOf = mNotificationWallpaperLandscape.findIndexOfValue(newValue.toString());
            switch (indexOf) {
            //Launches intent for user to select an image/crop it to set as background
            case 0:
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();
                Rect rect = new Rect();
                Window window = getActivity().getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int titleBarHeight = contentViewTop - statusBarHeight;
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                boolean isPortrait = getResources()
                        .getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT;
                intent.putExtra("aspectX", isPortrait ? height - titleBarHeight : width);
                intent.putExtra("aspectY", isPortrait ? width : height - titleBarHeight);
                intent.putExtra("outputX", isPortrait ? height : width);
                intent.putExtra("outputY", isPortrait ? width : height);
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", true);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                try {
                     customnavTempLandscape.createNewFile();
                     customnavTempLandscape.setWritable(true, false);
                     intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(customnavTempLandscape));
                     startActivityForResult(intent, REQUEST_PICK_WALLPAPER_LANDSCAPE);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
            //Sets background to default
            case 1:
                deleteWallpaper(true);
                observerResourceHelper();
                updateCustomBackgroundSummary();
                break;
            }
            return true;
        }
        return false;
    }
}
