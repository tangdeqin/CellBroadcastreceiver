/******************************************************************************/
/*                                                               Date:04/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  bo.xu                                                           */
/*  Email  :  Bo.Xu@tcl-mobile.com                                            */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 04/06/2013|        bo.xu         |      FR-400302       |[SMS]Cell broadc- */
/*           |                      |                      |ast SMS support   */
/* ----------|----------------------|----------------------|----------------- */
/* 04/11/2013|     Dandan.Fang      |       FR400297       |CBC notification  */
/*           |                      |                      |with pop up and   */
/*           |                      |                      |tone alert + vib- */
/*           |                      |                      |rate in CHILE     */
/* ----------|----------------------|----------------------|----------------- */
/* 06/15/2013|        bo.xu         |      CR-451418       |Set dedicated Ce- */
/*           |                      |                      |ll broadcast MI   */
/*           |                      |                      |for Israel Progr- */
/*           |                      |                      |ams               */
/* ----------|----------------------|----------------------|----------------- */
/* 06/27/2013|     Dandan.Fang      |       PR477879       |[CB]Mobile recei- */
/*           |                      |                      |ve CB when disab- */
/*           |                      |                      |le CB             */
/* ----------|----------------------|----------------------|----------------- */
/* 09/06/2013|     yugang.jia       |      FR-516039       |[SMS]Cell broadc- */
/*           |                      |                      |ast SMS support   */
/* ----------|----------------------|----------------------|----------------- */
/* 09/12/2014|      fujun.yang      |        772564        |new CLID variable */
/*           |                      |                      |to re-activate CB */
/*           |                      |                      |for NL            */
/* ----------|----------------------|----------------------|----------------- */
/* 09/22/2014|      tianming.lei    |        793727        |New requirements  */
/*           |                      |                      |for CB Channel    */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;

import java.util.ArrayList;
import java.util.HashMap;
/* MODIFIED-BEGIN by chaobing.huang, 2017-01-17,BUG-4014007*/
import java.util.HashSet;
import java.util.Locale.Category; // MODIFIED by yuxuan.zhang, 2016-09-12,BUG-2845457
import java.util.Set;
/* MODIFIED-END by chaobing.huang,BUG-4014007*/


//[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
import android.app.ActionBar;
//[FEATURE]-Add-END by TSCD.fujun.yang
import android.app.AlertDialog;
import android.content.ContentValues;
//[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
import android.content.Context;
//[FEATURE]-Add-END by TSCD.fujun.yang
import android.content.DialogInterface;
import android.content.Intent;
//[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
import android.content.IntentFilter;
//[FEATURE]-Add-END by TSCD.fujun.yang
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager; // MODIFIED by chaobing.huang, 2017-01-17,BUG-4014007
import android.preference.PreferenceScreen;

import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.cellbroadcastreceiver.CellBroadcast.Channel;

//import mediatek.telephony.MtkSmsManager;
import android.telephony.SubscriptionInfo; // MODIFIED by yuxuan.zhang, 2016-07-19,BUG-2548203

//[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
//CBC notification with pop up and tone alert + vibrate in CHILE
import android.widget.Toast;
//[FEATURE]-Add-END by TCTNB.Dandan.Fang
//[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
//Set dedicated Cell broadcast MI for Israel Programs
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
//[BUGFIX]-Add-END by TCTNB.bo.xu

//[BUGFIX]-Add by TCTNB.bo.xu,06/15/2013,CR-451418,
import com.android.cb.util.TLog;
//[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
import android.view.MenuItem;
//[FEATURE]-Add-END by TSCD.fujun.yang
//[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
import android.content.BroadcastReceiver;
//[FEATURE]-Add-END by TSCD.fujun.yang

import android.telephony.SubscriptionManager;
import com.android.internal.telephony.PhoneConstants;

import android.telephony.TelephonyManager;
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-06-06,BUG-1112693

/* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-1112693*/
import com.tct.util.FwkPlf;
import com.tct.util.IsdmParser;
/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
import com.tct.wrapper.TctWrapperManager;

