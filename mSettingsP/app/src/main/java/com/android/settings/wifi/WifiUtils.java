/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.wifi;

//Begin added by chenglong.cai for XR7437880 on 2019-1-30
import android.annotation.NonNull;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.android.settings.R;
//End added by chenglong.cai for XR7437880 on 2019-1-30
import android.text.TextUtils;




public class WifiUtils {

    private static final int SSID_ASCII_MIN_LENGTH = 1;
    private static final int SSID_ASCII_MAX_LENGTH = 32;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 63;


    public static boolean isSSIDTooLong(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        return ssid.length() > SSID_ASCII_MAX_LENGTH;
    }

    public static boolean isSSIDTooShort(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return true;
        }
        return ssid.length() < SSID_ASCII_MIN_LENGTH;
    }

    public static boolean isPasswordValid(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        final int length = password.length();
        return length >= PASSWORD_MIN_LENGTH && length <= PASSWORD_MAX_LENGTH;
    }
    //Begin added by chenglong.cai for XR7437880 on 2019-1-30
    @NonNull
    public static Drawable getWifiIcon(Context context, int signalLevel, Resources.Theme theme){
        return context.getResources().getDrawable(getWifiSignalResource(signalLevel), theme);
    }
    private static int getWifiSignalResource(int signalLevel){
        switch (signalLevel){
            case 0:
                return R.drawable.ic_wifi_signal_ex_0;
            case 1:
                return R.drawable.ic_wifi_signal_ex_1;
            case 2:
                return R.drawable.ic_wifi_signal_ex_2;
            case 3:
                return R.drawable.ic_wifi_signal_ex_3;
            case 4:
                return R.drawable.ic_wifi_signal_ex_4;
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }
    //End added by chenglong.cai for XR7437880 on 2019-1-30
}
