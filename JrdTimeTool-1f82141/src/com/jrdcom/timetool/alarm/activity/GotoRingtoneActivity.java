package com.jrdcom.timetool.alarm.activity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.TimeToolActivity;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;

public class GotoRingtoneActivity extends TabActivity {
	// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
	public static int musicID;
	public static String musicPath;
	public static int musicActivity;
	public static String musicName;
	private HomeKeyEventBroadCastReceiver receiver;

	// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.alarm_set_goto_ringtone);

		// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
		musicID = -1;
		musicPath = "";
		musicName = "";
		receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		// PR 587415 - Neo Skunkworks - Soar Gao - 001 end

		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent1, intent2; // Reusable Intent for each tab

		intent1 = new Intent().setClass(this, SystemActivity.class);
		spec = tabHost
				.newTabSpec(
						getResources().getString(R.string.system_acticity_name))
				.setIndicator(
						this.getResources().getString(
								R.string.system_acticity_name))
				.setContent(intent1);
		tabHost.addTab(spec);
		intent2 = new Intent().setClass(this, MusicActivity.class);
		spec = tabHost
				.newTabSpec(
						getResources().getString(R.string.music_acticity_name))
				.setIndicator(
						this.getResources().getString(
								R.string.music_acticity_name))
				.setContent(intent2);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(2);
		musicActivity = 0;// PR 587415 - Neo Skunkworks - Soar Gao - add -001
		TabWidget tabWidget = tabHost.getTabWidget();
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			View child = tabWidget.getChildAt(i);
			final TextView tv = (TextView) child
					.findViewById(android.R.id.title);
			tv.setTextSize(22);
			// PR:491155 add by XIBIN
			tv.setTextColor(0xfff7b400);
			// remove by chengyun.wu for PR563321 fonts issue
			// tv.setTypeface(Typeface.DEFAULT);
			tv.setInputType(0);

		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// stopService(new Intent(this, MediaPlayerService.class));// PR 587415
		// - Neo Skunkworks - Soar Gao - delete -001
	}

	// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(state);
		musicActivity = state.getInt("musicActivity");
		musicID = state.getInt("musicID");
		musicPath = state.getString("musicPath");
		musicName = state.getString("musicName");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putInt("musicID", musicID);
		outState.putInt("musicActivity", musicActivity);
		outState.putString("musicPath", musicPath);
		outState.putString("musicName", musicName);
	}

	/**
	 * touch others out of dialog to stop ring
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		stopService(new Intent(this, MediaPlayerService.class));
		return super.onTouchEvent(event);
	}
	// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
}

// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {

	static final String SYSTEM_REASON = "reason";
	static final String SYSTEM_HOME_KEY = "homekey";// home key
	static final String SYSTEM_POWER_KEY = "lock";// power key
	static final String SYSTEM_RECENT_APPS = "recentapps";// long home key
	static final String SYSTEM_LONGPOWER_KEY = "globalactions";// long power key

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
			String reason = intent.getStringExtra(SYSTEM_REASON);
			if (reason != null) {
				if (reason.equals(SYSTEM_HOME_KEY)) {
					context.stopService(new Intent(context,
							MediaPlayerService.class));
					// PR 594450 - Neo Skunkworks - Soar Gao - 001 begin
					((Activity) context).finish();
					// PR 594450 - Neo Skunkworks - Soar Gao - 001 end
				} else if (reason.equals(SYSTEM_RECENT_APPS)) {
					context.stopService(new Intent(context,
							MediaPlayerService.class));
				} else if (reason.equals(SYSTEM_POWER_KEY)) {
					context.stopService(new Intent(context,
							MediaPlayerService.class));
				} else if (reason.equals(SYSTEM_LONGPOWER_KEY)) {
					context.stopService(new Intent(context,
							MediaPlayerService.class));
				}
			}
		}
	}
}
// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
