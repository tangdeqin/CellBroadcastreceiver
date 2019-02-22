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

package com.android.settings.datausage;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.PreferenceViewHolder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.CustomDialogPreference;

import java.util.List;
//Begin added by miaoliu for XRP10024746 on 2018/10/22
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
//End added by miaoliu for XRP10024746 on 2018/10/22
import com.android.settings.sim.tct.TclInterfaceAdapter;//Added by miaoliu for XR7137438 on 2018/11/21

public class CellDataPreference extends CustomDialogPreference implements TemplatePreference {

    private static final String TAG = "CellDataPreference";

    public int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    public boolean mChecked;
    public boolean mMultiSimDialog;
    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;
	//begin tao.ning.sz for XR P10029069 20190108
    public interface UpdateSummaryListener {
        void refreshUi();
    }
    public UpdateSummaryListener updateSummaryListener;
	//end tao.ning.sz for XR P10029069 20190108
    public CellDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context,
                android.support.v7.preference.R.attr.switchPreferenceStyle,
                android.R.attr.switchPreferenceStyle));
    }

    @Override
    protected void onRestoreInstanceState(Parcelable s) {
        CellDataState state = (CellDataState) s;
        super.onRestoreInstanceState(state.getSuperState());
        mTelephonyManager = TelephonyManager.from(getContext());
        mChecked = state.mChecked;
        mMultiSimDialog = state.mMultiSimDialog;
        if (mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            mSubId = state.mSubId;
            setKey(getKey() + mSubId);
        }
        notifyChanged();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        CellDataState state = new CellDataState(super.onSaveInstanceState());
        state.mChecked = mChecked;
        state.mMultiSimDialog = mMultiSimDialog;
        state.mSubId = mSubId;
        return state;
    }

    @Override
    public void onAttached() {
        super.onAttached();
        mListener.setListener(true, mSubId, getContext());
         //Begin added by miaoliu for XRP10024746 on 2018/10/22
        mIsAirplaneModeOn = isAirplaneModeOn(getContext());
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        getContext().registerReceiver(mReceiver, intentFilter);
        updateScreenEnableState();
        //End added by miaoliu for XRP10024746 on 2018/10/22
    }

    @Override
    public void onDetached() {
        mListener.setListener(false, mSubId, getContext());
        super.onDetached();
         //Begin added by miaoliu for XRP10024746 on 2018/10/22
        getContext().unregisterReceiver(mReceiver);
         //End added by miaoliu for XRP10024746 on 2018/10/22
    }

    @Override
    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        if (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            throw new IllegalArgumentException("CellDataPreference needs a SubscriptionInfo");
        }
        mSubscriptionManager = SubscriptionManager.from(getContext());
        mTelephonyManager = TelephonyManager.from(getContext());
        if (mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            mSubId = subId;
            setKey(getKey() + subId);
        }
        updateChecked();
    }

    private void updateChecked() {
        //[TCT ROM][Settings]Begin Add by zhouchenglin NBTelCode for BUG7261059 on 2019/01/03
        boolean isChecked = false;
        int defaultSubid = SubscriptionManager.getDefaultDataSubscriptionId();

        if (defaultSubid != -1 && defaultSubid ==  mSubId) {
            isChecked = mTelephonyManager.getDataEnabled(mSubId);
        }

        Log.d(TAG, "defaultSubid: " + defaultSubid + ", mSubId: " + mSubId);
        Log.d(TAG, "isChecked: " + isChecked + ", getdataenabled: " + mTelephonyManager.getDataEnabled(mSubId));

        setChecked(isChecked);
        //[TCT ROM][Settings]End Add by zhouchenglin  NBTelCode for BUG7261059 on 2019/01/03
    }

    @Override
    protected void performClick(View view) {
        final Context context = getContext();
        FeatureFactory.getFactory(context).getMetricsFeatureProvider()
                .action(context, MetricsEvent.ACTION_CELL_DATA_TOGGLE, !mChecked);
        final SubscriptionInfo currentSir = mSubscriptionManager.getActiveSubscriptionInfo(
                mSubId);
        final SubscriptionInfo nextSir = mSubscriptionManager.getDefaultDataSubscriptionInfo();
        if (mChecked) {
            // If the device is single SIM or is enabling data on the active data SIM then forgo
            // the pop-up.
            if (!Utils.showSimCardTile(getContext()) ||
                    (nextSir != null && currentSir != null &&
                            currentSir.getSubscriptionId() == nextSir.getSubscriptionId())) {
                setMobileDataEnabled(false);
                if (nextSir != null && currentSir != null &&
                        currentSir.getSubscriptionId() == nextSir.getSubscriptionId()) {
                    disableDataForOtherSubscriptions(mSubId);
                }
                return;
            }
            // disabling data; show confirmation dialog which eventually
            // calls setMobileDataEnabled() once user confirms.
            mMultiSimDialog = false;
            super.performClick(view);
        } else {
            // If we are showing the Sim Card tile then we are a Multi-Sim device.
            if (Utils.showSimCardTile(getContext())) {
                mMultiSimDialog = true;
                if (nextSir != null && currentSir != null &&
                        currentSir.getSubscriptionId() == nextSir.getSubscriptionId()) {
                    //Begin added by miaoliu for XRP23836 on 2018/10/16
                     if (isDataWaringEnabled()) {
                         handleSingleSimDataDialog();
                         return;
                     }
                    //End added by miaoliu for XRP23836 on 2018/10/16
                    //Begin added by ruihua.zhang.hz for tclPlugin telecomcode on 2018/08/21
                    if (isNeedtoShowRoamingMsg()) {
                        showConfirmEnableDataDialog();
                        return;
                    }
                    //End added by ruihua.zhang.hz for tclPlugin telecomcode on 2018/08/21
                    setMobileDataEnabled(true);
                    disableDataForOtherSubscriptions(mSubId);
                    return;
                }
                super.performClick(view);
            } else {
                //Begin added by ruihua.zhang.hz for tclPlugin telecomcode on 2018/08/21
                if (whetherShowReminder()) {
                    return;
                }
                //End added by ruihua.zhang.hz for tclPlugin telecomcode on 2018/08/21
                setMobileDataEnabled(true);
            }
        }
    }

    private void setMobileDataEnabled(boolean enabled) {
        if (DataUsageSummary.LOGD) Log.d(TAG, "setMobileDataEnabled(" + enabled + ","
                + mSubId + ")");
        mTelephonyManager.setDataEnabled(mSubId, enabled);
        setChecked(enabled);
    }

    private void setChecked(boolean checked) {
        if (mChecked == checked) return;
        mChecked = checked;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(android.R.id.switch_widget);
        switchView.setClickable(false);
        ((Checkable) switchView).setChecked(mChecked);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder,
            DialogInterface.OnClickListener listener) {
        if (mMultiSimDialog) {
            showMultiSimDialog(builder, listener);
        } else {
            showDisableDialog(builder, listener);
        }
    }

    private void showDisableDialog(AlertDialog.Builder builder,
            DialogInterface.OnClickListener listener) {
        builder.setTitle(null)
                .setMessage(R.string.data_usage_disable_mobile)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, null);
    }

    private void showMultiSimDialog(AlertDialog.Builder builder,
            DialogInterface.OnClickListener listener) {
        final SubscriptionInfo currentSir = mSubscriptionManager.getActiveSubscriptionInfo(mSubId);
        final SubscriptionInfo nextSir = mSubscriptionManager.getDefaultDataSubscriptionInfo();
       //Begin modified by miaoliu for XRP10030101 on 2019/1/24
        final String previousName = (nextSir == null)
            ? getContext().getResources().getString(R.string.sim_selection_required_pref)
            : nextSir.getDisplayName().toString();

        builder.setTitle(R.string.sim_change_data_title);
        
        final String currentName = String.valueOf(currentSir != null ? currentSir.getDisplayName() : null);
        if(previousName.equals(currentName)){
              int currentPhoneId = SubscriptionManager.getPhoneId(mSubId);
              int nextPhoneId = SubscriptionManager.getPhoneId(mSubscriptionManager.getDefaultDataSubscriptionId());
              builder.setMessage(getContext().getString(R.string.sim_change_data_message,
                (currentName + " " + (currentPhoneId + 1)), previousName + " " + (nextPhoneId + 1)));  
        }else{
           builder.setMessage(getContext().getString(R.string.sim_change_data_message, currentName,
                previousName)); 
        }
        //End modified by miaoliu for XRP10030101 on 2019/1/24
        builder.setPositiveButton(R.string.okay, listener);
        builder.setNegativeButton(R.string.cancel, null);
    }

    private void disableDataForOtherSubscriptions(int subId) {
        List<SubscriptionInfo> subInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                if (subInfo.getSubscriptionId() != subId) {
                    mTelephonyManager.setDataEnabled(subInfo.getSubscriptionId(), false);
                }
            }
        }
    }

    @Override
    protected void onClick(DialogInterface dialog, int which) {
        if (which != DialogInterface.BUTTON_POSITIVE) {
            return;
        }
        if (mMultiSimDialog) {
            //Begin added by ruihua.zhang.hz for tclPlugin telecomcode on 2018/08/21
            if (whetherShowReminder()) {
                return;
            }
            //End added by ruihua.zhang.hz for tclPlugin telecomcode on 2018/08/21
            mSubscriptionManager.setDefaultDataSubId(mSubId);
            setMobileDataEnabled(true);
            disableDataForOtherSubscriptions(mSubId);
			//begin tao.ning.sz for XR P10029069 20190108
            if(updateSummaryListener != null){
                updateSummaryListener.refreshUi();
            }
			//end tao.ning.sz for XR P10029069 20190108
        } else {
            // TODO: extend to modify policy enabled flag.
            setMobileDataEnabled(false);
        }
    }

    private final DataStateListener mListener = new DataStateListener() {
        @Override
        public void onChange(boolean selfChange) {
            updateChecked();
        }
    };

    public abstract static class DataStateListener extends ContentObserver {
        public DataStateListener() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void setListener(boolean listening, int subId, Context context) {
            if (listening) {
                Uri uri = Global.getUriFor(Global.MOBILE_DATA);
                if (TelephonyManager.getDefault().getSimCount() != 1) {
                    uri = Global.getUriFor(Global.MOBILE_DATA + subId);
                }
                context.getContentResolver().registerContentObserver(uri, false, this);
            } else {
                context.getContentResolver().unregisterContentObserver(this);
            }
        }
    }

    public static class CellDataState extends BaseSavedState {
        public int mSubId;
        public boolean mChecked;
        public boolean mMultiSimDialog;

        public CellDataState(Parcelable base) {
            super(base);
        }

        public CellDataState(Parcel source) {
            super(source);
            mChecked = source.readByte() != 0;
            mMultiSimDialog = source.readByte() != 0;
            mSubId = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) (mChecked ? 1 : 0));
            dest.writeByte((byte) (mMultiSimDialog ? 1 : 0));
            dest.writeInt(mSubId);
        }

        public static final Creator<CellDataState> CREATOR = new Creator<CellDataState>() {
            @Override
            public CellDataState createFromParcel(Parcel source) {
                return new CellDataState(source);
            }

            @Override
            public CellDataState[] newArray(int size) {
                return new CellDataState[size];
            }
        };
    }

    //Begin added by ruihua.zhang.hz for tclPlugin telecomcode on 2018/08/21
    interface ITclDataCallBack {
        void setCheckedCallBack(boolean checked);
    }

    ITclDataCallBack mITclDataCallBack = new ITclDataCallBack() {
        @Override
        public void setCheckedCallBack(boolean checked) {
            setChecked(checked);
        }
    };

    /**
     * Get the data roaming status of specific sim card
     *
     * @param subId indicates the sim card
     * @return boolean value indicate sim card roaming status
     */
    private boolean getDataRoaming() {
        ContentResolver resolver = getContext().getContentResolver();
        Boolean result = false;
            if (mTelephonyManager.getSimCount() == 1) {
                result =  Settings.Global.getInt(resolver, Settings.Global.DATA_ROAMING, 0) != 0;
            } else {
                result =  Settings.Global.getInt(resolver, Settings.Global.DATA_ROAMING + mSubId, 0) != 0;//zhixiong TelephonyManager.getIntWithSubId(resolver, Settings.Global.DATA_ROAMING, mSubId) != 0;
            }
        return result;
    }

    /**
     * To decide whether to show roaming message or not
     *
     * @param subId indicate specific sim card
     * @return boolean value indicate whether to show roaming message
     */
    private boolean isNeedtoShowRoamingMsg() {
        boolean isInRoaming = mTelephonyManager.isNetworkRoaming(mSubId);
        boolean isRoamingEnabled = getDataRoaming();
        Log.d(TAG, "isInRoaming=" + isInRoaming + " isRoamingEnabled=" + isRoamingEnabled);

        return (isInRoaming && !isRoamingEnabled);
    }

    /**
     * Return boolean value that whether show waring message or not when enable data
     *
     * @return boolean value
     */
    private boolean isDataWaringEnabled() {
        //Begin modified by miaoliu for XR7119445 on 2018/11/17
        boolean bValue = Utils.getBoolean(getContext(), "feature_tctfw_data_enalbewarning_on", "com.tct");
        //End modified by miaoliu for XR7119445 on 2018/11/17
        Log.d(TAG, "feature_tctfw_data_enalbewarning_on=" + bValue);
        return bValue;
    }

    /**
     * whether to show warning dialog when enable mobile data
     *
     * @return true if data warning enable or need to show roaming message
     */
    private boolean whetherShowReminder() {
        if (isNeedtoShowRoamingMsg() || isDataWaringEnabled()) {
            handleClick();
            return true;
        } else {
            return false;
        }
    }

    /**
     * handle the click action of data switch in data usage
     *
     */
    private void handleClick() {
        if (isDataWaringEnabled()) {
            handleSingleSimDataDialog();
        } else if (isNeedtoShowRoamingMsg()) {
            showConfirmEnableDataDialog();
        }
    }


    /**
     * show dialog when enable data for single sim card
     *
     */
    private void handleSingleSimDataDialog() {
        final int subId = mSubId;
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(getContext());
        final SubscriptionInfo currentSir = subscriptionManager.getActiveSubscriptionInfo(subId);
        Log.d(TAG, "handleSingleSimDataDialog, subId=" + subId + ", currentSir=" + currentSir);

        if (currentSir == null) {
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setTitle(getContext().getResources().getString(R.string.sim_change_data_title))
                .setMessage(getContext().getResources().getString(R.string.data_usage_enable_warning))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int defaultDataSubId = subscriptionManager.getDefaultDataSubscriptionId();
                        if (mTelephonyManager.getSimCount() > 1 && (defaultDataSubId != subId)) {
                            subscriptionManager.setDefaultDataSubId(subId);
                            disableDataForOtherSubscriptions(subId);
                        }
                        mTelephonyManager.setDataEnabled(subId, true);
                        mITclDataCallBack.setCheckedCallBack(true);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
        //WRAP_CONTENT
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    /**
     * Show attention dialog before enable data when roaming
     *
     */
    private void showConfirmEnableDataDialog() {
        final int subId = mSubId;
        final ContentResolver resolver = getContext().getContentResolver();
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(getContext());
        Log.d(TAG, "showConfirmEnableDataDialog, subId=" + subId);

        AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setTitle(android.R.string.dialog_alert_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getContext().getResources().getString(R.string.gemini_3g_disable_warning_case0))
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SubscriptionManager.from(getContext()).setDataRoaming(1, subId);
                                // Sigle sim card version, no need to set data roaming status with subId
                                if (TelephonyManager.getDefault().getSimCount() == 1) {
                                    Settings.Global.putInt(resolver, Settings.Global.DATA_ROAMING, 1);
                                } else {
                                    Settings.Global.putInt(resolver, Settings.Global.DATA_ROAMING + subId, 1);
                                }

                                int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
                                if (mTelephonyManager.getSimCount() > 1 && (defaultDataSubId != subId)) {
                                    subscriptionManager.setDefaultDataSubId(subId);
                                    disableDataForOtherSubscriptions(subId);
                                }
                                mTelephonyManager.setDataEnabled(subId, true);
                                mITclDataCallBack.setCheckedCallBack(true);
                            }

                        })
                .setNegativeButton(android.R.string.cancel, null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        //WRAP_CONTENT
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    //End added by ruihua.zhang.hz for tclPlugin telecomcode on 2018/08/21

    //Begin added by miaoliu for XRP10024746 on 2018/10/22
    private boolean mIsAirplaneModeOn;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

       @Override
       public void onReceive(Context context, Intent intent) {

           String action = intent.getAction();

           if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
              mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
              updateScreenEnableState();
           } 
       }
   };

   private void updateScreenEnableState() {
       //Begin modified by miaoliu for XR7107006 on 2019/1/4
        //setEnabled(!mIsAirplaneModeOn );
        setEnabled(!mIsAirplaneModeOn && TclInterfaceAdapter.isRadioOn(mSubId, getContext()));
        //End modified by miaoliu for XR7107006 on 2019/1/4
   }

   private boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
    //End added by miaoliu for XRP10024746 on 2018/10/22


//begin tao.ning.sz for XR P10029069 20190108
    public void setClickListener(UpdateSummaryListener updateSummaryListener){
        this.updateSummaryListener = updateSummaryListener;
    }
//end tao.ning.sz for XR P10029069 20190108
}