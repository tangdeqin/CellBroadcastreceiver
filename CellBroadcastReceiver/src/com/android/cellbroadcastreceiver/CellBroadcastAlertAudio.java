/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License.
 */
/*==================================================================================*/
/* Modifications on Features list / Changes Request / Problem Report                */
/* ----------|----------------------|----------------------|----------------------- */
/* 08/30/2014|     tianming.lei     |        777440        |Cell broadcast messages */
/*           |                      |                      | have an incorrect format*/
/* ----------|----------------------|----------------------|----------------------- */
/*==================================================================================*/

package com.android.cellbroadcastreceiver;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // MODIFIED by yuxuan.zhang, 2016-04-21,BUG-1112693
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.AudioSystem; // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1112693
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
//add by liang.zhang for Defect 6102916 at 2018-03-15 begin
import android.os.PowerManager;
import android.os.SystemClock;
//add by liang.zhang for Defect 6102916 at 2018-03-15 end
import android.os.Vibrator;
import android.os.SystemProperties;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils; // MODIFIED by yuxuan.zhang, 2016-10-12,BUG-1112693
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-05-07,BUG-1112693

import com.android.cb.util.TLog;

import java.util.Locale;

import static com.android.cellbroadcastreceiver.CellBroadcastReceiver.DBG;

 //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
 //CBC notification with pop up and tone alert + vibrate in CHILE
import android.database.Cursor;
import android.net.Uri;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
 //[FEATURE]-Add-END by TCTNB.Dandan.Fang

//add by liang.zhang for Defect 5960218 at 2018-02-06 begin
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;
import java.util.List;
//add by liang.zhang for Defect 5960218 at 2018-02-06 end

/**
 * Manages alert audio and vibration and text-to-speech. Runs as a service so that
 * it can continue to play if another activity overrides the CellBroadcastListActivity.
 */
