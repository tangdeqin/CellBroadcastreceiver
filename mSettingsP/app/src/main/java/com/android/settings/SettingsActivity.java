/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toolbar;
import com.android.internal.util.ArrayUtils;
import com.android.settings.Settings.WifiSettingsActivity;
import com.android.settings.backup.BackupSettingsActivity;
import com.android.settings.core.gateway.SettingsGateway;
import com.android.settings.core.instrumentation.MetricsFeatureProvider;
import com.android.settings.core.instrumentation.SharedPreferencesLogger;
import com.android.settings.dashboard.DashboardFeatureProvider;
import com.android.settings.dashboard.DashboardSummary;
import com.android.settings.development.DevelopmentSettings;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.SearchActivity;
import com.android.settings.wfd.WifiDisplaySettings;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
//Begin added by miaoliu for XR5429846 on 2017/11/6
import android.view.Menu;
import android.app.Activity;
//End added by miaoliu for XR5429846 on 2017/11/6
//Begin added by shilei.zhang for XR7238432 on 2018/12/26
import android.view.inputmethod.InputMethodManager;
//End added by shilei.zhang for XR7238432 on 2018/12/26
public class SettingsActivity extends SettingsDrawerActivity
        implements PreferenceManager.OnPreferenceTreeClickListener,
        PreferenceFragment.OnPreferenceStartFragmentCallback,
        ButtonBarHandler, FragmentManager.OnBackStackChangedListener, OnClickListener {

    private static final String LOG_TAG = "Settings";

    // Constants for state save/restore
    private static final String SAVE_KEY_CATEGORIES = ":settings:categories";
    @VisibleForTesting
    static final String SAVE_KEY_SHOW_HOME_AS_UP = ":settings:show_home_as_up";

    /**
     * When starting this activity, the invoking Intent can contain this extra
     * string to specify which fragment should be initially displayed.
     * <p/>Starting from Key Lime Pie, when this argument is passed in, the activity
     * will call isValidFragment() to confirm that the fragment class name is valid for this
     * activity.
     */
    public static final String EXTRA_SHOW_FRAGMENT = ":settings:show_fragment";

    /**
     * The metrics category constant for logging source when a setting fragment is opened.
     */
    public static final String EXTRA_SOURCE_METRICS_CATEGORY = ":settings:source_metrics";

    /**
     * When starting this activity and using {@link #EXTRA_SHOW_FRAGMENT},
     * this extra can also be specified to supply a Bundle of arguments to pass
     * to that fragment when it is instantiated during the initial creation
     * of the activity.
     */
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args";

    /**
     * Fragment "key" argument passed thru {@link #EXTRA_SHOW_FRAGMENT_ARGUMENTS}
     */
    public static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";

    public static final String BACK_STACK_PREFS = ":settings:prefs";

    // extras that allow any preference activity to be launched as part of a wizard

    // show Back and Next buttons? takes boolean parameter
    // Back will then return RESULT_CANCELED and Next RESULT_OK
    protected static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";

    // add a Skip button?
    private static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip";

    // specify custom text for the Back or Next buttons, or cause a button to not appear
    // at all by setting it to null
    protected static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
    protected static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";

    /**
     * When starting this activity and using {@link #EXTRA_SHOW_FRAGMENT},
     * those extra can also be specify to supply the title or title res id to be shown for
     * that fragment.
     */
    public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":settings:show_fragment_title";
    /**
     * The package name used to resolve the title resource id.
     */
    public static final String EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME =
            ":settings:show_fragment_title_res_package_name";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE_RESID =
            ":settings:show_fragment_title_resid";
    public static final String EXTRA_SHOW_FRAGMENT_AS_SHORTCUT =
            ":settings:show_fragment_as_shortcut";

    public static final String EXTRA_SHOW_FRAGMENT_AS_SUBSETTING =
            ":settings:show_fragment_as_subsetting";

    @Deprecated
    public static final String EXTRA_HIDE_DRAWER = ":settings:hide_drawer";

    public static final String META_DATA_KEY_FRAGMENT_CLASS =
        "com.android.settings.FRAGMENT_CLASS";

    private static final String EXTRA_UI_OPTIONS = "settings:ui_options";

    private static final int REQUEST_SUGGESTION = 42;

    private String mFragmentClass;

    private CharSequence mInitialTitle;
    private int mInitialTitleResId;

    private static final String[] LIKE_SHORTCUT_INTENT_ACTION_ARRAY = {
            "android.settings.APPLICATION_DETAILS_SETTINGS"
    };

    private SharedPreferences mDevelopmentPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mDevelopmentPreferencesListener;

    private boolean mBatteryPresent = true;
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                boolean batteryPresent = Utils.isBatteryPresent(intent);

                if (mBatteryPresent != batteryPresent) {
                    mBatteryPresent = batteryPresent;
                    updateTilesList();
                }
            }
        }
    };

    private SwitchBar mSwitchBar;

    private Button mNextButton;

    @VisibleForTesting
    boolean mDisplayHomeAsUpEnabled;

    private boolean mIsShowingDashboard;
    private boolean mIsShortcut;

    //Begin added by miaoliu for XR5429846 on 2017/11/6
    boolean mDisplaySearch;
    static final String SAVE_KEY_SHOW_SEARCH = ":settings:show_search";
    //End added by miaoliu for XR5429846 on 2017/11/6

    private ViewGroup mContent;

    private MetricsFeatureProvider mMetricsFeatureProvider;

    // Categories
    private ArrayList<DashboardCategory> mCategories = new ArrayList<>();

    private DashboardFeatureProvider mDashboardFeatureProvider;
    private ComponentName mCurrentSuggestion;

    public SwitchBar getSwitchBar() {
        return mSwitchBar;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        startPreferencePanel(caller, pref.getFragment(), pref.getExtras(), -1, pref.getTitle(),
                null, 0);
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return false;
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (name.equals(getPackageName() + "_preferences")) {
            return new SharedPreferencesLogger(this, getMetricsTag());
        }
        return super.getSharedPreferences(name, mode);
    }

    private String getMetricsTag() {
        String tag = getClass().getName();
        if (getIntent() != null && getIntent().hasExtra(EXTRA_SHOW_FRAGMENT)) {
            tag = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
        }
        if (tag.startsWith("com.android.settings.")) {
            tag = tag.replace("com.android.settings.", "");
        }
        return tag;
    }

    private static boolean isShortCutIntent(final Intent intent) {
        Set<String> categories = intent.getCategories();
        return (categories != null) && categories.contains("com.android.settings.SHORTCUT");
    }

    private static boolean isLikeShortCutIntent(final Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return false;
        }
        for (int i = 0; i < LIKE_SHORTCUT_INTENT_ACTION_ARRAY.length; i++) {
            if (LIKE_SHORTCUT_INTENT_ACTION_ARRAY[i].equals(action)) return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        
        //begin add by zhixiong.liu.hz for defect 7142662  is run monkey ,not start copy from MTK 20181210
        if (Utils.isMonkeyRunning()) {
            finish();
        }
        //end  add by zhixiong.liu.hz is run monkey ,not start copy from MTK 20181210
        long startTime = System.currentTimeMillis();

        final FeatureFactory factory = FeatureFactory.getFactory(this);

        mDashboardFeatureProvider = factory.getDashboardFeatureProvider(this);
        mMetricsFeatureProvider = factory.getMetricsFeatureProvider();

        // Should happen before any call to getIntent()
        getMetaData();

        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_UI_OPTIONS)) {
            getWindow().setUiOptions(intent.getIntExtra(EXTRA_UI_OPTIONS, 0));
        }

        mDevelopmentPreferences = getSharedPreferences(DevelopmentSettings.PREF_FILE,
                Context.MODE_PRIVATE);

        // Getting Intent properties can only be done after the super.onCreate(...)
        final String initialFragmentName = intent.getStringExtra(EXTRA_SHOW_FRAGMENT);

        mIsShortcut = isShortCutIntent(intent) || isLikeShortCutIntent(intent) ||
                intent.getBooleanExtra(EXTRA_SHOW_FRAGMENT_AS_SHORTCUT, false);

        final ComponentName cn = intent.getComponent();
        final String className = cn.getClassName();

        mIsShowingDashboard = className.equals(Settings.class.getName());

        // This is a "Sub Settings" when:
        // - this is a real SubSettings
        // - or :settings:show_fragment_as_subsetting is passed to the Intent
        final boolean isSubSettings = this instanceof SubSettings ||
                intent.getBooleanExtra(EXTRA_SHOW_FRAGMENT_AS_SUBSETTING, false);

        // If this is a sub settings, then apply the SubSettings Theme for the ActionBar content
        // insets
        if (isSubSettings) {
            setTheme(R.style.Theme_SubSettings);
        }

        setContentView(mIsShowingDashboard ?
                R.layout.settings_main_dashboard : R.layout.settings_main_prefs);

        mContent = findViewById(R.id.main_content);

        getFragmentManager().addOnBackStackChangedListener(this);

        if (savedState != null) {
            // We are restarting from a previous saved state; used that to initialize, instead
            // of starting fresh.
            setTitleFromIntent(intent);

            ArrayList<DashboardCategory> categories =
                    savedState.getParcelableArrayList(SAVE_KEY_CATEGORIES);
            if (categories != null) {
                mCategories.clear();
                mCategories.addAll(categories);
                setTitleFromBackStack();
            }

            mDisplayHomeAsUpEnabled = savedState.getBoolean(SAVE_KEY_SHOW_HOME_AS_UP);

        } else {
            launchSettingFragment(initialFragmentName, isSubSettings, intent);
        }
