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
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.settingslib.CustomEditTextPreference;

/**
 * TODO: Add a soft dialpad for PIN entry.
 */
class EditPinPreference extends CustomEditTextPreference {
    private static final String TAG = "CEditTextPref";

    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference preference, boolean positiveResult);
        // BEGIN XR#P10030293 Added by binjian.tu on 2019/01/29
        void onResume(AlertDialog dlg);
        // END XR#P10030293 Added by binjian.tu on 2019/01/29
    }

    private OnPinEnteredListener mPinListener;
    
    public EditPinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditPinPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        mPinListener = listener;
    }

    // BEGIN XR#P10030293 Added by binjian.tu on 2019/01/29
    @Override
    protected void onResume() {
        if (null == mPinListener) return;

        Dialog dlg = getDialog();
        if (null == dlg) {
            Log.e(TAG, "dialog is null?");
            return;
        }
        AlertDialog ad = (AlertDialog)dlg;
        mPinListener.onResume(ad);
    }
    // END XR#P10030293 Added by binjian.tu on 2019/01/29

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        final EditText editText = (EditText) view.findViewById(android.R.id.edit);
        if (editText != null) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        }
    }

    public boolean isDialogOpen() {
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (mPinListener != null) {
            mPinListener.onPinEntered(this, positiveResult);
        }
    }

    public void showPinDialog() {
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            onClick();
        }
    }
}
