/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.deskclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.android.deskclock.worldclock.Cities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

// add by junye.li for defect 2397022 begin
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.util.Locale;
// add by junye.li for defect 2397022 end

//add Russian Timezone for defect 4068759 by kangsen.chen 2017-0205 begin
import java.util.HashMap;
//add Russian Timezone for defect 4068759 by kangsen.chen 2017-0205 end
import android.content.SharedPreferences; //add by yeqing.lv for defect 4068759 at 2017-2-23
/**
 * Settings for the Alarm Clock.
 */
public class SettingsActivity extends BaseActivity {

    public static final String KEY_ALARM_SNOOZE = "snooze_duration";
    public static final String KEY_ALARM_VOLUME = "volume_setting";
    public static final String KEY_VOLUME_BEHAVIOR = "volume_button_setting";
    public static final String KEY_AUTO_SILENCE = "auto_silence";
    public static final String KEY_CLOCK_STYLE = "clock_style";
    public static final String KEY_HOME_TZ = "home_time_zone";
    public static final String KEY_AUTO_HOME_CLOCK = "automatic_home_clock";
    public static final String KEY_VOLUME_BUTTONS = "volume_button_setting";
    public static final String KEY_WEEK_START = "week_start";

    public static final String DEFAULT_VOLUME_BEHAVIOR = "0";
    public static final String VOLUME_BEHAVIOR_SNOOZE = "1";
    public static final String VOLUME_BEHAVIOR_DISMISS = "2";