//Set dedicated Cell broadcast MI for Israel Programs
public class CBMSettingActivity extends PreferenceActivity implements
        Preference.OnPreferenceClickListener, OnPreferenceChangeListener{
    CBSUtills cbu;
    private PreferenceCategory channel_list = null;

    private PreferenceScreen cb_language = null;//[FEATURE]-Del by TCTNB.bo.xu,04/10/2013,FR-400302,delete cb language

    private PreferenceScreen channel_add = null;
    //[BUGFIX]-Del by TCTNB.bo.xu,06/15/2013,CR-451418,
    //Set dedicated Cell broadcast MI for Israel Programs
    //CheckBoxPreference receive_channel = null;

    static final int EDIT_CHANNEL_REQUEST = 0;

    static final int ADD_CHANNEL_REQUEST = 1;

    private static final String LOG_TAG = "CBMSettingActivity";
    /*PR 920761   - SH SW4 Framework - Jerry Zheng - add tag to control debug info */
    static final boolean DEBUG = true;

    String ENABLE = "Enable";

    String DISABLE = "Disable";
    //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
    //Set dedicated Cell broadcast MI for Israel Programs
    private ListPreference mChannelMode = null;
    private static final String PREFS_NAME = "com.android.cellbroadcastreceiver_preferences";
    private String channelMode = "1";
    SharedPreferences settings;
    //[BUGFIX]-Add-END by TCTNB.bo.xu

    private int subDescription = PhoneConstants.SUB1;
    private final String SIM_Language = "content://com.jrd.provider.CellBroadcast/CBLanguage/sub";
    private final String SIM_Channle = "content://com.jrd.provider.CellBroadcast/Channel/sub";
    public static Uri SIMLanguageUri = null;
    public static Uri SIMChannleUri = null;
  //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
    private String READSIMCARDCHANNELACTION = "android.telephony.SmsManager.CBMSettingActivity.ACTION";
    private SimCardChannelReceiver simcardchannelreceiver= null;
    private boolean mEnableSingleSIM = false;
    private String mDefaultMode = "1"; // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
    private boolean mChannelExFlag = false;

    private class SimCardChannelReceiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TLog.i(LOG_TAG,"receiver--readsimcardchannel-done");
            if(READSIMCARDCHANNELACTION.equals(action)){
                if (null != listener) {
                    try {
                        int num = intent.getIntExtra("num",0);
                        String cbindex = intent.getStringExtra("cbindex");
                        String cbable = intent.getStringExtra("cbable");
                        if (true == DEBUG) TLog.i(LOG_TAG,"num="+num+" cbindex="+cbindex+" cbable"+cbable);
                        listener.onFinished(num,cbindex,cbable);
                    } catch (RemoteException e) {
                        // ignore it
                    }
                }
            }
        }
    }
    public void registerSimCardChannelRecevier(){
        Log.i(LOG_TAG,"registerReceiver-simcardchannelrecevier"); // MODIFIED by yuxuan.zhang, 2016-06-12,BUG-1112693
      //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/15/2014,772564,new CLID variable to re-activate CB for NL
        if(simcardchannelreceiver == null){
            simcardchannelreceiver = new SimCardChannelReceiver();
        }
      //[FEATURE]-Add-END by TSCD.fujun.yang
        IntentFilter filter = new IntentFilter();
        filter.addAction(READSIMCARDCHANNELACTION);
        registerReceiver(simcardchannelreceiver,filter);
    }
  //[FEATURE]-Add-END by TSCD.fujun.yang
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.cbsetting_preference);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-1112693*/
        mEnableSingleSIM = IsdmParser.getBooleanFwk(this,
                FwkPlf.def_cellbroadcast_enable_single_sim, false);
        mChannelExFlag = getResources().getBoolean(R.bool.def_expand_normal_cb_channel); // MODIFIED by yuxuan.zhang, 2016-09-20,BUG-2845457
        mDefaultMode = getResources().getString(R.string.def_is_all_channel_mode_cellbroadcast); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
        Log.i(LOG_TAG,"mEnableSingleSIM"+mEnableSingleSIM);
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

        if(TelephonyManager.getDefault().isMultiSimEnabled()){
            Intent intent = getIntent();
            subDescription = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, PhoneConstants.SUB1);
            if(mEnableSingleSIM){
                subDescription = PhoneConstants.SUB1;
            }
            TLog.i(LOG_TAG,"multisimcard-subDescription"+subDescription);
            SIMLanguageUri = Uri.parse(SIM_Language+subDescription);
            SIMChannleUri = Uri.parse(SIM_Channle+subDescription);
            if (true == DEBUG) {
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-1112693*/
                Log.i(LOG_TAG,"onCreate-subDescription="+subDescription);
                Log.i(LOG_TAG,"onCreate-SIMLanguageUri="+SIMLanguageUri);
                Log.i(LOG_TAG,"-onCreate-SIMChannleUri="+SIMChannleUri);
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            }
        }

        //[BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,09/22/2014,793727
        boolean registReceiver = getResources().getBoolean(R.bool.def_registerSimCardChannelReceiver_on);
        if(registReceiver){
            //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
            registerSimCardChannelRecevier();
            //[FEATURE]-Add-END by TSCD.fujun.yang
        }
        //[BUGFIX]-Mod-End by TSCD.tianming.lei
        cbu = new CBSUtills(this);
        initClicker();
        initCBLanguage();//[FEATURE]-Del by TCTNB.bo.xu,04/10/2013,FR-400302,delete cb language

        // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879,
        // [CB][mobile receive CB when disable CB
        // initChannelList();
        // [BUGFIX]-Add-END by TCTNB.Dandan.Fang

        // sync db data with sim card cb data
        //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
        //Set dedicated Cell broadcast MI for Israel Programs
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
        settings = this.getSharedPreferences(PREFS_NAME, 0);
        channelMode = settings.getString("pref_key_choose_channel", mDefaultMode);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-12,BUG-2845457*/
        mChannelMode = (ListPreference) findPreference("pref_key_choose_channel");
        // [BUGFIX]-Add-BEGIN by yuwan,04/28/2017,4582710
        if (mChannelExFlag){
            mChannelMode.setEntries(R.array.pref_cb_channel_mode_entries_ex);
        }else{
            mChannelMode.setEntries(R.array.pref_cb_channel_mode_entries);
        }
        // [BUGFIX]-Add-END by yuwan,04/28/2017,4582710
        if (getResources().getBoolean(R.bool.def_is_show_cellbroadcast_channel_mode_menu)) {
            mChannelMode.setOnPreferenceChangeListener(this);
            updataChannelMode(channelMode);
        } else {
            PreferenceCategory cbm = (PreferenceCategory) findPreference("cbsetting_prefcategory");
            cbm.removePreference(mChannelMode);
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
        SharedPreferences.Editor editor = settings.edit();
        Log.w(LOG_TAG, "TelephonyManager.getDefault().isMultiSimEnabled() = "+TelephonyManager.getDefault().isMultiSimEnabled());
        if(TelephonyManager.getDefault().isMultiSimEnabled()){


            if(settings.getString("pref_key_choose_channel_sim2", "FF").equals("FF")){
                editor.putString("pref_key_choose_channel_sim2", mDefaultMode);
            }
            if(settings.getString("pref_key_choose_channel_sim1", "FF").equals("FF")){
                editor.putString("pref_key_choose_channel_sim1", mDefaultMode);
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            }
           editor.commit();
        }

        //[BUGFIX]-Add-END by TCTNB.bo.xu
        //[BUGFIX]-Mod-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
        //Set dedicated Cell broadcast MI for Israel Programs

//        MtkSmsManager manager = null;
        if(TelephonyManager.getDefault().isMultiSimEnabled() && subDescription != -1){
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-19,BUG-2548203*/
            SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
            SubscriptionInfo mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(subDescription);
            int subId = -1002;
            if (mSubInfoRecord != null) {
                subId = mSubInfoRecord.getSubscriptionId();
                Log.i(LOG_TAG, "get CellBroadcastConfig subId = " + subId);
            }else{
                Log.w(LOG_TAG, "subDescription:"+subDescription + " is not active");
            }
//            manager = MtkSmsManager.getSmsManagerForSubscriptionId(subId);
            /* MODIFIED-END by yuxuan.zhang,BUG-2548203*/
            if(subDescription == 0){
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                channelMode = settings.getString("pref_key_choose_channel_sim1", mDefaultMode);
            }else if(subDescription == 1){
                channelMode = settings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            }
            if ("1".equalsIgnoreCase(channelMode)) {
                //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
                //getCBMConfig();
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-06,BUG-1112693*/
                Log.i(LOG_TAG,"oncreate--read-channel-subDescription="+subDescription);
                try {
                    TctWrapperManager.getCellBroadcastConfig(subId);
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-28,BUG-2422769*/
                } catch (Throwable e) {
                    Log.w(LOG_TAG, "fail to get CellBroadcastConfig");
                    /* MODIFIED-END by yuxuan.zhang,BUG-2422769*/
                }
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
              //[FEATURE]-Add-END by TSCD.fujun.yang
            }
        }else{
//            manager = MtkSmsManager.getDefault();
        	int subId = SubscriptionManager.getDefaultSubscriptionId();
            if ("1".equalsIgnoreCase(channelMode)) {
                //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
                //getCBMConfig();
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-06,BUG-1112693*/
                Log.i(LOG_TAG,"oncreate--read-channel");
                try {
                    TctWrapperManager.getCellBroadcastConfig(subId);
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-28,BUG-2422769*/
                } catch (Throwable e) {
                    Log.w(LOG_TAG, "fail to getCellBroadcastConfig");
                    /* MODIFIED-END by yuxuan.zhang,BUG-2422769*/
                }
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
              //[FEATURE]-Add-END by TSCD.fujun.yang
            }
        }

        //[BUGFIX]-Add-END by TCTNB.bo.xu

        // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879 ,
        // [CB][mobile receive CB when disable CB
        hideMyChannelList();
        // [BUGFIX]-Add-END by TCTNB.Dandan.Fang
      //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back); // MODIFIED by yuxuan.zhang, 2016-06-17,BUG-1112693
        //[FEATURE]-Add-END by TSCD.fujun.yang
    }
  //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
  //[FEATURE]-Add-END by TSCD.fujun.yang
  //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/15/2014,772564,new CLID variable to re-activate CB for NL
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(simcardchannelreceiver != null){
            TLog.i(LOG_TAG,"unregisterReceiver-simcardchannelreceiver");
            unregisterReceiver(simcardchannelreceiver);
            simcardchannelreceiver = null;
        }
    }
  //[FEATURE]-Add-END by TSCD.fujun.yang
    void initClicker() {
        channel_add = (PreferenceScreen) findPreference("channel_add");
        channel_add.setOnPreferenceClickListener(this);
//[FEATURE]-Del-BEGIN by TCTNB.bo.xu,04/10/2013,FR-400302,delete cb language
        cb_language = (PreferenceScreen) findPreference("cb_language");
        cb_language.setOnPreferenceClickListener(this);
//[FEATURE]-Del-END by TCTNB.bo.xu
          //[BUGFIX]-Del by TCTNB.bo.xu,06/15/2013,CR-451418,
          //Set dedicated Cell broadcast MI for Israel Programs
          //receive_channel = (CheckBoxPreference) findPreference("receive_channel");
          //receive_channel.setOnPreferenceClickListener(this);
    }
