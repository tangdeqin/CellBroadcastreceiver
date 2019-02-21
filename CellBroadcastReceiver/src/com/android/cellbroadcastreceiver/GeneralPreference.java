
package com.android.cellbroadcastreceiver;

import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.MediaStore;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.cb.util.TLog;
import com.tct.util.IsdmParser;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;

import com.tct.wrapper.TctWrapperManager;
import android.os.Handler;
import android.app.ProgressDialog;

public class GeneralPreference extends PreferenceActivity
        implements OnPreferenceChangeListener, OnSharedPreferenceChangeListener {

    private static final String TAG = "GeneralPreference";
//    public static final String CB_RINGTONE = "pref_key_cb_ringtone";
    
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 10;

    private PreferenceCategory mCbOptions;
    private PreferenceCategory mCbOptionsSim2;
    private ListPreference mVibrateWhenCBPref;
    private CheckBoxPreference startup;
    private CheckBoxPreference startup_sim2;
    private RingtonePreference cbringtone;
    private CheckBoxPreference cbledindicate;
    private CheckBoxPreference cblightindicate;
    private Preference mCBSettingsPref;
    private Preference mCBSettingsPref_Sim2;
    private Preference mEmergencyAlertPref;
    private PreferenceCategory mCBNotification;
    private ListPreference mCBvibrate;
    
    private CharSequence[] mVibrateEntries;
    private CharSequence[] mVibrateValues;

    private boolean mNeedRequestPermission = true;
    private String mExternalCheckUri = null;
    private String mExternalSim1CheckUri = null;
    private String mExternalSim2CheckUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPrefs();
    }

    @Override
    public void onResume() {
        TLog.d(TAG, "onResume");
        super.onResume();

        registerListeners();
        updateEnabledState();
        
/*      try {
        	SharedPreferences editor = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String cbringuri = editor.getString(CB_RINGTONE, null);
            if (!TextUtils.isEmpty(cbringuri) && !isRingtoneExist(getActivity(), Uri.parse(cbringuri))) {
            	MmsSystemEventReceiver.checkDefaultCBRingtone(getActivity(), true);
            }
        } catch (Exception ex) {
        	TLog.e(TAG, "onResume : isRingtoneExist occur error"
                    + ex.toString());
        }
*/
    }

    private void loadPrefs() {
        addPreferencesFromResource(R.xml.general_preference);

        mCbOptions = (PreferenceCategory) findPreference("working_cell_broadcast_setting");
        mCbOptionsSim2 = (PreferenceCategory) findPreference("working_cell_broadcast_setting_sim2");
        mCBNotification = (PreferenceCategory) findPreference("working_cell_broadcast_notificaton"); 
        mCBNotification.setTitle(getString(R.string.cbsetting) + " "
                + getString(R.string.pref_notification_settings_title));
        mCBvibrate = (ListPreference) findPreference("pref_key_vibrateWhen_cb");
        mVibrateWhenCBPref = (ListPreference) findPreference("pref_key_vibrateWhen_cb");
        startup = (CheckBoxPreference) findPreference("startup");
        startup_sim2 = (CheckBoxPreference) findPreference("startup_sim2");
        cbringtone = (RingtonePreference) findPreference("pref_key_cb_ringtone");
        cbledindicate = (CheckBoxPreference) findPreference("pref_key_enable_cb_led_indicator");
        cblightindicate = (CheckBoxPreference) findPreference("pref_key_enable_cb_light_indicator");
        mCBSettingsPref = findPreference("cbsetting");
        mCBSettingsPref_Sim2 = findPreference("cbsetting_sim2");
        mEmergencyAlertPref = findPreference("perf_key_emergency_alert_settings");
        
        mVibrateEntries = getResources().getTextArray(R.array.prefEntries_vibrateWhen);
        mVibrateValues = getResources().getTextArray(R.array.prefValues_vibrateWhen);
        
        boolean isHideCBLEDIndicator = getResources().getBoolean(R.bool.def_hide_cb_LED_indicator_in_cb_setting);
        if (isHideCBLEDIndicator) {
        	mCBNotification.removePreference(cbledindicate);
        }
        //add by deqin.tang for Defect 7140360 at 2018-11-22  begin
        int persoRule = getResources().getInteger(R.integer.def_telephony_CBMessage_Filter);
        if(persoRule== 2  ||  persoRule==4){
                mCBNotification.removePreference(mEmergencyAlertPref);
        }
          //add by deqin.tang for Defect 7140360 at 2018-11-22  end
         //add by deqin.tang for Defect  7312081  at 2019-1-8  begin
         SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
         for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                final SubscriptionInfo sir = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
                if (sir != null) {
                    break;  
                }
                if (i == TelephonyManager.getDefault().getPhoneCount()-1) {
                        mEmergencyAlertPref.setEnabled(false);
                }   
         }
        //add by deqin.tang for Defect  7312081  at 2019-1-8  end
        setMessagePreferences();
    }

    private void setMessagePreferences() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        
        initCbSettings();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void registerListeners() {
        if (startup != null) {
            startup.setOnPreferenceChangeListener(this);
        }
        if (startup_sim2 != null) {
            startup_sim2.setOnPreferenceChangeListener(this);
        }
        if (cbringtone != null) {
            cbringtone.setOnPreferenceChangeListener(this);
        }
        if (mVibrateWhenCBPref != null) {
            mVibrateWhenCBPref.setOnPreferenceChangeListener(this);
        }
        if (cbledindicate != null) {
            cbledindicate.setOnPreferenceChangeListener(this);
        }
        if (cblightindicate != null) {
            cblightindicate.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCBSettingsPref) {
        	Intent intent = new Intent("android.intent.action.MAIN");
            intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY,
                    PhoneConstants.SUB1);
            intent.setClassName("com.android.cellbroadcastreceiver",
                    "com.android.cellbroadcastreceiver.CBMSettingActivity");
            startActivity(intent);
        } else if (preference == mCBSettingsPref_Sim2) {
        	Intent intent = new Intent("android.intent.action.MAIN");
            intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY,
                    PhoneConstants.SUB2);
            intent.setClassName("com.android.cellbroadcastreceiver",
                    "com.android.cellbroadcastreceiver.CBMSettingActivity");
            startActivity(intent);
        } else if (preference == mEmergencyAlertPref) {
        	Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName("com.android.cellbroadcastreceiver",
                    "com.android.cellbroadcastreceiver.CellBroadcastSettings");
            startActivity(intent);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
        if (preference == startup) {
            boolean startUp = (Boolean) newValue;
            startup.setChecked(startUp);
            sendCBSetIntent(startUp, PhoneConstants.SUB1);
        } else if (preference == startup_sim2) {
            boolean startUp = (Boolean) newValue;
            startup_sim2.setChecked(startUp);
            sendCBSetIntent(startUp, PhoneConstants.SUB2);
        }
        /* else if (preference == cbringtone) {
            SharedPreferences editor = PreferenceManager.getDefaultSharedPreferences(context);
            editor.edit().putString("pref_key_cb_ringtone", newValue.toString()).commit();
            String cbringuri = editor.getString("pref_key_cb_ringtone", null);
            context.getContentResolver().delete(
                    Uri.parse("content://" + "com.jrd.provider.CellBroadcast" + "/CBRingtone"),
                    null, null);
            ContentValues values;
            values = new ContentValues();
            values.put("cbringtone", cbringuri);
            context.getContentResolver().insert(
                    Uri.parse("content://" + "com.jrd.provider.CellBroadcast" + "/CBRingtone"),
                    values);
            if (cbringuri != null && cbringuri.contains("content://media/external/audio/")) {
            	if (!OsUtil.hasStoragePermission() && mNeedRequestPermission) {
                    mNeedRequestPermission = false;
                    requestPermissions(new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, STORAGE_PERMISSION_REQUEST_CODE);
                } else if (!mNeedRequestPermission) {
                    mNeedRequestPermission = true;
                }
            }
        } */
        else if (preference == cbledindicate) {
            boolean cbledenable = (Boolean) newValue;
            if (cbledenable == true) {
                android.provider.Settings.System.putString(getContentResolver(),
                        "CBLedEnable", "on");
            } else {
                android.provider.Settings.System.putString(getContentResolver(),
                        "CBLedEnable", "off");
            }
            result = true;
        } else if (preference == cblightindicate) {
            boolean cblightenable = (Boolean) newValue;
            if (cblightenable == true) {
                android.provider.Settings.System.putString(getContentResolver(),
                        "CBLightEnable", "on");
            } else {
                android.provider.Settings.System.putString(getContentResolver(),
                        "CBLightEnable", "off");
            }
            result = true;
        } else if (preference == mVibrateWhenCBPref) {
            adjustVibrateCBSummary((String) newValue);
            result = true;
        }

        return result;
    }

    private void updateEnabledState() {
        /*if (startup != null)
            startup.setEnabled(MessagingSettings.hasIccCardforSim1());
        if (startup_sim2 != null)
            startup_sim2.setEnabled(MessagingSettings.hasIccCardforSim2());
        if (mEmergencyAlertPref != null)
            mEmergencyAlertPref.setEnabled(isSmsEnabled);
        if (mCBSettingsPref_Sim2 != null)
            mCBSettingsPref_Sim2.setEnabled(isSmsEnabled);
        if (mCBSettingsPref != null)
            mCBSettingsPref.setEnabled(isSmsEnabled);*/
        
        if((startup != null && startup.isEnabled()) 
        || (startup_sim2 != null && startup_sim2.isEnabled())) {
        	SetCBNotificationSettingsEnabled(true);
        } else {
        	SetCBNotificationSettingsEnabled(false);
        }
    }

    private void initCbSettings() {
        SharedPreferences editor = PreferenceManager.getDefaultSharedPreferences(this);
        TelephonyManager telem = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telem.getSimState(PhoneConstants.SUB1) != TelephonyManager.SIM_STATE_ABSENT) {
            boolean startup = editor.getBoolean("startup", false);
            this.startup.setEnabled(true);
            this.startup.setChecked(startup);

        } else {
            startup.setEnabled(false);
            sendCBSetIntent(false, PhoneConstants.SUB1);
        }
        if (telem.getSimState(PhoneConstants.SUB2) != TelephonyManager.SIM_STATE_ABSENT) {
            boolean startup_sim2 = editor.getBoolean("startup_sim2", false);
            this.startup_sim2.setEnabled(true);
            this.startup_sim2.setChecked(startup_sim2);

        } else {
            startup_sim2.setEnabled(false);
            sendCBSetIntent(false, PhoneConstants.SUB2);
        }
        
        if((startup != null && startup.isEnabled()) 
        || (startup_sim2 != null && startup_sim2.isEnabled())) {
        	SetCBNotificationSettingsEnabled(true);
        } else {
        	SetCBNotificationSettingsEnabled(false);
        }

        SharedPreferences sps = this.getSharedPreferences("boot", 0);
        /*
         * if cb isn't reset by ssv check and is first boot, will do the custmization, otherwise
         * will do nothing
         */
        if (!sps.getBoolean("isFirstBootForCB", false)) {
            boolean ActiveCB = getResources().getBoolean(
                    R.bool.def_mms_cellbroadcast_on_in_cb_setting);
            if (ActiveCB) {
                startup.setChecked(true);
                sendCBSetIntent(true, PhoneConstants.SUB1);
                startup.setSummaryOn("");

                startup_sim2.setChecked(true);
                sendCBSetIntent(true, PhoneConstants.SUB2);
                startup_sim2.setSummaryOn("");
            } else {
                startup.setChecked(false);
                sendCBSetIntent(false, PhoneConstants.SUB1);
                // [SMSCB]Move CB from Phone to CellBroadcastReceiver
                startup.setSummaryOff(R.string.summary_startup);

                startup_sim2.setChecked(false);
                sendCBSetIntent(false, PhoneConstants.SUB2);
                startup_sim2.setSummaryOff(R.string.summary_startup);
            }
        }
        SharedPreferences.Editor editorForFirstBoot = sps.edit();
        editorForFirstBoot.putBoolean("isFirstBootForCB", true);
        editorForFirstBoot.commit();
        if (startup.isChecked()) {
            startup.setSummaryOn("");
        } else {
            startup.setSummaryOff(R.string.summary_startup);
        }
        if (startup_sim2.isChecked()) {
            startup_sim2.setSummaryOn("");
        } else {
            startup_sim2.setSummaryOff(R.string.summary_startup);
        }

        if ("true".equalsIgnoreCase(SystemProperties.get("ro.set.nl.cb.on", "false"))) {
            String tag = "CellBroadcastReceiver";
            TLog.i(tag, "GeneralPreference-def_set_NL_CB_on");
            TLog.i(tag, "GeneralPreference-startup.isChecked()=" + startup.isChecked());
            TLog.i(tag,
                    "GeneralPreference-startup_sim2.isChecked()=" + startup_sim2.isChecked());
            boolean isHollandSimCard = isHollandSimCard(this);
            if (!startup.isChecked() && isHollandSimCard) {
                startup.setChecked(true);
                startup.setSummaryOn("");
                sendCBSetIntent(true, PhoneConstants.SUB1);
            }
            if (!startup_sim2.isChecked() && isHollandSimCard) {
                startup_sim2.setChecked(true);
                startup_sim2.setSummaryOn("");
                sendCBSetIntent(true, PhoneConstants.SUB2);
            }
        }

        Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()
                || !(getResources().getBoolean(R.bool.feature_mms_set_vibrate_in_cb_setting))) {
            mCBNotification.removePreference(mVibrateWhenCBPref);
        } else {
            adjustVibrateCBSummary(mVibrateWhenCBPref.getValue());
        }

        PreferenceScreen pref = this.getPreferenceScreen();
        mCbOptionsSim2.setTitle(getString(R.string.title_cbsetting) + " ("
                + getString(R.string.txt_sim2) + ")");
        // if not exist this sdmid, donot append sim1
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (IsdmParser.getBooleanFwk(this, "def_cellbroadcast_enable_single_sim", false)) {
                if (mCbOptions != null) {
                    mCbOptions.setTitle(getString(R.string.title_cbsetting) + " ("
                            + getString(R.string.txt_sim1) + ")");
                }
                if (mEmergencyAlertPref != null) {
                    mEmergencyAlertPref
                            .setSummary(getResources().getString(R.string.sim1_only_summary));
                }
                pref.removePreference(mCbOptionsSim2);
            }
        } else {
            pref.removePreference(mCbOptionsSim2);
        }

        //modify  by deqin.tang for Defect 7107664  at 2018-11-15 begin
        //if (mCBNotification != null && !this.getResources().getBoolean(R.bool.def_hide_cb_LED_indicator_in_cb_setting)) {
        	//mCBNotification.removePreference(cbledindicate);
        //}
        //modify  by deqin.tang for Defect 7107664  at 2018-11-15 end

        if (mCBNotification != null && !this.getResources().getBoolean(R.bool.feature_mms_cbLightUpScreen_on_in_cb_setting)) {
        	mCBNotification.removePreference(cblightindicate);
        }
