package com.android.settings.hybrid;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.ExtendedPropertiesUtils;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.utils.CMDProcessor;

public class Applications {

    protected static final String PREFS_NAME = "custom_dpi_groups_preference";
    protected static final String PROPERTY_AUTO_BACKUP = "auto_backup";

    public static class AppInfo {

        public String name = "";
        public String pack = "";
        public Drawable icon;
        public ApplicationInfo info;
        public int dpi;
    }

    private static class AppComparator implements Comparator<AppInfo> {

        public int compare(AppInfo a1, AppInfo a2) {
            String name1 = a1.name == null ? "" : a1.name.toLowerCase();
            String name2 = a2.name == null ? "" : a2.name.toLowerCase();
            return name1.compareTo(name2);
        }
    }

    private static final String CONF_FILE = "/system/etc/beerbong/properties.conf";
    private static final String BACKUP = "/data/data/com.android.settings/files/properties.back";

    private static final String APPEND_CMD = "echo \"%s=%s\" >> " + CONF_FILE;
    private static final String REPLACE_CMD = "busybox sed -i \"/%s/ c %<s=%s\" " + CONF_FILE;
    private static final String PROP_EXISTS_CMD = "grep -q %s " + CONF_FILE;
    private static final String REMOUNT_CMD = "busybox mount -o %s,remount -t yaffs2 /dev/block/mtdblock1 /system";

    private static final CMDProcessor cmd = new CMDProcessor();

    private static List<AppInfo> appList = new ArrayList<AppInfo>();
    private static int mLastDpi = 0;

    public static void addApplication(Context mContext, String packageName) {
        addApplication(mContext, findAppInfo(mContext, packageName), mLastDpi);
    }

    public static void addWidget(Context mContext, String packageName) {
        addWidget(mContext, findAppInfo(mContext, packageName));
    }

    public static void addApplication(Context mContext, AppInfo app, int dpi) {
        setProperty(mContext, app.pack, app.pack + ".dpi", String.valueOf(dpi), true);
    }

    public static void addWidget(Context mContext, AppInfo app) {
        setProperty(mContext, app.pack, app.pack + ".force", "1", false);
    }

    public static void addApplicationLayout(Context mContext, String packageName, int layout) {
        addApplicationLayout(mContext, findAppInfo(mContext, packageName), layout);
    }

    public static void addApplicationLayout(Context mContext, AppInfo app, int layout) {
        setProperty(mContext, app.pack, app.pack + ".layout", String.valueOf(layout), true);
    }

    public static void addSystem(Context mContext, int dpi) {
        setProperties(mContext, "com.android.systemui", new String[] { "android.dpi",
                "com.android.systemui.dpi" }, String.valueOf(dpi), true);
    }

    public static void addSystemLayout(Context mContext, String layout) {
        if ("1000".equals(layout)) {
            setProperty(mContext, "com.android.systemui", "com.android.systemui.navbar.dpi", "100", 
                    false);
            setProperties(mContext, "com.android.systemui", new String[] { "android.layout",
                    "com.android.systemui.layout" }, layout, true);
        } else {
            setProperties(mContext, "com.android.systemui", new String[] { "android.layout",
                    "com.android.systemui.layout" }, layout, true);
        }
    }

    public static void addAppsLayout(Context mContext, String layout) {
        setProperty(mContext, "", "%user_default_layout", layout, false);
    }

    public static void addProperty(Context mContext, String property, int value, boolean restartui) {
        setProperty(mContext, "com.android.systemui", property, String.valueOf(value), restartui);
    }

    public static void removeApplication(Context mContext, String packageName) {
        setProperty(mContext, packageName, packageName + ".dpi", "0", true);
    }

    public static void removeWidget(Context mContext, String packageName) {
        setProperty(mContext, packageName, packageName + ".force", "0", true);
    }

    public static boolean isAppDpiProperty(String property) {
        return property.endsWith(".dpi")
                && !property.startsWith(ExtendedPropertiesUtils.BEERBONG_PREFIX)
                && !property.startsWith("com.android.systemui.statusbar.")
                && !property.startsWith("com.android.systemui.navbar.");
    }

    public static AppInfo[] getApplicationList(Context mContext, int dpi) {

        mLastDpi = dpi;
        return getApplicationList(mContext, "dpi", String.valueOf(dpi));
    }

    public static AppInfo[] getWidgetList(Context mContext) {
        return getApplicationList(mContext, "force", "1");
    }

