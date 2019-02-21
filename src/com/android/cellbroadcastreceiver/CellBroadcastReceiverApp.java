/*
 * Copyright (C) 2011 The Android Open Source Project
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
/* ==========================================================================
 *     Modifications on Features list / Changes Request / Problems Report
 * --------------------------------------------------------------------------
 *    date   |        author        |         Key          |     comment
 * ----------|----------------------|----------------------|-----------------
 * ----------|----------------------|----------------------|-----------------
 * 05/07/2014|ke.meng               | FR-642065            |RUSSIA REQ
 * ----------|----------------------|----------------------|-----------------
 * 08/30/2014|     tianming.lei     |        777440        |Cell broadcast messages
 *           |                      |                      |have an incorrect format
 * ----------|----------------------|----------------------|-----------------
 *****************************************************************************/
package com.android.cellbroadcastreceiver;

import android.app.Application;
import android.telecom.Log; // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
import android.telephony.CellBroadcastMessage;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.Map;
//[BUGFIX]-ADD-BEGIN by TCTNB.ke.meng,05/07/2014,642065
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences; // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693

import java.util.Iterator;
//[BUGFIX]-ADD-END by TCTNB.ke.meng
import com.android.cb.util.TLog;

/**
 * The application class loads the default preferences at first start,
 * and remembers the time of the most recently received broadcast.
 */
public class CellBroadcastReceiverApp extends Application {
    private static final String TAG = "CellBroadcastReceiverApp";
    // [BUGFIX]-Add-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
    public static CellBroadcastReceiverApp instance;
    // [BUGFIX]-Add-End by TSCD.tianming.lei

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: fix strict mode violation from the following method call during app creation
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    // [BUGFIX]-Add-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
        instance = this;
    // [BUGFIX]-Add-End by TSCD.tianming.lei
    }
    // [BUGFIX]-Add-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
    synchronized public static CellBroadcastReceiverApp getApplication(){
        return instance;
    }
    // [BUGFIX]-Add-End by TSCD.tianming.lei

    /** List of unread non-emergency alerts to show when user selects the notification. */
    private static final ArrayList<CellBroadcastMessage> sNewMessageList =
            new ArrayList<CellBroadcastMessage>(4);

    /** Latest area info cell broadcast received. */
    private static Map<Integer, CellBroadcastMessage> sLatestAreaInfo =
            new HashMap<Integer,CellBroadcastMessage>();
    //[BUGFIX]-MOD-BEGIN by TCTNB.ke.meng,05/07/2014,642065
    public static Map<Integer,Integer> notificationid =
            new HashMap<Integer,Integer>();
    private static final int NOTIFICATION_ID = 0;
    private static int repeatnotification = 0;
    /** Adds a new unread non-emergency message and returns the current list. */
    static ArrayList<CellBroadcastMessage> addNewMessageToList(CellBroadcastMessage message) {
        sNewMessageList.add(message);
        return sNewMessageList;
    }

    /* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
    static ArrayList<CellBroadcastMessage> addNewMessageToListRussia(CellBroadcastMessage message,
                                                                     boolean isRussia) {
        setNotificationId(message.getServiceCategory());
        if (isRussia) {
            sNewMessageList.clear();
        }
        Iterator iter = sNewMessageList.iterator();
        while (iter.hasNext()) {
            CellBroadcastMessage cbm = (CellBroadcastMessage) iter.next();
            if (cbm.getServiceCategory() == message.getServiceCategory()) {
                clearMessageList(cbm);
                sNewMessageList.add(message);
                return sNewMessageList;
            }
        }
        sNewMessageList.add(message);
        return sNewMessageList;
    }
    /* MODIFIED-END by yuwan,BUG-4623008*/

    static ArrayList<CellBroadcastMessage> addNewMessageToListRussia(CellBroadcastMessage message) {
        TLog.d(TAG,"JYG,CellbroadcastReceiverApp,addNewMessageToListRussia,message.getServiceCategory():"+message.getServiceCategory());
        setNotificationId(message.getServiceCategory());
        Iterator iter = sNewMessageList.iterator();
        while(iter.hasNext()){
            CellBroadcastMessage cbm = (CellBroadcastMessage)iter.next();
            if(cbm.getServiceCategory() == message.getServiceCategory()){
                TLog.d(TAG,"JYG,CellbroadcastReceiverApp,addNewMessageToListRussia,This mesage is repeat");
                clearMessageList(cbm);
                sNewMessageList.add(message);
                return sNewMessageList;
            }
        }
        sNewMessageList.add(message);
        return sNewMessageList;
    }
    //[BUGFIX]-MOD-END by TCTNB.ke.meng
    /** Clears the list of unread non-emergency messages. */
    static void clearNewMessageList() {
        sNewMessageList.clear();
    }
    //[BUGFIX]-Add-BEGIN by TCTNB.ke.meng,05/07/2014,642065
    static void setNotificationId(int id){
        TLog.d(TAG,"JYG,CellbroadcastReceiverApp,setNotificationId,id:"+id);
        notificationid.put(id,id);
    }
    static int getNotificationId(int id){
        if(notificationid.get(id)==null||notificationid.get(id)==0){
            TLog.d(TAG,"JYG,CellbroadcastReceiverApp,getNotificationId,NOTIFICATION_ID");
            return NOTIFICATION_ID;
        }
        TLog.d(TAG,"JYG,CellbroadcastReceiverApp,getNotificationId,id:"+id);
        return notificationid.get(id);
    }
    /** Clears duplicate messages. */
    static void clearMessageList(CellBroadcastMessage cbm) {
        sNewMessageList.remove(cbm);
        repeatnotification = cbm.getServiceCategory();
        TLog.d(TAG,"JYG,CellbroadcastReceiverApp,clearMessageList,repeatnotification:"+repeatnotification);
    }
    static int getRepeatNotification() {
        if(repeatnotification!=NOTIFICATION_ID){
            TLog.d(TAG,"jia,CellbroadcastReceiverApp,getRepeatNotification,repeatnotification:"+repeatnotification);
            return repeatnotification;
        }
        TLog.d(TAG,"JYG,CellbroadcastReceiverApp,getRepeatNotification,NOTIFICATION_ID");
        return NOTIFICATION_ID;
    }
    //[BUGFIX]-Add-END by TCTNB.ke.meng
    /** Saves the latest area info broadcast received. */

    //[BUGFIX]-Add-BEGIN by lijun.zhang,10/14/2015,PR-1084572
    static void resetRepeatNotification() {
        repeatnotification = NOTIFICATION_ID;
    }
    //[BUGFIX]-Add-End by lijun.zhang

    static void setLatestAreaInfo(CellBroadcastMessage areaInfo) {
        sLatestAreaInfo.put(areaInfo.getSubId(), areaInfo);
    }

    /** Returns the latest area info broadcast received. */
    static CellBroadcastMessage getLatestAreaInfo(int subId ) {
        return sLatestAreaInfo.get(subId);
    }
}
