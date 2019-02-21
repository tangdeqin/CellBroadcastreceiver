/*
 * Copyright (C) 2011 The Android Open Source Project
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
/*
| ============================================================================ |
|      Modifications on Features list / Changes Request / Problems Report      |
| **************************************************************************** |
|    date   |        author        |         Key          |      comment       |
| **********|**********************|**********************|******************* |
| 08/29/2013|    Anming.Wei        |        467596        |porting cellbroadc  |
|           |                      |                      | ast feature from d |
|           |                      |                      | iablo to miata     |
| **********|**********************|**********************|******************* |
| 09/04/2013|    Anming.Wei        |        473254        |porting cellbroadc  |
|           |                      |                      | ast sdm for Customi|
|           |                      |                      | zedChannels        |
| **********|**********************|**********************|******************* |
| 09/06/2013|     yugang.jia       |      FR-516039       |[SMS]Cell broadc-   |
|           |                      |                      |ast SMS support     |
| ----------|----------------------|----------------------|--------------------|
| 01/15/2014|     Dandan.Fang      |       PR589769       |[Call] Device ge-   |
|           |                      |                      |ts warm (hot) du-   |
|           |                      |                      |ring call           |
| ----------|----------------------|----------------------|-----------------   |
| 07/16/2014|     tianming.lei     |      708134          |set default channel |
|           |                      |                      |for diffrent mccmnc |
| ----------|----------------------|----------------------|-----------------   |
| 07/22/2014|      tianming.lei    |         708134       |change the method   |
|           |                      |                      |to read mcc mnc     |
| ----------|----------------------|----------------------|-----------------   |
| 08/12/2014|      tianming.lei    |         752652       |After airplane mode,|
|           |                      |                      |mobile can't receive|
| ----------|----------------------|----------------------|-----------------   |
| 08/22/2014|      fujun.yang      |        772564        |new CLID variable   |
|           |                      |                      |to re-activate CB   |
|           |                      |                      |for NL              |
| ----------|----------------------|----------------------|-----------------   |
| 08/26/2014|      fujun.yang      |        775462        |[REG][SMS]Pop-up    |
|           |                      |                      |"Message has        |
|           |                      |                      |stopped" after      |
|           |                      |                      |enter into message  |
|           |                      |                      |settings and        |
|           |                      |                      |Pop-up "Cell        |
|           |                      |                      |broadcasts message  |
|           |                      |                      |has stopped" after  |
|           |                      |                      |power on            |
| ----------|----------------------|----------------------|-----------------   |
| 09/04/2014|     tianming.lei     |        715519        |DT variant for tmo  |
| ----------|----------------------|----------------------|-----------------   |
| 09/18/2014|      fujun.yang      |        779073        |[SDM][CellBroadcastR|
|           |                      |                      |eceiver]def_cellbroa|
|           |                      |                      |dcastreceiver_custom|
|           |                      |                      |ized_channel_names" |
|           |                      |                      |is invalid while    |
|           |                      |                      |it as               |
|           |                      |                      |"NL-Alert;NL-Info"  |
| ----------|----------------------|----------------------|-----------------   |
| 09/23/2014|      fujun.yang      |        795416        |after changed simcar|
|           |                      |                      |d,add perso control |
|           |                      |                      |the CB value.       |
| ----------|----------------------|----------------------|--------------------|
| 09/25/2014|     tianming.lei     |        797794        |[Cellbroadcast][SDM]|
|           |                      |                      | Can not cust CB    |
|           |                      |                      | channel on2C26     |
| ----------|----------------------|----------------------|-----------------   |
| 01/08/2014|     tianming.lei     |        879862        |The contents of Eme-|
|           |                      |                      |rgency Alerts item  |
|           |                      |                      |cannot restored to  |
|           |                      |                      |default settings    |
| ----------|----------------------|----------------------|-----------------   |
| 02/05/2015|      fujun.yang      |        886284        |[SCB][CMAS][ETWS]Can|
|           |                      |                      |receive CMAS and    |
|           |                      |                      |ETWS even not       |
|           |                      |                      |enable "show        |
|           |                      |                      |ETWS/CMAS test      |
|           |                      |                      |broadcasts"         |
| ----------|----------------------|----------------------|--------------------|
| 03/25/2015|     pingzhi.wang     |        949742        |[SMS]The Cell broadc|
|           |                      |                      |ast settings can not|
|           |                      |                      |restore default sett|
|           |                      |                      |ings successfully   |
| ----------|----------------------|----------------------|--------------------|
================================================================================
*/

package com.android.cellbroadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
//[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,09/18/2014,779073,[SDM][CellBroadcastReceiver]
//def_cellbroadcastreceiver_customized_channel_names"is invalid while it as "NL-Alert;NL-Info"
import android.net.Uri;
import android.os.Bundle;
//[BUGFIX]-Add-END by TSCD.fujun.yang
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.CellBroadcastMessage;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SmsCbMessage;
import android.telephony.SubscriptionInfo; // MODIFIED by yuxuan.zhang, 2016-09-18,BUG-2854327
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaSmsCbProgramData;
import android.app.NotificationManager;
//[FEATURE]-begin-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
//[SMS]Cell broadcast SMS support
import android.provider.Settings;
//import mediatek.telephony.MtkSmsManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.IccCardConstants;

// add by liang.zhang for Defect 6012945 at 2018-03-07 begin
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
// add by liang.zhang for Defect 6012945 at 2018-03-07 end

//[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,07/11/2013,PR485579,
//Can not receive channel 4371
//import android.telephony.PhoneStateListener;
import android.provider.Settings;

import com.android.internal.telephony.TelephonyIntents;

import android.os.SystemProperties;
//[BUGFIX]-Add-END by TCTNB.Dandan.Fang
//[FEATURE]-ADD-BEGIN by TSCD.tianming.lei,07/22/2014,708134
//import android.os.SsvManager;
//[FEATURE]-ADD-END by TSCD.tianming.lei

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
//[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/06/2013,FR-400302,
//[SMS]Cell broadcast SMS support
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsCbConstants; // MODIFIED by yuxuan.zhang, 2016-09-20,BUG-2845457

import android.content.ContentValues;

import com.android.cellbroadcastreceiver.CellBroadcast.Channel;
import com.android.cellbroadcastreceiver.CellBroadcast.CBLanguage;
//[FEATURE]-Add-END by TCTNB.bo.xu

//[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,07/20/2013,PR477924
//[CB]After airplane mode, mobile can not receive CMAS message from 4371
import com.android.internal.telephony.ISms;
//[BUGFIX]-Add-END by TCTNB.Dandan.Fang
//[FEATURE]-Add-BEGIN by TSCD.fujun.yang,08/22/2014,772564,new CLID variable to re-activate CB for NL
import com.android.internal.telephony.TelephonyProperties;
//[FEATURE]-Add-END by TSCD.fujun.yang

import com.android.internal.telephony.PhoneConstants;

import android.telephony.SubscriptionManager;
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-05-06,BUG-1112693

import com.android.cb.util.TLog;
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-1112693*/
import com.tct.util.FwkPlf;
import com.tct.util.IsdmParser;
/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
import com.tct.wrapper.TctWrapperManager;

// add by liang.zhang for Defect 5317399 at 2017-09-19 begin
import android.database.Cursor;
//add by liang.zhang for Defect 5317399 at 2017-09-19 end

