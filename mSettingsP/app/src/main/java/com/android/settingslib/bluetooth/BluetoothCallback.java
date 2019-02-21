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

package com.android.settingslib.bluetooth;


/**
 * BluetoothCallback provides a callback interface for the settings
 * UI to receive events from {@link BluetoothEventManager}.
 */
public interface BluetoothCallback {
    void onBluetoothStateChanged(int bluetoothState);
    void onScanningStateChanged(boolean started);
    void onDeviceAdded(CachedBluetoothDevice cachedDevice);
    void onDeviceDeleted(CachedBluetoothDevice cachedDevice);
    void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState);
    void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state);
    //Begin added by jiabin.zheng.hz for XR7177433 on 2018/12/11
    void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile);
    void onAudioModeChanged();
    default void onProfileConnectionStateChanged(CachedBluetoothDevice cachedDevice,
            int state, int bluetoothProfile) {
    }
    //End added by jiabin.zheng.hz for XR7177433 on 2018/12/11
}
