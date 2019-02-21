/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.settings.nfc;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
// BEGIN XR#5440260 Added by binjian.tu on 2018/03/19
//import android.nfc.cardemulation.ApduServiceInfo;
import com.tct.nfc.VApduServiceInfo;
import com.tct.nfc.VNfcAdapter;
// END XR#5440260 Added by binjian.tu on 2018/03/19
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
// Added by binjian.tu.hz for Task#2701218 on 2016/08/13 BEGIN
import android.preference.PreferenceManager;  
import com.tct.nfc.PaymentAppChangedReceiver; 
import android.content.SharedPreferences;
// Added by binjian.tu.hz for Task#2701218 on 2016/08/13 END

import java.util.ArrayList;
import java.util.List;

public class PaymentBackend {
    public static final String TAG = "Settings.PaymentBackend";

    public interface Callback {
        void onPaymentAppsChanged();
    }

    public static class PaymentAppInfo {
        public CharSequence label;
        CharSequence description;
        Drawable banner;
        boolean isDefault;
        public ComponentName componentName;
        public ComponentName settingsComponent;
    }

    private final Context mContext;
    private final NfcAdapter mAdapter;
    //Modify by weilong.zhou for defect 4342653 begin
    private CardEmulation mCardEmuManager = null;
    private final PackageMonitor mSettingsPackageMonitor = new SettingsPackageMonitor();
    // Fields below only modified on UI thread
    private ArrayList<PaymentAppInfo> mAppInfos;
    private PaymentAppInfo mDefaultAppInfo;
    private ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private boolean mPaused = false;
    private boolean mNeedRefresh = false;

    // BEGIN XR#5440260 Added by binjian.tu on 2018/03/19
    private final VNfcAdapter mVAdapter;
    // END XR#5440260 Added by binjian.tu on 2018/03/19

    public PaymentBackend(Context context) {
        mContext = context;

        mAdapter = NfcAdapter.getDefaultAdapter(context);
        //Modify by weilong.zhou for defect 4342653 begin
        if (mAdapter != null) {
        	mCardEmuManager = CardEmulation.getInstance(mAdapter);
            mVAdapter = VNfcAdapter.instance;
        } else {
            mVAdapter = null;
        }
       //Modify by weilong.zhou for defect 4342653 end
        refresh();
    }

    public void onPause() {
        mPaused = true;
    }

    public void onDestroy() {
        mSettingsPackageMonitor.unregister();
    }

    public void onCreate(Bundle icicle) {
        mSettingsPackageMonitor.register(mContext, mContext.getMainLooper(), false);
        refresh();
    }

    public void onResume() {
        mPaused = false;
        // Added by binjian.tu.hz for Task#4634984 on 2017/05/10 BEGIN
        if (mNeedRefresh || null != needRollbackPaymentApp(true)) {
            refresh();
            mNeedRefresh = false;
        }
        // Added by binjian.tu.hz for Task#4634984 on 2017/05/10 END
    }

    public void refresh() {
        //Add by weilong.zhou for defect 4342653 begin
        if (mAdapter == null) {
            return;
        }
        //Add by weilong.zhou for defect 4342653 end
        PackageManager pm = mContext.getPackageManager();
        /**
         * Use VApduServiceInfo instead for TS.27 15.7.3.1.5
         */
        /*List<ApduServiceInfo> serviceInfos =
                mCardEmuManager.getServices(CardEmulation.CATEGORY_PAYMENT);*/
        List<VApduServiceInfo> serviceInfos = mVAdapter.getVendorServicesInfo(CardEmulation.CATEGORY_PAYMENT);
        ArrayList<PaymentAppInfo> appInfos = new ArrayList<PaymentAppInfo>();

        if (serviceInfos == null) {
            makeCallbacks();
            return;
        }

        ComponentName defaultAppName = getDefaultPaymentApp();
        PaymentAppInfo foundDefaultApp = null;
        //for (ApduServiceInfo service : serviceInfos) {
        for (VApduServiceInfo service : serviceInfos) {
            PaymentAppInfo appInfo = new PaymentAppInfo();
            appInfo.label = service.loadLabel(pm);
            if (appInfo.label == null) {
                appInfo.label = service.loadAppLabel(pm);
            }
            appInfo.isDefault = service.getComponent().equals(defaultAppName);
            if (appInfo.isDefault) {
                foundDefaultApp = appInfo;
            }
            appInfo.componentName = service.getComponent();
            String settingsActivity = service.getSettingsActivityName();
            if (settingsActivity != null) {
                appInfo.settingsComponent = new ComponentName(appInfo.componentName.getPackageName(),
                        settingsActivity);
            } else {
                appInfo.settingsComponent = null;
            }
            appInfo.description = service.getDescription();
            appInfo.banner = service.loadBanner(pm);
            appInfos.add(appInfo);
            Log.d(TAG, appInfo.componentName.toString() + " is added.");
        }
        mAppInfos = appInfos;
        mDefaultAppInfo = foundDefaultApp;
        makeCallbacks();
    }

    public void registerCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    public void unregisterCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public List<PaymentAppInfo> getPaymentAppInfos() {
        return mAppInfos;
    }

