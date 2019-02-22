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

package com.android.settings.bluetooth;

import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.widget.FooterPreference;
import com.android.settingslib.widget.FooterPreferenceMixin;

/**
 * This class adds the device MAC address to a footer.
 */
public class BluetoothDetailsMacAddressController extends BluetoothDetailsController {
    FooterPreferenceMixin mFooterPreferenceMixin;
    FooterPreference mFooterPreference;

    public BluetoothDetailsMacAddressController(Context context,
            PreferenceFragment fragment,
            CachedBluetoothDevice device,
            Lifecycle lifecycle) {
        super(context, fragment, device, lifecycle);
        mFooterPreferenceMixin = new FooterPreferenceMixin(fragment, lifecycle);
    }

    @Override
    protected void init(PreferenceScreen screen) {
        mFooterPreference = mFooterPreferenceMixin.createFooterPreference();
        //[TCT-ROM][BT]Begin modified by weijun.pan for XR7306980 on 2019/01/02
        mFooterPreference.setTitle(mContext.getString(
                    R.string.bluetooth_device_mac_address, "\n" + mCachedDevice.getAddress()));
        //[TCT-ROM][BT]End modified by weijun.pan for XR7306980 on 2019/01/02
    }

    @Override
    protected void refresh() {}

    @Override
    public String getPreferenceKey() {
        return mFooterPreference.getKey();
    }
}