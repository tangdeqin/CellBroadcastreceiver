package com.android.settings.sim.tct;

import android.content.Context;
import android.content.res.Resources;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.RadioAccessFamily;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.content.Intent;
import android.content.ContentValues;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.SystemProperties;
import android.content.res.Configuration;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
//import android.util.ResUtils;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
//import com.tcl.plugin.common.TclInterfaceAdapter;
//import com.tcl.settings.TclDataSettingsOP00;
//import com.tcl.OP00.plugin.R;

import java.util.ArrayList;
import java.util.List;
//Begin added by zubai.li for XR6168008 on 2018.04.04
//import com.android.settings.sim.TclSimPreference;
//Begin added by zubai.li for XR6168008 on 2018.04.04
import com.android.settings.R;


public class TclDualSimManagement{
    private static final String TAG = "TclDualSimManagement";

    //Begin added by wensen.luo for XR4646589 on 2017/07/31
    private String[][] mNetModeStrings = new String[2][];
    private String[][] mNetModeArray = new String[2][];

    private String[] tempNetModeStrings;
    private String[] tempNetModeArray;
    private int[] mPreferenceNetworkMode = {-1, -1};

    private Context mContext;
    private int serviceProvider;
    private RadioPowerController mRadioController;
    private static TclDualSimManagement sInstance = null;
    private SubscriptionManager mSubscriptionManager;
    private static final String KEY_CELLULAR_DATA = "sim_cellular_data";
    private static final String KEY_CALLS = "sim_calls";
    private static final String KEY_SMS = "sim_sms";

    public static final int INVALID_PICK = -1;
    public static final int DATA_PICK = 0;
    public static final int CALLS_PICK = 1;
    public static final int SMS_PICK = 2;
    public static final int PREFERRED_PICK = 3;

    public TclDualSimManagement(Context context) {
        mContext = context;
        //serviceProvider = ResUtils.getInteger(mContext, "def_prefered_network_mode", 32);
        //Begin modified by zubai.li for XR7072293 telecomcode on 2018/10/31
        serviceProvider = TclInterfaceAdapter.getPreferedNetworkMode(mContext);
        //End modified by zubai.li for XR7072293 telecomcode on 2018/10/31
        /// M: for radio switch control
        mRadioController = RadioPowerController.getInstance(mContext);
        /// @}
       mSubscriptionManager = SubscriptionManager.from(mContext);

    }
    //End added by wensen.luo for XR4646589 on 2017/07/31

    private static synchronized void createInstance(Context context) {
        if(sInstance == null) {
            sInstance = new TclDualSimManagement(context);
        }
    }

    public static TclDualSimManagement getInstance(Context context) {
        if(sInstance == null) {
            createInstance(context.getApplicationContext());//Modified by miaoliu for XRP24058 on 2018/9/19
        }
        return sInstance;
    }

    /*
    public static String customizeSimDisplayString (String title, int slotId, SubscriptionInfo subInfoRecord){
        if (subInfoRecord == null) {
            return "";
        }
        return String.format(title, (slotId + 1)) + "(" + subInfoRecord.getDisplayName() + ")";
    }
    */


    /*
     * for new sim ergo
     * add the item "data off"/"Both SIM cards" for data pick and sms pick
     */
    public boolean addItemForSmsAndDataPick(List<SubscriptionInfo> subInfoList, ArrayList<String> list, int selectableSubInfoLength, boolean isSmsPick, boolean fromSimSelectService){
        log("addItemForPick");
        if(subInfoList == null || list == null){
            return false;
        }
        if(isSmsPick){
            if (selectableSubInfoLength > 1) {
                list.add(0, mContext.getResources().getString(R.string.sim_sms_both_sim_cards_prefs_title));
                subInfoList.add(0, null);
            }
        }else{
            Log.d(TAG, "addItemForSmsAndDataPick fromSimSelectService:" + fromSimSelectService);
            for (int i = 0; i < subInfoList.size() && !fromSimSelectService; ++i) {
                SubscriptionInfo sir = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
                if (sir != null) {
                    int subId = sir.getSubscriptionId();
                    if (!TclInterfaceAdapter.isRadioOn(subId, mContext)) {
                        Log.d(TAG, "addItemForSmsAndDataPick remove item as radio is not on, subId:" + subId);
                        list.remove(i);
                        subInfoList.remove(i);
                    }
                }
            }

            list.add(mContext.getResources().getString(R.string.sim_calls_cellular_data_disabled));
            subInfoList.add(null);
        }
        return true;
    }

