
package com.tct.wrapper;

import java.lang.reflect.Method;//[add]-by-chaobing.huang-01102017-defect3992285
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-05,BUG-1112693*/
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.tct.constants.TctMtkConstants;
import com.tct.constants.TctQctConstants;
import com.tct.telecom.TctMtkCellBroadcast; // MODIFIED by yuxuan.zhang, 2016-06-13,BUG-1112693
import com.tct.telecom.TctQctCellBroadcast;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;//[add]-by-chaobing.huang-01102017-defect3992285

import android.media.ToneGenerator;
import android.telephony.SmsManager;
import android.util.Log;

abstract class AbsMtkWrapper implements IWrapper {

    public static final int MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE_END = 0x112B;// SmsCbConstants.MESSAGE_ID_CMAS
    public static final int MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_IMMEDIATE_OBSERVED= 0x1120;
    public static final int MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_IMMEDIATE_LIKELY= 0x1121;
    public static final int MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_EXPECTED_OBSERVED= 0x1122;
    public static final int MESSAGE_ID_CMAS_ALERT_SPANISH_SERVERE_EXPECTED_LIKELY= 0x1127;
    public static final int MESSAGE_ID_CMAS_ALERT_SPANISH_AMBER_ALERT= 0x1128;
    public static final int MESSAGE_ID_CMAS_ALERT_SPANISH_REQUIRED_MONTHLY_TEST= 0x1129;
    public static final int MESSAGE_ID_CMAS_ALERT_SPANISH_EXERCISE= 0x112A;
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    @Override
    public int getTctToneCmas() {
        // TODO need implements in mtk ext so we can get it here.
        // currently, return unknow tone.
        return ToneGenerator.TONE_UNKNOWN;
    }

    @Override
    public String tct_digitsAsteriskAndPlusOnly(Matcher match) {
        return TctMtkCellBroadcast.tctDigitsAsteriskAndPlusOnly(match); // MODIFIED by yuxuan.zhang, 2016-06-13,BUG-1112693
    }

    @Override
    public void tctSetRangesEmpty() {
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-05,BUG-1112693*/
        Log.w("mtkWrapper", "no this method in mtk platform");
        return;
    }

//    @Override
//    public void getCellBroadcastConfig(SmsManager manager) {
//        TctMtkCellBroadcast.getCellBroadcastConfig(manager);
//    }
    
    @Override
    public void getCellBroadcastConfig(int subId) {
//    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
        TctMtkCellBroadcast.getCellBroadcastConfig(subId);
    }

    @Override
    public Pattern getTctPhoneIsrael() {
        return TctMtkConstants.TCT_PHONE_ISRAEL;
    }

    @Override
    public String getTctWapMessageEnable() {
        Log.w("mtkWrapper", "no string defined in mtk platform");
        return null;
    }

    @Override
    public String getTctEmergencyBroadcastDisplay() {
        return TctMtkConstants.IS_EMERGENCY_BROADCAST_DISPLAY;
    }

    @Override
    public int getTctCmasAlertPersidentialLevelLanguageEnd() {
        return TctMtkConstants.MESSAGE_ID_CMAS_ALERT_PRESIDENTIAL_LEVEL_LANGUAGE_END;
    }

    @Override
    public int getTctCmasAlertSpanishExtremeImmediateObserved() {
        return TctMtkConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_IMMEDIATE_OBSERVED;
    }

    @Override
    public int getTctCmasAlertSpanishExtremeImmediateLikely() {
        return TctMtkConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_IMMEDIATE_LIKELY;
    }

    @Override
    public int getTctCmasAlertSpanishExtremeExpectedObserved() {
        return TctMtkConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_EXTREME_EXPECTED_OBSERVED;
    }

    @Override
    public int getTctCmasAlertSpanishServereExpectedLikely() {
        return TctMtkConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_SERVERE_EXPECTED_LIKELY;
    }

    @Override
    public int getTctCmasAlertSpanishAmberAlert() {
        return TctMtkConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_AMBER_ALERT;
    }

    @Override
    public int getTctCmasAlertSpanishRequiredMonthlyTest() {
        return TctMtkConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_REQUIRED_MONTHLY_TEST;
    }

