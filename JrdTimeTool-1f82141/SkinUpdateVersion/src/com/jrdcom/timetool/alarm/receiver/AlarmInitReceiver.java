/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.jrdcom.timetool.alarm.receiver;

import com.jrdcom.timetool.alarm.AlarmAlertWakeLock;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.alarm.AsyncHandler;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.PowerManager.WakeLock;

public class AlarmInitReceiver extends BroadcastReceiver {

    /**
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();

        final PendingResult result = goAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        AsyncHandler.post(new Runnable() {
            @Override public void run() {
                //PR: 480471 update by XIBIN start
                // Remove the snooze alarm after a boot.
//                if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
//                    Alarms.saveSnoozeAlert(context, Alarms.INVALID_ALARM_ID, -1);
//                }
                //PR: 480471 update by XIBIN end
                Alarms.disableExpiredAlarms(context);
                Alarms.setNextAlert(context);
                result.finish();
                wl.release();
            }
        });
    }
}
