package com.android.settings.aokp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.ExtendedPropertiesUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.aokp.util.ShortcutPickerHelper;
import com.android.settings.aokp.widgets.NavBarItemPreference;
import com.android.settings.aokp.widgets.SeekBarPreference;
import com.android.settings.aokp.NavRingTargets;
import com.android.settings.aokp.WidgetConfigurationFragment;

import com.android.settings.hybrid.Applications;
import com.android.settings.cyanogenmod.colorpicker.ColorPickerPreference;

public class Navbar extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, ShortcutPickerHelper.OnPickListener {

    // move these later
    private static final String PREF_MENU_UNLOCK = "pref_menu_display";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "navbar_menu_display";
    private static final String NAVIGATION_BAR_COLOR = "nav_bar_color";
    private static final String PREF_NAV_COLOR = "nav_button_color";
    private static final String PREF_NAV_GLOW_COLOR = "nav_button_glow_color";
    private static final String PREF_GLOW_TIMES = "glow_times";
    private static final String PREF_NAVBAR_QTY = "navbar_qty";
    private static final String PREF_NAVRING_AMOUNT = "pref_navring_amount";
    private static final String ENABLE_NAVRING_LONG = "enable_navring_long";
    private static final String NAVIGATION_BAR_WIDGETS = "navigation_bar_widgets";
    private static final String PREF_MENU_ARROWS = "navigation_bar_menu_arrow_keys";
    private static final String PREF_NAV_BAR_ALPHA_MODE = "nav_bar_alpha_mode";

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;

    public static final String PREFS_NAV_BAR = "navbar";
    protected Context mContext;

    private Preference mNavbarHeight;
    Preference mNavRingTargets;

    // move these later
    ColorPickerPreference mNavigationColor;
    ColorPickerPreference mNavigationBarColor;
    ColorPickerPreference mNavigationBarGlowColor;
    ListPreference mGlowTimes;
    ListPreference menuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    ListPreference mNavBarButtonQty;
    ListPreference mNavRingButtonQty;
    SeekBarPreference mButtonAlpha;
    SeekBarPreference mNavBarAlpha;
    ListPreference mAlphaMode;
    CheckBoxPreference mEnableNavringLong;
    CheckBoxPreference mMenuArrowKeysCheckBox;
    Preference mConfigureWidgets;

    private int mPendingIconIndex = -1;
    private NavBarCustomAction mPendingNavBarCustomAction = null;

    private int mNavbarHeightProgress;

    private static class NavBarCustomAction {
        String activitySettingName;
        Preference preference;
        int iconIndex = -1;
    }

    Preference mPendingPreference;
    private ShortcutPickerHelper mPicker;

