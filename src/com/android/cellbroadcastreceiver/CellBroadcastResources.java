/*
 * Copyright (C) 2011 The Android Open Source Project
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
/*==================================================================================*/
/* Modifications on Features list / Changes Request / Problem Report                */
/* ----------|----------------------|----------------------|----------------------- */
/* 08/30/2014|     tianming.lei     |        777440        |Cell broadcast messages */
/*           |                      |                      | have an incorrect format*/
/* ----------|----------------------|----------------------|----------------------- */
/*==================================================================================*/

package com.android.cellbroadcastreceiver;

/*MODIFIED-BEGIN by yuxuan.zhang, 2016-04-19,BUG-838839*/
import com.android.cb.util.TLog;
import com.android.cellbroadcastreceiver.CellBroadcastConfigService;
import com.android.cellbroadcastreceiver.CellBroadcastSettings;
/*MODIFIED-END by yuxuan.zhang,BUG-838839*/

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.SystemProperties;
import android.telephony.CellBroadcastMessage;
import android.telephony.SmsCbCmasInfo;
import android.telephony.SmsCbEtwsInfo;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-04-21,BUG-1112693

/**
 * Returns the string resource ID's for CMAS and ETWS emergency alerts.
 */
public class CellBroadcastResources {

    //[BUGFIX]-Add- begin-by TCTNB.yugang.jia,01/09/2014,525219
    private static final int CHANNEL1 = 919;
    private static final int CHANNEL2 = 921;
    //[BUGFIX]-Add- end-by TCTNB.yugang.jia,01/09/2014,525219
    private static final int CHANNEL3 = 4370;//Add by bin.xue for PR1071073
    private static final int CHANNEL4 = 50;//Add by chenglin.jiang for PR1030633
    private static final int CHANNEL5 = 4383;//Add by chaobing.huang for PR1105747
    private static final int CHANNEL6 = 4386;//Add by chaobing.huang for PR1105747
    private static final int CHANNEL7 = 4392;//Add by chaobing.huang for PR1105747
    private static final int CHANNEL8 = 4393;//Add by chaobing.huang for PR1105747
    private static final int CHANNEL9 = 4394;//Add by chaobing.huang for PR1105747
    private static final int CHANNEL10 = 4395;//Add by chaobing.huang for PR1105747
    private CellBroadcastResources() {
    }

    /**
     * Returns a styled CharSequence containing the message date/time and alert details.
     * @param context a Context for resource string access
     * @return a CharSequence for display in the broadcast alert dialog
     */
    public static CharSequence getMessageDetails(Context context, CellBroadcastMessage cbm) {
        SpannableStringBuilder buf = new SpannableStringBuilder();

        //ADD-Alert-ID-begin-by-chaobing-9/14/2015-PR1084768
        boolean isShowCMASDialogId = context.getResources().getBoolean(R.bool.def_showCMASDialogId);
        if(isShowCMASDialogId && cbm.isCmasMessage()){
        	appendCmasAlertID(context, buf, cbm);
        	appendCmasAlertDetails(context, buf, cbm.getCmasWarningInfo());
        	buf.append("\n");
            buf.append(cbm.getMessageBody());
        }else{
        // Alert date/time
        int start = buf.length();
        buf.append(context.getString(R.string.delivery_time_heading));
        int end = buf.length();
        buf.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        buf.append(" ");
        buf.append(cbm.getDateString(context));

        if (cbm.isCmasMessage()) {
            // CMAS category, response type, severity, urgency, certainty
            appendCmasAlertDetails(context, buf, cbm.getCmasWarningInfo());
        }
        }
        //ADD-Alert-ID-end-by-chaobing-9/14/2015-PR1084768
        return buf;
    }

       //ADD-Alert-ID-begin-by-chaobing-9/14/2015-PR1084768
       private static void appendCmasAlertID(Context context, SpannableStringBuilder buf,
              CellBroadcastMessage cbinfo) {
          int start = buf.length();
          buf.append("ID:");
          int end = buf.length();
          //buf.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          buf.append(" ");
          buf.append("MsgId"+(cbinfo.getServiceCategory()-4369));
       }
       //ADD-Alert-ID-end-by-chaobing-9/14/2015-PR1084768
    private static void appendCmasAlertDetails(Context context, SpannableStringBuilder buf,
            SmsCbCmasInfo cmasInfo) {
        // CMAS category
        int categoryId = getCmasCategoryResId(cmasInfo);
        if (categoryId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_category_heading, categoryId);
        }

