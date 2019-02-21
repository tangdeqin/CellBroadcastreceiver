/******************************************************************************/
/*                                                               Date:04/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  bo.xu                                                           */
/*  Email  :  Bo.Xu@tcl-mobile.com                                            */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 04/06/2013|        bo.xu         |      FR-400302       |[SMS]Cell broadc- */
/*           |                      |                      |ast SMS support   */
/* ----------|----------------------|----------------------|----------------- */
/* 04/11/2013|     Dandan.Fang      |       FR400297       |CBC notification  */
/*           |                      |                      |with pop up and   */
/*           |                      |                      |tone alert + vib- */
/*           |                      |                      |rate in CHILE     */
/* ----------|----------------------|----------------------|----------------- */
/* 09/06/2013|     yugang.jia       |      FR-516039       |[SMS]Cell broadc- */
/*           |                      |                      |ast SMS support   */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;

import static android.telephony.SmsMessage.ENCODING_16BIT;
import static android.telephony.SmsMessage.ENCODING_7BIT;
import static android.telephony.SmsMessage.ENCODING_8BIT;
import static android.telephony.SmsMessage.ENCODING_UNKNOWN;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import android.telephony.CellBroadcastMessage;
import android.os.SystemProperties;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsMessage.MessageClass;
import android.text.TextUtils;

import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.cellbroadcastreceiver.CellBroadcast.Channel;
import com.android.cb.util.TLog;

//add by liang.zhang for Defect 5960218 at 2018-02-05 begin
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;
import java.util.List;
//add by liang.zhang for Defect 5960218 at 2018-02-05 end

public class CBSUtills {
    /** User data text encoding code unit size */
    /*
     * public static final int ENCODING_UNKNOWN = 0; public static final int
     * ENCODING_7BIT = 1; public static final int ENCODING_8BIT = 2; public
     * static final int ENCODING_16BIT = 3;
     */
    public static final int MAX_CBM_SIZE = 10;

    final int DATACODESCHEMA = 4095;

    String ENABLE = "Enable";

    String DISABLE = "Disable";

    String LOG_TAG = "CELLBROADCASTMESSAGE";

    int GeoScope;

    int MessageCode;

    int UpdateNO;

    int MessageId;

    int dataCodingScheme;

    int PageParm;

    int Language;

    Context context;

    private MessageClass messageClass;

    static HashMap<String, String> pageBuffer = new HashMap<String, String>();// cache
                                                                              // 15
                                                                              // pages

    static ArrayList<String> pageParam = new ArrayList<String>(15);// cache 15
                                                                   // pages

    // messageParam
    static ArrayList<String> getData = new ArrayList<String>();// already exist
                                                               // data after

    static String oldKey = "";

    static public String PRE_DEFINE_CHANNEL_4371 = "ro.cb.prechannel4371"; //PR 1054793 Added by fang.song

    CBSUtills(Context ct) {
        GeoScope = 0;// SerialNO
        MessageCode = 0;// SerialNO
        UpdateNO = 0;// SerialNO
        MessageId = 0;
        dataCodingScheme = 0;
        PageParm = 0;
        pdu = null;
        context = ct;

    }

    byte[] pdu;


    void log(String mes) {
        TLog.d(LOG_TAG, mes + "\n");
    }

    void addChannel(ContentValues values) {
        context.getContentResolver().insert(Channel.CONTENT_URI, values);
    }

    void updateChannel(String channelId, ContentValues values) {
        Uri uri = ContentUris.withAppendedId(Channel.CONTENT_URI, Integer.parseInt(channelId));
        context.getContentResolver().update(uri, values, null, null);
    }

    void updateChannel(ContentValues values) {
        context.getContentResolver().update(Channel.CONTENT_URI, values, null, null);
    }

    void deleteChannel(String channelId) {
        Uri uri = ContentUris.withAppendedId(Channel.CONTENT_URI, Integer.parseInt(channelId));
        context.getContentResolver().delete(uri, null, null);
    }

    // delete all cb channel of db ,add by 20100208
    void deleteChannel() {
        context.getContentResolver().delete(Channel.CONTENT_URI, null, null);
    }

    Cursor queryChannel() {
        Cursor c = context.getContentResolver().query(Channel.CONTENT_URI, null, null, null,
                "_id asc");
        return c;
    }

    Cursor queryChannel(String channelId) {
        Uri uri = ContentUris.withAppendedId(Channel.CONTENT_URI, Integer.parseInt(channelId));
        Cursor c = context.getContentResolver().query(uri, null, null, null, "_id asc");
        return c;
    }

