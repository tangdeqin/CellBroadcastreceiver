
package com.tct.wrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.cellbroadcastreceiver.CellBroadcastReceiverApp;

import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;

public class TctWrapperManager {

    private static IWrapper sIWrapper = null;

    private static TctWrapperManager mWrapperManager = null;

    private static final String TAG = "TctCellBroadcastReceiver"; // MODIFIED by yuxuan.zhang, 2016-09-26,BUG-2854327
    private static final String KEY_PLATEFORM = "key_plateform";
    private static final String RIL_QCOM = "qualcomm";
    private static final String RIL_MTK = "mtk";
    private static final String RIL_IMPL_KEY_ = "gsm.version.ril-impl";

    private static IWrapper ChooseWrapper() {
        if (sIWrapper == null) {
            final SharedPreferences plateformPreference = PreferenceManager
                    .getDefaultSharedPreferences(CellBroadcastReceiverApp.getApplication());
            String plateformInfoFromSystemProp = SystemProperties.get(RIL_IMPL_KEY_, "")
                    .toLowerCase();
            final String plateformInfoFromSharedPre = plateformPreference.getString(KEY_PLATEFORM,
                    "");
            final String platform = TextUtils.isEmpty(plateformInfoFromSharedPre) ? plateformInfoFromSystemProp
                    : plateformInfoFromSharedPre;
            Log.i(TAG, "TctWrapperManager plateformInfoFromSystemProp = "
                    + plateformInfoFromSystemProp + " , plateformInfoFromSharedPre = "
                    + plateformInfoFromSharedPre + " , platform = " + platform);

            restorePlateformInfo(plateformInfoFromSystemProp, plateformInfoFromSharedPre);

            /*if (platform.contains(RIL_QCOM)) {
                // qct plateform,
                sIWrapper = QctWrapper.getInstance();
            } else*/ 
            
            if (platform.contains(RIL_MTK)) {
                // mtk plateform
                sIWrapper = MtkWrapper.getInstance();
            } else {
                // other platform, not support throw exception
                // throw new RuntimeException("Platform not supported, ril-impl is " + platform);
                sIWrapper = QctWrapper.getInstance();// default use mtk // MODIFIED by yuxuan.zhang, 2016-09-26,BUG-2854327
            }
        }
        return sIWrapper;
    }

    private static void restorePlateformInfo(String plateformInfoFromSystemProp,
            String plateformInfoFromSharedPre) {
        if (plateformInfoFromSystemProp != null && TextUtils.isEmpty(plateformInfoFromSharedPre)) {
            final SharedPreferences plateformPreference = PreferenceManager
                    .getDefaultSharedPreferences(CellBroadcastReceiverApp.getApplication());
            plateformPreference.edit().putString(KEY_PLATEFORM, plateformInfoFromSystemProp)
                    .commit();
        }
    }

    public static TctWrapperManager getInstance() {
        if (mWrapperManager == null) {
            mWrapperManager = new TctWrapperManager();
            ChooseWrapper();
        }
        return mWrapperManager;
    }

    /**
     * return cmas tone id, which need be added in project side refer to 840712
     *
     * @return
     */
    public static int getTctToneCmas() {
        return ChooseWrapper().getTctToneCmas();
    }

    public static String tct_digitsAsteriskAndPlusOnly(Matcher match) {
        return ChooseWrapper().tct_digitsAsteriskAndPlusOnly(match);
    }

    public static void tctSetRangesEmpty() {
        ChooseWrapper().tctSetRangesEmpty();
    }

/*    public static void getCellBroadcastConfig(TctMtkSmsManager manager) {
        ChooseWrapper().getCellBroadcastConfig(manager);
    }*/
    
    public static void getCellBroadcastConfig(int subId) {
        ChooseWrapper().getCellBroadcastConfig(subId);
    }

    public static Pattern getIsTctPhoneIsrael(){
        return ChooseWrapper().getTctPhoneIsrael();
    }

    public static String getTctWapMessageEnable(){
        return ChooseWrapper().getTctWapMessageEnable();
        }

    public static String getTctEmergencyBroadcastDisplay(){
        return ChooseWrapper().getTctEmergencyBroadcastDisplay();
        }

    public static int getTctCmasAlertPersidentialLevelLanguageEnd(){
        return ChooseWrapper().getTctCmasAlertPersidentialLevelLanguageEnd();
        }