//        if (mCBNotification != null && !getResources().getBoolean(R.bool.feature_mms_select_cb_ringtone_on)) {
        if (mCBNotification != null && !false) {
        	mCBNotification.removePreference(cbringtone);
        }

//        if (!getResources().getBoolean(R.bool.feature_mms_cellbroadcast_on)) {
        if (!true) {
            pref.removePreference(mCbOptions);
            pref.removePreference(mCbOptionsSim2);
            pref.removePreference(mCBNotification);
        }
    }

    private boolean isHollandSimCard(Context mContext) {
        boolean mIsNLSimCard = false;
        String numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
        TLog.i("CellBroadcastReceiver", "numeric=" + numeric);
        if (numeric != null && numeric.length() > 4) {
            if (TelephonyManager.getDefault().isMultiSimEnabled()
                    && TelephonyManager.getDefault().hasIccCard(1)) {
                String[] temp = numeric.split(",");
                if (temp != null && temp.length > 0) {
                    for (int i = 0; i < temp.length; i++) {
                        if (temp[i] != null && temp[i].length() > 3) {
                            TLog.i(TAG, "temp[i]=" + temp[i]);
                            if (temp[i].subSequence(0, 3).equals("204")) {
                                mIsNLSimCard = true;
                            }
                        }
                    }
                }
            } else {
                String strMcc = numeric.substring(0, 3);
                TLog.i("CellBroadcastReceiver", "strMcc=" + strMcc);
                if (strMcc.equals("204")) {
                    mIsNLSimCard = true;
                }
            }
        }
        if (mIsNLSimCard) {
            return mIsNLSimCard;
        }
        TelephonyManager telephonyManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        String numeric_str = telephonyManager.getNetworkOperator();
        TLog.i("CellBroadcastReceiver", "Read telephony service numeric=" + numeric);
        if (numeric_str != null && numeric_str.length() > 4) {
            if (TelephonyManager.getDefault().isMultiSimEnabled()
                    && TelephonyManager.getDefault().hasIccCard(0)
                    && TelephonyManager.getDefault().hasIccCard(1)) {
                String[] temp = numeric.split(",");
                if (temp != null && temp.length > 0) {
                    for (int i = 0; i < temp.length; i++) {
                        if (temp[i] != null && temp[i].length() > 3) {
                            TLog.i(TAG, "temp[i]=" + temp[i]);
                            if (temp[i].subSequence(0, 3).equals("204")) {
                                mIsNLSimCard = true;
                            }
                        }
                    }
                }
            } else {
                String strMcc = numeric_str.substring(0, 3);
                if (strMcc.equals("204")) {
                    mIsNLSimCard = true;
                }
            }
        }

        return mIsNLSimCard;
    }

    private void sendCBSetIntent(boolean start, int phoneId) {
        String ActionString = "com.android.cellbroadcastreceiver.setstartup";
        Intent intent = new Intent(ActionString);
        intent.putExtra("startup", start);
        intent.putExtra(PhoneConstants.PHONE_KEY, phoneId);
        sendBroadcast(intent);
    }

    private void adjustVibrateCBSummary(String value) {
        int len = mVibrateValues.length;
        for (int i = 0; i < len; i++) {
            if (mVibrateValues[i].equals(value)) {
                mVibrateWhenCBPref.setSummary(mVibrateEntries[i]);
                android.provider.Settings.System.putString(getContentResolver(),
                        "vibrateWhenCB", value);
                return;
            }
        }
        mVibrateWhenCBPref.setSummary(null);
        android.provider.Settings.System.putString(getContentResolver(),
                "vibrateWhenCB", "");
    }

    public void refreshWhenSimStateChange(String state) {
    	TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	tm.hasIccCard(PhoneConstants.SUB1);
        TLog.i(TAG, "refreshWhenSimStateChange == " + state);
        if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(state)) {
            if (startup != null && startup.isEnabled() && !tm.hasIccCard(PhoneConstants.SUB1)) {
                startup.setEnabled(false);
            }
            if (startup_sim2 != null && startup_sim2.isEnabled()
                    && !tm.hasIccCard(PhoneConstants.SUB2)) {
                startup_sim2.setEnabled(false);
            }
        } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(state)) {
            if (startup != null && !startup.isEnabled() && tm.hasIccCard(PhoneConstants.SUB1)) {
                startup.setEnabled(true);
            }
            if (startup_sim2 != null && !startup_sim2.isEnabled()
                    && tm.hasIccCard(PhoneConstants.SUB2)) {
                startup_sim2.setEnabled(true);
            }
        } else if (IccCardConstants.INTENT_VALUE_ICC_UNKNOWN.equals(state) ||
                IccCardConstants.INTENT_VALUE_ICC_NOT_READY.equals(state) ||
                IccCardConstants.INTENT_VALUE_ICC_READY.equals(state) ||
                IccCardConstants.INTENT_VALUE_ICC_IMSI.equals(state)) {
        }
        
        if((startup != null && startup.isEnabled()) 
        || (startup_sim2 != null && startup_sim2.isEnabled())) {
        	SetCBNotificationSettingsEnabled(true);
        } else {
        	SetCBNotificationSettingsEnabled(false);
        }
    }
    
    public void SetCBNotificationSettingsEnabled(boolean isEnable) {
        TLog.i(TAG, "refreshCBNotificationSettings , isEnable =" + isEnable);
        if(isEnable) {            
//            if (mEmergencyAlertPref != null) mEmergencyAlertPref.setEnabled(true);
            if (cbledindicate != null) cbledindicate.setEnabled(true);
            if (cblightindicate != null) cblightindicate.setEnabled(true);
            if (cbringtone != null) cbringtone.setEnabled(true);
            if (mCBvibrate != null ) mCBvibrate.setEnabled(true);
        } else {
//            if (mEmergencyAlertPref != null) mEmergencyAlertPref.setEnabled(false);
            if (cbledindicate != null) cbledindicate.setEnabled(false);
            if (cblightindicate != null) cblightindicate.setEnabled(false);
            if (cbringtone != null) cbringtone.setEnabled(false);
            if (mCBvibrate != null ) mCBvibrate.setEnabled(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        TLog.i(TAG, "onSharedPreferenceChanged.key = " + key);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {/*
            final boolean permissionGranted = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (permissionGranted) {
                mNeedRequestPermission = true; // MODIFIED by guoqing.zeng, 2017-03-16,BUG-4362796
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                     MODIFIED-BEGIN by pengpeng.jiao, 2016-11-30,BUG-3540472 
                    if (MessageUtils.isIccActive(WrapConstants.PHONE_ID1)) {
                        TLog.i(TAG, "permissionGranted mExternalSim1CheckUri = "
                                + mExternalSim1CheckUri);
                        setRingtoneSummary(mExternalSim1CheckUri, WrapConstants.PHONE_ID1);
                    }
                    if (MessageUtils.isIccActive(WrapConstants.PHONE_ID2)) {
                        TLog.i(TAG, "permissionGranted mExternalSim2CheckUri = "
                                + mExternalSim2CheckUri);
                        setRingtoneSummary(mExternalSim2CheckUri, WrapConstants.PHONE_ID2);
                    }
                } else {
                    TLog.i(TAG, "permissionGranted mExternalCheckUri = " + mExternalCheckUri);
                    setRingtoneSummary(mExternalCheckUri, WrapConstants.PHONE_ID1);
                     MODIFIED-END by pengpeng.jiao,BUG-3540472 
                }
            } else {
                mNeedRequestPermission = false;
                TLog.i(TAG, "no mNeedRequestPermission");
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                     MODIFIED-BEGIN by pengpeng.jiao, 2016-11-30,BUG-3540472 
                    if (MessageUtils.isIccActive(WrapConstants.PHONE_ID1)) {
                        setRingtoneSummary(null, WrapConstants.PHONE_ID1);
                        editor.putString(MessagingSettings.NOTIFICATION_RINGTONE_SIM1, null);
                    }
                    if (MessageUtils.isIccActive(WrapConstants.PHONE_ID2)) {
                        setRingtoneSummary(null, WrapConstants.PHONE_ID2);
                        editor.putString(MessagingSettings.NOTIFICATION_RINGTONE_SIM2, null);
                    }
                } else {
                    setRingtoneSummary(null, WrapConstants.PHONE_ID1);
                     MODIFIED-END by pengpeng.jiao,BUG-3540472 
                    editor.putString(MessagingSettings.NOTIFICATION_RINGTONE, null);
                }
                editor.apply();
            }
        */}
    }
}
