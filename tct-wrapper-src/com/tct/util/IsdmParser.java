/* Copyright (C) 2016 Tcl Corporation Limited */

package com.tct.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;

public class IsdmParser {
    private static final String TAG = "CB/IsdmParser";

    private static final String PATH = "";
    private static final String FILE = "";

    private static final String FRAMEWORK_PATH = "";
    private static final String FRAMEWORK_FILE = "";

    private static final String FRAMEWORK_PKG = "android";
    private static final String CB_PKG = "com.android.cellbroadcastreceiver";

    private static final String TYPE_INTEGER    = "integer";
    private static final String TYPE_BOOL       = "bool";
    private static final String TYPE_STRING     = "string";
    private static final String TYPE_ARRAY      = "array";
    /**
     * get isdm value which is integer
     *
     * @param context
     * @param def_name : the name of isdmID
     * @return
     */
    public static int getInt(Context context, String def_name,
            int defaultValue) {
        int result = defaultValue;

        Resources res = context.getResources();
        int id = res.getIdentifier(def_name, TYPE_INTEGER, CB_PKG);

        // get the native isdmID value
        try {
            result = context.getResources().getInteger(id);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getInt," + def_name + " not found. ");
        }

        return result;
    }

    /**
     * get isdm value which is integer in framework
     *
     * @param context
     * @param def_name : the name of isdmID
     * @return
     */
    public static int getIntFwk(Context context, String def_name,
            int defaultValue) {
        int result = defaultValue;

        Resources res = context.getResources();
        int id = res.getIdentifier(def_name, TYPE_INTEGER, FRAMEWORK_PKG);

        // get the native isdmID value
        try {
            result = context.getResources().getInteger(id);
            Log.d(TAG, "Fwk sdmid:" + def_name + "=" + result);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getIntFwk," + def_name + " not found. ");
        }

        return result;
    }

    /**
     * get int array from framework id
     * @param context
     * @param def_name
     * @return
     */
    public static int[] getIntArrayFwk(String def_name) {
        int[] result = null;

        Resources res = Resources.getSystem();
        int id = res.getIdentifier(def_name, TYPE_ARRAY, FRAMEWORK_PKG);
        Log.v(TAG, "id"+id);
        // get the native isdmID value
        try {
            result = res.getIntArray(id);
            Log.d(TAG, "Fwk sdmid:" + def_name + "=" + result);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getIntFwk," + def_name + " not found. ");
            result = new int[0];
        }

        return result;
    }

    /**
     * get isdm value which is bool
     *
     * @param context
     * @param def_name : the name of isdmID
     * @return
     */
    public static boolean getBoolean(Context context, String def_name,
            boolean defaultValue) {
        boolean result = defaultValue;

        Resources res = context.getResources();
        int id = res.getIdentifier(def_name, TYPE_BOOL, CB_PKG);

        // get the native isdmID value
        try {
            result = res.getBoolean(id);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getBoolean," + def_name + " not found. ");
        }

        return result;
    }

    /**
     * get isdm value which is bool in framework
     *
     * @param context
     * @param def_name : the name of isdmID
     * @return
     */
    public static boolean getBooleanFwk(Context context, String def_name,
            boolean defaultValue) {
        boolean result = defaultValue;

        Resources res = context.getResources();
        int id = res.getIdentifier(def_name, TYPE_BOOL, FRAMEWORK_PKG);

        // get the native isdmID value
        try {
            result = res.getBoolean(id);
            Log.d(TAG, "Fwk sdmid:" + def_name + "=" + result);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getBooleanFwk," + def_name + " not found. ");
        }

        return result;
    }

    /**
     * get isdm value which is string
     *
     * @param context
     * @param def_name : the name of isdmID
     * @return
     */
    public static String getString(Context context, String def_name,
            String defaultValue) {
        String result = defaultValue;

        Resources res = context.getResources();
        int id = res.getIdentifier(def_name, TYPE_STRING, CB_PKG);

        // get the native isdmID value
        try {
            result = context.getResources().getString(id);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getString," + def_name + " not found. ");
        }

        return result;
    }

    /**
     * get isdm value which is string
     *
     * @param context
     * @param def_name : the name of isdmID
     * @return
     */
    public static String getStringFwk(Context context, String def_name,
            String defaultValue) {
        String result = defaultValue;

        Resources res = context.getResources();
        int id = res.getIdentifier(def_name, TYPE_STRING, FRAMEWORK_PKG);

        // get the native isdmID value
        try {
            result = context.getResources().getString(id);
            Log.d(TAG, "Fwk sdmid:" + def_name + "=" + result);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "getStringFwk," + def_name + " not found. ");
        }

        return result;
    }

    /**
     * this seems deprecated
     * parser the XML file to get the isdmID value
     *
     * @param file : xml file
     * @param name : isdmID
     * @param type : isdmID type like bool and string
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String getISDMString(File file, String name, String type)
            throws XmlPullParserException, IOException {
        if (!file.exists() || null == file) {
            Log.w(TAG, "file not exist : " + file);
            return null;
        }
        String result = null;
        InputStream inputStream = new FileInputStream(file);
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setInput(inputStream, "utf-8");

        int evtType = xmlParser.getEventType();
        boolean query_end = false;
        while (evtType != XmlPullParser.END_DOCUMENT && !query_end) {

            switch (evtType) {
                case XmlPullParser.START_TAG:

                    String start_tag = xmlParser.getAttributeValue(null, "name");
                    String start_type = xmlParser.getName();
                    if (null != start_tag && type.equals(start_type) && start_tag.equals(name)) {
                        result = xmlParser.nextText();
                        query_end = true;
                    }
                    break;

                case XmlPullParser.END_TAG:

                    break;

                default:
                    break;
            }
            // move to next node if not tail
            evtType = xmlParser.next();
        }
        inputStream.close();
        return result;
    }

    /**
     * use this to get system properties to make it more easy to handle
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getSystemProperty(String key, String defaultValue) {
        return SystemProperties.get(key, defaultValue);
    }
}