    /*
     * for new sim ergo
     * change the item title from "Selection required" to "Both SIM cards" for call pick
     */
    public boolean customizeFirstItemName(ArrayList<String> list){
        log("customizeFirstItemName");
        ArrayList<String> temp = list;
        if (list != null && list.size() > 1) {
            list.remove(0);
            list.add(0, mContext.getResources().getString(R.string.sim_calls_both_sim_cards_prefs_title));
        }
        return true;
    }

    /*
     * for new sim ergo
     * change the item view as we need the change the icon for items "data off"/"Both SIM cards"
     * and change title from TextView to MarqueeText
     */
    public View customItemView(View convertView, int position,
            List<SubscriptionInfo> subInfoList, int dialogId, String defaultName) {
        if(subInfoList == null){
            return null;
        }

        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        final ViewHolder holder;

        if (convertView == null) {
            // Cache views for faster scrolling
            rowView = inflater.inflate(R.layout.tcl_select_account_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.title);
            holder.summary = (TextView) rowView.findViewById(R.id.summary);
            holder.icon = (ImageView) rowView.findViewById(R.id.icon);
            rowView.setTag(holder);
        } else {
            rowView = convertView;
            holder = (ViewHolder) rowView.getTag();
        }

        final SubscriptionInfo sir = subInfoList.get(position);
        //Modified by zubai.li for dualsim 2016.09.22 Task2963040 start
        if (sir == null) {
            holder.title.setText(defaultName);
            holder.summary.setText("");
            holder.summary.setVisibility(View.GONE);
            if (DATA_PICK == dialogId) {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(
                        R.drawable.ic_turn_off_data));
            } else {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(
                        R.drawable.ic_both_sim_cards));
            }

