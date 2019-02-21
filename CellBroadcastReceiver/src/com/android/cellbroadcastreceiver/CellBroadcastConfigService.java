/*
 * Copyright (c) 2013, The Linux Foundation. All rights reserved.
 * Not a Contribution.
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
/* ============================================================================
 *     Modifications on Features list / Changes Request / Problems Report
 * ----------------------------------------------------------------------------
 *    date   |        author        |         Key          |     comment
 * ----------|----------------------|----------------------|-------------------
 * 09/04/2014|     tianming.lei     |        715519        |DT variant for tmo
 * ----------|----------------------|----------------------|-------------------
 * 01/21/2015|      fujun.yang      |        895849        |[SCB][CMAS]Can't
 *           |                      |                      |receive
 *           |                      |                      |RMT&Exercise
 *           |                      |                      |alerts
 * ----------|----------------------|----------------------|-----------------
 * 02/10/2015|      fujun.yang      |        886284        |an receive CMAS
 *           |                      |                      |and ETWS even not
 *           |                      |                      |enable "show
 *           |                      |                      |ETWS/CMAS test
 *           |                      |                      |broadcasts"
 * ----------|----------------------|----------------------|-------------------
 * 03/24/2015|      fujun.yang      |        914630        |SMSCB behavior in
 *           |                      |                      |Android phones
 * ----------|----------------------|----------------------|-------------------
 * 07/17/2015|      fang.song       |        1041463       |FC happened when double click the WEA(CMAS) message on the notification bar
 * ----------|----------------------|----------------------|------------------
 * 07/17/2015|      fang.song       |        1043168 	   |FC happened when reject a MT call after read a WEA message
 * ----------|----------------------|----------------------|------------------
 * ============================================================================*/

package com.android.cellbroadcastreceiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.CellBroadcastMessage;
//import mediatek.telephony.MtkSmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.android.internal.telephony.gsm.SmsCbConstants;

import static com.android.cellbroadcastreceiver.CellBroadcastReceiver.DBG;

import android.telephony.SubscriptionManager;
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-04-21,BUG-1112693

import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.PhoneConstants;
import com.android.cb.util.TLog;
import com.tct.telecom.TctQctCellBroadcast;
import com.tct.wrapper.TctWrapperManager;

import java.util.ArrayList;
//add by liang.zhang for Defect 5960221 at 2018-01-30 begin
import android.telephony.SubscriptionInfo;
//add by liang.zhang for Defect 5960221 at 2018-01-30 end

/**
 * This service manages enabling and disabling ranges of message identifiers
 * that the radio should listen for. It operates independently of the other
 * services and runs at boot time and after exiting airplane mode.
 * <p>
 * Note that the entire range of emergency channels is enabled. Test messages
 * and lower priority broadcasts are filtered out in CellBroadcastAlertService
 * if the user has not enabled them in settings.
 * <p>
 * TODO: add notification to re-enable channels after a radio reset.
 */
public class CellBroadcastConfigService extends IntentService {
    private static final String TAG = "CellBroadcastConfigService";

    static final String ACTION_ENABLE_CHANNELS = "ACTION_ENABLE_CHANNELS";

    static final String EMERGENCY_BROADCAST_RANGE_GSM =
            "ro.cb.gsm.emergencyids";

    private static final String KEY_PLATEFORM = "key_plateform";
    private static final String RIL_QCOM = "qualcomm";
    private static final String RIL_MTK = "mtk";
    // private static final String TAG = "channel";//rm by yong,wang for fr 7028757 at 2018-10-17
    private static final String RIL_IMPL_KEY_ = "gsm.version.ril-impl";

    //[BUGFIX]-MOD-BEGIN by TSCD,tianming.lei 01/08/2015,PR-879862
    //private long mSubscription = SubscriptionManager.DEFAULT_SUB_ID;
    private int mSubscription = PhoneConstants.SUB1;
    //[BUGFIX]-MOD-END by TSCD,tianming.lei
    private int mPhoneId = 0;

    public CellBroadcastConfigService() {
        super(TAG);          // use class name for worker thread name
    }

    private void enableCellBroadcast(int messageIdentifier, boolean isMSim) {
        //[modify]-begin-by-chaobing.huang-01102017-defect3992285
        if (isMSim) {
            //SmsManager.getSmsManagerForSubscriptionId(mSubscription).enableCellBroadcast(messageIdentifier, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
            TctWrapperManager.enableCellBroadcast(messageIdentifier, mSubscription);
        } else {
            //SmsManager.getDefault().enableCellBroadcast(messageIdentifier, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
            TctWrapperManager.enableCellBroadcast(messageIdentifier, SubscriptionManager.getDefaultSubscriptionId());
        }
        //[modify]-end-by-chaobing.huang-01102017-defect3992285
    }

    private void disableCellBroadcast(int messageIdentifier, boolean isMSim) {
        //[modify]-begin-by-chaobing.huang-01102017-defect3992285
        if (isMSim) {
            //SmsManager.getSmsManagerForSubscriptionId(mSubscription).disableCellBroadcast(messageIdentifier, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
            TctWrapperManager.disableCellBroadcast(messageIdentifier, mSubscription);
        } else {
            //SmsManager.getDefault().disableCellBroadcast(messageIdentifier, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
            TctWrapperManager.disableCellBroadcast(messageIdentifier, SubscriptionManager.getDefaultSubscriptionId());
        }
        //[modify]-end-by-chaobing.huang-01102017-defect3992285
    }

    private void enableCellBroadcastRange(int startMessageId, int endMessageId, boolean isMSim) {
        //[modify]-begin-by-chaobing.huang-01102017-defect3992285
        if (isMSim) {
            //SmsManager.getSmsManagerForSubscriptionId(mSubscription).enableCellBroadcastRange(startMessageId, endMessageId, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
            TctWrapperManager.enableCellBroadcastRange(startMessageId, endMessageId, mSubscription);
        } else {
            //SmsManager.getDefault().enableCellBroadcastRange(startMessageId, endMessageId, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
            TctWrapperManager.enableCellBroadcastRange(startMessageId, endMessageId, SubscriptionManager.getDefaultSubscriptionId());
        }
        //[modify]-end-by-chaobing.huang-01102017-defect3992285
    }

    private void disableCellBroadcastRange(int startMessageId, int endMessageId, boolean isMSim) {
        //[modify]-begin-by-chaobing.huang-01102017-defect3992285
        if (isMSim) {
            //SmsManager.getSmsManagerForSubscriptionId(mSubscription).disableCellBroadcastRange(startMessageId, endMessageId, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
            TctWrapperManager.disableCellBroadcastRange(startMessageId, endMessageId, mSubscription);
        } else {
            //SmsManager.getDefault().disableCellBroadcastRange(startMessageId, endMessageId,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
            TctWrapperManager.disableCellBroadcastRange(startMessageId, endMessageId, SubscriptionManager.getDefaultSubscriptionId());
        }
        //[modify]-end-by-chaobing.huang-01102017-defect3992285
    }

