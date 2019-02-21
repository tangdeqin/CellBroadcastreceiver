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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
//Begin added by jinlong.lu for XR6618444 on 18-8-1
import android.content.SharedPreferences;
import android.content.Context;
import com.android.settings.fingerprint.ex.FingerprintUtils;
import android.view.KeyEvent;
//End added by jinlong.lu for XR6618444 on 18-8-1

/**
 * Activity which handles the actual enrolling for fingerprint.
 */
public class FingerprintEnrollEnrolling extends FingerprintEnrollBase
        implements FingerprintEnrollSidecar.Listener {

    static final String TAG_SIDECAR = "sidecar";

    private static final int PROGRESS_BAR_MAX = 10000;
    private static final int FINISH_DELAY = 250;

    /**
     * If we don't see progress during this time, we show an error message to remind the user that
     * he needs to lift the finger and touch again.
     */
    private static final int HINT_TIMEOUT_DURATION = 2500;

    /**
     * How long the user needs to touch the icon until we show the dialog.
     */
    private static final long ICON_TOUCH_DURATION_UNTIL_DIALOG_SHOWN = 500;

    /**
     * How many times the user needs to touch the icon until we show the dialog that this is not the
     * fingerprint sensor.
     */
    private static final int ICON_TOUCH_COUNT_SHOW_UNTIL_DIALOG_SHOWN = 3;

    private ProgressBar mProgressBar;
    private ObjectAnimator mProgressAnim;
    private TextView mStartMessage;
    private TextView mRepeatMessage;
    private TextView mErrorText;
    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mLinearOutSlowInInterpolator;
    private Interpolator mFastOutLinearInInterpolator;
    private int mIconTouchCount;
    private FingerprintEnrollSidecar mSidecar;
    private boolean mAnimationCancelled;
    private AnimatedVectorDrawable mIconAnimationDrawable;
    private Drawable mIconBackgroundDrawable;
    private int mIndicatorBackgroundRestingColor;
    private int mIndicatorBackgroundActivatedColor;
    private boolean mRestoring;
    //Begin added by jinlong.lu for XR6618444 on 18-7-31
    private Button mFindSensor;
    private SharedPreferences sp;
    //End added by jinlong.lu for XR6618444 on 18-7-31
    //Begin added by jinlong.lu for XRP10024314 on 18-10-13
    private View mEnrollFooterView;
    //End added by jinlong.lu for XRP10024314 on 18-10-13
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /// M: Enabled fragment logs: @{
        if (android.os.Build.TYPE.equals("eng")) {
            getFragmentManager().enableDebugLogging(true);
        }
        /// @}
        setContentView(R.layout.fingerprint_enroll_enrolling);
        setHeaderText(R.string.security_settings_fingerprint_enroll_repeat_title);
        mStartMessage = (TextView) findViewById(R.id.start_message);
        mRepeatMessage = (TextView) findViewById(R.id.repeat_message);
        mErrorText = (TextView) findViewById(R.id.error_text);
        mProgressBar = (ProgressBar) findViewById(R.id.fingerprint_progress_bar);

        Button skipButton = findViewById(R.id.skip_button);
        skipButton.setOnClickListener(this);
        //Begin added by dongchi.chen for XR6167859
        if(getIntent().getBooleanExtra(FingerprintUtils.EXTRA_KEY_SHOW_SKIP_BUTTON, false)) {
            skipButton.setVisibility(View.VISIBLE);
        }
        //End added by dongchi.chen for XR6167859

        final LayerDrawable fingerprintDrawable = (LayerDrawable) mProgressBar.getBackground();
        mIconAnimationDrawable = (AnimatedVectorDrawable)
                fingerprintDrawable.findDrawableByLayerId(R.id.fingerprint_animation);
        mIconBackgroundDrawable =
                fingerprintDrawable.findDrawableByLayerId(R.id.fingerprint_background);
        mIconAnimationDrawable.registerAnimationCallback(mIconAnimationCallback);
        mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.fast_out_slow_in);
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.linear_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.fast_out_linear_in);
        mProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mIconTouchCount++;
                    if (mIconTouchCount == ICON_TOUCH_COUNT_SHOW_UNTIL_DIALOG_SHOWN) {
                        showIconTouchDialog();
                    } else {
                        mProgressBar.postDelayed(mShowDialogRunnable,
                                ICON_TOUCH_DURATION_UNTIL_DIALOG_SHOWN);
                    }
                } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL
                        || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    mProgressBar.removeCallbacks(mShowDialogRunnable);
                }
                return true;
            }
        });
        mIndicatorBackgroundRestingColor
                = getColor(R.color.fingerprint_indicator_background_resting);
        mIndicatorBackgroundActivatedColor
                = getColor(R.color.fingerprint_indicator_background_activated);
        mIconBackgroundDrawable.setTint(mIndicatorBackgroundRestingColor);
        mRestoring = savedInstanceState != null;
        //Begin added by jinlong.lu for XR6618444 on 18-7-31
        sp = getSharedPreferences(FingerprintEnrollFindSensor.FINGERPRINT_FIND_SENSOR, Context.MODE_PRIVATE);
        mFindSensor =(Button)findViewById(R.id.find_sensor_button);
        //Begin modified by jinlong.lu for XRP10024314 on 18-10-13
        mEnrollFooterView = findViewById(R.id.view_enroll_footer);
        if(mFindSensor!=null){
            int  isFirstFindSensor =sp.getInt(FingerprintEnrollFindSensor.FINGERPRINT_FIND_SENSOR, 0);
            if(isFirstFindSensor !=0){
                mFindSensor.setVisibility(View.GONE);
                if (mEnrollFooterView != null) {
                    mEnrollFooterView.setVisibility(View.INVISIBLE);
                }
            }
        //End modified by jinlong.lu for XRP10024314 on 18-10-13
            mFindSensor.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    launchFindSensor(mToken);
                }
            });
        }
        //End added by jinlong.lu for XR6618444 on 18-7-31
    }
    //Begin added by jinlong.lu for XR6618444 on 18-7-31
    private void launchFindSensor(byte[] token) {
        Intent intent = getFindSensorIntent();
        if (token != null) {
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
        }
        if (mUserId != UserHandle.USER_NULL) {
            intent.putExtra(Intent.EXTRA_USER_ID, mUserId);
        }
        startActivity(intent);
    }
    protected Intent getFindSensorIntent() {
        Intent intent = new Intent(this, FingerprintEnrollFindSensor.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        return intent;
    }
    //End added by jinlong.lu for XR6618444 on 18-7-31
    @Override
    protected void onStart() {
        super.onStart();
        mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag(TAG_SIDECAR);
        if (mSidecar == null) {
            mSidecar = new FingerprintEnrollSidecar();
            getFragmentManager().beginTransaction().add(mSidecar, TAG_SIDECAR).commit();
        }
        mSidecar.setListener(this);
        updateProgress(false /* animate */);
        updateDescription();
        if (mRestoring) {
            startIconAnimation();
        }
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        mAnimationCancelled = false;
        startIconAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSidecar != null) {
            mSidecar.setListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSidecar != null) {
            mSidecar.setListener(null);
        }
    }

    private void startIconAnimation() {
        mIconAnimationDrawable.start();
    }

    private void stopIconAnimation() {
        mAnimationCancelled = true;
        mIconAnimationDrawable.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSidecar != null) {
            mSidecar.setListener(null);
        }
        stopIconAnimation();
        if (!isChangingConfigurations()) {
            if (mSidecar != null) {
                mSidecar.cancelEnrollment();
                getFragmentManager().beginTransaction().remove(mSidecar).commitAllowingStateLoss();
            }
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (mSidecar != null) {
            mSidecar.setListener(null);
            mSidecar.cancelEnrollment();
            getFragmentManager().beginTransaction().remove(mSidecar).commitAllowingStateLoss();
            mSidecar = null;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skip_button:
                setResult(RESULT_SKIP);
                finish();
                break;
            default:
                super.onClick(v);
        }
    }

    private void animateProgress(int progress) {
        if (mProgressAnim != null) {
            mProgressAnim.cancel();
        }
        ObjectAnimator anim = ObjectAnimator.ofInt(mProgressBar, "progress",
                mProgressBar.getProgress(), progress);
        anim.addListener(mProgressAnimationListener);
        anim.setInterpolator(mFastOutSlowInInterpolator);
        anim.setDuration(250);
        anim.start();
        mProgressAnim = anim;
    }

    private void animateFlash() {
        ValueAnimator anim = ValueAnimator.ofArgb(mIndicatorBackgroundRestingColor,
                mIndicatorBackgroundActivatedColor);
        final ValueAnimator.AnimatorUpdateListener listener =
                new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIconBackgroundDrawable.setTint((Integer) animation.getAnimatedValue());
            }
        };
        anim.addUpdateListener(listener);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ValueAnimator anim = ValueAnimator.ofArgb(mIndicatorBackgroundActivatedColor,
                        mIndicatorBackgroundRestingColor);
                anim.addUpdateListener(listener);
                anim.setDuration(300);
                anim.setInterpolator(mLinearOutSlowInInterpolator);
                anim.start();
            }
        });
        anim.setInterpolator(mFastOutSlowInInterpolator);
        anim.setDuration(300);
        anim.start();
    }

    private void launchFinish(byte[] token) {
        Intent intent = getFinishIntent();
        //Begin deleted by jiatang.ye for XR7432990 on 19-1-29
        // FLAG_ACTIVITY_FORWARD_RESULT can not use with FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
//                | Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //End deleted by jiatang.ye for XR7432990 on 19-1-29
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
        if (mUserId != UserHandle.USER_NULL) {
            intent.putExtra(Intent.EXTRA_USER_ID, mUserId);
        }
        //Begin added by dongchi.chen for XR5904853
        intent.putExtra(FingerprintUtils.EXTRA_KEY_ENROLL_FINGERPRINT_TAG,
               getIntent().getIntExtra(
                       FingerprintUtils.EXTRA_KEY_ENROLL_FINGERPRINT_TAG, 0));
        //End added by dongchi.chen for XR5904853
        startActivity(intent);
        overridePendingTransition(R.anim.suw_slide_next_in, R.anim.suw_slide_next_out);
        finish();
    }

    protected Intent getFinishIntent() {
        return new Intent(this, FingerprintEnrollFinish.class);
    }

    private void updateDescription() {
        if (mSidecar.getEnrollmentSteps() == -1) {
            mStartMessage.setVisibility(View.VISIBLE);
            mRepeatMessage.setVisibility(View.INVISIBLE);
        } else {
            mStartMessage.setVisibility(View.INVISIBLE);
            mRepeatMessage.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onEnrollmentHelp(CharSequence helpString) {
        //Begin modified by jinlong.lu for XR6618444 on 18-7-31
        //mErrorText.setText(helpString);
        showError(helpString);
        //End modified by jinlong.lu for XR6618444 on 18-7-31
    }

    @Override
    public void onEnrollmentError(int errMsgId, CharSequence errString) {
        int msgId;
        switch (errMsgId) {
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                // This message happens when the underlying crypto layer decides to revoke the
                // enrollment auth token.
                msgId = R.string.security_settings_fingerprint_enroll_error_timeout_dialog_message;
                break;
            default:
                // There's nothing specific to tell the user about. Ask them to try again.
                msgId = R.string.security_settings_fingerprint_enroll_error_generic_dialog_message;
                break;
        }
        showErrorDialog(getText(msgId), errMsgId);
        stopIconAnimation();
        mErrorText.removeCallbacks(mTouchAgainRunnable);
    }

    @Override
    public void onEnrollmentProgressChange(int steps, int remaining) {
        updateProgress(true /* animate */);
        updateDescription();
        clearError();
        animateFlash();
        mErrorText.removeCallbacks(mTouchAgainRunnable);
        mErrorText.postDelayed(mTouchAgainRunnable, HINT_TIMEOUT_DURATION);
        //Begin added by jinlong.lu for XR6618444 on 18-7-31
        if(mFindSensor!=null&&mFindSensor.getVisibility() ==View.VISIBLE){
            mFindSensor.setVisibility(View.GONE);
          //Begin added by jinlong.lu for XRP10024314 on 18-10-13
            if (mEnrollFooterView != null) {
                mEnrollFooterView.setVisibility(View.INVISIBLE);
            }
          //End added by jinlong.lu for XRP10024314 on 18-10-13
            if(sp!=null){
                SharedPreferences.Editor ed = sp.edit();
                ed.putInt(FingerprintEnrollFindSensor.FINGERPRINT_FIND_SENSOR, 1);
                ed.apply();
            }
        }
        //End added by jinlong.lu for XR6618444 on 18-7-31
    }

    private void updateProgress(boolean animate) {
        int progress = getProgress(
                mSidecar.getEnrollmentSteps(), mSidecar.getEnrollmentRemaining());
        if (animate) {
            animateProgress(progress);
        } else {
            mProgressBar.setProgress(progress);
            if (progress >= PROGRESS_BAR_MAX) {
                mDelayedFinishRunnable.run();
            }
        }
    }

    private int getProgress(int steps, int remaining) {
        if (steps == -1) {
            return 0;
        }
        int progress = Math.max(0, steps + 1 - remaining);
        return PROGRESS_BAR_MAX * progress / (steps + 1);
    }

    private void showErrorDialog(CharSequence msg, int msgId) {
        ErrorDialog dlg = ErrorDialog.newInstance(msg, msgId);
        dlg.show(getFragmentManager(), ErrorDialog.class.getName());
    }

    private void showIconTouchDialog() {
        mIconTouchCount = 0;
        new IconTouchDialog().show(getFragmentManager(), null /* tag */);
    }

    private void showError(CharSequence error) {
        mErrorText.setText(error);
        if (mErrorText.getVisibility() == View.INVISIBLE) {
            mErrorText.setVisibility(View.VISIBLE);
            mErrorText.setTranslationY(getResources().getDimensionPixelSize(
                    R.dimen.fingerprint_error_text_appear_distance));
            mErrorText.setAlpha(0f);
            mErrorText.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(mLinearOutSlowInInterpolator)
                    .start();
        } else {
            mErrorText.animate().cancel();
            mErrorText.setAlpha(1f);
            mErrorText.setTranslationY(0f);
        }
    }

    private void clearError() {
        if (mErrorText.getVisibility() == View.VISIBLE) {
            mErrorText.animate()
                    .alpha(0f)
                    .translationY(getResources().getDimensionPixelSize(
                            R.dimen.fingerprint_error_text_disappear_distance))
                    .setDuration(100)
                    .setInterpolator(mFastOutLinearInInterpolator)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mErrorText.setVisibility(View.INVISIBLE);
                        }
                    })
                    .start();
        }
    }

    private final Animator.AnimatorListener mProgressAnimationListener
            = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) { }

        @Override
        public void onAnimationRepeat(Animator animation) { }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mProgressBar.getProgress() >= PROGRESS_BAR_MAX) {
                mProgressBar.postDelayed(mDelayedFinishRunnable, FINISH_DELAY);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) { }
    };

    // Give the user a chance to see progress completed before jumping to the next stage.
    private final Runnable mDelayedFinishRunnable = new Runnable() {
        @Override
        public void run() {
            launchFinish(mToken);
        }
    };

    private final Animatable2.AnimationCallback mIconAnimationCallback =
            new Animatable2.AnimationCallback() {
        @Override
        public void onAnimationEnd(Drawable d) {
            if (mAnimationCancelled) {
                return;
            }

            // Start animation after it has ended.
            mProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    startIconAnimation();
                }
            });
        }
    };

    private final Runnable mShowDialogRunnable = new Runnable() {
        @Override
        public void run() {
            showIconTouchDialog();
        }
    };

    private final Runnable mTouchAgainRunnable = new Runnable() {
        @Override
        public void run() {
            showError(getString(R.string.security_settings_fingerprint_enroll_lift_touch_again));
        }
    };

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.FINGERPRINT_ENROLLING;
    }

    public static class IconTouchDialog extends InstrumentedDialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.security_settings_fingerprint_enroll_touch_dialog_title)
                    .setMessage(R.string.security_settings_fingerprint_enroll_touch_dialog_message)
                    .setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
            return builder.create();
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.DIALOG_FINGERPRINT_ICON_TOUCH;
        }
    }

    public static class ErrorDialog extends InstrumentedDialogFragment {

        /**
         * Create a new instance of ErrorDialog.
         *
         * @param msg the string to show for message text
         * @param msgId the FingerprintManager error id so we know the cause
         * @return a new ErrorDialog
         */
        static ErrorDialog newInstance(CharSequence msg, int msgId) {
            ErrorDialog dlg = new ErrorDialog();
            Bundle args = new Bundle();
            args.putCharSequence("error_msg", msg);
            args.putInt("error_id", msgId);
            dlg.setArguments(args);
            return dlg;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            CharSequence errorString = getArguments().getCharSequence("error_msg");
            final int errMsgId = getArguments().getInt("error_id");
            builder.setTitle(R.string.security_settings_fingerprint_enroll_error_dialog_title)
                    .setMessage(errorString)
                    .setCancelable(false)
                    .setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    boolean wasTimeout =
                                        errMsgId == FingerprintManager.FINGERPRINT_ERROR_TIMEOUT;
                                    Activity activity = getActivity();
                                    activity.setResult(wasTimeout ?
                                            RESULT_TIMEOUT : RESULT_FINISHED);
                                    activity.finish();
                                }
                            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            //Begin added by jinlong.lu for XR6618444 on 18-3-23
            dialog.setCancelable(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Activity activity = getActivity();
                        activity.setResult(RESULT_CANCELED);
                        activity.finish();
                        return true;

                    } else {
                        return false;
                    }
                }

            });
            //End added by jinlong.lu for XR6618444 on 18-3-23
            return dialog;
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.DIALOG_FINGERPINT_ERROR;
        }
    }
}