            holder.icon.setAlpha(/*OPACITY*/0.54f);
        } else {
            holder.title.setText(sir.getDisplayName());
            String number = sir.getNumber();

            //Begin added by yangning.hong.hz for XR6335032 on 2018/05/22
            if (mContext.getResources().getBoolean(R.bool.def_settings_hide_MSISDN)) {
                number = "";
            }
            //End added by yangning.hong.hz for XR6335032 on 2018/05/22

            holder.summary.setText(number);
            if(TextUtils.isEmpty(number)){
                holder.summary.setVisibility(View.GONE);
            } else {
                holder.summary.setVisibility(View.VISIBLE);
            }
            holder.icon.setImageBitmap(sir.createIconBitmap(mContext));
            holder.icon.setAlpha(1.0f);

            //Added by quantai.zhu for Task 4941440 on 6/21/17 begin
            Configuration cfg =mContext.getResources().getConfiguration();
            String localeLanguage = cfg.getLocales().get(0).getLanguage();
            Log.d(TAG, "createEditDialog: localeLanguage = " + localeLanguage + "summary:" + holder.summary.getText());
            if("iw".equals(localeLanguage) || "ar".equals(localeLanguage) || "fa".equals(localeLanguage)) {
                String summaryStr = holder.summary.getText().toString();
                if(!TextUtils.isEmpty(summaryStr)) {
                    summaryStr = BidiFormatter.getInstance().unicodeWrap(summaryStr,
                            TextDirectionHeuristics.LTR);
                    holder.summary.setText(summaryStr);
                }
            }
            //Added by quantai.zhu for Task 4941440 on 6/21/17 end
        }
        //Modified by zubai.li for dualsim 2016.09.22 Task2963040 end
        return rowView;
    }

    private class ViewHolder {
        TextView title;
        TextView summary;
        ImageView icon;
    }

    public boolean customItemsForPick(List<SubscriptionInfo> subInfoList, ArrayList<String> list, int selectableSubInfoLength, int id, boolean fromSimSelectService) {
        log("customItemsForPick type id:" + id + ", list:" + list);
        if(id == CALLS_PICK || id == SMS_PICK){
            customizeFirstItemName(list);
        }else{
            addItemForSmsAndDataPick(subInfoList, list, selectableSubInfoLength, id == SMS_PICK, fromSimSelectService);
            TclInterfaceAdapter.deleteSlotIfNeed(mContext, id, list, subInfoList);
        }
        return true;
    }

    public int getSimIconType(int slotIndex, String iccId){
        log("getSimIconType");
        return TclDualSim.getSimIconType(mContext, slotIndex, iccId);
    }

    //Begin added by wensen.luo for XR4646589 on 2017/07/31
    /**
     * Init the Network Mode strings and values
     *
     * @param mSlotId
     */
    public void initModeStringsValues(int mSlotId) {
        final TelephonyManager tm =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        int settingsNetworkMode = getPreferredNetworkModeForSubId(mSlotId);
        final int phoneType = TelephonyManager.getPhoneType(settingsNetworkMode);
        log("slotId: " + mSlotId + ",phoneType: " + phoneType + ",settingsNetworkMode: " + settingsNetworkMode);
        int[] sId = SubscriptionManager.getSubId(mSlotId);
        if (sId == null) {
            return;
        }
        int subId = sId[0];
        Resources res = mContext.getResources();
        SubscriptionInfo subInfo = SubscriptionManager.from(mContext).getActiveSubscriptionInfo(subId);
        if (subInfo == null) {
            return;
        }
        int phoneId = SubscriptionManager.getPhoneId(subId);
        int mainPhoneId = TclInterfaceAdapter.getMainPhoneId(mContext);
        log("subId: " + subId + ",phoneId: " + phoneId + ",mainPhoneId: " + mainPhoneId);

        if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
            log("def_prefered_network_mode = " + serviceProvider);
            switch (serviceProvider) {
                case 0:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_default);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_default);
                    break;
                case 1:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_latam);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_latam);
                    break;
                case 2:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_russian);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_russian);
                    break;
                case 3:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type3);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type3);
                    break;
                case 4:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type4);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type4);
                    break;
                case 5:
                    tempNetModeStrings = res.getStringArray(
                            R.array.enabled_networks_except_gsm_4g_choices);
                    tempNetModeArray = res.getStringArray(
                            R.array.enabled_networks_except_gsm_values);
                    break;
                case 6:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type6);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type6);
                    break;
                case 7:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type7);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type7);
                    break;
                case 8:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type8);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type8);
                    break;
                case 9:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type9);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type9);
                    break;
                case 10:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type10);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type10);
                    break;
                case 11:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type11);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type11);
                    break;
                case 12:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type12);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type12);
                    break;
                case 13:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_russian1);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_russian1);
                    break;
                case 14:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type14);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type14);
                    break;
                case 15:
                    String simNumeric = tm.getSimOperatorNumericForPhone(phoneId);
                    if (("334090".equals(simNumeric) || "33409".equals(simNumeric))
                            && subInfo.getIccId() != null
                            && subInfo.getIccId().substring(8, 9).equals("0")) {
                        tempNetModeStrings = res.getStringArray(
                                R.array.preferred_network_mode_choices_custom_2006N);
                        tempNetModeArray = res.getStringArray(
                                R.array.preferred_network_mode_values_custom_2006N);
                    } else {
                        tempNetModeStrings = res.getStringArray(
                                R.array.preferred_network_mode_choices_custom_type15_default);
                        tempNetModeArray = res.getStringArray(
                                R.array.preferred_network_mode_values_custom_type15_default);
                    }
                    break;
                //Begin added by quan.luo for XR6053308 on 2018/03/06
                case 16:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type16);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type16);
                    break;
                //End added by quan.luo for XR6053308 on 2018/03/06
                //Begin added by quan.luo for XR6100103 on 2018/03/19
                case 17:
                    tempNetModeStrings = res.getStringArray(
                            R.array.enabled_networks_choices);
                    tempNetModeArray = res.getStringArray(
                            R.array.enabled_networks_values);
                    break;
                //End added by quan.luo for XR6100103 on 2018/03/19
                //Begin added by quan.luo for XR6159692 on 2018/04/11
                case 18:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_type18);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_type18);
                    break;
                //End added by quan.luo for XR6159692 on 2018/04/11
                case 32:
                    tempNetModeStrings = res.getStringArray(
                            R.array.enabled_networks_4g_choices);
                    tempNetModeArray = res.getStringArray(
                            R.array.enabled_networks_values);
                    break;
                default:
                    log("something wrong with the serviceProvider, please check the value");
                    //bai mtk
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_choices_custom_default);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_custom_default);
                    //bai mtk
                    break;
            }
        }
        //begin zhixiong.liu.hz for P24540 20181009 temp fix for CDMA card
        else if(phoneType == PhoneConstants.PHONE_TYPE_CDMA){
            int networkMode = Settings.Global.getInt(mContext.getContentResolver(), 
                                                     Settings.Global.PREFERRED_NETWORK_MODE + subId,
                                                     Phone.PREFERRED_NT_MODE);
      
            switch (networkMode){
                case Phone.NT_MODE_CDMA:
                case Phone.NT_MODE_CDMA_NO_EVDO:
                case Phone.NT_MODE_EVDO_NO_CDMA:                                              
                     tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_cdma_nolte);
                     tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_cdma_nolte);
                    break;
                    
                default:
                    tempNetModeStrings = res.getStringArray(
                            R.array.preferred_network_mode_cdma);
                    tempNetModeArray = res.getStringArray(
                            R.array.preferred_network_mode_values_cdma);
                    break;
          }

        }
        else {
             //throw new IllegalStateException("Unexpected phone type: " + phoneType);
             //avoid fc
             log("something wrong with the serviceProvider, please check the value");
             tempNetModeStrings = res.getStringArray(
                     R.array.preferred_network_mode_choices_custom_default);
             tempNetModeArray = res.getStringArray(
                    R.array.preferred_network_mode_values_custom_default);
        }
        //end zhixiong.liu.hz for P24540 20181009 not throw ex,will cause fc 
        if (mainPhoneId != phoneId) {

            int mSettingsNetworkMode = Settings.Global.getInt(
                    mContext.getContentResolver(), Settings.Global.PREFERRED_NETWORK_MODE
                            + subId, Phone.NT_MODE_GSM_ONLY);
            //Begin modified by quan.luo for 6179838 on 2018/04/16
            int currRat = TclInterfaceAdapter.getRadioAccessFamily(phoneId,mContext);
            log("mSettingsNetworkMode: " + mSettingsNetworkMode + ",currRat: " + currRat);
            if ((currRat & RadioAccessFamily.RAF_LTE) == RadioAccessFamily.RAF_LTE) {
            } else if ((currRat & RadioAccessFamily.RAF_UMTS) == RadioAccessFamily.RAF_UMTS) {
             //End modified by quan.luo for 6179838 on 2018/04/16
                // Support 3/2G for WorldMode is uLWG
                //Begin modified by ruihua.zhang.hz for XR6099966 on 2018/03/14
                //Begin modified by zubai.li for XR7072293 telecomcode on 2018/10/31
                if (!TclInterfaceAdapter.isSeparate3G4G(mContext)) {
                //End modified by zubai.li for XR7072293 telecomcode on 2018/10/31
                    tempNetModeStrings = mContext.getResources().getStringArray(
                            R.array.enabled_networks_except_lte_choices);
                    tempNetModeArray = mContext.getResources().getStringArray(
                            R.array.enabled_networks_except_lte_values);
                }
                //End modified by ruihua.zhang.hz for XR6099966 on 2018/03/14
                if (mSettingsNetworkMode > Phone.NT_MODE_GSM_ONLY) {
                    mPreferenceNetworkMode[mSlotId] = Phone.NT_MODE_WCDMA_PREF;
                    log("mPreferenceNetworkMode[mSlotId] = " + mPreferenceNetworkMode[mSlotId] + " slotid = " + mSlotId);
                }
            //Begin added by quan.luo for XR5818534 on 2017/01/04
            } else {
                //Begin modified by ruihua.zhang.hz for XR6099966 on 2018/03/14
                //Begin modified by zubai.li for XR7072293 telecomcode on 2018/10/31
                if (!TclInterfaceAdapter.isSeparate3G4G(mContext)) {
                //End modified by zubai.li for XR7072293 telecomcode on 2018/10/31
                    tempNetModeStrings = mContext.getResources().getStringArray(R.array.networks_2G_only);
                    tempNetModeArray = mContext.getResources().getStringArray(R.array.networks_2G_only_values);
                }
                //End modified by ruihua.zhang.hz for XR6099966 on 2018/03/14
                mPreferenceNetworkMode[mSlotId] = Phone.NT_MODE_GSM_ONLY;
            }
            //End added by quan.luo for XR5818534 on 2017/01/04
        }
        mNetModeStrings[mSlotId] = tempNetModeStrings;
        mNetModeArray[mSlotId] = tempNetModeArray;
        for (String modeStr : mNetModeStrings[mSlotId]) {
            log("NetModeStrings: " + modeStr);
        }
        for (String valueStr : mNetModeArray[mSlotId]) {
            log("NetModeValueStrings: " + valueStr);
        }
    }

    /**
     * Print the debug log.
     *
     * @param msg
     */
    void log(String msg) {
        Log.d(TAG, msg);
    }

    /**
     * get preferred Network Mode from the DB
     *
     * @param mSlotId
     * @return preferred network mode value
     */
    public int getPreferredNetworkModeForSubId(int mSlotId) {
        int[] sId = SubscriptionManager.getSubId(mSlotId);
        if (sId == null) {
            return -1;
        }
        final int subId = sId[0];

        int nwMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.PREFERRED_NETWORK_MODE + subId,
                Phone.PREFERRED_NT_MODE);

        return nwMode;
    }

    /**
     * Get Network Mode strings
     *
     * @param slotId
     * @return Network Mode strings
     */
    public String[] getNetModeStrings(int slotId) {
        return mNetModeStrings[slotId];
    }

    /**
     * Get Network Mode values
     *
     * @param slotId
     * @return Network Mode values
     */
    public String[] getNetModeArray(int slotId) {
        return mNetModeArray[slotId];
    }

    /**
     * Get the preference Network Mode value
     *
     * @param slotId
     * @return the preference Network Mode value of the slotId
     */
    public int getPreferenceNetworkMode(int slotId) {
        return mPreferenceNetworkMode[slotId];
    }

    /**
     * Reset the preference Network Mode for the first time init the SimPreference
     *
     * @param slotId
     */
    public void resetPreferenceNetworkMode(int slotId) {
        mPreferenceNetworkMode[slotId] = -1;
    }
    //End added by wensen.luo for XR4646589 on 2017/07/31


    //Begin added by zubai.li for dualsim Task5245955 on 2017.09.02
    /**
     * Set SIM icon type by simInfo index
     * @param the icon type of the SIM
     * @param subId the unique SubInfoRecord index in database
     * @return the number of records updated
     */
    public int setSimIconType(int type, int subId) {
        log("[setSimIconType]+ type:" + type + " subId:" + subId);
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            log("[setSimIconType]- fail");
            return -1;
        }

        //enforceModifyPhoneState("setSimIconType");

        // Now that all security checks passes, perform the operation as ourselves.
        //final long identity = Binder.clearCallingIdentity();
        try {
            //validateSubId(subId);
            ContentValues value = new ContentValues(1);
            value.put("sim_icon_type", type);
            log("[setSimIconType]- type:" + type + " set");

            int result = mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI,
                    value, SubscriptionManager.UNIQUE_KEY_SUBSCRIPTION_ID + "=" +
                    Long.toString(subId), null);

            // MTK-START
            log("[setSimIconType]- update result :" + result);

            /*
            if (mActiveList != null && result > 0) {
                for (SubscriptionInfo record : mActiveList) {
                    if (record.getSubscriptionId() == subId) {
                        record.setSimIconType(type);
                    }
                }
            }
            */
            // MTK-END

            //SubscriptionController.getInstance().notifySubscriptionInfoChanged();

            return result;
        } finally {
            //Binder.restoreCallingIdentity(identity);
        }
    }
    //End added by zubai.li for dualsim Task5245955 on 2017.09.02
}
