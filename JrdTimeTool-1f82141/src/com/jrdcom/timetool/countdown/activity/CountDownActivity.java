
package com.jrdcom.timetool.countdown.activity;

import java.io.File;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.android.deskclock.R;
import com.jrdcom.timetool.TimeToolActivity;
import com.jrdcom.timetool.alarm.activity.HandleSetAlarm;
import com.jrdcom.timetool.countdown.AlarmAlertWakeLock;
import com.jrdcom.timetool.countdown.service.BackgroundCountDownService;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;
import com.jrdcom.timetool.countdown.view.CountDownPickerTcl;
import com.jrdcom.timetool.countdown.view.CountDownPickerTcl.OnTimerChangedListener;
import com.jrdcom.timetool.timer.activity.TimerActivity;
import android.content.ContentValues;

public class CountDownActivity extends Activity {

    private CountDownPickerTcl mCountDownPicker;
    /* PR 586309- Neo Skunkworks - Paul Xu added - 001 Begin */
    private Button mStartBtn;
    private Button mCancelBtn;
    private Button mRingBtn;
    private View mSetTimeView;
    private View mTimeRunView;
    /* PR 586309- Neo Skunkworks - Paul Xu added - 001 End */

    private RefreshHandler mRefreshHandler;

    public static final String MEDIA_PATH_ALCATEL = Environment.getRootDirectory().toString()
            + "/media_alcatel/audio/alarms";

    public static final String MEDIA_PATH_DEFAULT = Environment.getRootDirectory().toString()
            + "/media/audio/alarms";

    public static final String TIMER_RPREFERENCES = "timetool.timer";

    public static final String ALERT_RINGTONE_PATH_KEY = "countdown.alert.ringtone.path";

    public static final String ALERT_SILENT_PATH = "silent";

    public static final int STOP_STATE = 1;

    public static final int START_STATE = 2;

    public static final int CURRENT_STATE = 3;

    private static int mCurrentState;

    private int timeCount = 0;

    private int HOUR = 0;

    private int MINUTE = 1;

    private int SECOND = 0;

    private int mRequestCode;

    private static String mFilePath;

    // private Locale mLocale_MainPort;

    // private Locale mLocale_MainLand;

    private int checkedItemPosition = 0;

    public static final String SCREEN_OFF = "screen_off";

    public static final String SCROLL_MOVE = "scroll_move";
    // PR:510457 add by xibin start
    private AlertDialog alertDialogTimerWarning = null;
    private AlertDialog alertDialogTimerRingtoneChoose = null;
 

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // mLocale_MainLand = getResources().getConfiguration().locale;
        // mLocale_MainPort = getResources().getConfiguration().locale;
        setContentView(R.layout.countdown_main);
        initView();

        mCurrentState = STOP_STATE;
        setButtonVisibility(mCurrentState);