    private static final String TAG = "NavBar";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        getActivity().setTitle(R.string.title_navbar);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_navbar);

        PreferenceScreen prefs = getPreferenceScreen();

        boolean isTablet = ExtendedPropertiesUtils.isTablet();

        mNavbarHeight = findPreference("navbar_height");
        mNavbarHeight.setEnabled(!isTablet);

        mPicker = new ShortcutPickerHelper(this, this);

        mNavRingTargets = findPreference("navring_settings");

        menuDisplayLocation = (ListPreference) findPreference(PREF_MENU_UNLOCK);
        menuDisplayLocation.setOnPreferenceChangeListener(this);
        menuDisplayLocation.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_LOCATION,
                0) + "");

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);
        mNavBarMenuDisplay.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_VISIBILITY,
                0) + "");

        mNavRingButtonQty = (ListPreference) findPreference(PREF_NAVRING_AMOUNT);
        mNavRingButtonQty.setOnPreferenceChangeListener(this);
        mNavRingButtonQty.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SYSTEMUI_NAVRING_AMOUNT, 1) + "");

        mNavBarButtonQty = (ListPreference) findPreference(PREF_NAVBAR_QTY);
        mNavBarButtonQty.setOnPreferenceChangeListener(this);
        mNavBarButtonQty.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3) + "");

        mEnableNavringLong = (CheckBoxPreference) findPreference("enable_navring_long");
        /*mEnableNavringLong.setChecked(Settings.System.getBoolean(getContentResolver(),
                Settings.System.SYSTEMUI_NAVRING_LONG_ENABLE, false));*/

        mPicker = new ShortcutPickerHelper(this, this);

        mNavigationColor = (ColorPickerPreference) findPreference(NAVIGATION_BAR_COLOR);
        mNavigationColor.setOnPreferenceChangeListener(this);

        mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
        mNavigationBarColor.setOnPreferenceChangeListener(this);

        mNavigationBarGlowColor = (ColorPickerPreference) findPreference(PREF_NAV_GLOW_COLOR);
        mNavigationBarGlowColor.setOnPreferenceChangeListener(this);

        mGlowTimes = (ListPreference) findPreference(PREF_GLOW_TIMES);
        mGlowTimes.setOnPreferenceChangeListener(this);

        final float defaultButtonAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                0.6f);
        mButtonAlpha = (SeekBarPreference) findPreference("button_transparency");
        mButtonAlpha.setInitValue((int) (defaultButtonAlpha * 100));
        mButtonAlpha.setOnPreferenceChangeListener(this);

        mConfigureWidgets = findPreference(NAVIGATION_BAR_WIDGETS);

        mMenuArrowKeysCheckBox = (CheckBoxPreference) findPreference(PREF_MENU_ARROWS);
        mMenuArrowKeysCheckBox.setChecked(Settings.System.getBoolean(getContentResolver(),
                Settings.System.NAVIGATION_BAR_MENU_ARROW_KEYS, true));

        mNavBarAlpha = (SeekBarPreference) findPreference("navigation_bar_alpha");
        mNavBarAlpha.setOnPreferenceChangeListener(this);

        mAlphaMode = (ListPreference) prefs.findPreference(PREF_NAV_BAR_ALPHA_MODE);
        int alphaMode = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_NAV_BAR_ALPHA_MODE, 1);
        mAlphaMode.setValue(String.valueOf(alphaMode));
        mAlphaMode.setSummary(mAlphaMode.getEntry());
        mAlphaMode.setOnPreferenceChangeListener(this);

        refreshSettings();
        setHasOptionsMenu(true);
        updateGlowTimesSummary();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.nav_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_NAV_BAR_ALPHA_MODE, 1);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_COLOR, -1);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_TINT, Integer.MIN_VALUE);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_GLOW_TINT, Integer.MIN_VALUE);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3);

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[0], "**back**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[1], "**home**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[2], "**recents**");

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[0], "**null**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[1], "**null**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[2], "**null**");

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[0], "");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[1], "");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[2], "");
                refreshSettings();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mNavbarHeight) {
            showNavbarHeightDialog();
        } else if (preference == mEnableNavringLong) {

            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG_ENABLE,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
            resetNavRingLong();
            return true;
        } else if (preference == mNavRingTargets) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NavRingTargets fragment = new NavRingTargets();
            ft.addToBackStack("config_nav_ring");
            ft.replace(this.getId(), fragment);
            ft.commit();
            return true;
        } else if (preference == mConfigureWidgets) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            WidgetConfigurationFragment fragment = new WidgetConfigurationFragment();
            ft.addToBackStack("config_widgets");
            ft.replace(this.getId(), fragment);
            ft.commit();
            return true;
        } else if (preference == mMenuArrowKeysCheckBox) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_MENU_ARROW_KEYS,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == menuDisplayLocation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_LOCATION, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavBarMenuDisplay) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_VISIBILITY, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavRingButtonQty) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_AMOUNT, val);
            resetNavRing();
            resetNavRingLong();
            refreshSettings();
            return true;
        } else if (preference == mNavBarButtonQty) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTONS_QTY, val);
            refreshSettings();
            return true;
        } else if ((preference.getKey().startsWith("navbar_action"))
                || (preference.getKey().startsWith("navbar_longpress"))) {
            boolean longpress = preference.getKey().startsWith("navbar_longpress_");
            int index = Integer.parseInt(preference.getKey().substring(
                    preference.getKey().lastIndexOf("_") + 1));

            if (newValue.equals("**app**")) {
                mPendingNavBarCustomAction = new NavBarCustomAction();
                mPendingNavBarCustomAction.preference = preference;
                if (longpress) {
                    mPendingNavBarCustomAction.activitySettingName = Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[index];
                    mPendingNavBarCustomAction.iconIndex = -1;
                } else {
                    mPendingNavBarCustomAction.activitySettingName = Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[index];
                    mPendingNavBarCustomAction.iconIndex = index;
                }
                mPicker.pickShortcut();
            } else {
                if (longpress) {
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[index],
                            (String) newValue);
                } else {
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[index],
                            (String) newValue);
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_CUSTOM_APP_ICONS[index], "");
                }
            }
            refreshSettings();
            return true;
        } else if (preference == mNavigationColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex) & 0x00FFFFFF;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_COLOR, intHex);
            return true;
        } else if (preference == mNavigationBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_TINT, intHex);
            return true;
        } else if (preference == mNavigationBarGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_TINT, intHex);
            return true;
        } else if (preference == mGlowTimes) {
            // format is (on|off) both in MS
            String value = (String) newValue;
            String[] breakIndex = value.split("\\|");
            int onTime = Integer.valueOf(breakIndex[0]);
            int offTime = Integer.valueOf(breakIndex[1]);

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[0], offTime);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[1], onTime);
            updateGlowTimesSummary();
            return true;
        } else if (preference == mButtonAlpha) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                    val * 0.01f);
            return true;
        } else if (preference == mNavBarAlpha) {
            float val = (float) (Integer.parseInt((String)newValue) * 0.01);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_ALPHA,
                    val);
            return true;
        } else if (preference == mAlphaMode) {
            int alphaMode = Integer.valueOf((String) newValue);
            int index = mAlphaMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_NAV_BAR_ALPHA_MODE, alphaMode);
            mAlphaMode.setSummary(mAlphaMode.getEntries()[index]);
            return true;
        }
        return false;
    }

    public void resetNavRing() {
            // TODO : FIXME
    }

    public void resetNavRingLong() {
            // TODO : FIXME
    }


    private void updateGlowTimesSummary() {
        int resId;
        String combinedTime = Settings.System.getString(getContentResolver(),
                Settings.System.NAVIGATION_BAR_GLOW_DURATION[1]) + "|" +
                Settings.System.getString(getContentResolver(),
                        Settings.System.NAVIGATION_BAR_GLOW_DURATION[0]);

        String[] glowArray = getResources().getStringArray(R.array.glow_times_values);

        if (glowArray[0].equals(combinedTime)) {
            resId = R.string.glow_times_off;
            mGlowTimes.setValueIndex(0);
        } else if (glowArray[1].equals(combinedTime)) {
            resId = R.string.glow_times_superquick;
            mGlowTimes.setValueIndex(1);
        } else if (glowArray[2].equals(combinedTime)) {
            resId = R.string.glow_times_quick;
            mGlowTimes.setValueIndex(2);
        } else {
            resId = R.string.glow_times_normal;
            mGlowTimes.setValueIndex(3);
        }
        mGlowTimes.setSummary(getResources().getString(resId));
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if ((requestCode == REQUEST_PICK_CUSTOM_ICON)
                    || (requestCode == REQUEST_PICK_LANDSCAPE_ICON)) {

                String iconName = getIconFileName(mPendingIconIndex);
                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput(iconName, Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                Uri selectedImageUri = getTempFileUri();
                try {
                    Log.e(TAG, "Selected image path: " + selectedImageUri.getPath());
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                } catch (NullPointerException npe) {
                    Log.e(TAG, "SeletedImageUri was null.");
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Settings.System.putString(
                        getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingIconIndex], "");
                Settings.System.putString(
                        getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingIconIndex],
                        Uri.fromFile(
                                new File(mContext.getFilesDir(), iconName)).getPath());

                File f = new File(selectedImageUri.getPath());
                if (f.exists())
                    f.delete();

                Toast.makeText(
                        getActivity(),
                        mPendingIconIndex
                                + getResources().getString(
                                        R.string.custom_app_icon_successfully),
                        Toast.LENGTH_LONG).show();
                refreshSettings();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void refreshSettings() {

        int navbarQuantity = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3);

        PreferenceGroup targetGroup = (PreferenceGroup) findPreference("navbar_buttons");
        targetGroup.removeAll();

        PackageManager pm = mContext.getPackageManager();
        Resources res = mContext.getResources();

        for (int i = 0; i < navbarQuantity; i++) {
            final int index = i;
            NavBarItemPreference pAction = new NavBarItemPreference(getActivity());
            ListPreference mLongPress = new ListPreference(getActivity());
            // NavBarItemPreference pLongpress = new
            // NavBarItemPreference(getActivity());
            String dialogTitle = String.format(
                    getResources().getString(R.string.navbar_action_title), i + 1);
            pAction.setDialogTitle(dialogTitle);
            pAction.setEntries(R.array.navbar_button_entries);
            pAction.setEntryValues(R.array.navbar_button_values);
            String title = String.format(getResources().getString(R.string.navbar_action_title),
                    i + 1);
            pAction.setTitle(title);
            pAction.setKey("navbar_action_" + i);
            pAction.setSummary(getProperSummary(i, false));
            pAction.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(pAction);

            dialogTitle = String.format(
                    getResources().getString(R.string.navbar_longpress_title), i + 1);
            mLongPress.setDialogTitle(dialogTitle);
            mLongPress.setEntries(R.array.navbar_button_entries);
            mLongPress.setEntryValues(R.array.navbar_button_values);
            title = String.format(getResources().getString(R.string.navbar_longpress_title), i + 1);
            mLongPress.setTitle(title);
            mLongPress.setKey("navbar_longpress_" + i);
            mLongPress.setSummary(getProperSummary(i, true));
            mLongPress.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(mLongPress);

            pAction.setImageListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPendingIconIndex = index;
                    int width = 100;
                    int height = width;

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                    intent.setType("image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", width);
                    intent.putExtra("aspectY", height);
                    intent.putExtra("outputX", width);
                    intent.putExtra("outputY", height);
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempFileUri());
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                    Log.i(TAG, "started for result, should output to: " + getTempFileUri());
                    startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON);
                }
            });

            String customIconUri = Settings.System.getString(getContentResolver(),
                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[i]);
            if (customIconUri != null && customIconUri.length() > 0) {
                File f = new File(Uri.parse(customIconUri).getPath());
                if (f.exists())
                    pAction.setIcon(resize(new BitmapDrawable(res, f.getAbsolutePath())));
            }

            if (customIconUri != null && !customIconUri.equals("")
                    && customIconUri.startsWith("file")) {
                // it's an icon the user chose from the gallery here
                File icon = new File(Uri.parse(customIconUri).getPath());
                if (icon.exists())
                    pAction.setIcon(resize(new BitmapDrawable(getResources(), icon
                            .getAbsolutePath())));

            } else if (customIconUri != null && !customIconUri.equals("")) {
                // here they chose another app icon
                try {
                    pAction.setIcon(resize(pm.getActivityIcon(Intent.parseUri(customIconUri, 0))));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                // ok use default icons here
                pAction.setIcon(resize(getNavbarIconImage(i, false)));
            }
        }
    }

    private Drawable resize(Drawable image) {
        int size = 50;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources()
                .getDisplayMetrics());

        Bitmap d = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, px, px, false);
        return new BitmapDrawable(mContext.getResources(), bitmapOrig);
    }

    private Drawable getNavbarIconImage(int index, boolean landscape) {
        String uri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[index]);

        if (uri == null)
            return getResources().getDrawable(R.drawable.ic_sysbar_null);

        if (uri.startsWith("**")) {
            if (uri.equals("**home**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_home);
            } else if (uri.equals("**back**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_back);
            } else if (uri.equals("**recents**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_recent);
            } else if (uri.equals("**search**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_search);
            } else if (uri.equals("**screenshot**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_screenshot);
            } else if (uri.equals("**menu**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_menu_big);
             } else if (uri.equals("**ime**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_ime_switcher);
            } else if (uri.equals("**kill**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_killtask);
            } else if (uri.equals("**power**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_power);
            } else if (uri.equals("**notifications**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_notifications);
            } else if (uri.equals("**lastapp**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_lastapp);
            }
        } else {
            try {
                return mContext.getPackageManager().getActivityIcon(Intent.parseUri(uri, 0));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return getResources().getDrawable(R.drawable.ic_sysbar_null);
    }

    private String getProperSummary(int i, boolean longpress) {
        String uri = "";
        if (longpress)
            uri = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[i]);
        else
            uri = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[i]);
        if (uri == null)
            return getResources().getString(R.string.navbar_action_none);

        if (uri.startsWith("**")) {
            if (uri.equals("**home**"))
                return getResources().getString(R.string.navbar_action_home);
            else if (uri.equals("**back**"))
                return getResources().getString(R.string.navbar_action_back);
            else if (uri.equals("**recents**"))
                return getResources().getString(R.string.navbar_action_recents);
            else if (uri.equals("**search**"))
                return getResources().getString(R.string.navbar_action_search);
            else if (uri.equals("**screenshot**"))
                return getResources().getString(R.string.navbar_action_screenshot);
            else if (uri.equals("**menu**"))
                return getResources().getString(R.string.navbar_action_menu);
            else if (uri.equals("**ime**"))
                return getResources().getString(R.string.navbar_action_ime);
            else if (uri.equals("**kill**"))
                return getResources().getString(R.string.navbar_action_kill);
            else if (uri.equals("**power**"))
                return getResources().getString(R.string.navbar_action_power);
            else if (uri.equals("**notifications**"))
                return getResources().getString(R.string.navbar_action_notifications);
            else if (uri.equals("**lastapp**"))
                return getResources().getString(R.string.navbar_action_lastapp);
            else if (uri.equals("**null**"))
                return getResources().getString(R.string.navbar_action_none);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        if (Settings.System.putString(getActivity().getContentResolver(),
                mPendingNavBarCustomAction.activitySettingName, uri)) {
            if (mPendingNavBarCustomAction.iconIndex != -1) {
                if (bmp == null) {
                    Settings.System
                            .putString(
                                    getContentResolver(),
                                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingNavBarCustomAction.iconIndex],
                                    "");
                } else {
                    String iconName = getIconFileName(mPendingNavBarCustomAction.iconIndex);
                    FileOutputStream iconStream = null;
                    try {
                        iconStream = mContext.openFileOutput(iconName, Context.MODE_WORLD_READABLE);
                    } catch (FileNotFoundException e) {
                        return; // NOOOOO
                    }
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                    Settings.System
                            .putString(
                                    getContentResolver(),
                                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingNavBarCustomAction.iconIndex], "");
                    Settings.System
                            .putString(
                                    getContentResolver(),
                                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingNavBarCustomAction.iconIndex],
                                    Uri.fromFile(mContext.getFileStreamPath(iconName)).toString());
                }
            }
            mPendingNavBarCustomAction.preference.setSummary(friendlyName);
        }
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mPendingIconIndex + ".png"));

    }

    private String getIconFileName(int index) {
        return "navbar_icon_" + index + ".png";
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mNavBarAlpha != null) {
            final float defaultNavAlpha = Settings.System.getFloat(getActivity()
                    .getContentResolver(), Settings.System.NAVIGATION_BAR_ALPHA,
                    1.0f);
            mNavBarAlpha.setInitValue(Math.round(defaultNavAlpha * 100));
        }
        refreshSettings();
    }

    public static class NavbarLayout extends ListFragment {
        private static final String TAG = "NavbarLayout";

        Context mContext;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            mContext = getActivity().getBaseContext();
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        };

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }

    private void showNavbarHeightDialog() {
        Resources res = getResources();
        String cancel = res.getString(R.string.cancel);
        String ok = res.getString(R.string.ok);
        String title = res.getString(R.string.navbar_height_title);
        int savedProgress = Integer.parseInt(ExtendedPropertiesUtils
                .getProperty("com.android.systemui.navbar.dpi")) / 5;

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);
        SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);
        final TextView seektext = (TextView) alphaDialog
                .findViewById(R.id.seek_text);
        OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress,
                    boolean fromUser) {
                mNavbarHeightProgress = seekbar.getProgress() * 5;
                seektext.setText(mNavbarHeightProgress + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) {
            }
        };
        seektext.setText((savedProgress * 5) + "%");
        seekbar.setMax(20);
        seekbar.setProgress(savedProgress);
        seekbar.setOnSeekBarChangeListener(seekBarChangeListener);
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(alphaDialog)
                .setNegativeButton(cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // nothing
                            }
                        })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Applications.addProperty(mContext,
                                "com.android.systemui.navbar.dpi",
                                mNavbarHeightProgress, true);
                    }
                }).create().show();
    }

}
