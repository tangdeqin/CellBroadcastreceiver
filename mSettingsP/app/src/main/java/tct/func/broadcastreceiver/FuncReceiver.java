package tct.func.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tct.func.FuncSettings;
import tct.func.FuncUtilSettings;

public class FuncReceiver extends BroadcastReceiver {
    private static final String TAG = "FuncReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_LOCALE_CHANGED)) {
            Log.d(TAG, ">>>Intent.ACTION_LOCALE_CHANGED");
            List<FuncSettings.ShortcutsItem> totalLists = new ArrayList<FuncSettings.ShortcutsItem>();
            totalLists = FuncUtilSettings.getDefaultShortcutsItem(context, true);
            String sTotal = FuncUtilSettings.saveTotalListJson(context,
                    totalLists);
        }
    }
}