public class CellBroadcastAlertAudio extends Service implements TextToSpeech.OnInitListener,
        TextToSpeech.OnUtteranceCompletedListener {
    private static final String TAG = "CellBroadcastAlertAudio";

    /** Action to start playing alert audio/vibration/speech. */
    static final String ACTION_START_ALERT_AUDIO = "ACTION_START_ALERT_AUDIO";

    /** Extra for alert audio duration (from settings). */
    public static final String ALERT_AUDIO_DURATION_EXTRA =
            "com.android.cellbroadcastreceiver.ALERT_AUDIO_DURATION";

    /** Extra for message body to speak (if speech enabled in settings). */
    public static final String ALERT_AUDIO_MESSAGE_BODY =
            "com.android.cellbroadcastreceiver.ALERT_AUDIO_MESSAGE_BODY";

    /** Extra for text-to-speech language (if speech enabled in settings). */
    public static final String ALERT_AUDIO_MESSAGE_LANGUAGE =
            "com.android.cellbroadcastreceiver.ALERT_AUDIO_MESSAGE_LANGUAGE";

    /** Extra for alert vibration enabled (from settings). */
    public static final String ALERT_VIBRATE_EXTRA =
            "com.android.cellbroadcastreceiver.ALERT_VIBRATE";
    /* aiyan-978029-Extra for alert audio enabled (from settings).T-Mobile requirement */
    public static final String ALERT_AUDIO_EXTRA =
            "com.android.cellbroadcastreceiver.ALERT_AUDIO";

    /** Extra for alert audio ETWS behavior (always vibrate, even in silent mode). */
    public static final String ALERT_AUDIO_ETWS_VIBRATE_EXTRA =
            "com.android.cellbroadcastreceiver.ALERT_AUDIO_ETWS_VIBRATE";

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
    /** Extra for wpas alert audio opt. */
    public static final String WPAS_ALERT_AUDIO_OPT_EXTRA =
            "com.android.cellbroadcastreceiver.WPAS_ALERT_AUDIO_OPT";
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    public static final String ALERT_REMINDER = "alert_reminder"; // MODIFIED by bin.huang, 2016-11-10,BUG-1112693
    /** Pause duration between alert sound and alert speech. */
    private static final int PAUSE_DURATION_BEFORE_SPEAKING_MSEC = 1000;

    // modify by liang.zhang for Defect 5772065 at 2018-01-08 begin
    /** Vibration uses the same on/off pattern as the CMAS alert tone */
    private static final long[] sVibratePattern = { 0, 2000, 500, 1000, 500, 1000, 500,
            2000, 500, 1000, 500, 1000, 500};
    // modify by liang.zhang for Defect 5772065 at 2018-01-08 end

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
    /** Vibration uses the same on/off pattern as the Wpas alert tone */
    public static final long[] sWpasVibratePattern = { // MODIFIED by yuxuan.zhang, 2016-05-09,BUG-1112693
            0, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500
    };
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    //MODIFIED-BEGIN by yuwan, 2017-04-05,BUG-4447081
    public static final int EMERGENCY_SOUND_DURATION = 10500; //MODIFIED by yuxuan.zhang, 2016-04-19,BUG-838839
    private static final int STATE_IDLE = 0;
    private static final int STATE_ALERTING = 1;
    private static final int STATE_PAUSING = 2;
    private static final int STATE_SPEAKING = 3;

    private int mState;

    private TextToSpeech mTts;
    private boolean mTtsEngineReady;

    private String mMessageBody;
    private String mMessageLanguage;
    private boolean mTtsLanguageSupported;
    private boolean mEnableVibrate;
    private boolean mEnableAudio;
    private int mAlertReminderValue; // MODIFIED by bin.huang, 2016-11-10,BUG-1112693
    private int mAlertOnlyOnce = 1;//MODIFIED-BEGIN by yuwan, 2017-04-05,BUG-4447081

    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private TelephonyManager mTelephonyManager;
    private int mInitialCallState;

    private PendingIntent mPlayReminderIntent;

    // Internal messages
    private static final int ALERT_SOUND_FINISHED = 1000;
    private static final int ALERT_PAUSE_FINISHED = 1001;
    // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
    // CBC notification with pop up and tone alert + vibrate in CHILE
    // Let mobile vibrate when screen off and vibrate loop.
    private static final int CHILE_VIBRATOR_LOOP = 1002;
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
    private static final int REPEAT_ALERT_START = 1003;
    private static final int CHILE_REPEAT_DURATION_VIBRATOR_SCREENOFF = 1000;
    private static final int CHILE_REPEAT_DURATION_VIBRATOR = 11000;
    boolean forceVibrateChile = false;
    private boolean mCmasRingtoneFlag = false; // MODIFIED by yuxuan.zhang, 2016-09-27,BUG-2845457
    private boolean mVibrate = true;
    // [FEATURE]-Add-END by TCTNB.Dandan.Fang
    boolean isPresientialAlert = false;//[BUGFIX] Add by bin.xue for PR1071073
    boolean isWpasAlert = false;
    boolean mAlreadySpeakFlag = false; // MODIFIED by yuxuan.zhang, 2016-05-13,BUG-1112693
    int mOriginalStreamVolume; // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1112693
    int mOriginalRingerMode = -1;
    private boolean isPresient = false; // MODIFIED by yuwan, 2017-06-05,BUG-4882170
    
    // add by liang.zhang for Defect 5912393 at 2018-01-29 begin
    private boolean isOriginalRingerMode = true;
    // add by liang.zhang for Defect 5912393 at 2018-01-29 end
    
    // add by liang.zhang for Defect 5960218 at 2018-02-06 begin
    private int channelId = -1;
    private boolean isUAE = false;
    private static final int UAE_CHANNEL1 = 4383;
    private static final int UAE_CHANNEL2 = 4384;
    // 30 seconds
    private static final long[] sVibratePatternForUAE = { 0, 2000, 500, 1000, 500, 1000, 500,
        2000, 500, 1000, 500, 1000, 500, 2000, 500, 1000, 500, 1000, 500 , 2000, 500, 1000, 500, 1000, 500,
        2000, 500, 1000, 500, 1000, 500 , 2000, 500};
    private int mAlertReminderValueForUAE = 30000;
    // add by liang.zhang for Defect 5960218 at 2018-02-06 end
    
    // Add by liang.zhang for Defect 6517924 at 2018-07-06 begin
    private boolean isNZ = false;
    // Add by liang.zhang for Defect 6517924 at 2018-07-06 end
    
    // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
    BroadcastReceiver mStopRingtoneReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ControlRingtoneHandler.STOP_RINGTONE)) {
        		if (android.provider.Settings.Global.getInt(getContentResolver(), ControlRingtoneHandler.CMAS_EMERGENCY_DISPLAY, 0) != 0) {
        			Log.i(TAG, "Release keys locked.");
        			android.provider.Settings.Global.putInt(getContentResolver(), ControlRingtoneHandler.CMAS_EMERGENCY_DISPLAY, 0);
            		
            		Log.i(TAG, "Turn off screen.");
            		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            		pm.goToSleep(SystemClock.uptimeMillis());
        		}
        		
        		Log.i(TAG, "Stop ringtone and vibrate,reset ringtone mode.");
    			stopSelf();
            }
        }
    };
    
    private boolean isPeru = false;
    
    private ControlRingtoneHandler mControlRingtoneHandler = ControlRingtoneHandler.getInstance(this);
    // add by liang.zhang for Defect 6102916 at 2018-03-15 end
    
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALERT_SOUND_FINISHED:
                    if (DBG) log("ALERT_SOUND_FINISHED");
                    stop();     // stop alert sound
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
                        if (mMessageBody != null && mTtsEngineReady && mTtsLanguageSupported && !mAlreadySpeakFlag) {
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693 */
                            mHandler.sendMessageDelayed(
                                    mHandler.obtainMessage(ALERT_PAUSE_FINISHED),
                                    PAUSE_DURATION_BEFORE_SPEAKING_MSEC);
                            mState = STATE_PAUSING;
                            mAlreadySpeakFlag = true;
                        } else {
                            //stopSelf();
                            mState = STATE_IDLE;
                        }
                    //MODIFIED-BEGIN by yuwan, 2017-04-05,BUG-4447081
                        log("mAlertReminderValue = "+mAlertReminderValue);
                        /* MODIFIED-BEGIN by bin.huang, 2016-11-10,BUG-1112693*/
                        if (CellBroadcastAlertService.ALERT_ONLY_ONCE == mAlertReminderValue &&
                                mAlertOnlyOnce == 1) {
                            mHandler.sendEmptyMessageDelayed(REPEAT_ALERT_START, 2 * 60 * 1000);
                            mAlertOnlyOnce = 0;
                        } else if (CellBroadcastAlertService.ALERT_NEVER == mAlertReminderValue){

                        } else if(CellBroadcastAlertService.ALERT_TWO_MINUTES_INTERVAL == mAlertReminderValue ||
                                CellBroadcastAlertService.ALERT_FIFTENN_MINUTES == mAlertReminderValue){
                            mHandler.sendEmptyMessageDelayed(REPEAT_ALERT_START,
                                    mAlertReminderValue * 60 * 1000);
                                    /* MODIFIED-END by bin.huang,BUG-1112693*/
                        }
                    //MODIFIED-END by yuwan, 2017-04-05,BUG-4447081
                        //break; // MODIFIED by yuxuan.zhang, 2016-05-13,BUG-1112693
                    //}
                    TLog.w(TAG, "mTtsEngineReady =" + mTtsEngineReady
                            + ";mTtsLanguageSupported=" + mTtsLanguageSupported);
                    // if we can speak the message text
