
package com.tct.wrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;

interface IWrapper {

    /**
     * return cmas tone id, which need be added in project side refer to 840712
     *
     * @return
     */
    int getTctToneCmas();

    String tct_digitsAsteriskAndPlusOnly(Matcher match);

    void tctSetRangesEmpty();

//    void getCellBroadcastConfig(TctMtkSmsManager manager);
    
    void getCellBroadcastConfig(int subId);

    Pattern getTctPhoneIsrael();

    String getTctWapMessageEnable();

    String getTctEmergencyBroadcastDisplay();

    int getTctCmasAlertPersidentialLevelLanguageEnd();

    int getTctCmasAlertSpanishExtremeImmediateObserved();

    int getTctCmasAlertSpanishExtremeImmediateLikely();

    int getTctCmasAlertSpanishExtremeExpectedObserved();

    int getTctCmasAlertSpanishServereExpectedLikely();

    int getTctCmasAlertSpanishAmberAlert();

    int getTctCmasAlertSpanishRequiredMonthlyTest();

    int getTctCmasAlertSpanishExercise();

//    boolean activateCellBroadcastSms(TctMtkSmsManager m); // MODIFIED by yuxuan.zhang, 2016-09-22,BUG-2845457
    
    boolean activateCellBroadcastSms(int subId);

    String[] getQueryColumns(); // MODIFIED by yuxuan.zhang, 2016-09-27,BUG-2845457

    //[add]-begin-by-chaobing.huang-01102017-defect3992285
//    boolean enableCellBroadcast(int messageIdentifier, TctMtkSmsManager manager);
    
    boolean enableCellBroadcast(int messageIdentifier, int subId);

//    boolean disableCellBroadcast(int messageIdentifier, TctMtkSmsManager manager);
    boolean disableCellBroadcast(int messageIdentifier, int subId);

//    boolean enableCellBroadcastRange(int startMessageId, int endMessageId, TctMtkSmsManager manager);
    boolean enableCellBroadcastRange(int startMessageId, int endMessageId, int subId);

//    boolean disableCellBroadcastRange(int startMessageId, int endMessageId, TctMtkSmsManager manager);
    boolean disableCellBroadcastRange(int startMessageId, int endMessageId, int subId);

//    boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels,
//                                      SmsBroadcastConfigInfo[] languages,TctMtkSmsManager manager);
    
    boolean setCellBroadcastSmsConfig(SmsBroadcastConfigInfo[] channels,
            SmsBroadcastConfigInfo[] languages,int subId);

    //[add]-end-by-chaobing.huang-01102017-defect3992285
//    boolean removeCellBroadcastMsg(int channelId, int serialNumber, TctMtkSmsManager manager); // MODIFIED by yuwan, 2017-06-14,BUG-4865013
    boolean removeCellBroadcastMsg(int channelId, int serialNumber, int subId);
}