        // CMAS response type
        int responseId = getCmasResponseResId(cmasInfo);
        if (responseId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_response_heading, responseId);
        }

        // CMAS severity
        int severityId = getCmasSeverityResId(cmasInfo);
        if (severityId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_severity_heading, severityId);
        }

        // CMAS urgency
        int urgencyId = getCmasUrgencyResId(cmasInfo);
        if (urgencyId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_urgency_heading, urgencyId);
        }

        // CMAS certainty
        int certaintyId = getCmasCertaintyResId(cmasInfo);
        if (certaintyId != 0) {
            appendMessageDetail(context, buf, R.string.cmas_certainty_heading, certaintyId);
        }
    }

    private static void appendMessageDetail(Context context, SpannableStringBuilder buf,
            int typeId, int valueId) {
        if (buf.length() != 0) {
            buf.append("\n");
        }
        int start = buf.length();
        buf.append(context.getString(typeId));
        int end = buf.length();
        //buf.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        buf.append(" ");
        buf.append(context.getString(valueId));
    }

    /**
     * Returns the string resource ID for the CMAS category.
     * @return a string resource ID, or 0 if the CMAS category is unknown or not present
     */
    private static int getCmasCategoryResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getCategory()) {
            case SmsCbCmasInfo.CMAS_CATEGORY_GEO:
                return R.string.cmas_category_geo;

            case SmsCbCmasInfo.CMAS_CATEGORY_MET:
                return R.string.cmas_category_met;

            case SmsCbCmasInfo.CMAS_CATEGORY_SAFETY:
                return R.string.cmas_category_safety;

            case SmsCbCmasInfo.CMAS_CATEGORY_SECURITY:
                return R.string.cmas_category_security;

            case SmsCbCmasInfo.CMAS_CATEGORY_RESCUE:
                return R.string.cmas_category_rescue;

            case SmsCbCmasInfo.CMAS_CATEGORY_FIRE:
                return R.string.cmas_category_fire;

            case SmsCbCmasInfo.CMAS_CATEGORY_HEALTH:
                return R.string.cmas_category_health;

            case SmsCbCmasInfo.CMAS_CATEGORY_ENV:
                return R.string.cmas_category_env;

            case SmsCbCmasInfo.CMAS_CATEGORY_TRANSPORT:
                return R.string.cmas_category_transport;

            case SmsCbCmasInfo.CMAS_CATEGORY_INFRA:
                return R.string.cmas_category_infra;

            case SmsCbCmasInfo.CMAS_CATEGORY_CBRNE:
                return R.string.cmas_category_cbrne;

            case SmsCbCmasInfo.CMAS_CATEGORY_OTHER:
                return R.string.cmas_category_other;

            default:
                return 0;
        }
    }

    /**
     * Returns the string resource ID for the CMAS response type.
     * @return a string resource ID, or 0 if the CMAS response type is unknown or not present
     */
    private static int getCmasResponseResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getResponseType()) {
            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_SHELTER:
                return R.string.cmas_response_shelter;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_EVACUATE:
                return R.string.cmas_response_evacuate;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_PREPARE:
                return R.string.cmas_response_prepare;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_EXECUTE:
                return R.string.cmas_response_execute;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_MONITOR:
                return R.string.cmas_response_monitor;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_AVOID:
                return R.string.cmas_response_avoid;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_ASSESS:
                return R.string.cmas_response_assess;

            case SmsCbCmasInfo.CMAS_RESPONSE_TYPE_NONE:
                return R.string.cmas_response_none;

            default:
                return 0;
        }
    }

    /**
     * Returns the string resource ID for the CMAS severity.
     * @return a string resource ID, or 0 if the CMAS severity is unknown or not present
     */
    private static int getCmasSeverityResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getSeverity()) {
            case SmsCbCmasInfo.CMAS_SEVERITY_EXTREME:
                return R.string.cmas_severity_extreme;

            case SmsCbCmasInfo.CMAS_SEVERITY_SEVERE:
                return R.string.cmas_severity_severe;

            default:
                return 0;
        }
    }

    /**
     * Returns the string resource ID for the CMAS urgency.
     * @return a string resource ID, or 0 if the CMAS urgency is unknown or not present
     */
    private static int getCmasUrgencyResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getUrgency()) {
            case SmsCbCmasInfo.CMAS_URGENCY_IMMEDIATE:
                return R.string.cmas_urgency_immediate;

            case SmsCbCmasInfo.CMAS_URGENCY_EXPECTED:
                return R.string.cmas_urgency_expected;

            default:
                return 0;
        }
    }

    /**
     * Returns the string resource ID for the CMAS certainty.
     * @return a string resource ID, or 0 if the CMAS certainty is unknown or not present
     */
    private static int getCmasCertaintyResId(SmsCbCmasInfo cmasInfo) {
        switch (cmasInfo.getCertainty()) {
            case SmsCbCmasInfo.CMAS_CERTAINTY_OBSERVED:
                return R.string.cmas_certainty_observed;

            case SmsCbCmasInfo.CMAS_CERTAINTY_LIKELY:
                return R.string.cmas_certainty_likely;

            default:
                return 0;
        }
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
    public static boolean checkIsWpasMessage(CellBroadcastMessage cbm) {
        if(cbm == null){
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1112693*/
            TLog.e("Wpas", "Wpas cbm is null");
            return false;
        }

        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-17,BUG-1112693*/
        boolean allowWpas = CellBroadcastReceiverApp.getApplication().getResources().getBoolean(R.bool.def_enable_wpas_function);
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(CellBroadcastReceiverApp.getApplication())) {
        	allowWpas = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        if (!allowWpas) {
            TLog.e("Wpas", "Wpas is not allow");
            return false;
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

        SmsCbCmasInfo cmasInfo;

        try {
            cmasInfo = cbm.getCmasWarningInfo();
        } catch (NullPointerException e) {
            TLog.e("Wpas", "mSmsCbMessage is null");
            cmasInfo = null;
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        if (null != cmasInfo) {
            int cmasInfoClass = cmasInfo.getMessageClass();
            int wpasFrequency = cbm.getServiceCategory();
            boolean extreme = cmasInfoClass != SmsCbCmasInfo.CMAS_CLASS_EXTREME_THREAT;
            boolean server = cmasInfoClass != SmsCbCmasInfo.CMAS_CLASS_SEVERE_THREAT;
            boolean amber = cmasInfoClass != SmsCbCmasInfo.CMAS_CLASS_CHILD_ABDUCTION_EMERGENCY;
            boolean president = cmasInfoClass == SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT;
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-01,BUG-2344436*/
            if (wpasFrequency >= CellBroadcastSettings.WPAS_ALERT_FREQUENCY_BEGIN
                    && wpasFrequency <= CellBroadcastSettings.WPAS_ALERT_FREQUENCY_END
                    /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/
                    && (cmasInfoClass != SmsCbCmasInfo.CMAS_CLASS_EXTREME_THREAT
                            || cmasInfoClass != SmsCbCmasInfo.CMAS_CLASS_SEVERE_THREAT || cmasInfoClass != SmsCbCmasInfo.CMAS_CLASS_CHILD_ABDUCTION_EMERGENCY)
                    || cmasInfoClass == SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT) {
                TLog.i("Wpas", "is Wpas"); // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1112693
                return true;
            }
        }
        return false;
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    
    // add by liang.zhang for Defect 6012945 at 2018-03-07 begin
    public static int getDialogTitleResourceForPeru(CellBroadcastMessage message) {
    	int channelId = message.getServiceCategory();
    	switch (channelId) {
	    	case 4370:
	    	case 4383:
	    		return R.string.title_chile_cb_dialog; //Emergency alert 
	    		
	    	case 4380:
	    		return R.string.peru_dialog_title_test; // Test
	    		
	    	case 4381:
	    		return R.string.peru_dialog_title_exercise; // Exercise
	    		
	    	case 4382:
	    		return R.string.peru_dialog_title_informative; //Informative
	    		
	    	case 4396:
	    	case 4397:
	    	case 4398:
	    	case 4399:
	    		return R.string.latam_dialog_title_reserved; //Reserved
	    		
	    	default:
	    		return -1;
    	}
    }
    
    public static int getDialogTitleResourceForMexico(CellBroadcastMessage message) {
    	int channelId = message.getServiceCategory();
    	switch (channelId) {
	    	case 4370:
	    	case 4371:
	    	case 4372:
	    	case 4373:
	    	case 4374:
	    	case 4375:
	    	case 4376:
	    	case 4377:
	    	case 4378:
	    		
	    	case 4383:
	    	case 4384:
	    	case 4385:
	    	case 4386:
	    	case 4387:
	    	case 4388:
	    	case 4389:
	    	case 4390:
	    	case 4391:
	    	case 919:// add by liang.zhang for Defect 6925301 at 2018-09-06
	    		return R.string.title_chile_cb_dialog; //Emergency alert 
	    		
	    	case 4380:
	    		return R.string.mexico_dialog_title_test; // Test
	    		
	    	case 4381:
	    		return R.string.mexico_dialog_title_exercise; // Exercise
	    		
	    	case 6400:
	    		return R.string.mexico_dialog_title_informative; //Information
	    		
	    	case 4396:
	    	case 4397:
	    	case 4398:
	    	case 4399:
	    		return R.string.latam_dialog_title_reserved; //Reserved
	    		
	    	default:
	    		return -1;
    	}
    }
    // add by liang.zhang for Defect 6012945 at 2018-03-07 end
    
      // add by deqin.tang for Defect 7143375 at2018-12-06 begin
    public static class DialogTitleReturnForUAE{
        String language;
        int titleid;
        private DialogTitleReturnForUAE() {
        }
    } 
   public static DialogTitleReturnForUAE getDialogTitleResourceForUAE(CellBroadcastMessage message) {
        int channelId = message.getServiceCategory();
        DialogTitleReturnForUAE mDialogTitleReturnForUAE = new DialogTitleReturnForUAE();
        switch (channelId) {
            case 4370:{
                mDialogTitleReturnForUAE.language = "ar";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class1;
                return mDialogTitleReturnForUAE;
            }

            case 4383:{
                mDialogTitleReturnForUAE.language = "en";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class1;
                return mDialogTitleReturnForUAE;
            }      
                
            case 4371:{
                mDialogTitleReturnForUAE.language = "ar";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class2;
                return mDialogTitleReturnForUAE;
            }
            case 4384:{
                mDialogTitleReturnForUAE.language = "en";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class2;
                return mDialogTitleReturnForUAE;
            }
                
            case 4372:
            case 4373:
            case 4374:
            case 4375:
            case 4376:
            case 4377:
            case 4378:{
                mDialogTitleReturnForUAE.language = "ar";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class3;
                return mDialogTitleReturnForUAE;
            }
            case 4385:
            case 4386:
            case 4387:
            case 4388:
            case 4389:
            case 4390:
            case 4391:{
                mDialogTitleReturnForUAE.language = "en";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class3;
                return mDialogTitleReturnForUAE;
            }
                
            case 4379:{
                mDialogTitleReturnForUAE.language = "ar";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class4;
                return mDialogTitleReturnForUAE;
            }
            case 4392:{
                mDialogTitleReturnForUAE.language = "en";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class4;
                return mDialogTitleReturnForUAE;
            }
                
            case 4380:{
                mDialogTitleReturnForUAE.language = "ar";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class5;
                return mDialogTitleReturnForUAE;
            }
            case 4393:{
                mDialogTitleReturnForUAE.language = "en";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class5;
                return mDialogTitleReturnForUAE;
            }
        
                
            case 4381:{
                mDialogTitleReturnForUAE.language = "ar";
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class6;
                return mDialogTitleReturnForUAE;
            }
            case 4394:{
                 // modfy by deqin.tang for Defect 7235792 at2018-12-26 begin
                //mDialogTitleReturnForUAE.language = "ar";
                mDialogTitleReturnForUAE.language = "en";
                 // modfy by deqin.tang for Defect 7235792 at2018-12-26 end
                mDialogTitleReturnForUAE.titleid =  R.string.uae_dialog_title_class6;
                return mDialogTitleReturnForUAE;
            }
                
            default:
                return null;
        }
    }
    // add by deqin.tang for Defect 7143375 at2018-12-06 end
    
    // modfy by deqin.tang for Defect 7143375 at2018-12-06 begin
    /*
    // add by liang.zhang for Defect 5960227 at 2018-02-01 begin
    public static int getDialogTitleResourceForUAE(CellBroadcastMessage message) {
    	int channelId = message.getServiceCategory();
    	switch (channelId) {
	    	case 4370:
	    	case 4383:
	    		return R.string.uae_dialog_title_class1;
	    		
	    	case 4371:
	    	case 4384:
	    		return R.string.uae_dialog_title_class2;
	    		
	    	case 4372:
	    	case 4373:
	    	case 4374:
	    	case 4375:
	    	case 4376:
	    	case 4377:
	    	case 4378:
	    	case 4385:
	    	case 4386:
	    	case 4387:
	    	case 4388:
	    	case 4389:
	    	case 4390:
	    	case 4391:
	    		return R.string.uae_dialog_title_class3;
	    		
	    	case 4379:
	    	case 4392:
	    		return R.string.uae_dialog_title_class4;
	    		
	    	case 4380:
	    	case 4393:
	    		return R.string.uae_dialog_title_class5;
	    		
	    	case 4381:
	    	case 4394:
	    		return R.string.uae_dialog_title_class6;
	    		
	    	default:
	    		return -1;
    	}
    }
    // add by liang.zhang for Defect 5960227 at 2018-02-01 end
*/    
  // modfy by deqin.tang for Defect 7143375 at2018-12-06 end
	
    // add by liang.zhang for Defect 6369692 at 2018-06-07 begin
    public static int getDialogTitleResourceForNZ(CellBroadcastMessage message) {
    	int channelId = message.getServiceCategory();
    	switch (channelId) {
	    	case 4370:
	    	case 4371:
	    	case 4372:
	    	case 4373:
	    	case 4374:
	    	case 4375:
	    	case 4376:
	    	case 4377:
	    	case 4378:
	    		return R.string.title_chile_cb_dialog;
	    		
	    	default:
	    		return -1;
    	}
    }
	// add by liang.zhang for Defect 6369692 at 2018-06-07 end

    // [BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
    public static int getDialogTitleResource(CellBroadcastMessage cbm) {

        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
        //for wapstest alert title begin
//        if (cbm.getServiceCategory() == CellBroadcastSettings.WPAS_ALERT_FREQUENCY_EIGHT_ZERO
//                || cbm.getServiceCategory() == CellBroadcastSettings.WPAS_ALERT_FREQUENCY_NINE_THREE) {
//            return R.string.wpas_test_alert;
//        }
        //for wapstest alert title end

        //for EMERGENCY ALERT / ALERTE D’URGENCE alert title begin
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
        if (checkIsWpasMessage(cbm)) {
            return R.string.emergency_alert_default;
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        //[BUGFIX]-Add- begin-by TCTNB.yugang.jia,01/09/2014,525219
        if(cbm.getServiceCategory() == CHANNEL1 || cbm.getServiceCategory() == CHANNEL2){
             return R.string.title_chile_cb_dialog;
        }
        //[BUGFIX]-Add-BEGIN-by bin.xue PR-1071073
        Context context = CellBroadcastReceiverApp.getApplication();
        if(context.getResources().getBoolean(
                R.bool.feature_cellbroadcastreceiver_forceVibrateForChile_on) && cbm.getServiceCategory() == CHANNEL3){
            return R.string.title_chile_cb_dialog;
        }
        //[BUGFIX]-Add-END-by bin.xue
        //[BUGFIX]-Add-BEGIN-by chaobing.huang PR-1105747
        if (context.getResources().getBoolean(
                R.bool.def_showSpanishLanguageAlerts)){
            if (cbm.getServiceCategory() == CHANNEL5){
                return R.string.cmas_spanish_presidential_level_alert;
            } else if (cbm.getServiceCategory() > CHANNEL5 && cbm.getServiceCategory() < CHANNEL6){
                return R.string.cmas_spanish_extreme_alert;
            } else if (cbm.getServiceCategory() >= CHANNEL6 && cbm.getServiceCategory() < CHANNEL7){
                return R.string.cmas_spanish_severe_alert;
            } else if (cbm.getServiceCategory() == CHANNEL7){
                return R.string.cmas_spanish_amber_alert;
            } else if (cbm.getServiceCategory() == CHANNEL8){
                return R.string.cmas_spanish_required_monthly_test;
            } else if (cbm.getServiceCategory() == CHANNEL9){
                return R.string.cmas_spanish_exercise_alert;
            } else if (cbm.getServiceCategory() == CHANNEL10){
                return R.string.cmas_spanish_operator_defined_alert;
            }
        }
        //[BUGFIX]-Add-END-by chaobing.huang PR-1105747
        //Add by chenglin.jiang for PR1030633 Begin
        //Context context = CellBroadcastReceiverApp.getApplication();
        if(context.getResources().getBoolean(R.bool.feature_cellbroadcastreceiver_forceVibrateForChile_on)
                && (SystemProperties.getBoolean("ro.ssv.enabled", false)
                && cbm.getServiceCategory() == CHANNEL4)){
            return R.string.title_chile_cb_dialog;
        }
      //Add by chenglin.jiang for PR1030633 End
        //[BUGFIX]-Add- end-by TCTNB.yugang.jia,01/09/2014,525219

        //for EMERGENCY ALERT / ALERTE D’URGENCE alert title end
        // ETWS warning types
        SmsCbEtwsInfo etwsInfo = cbm.getEtwsWarningInfo();
        //if (etwsInfo != null) {
        if (etwsInfo != null && !isChile()) {
            switch (etwsInfo.getWarningType()) {
                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_EARTHQUAKE:
                    return R.string.etws_earthquake_warning;

                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_TSUNAMI:
                    return R.string.etws_tsunami_warning;

                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_EARTHQUAKE_AND_TSUNAMI:
                    return R.string.etws_earthquake_and_tsunami_warning;

                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_TEST_MESSAGE:
                    return R.string.etws_test_message;

                case SmsCbEtwsInfo.ETWS_WARNING_TYPE_OTHER_EMERGENCY:
                default:
                    return R.string.etws_other_emergency_type;
            }
        }else if(etwsInfo != null && isChile()){
            return R.string.pws_other_message_identifiers;
        }

        // CMAS warning types
        //if (cmasInfo != null) {
        SmsCbCmasInfo cmasInfo = cbm.getCmasWarningInfo(); // MODIFIED by yuxuan.zhang, 2016-04-21,BUG-1112693
        if (cmasInfo != null && !isChile()) {
            switch (cmasInfo.getMessageClass()) {
                case SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT:
                    return R.string.cmas_presidential_level_alert;

                case SmsCbCmasInfo.CMAS_CLASS_EXTREME_THREAT:
                    //PR 1054793 Added by fang.song begin
                    if (CBSUtills.isShow4371AsNormal(cbm)){
                        return R.string.cb_other_message_identifiers;
                    }
                    //PR 1054793 Added by fang.song end
                    return R.string.cmas_extreme_alert;

                case SmsCbCmasInfo.CMAS_CLASS_SEVERE_THREAT:
                    return R.string.cmas_severe_alert;

                case SmsCbCmasInfo.CMAS_CLASS_CHILD_ABDUCTION_EMERGENCY:
                    return R.string.cmas_amber_alert;

                case SmsCbCmasInfo.CMAS_CLASS_REQUIRED_MONTHLY_TEST:
                    return R.string.cmas_required_monthly_test;

                case SmsCbCmasInfo.CMAS_CLASS_CMAS_EXERCISE:
                    return R.string.cmas_exercise_alert;

                case SmsCbCmasInfo.CMAS_CLASS_OPERATOR_DEFINED_USE:
                    return R.string.cmas_operator_defined_alert;

                default:
                    return R.string.pws_other_message_identifiers;
            }
        }else if (cmasInfo != null && isChile()) {
            return R.string.pws_other_message_identifiers;
        }

        if (CellBroadcastConfigService.isEmergencyAlertMessage(cbm)) {
            return R.string.pws_other_message_identifiers;
        } else {
            return R.string.cb_other_message_identifiers;
        }
    }
    // [BUGFIX]-Mod-END by TSCD.tianming.lei

    // [BUGFIX]-Add-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
    public static boolean isChile(){
        boolean ssvEnabled = "true".equals(SystemProperties.get("ro.ssv.enabled", "false"));
        if(ssvEnabled){
            Context context = CellBroadcastReceiverApp.getApplication();
            if(null != context){
                String chilePerso = context.getResources().getString(R.string.def_ssv_mccmnc_ForChile);
                String cmccmnc = SystemProperties.get("persist.sys.lang.mccmnc","");
                if(!cmccmnc.equals("") && null != chilePerso && !chilePerso.equals("")){
                    String[] mccmncs = chilePerso.split(",");
                    for(String mccmnc:mccmncs){
                        if(mccmnc.trim().equals(cmccmnc)){
                            return true;
                        }
                    }
                }
            }else{
                android.util.Log.i("ltm","context null");
            }
        }
        return false;
    }
    // [BUGFIX]-Add-END by TSCD.tianming.lei

} //MODIFIED by yuxuan.zhang, 2016-04-19,BUG-838839
