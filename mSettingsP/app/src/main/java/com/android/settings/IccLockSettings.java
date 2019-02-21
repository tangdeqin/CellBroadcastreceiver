/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.Toast;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.sim.tct.TclInterfaceAdapter;//Added by lei.ren.hz for P10028458 on 2018/12/27

/**
 * Implements the preference screen to enable/disable ICC lock and
 * also the dialogs to change the ICC PIN. In the former case, enabling/disabling
 * the ICC lock will prompt the user for the current PIN.
 * In the Change PIN case, it prompts the user for old pin, new pin and new pin
 * again before attempting to change it. Calls the SimCard interface to execute
 * these operations.
 *
 */
public class IccLockSettings extends SettingsPreferenceFragment
        implements EditPinPreference.OnPinEnteredListener {
    private static final String TAG = "IccLockSettings";
    private static final boolean DBG = true;

    private static final int OFF_MODE = 0;
    // State when enabling/disabling ICC lock
    private static final int ICC_LOCK_MODE = 1;
    // State when entering the old pin
    private static final int ICC_OLD_MODE = 2;
    // State when entering the new pin - first time
    private static final int ICC_NEW_MODE = 3;
    // State when entering the new pin - second time
    private static final int ICC_REENTER_MODE = 4;

    // Keys in xml file
    private static final String PIN_DIALOG = "sim_pin";
    private static final String PIN_TOGGLE = "sim_toggle";
    // Keys in icicle
    private static final String DIALOG_STATE = "dialogState";
    private static final String DIALOG_PIN = "dialogPin";
    private static final String DIALOG_ERROR = "dialogError";
    private static final String ENABLE_TO_STATE = "enableState";

    // Save and restore inputted PIN code when configuration changed
    // (ex. portrait<-->landscape) during change PIN code
    private static final String OLD_PINCODE = "oldPinCode";
    private static final String NEW_PINCODE = "newPinCode";

    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 8;
    // Which dialog to show next when popped up
    private int mDialogState = OFF_MODE;

    private String mPin;
    private String mOldPin;
    private String mNewPin;
    private String mError;
    // Are we trying to enable or disable ICC lock?
    private boolean mToState;

    private TabHost mTabHost;
    private TabWidget mTabWidget;
    private ListView mListView;

    private Phone mPhone;

    private EditPinPreference mPinDialog;
    private SwitchPreference mPinToggle;

    private Resources mRes;

    // For async handler to identify request type
    private static final int MSG_ENABLE_ICC_PIN_COMPLETE = 100;
    private static final int MSG_CHANGE_ICC_PIN_COMPLETE = 101;
    private static final int MSG_SIM_STATE_CHANGED = 102;

    private int mCurrestTabSlotId = -1;//Added by lei.ren.hz for P10028458 on 2018/12/27
    // For replies from IccCard interface
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            //added by wenjie.chen for XR7175466 on 2018/11/29 begin
            if(MSG_SIM_STATE_CHANGED != msg.what) {
                setRetryCounterByPhone(mPhone.getPhoneId(), msg.arg1);
            }
            //added by wenjie.chen for XR7175466 on 2018/11/29 end
            switch (msg.what) {
                case MSG_ENABLE_ICC_PIN_COMPLETE:
                    iccLockChanged(ar.exception == null, msg.arg1);
                    break;
                case MSG_CHANGE_ICC_PIN_COMPLETE:
                    iccPinChanged(ar.exception == null, msg.arg1);
                    break;
                case MSG_SIM_STATE_CHANGED:
                    updatePreferences();
                    break;
            }

            return;
        }
    };

    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SIM_STATE_CHANGED));
            }
        }
    };

    // For top-level settings screen to query
    static boolean isIccLockEnabled() {
        return PhoneFactory.getDefaultPhone().getIccCard().getIccLockEnabled();
    }

    static String getSummary(Context context) {
        Resources res = context.getResources();
        String summary = isIccLockEnabled()
                ? res.getString(R.string.sim_lock_on)
                : res.getString(R.string.sim_lock_off);
        return summary;
    }

    // BEGIN XR#P10030293 Added by binjian.tu on 2019/01/29
    private static void checkValidPIN(Button b, Editable e) {
        final int len = e.toString().length();
        b.setEnabled(len <= 8 && len >= 4);
    }

    @Override
    public void onResume(AlertDialog dlg) {
        final Button positive = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        if (null == positive) {
            Log.e(TAG, "positive button is null?");
            return;
        }
        EditText ed = mPinDialog.getEditText();
        if (null == ed) {
            Log.e(TAG, "EditText is null?");
            return;
        }
        checkValidPIN(positive, ed.getText());
        ed.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                String a = e.toString();
                checkValidPIN(positive, e);
            }
        });
    }
    // END XR#P10030293 Added by binjian.tu on 2019/01/29

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isMonkeyRunning()) {
            finish();
            return;
        }

        addPreferencesFromResource(R.xml.sim_lock_settings);

        mPinDialog = (EditPinPreference) findPreference(PIN_DIALOG);
        mPinToggle = (SwitchPreference) findPreference(PIN_TOGGLE);
        if (savedInstanceState != null && savedInstanceState.containsKey(DIALOG_STATE)) {
            mDialogState = savedInstanceState.getInt(DIALOG_STATE);
            mPin = savedInstanceState.getString(DIALOG_PIN);
            mError = savedInstanceState.getString(DIALOG_ERROR);
            mToState = savedInstanceState.getBoolean(ENABLE_TO_STATE);

            // Restore inputted PIN code
            switch (mDialogState) {
                case ICC_NEW_MODE:
                    mOldPin = savedInstanceState.getString(OLD_PINCODE);
                    break;

                case ICC_REENTER_MODE:
                    mOldPin = savedInstanceState.getString(OLD_PINCODE);
                    mNewPin = savedInstanceState.getString(NEW_PINCODE);
                    break;

                case ICC_LOCK_MODE:
                case ICC_OLD_MODE:
                default:
                    break;
            }
        }

        mPinDialog.setOnPinEnteredListener(this);

        // Don't need any changes to be remembered
        getPreferenceScreen().setPersistent(false);

        mRes = getResources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final TelephonyManager tm =
                (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        final int numSims = tm.getSimCount();
        if (numSims > 1) {
            View view = inflater.inflate(R.layout.icc_lock_tabs, container, false);
            final ViewGroup prefs_container = (ViewGroup) view.findViewById(R.id.prefs_container);
            Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
            View prefs = super.onCreateView(inflater, prefs_container, savedInstanceState);
            prefs_container.addView(prefs);

            mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
            mTabWidget = (TabWidget) view.findViewById(android.R.id.tabs);
            mListView = (ListView) view.findViewById(android.R.id.list);

            mTabHost.setup();
            mTabHost.setOnTabChangedListener(mTabListener);
            mTabHost.clearAllTabs();

            SubscriptionManager sm = SubscriptionManager.from(getContext());
            for (int i = 0; i < numSims; ++i) {
                final SubscriptionInfo subInfo = sm.getActiveSubscriptionInfoForSimSlotIndex(i);
                mTabHost.addTab(buildTabSpec(String.valueOf(i),
                        String.valueOf(subInfo == null
                            ? getContext().getString(R.string.sim_editor_title, i + 1)
                            : getContext().getString(R.string.sim_diff_tabs,i + 1)+subInfo.getDisplayName())));//modified by lei.ren.hz for P10030198 on 2019/1/28
            }
            final SubscriptionInfo sir = sm.getActiveSubscriptionInfoForSimSlotIndex(0);
            mCurrestTabSlotId = 0;//Added by lei.ren.hz for P10028458 on 2018/12/27
            mPhone = (sir == null) ? null
                : PhoneFactory.getPhone(SubscriptionManager.getPhoneId(sir.getSubscriptionId()));
            return view;
        } else {
            mCurrestTabSlotId = -1;//Added by lei.ren.hz for P10028458 on 2018/12/27
            mPhone = PhoneFactory.getDefaultPhone();
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updatePreferences();
    }

    private void updatePreferences() {
        if (mPinDialog != null) {
            mPinDialog.setEnabled(mPhone != null);
        }
        if (mPinToggle != null) {

            //Begin modified by lei.ren.hz for P10028458 on 2018/12/27
            if(mCurrestTabSlotId == 0 || mCurrestTabSlotId == 1){
                boolean isRadioOn = TclInterfaceAdapter.isRadioOn(SubscriptionManager.getSubId(mCurrestTabSlotId)[0], getContext());
                mPinToggle.setEnabled(mPhone != null && isRadioOn);
            }else{
                mPinToggle.setEnabled(mPhone != null);
            }
            //mPinToggle.setEnabled(mPhone != null);
            //End modified by lei.ren.hz for P10028458 on 2018/12/27

            if (mPhone != null) {
                mPinToggle.setChecked(mPhone.getIccCard().getIccLockEnabled());
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ICC_LOCK;
    }

    @Override
    public void onResume() {
        super.onResume();

        //added by wenjie.chen for XR7307780 on 2019/01/08 begin
        setDefaultRetryCount();
        //added by wenjie.chen for XR7307780 on 2019/01/08 end

        // ACTION_SIM_STATE_CHANGED is sticky, so we'll receive current state after this call,
        // which will call updatePreferences().
        final IntentFilter filter = new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        getContext().registerReceiver(mSimStateReceiver, filter);

        if (mDialogState != OFF_MODE) {
            showPinDialog();
        } else {
            // Prep for standard click on "Change PIN"
            resetDialogState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mSimStateReceiver);
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_icc_lock;
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        // Need to store this state for slider open/close
        // There is one case where the dialog is popped up by the preference
        // framework. In that case, let the preference framework store the
        // dialog state. In other cases, where this activity manually launches
        // the dialog, store the state of the dialog.
        if (mPinDialog.isDialogOpen()) {
            out.putInt(DIALOG_STATE, mDialogState);
            out.putString(DIALOG_PIN, mPinDialog.getEditText().getText().toString());
            out.putString(DIALOG_ERROR, mError);
            out.putBoolean(ENABLE_TO_STATE, mToState);

            // Save inputted PIN code
            switch (mDialogState) {
                case ICC_NEW_MODE:
                    out.putString(OLD_PINCODE, mOldPin);
                    break;

                case ICC_REENTER_MODE:
                    out.putString(OLD_PINCODE, mOldPin);
                    out.putString(NEW_PINCODE, mNewPin);
                    break;

                case ICC_LOCK_MODE:
                case ICC_OLD_MODE:
                default:
                    break;
            }
        } else {
            super.onSaveInstanceState(out);
        }
    }

    private void showPinDialog() {
        if (mDialogState == OFF_MODE) {
            return;
        }
        setDialogValues();

        mPinDialog.showPinDialog();
    }

    //added by wenjie.chen for XR7175466 on 2018/11/29 begin
    private static final int GET_SIM_RETRY_EMPTY = -1;
    private static int mPinRetryCount = GET_SIM_RETRY_EMPTY;
    private static int[] mCounter = new int[]{-1,-1,-1,-1};
    public String getRetryPinString(final int phoneId) {
        mPinRetryCount = getRetryPinCount(phoneId);
        Log.i("Log by Jack", "getRetryPinString: phoneId:" + phoneId + "||mPinRetryCount:"+mPinRetryCount);
        switch (mPinRetryCount) {
            case GET_SIM_RETRY_EMPTY:
                return "";
            default:
                return "(" + mRes.getString(R.string.retries_left, mPinRetryCount) + ")";
        }
    }

    public int getRetryPinCount(final int phoneId) {
        if (phoneId == 3 || phoneId == 2 || phoneId == 1) {
            return mCounter[phoneId];
        } else {
            return mCounter[0];
        }
    }

    public void setRetryCounterByPhone(final int phoneId, int attemptsRemaining){
        Log.i("Log by Jack", "setRetryCounterByPhone: phoneId:"+phoneId + "||attemptsRemaining:"+attemptsRemaining);
        if (phoneId == 3 || phoneId == 2 || phoneId == 1) {
            mCounter[phoneId] = attemptsRemaining;
        } else {
            mCounter[0] = attemptsRemaining;
        }
        resetDialogState();//added by wenjie.chen for XR7307780 on 2019/01/08
    }

    public String customizePinRetryString(String originalMsg) {
        int idf = mRes.getIdentifier("def_keyguard_show_pin_retry_left", "bool", "com.tct");
        Log.i("Log by Jack", "customizePinRetryString: "+idf);
        if (idf!=0 && mRes.getBoolean(idf) && mPhone != null){//add !=null by wenjie.chen for XR7232541 on 2018/12/21
            return originalMsg + " " + getRetryPinString(mPhone.getPhoneId());
        }else {
            return originalMsg;
        }
    }

    public void setDefaultRetryCount(){
        if(mPhone != null) {
            mPhone.getIccCard().supplyPin("", mHandler.obtainMessage());
        }
    }

    //added by wenjie.chen for XR7175466 on 2018/11/29 end

    private void setDialogValues() {
        mPinDialog.setText(mPin);
        String message = "";
        switch (mDialogState) {
            case ICC_LOCK_MODE:
                message = mRes.getString(R.string.sim_enter_pin);
                //modified by wenjie.chen for XR7175466 on 2018/11/29 begin
                /*old mPinDialog.setDialogTitle(mToState
                        ? mRes.getString(R.string.sim_enable_sim_lock)
                        : mRes.getString(R.string.sim_disable_sim_lock));old*/
                mPinDialog.setDialogTitle(customizePinRetryString((mToState
                        ? mRes.getString(R.string.sim_enable_sim_lock)
                        : mRes.getString(R.string.sim_disable_sim_lock))));
                //modified by wenjie.chen for XR7175466 on 2018/11/29 end
                break;
            case ICC_OLD_MODE:
                message = mRes.getString(R.string.sim_enter_old);
                //modified by wenjie.chen for XR7175466 on 2018/11/29 begin
                /*old mPinDialog.setDialogTitle(mRes.getString(R.string.sim_change_pin));old */
                mPinDialog.setDialogTitle(customizePinRetryString(mRes.getString(R.string.sim_change_pin)));
                //modified by wenjie.chen for XR7175466 on 2018/11/29 end
                break;
            case ICC_NEW_MODE:
                message = mRes.getString(R.string.sim_enter_new);
                mPinDialog.setDialogTitle(mRes.getString(R.string.sim_change_pin));
                break;
            case ICC_REENTER_MODE:
                message = mRes.getString(R.string.sim_reenter_new);
                mPinDialog.setDialogTitle(mRes.getString(R.string.sim_change_pin));
                break;
        }
        if (mError != null) {
            message = mError + "\n" + message;
            // BEGIN XR#P10030293 Added by binjian.tu on 2019/02/13
            mError = mRes.getString(R.string.sim_invalid_pin);
            //mError = null;
            // END XR#P10030293 Added by binjian.tu on 2019/01/29
        }
        mPinDialog.setDialogMessage(message);
    }

    @Override
    public void onPinEntered(EditPinPreference preference, boolean positiveResult) {
        if (!positiveResult) {
            resetDialogState();
            return;
        }

        mPin = preference.getText();
        if (!reasonablePin(mPin)) {
            // inject error message and display dialog again
            mError = mRes.getString(R.string.sim_invalid_pin);//Modified by miaoliu for XR7098190 on 2018/11/5
            showPinDialog();
            return;
        }
        switch (mDialogState) {
            case ICC_LOCK_MODE:
                tryChangeIccLockState();
                break;
            case ICC_OLD_MODE:
                mOldPin = mPin;
                mDialogState = ICC_NEW_MODE;
                // BEGIN XR#P10030293 Added by binjian.tu on 2019/02/13
                mError = mRes.getString(R.string.sim_invalid_pin);
                //mError = null;
                // END XR#P10030293 Added by binjian.tu on 2019/01/29
                mPin = null;
                showPinDialog();
                break;
            case ICC_NEW_MODE:
                mNewPin = mPin;
                mDialogState = ICC_REENTER_MODE;
                mPin = null;
                showPinDialog();
                break;
            case ICC_REENTER_MODE:
                if (!mPin.equals(mNewPin)) {
                    mError = mRes.getString(R.string.sim_pins_dont_match);
                    mDialogState = ICC_NEW_MODE;
                    mPin = null;
                    showPinDialog();
                } else {
                    // BEGIN XR#P10030293 Added by binjian.tu on 2019/02/13
                    mError = mRes.getString(R.string.sim_invalid_pin);
                    //mError = null;
                    // END XR#P10030293 Added by binjian.tu on 2019/01/29
                    tryChangePin();
                }
                break;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mPinToggle) {
            // Get the new, preferred state
            mToState = mPinToggle.isChecked();
            // Flip it back and pop up pin dialog
            mPinToggle.setChecked(!mToState);
            mDialogState = ICC_LOCK_MODE;
            showPinDialog();
        } else if (preference == mPinDialog) {
            mDialogState = ICC_OLD_MODE;
            return false;
        }
        return true;
    }

    private void tryChangeIccLockState() {
        // Try to change icc lock. If it succeeds, toggle the lock state and
        // reset dialog state. Else inject error message and show dialog again.
        Message callback = Message.obtain(mHandler, MSG_ENABLE_ICC_PIN_COMPLETE);
        mPhone.getIccCard().setIccLockEnabled(mToState, mPin, callback);
        // Disable the setting till the response is received.
        mPinToggle.setEnabled(false);
    }

    private void iccLockChanged(boolean success, int attemptsRemaining) {
        if (success) {
            setDefaultRetryCount();//added by wenjie.chen for XR7175466 on 2018/11/29
            mPinToggle.setChecked(mToState);
        } else {
            Toast.makeText(getContext(), getPinPasswordErrorMessage(attemptsRemaining),
                    Toast.LENGTH_LONG).show();
        }
        mPinToggle.setEnabled(true);
        resetDialogState();
    }

    private void iccPinChanged(boolean success, int attemptsRemaining) {
        if (!success) {
            Toast.makeText(getContext(), getPinPasswordErrorMessage(attemptsRemaining),
                    Toast.LENGTH_LONG)
                    .show();
        } else {
            setDefaultRetryCount();//added by wenjie.chen for XR7175466 on 2018/11/29
            Toast.makeText(getContext(), mRes.getString(R.string.sim_change_succeeded),
                    Toast.LENGTH_SHORT)
                    .show();

        }
        resetDialogState();
    }

    private void tryChangePin() {
        Message callback = Message.obtain(mHandler, MSG_CHANGE_ICC_PIN_COMPLETE);
        mPhone.getIccCard().changeIccLockPassword(mOldPin,
                mNewPin, callback);
    }

    private String getPinPasswordErrorMessage(int attemptsRemaining) {
        String displayMessage;

        if (attemptsRemaining == 0) {
            displayMessage = mRes.getString(R.string.wrong_pin_code_pukked);
        } else if (attemptsRemaining > 0) {
            displayMessage = mRes
                    .getQuantityString(R.plurals.wrong_pin_code, attemptsRemaining,
                            attemptsRemaining);
        } else {
            displayMessage = mRes.getString(R.string.pin_failed);
        }
        if (DBG) Log.d(TAG, "getPinPasswordErrorMessage:"
                + " attemptsRemaining=" + attemptsRemaining + " displayMessage=" + displayMessage);
        return displayMessage;
    }

    private boolean reasonablePin(String pin) {
        if (pin == null || pin.length() < MIN_PIN_LENGTH || pin.length() > MAX_PIN_LENGTH) {
            return false;
        } else {
            return true;
        }
    }

    private void resetDialogState() {
        // BEGIN XR#P10030293 Added by binjian.tu on 2019/02/13
        mError = mRes.getString(R.string.sim_invalid_pin);
        //mError = null;
        // END XR#P10030293 Added by binjian.tu on 2019/01/29
        mDialogState = ICC_OLD_MODE; // Default for when Change PIN is clicked
        mPin = "";
        setDialogValues();
        mDialogState = OFF_MODE;
    }

    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            final int slotId = Integer.parseInt(tabId);
            final SubscriptionInfo sir = SubscriptionManager.from(getActivity().getBaseContext())
                    .getActiveSubscriptionInfoForSimSlotIndex(slotId);

            mPhone = (sir == null) ? null
                : PhoneFactory.getPhone(SubscriptionManager.getPhoneId(sir.getSubscriptionId()));

            mCurrestTabSlotId =slotId;//Added by lei.ren.hz for P10028458 on 2018/12/27
            // The User has changed tab; update the body.
            updatePreferences();
            //added by wenjie.chen for XR7307780 on 2019/01/08 begin
            setDefaultRetryCount();
            //added by wenjie.chen for XR7307780 on 2019/01/08 end
        }
    };

    private TabContentFactory mEmptyTabContent = new TabContentFactory() {
        @Override
        public View createTabContent(String tag) {
            return new View(mTabHost.getContext());
        }
    };

    private TabSpec buildTabSpec(String tag, String title) {
        return mTabHost.newTabSpec(tag).setIndicator(title).setContent(
                mEmptyTabContent);
    }
}
