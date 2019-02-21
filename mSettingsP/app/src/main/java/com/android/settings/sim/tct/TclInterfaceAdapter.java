package com.android.settings.sim.tct;

import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.util.Iterator;
import android.media.AudioManager;
import android.util.Log;
import android.telephony.SubscriptionInfo;
import java.util.List;
import java.util.ArrayList;
import com.android.settings.R;
import android.app.AlertDialog;
import android.app.Dialog;
import com.tcl.sdk.TclPluginManager;
import android.text.TextUtils;
//Begin added by miaoliu for XRP10027475 on 2018/12/18
import com.tct.telephony.ITelephonySupport;
import android.os.RemoteException;
import android.os.ServiceManager;
//End added by miaoliu for XRP10027475 on 2018/12/18
/**
 * Created by user on 3/19/18.
 * Modify by chenli.gao.hz for XR6507882 on 2018/08/06
 */

public class TclInterfaceAdapter {
    private final static String TAG = "TclInterfaceAdapter";
    private final static boolean DBG = true;

    public static boolean hasTclPlugin(Context context){
        Log.i(TAG, "hasTclPlugin = true");
        return TclPluginManager.getTclSdkAdapter(context).hasTclPlugin();
    }

    /**
     * capability switch.
     * @return true : switching
     */
    public static boolean isCapabilitySwitching(Context context){
        return false;//TclPluginManager.getTclSdkAdapter(context).isCapabilitySwitching();
    }

    private static final String PROPERTY_3G_SIM = "persist.radio.simswitch";
    public static int getMainPhoneId(Context context){
        //return TclPluginManager.getTclSdkAdapter(context).getMainPhoneId();
        int mainPhoneId = SubscriptionManager.INVALID_PHONE_INDEX;

        String curr3GSim = SystemProperties.get(PROPERTY_3G_SIM, "1");
        if (DBG) logd("current 3G Sim = " + curr3GSim);

        if (!TextUtils.isEmpty(curr3GSim)) {
            int curr3GPhoneId = Integer.parseInt(curr3GSim);
            mainPhoneId = curr3GPhoneId - 1;
        }
        if (DBG) logd("getMainPhoneId: " + mainPhoneId);

        return mainPhoneId;
    }

    public static int setLine1Number(Context context, int subId, String alphaTag, String number) {
        return TclPluginManager.getTclSdkAdapter(context).setLine1Number(subId, alphaTag, number);
    }


    public static boolean isRadioOffBySimManagement(Context context, int subId) {
        return TclPluginManager.getTclSdkAdapter(context).isRadioOffBySimManagement(subId);
    }

    public static boolean setRadioCapability(Context context, RadioAccessFamily[] rafs){
        return TclPluginManager.getTclSdkAdapter(context).setRadioCapability(rafs);
    }

    public static boolean setRadioCapability(Context context, int targetSubId) {
        return TclPluginManager.getTclSdkAdapter(context).setRadioCapability(context, targetSubId);
    }

    public static boolean is34GServiceOn(Context context) {
        return TclPluginManager.getTclSdkAdapter(context).is34GServiceOn();
    }

    public static boolean isDualLteSupport(Context context) {
        return TclPluginManager.getTclSdkAdapter(context).isDualLteSupport();
    }

    public static boolean isRadioOn(int subId, Context context) {
        //Begin modified by miaoliu for XR7107006 on 2019/1/4
        //return TclPluginManager.getTclSdkAdapter(context).isRadioOn(subId, context);
        return isUiccCardProvisioned(subId);
        //End modified by miaoliu for XR7107006 on 2019/1/4
    }

    public static boolean isAirplaneModeOn(Context context) {
        //Begin modified by miaoliu for XR7107006 on 2019/1/4
        //return TclPluginManager.getTclSdkAdapter(context).isAirplaneModeOn(context);
        return com.android.settingslib.WirelessUtils.isAirplaneModeOn(context);
        //End modified by miaoliu for XR7107006 on 2019/1/4
    }

    public static int getSubIdUsingPhoneId(Context context, int otherSimId) {
        return TclPluginManager.getTclSdkAdapter(context).getSubIdUsingPhoneId(otherSimId);
    }


