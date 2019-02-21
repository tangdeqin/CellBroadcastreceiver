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
/* ==========================================================================
 *     Modifications on Features list / Changes Request / Problems Report
 * --------------------------------------------------------------------------
 *    date   |        author        |         Key          |     comment
 * ----------|----------------------|----------------------|-----------------
 * 09/07/2012|yugang.jia            | FR-516039            |[Ergo][DEV]CMAS & CB,
 * ----------|----------------------|----------------------|-----------------
 * 04/18/2014|ke.meng               | FR-636168            |[Ergo][DEV]CMAS & CB,
/* ----------|----------------------|----------------------|----------------- */
/* 07/11/2014|      wenmi.wei       |        PR723331      |[Dual][Cell broadcasts]*/
/*           |                      |                      |[Force close][Settings]*/
/*           |                      |                      |Cell broadcasts crashed*/
/*           |                      |                      | unexpectedly          */
/* ----------|----------------------|----------------------|------------------*/
/* 09/04/2014|     tianming.lei     |        715519        |DT variant for tmo*/
/* ----------|----------------------|----------------------|----------------- */
/* 09/17/2014|      fujun.yang      |        778720        |[SS][CMAS]There   */
/*           |                      |                      |are some wrong    */
/*           |                      |                      |points on CMAS    */
/*           |                      |                      |settings          */
/* ----------|----------------------|----------------------|----------------- */
/* 01/21/2015|      fujun.yang      |        895849        |[SCB][CMAS]Can't--*/
/*           |                      |                      |receive-----------*/
/*           |                      |                      |RMT&Exercise------*/
/*           |                      |                      |alerts------------*/
/* ----------|----------------------|----------------------|----------------- */
/* 02/05/2015|      fujun.yang      |        886284        |[SCB][CMAS][ETWS]Can*/
/*           |                      |                      |receive CMAS and  */
/*           |                      |                      |ETWS even not     */
/*           |                      |                      |enable "show      */
/*           |                      |                      |ETWS/CMAS test    */
/*           |                      |                      |broadcasts"       */
/* ----------|----------------------|----------------------|----------------- */
/* 02/14/2015|      fujun.yang      |        928215        |CMAS]Emergency    */
/*           |                      |                      |alert settings    */
/* ----------|----------------------|----------------------|----------------- */
/* 07/18/2015|      peng.guo        |        1045088       |[SMS]Please remove*/
/*           |                      |                      | the Vibrate option*/
/*           |                      |                      |in Emergency alerts.*/
/* ----------|----------------------|----------------------|----------------- */
/* 11/26/2018|      yong.wang       |        7134121       |[CB]there always  */ 
/*			 |						|					   |pop up simcard    */
/*			 |						|					   |choosing dialog   */
/*			 |						|					   |when click  some  */
/*			 |						|					   |items on cb       */ 
/*			 |						|					   |setting screen    */
/* ----------| ---------------------|----------------------|----------------- */
/******************************************************************************/
package com.android.cellbroadcastreceiver;


import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor; // MODIFIED by yuxuan.zhang, 2016-05-09,BUG-1112693
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
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.media.AudioManager;
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-09,BUG-1112693*/
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener; // MODIFIED by yuxuan.zhang, 2016-05-13,BUG-1112693
import android.media.ToneGenerator;
import android.media.MediaPlayer.OnErrorListener;
/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
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
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-04-25,BUG-1112693
//add by liang.zhang for Defect 5960218 at 2018-02-06 begin
import java.util.ArrayList;
import java.util.List;
import android.telephony.SubscriptionInfo;
//add by liang.zhang for Defect 5960218 at 2018-02-06 end

import com.android.internal.telephony.PhoneConstants;
import com.android.cb.util.TLog;
import com.tct.constants.TctQctConstants;
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-13,BUG-1112693*/
import com.tct.util.FwkPlf;
import com.tct.util.IsdmParser;
/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
import com.tct.wrapper.TctWrapperManager;

/**
 * Settings activity for the cell broadcast receiver.
 */
public class CellBroadcastSettings extends PreferenceActivity {

    public static final String TAG = "CellBroadcastSettings";
    public static Intent cbIntent = null;
    //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
    // Preference key for whether to enable emergency notifications (default enabled).
    public static final boolean KEY_ENABLE_EMERGENCY_ALERTS_BOOLEAN = true;

    public static final String KEY_ENABLE_EMERGENCY_ALERTS = "enable_emergency_alerts";

    public static final String KEY_PREVIEW_CMAS_ALERT_TONE = "pref_key_preview_cmas_alert_tone";

    public static final String KEY_PREVIEW_CMAS_VIBRATION = "pref_key_preview_cmas_vibration";

    public static final String KEY_ENABLE_CMAS_RMT_ALERTS = "pref_key_enable_cmas_rmt_alerts";

    public static final String KEY_ENABLE_CMAS_EXERCISE_ALERTS = "pref_key_enable_cmas_exercise_alerts";

    public static final String KEY_ENABLE_CMAS_OPERATOR_DEFINED_ALERTS = "key_enable_cmas_operator_defined_alerts";
    //[BUGFIX]-Add by peng.guo, 2015-07-18,PR1045088 Begin
    public static final String KEY_CATEGORY_CMAS_ALERT_PREVIEW = "category_cmas_alert_preview";
    //[BUGFIX]-Add by peng.guo, 2015-07-18,PR1045088 END
    // Duration of alert sound (in seconds).
    public static final String KEY_ALERT_SOUND_DURATION = "alert_sound_duration";

    // Default alert duration (in seconds).
    public static final String ALERT_SOUND_DEFAULT_DURATION = "10";
    //[BUGFIX]-MOD-END by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo

    // Enable vibration on alert (unless master volume is silent).
    public static final String KEY_ENABLE_ALERT_VIBRATE = "enable_alert_vibrate";
    /* aiyan-978029-Enable audio on alert (unless master volume is silent).T-Mobile requirement */
    public static final String KEY_ENABLE_ALERT_AUDIO = "enable_alert_audio";

    // Speak contents of alert after playing the alert sound.
    public static final String KEY_ENABLE_ALERT_SPEECH = "enable_alert_speech";
    public static final String KEY_ENABLE_ALERT_SPEECH_EX = "enable_alert_speech_ex"; // MODIFIED by yuxuan.zhang, 2016-07-22,BUG-2579682

    // Preference category for emergency alert and CMAS settings.
    public static final String KEY_CATEGORY_ALERT_SETTINGS = "category_alert_settings";

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-20,BUG-1112693*/
    // Preference category for presidential CMAS settings.
    public static final String KEY_PRESIDENTIAL_ALERT_SETTINGS = "pref_key_presidential_alert";
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    // Preference category for ETWS related settings.
    public static final String KEY_CATEGORY_ETWS_SETTINGS = "category_etws_settings";

    // Whether to display CMAS extreme threat notifications (default is enabled).
    public static final String KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS =
            "enable_cmas_extreme_threat_alerts";

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
    // Whether to display Wpas notifications (default is enabled,can not be changed).
    public static final String KEY_ENABLE_WPAS_ALERTS =
            "enable_wpas_alerts";
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    
    // add by liang.zhang for Defect 6272370 at 2018-05-04 begin
    public static final String KEY_WPAS_ALERTS_INBOX = "wpas_alerts_inbox";
    // add by liang.zhang for Defect 6272370 at 2018-05-04 end
    
    public static final String KEY_CATEGORY_OTHER_SETTINGS = "other_alert_settings";
    // Whether to display CMAS severe threat notifications (default is enabled).
    public static final String KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS =
            "enable_cmas_severe_threat_alerts";

    // Whether to display CMAS amber alert messages (default is enabled).
    public static final String KEY_ENABLE_CMAS_AMBER_ALERTS = "enable_cmas_amber_alerts";

    // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
    // Whether to display CMAS Spanish Language alert messages (default is enabled).
    public static final String KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS = "enable_cmas_spanish_language_alerts";
    // [BUGFIX]-MOD-END by bin.xue

    // Preference category for development settings (enabled by settings developer options toggle).
    public static final String KEY_CATEGORY_DEV_SETTINGS = "category_dev_settings";

    // Whether to display ETWS test messages (default is disabled).
    public static final String KEY_ENABLE_ETWS_TEST_ALERTS = "enable_etws_test_alerts";

    // Whether to display CMAS monthly test messages (default is disabled).
    public static final String KEY_ENABLE_CMAS_TEST_ALERTS = "enable_cmas_test_alerts";

    // Preference category for Brazil specific settings.
    public static final String KEY_CATEGORY_BRAZIL_SETTINGS = "category_brazil_settings";

    // Preference category for India specific settings.
    public static final String KEY_CATEGORY_INDIA_SETTINGS = "category_india_settings";

    // Preference key for whether to enable channel 50 notifications
    // Enabled by default for phones sold in Brazil, otherwise this setting may be hidden.
    public static final String KEY_ENABLE_CHANNEL_50_ALERTS = "enable_channel_50_alerts";