//[FEATURE]-Del-BEGIN by TCTNB.bo.xu,04/10/2013,FR-400302,delete cb language
    private void initCBLanguage() {
        int k;
        if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
            k = cbu.queryCBLanguage(subDescription);
        }else{
            k = cbu.queryCBLanguage();
        }
        log("cbLanguage is : " + k);
        // String s =
        // "German,English,Italian,French,Spanish,Dutch,Swedish,Danish,Portuguese,Finnish,Norwegian,Greek,Turkish,Hungarian,Polish,All languages";
        // String[] sa = s.split(",");

        String[] sa = getResources().getStringArray(R.array.cb_language_items);
        String lan;
        lan = sa[k];
        cb_language.setSummary(lan);
    }
//[FEATURE]-Del-END by TCTNB.bo.xu
    private void initChannelList() {
        channel_list = (PreferenceCategory) findPreference("channel_list");
        channel_list.removeAll();
        Cursor c = null;
        if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
            c = cbu.queryChannel(subDescription);
        }else{
            c = cbu.queryChannel();
        }
        if (true == DEBUG) TLog.i(LOG_TAG, "initChanneList--enter");
        if(c == null){
            TLog.i(LOG_TAG, "initChanneList--return");
            return;
        }
        String channelName = "";
        String channelEnable = "";
        String channelId = "";
        String channelIndex = "";

        //[BUGFIX]-Add-BEGIN by TCTNB.meng.tong,12/29/2012,353549,
        //Cell broadcast channel list error
        boolean selected = false;
        boolean isSelectAll = true;
        //[BUGFIX]-Add-END by TCTNB.meng.tong

        if (c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                channelName = "";
                channelEnable = "";
      //[BUGFIX]-Mod-BEGIN by TCTNB.YuTao.Yang,12/26/2012,380033,Modify preference summary language display
                String openStatus = "";
                channelId = "";
                channelIndex = "";
                Preference channelPref = new Preference(this);
                channelName = c.getString(c.getColumnIndex(Channel.NAME));
                channelId = c.getString(c.getColumnIndex(Channel._ID));
                channelIndex = c.getString(c.getColumnIndex(Channel.INDEX));
                channelEnable = c.getString(c.getColumnIndex(Channel.Enable));

                channelPref.setKey(channelId + "");// channel index
                channelPref.setTitle(channelName + "(" + channelIndex + ")");// channel
                // name(index)

                if (channelEnable.equalsIgnoreCase(ENABLE)) {
                    openStatus = getString(R.string.enable).toString();

                    //[BUGFIX]-Add-BEGIN by TCTNB.meng.tong,12/29/2012,353549,
                    //Cell broadcast channel list error
                    selected = true;
                    //[BUGFIX]-Add-END by TCTNB.meng.tong

                } else if (channelEnable.equalsIgnoreCase(DISABLE)) {
                    openStatus = getString(R.string.disable).toString();

                    //[BUGFIX]-Add-BEGIN by TCTNB.meng.tong,12/29/2012,353549,
                    //Cell broadcast channel list error
                    selected = false;
                    //[BUGFIX]-Add-END by TCTNB.meng.tong

                }

                //[BUGFIX]-Add-BEGIN by TCTNB.meng.tong,12/29/2012,353549,
                //Cell broadcast channel list error
                isSelectAll = isSelectAll & selected;
                //[BUGFIX]-Del by TCTNB.bo.xu,06/15/2013,CR-451418,
                //Set dedicated Cell broadcast MI for Israel Programs
                //receive_channel.setChecked(isSelectAll);
                //[BUGFIX]-Add-END by TCTNB.meng.tong

                channelPref.setSummary(openStatus);// channel enable/disable
      //[BUGFIX]-Mod-END by TCTNB.YuTao.Yang

                channelPref.setOnPreferenceClickListener(this);
                //[FEATURE]-Add-BEGIN by TCTNB.ye.shen,12/12/2012,FR-313552,
                //Cell broadcast SMS support
                Boolean isCmas = false;
                if(channelIndex.length() == 4)
                {//check for CMAS
                    int channelIndexNumber=Integer.parseInt(channelIndex);
                        //PR 1054793 Modified by fang.song begin
                        //[add]-begin-by-chaobing.huang-01132017-defect4007891
                        //if ("true".equalsIgnoreCase(SystemProperties.get(CBSUtills.PRE_DEFINE_CHANNEL_4371, "false"))) {
                       if (getResources().getBoolean(R.bool.ro_cb_prechannel4371)) {
                        //[add]-begin-by-chaobing.huang-01132017-defect4007891
                            if(channelIndexNumber >= 4370 && channelIndexNumber <= 4381 && channelIndexNumber != 4371)
                                isCmas = true;
                        } else {
                            if(channelIndexNumber >= 4370 && channelIndexNumber <= 4381)
                                isCmas = true;
                        }
                        //PR 1054793 Modified by fang.song end
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-19,BUG-1112693*/
                        if(getResources().getBoolean(R.bool.def_allow_customize_emergency_channels)){
                        if (channelIndexNumber == 4370 || channelIndexNumber == 4372
                                || channelIndexNumber == 4373) {
                            isCmas = false;
                        }
                        }
                        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                }
                if(!isCmas){
                //[FEATURE]-Add-END by TCTNB.ye.shen

                // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879 ,
                // [CB][mobile receive CB when disable CB
                // channel_list.addPreference(channelPref);
                if(TelephonyManager.getDefault().isMultiSimEnabled()){
                     if(subDescription == 0){
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                        channelMode = settings.getString("pref_key_choose_channel_sim1", mDefaultMode);
                    }else if(subDescription == 1){
                        channelMode = settings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                    }
                }else{
                    channelMode = settings.getString("pref_key_choose_channel",mDefaultMode);
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                }
                }
                if (true == DEBUG) TLog.i(LOG_TAG,"channelMode="+channelMode);
                if ("1".equalsIgnoreCase(channelMode)) {
                    // channel mode is "my channel list", display all default channel and channel added by user
                    if (!isCmas) {
                        channel_list.addPreference(channelPref);
                    }
                } else {
                    // channel mode is "0-999", display default channel which index >1000
                    if (channelIndex.length() == 4 && !isCmas) {
                        channel_list.addPreference(channelPref);
                    }
                }
                // [BUGFIX]-Add-END by TCTNB.Dandan.Fang

                c.moveToNext();
            }
        }
        c.close();

    }

    Preference preferenceModify;

    String channelIdModify = "";

    AlertDialog ad;

    public boolean onPreferenceClick(Preference preference) {
        if (preference == channel_add) {
            Log.i(LOG_TAG, "channel_add"); // MODIFIED by yuxuan.zhang, 2016-06-28,BUG-1112693
            Intent in = new Intent(CBMSettingActivity.this, ChannelSetActivity.class);
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                in.putExtra("channeladd",true);
                in.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subDescription);
            }
            startActivityForResult(in, ADD_CHANNEL_REQUEST);