    public static boolean containsCarrier(Context context, String carrierName) {
        return TclPluginManager.getTclSdkAdapter(context).containsCarrier(carrierName);
    }

    public static String getSpn(Context context, String carrierName) {
        return TclPluginManager.getTclSdkAdapter(context).getSpn(carrierName);
    }

    public static int getRadioAccessFamily(int phoneId,Context context) {
        return TclPluginManager.getTclSdkAdapter(context).getRadioAccessFamily(phoneId, context);
    }

    public static boolean isNeedDisableSimPref(Context context, List<SubscriptionInfo> subInfoList) {
        return TclPluginManager.getTclSdkAdapter(context).isNeedDisableSimPref(subInfoList);
    }

    public static void deleteSlotIfNeed(Context context, int id, ArrayList<String> list, List<SubscriptionInfo> subInfoList) {
        TclPluginManager.getTclSdkAdapter(context).deleteSlotIfNeed(id, list, subInfoList);
    }

    public static boolean isGuineaCompetitor(Context context, int slotId){
        return TclPluginManager.getTclSdkAdapter(context).isGuineaCompetitor(context, slotId);
    }

    public static boolean isGuineaSimlock(Context context, int slotId){
        return TclPluginManager.getTclSdkAdapter(context).isGuineaSimlock(context, slotId);
    }

    public static boolean isOrangeSimlock(Context context, int slotId){
        return TclPluginManager.getTclSdkAdapter(context).isOrangeSimlock(context, slotId);
    }

    public static boolean isNeedHideSimInfo(Context context, int slotId) {
        return TclPluginManager.getTclSdkAdapter(context).isNeedHideSimInfo(context, slotId);
    }

    public static Dialog createServiceDialog(Context context, Object object, int id) {
        return TclPluginManager.getTclSdkAdapter(context).createServiceDialog(context, object, id);
    }

    public static int setDefaultDataSubId(Context context, final int subId) {
        return TclPluginManager.getTclSdkAdapter(context).setDefaultDataSubId(subId);
    }

    //Begin added by zubai.li for XR7072293 telecomcode on 2018/10/31
    public static boolean isUkraineSimlock(Context context, int slotId){
        return TclPluginManager.getTclSdkAdapter(context).isUkraineSimlock(context, slotId);
    }

    public static boolean isCellcomGuineaSimlock(Context context, int slotId){
        return TclPluginManager.getTclSdkAdapter(context).isCellcomGuineaSimlock(context, slotId);
    }

    public static boolean isDisabledData(Context context){
        return TclPluginManager.getTclSdkAdapter(context).isDisabledData();
    }

    public static int getPreferedNetworkMode(Context context){
        return TclPluginManager.getTclSdkAdapter(context).getPreferedNetworkMode();
    }

    public static boolean isSeparate3G4G(Context context){
        return TclPluginManager.getTclSdkAdapter(context).isSeparate3G4G();
    }
    //End added by zubai.li for XR7072293 telecomcode on 2018/10/31
    //Begin added by miaoliu for XRP10027475 on 2018/12/18
    private static  final int PROVISIONED = 1;
    private static final int INVALID_STATE = -1;
     private static boolean isUiccCardProvisioned(int subId) {
        int provisionStatus = INVALID_STATE;
        int slotId = SubscriptionManager.getSlotIndex(subId);
       try {
            ITelephonySupport telephony = getITelephonySupport();
            provisionStatus = telephony.getCurrentUiccCardProvisioningStatus(slotId);
        } catch (RemoteException | NullPointerException ex) {
            Log.d(TAG, "getCurrentUiccCardProvisioningStatus failed", ex);
            return false;
        }
        return provisionStatus == PROVISIONED;
   }
    private static ITelephonySupport getITelephonySupport() {
        return ITelephonySupport.Stub.asInterface(ServiceManager.getService("phone_support"));
    }
    //Begin added by miaoliu for XRP10027475 on 2018/12/18

    private static void logd(String msg){
        if (DBG) Log.d(TAG, msg);
    }

    private static void loge(String msg){
        Log.e(TAG, msg);
    }

}
