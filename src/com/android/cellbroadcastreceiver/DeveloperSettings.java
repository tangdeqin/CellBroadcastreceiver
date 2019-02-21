/* Copyright (C) 2016 Tcl Corporation Limited */
/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (c) 2013, The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/******************************************************************************/
package com.android.cellbroadcastreceiver;


import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor; // MODIFIED by yuxuan.zhang, 2016-09-29,BUG-2845457
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.media.AudioManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;
import com.android.cb.util.TLog;
import com.tct.constants.TctQctConstants;
import com.tct.util.FwkPlf;
import com.tct.util.IsdmParser;
import com.tct.wrapper.TctWrapperManager;

/**
 * Settings activity for the cell broadcast receiver.
 */
public class DeveloperSettings extends PreferenceActivity {

    public static final String TAG = "DeveloperSettings";

    public static final String KEY_ENABLE_CMAS_RMT_ALERTS = "pref_key_enable_cmas_rmt_alerts";

    public static final String KEY_ENABLE_CMAS_EXERCISE_ALERTS = "pref_key_enable_cmas_exercise_alerts";

    public static final String KEY_CATEGORY_DEVELOP_SETTINGS = "category_develop_settings"; // MODIFIED by yuxuan.zhang, 2016-09-29,BUG-2845457

    public static long sSubscription = PhoneConstants.SUB1;

    private static int[] subString = {R.string.sub1, R.string.sub2};

    private boolean mEnableSingleSIM = false;

    public boolean mWpasFlag = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEnableSingleSIM = IsdmParser.getBooleanFwk(getApplicationContext(),
                FwkPlf.def_cellbroadcast_enable_single_sim, false);
         mWpasFlag = getResources().getBoolean(R.bool.def_enable_wpas_function);
         // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
         if (!CBSUtills.isCanadaSimCard(this)) {
        	 mWpasFlag = false; 
         }
         // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        Log.i(TAG, "mEnableSingleSIM=" + mEnableSingleSIM);
        if (TelephonyManager.getDefault().isMultiSimEnabled() && !mEnableSingleSIM) {
            sSubscription = PhoneConstants.SUB1;
            final ActionBar actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayShowTitleEnabled(true);
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                String tabLabel = getString(subString[i]);
                actionBar.addTab(actionBar.newTab().setText(tabLabel).setTabListener(
                        new MySubTabListener(new CellBroadcastSettingsFragment(),
                        tabLabel, i)));
            }
        } else {
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    new CellBroadcastSettingsFragment()).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("android:fragments", null);
    }

    private class MySubTabListener implements ActionBar.TabListener {

        private CellBroadcastSettingsFragment mFragment;
        private String tag;
        private int subScription;

        public MySubTabListener(CellBroadcastSettingsFragment cbFragment, String tag,
                int subScription) {
            this.mFragment = cbFragment;
            this.tag = tag;
            this.subScription = subScription;
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            ft.add(android.R.id.content, mFragment, tag);
            sSubscription = subScription;
            TLog.d(TAG, "onTabSelected  sSubscription:" + sSubscription);
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.remove(mFragment);
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }

    private class CellBroadcastSettingsFragment extends PreferenceFragment implements
            Preference.OnPreferenceClickListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            TLog.d(TAG, "onCreate CellBroadcastSettingsFragment  sSubscription :" + sSubscription);
            addPreferencesFromResource(R.xml.develop_preferences);
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            PreferenceCategory alertCategory =
                    (PreferenceCategory) findPreference(KEY_CATEGORY_DEVELOP_SETTINGS); // MODIFIED by yuxuan.zhang, 2016-09-29,BUG-2845457
            final CheckBoxPreference enableCmasRMTAlerts =
                    (CheckBoxPreference)findPreference(KEY_ENABLE_CMAS_RMT_ALERTS);
            final CheckBoxPreference enableCmasExerciseAlerts =
                    (CheckBoxPreference)findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS);
            Preference.OnPreferenceChangeListener startConfigServiceListener =
                    new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference pref, Object newValue) {
                    String value = String.valueOf(newValue);
                    SharedPreferences.Editor editor = prefs.edit();

                    if(pref == enableCmasRMTAlerts) { // MODIFIED by yuxuan.zhang, 2016-09-29,BUG-2845457
                        TLog.d(TAG, "enableCmasRMTAlerts: " + Boolean.valueOf((value)));
                        editor.putBoolean(KEY_ENABLE_CMAS_RMT_ALERTS
                                + sSubscription, Boolean.valueOf((value)));
                    }else if (pref == enableCmasExerciseAlerts) {
                        TLog.d(TAG, "enableCmasExerciseAlerts: " + Boolean.valueOf((value)));
                        editor.putBoolean(KEY_ENABLE_CMAS_EXERCISE_ALERTS
                                + sSubscription, Boolean.valueOf((value)));
                    }
                    editor.commit();
                    CellBroadcastReceiver.startConfigServiceFromCMASSetting(pref.getContext(), sSubscription);

                    return true;
                }
            };

            boolean isEnableRMTExceriseAlertType = getResources().getBoolean(R.bool.def_enableRMTExerciseTestAlert);
            android.util.Log.d(TAG,"isEnableRMTExceriseAlertType = "+isEnableRMTExceriseAlertType);
            if(!isEnableRMTExceriseAlertType) { // MODIFIED by yuxuan.zhang, 2016-09-29,BUG-2845457
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_CMAS_RMT_ALERTS));
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS));
            }

            if (enableCmasRMTAlerts != null) {
                enableCmasRMTAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }

            if (enableCmasExerciseAlerts != null) {
                enableCmasExerciseAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }

        }

        @Override
        public void onResume() {
            Log.i(TAG, "onResume");
            super.onResume();
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            final CheckBoxPreference pref = (CheckBoxPreference)preference;
            if (!pref.isChecked()) {

            } else {
                    pref.setChecked(true);
                    CellBroadcastReceiver.startConfigServiceFromCMASSetting(pref.getContext(),sSubscription);
            }
            return true;
    }
    }
}
