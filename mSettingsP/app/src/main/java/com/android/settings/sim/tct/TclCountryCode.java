package com.android.settings.sim.tct;
/**
 * Created by miaoliu for XRP10027918 on 2018/12/17
 */

import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.util.ResUtils;
import android.text.TextUtils;
import android.telephony.SubscriptionManager;

public class TclCountryCode {

    public static final String TAG = "TclCountryCode";


    //for orange operator
    private static final String COUNTRY_CODE_ORANGE_FRANCE = "FR";
    private static final String COUNTRY_CODE_ORANGE_SPAIN = "ES";
    private static final String COUNTRY_CODE_ORANGE_POLAND = "PL";
    private static final String COUNTRY_CODE_ORANGE_ROMANIA = "RO";
    private static final String COUNTRY_CODE_ORANGE_SLOVAKIA = "SK";
    private static final String COUNTRY_CODE_ORANGE_BELEGIUM = "BE";
    private static final String COUNTRY_CODE_ORANGE_MCV = "EU";

    //for orange operator plmn
    public static final String ORANGE_FRANCE_PLMN = "20801";
    public static final String ORANGE_SPAIN_PLMN = "21403";
    public static final String ORANGE_POLAND_PLMN = "26003";
    public static final String ORANGE_ROMANIA_PLMN = "22610";
    public static final String ORANGE_SLOVAKIA_PLMN = "23101";
    public static final String ORANGE_BELGIUM_PLMN = "20610";
    public static final String ORANGE_MOLDOVA_PLMN = "25901";

    public enum CountryCodeType {
        //for orange operator
        CCT_ORANGE_FR,
        CCT_ORANGE_SP,
        CCT_ORANGE_PL,
        CCT_ORANGE_RO,
        CCT_ORANGE_SK,
        CCT_ORANGE_BE,
        CCT_ORANGE_EU,
        //add the country code type under for other operator
        CCT_DEFAULT_EU
    }

    private static CountryCodeType getCountryCode() {
        String countryCode = SystemProperties.get("ro.def.svn.countrycode");
        if (SystemProperties.getBoolean("ro.ssv.enabled", false)) {
            countryCode = ResUtils.getString("def_ssv_svn_countrycode","");
        }
        log("getCountryCode = " + countryCode);
        if(SystemProperties.getInt("RO_OPERATOR_REQ", 0x00) == 0x03) {
            switch (countryCode) {
                case COUNTRY_CODE_ORANGE_FRANCE:
                    return CountryCodeType.CCT_ORANGE_FR;
                case COUNTRY_CODE_ORANGE_SPAIN:
                    return CountryCodeType.CCT_ORANGE_SP;
                case COUNTRY_CODE_ORANGE_POLAND:
                    return CountryCodeType.CCT_ORANGE_PL;
                case COUNTRY_CODE_ORANGE_ROMANIA:
                    return CountryCodeType.CCT_ORANGE_RO;
                case COUNTRY_CODE_ORANGE_SLOVAKIA:
                    return CountryCodeType.CCT_ORANGE_SK;
                case COUNTRY_CODE_ORANGE_BELEGIUM:
                    return CountryCodeType.CCT_ORANGE_BE;
                default:
                    return CountryCodeType.CCT_ORANGE_EU;
            }
        } else {
            //for other operator
        }

        return CountryCodeType.CCT_DEFAULT_EU;
    }

    public static boolean matchCountryCode(CountryCodeType ccType) {
        if(ccType == getCountryCode()) {
            return true;
        }
        return false;
    }

    // private static boolean matchCountryCodeAndPlmn(CountryCodeType ccType, String spn, String plmn) {
    //     boolean isMatchCCPlmn = false;

    //     if(SystemProperties.getInt("RO_OPERATOR_REQ", 0x00) == 0x03) {
    //         if("Orange".equals(spn)
    //                 && (ccType == CountryCodeType.CCT_ORANGE_FR && plmn.equals(ORANGE_FRANCE_PLMN)
    //                 || ccType == CountryCodeType.CCT_ORANGE_SP && plmn.equals(ORANGE_SPAIN_PLMN)
    //                 || ccType == CountryCodeType.CCT_ORANGE_PL && plmn.equals(ORANGE_POLAND_PLMN)
    //                 || ccType == CountryCodeType.CCT_ORANGE_RO && plmn.equals(ORANGE_ROMANIA_PLMN)
    //                 || ccType == CountryCodeType.CCT_ORANGE_SK && plmn.equals(ORANGE_SLOVAKIA_PLMN))){
    //             isMatchCCPlmn = true;
    //         } else if ("Orange B".equals(spn)
    //                 && ccType == CountryCodeType.CCT_ORANGE_BE
    //                 && plmn.equals(ORANGE_BELGIUM_PLMN)){
    //             isMatchCCPlmn =true;
    //         }
    //     } else {
    //         //for other operator
    //     }

    //     return isMatchCCPlmn;
    // }

    // public static boolean matchCountryCodeAndPlmn(CountryCodeType ccType) {
    //     boolean isMatchCCPlmn = false;

    //     //Begin added by zehong.chen for XR7069840 on 2018/10/31
    //     if(SystemProperties.getInt("RO_OPERATOR_REQ", 0x00) != 0x03) {
    //         return false;
    //     }
    //     //End added by zehong.chen for XR7069840 on 2018/10/31

    //     TelephonyManager tm = TelephonyManager.getDefault();
    //     String spn = tm.getSimOperatorName();
    //     String defaultPlmn = tm.getSimOperator();
    //     if (TextUtils.isEmpty(defaultPlmn)) { //defaultPlmn is empty
    //         Log.i(TAG, "matchCountryCodeAndPlmn - it may be NOT_READY, so return Mactch firstly!");
    //         return true;
    //     }

    //     int mainSoltId = SubscriptionManager.getSlotIndex(TclUtils.getMainSubId());
    //     int simState =  TelephonyManager.getDefault().getSimState(mainSoltId);
    //     log("matchCountryCodeAndPlmn defaultPlmn= " + defaultPlmn + ", spn= " + spn + ", ccType= " + ccType);

    //     if ((TelephonyManager.SIM_STATE_READY == simState)) {
    //         isMatchCCPlmn = matchCountryCodeAndPlmn(ccType, spn, defaultPlmn);
    //     }
    //     return isMatchCCPlmn;
    // }

    // /**
    //  * Hide or grey the VoLTE and VoWIFI related settings when insert non opeartor SIM card
    //  **/
    // public static boolean getDisplay4GVolteSettings() {
    //     boolean retVal = true;

    //     int mainSoltId = SubscriptionManager.getSlotIndex(TclUtils.getMainSubId());
    //     int simState =  TelephonyManager.getDefault().getSimState(mainSoltId);
    //     if(TelephonyManager.SIM_STATE_READY == simState) {
    //         if (matchCountryCode(CountryCodeType.CCT_ORANGE_SP)) {
    //             retVal = matchCountryCodeAndPlmn(CountryCodeType.CCT_ORANGE_SP);
    //         }
    //     }
    //     log("getDisplay4GVolteSettings = " + retVal + ", mainPhoneId=" + mainSoltId + ", simState=" + simState);
    //     return retVal;
    // }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
