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

package com.android.cellbroadcastreceiver;

/* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
import com.android.cb.util.TLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.telephony.CellBroadcastMessage;
import android.util.Log;
/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

/**
 * Custom alert dialog with optional flashing warning icon.
 * Alert audio and text-to-speech handled by {@link CellBroadcastAlertAudio}.
 * Keyguard handling based on {@code AlarmAlert} class from DeskClock app.
 */
public class CellBroadcastAlertDialog extends CellBroadcastAlertFullScreen {

    private BroadcastReceiver mScreenOffReceiver;
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
    private final String TAG = "CellBroadcastAlertDialog";
    private final String ALERT_DIALOG_DISMISS_FLAG = "alter_dialot_dismiss_flag"; // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1748495
    public static final String DIALOG_DISMISS_ACTION = "dialog_finish_action";
    private BroadcastReceiver mDismissReceiver;
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    private class ScreenOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleScreenOff();
        }
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-01,BUG-2344436*/
    private class ListActivityReveiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mDissmissFlag = true; // MODIFIED by yuxuan.zhang, 2016-07-11,BUG-1112693
            Log.i(TAG, "dismiss alert");
            finish();
        }
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/

    @Override
    protected void onCreate(Bundle icicle) {
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1748495*/
        Log.i(TAG, "onCreate");
        super.onCreate(icicle);
        initFromSaveInstance(icicle);
        /* MODIFIED-END by yuxuan.zhang,BUG-1748495*/
        // Listen for the screen turning off so that when the screen comes back
        // on, the user does not need to unlock the phone to dismiss the alert.
        if (CellBroadcastConfigService.isEmergencyAlertMessage(getLatestMessage())) {
            mScreenOffReceiver = new ScreenOffReceiver();
            registerReceiver(mScreenOffReceiver,
                    new IntentFilter(Intent.ACTION_SCREEN_OFF));
        }
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-01,BUG-2344436*/
        mDismissReceiver = new ListActivityReveiver();
        registerReceiver(mDismissReceiver,
                new IntentFilter(DIALOG_DISMISS_ACTION));
                /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/
        setFinishOnTouchOutside(false); // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1112693
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1748495*/
    private void initFromSaveInstance(Bundle outState) {
        if (outState != null) {
            mDissmissFlag = outState.getBoolean(ALERT_DIALOG_DISMISS_FLAG);
        } else {
            mDissmissFlag = false;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy mDissmissFlag = "+mDissmissFlag ); // MODIFIED by yuxuan.zhang, 2016-07-11,BUG-1112693
        super.onDestroy();
        if (mScreenOffReceiver != null) {
            unregisterReceiver(mScreenOffReceiver);
        }
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-01,BUG-2344436*/
        if (mDismissReceiver != null) {
            unregisterReceiver(mDismissReceiver);
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/
        if (mDissmissFlag) {
            stopService(new Intent(this, CellBroadcastAlertAudio.class));
        }
       // MODIFIED by yuxuan.zhang, 2016-06-20,BUG-2344436
    }

    @Override
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-08-08,BUG-1112693*/
    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        boolean WpasEnableFlag = getResources().getBoolean(R.bool.def_enable_wpas_function);
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(this)) {
        	WpasEnableFlag = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        if(!WpasEnableFlag){
            super.onApplyDialogThemeResource(theme, resid, first);
        } else {
            super.onApplyDialogThemeResource(theme, R.style.fullScreen_dialog, first);
        }
    }

    @Override
    public void onBackPressed() {
        //PR 1041716 Added by fang.song begin
        /*if (Settings.Global.getInt(getContentResolver(), Settings.Global.IS_EMERGENCY_BROADCAST_DISPLAY, 0) == 1) {
            return;
        }*///TODO FNY
        //PR 1041716 Added by fang.song end

        // stop animating warning icon, stop playing alert sound, mark broadcast as read
        //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
        //CBC notification with pop up and tone alert + vibrate in CHILE
        // dismiss();
        Intent intent = getIntent();
        boolean forceVibrateChile = intent.getBooleanExtra("forceVibrate", false);
        if (!forceVibrateChile) {
             dismiss();
        }
        //[FEATURE]-Add-END by TCTNB.Dandan.Fang
    }

    @Override
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    protected int getLayoutResId() {
        return R.layout.cell_broadcast_alert;
    }

    private void handleScreenOff() {
        Log.i(TAG, "handleScreenOff"); // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1748495
        // Launch the full screen activity but do not turn the screen on.
        Intent i = new Intent(this, CellBroadcastAlertFullScreen.class);
        i.putParcelableArrayListExtra(CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA, mMessageList);
        i.putExtra(SCREEN_OFF_EXTRA, true);
        startActivity(i);
        finish();
    }

    // begin: aiyan-999810-T-Mobile request for incoming call after WEA message
    @Override
    protected void setFullScreenExist() {
        // need do nothing, just distinguish with CellBroadcastAlertFullScreen
    }
    // end: aiyan-999810-T-Mobile request for incoming call after WEA message
}