    // true: already has data
    // false: has nothing
    boolean queryChannelIndexA(String channelIndex) {
        String select = Channel.INDEX + " =" + sqlText(channelIndex);
        Cursor c = context.getContentResolver()
                .query(Channel.CONTENT_URI, null, select, null, null);
        int count = c.getCount();// add by fiona for pr84673
        c.close();// add by fiona for pr84673
        if (count > 0)
            return true;
        return false;
    }

    // true: already has data
    // false: has nothing
    boolean queryChannelIndexE(String channelIndexNew, String channelId) {
        String select = Channel.INDEX + "=" + sqlText(channelIndexNew) + " and " + Channel._ID
                + " !=" + channelId + "  ";

        Cursor c = context.getContentResolver()
                .query(Channel.CONTENT_URI, null, select, null, null);
        int count = c.getCount();// add by fiona for pr84673
        c.close();// add by fiona for pr84673
        if (count > 0)
            return true;
        return false;
    }

    String sqlText(String value) {
        return " '" + value.trim() + "' ";
    }

    SmsBroadcastConfigInfo[] getSmsBroadcastConfigInfo() {
        boolean chEnableTmp;
        int chIndexTmp;
        int i = 0;
        // set channel index to mobile
        Cursor c = queryChannel();
        int CursorSize = c.getCount();
        SmsBroadcastConfigInfo[] cbi;
        cbi = new SmsBroadcastConfigInfo[CursorSize];
        if (CursorSize > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                chIndexTmp = Integer.parseInt(c.getString(c.getColumnIndex(Channel.INDEX)));
                chEnableTmp = (c.getString(c.getColumnIndex(Channel.Enable)))
                        .equalsIgnoreCase(ENABLE) ? true : false;
                cbi[i] = new SmsBroadcastConfigInfo(chIndexTmp, chIndexTmp, DATACODESCHEMA,
                        DATACODESCHEMA, chEnableTmp);
                i++;
                c.moveToNext();
            }
        }
        c.close();
        return cbi;
    }

    // save cb language to db
    void saveCBLanguage(String ret) {
        context.getContentResolver().delete(CellBroadcast.CBLanguage.CONTENT_URI, null, null);
        ContentValues values;
        values = new ContentValues();
        values.put(CellBroadcast.CBLanguage.CBLANGUAGE, ret);
        context.getContentResolver().insert(CellBroadcast.CBLanguage.CONTENT_URI, values);
    }

    // get cb language
    int queryCBLanguage() {
        //[FEATURE]-begin-MOD by TCTNB.yugang.jia,09/06/2013,FR-516039,
        int cblanguage = 0;

        Cursor c = context.getContentResolver().query(CellBroadcast.CBLanguage.CONTENT_URI, null,
                null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            cblanguage = Integer.parseInt(c.getString(c
                    .getColumnIndex(CellBroadcast.CBLanguage.CBLANGUAGE)));
        }
        c.close();
        return cblanguage;
    }

    //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
    //CBC notification with pop up and tone alert + vibrate in CHILE
    //SMSCB channels should not be customizable or editable by the end user.
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-19,BUG-1112693*/
    boolean isForbiddenToModifyPredefinedChannels(String channelId , boolean enableEdit) {
        //[FEATURE]-Mod-BEGIN by TCTNB.Tongyuan.Lv, 07/08/2013, FR-482850, add customized cell broadcast channel
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
        boolean isForbidden = enableEdit ? context.getResources().getBoolean(R.bool.def_is_forbidden_modify_predefined_channels) : context.getResources().getBoolean(
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                R.bool.feature_cellbroadcastreceiver_forbiddenModifyPredefinedChannels_on);
        if (isForbidden) {
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            /*
             * because we use the def_cellbroadcastreceiver_customized_channel_names and
             * def_cellbroadcastreceiver_customized_channel_numbers instead of
             * def_cellbroadcastreceiver_chn?name, and def_cellbroadcastreceiver_chn?number
             * to define predefined customized cell broadcast channel names and numbers,
             * so need to make a update here.
             */
            /*int channel1 = context.getResources().getInteger(
                    R.integer.def_cellbroadcastreceiver_chn1number);
            int channel2 = context.getResources().getInteger(
                    R.integer.def_cellbroadcastreceiver_chn2number);*/


            try {
                Cursor c = queryChannel(channelId);
                int currentIndex = -1;
                if (c != null && c.moveToFirst()) {
                    currentIndex = Integer.valueOf(c.getString(c.getColumnIndex(Channel.INDEX)));
                }
                c.close();
                /*if (currentIndex != 0 && (currentIndex == channel1 || currentIndex == channel2)) {
                    return true;
                }*/
                String persoChannelNumbersAndPolicy = context.getResources().getString(
                        R.string.def_cellbroadcastreceiver_customized_channel_numbers_policy);

                if (TextUtils.isEmpty(persoChannelNumbersAndPolicy.trim())) {
                    TLog.i("CellBroadcastReceiver", "empty customized channel, no forbidden channel");
                     /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-29,BUG-2845457*/
                     persoChannelNumbersAndPolicy = context.getResources().getString(
                                R.string.def_cellbroadcastreceiver_chn1);
                    if (TextUtils.isEmpty(persoChannelNumbersAndPolicy.trim())) {
                        return false;
                    }
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                }
                String[] channelNumbersAndPolicy = persoChannelNumbersAndPolicy.split(";");
                int len = channelNumbersAndPolicy.length;

                for (int i = 0; i < len; i++) {
                    String[] channelInfo = channelNumbersAndPolicy[i].split(",");
                    if (channelInfo.length == 2) {
                        String channelNumber = channelInfo[0].trim();
                        String channelPolicy = channelInfo[1].trim();
                        if (!TextUtils.isEmpty(channelNumber)
                                && currentIndex == Integer.parseInt(channelNumber)
                                /*&& channelPolicy.equals("-")*/) {//Modify by chenglin.jiang for PR1017965
                            TLog.i("CellBroadcastReceiver", "get matched forbidden edit channel " + channelNumber);
                            return true;
                        }
                    } else {
                        TLog.e("CellBroadcastReceiver", "detected wrong number and policy, please check the perso settings");
                    }
                }
                return false;
            } catch (NumberFormatException e) {
                TLog.e("CELLBROADCAST-CBMSetting", "NumberFormatException:" + e.toString());
                return false;
            }
        //}
        //[FEATURE]-Mod-END by TCTNB.Tongyuan.Lv
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
        }
        return false;
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
    }
   //[FEATURE]-Add-END by TCTNB.Dandan.Fang


    int queryCBLanguage(long subid) {
        //[FEATURE]-begin-MOD by TCTNB.yugang.jia,09/06/2013,FR-516039,
        int cblanguage = 0;
        TLog.i(LOG_TAG,"queryCBLanguage(int subid)"+Uri.withAppendedPath(CellBroadcast.CBLanguage.CONTENT_URI,"sub"+subid));
        Cursor c = context.getContentResolver().query(Uri.withAppendedPath(CellBroadcast.CBLanguage.CONTENT_URI,"sub"+subid), null,
                null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            cblanguage = Integer.parseInt(c.getString(c
                    .getColumnIndex(CellBroadcast.CBLanguage.CBLANGUAGE)));
        }
        c.close();
        return cblanguage;
    }
    Cursor queryChannel(long subid) {
        TLog.i(LOG_TAG,"queryChannel(int subid)="+Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid));
        Cursor c = context.getContentResolver().query(Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid), null, null, null,
                "_id asc");
        return c;
    }
    void saveCBLanguage(String ret,long subid) {
        TLog.i(LOG_TAG,"saveCBLanguage(String ret,int subid)="+Uri.withAppendedPath(CellBroadcast.CBLanguage.CONTENT_URI,"sub"+subid));
        context.getContentResolver().delete(Uri.withAppendedPath(CellBroadcast.CBLanguage.CONTENT_URI,"sub"+subid), null, null);
        ContentValues values;
        values = new ContentValues();
        values.put(CellBroadcast.CBLanguage.CBLANGUAGE, ret);
        context.getContentResolver().insert(Uri.withAppendedPath(CellBroadcast.CBLanguage.CONTENT_URI,"sub"+subid), values);
    }
    Cursor queryChannel(String channelId,long subid) {
        TLog.i(LOG_TAG,"queryChannel(String channelId,int subid)="+Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid));
        Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid), Integer.parseInt(channelId));
        Cursor c = context.getContentResolver().query(uri, null, null, null, "_id asc");
        return c;
    }
    //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
    //CBC notification with pop up and tone alert + vibrate in CHILE
    //SMSCB channels should not be customizable or editable by the end user.
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-10-19,BUG-1112693*/
    boolean isForbiddenToModifyPredefinedChannels(String channelId,long subid,boolean enableEdit) {
        //[FEATURE]-Mod-BEGIN by TCTNB.Tongyuan.Lv, 07/08/2013, FR-482850, add customized cell broadcast channel
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
        boolean isForbidden = enableEdit ? context.getResources().getBoolean(R.bool.def_is_forbidden_modify_predefined_channels) : context.getResources().getBoolean(
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                R.bool.feature_cellbroadcastreceiver_forbiddenModifyPredefinedChannels_on);
        if (isForbidden) {
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
            /*
             * because we use the def_cellbroadcastreceiver_customized_channel_names and
             * def_cellbroadcastreceiver_customized_channel_numbers instead of
             * def_cellbroadcastreceiver_chn?name, and def_cellbroadcastreceiver_chn?number
             * to define predefined customized cell broadcast channel names and numbers,
             * so need to make a update here.
             */
            /*int channel1 = context.getResources().getInteger(
                    R.integer.def_cellbroadcastreceiver_chn1number);
            int channel2 = context.getResources().getInteger(
                    R.integer.def_cellbroadcastreceiver_chn2number);*/


            try {
                TLog.i(LOG_TAG,"isForbiddenToModifyPredefinedChannels-channelId="+channelId+" subid="+subid);
                Cursor c = queryChannel(channelId,subid);
                int currentIndex = -1;
                if (c != null && c.moveToFirst()) {
                    currentIndex = Integer.valueOf(c.getString(c.getColumnIndex(Channel.INDEX)));
                }
                c.close();
                /*if (currentIndex != 0 && (currentIndex == channel1 || currentIndex == channel2)) {
                    return true;
                }*/
                
                // add by liang.zhang for Defect 5960218 at 2018-02-05 begin
                boolean isUAE = false;
                SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
                List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
            	if (subList != null && subList.size() > 0) {
            		for (int i = 0; i < subList.size(); i++) {
            			SubscriptionInfo info = subList.get(i);
            			if (info!= null && info.getMcc() == 424) {
            				isUAE = true;
            	        }
            		}
            	}
            	if (!enableEdit && isUAE) {
            		switch (currentIndex) {
            			case 4379:
            			case 4380:
            			case 4392:
            			case 4393:
            				return false;
            				
            			default:
            				break;
            		}
            	}
                // add by liang.zhang for Defect 5960218 at 2018-02-05 end
                
                String persoChannelNumbersAndPolicy = context.getResources().getString(
                        R.string.def_cellbroadcastreceiver_customized_channel_numbers_policy);
                if (TextUtils.isEmpty(persoChannelNumbersAndPolicy.trim())) {
                    TLog.i("CellBroadcastReceiver", "empty customized channel, no forbidden channel");
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-29,BUG-2845457*/
                    if (subid == 0) {
                        persoChannelNumbersAndPolicy = context.getResources().getString(
                                R.string.def_cellbroadcastreceiver_chn1);
                    }else if(subid == 1){
                        persoChannelNumbersAndPolicy = context.getResources().getString(
                                R.string.def_cellbroadcastreceiver_chn2);
                    }
                    if (TextUtils.isEmpty(persoChannelNumbersAndPolicy.trim())) {
                        return false;
                    }
                    /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
                }
                String[] channelNumbersAndPolicy = persoChannelNumbersAndPolicy.split(";");
                int len = channelNumbersAndPolicy.length;

                for (int i = 0; i < len; i++) {
                    String[] channelInfo = channelNumbersAndPolicy[i].split(",");
                    if (channelInfo.length == 2) {
                        String channelNumber = channelInfo[0].trim();
                        String channelPolicy = channelInfo[1].trim();
                        if (!TextUtils.isEmpty(channelNumber)
                                && currentIndex == Integer.parseInt(channelNumber)
                                /*&& channelPolicy.equals("-")*/) {//Modify by chenglin.jiang for PR1017965
                            TLog.i("CellBroadcastReceiver", "get matched forbidden edit channel " + channelNumber);
                            // add by liang.zhang for Defect 5861390 at 2018-01-27 begin
                            if (!enableEdit && Integer.parseInt(channelNumber) == 4371 && context.getResources().getBoolean(R.bool.def_isSupport_modify_4371)) {
                            	TLog.i("CellBroadcastReceiver", "get matched forbidden edit channel = 4371 and def_isSupport_modify_4371 = true while on in edit mode");
                            	return false;
                            }
                            // add by liang.zhang for Defect 5861390 at 2018-01-27 end
                            return true;
                        }
                    } else {
                        TLog.e("CellBroadcastReceiver", "detected wrong number and policy, please check the perso settings");
                    }
                }
                return false;
            } catch (NumberFormatException e) {
                TLog.e("CELLBROADCAST-CBMSetting", "NumberFormatException:" + e.toString());
                return false;
            }
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-22,BUG-2845457*/
        }
        return false;
        /* MODIFIED-END by yuxuan.zhang,BUG-2845457*/
        //[FEATURE]-Mod-END by TCTNB.Tongyuan.Lv
    }
    void updateChannel(String channelId, ContentValues values,long subid) {
        TLog.i(LOG_TAG,"updateChannel(String channelId, ContentValues values,int subid)="+Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid));;
        Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid), Integer.parseInt(channelId));
        context.getContentResolver().update(uri, values, null, null);
    }
    void deleteChannel(String channelId,long subid) {
        TLog.i(LOG_TAG,"deleteChannel(String channelId,int subid)="+Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid));
        Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid), Integer.parseInt(channelId));
        context.getContentResolver().delete(uri, null, null);
    }
    SmsBroadcastConfigInfo[] getSmsBroadcastConfigInfo(long subid) {
        boolean chEnableTmp;
        int chIndexTmp;
        int i = 0;
        // set channel index to mobile
        TLog.i(LOG_TAG,"getSmsBroadcastConfigInfo(int sub)-subid="+subid);
        Cursor c = queryChannel(subid);
        int CursorSize = c.getCount();
        SmsBroadcastConfigInfo[] cbi;
        cbi = new SmsBroadcastConfigInfo[CursorSize];
        if (CursorSize > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                chIndexTmp = Integer.parseInt(c.getString(c.getColumnIndex(Channel.INDEX)));
                chEnableTmp = (c.getString(c.getColumnIndex(Channel.Enable)))
                        .equalsIgnoreCase(ENABLE) ? true : false;
                cbi[i] = new SmsBroadcastConfigInfo(chIndexTmp, chIndexTmp, DATACODESCHEMA,
                        DATACODESCHEMA, chEnableTmp);
                i++;
                c.moveToNext();
            }
        }
        c.close();
        return cbi;
    }
    void addChannel(ContentValues values,long subid) {
        TLog.i(LOG_TAG,"addChannel(ContentValues values,int subid)="+Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid));
        context.getContentResolver().insert(Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid), values);
    }
    // true: already has data
    // false: has nothing
    boolean queryChannelIndexA(String channelIndex,long subid) {
        String select = Channel.INDEX + " =" + sqlText(channelIndex);
        TLog.i(LOG_TAG,"queryChannelIndexA(String channelIndex,int subid)="+Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid));
        Cursor c = context.getContentResolver()
                .query(Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid), null, select, null, null);
        int count = c.getCount();// add by fiona for pr84673
        c.close();// add by fiona for pr84673
        if (count > 0)
            return true;
        return false;
    }
    // true: already has data
    // false: has nothing
    boolean queryChannelIndexE(String channelIndexNew, String channelId,long subid) {
        String select = Channel.INDEX + "=" + sqlText(channelIndexNew) + " and " + Channel._ID
                + " !=" + channelId + "  ";

        TLog.i(LOG_TAG,"queryChannelIndexE(String channelIndexNew, String channelId,int subid)="+Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid));
        Cursor c = context.getContentResolver()
                .query(Uri.withAppendedPath(Channel.CONTENT_URI,"sub"+subid), null, select, null, null);
        int count = c.getCount();// add by fiona for pr84673
        c.close();// add by fiona for pr84673
        if (count > 0)
            return true;
        return false;
    }
    //PR 1054793 Added by fang.song begin
    public static boolean isShow4371AsNormal(CellBroadcastMessage cbm){
        // modify by liang.zhang for Defect 5772065 at 2018-01-08 begin
    	//[add]-begin-by-chaobing.huang-01132017-defect4007891
        //if ("true".equalsIgnoreCase(SystemProperties.get(CBSUtills.PRE_DEFINE_CHANNEL_4371, "false")) && cbm.getServiceCategory() == 4371){
//    	Context context = CellBroadcastReceiverApp.getApplication();
//    	if (context.getResources().getBoolean(R.bool.ro_cb_prechannel4371) && cbm.getServiceCategory() == 4371){
    	//[add]-end-by-chaobing.huang-01132017-defect4007891
//        	return true;
//        }
//        return false;
        
        return !CellBroadcastConfigService.isEmergencyAlertMessage(cbm);
        // modify by liang.zhang for Defect 5772065 at 2018-01-08 end
    }
    //PR 1054793 Added by fang.song end
    
    // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
    public static boolean isCanadaSimCard(Context context) {
    	SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
    	if (subList != null && subList.size() > 0) {
    		for (int i = 0; i < subList.size(); i++) {
    			SubscriptionInfo info = subList.get(i);
    			if (info != null && info.getMcc() == 302) {
    				return true;
                }
    		}
    	}
    	return false;
    }
    // add by liang.zhang for Defect 6929849 at 2018-09-01 end
}