    public static final String KEY_ENABLE_CHANNEL_60_ALERTS = "enable_channel_60_alerts";

    // Preference key for initial opt-in/opt-out dialog.
    public static final String KEY_SHOW_CMAS_OPT_OUT_DIALOG = "show_cmas_opt_out_dialog";

    // Alert reminder interval ("once" = single 2 minute reminder).
    public static final String KEY_ALERT_REMINDER_INTERVAL = "alert_reminder_interval";

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
    // Whether to display WPAS test messages (default is disabled).
    public static final String KEY_ENABLE_WPAS_TEST_ALERTS = "enable_wpas_test_alerts";
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    // Default reminder interval is off.
    //public static final String ALERT_REMINDER_INTERVAL_DEFAULT_DURATION = "0"; // MODIFIED by bin.huang, 2016-11-10,BUG-1112693
 /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1112693*/
    // Default wpas reminder interval is off.
    public static final String WPAS_ALERT_REMINDER_INTERVAL_DEFAULT_DURATION = "2";
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    //[BUGFIX]-MOD-BEGIN by TSCD,tianming.lei 01/08/2015,PR-879862
    //public static long sPhoneId = SubscriptionManager.DEFAULT_SUB_ID;
    public static long sPhoneId = PhoneConstants.SUB1;
    //[BUGFIX]-MOD-END by TSCD,tianming.lei
    private static final String TAG_CMAS = "CMAS";//[FEATURE]-add-BEGIN by TCTNB.yugang.jia,09/06/2013,FR-516039

    //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
    private static boolean showMoreCMASMenu = true;
    private static CMASNotificationPreviewer cmasNotiPreviewer = null;

    private Handler handler;
    private static final int MESSAGE_ID_TONE_FINISHED = 2000;
    private static final int MESSAGE_ID_VIBRATE_FINISHED = 2001;
    //[FEATURE]-Add-END by TCTNB.yugang.jia
    private static int[] subString = {R.string.sub1, R.string.sub2};

    private static boolean showMoreCmasSetting = false; //[BUGFIX] by AMNJ.liujia, 11/18/2014, pr842839
    private final String SHOW_MORE_CMAS_SETTINGS = "show_more_cmas_settings"; //[BUGFIX] by AMNJ.liujia, 11/18/2014, pr842839
    private boolean mEnableSingleSIM = false;

