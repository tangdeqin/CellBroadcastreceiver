/******************************************************************************/
/*                                                               Date:12/2012 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2012 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  Tongyuan.Lv                                                     */
/*  Email  :  Tongyuan.Lv@tcl-mobile.com                                      */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments : Add this file to test CB, must be delete in official release   */
/*  File     : packages/apps/cellbroadcastreceiver/src/com/android/           */
/*             cellbroadcastreceiver/CellBroadcastTestReceiver.java           */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 12/11/2012|tongyuan.lv           |FR-352721             |add to test cb    */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsCbCmasInfo;
import android.telephony.SmsCbEtwsInfo;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;

public class CellBroadcastTestReceiver extends BroadcastReceiver {

    private static final int MESSAGE_TYPE_PRESIDENTIAL_ALERT = 4370;
    private static final int MESSAGE_TYPE_EXTREME_ALERT = 4371;  // 4371-4374
    private static final int MESSAGE_TYPE_SEVERE_ALERT = 4375;  // 4375-4378
    private static final int MESSAGE_TYPE_AMBER_ALERT = 4379;
    private static final int MESSAGE_TYPE_TEST_ALERT = 4380;
    private static final int MESSAGE_TYPE_EXERCISE_ALERT = 4381;
    private static final int MESSAGE_TYPE_COMMON_ALERT = -1;

    private static final String EXTRA_KEY_TYPE = "type";
    private static final String EXTRA_KEY_IDENTIFIER = "identifier";
    private static final String EXTRA_KEY_BODY = "body";
    private static final String EXTRA_KEY_COUNT = "count";
    private static final String EXTRA_KEY_EXPIRE_DATE = "expire_date";
    private static final String EXTRA_KEY_CHANNEL = "channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub



        // int messageFormat, int geographicalScope, int serialNumber,
        // SmsCbLocation location, int serviceCategory, String language, String
        // body,
        // int priority, SmsCbEtwsInfo etwsWarningInfo, SmsCbCmasInfo
        // cmasWarningInfo
        int messageFormat = SmsCbMessage.MESSAGE_FORMAT_3GPP;
        int geographicalScope = SmsCbMessage.GEOGRAPHICAL_SCOPE_CELL_WIDE;
        int serialNumber = intent.getIntExtra(EXTRA_KEY_IDENTIFIER, 0);
        SmsCbLocation location = new SmsCbLocation();

        String language = "en";
        String body = intent.getStringExtra(EXTRA_KEY_BODY);

        SmsCbCmasInfo cmasInfo = null;
        int priority = SmsCbMessage.MESSAGE_PRIORITY_NORMAL;
        int serviceCategory = -1;

        try {
            if((intent.getIntExtra(EXTRA_KEY_TYPE, 0) == MESSAGE_TYPE_COMMON_ALERT )) {
                serviceCategory = Integer.valueOf(intent.getStringExtra(EXTRA_KEY_CHANNEL));
            } else {
                cmasInfo = getSmsCbCmasInfo(intent);
                priority = SmsCbMessage.MESSAGE_PRIORITY_EMERGENCY;
                serviceCategory = cmasInfo.getCategory();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        SmsCbMessage cb = new SmsCbMessage(messageFormat, geographicalScope, serialNumber,
                location, serviceCategory, language, body, priority, null, cmasInfo);
        intent.setAction(Telephony.Sms.Intents.SMS_EMERGENCY_CB_RECEIVED_ACTION);
        intent.putExtra("message", cb);
        intent.setClass(context, CellBroadcastAlertService.class);
        context.startService(intent);
    }

    private SmsCbCmasInfo getSmsCbCmasInfo(Intent intent) {
        int messageClass = 0, category, responseType, severity = 0, urgency = 0, certainty = 0;

        category = SmsCbCmasInfo.CMAS_CATEGORY_GEO;
        responseType = SmsCbCmasInfo.CMAS_RESPONSE_TYPE_SHELTER;
        //urgency = SmsCbCmasInfo.CMAS_URGENCY_IMMEDIATE;
        //certainty = SmsCbCmasInfo.CMAS_CERTAINTY_LIKELY;

        switch (intent.getIntExtra(EXTRA_KEY_TYPE, MESSAGE_TYPE_PRESIDENTIAL_ALERT)) {
            case MESSAGE_TYPE_PRESIDENTIAL_ALERT:
                messageClass = SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT;
                break;

            case MESSAGE_TYPE_EXTREME_ALERT:
                messageClass = SmsCbCmasInfo.CMAS_CLASS_EXTREME_THREAT;
                severity = SmsCbCmasInfo.CMAS_SEVERITY_EXTREME;
                urgency = SmsCbCmasInfo.CMAS_URGENCY_IMMEDIATE;
                certainty = SmsCbCmasInfo.CMAS_CERTAINTY_LIKELY;
                break;

            case MESSAGE_TYPE_SEVERE_ALERT:
                messageClass = SmsCbCmasInfo.CMAS_CLASS_SEVERE_THREAT;
                severity = SmsCbCmasInfo.CMAS_SEVERITY_SEVERE;
                urgency = SmsCbCmasInfo.CMAS_URGENCY_EXPECTED;
                certainty = SmsCbCmasInfo.CMAS_CERTAINTY_LIKELY;
                break;

            case MESSAGE_TYPE_AMBER_ALERT:
                messageClass = SmsCbCmasInfo.CMAS_CLASS_CHILD_ABDUCTION_EMERGENCY;
                break;

            case MESSAGE_TYPE_TEST_ALERT:
                messageClass = SmsCbCmasInfo.CMAS_CLASS_REQUIRED_MONTHLY_TEST;
                break;

            case MESSAGE_TYPE_EXERCISE_ALERT:
                messageClass = SmsCbCmasInfo.CMAS_CLASS_CMAS_EXERCISE;
                break;

            default:
                break;
        }

        return new SmsCbCmasInfo(messageClass, category, responseType, severity, urgency, certainty);
    }

}
