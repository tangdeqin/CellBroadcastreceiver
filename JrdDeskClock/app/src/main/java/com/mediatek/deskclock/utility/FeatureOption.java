package com.mediatek.deskclock.utility;

//import android.os.SystemProperties;

import com.tct.deskclock.libs.TctSystemProperties;

/**
 * M: Add FeatureOption class.
 */
public class FeatureOption {

    public static final boolean MTK_GEMINI_SUPPORT =
            TctSystemProperties.get("ro.mtk_gemini_support").equals("1");
            //SystemProperties.get("ro.mtk_gemini_support").equals("1");
}