//                    if (mMessageBody != null && mTtsEngineReady && mTtsLanguageSupported) {
//                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
//                        mHandler.sendMessageDelayed(mHandler.obtainMessage(ALERT_PAUSE_FINISHED),
//                                PAUSE_DURATION_BEFORE_SPEAKING_MSEC);
//                        mState = STATE_PAUSING;
//                    } else {
//                        Log.w(TAG, "stopSelf");
//                        stopSelf();
//                        mState = STATE_IDLE;
//                    }
                    break;

                case ALERT_PAUSE_FINISHED:
                    if (DBG) log("ALERT_PAUSE_FINISHED");
                    if (mMessageBody != null && mTtsEngineReady && mTtsLanguageSupported) {
                        if (DBG) log("Speaking broadcast text: " + mMessageBody);
                        // [BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
                        if(!getResources().getBoolean(R.bool.def_ssv_emergency_cb_alert_always_on)){
                            mTts.speak(mMessageBody, TextToSpeech.QUEUE_FLUSH, null);
                            mState = STATE_SPEAKING;
                        }
                        //[BUGFIX]-Mod-EBD by TSCD.tianming.lei
                    } else {
                        loge("TTS engine not ready or language not supported");
                        stopSelf();
                        mState = STATE_IDLE;
                    }
                    break;
            // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
            // CBC notification with pop up and tone alert + vibrate in CHILE
            case CHILE_VIBRATOR_LOOP:
                log("CHILE_VIBRATOR_LOOP");
                mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // modify by liang.zhang for Defect 5772065 at 2018-01-08 begin
                int isRepeat = -1;
                if (getResources().getBoolean(R.bool.def_ssv_emergency_cb_alert_always_on)) {
                	isRepeat = 0;
                }
                mVibrator.vibrate(sVibratePattern, isRepeat);
                // modify by liang.zhang for Defect 5772065 at 2018-01-08 end
                mHandler.sendMessageDelayed(
                        mHandler.obtainMessage(CHILE_VIBRATOR_LOOP), CHILE_REPEAT_DURATION_VIBRATOR);
                break;
            // [FEATURE]-Add-END by TCTNB.Dandan.Fang
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
                case REPEAT_ALERT_START:
                    play(EMERGENCY_SOUND_DURATION, false);
                    break;
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                default:
                    loge("Handler received unknown message, what=" + msg.what);
            }
        }
    };

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // Stop the alert sound and speech if the call state changes.
            if (state != TelephonyManager.CALL_STATE_IDLE
                    && state != mInitialCallState) {
                stopSelf();
            }
        }
    };

    /**
     * Callback from TTS engine after initialization.
     * @param status {@link TextToSpeech#SUCCESS} or {@link TextToSpeech#ERROR}.
     */
    @Override
    public void onInit(int status) {
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
        if (DBG)
            Log.i(TAG, "onInit() TTS engine status: " + status);
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        if (status == TextToSpeech.SUCCESS) {
            mTtsEngineReady = true;
            // try to set the TTS language to match the broadcast
            setTtsLanguage();
        } else {
            mTtsEngineReady = false;
            mTts = null;
            Log.e(TAG, "onInit() TTS engine error: " + status); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
        }
    }

    /**
     * Try to set the TTS engine language to the value of mMessageLanguage.
     * mTtsLanguageSupported will be updated based on the response.
     */
    private void setTtsLanguage() {
        if (mMessageLanguage != null) {
            if (DBG) log("Setting TTS language to '" + mMessageLanguage + '\'');
            int result = mTts.setLanguage(new Locale(mMessageLanguage));
            // success values are >= 0, failure returns negative value
            if (DBG) log("TTS setLanguage() returned: " + result);
            mTtsLanguageSupported = result >= 0;
        } else {
            // try to use the default TTS language for broadcasts with no language specified
            if (DBG) log("No language specified in broadcast: using default");
            mTtsLanguageSupported = true;
        }
    }

    /**
     * Callback from TTS engine.
     * @param utteranceId the identifier of the utterance.
     */
    @Override
    public void onUtteranceCompleted(String utteranceId) {
        stopSelf();
    }

    @Override
    public void onCreate() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        mCmasRingtoneFlag = getResources().getBoolean(R.bool.def_cb_cmas_alert_fixed_ringtone); // MODIFIED by yuxuan.zhang, 2016-09-27,BUG-2845457
        // Listen for incoming calls to kill the alarm.
        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(
                mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
        // CBC notification with pop up and tone alert + vibrate in CHILE
        // when screen off , need to start vibrator again
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        getBaseContext().registerReceiver(mScreenOffReceiver, filter);
        // [FEATURE]-Add-END by TCTNB.Dandan.Fang
        
        // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
        IntentFilter ringtoneFilter = new IntentFilter();
        ringtoneFilter.addAction(ControlRingtoneHandler.STOP_RINGTONE);
        getBaseContext().registerReceiver(mStopRingtoneReceiver, ringtoneFilter);
        // add by liang.zhang for Defect 6102916 at 2018-03-15 end
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "services onDestroy "); // MODIFIED by yuxuan.zhang, 2016-07-11,BUG-1112693
        // stop audio, vibration and TTS
        stop();
        // Stop listening for incoming calls.
        mTelephonyManager.listen(mPhoneStateListener, 0);
        // shutdown TTS engine
        if (mTts != null) {
            try {
                mTts.shutdown();
            } catch (IllegalStateException e) {
                // catch "Unable to retrieve AudioTrack pointer for stop()" exception
                loge("exception trying to shutdown text-to-speech");
            }
        }
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1112693*/
        if (null != mAudioManager && !isWpasAlert && (mCmasRingtoneFlag || isPresient)) { // MODIFIED by yuwan, 2017-06-05,BUG-4882170
            Log.i(TAG, "OriginalStreamVolume onDestroy= " + mOriginalStreamVolume); // MODIFIED by yuxuan.zhang, 2016-06-06,BUG-2251957
            mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mOriginalStreamVolume, 0);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-2251957*/
            Log.i(TAG, "mOriginalRingerMode onDestroy= " + mOriginalRingerMode);
            if (mOriginalRingerMode != -1) {
                mAudioManager.setRingerMode(mOriginalRingerMode);
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-2251957*/
            
            // add by liang.zhang for Defect 5912393 at 2018-01-29 begin
            isOriginalRingerMode = true;
            // add by liang.zhang for Defect 5912393 at 2018-01-29 end
        }
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
        // CBC notification with pop up and tone alert + vibrate in CHILE
        getBaseContext().unregisterReceiver(mScreenOffReceiver);
        // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
        getBaseContext().unregisterReceiver(mStopRingtoneReceiver);
        // add by liang.zhang for Defect 6102916 at 2018-03-15 end
        // [FEATURE]-Add-END by TCTNB.Dandan.Fang
        // release CPU wake lock acquired by CellBroadcastAlertService
        CellBroadcastAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
    private boolean isPeruCMASChannel(int channelId) {
    	Log.v(TAG, "channelId = " + channelId);
    	switch(channelId) {
	    	case 4370:
	    	case 4380:
	    	case 4381:
	    	case 4382:
	    	case 4383:
	    	case 4396:
	    	case 4397:
	    	case 4398:
	    	case 4399:
	    		return true;
    		
    		default:
    			return false;
    	}
    }
    // add by liang.zhang for Defect 6102916 at 2018-03-15 end

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
        cancelRepeat();
        
        // add by liang.zhang for Defect 5968759 at 2018-02-06 begin
        channelId = intent.getIntExtra("channelId", -1);
        Log.i(TAG, "channelId = "+ channelId);
        SubscriptionManager subscriptionManager = SubscriptionManager.from(getBaseContext());
        List<SubscriptionInfo> subList = subscriptionManager.getActiveSubscriptionInfoList();
    	if (subList != null && subList.size() > 0) {
    		for (int i = 0; i < subList.size(); i++) {
    			SubscriptionInfo info = subList.get(i);
    			if (info!= null && info.getMcc() == 424) {
    				isUAE = true;
    	        }
    		    // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
    			if (info!= null && info.getMcc() == 716) {
    				isPeru = true;
    			}
    		    // add by liang.zhang for Defect 6102916 at 2018-03-15 end
    			
    		    // Add by liang.zhang for Defect 6517924 at 2018-07-06 begin
    			if (info!= null && info.getMcc() == 530) {
    				isNZ = true;
    			}
    		    // Add by liang.zhang for Defect 6517924 at 2018-07-06 end
    		}
    	}
        // add by liang.zhang for Defect 5968759 at 2018-02-06 end
    	
        // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
    	if (isPeru) {
        	if (isPeruCMASChannel(channelId) &&
        			android.provider.Settings.Global.getInt(getContentResolver(), ControlRingtoneHandler.CMAS_EMERGENCY_DISPLAY, 0) != 1) {
    			android.provider.Settings.Global.putInt(getContentResolver(), ControlRingtoneHandler.CMAS_EMERGENCY_DISPLAY, 1);
    		}
        	Message msg =  new Message();
        	msg.what = ControlRingtoneHandler.MESSAGE_STOP_RINGTONE;
        	mControlRingtoneHandler.removeMessages(ControlRingtoneHandler.MESSAGE_STOP_RINGTONE);
        	mControlRingtoneHandler.sendMessageDelayed(msg, 3 * 60 * 1000);
        }
        // add by liang.zhang for Defect 6102916 at 2018-03-15 begin
    	
	    // Add by liang.zhang for Defect 6517924 at 2018-07-06 begin
    	if (isNZ && channelId != 4370) {
    		mCmasRingtoneFlag = false;
    	} else {
    		mCmasRingtoneFlag = getResources().getBoolean(R.bool.def_cb_cmas_alert_fixed_ringtone);
    	}
	    // Add by liang.zhang for Defect 6517924 at 2018-07-06 begin
        
        mAlreadySpeakFlag = false;
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        // This extra should always be provided by CellBroadcastAlertService,
        // but default to 10.5 seconds just to be safe (CMAS requirement).
        int duration = intent.getIntExtra(ALERT_AUDIO_DURATION_EXTRA, 10500);
        isPresient = intent.getBooleanExtra("Presient", false); // MODIFIED by yuwan, 2017-06-05,BUG-4882170

      //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
      //CBC notification with pop up and tone alert + vibrate in CHILE
      forceVibrateChile = intent.getBooleanExtra("forceVibrate", false);
      isPresientialAlert = intent.getBooleanExtra("PresientialAlert", false);//[BUGFIX] Add by bin.xue for PR1071073
      isWpasAlert = intent.getBooleanExtra(WPAS_ALERT_AUDIO_OPT_EXTRA, false); // MODIFIED by yuxuan.zhang, 2016-04-21,BUG-1112693
      Log.i(TAG, "chile, isPresientialAlert= "+isPresientialAlert);
      mAlertReminderValue = intent.getIntExtra(ALERT_REMINDER,1); // MODIFIED by bin.huang, 2016-11-10,BUG-1112693
      if (forceVibrateChile && !isWpasAlert) { // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1112693
            if (mAudioManager != null) {
                Log.i(TAG,"chile, it is always vibrate,even in silent mode mAudioManager.getRingerMode() = "
                                + mAudioManager.getRingerMode());
            }
        mEnableVibrate = forceVibrateChile;
        switch (mAudioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                case AudioManager.RINGER_MODE_VIBRATE:
                    mEnableAudio = false;
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                default:
                    mEnableAudio = true;
                    break;
                }
      } else {
          //[FEATURE]-Add-END by TCTNB.Dandan.Fang
        // Get text to speak (if enabled by user)
        mMessageBody = intent.getStringExtra(ALERT_AUDIO_MESSAGE_BODY);
        mMessageLanguage = intent.getStringExtra(ALERT_AUDIO_MESSAGE_LANGUAGE);

        mEnableVibrate = intent.getBooleanExtra(ALERT_VIBRATE_EXTRA, true);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-07,BUG-1112693*/
            if (getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)
                    || isWpasAlert) {
                mEnableAudio = intent.getBooleanExtra(ALERT_AUDIO_EXTRA, true);// aiyan-978029
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        boolean forceVibrate = intent.getBooleanExtra(ALERT_AUDIO_ETWS_VIBRATE_EXTRA, false);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1112693*/
            if (intent.getBooleanExtra(ALERT_AUDIO_ETWS_VIBRATE_EXTRA, false)) {
                mEnableVibrate = true; // force enable vibration for ETWS alerts
            }
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            if (mAudioManager != null) {
                Log.i(TAG, "mAudioManager.getRingerMode() = " + mAudioManager.getRingerMode());
            }
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
        switch (mAudioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                if (DBG) Log.i(TAG,"Ringer mode: silent");
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                if (getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
                    mEnableVibrate = forceVibrate;// force enable vibration for ETWS alerts
                }
                mEnableAudio = false;
                mVibrate = false;
                /* MODIFIED-BEGIN by yuwan, 2017-06-05,BUG-4882170*/
                if (mCmasRingtoneFlag || isPresient) {
                    mVibrate = true;
                }
                /* MODIFIED-END by yuwan,BUG-4882170*/
                break;

            case AudioManager.RINGER_MODE_VIBRATE:
                if (DBG) Log.i(TAG,"Ringer mode: vibrate"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                mEnableAudio = false;
                mVibrate = true;
                break;

            case AudioManager.RINGER_MODE_NORMAL:
                mVibrate = true;
                //[BUGFIX]-Delete-BEGIN by TSCD.tianming.lei,01/13/2015,PR-891330
                //[FEATURE]-begin-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
                /*
                if (Settings.System.getInt(getContentResolver(),
                        Settings.System.VIBRATE_WHEN_RINGING, 0) == 0) {
                    mEnableVibrate = false;
                } else {
                    mEnableVibrate = true;
                }
                */
                //[FEATURE]-END-Add by TCTNB.yugang.jia,09/06/2013,FR-516039,
                //[BUGFIX]-Delete-END by TSCD.tianming.lei
            default:
                if (DBG) Log.i(TAG,"Ringer mode: normal"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                if (!getResources().getBoolean(R.bool.cellbroadcastreceiver_tmo_request_enable)) {
                    mEnableAudio = true;
                }
                break;
        }

            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
            if (mMessageBody != null && (mEnableAudio || isWpasAlert)) {
                mAudioManager.setSpeakerphoneOn(true); // MODIFIED by yuxuan.zhang, 2016-08-24,BUG-1112693
                if (mTts == null) {
                    mTts = new TextToSpeech(this, this);
                } else if (mTtsEngineReady) {
                    setTtsLanguage();
                }
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            }
        //[BUGFIX]-Add-BEGIN-by bin.xue for PR1071073
        if(isPresientialAlert){
            mEnableVibrate = true;
        }
        //[BUGFIX]-Add-END-by bin.xue
      } //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,Add by chenglin.jiang For PR1017965
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-12,BUG-1112693*/
      	// modify by liang.zhang for Defect 5912393 at 2018-01-29 begin
        if (isOriginalRingerMode && !isWpasAlert && (mCmasRingtoneFlag || isPresient)) { // MODIFIED by yuwan, 2017-06-05,BUG-4882170
            mEnableAudio = true;
            int streamType = AudioManager.STREAM_NOTIFICATION;
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-06,BUG-2251957*/
            mOriginalStreamVolume = mAudioManager.getStreamVolume(streamType);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-12,BUG-2251957*/
            mOriginalRingerMode = mAudioManager.getRingerMode();
            Log.i(TAG, "mOriginalRingerMode = " + mOriginalRingerMode);
            /* MODIFIED-END by yuxuan.zhang,BUG-2251957*/
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
            Log.i(TAG, "OriginalStreamVolume = " + mOriginalStreamVolume);
            int MaxStreamVolume = mAudioManager.getStreamMaxVolume(streamType);
            Log.i(TAG, "mMaxStreamVolume = " + MaxStreamVolume);
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); // MODIFIED by yuxuan.zhang, 2016-09-27,BUG-2845457
            mAudioManager.setStreamVolume(streamType, MaxStreamVolume, 0); // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1748495
            /* MODIFIED-END by yuxuan.zhang,BUG-2251957*/
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            isOriginalRingerMode = false;
        }
      	// modify by liang.zhang for Defect 5912393 at 2018-01-29 end
        if (mEnableAudio || mEnableVibrate || isWpasAlert) {
        /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
            //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
            //CBC notification with pop up and tone alert + vibrate in CHILE
            // play(duration);     // in milliseconds
            play(duration , forceVibrateChile);     // in milliseconds
            //[FEATURE]-Add-END by TCTNB.Dandan.Fang
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Record the initial call state here so that the new alarm has the
        // newest state.
        mInitialCallState = mTelephonyManager.getCallState();

      return START_STICKY;
    }

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    /**
     * Start playing the alert sound, and send delayed message when it's time to stop.
     * @param duration the alert sound duration in milliseconds
     */
    //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
    //CBC notification with pop up and tone alert + vibrate in CHILE
    //private void play(int duration) {
    //forceVibrateChile is used to choose specified tone for chile request
    private void play(int duration , boolean forceVibrateChile) {
    //[FEATURE]-Add-END by TCTNB.Dandan.Fang
        // stop() checks to see if we are already playing.
        stop();
        log("duration =" + duration);
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
        if (DBG)
            Log.w(TAG, "play()");
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

        // Start the vibration first.
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
        /* MODIFIED-BEGIN by yuwan, 2014-04-17,BUG-4577042*/
        // modify by liang.zhang for Defect 5772065 at 2018-01-08 begin
        int isRepeat = -1;
        if (getResources().getBoolean(R.bool.def_ssv_emergency_cb_alert_always_on)) {
        	isRepeat = 0;
        }
        
        if (mVibrate) {
            if (isWpasAlert && mEnableVibrate) {
                mVibrator.vibrate(sWpasVibratePattern, isRepeat);
            } else { // modify by liang.zhang for defect 6925301 at 2018-09-11
                // mVibrator.vibrate(sVibratePattern, -1);
            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                if (forceVibrateChile) {
                    mVibrator.vibrate(sVibratePattern, isRepeat);
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(CHILE_VIBRATOR_LOOP),
                            CHILE_REPEAT_DURATION_VIBRATOR);
                } else {
                	// modify by liang.zhang for Defect 5968759 at 2018-02-06 begin
                	if (isUAE && mOriginalRingerMode != AudioManager.RINGER_MODE_SILENT && isRepeat == -1 && (channelId == UAE_CHANNEL1 || channelId == UAE_CHANNEL2)) {
                		mVibrator.vibrate(sVibratePatternForUAE, -1);
                	} else {
                		mVibrator.vibrate(sVibratePattern, isRepeat);
                	}
                    // modify by liang.zhang for Defect 5968759 at 2018-02-06 end
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
                    // [BUGFIX]-Add-BEGIN-by bin.xue for PR1071073
                    if (getResources().getBoolean(
                            R.bool.feature_cellbroadcastreceiver_forceVibrateForChile_on)
                            && isPresientialAlert) {
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(CHILE_VIBRATOR_LOOP),
                                CHILE_REPEAT_DURATION_VIBRATOR);
                    }
                    // [BUGFIX]-Add-END-by bin.xue
                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                }
            }
        }
        // modify by liang.zhang for Defect 5772065 at 2018-01-08 end
        /* MODIFIED-END by yuwan, 2014-04-17,BUG-4577042*/

        if (mEnableAudio || isWpasAlert) { // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1112693
            // future optimization: reuse media player object
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "Error occurred while playing audio."); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                    mp.stop();
                    mp.release();
                    mMediaPlayer = null;
                    return true;
                }
            });

            try {
                // Check if we are in a call. If we are, play the alert
                // sound at a low volume to not disrupt the call.
                if (mTelephonyManager.getCallState()
                        != TelephonyManager.CALL_STATE_IDLE) {
                    log("in call: reducing volume");
                    mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                }
                //Modify by chenglin.jiang for PR1017965 Begin
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
                // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
                // CBC notification with pop up and tone alert + vibrate in CHILE
                if (forceVibrateChile) {
                    Log.i(TAG, "use specified tone for chile"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                    // [BUGFIX]-Mod-BEGIN-by bin.xue for PR1071073
                    if (getResources().getBoolean(
                            R.bool.def_cellbroadcastreceiver_use_cmas_ringToneForChile)) {
                        setDataSourceFromResource(getResources(), mMediaPlayer,
                                R.raw.attention_signal);
                    } else {
                        String cbRingtoneName = this.getResources().getString(
                                R.string.def_cellbroadcastreceiver_ringToneForChile);
                        String defaultToneUri = null;
                        Cursor c = null;
                        try {
                            c = this.getContentResolver().query(
                                    Uri.parse("content://media/internal/file"), null,
                                    "_display_name=?",
                                    new String[] {
                                        cbRingtoneName
                                    }, null);

                            if (c != null && c.moveToFirst()) {
                                String id = c.getString(c.getColumnIndex("_id"));
                                defaultToneUri = "content://media/internal/file/" + id;
                            } else {
                                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-31,BUG-1112693*/
                                Log.w(TAG, "Not found default cb ringtone name:" + cbRingtoneName);
                            }
                        } catch (SQLiteException e) {
                            Log.e(TAG, "SQLiteException:" + e.getMessage());
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                        } finally {
                            if (c != null) {
                                c.close();
                            }
                        }
                        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-08-24,BUG-1112693*/
                        if (!TextUtils.isEmpty(defaultToneUri)) {
                            mMediaPlayer.setDataSource(getBaseContext(), Uri.parse(defaultToneUri),
                                    null);
                        }else{
                            Log.w(TAG, "Use the default cmas ringtone");
                            setDataSourceFromResource(getResources(), mMediaPlayer,
                                    R.raw.attention_signal);
                        }
                                /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    }
                    // [BUGFIX]-Mod-END-by bin.xue
                } else {
		        //[FEATURE]-Add-END by TCTNB.Dandan.Fang
                //Modify by chenglin.jiang for PR1017965 End
                // start playing alert audio (unless master volume is vibrate only or silent).

                    setDataSourceFromResource(getResources(), mMediaPlayer,
                            (isWpasAlert ? R.raw.alarm_alert : R.raw.attention_signal));
                            /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                } //[FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,04/11/2013,FR400297,
                mAudioManager.requestAudioFocus(null, AudioSystem.STREAM_NOTIFICATION, // MODIFIED by yuxuan.zhang, 2016-05-12,BUG-1112693
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                startAlarm(mMediaPlayer);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to play alert sound: ", ex); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
            }
        }

        // stop alert after the specified duration
                // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
        // CBC notification with pop up and tone alert + vibrate in CHILE
        // fix error:audio and vibrator stop after duration
        // mHandler.sendMessageDelayed(mHandler.obtainMessage(ALERT_SOUND_FINISHED),
        // duration);
        // [BUGFIX]-Mod-BEGIN by TSCD.tianming.lei,08/30/2014,777440,
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-04-21,BUG-1112693*/
        if(!getResources().getBoolean(R.bool.def_ssv_emergency_cb_alert_always_on) || isWpasAlert){
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-27,BUG-2204561*/
            Log.w(TAG, "forceVibrateChile = "+forceVibrateChile);
            if (!forceVibrateChile) {
                //[BUGFIX]-Mod-BEGIN-by bin.xue for PR1071073
                if (isPresientialAlert) {
                    // do nothing
                } else {
                    TLog.w(TAG, "finish");
                    // modify by liang.zhang for Defect 5968759 at 2018-02-06 begin
                    /* MODIFIED-END by yuxuan.zhang,BUG-2204561*/
                    if (isUAE && mOriginalRingerMode != AudioManager.RINGER_MODE_SILENT && (channelId == UAE_CHANNEL1 || channelId == UAE_CHANNEL2)) {
                    	Log.w(TAG, "This is a UAE Emergency alert(4383/4384), set a 30 seconds duration.");
                    	mHandler.sendEmptyMessageDelayed(ALERT_SOUND_FINISHED, mAlertReminderValueForUAE);
                    } else {
                    	mHandler.sendEmptyMessageDelayed(ALERT_SOUND_FINISHED, duration);
                    }
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    // modify by liang.zhang for Defect 5968759 at 2018-02-06 end
                }
                //[BUGFIX]-Mod-END-by bin.xue
            }
        }
        //[BUGFIX]-Mod-END by TSCD.tianming.lei
        // [FEATURE]-Add-END by TCTNB.Dandan.Fang
        mState = STATE_ALERTING;
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-13,BUG-1112693*/
    public void cancelRepeat() {
        mHandler.removeMessages(REPEAT_ALERT_START);
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/

    // Do the common stuff when starting the alarm.
    private static void startAlarm(MediaPlayer player)
            throws java.io.IOException, IllegalArgumentException, IllegalStateException {
        player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        player.setLooping(true);
        player.prepare();
        player.start();
    }

    private static void setDataSourceFromResource(Resources resources,
            MediaPlayer player, int res) throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            Log.w(TAG, "afd != null"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    private void playAlertReminderSound() {
        Uri notificationUri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION | RingtoneManager.TYPE_ALARM);
        if (notificationUri == null) {
            loge("Can't get URI for alert reminder sound");
            return;
        }
        Ringtone r = RingtoneManager.getRingtone(this, notificationUri);
        if (r != null) {
            log("playing alert reminder sound");
            r.play();
        } else {
            loge("can't get Ringtone for alert reminder sound");
        }
    }

    /**
     * Stops alert audio and speech.
     */
    public void stop() {
        if (DBG) Log.w(TAG,"stop()"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693

        if (mPlayReminderIntent != null) {
            mPlayReminderIntent.cancel();
            mPlayReminderIntent = null;
        }

        mHandler.removeMessages(ALERT_SOUND_FINISHED);
        mHandler.removeMessages(ALERT_PAUSE_FINISHED);
        cancelRepeat(); // MODIFIED by yuxuan.zhang, 2016-06-20,BUG-2344436
        // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
        // CBC notification with pop up and tone alert + vibrate in CHILE
        if (forceVibrateChile) {
            mHandler.removeMessages(CHILE_VIBRATOR_LOOP);
        }
        // [FEATURE]-Add-END by TCTNB.Dandan.Fang
        //[BUGFIX]-Add-BEGIN-by bin.xue for PR1071073
        if(getResources().getBoolean(R.bool.feature_cellbroadcastreceiver_forceVibrateForChile_on) && isPresientialAlert){
            mHandler.removeMessages(CHILE_VIBRATOR_LOOP);
        }
        //[BUGFIX]-Add-END-by bin.xue
        if (mState == STATE_ALERTING || mState == STATE_SPEAKING) { // MODIFIED by yuxuan.zhang, 2016-06-20,BUG-2344436
            // Stop audio playing
            if (mMediaPlayer != null) {
                try {
                    Log.i(TAG, "stop service"); // MODIFIED by yuxuan.zhang, 2016-07-11,BUG-1112693
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                } catch (IllegalStateException e) {
                    // catch "Unable to retrieve AudioTrack pointer for stop()" exception
                    Log.e(TAG,"exception trying to stop media player"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
                }
                mMediaPlayer = null;
            }

            // Stop vibrator
            mVibrator.cancel();
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-06-20,BUG-2344436*/
        }

        if (mState == STATE_SPEAKING && mTts != null) {
        /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/
            try {
                mTts.stop();
            } catch (IllegalStateException e) {
                // catch "Unable to retrieve AudioTrack pointer for stop()" exception
                Log.e(TAG,"exception trying to stop text-to-speech"); // MODIFIED by yuxuan.zhang, 2016-05-31,BUG-1112693
            }
        }

        mAudioManager.abandonAudioFocus(null);
        mState = STATE_IDLE;
    }

    private static void log(String msg) {
        TLog.d(TAG, msg);
    }

    private static void loge(String msg) {
        TLog.e(TAG, msg);
    }
    // stop alert after the specified duration
    // [FEATURE]-Add-BEGIN by TCTNB.Dandan.Fang,05/06/2013,FR400297,
    // CBC notification with pop up and tone alert + vibrate in CHILE
    // fix error :when screen off , vibrator stop.
    BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (forceVibrateChile) {
                    TLog.i("fangdandan","screen is off , need to turn vibrate on");
                    mHandler.sendMessageDelayed(mHandler
                            .obtainMessage(CHILE_VIBRATOR_LOOP),
                            CHILE_REPEAT_DURATION_VIBRATOR_SCREENOFF);
                }
            }
        }
    };
    // [FEATURE]-Add-END by TCTNB.Dandan.Fang

}

