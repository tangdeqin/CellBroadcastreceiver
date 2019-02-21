/*
 * Copyright (C) 2011-2012, Code Aurora Forum. All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following
       disclaimer in the documentation and/or other materials provided
       with the distribution.
 * Neither the name of Code Aurora Forum, Inc. nor the names of its
       contributors may be used to endorse or promote products derived
       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/******************************************************************************/
/*                                                               Date:10/2012 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2012 TCL Communication Technology Holdings Limited.        */
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
/*  File     : packages/providers/TelephonyProvider/tct_src/com/jrd/providers/*/
/*             cellbroadcast/CellBroadcast.java                               */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 09/06/2013|yugang.jia            |FR-516039             |SMSCB             */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.cb.util.TLog;
import com.android.cellbroadcastreceiver.CellBroadcast;
import com.android.cellbroadcastreceiver.CellBroadcast.CBLanguage;
import com.android.cellbroadcastreceiver.CellBroadcast.CBRingtone;//[FEATURE]-Add by TCTNB.bo.xu,04/09/2013,FR-399953,
                                                                //Tone needed for CellBroadcast message received
import com.android.cellbroadcastreceiver.CellBroadcast.Cellbroadcasts;
import com.android.cellbroadcastreceiver.CellBroadcast.Channel;
import com.android.cellbroadcastreceiver.CellBroadcast;

public class CellBroadcastProvider extends ContentProvider {
    private static final String TAG = "CellBroadcastProvider";

    private static final String DATABASE_NAME = "cellbroadcast.db";

    private static final int DATABASE_VERSION = 3;

    private static final String CB_TABLE_NAME = "cellbroadcast";

    private static final String CBLANGUAGE_TABLE_NAME = "cblanguage";

    private static final String CHANNEL_TABLE_NAME = "channel";

    private static final String CBRINGTONE_TABLE_NAME = "cbringtone";//[FEATURE]-Add by TCTNB.bo.xu,04/09/2013,FR-399953,
                                                                     //Tone needed for CellBroadcast message received
    private static final String CBLANGUAGESIM1_TABLE_NAME = "cblanguagesim1";
    private static final String CHANNELSIM1_TABLE_NAME = "channelsim1";
    private static final String CBLANGUAGESIM2_TABLE_NAME = "cblanguagesim2";
    private static final String CHANNELSIM2_TABLE_NAME = "channelsim2";
    private static HashMap<String, String> sLanguageSim1Map;
    private static HashMap<String, String> sChannelSim1Map;
    private static HashMap<String, String> sLanguageSim2Map;
    private static HashMap<String, String> sChannelSim2Map;
    private static HashMap<String, String> sBroadcastMap;

    private static HashMap<String, String> sChannelMap;

    private static HashMap<String, String> sLanguageMap;

    private static HashMap<String, String> sRingtoneMap;//[FEATURE]-Add by TCTNB.bo.xu,04/09/2013,FR-399953,
                                                        //Tone needed for CellBroadcast message received

    private static HashMap<String, String> sPecialMap;

    private static final int CBS = 1;

    private static final int CB_ID = 2;

    private static final int CHANNEL = 3;

    private static final int CHANNEL_ID = 4;

    private static final int CBLANGUAGE = 5;

    private static final int CBLANGUAGE_ID = 6;

    private static final int CONTENT_URI_CBCH = 7;

    private static final int CONTENT_URI_CBCH_ID = 8;
    //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
    //Tone needed for CellBroadcast message received
    private static final int CBRINGTONE = 9;