    /*MODIFIED-BEGIN by yuxuan.zhang, 2016-04-19,BUG-838839*/
    public static final int WPAS_ALERT_FREQUENCY_BEGIN = 4370;
    public static final int WPAS_ALERT_FREQUENCY_END = 4399;
    public static final int WPAS_ALERT_FREQUENCY_EIGHT_ZERO = 4380;
    public static final int WPAS_ALERT_FREQUENCY_NINE_THREE = 4393;
    public static final String ALERT_TYPE_VALUE = "alert_type_value"; // MODIFIED by yuxuan.zhang, 2016-04-21,BUG-1112693
    /*MODIFIED-END by yuxuan.zhang,BUG-838839*/
    public static final int WPAS_OPERATOR_NINE_FOUR = 4394;
    public static final int WPAS_OPERATOR_NINE_FIVE = 4395;
    public static final int WPAS_OPERATOR_EIGHT_ONE = 4381;
    public static final int WPAS_OPERATOR_EIGHT_TWO = 4382;
    public static final float PREFERENCE_TITLE_TEXT_SIZE = 13f;
    public static final float PREFERENCE_SUMMARY_TEXT_SIZE = 11f;
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-09,BUG-1112693*/
    private MediaPlayer mMediaPlayer;
    private static final int STATE_IDLE = 0;
    private static final int STATE_ALERTING = 1;
    //[BUGFIX]-Add-BEGIN by yuwan,03/31/2017,4447029,
    private static final int STATE_COMPLETE = 2;
    //[BUGFIX]-Add-END by yuwan,03/31/2017,4447029,
    private int state = STATE_IDLE;
    int mOriginalStreamVolume = -1; // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1112693
    int mRingerMode = -1;
    public boolean mWpasFlag = false; // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2854334
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    private Dialog mDialog; // MODIFIED by bin.huang, 2016-11-08,BUG-3373995
    //[BUGFIX]-Add-BEGIN by yuwan,03/31/2017,4447029,
    private int soundCount;
    /* MODIFIED-BEGIN by yuwan, 2017-06-16,BUG-4937985*/
    private static final String SSKR = "sskr";
    private static final String HW_SIMCOUNT = "ro.telephony.hw.simcount";
    /* MODIFIED-END by yuwan,BUG-4937985*/
    //[BUGFIX]-Add-END by yuwan,03/31/2017,4447029,
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMoreCmasSetting = getIntent().getBooleanExtra(SHOW_MORE_CMAS_SETTINGS, false); //[BUGFIX] by AMNJ.liujia, 11/18/2014, pr842839
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-13,BUG-1112693*/
        mEnableSingleSIM = IsdmParser.getBooleanFwk(getApplicationContext(),
                FwkPlf.def_cellbroadcast_enable_single_sim, false);
        mWpasFlag = getResources().getBoolean(R.bool.def_enable_wpas_function); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2854334
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(this)) {
        	mWpasFlag = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        TLog.d(TAG, "mEnableSingleSIM=" + mEnableSingleSIM);
        cmasNotiPreviewer = new CMASNotificationPreviewer(); //[BUGFIX] ADD by chaobing.huang,9/2/2015,PR1077527
        /* MODIFIED-BEGIN by yuwan, 2017-06-16,BUG-4937985*/
        if (TelephonyManager.getDefault().isMultiSimEnabled() && !mEnableSingleSIM
                && !SystemProperties.get(HW_SIMCOUNT, null).equals(SSKR)) {
            Log.d(TAG, "key = " + SystemProperties.get(HW_SIMCOUNT, null));
             SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
            /* MODIFIED-END by yuwan,BUG-4937985*/
            sPhoneId = PhoneConstants.SUB1;
            final ActionBar actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayShowTitleEnabled(true);
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                final SubscriptionInfo sir = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
                if (sir == null) {
                    continue;
                }
                String tabLabel = getString(subString[i]);
                actionBar.addTab(actionBar.newTab().setText(tabLabel).setTabListener(
                        new MySubTabListener(new CellBroadcastSettingsFragment(),
                                tabLabel, i)));
            }
        } else {
            Log.d(TAG,"single sim"); // MODIFIED by yuwan, 2017-06-16,BUG-4937985
            //[BUGFIX]-Delete-BEGIN by TSCD.wenmi.wei,07/05/2014,723331,
            /*
      //[BUGFIX]-ADD-END by TCTNB.ke.meng, 09/11/2013, FR-636168, CMAS Ergo
            cmasNotiPreviewer = new CMASNotificationPreviewer();
            handler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                switch (msg.what) {
                                    case MESSAGE_ID_TONE_FINISHED:
                                        cmasNotiPreviewer.stopTone();
                                        break;
                                    case MESSAGE_ID_VIBRATE_FINISHED:
                                        cmasNotiPreviewer.stopVibrate();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        };
       //[FEATURE]-Add-END by TCTNB.ke.meng
            */
            //[BUGFIX]-Delete-END by TSCD.wenmi.wei
            // Display the fragment as the main content.
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    new CellBroadcastSettingsFragment()).commit();
        }
        //[BUGFIX]-Add-BEGIN by TSCD.wenmi.wei,07/05/2014,717211,
        //[BUGFIX]DELETE begin by chaobing.huang,9/2/2015,PR1077527
        //cmasNotiPreviewer = new CMASNotificationPreviewer();
        //[BUGFIX]DELETE end by chaobing.huang,9/2/2015,PR1077527
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_ID_TONE_FINISHED:
                        cmasNotiPreviewer.stopTone();
                        break;
                    case MESSAGE_ID_VIBRATE_FINISHED:
                        cmasNotiPreviewer.stopVibrate();
                        break;
                    default:
                        break;
                }
            }
        };
        //[BUGFIX]-Add-END by TSCD.wenmi.wei
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-06,BUG-2467069*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("android:fragments", null);
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-2467069*/

    private class MySubTabListener implements ActionBar.TabListener {

        private CellBroadcastSettingsFragment mFragment;
        private String tag;
        private int phoneId;

        public MySubTabListener(CellBroadcastSettingsFragment cbFragment, String tag,
                                int phoneId) {
            this.mFragment = cbFragment;
            this.tag = tag;
            this.phoneId = phoneId;
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            ft.add(android.R.id.content, mFragment, tag);
            sPhoneId = phoneId;
            TLog.d(TAG, "onTabSelected  sPhoneId:" + sPhoneId);
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

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-09,BUG-1112693*/
    private void play() {
        stop();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                TLog.e(TAG, "mMediaPlayer in settings is on Error");
                mp.stop();
                mp.release();
                state = STATE_IDLE;
                return true;
            }
        });

        try {
            setDataSourceFromResource(mMediaPlayer);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1112693*/
            int streamType = AudioManager.STREAM_NOTIFICATION;
            mOriginalStreamVolume = cmasNotiPreviewer.getAudioManager().getStreamVolume(streamType);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-2251957*/
            int mode = cmasNotiPreviewer.getAudioManager().getRingerMode();
            mRingerMode = mode;
            Log.i(TAG, "Original mode= " + mode);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            Log.i(TAG, "OriginalStreamVolume = " + mOriginalStreamVolume);
            int MaxStreamVolume = cmasNotiPreviewer.getAudioManager().getStreamMaxVolume(streamType);
            Log.i(TAG, "mMaxStreamVolume = " + MaxStreamVolume);
            /* MODIFIED-END by yuxuan.zhang,BUG-2251957*/
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            if (mWpasFlag) {
                cmasNotiPreviewer.getAudioManager().setStreamVolume(streamType, MaxStreamVolume, 0); // MODIFIED by yuxuan.zhang, 2016-05-13,BUG-1112693
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            cmasNotiPreviewer.getAudioManager().requestAudioFocus(null,
                    AudioManager.STREAM_NOTIFICATION,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            startAlarm(mMediaPlayer);
            //[BUGFIX]-Add-BEGIN by yuwan,03/31/2017,4447029,
            soundCount = 0;
            //[BUGFIX]-Add-END by yuwan,03/31/2017,4447029,
        } catch (Exception e) {
            Log.e(TAG, "mMediaPlayer in settings is on Error", e); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
            try {
                restoreToOriginalVolume(); // MODIFIED by yuxuan.zhang, 2016-05-13,BUG-1112693
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (Exception e2) {
                // TODO: handle exception
            }
            cmasNotiPreviewer.getAudioManager().abandonAudioFocus(null);
            state = STATE_IDLE;
            Log.e(TAG, "mMediaPlayer play failed"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
        }
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
    private void restoreToOriginalVolume() {
        if (mWpasFlag && cmasNotiPreviewer != null && mOriginalStreamVolume >= 0) { // MODIFIED by yuxuan.zhang, 2016-06-08,BUG-1112693
            Log.w(TAG, "restore mOriginalStreamVolume = " + mOriginalStreamVolume);
            cmasNotiPreviewer.getAudioManager().setStreamVolume(
                    AudioManager.STREAM_NOTIFICATION, mOriginalStreamVolume, 0);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-2251957*/
            if (mRingerMode != -1) {
                Log.i(TAG, "restore Ringermode= " + mRingerMode);
                cmasNotiPreviewer.getAudioManager().setRingerMode(mRingerMode);
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-2251957*/
        }
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    private void setDataSourceFromResource(MediaPlayer player) throws java.io.IOException {
        AssetFileDescriptor afd = getResources().openRawResourceFd(mWpasFlag ? R.raw.alarm_alert : R.raw.attention_signal); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2854334
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    private void startAlarm(MediaPlayer player)
            throws java.io.IOException, IllegalArgumentException, IllegalStateException {
        player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        player.setLooping(false); // MODIFIED by yuxuan.zhang, 2016-05-13,BUG-1112693
        player.prepare();
        player.start();
        state = STATE_ALERTING;
        //[BUGFIX]-Add-BEGIN by yuwan,03/31/2017,4447029,
        player.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        try {
                            if (soundCount < 1) {
                                player.start();
                                soundCount++;
                                Log.d(TAG, " scoundCount = " + soundCount);
                                state = STATE_ALERTING;
                            } else {
                                state = STATE_COMPLETE;
                                Log.d(TAG, " state = " + soundCount);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        //[BUGFIX]-Add-END by yuwan,03/31/2017,4447029,
    }

    private void stop() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (IllegalStateException e) {
                // catch "Unable to retrieve AudioTrack pointer for stop()" exception
                Log.e(TAG, "exception trying to stop media player"); // MODIFIED by yuxuan.zhang, 2016-06-08,BUG-1112693
            }
        }
        cmasNotiPreviewer.getAudioManager().abandonAudioFocus(null);
        state = STATE_IDLE;
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "state = " + state); // MODIFIED by yuxuan.zhang, 2016-06-08,BUG-1112693
        if (state == STATE_ALERTING) {
            stop();
            restoreToOriginalVolume(); // MODIFIED by yuxuan.zhang, 2016-06-12,BUG-2251957
        } else {
            super.onBackPressed();
        }
    }

    ;
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    /**
     * New fragment-style implementation of preferences.
     */
    //   public class CellBroadcastSettingsFragment extends PreferenceFragment {

    //[FEATURE]-MOD-BEGIN by TCTNB.yugang.jia, 09/07/2013, FR-516039,
    private class CellBroadcastSettingsFragment extends PreferenceFragment implements // MODIFIED by yuxuan.zhang, 2016-05-09,BUG-1112693
            Preference.OnPreferenceClickListener {
        //[FEATURE]-MOD-END by TCTNB.yugang.jia, 09/07/2013, FR-516039,

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            boolean showSpeechAlert = getResources().getBoolean(R.bool.def_showSpeechAlert);
            TLog.d(TAG, "onCreate CellBroadcastSettingsFragment  sPhoneId :" + sPhoneId);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            // Emergency alert preference category (general and CMAS preferences).
            PreferenceCategory alertCategory =
                    (PreferenceCategory) findPreference(KEY_CATEGORY_ALERT_SETTINGS);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
            PreferenceCategory otherCategory =
                    (PreferenceCategory) findPreference(KEY_CATEGORY_OTHER_SETTINGS);
                    /* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
            final CheckBoxPreference enablePwsAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_EMERGENCY_ALERTS);
            final ListPreference duration =
                    (ListPreference) findPreference(KEY_ALERT_SOUND_DURATION);
            final ListPreference interval =
                    (ListPreference) findPreference(KEY_ALERT_REMINDER_INTERVAL);
            final CheckBoxPreference enableChannel50Alerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CHANNEL_50_ALERTS);
            final CheckBoxPreference enableChannel60Alerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CHANNEL_60_ALERTS);
            final CheckBoxPreference enableEtwsAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_ETWS_TEST_ALERTS);
            final CheckBoxPreference enableCmasExtremeAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS);
            final CheckBoxPreference enableCmasSevereAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS);
            final CheckBoxPreference enableCmasAmberAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_AMBER_ALERTS);
            // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
            final CheckBoxPreference enableCmasSpanishLanguageAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS);
            // [BUGFIX]-MOD-END by bin.xue
            final CheckBoxPreference enableCmasTestAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_TEST_ALERTS);
            final CheckBoxPreference enableSpeakerAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_ALERT_SPEECH);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
            final CheckBoxPreference enableSpeakerAlertsEx =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_ALERT_SPEECH_EX);
                    /* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
            final CheckBoxPreference enableVibrateAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_ALERT_VIBRATE);
            
            // add by liang.zhang for Defect 5960218 at 2018-02-06 begin
            boolean isUAE = false;
            boolean isCanada = false; //add by liang.zhang for Defect 6353603 at 2018-06-02
            boolean isNZ = false; //add by liang.zhang for Defect 6369692 at 2018-06-07 
            SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
            List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info!= null && info.getMcc() == 424) {
        				isUAE = true;
        	        }
                    // add by liang.zhang for Defect 6353603 at 2018-06-02 begin
        			else if (info!= null && info.getMcc() == 302) {
        	        	isCanada = true;
        	        }
                    // add by liang.zhang for Defect 6353603 at 2018-06-02 end
        			// add by liang.zhang for Defect 6369692 at 2018-06-07 begin
        			else if (info!= null && info.getMcc() == 530) {
        				isNZ = true;
        	        }
        			// add by liang.zhang for Defect 6369692 at 2018-06-07 end
        		}
        	}
        	if (isUAE) {
        		alertCategory.removePreference(findPreference(KEY_ENABLE_ALERT_VIBRATE));
        	}
            // add by liang.zhang for Defect 5960218 at 2018-02-06 end
            
            final CheckBoxPreference enableAudioAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_ALERT_AUDIO);// aiyan-978029
            if (!getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
                alertCategory.removePreference(findPreference(KEY_ENABLE_ALERT_AUDIO));
            }
            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,01/21/2015,895849,[SCB][CMAS]Can't receive RMT&Exercise alerts
            final CheckBoxPreference enableCmasRMTAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_RMT_ALERTS);
             //add by yong.wang for Defect 7046076 at 2018-10-11 begin
            enableCmasRMTAlerts.setChecked(prefs.getBoolean(KEY_ENABLE_CMAS_RMT_ALERTS + sPhoneId,false));
            //add by yong.wang for Defect 7046076 at 2018-10-11 end
            final CheckBoxPreference enableCmasExerciseAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS);
            // add by liang.zhang for Defect 5955126 at 2018-02-08 begin
            enableCmasExerciseAlerts.setChecked(prefs.getBoolean(KEY_ENABLE_CMAS_EXERCISE_ALERTS + sPhoneId, false));
            enableCmasExerciseAlerts.setEnabled(!isUAE);
            enableCmasExtremeAlerts.setEnabled(!isUAE);
            enableCmasSevereAlerts.setEnabled(!isUAE);
            // add by liang.zhang for Defect 5955126 at 2018-02-08 end
            //[BUGFIX]-Add-END by TSCD.fujun.yang
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
            final CheckBoxPreference enableWpasTextAlerts =
                    (CheckBoxPreference) findPreference(KEY_ENABLE_WPAS_TEST_ALERTS);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1112693*/
            final NormalPreference enableWpasAlerts =
                    (NormalPreference) findPreference(KEY_ENABLE_WPAS_ALERTS);
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            
            // add by liang.zhang for Defect 6272370 at 2018-05-04 begin
            final NormalPreference wpasAlertsInbox = (NormalPreference) findPreference(KEY_WPAS_ALERTS_INBOX);
            wpasAlertsInbox.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), WPASAlertListActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
            // add by liang.zhang for Defect 6272370 at 2018-05-04 end
            
            final int idx = interval.findIndexOfValue(
                    (String) prefs.getString(KEY_ALERT_REMINDER_INTERVAL + sPhoneId,
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2854334*/
                            mWpasFlag ? WPAS_ALERT_REMINDER_INTERVAL_DEFAULT_DURATION
                                    /* MODIFIED-BEGIN by bin.huang, 2016-11-10,BUG-1112693*/
                                    : getResources().getString(R.string.def_alert_reminder_value)));
            interval.setSummary(interval.getEntries()[idx]);
            interval.setValue(prefs.getString(KEY_ALERT_REMINDER_INTERVAL
                    + sPhoneId, mWpasFlag ? WPAS_ALERT_REMINDER_INTERVAL_DEFAULT_DURATION
                    /* MODIFIED-END by yuxuan.zhang,BUG-2854334*/
                    : getResources().getString(R.string.def_alert_reminder_value)));
                    /* MODIFIED-END by bin.huang,BUG-1112693*/
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

            final int index = duration.findIndexOfValue(
                    (String) prefs.getString(KEY_ALERT_SOUND_DURATION + sPhoneId,
                            ALERT_SOUND_DEFAULT_DURATION));
            duration.setSummary(duration.getEntries()[index]);
            duration.setValue(prefs.getString(KEY_ALERT_SOUND_DURATION
                    + sPhoneId, ALERT_SOUND_DEFAULT_DURATION));
            enablePwsAlerts.setChecked(prefs.getBoolean(KEY_ENABLE_EMERGENCY_ALERTS
                    + sPhoneId, true));
            enableChannel50Alerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_CHANNEL_50_ALERTS + sPhoneId, true));
            //[FEATURE]-ADD-BEGIN by TSCD.tianming.lei,09/04/2014,715519
            boolean ssvEnable = "true".equals(SystemProperties.get("ro.ssv.enabled", "false"));
            if (ssvEnable) {
                String operator = SystemProperties.get("ro.ssv.operator.choose", "");
                if (operator.equals("TMO")) {
                    boolean tmo_50_enabled = getResources().getBoolean(R.bool.def_ssv_enable_local_50_channel);
                    TLog.i("ltm", "perso value:" + tmo_50_enabled);
                    enableChannel50Alerts.setChecked(prefs.getBoolean(
                            KEY_ENABLE_CHANNEL_50_ALERTS + sPhoneId, tmo_50_enabled));
                }
            }
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
            if (!mWpasFlag) {
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
                if (otherCategory != null) {
                    otherCategory.removePreference(enableSpeakerAlertsEx);
                    preferenceScreen.removePreference(otherCategory);
                }
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_WPAS_TEST_ALERTS));
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_WPAS_ALERTS));
                // add by liang.zhang for Defect 6272370 at 2018-05-04 begin
                alertCategory.removePreference(
                        findPreference(KEY_WPAS_ALERTS_INBOX));
                // add by liang.zhang for Defect 6272370 at 2018-05-04 end
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-20,BUG-1112693*/
                if (!getResources().getBoolean(R.bool.def_showPresidentialAlertOption)) {
                    alertCategory.removePreference(
                            findPreference(KEY_PRESIDENTIAL_ALERT_SETTINGS));
                }
            } else {
                alertCategory.removePreference(enableSpeakerAlerts);
                /* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
                alertCategory.removePreference(
                        findPreference(KEY_PRESIDENTIAL_ALERT_SETTINGS));
                        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                
                // add by liang.zhang for Defect 6288816 at 2018-05-04 begin
                preferenceScreen.removePreference(findPreference(KEY_CATEGORY_ETWS_SETTINGS));
                // add by liang.zhang for Defect 6288816 at 2018-05-04 end
                
                // add by liang.zhang for Defect 6353603 at 2018-06-02 begin
                if (isCanada) {
                	alertCategory.removePreference(findPreference(KEY_ENABLE_WPAS_TEST_ALERTS));
                }
                // add by liang.zhang for Defect 6353603 at 2018-06-02 begin
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            //[FEATURE]-ADD-END by TSCD.tianming.lei
            enableChannel60Alerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_CHANNEL_60_ALERTS + sPhoneId, true));
            enableEtwsAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_ETWS_TEST_ALERTS + sPhoneId,
                    getResources().getBoolean(R.bool.def_etws_test_alert_default_on)));//[BUGFIX]-ADD by TSNJ Yuanchang.Zhu PR-838662
            enableCmasExtremeAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + sPhoneId, true));
            enableCmasSevereAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + sPhoneId, true));
            /* MODIFIED-BEGIN by yuwan, 2017-06-16,BUG-4886195*/
            if (getResources().getBoolean(R.bool.def_extreme_severe_disable)) {
                if (!prefs.getBoolean(
                        KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + sPhoneId, true)) {
                    enableCmasSevereAlerts.setEnabled(false);
                } else {
                    enableCmasSevereAlerts.setEnabled(true);
                }
            }
            /* MODIFIED-END by yuwan,BUG-4886195*/
            enableCmasAmberAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_CMAS_AMBER_ALERTS + sPhoneId, true));
            // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
            enableCmasSpanishLanguageAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS + sPhoneId, false));
            // [BUGFIX]-MOD-END by bin.xue
            enableCmasTestAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_CMAS_TEST_ALERTS + sPhoneId, false));
            enableSpeakerAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_ALERT_SPEECH + sPhoneId, true));
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
            enableSpeakerAlertsEx.setChecked(prefs.getBoolean(
                    KEY_ENABLE_ALERT_SPEECH_EX + sPhoneId, true));
                    /* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
            enableVibrateAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_ALERT_VIBRATE + sPhoneId, true));
            enableAudioAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_ALERT_AUDIO + sPhoneId, true));// aiyan-978029
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
            enableWpasTextAlerts.setChecked(prefs.getBoolean(
                    KEY_ENABLE_WPAS_TEST_ALERTS + sPhoneId, false));
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            // Handler for settings that require us to reconfigure enabled channels in radio
            Preference.OnPreferenceChangeListener startConfigServiceListener =
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference pref, Object newValue) {
                            String value = String.valueOf(newValue);
                            SharedPreferences.Editor editor = prefs.edit();
                            Log.d(TAG, "preferece = " + pref + "  ------  value = " + value);
                            if (pref == enablePwsAlerts) {
                                editor.putBoolean(KEY_ENABLE_EMERGENCY_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableChannel50Alerts) {
                                editor.putBoolean(KEY_ENABLE_CHANNEL_50_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableChannel60Alerts) {
                                editor.putBoolean(KEY_ENABLE_CHANNEL_60_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableEtwsAlerts) {
                                editor.putBoolean(KEY_ENABLE_ETWS_TEST_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableCmasExtremeAlerts) {
                                editor.putBoolean(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableCmasSevereAlerts) {
                                editor.putBoolean(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableCmasAmberAlerts) {
                                editor.putBoolean(KEY_ENABLE_CMAS_AMBER_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                                // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
                            } else if (pref == enableCmasSpanishLanguageAlerts) {
                                TLog.d(TAG_CMAS, "enableCmasSpanishAlerts: " + Boolean.valueOf((value)));
                                editor.putBoolean(KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                                // [BUGFIX]-MOD-END by bin.xue
                            } else if (pref == enableCmasTestAlerts) {
                                editor.putBoolean(KEY_ENABLE_CMAS_TEST_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                                //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,01/21/2015,895849,[SCB][CMAS]Can't receive RMT&Exercise alerts
                            } else if (pref == enableCmasRMTAlerts) {
                                TLog.d(TAG_CMAS, "enableCmasRMTAlerts: " + Boolean.valueOf((value)));
                                editor.putBoolean(KEY_ENABLE_CMAS_RMT_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableCmasExerciseAlerts) {
                                TLog.d(TAG_CMAS, "enableCmasExerciseAlerts: " + Boolean.valueOf((value)));
                                editor.putBoolean(KEY_ENABLE_CMAS_EXERCISE_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
                            } else if (pref == enableWpasTextAlerts) {
                                TLog.d(TAG_CMAS, "enableWpasTextAlerts: " + Boolean.valueOf((value)));
                                editor.putBoolean(KEY_ENABLE_WPAS_TEST_ALERTS
                                        + sPhoneId, Boolean.valueOf((value)));
                                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                            }
                            //[BUGFIX]-Add-END by TSCD.fujun.yang
                            editor.commit();
                            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/05/2015,886284,[SCB][CMAS][ETWS]Can receive CMAS
                            //and ETWS even not enable "show ETWS/CMAS test broadcasts"
//                    CellBroadcastReceiver.startConfigService(pref.getContext(), sPhoneId);
                            CellBroadcastReceiver.startConfigServiceFromCMASSetting(pref.getContext(), sPhoneId);
                            //[BUGFIX]-Add-END by TSCD.fujun.yang

                            return true;
                        }
                    };

            //Listener for non-radio functionality
            Preference.OnPreferenceChangeListener startListener =
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference pref, Object newValue) {
                            String value = String.valueOf(newValue);
                            SharedPreferences.Editor editor = prefs.edit();

                            if (pref == enableSpeakerAlerts) {
                                editor.putBoolean(KEY_ENABLE_ALERT_SPEECH
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableVibrateAlerts) {
                                editor.putBoolean(KEY_ENABLE_ALERT_VIBRATE
                                        + sPhoneId, Boolean.valueOf((value)));
                            } else if (pref == enableAudioAlerts) {
                                editor.putBoolean(KEY_ENABLE_ALERT_AUDIO
                                        + sPhoneId, Boolean.valueOf((value)));// aiyan-978029
                            } else if (pref == interval) {
                                final int idx = interval.findIndexOfValue((String) newValue);

                                editor.putString(KEY_ALERT_REMINDER_INTERVAL + sPhoneId,
                                        String.valueOf(newValue));
                                interval.setSummary(interval.getEntries()[idx]);
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
                            } else if (pref == enableSpeakerAlertsEx) {
                                editor.putBoolean(KEY_ENABLE_ALERT_SPEECH_EX
                                        + sPhoneId, Boolean.valueOf((value)));
                                /* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
                            }
                            editor.commit();
                            return true;
                        }
                    };

            // Show extra settings when developer options is enabled in settings.
            boolean enableDevSettings = Settings.Global.getInt(getActivity().getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;

            Resources res = getResources();
            boolean showEtwsSettings = res.getBoolean(R.bool.show_etws_settings);

            // Show alert settings and ETWS categories for ETWS builds and developer mode.
            if (enableDevSettings || showEtwsSettings) {
                // enable/disable all alerts
                if (enablePwsAlerts != null) {
                    enablePwsAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
                }

                // alert sound duration
                duration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference pref, Object newValue) {
                        final int idx = duration.findIndexOfValue((String) newValue);
                        duration.setSummary(duration.getEntries()[idx]);
                        prefs.edit().putString(KEY_ALERT_SOUND_DURATION + sPhoneId,
                                String.valueOf(newValue)).commit();
                        return true;
                    }
                });
            } else {
                // Remove general emergency alert preference items (not shown for CMAS builds).
                alertCategory.removePreference(findPreference(KEY_ALERT_SOUND_DURATION));
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
                if (!mWpasFlag) {
                    alertCategory.removePreference(findPreference(KEY_ENABLE_EMERGENCY_ALERTS));
                    alertCategory.removePreference(findPreference(KEY_ENABLE_ALERT_SPEECH));
                }
                // Remove ETWS preference category.
                preferenceScreen.removePreference(findPreference(KEY_CATEGORY_ETWS_SETTINGS));
            }
            if (!getResources().getBoolean(R.bool.def_showAlertSoundDurationOption)) {
                alertCategory.removePreference(findPreference(KEY_ALERT_SOUND_DURATION));
            }
            //alertCategory.removePreference(findPreference(KEY_ALERT_REMINDER_INTERVAL));
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

            if (!res.getBoolean(R.bool.show_cmas_settings) && !mWpasFlag) { // MODIFIED by yuxuan.zhang, 2016-05-09,BUG-1112693
                // Remove CMAS preference items in emergency alert category.
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS));
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS));
                alertCategory.removePreference(findPreference(KEY_ENABLE_CMAS_AMBER_ALERTS));

                alertCategory.removePreference(
                        findPreference(KEY_PREVIEW_CMAS_ALERT_TONE));
                alertCategory.removePreference(
                        findPreference(KEY_PREVIEW_CMAS_VIBRATION));
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_CMAS_RMT_ALERTS));
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS));
            } else {
                if (!showMoreCMASMenu) {
                    alertCategory.removePreference(
                            findPreference(KEY_ENABLE_CMAS_RMT_ALERTS));
                    alertCategory.removePreference(
                            findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS));
                }
                CheckBoxPreference enableExtremeAlerts = (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS);
                CheckBoxPreference enableSevereAlerts = (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS);
                CheckBoxPreference enableAmberAlerts = (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_AMBER_ALERTS);

                enableExtremeAlerts.setOnPreferenceClickListener(this);
                enableSevereAlerts.setOnPreferenceClickListener(this);
                enableAmberAlerts.setOnPreferenceClickListener(this);

                Preference previewAlertTone = findPreference(KEY_PREVIEW_CMAS_ALERT_TONE);
                Preference previewVibration = findPreference(KEY_PREVIEW_CMAS_VIBRATION);

                previewAlertTone.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-09,BUG-1112693*/
                        //if (allowWpas) {
                        play();
                        return true;
                        //}
                        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
