package com.android.settings.sim.tct;

/**
 * Created by miaoliu for XRP10027918 on 2018/12/17
 */

import com.android.settings.sim.tct.TclCountryCode;
import com.android.settings.sim.tct.TclCountryCode.CountryCodeType;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.os.SystemProperties;
import com.android.settings.util.ResUtils;
import android.text.TextUtils;
import android.util.Log;
public class TclUtils{

    public static final String TAG = "TclUtils";
   
    public static boolean isSupportWfcActionBar() {
        return TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_SP)
                || TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_PL)
                || TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_BE)
                || TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_RO);
    }

    public static boolean isOrangePolandCC() {
        return TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_PL);
    }

    public static boolean isSupportWfcFirstTimesTips() {
        return TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_SP)
                || TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_PL)
                || TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_BE)
                || TclCountryCode.matchCountryCode(CountryCodeType.CCT_ORANGE_RO);
    }

    public static boolean isOpenMarketSim(String simNumeric) {
        if(!ResUtils.getBoolean("def_sim_service_for_openmarket", false)) {
            Log.w(TAG, "isOpenmarketSIM(), def_sim_service_for_openmarket is false, return");
            return false;
        }

        if(TextUtils.isEmpty(simNumeric)) {
            return false;
        }

       String sims = ResUtils.getString("def_sim_service_mccmnc_list", "");
        if (!TextUtils.isEmpty(sims)) {
            String[] simList = sims.split(",");
            for (int i = 0; i < simList.length; i++) {
                if(simNumeric.equals(simList[i])) {
                    return true;
                }
            }

        }
        return false;
    }


    // public boolean getDisplay4GVolteSettings() {
    //     return TclCountryCode.getDisplay4GVolteSettings();
    // }

    
    //For qualcomm platform
    //  public static int getMainSubId(){
    //     int mainPhoneId = SystemProperties.getInt("persist.radio.simswitch", 0) - 1;
    //     if (mainPhoneId < 0 || mainPhoneId >= TelephonyManager.getDefault().getPhoneCount()) {
    //         mainPhoneId = SubscriptionManager.INVALID_PHONE_INDEX;
    //     }
    //     int mainSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    //     if(mainPhoneId != SubscriptionManager.INVALID_PHONE_INDEX) {
    //         int[] subIds = SubscriptionManager.getSubId(mainPhoneId);
    //         if (subIds != null && subIds.length > 0) {
    //             mainSubId = subIds[0];
    //         }else{
    //             mainSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    //         }
    //     }
    //     return mainSubId;
    // }

}