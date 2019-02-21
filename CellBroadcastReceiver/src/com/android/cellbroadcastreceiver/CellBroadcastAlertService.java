/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (c) 2012-2013, The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 11/27/2013|    yugang.jia        |       PR562324       |CBC notification  */
/* ----------|----------------------|----------------------|----------------- */
/* 12/19/2013|guoju.yao             |561786                |[Russia]][CB]Addi */
/*           |                      |                      |tional requiremen */
/*           |                      |                      |ts for Cell Broad */
/*           |                      |                      |cast messages     */
/* ----------|----------------------|----------------------|----------------- */
/* 12/23/2013|    yugang.jia        |       PR575460       |more CBC more mod */
/*           |                      |                    |notification function*/
/* ----------|----------------------|----------------------|----------------- */
/* 12/24/2013|yugang.jia            |     PR-576568        |[CN][SDM][Russia] */
/*           |                      |                      |Need to add several*/
/*           |                      |                     |SDM value for russia*/
/* ----------|----------------------|----------------------|----------------- */
/* 01/03/2014|guoju.yao             |578373                |[HOMO][CB]Additio */
/*           |                      |                      |nal requirements  */
/*           |                      |                      |for Cell Broadcas */
/*           |                      |                      |t messages        */
/* ----------|----------------------|----------------------|----------------- */
/* 05/07/2014|ke.meng               |     PR-642065        |CB [Russia] REQ   */
/* ----------|----------------------|----------------------|----------------- */
/* 08/08/2014|bangju.wang           |757369                |There is no Ru lang*/
/*           |                      |                      |uage in the lang  */
/*           |                      |                      |uage list in CB   */
/*           |                      |                      |settings          */
/* ----------|----------------------|----------------------|----------------- */
/* 08/26/2014|      fujun.yang      |        755265        |SMSCB behavior in */
/*           |                      |                      |Android phones    */
/* ----------|----------------------|----------------------|----------------- */
/* 01/07/2014|     tianming.lei     |        887714        |[CMAS]Black screen*/
/*           |                      |                      |didn't turn on    */
/*           |                      |                      |with incoming eme-*/
/*           |                      |                      |rgency alert when */
/*           |                      |                      | MS has an unread */
/*           |                      |                      | emergency alert  */
/* ----------|----------------------|----------------------|----------------- */
/* 01/03/2015|     tianming.lei     |        888411        |Header for CB message*/
/*           |                      |                   |in notification panel*/
/*           |                      |                      | should be changed*/
/* ----------|----------------------|----------------------|----------------- */
/* 02/10/2015|      bangjun.wang    |        927272        |[SCB]Click CB     */
/*           |                      |                      |message in        */
/*           |                      |                      |notification have */
/*           |                      |                      |no effect after   */
/*           |                      |                      |click home key    */
/* ----------|----------------------|----------------------|----------------- */
/* 02/14/2015|      bangjun.wang    |        926352        |[Pre-cts][CB]light*/
/*           |                      |                      |up screen and cb  */
/*           |                      |                      |LED indicator     */
/*           |                      |                      |cannot be work    */
/* ----------|----------------------|----------------------|----------------- */
/* 02/24/2015|      fujun.yang      |        926300        |[Pre-CTS][CB]Header*/
/*           |                      |                      |for CB message in */
/*           |                      |                      |notification panel*/
/*           |                      |                      |should be changed */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/
package com.android.cellbroadcastreceiver;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.Handler; // MODIFIED by yuwan, 2017-06-13,BUG-4865013
import android.preference.PreferenceManager;
import android.provider.MediaStore; //modify by liang.zhang for Defect 5823627 at 2018-01-04
import android.provider.Telephony;
import android.telephony.CellBroadcastMessage;
import android.telephony.SmsCbCmasInfo;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
//[FEATURE]-Add-BEGIN by TSCD.fujun.yang,08/26/2014,755265,SMSCB behavior in Android phones
import android.telephony.TelephonyManager;
//[FEATURE]-Add-END by TSCD.fujun.yang
import com.android.internal.telephony.TelephonyProperties; // MODIFIED by yuwan, 2017-05-22,BUG-4654505
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
import java.util.UUID;
import java.lang.Integer;
/* MODIFIED-END by yuwan,BUG-4623008*/
import com.android.cb.util.TLog;

//[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,08/12/2013,501208,
//[HOMO][HOMO]Cell Broadcast messages realization for qualcomm/MTK/Broadcom/Spr-
//eadtrum/etc android smartphones
import android.media.AudioManager;
//[BUGFIX]-Add-END by TCTNB.Dandan.Fang

import android.telephony.SubscriptionManager;
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-05-06,BUG-1112693

import com.android.internal.telephony.PhoneConstants;
import com.android.cellbroadcastreceiver.CellBroadcast.Channel;//BUGFIX add by guolin.chen for PR1105891 at 2015/11/3
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-06,BUG-2812534*/
import com.android.cellbroadcastreceiver.CellBroadcastReceiverApp;
import com.android.cellbroadcastreceiver.CellBroadcastResources; //MODIFIED by yuxuan.zhang, 2016-04-19,BUG-838839
import com.android.cellbroadcastreceiver.CellBroadcastSettings;
import com.android.cellbroadcastreceiver.R;
/* MODIFIED-END by yuxuan.zhang,BUG-2812534*/
import com.tct.util.FwkPlf;
import com.tct.util.IsdmParser;
import com.tct.wrapper.TctWrapperManager; // MODIFIED by yuxuan.zhang, 2016-09-27,BUG-2845457
/* MODIFIED-BEGIN by yuwan, 2017-06-07,BUG-4582741*/
import android.telephony.SubscriptionInfo;
//import mediatek.telephony.MtkSmsManager;
/* MODIFIED-END by yuwan,BUG-4582741*/
/**
 * This service manages the display and animation of broadcast messages.
 * Emergency messages display with a flashing animated exclamation mark icon,
 * and an alert tone is played when the alert is first shown to the user
 * (but not when the user views a previously received broadcast).
 */
public class CellBroadcastAlertService extends Service {
    private static final String TAG = "CellBroadcastAlertService";

    /** Intent action to display alert dialog/notification, after verifying the alert is new. */
    static final String SHOW_NEW_ALERT_ACTION = "cellbroadcastreceiver.SHOW_NEW_ALERT";

    /** Use the same notification ID for non-emergency alerts. */
    static final int NOTIFICATION_ID = 1;
    static final int EMERGENCY_NOTIFICATION_ID = 2;

    /** Sticky broadcast for latest area info broadcast received. */
    static final String CB_AREA_INFO_RECEIVED_ACTION =
            "android.cellbroadcastreceiver.CB_AREA_INFO_RECEIVED";
    /** system property to enable/disable broadcast duplicate detecion.  */
    private static final String CB_DUP_DETECTION = "persist.cb.dup_detection";

    /** Check for system property to enable/disable duplicate detection.  */
    static boolean mUseDupDetection = SystemProperties.getBoolean(CB_DUP_DETECTION, true);

    /** Channel 50 Cell Broadcast. */
    static final int CB_CHANNEL_50 = 50;

    /** Channel 60 Cell Broadcast. */
    static final int CB_CHANNEL_60 = 60;

    // [BUGFIX]-Add-begin by TCTNB.yugang.jia,12/02/2013,564967
    public static final int MESSAGE_TYPE_COMMON_CELLBROADCAST = 0;
    public static final int MESSAGE_TYPE_ETWS_CELLBROADCAST = 1;
    public static final int MESSAGE_TYPE_CMAS_PRESIDENTITAL_CELLBROADCAST = 3;
    public static final int MESSAGE_TYPE_CMAS_OTHER_CELLBROADCAST = 2;
    // [BUGFIX]-Add-end by TCTNB.yugang.jia,12/02/2013,564967
    // [BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,08/12/2013,501208,
    // [HOMO][HOMO]Cell Broadcast messages realization for
    // qualcomm/MTK/Broadcom/Spr-eadtrum/etc android smartphones
    private static Context context = null;
    public static final long DUPLICATION_TIMEOUT = 1000 * 60 * 60 * 24; // 24 hours
    public static final long DUPLICATION_TIMEOUT_12H = 1000 * 60 * 60 * 12; // 12 hours[add]-by-chaobing.huang-01102016-defect3959549
    private static int mOriginCallState = 0;// aiyan-999810
    private static CellBroadcastMessage mMsg;// aiyan-999810

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
    public static final int ALERT_ONLY_ONCE = 1;
    public static final int ALERT_TWO_MINUTES_INTERVAL = 2;
    public static final int ALERT_FIVE_MINUTES_INTERVAL = 5;
    public static final int ALERT_FIFTENN_MINUTES = 15;//MODIFIED-BEGIN by yuwan, 2017-04-05,BUG-4447081
    public static final int ALERT_NEVER = 0;
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    /* MODIFIED-BEGIN by yuwan, 2017-05-22,BUG-4654505*/
    private static final String EMERGENCY_FALSE = "false";
    private static final String EMERGENCY_TRUE = "true";
    /* MODIFIED-END by yuwan,BUG-4654505*/
    private static final String ACTION = "com.tct.cellbroadcast.SEND50CHANNEL";
    private Handler mHandler = new Handler(); // MODIFIED by yuwan, 2017-06-13,BUG-4865013

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        context = this;
        super.onCreate();

        // begin: aiyan-999810-T-Mobile request for incoming call after WEA message
        //PR 1077028 Modified by fang.song begin
        IntentFilter filter = new IntentFilter();
        if (context.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
            filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        }// end: aiyan-999810-T-Mobile request for incoming call after WEA message

