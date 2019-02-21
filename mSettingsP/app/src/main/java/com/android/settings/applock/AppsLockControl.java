package com.android.settings.applock;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.Override;
import java.lang.String;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.settings.password.ConfirmDeviceCredentialBaseFragment;
import com.android.settings.R;
import com.android.settings.privatemode.util.Adapter.CommonAdapter;
import com.android.settings.privatemode.util.Adapter.ViewHolder;

import com.tct.sdk.base.applock.TctAppLockHelper;

import android.content.IntentSender;

public class AppsLockControl extends Activity{

    private final static String TAG = "AppsLockControl";

    private final static String ACTION_AUTH = "com.tct.appslock.action.auth";
    private final static String ACTION_CAMERA_SECURE = "android.media.action.STILL_IMAGE_CAMERA_SECURE";

    private final static int REQUEST_AUTH = 100;
    private final static int REQUEST_RESULT = REQUEST_AUTH + 1;

    Intent mIntent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = getIntent();
        String action = mIntent.getAction();
        if(null != action && action.equalsIgnoreCase(ACTION_AUTH)){
            Intent intent = new Intent(ACTION_AUTH);
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.applock.ConfirmAppsLockPattern"));
            intent.putExtra("package_name", mIntent.getStringExtra(Intent.EXTRA_PACKAGE_NAME));

            if(!mIntent.getBooleanExtra("intentSender", false)) {
                Intent _intent = mIntent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (null != _intent
                        && null != _intent.getAction()
                        && ACTION_CAMERA_SECURE.equals(_intent.getAction())) {
                    intent.putExtra("camera_secure", true);
                }
            }

            startActivityForResult(intent, REQUEST_AUTH);
            Log.d(TAG, "send auth request");
        }else {
            finish();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(REQUEST_AUTH == requestCode && RESULT_OK == resultCode){
            if(checkForPendingIntent()){
                return;
            }
            Intent intent = mIntent.getParcelableExtra(Intent.EXTRA_INTENT);
            boolean request = mIntent.getBooleanExtra("request", false);
            if(0 != (intent.getFlags() & Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                    || !request){
                startActivity(intent);
                finish();
            }else {
                startActivityForResult(intent, REQUEST_RESULT);
            }
            return;
        }
        if(REQUEST_RESULT == requestCode){
            setResult(resultCode, data);
        }
        finish();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent-" + intent);
    }

    private boolean checkForPendingIntent() {
        boolean useIntentSender = mIntent.getBooleanExtra("intentSender", false);
        if(useIntentSender){
            android.util.Log.d(TAG, "checkForPendingIntent");
            final IntentSender target = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
            try {
                startIntentSenderForResult(target, -1 /*requestCode*/, null /*fillInIntent*/,
                        0 /*flagsMask*/, 0 /*flagsValue*/, 0 /*extraFlags*/);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Error while starting intent sender", e);
            }
            finish();
        }
        return useIntentSender;
    }
}