    //modify by yeqing.lv for defect 4068759 at 2017-2-23 begin
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static HashMap<String, String> mRuZoneMap;
    private static String [] mRussia;
    public static int mIndx;
    private void initRussiaData() {
        mRussia = getResources().getStringArray(R.array.timezone_labels);
        mRuZoneMap = new HashMap<String, String>() {
            {
                put(mRussia[85],  "1");
                put(mRussia[45], "2");
                put(mRussia[92], "2");
                put(mRussia[93],  "2");
                put(mRussia[86],  "3");
                put(mRussia[94],  "3");
                put(mRussia[95], "3");
                put(mRussia[56], "4");
                put(mRussia[96], "4");
                put(mRussia[97],  "4");
                put(mRussia[98],  "5");
                put(mRussia[87],  "6");
                put(mRussia[62], "6");
                put(mRussia[99], "6");
                put(mRussia[66],  "7");
                put(mRussia[100],  "7");
                put(mRussia[72],  "8");
                put(mRussia[101], "8");
                put(mRussia[78], "9");
                put(mRussia[102],  "9");
                put(mRussia[88],  "10");
                put(mRussia[80],  "10");
                put(mRussia[89], "10");
                put(mRussia[90],  "11");
                put(mRussia[91],  "11");
     //modify by yeqing.lv for defect 4068759 at 2017-2-23 end
            }
        };
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //modify by yeqing.lv for defect 4068759 at 2017-2-23 begin
        sharedPreferences = getSharedPreferences("mIndex", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initRussiaData();
        //modify by yeqing.lv for defect 4068759 at 2017-2-23 end
        setVolumeControlStream(AudioManager.STREAM_ALARM);
        setContentView(R.layout.settings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        MenuItem help = menu.findItem(R.id.menu_item_help);
        if (help != null) {
            Utils.prepareHelpMenuItem(this, help);
        }
        return super.onCreateOptionsMenu(menu);
    }


    public static class PrefsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private static CharSequence[][] mTimezones;
        private long mTime;

        // add by junye.li for defect 2397022 begin
        private static Locale mLocale = null;
        private BroadcastReceiver mLocaleReceiver;
        // add by junye.li for defect 2397022 end

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            // We don't want to reconstruct the timezone list every single time onResume() is
            // called so we do it once in onCreate
            // modify by junye.li for defect 2397022 begin
            final Locale locale = getResources().getConfiguration().locale;
            if (mTimezones == null || mLocale != locale) {
                mTime = System.currentTimeMillis();
                mTimezones = getAllTimezones();
                mLocale = locale;
            }
            // modify by junye.li for defect 2397022 end
            final ListPreference homeTimezonePref = (ListPreference) findPreference(KEY_HOME_TZ);
            homeTimezonePref.setEntryValues(mTimezones[0]);
            homeTimezonePref.setEntries(mTimezones[1]);
            homeTimezonePref.setSummary(homeTimezonePref.getEntry());
            homeTimezonePref.setOnPreferenceChangeListener(this);
            // add by junye.li for defect 2397022 begin
            mLocaleReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                        Locale locale = context.getResources().getConfiguration().locale;
                        if (mLocale != locale) {
                            mLocale = locale;
                            mTime = System.currentTimeMillis();
                            mTimezones = getAllTimezones();
                            final ListPreference homeTimezonePref =
                                    (ListPreference) findPreference(KEY_HOME_TZ);
                            homeTimezonePref.setEntryValues(mTimezones[0]);
                            homeTimezonePref.setEntries(mTimezones[1]);
                            homeTimezonePref.setSummary(homeTimezonePref.getEntry());
                            homeTimezonePref.setOnPreferenceChangeListener(PrefsFragment.this);
                        }
                    }
                }
            };
            getActivity().registerReceiver(mLocaleReceiver,
                    new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
            // add by junye.li for defect 2397022 end
        }

        @Override
        public void onResume() {
            super.onResume();
            // By default, do not recreate the DeskClock activity
            getActivity().setResult(RESULT_CANCELED);
            refresh();
        }

        // add by junye.li for defect 2397022 begin
        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mLocaleReceiver != null) {
                getActivity().unregisterReceiver(mLocaleReceiver);
            }
        }
        // add by junye.li for defect 2397022 end

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            if (KEY_AUTO_SILENCE.equals(pref.getKey())) {
                final ListPreference autoSilencePref = (ListPreference) pref;
                String delay = (String) newValue;
                updateAutoSnoozeSummary(autoSilencePref, delay);
            } else if (KEY_CLOCK_STYLE.equals(pref.getKey())) {
                final ListPreference clockStylePref = (ListPreference) pref;
                final int idx = clockStylePref.findIndexOfValue((String) newValue);
                clockStylePref.setSummary(clockStylePref.getEntries()[idx]);
            } else if (KEY_HOME_TZ.equals(pref.getKey())) {
                final ListPreference homeTimezonePref = (ListPreference) pref;
                final int idx = homeTimezonePref.findIndexOfValue((String) newValue);
                //modify by yeqing.lv for defect 4068759 at 2017-2-23 begin
                // homeTimezonePref.setSummary(homeTimezonePref.getEntries()[idx]);
                editor.putInt("mIndx", idx);
                editor.commit();
                final String getRuCity = homeTimezonePref.getEntries()[idx].toString().substring(12);
                final String RuCityTime = homeTimezonePref.getEntries()[idx].toString().substring(1, 10);
                final String [] mRussianCity = this.getResources().getStringArray(R.array.russian_timezonename);
                //LogUtils.d("yeqing.lv" , "isCreate="+ mRuZoneMap.containsKey(getRuCity));
                if (mRuZoneMap.containsKey(getRuCity))  {
                    homeTimezonePref.setSummary("(" +RuCityTime+")" +" " + mRussianCity[Integer.parseInt(mRuZoneMap.get(getRuCity)) - 1]);
                } else {
                    homeTimezonePref.setSummary(homeTimezonePref.getEntries()[idx]);
                }
                //modify by yeqing .lvfor defect 4068759 at 2017-2-23 end
                notifyHomeTimeZoneChanged();
            } else if (KEY_AUTO_HOME_CLOCK.equals(pref.getKey())) {
                final boolean autoHomeClockEnabled = ((CheckBoxPreference) pref).isChecked();
                final Preference homeTimeZonePref = findPreference(KEY_HOME_TZ);
                homeTimeZonePref.setEnabled(!autoHomeClockEnabled);
                notifyHomeTimeZoneChanged();
            } else if (KEY_VOLUME_BUTTONS.equals(pref.getKey())) {
                final ListPreference volumeButtonsPref = (ListPreference) pref;
                final int index = volumeButtonsPref.findIndexOfValue((String) newValue);
                volumeButtonsPref.setSummary(volumeButtonsPref.getEntries()[index]);
            } else if (KEY_WEEK_START.equals(pref.getKey())) {
                final ListPreference weekStartPref = (ListPreference) findPreference(KEY_WEEK_START);
                final int idx = weekStartPref.findIndexOfValue((String) newValue);
                weekStartPref.setSummary(weekStartPref.getEntries()[idx]);
            }
            // Set result so DeskClock knows to refresh itself
            getActivity().setResult(RESULT_OK);
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference pref) {
            final Activity activity = getActivity();
            if (activity == null) {
                return false;
            }

            if (KEY_ALARM_VOLUME.equals(pref.getKey())) {
                final AudioManager audioManager =
                        (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
                        AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
                return true;
            }
            return false;
        }

        /**
         * Returns an array of ids/time zones. This returns a double indexed array
         * of ids and time zones for Calendar. It is an inefficient method and
         * shouldn't be called often, but can be used for one time generation of
         * this list.
         *
         * @return double array of tz ids and tz names
         */
        public CharSequence[][] getAllTimezones() {
            Resources resources = this.getResources();
            String[] ids = resources.getStringArray(R.array.timezone_values);
            String[] labels = resources.getStringArray(R.array.timezone_labels);
            int minLength = ids.length;
            if (ids.length != labels.length) {
                minLength = Math.min(minLength, labels.length);
                LogUtils.e("Timezone ids and labels have different length!");
            }
            List<TimeZoneRow> timezones = new ArrayList<>();
            for (int i = 0; i < minLength; i++) {
                timezones.add(new TimeZoneRow(ids[i], labels[i]));
            }
            Collections.sort(timezones);

            CharSequence[][] timeZones = new CharSequence[2][timezones.size()];
            int i = 0;
            for (TimeZoneRow row : timezones) {
                timeZones[0][i] = row.mId;
                timeZones[1][i++] = row.mDisplayName;
            }
            return timeZones;
        }

        private void refresh() {
            final ListPreference autoSilencePref =
                    (ListPreference) findPreference(KEY_AUTO_SILENCE);
            String delay = autoSilencePref.getValue();
            updateAutoSnoozeSummary(autoSilencePref, delay);
            autoSilencePref.setOnPreferenceChangeListener(this);

            final ListPreference clockStylePref = (ListPreference) findPreference(KEY_CLOCK_STYLE);
            clockStylePref.setSummary(clockStylePref.getEntry());
            clockStylePref.setOnPreferenceChangeListener(this);

            final Preference autoHomeClockPref = findPreference(KEY_AUTO_HOME_CLOCK);
            final boolean autoHomeClockEnabled =
                    ((CheckBoxPreference) autoHomeClockPref).isChecked();
            autoHomeClockPref.setOnPreferenceChangeListener(this);

            final ListPreference homeTimezonePref = (ListPreference) findPreference(KEY_HOME_TZ);
            homeTimezonePref.setEnabled(autoHomeClockEnabled);

            //modify by yeqing.lv for defect 4068759 at 2017-2-23 begin
            // homeTimezonePref.setSummary(homeTimezonePref.getEntry());
            mIndx = sharedPreferences.getInt("mIndx", 0);
            final String getRuCity = homeTimezonePref.getEntries()[mIndx].toString().substring(12);
            final String RuCityTime = homeTimezonePref.getEntries()[mIndx].toString().substring(1,10);
            final String [] mRussianCity = this.getResources().getStringArray(R.array.russian_timezonename);
            if (mRuZoneMap.containsKey(getRuCity)) {
                homeTimezonePref.setSummary("(" +RuCityTime+")" +" " + mRussianCity[Integer.parseInt(mRuZoneMap.get(getRuCity)) - 1]);
            } else {
                homeTimezonePref.setSummary(homeTimezonePref.getEntry());
            }
            //modify by yeqing.lv for defect 4068759 at 2017-2-23 end
            homeTimezonePref.setOnPreferenceChangeListener(this);

            final ListPreference volumeButtonsPref =
                    (ListPreference) findPreference(KEY_VOLUME_BUTTONS);
            volumeButtonsPref.setSummary(volumeButtonsPref.getEntry());
            volumeButtonsPref.setOnPreferenceChangeListener(this);

            final Preference volumePref = findPreference(KEY_ALARM_VOLUME);
            volumePref.setOnPreferenceClickListener(this);

            final SnoozeLengthDialog snoozePref =
                    (SnoozeLengthDialog) findPreference(KEY_ALARM_SNOOZE);
            snoozePref.setSummary();

            final ListPreference weekStartPref = (ListPreference) findPreference(KEY_WEEK_START);
            // Set the default value programmatically
            final String value = weekStartPref.getValue();
            // modify by junye.li for defect 1239052 begin
            int defaultIndex = weekStartPref.findIndexOfValue(
                    value == null ? String.valueOf(Utils.DEFAULT_WEEK_START) : value);
            // modify by fan.yang for task 1150916 start at 2015.12.14
            String firstdayOfWeekString = getResources().getString(R.string.def_alarm_firstdayOfWeek);
            final int firstdayOfWeekIndex = Integer.valueOf(firstdayOfWeekString).intValue();
            if (firstdayOfWeekIndex != -1) {
                defaultIndex = value == null ? firstdayOfWeekIndex : weekStartPref.findIndexOfValue(value);
            }
            final int idx = defaultIndex;
            weekStartPref.setValueIndex(idx);
            weekStartPref.setSummary(weekStartPref.getEntries()[idx]);
            // modify by fan.yang for task 1150916 end at 2015.12.14
            // modify by junye.li for defect 1239052 end
            weekStartPref.setOnPreferenceChangeListener(this);
        }

        private void updateAutoSnoozeSummary(ListPreference listPref, String delay) {
            int i = Integer.parseInt(delay);
            if (i == -1) {
                listPref.setSummary(R.string.auto_silence_never);
            } else {
                listPref.setSummary(Utils.getNumberFormattedQuantityString(getActivity(),
                        R.plurals.auto_silence_summary, i));
            }
        }

        private void notifyHomeTimeZoneChanged() {
            Intent i = new Intent(Cities.WORLDCLOCK_UPDATE_INTENT);
            getActivity().sendBroadcast(i);
        }

        private class TimeZoneRow implements Comparable<TimeZoneRow> {
            private static final boolean SHOW_DAYLIGHT_SAVINGS_INDICATOR = false;

            public final String mId;
            public final String mDisplayName;
            public final int mOffset;

            public TimeZoneRow(String id, String name) {
                mId = id;
                TimeZone tz = TimeZone.getTimeZone(id);
                boolean useDaylightTime = tz.useDaylightTime();
                mOffset = tz.getOffset(mTime);
                mDisplayName = buildGmtDisplayName(name, useDaylightTime);

            }

            @Override
            public int compareTo(TimeZoneRow another) {
                return mOffset - another.mOffset;
            }

            public String buildGmtDisplayName(String displayName, boolean useDaylightTime) {
                int p = Math.abs(mOffset);
                StringBuilder name = new StringBuilder("(GMT");
                name.append(mOffset < 0 ? '-' : '+');
                //modify by yeqing.lv for defect 4068759 at 2017-2-23 begin
                // name.append(p / DateUtils.HOUR_IN_MILLIS);
                if (p / DateUtils.HOUR_IN_MILLIS < 10) {
                    name.append('0');
                    name.append(p / DateUtils.HOUR_IN_MILLIS);
                } else {
                    name.append(p / DateUtils.HOUR_IN_MILLIS);
                }
                //modify by yeqing.lv for defect 4068759 at 2017-2-23 end
                name.append(':');

                int min = p / 60000;
                min %= 60;

                if (min < 10) {
                    name.append('0');
                }
                name.append(min);
                name.append(") ");
                name.append(displayName);
                if (useDaylightTime && SHOW_DAYLIGHT_SAVINGS_INDICATOR) {
                    name.append(" \u2600"); // Sun symbol
                }
                return name.toString();
            }
        }
    }
}
