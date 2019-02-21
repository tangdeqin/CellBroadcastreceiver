package com.android.settings.bluetooth;

/**
 * Impl for bluetooth feature provider
 */
public class BluetoothFeatureProviderImpl implements BluetoothFeatureProvider {

    @Override
    public boolean isPairingPageEnabled() {
        return false;
    }

    @Override
    public boolean isDeviceDetailPageEnabled() {
        return true; //[TCT-ROM][BT]modified by weijun.pan for XR7233513 on 2018/12/27
    }
}
