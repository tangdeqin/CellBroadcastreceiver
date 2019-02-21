/*
 * Copyright (C) 2012 The Android Open Source Project
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
/* ==========================================================================
 *     Modifications on Features list / Changes Request / Problems Report
 * --------------------------------------------------------------------------
 *    date   |        author        |         Key          |     comment
 * ----------|----------------------|----------------------|-----------------
 * ----------|----------------------|----------------------|-----------------
 * 05/07/2014|ke.meng               | FR-642065            |RUSSIA REQ
 * ----------|----------------------|----------------------|-----------------
 * 08/30/2014|     tianming.lei     |        777440        |Cell broadcast messages
 *           |                      |                      |have an incorrect format
 * ----------|----------------------|----------------------|-----------------
 * 01/03/2014|     tianming.lei     |        888411        |Header for CB message
 *           |                      |                      |in notification panel
 *           |                      |                      | should be changed
 * ----------|----------------------|----------------------|-----------------
 * 01/21/2015|      bangjun.wang    |        886044        |[SCB]Date display
 *           |                      |                      |error when open the
 *           |                      |                      |received CB message
 * ----------|----------------------|----------------------|-----------------
 * 02/24/2015|      fujun.yang      |        926300        |[Pre-CTS][CB]Header
 *           |                      |                      |for CB message in
 *           |                      |                      |notification panel
 *           |                      |                      |should be changed
 * ----------|----------------------|----------------------|-----------------
 * 03/02/2015|      fujun.yang      |        928255        |[SS][SCB]The phone
 *           |                      |                      |cannot parse the
 *           |                      |                      |short phone number
 *           |                      |                      |while receive
 *           |                      |                      |CB/CMAS
 * ----------|----------------------|----------------------|------------------
 * 03/02/2015|      fujun.yang      |        924513        |[SCB]CB
 *           |                      |                      |notification will
 *           |                      |                      |disappear after
 *           |                      |                      |incoming a
 *           |                      |                      |emergency alert
 * ----------|----------------------|----------------------|------------------
 * 07/17/2015|      fang.song       |        1041463       |FC happened when double click the WEA(CMAS) message on the notification bar
 * ----------|----------------------|----------------------|------------------
 * 07/17/2015|      fang.song       |        1043168 	   |FC happened when reject a MT call after read a WEA message
 * ----------|----------------------|----------------------|------------------
 *****************************************************************************/
package com.android.cellbroadcastreceiver;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
/* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.CellBroadcastMessage;
import android.telephony.SmsCbCmasInfo;
import android.telephony.TelephonyManager;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface; // MODIFIED by yuxuan.zhang, 2016-05-06,BUG-1112693
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

//[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
//CBC notification with pop up and tone alert + vibrate in CHILE
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.Long;
//[BUGFIX]-Add- by TCTNB.ke.meng,05/07/2014,642065
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//[BUGFIX]-Add- by TCTNB.ke.meng
import android.os.PowerManager;
import android.os.SystemClock;
//[FEATURE]-Add-END by TCTNB.Dandan.Fang

//[FEATURE]-Add-BEGIN by TCTNB.yugang.jia,11/15/2013,546255,
//[CDR-ECB-690/700]Message Parsing—Phone / E-mail
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils; // MODIFIED by yuxuan.zhang, 2016-07-29,BUG-1112693
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics; // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1748495
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-05-06,BUG-1112693
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
//[FEATURE]-Add-END by TCTNB.yugang.jia

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.android.cb.util.TLog;
import com.android.cellbroadcastreceiver.CellBroadcast.Channel;
import com.android.internal.telephony.cdma.sms.SmsEnvelope; // MODIFIED by chaobing.huang, 2017-01-17,BUG-4014007
import com.tct.telecom.TctQctCellBroadcast;
import com.tct.util.FwkPlf;
import com.tct.util.IsdmParser;
import com.tct.wrapper.TctWrapperManager;
//[BUGFIX]-Add-BEGIN by TSCD.bangjun.wang,01/21/2015,886044
import android.provider.Settings;

import java.util.Calendar;
//[BUGFIX]-Add-END by TSCD.bangjun.wang

//add by liang.zhang for Defect 5960227 at 2018-02-01 begin
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
//add by liang.zhang for Defect 5960227 at 2018-02-01 end

/**
 * Full-screen emergency alert with flashing warning icon.
 * Alert audio and text-to-speech handled by {@link CellBroadcastAlertAudio}.
 * Keyguard handling based on {@code AlarmAlertFullScreen} class from DeskClock app.
 */
public class CellBroadcastAlertFullScreen extends Activity {
    private static final String TAG = "CellBroadcastAlertFullScreen";

    /**
     * Intent extra for full screen alert launched from dialog subclass as a result of the
     * screen turning off.
     */
    static final String SCREEN_OFF_EXTRA = "screen_off";

    /**
     * Intent extra for non-emergency alerts sent when user selects the notification.
     */
    static final String FROM_NOTIFICATION_EXTRA = "from_notification";

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
    /**
     * Intent extra for wpas-emergency alerts sent when user selects the notification.
     */
    static final String FROM_WPAS_NOTIFICATION_EXTRA = "from_wpas_notification";
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    /**
     * Intent extra to stop full screen when incoming call. aiyan-999810
     */
    public static final String FINISH_FULL_SCREEN = "finish_full_screen";

    //[modify]-begin-by-chaobing.huang-22.12.2016-defect3823603
    public static final String LOW_STORAGE_MODE = "low_storage";
    //[modify]-begin-by-chaobing.huang-22.12.2016-defect3823603

    public static final String CMAS_EMERGENCY_DISPLAY = "cmas_emergency_display"; // MODIFIED by yuwan, 2017-06-08,BUG-4887465
    /**
     * List of cell broadcast messages to display (oldest to newest).
     */
    public static ArrayList<CellBroadcastMessage> mMessageList;// aiyan-999810

    /**
     * List of non-PRESIDENT cell broadcast messages to display (oldest to newest).
     */
    public static ArrayList<CellBroadcastMessage> mCBMessageList;//[FEATURE]-Add by jian.bu,08/12/2015,1052152

    /**
     * List of PRESIDENT cell broadcast messages to display (oldest to newest).
     */
    public static ArrayList<CellBroadcastMessage> mPresidentMessageList;//[FEATURE]-Add by jian.bu,08/12/2015,1052152

    /**
     * Whether a CMAS alert other than Presidential Alert was displayed.
     */
    private boolean mShowOptOutDialog;

    /**
     * Length of time for the warning icon to be visible.
     */
    private static final int WARNING_ICON_ON_DURATION_MSEC = 800;

    /**
     * Length of time for the warning icon to be off.
     */
    private static final int WARNING_ICON_OFF_DURATION_MSEC = 800;

    /**
     * Length of time to keep the screen turned on.
     */
    private static final int KEEP_SCREEN_ON_DURATION_MSEC = 60000;

    /**
     * Length of time to keep the screen turned on for Chile.
     */
    private static final int Chile_KEEP_SCREEN_ON_DURATION_MSEC = 10000;

    /**
     * Animation handler for the flashing warning icon (emergency alerts only).
     */
    private final AnimationHandler mAnimationHandler = new AnimationHandler();

    /**
     * Handler to add and remove screen on flags for emergency alerts.
     */
    private final ScreenOffHandler mScreenOffHandler = new ScreenOffHandler();

    //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
    //CBC notification with pop up and tone alert + vibrate in CHILE
    private boolean forceVibrateChile = false;
    //[FEATURE]-Add-END by TCTNB.Dandan.Fang
    // [BUGFIX]-ADD-BEGIN by bin.xue for PR-1105873
    private FrameLayout mFlList;
    private static int mFlListWidth = 0;
    // [BUGFIX]-ADD-END by bin.xue

    //[FEATURE]-Add-BEGIN by TCTNB.yugang.jia,11/15/2013,546255,
    //[CDR-ECB-690/700]Message Parsing—Phone / E-mail
    private Context mContext;
    //[FEATURE]-Add-END by TCTNB.yugang.jia
    private CellBroadcastMessage repeatmessage;//[BUGFIX]-Add- by TCTNB.ke.meng,05/07/2014,642065
    public static boolean mMsgFirstCome = false;// aiyan-979267
    /* flag true when notification exist. aiyan-999810 */
    public static boolean isFromNotification = false;
    /* flag true when instance of CellBroadcastAlertFullScreen exist. aiyan-999810 */
    public static boolean isFullScreenExist = false;
    private View mContentView;//RR 1041716 Added by fang.song
    //ADD-Alert-ID-begin-by-chaobing.huang-9/14/2015-PR1084768
    private static final int MSGID1 = 4370;
    private static final int MSGID2 = 4371;
    private static final int MSGID9 = 4378;
    private static final int MSGID12 = 4381;
    //ADD-Alert-ID-end-by-chaobing.huang-9/14/2015-PR1084768
    protected boolean mDissmissFlag = false;
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1748495*/
    protected TextView mMessageView = null;
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-08-24,BUG-1112693*/
    protected boolean mPortraitFlag;
    protected boolean mWpasEnableFlag = false;
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    /* MODIFIED-END by yuxuan.zhang,BUG-1748495*/
    private static PowerManager.WakeLock wakeLock;//[add]-by-chaobing.huang-22.12.2016-defect3857798

    /**
     * Animation handler for the flashing warning icon (emergency alerts only).
     */
    private class AnimationHandler extends Handler {
        /**
         * Latest {@code message.what} value for detecting old messages.
         */
        private final AtomicInteger mCount = new AtomicInteger();

        /**
         * Warning icon state: visible == true, hidden == false.
         */
        private boolean mWarningIconVisible;

        /**
         * The warning icon Drawable.
         */
        private Drawable mWarningIcon;