//Begin deleted by miaoliu for XR5429846 on 2017/11/6
//        if (mIsShowingDashboard) {
//            findViewById(R.id.search_bar).setVisibility(View.VISIBLE);
//            findViewById(R.id.action_bar).setVisibility(View.GONE);
//            Toolbar toolbar = findViewById(R.id.search_action_bar);
//            toolbar.setOnClickListener(this);
//            setActionBar(toolbar);
//
//            // Please forgive me for what I am about to do.
//            //
//            // Need to make the navigation icon non-clickable so that the entire card is clickable
//            // and goes to the search UI. Also set the background to null so there's no ripple.
//            View navView = toolbar.getNavigationView();
//            navView.setClickable(false);
//            navView.setBackground(null);
//        }
//End deleted by miaoliu for XR5429846 on 2017/11/6
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
            actionBar.setHomeButtonEnabled(mDisplayHomeAsUpEnabled);
            actionBar.setElevation(0);//Added by miaoliu for XR5237251 on 2017/10/10
        }
        mSwitchBar = findViewById(R.id.switch_bar);
        if (mSwitchBar != null) {
            mSwitchBar.setMetricsTag(getMetricsTag());
        }

        // see if we should show Back/Next buttons
        if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, false)) {

            View buttonBar = findViewById(R.id.button_bar);
            if (buttonBar != null) {
                buttonBar.setVisibility(View.VISIBLE);

                Button backButton = (Button)findViewById(R.id.back_button);
                backButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        setResult(RESULT_CANCELED, null);
                        finish();
                    }
                });
                Button skipButton = (Button)findViewById(R.id.skip_button);
                skipButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        setResult(RESULT_OK, null);
                        finish();
                    }
                });
                mNextButton = (Button)findViewById(R.id.next_button);
                mNextButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        setResult(RESULT_OK, null);
                        finish();
                    }
                });

                // set our various button parameters
                if (intent.hasExtra(EXTRA_PREFS_SET_NEXT_TEXT)) {
                    String buttonText = intent.getStringExtra(EXTRA_PREFS_SET_NEXT_TEXT);
                    if (TextUtils.isEmpty(buttonText)) {
                        mNextButton.setVisibility(View.GONE);
                    }
                    else {
                        mNextButton.setText(buttonText);
                    }
                }
                if (intent.hasExtra(EXTRA_PREFS_SET_BACK_TEXT)) {
                    String buttonText = intent.getStringExtra(EXTRA_PREFS_SET_BACK_TEXT);
                    if (TextUtils.isEmpty(buttonText)) {
                        backButton.setVisibility(View.GONE);
                    }
                    else {
                        backButton.setText(buttonText);
                    }
                }
                if (intent.getBooleanExtra(EXTRA_PREFS_SHOW_SKIP, false)) {
                    skipButton.setVisibility(View.VISIBLE);
                }
            }
        }

        if (DEBUG_TIMING) {
            Log.d(LOG_TAG, "onCreate took " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    @VisibleForTesting
    void launchSettingFragment(String initialFragmentName, boolean isSubSettings, Intent intent) {
        if (!mIsShowingDashboard && initialFragmentName != null) {
            mDisplaySearch = false;//Added by miaoliu for XR5429846 on 2017/11/6
            // UP will be shown only if it is a sub settings
            //Begin modified by miaoliu for XR7216698 on 2018/12/14
            // if (mIsShortcut) {
            //     mDisplayHomeAsUpEnabled = isSubSettings;
            // } else if (isSubSettings) {
            //     mDisplayHomeAsUpEnabled = true;
            // //Begin added by miaoliu for XR7216698 on 2018/12/08
            // } else if ("com.android.settings.wallpaper.WallpaperTypeSettings".equals(initialFragmentName)) {
            //     mDisplayHomeAsUpEnabled = true;
            // //End added by miaoliu for XR7216698 on 2018/12/14
            // } else {
            //     mDisplayHomeAsUpEnabled = false;
            // }
            if (isSubSettings || Utils.isDeviceProvisioned(this)) {
                mDisplayHomeAsUpEnabled = true;
            }else{
                mDisplayHomeAsUpEnabled = false;
            }
             //End modified by miaoliu for XR7216698 on 2018/12/08
            setTitleFromIntent(intent);

            Bundle initialArguments = intent.getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
            switchToFragment(initialFragmentName, initialArguments, true, false,
                mInitialTitleResId, mInitialTitle, false);
        } else {
            mDisplaySearch = true;//Added by miaoliu for XR5429846 on 2017/11/6
            // Show search icon as up affordance if we are displaying the main Dashboard
            mDisplayHomeAsUpEnabled = false;//Modified by miaoliu for XR5429846 on 2017/11/6
            mInitialTitleResId = R.string.dashboard_title;

            switchToFragment(DashboardSummary.class.getName(), null /* args */, false, false,
                mInitialTitleResId, mInitialTitle, false);
        }
    }

    private void setTitleFromIntent(Intent intent) {
        final int initialTitleResId = intent.getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID, -1);
        if (initialTitleResId > 0) {
            mInitialTitle = null;
            mInitialTitleResId = initialTitleResId;

            final String initialTitleResPackageName = intent.getStringExtra(
                    EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME);
            if (initialTitleResPackageName != null) {
                try {
                    Context authContext = createPackageContextAsUser(initialTitleResPackageName,
                            0 /* flags */, new UserHandle(UserHandle.myUserId()));
                    mInitialTitle = authContext.getResources().getText(mInitialTitleResId);
                    setTitle(mInitialTitle);
                    mInitialTitleResId = -1;
                    return;
                } catch (NameNotFoundException e) {
                    Log.w(LOG_TAG, "Could not find package" + initialTitleResPackageName);
                }
            } else {
                setTitle(mInitialTitleResId);
            }
        } else {
            mInitialTitleResId = -1;
            final String initialTitle = intent.getStringExtra(EXTRA_SHOW_FRAGMENT_TITLE);
            mInitialTitle = (initialTitle != null) ? initialTitle : getTitle();
            setTitle(mInitialTitle);
        }
    }

    @Override
    public void onBackStackChanged() {
        setTitleFromBackStack();
    }

    private void setTitleFromBackStack() {
        final int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            if (mInitialTitleResId > 0) {
                setTitle(mInitialTitleResId);
            } else {
                setTitle(mInitialTitle);
            }
            return;
        }

        FragmentManager.BackStackEntry bse = getFragmentManager().getBackStackEntryAt(count - 1);
        setTitleFromBackStackEntry(bse);
    }

    private void setTitleFromBackStackEntry(FragmentManager.BackStackEntry bse) {
        final CharSequence title;
        final int titleRes = bse.getBreadCrumbTitleRes();
        if (titleRes > 0) {
            title = getText(titleRes);
        } else {
            title = bse.getBreadCrumbTitle();
        }
        if (title != null) {
            setTitle(title);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    /**
     * For testing purposes to avoid crashes from final variables in Activity's onSaveInstantState.
     */
    @VisibleForTesting
    void saveState(Bundle outState) {
        if (mCategories.size() > 0) {
            outState.putParcelableArrayList(SAVE_KEY_CATEGORIES, mCategories);
        }

        outState.putBoolean(SAVE_KEY_SHOW_HOME_AS_UP, mDisplayHomeAsUpEnabled);
        outState.putBoolean(SAVE_KEY_SHOW_SEARCH, mDisplaySearch);//Added by miaoliu for XR5429846 on 2017/11/6
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mDisplayHomeAsUpEnabled = savedInstanceState.getBoolean(SAVE_KEY_SHOW_HOME_AS_UP);
        mDisplaySearch = savedInstanceState.getBoolean(SAVE_KEY_SHOW_SEARCH);//Added by miaoliu for XR5429846 on 2017/11/6
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDevelopmentPreferencesListener = (sharedPreferences, key) -> updateTilesList();
        mDevelopmentPreferences.registerOnSharedPreferenceChangeListener(
                mDevelopmentPreferencesListener);

        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        updateTilesList();
        //Begin added by shilei.zhang for XR7238432 on 2018/12/26
        getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        final InputMethodManager imm = ((InputMethodManager) getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        final View currentFocus = getCurrentFocus();
        if (imm != null && currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        //End added by shilei.zhang for XR7238432 on 2018/12/26
        //Begin added by miaoliu for XR5429846 on 2017/11/6
        if (getActionBar() != null && getActionBar().isShowing()) {
            //do nothing
        } else {
            setTitleSpaceVisibility(View.GONE);
        }
        //End added by miaoliu for XR5417203 on 2017/11/6
    }

    //Begin added by miaoliu for XR5429846 on 2017/11/6
    public void setTitleSpaceVisibility(final int visibility){
        View spacerView = (View) findViewById(R.id.title_spacer_settings);
        if(spacerView != null){
            spacerView.setVisibility(visibility);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mDisplaySearch) {
            return false;
        }
        setUpSearchMenu(menu, this);
        return true;
    }
    //End added by miaoliu for XR5417203 on 2017/11/6

    @Override
    protected void onPause() {
        super.onPause();
        mDevelopmentPreferences.unregisterOnSharedPreferenceChangeListener(
                mDevelopmentPreferencesListener);
        mDevelopmentPreferencesListener = null;
        unregisterReceiver(mBatteryInfoReceiver);
        //Begin added by shilei.zhang for XR7238432 on 2018/12/26
        getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        final InputMethodManager imm = ((InputMethodManager) getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        final View currentFocus = getCurrentFocus();
        if (imm != null && currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        //End added by shilei.zhang for XR7238432 on 2018/12/26
    }
    //Begin deleted by miaoliu for XR7238252 on 2019/1/3
    // @Override
    // public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    //     final Bitmap icon = getBitmapFromXmlResource(R.mipmap.ic_launcher_settings);//Modified by miaoliu for XRP10028336 on 2018/12/24
    //     taskDescription.setIcon(icon);
    //     super.setTaskDescription(taskDescription);
    // }
    //End deleted by miaoliu for XR7238252 on 2019/1/3
    protected boolean isValidFragment(String fragmentName) {
        // Almost all fragments are wrapped in this,
        // except for a few that have their own activities.
        for (int i = 0; i < SettingsGateway.ENTRY_FRAGMENTS.length; i++) {
            if (SettingsGateway.ENTRY_FRAGMENTS[i].equals(fragmentName)) return true;
        }
        return false;
    }

    @Override
    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        // This is called from super.onCreate, isMultiPane() is not yet reliable
        // Do not use onIsHidingHeaders either, which relies itself on this method
        if (startingFragment != null) {
            Intent modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            Bundle args = superIntent.getExtras();
            if (args != null) {
                args = new Bundle(args);
            } else {
                args = new Bundle();
            }
            args.putParcelable("intent", superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
            return modIntent;
        }
        return superIntent;
    }

    /**
     * Checks if the component name in the intent is different from the Settings class and
     * returns the class name to load as a fragment.
     */
    private String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null) return mFragmentClass;

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) return null;

        if ("com.android.settings.ManageApplications".equals(intentClass)
                || "com.android.settings.RunningServices".equals(intentClass)
                || "com.android.settings.applications.StorageUse".equals(intentClass)) {
            // Old names of manage apps.
            intentClass = com.android.settings.applications.ManageApplications.class.getName();
        }

        return intentClass;
    }

    /**
     * Start a new fragment containing a preference panel.  If the preferences
     * are being displayed in multi-pane mode, the given fragment class will
     * be instantiated and placed in the appropriate pane.  If running in
     * single-pane mode, a new activity will be launched in which to show the
     * fragment.
     *
     * @param fragmentClass Full name of the class implementing the fragment.
     * @param args Any desired arguments to supply to the fragment.
     * @param titleRes Optional resource identifier of the title of this
     * fragment.
     * @param titleText Optional text of the title of this fragment.
     * @param resultTo Optional fragment that result data should be sent to.
     * If non-null, resultTo.onActivityResult() will be called when this
     * preference panel is done.  The launched panel must use
     * {@link #finishPreferencePanel(Fragment, int, Intent)} when done.
     * @param resultRequestCode If resultTo is non-null, this is the caller's
     * request code to be received with the result.
     */
    public void startPreferencePanel(Fragment caller, String fragmentClass, Bundle args,
            int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        String title = null;
        if (titleRes < 0) {
            if (titleText != null) {
                title = titleText.toString();
            } else {
                // There not much we can do in that case
                title = "";
            }
        }
        Utils.startWithFragment(this, fragmentClass, args, resultTo, resultRequestCode,
                titleRes, title, mIsShortcut, mMetricsFeatureProvider.getMetricsCategory(caller));
    }

    /**
     * Start a new fragment in a new activity containing a preference panel for a given user. If the
     * preferences are being displayed in multi-pane mode, the given fragment class will be
     * instantiated and placed in the appropriate pane. If running in single-pane mode, a new
     * activity will be launched in which to show the fragment.
     *
     * @param fragmentClass Full name of the class implementing the fragment.
     * @param args Any desired arguments to supply to the fragment.
     * @param titleRes Optional resource identifier of the title of this fragment.
     * @param titleText Optional text of the title of this fragment.
     * @param userHandle The user for which the panel has to be started.
     */
    public void startPreferencePanelAsUser(Fragment caller, String fragmentClass,
            Bundle args, int titleRes, CharSequence titleText, UserHandle userHandle) {
        // This is a workaround.
        //
        // Calling startWithFragmentAsUser() without specifying FLAG_ACTIVITY_NEW_TASK to the intent
        // starting the fragment could cause a native stack corruption. See b/17523189. However,
        // adding that flag and start the preference panel with the same UserHandler will make it
        // impossible to use back button to return to the previous screen. See b/20042570.
        //
        // We work around this issue by adding FLAG_ACTIVITY_NEW_TASK to the intent, while doing
        // another check here to call startPreferencePanel() instead of startWithFragmentAsUser()
        // when we're calling it as the same user.
        if (userHandle.getIdentifier() == UserHandle.myUserId()) {
            startPreferencePanel(caller, fragmentClass, args, titleRes, titleText, null, 0);
        } else {
            String title = null;
            if (titleRes < 0) {
                if (titleText != null) {
                    title = titleText.toString();
                } else {
                    // There not much we can do in that case
                    title = "";
                }
            }
            Utils.startWithFragmentAsUser(this, fragmentClass, args, titleRes, title,
                    mIsShortcut, mMetricsFeatureProvider.getMetricsCategory(caller), userHandle);
        }
    }

    /**
     * Called by a preference panel fragment to finish itself.
     *
     * @param caller The fragment that is asking to be finished.
     * @param resultCode Optional result code to send back to the original
     * launching fragment.
     * @param resultData Optional result data to send back to the original
     * launching fragment.
     */
    public void finishPreferencePanel(Fragment caller, int resultCode, Intent resultData) {
        setResult(resultCode, resultData);
        finish();
    }

    /**
     * Start a new fragment.
     *
     * @param fragment The fragment to start
     * @param push If true, the current fragment will be pushed onto the back stack.  If false,
     * the current fragment will be replaced.
     */
    public void startPreferenceFragment(Fragment fragment, boolean push) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, fragment);
        if (push) {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(BACK_STACK_PREFS);
        } else {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * Switch to a specific Fragment with taking care of validation, Title and BackStack
     */
    private Fragment switchToFragment(String fragmentName, Bundle args, boolean validate,
            boolean addToBackStack, int titleResId, CharSequence title, boolean withTransition) {
        if (validate && !isValidFragment(fragmentName)) {
            throw new IllegalArgumentException("Invalid fragment for this activity: "
                    + fragmentName);
        }
        Fragment f = Fragment.instantiate(this, fragmentName, args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, f);
        if (withTransition) {
            TransitionManager.beginDelayedTransition(mContent);
        }
        if (addToBackStack) {
            transaction.addToBackStack(SettingsActivity.BACK_STACK_PREFS);
        }
        if (titleResId > 0) {
            transaction.setBreadCrumbTitle(titleResId);
        } else if (title != null) {
            transaction.setBreadCrumbTitle(title);
        }
        transaction.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
        return f;
    }

    private void updateTilesList() {
        // Generally the items that are will be changing from these updates will
        // not be in the top list of tiles, so run it in the background and the
        // SettingsDrawerActivity will pick up on the updates automatically.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                doUpdateTilesList();
            }
        });
    }

    private void doUpdateTilesList() {
        PackageManager pm = getPackageManager();
        final UserManager um = UserManager.get(this);
        final boolean isAdmin = um.isAdminUser();
        boolean somethingChanged = false;
        String packageName = getPackageName();
        somethingChanged = setTileEnabled(
                new ComponentName(packageName, WifiSettingsActivity.class.getName()),
                pm.hasSystemFeature(PackageManager.FEATURE_WIFI), isAdmin) || somethingChanged;

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.BluetoothSettingsActivity.class.getName()),
                pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH), isAdmin)
                || somethingChanged;

        boolean isDataPlanFeatureEnabled = FeatureFactory.getFactory(this)
                .getDataPlanFeatureProvider()
                .isEnabled();

        // When the data plan feature flag is turned on we disable DataUsageSummaryActivity
        // and enable DataPlanUsageSummaryActivity. When the feature flag is turned off we do the
        // reverse.

        // Disable DataUsageSummaryActivity if the data plan feature flag is turned on otherwise
        // disable DataPlanUsageSummaryActivity.
        somethingChanged = setTileEnabled(
                new ComponentName(packageName,
                        isDataPlanFeatureEnabled
                                ? Settings.DataUsageSummaryActivity.class.getName()
                                : Settings.DataPlanUsageSummaryActivity.class.getName()),
                false /* enabled */,
                isAdmin) || somethingChanged;

        // Enable DataUsageSummaryActivity if the data plan feature flag is turned on otherwise
        // enable DataPlanUsageSummaryActivity.
        somethingChanged = setTileEnabled(
                new ComponentName(packageName,
                        isDataPlanFeatureEnabled
                                ? Settings.DataPlanUsageSummaryActivity.class.getName()
                                : Settings.DataUsageSummaryActivity.class.getName()),
                Utils.isBandwidthControlEnabled() /* enabled */,
                isAdmin) || somethingChanged;

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.SimSettingsActivity.class.getName()),
                Utils.showSimCardTile(this), isAdmin)
                || somethingChanged;

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.PowerUsageSummaryActivity.class.getName()),
                mBatteryPresent, isAdmin) || somethingChanged;

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.UserSettingsActivity.class.getName()),
                UserHandle.MU_ENABLED && UserManager.supportsMultipleUsers()
                        && !Utils.isMonkeyRunning(), isAdmin)
                || somethingChanged;

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.NetworkDashboardActivity.class.getName()),
                !UserManager.isDeviceInDemoMode(this), isAdmin)
                || somethingChanged;

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.ConnectedDeviceDashboardActivity.class.getName()),
                !UserManager.isDeviceInDemoMode(this), isAdmin)
                || somethingChanged;

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.DateTimeSettingsActivity.class.getName()),
                !UserManager.isDeviceInDemoMode(this), isAdmin)
                || somethingChanged;
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        // BEGIN XR#5440260 Modified by binjian.tu on 2017/11/27
        somethingChanged = setTileEnabled(new ComponentName(packageName,
                Settings.NfcSettingsActivity.class.getName()), adapter != null, isAdmin)
                || somethingChanged;
                
        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.PaymentSettingsActivity.class.getName()),
                pm.hasSystemFeature(PackageManager.FEATURE_NFC)
                && pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)
                && adapter != null && adapter.isEnabled(), isAdmin)
                || somethingChanged;
        // END XR#5440260 Modified by binjian.tu on 2017/11/27

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.PrintSettingsActivity.class.getName()),
                pm.hasSystemFeature(PackageManager.FEATURE_PRINTING), isAdmin)
                || somethingChanged;

        final boolean showDev = mDevelopmentPreferences.getBoolean(
                DevelopmentSettings.PREF_SHOW, android.os.Build.TYPE.equals("eng"))
                && !um.hasUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES);
        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.DevelopmentSettingsActivity.class.getName()),
                showDev, isAdmin)
                || somethingChanged;

        // Enable/disable backup settings depending on whether the user is admin.
        somethingChanged = setTileEnabled(new ComponentName(packageName,
                BackupSettingsActivity.class.getName()), true, isAdmin)
                || somethingChanged;

        somethingChanged = setTileEnabled(new ComponentName(packageName,
                        Settings.WifiDisplaySettingsActivity.class.getName()),
                WifiDisplaySettings.isAvailable(this), isAdmin)
                || somethingChanged;
        //Begin deleted by miaoliu for XRP23480 on 2018/9/3
        //Begin added by miaoliu for XR23119 on 2018/8/22
        // boolean isSmartManagerInstalled = Utils.checkPackageExist(this, "com.tct.onetouchbooster");
        //  somethingChanged = setTileEnabled(new ComponentName(packageName,
        //         Settings.StorageDashboardActivity.class.getName()), !isSmartManagerInstalled, isAdmin)
        //         || somethingChanged;
        //  somethingChanged = setTileEnabled(new ComponentName(packageName,
        //         Settings.PowerUsageSummaryActivity.class.getName()), !isSmartManagerInstalled, isAdmin)
        //         || somethingChanged;  
        // somethingChanged = setTileEnabled(new ComponentName(packageName,
        //         Settings.ConfigureNotificationSettingsActivity.class.getName()), !isSmartManagerInstalled, isAdmin)
        //         || somethingChanged;
        //End added by miaoliu for XR23119 on 2018/8/22
        //End deleted by miaoliu for XRP23480 on 2018/9/3
        if (UserHandle.MU_ENABLED && !isAdmin) {

            // When on restricted users, disable all extra categories (but only the settings ones).
            final List<DashboardCategory> categories = mDashboardFeatureProvider.getAllCategories();
            synchronized (categories) {
                for (DashboardCategory category : categories) {
                    final int tileCount = category.getTilesCount();
                    for (int i = 0; i < tileCount; i++) {
                        final ComponentName component = category.getTile(i).intent.getComponent();

                        final String name = component.getClassName();
                        final boolean isEnabledForRestricted = ArrayUtils.contains(
                                SettingsGateway.SETTINGS_FOR_RESTRICTED, name);
                        if (packageName.equals(component.getPackageName())
                                && !isEnabledForRestricted) {
                            somethingChanged = setTileEnabled(component, false, isAdmin)
                                    || somethingChanged;
                        }
                    }
                }
            }
        }

        // Final step, refresh categories.
        if (somethingChanged) {
            Log.d(LOG_TAG, "Enabled state changed for some tiles, reloading all categories");
            updateCategories();
        } else {
            Log.d(LOG_TAG, "No enabled state changed, skipping updateCategory call");
        }
    }

    /**
     * @return whether or not the enabled state actually changed.
     */
    private boolean setTileEnabled(ComponentName component, boolean enabled, boolean isAdmin) {
        if (UserHandle.MU_ENABLED && !isAdmin && getPackageName().equals(component.getPackageName())
                && !ArrayUtils.contains(SettingsGateway.SETTINGS_FOR_RESTRICTED,
                component.getClassName())) {
            enabled = false;
        }
        return setTileEnabled(component, enabled);
    }

    private void getMetaData() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(),
                    PackageManager.GET_META_DATA);
            if (ai == null || ai.metaData == null) return;
            mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
        } catch (NameNotFoundException nnfe) {
            // No recovery
            Log.d(LOG_TAG, "Cannot get Metadata for: " + getComponentName().toString());
        }
    }

    // give subclasses access to the Next button
    public boolean hasNextButton() {
        return mNextButton != null;
    }

    public Button getNextButton() {
        return mNextButton;
    }

    @Override
    public boolean shouldUpRecreateTask(Intent targetIntent) {
        return super.shouldUpRecreateTask(new Intent(this, SettingsActivity.class));
    }

    public void startSuggestion(Intent intent) {
        if (intent == null || ActivityManager.isUserAMonkey()) {
            return;
        }
        mCurrentSuggestion = intent.getComponent();
        startActivityForResult(intent, REQUEST_SUGGESTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SUGGESTION && mCurrentSuggestion != null
                && resultCode != RESULT_CANCELED) {
            getPackageManager().setComponentEnabledSetting(mCurrentSuggestion,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @VisibleForTesting
    Bitmap getBitmapFromXmlResource(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes, getTheme());
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    //Begin added by miaoliu for XR5429846 on 2017/11/6
    public void setUpSearchMenu(Menu menu, final Activity activity) {
        if (menu == null || activity == null) {
            return;
        }
        String menuTitle = getString(R.string.search_menu);
        MenuItem menuItem = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, menuTitle)
                .setIcon(R.drawable.ic_search_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(activity, SearchActivity.class);
                        activity.startActivity(intent);
                        return true;
                    }
                });

        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
    //End added by miaoliu for XR5429846 on 2017/11/6
}