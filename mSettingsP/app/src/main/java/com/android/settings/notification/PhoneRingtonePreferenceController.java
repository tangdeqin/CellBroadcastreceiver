/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.notification;

import android.content.Context;
import android.media.RingtoneManager;
import com.android.settings.Utils;
//[TCT-ROM][SoundDual] Added by nanbing.zou for P23287 on 2018-08-31
import android.os.SystemProperties;
//[TCT-ROM][Sound]Begin added by yang.sun for XR7214612 on 18-12-5
import android.telephony.TelephonyManager;
//[TCT-ROM][Sound]End added by yang.sun for XR7214612 on 18-12-5

public class PhoneRingtonePreferenceController extends RingtonePreferenceControllerBase {

    private static final String KEY_PHONE_RINGTONE = "ringtone";
    private boolean dualSupport = false; //[TCT-ROM][SoundDual] Added by nanbing.zou for P23287 on 2018-08-31

    public PhoneRingtonePreferenceController(Context context) {
        super(context);
//[TCT-ROM][Sound]Begin modified by yang.sun for XR7214612 on 18-12-5
        dualSupport = TelephonyManager.getDefault().getPhoneCount() == 2 ? true:false;//SystemProperties.getBoolean("dualsim.ui.support", false);//[TCT-ROM][SoundDual] Added by nanbing.zou for P23287 on 2018-08-31

//[TCT-ROM][Sound]End modified by yang.sun for XR7214612 on 18-12-5
    }

    @Override
    public String getPreferenceKey() {
        return KEY_PHONE_RINGTONE;
    }

    @Override
    public boolean isAvailable() {
        //[TCT-ROM][SoundDual] Modified by nanbing.zou for P23287 on 2018-08-31 begin
        //return Utils.isVoiceCapable(mContext);
        if (dualSupport) {
            return false;
        } else {
            return true;
        }
        //[TCT-ROM][SoundDual] Modified by nanbing.zou for P23287 on 2018-08-31 end
    }

    @Override
    public int getRingtoneType() {
        return RingtoneManager.TYPE_RINGTONE;
    }
}
