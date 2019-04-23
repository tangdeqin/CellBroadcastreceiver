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

package com.jrdcom.timetool.alarm.activity;

import java.util.ArrayList;
import java.util.Arrays;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.os.SystemProperties;
import com.android.deskclock.R;

/**
 * Settings for the Alarm Clock.
 */
public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "SettingsActivity";
	private static final boolean DEBUG = false; 
	//Added by xiaxia.yao for PR:413675 begin
	private static final int ALARM_STREAM_TYPE_BIT =
            1 << AudioManager.STREAM_ALARM;
	static final String KEY_ALARM_IN_SILENT_MODE =
            "alarm_in_silent_mode";
	//Added by xiaxia.yao for PR:413675 end
    public static final String KEY_ALARM_SNOOZE = "snooze_duration"; // Add by caorongxing for PR:433114
    public static final String KEY_VOLUME_BEHAVIOR = "volume_button_setting";
    public static final String KEY_AUTO_SILENCE = "auto_silence";
    public static final String KEY_ALARM_PREFERENCE = "turn_over_preference";
    public static final String SNOOZE_ENABLE = "snooze_enabled";
    public static final String PRE_SUMMARY = "pre_summary";
    // modify by Yanjingming for pr525162 begin
    public static final String DEFAULT_ALARM_TIMEOUT = "10";
    private static final String ERROR_VALUE = "-1";
    // modify by Yanjingming for pr525162 end
    private SwitchPreference mTurnOverPreference;
    private String[] mAlarmItem;
    private String mCurSummmary;
    //Added by xiaxia.yao for turn over to active --001 begin 
    private boolean mSnoozeEnable;
    private boolean mStopEnable;
    //private boolean mSnoozeEnable = true;
    //private SharedPreferences sharedPre;
    //Added by xiaxia.yao for turn over to active --001 end
    // PR:510457 add by xibin
    private  AlertDialog mAlertDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_settings);
        View bk_btn = findViewById(R.id.bk_btn);
        bk_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
    protected void onDestroy() {
    	//Added by xiaxia.yao for turn over to active --002 begin 
        //sharedPre.edit().putString(PRE_SUMMARY, mCurSummmary)
         //       .putBoolean(SNOOZE_ENABLE, mSnoozeEnable).commit();
        //Added by xiaxia.yao for turn over to active --002 end
        super.onDestroy();
        // PR:510457 add by xibin start
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        // PR:510457 add by xibin end
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlarmItem = getResources().getStringArray(R.array.gestures_alarm_item);
        refresh();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            final Preference preference) {
    	//Added by xiaxia.yao for PR:413675 begin
    	if (KEY_ALARM_IN_SILENT_MODE.equals(preference.getKey())) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            int ringerModeStreamTypes = Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

            if (pref.isChecked()) {
                ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
            } else {
                ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
            }

            Settings.System.putInt(getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED,
                    ringerModeStreamTypes);

            return true;
        }        
    	//Added by xiaxia.yao for PR:413675 end
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (KEY_ALARM_SNOOZE.equals(pref.getKey())) {
            final ListPreference listPref = (ListPreference) pref;
            final int idx = listPref.findIndexOfValue((String) newValue);
            listPref.setSummary(listPref.getEntries()[idx]);
        } else if (KEY_AUTO_SILENCE.equals(pref.getKey())) {
            final ListPreference listPref = (ListPreference) pref;
            String delay = (String) newValue;
            updateAutoSnoozeSummary(listPref, delay);
            // Added by xiaxia.yao for turn over to active --003 begin
        } else if (KEY_ALARM_PREFERENCE.equals(pref.getKey())) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ALARM_TURNOVER_ENABLE, (Boolean)newValue ? 1 : 0);
        } // Added by xiaxia.yao for turn over to active --003 end
        return true;
    }

    private void updateAutoSnoozeSummary(ListPreference listPref, String delay) {
        int i = Integer.parseInt(delay);
        //modify by Yanjingming for pr525162 begin
        if (i <= 0) {
            i = Integer.parseInt(DEFAULT_ALARM_TIMEOUT);
        }
        //modify by Yanjingming for pr525162 end
            listPref.setSummary(getString(R.string.auto_silence_summary, i));
    }

    private void refresh() {
    	//Added by xiaxia.yao for PR:413675 begin
    	final CheckBoxPreference alarmInSilentModePref =
                (CheckBoxPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
        final int silentModeStreams =
                Settings.System.getInt(getContentResolver(),
                        Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
        alarmInSilentModePref.setChecked(
                (silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);

//        final int silentModeStreams = Settings.System.getInt(
//                getContentResolver(),
//                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
        //Added by xiaxia.yao for PR:413675 end
        ListPreference listPref = (ListPreference) findPreference(KEY_ALARM_SNOOZE);
        listPref.setSummary(listPref.getEntry());
        listPref.setOnPreferenceChangeListener(this);
        /*FR 548923- Neo Skunkworks - Paul Xu deleted - 001 Begin*/
        /*
        listPref = (ListPreference) findPreference(KEY_AUTO_SILENCE);
        String delay = listPref.getValue();
        // add by Yanjingming for pr525162 begin
        if(ERROR_VALUE.equals(delay)){
            listPref.setValue(DEFAULT_ALARM_TIMEOUT);
        }
        // add by Yanjingming for pr525162 end
        updateAutoSnoozeSummary(listPref, delay);
        listPref.setOnPreferenceChangeListener(this);
        */
        /*FR 548923- Neo Skunkworks - Paul Xu deleted - 001 End*/
        // Added by xiaxia.yao for turn over to active --004 begin
        mTurnOverPreference = (SwitchPreference) findPreference(KEY_ALARM_PREFERENCE);
        //mTurnOverPreference.setPersistent(true);
        mTurnOverPreference.setPersistent(false);
        /*PR 631260- Neo Skunkworks - Paul Xu added - 001 Begin*/
        /*Set alarm turnover enable to default value*/
        if(DEBUG){
            Log.d(TAG, "ro_def_alarm_turnover_enable:" + SystemProperties.get("ro_def_alarm_turnover_enable"));
        }
        if (("false".equalsIgnoreCase(SystemProperties.get("ro_def_alarm_turnover_enable")))){
            mTurnOverPreference.setChecked(Settings.System.getInt(getContentResolver(), 
                   Settings.System.ALARM_TURNOVER_ENABLE, 0) == 1);
        }else{
            mTurnOverPreference.setChecked(Settings.System.getInt(getContentResolver(), 
                   Settings.System.ALARM_TURNOVER_ENABLE, 1) == 1);
        }
        /*PR 631260- Neo Skunkworks - Paul Xu added - 001 End*/
        mTurnOverPreference.setOnPreferenceChangeListener(this);
        mTurnOverPreference.setOnPreferenceClickListener(this);
        //sharedPre = getSharedPreferences(SetAlarm.RINGTONE_OF_PREALARM,
        //       MODE_PRIVATE);
        //mSnoozeEnable = sharedPre.getBoolean(SNOOZE_ENABLE, true);
        mSnoozeEnable = Settings.System.getInt(getContentResolver(),
                Settings.System.SNOOZE_ENABLE, 1) == 1;
        mStopEnable = Settings.System.getInt(getContentResolver(),
                Settings.System.STOP_ENABLE, 0) == 1;
        if(mSnoozeEnable){
        	mCurSummmary = mAlarmItem[0];
        }//else{
        if(mStopEnable){
        	mCurSummmary = mAlarmItem[1];
        }
       // Added by xiaxia.yao for turn over to active --004 end
        mTurnOverPreference.setSummary(mCurSummmary);
    }
    
    /*PR 631260- Neo Skunkworks - Paul Xu added - 001 Begin*/
    /**
     * Get the alarm turnover enable value.
     *
     * @param  null
     * @return boolean
     */
    private boolean getAlarmTurnOverEnableValue(){
    	boolean enable = SystemProperties.getBoolean("ro_def_alarm_turnover_enable", true);
    	
    	return enable;
    }
    /*PR 631260- Neo Skunkworks - Paul Xu added - 001 End*/

    private void showAlarmStatusDialog() {
        ArrayList<String> labelArray = new ArrayList<String>(Arrays.asList(mAlarmItem));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, labelArray);
        int index = 0;
        if (mTurnOverPreference.getSummary().equals(mAlarmItem[0])) {
            index = 0;
        } else if (mTurnOverPreference.getSummary().equals(mAlarmItem[1])) {
            index = 1;
        }
        // PR:510457 add by xibin
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.turn_over_to_active_dialog_title))
                .setSingleChoiceItems(adapter, index, mItemChoiceListener)
                .setNegativeButton(android.R.string.cancel, null).create();
        mAlertDialog.show();//PR:512833 add by XIBIN
    }

    private DialogInterface.OnClickListener mItemChoiceListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            if (mAlarmItem != null && mAlarmItem.length > button) {
                mTurnOverPreference.setSummary(mAlarmItem[button]);
            }
            if (button == 0) {
            	// Added by xiaxia.yao for turn over to active --005 begin
                //mCurSummmary = mAlarmItem[0];
                //mSnoozeEnable = true;
            	Settings.System.putInt(getContentResolver(), Settings.System.SNOOZE_ENABLE, 1);
            	Settings.System.putInt(getContentResolver(), Settings.System.STOP_ENABLE, 0);
            } else if (button == 1) {
                //mCurSummmary = mAlarmItem[1];
                //mSnoozeEnable = false;
            	Settings.System.putInt(getContentResolver(), Settings.System.STOP_ENABLE, 1);
            	Settings.System.putInt(getContentResolver(), Settings.System.SNOOZE_ENABLE, 0);
             // Added by xiaxia.yao for turn over to active --005 end
            }
            dialog.dismiss();
        }
    };

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (KEY_ALARM_PREFERENCE.equals(preference.getKey())) {
            if (mTurnOverPreference.isChecked()) {
                mTurnOverPreference.setChecked(false);
            } else {
                mTurnOverPreference.setChecked(true);
            }
            // Added by xiaxia.yao for turn over to active --006 begin
            boolean value = mTurnOverPreference.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ALARM_TURNOVER_ENABLE, (Boolean) value ? 1 : 0);
            // Added by xiaxia.yao for turn over to active --006 end
            showAlarmStatusDialog();
        }
        return true;
    }

}
