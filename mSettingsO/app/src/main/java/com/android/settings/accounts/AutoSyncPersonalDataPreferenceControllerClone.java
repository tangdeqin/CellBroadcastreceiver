/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.accounts;

import android.app.Fragment;
import android.content.Context;

import com.android.settings.Utils;

public class AutoSyncPersonalDataPreferenceControllerClone extends AutoSyncPersonalDataPreferenceController {

    private static final String TAG = "AutoSyncWorkData";
    private static final String KEY_AUTO_SYNC_WORK_ACCOUNT_CLONE = "auto_sync_personal_account_data_clone";

    public AutoSyncPersonalDataPreferenceControllerClone(Context context, Fragment parent) {
        super(context, parent);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_AUTO_SYNC_WORK_ACCOUNT_CLONE;
    }

    @Override
    public boolean isAvailable() {
        return isAutoSyncAvailable() && isCloneEnable();
    }
}
