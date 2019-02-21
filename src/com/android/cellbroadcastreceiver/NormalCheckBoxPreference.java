/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.cellbroadcastreceiver;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class NormalCheckBoxPreference extends DefTextSizeCheckBoxPreference {

    public NormalCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        boolean isWpasEnable = mContext.getResources().getBoolean(R.bool.def_enable_wpas_function);
        // add by liang.zhang for Defect 6929849 at 2018-09-01 begin
        if (!CBSUtills.isCanadaSimCard(mContext)) {
        	isWpasEnable = false; 
        }
        // add by liang.zhang for Defect 6929849 at 2018-09-01 end
        final TextView titleView = (TextView) view.findViewById(com.android.internal.R.id.title);
        String code = getKey();
        if (titleView != null && isWpasEnable && !TextUtils.isEmpty(code)) {
            String title = checkWpasTitleInSettings(code);
            if (title != null) {
                titleView.setText(title);
            }
        }
    }

    private String checkWpasTitleInSettings(String code) {
        if (code.equalsIgnoreCase(CellBroadcastSettings.KEY_ENABLE_CMAS_EXTREME_THREAT_ALERTS)) {
            return mContext.getResources().getString(
                    R.string.enable_wpas_extreme_threat_alerts_title);
        } else if (code
                .equalsIgnoreCase(CellBroadcastSettings.KEY_ENABLE_CMAS_SEVERE_THREAT_ALERTS)) {
            return mContext.getResources().getString(
                    R.string.enable_wpas_severe_threat_alerts_title);
        } else if (code
                .equalsIgnoreCase(CellBroadcastSettings.KEY_ENABLE_CMAS_AMBER_ALERTS)) {
            return mContext.getResources().getString(
                    R.string.enable_wpas_amber_alerts_title);
        }
        return null;
    }
}