        setDefaultRingTone();

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.countdown_main);
        /* PR 586309- Neo Skunkworks - Paul Xu added - 001 Begin */
        initConfigurationChangedView();
        /* PR 586309- Neo Skunkworks - Paul Xu added - 001 End */
    }

    /* PR 586309- Neo Skunkworks - Paul Xu added - 001 Begin */
    private void initConfigurationChangedView() {
        initView();
        if (mCurrentState == START_STATE) {

            setViewVisibility(true);
            setButtonVisibility(START_STATE);
        } else {
            setViewVisibility(false);
            mCountDownPicker.setCurrentHour(HOUR);
            mCountDownPicker.setCurrentMinute(MINUTE);
            mCountDownPicker.setCurrentSecond(SECOND);
            setButtonVisibility(STOP_STATE);
            if (HOUR != 0 || MINUTE != 0 || SECOND != 0) {

                mStartBtn.setBackgroundResource(R.drawable.timer_btn_green_bk);
                mStartBtn.setTextColor(Color.WHITE);
                mStartBtn.setEnabled(true);
            } else {
                mStartBtn.setEnabled(false);
                mStartBtn.setBackgroundResource(R.drawable.timer_btn_gray_bk);
                mStartBtn.setTextColor(R.color.countdown_grey);
            }
        }
    }

    /* PR 586309- Neo Skunkworks - Paul Xu added - 001 End */

    public void onResume() {
        super.onResume();
        setTitle(getResources().getString(R.string.activity_timer));

        // when timer runs in background,start refresh service
        if (BackgroundCountDownService.isTimerRun()) {
            setViewVisibility(true);
            if (!mRefreshHandler.isStart()) {
                mRefreshHandler.start();
            }
        } else {

            setViewVisibility(false);
            timeCount = mCountDownPicker.getCurrentHour() * 3600
                    + mCountDownPicker.getCurrentMinute() * 60
                    + mCountDownPicker.getCurrentSecond();
            mCurrentState = STOP_STATE;// PR:510584 add by XIBIN
            setButtonVisibility(mCurrentState);
            /* PR 586309- Neo Skunkworks - Paul Xu modified - 001 Begin */
            if (timeCount != 0) {
                mStartBtn.setBackgroundResource(R.drawable.timer_btn_green_bk);
                mStartBtn.setTextColor(Color.WHITE);
                mStartBtn.setEnabled(true);
            } else {
                mStartBtn.setEnabled(false);
                mStartBtn.setBackgroundResource(R.drawable.timer_btn_gray_bk);
                mStartBtn.setTextColor(R.color.countdown_grey);
            }
            /* PR 586309- Neo Skunkworks - Paul Xu modified - 001 End */
        }

        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
        if (HandleSetAlarm.mExtraLength && HandleSetAlarm.mTimerSkipUI) {
            // HandleSetAlarm.mExtraLength = false;
            // HandleSetAlarm.mTimerSkipUI = false;
            Intent timerServiceIntent = new Intent(this, BackgroundCountDownService.class);
            timerServiceIntent.putExtra(BackgroundCountDownService.TIMER_COUNT_EXTRA,
                    HandleSetAlarm.mFromIntentTimecount);//PR743707-haiying.he
            startService(timerServiceIntent);

            finish();
            return;
        }
        if (HandleSetAlarm.mExtraLength && HandleSetAlarm.mTimerSkipUI == false) {
            startTimer();
        }
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
    }

    @Override
    protected void onPause() {
        super.onPause();

        // refresh pause
        if (mRefreshHandler.isStart()) {
            mRefreshHandler.stop();
        }
        stopService(new Intent(CountDownActivity.this, MediaPlayerService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    protected void onDestroy() {
        super.onDestroy();
        // PR:517007 add by XIBIN
        BackgroundCountDownService.setTimerRun(false);
        cancelTimer();
        // PR:510457 add by xibin start
        if (alertDialogTimerWarning != null && alertDialogTimerWarning.isShowing()) {
            alertDialogTimerWarning.dismiss();
        }
        if (alertDialogTimerRingtoneChoose != null && alertDialogTimerRingtoneChoose.isShowing()) {
            alertDialogTimerRingtoneChoose.dismiss();
        }
        // PR:510457 add by xibin end
    }

    public void onBackPressed() {
        TimeToolActivity parentActivity = (TimeToolActivity) getParent();

        if (!isRunning()) {

            if (!parentActivity.checkIsTimerRun()) {
                super.onBackPressed();
            }
        } else

            showAlert();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopService(new Intent(CountDownActivity.this, MediaPlayerService.class));
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Alert users exit or not
     */
    private void showAlert() {
        TimeToolActivity parentActivity = (TimeToolActivity) getParent();
        final TimerActivity TimerActivity = (TimerActivity) parentActivity
                .getLocalActivityManager().getActivity(parentActivity.Timer_TAB_TAG);
        Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertBuilder.setTitle(R.string.Timer_warning);
        alertBuilder.setMessage(R.string.timer_exit_warning);
        alertBuilder.setNegativeButton(R.string.timer_dialog_cancel, null);
        alertBuilder.setPositiveButton(R.string.timer_dialog_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cancelTimer();

                        // if the Timer is running,stop it.
                        if (TimerActivity != null) {
                            if (TimerActivity.isRunning()) {
                                TimerActivity.stopTimer();
                            }
                        }
                        finish();
                        dialog.dismiss();
                    }
                });
        // PR:510457 add by xibin
        alertDialogTimerWarning = alertBuilder.show();
    }

    public boolean isRunning() {
        return BackgroundCountDownService.isTimerRun();
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.timer_Start:
                startTimer();
                break;
            case R.id.timer_CancelofStart:
                cancelTimer();
                break;
            case R.id.ring_Btn:
                chooseRing();
                break;
            default:
        }
    }

    private void initView() {
        /* PR 586309- Neo Skunkworks - Paul Xu modified - 001 Begin */
        mStartBtn = (Button) findViewById(R.id.timer_Start);
        mCancelBtn = (Button) findViewById(R.id.timer_CancelofStart);
        mRingBtn = (Button) findViewById(R.id.ring_Btn);
        mSetTimeView = findViewById(R.id.timer_set_time_layout);
        mTimeRunView = findViewById(R.id.timer_time_run_layout);
        mCountDownPicker = (CountDownPickerTcl) findViewById(R.id.countDownPickerTcl);
        mCountDownPicker.setOnTimerChangedListener(new OnTimerChangedListener() {

            public void onTimerChanged(CountDownPickerTcl view, int hour, int minute, int second) {
                if (mCountDownPicker.getCurrentHour() != 0
                        || mCountDownPicker.getCurrentMinute() != 0
                        || mCountDownPicker.getCurrentSecond() != 0) {
                    HOUR = mCountDownPicker.getCurrentHour();
                    MINUTE = mCountDownPicker.getCurrentMinute();
                    SECOND = mCountDownPicker.getCurrentSecond();
                    mStartBtn.setEnabled(true);
                    mStartBtn.setBackgroundResource(R.drawable.timer_btn_green_bk);
                    mStartBtn.setTextColor(Color.WHITE);
                } else {
                    HOUR = 0;
                    MINUTE = 0;
                    SECOND = 0;
                    mStartBtn.setEnabled(false);
                    mStartBtn.setBackgroundResource(R.drawable.timer_btn_gray_bk);
                    mStartBtn.setTextColor(R.color.countdown_grey);
                }
            }
        });

        if (mRefreshHandler == null) {
            mRefreshHandler = new RefreshHandler(this);
        } else {
            if (mRefreshHandler.isStart()) {
                mRefreshHandler.stop();
                mRefreshHandler = new RefreshHandler(this);
                mRefreshHandler.start();
            } else {
                mRefreshHandler = new RefreshHandler(this);
            }
        }
        /* PR 586309- Neo Skunkworks - Paul Xu modified - 001 End */
    }

    // set current state
    public static void setCurrentState(int curState) {
        mCurrentState = curState;
    }

    // set visibility between set_time_layout and run_layout

    private void setViewVisibility(boolean isRun) {
        /* PR 586309- Neo Skunkworks - Paul Xu modified - 001 Begin */
        if (isRun) {
            mSetTimeView.setVisibility(View.GONE);
            mTimeRunView.setVisibility(View.VISIBLE);
        } else {
            mSetTimeView.setVisibility(View.VISIBLE);
            mTimeRunView.setVisibility(View.GONE);
        }
        /* PR 586309- Neo Skunkworks - Paul Xu modified - 001 End */
    }

    // start timer
    private void startTimer() {
        if (BackgroundCountDownService.isTimerRun()) {
            return;
        }

        int hour = mCountDownPicker.getCurrentHour();
        int minute = mCountDownPicker.getCurrentMinute();
        int second = mCountDownPicker.getCurrentSecond();
        timeCount = hour * 3600 + minute * 60 + second;
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
        if (HandleSetAlarm.mExtraLength && HandleSetAlarm.mTimerSkipUI == false) {
            HandleSetAlarm.mExtraLength = false;
            timeCount = HandleSetAlarm.mFromIntentTimecount;//PR743707-haiying.he start
        }
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
        Log.d("xu", "dddd timeCount:" + timeCount);

        // start background timer service

        // PR574723 for AT command AT+CTMRV begin
        ContentValues values = new ContentValues();
        values.put("count_time", timeCount);
        getContentResolver().insert(Uri.parse("content://com.jrdcom.timetool.provider"), values);
        // PR574723 for AT command AT+CTMRV end
        Intent timerServiceIntent = new Intent(this, BackgroundCountDownService.class);
        timerServiceIntent.putExtra(BackgroundCountDownService.TIMER_COUNT_EXTRA, timeCount);
        startService(timerServiceIntent);
        mCurrentState = START_STATE;
        setButtonVisibility(mCurrentState);

        // clear the set_time_layout
        HOUR = 0;
        MINUTE = 1;
        SECOND = 0;
        mCountDownPicker.setCurrentHour(0);
        mCountDownPicker.setCurrentMinute(1);
        mCountDownPicker.setCurrentSecond(0);
        setViewVisibility(true);
        mRefreshHandler.start();
    }

    public void cancelTimer() {
        stopService(new Intent(this, BackgroundCountDownService.class));
        mRefreshHandler.stop();
        // PR574723 for AT command AT+CTMRV begin
        getContentResolver()
                .delete(Uri.parse("content://com.jrdcom.timetool.provider"), null, null);
        // PR574723 for AT command AT+CTMRV end
        mCurrentState = STOP_STATE;
        setButtonVisibility(mCurrentState);
        setViewVisibility(false);
        AlarmAlertWakeLock.release();
    }

    // choose rings
    private void chooseRing() {
        String mediaPath = MEDIA_PATH_DEFAULT;
        String company = "";
        if ("ALCATEL".equals(company) || "FPT".equals(company)) {
            mediaPath = MEDIA_PATH_ALCATEL;
        }

        String[] ringItems = null;

        final File[] mediaFiles = new File(mediaPath).listFiles();
        if (mediaFiles == null || mediaFiles.length <= 0) {
            ringItems = new String[1];
            ringItems[0] = getString(R.string.timer_ringtone_silent);
        } else {
            ringItems = new String[mediaFiles.length + 1];
            ringItems[0] = getString(R.string.timer_ringtone_silent);

            for (int i = 0; i < mediaFiles.length; i++) {
                File mediaFile = mediaFiles[i];
                // PR 610734 - Neo Skunkworks - Soar Gao - 001 begin
                // Delete extension in display of ringtone
                // ringItems[i + 1] = mediaFile.getName()
                //modify by wei_zhang for PR798733 start
                //when music name contains '_', remove '_'
                String fileName = mediaFile.getName().replace("_", " ");
                ringItems[i + 1] = fileName.substring(0,
                        mediaFile.getName().lastIndexOf("."));
                //modify by wei_zhang for PR798733 end
                // PR 610734 - Neo Skunkworks - Soar Gao - 001 end
            }

        }

        // retrieve the former ring
        final SharedPreferences sharedPre = getSharedPreferences(TIMER_RPREFERENCES, MODE_PRIVATE);
        String preRingtonePath = sharedPre.getString(ALERT_RINGTONE_PATH_KEY, "");
        int checkedItem = 0;

        // when the former ring is not silent
        if (!ALERT_SILENT_PATH.equals(preRingtonePath)) {
            // when the ring does not exit ,set to be silent
            if (mediaFiles == null || mediaFiles.length <= 0) {
                sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH).commit();
            } else {
                boolean isFindPreMediaFile = false;
                for (int i = 0; i < mediaFiles.length; i++) {
                    if (preRingtonePath.equals(mediaFiles[i].getAbsolutePath())) {
                        isFindPreMediaFile = true;
                        // set the current pitched on item
                        checkedItem = i + 1;
                        break;
                    }
                }

                // added by lijuan.li at 2012.4.18 begin
                if (preRingtonePath != mFilePath)
                    mFilePath = preRingtonePath;
                // lijuan.li end

                // when do not find the former ring
                if (!isFindPreMediaFile) {

                    if (preRingtonePath == null) {
                        sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH)
                                .commit();
                    } else if (preRingtonePath != null) {
                        checkedItem = mediaFiles.length + 1;
                    }

                }
            }
        }

        // show the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.timer_ringtone_choose_title);
        // final int checkedItemPosition = 0;
        dialogBuilder.setSingleChoiceItems(ringItems, checkedItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // stop the former ring

                        stopService(new Intent(CountDownActivity.this, MediaPlayerService.class));

                        checkedItemPosition = ((AlertDialog) dialog).getListView()
                                .getCheckedItemPosition();

                        // when choose silent
                        if (which == 0) {
                            // added by lijuan.li 2012.03.27 begin
                            mFilePath = ALERT_SILENT_PATH;
                            // lijuan.li end
                            return;
                        }

                        if (which < mediaFiles.length + 1) {

                            // mFilePath = mediaFiles[checkedItemPosition - 1]
                            // .getAbsolutePath();

                            File file = mediaFiles[which - 1];

                            mFilePath = file.getAbsolutePath();

                            Intent intent = new Intent(CountDownActivity.this,
                                    MediaPlayerService.class);

                            intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA,
                                    file.getAbsolutePath());
                            /*
                             * PR 675449 - Neo Skunkworks - Paul Xu deleted -
                             * 001 Begin
                             */
                            /*
                             * intent.putExtra(MediaPlayerService.RUN_TIME_EXTRA,
                             * 3);
                             */
                            /*
                             * PR 675449 - Neo Skunkworks - Paul Xu deleted -
                             * 001 End
                             */

                            startService(intent);

                            // "Others" Item clicked
                        }
                    }
                });

        dialogBuilder.setPositiveButton(R.string.timer_dialog_ok,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        // if (checkedItemPosition == 0) {
                        if (mFilePath == null) {
                            sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH)
                                    .commit();
                        } else {

                            sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, mFilePath).commit();

                        }

                        stopService(new Intent(CountDownActivity.this, MediaPlayerService.class));
                        dialog.dismiss();
                    }
                });

        dialogBuilder.setNegativeButton(R.string.timer_dialog_cancel,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        stopService(new Intent(CountDownActivity.this, MediaPlayerService.class));
                        dialog.dismiss();
                    }
                });
        // PR:510457 add by xibin start
        alertDialogTimerRingtoneChoose = dialogBuilder.create();
        // PR 593029 - Neo Skunkworks - Soar Gao - 001 begin
        // alertDialogTimerRingtoneChoose.setCancelable(false);
        alertDialogTimerRingtoneChoose.setCanceledOnTouchOutside(false);
        alertDialogTimerRingtoneChoose.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                // TODO Auto-generated method stub
                stopService(new Intent(CountDownActivity.this, MediaPlayerService.class));
            }
        });
        // PR 593029 - Neo Skunkworks - Soar Gao - 001 end
        alertDialogTimerRingtoneChoose.show();
        // PR:510457 add by xibin end
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == mRequestCode) {

            // add by asads end<--
            if (data != null) {

                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                if (uri != null) {

                    uri.toString();

                    mFilePath = uri.toString();

                }
            }

        }

    }

    private void setDefaultRingTone() {
        // retrieve the former ring
        SharedPreferences sharedPre = getSharedPreferences(TIMER_RPREFERENCES, MODE_PRIVATE);
        String preRingtonePath = sharedPre.getString(ALERT_RINGTONE_PATH_KEY, "none");
        // set for the first time
        if ("none".equals(preRingtonePath)) {
            String mediaPath = MEDIA_PATH_DEFAULT;
            String company = "";
            if ("ALCATEL".equals(company) || "FPT".equals(company)) {
                mediaPath = MEDIA_PATH_ALCATEL;
            }

            final File[] mediaFiles = new File(mediaPath).listFiles();
            if (mediaFiles != null && mediaFiles.length > 0) {
                sharedPre.edit()
                        .putString(ALERT_RINGTONE_PATH_KEY, mediaFiles[0].getAbsolutePath())
                        .commit();
            } else {
                sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH).commit();
            }
        }
    }

    public static String translateTimeToString(int secondSum) {
        int hour = secondSum / 3600;
        int minute = (secondSum - hour * 3600) / 60;
        int second = secondSum - hour * 3600 - minute * 60;

        return new StringBuilder(20).append(pad(hour)).append(":").append(pad(minute)).append(":")
                .append(pad(second)).toString();
    }

    private static Uri toUri(String s) {

        return android.net.Uri.parse(s);
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + c;
    }

    private void setButtonVisibility(int state) {
        /* PR 586309- Neo Skunkworks - Paul Xu modified - 001 Begin */
        switch (state) {
            case STOP_STATE:
                mStartBtn.setVisibility(View.VISIBLE);
                mStartBtn.setEnabled(true);
                mStartBtn.setBackgroundResource(R.drawable.timer_btn_green_bk);
                mStartBtn.setTextColor(Color.WHITE);
                mCancelBtn.setVisibility(View.GONE);
                break;
            case START_STATE:
                mStartBtn.setVisibility(View.GONE);
                mCancelBtn.setVisibility(View.VISIBLE);
                mCancelBtn.setBackgroundResource(R.drawable.timer_btn_gray_bk);
                mCancelBtn.setTextColor(Color.BLACK);
                break;
            default:
                mStartBtn.setVisibility(View.VISIBLE);
                mStartBtn.setBackgroundResource(R.drawable.timer_btn_gray_bk);
                mStartBtn.setTextColor(this.getResources().getColor(R.color.countdown_grey));
                mCancelBtn.setVisibility(View.GONE);
                break;
        }
    }
    /* PR 586309- Neo Skunkworks - Paul Xu modified - 001 End */
}
