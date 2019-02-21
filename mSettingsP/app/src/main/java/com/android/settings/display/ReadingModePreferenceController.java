/* ----------|----------------------|----------------------|----------------- */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* -------------------------------------------------------------------------- */
/* 2018/10/21 |     shilei.zhang  |      task 7052220     |     create      */
/******************************************************************************/



package com.android.settings.display;

import android.annotation.UserIdInt;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.R;

public class ReadingModePreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin {

    private final String mReadingmodePrefKey;
    private boolean mAvailable = true;

    public ReadingModePreferenceController(Context context,String key) {
        super(context);
        mReadingmodePrefKey = key;
    }

    @Override
    public boolean isAvailable() {
         //Begin modified by miaoliu for XR7107006 on 2018/11/15 
        int ResourceId = mContext.getResources().getIdentifier("def_reading_mode_available", "bool", "com.tct");
        mAvailable = ResourceId > 0 ? mContext.getResources().getBoolean(ResourceId) : true;
        return mAvailable;
        //Begin modified by miaoliu for XR7107006 on 2018/11/15
         
    }
    @Override
    public void displayPreference(PreferenceScreen screen) {
        //super.displayPreference(screen);
        if(!isAvailable()){
            removePreference(screen, mReadingmodePrefKey);
            return;
        }
    }
    
    @Override
    public String getPreferenceKey() {
        return mReadingmodePrefKey;
    }
}