        if (context.getResources().getBoolean(R.bool.def_receiveduplicatmsg_boot_air)) {
            TLog.d(TAG, "def_receiveduplicatmsg_boot_air = "+context.getResources().getBoolean(R.bool.def_receiveduplicatmsg_boot_air));
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        }
        context.registerReceiver(mReceiver, filter);
        //PR 1077028 Modified by fang.song end
        //[add]-begin-by-chaobing.huang-01102016-defect3959549
        if (context.getResources().getBoolean(R.bool.def_ignoreDuplicate_msgBody)) {
            initHalfDayCmasList();
        }
        //[add]-end-by-chaobing.huang-01102016-defect3959549
    }
    // [BUGFIX]-Add-END by TCTNB.Dandan.Fang

    //[add]-begin-by-chaobing.huang-01102016-defect3959549
    private void initHalfDayCmasList() {
        long now = System.currentTimeMillis();
        // This is used to query necessary fields from cmas table
        // which are related duplicate check
        // for example receive date, cmas id and so on
        String[] project = new String[] {
            Telephony.CellBroadcasts.PLMN,
            Telephony.CellBroadcasts.LAC,
            Telephony.CellBroadcasts.CID,
            Telephony.CellBroadcasts.DELIVERY_TIME,
            Telephony.CellBroadcasts.SERVICE_CATEGORY,
            Telephony.CellBroadcasts.SERIAL_NUMBER,
            Telephony.CellBroadcasts.MESSAGE_BODY};
        Cursor cursor = getApplicationContext().getContentResolver().query(
                Telephony.CellBroadcasts.CONTENT_URI,project,
                Telephony.CellBroadcasts.DELIVERY_TIME + ">?",
                new String[]{now - DUPLICATION_TIMEOUT_12H + ""},
                Telephony.CellBroadcasts.DELIVERY_TIME + " DESC");
        if (sCmasIdSet != null) {
        	sCmasIdSet.clear();
        }
        MessageServiceCategoryAndScope newCmasId;
        int serviceCategory;
        int serialNumber;
        String messageBody;
        long deliveryTime;
        if(cursor != null){
            int plmnColumn = cursor.getColumnIndex(Telephony.CellBroadcasts.PLMN);
            int lacColumn = cursor.getColumnIndex(Telephony.CellBroadcasts.LAC);
            int cidColumn = cursor.getColumnIndex(Telephony.CellBroadcasts.CID);
            int serviceCategoryColumn = cursor.getColumnIndex(
                    Telephony.CellBroadcasts.SERVICE_CATEGORY);
            int serialNumberColumn = cursor.getColumnIndex(
                    Telephony.CellBroadcasts.SERIAL_NUMBER);
            Log.d(TAG,"serialNumberColumn = "+serialNumberColumn);
            int messageBodyColumn = cursor.getColumnIndex(Telephony.CellBroadcasts.MESSAGE_BODY);
            int deliveryTimeColumn = cursor.getColumnIndex(
                    Telephony.CellBroadcasts.DELIVERY_TIME);
            while(cursor.moveToNext()){
                String plmn = getStringColumn(plmnColumn, cursor);
                int lac = getIntColumn(lacColumn, cursor);
                int cid = getIntColumn(cidColumn, cursor);
                SmsCbLocation location = new SmsCbLocation(plmn, lac, cid);
                serviceCategory = getIntColumn(serviceCategoryColumn, cursor);
                serialNumber = getIntColumn(serialNumberColumn, cursor);
                messageBody = getStringColumn(messageBodyColumn, cursor);
                deliveryTime = getLongColumn(deliveryTimeColumn, cursor);
                newCmasId = new MessageServiceCategoryAndScope(
                        serviceCategory, serialNumber, location, messageBody);
                sCmasIdSet.add(newCmasId);
            }
        }
        if(cursor != null){
            cursor.close();
        }
    }
    private String getStringColumn (int column, Cursor cursor) {
        if (column != -1 && !cursor.isNull(column)) {
            return cursor.getString(column);
        } else {
            return null;
        }
    }

    private int getIntColumn (int column, Cursor cursor) {
        if (column != -1 && !cursor.isNull(column)) {
            return cursor.getInt(column);
        } else {
            return -1;
        }
    }

    private long getLongColumn (int column, Cursor cursor) {
        if (column != -1 && !cursor.isNull(column)) {
            return cursor.getLong(column);
        } else {
            return -1;
        }
    }
    //[add]-end-by-chaobing.huang-01102016-defect3959549

    /** Container for message ID and geographical scope, for duplicate message detection. */
    private static final class MessageServiceCategoryAndScope {
        private final int mServiceCategory;
        private final int mSerialNumber;
        private final SmsCbLocation mLocation;
        private final String mMessageBody;//[add]-by-chaobing.huang-01102016-defect3959549
        
        // add by liang.zhang for Defect 6353600 at 2018-06-04 begin
        private long mTime = 0;
        private boolean mIsCanada = false;
        // add by liang.zhang for Defect 6353600 at 2018-06-04 begin

        //[add]-begin-by-chaobing.huang-01102016-defect3959549
        MessageServiceCategoryAndScope(int serviceCategory, int serialNumber,
                SmsCbLocation location, String messageBody) {
            mServiceCategory = serviceCategory;
            mSerialNumber = serialNumber;
            mLocation = location;
            mMessageBody = messageBody;
        }
        //[add]-end-by-chaobing.huang-01102016-defect3959549

        MessageServiceCategoryAndScope(int serviceCategory, int serialNumber,
                SmsCbLocation location) {
            mServiceCategory = serviceCategory;
            mSerialNumber = serialNumber;
            mLocation = location;
            mMessageBody = "";//[add]-by-chaobing.huang-01102016-defect3959549
        }
        
        // add by liang.zhang for Defect 6353600 at 2018-06-04 begin
        void setTime(long time) {
        	mTime = time;
        }
        
        void isCanada(boolean isCanada) {
        	mIsCanada = isCanada;
        }
        // add by liang.zhang for Defect 6353600 at 2018-06-04 begin

        @Override
        public int hashCode() {
            /* MODIFIED-BEGIN by yuwan, 2017-06-03,BUG-4832604*/
            if (context.getResources().getBoolean(R.bool.def_isSupportFor_TC4)) {
                return mLocation.hashCode() + 7 * mSerialNumber;
            } else {
                return mLocation.hashCode() + 5 * mServiceCategory + 7 * mSerialNumber;
            }
            /* MODIFIED-END by yuwan,BUG-4832604*/
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof MessageServiceCategoryAndScope) {
                MessageServiceCategoryAndScope other = (MessageServiceCategoryAndScope) o;
                /* MODIFIED-BEGIN by yuwan, 2017-06-03,BUG-4832604*/
                if (context.getResources().getBoolean(R.bool.def_isSupportFor_TC4)) {
                    return (mSerialNumber == other.mSerialNumber &&
                            mLocation.equals(other.mLocation));
                } else {
                    // modify by liang.zhang for Defect 6353600 at 2018-06-04 begin
                    if (mIsCanada) {
                    	return (mServiceCategory == other.mServiceCategory &&
                                mSerialNumber == other.mSerialNumber &&
                                mLocation.equals(other.mLocation) &&
                                mMessageBody.equals(other.mMessageBody)
                                && mTime - 24 * 60 * 60 * 1000 < other.mTime);
                    } else {
                    	return (mServiceCategory == other.mServiceCategory &&
                                mSerialNumber == other.mSerialNumber &&
                            /* MODIFIED-BEGIN by chaobing.huang, 2017-01-13,BUG-3959549*/
                                mLocation.equals(other.mLocation) &&
                                mMessageBody.equals(other.mMessageBody));//[add]-by-chaobing.huang-01102016-defect3959549
                            /* MODIFIED-END by chaobing.huang,BUG-3959549*/
                    }
                    // modify by liang.zhang for Defect 6353600 at 2018-06-04 end
                }
                /* MODIFIED-END by yuwan,BUG-4832604*/
            }
            return false;
        }

        @Override
        public String toString() {
            return "{mServiceCategory: " + mServiceCategory + " serial number: " + mSerialNumber +
                    " location: " + mLocation.toString() + "mMessageBody:" +mMessageBody+ '}';//[add]-by-chaobing.huang-01102016-defect3959549
        }
    }

    /** Cache of received message IDs, for duplicate message detection. */
    private static final HashSet<MessageServiceCategoryAndScope> sCmasIdSet =
            new HashSet<MessageServiceCategoryAndScope>(8);

    /** Maximum number of message IDs to save before removing the oldest message ID. */
    private static final int MAX_MESSAGE_ID_SIZE = 65535;

    /** List of message IDs received, for removing oldest ID when max message IDs are received. */
    private static final ArrayList<MessageServiceCategoryAndScope> sCmasIdList =
            new ArrayList<MessageServiceCategoryAndScope>(8);

    /** Index of message ID to replace with new message ID when max message IDs are received. */
    private static int sCmasIdListIndex = 0;

    //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/05/2013,FR400297,
    //CBC notification with pop up and tone alert + vibrate in CHILE
    private static final int CHANNEL1 = 919;
    private static final int CHANNEL2 = 921;
    private static final int CHANNEL3 = 4370;//[BUGFIX] Add by bin.xue for PR1071073
    private static final int CHANNEL50 = 50;//PR 1038801 Added by fang.song
    Boolean forceVibrateChile = false;
    //[FEATURE]-Add-END by TCTNB.Dandan.Fang

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (Telephony.Sms.Intents.SMS_EMERGENCY_CB_RECEIVED_ACTION.equals(action) ||
                Telephony.Sms.Intents.SMS_CB_RECEIVED_ACTION.equals(action)) {
            handleCellBroadcastIntent(intent);
        } else if (SHOW_NEW_ALERT_ACTION.equals(action)) {
            showNewAlert(intent);
        } else {
            Log.e(TAG, "Unrecognized intent action: " + action); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
        }
        return START_NOT_STICKY;
    }

    private void handleCellBroadcastIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            TLog.e(TAG, "received SMS_CB_RECEIVED_ACTION with no extras!");
            return;
        }

        SmsCbMessage message = (SmsCbMessage) extras.get("message");

        if (message == null) {
            TLog.e(TAG, "received SMS_CB_RECEIVED_ACTION with no message extra");
            return;
        }

        final CellBroadcastMessage cbm = new CellBroadcastMessage(message);
        int defaultSubId = SubscriptionManager.getDefaultSmsSubscriptionId(); // MODIFIED by yuxuan.zhang, 2016-07-29,BUG-1112693
        int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
                SubscriptionManager.getPhoneId(defaultSubId));
        //[BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,2015/01/20,891330
        //long [] subId = SubscriptionManager.getSubId(phoneId);
        //cbm.setSubId(subId[0]);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-13,BUG-1112693*/
        boolean isEnableSingleSIM = IsdmParser.getBooleanFwk(getApplicationContext(),
                FwkPlf.def_cellbroadcast_enable_single_sim, false);
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        if(TelephonyManager.getDefault().isMultiSimEnabled() && isEnableSingleSIM){
            phoneId = PhoneConstants.SUB1;
        }

        cbm.setSubId(phoneId);
        //[BUGFIX]-Mod-END by TSCD.tianming.lei
        // modify by liang.zhang for Defect 6012945 at 2018-03-07 begin
        boolean isPeru = false;
        boolean isCanada = false; // add by liang.zhang for Defect 6353600 at 2018-06-04
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
    	if (subList != null && subList.size() > 0) {
    		for (int i = 0; i < subList.size(); i++) {
    			SubscriptionInfo info = subList.get(i);
    			if (info!= null && info.getMcc() == 716) {
    				isPeru = true;
    	        }
    			// add by liang.zhang for Defect 6353600 at 2018-06-04 begin
    			else if (info!= null && info.getMcc() == 302) {
    				isCanada = true;
    			}
    			// add by liang.zhang for Defect 6353600 at 2018-06-04 end
    		}
    	}
        
        if (!(isPeru && cbm.getServiceCategory() == 4382) && !isMessageEnabledByUser(cbm)) {
            Log.w(TAG, "ignoring alert of type " + cbm.getServiceCategory() + // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                    " by user preference");
            return;
        }
        // modify by liang.zhang for Defect 6012945 at 2018-03-07 end

        //[BUGFIX]-MOD-BEGIN by TSCD.bangjun.wang,08/08/2014,PR-757369
        CBSUtills cbu;
        cbu = new CBSUtills(this);
        int k ;
        Log.w(TAG, "isEnableSingleSIM="+isEnableSingleSIM+" phoneId="+phoneId); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
        if(TelephonyManager.getDefault().isMultiSimEnabled() && isEnableSingleSIM){
            k = cbu.queryCBLanguage(phoneId);
        }else{
           k = cbu.queryCBLanguage();
        }
        //[BUGFIX]-Add-BEGIN by chaobing.huang,2015/09/25,1092604
        boolean isReceivedOnlyIn2G = getResources().getBoolean(R.bool.feature_onlyReceiveCbMessageIn2G_on);
        android.util.Log.d(TAG,"isReceivedOnlyIn2G = "+isReceivedOnlyIn2G);
        if(isReceivedOnlyIn2G){
            TelephonyManager manager = TelephonyManager.getDefault();
            int [] subId = SubscriptionManager.getSubId(phoneId);
            if(subId != null && subId.length > 0){
                int voicenetworkType = manager.getVoiceNetworkType(subId[0]);
                int networkClass = manager.getNetworkClass(voicenetworkType);
//                String mccmnc1 = manager.getSimOperator(subId[0]);
                String mccmnc = manager.getNetworkOperator(subId[0]); // MODIFIED by yuxuan.zhang, 2016-07-29,BUG-1112693
                android.util.Log.d(TAG,"subId = "+subId[0]);
                android.util.Log.d(TAG,"voicenetworkType = "+voicenetworkType);
                android.util.Log.d(TAG,"networkClass = "+networkClass);
//                android.util.Log.d(TAG,"SimOperator = "+mccmnc1 + "   NetworkOperator = " +mccmnc2);
                if(
//                      (mccmnc1 != null && "72411".equals(mccmnc1)) &&
                    ( mccmnc != null && mccmnc.equals("72411"))
                    && networkClass != TelephonyManager.NETWORK_CLASS_2_G){
                    android.util.Log.d(TAG, "current is not in 2G, skip the recieving message");
                    return;
                }
            }
        }
      //[BUGFIX]-Add-END by chaobing.huang,2015/09/25,1092604
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
        Log.w(TAG,"cbLaCode cbm.queryCBLanguage:"+k);
        String cbLaCode = cbm.getLanguageCode();
        Log.w(TAG,"cbLaCode cbm.getLanguageCode:"+cbm.getLanguageCode());
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        //[modify]-begin-by-chaobing.huang-19.01.2017-defect4050110
        if(getResources().getBoolean(R.bool.def_isLanguageFilterCmas)) {
            if ((!(languageDecode(k).equalsIgnoreCase(cbLaCode))) && (!("all".equalsIgnoreCase(languageDecode(k))))) {
                return;
            }
        }else {
            if ((!(languageDecode(k).equalsIgnoreCase(cbLaCode))) && (!("all".equalsIgnoreCase(languageDecode(k)))) && !CellBroadcastConfigService.isEmergencyAlertMessage(cbm)) {
                return;
            }
        }
        //[modify]-end-by-chaobing.huang-19.01.2017-defect4050110
       //[BUGFIX]-MOD-END by TSCD.bangjun.wang

        /* MODIFIED-BEGIN by yuwan, 2017-05-22,BUG-4654505*/
        if (isInEmergencyCallMode()) {
            TLog.d(TAG, "handleBroadcastSms : Is in emergency call mode!");
            return;
        }
        /* MODIFIED-END by yuwan,BUG-4654505*/

        if (mUseDupDetection) {
            // Check for duplicate message IDs according to CMAS carrier requirements. Message IDs
            // are stored in volatile memory. If the maximum of 65535 messages is reached, the
            // message ID of the oldest message is deleted from the list.
        	//[add]-begin-by-chaobing.huang-01102016-defect3959549
        	MessageServiceCategoryAndScope newCmasId;
        	if (context.getResources().getBoolean(R.bool.def_ignoreDuplicate_msgBody)) {
        		newCmasId = new MessageServiceCategoryAndScope(
	                    message.getServiceCategory(), message.getSerialNumber(), message.getLocation(), message.getMessageBody());
                Log.d(TAG,"newCmasId1 = "+newCmasId);
			} else {
				newCmasId = new MessageServiceCategoryAndScope(
	                    message.getServiceCategory(), message.getSerialNumber(), message.getLocation());
                Log.d(TAG,"newCmasId2 = "+newCmasId);
			}
        	//[add]-end-by-chaobing.huang-01102016-defect3959549
        	
        	// add by liang.zhang for Defect 6353600 at 2018-06-04 begin
        	if (isCanada) {
        		newCmasId.setTime(cbm.getDeliveryTime());
        		newCmasId.isCanada(isCanada);
        	}
        	// add by liang.zhang for Defect 6353600 at 2018-06-04 end

            // Add the new message ID to the list. It's okay if this is a duplicate message ID,
            // because the list is only used for removing old message IDs from the hash set.
            if (sCmasIdList.size() < MAX_MESSAGE_ID_SIZE) {
                sCmasIdList.add(newCmasId);
            } else {
                // Get oldest message ID from the list and replace with the new message ID.
                MessageServiceCategoryAndScope oldestCmasId = sCmasIdList.get(sCmasIdListIndex);
                sCmasIdList.set(sCmasIdListIndex, newCmasId);
                TLog.d(TAG, "message ID limit reached, removing oldest message ID " + oldestCmasId);
                // Remove oldest message ID from the set.
                sCmasIdSet.remove(oldestCmasId);
                if (++sCmasIdListIndex >= MAX_MESSAGE_ID_SIZE) {
                    sCmasIdListIndex = 0;
                }
            }
            // Set.add() returns false if message ID has already been added
            if (!sCmasIdSet.add(newCmasId)) {
                // begin : aiyan-978947-if Duplication exceed 24 hours
                if (context.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
                    if (!isExceedDuplicationExpire(cbm)) {
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                        Log.w(TAG, "ignoring duplicate alert with " + newCmasId);
                        return;
                    }
                //PR 1077028 Added by fang.song begin
                } else if (context.getResources().getBoolean(R.bool.def_receiveduplicatmsg_boot_air)) {
                    Log.w(TAG, "def_receiveduplicatmsg_boot_air = "+context.getResources().getBoolean(R.bool.def_receiveduplicatmsg_boot_air)
                        +"no airplane turn on/off, and no reboot, ignoring duplicate alert with "+ newCmasId);
                    return;
                //PR 1077028 Added by fang.song end
                } else {
                    Log.w(TAG, "ignoring duplicate alert with " + newCmasId);
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    return;
                }
                // end : aiyan-978947
            }
        }

        final Intent alertIntent = new Intent(SHOW_NEW_ALERT_ACTION);
        alertIntent.setClass(this, CellBroadcastAlertService.class);
        alertIntent.putExtra("message", cbm);

        // write to database on a background thread
        new CellBroadcastContentProvider.AsyncCellBroadcastTask(getContentResolver())
                .execute(new CellBroadcastContentProvider.CellBroadcastOperation() {
                    @Override
                    public boolean execute(CellBroadcastContentProvider provider) {
                        if (provider.insertNewBroadcast(cbm)) {
                            /* MODIFIED-BEGIN by yuwan, 2017-06-13,BUG-4865013*/
                            if (context.getResources().getBoolean(
                                    R.bool.def_ignoreDuplicate_msgBody)) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
//                                        MtkSmsManager manager = null;
                                    	int subId = -1002;
                                        if (TelephonyManager.getDefault().isMultiSimEnabled()
                                                && cbm.getSubId() != -1) {
                                            SubscriptionManager mSubscriptionManager
                                                    = SubscriptionManager.from(context);
                                            SubscriptionInfo mSubInfoRecord = mSubscriptionManager
                                                    .getActiveSubscriptionInfoForSimSlotIndex(
                                                            cbm.getSubId());
                                            if (mSubInfoRecord != null) {
                                                subId = mSubInfoRecord.getSubscriptionId();
                                                Log.d(TAG, "We get CellBroadcastConfig subId = "
                                                        + subId);
                                            } else {
                                                Log.d(TAG, "The subDescription:"
                                                        + cbm.getSubId() + " is not active");
                                            }
//                                            manager = MtkSmsManager
//                                                    .getSmsManagerForSubscriptionId(subId);
                                        } else {
//                                            manager = MtkSmsManager.getDefault();
                                            subId = SubscriptionManager.getDefaultSubscriptionId();
                                        }
                                        /* MODIFIED-BEGIN by yuwan, 2017-06-14,BUG-4865013*/
                                        TctWrapperManager.removeCellBroadcastMsg(
                                                cbm.getServiceCategory(), cbm.getSerialNumber(),
                                                subId);
                                                /* MODIFIED-END by yuwan,BUG-4865013*/
                                        Log.d(TAG, "channel = " + cbm.getServiceCategory()
                                                + " sn = " + cbm.getSerialNumber());
                                    }
                                }, DUPLICATION_TIMEOUT_12H);
                            }
                            /* MODIFIED-END by yuwan,BUG-4865013*/
                            // new message, show the alert or notification on UI thread
                            startService(alertIntent);
                            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                            Log.w(TAG, "insert successfully");
                            return true;
                        } else {
                            Log.w(TAG, "failed to insert new cbm");
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                            return false;
                        }
                    }
                });
    }

    // begin : aiyan-978947-Use timestamp as a criteria for duplication detection
    private boolean isExceedDuplicationExpire(CellBroadcastMessage cbm) {
        // If the timestamp on a WEA message is more than 24 hours old, the WEA
        // message shall not be used for duplication detection purpose.
        if (cbm == null) {
            return false;
        }
        Cursor cursor = null;
        String serialNum = String.valueOf(cbm.getSerialNumber()) != null ?
                String.valueOf(cbm.getSerialNumber()) : "";
        String serviceCategory = String.valueOf(cbm.getServiceCategory()) != null ?
                String.valueOf(cbm.getServiceCategory()) : "";
        ContentValues msgLocation = cbm.getContentValues();
        String plmn = msgLocation.getAsString(Telephony.CellBroadcasts.PLMN);
        String lac = msgLocation.getAsString(Telephony.CellBroadcasts.LAC);
        String cid = msgLocation.getAsString(Telephony.CellBroadcasts.CID);
        long cbmDeliverTime = cbm.getDeliveryTime();
        TLog.d(TAG, "cbmDeliverTime: " + cbmDeliverTime);
        long latestDeliverTime = 0;

        try {
            // find the origin message time for Duplication.
            if (plmn == null || lac == null || cid == null) {
                // in test case, there may be no plmn, cid and lac
                cursor = context.getContentResolver()
                        .query(CellBroadcastContentProvider.CONTENT_URI,
                                new String[] {
                                    Telephony.CellBroadcasts.DELIVERY_TIME
                                },
                                Telephony.CellBroadcasts.SERIAL_NUMBER + "=?" + " and "
                                        + Telephony.CellBroadcasts.SERVICE_CATEGORY + "=?",
                                new String[] {
                                        serialNum, serviceCategory
                                },
                                Telephony.CellBroadcasts.DELIVERY_TIME + " DESC");
            } else {
                // in real world, plmn, lac and cid can not be null
                cursor = context.getContentResolver()
                        .query(CellBroadcastContentProvider.CONTENT_URI,
                                new String[] {
                                    Telephony.CellBroadcasts.DELIVERY_TIME
                                },
                                Telephony.CellBroadcasts.SERIAL_NUMBER + "=?"
                                        + " and " + Telephony.CellBroadcasts.SERVICE_CATEGORY + "=?"
                                        + " and " + Telephony.CellBroadcasts.PLMN + "=?"
                                        + " and " + Telephony.CellBroadcasts.LAC + "=?"
                                        + " and " + Telephony.CellBroadcasts.CID + "=?",
                                new String[] {
                                        serialNum, serviceCategory, plmn, lac, cid
                                },
                                Telephony.CellBroadcasts.DELIVERY_TIME + " DESC");
            }
            if (cursor != null) {
                TLog.d(TAG, "Duplication count for new message: " + cursor.getCount());
                //Modify by chenglin.jiang for PR1039019,1039620, 1039623 ,1039625,1039626,1039628,1039629 Begin
                if(cursor.moveToFirst())
                {
                    latestDeliverTime = cursor.getLong(cursor
                            .getColumnIndex(Telephony.CellBroadcasts.DELIVERY_TIME));
                    TLog.d(TAG, "latestDeliverTime: " + latestDeliverTime);
                    if (cbmDeliverTime - latestDeliverTime > DUPLICATION_TIMEOUT) {
                        // check if the time exceed 24 hours,if exceed, show the message
                        return true;
                    /* MODIFIED-BEGIN by chaobing.huang, 2017-01-13,BUG-3959549*/
                    }if(context.getResources().getBoolean(R.bool.def_ignoreDuplicate_msgBody) && (cbmDeliverTime - latestDeliverTime > DUPLICATION_TIMEOUT_12H)) {
                    	return true;
                    	/* MODIFIED-END by chaobing.huang,BUG-3959549*/
                    } else {
                        // else, ignore the message
                        return false;
                    }
                 }else {
                    // No duplication,show the message.
                    return true;
                }
                //Modify by chenglin.jiang for PR1039019,1039620, 1039623 ,1039625,1039626,1039628,1039629 End
            } else {
                // the message was deleted by user, show it anyway
                return true;
            }
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    TLog.e(TAG, "can not close DB." + e);
                }
            }
        }
    }
    // end : aiyan-978947-Use timestamp as a criteria for duplication detection

    private void showNewAlert(Intent intent) {
        Bundle extras = intent.getExtras();
        forceVibrateChile = false;//[BUGFIX] Add by bin.xue for PR1071073
        if (extras == null) {
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            Log.e(TAG, "received SHOW_NEW_ALERT_ACTION with no extras!");
            return;
        }

        CellBroadcastMessage cbm = (CellBroadcastMessage) extras.get("message");

        if (cbm == null) {
            Log.e(TAG, "received SHOW_NEW_ALERT_ACTION with no message extra");
            return;
        }
        mMsg = cbm;// aiyan-999810
        //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/05/2013,FR400297,
        //CBC notification with pop up and tone alert + vibrate in CHILE
        Log.i(TAG,"receive message from channel:" + Integer.toString(cbm.getServiceCategory()));
        //PR 1038801 Added by fang.song begin
        if (getBaseContext().getResources().getBoolean(
                R.bool.feature_cellbroadcastreceiver_forceVibrateForChile_on) &&
                (cbm.getServiceCategory() == CHANNEL1 || cbm.getServiceCategory() == CHANNEL2 || cbm.getServiceCategory() == CHANNEL50)) {//Modify by chenglin.jiang for PR1017965 //Modify by chenglin.jiang for PR1030633
        //PR 1038801 Added by fang.song end
            Log.i(TAG,"forcevibrate for chile when receive message from channel 919 or 921");
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            forceVibrateChile = true;
        }
        //[FEATURE]-Add-END by TCTNB.Dandan.Fang
        
        // modify by liang.zhang for Defect 6012945 at 2018-03-07 begin
        boolean isPeru = false;
        boolean isMexico = false;
        boolean isChile = false;
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
    	if (subList != null && subList.size() > 0) {
    		for (int i = 0; i < subList.size(); i++) {
    			SubscriptionInfo info = subList.get(i);
    			if (info!= null && info.getMcc() == 716) {
    				isPeru = true;
    	        } else if (info!= null && info.getMcc() == 730) {
    	        	isChile = true;
    	        } else if (info!= null && info.getMcc() == 334) {
    	        	isMexico = true;
    	        }
    		}
    	}
        
        // LATAM
    	// modify by liang.zhang for Defect 6925301 at 2018-09-06 begin
        if (isPeru || (isMexico && cbm.getServiceCategory() != CHANNEL50) || isChile) {
        	forceVibrateChile = true;
        }
    	// modify by liang.zhang for Defect 6925301 at 2018-09-06 end
        // modify by liang.zhang for Defect 6012945 at 2018-03-07 end
        
        Log.i("liang.zhang", cbm.getServiceCategory() + "");
    	// modify by liang.zhang for Defect 6925301 at 2018-09-06 begin
        if (CellBroadcastConfigService.isEmergencyAlertMessage(cbm) || (cbm.getServiceCategory() == 919 && isMexico)) {
        // modify by liang.zhang for Defect 6925301 at 2018-09-06 end
            // start alert sound / vibration / TTS and display full-screen alert
            openEmergencyAlertNotification(cbm); //MODIFIED by yuxuan.zhang, 2016-04-20,BUG-1112693
        } else {
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            Log.i(TAG,"do not showNewAlert");
            // add notification to the bar
           //[FEATURE]-Add-BEGIN by TSCD.fujun.yang,08/26/2014,755265,SMSCB behavior in Android phones
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-06,BUG-2812534*/
//            if("true".equalsIgnoreCase(SystemProperties.get("ro.cb.channel50.brazil","false"))){
//                Log.i(TAG, "#--- ro.cb.channel50.brazil =true ");
//                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
//                if(cbm.getServiceCategory() == 50){
//                     TLog.i(TAG, "#--- Receive 50 channel message. msg : " + cbm.getMessageBody());
//                     TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//                     int simState = telManager.getSimState();
//                     TLog.i(TAG, "#- Sim state is : " + simState);
//                     if(simState != TelephonyManager.SIM_STATE_READY){
//                         TLog.i(TAG, "#--- Sim state is : " + simState + " not show in the SPN/PLMN");
//                         return;
//                      }
//                     long slotId = 0;
//                     slotId = cbm.getSubId();
//                     Intent channel50Intent = new Intent("com.jrdcom.action.CHANNEL_50_MSG");
//                     channel50Intent.putExtra("channel_50_msg",cbm.getMessageBody());
//                     channel50Intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, slotId);
//                     sendBroadcast(channel50Intent);
//                     return;
//                  }
//            }
/* MODIFIED-END by yuxuan.zhang,BUG-2812534*/
          //[FEATURE]-Add-END by TSCD.fujun.yang
            //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
            //CBC notification with pop up and tone alert + vibrate in CHILE
            if (forceVibrateChile && (cbm.getServiceCategory() == CHANNEL1 || cbm.getServiceCategory() == CHANNEL2 || cbm.getServiceCategory() == CHANNEL50)) {//PR 1038801 Added by fang.song
                TLog.e(TAG, "openChileAlertNotification");
                openChileAlertNotification(cbm , forceVibrateChile );
            }
            //[BUGFIX]-Add-BEGIN by TCTNB.ke.meng,05/07/2014,642065
            else if(getBaseContext().getResources().getBoolean(
                    R.bool.feature_cellbroadcastreceiver_CBReceiverMode_on)){
                Log.d(TAG,"CBReceiverMode_on");
                addToNotificationBarDisplay(cbm);
            }
            //[BUGFIX]-Add-END by TCTNB.ke.meng
          //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/24/2015,926300,[Pre-CTS][CB]Header for CB message in notification panel should be changed
            else if(getBaseContext().getResources().getBoolean(
                    R.bool.feature_cellbroadcastreceiver_displayChannelId)){//for Russia REQ, the SDM:feature_cellbroadcastreceiver_CBReceiverMode_on for Russia beelin REQ
                Log.d(TAG,"displayChannelId");
                addToNotificationBarDisplay(cbm);
            }
          //[BUGFIX]-Add-END by TSCD.fujun.yang
            else {
            //[FEATURE]-Add-END by TCTNB.Dandan.Fang
            // add notification to the bar
                Log.d(TAG,"normal");
            addToNotificationBar(cbm);
            } //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
        }
    }

    /**
     * Filter out broadcasts on the test channels that the user has not enabled,
     * and types of notifications that the user is not interested in receiving.
     * This allows us to enable an entire range of message identifiers in the
     * radio and not have to explicitly disable the message identifiers for
     * test broadcasts. In the unlikely event that the default shared preference
     * values were not initialized in CellBroadcastReceiverApp, the second parameter
     * to the getBoolean() calls match the default values in res/xml/preferences.xml.
     *
     * @param message the message to check
     * @return true if the user has enabled this message type; false otherwise
     */
    private boolean isMessageEnabledByUser(CellBroadcastMessage message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long subscription = message.getSubId();
        int serviceCategory = message.getServiceCategory();
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
        int frequency = message.getServiceCategory();
        SharedPreferences sp = this.getSharedPreferences(
                CellBroadcastReceiver.PREFS_NAME, MODE_PRIVATE); // MODIFIED by yuxuan.zhang, 2016-08-22,BUG-1112693
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
        boolean allowWpas = getResources().getBoolean(R.bool.def_enable_wpas_function);
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(this)) {
        	allowWpas = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        if (allowWpas) {
            switch (frequency) {
                case CellBroadcastSettings.WPAS_OPERATOR_NINE_FOUR:
                case CellBroadcastSettings.WPAS_OPERATOR_NINE_FIVE:
                case CellBroadcastSettings.WPAS_OPERATOR_EIGHT_ONE:
                case CellBroadcastSettings.WPAS_OPERATOR_EIGHT_TWO:
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-07,BUG-1112693 */
                    return sp.getBoolean(CellBroadcastSettings.KEY_ENABLE_WPAS_TEST_ALERTS, false);
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693 */
                default:
                    break;
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        if (message.isEtwsTestMessage()) {
            return prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_ETWS_TEST_ALERTS
                    + subscription, true); // MODIFIED by yuxuan.zhang, 2016-06-06,BUG-2245013
        }

        if (message.isCmasMessage()) {
            boolean isEnableRMTExceriseAlertType = getResources().getBoolean(R.bool.def_enableRMTExerciseTestAlert);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
            boolean allowpas = getResources().getBoolean(R.bool.def_enable_wpas_function);
            // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
            if (!CBSUtills.isCanadaSimCard(this)) {
            	allowWpas = false; 
            }
            // add by liang.zhang for Defect 6929849 at 2018-09-01 end
            switch (message.getCmasMessageClass()) {
                case SmsCbCmasInfo.CMAS_CLASS_EXTREME_THREAT:
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-06,BUG-1112693*/
                    Log.w(TAG, "CMAS_CLASS_EXTREME_THREAT");
                    return prefs.getBoolean(
                            CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS
                            + subscription, true);

                case SmsCbCmasInfo.CMAS_CLASS_SEVERE_THREAT:
                    Log.w(TAG, "CMAS_CLASS_SEVERE_THREAT");
                    return prefs.getBoolean(
                            CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS
                            + subscription, true);

                case SmsCbCmasInfo.CMAS_CLASS_CHILD_ABDUCTION_EMERGENCY:
                    Log.w(TAG, "CMAS_CLASS_CHILD_ABDUCTION_EMERGENCY");
                    return PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_AMBER_ALERTS +subscription, true);
                //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
                case SmsCbCmasInfo.CMAS_CLASS_REQUIRED_MONTHLY_TEST:
                    Log.w(TAG, "CMAS_CLASS_REQUIRED_MONTHLY_TEST");
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    if(allowpas){
                        return true;
                    }
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    if(!isEnableRMTExceriseAlertType){
                        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_TEST_ALERTS +subscription, false);
                    }
                    return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                            CellBroadcastSettings.KEY_ENABLE_CMAS_RMT_ALERTS + subscription, false);
                case SmsCbCmasInfo.CMAS_CLASS_CMAS_EXERCISE:
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
                    Log.w(TAG, "CMAS_CLASS_CMAS_EXERCISE");
                    if(allowpas){
                        return true;
                    }
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    if(!isEnableRMTExceriseAlertType){
                        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_TEST_ALERTS +subscription, false);
                    }
                    return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                            CellBroadcastSettings.KEY_ENABLE_CMAS_EXERCISE_ALERTS +subscription, false);
                case SmsCbCmasInfo.CMAS_CLASS_OPERATOR_DEFINED_USE:
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
                    Log.w(TAG, "CMAS_CLASS_OPERATOR_DEFINED_USE");
                    if(allowpas){
                        return true;
                    }
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    if(!isEnableRMTExceriseAlertType){
                        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                                CellBroadcastSettings.KEY_ENABLE_CMAS_TEST_ALERTS +subscription, false);
                    }
                    return PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(CellBroadcastSettings.KEY_ENABLE_CMAS_OPERATOR_DEFINED_ALERTS + subscription, false);
                //[BUGFIX]-MOD-END by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
                default:
                    return true;    // presidential-level CMAS alerts are always enabled
            }
        }
      //[BUGFIX]-MOD-BEGIN by TCTNB.ke.meng,05/07/2014,642065
        if (serviceCategory == CB_CHANNEL_50 || serviceCategory == CB_CHANNEL_60) {
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-06,BUG-2812534*/
            boolean channel60Preference = false;
            if (serviceCategory == CB_CHANNEL_50) {
                // save latest area info on channel 50 for Settings display
                CellBroadcastReceiverApp.setLatestAreaInfo(message);
            } else { //it is Channel 60 CB
                boolean enable60Channel =  SubscriptionManager.getResourcesForSubId(
                        getApplicationContext(), message.getSubId()).getBoolean(
                        R.bool.show_india_settings);
                if (enable60Channel) {
                    channel60Preference = PreferenceManager.getDefaultSharedPreferences(this).
                            getBoolean(CellBroadcastSettings.KEY_ENABLE_CHANNEL_60_ALERTS,
                            enable60Channel);
                }
            }
            // send broadcasts for channel 50 and 60
            Intent intent = new Intent(CB_AREA_INFO_RECEIVED_ACTION);
            intent.putExtra("message", message);
            // Send broadcast twice, once for apps that have PRIVILEGED permission and once
            // for those that have the runtime one
            sendBroadcastAsUser(intent, UserHandle.ALL,
                    android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE);
            sendBroadcastAsUser(intent, UserHandle.ALL,
                    android.Manifest.permission.READ_PHONE_STATE);

            String country = TelephonyManager.getDefault().getSimCountryIso(message.getSubId());
            // In Brazil(50)/India(50/60) the area info broadcasts are displayed in Settings,
            // CBwidget or Mms.
            // But in other country it should be displayed as a normal CB alert.
            boolean isIgnore50CbDialog = getResources().
                    getBoolean(R.bool.def_brazil_50cb_ignore_dialog_on);

            /* MODIFIED-BEGIN by yuwan, 2017-06-07,BUG-4582741*/
            SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
            SubscriptionInfo mSubInfoRecord = mSubscriptionManager
                    .getActiveSubscriptionInfoForSimSlotIndex(message.getSubId());
            int subId = -1002;
            if (mSubInfoRecord != null) {
                subId = mSubInfoRecord.getSubscriptionId();
                Log.d(TAG, "We get CellBroadcastConfig subId = " + subId);
            } else {
                Log.d(TAG, "The subDescription:" + message.getSubId() + " is not active");
            }

             /* MODIFIED-BEGIN by yuwan, 2017-05-23,BUG-4582741*/
            if (isIgnore50CbDialog) {
                Intent channelIntent = new Intent(ACTION);
                channelIntent.putExtra("messagebody", message.getMessageBody());
                 /* MODIFIED-BEGIN by yuwan, 2017-06-06,BUG-4582741*/
                channelIntent.putExtra("SubId", subId);
                Log.d(TAG, "messagebody = " + message.getMessageBody()
                        + " subid = " + subId);
                         /* MODIFIED-END by yuwan,BUG-4582741*/
                         /* MODIFIED-END by yuwan,BUG-4582741*/
                sendBroadcast(channelIntent);
            }
            /* MODIFIED-END by yuwan,BUG-4582741*/

            Log.d("SCBBrazil", "isIgnore50CbDialog : " + isIgnore50CbDialog);
            boolean needIgnore = "in".equals(country)
                    || ("br".equals(country) && (message.getServiceCategory() == CB_CHANNEL_50)) ||  (isIgnore50CbDialog && (message.getServiceCategory() == CB_CHANNEL_50));
            return ((!needIgnore) || channel60Preference);
            /* MODIFIED-END by yuxuan.zhang,BUG-2812534*/
        }
      //[BUGFIX]-MOD-end by TCTNB.ke.meng
        return true;    // other broadcast messages are always enabled
    }

    /**
     * Display a full-screen alert message for emergency alerts.
     * @param message the alert to display
     */
    private void openEmergencyAlertNotification(CellBroadcastMessage message) {
        // Acquire a CPU wake lock until the alert dialog and audio start playing.
        //[BUGFIX]-ADD-BEGIN by TSCD,tianming.lei 01/07/2015,PR-887714
        CellBroadcastAlertWakeLock.releaseCpuLock();
        //[BUGFIX]-END-BEGIN by TSCD,tianming.lei
        CellBroadcastAlertWakeLock.acquireScreenCpuWakeLock(this);

        // Close dialogs and window shade
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(closeDialogs);

        // start audio/vibration/speech service for emergency alerts
        Intent audioIntent = new Intent(this, CellBroadcastAlertAudio.class);
        audioIntent.setAction(CellBroadcastAlertAudio.ACTION_START_ALERT_AUDIO);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
        boolean allowWpas = getResources().getBoolean(R.bool.def_enable_wpas_function);
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(this)) {
        	allowWpas = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        Log.i(TAG, "openEmergencyAlertNotification allowWpas=" + allowWpas); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        int duration;   // alert audio duration in ms
        if (message.isEmergencyAlertMessage()) {//[BUGFIX]-mod- by TCTNB.yugang.jia,12/04/2013,564967 ,
            // CMAS requirement: duration of the audio attention signal is 10.5 seconds.
            duration = 10500;
        } else {
            duration = Integer.parseInt(prefs.getString(
                    CellBroadcastSettings.KEY_ALERT_SOUND_DURATION + message.getSubId(),
                    CellBroadcastSettings.ALERT_SOUND_DEFAULT_DURATION))*1000;
        }

        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
        int channelTitleId;

        /* begin: aiyan-978029-add audio on/off for T-Mobile requirement */
        if (message.isEtwsMessage()) {
            // For ETWS, always vibrate, even in silent mode.
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_VIBRATE_EXTRA, true);
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_EXTRA, true);
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_ETWS_VIBRATE_EXTRA, true);
        } else {
            // For other alerts, vibration can be disabled in app settings.
            //PR 1076360 Added by fang.song begin
            if (context.getResources().getBoolean(R.bool.def_vibrate_priority_system_cbs)){
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                TLog.i(TAG, "def_vibrate_priority_system_cbs =true, should set system high priority for vibrate, "
                      + "and audioManager.getRingerMode() = "+audioManager.getRingerMode());
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT){
                    audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_VIBRATE_EXTRA, false);
                } else {
                    audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_VIBRATE_EXTRA,
                        prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_VIBRATE
                            + message.getSubId(), true));
                }

            } else {
                TLog.i(TAG, "def_vibrate_priority_system_cbs = false, vibrate follow CBS!");
                audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_VIBRATE_EXTRA,
                    prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_VIBRATE
                            + message.getSubId(), true));
            }
            //PR 1076360 Added by fang.song end

            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_EXTRA,
                    prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_AUDIO
                            + message.getSubId(), true));
        }
        /* end: aiyan-978029-add audio on/off for T-Mobile requirement */
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
        if (allowWpas && CellBroadcastResources.checkIsWpasMessage(message)) {
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_DURATION_EXTRA,
                    CellBroadcastAlertAudio.EMERGENCY_SOUND_DURATION);
            audioIntent.putExtra(CellBroadcastAlertAudio.WPAS_ALERT_AUDIO_OPT_EXTRA, true);
            channelTitleId = R.string.emergency_alert_default;
        } else {
            allowWpas = false;
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            channelTitleId = CellBroadcastResources.getDialogTitleResource(message);
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_DURATION_EXTRA,
                    duration);
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        String messageBody = message.getMessageBody();
        /*MODIFIED-BEGIN by yuxuan.zhang, 2016-04-19,BUG-838839*/
        CharSequence channelName = getText(channelTitleId);
        /*MODIFIED-END by yuxuan.zhang,BUG-838839*/
        if (allowWpas ? prefs.getBoolean(
                CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH_EX + message.getSubId(),
                true) : prefs.getBoolean(
                CellBroadcastSettings.KEY_ENABLE_ALERT_SPEECH + message.getSubId(),
                true)) {
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_MESSAGE_BODY, messageBody);

            String language = message.getLanguageCode();
            if (message.isEtwsMessage() && !"ja".equals(language)) {
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                Log.w(TAG, "bad language code for ETWS - using Japanese TTS");
                language = "ja";
            } else if (message.isCmasMessage() && !"en".equals(language)) {
                Log.w(TAG, "bad language code for CMAS - using English TTS");
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                language = "en";
            }
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_MESSAGE_LANGUAGE,
                    language);
        }
        //[BUGFIX]-Add-BEGIN-by bin.xue for PR-1071073
        if (getBaseContext().getResources().getBoolean(
                R.bool.feature_cellbroadcastreceiver_forceVibrateForChile_on)
                /* MODIFIED-BEGIN by yuwan, 2017-06-05,BUG-4882170*/
                && message.getServiceCategory() == CHANNEL3 ) {
            TLog.i(TAG,"set chile presientialasert is true");
            audioIntent.putExtra("PresientialAlert",true);
        }
        //[BUGFIX]-Add-END-by bin.xue

        if (getBaseContext().getResources().getBoolean(R.bool.def_cb_cmas_alert_presient_ringtone)
                && message.getServiceCategory() == CHANNEL3) {
            TLog.i(TAG, "set presientialasert is true");
            audioIntent.putExtra("Presient", true);
        }
        /* MODIFIED-END by yuwan,BUG-4882170*/
        
        // modify by liang.zhang for Defect 6929849 at 2018-09-01 begin
        boolean isAllowWpas = getResources().getBoolean(R.bool.def_enable_wpas_function);
        if (!CBSUtills.isCanadaSimCard(this)) {
        	isAllowWpas = false; 
        }

        /* MODIFIED-BEGIN by bin.huang, 2016-11-10,BUG-1112693*/
        String interveal = prefs.getString(
                CellBroadcastSettings.KEY_ALERT_REMINDER_INTERVAL + message.getSubId(),
                isAllowWpas ?
                        CellBroadcastSettings.WPAS_ALERT_REMINDER_INTERVAL_DEFAULT_DURATION
                        : getBaseContext().getResources().getString(R.string.def_alert_reminder_value));
        // modify by liang.zhang for Defect 6929849 at 2018-09-01 end
        int inter = Integer.parseInt(interveal);
        Log.i(TAG, "KEY_ALERT_REMINDER_INTERVAL inter=" + inter);
        audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_REMINDER,inter);
        /* MODIFIED-END by bin.huang,BUG-1112693*/
        /* MODIFIED-BEGIN by yuwan,BUG-4447081*/
        //if (CellBroadcastAlertService.ALERT_NEVER != inter) {
        // add by liang.zhang for Defect 5960218 at 2018-02-06 begin
        audioIntent.putExtra("channelId", message.getServiceCategory());
        // add by liang.zhang for Defect 5960218 at 2018-02-06 end
        startService(audioIntent);
        //}
        /* MODIFIED-END by yuwan,BUG-4447081*/

        CellBroadcastAlertFullScreen.mMsgFirstCome = true;// aiyan-979267
        // Decide which activity to start based on the state of the keyguard.
        Class c = CellBroadcastAlertDialog.class;
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()
                || CellBroadcastAlertFullScreen.isFullScreenExist
                && context.getResources().getBoolean(
                        R.bool.cellbroadcastreceiver_tmo_request_enable)) {
            // Use the full screen activity for security.
            c = CellBroadcastAlertFullScreen.class;
        }
        //[BUGFIX]-Add-BEGIN by TCTNB.yugang.jia,12/02/2013,564967 ,
        int unreadTotalMessages = 0;
        int highestLevel = MESSAGE_TYPE_COMMON_CELLBROADCAST;

        Cursor cursor = null;
        NotificationManager notificationManager =
            (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(EMERGENCY_NOTIFICATION_ID);
        // step 1: check if there are unread message.
        try {
            cursor = context.getContentResolver().query(
                    Telephony.CellBroadcasts.CONTENT_URI,
                    new String[] {
                            Telephony.CellBroadcasts.MESSAGE_READ,
                    }, Telephony.CellBroadcasts.MESSAGE_READ + "=?"+" and "+Telephony.CellBroadcasts.MESSAGE_PRIORITY + "=?", new String[]{"0","3"}, null);
            if (cursor != null) {
                unreadTotalMessages = cursor.getCount();
            }
            cursor = context.getContentResolver().query(
                    CellBroadcastContentProvider.CONTENT_URI,
                    Telephony.CellBroadcasts.QUERY_COLUMNS, // MODIFIED by yuxuan.zhang, 2016-09-27,BUG-2845457
                    Telephony.CellBroadcasts.MESSAGE_READ + "=?", new String[] {"0"},
                    Telephony.CellBroadcasts.DELIVERY_TIME + " DESC");

            if(cursor != null) {
                // step 2: get highest emergency level
                while (cursor.moveToNext()) {

                    int level = MESSAGE_TYPE_COMMON_CELLBROADCAST;
                    if (cursor.isNull(cursor
                            .getColumnIndex(Telephony.CellBroadcasts.CMAS_MESSAGE_CLASS))) {
                        if (cursor.isNull(cursor
                                .getColumnIndex(Telephony.CellBroadcasts.ETWS_WARNING_TYPE))) {
                            level = MESSAGE_TYPE_COMMON_CELLBROADCAST;
                        } else {
                            level = MESSAGE_TYPE_ETWS_CELLBROADCAST;
                        }
                    } else {
                        int messageClass;
                        messageClass = cursor.getInt(cursor
                                .getColumnIndex(Telephony.CellBroadcasts.CMAS_MESSAGE_CLASS));
                        if (messageClass == SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT) {
                            highestLevel = MESSAGE_TYPE_CMAS_PRESIDENTITAL_CELLBROADCAST;
                            break;
                        } else {
                            level = MESSAGE_TYPE_CMAS_OTHER_CELLBROADCAST;
                        }
                    }

                    highestLevel = level > highestLevel ? level : highestLevel;
                }
            }
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e(TAG, "openEmergencyAlertNotification Exception",e); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                }
            }
        }
        //[BUGFIX]-Add-END by TCTNB.yugang.jia,12/02/2013,564967 ,

        ArrayList<CellBroadcastMessage> messageList = new ArrayList<CellBroadcastMessage>(1);
        messageList.add(message);
        Intent alertDialogIntent = createDisplayMessageIntent(this, c, messageList);
        alertDialogIntent.putExtra(CellBroadcastAlertFullScreen.FROM_WPAS_NOTIFICATION_EXTRA, allowWpas); // MODIFIED by yuxuan.zhang, 2016-05-06,BUG-1112693
        if (getBaseContext().getResources().getBoolean(
                R.bool.feature_cellbroadcastreceiver_forceVibrateForChile_on)
                && message.getServiceCategory() == CHANNEL3) {
            alertDialogIntent.putExtra("forceVibrate",true);
        }
        alertDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alertDialogIntent);
        //[BUGFIX]-Add-BEGIN by TCTNB.yugang.jia,11/27/2013,562324,

        // Use lower 32 bits of emergency alert delivery time for notification ID
        int notificationId = (int) message.getDeliveryTime();
        //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia,12/23/2013,575460,

        // begin: aiyan-999810-T-Mobile request for incoming call after WEA message
        // Create intent to show the new messages when user selects the notification.
        Intent intent = null;
        if (context.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
            intent = new Intent(context, c);
            intent.putParcelableArrayListExtra(CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA,
                    CellBroadcastAlertFullScreen.mMessageList);
            CellBroadcastAlertFullScreen.isFromNotification = true;
        } else {
            intent = createDisplayMessageIntent(this, CellBroadcastListActivity.class, messageList);
        }
        intent.putExtra(CellBroadcastAlertFullScreen.FROM_NOTIFICATION_EXTRA, true);
        // end: aiyan-999810-T-Mobile request for incoming call after WEA message

        PendingIntent pi = PendingIntent.getActivity(this, EMERGENCY_NOTIFICATION_ID, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                           .setSmallIcon(R.drawable.ic_emergency_alert)
                           .setTicker(channelName)
                           .setWhen(System.currentTimeMillis())
                           .setContentIntent(pi)
                           .setDefaults(Notification.DEFAULT_ALL)
                           .setDefaults(Notification.DEFAULT_LIGHTS);
        //int unreadCount = messageList.size();
        //if (unreadCount > 1) {
        if(unreadTotalMessages>1){
            // use generic count of unread broadcasts if more than one unread
            builder.setContentTitle(getString(R.string.notification_multiple_title_cmas));//[BUGFIX]-MOD- by TCTNB.yugang.jia,01/04/2014,582768,
            builder.setContentText(getString(R.string.notification_multiple, unreadTotalMessages));
        } else {
            builder.setContentTitle(channelName).setContentText(messageBody);
        }
        //[BUGFIX]-MOD-END by TCTNB.yugang.jia,12/23/2013,575460,
        //[BUGFIX]-Add-begin by TCTNB.yugang.jia,12/02/2013,564967 ,
        int imageId = R.drawable.ic_emergency_alert;
        if (highestLevel == MESSAGE_TYPE_CMAS_PRESIDENTITAL_CELLBROADCAST) {
            imageId = R.drawable.ic_cmas_presidential_flash;
        }

        Notification noti = builder.build();
        noti.icon = imageId;
        //[BUGFIX]-Add-end by TCTNB.yugang.jia,12/02/2013,564967 ,
        notificationManager.notify(EMERGENCY_NOTIFICATION_ID, noti);
        //[BUGFIX]-Add-END by TCTNB.yugang.jia,11/272013,562324,

    }
        //[BUGFIX]-Add-BEGIN by TCTNB.ke.meng,05/07/2014,642065
    //BUGFIX BEGIN by guolin.chen for PR1105891 at 2015/11/3
    // modify by liang.zhang for Defect 4971123 at 2017-07-07 begin
    public static String getchannelNamebychanid(CharSequence channelNameo, CellBroadcastMessage message){
    	Uri uri = null;
    	if (TelephonyManager.getDefault().isMultiSimEnabled()) {
    		int subId = message.getSubId();
    		if (subId == 0) {
    			uri = Channel.CONTENT_URISIM1;
    		} else {
    			uri = Channel.CONTENT_URISIM2;
    		}
    	} else {
    		uri = Channel.CONTENT_URI;
    	}
    	
        Cursor c = context.getContentResolver().query(uri,
                 new String[] {Channel.NAME},Channel.INDEX + " = ?",new String[]{Integer.toString(message.getServiceCategory())},null);
        String channelName = channelNameo+"";
         if (c.getCount() > 0) {
             c.moveToFirst();
             if(c.getString(0) != null && !c.getString(0).equals("")){
             channelName = c.getString(0);
             }
         }
         c.close();
         return channelName;
     }
    // modify by liang.zhang for Defect 4971123 at 2017-07-07 end
    //BUGFIX END by guolin.chen for PR1105891 at 2015/11/3
       /**
         * Add the new alert to the notification bar (non-emergency alerts), or launch a
         * high-priority immediate intent for emergency alerts.
         * It is just for Russia Beeln
         * @param message the alert to display
         */
        private void addToNotificationBarDisplay(CellBroadcastMessage message) {
            int channelTitleId = CellBroadcastResources.getDialogTitleResource(message);
            CharSequence channelName = getText(channelTitleId);
            //BUGFIX BEGIN by guolin.chen for PR1105891 at 2015/11/3
            if(getBaseContext().getResources().getBoolean(R.bool.cellbroadcastreceiver_displayChannelName)){
                // modify by liang.zhang for Defect 4971123 at 2017-07-07 begin
                channelName = getchannelNamebychanid(channelName , message);
                // modify by liang.zhang for Defect 4971123 at 2017-07-07 end
            }
            //BUGFIX END by guolin.chen for PR1105891 at 2015/11/3
            String messageBody = message.getMessageBody();
            String poweronScreenEnable = android.provider.Settings.System.getString(CellBroadcastAlertService.context.getContentResolver(),"CBLightEnable");
            if (poweronScreenEnable != null && poweronScreenEnable.equals("on")) {
                    int val=android.provider.Settings.System.getInt(getContentResolver(),
                                     android.provider.Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                    PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                    | PowerManager.ON_AFTER_RELEASE, "CBMessageReceiverService");
                    wl.acquire(val);
            }

            String cbringtone = null;

            // modify by liang.zhang for Defect 6511785 at 2018-07-06 begin
            if (getResources().getBoolean(R.bool.def_show_cmas_settings_directly)) {
            	Cursor c = this.getContentResolver().query(Uri.parse("content://" + "com.jrd.provider.CellBroadcast" + "/CBRingtone"), null,
                        null, null, null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    cbringtone = c.getString(c
                            .getColumnIndex("cbringtone"));
                }
                c.close();
            } else {
            	cbringtone = "content://settings/system/notification_sound";
            }
            // modify by liang.zhang for Defect 6511785 at 2018-07-06 end
            
            /* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
            int notificationid;
            int repeatnotification;
            ArrayList<CellBroadcastMessage> messageList = null;
            messageList = CellBroadcastReceiverApp.addNewMessageToListRussia(message,
                    getBaseContext().getResources().getBoolean(
                            R.bool.def_isSupportNotification_forRussia));
            notificationid = CellBroadcastReceiverApp.getNotificationId(
                    message.getServiceCategory());
            repeatnotification = CellBroadcastReceiverApp.getRepeatNotification();
            TLog.d(TAG, "JYG,CellbroadcastAlertService,addToNotificationBarDisplay,notificationid:"
                    + notificationid + "\nrepeatnotification:" + repeatnotification);
            if (repeatnotification != 0 && !getBaseContext().getResources().getBoolean(
                    R.bool.def_isSupportNotification_forRussia)) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(repeatnotification);
                //[BUGFIX]-Add-BEGIN by lijun.zhang,10/14/2015,PR-1084572
                CellBroadcastReceiverApp.resetRepeatNotification();
                //[BUGFIX]-Add-End by lijun.zhang
            }
            Intent intent = createDisplayMessageIntent(this, CellBroadcastAlertDialog.class,
                    messageList);
            // modif by liang.zhang for Defect 5968945 at 2018-04-09 begin
            intent.putExtra(CellBroadcastAlertFullScreen.FROM_NOTIFICATION_EXTRA, false);
            // modif by liang.zhang for Defect 5968945 at 2018-04-09 end
            PendingIntent pi = null;
            if (getBaseContext().getResources().getBoolean(
                    R.bool.def_isSupportNotification_forRussia)) {
                pi = PendingIntent.getActivity(this, UUID.randomUUID().hashCode(),
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pi = PendingIntent.getActivity(this, notificationid, intent,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
            }
            /* MODIFIED-END by yuwan,BUG-4623008*/
              // use default sound/vibration/lights for non-emergency broadcasts
              //[modify]-begin-by-chaobing.huang-defect4011887-2.7.2017
              Notification.Builder builder = new Notification.Builder(this)
              .setSmallIcon(R.drawable.ic_notify_alert)
              .setTicker(channelName)
              .setWhen(System.currentTimeMillis());
              if(getResources().getBoolean(R.bool.def_isSupportClickCBNoti_forRussia)){
            	  builder.setContentIntent(pi);
              }
              //[modify]-end-by-chaobing.huang-defect4011887-2.7.2017
              //[BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,01/03/2015,888411
              //builder.setContentTitle(channelName).setContentText(messageBody);
              builder.setContentTitle(channelName+"("+message.getServiceCategory()+")").setContentText(messageBody);
              //[BUGFIX]-Mod-END by TSCD.tianming.lei

              //eadtrum/etc android smartphones
              Notification noti = builder.build();

              // modify by liang.zhang for Defect 5823627 at 2018-01-04 begin
              if (cbringtone != null) {
            	  if (cbringtone.contains("content://media/external/audio/")) {
            		  if (!isRingtoneExist(cbringtone)) {
            			  cbringtone = "content://settings/system/notification_sound";
            		  }
            	  }
                noti.sound = Uri.parse(cbringtone);
              }
              // modify by liang.zhang for Defect 5823627 at 2018-01-04 end

              String vibrateWhen = android.provider.Settings.System.getString(CellBroadcastAlertService.context.getContentResolver(), "vibrateWhenCB");
              boolean vibrateAlways = false;
              boolean vibrateSilent = false;
              if (vibrateWhen != null) {
                  vibrateAlways = vibrateWhen.equals("Always");
                  vibrateSilent = vibrateWhen.equals("Silent");
              }
              AudioManager audioManager =
                  (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
              boolean nowSilent =
                audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
              if (vibrateAlways || vibrateSilent && nowSilent) {
                  noti.defaults |= Notification.DEFAULT_VIBRATE;
              }

              //Additional requirements for Cell Broadcast messages
              String LedEnable = android.provider.Settings.System.getString(CellBroadcastAlertService.context.getContentResolver(),"CBLedEnable");
              if( LedEnable != null && LedEnable.equals("on")) {
                  noti.flags |= Notification.FLAG_SHOW_LIGHTS;
                  /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-26,BUG-2854327*/
                  if (getResources().getBoolean(R.bool.def_led_feature_for_normal_cb)) {
                      noti.ledARGB = 0xffff0000;
                      noti.ledOnMS = 125;
                      noti.ledOffMS = 2875;
                  } else {
                      noti.ledARGB = 0xff00ff00;
                      noti.ledOnMS = 500;
                      noti.ledOffMS = 2000;
                  }
                  /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
              }
            /* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //[modify]-begin-by-chaobing.huang-defect4011887-2.7.2017
            if (getBaseContext().getResources().getBoolean(
                    R.bool.def_isSupportNotification_forRussia)) {
                notificationManager.notify(message.getServiceCategory(), noti);
            } else {
                notificationManager.cancel(NOTIFICATION_ID);
                notificationManager.notify(NOTIFICATION_ID, noti);
            }
            /* MODIFIED-END by yuwan,BUG-4623008*/
              //[modify]-end-by-chaobing.huang-defect4011887-2.7.2017
        }
        //[BUGFIX]-Add-END by TCTNB.ke.meng
        
        // add by liang.zhang for Defect 5823627 at 2018-01-04 begin
        private static boolean isRingtoneExist(String uri) {
            if (uri != null && !uri.startsWith("content://media")) {
                return true;
            }

            boolean ringtoneExist = false;
            Cursor cursor = null;
            cursor = context.getContentResolver().query(Uri.parse(uri), new String[] {
                    MediaStore.Audio.Media.DATA
            }, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        ringtoneExist = true;
                    }
                } finally {
                    cursor.close();
                }
            }
            TLog.i(TAG, uri + " is exist " + ringtoneExist);
            return ringtoneExist;
        }
        // add by liang.zhang for Defect 5823627 at 2018-01-04 end
        
    /**
     * Add the new alert to the notification bar (non-emergency alerts), or launch a
     * high-priority immediate intent for emergency alerts.
     * @param message the alert to display
     */
    private void addToNotificationBar(CellBroadcastMessage message) {
        int channelTitleId = CellBroadcastResources.getDialogTitleResource(message);
        CharSequence channelName = getText(channelTitleId);
        String messageBody = message.getMessageBody();

        //[BUGFIX]-Add-BEGIN by TCTNB.jia yugang,12/24/2013,576568,
        String poweronScreenEnable = android.provider.Settings.System.getString(CellBroadcastAlertService.context.getContentResolver(),"CBLightEnable");
        if (poweronScreenEnable != null && poweronScreenEnable.equals("on")) {
                int val=android.provider.Settings.System.getInt(getContentResolver(),
                                 android.provider.Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                | PowerManager.ON_AFTER_RELEASE, "CBMessageReceiverService");
                wl.acquire(val);
        }
        //[BUGFIX]-Add-END by TCTNB.jia yugang,12/24/2013,576568

       //[FEATURE]-Mod-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
       //Tone needed for CellBroadcast message received
        //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,08/12/2013,501208,
        //[HOMO][HOMO]Cell Broadcast messages realization for qualcomm/MTK/Broadcom/Spr-
        //eadtrum/etc android smartphones
        //String cbringtone = "content://media/internal/audio/media/7";
        String cbringtone = null;
        //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
        // modify by liang.zhang for Defect 6511785 at 2018-07-06 begin
        if (getResources().getBoolean(R.bool.def_show_cmas_settings_directly)) {
        	Cursor c = this.getContentResolver().query(Uri.parse("content://" + "com.jrd.provider.CellBroadcast" + "/CBRingtone"), null,
                    null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                cbringtone = c.getString(c
                        .getColumnIndex("cbringtone"));
            }
            c.close();
        } else {
        	cbringtone = "content://settings/system/notification_sound";
        }
        // modify by liang.zhang for Defect 6511785 at 2018-07-06 end
        TLog.d(TAG, "cbringtone = "+cbringtone);
        //[FEATURE]-Mod-END by TCTNB.bo.xu

        // Pass the list of unread non-emergency CellBroadcastMessages
        ArrayList<CellBroadcastMessage> messageList = CellBroadcastReceiverApp
                .addNewMessageToList(message);

        //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia,12/23/2013,575460,
        // Create intent to show the new messages when user selects the notification.
        //Intent intent = createDisplayMessageIntent(this, CellBroadcastAlertDialog.class,
        //        messageList);
        Intent intent = new Intent(this, CellBroadcastListActivity.class);
        //[BUGFIX]-MOD-END by TCTNB.yugang.jia,08/12/2013,575460,
        intent.putExtra(CellBroadcastAlertFullScreen.FROM_NOTIFICATION_EXTRA, true);

        PendingIntent pi = PendingIntent.getActivity(this, NOTIFICATION_ID, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);

        // use default sound/vibration/lights for non-emergency broadcasts
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_notify_alert)
                .setTicker(channelName)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pi);
        //[BUGFIX]-Mod-BEGIN by TCTNJ.(guoju.yao),01/03/2014, PR-578373,
        //requirements for Cell Broadcast messages
                //.setDefaults(Notification.DEFAULT_ALL);

        //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,08/12/2013,501208,
        //builder.setDefaults(Notification.DEFAULT_ALL);
        //builder.setDefaults(Notification.DEFAULT_LIGHTS);
        //[BUGFIX]-Add-END by TCTNB.Dandan.Fang,08/12/2013,501208,
        // increment unread alert count (decremented when user dismisses alert dialog)
        //[BUGFIX]-Mod-END  by TCTNJ.(guoju.yao)
        //[BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,03/23/2015, PR952676
        //int unreadCount = messageList.size();
        Cursor cursor = null;
        int unreadCount = 0;
        try{
            cursor = context.getContentResolver().query(
                Telephony.CellBroadcasts.CONTENT_URI,
                new String[] {Telephony.CellBroadcasts.MESSAGE_READ,},
                Telephony.CellBroadcasts.MESSAGE_READ + "=?"+" and "+Telephony.CellBroadcasts.MESSAGE_PRIORITY + "=?", new String[]{"0","0"}, null);
            if (cursor != null) {
                unreadCount = cursor.getCount();
            }
        }catch(Exception e){
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }
        //[BUGFIX]-Mod-END by TSCD.tianming.lei
        if (unreadCount > 1) {
            //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia,12/23/2013,575460,
            if(getBaseContext().getResources().getBoolean(
                R.bool.feature_cellbroadcastreceiver_mutilCBNotification_on)){
                builder.setContentTitle(channelName).setContentText(messageBody);
            } else{
                // use generic count of unread broadcasts if more than one unread
                builder.setContentTitle(getString(R.string.notification_multiple_title_cb));//[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia,01/04/2014,582768,
                builder.setContentText(getString(R.string.notification_multiple, unreadCount));
            }
            //[BUGFIX]-MOD-END by TCTNB.yugang.jia,08/12/2013,575460,
        } else {
            builder.setContentTitle(channelName).setContentText(messageBody);
        }

        //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,08/12/2013,501208,
        //[HOMO][HOMO]Cell Broadcast messages realization for qualcomm/MTK/Broadcom/Spr-
        //eadtrum/etc android smartphones
        Notification noti = builder.build();

        // modify by liang.zhang for Defect 5823627 at 2018-01-04 begin
        if (cbringtone != null) {
      	  if (cbringtone.contains("content://media/external/audio/")) {
      		  if (!isRingtoneExist(cbringtone)) {
      			  cbringtone = "content://settings/system/notification_sound";
      		  }
      	  }
          noti.sound = Uri.parse(cbringtone);
        }
        // modify by liang.zhang for Defect 5823627 at 2018-01-04 end

        String vibrateWhen = android.provider.Settings.System.getString(CellBroadcastAlertService.context.getContentResolver(), "vibrateWhenCB");
        boolean vibrateAlways = false;
        boolean vibrateSilent = false;
        if (vibrateWhen != null) {
            vibrateAlways = vibrateWhen.equals("Always");
            vibrateSilent = vibrateWhen.equals("Silent");
        }
        AudioManager audioManager =
            (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        boolean nowSilent =
            audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
        if (vibrateAlways || vibrateSilent && nowSilent) {
            noti.defaults |= Notification.DEFAULT_VIBRATE;
            TLog.i(TAG,"noti.defaults is " + noti.defaults);
        }
        //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
        //[BUGFIX]-Add-BEGIN by TCTNJ.(guoju.yao),12/19/2013, PR-561786,
        //Additional requirements for Cell Broadcast messages
        String LedEnable = android.provider.Settings.System.getString(CellBroadcastAlertService.context.getContentResolver(),"CBLedEnable");
        if( LedEnable != null && LedEnable.equals("on")) {
            noti.flags |= Notification.FLAG_SHOW_LIGHTS;
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-26,BUG-2854327*/
            if (getResources().getBoolean(R.bool.def_led_feature_for_normal_cb)) {
                noti.ledARGB = 0xffff0000;
                noti.ledOnMS = 125;
                noti.ledOffMS = 2875;
            } else {
                noti.ledARGB = 0xff00ff00;
                noti.ledOnMS = 500;
                noti.ledOffMS = 2000;
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-2854327*/
        }
      //[BUGFIX]-Add-BEGIN by TSCD.bangjun.wang,02/10/2015,927272,[SCB]Click CB message in notification have no effect after click home key
      //[BUGFIX]-Add-BEGIN by TSCD.bangjun.wang,02/14/2015,926352,[Pre-cts][CB]light up screen and cb LED indicator cannot be work
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
      //[BUGFIX]-Add-END by TSCD.bangjun.wang
      //[BUGFIX]-Add-END by TSCD.bangjun.wang
        //[BUGFIX]-Add-END by TCTNB.guoju.yao
        NotificationManager notificationManager =
            (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);//[BUGFIX]-Add by TCTNB.yugang.jia,01/09/2014,586348,
        //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,08/12/2013,501208,
        //[HOMO][HOMO]Cell Broadcast messages realization for qualcomm/MTK/Broadcom/Spr-
        //eadtrum/etc android smartphones
        //notificationManager.notify(NOTIFICATION_ID, builder.build());
        notificationManager.notify(NOTIFICATION_ID, noti);
        //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
    }

    /* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
    @Override
    public IBinder onBind(Intent intent) {
        return null;    // clients can't bind to this service
    }

    static Intent createDisplayMessageIntent(Context context, Class intentClass,
                                             ArrayList<CellBroadcastMessage> messageList) {
        // Trigger the list activity to fire up a dialog that shows the received messages
        Intent intent = new Intent(context, intentClass);
        intent.putParcelableArrayListExtra(CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA, messageList);
        return intent;
    }
    //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
    /* MODIFIED-END by yuwan,BUG-4623008*/
    //CBC notification with pop up and tone alert + vibrate in CHILE
    // Chile request
    private void openChileAlertNotification(CellBroadcastMessage cbm , boolean forceVibrateChile ){
        // Acquire a CPU wake lock until the alert dialog and audio start playing.
        CellBroadcastAlertWakeLock.acquireScreenCpuWakeLock(this);

        // Close dialogs and window shade
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(closeDialogs);

        //start audio and vibrate
        TLog.i(TAG,"forcevibrate and specified audio for chile ,CellBroadcastAlertAudio ");
        Intent audioIntent = new Intent(this, CellBroadcastAlertAudio.class);
        audioIntent.setAction(CellBroadcastAlertAudio.ACTION_START_ALERT_AUDIO);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int duration = 10500;  //10.5 seconds
        audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_DURATION_EXTRA, duration);
        audioIntent.putExtra("forceVibrate",forceVibrateChile);
        // add by liang.zhang for Defect 5960218 at 2018-02-06 begin
        audioIntent.putExtra("channelId", cbm.getServiceCategory());
        // add by liang.zhang for Defect 5960218 at 2018-02-06 end
        startService(audioIntent);

        // Decide which activity to start based on the state of the keyguard.
        TLog.i(TAG,"pop up title, date, time for chile ,CellBroadcastAlertFullScreen ");
        Class c = CellBroadcastAlertDialog.class;
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            // Use the full screen activity for security.
            c = CellBroadcastAlertFullScreen.class;
        }

        ArrayList<CellBroadcastMessage> messageList = new ArrayList<CellBroadcastMessage>(1);
        messageList.add(cbm);

        Intent alertDialogIntent = createDisplayMessageIntent(this, c, messageList);
        alertDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alertDialogIntent.putExtra("forceVibrate", forceVibrateChile);
        startActivity(alertDialogIntent);
    }
    //[FEATURE]-Add-END by TCTNB.Dandan.Fang
    //[BUGFIX]-MOD-BEGIN by TSCD.bangjun.wang,08/08/2014,PR-757369
    private String languageDecode (int i) {
               if (i == 1) {
                   return "de";
                  } else if (i == 2) {
                    return "en";
                  } else if (i == 3) {
                   return "it";
                  } else if (i == 4) {
                    return "fr";
                  } else if (i == 5) {
                    return "es";
                  } else if (i == 6) {
                   return "nl";
                  } else if (i == 7) {
                   return "sv";
                  } else if (i == 8) {
                    return "da";
                  } else if (i == 9) {
                   return "pt";
                  } else if (i == 10) {
                  return "fi";
                  } else if (i ==11) {
                  return "no";
                  } else if (i == 12) {
                   return "el";
                  } else if (i == 13) {
                 return "tr";
                  } else if (i == 14) {
                    return "hu";
                  } else if (i == 15) {
                   return "pl";
                  } else if (i == 16) {
                   return "ru";
                  } else {
                    return "all";
                 }
    }
  //[BUGFIX]-MOD-END by TSCD.bangjun.wang

    // begin: aiyan-999810-T-Mobile request for incoming call after WEA message
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(context.TELEPHONY_SERVICE);
                int callState = tm.getCallState();
                TLog.i(TAG, "#- call state is : " + callState);
                // from idle to incallui,
                if (mOriginCallState == TelephonyManager.CALL_STATE_IDLE
                        && callState != TelephonyManager.CALL_STATE_IDLE
                        && CellBroadcastAlertFullScreen.isFromNotification) {

                    Intent finishFullScreenIntent = new Intent(
                            CellBroadcastAlertFullScreen.FINISH_FULL_SCREEN);
                    sendBroadcast(finishFullScreenIntent);

                    Class c = CellBroadcastAlertDialog.class;
                    addToNotificationBarForEmergency(c);

                    // after handing over the phone,if the message in status bar still exit
                    // restore the message box.
                } else if (mOriginCallState != TelephonyManager.CALL_STATE_IDLE
                        && callState == TelephonyManager.CALL_STATE_IDLE
                        && CellBroadcastAlertFullScreen.isFromNotification) {

                    Class c = CellBroadcastAlertDialog.class;
                    KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                    if (km.inKeyguardRestrictedInputMode()) {
                        // Use the full screen activity for security.
                        c = CellBroadcastAlertFullScreen.class;
                    }

                    Intent alertDialogIntent = new Intent(context, c);
                    alertDialogIntent.putParcelableArrayListExtra(
                            CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA,
                            CellBroadcastAlertFullScreen.mMessageList);

                    alertDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(alertDialogIntent);

                }
                mOriginCallState = callState;
            //PR 1077028 Added by fang.song begin
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                if (intent.getBooleanExtra("state", false) && sCmasIdSet != null) {
                    TLog.d(TAG, "airplane mode on, to clear sCmasIdSet!");
                    sCmasIdSet.clear();
                }
            //PR 1077028 Added by fang.song end
            }
        }
    };


    private void addToNotificationBarForEmergency(Class c) {
        if(mMsg == null){
            return;
        }
        ArrayList<CellBroadcastMessage> messageList = new ArrayList<CellBroadcastMessage>(1);
        messageList.add(mMsg);

        int unreadTotalMessages = getUnreadMsg();
        int highestLevel = gethighestLevel();
        String messageBody = mMsg.getMessageBody();
        int channelTitleId = CellBroadcastResources.getDialogTitleResource(mMsg);
        CharSequence channelName = getText(channelTitleId);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(EMERGENCY_NOTIFICATION_ID);

        // Create intent to show the new messages when user selects the notification.
        Intent intent = new Intent(context, c);
        intent.putParcelableArrayListExtra(CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA, //MODIFIED by yuxuan.zhang, 2016-04-20,BUG-1112693
                CellBroadcastAlertFullScreen.mMessageList);
        intent.putExtra(CellBroadcastAlertFullScreen.FROM_NOTIFICATION_EXTRA, true);
        CellBroadcastAlertFullScreen.isFromNotification = true;

        PendingIntent pi = PendingIntent.getActivity(this, EMERGENCY_NOTIFICATION_ID, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_emergency_alert)
                .setTicker(channelName)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pi)
                .setDefaults(Notification.DEFAULT_ALL)
                .setDefaults(Notification.DEFAULT_LIGHTS);
        if (unreadTotalMessages > 1) {
            // use generic count of unread broadcasts if more than one unread
            builder.setContentTitle(getString(R.string.notification_multiple_title_cmas));
            builder.setContentText(getString(R.string.notification_multiple, unreadTotalMessages));
        } else {
            builder.setContentTitle(channelName).setContentText(messageBody);
        }
        int imageId = R.drawable.ic_emergency_alert;
        if (highestLevel == MESSAGE_TYPE_CMAS_PRESIDENTITAL_CELLBROADCAST) {
            imageId = R.drawable.ic_cmas_presidential_flash;
        }

        Notification noti = builder.build();
        noti.icon = imageId;
        notificationManager.notify(EMERGENCY_NOTIFICATION_ID, noti);
    }

    private int getUnreadMsg() {
        Cursor cursor = null;
        int unreadTotalMessages = 0;
        try {
            cursor = context.getContentResolver().query(
                    Telephony.CellBroadcasts.CONTENT_URI,
                    new String[] {
                        Telephony.CellBroadcasts.MESSAGE_READ,
                    },
                    Telephony.CellBroadcasts.MESSAGE_READ + "=?"
                            + " and " + Telephony.CellBroadcasts.MESSAGE_PRIORITY + "=?",
                    new String[] {
                            "0", "3"
                    }, null);
            if (cursor != null) {
                unreadTotalMessages = cursor.getCount();
            }
            return unreadTotalMessages;
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    TLog.e(TAG, "error: " + e);
                }
            }
        }
    }

    /* MODIFIED-BEGIN by yuwan, 2017-05-22,BUG-4654505*/
    private static boolean isInEmergencyCallMode() {
        String inEcm = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE,
                EMERGENCY_FALSE);
        Log.d(TAG, "inEcm = " + inEcm);
        return EMERGENCY_TRUE.equals(inEcm);
    }
    /* MODIFIED-END by yuwan,BUG-4654505*/

    private int gethighestLevel() {
        Cursor cursor = null;
        int highestLevel = MESSAGE_TYPE_COMMON_CELLBROADCAST;
        try {
            cursor = context.getContentResolver().query(
                    CellBroadcastContentProvider.CONTENT_URI,
                    Telephony.CellBroadcasts.QUERY_COLUMNS, // MODIFIED by yuxuan.zhang, 2016-09-27,BUG-2845457
                    Telephony.CellBroadcasts.MESSAGE_READ + "=?", new String[] {
                        "0"
                    },
                    Telephony.CellBroadcasts.DELIVERY_TIME + " DESC");

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    int level = MESSAGE_TYPE_COMMON_CELLBROADCAST;
                    if (cursor.isNull(cursor
                            .getColumnIndex(Telephony.CellBroadcasts.CMAS_MESSAGE_CLASS))) {
                        if (cursor.isNull(cursor
                                .getColumnIndex(Telephony.CellBroadcasts.ETWS_WARNING_TYPE))) {
                            level = MESSAGE_TYPE_COMMON_CELLBROADCAST;
                        } else {
                            level = MESSAGE_TYPE_ETWS_CELLBROADCAST;
                        }
                    } else {
                        int messageClass;
                        messageClass = cursor.getInt(cursor
                                .getColumnIndex(Telephony.CellBroadcasts.CMAS_MESSAGE_CLASS));
                        if (messageClass == SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT) {
                            highestLevel = MESSAGE_TYPE_CMAS_PRESIDENTITAL_CELLBROADCAST;
                            break;
                        } else {
                            level = MESSAGE_TYPE_CMAS_OTHER_CELLBROADCAST;
                        }
                    }

                    highestLevel = level > highestLevel ? level : highestLevel;
                }
            }
            return highestLevel;
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    TLog.e(TAG, "error: " + e);
                }
            }
        }
    }

    // end: aiyan-999810-T-Mobile request for incoming call after WEA message
}