        /**
         * The View containing the warning icon.
         */
        private ImageView mWarningIconView;

        /**
         * Package local constructor (called from outer class).
         */
        AnimationHandler() {
        }

        /**
         * Start the warning icon animation.
         */
        void startIconAnimation() {
            if (!initDrawableAndImageView()) {
                return;     // init failure
            }
            //[BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
            boolean ssvEnabled = "true".equals(SystemProperties.get("ro.ssv.enabled", "false"));
            if (ssvEnabled) {
                mWarningIconVisible = false;
                mWarningIconView.setVisibility(View.GONE);
            } else {
                mWarningIconVisible = true;
                mWarningIconView.setVisibility(View.VISIBLE);
            }
            //[BUGFIX]-Mod-END by TSCD.tianming.lei
            updateIconState();
            queueAnimateMessage();
        }

        /**
         * Stop the warning icon animation.
         */
        void stopIconAnimation() {
            // Increment the counter so the handler will ignore the next message.
            mCount.incrementAndGet();
            if (mWarningIconView != null) {
                mWarningIconView.setVisibility(View.GONE);
            }
        }

        /**
         * Update the visibility of the warning icon.
         */
        private void updateIconState() {
            mWarningIconView.setImageAlpha(mWarningIconVisible ? 255 : 0);
            mWarningIconView.invalidateDrawable(mWarningIcon);
        }

        /**
         * Queue a message to animate the warning icon.
         */
        private void queueAnimateMessage() {
            int msgWhat = mCount.incrementAndGet();
            sendEmptyMessageDelayed(msgWhat, mWarningIconVisible ? WARNING_ICON_ON_DURATION_MSEC
                    : WARNING_ICON_OFF_DURATION_MSEC);
            // TLog.d(TAG, "queued animation message id = " + msgWhat);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == mCount.get()) {
                mWarningIconVisible = !mWarningIconVisible;
                updateIconState();
                queueAnimateMessage();
            }
        }