public class CellBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "CellBroadcastReceiver";
    static final boolean DBG = true;    // STOPSHIP: change to false before ship

    private static final String GET_LATEST_CB_AREA_INFO_ACTION =
            "android.cellbroadcastreceiver.GET_LATEST_CB_AREA_INFO";
    //[BUGFIX]-MOD-BEGIN by TSCD,tianming.lei 01/08/2015,PR-879862
    //private static long mSubscription = SubscriptionManager.DEFAULT_SUB_ID;
    private static long mSubscription = PhoneConstants.SUB1;
    //[BUGFIX]-MOD-END by TSCD,tianming.lei
    //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/10/2013,FR-400968,
    //Customize SMS cb channel list in CLID
    public static final String PREFS_NAME = "com.android.cellbroadcastreceiver_preferences"; // MODIFIED by yuxuan.zhang, 2016-05-06,BUG-1112693
    public static final String CONFIG_RESET_CBNV_VELUE = "config_reset_cbnv_value";
    //[BUGFIX]-Add by TCTNB.bo.xu,06/15/2013,CR-451418,
    //Set dedicated Cell broadcast MI for Israel Programs
    //[BUGFIX]-Add by TCTNB.bo.xu,06/15/2013,CR-451418,
    //Set dedicated Cell broadcast MI for Israel Programs
    //[FEATURE]-begin-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
    //Can not receive channel 4371
    //[BUGFIX]-Mod-BEGIN by TCTNB.Dandan.Fang,01/15/2014,PR589769,
    //[Call] Device gets warm (hot) during call
    //private int mServiceState = -1;
    private static int mServiceState = -1;
    //[BUGFIX]-Mod-END by TCTNB.Dandan.Fang
    private boolean cbEnabled = false ;
    private String channelMode = "1";
    static final String KEY_LAST_RECEIVED_MESSAGE_IDS = "key_last_received_message_ids";
    static final String KEY_LAST_SIM_CARD_SERIAL_NUMBER = "key_last_sim_card_serial_number";
    //[FEATURE]-Add-END by TCTNB.bo.xu
    //[FEATURE]-end-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
    //used to check normal CB is enabled or disabled, default is disabled.
    //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,08/12/2013,501208,
    //[HOMO][HOMO]Cell Broadcast messages realization for qualcomm/MTK/Broadcom/Spr-
    //eadtrum/etc android smartphones
    public static final String CB_ENABLED = "cb_enabled";
    public static final String CONFIG_CB_SET_DEFAULT = "config_cb_set_default";
    //[BUGFIX]-Add-END by TCTNB.Dandan.Fang

    //[FEATURE]-ADD-BEGIN by TSCD.tianming.lei,07/16/2014,708134
    public static final String PRE_MCC_MNC = "pref_key_mcc_mnc";
    public static final String PRE_RESET_CHANNEL = "reset_channel";
    //[FEATURE]-ADD-END by TSCD.tianming.lei
    public static final String IS_FIRST_BOOT = "is_first_boot";
    private boolean mEnableSingleSIM = false;
    private static boolean appNeedChange = false;
    //PR 1054793 Added by fang.song begin
    private String channelIndex_4371 = "4371";
    private String channelName_4371 = "NL-Alert";
    private String channelEnable_4371 = "ENABLE";
    protected String mDefaultMode = "1"; // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
    protected boolean mChannelExFlag = false;
    //PR 1054793 Added by fang.song end
    private static final int NOTIFICATION_ID = 1;
    @Override
    public void onReceive(Context context, Intent intent) {
        onReceiveWithPrivilege(context, intent, false);
    }

  //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/15/2014,772564,new CLID variable to re-activate CB for NL
  //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,08/22/2014,772564,new CLID variable to re-activate CB for NL
  //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,08/26/2014,775462,[REG][SMS]Pop-up "Message has stopped" after
    //enter into message settings and Pop-up "Cell broadcasts message has stopped" after power on
    public boolean isHollandSimCard(Context mContext ,long phoneId){
        boolean mIsNLSimCard = false;
        if(TelephonyManager.getDefault().isMultiSimEnabled()){
            //modify by yong.wang for defect-7134121 at 181126 start
            int mPhoneId = new Long(phoneId).intValue();
            //int mPhoneId = new Long(slotId).intValue();
            //if(mEnableSingleSIM && mPhoneId ==1){
            //    return false;
            //}
           //modify by yong.wang for defect-7134121 at 181126 end
            /* MODIFIED-BEGIN by bin.huang, 2016-11-03,BUG-3319271*/
            int subscription[] = SubscriptionManager.getSubId(mPhoneId);
            if(subscription != null){
                TLog.d(TAG,"subscription="+subscription[0]);
                TelephonyManager mmanger = TelephonyManager.getDefault();
                String numeric = mmanger.getSimOperator(subscription[0]);
                TLog.i(TAG,"multisimcard-isHollandSimCard="+numeric);
                if(numeric != null && numeric.length() > 4){
                    String sub = numeric.subSequence(0, 3).toString();
                    TLog.i(TAG,"mcc="+sub);
                    if(sub.equals("204")){
                        mIsNLSimCard = true;
                    }
                    /* MODIFIED-END by bin.huang,BUG-3319271*/
                }
            }
        }else{
            TelephonyManager manger = TelephonyManager.getDefault();
            String numeric = manger.getSimOperator();
            TLog.i(TAG,"isHollandSimCard="+numeric);
            if(numeric != null && numeric.length() > 4){
                String sub1 = numeric.subSequence(0, 3).toString();
                TLog.i(TAG,"mcc="+sub1);
                if(sub1.equals("204")){
                    mIsNLSimCard = true;
                }
            }
        }
        return mIsNLSimCard;
    }
  //[BUGFIX]-Add-END by TSCD.fujun.yang
  //[FEATURE]-Add-END by TSCD.fujun.yang
  //[FEATURE]-Add-END by TSCD.fujun.yang

    //[BUGFIX]-Remove-BEGIN by TSCD.Tianming.lei,09/25/2014,797794
    /*
  //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,09/18/2014,779073,[SDM][CellBroadcastReceiver]
    //def_cellbroadcastreceiver_customized_channel_names"is invalid while it as "NL-Alert;NL-Info"
    public boolean isCustomizedChannelConfigured(Context mContext){
        boolean mIsCustomizedChannel = false;
        String persoChannelNames = mContext.getResources().getString(
                R.string.def_cellbroadcastreceiver_customized_channel_names);
        String persoChannelNumbersAndPolicy = mContext.getResources().getString(
                R.string.def_cellbroadcastreceiver_customized_channel_numbers_policy);
        int namesLen = persoChannelNames.trim().length();
        int numbersLen = persoChannelNumbersAndPolicy.trim().length();
        TLog.i(TAG, "isCustomizedChannelConfigured-channlename="+persoChannelNames);
        TLog.i(TAG,"isCustomizedChannelConfigured-channelnumber="+persoChannelNumbersAndPolicy);
        if(namesLen != 0 && numbersLen != 0){
            mIsCustomizedChannel = true;
        }
        return mIsCustomizedChannel;
    }
  //[BUGFIX]-Add-END by TSCD.fujun.yang
   */
    //[BUGFIX]-Remove-END by TSCD.Tianming.lei

    protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
        if (DBG) log("onReceive " + intent);
        //[FEATURE]-begin-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
        //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
        //[FEATURE]-begin-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
        //Set dedicated Cell broadcast MI for Israel Programs
        String action = intent.getAction();
        //[BUGFIX]-Add-BEGIN by TCTNB.yugang.jia,01/07/2013,PR-582740
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
        Log.i(TAG,"onReceive action:"+action);
        //[BUGFIX]-Add-END by TCTNB.yugang.jia,01/07/2013,PR-582740
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
        mDefaultMode = context.getResources().getString(R.string.def_is_all_channel_mode_cellbroadcast);
        mChannelExFlag = context.getResources().getBoolean(R.bool.def_expand_normal_cb_channel); // MODIFIED by yuxuan.zhang, 2016-09-20,BUG-2845457
        SharedPreferences mSettings = context.getSharedPreferences(PREFS_NAME, 0);
            cbEnabled = mSettings.getBoolean("cb_enabled",false);
            channelMode = mSettings.getString("pref_key_choose_channel", mDefaultMode);
            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
        //[FEATURE]-end-Add by TCTNB.yugang.jia,09/06/2013,FR-516039
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-1112693*/
        mEnableSingleSIM = IsdmParser.getBooleanFwk(context,
                FwkPlf.def_cellbroadcast_enable_single_sim, false);
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        boolean isBrazil50CbSupported = context.getResources().getBoolean(R.bool.show_brazil_settings);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
        Log.i(TAG, "mEnableSingleSIM:" + mEnableSingleSIM + ";mChannelExFlag = " + mChannelExFlag
                + ";isBrazil50CbSupported = " + isBrazil50CbSupported);
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        // modify by liang.zhang for Defect 5769404 at 2017-12-27 begin
        if ("android.provider.Telephony.SMS_STATE_CHANGED".equals(action)) {
        	boolean isReady = intent.getBooleanExtra("ready", false);
        	if (DBG) {
        		Log.i(TAG, "SMS_STATE_CHANGED_ACTION" + isReady + "  " + intent.getIntExtra("simId", -1));
        	}
        	
            if (isReady) {
            	// add by liang.zhang for Defect 5852515 at 2018-01-15 begin
                SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            	CBSUtills utils = new CBSUtills(context);
            	
//            	MtkSmsManager manager = null;
                int subId = 0;
                SubscriptionInfo  subInfoRecord;
            	if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++){
                        //modify by yong.wang for defect-7134121 at 181126 start
                        final SubscriptionInfo sir = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
                        if (sir == null) {
                        	continue;
                        }
                        subId= sir.getSubscriptionId();
                        startConfigService(context, i);
                        // subInfoRecord = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
                        // if(subInfoRecord != null) {
                        //     subId= subInfoRecord.getSubscriptionId();
                        //     Log.i(TAG, "get subId " + i + " = "+subId);
                        // }
	       //modify by yong.wang for defect-7134121 at 181126 end
                        startConfigCB(context,subId, i);
                    }
                } else {
                    startConfigService(context);
//                    manager = MtkSmsManager.getDefault();
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                    Log.i(TAG, "get subId = " + subId);
                    startConfigCB(context,subId, PhoneConstants.SUB1);
                }
            	// add by liang.zhang for Defect 5852515 at 2018-01-15 end
            }
        } else
        // modify by liang.zhang for Defect 5769404 at 2017-12-27 end
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            /*
            if (DBG) log("Registering for ServiceState updates");

            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                TelephonyManager tm = TelephonyManager.getDefault();
                int numPhones = tm.getPhoneCount();
                for (int i = 0; i < numPhones; i++) {
                    tm.listen(new ServiceStateListener(context.getApplicationContext(), i),
                            PhoneStateListener.LISTEN_SERVICE_STATE);
                }
            } else {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(
                        Context.TELEPHONY_SERVICE);
                tm.listen(new ServiceStateListener(context.getApplicationContext()),
                        PhoneStateListener.LISTEN_SERVICE_STATE);
                }*/
            //[FEATURE]-begin-mod by TCTNB.yugang.jia,09/06/2013,FR-516039,
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            // add by liang.zhang for Defect 5163407 at 2017-09-01 begin
            if ("true".equals(SystemProperties.get("ro.cota.enable")) && "cota".equals(SystemProperties.get("ro.poweron.reason"))) {
            	SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(CONFIG_CB_SET_DEFAULT, true);
                editor.putBoolean(CONFIG_RESET_CBNV_VELUE, true);
                editor.commit();
            }
            // add by liang.zhang for Defect 5163407 at 2017-09-01 end
            boolean enableCBValue = settings.getBoolean(CONFIG_CB_SET_DEFAULT, true);
            Log.i(TAG, "enableCBValue = "+enableCBValue); // MODIFIED by yuxuan.zhang, 2016-09-29,BUG-2845457
            //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia,01/10/2014,525219,
            if (enableCBValue){
                 // at last, set check flag
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(CONFIG_CB_SET_DEFAULT, false);
                editor.commit();
                setCustomizedChannels(context);
            }
            //[BUGFIX]-Add-BEGIN by TSCD.tianming.lei,02/05/2015, PR-901210
            boolean isFirstBoot = settings.getBoolean(IS_FIRST_BOOT, true);
            if(isFirstBoot){
                initEmergencyPref(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(IS_FIRST_BOOT, false);
                editor.commit();
                 //add by yong.wang for Defect P10024221  at 2018-10-16 begin
                android.provider.Settings.System.putString(context.getContentResolver(),
                            "CBLightEnable", context.getResources().getBoolean(R.bool.feature_enable_cb_light_up_screen_in_cb_setting) ? "on" : "off");
                //add by yong.wang for Defect P10024221  at 2018-10-16 end
                //add by deqin.tang for Defect 7107664  at 2018-11-15 begin
                android.provider.Settings.System.putString(context.getContentResolver(),
                        "CBLedEnable", context.getResources().getBoolean(R.bool.feature_enable_cb_led_indicator_in_cb_setting) ? "on" : "off");
                //add by deqin.tang for Defect 7107664  at 2018-11-15 end
            }
            //[BUGFIX]-Add-END by TSCD.tianming.lei,02/05/2015, PR-901210

            //[BUGFIX]-Remove-BEGIN by TSCD.Tianming.lei,09/25/2014,797794
            /*
            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,09/18/2014,779073,[SDM][CellBroadcastReceiver]
            //def_cellbroadcastreceiver_customized_channel_names"is invalid while it as "NL-Alert;NL-Info"
            if(isCustomizedChannelConfigured(context)){
                TLog.i(TAG, "isCustomizedChannelConfigured");
                setCustomizedChannels(context);
            }
          //[BUGFIX]-Add-END by TSCD.fujun.yang
           */
            //[BUGFIX]-Remove-BEGIN by TSCD.Tianming.lei,09/25/2014,797794
            //[BUGFIX]-MOD-END by TCTNB.yugang.jia,01/10/2014,525219,
            //[FEATURE]-Add-END by TSNJ.Anming.Wei
        //[FEATURE]-end-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
            if (SystemProperties.getBoolean("ro.ssv.enabled", false)){
                String preMccMnc = settings.getString(PRE_MCC_MNC, "");
                String nowMccMnc = SystemProperties.get("persist.sys.lang.mccmnc", "");
                if(nowMccMnc != null && !nowMccMnc.equals("") && !nowMccMnc.equals(preMccMnc)){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-08,BUG-2219480*/
                    Log.i(TAG, "sim changed need to reset channel list");
                    appNeedChange = true;
                    settings.edit().putString(PRE_MCC_MNC, nowMccMnc).apply();
                    settings.edit().putBoolean(CONFIG_RESET_CBNV_VELUE, true).apply();
                    setCustomizedChannels(context);
                }else{
                    Log.i(TAG, "no need to reset channel list");
                    /* MODIFIED-END by yuxuan.zhang,BUG-2219480*/
                }
                /*
                if (SsvManager.getInstance().isAppNeedChange("cb")){
                    setCustomizedChannels(context);
                }
                */
            }
            // removed by liang.zhang for Defect 5968000 at 2018-04-20 begin
//            intent.setClass(context, CellBroadcastAlertService.class);
//            context.startService(intent);
            // remove by liang.zhang for Defect 5968000 at 2018-04-20 end
        } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            boolean airplaneModeOn = intent.getBooleanExtra("state", false);
            if (DBG) log("onReceive is AIRPLANE_MODE_CHANGED airplaneModeOn: " + airplaneModeOn);
            if (!airplaneModeOn) {
                /* if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                    for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++){
                        startConfigService(context, i);
                    }
                } else {
                    startConfigService(context);
                } */
                Log.d(TAG,"onReceive is airplane off then do nothing!"); // MODIFIED by yuxuan.zhang, 2016-06-08,BUG-2219480
            }
            //[BUGFIX]-Mod-BEGIN by TCTNB.tianming.lei,08/04/2014,PR752652
            //[BUGFIX]-Add-BEGIN by TCTNB.yugang.jia,01/08/2014,PR582740
            //[CB]After airplane mode, mobile can not receive CMAS message.
            else {
//                WrapManager.getInstance().setCellBroadcastRangesEmpty() ;
                /*try {
                    ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService("isms"));
                    if (iccISms != null) {
                        TLog.d(TAG,"onReceive is airplane on then setRangesEmpty!");
                        iccISms.tct_setRangesEmpty();
                   }
                } catch (RemoteException ex) {
                  // ignore it
                }*/
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-21,BUG-2384431*/
                try {
                    TctWrapperManager.tctSetRangesEmpty();
                } catch (Throwable e) {
                    Log.e(TAG, "method tct_setRangesEmpty() can not be found");
                }
                /* MODIFIED-END by yuxuan.zhang,BUG-2384431*/
            }
            //[BUGFIX]-Add-END by TCTNB.yugang.jia,01/08/2014,PR582740
            //[BUGFIX]-Mod-BEGIN by TCTNB.tianming.lei
        } else if (Telephony.Sms.Intents.SMS_EMERGENCY_CB_RECEIVED_ACTION.equals(action) ||
                Telephony.Sms.Intents.SMS_CB_RECEIVED_ACTION.equals(action)) {
            // If 'privileged' is false, it means that the intent was delivered to the base
            // no-permissions receiver class.  If we get an SMS_CB_RECEIVED message that way, it
            // means someone has tried to spoof the message by delivering it outside the normal
            // permission-checked route, so we just ignore it.
            //[BUGFIX]-PR1006417-chenglin.jiang-001 add Begin
            int persoRule = context.getResources().getInteger(R.integer.def_telephony_CBMessage_Filter);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
            boolean allowWpas = context.getResources().getBoolean(R.bool.def_enable_wpas_function);
            // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
            if (!CBSUtills.isCanadaSimCard(context)) {
            	allowWpas = false; 
            }
            // add by liang.zhang for Defect 6929849 at 2018-09-01 end
            //PR 1054793 Added by fang.song begin
            Bundle extras = intent.getExtras();
            SmsCbMessage message = (SmsCbMessage) extras.get("message");
            if(allowWpas){
                allowWpas = CellBroadcastResources.checkIsWpasMessage(new CellBroadcastMessage(message));
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
			//modify by yong.wang for defect-7134121 at 181126 start
            //int defaultSubId = SubscriptionManager.getDefaultSmsSubscriptionId();
            int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY, PhoneConstants.SUB1);
			//modify by yong.wang for defect-7134121 at 181126 end
            //[add]-begin-by-chaobing.huang-01132017-defect4007891
            //boolean shouldReceive4371 = "true".equalsIgnoreCase(SystemProperties.get(CBSUtills.PRE_DEFINE_CHANNEL_4371, "false")) && message.getServiceCategory() == 4371 && isHollandSimCard(context,phoneId);
            boolean shouldReceive4371 = context.getResources().getBoolean(R.bool.ro_cb_prechannel4371) && message.getServiceCategory() == 4371 && isHollandSimCard(context,phoneId);
            //[add]-end-by-chaobing.huang-01132017-defect4007891
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            Log.w(TAG, "shouldReceive4371 = " + shouldReceive4371 + ", and phoneId = " + phoneId);
          //[BUGFIX]PR 1104825 chaobing.huang -BEGIN
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean enableCmasSpanishAlerts = prefs.getBoolean(
                CellBroadcastSettings.KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS + phoneId, false);
            boolean isEnableCmasSpanish = context.getResources().getBoolean(R.bool.def_isEnableCmasSpanish);
            Log.w(TAG,"enableCmasSpanishAlerts = "+enableCmasSpanishAlerts+",isEnableCmasSpanish = "+isEnableCmasSpanish);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
            if (message != null) {
                Log.w(TAG, "category = " + message.getServiceCategory());
            } else {
                Log.w(TAG, "message = null");
            }

            // add by liang.zhang for Defect 5945459 at 2018-02-13 begin
            boolean isReceivedOnlyIn2G = context.getResources().getBoolean(R.bool.feature_onlyReceiveCbMessageIn2G_on);
            Log.d(TAG,"isReceivedOnlyIn2G = "+isReceivedOnlyIn2G);
            if(isReceivedOnlyIn2G){
            	TelephonyManager manager = TelephonyManager.getDefault();
            	SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            	List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
            	
            	boolean is72411Sim = false;
            	if (subList != null && subList.size() > 0) {
            		for (int i = 0; i < subList.size(); i++) {
            			SubscriptionInfo info = subList.get(i);
            			if (info!= null && info.getMcc() == 724 && info.getMnc() == 11) {
            				is72411Sim = true;
            	        }
            		}
            	}
            	
            	int [] subId = SubscriptionManager.getSubId(phoneId);
                if(subId != null && subId.length > 0){
                    int voicenetworkType = manager.getVoiceNetworkType(subId[0]);
                    int networkClass = manager.getNetworkClass(voicenetworkType);
                    Log.d(TAG,"subId = " + subId[0]);
                    Log.d(TAG,"voicenetworkType = "+voicenetworkType);
                    Log.d(TAG,"networkClass = "+networkClass);
                    if(is72411Sim && networkClass != TelephonyManager.NETWORK_CLASS_2_G){
                        Log.d(TAG, "current is not in 2G, skip the recieving message");
                        return;
                    }
                }
            }
            // add by liang.zhang for Defect 5945459 at 2018-02-13 end
            
            // add by liang.zhang for Defect 6012945 at 2018-03-07 begin
            boolean isPeru = false;
            boolean isMexico = false;
            boolean isChile = false;
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info != null && info.getMcc() == 716) {
                    	isPeru = true;
                    } else if (info != null && info.getMcc() == 730) {
                    	isChile = true;
                    } else if (info != null && info.getMcc() == 334) {
                    	isMexico = true;
                    }
        		}
        	}
        	
        	Locale curLocale = context.getResources().getConfiguration().locale;
            Log.v(TAG, "curLocale: " + curLocale);
            if ((curLocale.toString()).startsWith("en")) {
            	if (isPeru) {
            		if (message.getServiceCategory() == 4370) {
            			return;
            		}
            	}
            	if (isMexico) {
            		String[] channels = {"4370","4371","4372","4373","4374","4375","4376","4377","4378"};
            		List<String> channellist = Arrays.asList(channels);
            		if (channellist.contains(message.getServiceCategory() + "")) {
            			return;
            		}
            	}
            }
            
            if ((curLocale.toString()).startsWith("es")) {
            	if (isPeru) {
            		if (message.getServiceCategory() == 4383) {
            			return;
            		}
            	}
            	if (isMexico) {
            		String[] channels = {"4383","4384","4385","4386","4387","4388","4389","4390","4391"};
            		List<String> channellist = Arrays.asList(channels);
            		if (channellist.contains(message.getServiceCategory() + "")) {
            			return;
            		}
            	}
            }
            
            if (isChile) {
            	if (message.getServiceCategory() != 4370) {
        			return;
        		}
            }
            

            if (!(isPeru || isMexico || isChile)) {
            	if (!enableCmasSpanishAlerts && !isEnableCmasSpanish
                        && (message.getServiceCategory() == 4383 && !allowWpas)) {
                    CellBroadcastSettings.cbIntent = intent;
                    Log.w(TAG,"Ignore SMS CMAS spanish alert: " + message);
                }
            }
            // modify by liang.zhang for Defect 6012945 at 2018-03-07 end
            
            if (message.isCmasMessage() && (persoRule == 2 || persoRule == 4)
                    && !shouldReceive4371 && !allowWpas) {
                // [BUGFIX]PR 1104825 chaobing.huang -END

                // PR 1054793 Added by fang.song end
                Log.w(TAG,"Ignore SMS CB CMAS: " + message);
            }
            else if (message.isEtwsMessage() && (persoRule == 3 || persoRule == 4) && !allowWpas) {
                Log.w(TAG,"Ignore SMS CB ETWS: " + message);
            }
            else if (privileged) { // [BUGFIX]-PR1006417-chenglin.jiang-001 add End
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                intent.setClass(context, CellBroadcastAlertService.class);
                context.startService(intent);
            } else {
                Log.w(TAG,"ignoring unprivileged action received " + action);
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            }
        } else if (Telephony.Sms.Intents.SMS_SERVICE_CATEGORY_PROGRAM_DATA_RECEIVED_ACTION
                .equals(action)) {
            if (privileged) {
                mSubscription = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
                    PhoneConstants.SUB1);
                TLog.d(TAG, "onReceive SMS_CATEGORY_PROGRAM_DATA mSubscription :" + mSubscription);
                CdmaSmsCbProgramData[] programDataList = (CdmaSmsCbProgramData[])
                        intent.getParcelableArrayExtra("program_data_list");
                if (programDataList != null) {
                    handleCdmaSmsCbProgramData(context, programDataList);
                } else {
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                    Log.w(TAG,"SCPD intent received with no program_data_list");
                }
            } else {
                Log.w(TAG,"ignoring unprivileged action received " + action);
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            }
        } else if (GET_LATEST_CB_AREA_INFO_ACTION.equals(action)) {
            if (privileged) {
                int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, 0);
                CellBroadcastMessage message = CellBroadcastReceiverApp.getLatestAreaInfo(subId);
                if (message != null) {
                    Intent areaInfoIntent = new Intent(
                            CellBroadcastAlertService.CB_AREA_INFO_RECEIVED_ACTION);
                    areaInfoIntent.putExtra("message", message);
                    context.sendBroadcastAsUser(areaInfoIntent, UserHandle.ALL,
                            android.Manifest.permission.READ_PHONE_STATE);
                }
            } else {
                Log.e(TAG, "caller missing READ_PHONE_STATE permission, returning"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
        //[FEATURE]-begin-MOD by TCTNB.yugang.jia,09/06/2013,FR-516039,
        //[SMS]Cell broadcast SMS support
            }
          //[BUGFIX]-MOD-begin by chaobing.huang,9/22/2015,FR-1078247
        } else if (("com.android.internal.telephony.uicc.SIM_RECORD_ICCID_READY".equals(action)) &&
        		!context.getResources().getBoolean(R.bool.def_isResetCmasSetting)){
            resetCMASSettingsIfSimCardChanged(context);
          //[BUGFIX]-MOD-end by chaobing.huang,9/22/2015,FR-1078247
        //[BUGFIX]-ADD-BEGIN by bin.xue for PR-1104716
        } else if ("com.android.broadcast.resetcd".equals(action)){
            if(TelephonyManager.getDefault().isMultiSimEnabled()){
                for(int j = 0; j < TelephonyManager.getDefault().getPhoneCount();j++){
                    if(mEnableSingleSIM && j >= 1){
                        continue;
                    }
                    Uri uri = Uri.withAppendedPath(CellBroadcast.Channel.CONTENT_URI, "sub"+j);
                    context.getContentResolver().delete(uri, null, null);
                    //PR998175-shaoxia.wang add begin
                    uri = Uri.withAppendedPath(CellBroadcast.CBLanguage.CONTENT_URI, "sub"+j);
                    context.getContentResolver().delete(uri, null, null);
                    //PR998175-shaoxia.wang add end
                }
            }else{
                context.getContentResolver().delete(CellBroadcast.Channel.CONTENT_URI, null, null);
                context.getContentResolver().delete(CellBroadcast.CBLanguage.CONTENT_URI, null, null);//PR998175-shaoxia.wang add
            }
            
            // add by liang.zhang for Defect 5185928 at 2017-08-17 begin
            java.util.List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context)
                    .getActiveSubscriptionInfoList();
            if (subInfoList != null) {
            	final int availableSubInfoLength = subInfoList.size();
                for (int i = 0; i < availableSubInfoLength; ++i) {
                	final SubscriptionInfo sir = subInfoList.get(i);
//                	MtkSmsManager manager = MtkSmsManager.getSmsManagerForSubscriptionId(sir.getSubscriptionId());
					//modify by yong.wang for defect-7134121 at 181126 start
					if(sir == null){
						continue;
					}
					//modify by yong.wang for defect-7134121 at 181126 end
                	if (mChannelExFlag) {
                        //0 ~ 4351
                		TctWrapperManager.disableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, sir.getSubscriptionId());
                        //4357 ~ 4369
                		TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, sir.getSubscriptionId());
                        //4400 ~ 65534
                		TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, sir.getSubscriptionId());
                		Log.d(TAG,"disable channel 0~65534");
                	} else {
                		//0 ~ 999
                		TctWrapperManager.disableCellBroadcastRange(0, 999, SubscriptionManager.getDefaultSubscriptionId());
                		Log.d(TAG,"disable channel 0~999");
                	}
                }
            }
            // add by liang.zhang for Defect 5185928 at 2017-08-17 end
            
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String defLanguage = context.getResources().getString(R.string.all_languages);
            ContentValues values = new ContentValues();
            values.put(CBLanguage.CBLANGUAGE,defLanguage);
            context.getContentResolver().update(CBLanguage.CONTENT_URI,values,null,null);
            sharedPrefs.edit().putString("cb_language", defLanguage).apply();
          //[FEATURE]-Add-BEGIN by TSCD.pingzhi.wang,03/25/2015,949742
            sharedPrefs.edit().putString("pref_key_choose_channel", mDefaultMode).apply(); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
          //[FEATURE]-Add-END by TSCD.pingzhi.wang
            //[FEATURE]-ADD-BEGIN by TSCD.tianming.lei,07/24/2014,708134
            sharedPrefs.edit().putBoolean(PRE_RESET_CHANNEL, true).apply();
            Log.w(TAG,"mms reset channel >>>>>>>>>"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
            //[FEATURE]-ADD-END by TSCD.tianming.lei
            context.getContentResolver().delete(CellBroadcast.Channel.CONTENT_URI, null ,null);

            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();//PR998175-shaoxia.wang add
            editor.putBoolean(CONFIG_RESET_CBNV_VELUE, true);
            editor.commit();
            setCustomizedChannels(context);
        }else if ("com.android.CellBroadcast.resetEmergAlert".equals(action)){
            //[BUGFIX]-MOD-BEGIN by TSCD.tianming.lei,02/05/2015, PR-901210
            /*
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS, true).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS, true).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_AMBER_ALERTS, true).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS,false).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS,false).apply();
            */
            initEmergencyPref(context);
             //[BUGFIX]-MOD-END by TSCD,tianming.lei
        }else if ("com.android.cellbroadcastreceiver.setstartup".equals(action)) {
			//modify by yong.wang for defect-7134121 at 181126 start
			//change subDescription to mPhoneId
            int mPhoneId = PhoneConstants.SUB1;
			//modify by yong.wang for defect-7134121 at 181126 end
            boolean value = false;
            boolean nvFlag = context.getResources().getBoolean(R.bool.def_set_NL_CB_on); // MODIFIED by yuxuan.zhang, 2016-09-13,BUG-2845457
            if(TelephonyManager.getDefault().isMultiSimEnabled()){
                boolean isEnableCB = false;
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-18,BUG-2854327*/
                Log.i(TAG, "setstartup-mPhoneId="+mPhoneId);
                if (mEnableSingleSIM) {
                    mPhoneId = PhoneConstants.SUB1;
                }else {
                    mPhoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
                            PhoneConstants.SUB1);
                }
                Log.i(TAG, "setstartup-mPhoneId final="+mPhoneId);
                /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                if(mPhoneId == 0){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                    channelMode = mSettings.getString("pref_key_choose_channel_sim1", mDefaultMode);
                }else if(mPhoneId == 1){
                    channelMode = mSettings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                }
                isEnableCB = intent.getBooleanExtra("startup",true);
                Log.w(TAG, "MultiSim--isEnableCB="+isEnableCB);
                if(!isEnableCB && isHollandSimCard(context,mPhoneId) && nvFlag){ // MODIFIED by yuxuan.zhang, 2016-09-13,BUG-2845457
                    return;
                }
            }else{
                boolean isEnableCB = false;
                isEnableCB = intent.getBooleanExtra("startup",true);
                Log.w(TAG, "oneSim--isEnableCB="+isEnableCB);
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                if(!isEnableCB && isHollandSimCard(context,mPhoneId) && nvFlag){
                    return;
                }
				//modify by yong.wang for defect-7134121 at 181126 end
                //[BUGFIX]-Add-BEGIN by yuwan,04/16/2017,4582741,
                if (context.getResources().getBoolean(R.bool.def_notification_remove)) {
                    Log.d(TAG, "notification remove");
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(NOTIFICATION_ID);
                }
                //[BUGFIX]-Add-END by yuwan,04/16/2017,4582741,
                channelMode = mSettings.getString("pref_key_choose_channel", mDefaultMode); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
            }
            //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,07/11/2013,PR485579,
            //Can not receive channel 4371
            //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,08/12/2013,501208,
            //[HOMO][HOMO]Cell Broadcast messages realization for qualcomm/MTK/Broadcom/Spr-
            //eadtrum/etc android smartphones
            //cbEnabled = intent.getBooleanExtra("startup", true);
            //boolean value = intent.getBooleanExtra("startup", true);

            //PR 1054793 Added by fang.song begin
            //[add]-begin-by-chaobing.huang-01132017-defect4007891
            //if ("true".equalsIgnoreCase(SystemProperties.get(CBSUtills.PRE_DEFINE_CHANNEL_4371, "false"))){
            if (context.getResources().getBoolean(R.bool.ro_cb_prechannel4371)){
            //[add]-end-by-chaobing.huang-01132017-defect4007891
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                Log.w(TAG, "need add 4371 when sim state change and def_cellbroadcastreceiver_prechannel_4371: true");
                enableChannelForNL(context, true);
            } else {
                Log.w(TAG, "no need add 4371 as def_cellbroadcastreceiver_prechannel_4371: false");
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            }
            //PR 1054793 Added by fang.song end

            value = intent.getBooleanExtra("startup", true);
            cbEnabled = value ;
            SharedPreferences.Editor editor = mSettings.edit();
          //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/15/2014,772564,new CLID variable to re-activate CB for NL
            if(TelephonyManager.getDefault().isMultiSimEnabled()){
                if(mPhoneId == 0){
                    editor.putBoolean("enable_channel_sim1", value);
                }else if(mPhoneId == 1 && !mEnableSingleSIM){
                    editor.putBoolean("enable_channel_sim2", value);
                }
            }else{
                editor.putBoolean("cb_enabled", cbEnabled);
            }
          //[FEATURE]-Add-END by TSCD.fujun.yang
            editor.commit();
            //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
