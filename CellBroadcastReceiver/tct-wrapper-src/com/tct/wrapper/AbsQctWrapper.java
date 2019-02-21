
package com.tct.wrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Method;

import android.telephony.SmsManager;
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-09-26,BUG-2854327

import com.tct.constants.TctQctConstants;
import com.tct.telecom.TctQctCellBroadcast;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import android.media.ToneGenerator;

abstract class AbsQctWrapper implements IWrapper {

    @Override
    public int getTctToneCmas() {
//        return TctQctConstants.TCT_TONE_CMAS;
    	return ToneGenerator.TONE_UNKNOWN;
    }

    @Override
    public String tct_digitsAsteriskAndPlusOnly(Matcher match) {
        return TctQctCellBroadcast.tctDigitsAsteriskAndPlusOnly(match);
    }

    @Override
    public void tctSetRangesEmpty() {
        TctQctCellBroadcast.tctSetRangesEmpty();
    }

//    @Override
//    public void getCellBroadcastConfig(SmsManager manager) {
//        TctQctCellBroadcast.getCellBroadcastConfig(manager);
//    }
    
    @Override
    public void getCellBroadcastConfig(int subId) {
//    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
        TctQctCellBroadcast.getCellBroadcastConfig(subId);
    }

    @Override
    public Pattern getTctPhoneIsrael() {
        return TctQctConstants.TCT_PHONE_ISRAEL;
    }

    @Override
    public String getTctWapMessageEnable() {
//        return TctQctConstants.TCT_WAP_MESSAGE_ENABLE;
    	return "";
    }

	@Override
    public String getTctEmergencyBroadcastDisplay() {
        return TctQctConstants.IS_EMERGENCY_BROADCAST_DISPLAY;
    }

    @Override
    public int getTctCmasAlertPersidentialLevelLanguageEnd() {
        return TctQctConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE_END;
    }

    @Override
    public int getTctCmasAlertSpanishExtremeImmediateObserved() {
        return TctQctConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_IMMEDIATE_OBSERVED;
    }

    @Override
    public int getTctCmasAlertSpanishExtremeImmediateLikely() {
        return TctQctConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_IMMEDIATE_LIKELY;
    }

    @Override
    public int getTctCmasAlertSpanishExtremeExpectedObserved() {
        return TctQctConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_EXPECTED_OBSERVED;
    }

    @Override
    public int getTctCmasAlertSpanishServereExpectedLikely() {
        return TctQctConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_SERVERE_EXPECTED_LIKELY;
    }

    @Override
    public int getTctCmasAlertSpanishAmberAlert() {
        return TctQctConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_AMBER_ALERT;
    }

    @Override
    public int getTctCmasAlertSpanishRequiredMonthlyTest() {
        return TctQctConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_REQUIRED_MONTHLY_TEST;
    }

    @Override
    public int getTctCmasAlertSpanishExercise() {
        return TctQctConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_EXERCISE;
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
//    @Override
//    public boolean activateCellBroadcastSms(SmsManager m) {
//        Log.w("QcomWrapper", "Qct app activateCellBroadcastSms m ="+m); // MODIFIED by yuxuan.zhang, 2016-09-26,BUG-2854327
//        return true;
//    }
    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
    
    @Override
    public boolean activateCellBroadcastSms(int subId) {
        Log.w("QcomWrapper", "Qct app activateCellBroadcastSms subId =" + subId);
        return true;
    }
    
    @Override
    public String[] getQueryColumns(){
        return TctQctConstants.TCT_QCT_QUERY_COLUMNS;
    }
//    //[add]-begin-by-chaobing.huang-01102017-defect3992285
//    @Override
//    public boolean enableCellBroadcast(int messageIdentifier, SmsManager manager){
//    	return manager.enableCellBroadcast(messageIdentifier, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
//    }
//
//    @Override
//    public boolean disableCellBroadcast(int messageIdentifier, SmsManager manager){
//    	return manager.disableCellBroadcast(messageIdentifier, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
//    }
//
//    @Override
//    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, SmsManager manager){
//    	return manager.enableCellBroadcastRange(startMessageId, endMessageId, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
//    }
//
//    @Override
//    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, SmsManager manager){
//    	return manager.disableCellBroadcastRange(startMessageId, endMessageId,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
//    }
//    //[add]-end-by-chaobing.huang-01102017-defect3992285

//    @Override
//    public  boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels,
//                                              SmsBroadcastConfigInfo[] languages,SmsManager manager){
//        try {
//            for (Method m : manager.getClass().getMethods()) {
//                if (m.getName().equals("setCellBroadcastSmsConfig")) {
//                    return (boolean) m.invoke(manager, channels, languages);
//                }
//            }
//            return false;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean removeCellBroadcastMsg(int channelId, int serialNumber, SmsManager manager){
//        return false;
//    }
    
    @Override
    public boolean enableCellBroadcast(int messageIdentifier, int subId){
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
    	return manager.enableCellBroadcast(messageIdentifier, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
    }

    @Override
    public boolean disableCellBroadcast(int messageIdentifier, int subId){
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
    	return manager.disableCellBroadcast(messageIdentifier, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
    }

    @Override
    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int subId){
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
    	return manager.enableCellBroadcastRange(startMessageId, endMessageId, SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
    }

    @Override
    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int subId){
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
    	return manager.disableCellBroadcastRange(startMessageId, endMessageId,SmsManager.CELL_BROADCAST_RAN_TYPE_GSM);
    }

    @Override
    public  boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels,
                                              SmsBroadcastConfigInfo[] languages,int subId){
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
        try {
            for (Method m : manager.getClass().getMethods()) {
                if (m.getName().equals("setCellBroadcastSmsConfig")) {
                    return (boolean) m.invoke(manager, channels, languages);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeCellBroadcastMsg(int channelId, int serialNumber, int subId){
        return false;
    }
}