// add by liang.zhang for Defect 6102916 at 2018-03-15 begin
class ControlRingtoneHandler extends Handler {
	private static final String TAG = "CellBroadcastAlertAudio#ControlRingtoneHandler";
	public static final int MESSAGE_STOP_RINGTONE = 1000;
	public static final String CMAS_EMERGENCY_DISPLAY = "cmas_emergency_display";
	public static final String STOP_RINGTONE = "stop_ringtone";
	
	private static ControlRingtoneHandler instance;
	private static Context mContext;
	
	public static ControlRingtoneHandler getInstance(Context context) {
		mContext = context;
		if (instance == null) {
			synchronized(ControlRingtoneHandler.class) {
				if (instance == null) {
					instance = new ControlRingtoneHandler();
				}
			}
		}
		return instance;
	}
	
	@Override
    public void handleMessage(Message msg) {
    	if (msg.what == MESSAGE_STOP_RINGTONE) {
    		if (mContext == null) {
    			Log.i(TAG, "mContext is null!!");
    			return;
    		}
    		Log.i(TAG, "send stop ringtone broadcast");
    		Intent intent = new Intent();
    		intent.setAction(STOP_RINGTONE);
    		mContext.sendBroadcast(intent);
    	}
    	super.handleMessage(msg);
    }
}
// add by liang.zhang for Defect 6102916 at 2018-03-15 end