        /**
         * Initialize the Drawable and ImageView fields.
         *
         * @return true if successful; false if any field failed to initialize
         */
        private boolean initDrawableAndImageView() {
            if (mWarningIcon == null) {
                try {
                    mWarningIcon = getResources().getDrawable(R.drawable.ic_warning_large);
                } catch (Resources.NotFoundException e) {
                    TLog.e(TAG, "warning icon resource not found", e);
                    return false;
                }
            }
            if (mWarningIconView == null) {
                mWarningIconView = (ImageView) findViewById(R.id.icon);
                if (mWarningIconView != null) {
                    mWarningIconView.setImageDrawable(mWarningIcon);
                } else {
                    TLog.e(TAG, "failed to get ImageView for warning icon");
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Handler to add {@code FLAG_KEEP_SCREEN_ON} for emergency alerts. After a short delay,
     * remove the flag so the screen can turn off to conserve the battery.
     */
    private class ScreenOffHandler extends Handler {
        /**
         * Latest {@code message.what} value for detecting old messages.
         */
        private final AtomicInteger mCount = new AtomicInteger();

        /**
         * Package local constructor (called from outer class).
         */
        ScreenOffHandler() {
        }

        /**
         * Add screen on window flags and queue a delayed message to remove them later.
         */
        void startScreenOnTimer() {
            addWindowFlags();
            int msgWhat = mCount.incrementAndGet();
            removeMessages(msgWhat - 1);    // Remove previous message, if any.
            
            // add by liang.zhang for Defect 633102 at 2018-08-06 begin
            if (isNewZealandMessage(getLatestMessage())) {
            	return;
        	}
            // add by liang.zhang for Defect 633102 at 2018-08-06 end
            
            // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
            // CBC notification with pop up and tone alert + vibrate in CHILE
            // screen off after 10 seconds for Chile
            //sendEmptyMessageDelayed(msgWhat, KEEP_SCREEN_ON_DURATION_MSEC);
            if (forceVibrateChile) {
                // modified by jielong.xing for defect-4946447 at 2017-6-22 begin
                CellBroadcastMessage message = getLatestMessage();
                if (message != null && CellBroadcastConfigService.isEmergencyAlertMessage(message)) {
                    if (!(mContext.getResources().getBoolean(R.bool.def_cb_4370_all_softkey_disable)
                            && message.getServiceCategory() == MSGID1)) {
                        sendEmptyMessageDelayed(msgWhat,
                                Chile_KEEP_SCREEN_ON_DURATION_MSEC);
                    }
                } else {
                    sendEmptyMessageDelayed(msgWhat,
                            Chile_KEEP_SCREEN_ON_DURATION_MSEC);
                }
                // modified by jielong.xing for defect-4946447 at 2017-6-22 end
            } else {
                sendEmptyMessageDelayed(msgWhat, KEEP_SCREEN_ON_DURATION_MSEC);
            }
            // [FEATURE]-Add-END by TCTNB.Dandan.Fang
            TLog.d(TAG, "added FLAG_KEEP_SCREEN_ON, queued screen off message id " + msgWhat);
        }

        /**
         * Remove the screen on window flags and any queued screen off message.
         */
        void stopScreenOnTimer() {
            removeMessages(mCount.get());
            clearWindowFlags();
        }

        /**
         * Set the screen on window flags.
         */
        private void addWindowFlags() {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        /**
         * Clear the screen on window flags.
         */
        private void clearWindowFlags() {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void handleMessage(Message msg) {
            int msgWhat = msg.what;
            if (msgWhat == mCount.get()) {
                clearWindowFlags();
                removeMessages(msgWhat - 1);    // Remove previous message, if any.
                // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
                // CBC notification with pop up and tone alert + vibrate in CHILE
                // screen off after 10 seconds for Chile
                if (forceVibrateChile) {
                    PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (true == mPowerManager.isScreenOn()) {
                        TLog.d(TAG,
                                "10s, force screen off, removed FLAG_KEEP_SCREEN_ON with id "
                                        + msgWhat);
                        mPowerManager.goToSleep(SystemClock.uptimeMillis());
                    }
                }
                // [FEATURE]-Add-END by TCTNB.Dandan.Fang
                TLog.d(TAG, "removed FLAG_KEEP_SCREEN_ON with id " + msgWhat);
            } else {
                TLog.e(TAG, "discarding screen off message with id " + msgWhat);
            }
        }
    }

    /**
     * Returns the currently displayed message.
     */
    CellBroadcastMessage getLatestMessage() {
        int index = mMessageList.size() - 1;
        if (index >= 0) {
            return mMessageList.get(index);
        } else {
            return null;
        }
    }

    /**
     * Removes and returns the currently displayed message.
     */
    private CellBroadcastMessage removeLatestMessage() {
        int index = mMessageList.size() - 1;
        //[FEATURE]-Add-BEGIN by jian.bu,08/12/2015,1052152
        if (getResources().getBoolean(R.bool.def_showCbMessageAlertByDate_on)) {
            if (index >= 0) {
                CellBroadcastMessage removeCbm = mMessageList.get(index);
                int cbSize = mCBMessageList.size();
                int cbPreSize = mPresidentMessageList.size();

                if (removeCbm.getCmasMessageClass() == SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT) {
                    for (int i = 0; i < cbPreSize; i++) {
                        CellBroadcastMessage tempPreCbm = mPresidentMessageList.get(i);
                        if (removeCbm == tempPreCbm) {
                            TLog.d(TAG, "removeLatestMessage remove mPresidentMessageList index" + i);
                            mPresidentMessageList.remove(i);
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < cbSize; i++) {
                        CellBroadcastMessage tempCbm = mCBMessageList.get(i);
                        if (removeCbm == tempCbm) {
                            TLog.d(TAG, "removeLatestMessage remove mCBMessageList index" + i);
                            mCBMessageList.remove(i);
                            break;
                        }
                    }
                }
            }
        }
        /* MODIFIED-BEGIN by yuwan, 2017-05-30,BUG-4654434*/
        if (getResources().getBoolean(R.bool.def_show4370AlertByDate_on)) {
            if (index >= 0) {
                CellBroadcastMessage removeCbm = mMessageList.get(index);
                int cbSize = mCBMessageList.size();
                int cbPreSize = mPresidentMessageList.size();
                if (removeCbm.getServiceCategory() == SmsCbCmasInfo
                        .CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT) {
                    for (int i = 0; i < cbPreSize; i++) {
                        CellBroadcastMessage tempPreCbm = mPresidentMessageList.get(i);
                        if (removeCbm.getServiceCategory() == MSGID1) {
                            TLog.d(TAG, "4370 removeLatestMessage " +
                                    "remove mPresidentMessageList index" + i);
                            mPresidentMessageList.remove(i);
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < cbSize; i++) {
                        CellBroadcastMessage tempCbm = mCBMessageList.get(i);
                        if (removeCbm == tempCbm) {
                            TLog.d(TAG, "4370 removeLatestMessage remove mCBMessageList index" + i);
                            mCBMessageList.remove(i);
                            break;
                        }
                    }
                }
            }
        }
        /* MODIFIED-END by yuwan,BUG-4654434*/
        //[FEATURE]-Add-END by jian.bu,08/12/2015,1052152
        if (index >= 0) {
            return mMessageList.remove(index);
        } else {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Window win = getWindow();
        //[FEATURE]-Add-BEGIN by TCTNB.yugang.jia,11/15/2013,546255,
        mContext = this;
        //[FEATURE]-Add-END by TCTNB.yugang.jia

        // We use a custom title, so remove the standard dialog title bar
        win.requestFeature(Window.FEATURE_NO_TITLE);

        // Full screen alerts display above the keyguard and when device is locked.
        win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Initialize the view.
        LayoutInflater inflater = LayoutInflater.from(this);
        //PR 1041716 Modified by fang.song begin
        mContentView = (View) (inflater.inflate(getLayoutResId(), null));
        setContentView(mContentView);
        //PR 1041716 Modified by fang.song end
        // [BUGFIX]-ADD-BEGIN by bin.xue for PR-1105873
        mFlList = (FrameLayout) findViewById(R.id.cb_alert_layout);
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        if (wm.getDefaultDisplay().getWidth() < wm.getDefaultDisplay()
                .getHeight()) {
            mFlListWidth = (int) (wm.getDefaultDisplay().getWidth() * 0.89);
        } else {
            mFlListWidth = (int) (wm.getDefaultDisplay().getHeight() * 0.89);
        }
        TLog.d(TAG, "onCreate ContentView's Width:" + mFlListWidth);
        // [BUGFIX]-ADD-END by bin.xue
        Button dismissButton = (Button) findViewById(R.id.dismissButton);
        if (dismissButton != null) {
            // modify by liang.zhang for Defect 4971123 at 2017-07-07 begin
            if (getResources().getBoolean(
                    R.bool.feature_cellbroadcastreceiver_forceVibrateForChile_on)) {
//                    || getResources().getBoolean(
//                            R.bool.def_isSupportCBDialog_forRussia)) {
                // [BUGFIX] by yuwan 05-12-2017
                dismissButton.setText(getResources().getString(R.string.button_hide));
            }
            // modify by liang.zhang for Defect 4971123 at 2017-07-07 end
            dismissButton.setOnClickListener(
                    new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismiss();
                        }
                    });
        }

        // Get message list from saved Bundle or from Intent.
        if (savedInstanceState != null) {
            TLog.d(TAG, "onCreate getting message list from saved instance state");
            mMessageList = savedInstanceState.getParcelableArrayList(
                    CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA);
        } else {
            TLog.d(TAG, "onCreate getting message list from intent");
            Intent intent = getIntent();
            mMessageList = intent.getParcelableArrayListExtra(
                    CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA);

            // If we were started from a notification, dismiss it.
            clearNotification(intent);
        }
        //[FEATURE]-Add-BEGIN by jian.bu,08/12/2015,1052152
        mCBMessageList = new ArrayList<CellBroadcastMessage>();
        mPresidentMessageList = new ArrayList<CellBroadcastMessage>();
        if (getResources().getBoolean(R.bool.def_showCbMessageAlertByDate_on)) {
            TLog.d(TAG, "onCreate loaded message list of size " + mMessageList.size());
            for (int i = 0; i < mMessageList.size(); i++) {
                CellBroadcastMessage tempCbm = mMessageList.get(i);
                if (tempCbm != null) {
                    if (tempCbm.getCmasMessageClass() == SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT) {
                        mPresidentMessageList.add(tempCbm);
                    } else {
                        mCBMessageList.add(tempCbm);
                    }
                }
            }
            TLog.d(TAG, "onCreate loaded mPresidentMessageList size " + mPresidentMessageList.size() + "mCBMessageList size" + mCBMessageList.size());
            if (mMessageList.size() > 1) {
                TLog.d(TAG, "onCreate+ reset the message list, size= " + mMessageList.size());
                mMessageList.clear();
                int cbSize = mCBMessageList.size();
                int cbPreSize = mPresidentMessageList.size();
                for (int i = 0; i < cbSize; i++) {
                    int cbIndex = cbSize - 1 - i;
                    TLog.d(TAG, "onCreate add mCBMessageList i=" + i + " cbIndex=" + cbIndex);
                    CellBroadcastMessage tempCbm = mCBMessageList.get(cbIndex);
                    mMessageList.add(tempCbm);
                }
                for (int i = 0; i < cbPreSize; i++) {
                    int cbPreIndex = cbPreSize - 1 - i;
                    TLog.d(TAG, "onCreate add mPresidentMessageList i=" + i + "cbPreIndex=" + cbPreIndex);
                    CellBroadcastMessage tempPreCbm = mPresidentMessageList.get(cbPreIndex);
                    mMessageList.add(tempPreCbm);
                }
                TLog.d(TAG, "onCreate- reset the message list, size= " + mMessageList.size());
            }
        }
        /* MODIFIED-BEGIN by yuwan, 2017-05-30,BUG-4654434*/
        if (getResources().getBoolean(R.bool.def_show4370AlertByDate_on)) {
            TLog.d(TAG, "4370 onCreate loaded message list of size " + mMessageList.size());
            for (int i = 0; i < mMessageList.size(); i++) {
                CellBroadcastMessage tempCbm = mMessageList.get(i);
                if (tempCbm != null) {
                    if (tempCbm.getServiceCategory() == MSGID1) {
                        mPresidentMessageList.add(tempCbm);
                    } else {
                        mCBMessageList.add(tempCbm);
                    }
                }
            }
            TLog.d(TAG, "4370 onCreate loaded mPresidentMessageList size "
                    + mPresidentMessageList.size() + "mCBMessageList size" + mCBMessageList.size());
            if (mMessageList.size() > 1) {
                TLog.d(TAG, "onCreate+ reset the 4370 message list, size= " + mMessageList.size());
                mMessageList.clear();
                int cbSize = mCBMessageList.size();
                int cbPreSize = mPresidentMessageList.size();
                for (int i = 0; i < cbSize; i++) {
                    TLog.d(TAG, "4370 onCreate add mCBMessageList i=" + i);
                    CellBroadcastMessage tempCbm = mCBMessageList.get(i);
                    mMessageList.add(tempCbm);
                }
                for (int i = 0; i < cbPreSize; i++) {
                    int cbPreIndex = cbPreSize - 1 - i;
                    TLog.d(TAG, "4370 onCreate add mPresidentMessageList i="
                            + i + "cbPreIndex=" + cbPreIndex);
                    CellBroadcastMessage tempPreCbm = mPresidentMessageList.get(cbPreIndex);
                    mMessageList.add(tempPreCbm);
                }
                TLog.d(TAG, "4370 onCreate- reset the message list, size= " + mMessageList.size());
            }
        }
        /* MODIFIED-END by yuwan,BUG-4654434*/
        //[FEATURE]-Add-END by jian.bu
        mWpasEnableFlag = getResources().getBoolean(R.bool.def_enable_wpas_function); // MODIFIED by yuxuan.zhang, 2016-08-24,BUG-1112693
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(this)) {
        	mWpasEnableFlag = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        // begin: aiyan-999810-T-Mobile request for incoming call after WEA message
        if (mContext.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(FINISH_FULL_SCREEN);
            mContext.registerReceiver(mStopitselfReceiver, filter);
        }// end: aiyan-999810-T-Mobile request for incoming call after WEA message
        //PR 1041463, 1043168 Modified by fang.song 2015.07.16 begin
        //if (mMessageList != null) {
        if (mMessageList != null && mMessageList.size() > 0) {
            TLog.d(TAG, "onCreate loaded message list of size " + mMessageList.size());

            // For emergency alerts, keep screen on so the user can read it, unless this is a
            // full screen alert created by CellBroadcastAlertDialog when the screen turned off.
            CellBroadcastMessage message = getLatestMessage();
            repeatmessage = message;//[BUGFIX]-Add- by TCTNB.ke.meng,05/07/2014,642065
            if (CellBroadcastConfigService.isEmergencyAlertMessage(message) &&
                    (savedInstanceState != null ||
                            !getIntent().getBooleanExtra(SCREEN_OFF_EXTRA, false))) {
                TLog.d(TAG, "onCreate setting screen on timer for emergency alert");
                mScreenOffHandler.startScreenOnTimer();
            }

            //[FEATURE]-Mod-BEGIN by TCTNB.yugang.jia, 09/11/2013, FR-516039,
            // to make the message text linkable.
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1748495*/
            mMessageView = (TextView) findViewById(R.id.message);
            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,03/02/2015,928255,[SS][SCB]The phone cannot parse the short phone number while receive CB/CMAS
            //mMessageView.setAutoLinkMask(CBLinkify.ALL);
            //[BUGFIX]-Add-END by TSCD.fujun.yang
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metric);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-08-31,BUG-2813031*/
            if (mWpasEnableFlag) {
                Log.i(TAG, "density = " + metric.density);
                int density = (int) metric.density;
                if (density < 3) {
                    density = 3;
                }
                mMessageView.setTextSize(density * 7);
                /* MODIFIED-END by yuxuan.zhang,BUG-2813031*/
            }
            mMessageView.setText(
            /* MODIFIED-END by yuxuan.zhang,BUG-1748495*/
                    CellBroadcastResources.getMessageDetails(this, message));
            //[FEATURE]-Mod-END by TCTNB.yugang.jia
            //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
            //CBC notification with pop up and tone alert + vibrate in CHILE
            //updateAlertText(message);
            Intent intent = getIntent();
            forceVibrateChile = intent.getBooleanExtra("forceVibrate", false);
            if (forceVibrateChile) {
                TLog.i(TAG, "onCreate: startScreenOnTimer ");
                mScreenOffHandler.startScreenOnTimer();
                TLog.i(TAG, "onCreate:display specified title, date, time and message content for chile ");
                updateAlertText(message, forceVibrateChile);
            } else {
                updateAlertText(message);
            }
            //[FEATURE]-Add-END by TCTNB.Dandan.Fang
        } else {
            TLog.e(TAG, "onCreate failed to get message list from saved Bundle");
            finish();
        }
        //PR 1041463, 1043168 Modified by fang.song 2015.07.16 end
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-08-24,BUG-1112693*/
    @Override
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        // TODO Auto-generated method stub
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        TLog.i(TAG, "windowManager.getDefaultDisplay().getRotation() = " + windowManager.getDefaultDisplay().getRotation()); // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1748495
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                mPortraitFlag = true;
                break;
            case Surface.ROTATION_90:
                mPortraitFlag = false;
                break;
            case Surface.ROTATION_180:
                mPortraitFlag = true;
                break;
            case Surface.ROTATION_270:
                mPortraitFlag = false;
                break;
        }
        mWpasEnableFlag = getResources().getBoolean(R.bool.def_enable_wpas_function);
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(this)) {
        	mWpasEnableFlag = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        if (mWpasEnableFlag) {
            super.onApplyThemeResource(theme, R.style.WpasAlertFullScreenTheme, first);
        } else {
            super.onApplyThemeResource(theme, resid, first);
        }
    }

    protected void onApplyDialogThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    /**
     * Called by {@link CellBroadcastAlertService} to add a new alert to the stack.
     *
     * @param intent The new intent containing one or more {@link CellBroadcastMessage}s.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        ArrayList<CellBroadcastMessage> newMessageList = intent.getParcelableArrayListExtra(
                CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA);
        if (newMessageList != null) {
            TLog.d(TAG, "onNewIntent called with message list of size " + newMessageList.size());
            // notification is not new message. aiyan-999810
            // modify by liang.zhang for Defect 5628705 at 2017-12-02 begin
            if (!intent.getBooleanExtra(FROM_NOTIFICATION_EXTRA, false)) {
            // modify by liang.zhang for Defect 5628705 at 2017-12-02 end
                mMessageList.addAll(newMessageList);
                //[FEATURE]-Add-BEGIN by jian.bu,08/12/2015,1052152
                if (getResources().getBoolean(R.bool.def_showCbMessageAlertByDate_on)) {
                    TLog.d(TAG, "onNewIntent called with new message list of size " + newMessageList.size());
                    TLog.d(TAG, "onNewIntent + loaded message list of size " + mMessageList.size());
                    for (int i = 0; i < newMessageList.size(); i++) {
                        CellBroadcastMessage tempCbm = newMessageList.get(i);
                        if (tempCbm != null) {
                            if (tempCbm.getCmasMessageClass() == SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT) {
                                mPresidentMessageList.add(tempCbm);
                            } else {
                                mCBMessageList.add(tempCbm);
                            }
                        }
                    }
                    TLog.d(TAG, "onNewIntent loaded mPresidentMessageList size " + mPresidentMessageList.size() + "mCBMessageList size" + mCBMessageList.size());
                    if (mMessageList.size() > 1) {
                        TLog.d(TAG, "onNewIntent loaded message list of size " + mMessageList.size());
                        mMessageList.clear();
                        int cbSize = mCBMessageList.size();
                        int cbPreSize = mPresidentMessageList.size();
                        for (int i = 0; i < cbSize; i++) {
                            int cbIndex = cbSize - 1 - i;
                            TLog.d(TAG, "onNewIntent add mCBMessageList i=" + i + " cbIndex=" + cbIndex);
                            CellBroadcastMessage tempCbm = mCBMessageList.get(cbIndex);
                            mMessageList.add(tempCbm);
                        }
                        for (int i = 0; i < cbPreSize; i++) {
                            int cbPreIndex = cbPreSize - 1 - i;
                            TLog.d(TAG, "onNewIntent add mPresidentMessageList i=" + i + " cbPreIndex=" + cbPreIndex);
                            CellBroadcastMessage tempPreCbm = mPresidentMessageList.get(cbPreIndex);
                            mMessageList.add(tempPreCbm);
                        }
                        TLog.d(TAG, "onNewIntent - loaded message list of size" + mMessageList.size());
                    }
                }

                /* MODIFIED-BEGIN by yuwan, 2017-05-30,BUG-4654434*/
                if (getResources().getBoolean(R.bool.def_show4370AlertByDate_on)) {
                    TLog.d(TAG, "4370 onNewIntent called with new message list of size "
                            + newMessageList.size());
                    TLog.d(TAG, "4370 onNewIntent + loaded message list of size "
                            + mMessageList.size());
                    for (int i = 0; i < newMessageList.size(); i++) {
                        CellBroadcastMessage tempCbm = newMessageList.get(i);
                        if (tempCbm != null) {
                            if (tempCbm.getServiceCategory() == MSGID1) {
                                mPresidentMessageList.add(tempCbm);
                            } else {
                                mCBMessageList.add(tempCbm);
                            }
                        }
                    }
                    TLog.d(TAG, "4370 onCreate loaded mPresidentMessageList size "
                            + mPresidentMessageList.size() + "mCBMessageList size"
                            + mCBMessageList.size());
                    if (mMessageList.size() > 1) {
                        TLog.d(TAG, "4370 onNewIntent loaded message list of size "
                                + mMessageList.size());
                        mMessageList.clear();
                        int cbSize = mCBMessageList.size();
                        int cbPreSize = mPresidentMessageList.size();
                        for (int i = 0; i < cbSize; i++) {
                            TLog.d(TAG, "4370 onNewIntent add mCBMessageList i=" + i
                                    + " cbIndex=" + i);
                            CellBroadcastMessage tempCbm = mCBMessageList.get(i);
                            mMessageList.add(tempCbm);
                        }
                        for (int i = 0; i < cbPreSize; i++) {
                            int cbPreIndex = cbPreSize - 1 - i;
                            TLog.d(TAG, "4370 onNewIntent add mPresidentMessageList i="
                                    + i + "cbPreIndex=" + cbPreIndex);
                            CellBroadcastMessage tempPreCbm = mPresidentMessageList.get(cbPreIndex);
                            mMessageList.add(tempPreCbm);
                        }
                        TLog.d(TAG, "4370 onNewIntent reset the message list, size= "
                                + mMessageList.size());
                    }
                }
                /* MODIFIED-END by yuwan,BUG-4654434*/
                //[FEATURE]-Add-END by jian.bu,08/12/2015,1052152
            }
            //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
            //CBC notification with pop up and tone alert + vibrate in CHILE
            //updateAlertText(getLatestMessage());
            forceVibrateChile = intent.getBooleanExtra("forceVibrate", false);
            if (forceVibrateChile) {
                TLog.i(TAG, "onNewIntent:startScreenOnTimer ");
                mScreenOffHandler.startScreenOnTimer();
                TLog.i(TAG, "onNewIntent:display specified title, date, time and message content for chile ");
                updateAlertText(getLatestMessage(), forceVibrateChile);
            } else {
                updateAlertText(getLatestMessage());
            }
            //[FEATURE]-Add-END by TCTNB.Dandan.Fang
            // If the new intent was sent from a notification, dismiss it.
            clearNotification(intent);
        } else {
            TLog.e(TAG, "onNewIntent called without SMS_CB_MESSAGE_EXTRA, ignoring");
        }
    }

    /**
     * Try to cancel any notification that may have started this activity.
     */
    private void clearNotification(Intent intent) {
        if (intent.getBooleanExtra(FROM_NOTIFICATION_EXTRA, false)) {
            /* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
            ArrayList<CellBroadcastMessage> newMessageList = intent.getParcelableArrayListExtra(
                    CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA);
                    /* MODIFIED-END by yuwan,BUG-4623008*/
            TLog.d(TAG, "Dismissing notification");
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(CellBroadcastAlertService.NOTIFICATION_ID);
            notificationManager.cancel(CellBroadcastAlertService.EMERGENCY_NOTIFICATION_ID);
            //[BUGFIX]-MOD-BEGIN by TCTNB.ke.meng,05/07/2014,642065
            //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/24/2015,926300,[Pre-CTS][CB]Header for CB message in notification panel should be changed
            /* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
            if ((this.getResources().getBoolean
                    (R.bool.feature_cellbroadcastreceiver_CBReceiverMode_on)
                    || this.getResources().
                    getBoolean(R.bool.feature_cellbroadcastreceiver_displayChannelId)) &&
                    !getBaseContext().getResources().getBoolean(
                            R.bool.def_isSupportNotification_forRussia)) {
                cancelNotificationId(CellBroadcastReceiverApp.notificationid);
            } else if (getBaseContext().getResources().getBoolean(
                    R.bool.def_isSupportNotification_forRussia)) {
                for (CellBroadcastMessage cbm : newMessageList) {
                    cancelNotificationId(cbm.getServiceCategory());
                }
                /* MODIFIED-END by yuwan,BUG-4623008*/
            }
            //[BUGFIX]-Add-END by TSCD.fujun.yang
            //[BUGFIX]-MOD-BEGIN by TCTNB.ke.meng,05/07/2014,642065
            CellBroadcastReceiverApp.clearNewMessageList();
            isFromNotification = false;// aiyan-999810
        }
    }

