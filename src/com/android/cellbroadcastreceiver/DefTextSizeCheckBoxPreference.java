/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.cellbroadcastreceiver;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class DefTextSizeCheckBoxPreference extends CheckBoxPreference {
    protected Context mContext;

    public DefTextSizeCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        boolean isWpasEnable = mContext.getResources().getBoolean(R.bool.def_enable_wpas_function);
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-08,BUG-2219480*/
//        if (isWpasEnable) {
//            final TextView titleView = (TextView) view
//                    .findViewById(com.android.internal.R.id.title);
//            if (titleView != null) {
//                titleView.setTextSize(CellBroadcastSettings.PREFERENCE_TITLE_TEXT_SIZE);
//            }
//            final TextView summaryView = (TextView) view
//                    .findViewById(com.android.internal.R.id.summary);
//            if (summaryView != null) {
//                summaryView.setTextSize(CellBroadcastSettings.PREFERENCE_SUMMARY_TEXT_SIZE);
//            }
//        }
/* MODIFIED-END by yuxuan.zhang,BUG-2219480*/
    }
}
