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
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 01/07/2014|     tianming.lei     |        887714        |[CMAS]Black screen*/
/*           |                      |                      |didn't turn on    */
/*           |                      |                      |with incoming eme-*/
/*           |                      |                      |rgency alert when */
/*           |                      |                      | MS has an unread */
/*           |                      |                      | emergency alert  */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;

import android.content.Context;
import android.os.PowerManager;
import com.android.cb.util.TLog;

/**
 * Hold a wakelock that can be acquired in the CellBroadcastAlertService and
 * released in the CellBroadcastAlertFullScreen Activity.
 */
class CellBroadcastAlertWakeLock {
    private static final String TAG = "CellBroadcastAlertWakeLock";

    private static PowerManager.WakeLock sCpuWakeLock;

    private CellBroadcastAlertWakeLock() {}

    static void acquireScreenCpuWakeLock(Context context) {
        if (sCpuWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      //[BUGFIX]-MOD-BEGIN by TSCD,tianming.lei 01/07/2015,PR-887714
        sCpuWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
      //[BUGFIX]-MOD-END by TSCD,tianming.lei
        sCpuWakeLock.acquire();
        TLog.d(TAG, "acquired screen + CPU wake lock");
    }

    static void releaseCpuLock() {
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
            TLog.d(TAG, "released screen + CPU wake lock");
        }
    }
}
