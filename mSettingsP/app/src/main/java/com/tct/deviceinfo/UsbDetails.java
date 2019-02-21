/* ----------|----------------------|----------------------|----------------- */
/* Android P interface UsbDetails ,may special fit*/
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* -------------------------------------------------------------------------- */
/* 2018/10/30|     zhixiong.liu.hz  |       7070928    |     create      */
/******************************************************************************/

package com.tct.deviceinfo;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.deviceinfo.UsbModeChooserActivity;	
import android.util.Log;
/**
 * UI for the USB chooser dialog.
 *
 */
public class UsbDetails extends SettingsPreferenceFragment{
	private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext =getContext();
        Intent usbIntent = new Intent(mContext,UsbModeChooserActivity.class);
		mContext.startActivity(usbIntent);
         finish();
   } 
   

   @Override
    public int getMetricsCategory() {
       return MetricsEvent.USB_DEVICE_DETAILS;
     }
}