//[FEATURE]-Del-BEGIN by TCTNB.bo.xu,04/10/2013,FR-400302,delete cb language
        } else if (preference == cb_language) {
            int k;
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                k = cbu.queryCBLanguage(subDescription);
            }else{
                k = cbu.queryCBLanguage();
            }
            ad = new AlertDialog.Builder(CBMSettingActivity.this)
                    .setTitle(R.string.title_language)
                    .setSingleChoiceItems(R.array.cb_language_items, k,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // save value to language db
                                    if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                                        cbu.saveCBLanguage(whichButton + "",subDescription);
                                    }else{
                                        cbu.saveCBLanguage(whichButton + "");
                                    }
                                    initCBLanguage();
                                    ad.dismiss();
                                }
                            })
                    .setNegativeButton(R.string.language_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ad.dismiss();
                                    /* User clicked No so do some stuff */
                                }
                            }).create();
            ad.show();
//[FEATURE]-Del-END by TCTNB.bo.xu
        //[BUGFIX]-Del-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
        //Set dedicated Cell broadcast MI for Israel Programs
        } /*else if (preference == receive_channel) {
            // update channel db enable state
            ContentValues values = new ContentValues();
            values.put(Channel.Enable, receive_channel.isChecked() ? ENABLE : DISABLE);
            cbu.updateChannel(values);
            initChannelList();
            // enable : set channel list enable ,can select some channel
            // disable : set channel list disable,can select some channel
            // set channel to phone
            setCBMConfig();
        //[BUGFIX]-Del-END by TCTNB.bo.xu
        }*/ else if (preference == mChannelMode) {
        }else {
            Log.i(LOG_TAG ,"onPreferenceClick come in"); // MODIFIED by yuxuan.zhang, 2016-10-19,BUG-1112693
            final String channelId = preference.getKey();
            String channelNameTemp = "";
            Cursor c = null;
            String id = "";//[add]-by-chaobing.huang-01.17.2017-defect4005963
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                c = cbu.queryChannel(subDescription);
            }else{
                c = cbu.queryChannel();
            }
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    if (channelId.equalsIgnoreCase(c.getString(c.getColumnIndex(Channel._ID)))) {
                        channelNameTemp = c.getString(c.getColumnIndex(Channel.Enable));
                        id = c.getString(c.getColumnIndex(Channel.INDEX));//[add]-by-chaobing.huang-01.17.2017-defect4005963
                        break;
                    } else {
                        c.moveToNext();
                    }
                }
            }
            final String channelName = channelNameTemp;
            if(c != null){
                c.close();
            }
          //[BUGFIX]-Add-END by TCTNB.YuTao.Yang

            final Preference fpreference = preference;
            final int channel_dialog_items;

            //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
            //CBC notification with pop up and tone alert + vibrate in CHILE
            //SMSCB channels should not be customizable or editable by the end user.
            if (cbu == null) {
                cbu = new CBSUtills(CBMSettingActivity.this);
            }
            //PR 1054793 Added by fang.song begin
            //[add]-begin-by-chaobing.huang-01132017-defect4007891
            //if ("true".equalsIgnoreCase(SystemProperties.get(CBSUtills.PRE_DEFINE_CHANNEL_4371, "false"))) {
            if (getResources().getBoolean(R.bool.ro_cb_prechannel4371) &&
                    !getResources().getBoolean(R.bool.def_isSupport_modify_4371)) {
                // MODIFIED by yuwan, 2017-05-05,BUG-4584748
                //[add]-end-by-chaobing.huang-01132017-defect4007891
                if (((String) fpreference.getTitle()).contains("4371")) {
                    TLog.i(LOG_TAG, "Predefined channels cannot be modified.");
                    //[BUGFIX]-Add by yanxiang.ren, 2015-08-14,PR1066234 Begin
                    Toast.makeText(this, R.string.toast_message_channel_can_not_delete,
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            //PR 1054793 Added by fang.song end
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-19,BUG-1112693*/
                if (cbu.isForbiddenToModifyPredefinedChannels(channelId,subDescription,false)) {
                    log("Predefined channels cannot be modified.");
                    Toast.makeText(this, R.string.toast_message_can_not_modify_channel, Toast.LENGTH_LONG).show();
                    return false;
                }
            }else {
                if(cbu.isForbiddenToModifyPredefinedChannels(channelId,false)) {
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    log("Predefined channels cannot be modified.");
                    Toast.makeText(this, R.string.toast_message_can_not_modify_channel, Toast.LENGTH_LONG).show();
                    return false;
                  //[BUGFIX]-Add by yanxiang.ren, 2015-08-13,PR1066234 end
                }
            }

            //[FEATURE]-Add-END by TCTNB.Dandan.Fang
            //[add]-begin-by-chaobing.huang-01.17.2017-defect4005963
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
            	Log.i(LOG_TAG ,"onPreferenceClick come in====>1");
            	if(subDescription == 0) {
                	Set<String> set1 = new HashSet<String>();
                    set1 = pref.getStringSet("predefined1", null);
                    if(set1 != null && set1.contains(id)) { // MODIFIED by chaobing.huang, 2017-01-18,BUG-4005963
                    	Log.i(LOG_TAG ,"onPreferenceClick come in====>2"+set1);
                        Toast.makeText(this, R.string.toast_message_can_not_modify_channel, Toast.LENGTH_LONG).show();
                        return false;
                    }
            	}else {
                	Set<String> set2 = new HashSet<String>();
                	set2 = pref.getStringSet("predefined2", null);
                    if(set2 != null && set2.contains(id)) { // MODIFIED by chaobing.huang, 2017-01-18,BUG-4005963
                    	Log.i(LOG_TAG ,"onPreferenceClick come in====>"+set2);
                        Toast.makeText(this, R.string.toast_message_can_not_modify_channel, Toast.LENGTH_LONG).show();
                        return false;
                    }
            	}
            }else {
            	Set<String> set1 = new HashSet<String>();
                set1 = pref.getStringSet("predefined1", null);
                Log.i(LOG_TAG ,"onPreferenceClick come in====>"+set1+"channel:"+id);
                if(set1 != null && set1.contains(id)) {
                	Log.i(LOG_TAG ,"onPreferenceClick come in====>"+set1);
                    Toast.makeText(this, R.string.toast_message_can_not_modify_channel, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            //[add]-begin-by-chaobing.huang-01.17.2017-defect4005963

            if (channelName.equalsIgnoreCase(ENABLE)) {
                channel_dialog_items = R.array.channel_dialog_items_disable;
            } else {
                channel_dialog_items = R.array.channel_dialog_items_enable;
            }

            AlertDialog ad = new AlertDialog.Builder(CBMSettingActivity.this)
                    .setTitle(preference.getTitle())
                    .setItems(channel_dialog_items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            log(which + "");
                            // Toast.makeText(CBMSettingActivity.this,which+"",0).show();
                            switch (which) {
                                case 0: {
                                    // disable/enable
                                    String changeEnable = channelName.equalsIgnoreCase(ENABLE) ? DISABLE
                                            : ENABLE;
                                    TLog.i(LOG_TAG,"onclick---disable/enable");
                                  //[BUGFIX]-Mod-BEGIN by TCTNB.YuTao.Yang,12/26/2012,380033,Modify preference summary language display
                                    String openStatus = "";
                                    if (channelName.equalsIgnoreCase(ENABLE)) {
                                        openStatus = getString(R.string.disable).toString();
                                    } else if (channelName.equalsIgnoreCase(DISABLE)) {
                                        openStatus = getString(R.string.enable).toString();
                                    }

                                    // set channel disable/enable into db
                                    ContentValues values = new ContentValues();
                                    values.put(CellBroadcast.Channel.Enable, changeEnable);
                                    if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                                        cbu.updateChannel(channelId, values,subDescription);
                                    }else{
                                        cbu.updateChannel(channelId, values);
                                    }
                                    fpreference.setSummary(openStatus);
                                    // set channel disable/enable into phone

                                    initChannelList();
                                  //[BUGFIX]-Mod-END by TCTNB.YuTao.Yang
                                    //[BUGFIX]-Mod-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
                                    //Set dedicated Cell broadcast MI for Israel Programs
                                    if(TelephonyManager.getDefault().isMultiSimEnabled() ){
                                        if(subDescription == 0){
                                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                                            channelMode = settings.getString("pref_key_choose_channel_sim1", mDefaultMode);
                                        }else if(subDescription == 1){
                                            channelMode = settings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                                            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                                        }
                                        if ("1".equalsIgnoreCase(channelMode)) {
                                            setCBMConfig();
                                        }
                                    }else{
                                        if ("1".equalsIgnoreCase(channelMode)) {
                                            setCBMConfig();
                                        }
                                    }
                                    //[BUGFIX]-Mod-END by TCTNB.bo.xu
                                    break;
                                }
                                case 1:
                                    // edit
                                    // dispisAddlay cbsetactivity
                                    TLog.i(LOG_TAG,"onclick---edit");
                                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-19,BUG-1112693*/
                                    if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                                        if (cbu.isForbiddenToModifyPredefinedChannels(channelId,subDescription,true)) {
                                            log("Predefined channels cannot be modified.");
                                            Toast.makeText(CBMSettingActivity.this, R.string.toast_message_can_not_modify_channel, Toast.LENGTH_LONG).show();
                                            break;
                                        }
                                    }else {
                                        if(cbu.isForbiddenToModifyPredefinedChannels(channelId,true)) {
                                            log("Predefined channels cannot be modified.");
                                            Toast.makeText(CBMSettingActivity.this, R.string.toast_message_can_not_modify_channel, Toast.LENGTH_LONG).show();
                                            break;
                                        }
                                    }
                                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                                    Intent in = new Intent(CBMSettingActivity.this,
                                            ChannelSetActivity.class);
                                    in.putExtra("id", channelId);
                                    if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                                        in.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subDescription);
                                    }
                                    channelIdModify = channelId;
                                    preferenceModify = fpreference;
                                    startActivityForResult(in, EDIT_CHANNEL_REQUEST);
                                    break;
                                case 2: {
                                    TLog.i(LOG_TAG,"onclick---delete");
                                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-19,BUG-1112693*/
                                    if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                                        if (cbu.isForbiddenToModifyPredefinedChannels(channelId,subDescription,true)) {
                                            log("Customized channels cannot be modified for MultiSim.");
                                            Toast.makeText(CBMSettingActivity.this, R.string.toast_message_can_not_delete_channel, Toast.LENGTH_LONG).show();
                                            break;
                                        }
                                    }else {
                                        if(cbu.isForbiddenToModifyPredefinedChannels(channelId,true)) {
                                            log("Customized channels cannot be modified.");
                                            Toast.makeText(CBMSettingActivity.this, R.string.toast_message_can_not_delete_channel, Toast.LENGTH_LONG).show();
                                            break;
                                        }
                                    }
                                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                                    //[BUGFIX]-Add-BEGIN by TCTNB.Tongyuan.Lv, 03/08/2013, PR-408496,
                                    // fix the normal cb channel missed after set up CMAS
                                    // first disable the channel
                                    Cursor c = null;
									if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                                        c = cbu.queryChannel(channelId,subDescription);
                                    }else{
                                        c = cbu.queryChannel(channelId);
                                    }
                                    int index;
                                    if (c != null && c.getCount() > 0) {
                                        c.moveToFirst();
                                        if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                                            /* MODIFIED-BEGIN by yuwan, 2017-06-06,BUG-4886518*/
                                            SubscriptionManager mSubscriptionManager
                                                    = SubscriptionManager
                                                    .from(CBMSettingActivity.this);
                                            SubscriptionInfo mSubInfoRecord = mSubscriptionManager
                                                    .getActiveSubscriptionInfoForSimSlotIndex(
                                                            subDescription);
                                            int subId = -1002;
                                            if (mSubInfoRecord != null) {
                                                subId = mSubInfoRecord.getSubscriptionId();
                                                Log.i(LOG_TAG, "We get CellBroadcastConfig subId = "
                                                        + subId);
                                            } else {
                                                Log.w(LOG_TAG, "The subDescription:"
                                                        + subDescription + " is not active");
                                            }
//                                            MtkSmsManager manager = MtkSmsManager
//                                                    .getSmsManagerForSubscriptionId(subId);
                                                    /* MODIFIED-END by yuwan,BUG-4886518*/
                                            index = Integer.parseInt(c.getString(c.getColumnIndex(Channel.INDEX)));
                                            //[BUGFIX]-Mod-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
                                            //Set dedicated Cell broadcast MI for Israel Programs
                                            if ("1".equalsIgnoreCase(channelMode)) {
                                            	//[modify]-by-chaobing.huang-01102017-defect3992285
                                                //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                            	TctWrapperManager.disableCellBroadcast(index, subId);
                                                Log.d(LOG_TAG,"delete index = "+index);
                                            }
                                        }else{
//                                        	MtkSmsManager manager = MtkSmsManager.getDefault();
                                        	int subId = SubscriptionManager.getDefaultSubscriptionId();
                                            index = Integer.parseInt(c.getString(c.getColumnIndex(Channel.INDEX)));
                                            //[BUGFIX]-Mod-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
                                            //Set dedicated Cell broadcast MI for Israel Programs
                                            if ("1".equalsIgnoreCase(channelMode)) {
                                            	//[modify]-by-chaobing.huang-01102017-defect3992285
                                                //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                                            	TctWrapperManager.disableCellBroadcast(index, subId);
                                                Log.d(LOG_TAG,"delete index = "+index);
                                            }
                                        }
                                        //[BUGFIX]-Mod-END by TCTNB.bo.xu
                                    }

                                    if (c != null) {
                                        c.close();
                                    }
                                    //[BUGFIX]-Add-END by TCTNB.Tongyuan.Lv

                                    // delete channel from db
                                    if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                                        cbu.deleteChannel(channelId,subDescription);
                                    }else{
                                        cbu.deleteChannel(channelId);
                                    }
                                    channel_list.removePreference(fpreference);
                                    // delete channel from phone
                                    //[BUGFIX]-Mod-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
                                    //Set dedicated Cell broadcast MI for Israel Programs
                                    if ("1".equalsIgnoreCase(channelMode)) {
                                        setCBMConfig();
                                    }
                                    //[BUGFIX]-Mod-END by TCTNB.bo.xu
                                    break;
                                }
                                default:
                                    break;

                            }
                        }
                    }).create();
            ad.show();

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("requestCode:" + requestCode + "\n+resultCode:" + resultCode);
        if (requestCode == EDIT_CHANNEL_REQUEST) {
            if (channelIdModify.equals(""))
                return;
            Cursor c = null;
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                c = cbu.queryChannel(channelIdModify,subDescription);
            }else{
                c = cbu.queryChannel(channelIdModify);
            }
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                String channelName = "";
                String channelEnable = "";
                String channelIndex = "";
                channelName = c.getString(c.getColumnIndex(Channel.NAME));
                channelIndex = c.getString(c.getColumnIndex(Channel.INDEX));

              //[BUGFIX]-Add-BEGIN by TCTNB.YuTao.Yang,12/26/2012,380033,Modify preference summary language display
                String channelEnablefTemp = c.getString(c.getColumnIndex(Channel.Enable));
                // init control text
                if (channelEnablefTemp.equalsIgnoreCase(ENABLE)) {
                    channelEnable = getString(R.string.enable).toString();
                } else {
                    channelEnable = getString(R.string.disable).toString();
                }

              //[BUGFIX]-Add-END by TCTNB.YuTao.Yang

                // init control text
                preferenceModify.setTitle(channelName + "(" + channelIndex + ")");
                preferenceModify.setSummary(channelEnable);
            }
            if(c != null){
                c.close();
            }
        } else if (resultCode == ADD_CHANNEL_REQUEST) {
            initChannelList();
        }
    }

    boolean isSelectAll = true;

    void setCBMConfig() {
        log("setCbChannel");
//        MtkSmsManager manager = null;
        int subId = 0;
        SmsBroadcastConfigInfo[] cbi;
        if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-12,BUG-1112693*/
            SubscriptionManager mSubscriptionManager = SubscriptionManager.from(CBMSettingActivity.this);
            Log.i(LOG_TAG, "in setCBMConfig -subDescription : " + subDescription);
            SubscriptionInfo mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(subDescription);
            if (mSubInfoRecord != null) {
                subId = mSubInfoRecord.getSubscriptionId();
            }
//            manager = MtkSmsManager.getSmsManagerForSubscriptionId(subId);
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            cbi = cbu.getSmsBroadcastConfigInfo(subDescription);
        }else{
//            manager = MtkSmsManager.getDefault();
        	subId = SubscriptionManager.getDefaultSubscriptionId();
            cbi = cbu.getSmsBroadcastConfigInfo();
        }
      //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879  ,
      //[CB][mobile receive CB when disable CB
     /*      if(cbi != null){
               int num = cbi.length;
               for (int i=0; i<num;i++) {
                   int index = cbi[i].getFromServiceId();
                   if (cbi[i].isSelected()) {
                       manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                   } else {
                       manager.disableCellBroadcastRange(index,index);
                   }
               }
           }*/
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-27,BUG-2845457*/
        boolean zz = TctWrapperManager.activateCellBroadcastSms(subId);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-26,BUG-2854327*/
        Log.i(LOG_TAG, "zzz2 = "+zz);
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
        if ("1".equalsIgnoreCase(channelMode)){
            // channelMode is "MyChannelList"
            if(cbi != null){
                int num = cbi.length;
                for (int i=0; i<num;i++) {
                    int index = cbi[i].getFromServiceId();
                    TLog.i(LOG_TAG,"setCBMConfig-MyChannelList-index="+index);
                    if (cbi[i].isSelected()) {
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                    	//[modify]-by-chaobing.huang-01102017-defect3992285
                        //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        TctWrapperManager.enableCellBroadcast(index, subId);
                        Log.d(LOG_TAG,"set config enable channel ="+index);
                    } else {
                    	//[modify]-by-chaobing.huang-01102017-defect3992285
                        //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	TctWrapperManager.disableCellBroadcast(index, subId);
                        Log.d(LOG_TAG,"set config enable channel ="+index);
                        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                    }
                }
            }
        }else{
            //channelMode is "0-999", we just need to enabled channel which index >1000
            if(cbi != null){
                int num = cbi.length;
                for (int i=0; i<num;i++) {
                    int index = cbi[i].getFromServiceId();
                    TLog.i(LOG_TAG,"setCBMConfig-0-999-index="+index);
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                    if (index >= 1000 && !mChannelExFlag || mChannelExFlag && (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= index
                            && index <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER || SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= index
                            && index <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE)) {
                        if (cbi[i].isSelected()) {
                        	//[modify]-by-chaobing.huang-01102017-defect3992285
                            //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        	TctWrapperManager.enableCellBroadcast(index, subId);
                            Log.d(LOG_TAG,"set config enable channel > 1000 ="+index);
                        } else {
                        	//[modify]-by-chaobing.huang-01102017-defect3992285
                            //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                        	TctWrapperManager.disableCellBroadcast(index, subId);
                            Log.d(LOG_TAG,"set config disable channel > 1000 ="+index);
                            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                        }
                    }
                }
            }
        }
        //[BUGFIX]-Add-END by TCTNB.Dandan.Fang

    }

    HashMap getIndexName() {
        Cursor c = null;
        if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
           c = cbu.queryChannel(subDescription);
        }else{
            c = cbu.queryChannel();
        }
        String channelName = "";
        String channelIndex = "";
        HashMap IndexName = new HashMap<String, String>();
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                channelName = "";
                channelIndex = "";
                channelName = c.getString(c.getColumnIndex(Channel.NAME));
                channelIndex = c.getString(c.getColumnIndex(Channel.INDEX));
                if (!IndexName.containsKey(channelIndex)) {
                    IndexName.put(channelIndex, channelName);
                }
                c.moveToNext();
            }
        }
        if(c != null){
            c.close();
        }
        return IndexName;
    }

    void log(String mes) {
        if (true == DEBUG)
            TLog.d(LOG_TAG, mes + "\n");
    }

    private String index = null;

    private IListener.Stub listener = new IListener.Stub(){

        @Override
        public void onFinished(int num , String a, String b) throws RemoteException {
                if (true == DEBUG) TLog.i(LOG_TAG,"listener");
                Set<String> set1 = new HashSet<String>();//[add]-by-chaobing.huang-01.17.2017-defect4005963
                Set<String> set2 = new HashSet<String>();//[add]-by-chaobing.huang-01.17.2017-defect4005963
                HashMap indexName = getIndexName();
                String[] index =a.split("\\;");
                String[] cbable = b.split("\\;");
                ContentValues values = new ContentValues();
                for (int i = 0;i < num;i++){
                //[BUGFIX]-Mod-BEGIN by TCTNB.Tongyuan.Lv, 03/08/2013, PR-408496,
                //fix the normal cb channel missed after set up CMAS
                int indexId = -1;
                try {
                    indexId = Integer.parseInt(index[i]);
                } catch (NumberFormatException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // insert into
                if (!((indexId >= SmsCbConstants.MESSAGE_ID_CMAS_FIRST_IDENTIFIER)
                        && (indexId <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER))) {
                    boolean selected = "enable".equals(cbable[i]) ? true : false;
                    isSelectAll = isSelectAll & selected;
                    values.put(CellBroadcast.Channel.NAME,
                            indexName.containsKey(index[i].trim()) ? indexName.get(index[i])
                                    + "" : "");
                    values.put(CellBroadcast.Channel.INDEX, index[i]);
                    values.put(CellBroadcast.Channel.Enable, selected ? ENABLE : DISABLE);

                    //[add]-begin-by-chaobing.huang-01.17.2017-defect4005963
                    if (getResources().getBoolean(R.bool.def_is_forbidden_modify_predefined_channels)) {
	                    if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
	                        if(subDescription == 0) {
	                            set1.add(index[i]);
	                    	}else {
	                    		set2.add(index[i]);
	                    	}
	                    }else{
	                    	set1.add(index[i]);
	                    }
                    }
                    //[add]-end-by-chaobing.huang-01.17.2017-defect4005963

                    if (!indexName.containsKey(index[i].trim())) {
                        TLog.e("LV", "add new channel from modem: " + index[i]);
                        if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                            cbu.addChannel(values,subDescription);
                        }else{
                            cbu.addChannel(values);
                        }
                    } else {
                        TLog.e("LV", "update old channel from modem: " + index[i]);
                        if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                            if (true == DEBUG) TLog.i(LOG_TAG,"Channel_CONTENT_URI="+Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subDescription));
                            getContentResolver().update(Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subDescription), values,
                                    CellBroadcast.Channel.INDEX + "='" + index[i] + "'", null);
                        }else
                        getContentResolver().update(Channel.CONTENT_URI, values,
                                CellBroadcast.Channel.INDEX + "='" + index[i] + "'", null);
                    }
                //[BUGFIX]-Mod-END by TCTNB.Tongyuan.Lv
                    values.clear();
                }
            }
            //[add]-begin-by-chaobing.huang-01.17.2017-defect4005963
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(CBMSettingActivity.this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putStringSet("predefined1", set1);
            editor.putStringSet("predefined2", set2);
            editor.commit();
            TLog.e("LV", "dayinset1: " + set1);
            //[add]-end-by-chaobing.huang-01.17.2017-defect4005963
            //[BUGFIX]-Del by TCTNB.bo.xu,06/15/2013,CR-451418,
            //Set dedicated Cell broadcast MI for Israel Programs
            initChannelList();
        }
    };

    //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
    //Set dedicated Cell broadcast MI for Israel Programs
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
        if (preference == mChannelMode) {
            updataChannelMode(newValue);
            SharedPreferences.Editor editor = settings.edit();
            if(TelephonyManager.getDefault().isMultiSimEnabled()){
                if(subDescription == 0){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                    Log.i(LOG_TAG, "subDescription == 0");
                    editor.putString("pref_key_choose_channel_sim1", (String)newValue);
                }else if(subDescription == 1){
                    Log.i(LOG_TAG, "subDescription == 1");
                    editor.putString("pref_key_choose_channel_sim2", (String)newValue);
                }
            }else{
                Log.i(LOG_TAG, "not MultiSimEnabled");
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                editor.putString("pref_key_choose_channel", (String)newValue);
            }
            editor.commit();
//            MtkSmsManager manager = null;
            int subId = -1002;
            if(TelephonyManager.getDefault().isMultiSimEnabled()){
                /* MODIFIED-BEGIN by yuwan, 2017-06-07,BUG-4766895*/
                SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
                SubscriptionInfo mSubInfoRecord = mSubscriptionManager
                        .getActiveSubscriptionInfoForSimSlotIndex(subDescription);
                if (mSubInfoRecord != null) {
                    subId = mSubInfoRecord.getSubscriptionId();
                    Log.d(LOG_TAG, "We get CellBroadcastConfig subId = " + subId);
                } else {
                    Log.d(LOG_TAG, "The subDescription:" + subDescription + " is not active");
                }
//                manager = MtkSmsManager.getSmsManagerForSubscriptionId(subId);
                /* MODIFIED-END by yuwan,BUG-4766895*/
                if(subDescription == 0){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                    channelMode = settings.getString("pref_key_choose_channel_sim1", mDefaultMode);
                }else if(subDescription == 1){
                    channelMode = settings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                }
            }else{
                channelMode = settings.getString("pref_key_choose_channel", mDefaultMode);
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
//                manager = MtkSmsManager.getDefault();
                subId = SubscriptionManager.getDefaultSubscriptionId();
            }
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-27,BUG-2845457*/
            boolean zz = TctWrapperManager.activateCellBroadcastSms(subId);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-26,BUG-2854327*/
            Log.i(LOG_TAG, "zzz = "+zz);
            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879 ,
            // [CB][mobile receive CB when disable CB
            hideMyChannelList();
            // [BUGFIX]-Add-END by TCTNB.Dandan.Fang
            if ("0".equalsIgnoreCase(((String)newValue))) {
                // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,07/06/2013,PR477879 ,
                // [CB][mobile receive CB when disable CB
                // if channel mode changes to 0-999, we should disabled the channel added by user firstly,
                // then enable channel 0-999
                SmsBroadcastConfigInfo[] cbi;
                if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                    cbi = cbu.getSmsBroadcastConfigInfo(subDescription);
                }else{
                    cbi = cbu.getSmsBroadcastConfigInfo();
                }
                if(cbi != null){
                    int num = cbi.length;
                    for (int i=0; i<num;i++) {
                        int index = cbi[i].getFromServiceId();
                        TLog.i(LOG_TAG,"index="+index);
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                        if (index < 1000
                                && !mChannelExFlag
                                || mChannelExFlag
                                && (SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER < index
                                        || SmsCbConstants.MESSAGE_ID_ETWS_TYPE > index
                                        || index > SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE
                                        && index < SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL)) {
                                        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1L)){
                            	//[modify]-by-chaobing.huang-01102017-defect3992285
                                //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.disableCellBroadcast(index, subId);
                            }else{
                            	//[modify]-by-chaobing.huang-01102017-defect3992285
                                //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                            	TctWrapperManager.disableCellBroadcast(index, subId);
                            }
                        }
                    }
                }
                // [BUGFIX]-Add-END by TCTNB.Dandan.Fang
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                if (mChannelExFlag) {
                    //0 ~ 4351
                	//[modify]-begin-by-chaobing.huang-01102017-defect3992285
                    //manager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                    //4357 ~ 4369
                    //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                    //4400 ~ 65534
                    //manager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                    Log.d(LOG_TAG,"enable channel 0~65534");
                }else{
                    //manager.enableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.enableCellBroadcastRange(0, 999, subId);
                    Log.d(LOG_TAG,"enable channel 0~999");
                }
            } else {
                if (mChannelExFlag) {
                    //0 ~ 4351
                    //manager.disableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.disableCellBroadcastRange(0, SmsCbConstants.MESSAGE_ID_ETWS_TYPE - 1, subId);
                    //4357 ~ 4369
                    //manager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE + 1, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL - 1, subId);
                    //4400 ~ 65534
                    //manager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.disableCellBroadcastRange(SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER + 1, 65534, subId);
                    Log.d(LOG_TAG,"disable channel 0~65534");
                }else{
                    //manager.disableCellBroadcastRange(0, 999, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                	TctWrapperManager.disableCellBroadcastRange(0, 999, subId);
                    Log.d(LOG_TAG,"disable channel 0~999");
                    //[modify]-end-by-chaobing.huang-01102017-defect3992285
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                }
                // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879 ,
                // [CB][mobile receive CB when disable CB
                // setCBMConfig();
                // [BUGFIX]-Add-END by TCTNB.Dandan.Fang
            }
            // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879 ,
            // [CB][mobile receive CB when disable CB
            setCBMConfig();
            // [BUGFIX]-Add-END by TCTNB.Dandan.Fang
            result = true;
        }
        return result;
    }

    private void updataChannelMode(Object value) {
        // [BUGFIX]-Add-BEGIN by yuwan,04/28/2017,4582710
        CharSequence[] summaries = null;
        if (mChannelExFlag) {
            summaries = getResources().getTextArray(R.array.pref_cb_channel_mode_entries_ex);
        } else {
            summaries = getResources().getTextArray(R.array.pref_cb_channel_mode_entries);
        }
        // [BUGFIX]-Add-END by yuwan,04/28/2017,4582710
        CharSequence[] values = mChannelMode.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                mChannelMode.setSummary(summaries[i]);
                break;
            }
        }
    }
    //[BUGFIX]-Add-END by TCTNB.bo.xu

    // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,06/27/2013,PR477879 ,
    // [CB][mobile receive CB when disable CB
    private void hideMyChannelList() {
        initChannelList();
        if ("0".equalsIgnoreCase(channelMode)) {
            // channel mode is (0-999), hide my channel list, diabled "add channel " preference
            channel_add.setEnabled(false);
        } else {
            // channel mode is (my channel list), display my channel list
            channel_add.setEnabled(true);
        }
    }
    // [BUGFIX]-Add-END by TCTNB.Dandan.Fang
}