//                        cmasNotiPreviewer.playAlertTone();
//                        return true;
                    }
                });

                previewVibration.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-09,BUG-1112693*/
                        if (mWpasFlag) {
                            cmasNotiPreviewer.vibrateWpas();
                            return true;
                        }
                        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                        cmasNotiPreviewer.vibrate();
                        return true;
                    }
                });
            }
            //[FEATURE]-Add-END by TCTNB.yugang.jia, 09/07/2013, FR-516039,

            //[BUGFIX]-BEGIN by AMNJ.liujia, 11/26/2014, pr842839
            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/10/2015,886284,an receive CMAS and ETWS even not enable "show ETWS/CMAS test broadcasts"
            boolean isEnableRMTExceriseAlertType = getResources().getBoolean(R.bool.def_enableRMTExerciseTestAlert);
            android.util.Log.d(TAG, "isEnableRMTExceriseAlertType = " + isEnableRMTExceriseAlertType);
            if (!isEnableRMTExceriseAlertType || mWpasFlag) { // MODIFIED by yuxuan.zhang, 2016-06-28,BUG-2389849
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_CMAS_RMT_ALERTS));
                alertCategory.removePreference(
                        findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS));
            }
            //[BUGFIX]-Add-END by TSCD.fujun.yang

            //[BUGfix]-ADD-BEGIN-by-chaobing.huang,9/11/2015,PR1083982
            boolean isShowRMTExceriseAlertType = getResources().getBoolean(R.bool.def_showRMTExerciseTestAlert);
            android.util.Log.d(TAG, "isShowRMTExceriseAlertType = " + isShowRMTExceriseAlertType);
            if (!isShowRMTExceriseAlertType) {
                PreferenceCategory cmasAlertSettings = (PreferenceCategory) findPreference(KEY_CATEGORY_ALERT_SETTINGS);
                if (cmasAlertSettings != null) {
                    Preference cmasRmtAlerts = cmasAlertSettings.findPreference(KEY_ENABLE_CMAS_RMT_ALERTS);
                    Preference cmasExerciseAlerts = cmasAlertSettings.findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS);
                    if (cmasRmtAlerts != null && cmasExerciseAlerts != null) {
                        alertCategory.removePreference(
                                findPreference(KEY_ENABLE_CMAS_RMT_ALERTS));
                        alertCategory.removePreference(
                                findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS));
                    }
                }
            }
            //[BUGfix]-ADD-END-by-chaobing.huang,9/11/2015,PR1083982
            //[BUGFIX]-END by AMNJ.liujia, 11/26/2014, pr842839

            TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(
                    Context.TELEPHONY_SERVICE);

            boolean isMSim = TelephonyManager.getDefault().isMultiSimEnabled();
            String country = "";
            if (isMSim) {
                int mPhoneId = new Long(sPhoneId).intValue();
                int subscription[] = SubscriptionManager.getSubId(mPhoneId);
                TLog.d(TAG, "sPhoneId=" + sPhoneId + " subscription=" + subscription[0]);
                country = TelephonyManager.getDefault().getSimCountryIso(subscription[0]);
            } else {
                country = tm.getSimCountryIso();
            }
            boolean enableChannel50Support = res.getBoolean(R.bool.show_brazil_settings)
                    || "br".equals(country) || res.getBoolean(R.bool.show_india_settings)
                    || "in".equals(country) || res.getBoolean(R.bool.def_ssvShowChannel50Option_on);

            boolean enableChannel60Support = res.getBoolean(R.bool.show_india_settings)
                    || "in".equals(country);
            //[BUGFIX]-Add-BEGIN by bin.xue for PR1071520
            if (this.getResources().getBoolean(R.bool.def_cellbroadcastChannel50_disable)) {
                enableChannel50Support = false;
                TLog.i(TAG, "disableChannel50");
            }
            if (this.getResources().getBoolean(R.bool.def_cellbroadcastChannel60_disable)) {
                enableChannel60Support = false;
                TLog.i(TAG, "disableChannel60");
            }
            //[BUGFIX]-Add-END by bin.xue

            android.util.Log.d(TAG, "enableChannel50Support = " + enableChannel50Support
                    + "   def_ssvShowChannel50Option_on = " + res.getBoolean(R.bool.def_ssvShowChannel50Option_on));
            if (!enableChannel50Support) {
                preferenceScreen.removePreference(findPreference(KEY_CATEGORY_BRAZIL_SETTINGS));
            }

            if (!enableChannel60Support) {
                preferenceScreen.removePreference(findPreference(KEY_CATEGORY_INDIA_SETTINGS));
            }
            /*if (!enableDevSettings) {
                preferenceScreen.removePreference(findPreference(KEY_CATEGORY_DEV_SETTINGS));
            }*/

            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/10/2015,886284,an receive CMAS and ETWS even not enable "show ETWS/CMAS test broadcasts"
            if (isEnableRMTExceriseAlertType) {
                PreferenceCategory DevCategory =
                        (PreferenceCategory) findPreference(KEY_CATEGORY_DEV_SETTINGS);
                if (DevCategory != null && enableCmasTestAlerts != null) {
                    DevCategory.removePreference(enableCmasTestAlerts);
                }
            }
            //[BUGFIX]-Add-END by TSCD.fujun.yang

            if (enableChannel50Alerts != null) {
                enableChannel50Alerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            if (enableChannel60Alerts != null) {
                enableChannel60Alerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }

            if (enableEtwsAlerts != null) {
                enableEtwsAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            if (enableCmasExtremeAlerts != null) {
                enableCmasExtremeAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            if (enableCmasSevereAlerts != null) {
                enableCmasSevereAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            if (enableCmasAmberAlerts != null) {
                enableCmasAmberAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            if (enableCmasTestAlerts != null) {
                enableCmasTestAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }

            //Setting the listerner for non-radio functionality
            if (enableSpeakerAlerts != null) {
                enableSpeakerAlerts.setOnPreferenceChangeListener(startListener);
            }
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
            if (enableSpeakerAlertsEx != null) {
                enableSpeakerAlertsEx.setOnPreferenceChangeListener(startListener);
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
            if (enableVibrateAlerts != null) {
                enableVibrateAlerts.setOnPreferenceChangeListener(startListener);
            }
            if (enableAudioAlerts != null) {
                enableAudioAlerts.setOnPreferenceChangeListener(startListener);
            }// aiyan-978029
            if (interval != null) {
                interval.setOnPreferenceChangeListener(startListener);
            }
            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,01/21/2015,895849,[SCB][CMAS]Can't receive RMT&Exercise alerts
            if (enableCmasRMTAlerts != null) {
                enableCmasRMTAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            if (enableCmasExerciseAlerts != null) {
                enableCmasExerciseAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
            if (enableCmasSpanishLanguageAlerts != null) {
                enableCmasSpanishLanguageAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
            if (enableWpasTextAlerts != null) {
                enableWpasTextAlerts.setOnPreferenceChangeListener(startConfigServiceListener);
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            boolean isShowSpanishLanguageAlerts = getResources().getBoolean(R.bool.def_showSpanishLanguageAlerts);
            TLog.d(TAG, "def_showSpanishLanguageAlerts: " + isShowSpanishLanguageAlerts);
            if (!isShowSpanishLanguageAlerts) {
                alertCategory.removePreference(findPreference(KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS));
            }
            // [BUGFIX]-MOD-END by bin.xue
            //[BUGFIX]-Add-END by TSCD.fujun.yang
            //[BUGFIX]-Add by peng.guo, 2015-07-18,PR1045088 Begin
            //[BUGFIX] by chaobing.huang,9/2/2015,PR1077527 begin
            if (cmasNotiPreviewer != null && !cmasNotiPreviewer.isVibrationCapable()) {
                //[BUGFIX] by chaobing.huang,9/2/2015,PR1077527 end
                alertCategory.removePreference(findPreference(KEY_ENABLE_ALERT_VIBRATE));
                ((PreferenceCategory) findPreference(KEY_CATEGORY_CMAS_ALERT_PREVIEW)).removePreference(findPreference(KEY_PREVIEW_CMAS_VIBRATION));
            }
            //[BUGFIX]-Add by peng.guo, 2015-07-18,PR1045088 End
            if (!getResources().getBoolean(R.bool.def_showCmasAlertPreviewOption)) {
                preferenceScreen.removePreference(findPreference(KEY_CATEGORY_CMAS_ALERT_PREVIEW));
            }
            // PR1070443-chaobing.huang-001 begin
            boolean showEmergencyAlert = getResources().getBoolean(R.bool.def_emergencyAlert);
            TLog.d(TAG, "def_emergencyAlert: " + showEmergencyAlert);
            if (!showEmergencyAlert) {
                PreferenceCategory cmasAlertPreview = (PreferenceCategory) findPreference(KEY_CATEGORY_ALERT_SETTINGS);
                if (cmasAlertPreview != null) {
                    Preference alertEmergency = cmasAlertPreview.findPreference(KEY_ENABLE_EMERGENCY_ALERTS);
                    if (alertEmergency != null) {
                        cmasAlertPreview.removePreference(findPreference(KEY_ENABLE_EMERGENCY_ALERTS));
                    }
                }
            }
            // PR1070443-chaobing.huang-001 end
            // PR1070466-chaobing.huang-001 begin
            TLog.d(TAG, "def_showSpeechAlert: " + showSpeechAlert);
            if (!showSpeechAlert) {
                /* MODIFIED-BEGIN by bin.huang, 2016-11-04,BUG-3333029*/
                if (alertCategory != null) {
                    Preference alertSpeech = alertCategory.findPreference(KEY_ENABLE_ALERT_SPEECH);
                    if (alertSpeech != null) {
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-25,BUG-1112693*/
                        Log.i(TAG, "def_enable_wpas_function = " + mWpasFlag);
                        if (!mWpasFlag) {
                            alertCategory
                            /* MODIFIED-END by bin.huang,BUG-3333029*/
                                    .removePreference(findPreference(KEY_ENABLE_ALERT_SPEECH));
                        }
                        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    }
                }
            }
            // PR1070466-chaobing.huang-001 end
            //PR1070454-chaobing.huang-001  begin
            boolean showOptOutDialog = getResources().getBoolean(R.bool.def_showOptOutDialog);
            TLog.d(TAG, "def_showOptOutDialog:" + showOptOutDialog);
            if (!showOptOutDialog) {
                PreferenceCategory categoryDevSetting = (PreferenceCategory) findPreference(KEY_CATEGORY_DEV_SETTINGS);
                if (categoryDevSetting != null) {
                    Preference optOutDialog = categoryDevSetting.findPreference(KEY_SHOW_CMAS_OPT_OUT_DIALOG);
                    if (optOutDialog != null) {
                        categoryDevSetting.removePreference(findPreference(KEY_SHOW_CMAS_OPT_OUT_DIALOG));
                    }
                }
            }
            //PR1070454-chaobing.huang-001  end
            //PR1074758-chaobing.huang-001 begin
            boolean showETWSSettings = getResources().getBoolean(R.bool.def_showEtwsSettings);
            TLog.d(TAG, "def_showEtwsSettings:" + showETWSSettings);
            if (!showETWSSettings) {
                PreferenceCategory categoryEtwsSettings = (PreferenceCategory) findPreference(KEY_CATEGORY_ETWS_SETTINGS);
                if (categoryEtwsSettings != null) {
                    preferenceScreen.removePreference(findPreference(KEY_CATEGORY_ETWS_SETTINGS));
                }
            }
            boolean showDeveloperOptions = getResources().getBoolean(R.bool.def_showDeveloperOptions);
            TLog.d(TAG, "def_showDeveloperOptions:" + showDeveloperOptions);
            if (!showDeveloperOptions) {
                PreferenceCategory categoryDevSettings = (PreferenceCategory) findPreference(KEY_CATEGORY_DEV_SETTINGS);
                if (categoryDevSettings != null) {
                    preferenceScreen.removePreference(findPreference(KEY_CATEGORY_DEV_SETTINGS));
                }
            }
            //PR1074758-chaobing.huang-001 end

            // add by liang.zhang for Defect 5955126 at 2018-02-08 begin
            if (isUAE) {
            	alertCategory.removePreference(findPreference(KEY_PRESIDENTIAL_ALERT_SETTINGS));
            	alertCategory.removePreference(findPreference(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS));
            	alertCategory.removePreference(findPreference(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS));
            	alertCategory.removePreference(findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS));
            	enableCmasAmberAlerts.setTitle(R.string.uae_dialog_title_class4);
            	enableCmasAmberAlerts.setSummary("");
            	enableCmasRMTAlerts.setTitle(R.string.uae_dialog_title_class5);
            	enableCmasRMTAlerts.setSummary("");
            }
            // add by liang.zhang for Defect 5955126 at 2018-02-08 end
            
            // add by liang.zhang for Defect 6369692 at 2018-06-07 begin
            if (isNZ) {
            	alertCategory.removePreference(findPreference(KEY_ENABLE_ALERT_VIBRATE));
            	alertCategory.removePreference(findPreference(KEY_ENABLE_CMAS_AMBER_ALERTS));
            	alertCategory.removePreference(findPreference(KEY_ENABLE_CMAS_RMT_ALERTS));
            	alertCategory.removePreference(findPreference(KEY_ENABLE_CMAS_EXERCISE_ALERTS));
            	alertCategory.removePreference(findPreference(KEY_ALERT_REMINDER_INTERVAL));
            	
            	Preference presidential = findPreference(KEY_PRESIDENTIAL_ALERT_SETTINGS);
            	presidential.setTitle(R.string.enable_new_zealand_presidential_threat_alerts_title);
            	presidential.setSummary("");
            	presidential.setEnabled(false);
            	
            	Preference extreme = findPreference(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS);
            	extreme.setTitle(R.string.enable_new_zealand_extreme_threat_alerts_title);
            	extreme.setSummary("");
            	
            	Preference severe = findPreference(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS);
            	severe.setTitle(R.string.enable_new_zealand_severe_threat_alerts_title);
            	severe.setSummary("");
            }
            // add by liang.zhang for Defect 6369692 at 2018-06-07 end
        }

        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-27,BUG-1112693*/
        @Override
        public void onResume() {
            Log.i(TAG, "onResume");
            super.onResume();
            boolean showDeveloperOptions = getResources().getBoolean(
                    R.bool.def_showDeveloperOptions);
            if (showDeveloperOptions) {
                boolean showOptOutDialog = getResources().getBoolean(R.bool.def_showOptOutDialog);
                if (showOptOutDialog) {
                    PreferenceCategory categoryDevSetting = (PreferenceCategory) findPreference(KEY_CATEGORY_DEV_SETTINGS);
                    if (categoryDevSetting != null) {
                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(getActivity());
                        boolean pref = prefs.getBoolean(KEY_SHOW_CMAS_OPT_OUT_DIALOG, true);
                        if (pref) {
                            CheckBoxPreference optOutDialog = (CheckBoxPreference) categoryDevSetting
                                    .findPreference(KEY_SHOW_CMAS_OPT_OUT_DIALOG);
                            optOutDialog.setChecked(pref);
                        }
                    }
                }
            }
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

        //[FEATURE]-Add-BEGIN by TCTNB.yugang.jia, 09/07/2013, FR-516039,
        //warn the user when disable receive the CMAS message
        @Override
        public boolean onPreferenceClick(Preference preference) {
            //fix severe alerts can be enabled while extreme alerts are disabled issue
            final CheckBoxPreference pref = (CheckBoxPreference) preference;
            if (!pref.isChecked()) {
                /* MODIFIED-BEGIN by yuwan, 2017-06-16,BUG-4886195*/
                if (!getResources().getBoolean(R.bool.def_extreme_severe_disable)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(android.R.string.dialog_alert_title);
                    builder.setNegativeButton(R.string.disable_receive_cmas_dialog_btn_no,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pref.setChecked(true);
                                    TLog.d(TAG_CMAS, "user <canceled> the disable cmas message type: " + pref.getTitle());
                                    //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/14/2015,928215,CMAS]Emergency alert settings
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pref.getContext());
                                    SharedPreferences.Editor editor = prefs.edit();
                                    if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS)) {
                                        editor.putBoolean(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + sPhoneId, true);
                                        editor.commit();
                                        CellBroadcastReceiver.startConfigService(pref.getContext(),sPhoneId);
                                    } else if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_AMBER_ALERTS)) {
                                        editor.putBoolean(KEY_ENABLE_CMAS_AMBER_ALERTS + sPhoneId, true);
                                        editor.commit();
                                        CellBroadcastReceiver.startConfigService(pref.getContext(),sPhoneId);
                                    } else if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS)) {
                                        editor.putBoolean(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + sPhoneId, true);
                                        editor.commit();
                                        CellBroadcastReceiver.startConfigService(pref.getContext(),sPhoneId);
                                    }
                                    //[BUGFIX]-Add-END by TSCD.fujun.yang
                                }
                            });
                /* MODIFIED-BEGIN by bin.huang, 2016-11-08,BUG-3373995*/
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            pref.setChecked(true);
                            TLog.d(TAG_CMAS, "user <canceled2> the disable cmas message type: " + pref.getTitle());
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pref.getContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS)) {
                                editor.putBoolean(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + sPhoneId, true);
                                editor.commit();
                                CellBroadcastReceiver.startConfigService(pref.getContext(),sPhoneId);
                            } else if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_AMBER_ALERTS)) {
                                editor.putBoolean(KEY_ENABLE_CMAS_AMBER_ALERTS + sPhoneId, true);
                                editor.commit();
                                CellBroadcastReceiver.startConfigService(pref.getContext(),sPhoneId);
                            } else if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS)) {
                                editor.putBoolean(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + sPhoneId, true);
                                editor.commit();
                                CellBroadcastReceiver.startConfigService(pref.getContext(),sPhoneId);
                            }
                        }
                    });
                    final CheckBoxPreference enableCmasSevereAlerts =
                            (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS);
                    if (getResources().getBoolean(R.bool.feature_extreme_severe_correlated)
                            && pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS)
                        /* MODIFIED-END by bin.huang,BUG-3373995*/
                            && enableCmasSevereAlerts.isChecked()) {
                        // must not disable extreme alert with severe alert enabled, let user to choose
                        // disable both or just cancel operation
                        builder.setMessage(R.string.dialog_alert_msg_disable_extreme);
                        builder.setPositiveButton(R.string.disable_receive_cmas_dialog_btn_yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pref.setChecked(false);
                                        enableCmasSevereAlerts.setChecked(false);

                                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,09/17/2014,778720,[SS][CMAS]There are some wrong points on CMAS settings
                                        TLog.i(TAG, "disableCMASExtremeAlert-also-disableCMASSevereAlert--sPhoneId=" + sPhoneId);
                                        SharedPreferences prefs = PreferenceManager
                                                .getDefaultSharedPreferences(pref.getContext());
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putBoolean(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + sPhoneId, false);
                                        editor.commit();
                                        //[BUGFIX]-Add-END by TSCD.fujun.yang

                                        TLog.d(TAG_CMAS, "user <confirmed> disable cmas message: " + pref.getTitle());
                                        TLog.d(TAG_CMAS, "user <confirmed> disable cmas message: "
                                                + enableCmasSevereAlerts.getTitle());
                                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/05/2015,886284,[SCB][CMAS][ETWS]Can receive CMAS
                                        //and ETWS even not enable "show ETWS/CMAS test broadcasts"
//                                    CellBroadcastReceiver.startConfigService(pref.getContext());
                                        CellBroadcastReceiver.startConfigServiceFromCMASSetting(pref.getContext(), sPhoneId);
                                        //[BUGFIX]-Add-END by TSCD.fujun.yang
                                    }
                                });
                    } else {
                        builder.setMessage(R.string.dialog_alert_diable_receive_cmas_message);
                        builder.setPositiveButton(R.string.disable_receive_cmas_dialog_btn_yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pref.setChecked(false);
                                        TLog.d(TAG_CMAS, "user <confirmed> disable cmas message: " + pref.getTitle());
                                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/05/2015,886284,[SCB][CMAS][ETWS]Can receive CMAS
                                        //and ETWS even not enable "show ETWS/CMAS test broadcasts"
//                                    CellBroadcastReceiver.startConfigService(pref.getContext());
                                        CellBroadcastReceiver.startConfigServiceFromCMASSetting(pref.getContext(), sPhoneId);
                                        //[BUGFIX]-Add-END by TSCD.fujun.yang
                                    }
                                });
                    }

                /* MODIFIED-BEGIN by bin.huang, 2016-11-08,BUG-3373995*/
                    mDialog = builder.create();
                    mDialog.show();
                } else {
                    final CheckBoxPreference enableCmasSevereAlerts =
                            (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS);
                    if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS)
