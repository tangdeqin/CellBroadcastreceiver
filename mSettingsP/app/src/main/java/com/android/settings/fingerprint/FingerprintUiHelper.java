/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;

import android.util.Log;
import android.os.Handler;
import android.content.Context;
import android.app.KeyguardManager;
import android.provider.Settings;

import android.os.CountDownTimer;
import android.os.SystemClock;
import com.android.internal.widget.LockPatternUtils;


import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.Message;

/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {

    private static final long ERROR_TIMEOUT = 1300;
    private final String TAG = "FingerprintUiHelper"; //added by jianhao.zeng for XR7225621 on 2018/12/24

    private ImageView mIcon;
    private TextView mErrorTextView;
    private CancellationSignal mCancellationSignal;
    private int mUserId;

    private Callback mCallback;
    private FingerprintManager mFingerprintManager;

    private Context mContext; //added by dongchi.chen for XRP10027228 on 20181204

    public FingerprintUiHelper(ImageView icon, TextView errorTextView, Callback callback,
            int userId) {
        mFingerprintManager = Utils.getFingerprintManagerOrNull(icon.getContext());
        mIcon = icon;
        mErrorTextView = errorTextView;
        mCallback = callback;
        mUserId = userId;
        mContext = icon.getContext(); //added by dongchi.chen for XRP10027228 on 20181204
    }
    //Begin added by dongchi.chen for XR6167866
    private  boolean mIsAppsLock = false;
    public void setIsAppsLock(boolean val){
        mIsAppsLock = val;
    }
    //End added by dongchi.chen for XR6167866
    //Begin modified by jianhao.zeng for XRP10026904 on 2019/01/02
    public void startListening() {
        if(getIsFingerprintError()) {
            showAppLokcsFingerprintError();
        } else {
            if (mFingerprintManager != null && mFingerprintManager.isHardwareDetected()
                    && mFingerprintManager.getEnrolledFingerprints(mUserId).size() > 0) {

                //Begin modified by dongchi.chen for XRP10030300 on 20190211
                if (mIsAppsLock) {
                    if(null == mKfro){
                        mKfro = new KeyguardFingerprintRequestObserver();
                        mKfro.observer();
                    }
                }else {
                    mCancellationSignal = new CancellationSignal();
                    mFingerprintManager.setActiveUser(mUserId);
                    mFingerprintManager.authenticate(
                        null, mCancellationSignal, 0 /* flags */, this, null, mUserId);
                    setFingerprintIconVisibility(true);
                    mIcon.setImageResource(R.drawable.ic_fingerprint);
                }
                //End modified by dongchi.chen for XRP10030300 on 20190211
            }
        }
    }
    //End modified by jianhao.zeng for XRP10026904 on 2019/01/02
    //End added by dongchi.chen for XR7210352 on 20181205

    public void stopListening() {
        //Begin added by dongchi.chen for XRP10030300 on 20190211
        if(null != mKfro){
            mKfro.dispose();
            mKfro = null;
        }
        //End added by dongchi.chen for XRP10030300 on 20190211
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    public boolean isListening() {
        return mCancellationSignal != null && !mCancellationSignal.isCanceled();
    }

    private void setFingerprintIconVisibility(boolean visible) {
        mIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        mCallback.onFingerprintIconVisibilityChanged(visible);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        //Begin added by dongchi.chen for XRP10025811 on 21081110
        if(errMsgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED){
            return;
        }
        //End added by dongchi.chen for XRP10025811 on 21081110
        //Begin added/modified by jianhao.zeng for XR7225621,P10026904 on 2019/01/02
        showAppLokcsFingerprintError();
        setFingerprintIconVisibility(false);
        setIsFingerprintError(true);
       //End added/modified by jianhao.zeng for XR7225621,P10026904 on 2019/01/02
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        //Begin modified by dongchi.chen for XR5855478 on 18-1-8
        //old  showError(helpString);
        boolean isHelpMsgIdShow = true;
        if (helpMsgId > 1000 && helpMsgId < 1007) {
            isHelpMsgIdShow = false;
            //do not show some help msg when fingerprint authentication in systemUI
        }
        if (isHelpMsgIdShow) {
            showError(helpString);
        }
        //End modified by dongchi.chen for XR5855478 on 18-1-8
    }

    @Override
    public void onAuthenticationFailed() {
        showError(mIcon.getResources().getString(
                R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mCallback.onAuthenticated();
    }

    private void showError(CharSequence error) {
        if (!isListening()) {
            return;
        }

        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText(error);
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setText("");
            //Begin modified by dongchi.chen for XR6167866
            if(mIsAppsLock) {
                mIcon.setImageResource(R.drawable.ic_appslock_fingerprint);
            }else {
                mIcon.setImageResource(R.drawable.ic_fingerprint);
            }
            //End modified by dongchi.chen for XR6167866
        }
    };

    public interface Callback {
        void onAuthenticated();
        void onFingerprintIconVisibilityChanged(boolean visible);
    }

    //Begin added/modified/updated by jianhao.zeng for XR7225621,P10026904,P10029114 on 2019/01/09
    private void showAppLokcsFingerprintError() {
        if (!isListening()) {
            mErrorTextView.setText(R.string.apps_lock_fingerprint_error);
            return;
        }

        mErrorTextView.setText(R.string.apps_lock_fingerprint_error);
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
    }

    public boolean getIsFingerprintError() {
        int isFingerprintError = Settings.System.getInt(mContext.getContentResolver(), "apps_lock_fingerprint_error", 0);
        return isFingerprintError == 1;
    }

    public void setIsFingerprintError(boolean isError) {
        if(isError) {
            Settings.System.putInt(mContext.getContentResolver(), "apps_lock_fingerprint_error", 1);
        } else {
            Settings.System.putInt(mContext.getContentResolver(), "apps_lock_fingerprint_error", 0);
        }

    }

    public ImageView getFingerIcon(){
        return mIcon;
    }
    //End added/modified/updated by jianhao.zeng for XR7225621,P10026904,P10029114 on 2019/01/09


    //Begin added by dongchi.chen for XRP10030300 on 20190211
    /*
     when dobule click power button to launcher camera or the first app is appslock's activity,
     keyguard request fingerprint and appslock request fingerprint too, some times keyguard
     handle fingerprint control, so will observer 'keyguard_request_fingerprint', when keyguard
     release fingerprint control, request fingerprint again.
     */
    private KeyguardFingerprintRequestObserver mKfro = null;
    private class KeyguardFingerprintRequestObserver extends ContentObserver {
        private final Uri URI = Settings.Global.getUriFor("keyguard_request_fingerprint");

        KeyguardFingerprintRequestObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (URI.equals(uri) || uri == null) {
                boolean request = Settings.Global.getInt(mContext.getContentResolver(), "keyguard_request_fingerprint", 0) == 1;
                Log.d(TAG, "keyguard_request_fingerprint - " + request);
                if(!request){
                    setRequestFingerPrintMsg();
                }
            }
        }

        public void observer() {
            mContext.getContentResolver().registerContentObserver(URI, false, this, UserHandle.USER_ALL);
            //Begin modified by dongchi.chen for XRP10030436 on 20190212
            /*
             when use password/pattern to unlock, keyguard get 'handleFingerprintLockoutReset' message some time,
             and monitor not use CancellationSignal to release fingerprint, so when keguard dissmiss not long
             time ago, request fingerprint a little later
             */
            //onChange(true,null);
            boolean showing = Settings.Global.getInt(mContext.getContentResolver(), "is_keyguard_showing", 0) == 1;
            long lastTime = Settings.Global.getLong(mContext.getContentResolver(), "set_is_keyguard_showing_last_time", 0);
            long timeInterval = System.currentTimeMillis() - lastTime;
            Log.d(TAG, "timeDual - " + timeInterval + "; showing-" + showing);
            if(!showing && (500 > timeInterval)){
                mH.removeMessages(H.REQUEST_FINGERPRINT_MSG);
                mH.sendEmptyMessageDelayed(H.REQUEST_FINGERPRINT_MSG, 400);
            }else {
                mH.removeMessages(H.REQUEST_FINGERPRINT_MSG);
                mH.sendEmptyMessageDelayed(H.REQUEST_FINGERPRINT_MSG, 200);
            }
            //End modified by dongchi.chen for XRP10030436 on 20190212
        }

        public void dispose(){
            mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    private H mH = new H();

    final class H extends Handler {
        static final int REQUEST_FINGERPRINT_MSG = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_FINGERPRINT_MSG:
                    requestFingerPrint();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void requestFingerPrint(){
        mCancellationSignal = new CancellationSignal();
        mFingerprintManager.setActiveUser(mUserId);
        mFingerprintManager.authenticate(null, mCancellationSignal, 0, this, null, mUserId);
        Log.d(TAG, "fingerprintManager authenticate");
        setFingerprintIconVisibility(true);
        mIcon.setImageResource(R.drawable.ic_appslock_fingerprint);
    }

    private void setRequestFingerPrintMsg(){
        mH.removeMessages(H.REQUEST_FINGERPRINT_MSG);
        mH.sendEmptyMessageDelayed(H.REQUEST_FINGERPRINT_MSG, 200);
    }
    //End added by dongchi.chen for XRP10030300 on 20190211
}
