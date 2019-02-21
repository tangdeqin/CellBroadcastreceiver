/*
 * Copyright (C) 2013 The Android Open Source Project
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

import java.util.ArrayList;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import com.android.cb.util.TLog;

import static com.android.cellbroadcastreceiver.CellBroadcastReceiver.DBG;
import com.android.internal.telephony.PhoneConstants;
import android.telephony.SubscriptionManager;
import android.telephony.CellBroadcastMessage;

/**
 * Manages alert reminder notification.
 */
public class CellBroadcastAlertReminder extends Service {
    private static final String TAG = "CellBroadcastAlertReminder";

    /** Action to wake up and play alert reminder sound. */
    static final String ACTION_PLAY_ALERT_REMINDER = "ACTION_PLAY_ALERT_REMINDER";

    /**
     * Pending intent for alert reminder. This is static so that we don't have to start the
     * service in order to cancel any pending reminders when user dismisses the alert dialog.
     */
    private static PendingIntent sPlayReminderIntent;

    /**
     * Alert reminder for current ringtone being played.
     */
    private static Ringtone sPlayReminderRingtone;

    /**
     * aiyan-979267-begin TMO requirement(3.11, 3.12, 3.13) If the end user does not acknowledge the
     * WEA alert, the WEA audio attention signal or vibration cadence should be repeated 1 minute, 3
     * minutes and 5 minutes after the original alert. This applies only if the device is not in
     * silent mode and the audio attention alert UI setting is ON. The behavior of follow-up alerts
     * should match the behavior of the original alert.
     */
    /** get the CMAS information,so we can play the Audio. */
    private static CellBroadcastMessage mCbMsg;
    /** repeated 1 minute, 3 minutes and 5 minutes. */
    private static final int[] sRepeatTime = {
            1, 3, 5
    };
    private static ArrayList<Integer> mRepeatPattern;
    /** Action to wake up and play alert reminder sound. */
    static final String ACTION_PLAY_ALERT_REMINDER_TMO = "ACTION_PLAY_ALERT_REMINDER_TMO";
    /** Pending intent for alert reminder. */
    private static PendingIntent sPlayReminderIntentTmo;
    private static long sRealTime;
    // aiyan-979267-end

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (ACTION_PLAY_ALERT_REMINDER.equals(intent.getAction())) {
            log("playing alert reminder");
            playAlertReminderSound();

            long subscription = intent.getIntExtra(
                    PhoneConstants.SUBSCRIPTION_KEY,
                    SubscriptionManager.DEFAULT_SUBSCRIPTION_ID);

            if (queueAlertReminder(this, false, subscription)) {
                return START_STICKY;
            } else {
                log("no reminders queued");
                stopSelf();
                return START_NOT_STICKY;
            }
            // begin：aiyan-979267 add T-Mobile remind request
        } else if (ACTION_PLAY_ALERT_REMINDER_TMO.equals(intent.getAction())) {
            log("ACTION_PLAY_ALERT_REMINDER_TMO");
            if (mRepeatPattern == null || mRepeatPattern.isEmpty()) {
                log("mRepeatPattern.isEmpty()");
                stopSelf();
                return START_NOT_STICKY;
            } else {

                mRepeatPattern.remove(0);
                playAlertReminderSoundTmo();

                if (queueAlertReminderTmo(this, false, mCbMsg)) {
                    return START_STICKY;
                } else {
                    log("no reminders queued tmo");
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
            // end: aiyan-979267 add T-Mobile remind request
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    /**
     * Use the RingtoneManager to play the alert reminder sound.
     */
    private void playAlertReminderSound() {
        Uri notificationUri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION | RingtoneManager.TYPE_ALARM);
        if (notificationUri == null) {
            loge("Can't get URI for alert reminder sound");
            return;
        }
        Ringtone r = RingtoneManager.getRingtone(this, notificationUri);
        if (r != null) {
            log("playing alert reminder sound");
            r.play();
        } else {
            loge("can't get Ringtone for alert reminder sound");
        }
    }

    /**
     * Helper method to start the alert reminder service to queue the alert reminder.
     * @return true if a pending reminder was set; false if there are no more reminders
     */
    static boolean queueAlertReminder(Context context, boolean firstTime, long subscription) {
        // Stop any alert reminder sound and cancel any previously queued reminders.
        cancelAlertReminder();

        /* MODIFIED-BEGIN by bin.huang, 2016-11-10,BUG-1112693*/
        boolean allowWpas = context.getResources().getBoolean(R.bool.def_enable_wpas_function);
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(context)) {
        	allowWpas = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefStr = prefs
                .getString
                (CellBroadcastSettings.KEY_ALERT_REMINDER_INTERVAL + subscription,
                        allowWpas ? CellBroadcastSettings.WPAS_ALERT_REMINDER_INTERVAL_DEFAULT_DURATION
                                : context.getResources().getString(
                                        R.string.def_alert_reminder_value));
                                        /* MODIFIED-END by bin.huang,BUG-1112693*/

        if (prefStr == null) {
            if (DBG) log("no preference value for alert reminder");
            return false;
        }

        int interval;
        try {
            interval = Integer.valueOf(prefStr);
        } catch (NumberFormatException ignored) {
            loge("invalid alert reminder interval preference: " + prefStr);
            return false;
        }

        if (interval == 0 || (interval == 1 && !firstTime)) {
            return false;
        }
        if (interval == 1) {
            interval = 2;   // "1" = one reminder after 2 minutes
        }

        if (DBG) log("queueAlertReminder() in " + interval + " minutes");

        Intent playIntent = new Intent(context, CellBroadcastAlertReminder.class);
        playIntent.setAction(ACTION_PLAY_ALERT_REMINDER);
        playIntent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subscription);
        sPlayReminderIntent = PendingIntent.getService(context, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            loge("can't get Alarm Service");
            return false;
        }

        // remind user after 2 minutes or 15 minutes
        long triggerTime = SystemClock.elapsedRealtime() + (interval * 60000);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, sPlayReminderIntent);
        return true;
    }

