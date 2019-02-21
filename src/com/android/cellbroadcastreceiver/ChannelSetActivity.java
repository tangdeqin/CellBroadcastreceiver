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
/* 06/15/2013|        bo.xu         |      CR-451418       |Set dedicated Ce- */
/*           |                      |                      |ll broadcast MI   */
/*           |                      |                      |for Israel Progr- */
/*           |                      |                      |ams               */
/* ----------|----------------------|----------------------|----------------- */
/* 07/10/2013|        bo.xu         |      PR-477859       |[CB]CB list incr- */
/*           |                      |                      |ease new channel  */
/* ----------|----------------------|----------------------|----------------- */
/* 09/12/2014|      fujun.yang      |        772564        |new CLID variable */
/*           |                      |                      |to re-activate CB */
/*           |                      |                      |for NL            */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import mediatek.telephony.MtkSmsManager;
//import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo; // MODIFIED by yuxuan.zhang, 2016-07-19,BUG-2548203
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import android.widget.TextView;
import android.text.InputFilter;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsCbConstants; // MODIFIED by yuxuan.zhang, 2016-09-20,BUG-2845457
import com.android.cellbroadcastreceiver.CellBroadcast.Channel;

import android.content.Intent;

import android.telephony.SubscriptionManager;
import com.android.internal.telephony.PhoneConstants;
import android.telephony.TelephonyManager;
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-07-19,BUG-2548203

import com.android.cb.util.TLog;
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-22,BUG-2579682*/
import com.tct.util.FwkPlf;
import com.tct.util.IsdmParser;
/* MODIFIED-END by yuxuan.zhang,BUG-2579682*/
import com.tct.wrapper.TctWrapperManager;

import java.util.Locale;
import android.text.InputFilter;
public class ChannelSetActivity extends Activity {
    Button ok;

    Button cancel;

    EditText channelindex;
    TextView channelindexTitle;

    EditText channelname;

    CheckBox enable;

    String channelName = "";

    String channelId = "";

    String channelEnable = "";

    String channelOldIndex = "";

    String channelNewIndex = "";

    String ENABLE = "Enable";

    String DISABLE = "Disable";

    CBSUtills cbu;

    static final int EDIT_CHANNEL_REQUEST = 0;

    static final int ADD_CHANNEL_REQUEST = 1;

    final int MAXLENGTH = 14;

    final int RUMAXLENGTH = 46;

    boolean isAdd = true;

    private static final String LOG_TAG = "ChannelSetActivity";
    //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
    //Set dedicated Cell broadcast MI for Israel Programs
    private String channelMode = "1";
    SharedPreferences mSettings;
    //[BUGFIX]-Add-END by TCTNB.bo.xu

