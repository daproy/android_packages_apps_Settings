/*
 * Copyright (C) 2014 The Dirty Unicorns project
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

package com.android.settings.temasek.extrainfo;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.Display;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;

public class ExtraInfo {
    private static final String UNKNOWN = "unknown";
    private final Display mDisplay;
    private DisplayMetrics mDisplayMetrics;

    public String getBuildManufacturer() {         return gets(Build.MANUFACTURER); }
    public String getBuildVersionCodename() {      return gets(Build.VERSION.CODENAME); }
    public String getBuildCpuAbi() {               return gets(Build.CPU_ABI); }
    public String getBuildCpuAbi2() {              return gets(Build.CPU_ABI2); }
    public String getBuildBootloader() {           return gets(Build.BOOTLOADER); }
    public String getBuildDisplay() {              return gets(Build.DISPLAY); }

    public String getProp(String prop) {
        if (prop == null || prop.length() == 0) return UNKNOWN;
       String s = UNKNOWN;
        try { s = ExtraInfoLib.shellExec("getprop " + prop).get(0).trim(); }
        catch (IOException e) {}
        catch (SecurityException e) {}
        if (s == "[]") return UNKNOWN;
        return s;
    }

    public ExtraInfo(Context context) {
        mDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();
        mDisplayMetrics = new DisplayMetrics();
        mDisplay.getMetrics(mDisplayMetrics);
    }

    private String gets(String s) {
        return (s == null || s.length() == 0) ? UNKNOWN : s;
        }

        @SuppressWarnings("deprecation")
        public String getSystemSize() {
        StatFs stat = new StatFs("/system");
        return String.valueOf((long)stat.getBlockSize() * (long)stat.getBlockCount());
    }

        @SuppressWarnings("deprecation")
        public String getSystemSize(String scale, int decimalPlaces) {
        StatFs stat = new StatFs("/system");
        return ExtraInfoLib.round(
                        ExtraInfoLib.scaleData(
                                (double)stat.getBlockSize() * (double)stat.getBlockCount(),
                                "B", scale), decimalPlaces) + " " + scale;
    }

        @SuppressWarnings("deprecation")
        public String getDataSize() {
        StatFs stat = new StatFs("/data");
        return String.valueOf((long)stat.getBlockSize() * (long)stat.getBlockCount());
    }

        @SuppressWarnings("deprecation")
        public String getDataSize(String scale, int decimalPlaces) {
        StatFs stat = new StatFs("/data");
        return ExtraInfoLib.round(
                ExtraInfoLib.scaleData(
                    (double)stat.getBlockSize() * (double)stat.getBlockCount(),
                    "B", scale), decimalPlaces) + " " + scale;
    }

    public String getPropName() {           return getProp("ro.product.name"); }
    public String getPropHardware() {       return getProp("ro.hardware"); }
    public String getPropBootloader() {     return getProp("ro.bootloader"); }
    public String getPropCpuAbi2() {     return getProp("ro.product.cpu.abi2"); }

    private List<String> getProc(String proc) {
        List<String> list = new ArrayList<String>();
        if (proc == null || proc.length() == 0) return list;
        try { list = ExtraInfoLib.shellExec("cat /proc/" + proc); }
        catch (IOException e) {}
        catch (SecurityException e) {}
        return list;
    }
    public String getProcCpuField(String field) {
        if (field == null || field.length() == 0) return UNKNOWN;
        List<String> list = getProc("cpuinfo");
        for (String s : list) {
            String[] parts = s.split(":", 2);
            if (parts[0].trim().equals(field)) { return parts[1].trim(); }
        }
        return UNKNOWN;
    }
    public String getProcMemField(String field) {
        if (field == null || field.length() == 0) return UNKNOWN;
        List<String> list = getProc("meminfo");
        for (String s : list) {
            String[] parts = s.split(":", 2);
            if (parts[0].trim().equals(field)) { return parts[1].trim(); }
        }
        return UNKNOWN;
    }

    public List<String> getProcCpuInfo() {      return getProc("cpuinfo"); }

    public String getProcCpuDescription() {     return getProcCpuField("Processor"); }
    public String getProcCpuBogoMips() {        return getProcCpuField("BogoMIPS"); }
    public String getProcCpuFeatures() {        return getProcCpuField("Features"); }
    public String getProcCpuImplementer() {     return getProcCpuField("CPU implementer"); }
    public String getProcCpuArchitecture() {    return getProcCpuField("CPU architecture"); }
    public String getProcCpuVariant() {         return getProcCpuField("CPU variant"); }
    public String getProcCpuPart() {            return getProcCpuField("CPU part"); }
    public String getProcCpuRevision() {        return getProcCpuField("CPU revision"); }

    public List<String> getProcMemInfo() { return getProc("meminfo"); }

    public String getProcMemTotal() { return getProcMemField("MemTotal"); }
    public String getProcMemTotal(String scale) {
        if (scale.length() == 0) return UNKNOWN;
        String[] parts = getProcMemTotal().split("\\s", 2);
        double value = 0.0;
        try { value = Double.valueOf(parts[0]); }
        catch (NumberFormatException e) {}
        return String.valueOf(ExtraInfoLib.scaleData(value, parts[0], scale));
    }

    public String getProcVersion() { return getProc("version").get(0); }
    public String getProcVersionKernel() {
        String[] parts = getProc("version").get(0).split("\\s");
        return parts[2];
    }


    public String getDisplayWidthInches() {
        NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMaximumFractionDigits(2);
        return numFormat.format(mDisplay.getWidth() / mDisplayMetrics.xdpi);
    }
    public String getDisplayHeightInches() {
        NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMaximumFractionDigits(2);
        return numFormat.format(mDisplay.getHeight() / mDisplayMetrics.ydpi);
    }
    public String getDisplayDiagonalInches() {
        NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMaximumFractionDigits(2);

        return numFormat.format(
                        Math.sqrt(
                        Math.pow(mDisplay.getWidth() / mDisplayMetrics.xdpi, 2) +
                        Math.pow(mDisplay.getHeight() / mDisplayMetrics.ydpi, 2)));
    }
    public String getDisplayWidth() { return String.valueOf(mDisplay.getWidth()); }
    public String getDisplayHeight() { return String.valueOf(mDisplay.getHeight()); }
    public String getDisplayDpiX() { return String.valueOf(mDisplayMetrics.xdpi); }
    public String getDisplayDpiY() { return String.valueOf(mDisplayMetrics.ydpi); }
    public String getDisplayRefreshRate() { return String.valueOf(mDisplay.getRefreshRate()); }
    public String getDisplayLogicalDensity() { return String.valueOf(mDisplayMetrics.density); }

    public String getDisplayDpi() {
        return String.valueOf(mDisplayMetrics.densityDpi);
    }
    public String getDisplayDensity() {
            if ((int) (160 * mDisplayMetrics.density) == 120) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 121) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 122) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 123) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 124) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 125) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 126) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 127) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 128) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 129) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 130) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 131) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 132) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 133) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 134) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 135) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 136) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 137) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 138) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 139) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 140) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 141) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 142) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 143) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 144) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 145) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 146) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 147) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 148) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 149) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 150) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 151) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 152) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 153) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 154) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 155) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 156) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 157) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 158) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 159) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) == 160) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 161) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 162) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 163) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 164) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 165) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 166) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 167) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 168) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 169) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 170) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 171) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 172) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 173) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 174) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 175) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 176) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 177) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 178) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 179) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 180) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 181) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 182) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 183) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 184) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 185) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 186) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 187) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 188) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 189) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 190) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 191) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 192) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 193) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 194) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 195) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 196) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 197) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 198) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 199) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 200) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 201) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 202) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 203) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 204) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 205) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 206) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 207) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 208) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 209) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 210) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 211) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 212) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 213) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 214) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 215) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 216) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 217) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 218) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 219) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 220) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 221) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 222) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 223) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 224) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 225) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 226) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 227) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 228) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 229) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 230) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 231) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 232) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 233) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 234) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 235) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 236) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 237) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 238) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 239) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) == 240) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 241) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 242) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 243) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 244) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 245) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 246) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 247) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 248) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 249) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 250) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 251) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 252) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 253) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 254) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 255) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 256) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 257) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 258) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 259) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 260) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 261) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 262) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 263) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 264) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 265) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 266) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 267) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 268) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 269) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 270) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 271) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 272) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 273) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 274) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 275) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 276) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 277) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 278) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 279) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 280) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 281) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 282) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 283) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 284) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 285) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 286) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 287) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 288) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 289) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 290) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 291) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 292) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 293) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 294) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 295) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 296) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 297) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 298) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 299) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 300) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 301) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 302) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 303) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 304) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 305) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 306) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 307) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 308) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 309) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 310) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 311) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 312) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 313) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 314) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 315) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 316) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 317) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 318) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 319) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) == 320) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 321) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 322) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 323) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 324) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 325) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 326) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 327) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 328) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 329) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 330) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 331) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 332) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 333) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 334) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 335) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 336) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 337) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 338) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 339) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 340) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 341) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 342) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 343) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 344) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 345) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 346) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 347) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 348) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 349) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 350) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 351) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 352) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 353) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 354) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 355) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 356) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 357) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 358) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 359) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 360) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 361) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 362) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 363) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 364) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 365) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 366) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 367) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 368) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 369) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 370) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 371) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 372) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 373) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 374) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 375) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 376) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 377) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 378) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 379) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 380) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 381) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 382) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 383) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 384) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 385) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 386) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 387) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 388) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 389) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 390) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 391) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 392) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 393) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 394) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 395) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 396) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 397) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 398) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 399) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 400) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 401) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 402) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 403) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 404) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 405) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 406) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 407) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 408) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 409) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 410) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 411) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 412) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 413) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 414) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 415) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 416) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 417) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 418) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 419) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 420) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 421) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 422) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 423) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 424) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 425) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 426) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 427) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 428) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 429) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 430) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 431) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 432) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 433) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 434) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 435) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 436) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 437) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 438) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 439) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 440) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 441) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 442) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 443) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 444) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 445) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 446) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 447) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 448) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 449) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 450) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 451) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 452) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 453) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 454) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 455) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 456) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 457) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 458) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 459) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 460) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 461) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 462) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 463) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 464) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 465) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 466) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 467) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 468) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 469) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 470) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 471) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 472) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 473) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 474) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 475) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 476) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 477) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 478) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 479) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 480) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 481) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 482) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 483) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 484) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 485) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 486) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 487) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 488) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 489) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 490) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 491) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 492) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 493) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 494) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 495) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 496) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 497) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 498) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 499) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 500) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 501) return "XXHDPI";
	    if ((int) (160 * mDisplayMetrics.density) == 502) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 503) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 504) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 505) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 506) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 507) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 508) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 509) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 510) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 511) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 512) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 513) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 514) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 515) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 516) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 517) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 518) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 519) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 520) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 521) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 522) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 523) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 524) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 525) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 526) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 527) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 528) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 529) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 530) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 531) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 532) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 533) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 534) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 535) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 536) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 537) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 538) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 539) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 540) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 541) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 542) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 543) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 544) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 545) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 546) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 547) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 548) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 549) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 550) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 551) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 552) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 553) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 554) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 555) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 556) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 557) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 558) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 559) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 560) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 561) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 562) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 563) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 564) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 565) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 566) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 567) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 568) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 569) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 570) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 571) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 572) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 573) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 574) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 575) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 576) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 577) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 578) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 579) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 580) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 581) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 582) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 583) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 584) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 585) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 586) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 587) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 588) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 589) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 590) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 591) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 592) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 593) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 594) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 595) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 596) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 597) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 598) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 599) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 600) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 601) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 602) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 603) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 604) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 605) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 606) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 607) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 608) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 609) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 610) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 611) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 612) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 613) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 614) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 615) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 616) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 617) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 618) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 619) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 620) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 621) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 622) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 623) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 624) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 625) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 626) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 627) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 628) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 629) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 630) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 631) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 632) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 633) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 634) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 635) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 636) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 637) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 638) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 639) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) == 640) return "XXXHDPI";
        return UNKNOWN;
    }
}