    private static final int CBRINGTONE_ID = 10;
    //[FEATURE]-Add-END by TCTNB.bo.xu
    private static final int CHANNELSIM1 = 11;
    private static final int CHANNELSIM1_ID = 12;
    private static final int CBLANGUAGESIM1 = 13;
    private static final int CBLANGUAGESIM1_ID = 14;
    private static final int CHANNELSIM2 = 15;
    private static final int CHANNELSIM2_ID = 16;
    private static final int CBLANGUAGESIM2 = 17;
    private static final int CBLANGUAGESIM2_ID = 18;
    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + CB_TABLE_NAME + " (" + Cellbroadcasts._ID
                    + " INTEGER PRIMARY KEY," + Cellbroadcasts.MESSAGE + " TEXT,"
                    + Cellbroadcasts.CHANNEL + " TEXT," + Cellbroadcasts.CREATED + " INTEGER"
                    + ");");
            db.execSQL("CREATE TABLE " + CHANNEL_TABLE_NAME + " (" + Channel._ID
                    + " INTEGER PRIMARY KEY," + Channel.INDEX + " TEXT ," + Channel.NAME + " TEXT,"
                    + Channel.Enable + " TEXT," + Channel.CREATED + " INTEGER" + ");");
            db.execSQL("CREATE TABLE " + CBLANGUAGE_TABLE_NAME + " (" + CBLanguage._ID
                    + " INTEGER PRIMARY KEY," + CBLanguage.CBLANGUAGE + " TEXT" + ");");
            //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
            //Tone needed for CellBroadcast message received
            db.execSQL("CREATE TABLE " + CBRINGTONE_TABLE_NAME + " (" + CBRingtone._ID
                    + " INTEGER PRIMARY KEY," + CBRingtone.CBRINGTONE + " TEXT" + ");");
            //[FEATURE]-Add-END by TCTNB.bo.xu
            db.execSQL("CREATE TABLE " + CHANNELSIM1_TABLE_NAME + " (" + Channel._ID
                    + " INTEGER PRIMARY KEY," + Channel.INDEX + " TEXT ," + Channel.NAME + " TEXT,"
                    + Channel.Enable + " TEXT," + Channel.CREATED + " INTEGER" + ");");
                    db.execSQL("CREATE TABLE " + CBLANGUAGESIM1_TABLE_NAME + " (" + CBLanguage._ID
                    + " INTEGER PRIMARY KEY," + CBLanguage.CBLANGUAGE + " TEXT" + ");");
                    db.execSQL("CREATE TABLE " + CHANNELSIM2_TABLE_NAME + " (" + Channel._ID
                    + " INTEGER PRIMARY KEY," + Channel.INDEX + " TEXT ," + Channel.NAME + " TEXT,"
                    + Channel.Enable + " TEXT," + Channel.CREATED + " INTEGER" + ");");
                    db.execSQL("CREATE TABLE " + CBLANGUAGESIM2_TABLE_NAME + " (" + CBLanguage._ID
                    + " INTEGER PRIMARY KEY," + CBLanguage.CBLANGUAGE + " TEXT" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            TLog.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            switch (oldVersion) {
                case 2:
                    if (newVersion <= 2) {
                        return;
                    }
                    db.beginTransaction();
                    try {
                        upgradeDatabaseToVersion3(db);
                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        TLog.e(TAG, ex.getMessage(), ex);
                    break;
                    } finally {
                        db.endTransaction();
                    }
                    return ;
            }
            dropAll(db);
            onCreate(db);
        }
        private void dropAll(SQLiteDatabase db){
            db.execSQL("DROP TABLE IF EXISTS notes");
            db.execSQL("DROP TABLE IF EXISTS cellbroadcast");
            db.execSQL("DROP TABLE IF EXISTS channel");
            db.execSQL("DROP TABLE IF EXISTS cblanguage");
            db.execSQL("DROP TABLE IF EXISTS cbringtone");
            db.execSQL("DROP TABLE IF EXISTS channelsim1");
            db.execSQL("DROP TABLE IF EXISTS channelsim2");
            db.execSQL("DROP TABLE IF EXISTS cblanguagesim1");
            db.execSQL("DROP TABLE IF EXISTS cblanguagesim2");
            }
        private void upgradeDatabaseToVersion3(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + CHANNELSIM1_TABLE_NAME + " (" + Channel._ID
            + " INTEGER PRIMARY KEY," + Channel.INDEX + " TEXT ," + Channel.NAME + " TEXT,"
            + Channel.Enable + " TEXT," + Channel.CREATED + " INTEGER" + ");");
            db.execSQL("CREATE TABLE " + CBLANGUAGESIM1_TABLE_NAME + " (" + CBLanguage._ID
            + " INTEGER PRIMARY KEY," + CBLanguage.CBLANGUAGE + " TEXT" + ");");
            db.execSQL("CREATE TABLE " + CHANNELSIM2_TABLE_NAME + " (" + Channel._ID
            + " INTEGER PRIMARY KEY," + Channel.INDEX + " TEXT ," + Channel.NAME + " TEXT,"
            + Channel.Enable + " TEXT," + Channel.CREATED + " INTEGER" + ");");
            db.execSQL("CREATE TABLE " + CBLANGUAGESIM2_TABLE_NAME + " (" + CBLanguage._ID
            + " INTEGER PRIMARY KEY," + CBLanguage.CBLANGUAGE + " TEXT" + ");");
           }
}

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        android.util.Log.d("===ydg===", "CellBroadcastProvider query uri="+sUriMatcher.match(uri) +
                " ,id ="+uri.getLastPathSegment());
        switch (sUriMatcher.match(uri)) {
            case CBS:
                qb.setTables(CB_TABLE_NAME);
                qb.setProjectionMap(sBroadcastMap);
                break;

            case CB_ID:
                qb.setTables(CB_TABLE_NAME);
                qb.setProjectionMap(sBroadcastMap);
                qb.appendWhere(Cellbroadcasts._ID + "=" + uri.getPathSegments().get(1));
                break;
            case CHANNEL:
                qb.setTables(CHANNEL_TABLE_NAME);
                qb.setProjectionMap(sChannelMap);
                break;
            case CHANNEL_ID:
                qb.setTables(CHANNEL_TABLE_NAME);
                qb.setProjectionMap(sChannelMap);
                qb.appendWhere(Channel._ID + "=" + uri.getPathSegments().get(1));
                break;
            case CBLANGUAGE:
                qb.setTables(CBLANGUAGE_TABLE_NAME);
                qb.setProjectionMap(sLanguageMap);
                break;
            case CBLANGUAGE_ID:
                qb.setTables(CBLANGUAGE_TABLE_NAME);
                qb.setProjectionMap(sLanguageMap);
                qb.appendWhere(CBLanguage._ID + "=" + uri.getPathSegments().get(1));
                break;
            case CHANNELSIM1:
                qb.setTables(CHANNELSIM1_TABLE_NAME);
                qb.setProjectionMap(sChannelSim1Map);
                break;
            case CHANNELSIM1_ID:
                qb.setTables(CHANNELSIM1_TABLE_NAME);
                qb.setProjectionMap(sChannelSim1Map);
                qb.appendWhere(Channel._ID + "=" + uri.getLastPathSegment());
                break;
            case CBLANGUAGESIM1:
                qb.setTables(CBLANGUAGESIM1_TABLE_NAME);
                qb.setProjectionMap(sLanguageSim1Map);
                break;
            case CBLANGUAGESIM1_ID:
                qb.setTables(CBLANGUAGESIM1_TABLE_NAME);
                qb.setProjectionMap(sLanguageSim1Map);
                qb.appendWhere(CBLanguage._ID + "=" + uri.getLastPathSegment());
                break;
            case CHANNELSIM2:
                qb.setTables(CHANNELSIM2_TABLE_NAME);
                qb.setProjectionMap(sChannelSim2Map);
                break;
            case CHANNELSIM2_ID:
                qb.setTables(CHANNELSIM2_TABLE_NAME);
                qb.setProjectionMap(sChannelSim2Map);
                qb.appendWhere(Channel._ID + "=" + uri.getLastPathSegment());
                break;
            case CBLANGUAGESIM2:
                qb.setTables(CBLANGUAGESIM2_TABLE_NAME);
                qb.setProjectionMap(sLanguageSim2Map);
                break;
            case CBLANGUAGESIM2_ID:
                qb.setTables(CBLANGUAGESIM2_TABLE_NAME);
                qb.setProjectionMap(sLanguageSim2Map);
                qb.appendWhere(CBLanguage._ID + "=" + uri.getLastPathSegment());
                break;
            //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
            //Tone needed for CellBroadcast message received
            case CBRINGTONE:
                qb.setTables(CBRINGTONE_TABLE_NAME);
                qb.setProjectionMap(sRingtoneMap);
                break;
            case CBRINGTONE_ID:
                qb.setTables(CBRINGTONE_TABLE_NAME);
                qb.setProjectionMap(sRingtoneMap);
                qb.appendWhere(CBRingtone._ID + "=" + uri.getPathSegments().get(1));
                break;
            //[FEATURE]-Add-END by TCTNB.bo.xu
            case CONTENT_URI_CBCH:
                qb.setTables("cellbroadcast LEFT OUTER JOIN channel ON (cellbroadcast.channel = channel.mesid)");
                qb.setProjectionMap(sPecialMap);
                break;
            case CONTENT_URI_CBCH_ID:
                qb.setTables("cellbroadcast LEFT OUTER JOIN channel ON (cellbroadcast.channel = channel.mesid)");
                qb.setProjectionMap(sPecialMap);
                qb.appendWhere(Cellbroadcasts.CHANNEL + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy = null;
        if (TextUtils.isEmpty(sortOrder)) {
        //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
        //Tone needed for CellBroadcast message received
        if (sUriMatcher.match(uri) != CBLANGUAGE && sUriMatcher.match(uri) != CBLANGUAGE_ID
                    && sUriMatcher.match(uri) != CBRINGTONE && sUriMatcher.match(uri) != CBRINGTONE_ID
                    && sUriMatcher.match(uri) != CBLANGUAGESIM1 && sUriMatcher.match(uri) != CBLANGUAGESIM1_ID
                    && sUriMatcher.match(uri) != CBLANGUAGESIM2 && sUriMatcher.match(uri) != CBLANGUAGESIM2_ID) {
			//[FEATURE]-Add-END by TCTNB.bo.xu
			orderBy = Cellbroadcasts.DEFAULT_SORT_ORDER;
		}
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data
        // changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return "";
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        //[FEATURE]-Mod by TCTNB.bo.xu,04/09/2013,FR-399953,
        //Tone needed for CellBroadcast message received
        if (sUriMatcher.match(uri) != CBS && sUriMatcher.match(uri) != CHANNEL
                && sUriMatcher.match(uri) != CBLANGUAGE && sUriMatcher.match(uri) != CBRINGTONE
                && sUriMatcher.match(uri) != CBLANGUAGESIM1 && sUriMatcher.match(uri) != CBLANGUAGESIM2
                && sUriMatcher.match(uri) != CHANNELSIM1 && sUriMatcher.match(uri) != CHANNELSIM2) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        android.util.Log.d("===ydg===", "CellBroadcastProvider insert uri="+sUriMatcher.match(uri) +" ,id ="+uri.getLastPathSegment());

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Long now = Long.valueOf(System.currentTimeMillis());
        long rowId = 0;
        Uri muri = null;
        // Make sure that the fields are all set
        switch (sUriMatcher.match(uri)) {
            case CBS:
                if (values.containsKey(Cellbroadcasts.CREATED) == false) {
                    values.put(Cellbroadcasts.CREATED, now);
                }
                rowId = db.insert(CB_TABLE_NAME, Cellbroadcasts.MESSAGE, values);
                muri = Cellbroadcasts.CONTENT_URI;
                break;
            case CHANNEL:
                if (values.containsKey(Channel.CREATED) == false) {
                    values.put(Channel.CREATED, now);
                }
                rowId = db.insert(CHANNEL_TABLE_NAME, Channel.INDEX, values);
                muri = Channel.CONTENT_URI;
                break;
            case CBLANGUAGE:
                rowId = db.insert(CBLANGUAGE_TABLE_NAME, CBLanguage.CBLANGUAGE, values);
                muri = CBLanguage.CONTENT_URI;
                break;
            case CHANNELSIM1:
                if (values.containsKey(Channel.CREATED) == false) {
                    values.put(Channel.CREATED, now);
                }
                rowId = db.insert(CHANNELSIM1_TABLE_NAME, Channel.INDEX, values);
                muri = Channel.CONTENT_URISIM1;
                break;
            case CBLANGUAGESIM1:
                rowId = db.insert(CBLANGUAGESIM1_TABLE_NAME, CBLanguage.CBLANGUAGE, values);
                muri = CBLanguage.CONTENT_URISIM1;
                break;
            case CHANNELSIM2:
                if (values.containsKey(Channel.CREATED) == false) {
                    values.put(Channel.CREATED, now);
                }
                rowId = db.insert(CHANNELSIM2_TABLE_NAME, Channel.INDEX, values);
                muri = Channel.CONTENT_URISIM2;
                break;
            case CBLANGUAGESIM2:
                rowId = db.insert(CBLANGUAGESIM2_TABLE_NAME, CBLanguage.CBLANGUAGE, values);
                muri = CBLanguage.CONTENT_URISIM2;
                break;
            //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
            //Tone needed for CellBroadcast message received
            case CBRINGTONE:
                rowId = db.insert(CBRINGTONE_TABLE_NAME, CBRingtone.CBRINGTONE, values);
                muri = CBRingtone.CONTENT_URI;
                break;
            //[FEATURE]-Add-END by TCTNB.bo.xu
        }

        if (rowId > 0) {
            Uri CBUri = ContentUris.withAppendedId(muri, rowId);
            getContext().getContentResolver().notifyChange(CBUri, null);
            return CBUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        boolean cbFlag = false;
        android.util.Log.d("===ydg===", "CellBroadcastProvider delete uri="+sUriMatcher.match(uri) +" ,id ="+uri.getLastPathSegment());
        switch (sUriMatcher.match(uri)) {
            case CBS:
                count = db.delete(CB_TABLE_NAME, where, whereArgs);
                break;

            case CB_ID:
                String MesId = uri.getPathSegments().get(1);
                if (whereArgs != null && whereArgs[0].equals("cblist")) {
                    // fresh cb mes list screen
                    cbFlag = true;
                    count = db.delete(CB_TABLE_NAME,
                            Cellbroadcasts._ID + "=" + MesId
                                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                            null);
                } else {
                    count = db.delete(CB_TABLE_NAME,
                            Cellbroadcasts._ID + "=" + MesId
                                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                            whereArgs);
                }

                break;
            case CHANNEL:
                count = db.delete(CHANNEL_TABLE_NAME, where, whereArgs);
                break;
            case CHANNELSIM1:
                count = db.delete(CHANNELSIM1_TABLE_NAME, where, whereArgs);
                break;
            case CHANNELSIM2:
                count = db.delete(CHANNELSIM2_TABLE_NAME, where, whereArgs);
                break;
            case CHANNEL_ID:
                String chId = uri.getPathSegments().get(1);
                count = db.delete(CHANNEL_TABLE_NAME,
                        Channel._ID + "=" + chId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;
            case CHANNELSIM1_ID:
                String chIdsim1 = uri.getLastPathSegment();
                count = db.delete(CHANNELSIM1_TABLE_NAME,
                                  Channel._ID + "=" + chIdsim1
                                  + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),whereArgs);
                break;
            case CHANNELSIM2_ID:
                String chIdsim2 = uri.getLastPathSegment();
                count = db.delete(CHANNELSIM2_TABLE_NAME,
                                  Channel._ID + "=" + chIdsim2
                                  + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),whereArgs);
                break;
            case CBLANGUAGE:
                count = db.delete(CBLANGUAGE_TABLE_NAME, where, whereArgs);
                break;
            case CBLANGUAGESIM1:
                count = db.delete(CBLANGUAGESIM1_TABLE_NAME, where, whereArgs);
                break;
            case CBLANGUAGESIM2:
                count = db.delete(CBLANGUAGESIM2_TABLE_NAME, where, whereArgs);
                break;
            //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
            //Tone needed for CellBroadcast message received
            case CBRINGTONE:
                count = db.delete(CBRINGTONE_TABLE_NAME, where, whereArgs);
                break;
            //[FEATURE]-Add-END by TCTNB.bo.xu
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (cbFlag) {
            // fresh cb mes list screen
            getContext().getContentResolver().notifyChange(
                    CellBroadcast.SpecialURI.CONTENT_URI_CBCH, null);
        } else {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        android.util.Log.d("===ydg===", "CellBroadcastProvider update uri="+sUriMatcher.match(uri) +" ,id ="+uri.getLastPathSegment());
        switch (sUriMatcher.match(uri)) {
            case CBS:
                count = db.update(CB_TABLE_NAME, values, where, whereArgs);
                break;

            case CB_ID:
                String MesId = uri.getPathSegments().get(1);
                count = db.update(CB_TABLE_NAME, values, Cellbroadcasts._ID + "=" + MesId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            case CHANNEL:
                count = db.update(CHANNEL_TABLE_NAME, values, where, whereArgs);
                break;
            case CHANNEL_ID:
                String chId = uri.getPathSegments().get(1);
                count = db.update(CHANNEL_TABLE_NAME, values, Channel._ID + "=" + chId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            case CBLANGUAGE:
                count = db.delete(CBLANGUAGE_TABLE_NAME, where, whereArgs);
                break;
            case CBLANGUAGE_ID:
                String lanId = uri.getPathSegments().get(1);
                count = db.delete(CBLANGUAGE_TABLE_NAME,
                        CBLanguage._ID + "=" + lanId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;
            case CHANNELSIM1:
                count = db.update(CHANNELSIM1_TABLE_NAME, values, where, whereArgs);
                break;
            case CHANNELSIM1_ID:
                String chId1 = uri.getLastPathSegment();
                count = db.update(CHANNELSIM1_TABLE_NAME, values, Channel._ID + "=" + chId1
                                  + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            case CBLANGUAGESIM1:
                count = db.delete(CBLANGUAGESIM1_TABLE_NAME, where, whereArgs);
                break;
            case CBLANGUAGESIM1_ID:
                String chId2 = uri.getLastPathSegment();
                count = db.delete(CBLANGUAGESIM1_TABLE_NAME,
                                  CBLanguage._ID + "=" + chId2
                                   + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),whereArgs);
                break;
            case CHANNELSIM2:
                count = db.update(CHANNELSIM2_TABLE_NAME, values, where, whereArgs);
                break;
            case CHANNELSIM2_ID:
                String chId3 = uri.getLastPathSegment();
                count = db.update(CHANNELSIM2_TABLE_NAME, values, Channel._ID + "=" + chId3
                                 + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            case CBLANGUAGESIM2:
                count = db.delete(CBLANGUAGESIM2_TABLE_NAME, where, whereArgs);
                break;
            case CBLANGUAGESIM2_ID:
                String chId4 = uri.getLastPathSegment();
                count = db.delete(CBLANGUAGESIM2_TABLE_NAME,
                                  CBLanguage._ID + "=" + chId4+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
            //Tone needed for CellBroadcast message received
            case CBRINGTONE:
                count = db.delete(CBRINGTONE_TABLE_NAME, where, whereArgs);
                break;
            case CBRINGTONE_ID:
                String ringId = uri.getPathSegments().get(1);
                count = db.delete(CBRINGTONE_TABLE_NAME,
                        CBRingtone._ID + "=" + ringId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;
            //[FEATURE]-Add-END by TCTNB.bo.xu
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // CELL BROADCAST
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "Cellbroadcasts", CBS);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "Cellbroadcasts/#", CB_ID);

        // CHANNEL
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "Channel", CHANNEL);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "Channel/#", CHANNEL_ID);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "Channel/sub0", CHANNELSIM1);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "Channel/sub0/#", CHANNELSIM1_ID);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "Channel/sub1", CHANNELSIM2);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "Channel/sub1/#", CHANNELSIM2_ID);

        // SPECIAL URI

        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "cbch", CONTENT_URI_CBCH);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "cbch/#", CONTENT_URI_CBCH_ID);

        // CBLANGUAGE
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "CBLanguage", CBLANGUAGE);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "CBLanguage/#", CBLANGUAGE_ID);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "CBLanguage/sub0", CBLANGUAGESIM1);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "CBLanguage/sub0/#", CBLANGUAGESIM1_ID);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "CBLanguage/sub1", CBLANGUAGESIM2);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "CBLanguage/sub1/#", CBLANGUAGESIM2_ID);
        //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
        //Tone needed for CellBroadcast message received
        // CBRINGTONE
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "CBRingtone", CBRINGTONE);
        sUriMatcher.addURI(CellBroadcast.AUTHORITY, "CBRingtone/#", CBRINGTONE_ID);
        //[FEATURE]-Mod-END by TCTNB.bo.xu

        // CHELL BROADCAST
        sBroadcastMap = new HashMap<String, String>();
        sBroadcastMap.put(Cellbroadcasts._ID, Cellbroadcasts._ID);
        sBroadcastMap.put(Cellbroadcasts.MESSAGE, Cellbroadcasts.MESSAGE);
        sBroadcastMap.put(Cellbroadcasts.CHANNEL, Cellbroadcasts.CHANNEL);
        sBroadcastMap.put(Cellbroadcasts.CREATED, Cellbroadcasts.CREATED);

        // SPECIAL MAP
        sPecialMap = new HashMap<String, String>();
        sPecialMap.put(Channel.NAME, Channel.NAME);// for select channel.name
                                                   // from Cellbroadcasts left
                                                   // outer join Channel
        sPecialMap.put(Cellbroadcasts._ID, CB_TABLE_NAME + "." + Cellbroadcasts._ID + " AS "
                + Cellbroadcasts._ID);
        sPecialMap.put(Cellbroadcasts.MESSAGE, Cellbroadcasts.MESSAGE);
        sPecialMap.put(Cellbroadcasts.CHANNEL, Cellbroadcasts.CHANNEL);

        // CHANNEL
        sChannelMap = new HashMap<String, String>();
        sChannelMap.put(Channel._ID, Channel._ID);
        sChannelMap.put(Channel.INDEX, Channel.INDEX);
        sChannelMap.put(Channel.NAME, Channel.NAME);
        sChannelMap.put(Channel.Enable, Channel.Enable);
        sChannelMap.put(Channel.CREATED, Channel.CREATED);

        sChannelSim1Map = new HashMap<String, String>();
        sChannelSim1Map.put(Channel._ID, Channel._ID);
        sChannelSim1Map.put(Channel.INDEX, Channel.INDEX);
        sChannelSim1Map.put(Channel.NAME, Channel.NAME);
        sChannelSim1Map.put(Channel.Enable, Channel.Enable);
        sChannelSim1Map.put(Channel.CREATED, Channel.CREATED);
        sChannelSim2Map = new HashMap<String, String>();
        sChannelSim2Map.put(Channel._ID, Channel._ID);
        sChannelSim2Map.put(Channel.INDEX, Channel.INDEX);
        sChannelSim2Map.put(Channel.NAME, Channel.NAME);
        sChannelSim2Map.put(Channel.Enable, Channel.Enable);
        sChannelSim2Map.put(Channel.CREATED, Channel.CREATED);
        // CBLANGUAGE
        sLanguageMap = new HashMap<String, String>();
        sLanguageMap.put(CBLanguage._ID, CBLanguage._ID);
        sLanguageMap.put(CBLanguage.CBLANGUAGE, CBLanguage.CBLANGUAGE);

        sLanguageSim1Map = new HashMap<String, String>();
        sLanguageSim1Map.put(CBLanguage._ID, CBLanguage._ID);
        sLanguageSim1Map.put(CBLanguage.CBLANGUAGE, CBLanguage.CBLANGUAGE);
        sLanguageSim2Map = new HashMap<String, String>();
        sLanguageSim2Map.put(CBLanguage._ID, CBLanguage._ID);
        sLanguageSim2Map.put(CBLanguage.CBLANGUAGE, CBLanguage.CBLANGUAGE);
        //[FEATURE]-Add-BEGIN by TCTNB.bo.xu,04/09/2013,FR-399953,
        //Tone needed for CellBroadcast message received
        // CBRINGTONE
        sRingtoneMap = new HashMap<String, String>();
        sRingtoneMap.put(CBRingtone._ID, CBRingtone._ID);
        sRingtoneMap.put(CBRingtone.CBRINGTONE, CBRingtone.CBRINGTONE);
        //[FEATURE]-Mod-END by TCTNB.bo.xu
    }
}