    /**
     * Save the list of messages so the state can be restored later.
     *
     * @param outState Bundle in which to place the saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA, mMessageList);
        TLog.d(TAG, "onSaveInstanceState saved message list to bundle");
    }

    /**
     * Returns the resource ID for either the full screen or dialog layout.
     */
    protected int getLayoutResId() {
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-08-24,BUG-1112693*/
        Log.i(TAG, "mPortraitFlag = " + mPortraitFlag);
        isFullScreenExist = true;// aiyan-999810
        if (!mWpasEnableFlag) {
            return R.layout.cell_broadcast_alert_fullscreen;
        } else {
            return R.layout.cell_broadcast_alert;
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    }
    
    // add by liang.zhang for Defect 5960227 at 2018-02-01 begin
    private boolean isUAEMessage(CellBroadcastMessage message) {
    	SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
        SubscriptionInfo subInfo = subscriptionManager.getActiveSubscriptionInfo(message.getSubId());
        if (subInfo!= null && subInfo.getMcc() == 424) {
        	return true;
        }
        
        if (subInfo == null) {
        	List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info!= null && info.getMcc() == 424) {
        	        	return true;
        	        }
        		}
        	}
        }
        return false;
    }
    // add by liang.zhang for Defect 5960227 at 2018-02-01 end
    
    // add by liang.zhang for Defect 6012945 at 2018-03-07 begin
    private int isLATAMMessage(CellBroadcastMessage message) {
    	SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
        SubscriptionInfo subInfo = subscriptionManager.getActiveSubscriptionInfo(message.getSubId());
        if (subInfo!= null && subInfo.getMcc() == 716) {
        	return 1;
        } else if (subInfo!= null && subInfo.getMcc() == 730) {
        	return 2;
        } else if (subInfo!= null && subInfo.getMcc() == 334) {
        	return 3;
        }
        
        if (subInfo == null) {
        	List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info!= null && info.getMcc() == 716) {
        	        	return 1;
        	        } else if (info!= null && info.getMcc() == 730) {
        	        	return 2;
        	        } else if (info!= null && info.getMcc() == 334) {
        	        	return 3;
        	        }
        		}
        	}
        }
        return -1;
    }
    // add by liang.zhang for Defect 6012945 at 2018-03-07 end
    
	// add by liang.zhang for Defect 6369692 at 2018-06-07 begin
    private boolean isNewZealandMessage(CellBroadcastMessage message) {
    	SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
        SubscriptionInfo subInfo = subscriptionManager.getActiveSubscriptionInfo(message.getSubId());
        if (subInfo!= null && subInfo.getMcc() == 530) {
        	return true;
        }
        
        if (subInfo == null) {
        	List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info!= null && info.getMcc() == 530) {
        	        	return true;
        	        }
        		}
        	}
        }
        return false;
    }
	// add by liang.zhang for Defect 6369692 at 2018-06-07 end

  // add by wangyong for Defect 7143375 at2018-12-06 begin
    public  Context getLocalLanguage(String local){
            Configuration cofig= getResources().getConfiguration(); 
            cofig.setLocale(new Locale(local));
            Context con = getBaseContext().createConfigurationContext(cofig);
            return con;
    }
     // add by wangyong for Defect 7143375 at2018-12-06   end

    /**
     * Update alert text when a new emergency alert arrives.
     */
    private void updateAlertText(CellBroadcastMessage message) {
        TLog.d(TAG, "updateAlertText(CellBroadcastMessage message)  is running");
        int titleId = CellBroadcastResources.getDialogTitleResource(message);
        setTitle(titleId);
       CharSequence stringCharSequence = getText(titleId);
         
         // modify by deqin.tang for Defect 7143375 at2018-12-06 begin
        // add by liang.zhang for Defect 5960227 at 2018-02-01 begin
       if (isUAEMessage(message)) {
        	/*int UAETitleId = CellBroadcastResources.getDialogTitleResourceForUAE(message);
        	if (UAETitleId != -1) {
        		stringCharSequence = getText(UAETitleId);
        	}*/
               CellBroadcastResources.DialogTitleReturnForUAE mDialogTitleReturnForUAE = CellBroadcastResources.getDialogTitleResourceForUAE(message);
                TLog.d(TAG, "mDialogTitleReturnForUAE .language= "+mDialogTitleReturnForUAE.language+" mDialogTitleReturnForUAE.titleid= "+mDialogTitleReturnForUAE.titleid);
                if (mDialogTitleReturnForUAE != null) {
                stringCharSequence = getLocalLanguage(mDialogTitleReturnForUAE.language).getText(mDialogTitleReturnForUAE.titleid);
                TLog.d(TAG, "stringCharSequence ="+stringCharSequence);
            }
        }
        // add by liang.zhang for Defect 5960227 at 2018-02-01 end
      // modify by deqin.tang for Defect 7143375 at2018-12-06 begin

        // add by liang.zhang for Defect 6012945 at 2018-03-07 begin
        if (isLATAMMessage(message) == 1 || isLATAMMessage(message) == 2) {
        	int PeruTitleId = CellBroadcastResources.getDialogTitleResourceForPeru(message);
        	if (PeruTitleId != -1) {
        		stringCharSequence = getText(PeruTitleId);
        	}
        } else if (isLATAMMessage(message) == 3) {
        	int MexicoTitleId = CellBroadcastResources.getDialogTitleResourceForMexico(message);
        	if (MexicoTitleId != -1) {
        		stringCharSequence = getText(MexicoTitleId);
        	}
        }
        // add by liang.zhang for Defect 6012945 at 2018-03-07 end
        
    	// add by liang.zhang for Defect 6369692 at 2018-06-07 begin
        if (isNewZealandMessage(message)) {
        	int NZTitleId = CellBroadcastResources.getDialogTitleResourceForNZ(message);
        	if (NZTitleId != -1) {
        		stringCharSequence = getText(NZTitleId);
        	}
        }
    	// add by liang.zhang for Defect 6369692 at 2018-06-07 end
        
        //BUGFIX BEGIN by guolin.chen for PR1105891 at 2015/11/3
        if (getBaseContext().getResources().getBoolean(
                R.bool.cellbroadcastreceiver_displayChannelName)) {
            // modify by liang.zhang for Defect 4971123 at 2017-07-07 begin
            stringCharSequence = getchannelNamebychanid(stringCharSequence, message);
            // modify by liang.zhang for Defect 4971123 at 2017-07-07 end
        }
        String channelName = " ";
        TextView alertTitle = (TextView) findViewById(R.id.alertTitle);
        
        // modify by liang.zhang for Defect 6012945 at 2018-03-07 begin
        Locale curLocale = getResources().getConfiguration().locale;
     	if (isLATAMMessage(message) == 1 && isPeruCMASChannel(message.getServiceCategory())) {
     		alertTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
     		alertTitle.setMarqueeRepeatLimit(3);
     		alertTitle.setSelected(true);
     	} else {
     		alertTitle.setEllipsize(TextUtils.TruncateAt.END);
     	}
        //BUGFIX END by guolin.chen for PR1105891 at 2015/11/3
        //[BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,01/03/2015,888411
        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/24/2015,926300,[Pre-CTS][CB]Header for CB message in notification panel should be changed
        if ((!message.isEmergencyAlertMessage() && this.getResources().
                getBoolean(R.bool.feature_cellbroadcastreceiver_CBReceiverMode_on) &&
                !this.getResources().getBoolean(R.bool.def_isSupportCBDialog_forRussia))
                || !message.isEmergencyAlertMessage() && this.getResources().
                getBoolean(R.bool.feature_cellbroadcastreceiver_displayChannelId)) {
        	alertTitle.setText(stringCharSequence +
                    "(" + message.getServiceCategory() + ")");
            //[Defect]--BEGIN by yuwan,02/05/2017,4621584
        } else if (!message.isEmergencyAlertMessage() &&
                this.getResources().getBoolean(R.bool.def_isSupportCBDialog_forRussia)) {
            // modify by liang.zhang for Defect 4971123 at 2017-07-07 begin
            channelName = getchannelNamebychanid(channelName, message);
            // modify by liang.zhang for Defect 4971123 at 2017-07-07 end
            alertTitle.setText(getString(R.string.Russia_dialog_title).toString() + " " + channelName
                            + "(" + message.getServiceCategory() + ")");
        } else {//[Defect]--END by yuwan,02/05/2017,4621584
        	alertTitle.setText(stringCharSequence);
        }
        // modify by liang.zhang for Defect 6012945 at 2018-03-07 end
        
        //[BUGFIX]-Add-END by TSCD.fujun.yang
        //[BUGFIX]-Mod-END by TSCD.tianming.lei
        //[FEATURE]-Add-BEGIN by TCTNB.yugang.jia,11/15/2013,546255,
        TextView mMessageView = (TextView) findViewById(R.id.message); // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1748495
        //((TextView) findViewById(R.id.message)).setText(message.getMessageBody());
        //[BUGFIX]-Add-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
        //[BUGFIX]-Add-Mod by TSCD.bangjun.wang,01/21/2015,886044
        //date time
        TextView datetime = ((TextView) findViewById(R.id.datetime));
        datetime.setVisibility(View.VISIBLE);
        TextView date = ((TextView) findViewById(R.id.date));
        date.setVisibility(View.VISIBLE);
        // add by liang.zhang for Defect 4971123 at 2017-07-06 begin
        if (getResources().getBoolean(R.bool.def_isSupportCBDialog_forRussia)) {
        	datetime.setVisibility(View.GONE);
        	date.setVisibility(View.GONE);
        }
        // add by liang.zhang for Defect 4971123 at 2017-07-06 end
        long mDeliveryTime = message.getDeliveryTime();
        Date date1 = new Date(mDeliveryTime);
        SimpleDateFormat timeFormat = null;
        String sTime = null;
        String _12or24Hour = Settings.System.getString(getContentResolver(), Settings.System.TIME_12_24);
        if (!TextUtils.isEmpty(_12or24Hour) && _12or24Hour.equals("24")) { // MODIFIED by yuxuan.zhang, 2016-07-29,BUG-1112693
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            sTime = timeFormat.format(date1);
        } else {
            timeFormat = new SimpleDateFormat("hh:mm:ss");
            Calendar calendar = timeFormat.getCalendar();
            sTime = timeFormat.format(date1);
            if (calendar.get(Calendar.AM_PM) == 0) {
                sTime += "  AM";
            } else {
                sTime += "  PM";
            }
        }
        //PR 1031792 Added by fang.song begin
        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String defaultFormat = "";
        String currentFormat = Settings.System.getString(getContentResolver(),
                Settings.System.DATE_FORMAT);
        SimpleDateFormat dateFormat;
        if (this.getResources().getBoolean(R.bool.def_useUSDateFormat_on)) {
            defaultFormat = "MM-dd-yyyy";
        } else {
            if (currentFormat != null && !currentFormat.equals("")) {
                defaultFormat = currentFormat;
            } else {
                defaultFormat = "MM-dd-yyyy";
            }
        }
        dateFormat = new SimpleDateFormat(defaultFormat);
        //PR 1031792 Added by fang.song end
        String sDate = dateFormat.format(date1);
        date.setText(sDate);
        datetime.setText(sTime);
        //[BUGFIX]-Mod-END by TSCD.bangjun.wang
        //[BUGFIX]-Add-END by TSCD.tianming.lei
        //ADD-Alert-ID-begin-by-chaobing-9/14/2015-PR1084768
        int serviceCategory = message.getServiceCategory();
        boolean isShowCMASDialogId = mContext.getResources().getBoolean(R.bool.def_showCMASDialogId);
        if (isShowCMASDialogId && serviceCategory >= MSGID1 && serviceCategory <= MSGID12) {
            if (serviceCategory >= MSGID2 && serviceCategory <= MSGID9) {
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1748495*/
                mMessageView.setText(
                        CellBroadcastResources.getMessageDetails(this, message));
            } else {
                mMessageView.setText("ID: MsgId" + (serviceCategory - 4369) + "\n" + message.getMessageBody());
            }
        } else {
        	// modify by liang.zhang for Defect 6564926 at 2018-07-09 begin
            if (isNewZealandMessage(message)) {
            	String body = message.getMessageBody();
            	switch (message.getServiceCategory()) {
            	case 4371:
            	case 4372:
            		body = body + "\n" + "\n" + getResources().getString(R.string.enable_new_zealand_extreme_threat_alerts_title);
            		break;
            		
            	case 4373:
            	case 4374:
            	case 4375:
            	case 4376:
            	case 4377:
            	case 4378:
            		body = body + "\n" + "\n" + getResources().getString(R.string.enable_new_zealand_severe_threat_alerts_title);
            		break;
            		
            	default:
            		break;
            	}
            	mMessageView.setText(body);
            } else {
            	mMessageView.setText(message.getMessageBody());
            }
        	// modify by liang.zhang for Defect 6564926 at 2018-07-09 end
        }
        //ADD-Alert-ID-END-by-chaobing-9/14/2015-PR1084768
        // 999725-Deleted by aiyan for T-mobile request
        if (!mContext.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
            // [BUGFIX]-Add-BEGIN by TSCD.fujun.yang,03/02/2015,928255,[SS][SCB]The phone cannot
            // parse the short phone number while receive CB/CMAS
            //[modify]-begin-by-chaobing.huang-01162017-defect4014021
            if (mContext.getResources().getBoolean(R.bool.def_isSupportHyperlink_sprint)) {
                if (serviceCategory == SmsEnvelope.SERVICE_CATEGORY_CMAS_PRESIDENTIAL_LEVEL_ALERT) {
                    CBLinkify.addLinks(mMessageView, CBLinkify.WEB_URLS | CBLinkify.PHONE_NUMBERS);
                }
            } else {
                CBLinkify.addLinks(mMessageView, CBLinkify.ALL);
            }
            //[modify]-end-by-chaobing.huang-01162017-defect4014021
            // [BUGFIX]-Add-END by TSCD.fujun.yang
            // [CDR-ECB-690/700]Message Parsing—Phone / E-mail
            URLSpan[] spans = mMessageView.getUrls();
            CharSequence text = mMessageView.getText();
            if (text instanceof Spannable) {
                int end = text.length();
                Spannable sp = (Spannable) mMessageView.getText();
                SpannableStringBuilder style = new SpannableStringBuilder(text);
                style.clearSpans();
                for (URLSpan url : spans) {
                    myUrlSpan myurlSpan = new myUrlSpan(mContext, mMessageView, url.getURL());
                    style.setSpan(myurlSpan, sp.getSpanStart(url), sp.getSpanEnd(url),
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                //ADD-Alert-ID-begin-by-chaobing-9/14/2015-PR1084768
                //mMessageView.setText(style);
                //mMessageView.setTextSize(16);
                /* MODIFIED-END by yuxuan.zhang,BUG-1748495*/
                //ADD-Alert-ID-end-by-chaobing-9/14/2015-PR1084768
            }
            // [FEATURE]-MOD-END by TCTNB.yugang.jia
        }

        // Set alert reminder depending on user preference
        // begin：aiyan-979267 add T-Mobile remind request
        if (mContext.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
            TLog.d(TAG, "mMsgFirstCome:" + mMsgFirstCome);
            if (mMsgFirstCome) {
                CellBroadcastAlertReminder.queueAlertReminderTmo(this, true, message);
                mMsgFirstCome = false;
            }
        } else {
            CellBroadcastAlertReminder.queueAlertReminder(this, true, message.getSubId());
        }
        // end：aiyan-979267 add T-Mobile remind request
    }
    
    public String getchannelNamebychanid(CharSequence channelNameo, CellBroadcastMessage message){
    	Uri uri = null;
    	if (TelephonyManager.getDefault().isMultiSimEnabled()) {
    		int subId = message.getSubId();
    		if (subId == 0) {
    			uri = Channel.CONTENT_URISIM1;
    		} else {
    			uri = Channel.CONTENT_URISIM2;
    		}
    	} else {
    		uri = Channel.CONTENT_URI;
    	}
    	
        Cursor c = getContentResolver().query(uri,
                 new String[] {Channel.NAME},Channel.INDEX + " = ?",new String[]{Integer.toString(message.getServiceCategory())},null);
        String channelName = channelNameo+"";
         if (c.getCount() > 0) {
             c.moveToFirst();
             if(c.getString(0) != null && !c.getString(0).equals("")){
             channelName = c.getString(0);
             }
         }
         c.close();
         return channelName;
     }

    // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
    // CBC notification with pop up and tone alert + vibrate in CHILE
    // display specified title, date and message content for child request
    private void updateAlertText(CellBroadcastMessage message,
                                 boolean forceVibrateChile) {
        // title ,date, message content
        if (forceVibrateChile) {
            // title
            String chileTitle = null;
            chileTitle = getResources().getString(
                    R.string.title_chile_cb_dialog);
            setTitle(chileTitle);
            ((TextView) findViewById(R.id.alertTitle)).setText(chileTitle);

            //date time
            //[modify]-begin-by-chaobing.huang-defect4005963
            //TextView datetime = ((TextView) findViewById(R.id.datetime));
            //datetime.setVisibility(View.VISIBLE);
            //datetime.setText(message
            //.getDateString(getBaseContext()));
            //[modify]-end-by-chaobing.huang-defect4005963
            // message content
            //[FEATURE]-Add-BEGIN by TCTNB.yugang.jia,11/15/2013,546255,
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1748495*/
            TextView mMessageView = (TextView) findViewById(R.id.message);
            //((TextView) findViewById(R.id.message)).setText(message.getMessageBody());
            //date time
            //[modify]-begin-by-chaobing.huang-defect4005963
            TextView datetime = ((TextView) findViewById(R.id.datetime));
            datetime.setVisibility(View.VISIBLE);
            TextView date = ((TextView) findViewById(R.id.date));
            date.setVisibility(View.VISIBLE);
            // add by liang.zhang for Defect 4971123 at 2017-07-06 begin
            if (getResources().getBoolean(R.bool.def_isSupportCBDialog_forRussia)) {
            	datetime.setVisibility(View.GONE);
            	date.setVisibility(View.GONE);
            }
            // add by liang.zhang for Defect 4971123 at 2017-07-06 end
            long mDeliveryTime = message.getDeliveryTime();
            Date date1 = new Date(mDeliveryTime);
            SimpleDateFormat timeFormat = null;
            String sTime = null;
            String _12or24Hour = Settings.System.getString(getContentResolver(), Settings.System.TIME_12_24);
            if (!TextUtils.isEmpty(_12or24Hour) && _12or24Hour.equals("24")) { // MODIFIED by yuxuan.zhang, 2016-07-29,BUG-1112693
                timeFormat = new SimpleDateFormat("HH:mm:ss");
                sTime = timeFormat.format(date1);
            } else {
                timeFormat = new SimpleDateFormat("hh:mm:ss");
                Calendar calendar = timeFormat.getCalendar();
                sTime = timeFormat.format(date1);
                if (calendar.get(Calendar.AM_PM) == 0) {
                    sTime += "  AM";
                } else {
                    sTime += "  PM";
                }
            }
            //SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String defaultFormat = "";
            String currentFormat = Settings.System.getString(getContentResolver(),
                    Settings.System.DATE_FORMAT);
            SimpleDateFormat dateFormat;
            if (this.getResources().getBoolean(R.bool.def_useUSDateFormat_on)) {
                defaultFormat = "MM-dd-yyyy";
            } else {
                if (currentFormat != null && !currentFormat.equals("")) {
                    defaultFormat = currentFormat;
                } else {
                    defaultFormat = "MM-dd-yyyy";
                }
            }
            dateFormat = new SimpleDateFormat(defaultFormat);
            String sDate = dateFormat.format(date1);
            date.setText(sDate);
            datetime.setText(sTime);
            //[modify]-end-by-chaobing.huang-defect4005963
            //ADD-Alert-ID-begin-by-chaobing-9/14/2015-PR1084768
            int serviceCategory = message.getServiceCategory();
            boolean isShowCMASDialogId = mContext.getResources().getBoolean(R.bool.def_showCMASDialogId);
            if (isShowCMASDialogId && serviceCategory >= MSGID1 && serviceCategory <= MSGID12) {
                if (serviceCategory >= MSGID2 && serviceCategory <= MSGID9) {
                    mMessageView.setText(
                            CellBroadcastResources.getMessageDetails(this, message));
                } else {
                    mMessageView.setText("ID: MsgId" + (serviceCategory - 4369) + "\n" + message.getMessageBody());
                }
            } else {
                mMessageView.setText(message.getMessageBody());
            /* MODIFIED-END by yuxuan.zhang,BUG-1748495*/
            }
            //ADD-Alert-ID-END-by-chaobing-9/14/2015-PR1084768

            // 999725-Deleted by aiyan for T-mobile request
            if (!mContext.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
                // [BUGFIX]-Add-BEGIN by TSCD.fujun.yang,03/02/2015,928255,[SS][SCB]The phone cannot
                // parse the short phone number while receive CB/CMAS
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1748495*/
                //[modify]-begin-by-chaobing.huang-01162017-defect4014021
                if (mContext.getResources().getBoolean(R.bool.def_isSupportHyperlink_sprint)) {
                    if (serviceCategory == SmsEnvelope.SERVICE_CATEGORY_CMAS_PRESIDENTIAL_LEVEL_ALERT) {
                        CBLinkify.addLinks(mMessageView, CBLinkify.WEB_URLS | CBLinkify.PHONE_NUMBERS);
                    }
                } else {
                    CBLinkify.addLinks(mMessageView, CBLinkify.ALL);
                }
                //[modify]-end-by-chaobing.huang-01162017-defect4014021
                // [BUGFIX]-Add-END by TSCD.fujun.yang
                // [CDR-ECB-690/700]Message Parsing—Phone / E-mail
                URLSpan[] spans = mMessageView.getUrls();
                CharSequence text = mMessageView.getText();
                if (text instanceof Spannable) {
                    int end = text.length();
                    Spannable sp = (Spannable) mMessageView.getText();
                    SpannableStringBuilder style = new SpannableStringBuilder(text);
                    style.clearSpans();
                    for (URLSpan url : spans) {
                        myUrlSpan myurlSpan = new myUrlSpan(mContext, mMessageView, url.getURL());
                        style.setSpan(myurlSpan, sp.getSpanStart(url), sp.getSpanEnd(url),
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    //ADD-Alert-ID-begin-by-chaobing-9/14/2015-PR1084768
                    //mMessageView.setText(style);
                    //mMessageView.setTextSize(16);
                    /* MODIFIED-END by yuxuan.zhang,BUG-1748495*/
                    //ADD-Alert-ID-end-by-chaobing-9/14/2015-PR1084768
                }
                // [FEATURE]-MOD-END by TCTNB.yugang.jia
            }
        }
    }
    // [FEATURE]-Add-END by TCTNB.Dandan.Fang


    //[FEATURE]-Add-BEGIN by TCTNB.yugang.jia,11/15/2013,546255,
    //[CDR-ECB-690/700]Message Parsing—Phone / E-mail
    public class myUrlSpan extends ClickableSpan {
        public String mUrl;
        private TextView mTv;
        private Context mContext;
        final String mTelPrefix = "tel:";
        final String mEmailPrefix = "mailto:";
        final String mSmsPrefix = "smsto:";

        myUrlSpan(Context context, TextView mTextView, String url) {
            mUrl = url;
            mTv = mTextView;
            mContext = context;
        }

        @Override
        public void onClick(View widget) {
            if (mUrl.startsWith(mTelPrefix)) {
                URLSpan[] TempSpan = new URLSpan[2];
                TempSpan[0] = new URLSpan(mTelPrefix + mUrl.substring(mUrl.indexOf(":") + 1));
                TempSpan[1] = new URLSpan(mSmsPrefix + mUrl.substring(mUrl.indexOf(":") + 1));
                dealClick(TempSpan);

            } else if (mUrl.startsWith(mEmailPrefix)) {
                URLSpan url = new URLSpan(mSmsPrefix + mUrl.substring(mUrl.indexOf(":") + 1));
                url.onClick(mTv);
            } else {
                URLSpan url = new URLSpan(mUrl);
                url.onClick(mTv);
            }
        }

        public void dealClick(URLSpan[] spans) {
            final URLSpan[] Spans = spans;
            ArrayAdapter<URLSpan> adapter = new ArrayAdapter<URLSpan>(mContext,
                    android.R.layout.select_dialog_item, Spans) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    URLSpan span = getItem(position);
                    String url = span.getURL();
                    TextView tv = (TextView) v;
                    if (url.startsWith(mTelPrefix)) {
                        tv.setText(mContext.getResources().getString(R.string.call_phone_att));
                    } else if (url.startsWith(mSmsPrefix)) {
                        tv.setText(mContext.getResources().getString(R.string.Send_message_att));
                    }
                    return v;
                }
            };

            AlertDialog.Builder b = new AlertDialog.Builder(mContext);
            DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialog, int which) {
                    if (which >= 0) {
                        Spans[which].onClick(mTv);
                    }
                    dialog.dismiss();
                }
            };

            b.setTitle(spans[0].getURL().substring(spans[0].getURL().indexOf(":") + 1));
            b.setCancelable(true);
            b.setAdapter(adapter, click);

            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            b.show();
        }
    }
    //[FEATURE]-Add-END by TCTNB.yugang.jia

    /**
     * Start animating warning icon.
     */
    @Override
    protected void onResume() {
        TLog.d(TAG, "onResume called");
        super.onResume();
        //[add]-begin-by-chaobing.huang-defect3857798-27.12.2016
        if (getResources().getBoolean(R.bool.feature_enable_cmas_light_up_screen)) {
            keepScreenOn(mContext, true);
            mScreenOffHandler.stopScreenOnTimer();
        }
        //[add]-end-by-chaobing.huang-defect3857798-27.12.2016
        // [BUGFIX]-ADD-BEGIN by bin.xue for PR-1105873
        if (mFlList != null && mFlListWidth > 0 && (mPortraitFlag || !mWpasEnableFlag)) { // MODIFIED by yuxuan.zhang, 2016-08-24,BUG-1112693
            TLog.d(TAG, "mFlListWidth:" + mFlListWidth);
            mFlList.getLayoutParams().width = mFlListWidth;
        }
        // [BUGFIX]-ADD-END by bin.xue
        CellBroadcastMessage message = getLatestMessage();
        if (message != null && CellBroadcastConfigService.isEmergencyAlertMessage(message)) {
            //PR 1041716 Added by fang.song begin
            //[modify]-begin-by-chaobing.huang-22.12.2016-defect3823603
            if (mContext.getResources().getBoolean(R.bool.def_cellbroadcastreceiver_disable_functionkey)
                /* MODIFIED-BEGIN by chaobing.huang, 2016-12-22,BUG-3823603*/
                    /* MODIFIED-BEGIN by yuwan, 2017-06-08,BUG-4887465*/
                    && Settings.Global.getInt(
                    /* MODIFIED-BEGIN by yuwan, 2017-06-14,BUG-4887465*/
                    mContext.getContentResolver(), LOW_STORAGE_MODE, 0) != 1 &&
                    !mContext.getResources().getBoolean(R.bool.def_cb_4370_all_softkey_disable)) {
                Log.d(TAG, "all cmas functionkey = true");
                this.setFinishOnTouchOutside(false);
                mContentView.setSystemUiVisibility(View.STATUS_BAR_DISABLE_EXPAND);
                Settings.Global.putInt(mContext.getContentResolver(), LOW_STORAGE_MODE, 1);
                /* MODIFIED-END by yuwan,BUG-4887465*/
                /* MODIFIED-END by chaobing.huang,BUG-3823603*/
            }
            if (mContext.getResources().getBoolean(R.bool.def_cb_4370_all_softkey_disable)
                    && message.getServiceCategory() == MSGID1 && Settings.Global.getInt(
                    mContext.getContentResolver(), CMAS_EMERGENCY_DISPLAY, 0) != 1) {
                Log.d(TAG, "4370 functionkey = true");
                this.setFinishOnTouchOutside(false);
                mContentView.setSystemUiVisibility(View.STATUS_BAR_DISABLE_EXPAND);
                Settings.Global.putInt(mContext.getContentResolver(), CMAS_EMERGENCY_DISPLAY, 1);
            }
            
            // add by liang.zhang for Defect 6012945 at 2018-03-07 begin
            boolean isMexico = false;
            boolean isChile = false;
            SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
            List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info!= null && info.getMcc() == 716) {
        				isPeru = true;
        	        } else if (info!= null && info.getMcc() == 730) {
        	        	isChile = true;
        	        } else if (info!= null && info.getMcc() == 334) {
        	        	isMexico = true;
        	        }
        		}
        	}
            
            // LATAM
            if (isPeru || isMexico || isChile) {
            	this.setFinishOnTouchOutside(false);
                mContentView.setSystemUiVisibility(View.STATUS_BAR_DISABLE_EXPAND);
                Settings.Global.putInt(mContext.getContentResolver(), CMAS_EMERGENCY_DISPLAY, 1);
            }
            // add by liang.zhang for Defect 6012945 at 2018-03-07 end
            /* MODIFIED-END by yuwan,BUG-4887465*/
            //[modify]-begin-by-chaobing.huang-22.12.2016-defect3823603
            //PR 1041716 Added by fang.song end
            mAnimationHandler.startIconAnimation();
        }
    }

    /**
     * Stop animating warning icon.
     */
    @Override
    protected void onPause() {
        TLog.d(TAG, "onPause called");
        mAnimationHandler.stopIconAnimation();
        super.onPause();
    }

    /* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
    void cancelNotificationId(int messageId) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(messageId);
    }
    /* MODIFIED-END by yuwan,BUG-4623008*/

    //[BUGFIX]-ADD-BEGIN by TCTNB.ke.meng,05/07/2014,642065
    void cancelNotificationId(Map<Integer, Integer> notificationid) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Iterator iter = notificationid.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
            int val = (int) entry.getValue();
            notificationManager.cancel(val);
        }
    }
    //[BUGFIX]-ADD-END by TCTNB.mengke,02/14/2014,598712

    /**
     * Stop animating warning icon and stop the {@link CellBroadcastAlertAudio}
     * service if necessary.
     */
    void dismiss() {
        //[BUGFIX]-Add- begin-by TCTNB.yugang.jia,12/02/2013,564967
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,03/02/2015,924513,[SCB]CB notification will disappear after incoming a emergency alert
        CellBroadcastMessage message = getLatestMessage();
        if (!CellBroadcastConfigService.isEmergencyAlertMessage(message)) {
            notificationManager.cancel(CellBroadcastAlertService.NOTIFICATION_ID);
        } else {
            notificationManager.cancel(CellBroadcastAlertService.EMERGENCY_NOTIFICATION_ID);
        }
        //[BUGFIX]-Add-END by TSCD.fujun.yang
        //[BUGFIX]-MOD-BEGIN by TCTNB.ke.meng,05/07/2014,642065
        //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/24/2015,926300,[Pre-CTS][CB]Header for CB message in notification panel should be changed
        /* MODIFIED-BEGIN by yuwan, 2017-05-13,BUG-4623008*/
        if ((this.getResources().getBoolean
                (R.bool.feature_cellbroadcastreceiver_CBReceiverMode_on)
                || this.getResources().
                getBoolean(R.bool.feature_cellbroadcastreceiver_displayChannelId)) &&
                !getBaseContext().getResources().getBoolean(
                        R.bool.def_isSupportNotification_forRussia)) {
                        /* MODIFIED-END by yuwan,BUG-4623008*/
            cancelNotificationId(CellBroadcastReceiverApp.notificationid);
        }
        //[BUGFIX]-Add-END by TSCD.fujun.yang
        //[BUGFIX]-MOD-END by TCTNB.ke.meng
        CellBroadcastReceiverApp.clearNewMessageList();
        //[BUGFIX]-Add- end-by TCTNB.yugang.jia,12/02/2013,564967
        mDissmissFlag = true; // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1748495
        // Stop playing alert sound/vibration/speech (if started)
        stopService(new Intent(this, CellBroadcastAlertAudio.class));
        // Cancel any pending alert reminder
        if (mContext.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
            CellBroadcastAlertReminder.cancelAlertReminderTmo();// aiyan-979267 T-Mobile remind
        } else {
            CellBroadcastAlertReminder.cancelAlertReminder();
        }

        // Remove the current alert message from the list.
        CellBroadcastMessage lastMessage = removeLatestMessage();
        if (lastMessage == null) {
            TLog.e(TAG, "dismiss() called with empty message list!");
            finish();//[BUGFIX]-ADD by bin.xue for PR1083085
            return;
        }

        // Mark the alert as read.
        final long deliveryTime = lastMessage.getDeliveryTime();

        // Mark broadcast as read on a background thread.
        new CellBroadcastContentProvider.AsyncCellBroadcastTask(getContentResolver())
                .execute(new CellBroadcastContentProvider.CellBroadcastOperation() {
                    @Override
                    public boolean execute(CellBroadcastContentProvider provider) {
                        return provider.markBroadcastRead(
                                Telephony.CellBroadcasts.DELIVERY_TIME, deliveryTime);
                    }
                });

        // Set the opt-out dialog flag if this is a CMAS alert (other than Presidential Alert).
        if (lastMessage.isCmasMessage() && lastMessage.getCmasMessageClass() !=
                SmsCbCmasInfo.CMAS_CLASS_PRESIDENTIAL_LEVEL_ALERT && !CBSUtills.isShow4371AsNormal(lastMessage)) {//PR 1054793 Modified by fang.song
            mShowOptOutDialog = true;
        }

        // If there are older emergency alerts to display, update the alert text and return.
        CellBroadcastMessage nextMessage = getLatestMessage();
        if (nextMessage != null) {
            updateAlertText(nextMessage);
            if (CellBroadcastConfigService.isEmergencyAlertMessage(nextMessage)) {
                mAnimationHandler.startIconAnimation();
            } else {
                mAnimationHandler.stopIconAnimation();
            }
            return;
        }

        // Remove pending screen-off messages (animation messages are removed in onPause()).
        mScreenOffHandler.stopScreenOnTimer();

        // Show opt-in/opt-out dialog when the first CMAS alert is received.
        if (mShowOptOutDialog) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean(CellBroadcastSettings.KEY_SHOW_CMAS_OPT_OUT_DIALOG, true)
                    && getResources().getBoolean(R.bool.def_showOptOutDialog)) {
                // Clear the flag so the user will only see the opt-out dialog once.
                prefs.edit().putBoolean(CellBroadcastSettings.KEY_SHOW_CMAS_OPT_OUT_DIALOG, false)
                        .apply();

                KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (km.inKeyguardRestrictedInputMode()) {
                    TLog.d(TAG, "Showing opt-out dialog in new activity (secure keyguard)");
                    Intent intent = new Intent(this, CellBroadcastOptOutActivity.class);
                    startActivity(intent);
                } else {
                    TLog.d(TAG, "Showing opt-out dialog in current activity");
                    CellBroadcastOptOutActivity.showOptOutDialog(this);
                    return; // don't call finish() until user dismisses the dialog
                }
            }
        }
        finishAndRemoveTask(); // MODIFIED by yuxuan.zhang, 2016-06-20,BUG-2344436
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        CellBroadcastMessage message = getLatestMessage();
        //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
        //CBC notification with pop up and tone alert + vibrate in CHILE
        // cannot stop audio and vibrate forceVibrate
        /* MODIFIED-BEGIN by chaobing.huang, 2017-01-17,BUG-4014007*/
        if (message != null && !message.isEtwsMessage()) {
            //if (message != null && !message.isEtwsMessage() && !forceVibrateChile ) {//[modify]-by-chaobing.huang-defect4005963
        /* MODIFIED-END by chaobing.huang,BUG-4014007*/
            //[FEATURE]-Add-END by TCTNB.Dandan.Fang
        	
            // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
        	if (isPeru && isPeruCMASChannel(message.getServiceCategory())) {
        		return super.dispatchKeyEvent(event);
        	}
            // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
        	
            switch (event.getKeyCode()) {
                // Volume keys and camera keys mute the alert sound/vibration (except ETWS).
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_MUTE:
                case KeyEvent.KEYCODE_CAMERA:
                case KeyEvent.KEYCODE_FOCUS:
                    // Stop playing alert sound/vibration/speech (if started)
                    //[modify]-begin-by-chaobing.huang-22.12.2016-defect3857798
                    if (!mContext.getResources().getBoolean(R.bool.def_cellbroadcastreceiver_disable_volumekey)) {
                        stopService(new Intent(this, CellBroadcastAlertAudio.class));
                    }
                    //[modify]-begin-by-chaobing.huang-22.12.2016-defect3857798
                    return true;
                //[modify]-begin-by-chaobing.huang-22.12.2016-defect3823603
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_MENU:
                    if (mContext.getResources().getBoolean(R.bool.def_cellbroadcastreceiver_disable_functionkey)) {
                        return true;
                    }
                    //[add]-end-by-chaobing.huang-22.12.2016-
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    
    // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
    boolean isPeru = false;
    private boolean isPeruCMASChannel(int channelId) {
    	Log.v(TAG, "channelId = " + channelId);
    	switch(channelId) {
	    	case 4370:
	    	case 4380:
	    	case 4381:
	    	case 4382:
	    	case 4383:
	    	case 4396:
	    	case 4397:
	    	case 4398:
	    	case 4399:
	    		return true;
    		
    		default:
    			return false;
    	}
    }
    // add by liang.zhang for Defect 6102916 at 2018-03-15 end

    /**
     * Ignore the back button for emergency alerts (overridden by alert dialog so that the dialog
     * is dismissed).
     */
    @Override
    public void onBackPressed() {
        // ignored
    }

    // begin: aiyan-999810-T-Mobile request for incoming call after WEA message
    private BroadcastReceiver mStopitselfReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (FINISH_FULL_SCREEN.equals(intent.getAction())) {
                finish();
            }
        }
    };

    protected void setFullScreenExist() {
        TLog.d(TAG, "setFullScreenExist");
        isFullScreenExist = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setFullScreenExist();
        if (mContext.getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
            unregisterReceiver(mStopitselfReceiver);
        }
        //PR 1041716 Added by fang.song begin
        /* MODIFIED-BEGIN by chaobing.huang, 2016-12-22,BUG-3823603*/
        /* MODIFIED-BEGIN by yuwan, 2017-06-08,BUG-4887465*/
        if (Settings.Global.getInt(mContext.getContentResolver(), LOW_STORAGE_MODE, 0) == 1) {
            Settings.Global.putInt(mContext.getContentResolver(), LOW_STORAGE_MODE, 0);
            /* MODIFIED-END by yuwan,BUG-4887465*/
            /* MODIFIED-END by chaobing.huang,BUG-3823603*/
        }
        if (Settings.Global.getInt(mContext.getContentResolver(), CMAS_EMERGENCY_DISPLAY, 0) == 1) {
            Settings.Global.putInt(mContext.getContentResolver(), CMAS_EMERGENCY_DISPLAY, 0);
        }
        //PR 1041716 Added by fang.song end
        //[add]-begin-by-chaobing.huang-defect3857798-27.12.2016
        if (getResources().getBoolean(R.bool.feature_enable_cmas_light_up_screen)) {
            keepScreenOn(mContext, false);
        }
        //[add]-end-by-chaobing.huang-defect3857798-27.12.2016
    }

    // end: aiyan-999810-T-Mobile request for incoming call after WEA message
    //[add]-begin-by-chaobing.huang-defect3857798-27.12.2016
    public static void keepScreenOn(Context context, boolean on) {
        if (on) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "==KeepScreenOn==");
            wakeLock.acquire();
        } else {
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
        }
    }
    //[add]-end-by-chaobing.huang-defect3857798-27.12.2016
}