    private static AppInfo[] getApplicationList(Context mContext, String property, String value) {
        Properties properties = null;

        try {
            properties = new Properties();
            properties.load(new FileInputStream(CONF_FILE));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        List<AppInfo> items = new ArrayList<AppInfo>();

        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            String packageName = (String) it.next();
            String currentdpi = properties.getProperty(packageName);
            if (packageName.endsWith("." + property) && value.equals(currentdpi)) {
                AppInfo bAppInfo = findAppInfo(mContext, packageName);

                if (bAppInfo == null) {
                    removeApplication(mContext,
                            packageName.substring(0, packageName.lastIndexOf("." + property)));
                } else {
                    items.add(bAppInfo);
                }
            }
        }

        Collections.sort(items, new AppComparator());

        return items.toArray(new AppInfo[items.size()]);
    }

    public static AppInfo[] getApplicationList(Context mContext) {

        Properties properties = null;

        try {
            properties = new Properties();
            properties.load(new FileInputStream(CONF_FILE));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        PackageManager pm = mContext.getPackageManager();

        List<ApplicationInfo> mPackageList = pm.getInstalledApplications(0);
        AppInfo[] items = new AppInfo[mPackageList == null ? 0 : mPackageList
                .size()];

        appList.clear();

        for (int i = 0; mPackageList != null && i < mPackageList.size(); i++) {
            ApplicationInfo app = mPackageList.get(i);
            items[i] = new AppInfo();
            items[i].name = (String) pm.getApplicationLabel(app);
            items[i].icon = pm.getApplicationIcon(app);
            items[i].pack = app.packageName;
            items[i].info = app;
            items[i].dpi = properties.getProperty(app.packageName) == null ? 0 : Integer
                    .parseInt(properties.getProperty(app.packageName));
            appList.add(items[i]);
        }
        Arrays.sort(items, new AppComparator());
        return items;
    }

    public static void backup(Context mContext) {
        Utils.execute(new String[] { "cd /data/data/com.android.settings", "mkdir files",
                "chmod 777 files", "cp " + CONF_FILE + " " + BACKUP, "chmod 644 " + BACKUP }, 0);
        Toast.makeText(mContext, R.string.dpi_groups_backup_done, Toast.LENGTH_SHORT).show();
    }

    public static void restore(Context mContext) {
        Utils.execute(new String[] { Utils.MOUNT_SYSTEM_RW, "cp " + BACKUP + " " + CONF_FILE,
                "chmod 644 " + CONF_FILE, Utils.MOUNT_SYSTEM_RO }, 0);
        Toast.makeText(mContext, R.string.dpi_groups_restore_done, Toast.LENGTH_SHORT).show();
    }

    public static boolean backupExists() {
        return new File(BACKUP).exists();
    }

    private static void checkAutoBackup(Context mContext) {
        boolean isAutoBackup = mContext.getSharedPreferences(PREFS_NAME, 0).getBoolean(
                PROPERTY_AUTO_BACKUP, false);
        if (isAutoBackup) {
            backup(mContext);
        }
    }

    private static boolean mount(String read_value) {
        return cmd.su.runWaitFor(String.format(REMOUNT_CMD, read_value)).success();
    }

    private static boolean propExists(String prop) {
        return cmd.su.runWaitFor(String.format(PROP_EXISTS_CMD, prop)).success();
    }

    private static AppInfo findAppInfo(Context mContext, String packageName) {
        if (packageName.endsWith(".dpi")) {
            packageName = packageName.substring(0, packageName.lastIndexOf(".dpi"));
        } else if (packageName.endsWith(".force")) {
            packageName = packageName.substring(0, packageName.lastIndexOf(".force"));
        }
        if (appList.size() == 0) {
            getApplicationList(mContext);
        }
        for (int i = 0; i < appList.size(); i++) {
            AppInfo app = appList.get(i);
            if (app.pack.equals(packageName))
                return app;
        }
        return null;
    }

    private static void setProperty(Context mContext, String packageName, String property,
            String value, boolean restartSystemUI) {
        setProperties(mContext, packageName, new String[] { property }, value, restartSystemUI);
    }

    private static void setProperties(Context mContext, String packageName, String[] properties,
            String value, boolean restartSystemUI) {

        if (!mount("rw")) {
            throw new RuntimeException("Could not remount /system rw");
        }
        try {
            for (int i = 0; i < properties.length; i++) {
                if (propExists(properties[i])) {
                    cmd.su.runWaitFor(String.format(REPLACE_CMD, properties[i], value));
                } else {
                    cmd.su.runWaitFor(String.format(APPEND_CMD, properties[i], value));
                }
            }
            if ("com.android.systemui".equals(packageName)) {
                if (restartSystemUI) {
                    Utils.restartUI(mContext);
                }
            } else {
                try {
                    IActivityManager am = ActivityManagerNative.getDefault();
                    am.forceStopPackage(packageName, UserHandle.myUserId());
                } catch (android.os.RemoteException ex) {
                    // ignore
                }
            }
            ExtendedPropertiesUtils.refreshProperties();
        } finally {
            mount("ro");
        }
        checkAutoBackup(mContext);
    }
}