    private void setChannelRange(String ranges, boolean enable) {
        if (DBG) log("setChannelRange: " + ranges);
        boolean isMSim = TelephonyManager.getDefault().isMultiSimEnabled();
        try {
            for (String channelRange : ranges.split(",")) {
                int dashIndex = channelRange.indexOf('-');
                if (dashIndex != -1) {
                    int startId = Integer.decode(channelRange.substring(0, dashIndex).trim());
                    int endId = Integer.decode(channelRange.substring(dashIndex + 1).trim());
                    if (enable) {
                        if (DBG) log("enabling emergency IDs " + startId + '-' + endId);
                        enableCellBroadcastRange(startId, endId, isMSim);
                    } else {
                        if (DBG) log("disabling emergency IDs " + startId + '-' + endId);
                        disableCellBroadcastRange(startId, endId, isMSim);
                    }
                } else {
                    int messageId = Integer.decode(channelRange.trim());
                    if (enable) {
                        if (DBG) log("enabling emergency message ID " + messageId);
                        enableCellBroadcast(messageId, isMSim);
                    } else {
                        if (DBG) log("disabling emergency message ID " + messageId);
                        disableCellBroadcast(messageId, isMSim);
                    }
                }
            }
        } catch (NumberFormatException e) {
            TLog.e(TAG, "Number Format Exception parsing emergency channel range", e);
        }

        // Make sure CMAS Presidential is enabled (See 3GPP TS 22.268 Section 6.2).
        if (DBG) log("setChannelRange: enabling CMAS Presidential");
//        if (CellBroadcastReceiver.phoneIsCdma(mSubscription)) {
        enableCellBroadcast(SmsEnvelope.SERVICE_CATEGORY_CMAS_PRESIDENTIAL_LEVEL_ALERT, isMSim);
//        } else {
        enableCellBroadcast(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL, isMSim);
//        }
    }

    /**
     * Returns true if this is a standard or operator-defined emergency alert message.
     * This includes all ETWS and CMAS alerts, except for AMBER alerts.
     *
     * @param message the message to test
     * @return true if the message is an emergency alert; false otherwise
     */
    static boolean isEmergencyAlertMessage(CellBroadcastMessage message) {
        //PR 1041463, 1043168 Added by fang.song 2015.07.17 begin
        if (message == null) {
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            Log.w(TAG, "isEmergencyAlertMessage: message == null");
            return false;
        }
        //PR 1041463, 1043168 Added by fang.song 2015.07.17 end

        // modify by liang.zhang for Defect 5772065 at 2018-01-08 begin
        if (message.isEmergencyAlertMessage()) {
            Log.w(TAG, "isEmergencyAlertMessage");
            //PR 1054793 Added by fang.song begin
//            if (CBSUtills.isShow4371AsNormal(message)) {
//                Log.w(TAG, "isEmergencyAlertMessage:Show4371AsNormal");
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
//                return false;
//            }
            //PR 1054793 Added by fang.song end
            return true;
        }
        // modify by liang.zhang for Defect 5772065 at 2018-01-08 end
        
        // add by liang.zhang for Defect 6363569 at 201-9-10 begin
        if (message.getServiceCategory() >= 4370 && message.getServiceCategory() <= 4399) {
        	return true;
        }
        // add by liang.zhang for Defect 6363569 at 201-9-10 end
        
        
        // add by liang.zhang for Defect 6012945 at 2018-03-07 begin
        if (message.getServiceCategory() == 6400) {
        	return true;
        }
        // add by liang.zhang for Defect 6012945 at 2018-03-07 end

        // Check for system property defining the emergency channel ranges to enable
        String emergencyIdRange = (CellBroadcastReceiver.phoneIsCdma(message.getSubId())) ?
                "" : SystemProperties.get(EMERGENCY_BROADCAST_RANGE_GSM);

        if (TextUtils.isEmpty(emergencyIdRange)) {
            Log.w(TAG, "emergencyIdRange is empty"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
            return false;
        }
        try {
            int messageId = message.getServiceCategory();
            for (String channelRange : emergencyIdRange.split(",")) {
                int dashIndex = channelRange.indexOf('-');
                Log.i(TAG, "isEmergencyAlertMessage channelRange = " + channelRange); // MODIFIED by yuxuan.zhang, 2016-04-21,BUG-1112693
                if (dashIndex != -1) {
                    int startId = Integer.decode(channelRange.substring(0, dashIndex).trim());
                    int endId = Integer.decode(channelRange.substring(dashIndex + 1).trim());
                    if (messageId >= startId && messageId <= endId) {
                        return true;
                    }
                } else {
                    int emergencyMessageId = Integer.decode(channelRange.trim());
                    if (emergencyMessageId == messageId) {
                        return true;
                    }
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Number Format Exception parsing emergency channel range", e); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
        }
        
        return false;
    }

    //add by yong.wang for Defect 7028757 for integration the QCOM and MTK processing logic on 2018-10-17 begin 
    protected void doOnHandleIntent(Intent intent){
        int subId = 0;
        String platform = SystemProperties.get("ro.mediatek.platform");
        boolean isMSim = TelephonyManager.getDefault().isMultiSimEnabled();
        if (ACTION_ENABLE_CHANNELS.equals(intent.getAction())) {
            boolean enableCustomizedCmas = getResources().getBoolean(R.bool.def_allow_customize_emergency_channels); // MODIFIED by yuxuan.zhang, 2016-10-19,BUG-1112693
            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/10/2015,886284,an receive CMAS and ETWS even not enable "show ETWS/CMAS test broadcasts"
            boolean isEnableRMTExceriseAlertType = getResources().getBoolean(R.bool.def_enableRMTExerciseTestAlert);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            if (DBG)
                Log.w(TAG, "isEnableRMTExceriseAlertType=" + isEnableRMTExceriseAlertType);
                //[BUGFIX]-Add-END by TSCD.fujun.yang
                mSubscription = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
                PhoneConstants.SUB1);//[BUGFIX]-MOD by TSCD,tianming.lei 01/08/2015,PR-879862
                mPhoneId = SubscriptionManager.getPhoneId(mSubscription);
                Log.w(TAG, "mPhoneId=" + mPhoneId + " mSubscription=" + mSubscription);
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                try {
                    boolean disableChannel50 = false;//[BUGFIX]-Add by bin.xue for PR1071520
                    boolean disableChannel60 = false;//[BUGFIX]-Add by bin.xue for PR1071520
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    Resources res = getResources();

                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-08,BUG-2219480*/
                    boolean isWpasEnable = res.getBoolean(R.bool.def_enable_wpas_function);
                    // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
                    if (!CBSUtills.isCanadaSimCard(this)) {
                        isWpasEnable = false; 
                    }
                    // add by liang.zhang for Defect 6929849 at 2018-09-01 end
                    boolean enableWpasTest = isWpasEnable ? prefs.getBoolean(
                    CellBroadcastSettings.KEY_ENABLE_WPAS_TEST_ALERTS + mPhoneId, false) : false; // MODIFIED by yuxuan.zhang, 2016-06-28,BUG-2389849
                    Log.i(TAG, "enableWpasTest = " + enableWpasTest + ";isWpasEnable =" + isWpasEnable);
                    /* MODIFIED-END by yuxuan.zhang,BUG-2219480*/
                    // boolean for each user preference checkbox, true for checked, false for unchecked
                    // Note: If enableEmergencyAlerts is false, it disables ALL emergency broadcasts
                    // except for cmas presidential. i.e. to receive cmas severe alerts, both
                    // enableEmergencyAlerts AND enableCmasSevereAlerts must be true.
                    boolean enableEmergencyAlerts = prefs.getBoolean(
                    CellBroadcastSettings.KEY_ENABLE_EMERGENCY_ALERTS + mPhoneId, true);

                    TelephonyManager tm = (TelephonyManager) getSystemService(
                        Context.TELEPHONY_SERVICE);

                        String country = "";
                        if (isMSim) {
                            country = TelephonyManager.getDefault().getSimCountryIso(mSubscription);
                        } else {
                            country = tm.getSimCountryIso();
                        }
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
                        if (isMSim) {
                            boolean zz = TctWrapperManager.activateCellBroadcastSms(mSubscription);
                            Log.i(TAG, "zz= " + zz);
                        } else {
                            boolean yy = TctWrapperManager.activateCellBroadcastSms(SubscriptionManager.getDefaultSubscriptionId());
                            Log.i(TAG, "yy = " + yy);
                        }
                        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                        boolean enableChannel50Support = res.getBoolean(R.bool.show_brazil_settings)
                                || "br".equals(country) || res.getBoolean(R.bool.show_india_settings)
                                || "in".equals(country) || "co".equals(country); // modify by liang.zhang for Defect 6353597 at 2018-05-30

                        boolean enableChannel60Support = res.getBoolean(R.bool.show_india_settings)
                                || "in".equals(country);
                        //[BUGFIX]-Add-BEGIN by bin.xue for PR1071520
                        if (this.getResources().getBoolean(R.bool.def_cellbroadcastChannel50_disable)) {
                            enableChannel50Support = false;
                            disableChannel50 = true;
                            TLog.i(TAG, "disableChannel50");
                        }
                        if (this.getResources().getBoolean(R.bool.def_cellbroadcastChannel60_disable)) {
                            enableChannel60Support = false;
                            disableChannel60 = true;
                            TLog.i(TAG, "disableChannel60");
                        }
                        //[BUGFIX]-Add-END by bin.xue

                        boolean enableChannel50Alerts = enableChannel50Support &&
                                prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_CHANNEL_50_ALERTS
                                        + mPhoneId, true);

                        //[FEATURE]-ADD-BEGIN by TSCD.tianming.lei,09/04/2014,715519
                        boolean ssvEnable = "true".equals(SystemProperties.get("ro.ssv.enabled", "false"));
                        if (ssvEnable) {
                            String operator = SystemProperties.get("ro.ssv.operator.choose", "");
                            if (operator.equals("TMO")) {
                                boolean tmo_50_enabled = getResources().getBoolean(R.bool.def_ssv_enable_local_50_channel);
                                TLog.i("ltm", "perso value:" + tmo_50_enabled);
                                enableChannel50Alerts = enableChannel50Support &&
                                        prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_CHANNEL_50_ALERTS
                                                + mPhoneId, tmo_50_enabled);
                                //[BUGFIX]-Add-BEGIN by TSNJ,kaibang.liu for PR1076464
                            } else if (operator.equals("TEF")) {
                                enableChannel50Alerts = true;
                            }
                            //[BUGFIX]-Add-END by TSNJ,kaibang.liu
                        }
                        //[FEATURE]-ADD-END by TSCD.tianming.lei

                        boolean enableChannel60Alerts = enableChannel60Support &&
                                prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_CHANNEL_60_ALERTS
                                        + mPhoneId, true);

                        // Note:  ETWS is for 3GPP only
                        boolean enableEtwsTestAlerts = prefs.getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_ETWS_TEST_ALERTS + mPhoneId, getResources().getBoolean(R.bool.def_etws_test_alert_default_on)); // MODIFIED by yuxuan.zhang, 2016-07-28,BUG-2632234

                        boolean enableCmasExtremeAlerts = prefs.getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS + mPhoneId,
                                true);