//            MtkSmsManager manager = null;
            int subId =0;
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-13,BUG-3103010*/
            if(TelephonyManager.getDefault().isMultiSimEnabled() && !mEnableSingleSIM){
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-18,BUG-2854327*/
                Log.i(TAG, "setUp action -mPhoneId = "+mPhoneId);
                SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                SubscriptionInfo  mSubInfoRecord;
                if(mEnableSingleSIM){
                    mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(PhoneConstants.SUB1);
                }else{
                    mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(mPhoneId);
                }
                if(mSubInfoRecord != null) {
                    subId= mSubInfoRecord.getSubscriptionId();
                    Log.i(TAG, "get subId 1 = "+subId);
                }
                Log.i(TAG, "We get final subId = "+subId);
//                manager = MtkSmsManager.getSmsManagerForSubscriptionId(subId);
                /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
            }else{
                Log.i(TAG, "defualt smsmanager");
                /* MODIFIED-END by yuxuan.zhang,BUG-3103010*/
//                manager = MtkSmsManager.getDefault();
                subId = SubscriptionManager.getDefaultSubscriptionId();
            }
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
            boolean ww = TctWrapperManager.activateCellBroadcastSms(subId);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-26,BUG-2854327*/
            Log.i(TAG, "ww = "+ww);
            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            CBSUtills cbu = new CBSUtills(context);
            // remove by liang.zhang for Defect 5163407 at 207-09-05 begin
            /*ContentValues values = new ContentValues();
            values.put(Channel.Enable, value ? "Enable" : "Disable");
            cbu.updateChannel(values);*/
            // remove by liang.zhang for Defect 5163407 at 207-09-05 end
            //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879,
            /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
            if((mPhoneId != -1) && TelephonyManager.getDefault().isMultiSimEnabled()){
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                Log.w(TAG, "mPhoneId != -1 && TelephonyManager.getDefault().isMultiSimEnabled()");
                // here, disabled all channel
                    SmsBroadcastConfigInfo[] cbi = null;
//                    MtkSmsManager manager1 = MtkSmsManager.getSmsManagerForSubscriptionId(PhoneConstants.SUB1);
//                    MtkSmsManager manager2 = MtkSmsManager.getSmsManagerForSubscriptionId(PhoneConstants.SUB2);
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                    if (mChannelExFlag) {
                    	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                        //0 ~ 4351
                        //manager1.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	TctWrapperManager.disableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                    	//4357 ~ 4369
                        //manager1.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	//modify by yong.wang for defect-7134121 at 181126 start
						TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                    	//4400 ~ 65534
                        //manager1.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                    	
                        //manager2.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	//TctWrapperManager.disableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, PhoneConstants.SUB2);
                    	//4357 ~ 4369
                        //manager2.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	//TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, PhoneConstants.SUB2);
                    	//4400 ~ 65534
                        //manager2.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	//TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, PhoneConstants.SUB2);
                    }else{
                        //manager1.disableCellBroadcastRange(0, 999,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	TctWrapperManager.disableCellBroadcastRange(0, 999, subId);
                        //manager2.disableCellBroadcastRange(0, 999,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	//TctWrapperManager.disableCellBroadcastRange(0, 999, PhoneConstants.SUB2);
                    }
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                   //boolean a1 =  manager1.disableCellBroadcastRange(0, 999,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                   //boolean a2 = manager2.disableCellBroadcastRange(0, 999,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    boolean a1 = TctWrapperManager.disableCellBroadcastRange(0, 999, subId);
                    //boolean a2 = TctWrapperManager.disableCellBroadcastRange(0, 999, PhoneConstants.SUB2);
					//[modify]-end-by-chaobing.huang-01102017-defect3992285
					Log.w(TAG, "subDescription == -1 a1 = " + a1);
					/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
					// [BUGFIX]-ADD-BEGIN by TCTNB.cheng.zhao, 11/25/2014, PR-845553
					//modify by yong.wang for defect-7134121 at 181126 end
                    try {
                        cbi = cbu.getSmsBroadcastConfigInfo(PhoneConstants.SUB1);
                    } catch (IllegalArgumentException e) {
                        cbi = null;
                    }
					// [BUGFIX]-ADD-END by TCTNB.cheng.zhao, 11/25/2014, PR-845553
					if (cbi != null) {
						int num = cbi.length;
						for (int i = 0; i < num; i++) {
							int index = cbi[i].getFromServiceId();
							/* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
							//[modify]-by-chaobing.huang-01102017-defect3992285
							//boolean b = manager1.disableCellBroadcastRange(index, index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
							boolean b = TctWrapperManager.disableCellBroadcastRange(index, index, subId);
							Log.w(TAG, "subDescription == -1 b = " + b + ";index = " + index);
							/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
						}
					}
					if(!mEnableSingleSIM){
						// [BUGFIX]-ADD-BEGIN by TCTNB.cheng.zhao, 11/25/2014, PR-845553
						try {
							cbi = cbu.getSmsBroadcastConfigInfo(PhoneConstants.SUB2);
						} catch (IllegalArgumentException e) {
							Log.w(TAG, "!mEnableSingleSIM IllegalArgumentException", e); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
							cbi = null;
						}
						// [BUGFIX]-ADD-END by TCTNB.cheng.zhao, 11/25/2014, PR-845553
						if (cbi != null) {
							int num = cbi.length;
							for (int i = 0; i < num; i++) {
								int index = cbi[i].getFromServiceId();
								/* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
								//[modify]-by-chaobing.huang-01102017-defect3992285
								//boolean c = manager2.disableCellBroadcastRange(index, index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
								boolean c = TctWrapperManager.disableCellBroadcastRange(index, index, subId);
								Log.w(TAG, "c = " + c + ";index = " + index);
								/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
							}
						}
					}
					// remove by liang.zhang for Defect 6168344 at 2018-04-10 begin
//                	return;
					// remove by liang.zhang for Defect 6168344 at 2018-04-10 end
			}
            //[CB][mobile receive CB when disable CB

            if (value) {
                //here, according the channelMode to enable or disabled channel.
                Log.d(TAG,"value ="+value);
                if ("1".equalsIgnoreCase(channelMode)) {
                     //channelMode is "My channel list"
                    Log.d(TAG,"channelMode ="+channelMode);
                    SmsBroadcastConfigInfo[] cbi = null;
                    if(TelephonyManager.getDefault().isMultiSimEnabled()){
					//modify by yong.wang for defect-7134121 at 181126
                        cbi = cbu.getSmsBroadcastConfigInfo(mPhoneId);
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-18,BUG-2854327*/
                        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                        //modify by yong.wang for defect-7134121 at 181126
						SubscriptionInfo  mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(mPhoneId);
                        if(mSubInfoRecord != null) {
                            subId= mSubInfoRecord.getSubscriptionId();
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-13,BUG-3103010*/
                            Log.i(TAG, "active subId 1 = "+subId);
                        }
                        Log.i(TAG, "active subId 2 = "+subId);
                        /* MODIFIED-END by yuxuan.zhang,BUG-3103010*/
//                        manager = MtkSmsManager.getSmsManagerForSubscriptionId(subId);
                        /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
                    }else{
//                        cbi = cbu.getSmsBroadcastConfigInfo();
                    	subId = SubscriptionManager.getDefaultSubscriptionId();
                    }
                         if(cbi != null){
                             int num = cbi.length;
                             for (int i=0; i<num;i++) {
                                 int index = cbi[i].getFromServiceId();
                                 if(TelephonyManager.getDefault().isMultiSimEnabled()){
                                     if (cbi[i].isSelected()) {
                                         /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                                         //[modify]-by-chaobing.huang-01102017-defect3992285
                                         //boolean d = manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	 boolean d = TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                    	 Log.w(TAG, "d = " + d + ";index = " + index);
                                     } else {
                                         /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                         if (!(isBrazil50CbSupported && index == 50)) {
                                        	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                             //boolean e = manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	 boolean e = TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                        	 Log.w(TAG, "e = " + e + ";index = " + index);
                                         } else {
                                             Log.d(TAG, "AisBrazil50CbSupported is true and index == 50 ignore");
                                         }
                                         /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                                     }
                                 }else{
                                     if (cbi[i].isSelected()) {
                                      	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                         //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	 TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                     } else {
                                         if (!(isBrazil50CbSupported && index == 50)) {
                                        	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                             //manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	 TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                         } else {
                                             Log.d(TAG, "BisBrazil50CbSupported is true and index == 50 ignore");
                                         }
                                         /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                     }
                                 }

                             }
                         }
                }else {
                    Log.d(TAG,"channelMode ="+channelMode);
                    //channelMode is 0-999, enabled it
                    SmsBroadcastConfigInfo[] cbi = null;
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                    if (mChannelExFlag) {
                    	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                        //0 ~ 4351
                        //boolean f1 = manager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	boolean f1 = TctWrapperManager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                    	//4357 ~ 4369
                        //boolean f2 = manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	boolean f2 = TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                    	//4400 ~ 65534
                        //boolean f3 = manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	boolean f3 = TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                    	Log.d(TAG, "f1 = " + f1 + ";f2 = " + f2 + ";f3=" + f3);
                    }else{
                        //manager.enableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	TctWrapperManager.enableCellBroadcastRange(0, 999, subId);
                        //[modify]-end-by-chaobing.huang-01102017-defect3992285
                    }
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                    if(TelephonyManager.getDefault().isMultiSimEnabled()){
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
						//modify by yong.wang for defect-7134121 at 181126
                        cbi = cbu.getSmsBroadcastConfigInfo(mPhoneId);
                    }else{
                        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                        cbi = cbu.getSmsBroadcastConfigInfo();
                    }

                    //if channel index is more than 999, if it is selected, enabled it , else disabled

                    if(cbi != null){
                        int num = cbi.length;
                        Log.d(TAG,"num = "+num);
                        for (int i=0; i<num;i++) {
                            int index = cbi[i].getFromServiceId();
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                            if (index >= 1000 && !mChannelExFlag || mChannelExFlag && (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= index
                                    && index <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER || SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= index
                                    && index <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE)) {
                                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                if(TelephonyManager.getDefault().isMultiSimEnabled()){
                                    if (cbi[i].isSelected()) {
                                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                                    	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                        //boolean h = manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	boolean h = TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                    	Log.w(TAG, "h = " + h +";index = "+index);
                                    } else {
                                        //boolean j = manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	boolean j = TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                    	Log.w(TAG, "j = " + j + ";index = " + index);
                                    }
                                }else{
                                    if (cbi[i].isSelected()) {
                                        //boolean k = manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	boolean k = TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                    	Log.w(TAG, "k = " + k + ";index = " + index);
                                    } else {
                                        //boolean l = manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	boolean l = TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                    	//[modify]-end-by-chaobing.huang-01102017-defect3992285
                                        Log.w(TAG, "l = " + l + ";index = " + index);
                                        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                                    }
                                }

                            }
                        }
                    }
                }
            } else {
                // here, disabled all channel
                SmsBroadcastConfigInfo[] cbi = null;
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                if (mChannelExFlag) {
                	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                    //0 ~ 4351
                    //boolean flag1 = manager.disableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	boolean flag1 = TctWrapperManager.disableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                	//4357 ~ 4369
                    //boolean flag2 =manager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	boolean flag2 = TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                	//4400 ~ 65534
                    //boolean flag3 =manager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	boolean flag3 = TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                	Log.d(TAG, "flag1 = " + flag1 + ";flag2 = " + flag2 + ";flag3=" + flag3);
                }else{
                    //manager.disableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                       TctWrapperManager.disableCellBroadcastRange(0, 999, subId);
                    //[modify]-end-by-chaobing.huang-01102017-defect3992285
                }
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                if(TelephonyManager.getDefault().isMultiSimEnabled()){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
					//modify by yong.wang for defect-7134121 at 181126
                    cbi = cbu.getSmsBroadcastConfigInfo(mPhoneId);
                }else{
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    cbi = cbu.getSmsBroadcastConfigInfo();
                }
                if (cbi != null) {
                    int num = cbi.length;
                    for (int i = 0; i < num; i++) {
                        int index = cbi[i].getFromServiceId();
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                        if (!(isBrazil50CbSupported && index == 50)) {
                        	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                            //manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        	TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                        } else {
                            //manager.enableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        	TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                            //[modify]-end-by-chaobing.huang-01102017-defect3992285
                            Log.d(TAG, "isBrazil50CbSupported is true and enable 50");
                            /* MODIFIED-END by gang-chen,BUG-2642577*/
                            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                        }

                    }
                }
            }
        //Can not receive channel 4371
        }else if (action.equals(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED)){
            int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, SubscriptionManager.INVALID_SUBSCRIPTION_ID);
			//modify by yong.wang for defect-7134121 at 181126
			int mPhoneId = intent.getIntExtra(PhoneConstants.SLOT_KEY,PhoneConstants.SUB1);
            
            // add by liang.zhang for Defect 5955126 at 2018-02-08 begin
            boolean isUAE = false;
            boolean isPeru = false;
            boolean isMexico = false;
            boolean isChile = false;
            boolean isRomania = false;// add by liang.zhang for Defect 6364780 at 2018-06-05
            boolean isNZ = false; //add by liang.zhang for Defect 6369692 at 2018-06-07
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info != null && info.getMcc() == 424) {
        				isUAE = true;
        	        }  else if (info != null && info.getMcc() == 716) {
                    	isPeru = true;
                    } else if (info != null && info.getMcc() == 730) {
                    	isChile = true;
                    } else if (info != null && info.getMcc() == 334) {
                    	isMexico = true;
                    }
                    // add by liang.zhang for Defect 6364780 at 2018-06-05 begin
                    else if (info != null && info.getMcc() == 226) {
                    	isRomania = true;
                    }
                    // add by liang.zhang for Defect 6364780 at 2018-06-05 end
        			// add by liang.zhang for Defect 6369692 at 2018-06-07 begin
        			else if (info!= null && info.getMcc() == 530) {
        				isNZ = true;
        	        }
        			// add by liang.zhang for Defect 6369692 at 2018-06-07 end
        		}
        	}
        	TLog.i(TAG, "ACTION_SERVICE_STATE_CHANGED, isUAE = " + isUAE);
        	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
			//modify by yong.wang for defect-7134121 at 181126 start change SUB1 to mphoneId
        	if (isUAE || isRomania) { //modify by liang.zhang for Defect 6364780 at 2018-06-05
                editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS + mPhoneId, true);
        	}
        	if (isPeru) {
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS + mPhoneId, true);
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS + mPhoneId, true);
        	}
        	if (isMexico) {
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS + mPhoneId, true);
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS + mPhoneId, true);
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + mPhoneId, true);
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + mPhoneId, true);
        	}
        	// add by liang.zhang for Defect 6369692 at 2018-06-07 begin
        	if (isNZ) {
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + mPhoneId, true);
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + mPhoneId, true);
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH + mPhoneId, true);
        		
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS + mPhoneId, false);
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS + mPhoneId, false);
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_AMBER_ALERTS + mPhoneId, false);
        		// modify by liang.zhang for Defect 6766702 at 2018-08-14 begin
        		editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_VIBRATE + mPhoneId, true);
        		// modify by liang.zhang for Defect 6766702 at 2018-08-14 end
        		editor.putString(CellBroadcastSettings.KEY_ALERT_REMINDER_INTERVAL + mPhoneId, "0");// modify by liang.zhang for Defect 6957889 at 2018-09-06
        	}
			//modify by yong.wang for defect-7134121 at 181126 end
        	// add by liang.zhang for Defect 6369692 at 2018-06-07 end
        	editor.commit();
            // add by liang.zhang for Defect 5955126 at 2018-02-08 end
            
            ServiceState ss = ServiceState.newFromBundle(intent.getExtras());
            int newState = ss.getState();
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            Log.w(TAG,"onReceive is SERVICE_STATE_CHANGED newState: "+newState+" Full: "+ss);
			//modify by yong.wang for defect-7134121 at 181126 start
            //startCbConfig(context.getApplicationContext(),newState,subId);
			startCbConfig(context.getApplicationContext(),newState,mPhoneId);
			//modify by yong.wang for defect-7134121 at 181126 end
         //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
          //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,09/23/2014,795416,after changed simcard,add perso control the CB value
            if("true".equalsIgnoreCase(SystemProperties.get("ro.config.cb.for.alwe.version","false")) || isHollandSimCard(context,(long)mPhoneId) && context.getResources().getBoolean(R.bool.def_set_NL_CB_on)){ // MODIFIED by yuxuan.zhang, 2016-10-19,BUG-1112693
                Log.i(TAG, "alwe version");
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                enableCBIfSimCardChanged(context,newState);
            }
          //[BUGFIX]-Add-END by TSCD.fujun.yang

        //PR 1054793 Added by fang.song begin
        } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
            if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(stateExtra)
                || IccCardConstants.INTENT_VALUE_ICC_IMSI.equals(stateExtra)
                || IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(stateExtra)) {
                //mSimState = IccCardConstants.State.READY;
            	//[add]-begin-by-chaobing.huang-01132017-defect4007891
                //if ("true".equalsIgnoreCase(SystemProperties.get(CBSUtills.PRE_DEFINE_CHANNEL_4371, "false"))){
                if (context.getResources().getBoolean(R.bool.ro_cb_prechannel4371)){
                //[add]-END-by-chaobing.huang-01132017-defect4007891
                    TLog.i(TAG, "need add 4371 when sim state change and def_cellbroadcastreceiver_prechannel_4371: true");
                    enableChannelForNL(context, true);
                }
            }
          //[BUGFIX]-MOD-begin by chaobing.huang,9/22/2015,FR-1078247
            boolean isResetCmasSetting = context.getResources().getBoolean(R.bool.def_isResetCmasSetting);
            if((intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE).equals(IccCardConstants.INTENT_VALUE_ICC_LOADED)) &&
            		isResetCmasSetting){
            	resetCMASSettingsIfSimCardChanged(context);
            }
          //[BUGFIX]-MOD-end by chaobing.huang,9/22/2015,FR-1078247
        //PR 1054793 Added by fang.song end
        }else if(action.equals("android.telephony.SmsManager.CBMSettingActivity.ACTION")){
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            Log.i(TAG,"read-done-channel");
        }
        // PR-1052792-david.zhang-001 begin
        else if ("com.android.cmas.rmt.exercise.alerts".equals(action)) {
            Log.i(TAG, "com.android.cmas.rmt.exercise.alerts");
            String alertType = "";
            boolean alertValue = false;
            alertType = intent.getStringExtra("alert_type");
            alertValue = intent.getBooleanExtra("alert_value", true);
            Log.i(TAG, "com.android.cmas.rmt.exercise.alerts--alertType=" + alertType);
            Log.i(TAG, "com.android.cmas.rmt.exercise.alerts--alertValue=" + alertValue);
			//modify by yong.wang for defect-7134121 at 181126 satrt
            setCMASAlertPref(context, CellBroadcastSettings.sPhoneId, alertType, alertValue);
            startConfigServiceFromCMASSetting(context, CellBroadcastSettings.sPhoneId);
        }
        // PR-1052792-david.zhang-001 end
        else {
         //[FEATURE]-end-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
            Log.w(TAG, "onReceive() unexpected action " + action);
        }

    }
  //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/15/2014,772564,new CLID variable to re-activate CB for NL
    public void sendCBSetIntent(Context mcontext,boolean value,int sc_sim_id){
        Log.i(TAG, "send-CB-enable-intent-simcard-changed");
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        String ActionString = "com.android.cellbroadcastreceiver.setstartup";
        Intent intent = new Intent(ActionString);
        intent.putExtra("startup", value);
        //intent.putExtra(MSimConstants.SUBSCRIPTION_KEY,sc_sim_id);
        mcontext.sendBroadcast(intent);
    }

    //PR 1055006 Added by fang.song begin
    public void addChannel(Context context, String channelName, String sIndex, String channelEnable, int subDescription) {
            ContentValues values = new ContentValues();
            CBSUtills cbu = new CBSUtills(context);
            values.put(CellBroadcast.Channel.NAME, channelName);
            values.put(CellBroadcast.Channel.INDEX, sIndex);
            values.put(CellBroadcast.Channel.Enable, channelEnable);
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1)){
                if (cbu.queryChannelIndexA(sIndex,subDescription)) {
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                    Log.i(TAG, "already have this channel, return! subDescription = "+subDescription);
                    return;
                }
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-26,BUG-2854327*/
                if(context.getResources().getBoolean(R.bool.def_cb_disable_channel)){
                    values.clear();
                    values.put(CellBroadcast.Channel.Enable, "Disable");
                cbu.updateChannel("919", values, subDescription);
                }
                /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
                cbu.addChannel(values,subDescription);
            }else{
                if (cbu.queryChannelIndexA(sIndex)) {
                    Log.i(TAG, "single sim already have this channel, return! subDescription = 0");
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    return;
                }
                cbu.addChannel(values);
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-26,BUG-2854327*/
                if(context.getResources().getBoolean(R.bool.def_cb_disable_channel)){
                    values.clear();
                    values.put(CellBroadcast.Channel.Enable, "Disable");
                    cbu.updateChannel("919", values);
                }
                /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
            }
    }

    public void enableChannelForNL(Context context, boolean enable){
        if (!enable) return;//should delete channel 4371
            if(TelephonyManager.getDefault().isMultiSimEnabled()){
                for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++){
                    if(!isHollandSimCard(context, i)){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                    Log.i(TAG, "is not nl sim and subscription="+i);
                        continue;
                    }
                    Log.i(TAG, " add channel subscription="+i);
                    addChannel(context, channelName_4371, channelIndex_4371, channelEnable_4371, i);
                }
            } else {
                if(!isHollandSimCard(context, 0)){
                    Log.i(TAG, "is not nl sim and subscription=0");
                    return;
                }
                Log.i(TAG, "single sim add channel subscription=0");
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                addChannel(context, channelName_4371, channelIndex_4371, channelEnable_4371, 0);
            }
    }
    //PR 1055006 Added by fang.song end


    public void enableCBIfSimCardChanged(Context mcontext,int newState){
       if(newState != ServiceState.STATE_IN_SERVICE ||
                newState != ServiceState.STATE_EMERGENCY_ONLY){
           return;
       }
       if(TelephonyManager.getDefault().isMultiSimEnabled()){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mcontext);
            TelephonyManager tm = TelephonyManager.getDefault();
            for (int sc_sim_id = 0; sc_sim_id < tm.getPhoneCount(); sc_sim_id++){
                String currentSerialNumber = tm.getSimSerialNumber(sc_sim_id);
                TLog.i(TAG, ",currentSerialNumber="+currentSerialNumber);
                String lastSerialNumber = null;
                if(sc_sim_id == 0){
                    lastSerialNumber = sp.getString("key_last_sim_card_serial_number1", "");
                    sp.edit().putString("key_last_sim_card_serial_number1", currentSerialNumber).commit();
                }else if(sc_sim_id == 1 && !mEnableSingleSIM){
                    lastSerialNumber = sp.getString("key_last_sim_card_serial_number2", "");
                    sp.edit().putString("key_last_sim_card_serial_number2", currentSerialNumber).commit();
                }
                TLog.i(TAG, ",lastSerialNumber="+lastSerialNumber);
                if((lastSerialNumber != null ) && (lastSerialNumber != "") &&(currentSerialNumber != null) &&!lastSerialNumber.equals(currentSerialNumber)) {
                    if(sc_sim_id == 0){
                        sp.edit().putBoolean("enable_channel_sim1", true).commit();
                     }else if(sc_sim_id == 1 && !mEnableSingleSIM){
                        sp.edit().putBoolean("enable_channel_sim2", true).commit();
                     }
                    sendCBSetIntent(mcontext,true,sc_sim_id);
                }
            }
        }else{
            TelephonyManager tm = TelephonyManager.getDefault();

            String currentSerialNumber = tm.getSimSerialNumber();
            TLog.i(TAG, ",currentSerialNumber="+currentSerialNumber);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mcontext);
            String lastSerialNumber = sp.getString("key_last_sim_card_serial_number", "");
            TLog.i(TAG, "lastSerialNumber="+lastSerialNumber);
            if((lastSerialNumber != null ) && (lastSerialNumber != "") &&(currentSerialNumber != null) && !lastSerialNumber.equals(currentSerialNumber)) {
                sp.edit().putString("key_last_sim_card_serial_number", currentSerialNumber).commit();
                sendCBSetIntent(mcontext,true,PhoneConstants.SUB1);
            }
        }
    }
  //[FEATURE]-Add-END by TSCD.fujun.yang
    /**
     * Handle Service Category Program Data message.
     * TODO: Send Service Category Program Results response message to sender
     *
     * @param context
     * @param programDataList
     */
    private void handleCdmaSmsCbProgramData(Context context,
            CdmaSmsCbProgramData[] programDataList) {
        for (CdmaSmsCbProgramData programData : programDataList) {
            switch (programData.getOperation()) {
                case CdmaSmsCbProgramData.OPERATION_ADD_CATEGORY:
                    tryCdmaSetCategory(context, programData.getCategory(), true);
                    break;

                case CdmaSmsCbProgramData.OPERATION_DELETE_CATEGORY:
                    tryCdmaSetCategory(context, programData.getCategory(), false);
                    break;

                case CdmaSmsCbProgramData.OPERATION_CLEAR_CATEGORIES:
                    tryCdmaSetCategory(context,
                            SmsEnvelope.SERVICE_CATEGORY_CMAS_EXTREME_THREAT, false);
                    tryCdmaSetCategory(context,
                            SmsEnvelope.SERVICE_CATEGORY_CMAS_SEVERE_THREAT, false);
                    tryCdmaSetCategory(context,
                            SmsEnvelope.SERVICE_CATEGORY_CMAS_CHILD_ABDUCTION_EMERGENCY, false);
                    tryCdmaSetCategory(context,
                            SmsEnvelope.SERVICE_CATEGORY_CMAS_TEST_MESSAGE, false);
                    break;

                default:
                    loge("Ignoring unknown SCPD operation " + programData.getOperation());
            }
        }
    }

    private void tryCdmaSetCategory(Context context, int category, boolean enable) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = null;
        if(TelephonyManager.getDefault().isMultiSimEnabled()){
            mSubscription = PhoneConstants.SUB1;
        }

        switch (category) {
            case SmsEnvelope.SERVICE_CATEGORY_CMAS_EXTREME_THREAT:
                key = CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + mSubscription;
                break;

            case SmsEnvelope.SERVICE_CATEGORY_CMAS_SEVERE_THREAT:
                key = CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + mSubscription;
                break;

            case SmsEnvelope.SERVICE_CATEGORY_CMAS_CHILD_ABDUCTION_EMERGENCY:
                key = CellBroadcastSettings.KEY_ENABLE_CMAS_AMBER_ALERTS + mSubscription;
                break;

            case SmsEnvelope.SERVICE_CATEGORY_CMAS_TEST_MESSAGE:
                key = CellBroadcastSettings.KEY_ENABLE_CMAS_TEST_ALERTS + mSubscription;
                break;

            default:
                TLog.w(TAG, "Ignoring SCPD command to " + (enable ? "enable" : "disable")
                        + " alerts in category " + category);
        }
        if (null != key) sharedPrefs.edit().putBoolean(key, enable).apply();
    }

    /**
     * Tell {@link CellBroadcastConfigService} to enable the CB channels.
     * @param context the broadcast receiver context
     */
    static void startConfigService(Context context) {
        //[BUGFIX]-Add-BEGIN by TCTNB.yugang.jia,01/07/2013,PR-582740
        TLog.d(TAG,"startConfigService ACTION_ENABLE");
        //[BUGFIX]-Add-END by TCTNB.yugang.jia,01/07/2013,PR-582740
        Intent serviceIntent = new Intent(CellBroadcastConfigService.ACTION_ENABLE_CHANNELS,
                null, context, CellBroadcastConfigService.class);
        context.startService(serviceIntent);
    }
  
  	//modify by yong.wang for defect-7134121 at 181126 satrt
    /*static void startConfigService(Context context, long subscription) {
        Intent serviceIntent = new Intent(CellBroadcastConfigService.ACTION_ENABLE_CHANNELS, null,
                context, CellBroadcastConfigService.class);
        serviceIntent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, (int) subscription); // MODIFIED by yuxuan.zhang, 2016-06-16,BUG-2251977
        context.startService(serviceIntent);
    }*/
	static void startConfigService(Context context, long phoneId) {
        int mPhoneId = new Long(phoneId).intValue();
        int subscriptionid[] = SubscriptionManager.getSubId(mPhoneId);
        Intent serviceIntent = new Intent(CellBroadcastConfigService.ACTION_ENABLE_CHANNELS, null,
                context, CellBroadcastConfigService.class);
        serviceIntent.putExtra(PhoneConstants.SUBSCRIPTION_KEY,subscriptionid[0]); // MODIFIED by yuxuan.zhang, 2016-06-16,BUG-2251977
        context.startService(serviceIntent);
    }
	//modify by yong.wang for defect-7134121 at 181126 end
  //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/05/2015,886284,[SCB][CMAS][ETWS]Can receive CMAS
    //and ETWS even not enable "show ETWS/CMAS test broadcasts"
    static void startConfigServiceFromCMASSetting(Context context, long phoneId) {
        int mPhoneId = new Long(phoneId).intValue();
        int subscription[] = SubscriptionManager.getSubId(mPhoneId);
        TLog.d(TAG,"startConfigServiceFromCMASSetting-subscription="+subscription[0]+" mPhoneId="+mPhoneId);
        Intent serviceIntent = new Intent(CellBroadcastConfigService.ACTION_ENABLE_CHANNELS, null,
                context, CellBroadcastConfigService.class);
        serviceIntent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subscription[0]);
        context.startService(serviceIntent);
    }
  //[BUGFIX]-Add-END by TSCD.fujun.yang

    /**
     * @return true if the phone is a CDMA phone type
     */
    static boolean phoneIsCdma(int subscription) {
        boolean isCdma = false;
        try {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                isCdma = TelephonyManager.getDefault().getCurrentPhoneType(subscription) == TelephonyManager.PHONE_TYPE_CDMA;
            } else {
                isCdma = (TelephonyManager.getDefault().getCurrentPhoneType() == TelephonyManager.PHONE_TYPE_CDMA);
            }
        } catch (Exception e) {
            TLog.w(TAG, "phone.getActivePhoneType() failed");
        }
        return isCdma;
    }
    private void startConfigCB(Context mContext ,int subId,int phoneId) {
        TLog.i(TAG,"startConfigCB");
        String channelModeSim = null;
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
        boolean isBrazil50CbSupported = mContext.getResources().getBoolean(R.bool.show_brazil_settings);
        Log.d(TAG, "isBrazil50CbSupported A:" + isBrazil50CbSupported);
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
        SharedPreferences mSettings = mContext.getSharedPreferences(PREFS_NAME, 0);
        if(!TelephonyManager.getDefault().isMultiSimEnabled()){//single simcard
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2854327*/
            Boolean enable = mSettings.getBoolean("cb_enabled", false);
            Log.i(TAG,"startCbConfig-onesimcard-enable="+enable);
            if(!enable){
                return;
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
//            MtkSmsManager manager = MtkSmsManager.getDefault();
            subId = SubscriptionManager.getDefaultSubscriptionId();
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
            boolean xx = TctWrapperManager.activateCellBroadcastSms(subId);
            Log.i(TAG, "xx = "+xx);
            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            CBSUtills cbu = new CBSUtills(mContext);
            SmsBroadcastConfigInfo[] cbi = cbu.getSmsBroadcastConfigInfo();
            channelModeSim = mSettings.getString("pref_key_choose_channel", mDefaultMode); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
            TLog.i(TAG,"startCbConfig-onesimcard-channelModeSim="+channelModeSim);
            if ("0".equalsIgnoreCase(channelModeSim)){
                //channelMode is 0-999, enabled it
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                if (mChannelExFlag) {
                	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                    //0 ~ 4351
                    //manager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                	//4357 ~ 4369
                    //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                	//4400 ~ 65534
                    //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                }else{
                    //manager.enableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(0, 999, subId);
                    //[modify]-end-by-chaobing.huang-01102017-defect3992285
                }
                //if channel index is more than 999, if it is selected, enabled it , else disabled
                if(cbi != null){
                    int num = cbi.length;
                    for (int i=0; i<num;i++) {
                        int index = cbi[i].getFromServiceId();
                        if (index >= 1000 && !mChannelExFlag || mChannelExFlag && (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= index
                                && index <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER || SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= index
                                && index <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE)) {
                                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                            if (cbi[i].isSelected()) {
                            	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                            } else {
                                //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                //[modify]-end-by-chaobing.huang-01102017-defect3992285
                            }
                        }
                    }
                }
            }else{
                //channelMode is "My channel list"
                if(cbi != null){
                         int num = cbi.length;
                         for (int i=0; i<num;i++) {
                             int index = cbi[i].getFromServiceId();
                             if (cbi[i].isSelected()) {
                            	 //[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                 //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	 TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                             } else {
                                 /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                 if (!(isBrazil50CbSupported && index == 50)) {
                                     //manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                	 TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                     //[modify]-end-by-chaobing.huang-01102017-defect3992285
                                 } else {
                                     Log.d(TAG, "CisBrazil50CbSupported is true and index == 50, ignore");
                                 }
                                 /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                             }
                         }
                     }
            }
        }else{//multi simcard
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2854327*/
            Boolean isable = false;
              if(phoneId == 0){
                  isable = mSettings.getBoolean("enable_channel_sim1", false);
              }else if(phoneId == 1){
                  isable = mSettings.getBoolean("enable_channel_sim2", false);
              }
              Log.i(TAG, "phoneId = "+phoneId+";isable = "+isable);
              if(!isable){
                  return;
              }
              /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
//              MtkSmsManager manager = MtkSmsManager.getSmsManagerForSubscriptionId(subId);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
            boolean cc = TctWrapperManager.activateCellBroadcastSms(subId);
            Log.i(TAG, "cc = "+cc);
            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            CBSUtills cbu = new CBSUtills(mContext);
            SmsBroadcastConfigInfo[] cbi = cbu.getSmsBroadcastConfigInfo(phoneId);
            if(phoneId == 0){
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                channelModeSim = mSettings.getString("pref_key_choose_channel_sim1", mDefaultMode);
            }else if(phoneId == 1){
                channelModeSim = mSettings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            }
            TLog.i(TAG,"startCbConfig-multisimcard-channelModeSim="+channelModeSim);
            if ((channelModeSim != null) && ("0".equalsIgnoreCase(channelModeSim))){
                //channelMode is 0-999, enabled it
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                if (mChannelExFlag) {
                	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                    //0 ~ 4351
                    //manager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                	//4357 ~ 4369
                    //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                	//4400 ~ 65534
                    //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                }else{
                    //manager.enableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(0, 999, subId);
                    //[modify]-end-by-chaobing.huang-01102017-defect3992285
                }
                //if channel index is more than 999, if it is selected, enabled it , else disabled
                if(cbi != null){
                    int num = cbi.length;
                    for (int j=0; j<num;j++) {
                        int index = cbi[j].getFromServiceId();
                        if (index >= 1000 && !mChannelExFlag || mChannelExFlag && (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= index
                                && index <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER || SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= index
                                && index <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE)) {
                                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                            if (cbi[j].isSelected()) {
                            	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                            } else {
                                //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                //[modify]-end-by-chaobing.huang-01102017-defect3992285
                            }
                        }
                    }
                }
            }else{
                //channelMode is "My channel list"
                if(cbi != null){
                         int num = cbi.length;
                         for (int j=0; j<num;j++) {
                             int index = cbi[j].getFromServiceId();
                             if (cbi[j].isSelected()) {
                            	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                 //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	 TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                             } else {
                                 /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                 if (!(isBrazil50CbSupported && index == 50)) {
                                	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                     //manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                	 TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                 } else {
                                     Log.d(TAG, "CisBrazil50CbSupported is true and index == 50, ignore");
                                 }
                                 /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                             }
                         }
                     }
            }
        }
    }
	//modify by yong.wang for defect-7134121 at 181126 satrt -- change subid to phoneId
    private void startConfigCMAS(Context mContext,long phoneId) {
        if(phoneId != -1){
            startConfigService(mContext, phoneId);
        }else{
            startConfigService(mContext);
        }
    }
	//modify by yong.wang for defect-7134121 at 181126 end -- change subid to phoneId
    private void startCbConfig(Context mContext, int newState ,int mPhoneId){
		//modify by yong.wang for defect-7134121 at 181126 start
        //TLog.d(TAG,"startCbConfig-newState="+newState+" subId="+subId);
        //int phoneId = SubscriptionManager.getPhoneId(subId);
        //TLog.d(TAG,"startCbConfig-phoneId="+phoneId);
        int subscriptionid[] = SubscriptionManager.getSubId(mPhoneId);
         TLog.d(TAG,"startCbConfig-newState = "+newState+";    mPhoneId= "+mPhoneId +";    subscriptionid = " +subscriptionid[0]);
		 //modify by yong.wang for defect-7134121 at 181126 end
        if(!SubscriptionManager.isValidSubscriptionId(subscriptionid[0])){
            return;
        }
        if(!SubscriptionManager.isValidPhoneId(mPhoneId)){
            return;
        }
        if(mEnableSingleSIM && mPhoneId >= 1){
            return;
        }
        if(TelephonyManager.getDefault().isMultiSimEnabled()){//multi simcard
            if(mPhoneId == 0){//sim1
                SharedPreferences mSettings = mContext.getSharedPreferences(PREFS_NAME, 0);
                int mServiceStateSim1 = mSettings.getInt("mServiceStateSim1",-1);
                TLog.d(TAG,"startCbConfig-mServiceStateSim1="+mServiceStateSim1);
                mSettings.edit().putInt("mServiceStateSim1", newState).commit();
                if((mServiceStateSim1 != newState) && (newState == ServiceState.STATE_IN_SERVICE ||
                        newState == ServiceState.STATE_EMERGENCY_ONLY)){
					//modify by yong.wang for defect-7134121 at 181126 start
                    startConfigCMAS(mContext,mPhoneId);
                    startConfigCB(mContext,subscriptionid[0],mPhoneId);
					//modify by yong.wang for defect-7134121 at 181126 end
                }
            }else if(mPhoneId == 1){//sim2
                SharedPreferences mSettings = mContext.getSharedPreferences(PREFS_NAME, 0);
                int mServiceStateSim2 = mSettings.getInt("mServiceStateSim2",-1);
                TLog.d(TAG,"startCbConfig-mServiceStateSim2="+mServiceStateSim2);
                mSettings.edit().putInt("mServiceStateSim2", newState).commit();
                if((mServiceStateSim2 != newState) && (newState == ServiceState.STATE_IN_SERVICE ||
                        newState == ServiceState.STATE_EMERGENCY_ONLY)){
					//modify by yong.wang for defect-7134121 at 181126 start
                    startConfigCMAS(mContext,mPhoneId);
                    startConfigCB(mContext,subscriptionid[0],mPhoneId);
					//modify by yong.wang for defect-7134121 at 181126 end
                }
            }
        }else{
            SharedPreferences mSettings = mContext.getSharedPreferences(PREFS_NAME, 0);
            int mServiceState0 = mSettings.getInt("mServiceState",-1);
            TLog.d(TAG,"startCbConfig-mServiceState0="+mServiceState0);
            mSettings.edit().putInt("mServiceState", newState).commit();
            if((mServiceState0 != newState) && (newState == ServiceState.STATE_IN_SERVICE ||
            	newState == ServiceState.STATE_EMERGENCY_ONLY)){//single simcard
				//modify by yong.wang for defect-7134121 at 181126 start
                startConfigCMAS(mContext,mPhoneId);//[modify]-by-chaobing.huang-defect4256931-20170222
                startConfigCB(mContext,0,mPhoneId);
				//modify by yong.wang for defect-7134121 at 181126 end
            }
        }
    }
    private void startCbConfig(Context mContext, int newState){
           String channelModeSim = null;
           boolean isBrazil50CbSupported = mContext.getResources().getBoolean(R.bool.show_brazil_settings); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
           SharedPreferences mSettings = mContext.getSharedPreferences(PREFS_NAME, 0);
		   //modify by yong.wang for defect-7134121 at 181126
		   SubscriptionManager mSubscriptionManager = SubscriptionManager.from(mContext);
           int subId = 0;
            if (newState == ServiceState.STATE_IN_SERVICE ||
                    newState == ServiceState.STATE_EMERGENCY_ONLY) {
                TLog.d(TAG,"startCbConfig only service in IN_SERVICE or EMERGENCY_ONLY. newState: "+newState);
                //firstly, config and activate CMAS channel
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                    for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++){
					//modify by yong.wang for defect-7134121 at 181126 start
						SubscriptionInfo sir = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
						if (sir == null) {
							continue;
						}
						//modify by yong.wang for defect-7134121 at 181126 end	
                        startConfigService(mContext, i);
                    }
                } else {
                    startConfigService(mContext);
                }
              //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/15/2014,772564,new CLID variable to re-activate CB for NL
                TLog.i(TAG,"startCbConfig");
                if(TelephonyManager.getDefault().isMultiSimEnabled() && "true".equalsIgnoreCase(SystemProperties.get("ro.set.nl.cb.on","false"))){
                    for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++){
						//modify by yong.wang for defect-7134121 at 181126 start
						SubscriptionInfo sir = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
						if (sir == null) {
							continue;
						}
						//modify by yong.wang for defect-7134121 at 181126 end	
                        if(!isHollandSimCard(mContext, i)){
                            continue;
                         }
                            TLog.i(TAG, "startCbConfig-mutilsimcard-subscription="+i);
                            if (newState == ServiceState.STATE_IN_SERVICE) {
                                if (true){
//                                	MtkSmsManager manager = MtkSmsManager.getSmsManagerForSubscriptionId(i);
                                	subId = i;
                                    CBSUtills cbu = new CBSUtills(mContext);
                                    SmsBroadcastConfigInfo[] cbi = cbu.getSmsBroadcastConfigInfo(i);
                                    if(i == 0){
                                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                        channelModeSim = mSettings.getString("pref_key_choose_channel_sim1", mDefaultMode);
                                    }else if(i == 1){
                                        channelModeSim = mSettings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                                        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                    }
                                    TLog.i(TAG,"startCbConfig-multisimcard-channelModeSim="+channelModeSim);
                                    if ((channelModeSim != null) && ("0".equalsIgnoreCase(channelModeSim))){
                                        //channelMode is 0-999, enabled it
                                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                                        if (mChannelExFlag) {
                                        	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                            //0 ~ 4351
                                            //manager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	TctWrapperManager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                                        	//4357 ~ 4369
                                            //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                                        	//4400 ~ 65534
                                            //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                                        }else{
                                            //manager.enableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	TctWrapperManager.enableCellBroadcastRange(0, 999, subId);
                                            //[modify]-end-by-chaobing.huang-01102017-defect3992285
                                        }
                                        //if channel index is more than 999, if it is selected, enabled it , else disabled
                                        if(cbi != null){
                                            int num = cbi.length;
                                            for (int j=0; j<num;j++) {
                                                int index = cbi[j].getFromServiceId();
                                                if (index >= 1000 && !mChannelExFlag || mChannelExFlag && (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= index
                                                        && index <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER || SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= index
                                                        && index <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE)) {
                                                        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                                    if (cbi[j].isSelected()) {
                                                    	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                                        //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                    	TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                                    } else {
                                                        //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                    	TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                                    	//[modify]-end-by-chaobing.huang-01102017-defect3992285
                                                    }
                                                }
                                            }
                                        }
                                    }else{
                                        //channelMode is "My channel list"
                                        if(cbi != null){
                                                 int num = cbi.length;
                                                 for (int j=0; j<num;j++) {
                                                     int index = cbi[j].getFromServiceId();
                                                     if (cbi[j].isSelected()) {
                                                    	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                                         //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                    	 TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                                     } else {
                                                         /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                                         if (!(isBrazil50CbSupported && index == 50)) {
                                                        	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                                             //manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                        	 TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                                         } else {
                                                             Log.d(TAG, "DisBrazil50CbSupported is true and index == 50 ignore");
                                                         }
                                                         /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                                     }
                                                 }
                                             }
                                    }
                                }
                            }
                    }
                    return;
                }else{
                  if("true".equalsIgnoreCase(SystemProperties.get("ro.set.nl.cb.on","false")) && isHollandSimCard(mContext, 0)){
                    TLog.i(TAG, "startCbConfig-one-simcard");
//                    MtkSmsManager manager = MtkSmsManager.getDefault();
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                    CBSUtills cbu = new CBSUtills(mContext);
                    SmsBroadcastConfigInfo[] cbi = cbu.getSmsBroadcastConfigInfo();
                    channelModeSim = mSettings.getString("pref_key_choose_channel", mDefaultMode); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
                    TLog.i(TAG,"startCbConfig-onesimcard-channelModeSim="+channelModeSim);
                    if ("0".equalsIgnoreCase(channelModeSim)){
                        //channelMode is 0-999, enabled it
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                        if (mChannelExFlag) {
                        	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                            //0 ~ 4351
                            //manager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        	TctWrapperManager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                        	//4357 ~ 4369
                            //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                        	//4400 ~ 65534
                            //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                        }else{
                            //manager.enableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        	TctWrapperManager.enableCellBroadcastRange(0, 999, subId);
                            //[modify]-end-by-chaobing.huang-01102017-defect3992285
                        }
                        //if channel index is more than 999, if it is selected, enabled it , else disabled
                        if(cbi != null){
                            int num = cbi.length;
                            for (int i=0; i<num;i++) {
                                int index = cbi[i].getFromServiceId();
                                if (index >= 1000 && !mChannelExFlag || mChannelExFlag && (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= index
                                        && index <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER || SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= index
                                        && index <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE)) {
                                        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                    if (cbi[i].isSelected()) {
                                    	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                        //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                    } else {
                                        //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                        //[modify]-end-by-chaobing.huang-01102017-defect3992285
                                    }
                                }
                            }
                        }
                    }else{
                        //channelMode is "My channel list"
                        if(cbi != null){
                                 int num = cbi.length;
                                 for (int i=0; i<num;i++) {
                                     int index = cbi[i].getFromServiceId();
                                     if (cbi[i].isSelected()) {
                                    	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                         //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                    	 TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                     } else {
                                         /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                         if (!(isBrazil50CbSupported && index == 50)) {
                                        	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                             //manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	 TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                         } else {
                                             Log.d(TAG, "EisBrazil50CbSupported is true and index == 50 ,ignore");
                                         }
                                         /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                     }
                                 }
                             }
                    }
                    return ;
                  }
                }
                //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/10/2013,FR-400968,
                //Customize SMS cb channel list in CLID
                if (newState == ServiceState.STATE_IN_SERVICE) {
                    if(TelephonyManager.getDefault().isMultiSimEnabled()){
                        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++){
                               Boolean isable = false;
                             //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,09/23/2014,795416,after changed simcard,add perso control the CB value
                               if(i == 0){
                                   isable = mSettings.getBoolean("enable_channel_sim1", false);
                               }else if(i == 1){
                                   isable = mSettings.getBoolean("enable_channel_sim2", false);
                               }
                             //[BUGFIX]-Add-END by TSCD.fujun.yang
                               TLog.i(TAG,"startCbConfig-multisimcard-isable="+isable);
                                if (newState == ServiceState.STATE_IN_SERVICE) {
                                    if (isable){
//                                    	MtkSmsManager manager = MtkSmsManager.getSmsManagerForSubscriptionId(i);
                                    	subId = i;
                                        CBSUtills cbu = new CBSUtills(mContext);
                                        SmsBroadcastConfigInfo[] cbi = cbu.getSmsBroadcastConfigInfo(i);
                                        if(i == 0){
                                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                            channelModeSim = mSettings.getString("pref_key_choose_channel_sim1", mDefaultMode);
                                        }else if(i == 1){
                                            channelModeSim = mSettings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                                            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                        }
                                        TLog.i(TAG,"startCbConfig-channelModeSim="+channelModeSim);
                                        if ((channelModeSim != null) && ("0".equalsIgnoreCase(channelModeSim))){
                                            //channelMode is 0-999, enabled it
                                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                                            if (mChannelExFlag) {
                                            	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                                //0 ~ 4351
                                                //manager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                            	TctWrapperManager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                                                //4357 ~ 4369
                                                //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                            	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                                                //4400 ~ 65534
                                                //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                            	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                                            }else{
                                                //manager.enableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                TctWrapperManager.enableCellBroadcastRange(0, 999, subId);
                                                //[modify]-end-by-chaobing.huang-01102017-defect3992285
                                            }
                                            //if channel index is more than 999, if it is selected, enabled it , else disabled
                                            if(cbi != null){
                                                int num = cbi.length;
                                                for (int j=0; j<num;j++) {
                                                    int index = cbi[j].getFromServiceId();
                                                    if (index >= 1000 && !mChannelExFlag || mChannelExFlag && (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= index
                                                            && index <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER || SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= index
                                                            && index <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE)) {
                                                            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                                        if (cbi[j].isSelected()) {
                                                        	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                                            //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                        	TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                                        } else {
                                                            //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                        	TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                                            //[modify]-end-by-chaobing.huang-01102017-defect3992285
                                                        }
                                                    }
                                                }
                                            }
                                        }else{
                                            //channelMode is "My channel list"
                                            if(cbi != null){
                                                     int num = cbi.length;
                                                     for (int j=0; j<num;j++) {
                                                         int index = cbi[j].getFromServiceId();
                                                         if (cbi[j].isSelected()) {
                                                        	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                                             //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                        	 TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                                         } else {
                                                             /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                                             if (!(isBrazil50CbSupported && index == 50)) {
                                                            	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                                                 //manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                                            	 TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                                             } else {
                                                                 Log.d(TAG, "G isBrazil50CbSupported is true and index == 50, ignore");
                                                             }
                                                             /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                                         }
                                                     }
                                                 }
                                        }
                                    }
                                }
                            }
                    }else{
//                    	MtkSmsManager manager = MtkSmsManager.getDefault();
                    	subId = SubscriptionManager.getDefaultSubscriptionId();
                        CBSUtills cbu = new CBSUtills(mContext);
                        SmsBroadcastConfigInfo[] cbi = cbu.getSmsBroadcastConfigInfo();
                        channelModeSim = mSettings.getString("pref_key_choose_channel", mDefaultMode); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
                        Boolean enable = mSettings.getBoolean("cb_enabled", false);
                        TLog.i(TAG,"startCbConfig-onesimcard-enable="+enable);
                      //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,09/23/2014,795416,after changed simcard,add perso control the CB value
                        if(!enable){
                            return;
                        }
                      //[BUGFIX]-Add-END by TSCD.fujun.yang
                        if ("0".equalsIgnoreCase(channelModeSim)){
                            //channelMode is 0-999, enabled it
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                            if (mChannelExFlag) {
                            	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                //0 ~ 4351
                                //manager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                            	//4357 ~ 4369
                                //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                            	//4400 ~ 65534
                                //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                            }else{
                                //manager.enableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.enableCellBroadcastRange(0, 999, subId);
                                //[modify]-end-by-chaobing.huang-01102017-defect3992285
                            }
                            //if channel index is more than 999, if it is selected, enabled it , else disabled
                            if(cbi != null){
                                int num = cbi.length;
                                for (int i=0; i<num;i++) {
                                    int index = cbi[i].getFromServiceId();
                                    if (index >= 1000 && !mChannelExFlag || mChannelExFlag && (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= index
                                            && index <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER || SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= index
                                            && index <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE)) {
                                            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                        if (cbi[i].isSelected()) {
                                        	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                                            //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                        } else {
                                            //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                            //[modify]-end-by-chaobing.huang-01102017-defect3992285
                                        }
                                    }
                                }
                            }
                        }else{
                            //channelMode is "My channel list"
                            if(cbi != null){
                                     int num = cbi.length;
                                     for (int i=0; i<num;i++) {
                                         int index = cbi[i].getFromServiceId();
                                         if (cbi[i].isSelected()) {
                                        	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                             //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                        	 TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                                         } else {
                                             /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                             if (!(isBrazil50CbSupported && index == 50)) {
                                            	 //[modify]-by-chaobing.huang-01102017-defect3992285
                                                 //manager.disableCellBroadcastRange(index, index, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                            	 TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                                             } else {
                                                 Log.d(TAG, "G isBrazil50CbSupported is true and index == 50, ignore");
                                             }
                                             /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                         }
                                     }
                                 }
                        }
                    }
            }
              //[FEATURE]-Add-END by TSCD.fujun.yang
        }
    }

    private static class ServiceStateListener extends PhoneStateListener {
        private final Context mContext;
        private int mServiceState = -1;

        ServiceStateListener(Context context) {
            mContext = context;
        }

        ServiceStateListener(Context context, int subscription) {
            mContext = context;
            mSubscription = subscription;
        }

        @Override
        public void onServiceStateChanged(ServiceState ss) {
            int newState = ss.getState();
            if (newState != mServiceState) {
                TLog.d(TAG, "Service state changed! " + newState + " Full: " + ss);
                mServiceState = newState;
                if (newState == ServiceState.STATE_IN_SERVICE ||
                        newState == ServiceState.STATE_EMERGENCY_ONLY) {
                    if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                        TLog.d(TAG, "Service state changed for Subscription: " + mSubscription);
                        startConfigService(mContext, mSubscription);
                    } else {
                        startConfigService(mContext);
                    }
                }
            }
        }
    }

    private static void log(String msg) {
        TLog.d(TAG, msg);
    }

    private static void loge(String msg) {
        TLog.e(TAG, msg);
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-29,BUG-2845457*/
    private boolean setCustomized(String persoChannelNames, String persoChannelNumbersAndPolicy ,Context mContext ,int simFlag) {
        int namesLen = persoChannelNames.trim().length();
        int numbersLen = persoChannelNumbersAndPolicy.trim().length();
        int channellengh = namesLen;

        // step 1a, check the if the settings is valid
        if((namesLen == 0 && numbersLen != 0)
                || (numbersLen == 0 && namesLen != 0)) {
            if (DBG) TLog.e(TAG, "detected length not equal, please check the perso settings for flag " + simFlag);
        } else if(namesLen == 0 && numbersLen == 0) {
            if (DBG) TLog.i(TAG, "empty customized channalfor flag " + simFlag);
            return false;
        } else {
            String[] channelNames = persoChannelNames.split(";");
            String[] channelNumbersAndPolicy = persoChannelNumbersAndPolicy.split(";");

            int len = channelNumbersAndPolicy.length;
            String[] channelNumbers = new String[len];
            String[] channelPolicy = new String[len];
            channellengh = channelNumbers.length;//[BUGFIX]-Add- by TCTNB.yugang.jia,01/08/2014,PR582740
            if (channelNames.length != channelNumbers.length) {
               System.out.println("detected wrong perso values, please check the perso settings");
               //[FEATURE]-begin-MOD by TCTNB.yugang.jia,09/06/2013,FR-516039,
               channellengh = channelNumbers.length>channelNames.length?channelNames.length:channelNumbers.length;
               //return;
            }

            for (int i = 0; i < len; i++) {
                String[] channelInfo = channelNumbersAndPolicy[i].split(",");
                if (channelInfo.length == 2) {
                    channelNumbers[i] = channelInfo[0].trim();
                    channelPolicy[i] = channelInfo[1].trim();
                    //[FEATURE]-begin-MOD by TCTNB.yugang.jia,09/06/2013,FR-516039,
                    if(channelPolicy[i].equals("+")) channelPolicy[i] = "Enable";
                    else if(channelPolicy[i].equals("-")) channelPolicy[i] = "Disable";
                    if (DBG) TLog.i(TAG, "get right channel number and policy: " + channelNumbers[i] + "," + channelPolicy[i]);
                } else {
                    if (DBG) TLog.e(TAG, "detected wrong number and policy, please check the perso settings");
                    return false;
                }
            }

            // modify by liang.zhang for Defect 5317399 at 2017-09-19 begin
            CBSUtills utils = new CBSUtills(mContext);
            // second, save channels into databases
            //[FEATURE]-begin-MOD by TCTNB.yugang.jia,09/06/2013,FR-516039,
            int length = channellengh;
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    ContentValues values = new ContentValues();
                    values.put(CellBroadcast.Channel.NAME, channelNames[i].trim());
                    values.put(CellBroadcast.Channel.INDEX, channelNumbers[i]);
                    values.put(CellBroadcast.Channel.Enable, channelPolicy[i]);
                    if(TelephonyManager.getDefault().isMultiSimEnabled()){
                        for(int j = 0; j < TelephonyManager.getDefault().getPhoneCount();j++){
                            if(mEnableSingleSIM && j >= 1){
                                continue;
                            }
                            if(simFlag > 0 && simFlag != j){
                                continue;
                            }
                            Uri uri = Uri.withAppendedPath(CellBroadcast.Channel.CONTENT_URI, "sub"+j);
                            Cursor c = null;
                            try {
                            	c = mContext.getContentResolver().query(uri, null, CellBroadcast.Channel.INDEX + " =" + utils.sqlText(channelNumbers[i]) , null, null);
                                if (c != null && c.getCount() > 0) {
                                	continue;
                                }
                            } catch (Exception ex) {
                            	TLog.i(TAG, "There is a exception while query, exception is " + ex.getMessage());
                            } finally {
                            	if (c != null) {
                            		c.close();
                            	}
                            }
                            mContext.getContentResolver().insert(uri,values);
                        }
                    }else{
                        if(simFlag == 1){
                            return false;
                        }
                        Cursor c = null;
                        try {
                        	c = mContext.getContentResolver().query(CellBroadcast.Channel.CONTENT_URI, null, CellBroadcast.Channel.INDEX + " =" + utils.sqlText(channelNumbers[i]) , null, null);
                        	if (c != null && c.getCount() > 0) {
                            	continue;
                            }
                        } catch (Exception ex) {
                        	TLog.i(TAG, "There is a exception while query, exception is " + ex.getMessage());
                        } finally {
                        	if (c != null) {
                        		c.close();
                        	}
                        }
                        
                        mContext.getContentResolver().insert(CellBroadcast.Channel.CONTENT_URI,
                            values);
                    }
                }
            }
            //[FEATURE]-END-MOD by TCTNB.yugang.jia,09/06/2013,FR-516039,
            // modify by liang.zhang for Defect 5317399 at 2017-09-19 end
        }
        return true;
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/

    //[FEATURE]-Add-BEGIN by TSNJ.Anming.Wei,09/04/2013,FR-473254
    /*
     * set the customized cell broadcast channel from person at first boot
     * @param mContext
     */
    private void setCustomizedChannels(Context mContext) {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);

        boolean bResetCbNvValue = settings.getBoolean(CONFIG_RESET_CBNV_VELUE, true);
        TLog.i(TAG," bResetCbNvValue >>>> "+bResetCbNvValue);
        if (bResetCbNvValue) {
            // step 1, get the channel name and number from perso
            String persoChannelNames = mContext.getResources().getString(
                    R.string.def_cellbroadcastreceiver_customized_channel_names);
            String persoChannelNumbersAndPolicy = mContext.getResources().getString(
                    R.string.def_cellbroadcastreceiver_customized_channel_numbers_policy);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-29,BUG-2845457*/
            String persoChannelNames1 = mContext.getResources().getString(
                    R.string.def_cellbroadcastreceiver_name1);
            String persoChannelNumbersAndPolicy1 = mContext.getResources().getString(
                    R.string.def_cellbroadcastreceiver_chn1);
            String persoChannelNames2 = mContext.getResources().getString(
                    R.string.def_cellbroadcastreceiver_name2);
            String persoChannelNumbersAndPolicy2 = mContext.getResources().getString(
                    R.string.def_cellbroadcastreceiver_chn2);
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/

            //[FEATURE]-ADD-BEGIN by TSCD.tianming.lei,07/15/2014,708134
            if(SystemProperties.getBoolean("ro.ssv.enabled", false)){
                String cMccMnc = SystemProperties.get("persist.sys.lang.mccmnc","");
                String operator = SystemProperties.get("ro.ssv.operator.choose","");
                boolean resetCb = settings.getBoolean(PRE_RESET_CHANNEL, false);
                TLog.i(TAG,"setCustomizedChannels:lang.mccmnc = "+cMccMnc + "  operator = "+operator);
                TLog.i(TAG, "resetCb = "+resetCb+"   appNeedChange = "+appNeedChange);
                if(resetCb || appNeedChange){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-08-31,BUG-2813031*/
                    if(TelephonyManager.getDefault().isMultiSimEnabled()){
                        for(int j = 0; j < TelephonyManager.getDefault().getPhoneCount();j++){
                            if(mEnableSingleSIM && j >= 1){
                                continue;
                            }
                            Uri uri = Uri.withAppendedPath(CellBroadcast.Channel.CONTENT_URI, "sub"+j);
                            mContext.getContentResolver().delete(uri, null, null);
                        }
                    }else{
                        mContext.getContentResolver().delete(CellBroadcast.Channel.CONTENT_URI, null, null);
                    }
                    SharedPreferences.Editor editor = settings.edit();
                    TLog.i(TAG,"load ssv channel: name = "+persoChannelNames+"       number = "+persoChannelNumbersAndPolicy);
                    editor.putBoolean(PRE_RESET_CHANNEL, false);
                    editor.commit();
                }
            }
            //[FEATURE]-ADD-END by TSCD.tianming.lei
            if (DBG) TLog.i(TAG, "customized channel names: " + persoChannelNames);
            if (DBG) TLog.i(TAG, "customized channel numbers: " + persoChannelNumbersAndPolicy);

            int numbersLen = persoChannelNumbersAndPolicy.trim().length();
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-29,BUG-2845457*/
            int numbersLen1 = persoChannelNumbersAndPolicy1.trim().length();
            int numbersLen2 = persoChannelNumbersAndPolicy2.trim().length();
            if (numbersLen != 0) {
                Log.i(TAG, "if numbersLen = "+numbersLen);
                if(!setCustomized(persoChannelNames, persoChannelNumbersAndPolicy ,mContext,-1)){
                    return;
                }
            }else{
                Log.i(TAG, "else persoChannelNames1="+persoChannelNames1+";persoChannelNumbersAndPolicy1 = "+persoChannelNumbersAndPolicy1 );
                Log.i(TAG, "else persoChannelNames1="+persoChannelNames2+";persoChannelNumbersAndPolicy1 = "+persoChannelNumbersAndPolicy2 );
                if(!setCustomized(persoChannelNames1, persoChannelNumbersAndPolicy1 ,mContext,0) && !setCustomized(persoChannelNames2, persoChannelNumbersAndPolicy2 ,mContext,1)){
                    return;
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                }
            }

            // at last, set check flag
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(CONFIG_RESET_CBNV_VELUE, false);
            editor.commit();
        }
    }

    private void resetCMASSettingsIfSimCardChanged(Context mContext) {//Modify by chenglin.jiang for FR1022611
        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(
                Context.TELEPHONY_SERVICE);

        String currentSerialNumber = tm.getSimSerialNumber();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String lastSerialNumber = sp.getString(KEY_LAST_SIM_CARD_SERIAL_NUMBER, "");

        if(!lastSerialNumber.equals(currentSerialNumber)) {
            sp.edit().clear().commit();
            sp.edit().putString(KEY_LAST_SIM_CARD_SERIAL_NUMBER, currentSerialNumber).commit();
            //Modify by chenglin.jiang for FR1022611 Begin
            if(mContext.getResources().getBoolean(R.bool.cellbroadcastreceiver_tf_resetcmas)){
                initEmergencyPref(mContext);
            }
            //Modify by chenglin.jiang for FR1022611 End
            startConfigService(mContext);
        }
    }
    private void initEmergencyPref(Context context){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-17,BUG-1112693*/
        boolean allowWpas = context.getResources().getBoolean(R.bool.def_enable_wpas_function);
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(context)) {
        	allowWpas = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        boolean showSpeechAlert = context.getResources().getBoolean(R.bool.def_showSpeechAlert);
        if(TelephonyManager.getDefault().isMultiSimEnabled()){
            TelephonyManager tm = TelephonyManager.getDefault();
            for(int i = 0 ; i <  tm.getPhoneCount() ; i ++){
                Log.i(TAG,"restore CB default--multisimcard");
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                if(mEnableSingleSIM && i >= 1){
                    continue;
                }
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + i, true).apply();
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + i, true).apply();
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_AMBER_ALERTS + i, true).apply();
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS+ i,false).apply();
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS + i,false).apply();
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS + i,false).apply();// [BUGFIX]-Add by bin.xue for PR1077032
                boolean showEmergencyAlert = context.getResources().getBoolean(R.bool.def_emergencyAlert);
                if (!showEmergencyAlert) {
                    sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_EMERGENCY_ALERTS + i ,true).apply();
                }else{
                    sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_EMERGENCY_ALERTS + i ,true).apply();
                }
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_VIBRATE + i ,true).apply();
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_AUDIO + i ,true).apply();//aiyan-978029
                if (!showSpeechAlert) {
                    sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH + i ,false).apply();
                }else{
                    sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH + i ,true).apply();
                }
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
                if (!allowWpas) {
                    sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH_EX + i ,false).apply();
                }else{
                    sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH_EX + i ,true).apply();
                }
                /* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ETWS_TEST_ALERTS + i ,context.getResources().getBoolean(R.bool.def_etws_test_alert_default_on)).apply(); // MODIFIED by bin.huang, 2016-11-04,BUG-3333029
            }
        }else{
            Log.i(TAG,"restore CB default--onesimcard"); // MODIFIED by yuxuan.zhang, 2016-06-17,BUG-1112693
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + PhoneConstants.SUB1, true).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + PhoneConstants.SUB1, true).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_AMBER_ALERTS + PhoneConstants.SUB1, true).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS + PhoneConstants.SUB1,false).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS + PhoneConstants.SUB1,false).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS + PhoneConstants.SUB1,false).apply();// [BUGFIX]-Add by bin.xue for PR1077032
            boolean showEmergencyAlert = context.getResources().getBoolean(R.bool.def_emergencyAlert);
            if (!showEmergencyAlert) {
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_EMERGENCY_ALERTS + PhoneConstants.SUB1 ,true).apply();
            }else{
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_EMERGENCY_ALERTS + PhoneConstants.SUB1 ,true).apply();
            }
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_VIBRATE + PhoneConstants.SUB1 ,true).apply();
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_AUDIO + PhoneConstants.SUB1 ,true).apply();//aiyan-978029
            if (!showSpeechAlert) {
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH + PhoneConstants.SUB1 ,false).apply();
            }else{
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH + PhoneConstants.SUB1 ,true).apply();
            }
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
            if (!allowWpas) {
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH_EX + PhoneConstants.SUB1 ,false).apply();
            }else{
                sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH_EX + PhoneConstants.SUB1 ,true).apply();
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
            sharedPrefs.edit().putBoolean(CellBroadcastSettings.KEY_ENABLE_ETWS_TEST_ALERTS + PhoneConstants.SUB1 ,context.getResources().getBoolean(R.bool.def_etws_test_alert_default_on)).apply(); // MODIFIED by bin.huang, 2016-11-04,BUG-3333029
        }
    }
    //[BUGFIX]-Add-END by TSCD.tianming.lei,02/05/2015, PR-901210

    // PR-1052792-david.zhang-001 begin
    private void setCMASAlertPref(Context context, long sub, String alertType, boolean alertValue) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if ("RMT".equalsIgnoreCase(alertType)) {
            editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS + sub, alertValue);
        } else if ("EXERCISE".equalsIgnoreCase(alertType)) {
            editor.putBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS + sub,
                    alertValue);
        }
        editor.commit();
    }
    // PR-1052792-david.zhang-001 end
}
