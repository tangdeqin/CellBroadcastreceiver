/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.cellbroadcastreceiver;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.TextView;
import com.android.cellbroadcastreceiver.R;

public class WpasCheckBoxPreference extends DefTextSizeCheckBoxPreference {

    private View mParent = null;
    private CheckBox mCheckBox;
    private final String TAG = "WpasCheckBox";
    public WpasCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        mParent = view;
        super.onBindView(view);
        mCheckBox = (CheckBox) view.findViewById(com.android.internal.R.id.checkbox);
            if (mParent != null) {
                mParent.setClickable(false);
                mParent.setFocusable(false);
                mParent.setEnabled(false);
                mParent.setFocusableInTouchMode(false);
            }
            mParent.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // do nothing
                    Log.i(TAG, "onClick parent mParent = " + mParent);
                    return;
                }
            });
            if (mCheckBox != null) {
                mCheckBox.setClickable(false);
                mCheckBox.setFocusable(false);
                mCheckBox.setEnabled(false);
                mCheckBox.setFocusableInTouchMode(false);
                mCheckBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick");
                        return;
                    }
                });
            }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}