    /**
     * Stops alert reminder and cancels any queued reminders.
     */
    static void cancelAlertReminder() {
        if (DBG) log("cancelAlertReminder()");
        if (sPlayReminderRingtone != null) {
            if (DBG) log("stopping play reminder ringtone");
            sPlayReminderRingtone.stop();
            sPlayReminderRingtone = null;
        }
        if (sPlayReminderIntent != null) {
            if (DBG) log("canceling pending play reminder intent");
            sPlayReminderIntent.cancel();
            sPlayReminderIntent = null;
        }
    }

    // begin：aiyan-979267 add T-Mobile remind request
    private void playAlertReminderSoundTmo() {
        if (mCbMsg == null) {
            return;
        }
        CellBroadcastAlertWakeLock.releaseCpuLock();
        CellBroadcastAlertWakeLock.acquireScreenCpuWakeLock(this);

        // start audio/vibration/speech service for emergency alerts
        Intent audioIntent = new Intent(this, CellBroadcastAlertAudio.class);
        audioIntent.setAction(CellBroadcastAlertAudio.ACTION_START_ALERT_AUDIO);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        int duration;
        if (mCbMsg.isEmergencyAlertMessage()) {
            duration = 10500;
        } else {
            duration = Integer.parseInt(prefs.getString(
                    CellBroadcastSettings.KEY_ALERT_SOUND_DURATION + mCbMsg.getSubId(),
                    CellBroadcastSettings.ALERT_SOUND_DEFAULT_DURATION)) * 1000;
        }

        audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_DURATION_EXTRA, duration);

        if (mCbMsg.isEtwsMessage()) {
            // For ETWS, always vibrate, even in silent mode.
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_VIBRATE_EXTRA, true);
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_EXTRA, true);
            audioIntent.putExtra(CellBroadcastAlertAudio.ALERT_AUDIO_ETWS_VIBRATE_EXTRA, true);
        } else {
            // For other alerts, vibration can be disabled in app settings.
            audioIntent.putExtra(
                    CellBroadcastAlertAudio.ALERT_VIBRATE_EXTRA,
                    prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_VIBRATE
                            + mCbMsg.getSubId(), true));
            audioIntent.putExtra(
                    CellBroadcastAlertAudio.ALERT_AUDIO_EXTRA,
                    prefs.getBoolean(CellBroadcastSettings.KEY_ENABLE_ALERT_AUDIO
                            + mCbMsg.getSubId(), true));
        }
        startService(audioIntent);
    }

    static boolean queueAlertReminderTmo(Context context, boolean firstTime,
            CellBroadcastMessage message) {
        cancelAlertReminder();

        if (firstTime) {
            mCbMsg = message;
            mRepeatPattern = new ArrayList<Integer>();
            for (int i : sRepeatTime) {
                mRepeatPattern.add(i);
            }
            sRealTime = SystemClock.elapsedRealtime();
        }
        if (mRepeatPattern.isEmpty() || null == mRepeatPattern) {
            log("mRepeatPattern.isEmpty()");
            return false;
        }

        Intent playIntent = new Intent(context, CellBroadcastAlertReminder.class);
        playIntent.setAction(ACTION_PLAY_ALERT_REMINDER_TMO);
        sPlayReminderIntentTmo = PendingIntent.getService(context, 0,
                playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            loge("can't get Alarm Service");
            return false;
        }
        log(mRepeatPattern.size() + " times pattern left:");
        long triggerTime = sRealTime + (mRepeatPattern.get(0) * 60000);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, sPlayReminderIntentTmo);
        return true;
    }

    static void cancelAlertReminderTmo() {
        if (DBG)
            log("cancelAlertReminderTmo()");
        if (sPlayReminderIntentTmo != null) {
            if (DBG)
                log("canceling pending play reminder intent TMO");
            sPlayReminderIntentTmo.cancel();
            sPlayReminderIntentTmo = null;
        }
    }
    // end-979267 add T-Mobile remind request

    private static void log(String msg) {
        TLog.d(TAG, msg);
    }

    private static void loge(String msg) {
        TLog.e(TAG, msg);
    }
}