    public static int getTctCmasAlertSpanishExtremeImmediateObserved(){
        return ChooseWrapper().getTctCmasAlertSpanishExtremeImmediateObserved();
        }

    public static int getTctCmasAlertSpanishExtremeImmediateLikely(){
        return ChooseWrapper().getTctCmasAlertSpanishExtremeImmediateLikely();
        }

    public static int getTctCmasAlertSpanishExtremeExpectedObserved(){
        return ChooseWrapper().getTctCmasAlertSpanishExtremeExpectedObserved();
        }

    public static int getTctCmasAlertSpanishServereExpectedLikely(){
        return ChooseWrapper().getTctCmasAlertSpanishServereExpectedLikely();
        }

    public static int getTctCmasAlertSpanishAmberAlert(){
        return ChooseWrapper().getTctCmasAlertSpanishAmberAlert();
        }

    public static int getTctCmasAlertSpanishRequiredMonthlyTest(){
        return ChooseWrapper().getTctCmasAlertSpanishRequiredMonthlyTest();
        }

    public static int getTctCmasAlertSpanishExercise(){
        return ChooseWrapper().getTctCmasAlertSpanishExercise();
        }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
    /*public static boolean activateCellBroadcastSms(TctMtkSmsManager m){
        return ChooseWrapper().activateCellBroadcastSms(m);
        }*/
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
    
    public static boolean activateCellBroadcastSms(int subId){
        return ChooseWrapper().activateCellBroadcastSms(subId);
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-27,BUG-2845457*/
    public static String[] getQueryColumns(){
        return ChooseWrapper().getQueryColumns();
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
    //[add]-begin-by-chaobing.huang-01102017-defect3992285
    /*public static boolean enableCellBroadcast(int messageIdentifier, TctMtkSmsManager manager){
    	return ChooseWrapper().enableCellBroadcast(messageIdentifier, manager);
    }*/
    public static boolean enableCellBroadcast(int messageIdentifier, int subId){
    	return ChooseWrapper().enableCellBroadcast(messageIdentifier, subId);
    }
    
    /*public static boolean disableCellBroadcast(int messageIdentifier, TctMtkSmsManager manager){
    	return ChooseWrapper().disableCellBroadcast(messageIdentifier, manager);
    }*/
    public static boolean disableCellBroadcast(int messageIdentifier, int subId){
    	return ChooseWrapper().disableCellBroadcast(messageIdentifier, subId);
    }
    
    /*public static boolean enableCellBroadcastRange(int startMessageId, int endMessageId, TctMtkSmsManager manager){
    	return ChooseWrapper().enableCellBroadcastRange(startMessageId, endMessageId, manager);
    }*/
    public static boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int subId){
    	return ChooseWrapper().enableCellBroadcastRange(startMessageId, endMessageId, subId);
    }
    
    /*public static boolean disableCellBroadcastRange(int startMessageId, int endMessageId, TctMtkSmsManager manager){
    	return ChooseWrapper().disableCellBroadcastRange(startMessageId, endMessageId, manager);
    }*/
    public static boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int subId){
    	return ChooseWrapper().disableCellBroadcastRange(startMessageId, endMessageId, subId);
    }
    
/*    public static boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels,
                                                    SmsBroadcastConfigInfo[] languages,TctMtkSmsManager manager){
        return ChooseWrapper().setCellBroadcastSmsConfig(channels, languages, manager);
    }*/
    public static boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels,
            SmsBroadcastConfigInfo[] languages,int subId){
    	return ChooseWrapper().setCellBroadcastSmsConfig(channels, languages, subId);
    }
    //[add]-end-by-chaobing.huang-01102017-defect3992285
    /* MODIFIED-BEGIN by yuwan, 2017-06-14,BUG-4865013*/
/*    public static boolean removeCellBroadcastMsg(int channelId, int serialNumber,
                                                 TctMtkSmsManager manager) {
        return ChooseWrapper().removeCellBroadcastMsg(channelId, serialNumber, manager);
    }*/
    /* MODIFIED-END by yuwan,BUG-4865013*/
    public static boolean removeCellBroadcastMsg(int channelId, int serialNumber,
            int subId) {
    	return ChooseWrapper().removeCellBroadcastMsg(channelId, serialNumber, subId);
    }
}