                        boolean enableCmasSevereAlerts = prefs.getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS + mPhoneId,
                                true);

                        boolean enableCmasAmberAlerts = prefs.getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_AMBER_ALERTS + mPhoneId, true);

                        boolean enableCmasTestAlerts = prefs.getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_TEST_ALERTS + mPhoneId, false);

                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,01/21/2015,895849,[SCB][CMAS]Can't receive RMT&Exercise alerts
                        boolean enableCmasRMTAlerts = prefs.getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS + mPhoneId, false);
                        boolean enableCmasExerciseAlerts = prefs.getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS + mPhoneId, false);
                        //[BUGFIX]-Add-END by TSCD.fujun.yang
                        // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
                        boolean enableCmasSpanishAlerts = prefs.getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_SPANISH_LANGUAGE_ALERTS + mPhoneId, false);
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-20,BUG-1112693*/
                        if (isWpasEnable) {
                            enableCmasSpanishAlerts = true;
                        }
                        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                        // [BUGFIX]-MOD-END by bin.xue
                        // set up broadcast ID ranges to be used for each category
                        int cmasExtremeStart =
                                SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED;
                        int cmasExtremeEnd = SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_LIKELY;
                        int cmasSevereStart =
                                SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_EXPECTED_OBSERVED;
                        int cmasSevereEnd = SmsCbConstants.MESSAGE_ID_CMAS_ALERT_SEVERE_EXPECTED_LIKELY;
                        int cmasAmber = SmsCbConstants.MESSAGE_ID_CMAS_ALERT_CHILD_ABDUCTION_EMERGENCY;
                        int cmasTestStart = SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST;
                        int cmasTestEnd = SmsCbConstants.MESSAGE_ID_CMAS_ALERT_OPERATOR_DEFINED_USE;
                        int cmasPresident = SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL;
                        // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
                        int cmasSpanishBegin = SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE;
                        int cmasSpanishEnd = TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd();
                        // [BUGFIX]-MOD-END by bin.xue
                        //add by yong,wang for fr 7028757 at 2018-10-17 begin
                        int letCmasExtremeStart = 0;
                        int letCmasExtremeEnd = 0;
                        int letCmasSevereStart = 0;
                        int letCmasSevereEnd = 0;
                        int letCmasAmber = 0;
                        int letCmasTestStart = 0;
                        int letCmasTestEnd = 0;
                        int letCmasPresident = 0;
                         
                        if(!platform.startsWith("MT")){
                            letCmasExtremeStart =
                                            SmsEnvelope.SERVICE_CATEGORY_CMAS_EXTREME_THREAT;//4097
                            letCmasExtremeEnd = letCmasExtremeStart;
                            letCmasSevereStart = SmsEnvelope.
                                            SERVICE_CATEGORY_CMAS_SEVERE_THREAT;//4098
                            letCmasSevereEnd = letCmasSevereStart;
                            letCmasAmber = SmsEnvelope.
                                            SERVICE_CATEGORY_CMAS_CHILD_ABDUCTION_EMERGENCY;//4099
                            letCmasTestStart = SmsEnvelope.SERVICE_CATEGORY_CMAS_TEST_MESSAGE;//4100
                            letCmasTestEnd = letCmasTestStart;
                            letCmasPresident = SmsEnvelope.
                                            SERVICE_CATEGORY_CMAS_PRESIDENTIAL_LEVEL_ALERT;//4096
                        }
                         //add by yong,wang for fr 7028757 at 2018-10-17 end
                        // set to CDMA broadcast ID rage if phone is in CDMA mode.
                        boolean isCdma = CellBroadcastReceiver.phoneIsCdma(mSubscription);
                        Log.w(TAG, "onHandleIntent isCdma = " + isCdma); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                         //add by yong,wang for fr 7028757 at 2018-10-17 begin
                        if (isCdma && platform.startsWith("MT")) {
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-20,BUG-2845457*/
                            cmasExtremeStart =
                                            SmsEnvelope.SERVICE_CATEGORY_CMAS_EXTREME_THREAT;//4097
                            cmasExtremeEnd = cmasExtremeStart;
                            cmasSevereStart = SmsEnvelope.SERVICE_CATEGORY_CMAS_SEVERE_THREAT;//4098
                            cmasSevereEnd = cmasSevereStart;
                            cmasAmber = SmsEnvelope.
                                            SERVICE_CATEGORY_CMAS_CHILD_ABDUCTION_EMERGENCY;//4099
                            cmasTestStart = SmsEnvelope.SERVICE_CATEGORY_CMAS_TEST_MESSAGE;//4100
                            cmasTestEnd = cmasTestStart;
                            cmasPresident = SmsEnvelope.
                                            SERVICE_CATEGORY_CMAS_PRESIDENTIAL_LEVEL_ALERT;//4096
                            /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                        }
                         //add by yong,wang for fr 7028757 at 2018-10-17 end
                        // Check for system property defining the emergency channel ranges to enable
                        String emergencyIdRange = isCdma ?
                                "" : SystemProperties.get(EMERGENCY_BROADCAST_RANGE_GSM);
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                        Log.w(TAG, "onHandleIntent emergencyIdRange = " + emergencyIdRange);
                        if (enableEmergencyAlerts || isWpasEnable) { // MODIFIED by yuxuan.zhang, 2016-06-08,BUG-2219480
                            if (DBG) Log.w(TAG, "enabling emergency cell broadcast channels");
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                            if (!TextUtils.isEmpty(emergencyIdRange)) {
                                setChannelRange(emergencyIdRange, true);
                            } else {
                                // No emergency channel system property, enable all emergency channels
                                // that have checkbox checked
                                if (!isCdma) {
                                    enableCellBroadcastRange(
                                            SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_WARNING,
                                            SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_AND_TSUNAMI_WARNING,
                                            isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_WARNING,
//                                            SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_AND_TSUNAMI_WARNING, -1, -1, true));
//                                    i++;
                                    Log.d(TAG, "enable1 " + SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_WARNING + " -- " + SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_AND_TSUNAMI_WARNING);
                                    if (enableEtwsTestAlerts) {
                                        enableCellBroadcast(SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE,
                                                isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE,
//                                                SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE, -1, -1, true));
//                                        i++;
                                        Log.d(TAG, "enable2 " + SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE);
                                    }
                                    enableCellBroadcast(
                                            SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE, isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE,
//                                            SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE, -1, -1, true));
//                                    i++;
                                    Log.d(TAG, "enable3 " + SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE);
                                }
                                if (enableCmasExtremeAlerts) {
                                    enableCellBroadcastRange(cmasExtremeStart, cmasExtremeEnd, isMSim);
//                                  infos.add(new SmsBroadcastConfigInfo(cmasExtremeStart, cmasExtremeEnd, -1, -1, true));
//                                    i++;
                                     //add by yong,wang for fr 7028757 at 2018-10-17 begin
                                    if(!platform.startsWith("MT")){
                                        enableCellBroadcastRange(
                                                        letCmasExtremeStart, letCmasExtremeEnd, isMSim);
                                     }
                                      //add by yong,wang for fr 7028757 at 2018-10-17 end
                                    Log.d(TAG, "enable4 " + cmasExtremeStart + " -- " + cmasExtremeEnd);
                                }
                                if (enableCmasSevereAlerts) {
                                    enableCellBroadcastRange(cmasSevereStart, cmasSevereEnd, isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(cmasSevereStart, cmasSevereEnd, -1, -1, true));
//                                    i++;
                                    Log.d(TAG, "enable5 " + cmasSevereStart + " -- " + cmasSevereEnd);
                                     //add by yong,wang for fr 7028757 at 2018-10-17 begin
                                    if(!platform.startsWith("MT")){
                                        enableCellBroadcastRange(
                                                        letCmasSevereStart, letCmasSevereEnd, isMSim);
                                     }
                                      //add by yong,wang for fr 7028757 at 2018-10-17 end
                                }
                                if (enableCmasAmberAlerts) {
                                    enableCellBroadcast(cmasAmber, isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(cmasAmber, cmasAmber, -1, -1, true));
//                                    i++;
                                    Log.d(TAG, "enable6 " + cmasAmber);
                                     //add by yong,wang for fr 7028757 at 2018-10-17 begin
                                    if(!platform.startsWith("MT")){
                                        enableCellBroadcast(letCmasAmber, isMSim);
                                    }
                                     //add by yong,wang for fr 7028757 at 2018-10-17 end
                                }
                                //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,01/21/2015,895849,[SCB][CMAS]Can't receive RMT&Exercise alerts
                                //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/10/2015,886284,an receive CMAS and ETWS even not enable "show ETWS/CMAS test broadcasts"
                                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-08,BUG-2219480*/
                                if (enableCmasRMTAlerts && isEnableRMTExceriseAlertType || isWpasEnable) {
                                    enableCellBroadcast(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST, isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST, -1, -1, true));
//                                    i++;
                                    Log.d(TAG, "enable7 " + SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST);
                                    if(!platform.startsWith("MT")){
                                        enableCellBroadcast(
                                                            SmsEnvelope.SERVICE_CATEGORY_CMAS_TEST_MESSAGE, isMSim);
                                    }
                                }
                                if (enableCmasExerciseAlerts && isEnableRMTExceriseAlertType) {
                                    enableCellBroadcast(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE, isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE, -1, -1, true));
//                                    i++;
                                    Log.d(TAG, "enable8 " + SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE);
                                }
                                //[BUGFIX]-Add-END by TSCD.fujun.yang
                                if (enableCmasTestAlerts && !isEnableRMTExceriseAlertType || (isWpasEnable && enableWpasTest)) {
                                    enableCellBroadcastRange(cmasTestStart, cmasTestEnd, isMSim);
                                    Log.d(TAG, "enable9 " + cmasTestStart + " -- " + cmasTestEnd);
//                                    infos.add(new SmsBroadcastConfigInfo(cmasTestStart, cmasTestEnd, -1, -1, true)); // MODIFIED by yuwan, 2017-06-07,BUG-4903321
//                                    i++;
                                     //add by yong,wang for fr 7028757 at 2018-10-17 begin
                                    if(!platform.startsWith("MT")){
                                        enableCellBroadcastRange(
                                                    letCmasTestStart, letCmasTestEnd, isMSim);
                                    }
                                     //add by yong,wang for fr 7028757 at 2018-10-17 end
                                } else if (isWpasEnable && !enableWpasTest) {
                                    disableCellBroadcastRange(cmasTestStart + 1, cmasTestEnd, isMSim);
                                    Log.d(TAG, "disable10 " + (cmasTestStart + 1) + " -- " + cmasTestEnd);
//                                    infos.add(new SmsBroadcastConfigInfo(cmasTestStart + 1, cmasTestEnd, -1, -1, false));
//                                    i++;
                                     //add by yong,wang for fr 7028757 at 2018-10-17 begin
                                    if(!platform.startsWith("MT")){
                                        disableCellBroadcastRange(
                                                            letCmasTestStart + 1, letCmasTestEnd, isMSim);
                                    }
                                     //add by yong,wang for fr 7028757 at 2018-10-17 end
                                }
                                //[BUGFIX]-Add-END by TSCD.fujun.yang
                                // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
                                //[BUGFIX]-MOD-BEGIN by chaobing.huang for PR1104825
                                if (enableCmasSpanishAlerts || isWpasEnable) {
                                    if (enableCmasExtremeAlerts) {
                                        if (DBG)
                                            Log.w(TAG, "4384-4385 enabling Spanish cell broadcast CMAS extreme"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                                           /* MODIFIED-END by yuxuan.zhang,BUG-2219480*/
                                           enableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateObserved(),
                                                       TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateLikely(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateObserved(), TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateLikely(), -1, -1, true));
//                                        i++;
                                    } else {
                                        disableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateObserved(),
                                                TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateLikely(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateObserved(), TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateLikely(), -1, -1, false));
//                                        i++;
                                        Log.d(TAG, "disable12 4384-4385");
                                    }
                                    if (enableCmasSevereAlerts) {
                                            enableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExtremeExpectedObserved(),
                                                    TctWrapperManager.getTctCmasAlertSpanishServereExpectedLikely(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExtremeExpectedObserved()
//                                                , TctWrapperManager.getTctCmasAlertSpanishServereExpectedLikely(), -1, -1, true));
//                                        i++;
                                        Log.d(TAG, "enable13 4386-4391");
                                    } else {
                                        disableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExtremeExpectedObserved(),
                                                TctWrapperManager.getTctCmasAlertSpanishServereExpectedLikely(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExtremeExpectedObserved()
//                                                , TctWrapperManager.getTctCmasAlertSpanishServereExpectedLikely(), -1, -1, false));
//                                        i++;
                                        Log.d(TAG, "disable14 4386-4391");
                                    }
                                    if (enableCmasAmberAlerts) {
                                           /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                                            enableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishAmberAlert()
//                                                , TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), -1, -1, true));
//                                        i++;
                                        Log.d(TAG, "enable15 4392");
                                    } else {
                                        disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishAmberAlert()
//                                                , TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), -1, -1, false));
//                                        i++;
                                        Log.d(TAG, "disable16 4392");
                                    }
                                    if (enableCmasRMTAlerts && isEnableRMTExceriseAlertType || isWpasEnable) {
                                        enableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest()
//                                                , TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), -1, -1, true));
//                                        i++;
                                        Log.d(TAG, "ensable17 4393");
                                    } else {
                                        disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest()
//                                                , TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), -1, -1, false));
//                                        i++;
                                        Log.d(TAG, "disable18 4393");
                                    }
                                    if (enableCmasExerciseAlerts && isEnableRMTExceriseAlertType || (isWpasEnable && enableWpasTest)) {
                                            enableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishExercise(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExercise()
//                                                , TctWrapperManager.getTctCmasAlertSpanishExercise(), -1, -1, true));
//                                        i++;
                                        Log.d(TAG, "enable19 4394");
                                    } else {
                                        disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishExercise(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExercise()
//                                                , TctWrapperManager.getTctCmasAlertSpanishExercise(), -1, -1, false));
//                                        i++;
                                        Log.d(TAG, "disable20 4394");
                                    }
                                    if (enableCmasTestAlerts && !isEnableRMTExceriseAlertType || (isWpasEnable && enableWpasTest)) {
                                        if (DBG)
                                           /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                                           enableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest()
                                                   ,TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(),isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest()
//                                                , TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(), -1, -1, true));
//                                        i++;
                                        Log.d(TAG, "enable21 4395");
                                    } else if (!isEnableRMTExceriseAlertType && isWpasEnable && !enableWpasTest) {
                                        disableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExercise()
                                                   ,TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(), isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExercise()
//                                                , TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(), -1, -1, false));
//                                        i++;
                                        Log.d(TAG, "disable22 " + TctWrapperManager.getTctCmasAlertSpanishExercise() + " - " + TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd());
                                    } else if (!isEnableRMTExceriseAlertType) {
                                        disableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest()
                                                   ,TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(),isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest()
//                                                , TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(), -1, -1, false));
//                                        i++;
                                        Log.d(TAG, "disable23 " + TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest() + " - " + TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd());
                                    } else {
                                        disableCellBroadcast(TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(),isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd()
//                                                , TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(), -1, -1, false));
//                                        i++;
                                        Log.d(TAG, "disable24 " + TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd());
                                    }
                                    enableCellBroadcast(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE,isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE
//                                            , SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE, -1, -1, true));
//                                    i++;
                                    Log.d(TAG, "enable25 " + TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd());
                                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-01,BUG-2344436*/
                                    if (isWpasEnable) {
                                        enableCellBroadcastRange(CellBroadcastSettings.WPAS_OPERATOR_NINE_FIVE + 1, CellBroadcastSettings.WPAS_ALERT_FREQUENCY_END, isMSim);
//                                        infos.add(new SmsBroadcastConfigInfo(CellBroadcastSettings.WPAS_OPERATOR_NINE_FIVE
//                                                , CellBroadcastSettings.WPAS_ALERT_FREQUENCY_END, -1, -1, true));
//                                        i++;
                                        Log.d(TAG, "enable26 4396 - 4399");
                                    }
                                       /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/

                                    if (CellBroadcastSettings.cbIntent != null) {
                                        Log.w(TAG, "onreceive Spanish Presidential"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                                        sendBroadcast(CellBroadcastSettings.cbIntent);
                                        CellBroadcastSettings.cbIntent = null;
                                    }
                                }
                                //[BUGFIX]-MOD-END by chaobing.huang for PR1104825
                                // [BUGFIX]-MOD-END by bin.xue
                                // CMAS Presidential must be on (See 3GPP TS 22.268 Section 6.2).
                                enableCellBroadcast(cmasPresident, isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(cmasPresident
//                                        , cmasPresident, -1, -1, true));
//                                i++;
                            }
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                            if (DBG) Log.w(TAG, "enabled emergency cell broadcast channels");
                        } else {
                            // we may have enabled these channels previously, so try to disable them
                            if (DBG) Log.w(TAG, "disabling emergency cell broadcast channels");
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                            if (!TextUtils.isEmpty(emergencyIdRange)) {
                                setChannelRange(emergencyIdRange, false);
                            } else {
                                // No emergency channel system property, disable all emergency channels
                                // except for CMAS Presidential (See 3GPP TS 22.268 Section 6.2)
                                if (!isCdma) {
                                    disableCellBroadcastRange(
                                            SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_WARNING,
                                            SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_AND_TSUNAMI_WARNING,
                                            isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_WARNING,
//                                            SmsCbConstants.MESSAGE_ID_ETWS_EARTHQUAKE_AND_TSUNAMI_WARNING, -1, -1, false));
//                                    i++;
                                    disableCellBroadcast(SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE,
                                            isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE,
//                                            SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE, -1, -1, false));
//                                    i++;
                                    disableCellBroadcast(
                                            SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE, isMSim);
//                                    infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE,
//                                            SmsCbConstants.MESSAGE_ID_ETWS_OTHER_EMERGENCY_TYPE, -1, -1, false));
//                                    i++;
                                }
                                disableCellBroadcastRange(cmasExtremeStart, cmasExtremeEnd, isMSim);
                                disableCellBroadcastRange(cmasSevereStart, cmasSevereEnd, isMSim);
                                disableCellBroadcast(cmasAmber, isMSim);
                                disableCellBroadcastRange(cmasTestStart, cmasTestEnd, isMSim);
                                 //add by yong,wang for fr 7028757 at 2018-10-17 begin                                
                                if(!platform.startsWith("MT")){
                                    disableCellBroadcastRange(
                                                        letCmasExtremeStart, letCmasExtremeEnd, isMSim);
                                    disableCellBroadcastRange(
                                                        letCmasSevereStart, letCmasSevereEnd, isMSim);
                                    disableCellBroadcast(letCmasAmber, isMSim);
                                    disableCellBroadcastRange(letCmasTestStart, letCmasTestEnd, isMSim);
                                }
                                 //add by yong,wang for fr 7028757 at 2018-10-17 end

                                //[BUGFIX]-MOD-BEGIN by chaobing.huang for PR1104825
                                disableCellBroadcast(cmasSpanishBegin, isMSim);
                                disableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateObserved(),
                                        TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateLikely(), isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateObserved(),
//                                        TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateLikely(), -1, -1, false));
//                                i++;
                                disableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExtremeExpectedObserved(),
                                        TctWrapperManager.getTctCmasAlertSpanishServereExpectedLikely(), isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExtremeExpectedObserved(),
//                                        TctWrapperManager.getTctCmasAlertSpanishServereExpectedLikely(), -1, -1, false));
//                                i++;
                                disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishAmberAlert(),
//                                        TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), -1, -1, false));
//                                i++;
                                disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(),
//                                        TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), -1, -1, false));
//                                i++;
                                disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishExercise(), isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExercise(),
//                                        TctWrapperManager.getTctCmasAlertSpanishExercise(), -1, -1, false));
//                                i++;
                                disableCellBroadcast(TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(),isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(),
//                                        TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(), -1, -1, false));
//                                i++;
                                //[BUGFIX]-MOD-END by chaobing.huang for PR1104825

                                // CMAS Presidential must be on (See 3GPP TS 22.268 Section 6.2).
                                enableCellBroadcast(cmasPresident, isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(cmasPresident, cmasPresident, -1, -1, true));
//                                i++;
                            }
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                            if (DBG) Log.w(TAG, "disabled emergency cell broadcast channels");
                        }

                        if (isCdma) {
                            if (DBG) Log.w(TAG, "channel 50 is not applicable for cdma");
                        } else if (enableChannel50Alerts) {
                            if (DBG) Log.w(TAG, "enabling cell broadcast channel 50");
                            enableCellBroadcast(50, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(50, 50, -1, -1, true));
//                            i++;
                            //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,03/24/2015,914630,SMSCB behavior in Android phones
                        } else if (!enableChannel50Support) {
                            if (DBG) Log.w(TAG, "cell broadcast channel 50 default is false");
                            //[FEATURE]-Add-END by TSCD.fujun.yang
                        } else {
                            if (DBG) Log.w(TAG, "disabling cell broadcast channel 50");
                            disableCellBroadcast(50, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(50, 50, -1, -1, false));
//                            i++;
                        }

                        // Enable Channel 60 for India
                        if (isCdma) {
                            if (DBG) Log.w(TAG, "channel 60 is not applicable for cdma");
                        } else if (enableChannel60Alerts) {
                            if (DBG) Log.w(TAG, "enabling cell broadcast channel 60");
                            enableCellBroadcast(60, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(60, 60, -1, -1, true));
//                            i++;
                            //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,03/24/2015,914630,SMSCB behavior in Android phones
                        } else if (!enableChannel60Support) {
                            if (DBG) Log.w(TAG, "cell broadcast channel 60 default is false");
                            //[FEATURE]-Add-END by TSCD.fujun.yang
                        } else {
                            if (DBG) Log.w(TAG, "disabling cell broadcast channel 60");
                            disableCellBroadcast(60, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(60, 60, -1, -1, false));
//                            i++;
                        }

                        if ("il".equals(tm.getSimCountryIso()) || "il".equals(tm.getNetworkCountryIso())
                                || getResources().getBoolean(R.bool.def_isSupport_919_928)) {
                            /* MODIFIED yuwan, 05-05-2017,BUG-4623807*/
                            if (DBG) Log.w(TAG, "enabling channels 919-928 for Israel");
                            enableCellBroadcastRange(919, 928, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(919, 928, -1, -1, true));
//                            i++;
                        } else {
                            if (DBG) Log.w(TAG, "disabling channels 919-928");
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                            disableCellBroadcastRange(919, 928, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(919, 928, -1, -1, false));
//                            i++;
                        }

                        // Disable per user preference/checkbox.
                        // This takes care of the case where enableEmergencyAlerts is true,
                        // but check box is unchecked to receive such as cmas severe alerts.
                        if (!enableEtwsTestAlerts && !isCdma) {
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                            if (DBG) Log.w(TAG, "disabling cell broadcast ETWS test messages");
                            disableCellBroadcast(
                            SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE,
//                                    SmsCbConstants.MESSAGE_ID_ETWS_TEST_MESSAGE, -1, -1, false));
//                            i++;
                        }
                        if (!enableCmasExtremeAlerts) {
                            if (DBG) Log.w(TAG, "disabling cell broadcast CMAS extreme");
                            disableCellBroadcastRange(cmasExtremeStart, cmasExtremeEnd, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(cmasExtremeStart, cmasExtremeEnd, -1, -1, false));
//                            i++;
                             //add by yong,wang for fr 7028757 at 2018-10-17 begin
                            if(!platform.startsWith("MT")){
                                    disableCellBroadcastRange(
                                                    letCmasExtremeStart, letCmasExtremeEnd, isMSim);
                             }
                              //add by yong,wang for fr 7028757 at 2018-10-17 end
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-19,BUG-1112693*/
                            if (enableCustomizedCmas) {
                                enableCellBroadcast(4372, isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(4372, 4372, -1, -1, true));
//                                i++;
                            }
                        }
                        if (!enableCmasSevereAlerts) {
                            if (DBG) Log.w(TAG, "disabling cell broadcast CMAS severe");
                            disableCellBroadcastRange(cmasSevereStart, cmasSevereEnd, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(cmasSevereStart, cmasSevereEnd, -1, -1, false));
//                            i++;
                             //add by yong,wang for fr 7028757 at 2018-10-17 begin
                            if(!platform.startsWith("MT")){
                                disableCellBroadcastRange(letCmasSevereStart, letCmasSevereEnd, isMSim);
                            }   
                             //add by yong,wang for fr 7028757 at 2018-10-17 end
                            if (enableCustomizedCmas) {
                                enableCellBroadcast(4373, isMSim);
//                                infos.add(new SmsBroadcastConfigInfo(4373, 4373, -1, -1, true));
//                                i++;
                            }
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                        }
                        if (!enableCmasAmberAlerts) {
                            if (DBG) Log.w(TAG, "disabling cell broadcast CMAS amber");
                            disableCellBroadcast(cmasAmber, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(cmasAmber, cmasAmber, -1, -1, false));
//                            i++;
                            Log.d(TAG, "disabled cell broadcast CMAS amber");
                             //add by yong,wang for fr 7028757 at 2018-10-17 begin
                            if(!platform.startsWith("MT")){
                                disableCellBroadcast(letCmasAmber, isMSim);
                            }
                             //add by yong,wang for fr 7028757 at 2018-10-17 end
                        }
                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/10/2015,886284,an receive CMAS and ETWS even not enable "show ETWS/CMAS test broadcasts"
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-08,BUG-2219480*/
                        if (!enableCmasTestAlerts && !isEnableRMTExceriseAlertType && !isWpasEnable) {
                            if (DBG) Log.w(TAG, "disabling cell broadcast CMAS test messages");
                            disableCellBroadcastRange(cmasTestStart, cmasTestEnd, isMSim);
                             //add by yong,wang for fr 7028757 at 2018-10-17 begin
                            if(!platform.startsWith("MT")){
                                disableCellBroadcastRange(letCmasTestStart, letCmasTestEnd, isMSim);
                            }
                             //add by yong,wang for fr 7028757 at 2018-10-17 end
//                            infos.add(new SmsBroadcastConfigInfo(cmasTestStart, cmasTestEnd, -1, -1, false));
//                            i++;
                        } else if (isWpasEnable && !enableWpasTest) {
                            Log.w(TAG, "disabling cell broadcast wpas test messages");
                            disableCellBroadcastRange(cmasTestStart + 1, cmasTestEnd, isMSim);
                             //add by yong,wang for fr 7028757 at 2018-10-17 begin
                            if(!platform.startsWith("MT")){
                                disableCellBroadcastRange(letCmasTestStart + 1, letCmasTestEnd, isMSim);
                            }
                             //add by yong,wang for fr 7028757 at 2018-10-17 end
//                            infos.add(new SmsBroadcastConfigInfo(cmasTestStart + 1, cmasTestEnd, -1, -1, false));
//                            i++;
                        }
                        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,01/21/2015,895849,[SCB][CMAS]Can't receive RMT&Exercise alerts
                        if (!enableCmasRMTAlerts && isEnableRMTExceriseAlertType && !isWpasEnable) {
                            if (DBG) Log.w(TAG, "disabling cell broadcast CMAS RMT");
                            disableCellBroadcast(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST, isMSim);
                             //add by yong,wang for fr 7028757 at 2018-10-17 begin
                            if(!platform.startsWith("MT")){
                                disableCellBroadcast(
                                                SmsEnvelope.SERVICE_CATEGORY_CMAS_TEST_MESSAGE, isMSim);
                            }
                             //add by yong,wang for fr 7028757 at 2018-10-17 end
//                            infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_REQUIRED_MONTHLY_TEST, -1, -1, false));
//                            i++;
                        }
                        if (!enableCmasExerciseAlerts && isEnableRMTExceriseAlertType && !isWpasEnable) {
                            if (DBG) Log.w(TAG, "disabling cell broadcast CMAS Exercise");
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                            disableCellBroadcast(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE, SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXERCISE, -1, -1, false));
//                            i++;
                        }
                        // [BUGFIX]-MOD-BEGIN by bin.xue for PR1077032
                        //[BUGFIX]-MOD-BEGIN by chaobing.huang for PR1104825
                        if (!enableCmasSpanishAlerts) { // MODIFIED by yuxuan.zhang, 2016-06-28,BUG-2389849
                            /* MODIFIED-END by yuxuan.zhang,BUG-2219480*/
                             //add by yong,wang for fr 7028757 at 2018-10-17 begin
                            if(platform.startsWith("MT")){
                                            disableCellBroadcast(cmasSpanishBegin,isMSim);
                            }
                             //add by yong,wang for fr 7028757 at 2018-10-17 end
                            disableCellBroadcast(SmsCbConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE,isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(cmasPresident, cmasPresident, -1, -1, true));
//                            i++;
                            Log.i(TAG, "disabling cell broadcast CMAS Exercise========>2015.10.10-begin"); // MODIFIED by yuxuan.zhang, 2016-06-07,BUG-1748495
                            disableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateObserved(),
                                               TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateLikely(), isMSim);
                            /* MODIFIED-BEGIN by yuwan, 2017-06-14,BUG-4932893*/
//                            infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.
//                                    getTctCmasAlertSpanishExtremeImmediateObserved(),
//                                    TctWrapperManager.getTctCmasAlertSpanishExtremeImmediateLikely()
//                                    , -1, -1, false));
//                            i++;
                            disableCellBroadcastRange(TctWrapperManager.getTctCmasAlertSpanishExtremeExpectedObserved(),
                                               TctWrapperManager.getTctCmasAlertSpanishServereExpectedLikely(), isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.
//                                    getTctCmasAlertSpanishExtremeExpectedObserved(),
//                                    TctWrapperManager.getTctCmasAlertSpanishServereExpectedLikely(),
//                                    -1, -1, false));
                            /* MODIFIED-END by yuwan,BUG-4932893*/
//                            i++;
                            disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), TctWrapperManager.getTctCmasAlertSpanishAmberAlert(), -1, -1, false));
//                            i++;
                            disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), TctWrapperManager.getTctCmasAlertSpanishRequiredMonthlyTest(), -1, -1, false));
//                            i++;
                            disableCellBroadcast(TctWrapperManager.getTctCmasAlertSpanishExercise(), isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertSpanishExercise(), TctWrapperManager.getTctCmasAlertSpanishExercise(), -1, -1, false));
//                            i++;
                            disableCellBroadcast(TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(),isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(), TctWrapperManager.getTctCmasAlertPersidentialLevelLanguageEnd(), -1, -1, false));
//                            i++;
                            Log.i(TAG, "disabling cell broadcast CMAS Exercise========>2015.10.10-end"); // MODIFIED by yuxuan.zhang, 2016-06-07,BUG-1748495
                        }
                        //[BUGFIX]-MOD-END by chaobing.huang for PR1104825

                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-01,BUG-2344436*/
                        if (!isWpasEnable) {
                            disableCellBroadcastRange(4396, 4399, isMSim);
//                            infos.add(new SmsBroadcastConfigInfo(4396, 4399, -1, -1, false));
//                            i++;
                        }
                        Log.d(TAG, "finish disable");
                        /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/
                        // [BUGFIX]-MOD-END by bin.xue
                        //[BUGFIX]-Add-END by TSCD.fujun.yang
                        //[BUGFIX]-Add-END by TSCD.fujun.yang
                    } catch (Exception ex) {
                        Log.e(TAG, "exception enabling cell broadcast channels", ex); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                    }
                }
                
                //  modify by liang.zhang for Defect 5960221 at 2018-01-30 begin
                String plmn = "";
                boolean isUAE = false;
                boolean isPeru = false;
                boolean isMexico = false;
                boolean isChile = false;
                boolean isCanada = false;  //add by liang.zhang for Defect 6353603 at 2018-06-02
                boolean isRomania = false; //add by liang.zhang for Defect 6364780 at 2018-06-05
                boolean isSalvador = false; //add by liang.zhang for Defect 6438931 at 2018-06-20
                TelephonyManager tm = (TelephonyManager) getSystemService(
                        Context.TELEPHONY_SERVICE);
                if (isMSim) {
                    plmn = TelephonyManager.getDefault().getNetworkOperator(mSubscription);
                } else {
                    plmn = tm.getNetworkOperator();
                }
                if(plmn != null && plmn.length() > 4){
                    String sub = plmn.subSequence(0, 3).toString();
                    Log.d(TAG, "mcc="+sub);
                    if(sub.equals("424")){
                        isUAE = true;
                    } else if (sub.equals("716")) {
                        isPeru = true;
                    } else if (sub.equals("730")) {
                        isChile = true;
                    } else if (sub.equals("334")) {
                        isMexico = true;
                    }
                    // add by liang.zhang for Defect 6353603 at 2018-06-02 begin
                    else if (sub.equals("302")) {
                        isCanada = true;
                    }
                    // add by liang.zhang for Defect 6353603 at 2018-06-02 end
                    // add by liang.zhang for Defect 6364780 at 2018-06-05 begin
                    else if (sub.equals("226")) {
                        isRomania = true;
                    }
                    // add by liang.zhang for Defect 6364780 at 2018-06-05 end
                    // add by liang.zhang for Defect 6438931 at 2018-06-20 begin
                    else if (sub.equals("706")) {
                        isSalvador = true;
                    }
                    // add by liang.zhang for Defect 6438931 at 2018-06-20 end
                }
                
                SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
                SubscriptionInfo subInfo = null;
                if (isMSim) {
                    subInfo = subscriptionManager.getActiveSubscriptionInfo(mSubscription);
                } else {
                    subInfo = subscriptionManager.getActiveSubscriptionInfo(subscriptionManager.getDefaultSubscriptionId());
                }
                if (subInfo!= null && subInfo.getMcc() == 424) {
                    isUAE = true;
                } else if (subInfo!= null && subInfo.getMcc() == 716) {
                    isPeru = true;
                } else if (subInfo!= null && subInfo.getMcc() == 730) {
                    isChile = true;
                } else if (subInfo!= null && subInfo.getMcc() == 334) {
                    isMexico = true;
                }
                // add by liang.zhang for Defect 6353603 at 2018-06-02 begin
                else if (subInfo!= null && subInfo.getMcc() == 302) {
                    isCanada = true;
                }
                // add by liang.zhang for Defect 6353603 at 2018-06-02 end
                // add by liang.zhang for Defect 6364780 at 2018-06-05 begin
                else if (subInfo!= null && subInfo.getMcc() == 226) {
                    isRomania = true;
                }
                // add by liang.zhang for Defect 6364780 at 2018-06-05 end
                
                // add by liang.zhang for Defect 6438931 at 2018-06-20 begin
                else if (subInfo!= null && subInfo.getMcc() == 706) {
                    isSalvador = true;
                }
                // add by liang.zhang for Defect 6438931 at 2018-06-20 end
                
                if (isUAE) {
                    enableCellBroadcastRange(4383, 4394, isMSim);
                }
                
                if (isPeru) {
                    enableCellBroadcast(4370, isMSim);
                    enableCellBroadcastRange(4380, 4383, isMSim);
                    enableCellBroadcastRange(4396, 4399, isMSim);
                    disableCellBroadcast(919, isMSim);
                    disableCellBroadcast(519, isMSim);
                }
                
                if (isMexico) {
                    enableCellBroadcastRange(4370, 4378, isMSim);
                    enableCellBroadcastRange(4380, 4381, isMSim);
                    enableCellBroadcastRange(4383, 4391, isMSim);
                    enableCellBroadcast(6400, isMSim);
                    enableCellBroadcastRange(4396, 4399, isMSim);
                    // modify by liang.zhang for Defect 6925301 at 2018-09-06 begin
                    disableCellBroadcast(519, isMSim);
                    enableCellBroadcast(919, isMSim);
                    // modify by liang.zhang for Defect 6925301 at 2018-09-06 end
                }
                
                if (isChile) {
                    enableCellBroadcast(4370, isMSim);
                    disableCellBroadcast(919, isMSim);
                }
                
                // add by liang.zhang for Defect 6353603 at 2018-06-02 begin
                if (isCanada) {
                    disableCellBroadcast(4380, isMSim);
                }
                // add by liang.zhang for Defect 6353603 at 2018-06-02 end
                
                // add by liang.zhang for Defect 6364780 at 2018-06-05 begin
                if (isRomania) {
                    enableCellBroadcastRange(916, 919, isMSim);
                }
                // add by liang.zhang for Defect 6364780 at 2018-06-05 end
                
                // add by liang.zhang for Defect 6438931 at 2018-06-20 begin
                if (isSalvador) {
                    enableCellBroadcastRange(50, 50, isMSim);
                    enableCellBroadcastRange(916, 919, isMSim);
                    enableCellBroadcastRange(4370, 4370, isMSim);
                }
                // add by liang.zhang for Defect 6438931 at 2018-06-20 begin
                // modify by liang.zhang for Defect 5960221 at 2018-01-30 end
                
                if (isMSim) {
                    subId = mSubscription;
                } else {
//                    manager = MtkSmsManager.getDefault();
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                }
//                int size = infos.size();
//                SmsBroadcastConfigInfo[] info = (SmsBroadcastConfigInfo[]) infos.toArray(new SmsBroadcastConfigInfo[size]);
//                TctWrapperManager.setCellBroadcastSmsConfig(info, info, manager);
//                Log.d(MTK_TAG, "setCellBroadcastSmsConfig" + " channel number = " + i);
            
    }
    //add by yong.wang for Defect 7028757 for integration the QCOM and MTK processing logic on 2018-10-17 end

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
           doOnHandleIntent(intent);//modify by yong.wang for Defect 7028757 for integration the QCOM and MTK processing logic on 2018-10-17 
        } catch (Exception e) {
           doOnHandleIntent(intent);//modify by yong.wang for Defect 7028757 for integration the QCOM and MTK processing logic on 2018-10-17
        }
    }

    private static void log(String msg) {
        TLog.d(TAG, msg);
    }
}