    private int subDescription = PhoneConstants.SUB1;
    private boolean addChannel = false;
    private boolean mEnableSingleSIM = false;
    private String mDefaultMode = "1"; // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
    private boolean mChannelExFlag = false;
    private String mlanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.addchannel);
        mChannelExFlag = getResources().getBoolean(R.bool.def_expand_normal_cb_channel);
        findView();
        //[BUGFIX]-Add-BEGIN by yuwan,04/18/2017  4619398
        Locale locale = getResources().getConfiguration().locale;
        mlanguage = locale.getLanguage();
        Log.d(LOG_TAG,"language = "+ mlanguage);
        if (mlanguage == "ru"){
            channelname.setFilters(new InputFilter[]{new InputFilter.LengthFilter(23)});
        }
        //[BUGFIX]-Add-END by yuwan,04/18/2017   4619398
        setListener();
        mDefaultMode = getResources().getString(R.string.def_is_all_channel_mode_cellbroadcast); // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
        cbu = new CBSUtills(this);
        //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
        //Set dedicated Cell broadcast MI for Israel Programs
        mSettings = this.getSharedPreferences("com.android.cellbroadcastreceiver_preferences", 0);
      //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
        mEnableSingleSIM = IsdmParser.getBooleanFwk(this, FwkPlf.def_cellbroadcast_enable_single_sim, false); // MODIFIED by yuxuan.zhang, 2016-07-22,BUG-2579682
        log("mEnableSingleSIM="+mEnableSingleSIM);
        if(TelephonyManager.getDefault().isMultiSimEnabled()){
            if(subDescription == 0){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                    channelMode = mSettings.getString("pref_key_choose_channel_sim1", mDefaultMode);
                }else if(subDescription == 1){
                    channelMode = mSettings.getString("pref_key_choose_channel_sim2", mDefaultMode);
            }
        }else{
            channelMode = mSettings.getString("pref_key_choose_channel", mDefaultMode);
            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
        }
      //[FEATURE]-Add-END by TSCD.fujun.yang
        //[BUGFIX]-Add-END by TCTNB.bo.xu
        Intent intent = this.getIntent();
        if(TelephonyManager.getDefault().isMultiSimEnabled() && (intent != null)){
            subDescription =intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, PhoneConstants.SUB1);
            if(mEnableSingleSIM){
                subDescription = PhoneConstants.SUB1;
            }
            log("multisimcard-subDescription="+subDescription);
            addChannel = intent.getBooleanExtra("channeladd",false);
        }
        if (!addChannel && this.getIntent().getExtras() != null) {
            isAdd = false;
            channelId = this.getIntent().getExtras().getString("id");
            Cursor c = null;
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1)){
                c = cbu.queryChannel(channelId,subDescription);
            }else{
                c = cbu.queryChannel(channelId);
            }
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                channelName = c.getString(c.getColumnIndex(Channel.NAME));
                channelOldIndex = c.getString(c.getColumnIndex(Channel.INDEX));
                channelEnable = c.getString(c.getColumnIndex(Channel.Enable));
                // init control text
                channelname.setText(channelName);
                channelindex.setText(channelOldIndex);
                enable.setChecked(channelEnable.equalsIgnoreCase(ENABLE) ? true : false);
            }
            if(c != null){
                c.close();
            }
        } else {
            addChannel = false;
            isAdd = true;
        }
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                R.drawable.ic_dialog_alert_holo_light);
        // add by liang.zhang for Defect 4932674,4933286 at 2017-06-20 begin
        channelname.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        // add by liang.zhang for Defect 4932674,4933286 at 2017-06-20 end
    }

    void findView() {
        ok = (Button) findViewById(R.id.ok);
        cancel = (Button) findViewById(R.id.cancel);
        channelname = (EditText) findViewById(R.id.channelname);
        channelindex = (EditText) findViewById(R.id.channelindex);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
        channelindexTitle = (TextView) findViewById(R.id.channelindextitle);
        Log.i(LOG_TAG, "mChannelExFlag = "+mChannelExFlag);
        if (mChannelExFlag) {
            channelindex.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(5),
            });
            channelindexTitle.setText(getResources().getString(R.string.channelEx_index));
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
        enable = (CheckBox) findViewById(R.id.channelenable);
    }

    void setListener() {
        ok.setOnClickListener(mOkListener);
        cancel.setOnClickListener(mCancelListenertest);
    }

    // true: is numeric
    boolean isNumeric(String obj) {
        try {
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
            int channel = Integer.parseInt(obj);
            if (mChannelExFlag && channel > 65534) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    boolean isEmergency(String obj) {
        int channel = Integer.parseInt(obj);
        // do not allow to add cmas channel
        if (SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL <= channel
                && channel <= SmsCbConstants.MESSAGE_ID_CMAS_LAST_IDENTIFIER) {
            return false;
        }
        // Etws:4352~4356
        if (SmsCbConstants.MESSAGE_ID_ETWS_TYPE <= channel
                && channel <= SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE) {
            return false;
        }
        return true;
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/

    private OnClickListener mOkListener = new OnClickListener() {
        public void onClick(View v) {
            // check index is integer
            String channelName = "";
            String channelEnable = "";
            channelNewIndex = channelindex.getText().toString().trim();

            if (channelNewIndex.trim().equalsIgnoreCase("")) {
                // alert user
                Toast.makeText(ChannelSetActivity.this, R.string.ChannelIsNull, Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if (!isNumeric(channelNewIndex)) {
                // alert user
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                Toast.makeText(ChannelSetActivity.this, mChannelExFlag ? R.string.ChannelExNotNumeric : R.string.ChannelNotNumeric,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (mChannelExFlag && !isEmergency(channelNewIndex)) {
                Toast.makeText(ChannelSetActivity.this, R.string.EmergencyChannelNotAdded,
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int chIndex = Integer.parseInt(channelNewIndex);
            String sIndex = chIndex + "";

            // add this channel to db
            channelName = channelname.getText().toString().trim();

            if (isOutLength(channelName)) {
                // alert user
                Toast.makeText(ChannelSetActivity.this, R.string.ChannelNameOut, Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            channelEnable = enable.isChecked() ? ENABLE : DISABLE;
            ContentValues values = new ContentValues();
            values.put(CellBroadcast.Channel.NAME, channelName);
            values.put(CellBroadcast.Channel.INDEX, sIndex);
            values.put(CellBroadcast.Channel.Enable, channelEnable);

            if (isAdd) {
                // set cb Channel start----------------------------
                // judge channel index is duplicate
                if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1)){
                    if (cbu.queryChannelIndexA(sIndex,subDescription)) {
                        // alert user
                        Toast.makeText(ChannelSetActivity.this, R.string.ChannelExist,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // add channel to db-----------------------
                    cbu.addChannel(values,subDescription);
                }else{
                    if (cbu.queryChannelIndexA(sIndex)) {
                        // alert user
                        Toast.makeText(ChannelSetActivity.this, R.string.ChannelExist,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // add channel to db-----------------------
                    cbu.addChannel(values);
                }
                setResult(ADD_CHANNEL_REQUEST);

            } else {
                // judge channel index is duplicate
                if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1)){
                    if (cbu.queryChannelIndexE(sIndex, channelId,subDescription)) {
                        // alert user
                        Toast.makeText(ChannelSetActivity.this, R.string.ChannelExist,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // modify by liang.zhang for Defect 5709075 at 2017-12-09 begin
                    SubscriptionManager mSubscriptionManager = SubscriptionManager.from(ChannelSetActivity.this);
                    SubscriptionInfo mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(subDescription);
                    int subId = mSubscriptionManager.getDefaultSubscriptionId();
                    if (mSubInfoRecord != null) {
                        subId = mSubInfoRecord.getSubscriptionId();
                        Log.i(LOG_TAG, "We get CellBroadcastConfig subId = " + subId);
                    }else{
                        Log.w(LOG_TAG, "The subDescription:"+subDescription + " is not active");
                    }
                    
                    //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,07/10/2013,PR-477859,
                    //[CB]CB list increase new channel
//                    MtkSmsManager manager = MtkSmsManager.getSmsManagerForSubscriptionId(subId);
                    // modify by liang.zhang for Defect 5709075 at 2017-12-09 end 
                    int index = Integer.parseInt(channelOldIndex);
                    //[modify]-by-chaobing.huang-01102017-defect3992285
                    //manager.disableCellBroadcastRange(index, index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                    //[BUGFIX]-Add-END by TCTNB.bo.xu
                    // update channel in to db
                    cbu.updateChannel(channelId, values,subDescription);
                }else{
                    if (cbu.queryChannelIndexE(sIndex, channelId)) {
                        // alert user
                        Toast.makeText(ChannelSetActivity.this, R.string.ChannelExist,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,07/10/2013,PR-477859,
                    //[CB]CB list increase new channel
//                    MtkSmsManager manager = MtkSmsManager.getDefault();
                    SubscriptionManager mSubscriptionManager = SubscriptionManager.from(ChannelSetActivity.this);
                    int subId = mSubscriptionManager.getDefaultSubscriptionId();
                    int index = Integer.parseInt(channelOldIndex);
                    //[modify]-by-chaobing.huang-01102017-defect3992285
                    //manager.disableCellBroadcastRange(index, index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                    //[BUGFIX]-Add-END by TCTNB.bo.xu
                    // update channel in to db
                    cbu.updateChannel(channelId, values);
                }

                setResult(EDIT_CHANNEL_REQUEST);
            }
            //[BUGFIX]-Add-BEGIN by TCTNB.bo.xu,06/15/2013,CR-451418,
            //Set dedicated Cell broadcast MI for Israel Programs
          //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,09/12/2014,772564,new CLID variable to re-activate CB for NL
            if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1)){
                if(subDescription == 0){
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
                    channelMode = mSettings.getString("pref_key_choose_channel_sim1", mDefaultMode);
                }else if(subDescription == 1){
                    channelMode = mSettings.getString("pref_key_choose_channel_sim2", mDefaultMode);
                }
            }else{
                channelMode = mSettings.getString("pref_key_choose_channel", mDefaultMode);
                /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            }
          //[FEATURE]-Add-END by TSCD.fujun.yang
            if ("1".equalsIgnoreCase(channelMode)) {
                setCBMConfig();
            }
            //[BUGFIX]-Add-END by TCTNB.bo.xu
            finish();

        }
    };

    void setCBMConfig() {
        log("setCbChannel");
        if(TelephonyManager.getDefault().isMultiSimEnabled() && (subDescription != -1)){
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-19,BUG-2548203*/
            SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
            SubscriptionInfo mSubInfoRecord = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(subDescription);
            int subId = -1002;
            if (mSubInfoRecord != null) {
                subId = mSubInfoRecord.getSubscriptionId();
                Log.i(LOG_TAG, "We get CellBroadcastConfig subId = " + subId);
            }else{
                Log.w(LOG_TAG, "The subDescription:"+subDescription + " is not active");
            }
//            MtkSmsManager manager = MtkSmsManager.getSmsManagerForSubscriptionId(subId);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-27,BUG-2845457*/
            boolean zz = TctWrapperManager.activateCellBroadcastSms(subId);
            Log.i(LOG_TAG, "zz = " + zz);
            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            /* MODIFIED-END by yuxuan.zhang,BUG-2548203*/
            SmsBroadcastConfigInfo[] cbi = cbu.getSmsBroadcastConfigInfo(subDescription);
               if(cbi != null){
                   int num = cbi.length;
                   for (int i=0; i<num;i++) {
                       int index = cbi[i].getFromServiceId();
                       if (cbi[i].isSelected()) {
                    	   //[modify]-begin-by-chaobing.huang-01102017-defect3992285
                           //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	   TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                           Log.d(LOG_TAG,"enable1 channel = "+index);
                       } else {
                           //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	   TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                           Log.d(LOG_TAG,"disable1 channel = "+index);
                    	   //[modify]-end-by-chaobing.huang-01102017-defect3992285
                       }
                   }
               }
        }else{
        	SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
        	int subId = mSubscriptionManager.getDefaultSubscriptionId();
//        	MtkSmsManager manager = MtkSmsManager.getDefault();
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-27,BUG-2845457*/
            boolean yy = TctWrapperManager.activateCellBroadcastSms(subId);
            Log.i(LOG_TAG, "yy = " + yy);
            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            SmsBroadcastConfigInfo[] cbi = cbu.getSmsBroadcastConfigInfo();
               if(cbi != null){
                   int num = cbi.length;
                   for (int i=0; i<num;i++) {
                       int index = cbi[i].getFromServiceId();
                       if (cbi[i].isSelected()) {
                    	   //[modify]-begin-by-chaobing.huang-01102017-defect3992285
                           //manager.enableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	   TctWrapperManager.enableCellBroadcastRange(index, index, subId);
                           Log.d(LOG_TAG,"enable2 channel = "+index);
                       } else {
                           //manager.disableCellBroadcastRange(index,index,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
                    	   TctWrapperManager.disableCellBroadcastRange(index, index, subId);
                           //[modify]-end-by-chaobing.huang-01102017-defect3992285
                           Log.d(LOG_TAG,"disable2 channel = "+index);
                       }
                   }
               }
        }
    }

    private OnClickListener mCancelListenertest = new OnClickListener() {
        public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    // true: out of max length limit
    // false : not outof
    boolean isOutLength(String aString) {
        // aString = "This is a test string.";
        String anotherString = null;
        try {
            anotherString = new String(aString.getBytes("GBK"), "ISO8859_1");
            log("aString.length():  " + aString.length() + ", anotherString.length(): "
                    + anotherString.length());
            log("isOutLength");
            //[BUGFIX]-Add-BEGIN by yuwan,04/18/2017  4619398
            if (mlanguage == "ru"){
                if (anotherString.length() > RUMAXLENGTH) {
                    log("1");
                    return true;
                }
            }else {
                if (anotherString.length() > MAXLENGTH) {
                    log("2");
                    return true;
                }
            }
            //[BUGFIX]-Add-END by yuwan,04/18/2017  4619398
        } catch (UnsupportedEncodingException ex) {
        }

        return false;
    }

    private boolean HasLog = true;// add by xielianghui

    void log(String mes) {
        if (HasLog)
            TLog.d("ChannelSetActivity", mes + "\n");
    }

}