//                            && enableCmasSevereAlerts.isChecked()
                            ) {
                        pref.setChecked(false);
                        enableCmasSevereAlerts.setChecked(false);
                        TLog.i(TAG, "disableCMASExtremeAlert-also-disableCMASSevereAlert--sPhoneId=" + sPhoneId);
                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(pref.getContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + sPhoneId, false);
                        editor.commit();
                        TLog.d(TAG_CMAS, "user <confirmed> disable cmas message: " + pref.getTitle());
                        TLog.d(TAG_CMAS, "user <confirmed> disable cmas message: "
                                + enableCmasSevereAlerts.getTitle());
                        CellBroadcastReceiver.startConfigServiceFromCMASSetting(pref.getContext(), sPhoneId);
                        enableCmasSevereAlerts.setEnabled(false);
//                        enableCmasSevereAlerts.setShouldDisableView(true);
                    }
                }
            } else {
                if (!getResources().getBoolean(R.bool.def_extreme_severe_disable)) {
                    final CheckBoxPreference enableCmasExtremeAlerts =
                            (CheckBoxPreference) findPreference(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS);
                    if (getResources().getBoolean(R.bool.feature_extreme_severe_correlated)
                            && pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS)
                        /* MODIFIED-END by bin.huang,BUG-3373995*/
                            && !enableCmasExtremeAlerts.isChecked()) {
                        // must not enable severe alert with extreme disabled
                        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(R.string.dialog_alert_msg_enable_severe);

                        builder.setNegativeButton(R.string.disable_receive_cmas_dialog_btn_no,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pref.setChecked(false);
                                        TLog.d(TAG_CMAS, "user <canceled> the disable cmas message type: " + pref.getTitle());
                                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/14/2015,928215,CMAS]Emergency alert settings
                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pref.getContext());
                                        SharedPreferences.Editor editor = prefs.edit();
                                        if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS)) {
                                            editor.putBoolean(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + sPhoneId, false);
                                            editor.commit();
                                            CellBroadcastReceiver.startConfigService(pref.getContext());
                                        }
                                        //[BUGFIX]-Add-END by TSCD.fujun.yang
                                    }
                                });
                    /* MODIFIED-BEGIN by bin.huang, 2016-11-08,BUG-3373995*/
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                pref.setChecked(false);
                                TLog.d(TAG_CMAS, "user <canceled2> the disable cmas message type: " + pref.getTitle());
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pref.getContext());
                                SharedPreferences.Editor editor = prefs.edit();
                                if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS)) {
                                    editor.putBoolean(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + sPhoneId, false);
                                    editor.commit();
                                    CellBroadcastReceiver.startConfigService(pref.getContext());
                                }
                            }
                        });
                    /* MODIFIED-END by bin.huang,BUG-3373995*/

                        builder.setPositiveButton(R.string.disable_receive_cmas_dialog_btn_yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pref.setChecked(true);
                                        enableCmasExtremeAlerts.setChecked(true);

                                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,09/17/2014,778720,[SS][CMAS]There are some wrong points on CMAS settings
                                        TLog.i(TAG, "enableCMASSevereAlert-also-enableCMASExtremeAlert--sPhoneId=" + sPhoneId);
                                        SharedPreferences prefs = PreferenceManager
                                                .getDefaultSharedPreferences(pref.getContext());
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putBoolean(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + sPhoneId, true);
                                        editor.commit();
                                        //[BUGFIX]-Add-END by TSCD.fujun.yang

                                        TLog.d(TAG_CMAS, "user <confirmed> enable cmas message: " + pref.getTitle());
                                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/05/2015,886284,[SCB][CMAS][ETWS]Can receive CMAS
                                        //and ETWS even not enable "show ETWS/CMAS test broadcasts"
//                                    CellBroadcastReceiver.startConfigService(pref.getContext());
                                        CellBroadcastReceiver.startConfigServiceFromCMASSetting(pref.getContext(), sPhoneId);
                                        //[BUGFIX]-Add-END by TSCD.fujun.yang
                                    }
                                });

                    /* MODIFIED-BEGIN by bin.huang, 2016-11-08,BUG-3373995*/
                        mDialog = builder.create();
                        mDialog.show();
                    /* MODIFIED-END by bin.huang,BUG-3373995*/
                    } else {
                        pref.setChecked(true);
                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/05/2015,886284,[SCB][CMAS][ETWS]Can receive CMAS
                        //and ETWS even not enable "show ETWS/CMAS test broadcasts"
//                    CellBroadcastReceiver.startConfigService(pref.getContext());
//                    Log.d(TAG,"111");
//                    CellBroadcastReceiver.startConfigServiceFromCMASSetting(pref.getContext(),sPhoneId);
                        //[BUGFIX]-Add-END by TSCD.fujun.yang
                    }
                } else {
                    final CheckBoxPreference enableCmasSevereAlerts =
                            (CheckBoxPreference)
                                    findPreference(KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS);
                    if (pref.getKey().equalsIgnoreCase(KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS)
                            && !enableCmasSevereAlerts.isChecked()) {
                        pref.setChecked(true);
                        enableCmasSevereAlerts.setEnabled(true);
//                        enableCmasSevereAlerts.setShouldDisableView(false);
                    }
                    /* MODIFIED-END by yuwan,BUG-4886195*/
                }
            }
            return true;
        }
        //[FEATURE]-Add-END by TCTNB.yugang.jia
    }

    //[FEATURE]-ADD-BEGIN by TCTNB.yugang.jia, 09/11/2013, FR-516039
    // to preview the CMAS notification
    // Provide a helper class to preview the cmas notification,
    // include alert tone, vibration.
    class CMASNotificationPreviewer {
        private boolean isTonePlaying = false;
        private boolean isVibrating = false;
        // private boolean isVibrating = false;
        // Vibration uses the same on/off pattern as the CMAS alert tone
        private final long[] sVibratePattern = new long[]{
                0, 2000, 500, 1000, 500, 1000, 500, 2000, 500, 1000, 500, 1000, 500
        };

        private Vibrator mVibrator;
        private AudioManager mAudioManager;
        private ToneGenerator mToneGenerator = null;

        CMASNotificationPreviewer() {

            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }

        // start to play alert tone
        void playAlertTone() {
            if (isTonePlaying == true) {
                return;
            }

            int volumeIndex = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
            int maxVolumeIndex = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            int volume = volumeIndex * 100 / maxVolumeIndex;

            mToneGenerator = new ToneGenerator(AudioManager.STREAM_RING, volume);
            //[BUGFIX]-MOD-BEGIN BY TSNJ.shiqiang.xu,11/20/2014,PR-840712
            Log.w("yy", "TctWrapperManager.getTctToneCmas() = " + TctWrapperManager.getTctToneCmas()); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2854334
            mToneGenerator.startTone(TctWrapperManager.getTctToneCmas());
            //[BUGFIX]-MOD-BEGIN BY TSNJ.shiqiang.xu
            isTonePlaying = true;
            handler.sendEmptyMessageDelayed(MESSAGE_ID_TONE_FINISHED,
                    (int) (Float.parseFloat(ALERT_SOUND_DEFAULT_DURATION) * 1000));
        }

        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-09,BUG-1112693*/
        public AudioManager getAudioManager() {
            return mAudioManager;
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

        // start to vibrate
        void vibrate() {
            if (isVibrating) {
                return;
            }

            mVibrator.cancel();
            mVibrator.vibrate(sVibratePattern, -1);

            handler.sendEmptyMessageDelayed(MESSAGE_ID_VIBRATE_FINISHED,
                    (int) (Float.parseFloat(ALERT_SOUND_DEFAULT_DURATION) * 1000));
            isVibrating = true;
        }

        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-09,BUG-1112693*/
        void vibrateWpas() {
            if (isVibrating) {
                return;
            }

            mVibrator.cancel();
            mVibrator.vibrate(CellBroadcastAlertAudio.sWpasVibratePattern, -1);

            handler.sendEmptyMessageDelayed(MESSAGE_ID_VIBRATE_FINISHED,
                    CellBroadcastAlertAudio.EMERGENCY_SOUND_DURATION);
            isVibrating = true;
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

        // stop the tone and vibration
        void stopTone() {
            isTonePlaying = false;

            if (handler.hasMessages(MESSAGE_ID_TONE_FINISHED)) {
                handler.removeMessages(MESSAGE_ID_TONE_FINISHED);
            }

            if (mToneGenerator != null) {
                mToneGenerator.stopTone();
                mToneGenerator.release();
                mToneGenerator = null;
            }
        }

        void stopVibrate() {
            isVibrating = false;

            if (handler.hasMessages(MESSAGE_ID_VIBRATE_FINISHED)) {
                handler.removeMessages(MESSAGE_ID_VIBRATE_FINISHED);
            }

            // Stop vibrator
            mVibrator.cancel();
        }

        void stop() {
            stopTone();
            stopVibrate();
        }

        //[BUGFIX]-Add by peng.guo, 2015-07-18,PR1045088 Begin
        boolean isVibrationCapable() {
            return mVibrator != null && mVibrator.hasVibrator();
        }
        //[BUGFIX]-Add by peng.guo, 2015-07-18,PR1045088 Begin
    }

    @Override
    protected void onPause() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancel();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        restoreToOriginalVolume(); // MODIFIED by yuxuan.zhang, 2016-05-13,BUG-1112693
        if (cmasNotiPreviewer != null) {
            cmasNotiPreviewer.stop();
        }
        super.onDestroy();
    }
    //[FEATURE]-ADD-END by TCTNB.yugang.jia, 09/11/2013, FR-516039
}
