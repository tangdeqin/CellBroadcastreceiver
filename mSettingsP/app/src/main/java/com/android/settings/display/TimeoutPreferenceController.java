/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.display;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;//add by yeqing.lv for XR-7218230 CtsVerifier
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.TimeoutListPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;//add by yeqing.lv for XR-7218230 CtsVerifiern
import com.android.settingslib.core.AbstractPreferenceController;
//Begin added by miaoliu for XRP10028602 on 2019/1/3
import android.database.ContentObserver;
import android.os.Handler;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.core.lifecycle.events.OnPause;
import android.support.v7.preference.PreferenceScreen;
import android.os.Looper;
//End added by miaoliu for XRP10028602 on 2019/1/3
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

public class TimeoutPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause{//Modified by miaoliu for XRP10028602 on 2019/1/3

    private static final String TAG = "TimeoutPrefContr";

    /** If there is no setting in the provider, use this. */
    public static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private final String mScreenTimeoutKey;
     private TimeoutListPreference mTimeoutPref;
    public TimeoutPreferenceController(Context context, String key) {
        super(context);
        mScreenTimeoutKey = key;
    }
    //Begin added by miaoliu for XRP10028602 on 2019/1/3
    public TimeoutPreferenceController(Context context, String key, Lifecycle lifecycle) {
        super(context);
        mScreenTimeoutKey = key;
        //Begin modified by miaoliu for XRP10028602 on 2019/1/9
        if(lifecycle != null){
          lifecycle.addObserver(this);
        }
        //End modified by miaoliu for XRP10028602 on 2019/1/9
    }
    //End added by miaoliu for XRP10028602 on 2019/1/3

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return mScreenTimeoutKey;
    }

    @Override
    public void updateState(Preference preference) {
        final TimeoutListPreference timeoutListPreference = (TimeoutListPreference) preference;
        final long currentTimeout = Settings.System.getLong(mContext.getContentResolver(),
                SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
        timeoutListPreference.setValue(String.valueOf(currentTimeout));
        final DevicePolicyManager dpm = (DevicePolicyManager) mContext.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        if (dpm != null) {
            final RestrictedLockUtils.EnforcedAdmin admin =
                    RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(mContext);
            final long maxTimeout =
                    dpm.getMaximumTimeToLock(null /* admin */, UserHandle.myUserId());//zhixiong.liu may check func
            timeoutListPreference.removeUnusableTimeouts(maxTimeout, admin);
        }
        updateTimeoutPreferenceDescription(timeoutListPreference, currentTimeout);
        //add by yeqing.lv for XR-7218230 CtsVerifier on 2018-12-13 begin
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(
                        mContext, UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT,
                        UserHandle.myUserId());
        if(admin != null) {
            timeoutListPreference.removeUnusableTimeouts(0/* disable all*/, admin);
        }
        //add by yeqing.lv for XR-7218230 CtsVerifier on 2018-12-13 end
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContext.getContentResolver(), SCREEN_OFF_TIMEOUT, value);
            updateTimeoutPreferenceDescription((TimeoutListPreference) preference, value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist screen timeout setting", e);
        }
        return true;
    }

    public static CharSequence getTimeoutDescription(
            long currentTimeout, CharSequence[] entries, CharSequence[] values) {
        if (currentTimeout < 0 || entries == null || values == null
                || values.length != entries.length) {
            return null;
        }

        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (currentTimeout == timeout) {
                return entries[i];
            }
        }
        return null;
    }

    private void updateTimeoutPreferenceDescription(TimeoutListPreference preference,
            long currentTimeout) {
        final CharSequence[] entries = preference.getEntries();
        final CharSequence[] values = preference.getEntryValues();
        final String summary;
        if (preference.isDisabledByAdmin()) {
            summary = mContext.getString(com.android.settings.R.string.disabled_by_policy_title);
        } else if (currentTimeout == 1){//Begin add by miaoliu for XR6104698 on 2018/3/16
            summary = mContext.getString(R.string.screen_timeout_zero_summary);
            //End add by miaoliu for XR6104698 on 2018/3/16
        } else {
            final CharSequence timeoutDescription = getTimeoutDescription(
                    currentTimeout, entries, values);
            summary = timeoutDescription == null
                    ? ""
                    : mContext.getString(R.string.screen_timeout_summary, timeoutDescription);
        }
        preference.setSummary(summary);
    }

    //Begin added by miaoliu for XRP10028602 on 2019/1/3
    @Override
    public void onResume() {
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT), true, mObserver);
    }
    @Override
    public void onPause() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }
    private ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {//Modified by miaoliu for XRP10028602 on 2019/1/9
        @Override
        public void onChange(boolean selfChange) {
            updateState(mTimeoutPref);
        }
    };
    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mTimeoutPref = (TimeoutListPreference) screen.findPreference(mScreenTimeoutKey);
    }
    //End added by miaoliu for XRP10028602 on 2019/1/3

}