    @Override
    public int getTctCmasAlertSpanishExercise() {
        return TctMtkConstants.MESSAGE_ID_CMAS_ALERT_SPANISH_EXERCISE;
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
    /*@Override
    public boolean activateCellBroadcastSms(TctTctMtkSmsManager m) {
        Log.i("mtkWrapper", "mtk app activateCellBroadcastSms m ="+m); // MODIFIED by yuxuan.zhang, 2016-09-26,BUG-2854327
        return TctMtkCellBroadcast.activateCellBroadcastSms(m);
    }*/
    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
    
    @Override
    public boolean activateCellBroadcastSms(int subId) {
//    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
        Log.i("mtkWrapper", "mtk app activateCellBroadcastSms subId =" + subId); // MODIFIED by yuxuan.zhang, 2016-09-26,BUG-2854327
        return TctMtkCellBroadcast.activateCellBroadcastSms(subId);
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-27,BUG-2845457*/
    @Override
    public String[] getQueryColumns(){
        return TctMtkConstants.TCT_MTK_QUERY_COLUMNS;
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/

    //[add]-begin-by-chaobing.huang-01102017-defect3992285
    /*@Override
    public boolean enableCellBroadcast(int messageIdentifier, TctMtkSmsManager manager){
//    	SmsBroadcastConfigInfo[] infos = {new SmsBroadcastConfigInfo(messageIdentifier,messageIdentifier, -1, -1, true)};
    	try{
	    	for (Method m : manager.getClass().getMethods()) {
//	    		if(m.getName().equals("setCellBroadcastSmsConfig")){
	    		if(m.getName().equals("enableCellBroadcast")) {
//	    			return (boolean)m.invoke(manager, infos,infos);
	    			return (boolean) m.invoke(manager, messageIdentifier, 0);
	    		}
	    	}
	    	return false;
    	}catch(Exception e){
        	return false;
    	}
        //return manager.setCellBroadcastSmsConfig(infos, infos);
    }*/
    
    @Override
    public boolean enableCellBroadcast(int messageIdentifier, int subId) {
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
    	try{
	    	for (Method m : manager.getClass().getMethods()) {
//	    		if(m.getName().equals("setCellBroadcastSmsConfig")){
	    		if(m.getName().equals("enableCellBroadcast")) {
//	    			return (boolean)m.invoke(manager, infos,infos);
	    			return (boolean) m.invoke(manager, messageIdentifier, 0);
	    		}
	    	}
	    	return false;
    	}catch(Exception e){
        	return false;
    	}
    }

    /*@Override
    public boolean disableCellBroadcast(int messageIdentifier, TctMtkSmsManager manager){
//    	SmsBroadcastConfigInfo[] infos = {new SmsBroadcastConfigInfo(messageIdentifier,messageIdentifier, -1, -1, false)};
    	try{
	    	for (Method m : manager.getClass().getMethods()) {
//	    		if(m.getName().equals("setCellBroadcastSmsConfig")){
	    		if(m.getName().equals("disableCellBroadcast")){
	    			return (boolean) m.invoke(manager, messageIdentifier, 0);
	    		}
	    	}
	    	return false;
    	}catch(Exception e){
        	return false;
    	}
    	//return manager.setCellBroadcastSmsConfig(infos, infos);
    }*/
    
    @Override
    public boolean disableCellBroadcast(int messageIdentifier, int subId){
//    	SmsBroadcastConfigInfo[] infos = {new SmsBroadcastConfigInfo(messageIdentifier,messageIdentifier, -1, -1, false)};
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
    	try{
	    	for (Method m : manager.getClass().getMethods()) {
//	    		if(m.getName().equals("setCellBroadcastSmsConfig")){
	    		if(m.getName().equals("disableCellBroadcast")){
	    			return (boolean) m.invoke(manager, messageIdentifier, 0);
	    		}
	    	}
	    	return false;
    	}catch(Exception e){
        	return false;
    	}
    	//return manager.setCellBroadcastSmsConfig(infos, infos);
    }

    /*@Override
    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, TctMtkSmsManager manager){
//    	SmsBroadcastConfigInfo[] infos = {new SmsBroadcastConfigInfo(startMessageId,endMessageId, -1, -1, true)};
    	try{
	    	for (Method m : manager.getClass().getMethods()) {
//	    		if(m.getName().equals("setCellBroadcastSmsConfig")){
	    		if(m.getName().equals("enableCellBroadcastRange")) {
//	    			return (boolean)m.invoke(manager, infos,infos);
	    			return (boolean) m.invoke(manager, startMessageId, endMessageId, 0);
	    		}
	    	}
	    	return false;
    	}catch(Exception e){
        	return false;
    	}
    	//return manager.setCellBroadcastSmsConfig(infos, infos);
    }*/
    
    @Override
    public boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int subId){
//    	SmsBroadcastConfigInfo[] infos = {new SmsBroadcastConfigInfo(startMessageId,endMessageId, -1, -1, true)};
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
    	try{
	    	for (Method m : manager.getClass().getMethods()) {
//	    		if(m.getName().equals("setCellBroadcastSmsConfig")){
	    		if(m.getName().equals("enableCellBroadcastRange")) {
//	    			return (boolean)m.invoke(manager, infos,infos);
	    			return (boolean) m.invoke(manager, startMessageId, endMessageId, 0);
	    		}
	    	}
	    	return false;
    	}catch(Exception e){
        	return false;
    	}
    	//return manager.setCellBroadcastSmsConfig(infos, infos);
    }

    /*@Override
    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, TctMtkSmsManager manager){
//    	SmsBroadcastConfigInfo[] infos = {new SmsBroadcastConfigInfo(startMessageId,endMessageId, -1, -1, false)};
    	try{
	    	for (Method m : manager.getClass().getMethods()) {
//	    		if(m.getName().equals("setCellBroadcastSmsConfig")){
	    		if(m.getName().equals("disableCellBroadcastRange")) {
//	    			return (boolean)m.invoke(manager, infos,infos);
	    			return (boolean) m.invoke(manager, startMessageId, endMessageId, 0);
	    		}
	    	}
	    	return false;
    	}catch(Exception e){
        	return false;
    	}
    	//return manager.setCellBroadcastSmsConfig(infos, infos);
    }*/
    //[add]-end-by-chaobing.huang-01102017-defect3992285
    
    @Override
    public boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int subId){
//    	SmsBroadcastConfigInfo[] infos = {new SmsBroadcastConfigInfo(startMessageId,endMessageId, -1, -1, false)};
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
    	try{
	    	for (Method m : manager.getClass().getMethods()) {
//	    		if(m.getName().equals("setCellBroadcastSmsConfig")){
	    		if(m.getName().equals("disableCellBroadcastRange")) {
//	    			return (boolean)m.invoke(manager, infos,infos);
	    			return (boolean) m.invoke(manager, startMessageId, endMessageId, 0);
	    		}
	    	}
	    	return false;
    	}catch(Exception e){
        	return false;
    	}
    	//return manager.setCellBroadcastSmsConfig(infos, infos);
    }

    /*@Override
    public  boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels,
                                          SmsBroadcastConfigInfo[] languages,TctMtkSmsManager manager){
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
    	
    	//TODO setCellBroadcastSmsConfig is removed in SmsManager
    	return false;
    }*/
    
    @Override
    public  boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels,
                                          SmsBroadcastConfigInfo[] languages,int subId){
        /*
         * TctMtkSmsManager manager = TctMtkSmsManager.getSmsManagerForSubscriptionId(subId);
         * try {
            for (Method m : manager.getClass().getMethods()) {
                if (m.getName().equals("setCellBroadcastSmsConfig")) {
                    return (boolean) m.invoke(manager, channels, languages);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }*/
    	
    	//TODO setCellBroadcastSmsConfig is removed in SmsManager
    	return false;
    }

    /* MODIFIED-BEGIN by yuwan, 2017-06-14,BUG-4865013*/
    /*@Override
    public boolean removeCellBroadcastMsg(int channelId, int serialNumber, TctMtkSmsManager manager){
        try {
            for (Method m : manager.getClass().getMethods()) {
                if (m.getName().equals("removeCellBroadcastMsg")) {
                    return (boolean) m.invoke(manager,channelId,serialNumber);
                }
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }*/
    /* MODIFIED-END by yuwan,BUG-4865013*/
    
    @Override
    public boolean removeCellBroadcastMsg(int channelId, int serialNumber, int subId) {
    	SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
        try {
            for (Method m : manager.getClass().getMethods()) {
                if (m.getName().equals("removeCellBroadcastMsg")) {
                    return (boolean) m.invoke(manager,channelId,serialNumber);
                }
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