    public PaymentAppInfo getDefaultApp() {
        return mDefaultAppInfo;
    }

    void makeCallbacks() {
        for (Callback callback : mCallbacks) {
            callback.onPaymentAppsChanged();
        }
    }

    Drawable loadDrawableForPackage(String pkgName, int drawableResId) {
        PackageManager pm = mContext.getPackageManager();
        try {
            Resources res = pm.getResourcesForApplication(pkgName);
            Drawable banner = res.getDrawable(drawableResId);
            return banner;
        } catch (Resources.NotFoundException e) {
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    boolean isForegroundMode() {
        try {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.NFC_PAYMENT_FOREGROUND) != 0;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    void setForegroundMode(boolean foreground) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.NFC_PAYMENT_FOREGROUND, foreground ? 1 : 0) ;
    }

    ComponentName getDefaultPaymentApp() {
        // Added by binjian.tu.hz for Task#2701218 on 2016/08/13 BEGIN
        ComponentName defaultApp = needRollbackPaymentApp(false);
        if (defaultApp != null) {
            return defaultApp;
        }
        // Added by binjian.tu.hz for Task#2701218 on 2016/08/13 END
        String componentString = getDefaultPaymentAppFromSettings();
        if (componentString != null) {
            return ComponentName.unflattenFromString(componentString);
        } else {
            return null;
        }
    }

    // Added by binjian.tu.hz for Task#4634984 on 2017/05/10 BEGIN
    private boolean isRoutingTableFull() {
        return Settings.Global.getInt(mContext.getContentResolver(), "aid_routing_table_full", 0x00) == 0x01;
    }

    private String getDefaultPaymentAppFromSettings() {
        return Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT);
    }

    private ComponentName needRollbackPaymentApp(boolean setDefaultPaymentAgainIfRouteNotFull) {
        /**
         * Settings overrides getSharedPreferences.
         * When you try to get default shared preference,
         * it will always return a new logger instead of file persistent preference to caller!
         */
        SharedPreferences preferences = mContext.getSharedPreferences(
                PaymentAppChangedReceiver.NFC_PAYMENT_PERSISTENT, Context.MODE_PRIVATE);
        final int currentUser = ActivityManager.getCurrentUser();
        final String rollbackKey = PaymentAppChangedReceiver.ROLLBACK_KEY + currentUser;
        final boolean rollback = preferences.getBoolean(rollbackKey, false);
        ComponentName componentName = null;
        if (rollback) {
            SharedPreferences.Editor e = preferences.edit();
            e.putBoolean(rollbackKey, false);
            e.apply();

            final String previousApp = preferences.getString(
                    PaymentAppChangedReceiver.PREVIOUS_PAYMENT_APP_KEY + currentUser, null);
            /**
             * previousApp can be null!
             */
            if (null != previousApp) {
                componentName = ComponentName.unflattenFromString(previousApp);
                final String now = getDefaultPaymentAppFromSettings();
                setDefaultPaymentAppWithoutRefresh(componentName);

                if (setDefaultPaymentAgainIfRouteNotFull && !isRoutingTableFull()) {
                    Log.d(TAG, "try to set default payment app again by " + now);
                    componentName = ComponentName.unflattenFromString(now);
                    setDefaultPaymentAppWithoutRefresh(componentName);
                    return null;
                }
            }
        }
        return componentName;
    }

    private void setDefaultPaymentAppWithoutRefresh(ComponentName app) {
        Settings.Secure.putString(mContext.getContentResolver(),
                Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT,
                app != null ? app.flattenToString() : null);
    }
    // Added by binjian.tu.hz for Task#4634984 on 2017/05/10 END

    public void setDefaultPaymentApp(ComponentName app) {
        // Modified by binjian.tu.hz for Task#2701218 on 2016/08/13 BEGIN
        // Settings.Secure.putString(mContext.getContentResolver(),
        //        Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT,
        //        app != null ? app.flattenToString() : null);
        setDefaultPaymentAppWithoutRefresh(app);
        // Modified by binjian.tu.hz for Task#2701218 on 2016/08/13 END
        refresh();
    }

    public static final int DELAY_TIME = 1000;
    public static final int MSG_DELAY = 700;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELAY:
                    if (!mPaused) {
                        refresh();
                    } else {
                        mNeedRefresh = true;
                    }
                    break;
            }
        }
    };

    private class SettingsPackageMonitor extends PackageMonitor {

        private void delayPost() {
            Message msg = mHandler.obtainMessage(MSG_DELAY);
            mHandler.sendMessageDelayed(msg, DELAY_TIME);
        }

        @Override
        public void onPackageAdded(String packageName, int uid) {
            //mHandler.obtainMessage().sendToTarget();
        }

        @Override
        public void onPackageAppeared(String packageName, int reason) {
            delayPost();
            //mHandler.obtainMessage().sendToTarget();
        }

        @Override
        public void onPackageDisappeared(String packageName, int reason) {
            delayPost();
            //mHandler.obtainMessage().sendToTarget();
        }

        @Override
        public void onPackageRemoved(String packageName, int uid) {
            //mHandler.obtainMessage().sendToTarget();
        }
    }
}
