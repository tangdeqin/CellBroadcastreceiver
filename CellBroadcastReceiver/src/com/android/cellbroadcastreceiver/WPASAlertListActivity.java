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
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/* 08/25/2014|      fujun.yang      |        755265        |SMSCB behavior in */
/*           |                      |                      |Android phones    */
/* ----------|----------------------|----------------------|----------------- */
/* 02/03/2015|       qiang.li       |        889602        |Click CB message in*/
/*           |                      |                      | notification bar  */
/*           |                      |                      |have no any effect */
/* ----------|----------------------|----------------------|----------------- */
/* 02/10/2015|       qiang.li       |        892380        |[SCB]Click delete */
/*           |                      |                      |icon will cancel the*/
/*           |                      |                      |marked CB message */
/*           |                      |                      |after rotate device*/
/* ----------|----------------------|----------------------|----------------- */
/* 02/23/2015|      fujun.yang      |        932845        |Unread CB message */
/*           |                      |                      |number display    */
/*           |                      |                      |error in          */
/*           |                      |                      |notification bar  */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;


import android.app.Activity;
/* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-01,BUG-2344436*/
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.ActivityManager.RunningTaskInfo;
/* MODIFIED-END by yuxuan.zhang,BUG-2344436*/
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration; // MODIFIED by yuxuan.zhang, 2016-07-11,BUG-1112693
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
//[FEATURE]-Add-BEGIN by TSCD.fujun.yang,08/25/2014,755265,SMSCB behavior in Android phones
import android.os.SystemProperties;
import com.android.cb.util.TLog;
//[FEATURE]-Add-END by TSCD.fujun.yang
import android.provider.Telephony;
import android.telephony.CellBroadcastMessage;
import android.util.Log; // MODIFIED by yuxuan.zhang, 2016-07-01,BUG-2344436
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
//[BUGFIX]-Mod-BEGIN by AMNJ.Bin.Huang, 2014/11/17, PR-842834
import java.util.Collections;
import java.util.Comparator;
//[BUGFIX]-Mod-END by AMNJ.Bin.Huang, 2014/11/17, PR-842834

import com.android.cellbroadcastreceiver.R;
import com.tct.wrapper.TctWrapperManager; // MODIFIED by yuxuan.zhang, 2016-09-27,BUG-2845457

/**
 * This activity provides a list view of received cell broadcasts. Most of the work is handled
 * in the inner CursorLoaderListFragment class.
 */
public class WPASAlertListActivity extends Activity {

    private final String TAG = "CellBroadcastListActivity"; // MODIFIED by yuxuan.zhang, 2016-07-04,BUG-2344436
    //[BUGFIX]-add begin-by TCTNB.yugang.jia,12/25/2013,PR-578732
    public static HashSet<Long> messageCheckedStatus = new HashSet<Long>();
    //[BUGFIX]-Add end-by TCTNB.yugang.jia,12/25/2013,PR-578732
    //[BUGFIX]-Add-BEGIN-by TSCD.tianming.lei,02/10/2015,PR-892380
    public static ArrayList<Long> selectedMsgIds = null;
    //[BUGFIX]-Add-END-by TSCD.tianming.lei
    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-09-08,BUG-2845457*/
    public static final String[] QUERY_COLUMNS = {
        Telephony.CellBroadcasts._ID,
        Telephony.CellBroadcasts.GEOGRAPHICAL_SCOPE,
        Telephony.CellBroadcasts.PLMN,
        Telephony.CellBroadcasts.LAC,
        Telephony.CellBroadcasts.CID,
        Telephony.CellBroadcasts.SERIAL_NUMBER,
        Telephony.CellBroadcasts.SERVICE_CATEGORY,
        Telephony.CellBroadcasts.LANGUAGE_CODE,
        Telephony.CellBroadcasts.MESSAGE_BODY,
        Telephony.CellBroadcasts.DELIVERY_TIME,
        Telephony.CellBroadcasts.MESSAGE_READ,
        Telephony.CellBroadcasts.MESSAGE_FORMAT,
        Telephony.CellBroadcasts.MESSAGE_PRIORITY,
        Telephony.CellBroadcasts.ETWS_WARNING_TYPE,
        Telephony.CellBroadcasts.CMAS_MESSAGE_CLASS,
        Telephony.CellBroadcasts.CMAS_CATEGORY,
        Telephony.CellBroadcasts.CMAS_RESPONSE_TYPE,
        Telephony.CellBroadcasts.CMAS_SEVERITY,
        Telephony.CellBroadcasts.CMAS_URGENCY,
        Telephony.CellBroadcasts.CMAS_CERTAINTY,
};
/* MODIFIED-END by yuxuan.zhang,BUG-2845457*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate"); // MODIFIED by yuxuan.zhang, 2016-07-04,BUG-2344436
        super.onCreate(savedInstanceState);

       //[BUGFIX]-delete begin-by TCTNB.yugang.jia,12/25/2013,PR-578732
        // Dismiss the notification that brought us here (if any).
        //((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
       //         .cancel(CellBroadcastAlertService.NOTIFICATION_ID);
       //[BUGFIX]-delete END-by TCTNB.yugang.jia,12/25/2013,PR-578732
        //[BUGFIX]-Add-BEGIN by TCTNB.yugang.jia,11/27/2013,561441,
        //[CB] CB number display issue
        CellBroadcastReceiverApp.clearNewMessageList();
        //[BUGFIX]-Add-END by TCTNB.yugang.jia,11/27/2013,561441,
        FragmentManager fm = getFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            CursorLoaderListFragment listFragment = new CursorLoaderListFragment();
            fm.beginTransaction().add(android.R.id.content, listFragment).commit();
        }
        /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-04,BUG-2344436*/
        cancelNotification();
    }

    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-11,BUG-1112693*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }
    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
    private void cancelNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CellBroadcastAlertService.NOTIFICATION_ID);
        notificationManager.cancel(CellBroadcastAlertService.EMERGENCY_NOTIFICATION_ID);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        stopService(new Intent(this, CellBroadcastAlertAudio.class)); // MODIFIED by yuxuan.zhang, 2016-07-11,BUG-1112693
        cancelNotification();
        super.onNewIntent(intent);
    }
    /**
     * List fragment queries SQLite database on worker thread.
     */
    public static class CursorLoaderListFragment extends ListFragment
            implements LoaderManager.LoaderCallbacks<Cursor> {

        private final String TAG = "CursorLoaderListFragment";
        /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/
        // IDs of the main menu items.
        private static final int MENU_DELETE_ALL           = 3;
        private static final int MENU_PREFERENCES          = 4;//[FEATTURE]-MOD- by TCTNB.Yugang.Jia-516039 // MODIFIED by yuxuan.zhang, 2016-05-06,BUG-1112693

        // IDs of the context menu items (package local, accessed from inner DeleteThreadListener).
        static final int MENU_DELETE               = 0;
        static final int MENU_VIEW_DETAILS         = 1;

        // This is the Adapter being used to display the list's data.
        CursorAdapter mAdapter;
        MenuItem deleteAllItem = null;//[BUGFIX]-ADD by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
        //[BUGFIX]-Add begin-by TCTNB.yugang.jia,12/25/2013,PR-578732
        private ImageButton deleteButton;
        CellBroadcastListItem cellitem;
        //[BUGFIX]-Add end-by TCTNB.yugang.jia,12/25/2013,PR-578732

        //[FEATURE]-Mod-Begin by TSNJ.wei.li,10/29/2014, FR-728852  Presidential msg are pinned to the top
        private static String mOrderBy = "service_category ASC,date DESC";
        //[FEATURE]-Mod-End by TSNJ.wei.li,10/29/2014,
        
        private static final String WHERE_CLAUSE = "service_category >= 4370 AND service_category <= 4399";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // We have a menu item to show in action bar.
            // modify by liang.zhang for Defect 6049118 at 2018-03-02 begin
            setHasOptionsMenu(false);
            // modify by liang.zhang for Defect 6049118 at 2018-03-02 end
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.wpas_cell_broadcast_list_screen, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Set context menu for long-press.
            ListView listView = getListView();
            //[BUGFIX]-mod begin-by TCTNB.yugang.jia,12/25/2013,PR-578732
            //listView.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new CellBroadcastMultiChoiceModeListener());
            //[BUGFIX]-mod end-by TCTNB.yugang.jia,12/25/2013,PR-578732

            // Create a cursor adapter to display the loaded data.
            mAdapter = new CellBroadcastCursorAdapter(getActivity(), null);
            setListAdapter(mAdapter);
            deleteButton = (ImageButton)getView().findViewById(R.id.delete_button); //[BUGFIX]-Add by TCTNB.yugang.jia,12/25/2013,PR-578732
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
            /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-01,BUG-2344436*/
            Log.i(TAG, "onCreate fragment");
            getActivity().sendBroadcast(new Intent(CellBroadcastAlertDialog.DIALOG_DISMISS_ACTION));
            /* MODIFIED-END by yuxuan.zhang,BUG-2344436*/
        }

        @Override
        public void onResume() {
            Log.i(TAG, "onResume fragment");
            super.onResume();
        }
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.add(0, MENU_DELETE_ALL, 0, R.string.menu_delete_all).setIcon(
                    android.R.drawable.ic_menu_delete);
            int persoRule = getActivity().getResources().getInteger(R.integer.def_telephony_CBMessage_Filter);
          //[FEATTURE]-MOD-BEGIN by TCTNB.Yugang.Jia,09/16/2013,FR-516039
            if (persoRule == 1 || persoRule == 3) {
                menu.add(0, MENU_PREFERENCES, 0, R.string.menu_preferences).setIcon(
                        android.R.drawable.ic_menu_preferences);
            }
          //[FEATTURE]-MOD-END by TCTNB.Yugang.Jia,09/16/2013,FR-516039
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
          //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
            deleteAllItem = menu.findItem(MENU_DELETE_ALL);
            deleteAllItem.setVisible(!mAdapter.isEmpty());
          //[BUGFIX]-MOD-END by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            CellBroadcastListItem cbli = (CellBroadcastListItem) v;
            showDialogAndMarkRead(cbli.getMessage());
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // [BUGFIX]-Mod-BEGIN by AMNJ.Bin.Huang, 2014/11/17, PR-842834
            return new SortedCursorLoader(getActivity(), CellBroadcastContentProvider.CONTENT_URI,
            // return new CursorLoader(getActivity(), CellBroadcastContentProvider.CONTENT_URI,
            //[BUGFIX]-Mod-END by AMNJ.Bin.Huang, 2014/11/17, PR-842834
                    Telephony.CellBroadcasts.QUERY_COLUMNS, WHERE_CLAUSE, null, // MODIFIED by yuxuan.zhang, 2016-09-08,BUG-2845457
                    //[FEATURE]-Mod-Begin by TSNJ.wei.li,10/29/2014, FR-728852 Presidential msg are pinned to the top
                    mOrderBy);
                    //Telephony.CellBroadcasts.DELIVERY_TIME + " DESC");
                    //[FEATURE]-Mod-End by TSNJ.wei.li,10/29/2014,
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data);
          //[BUGFIX]-MOD-BEGIN by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
            if (deleteAllItem != null) {
                deleteAllItem.setVisible(!mAdapter.isEmpty());
            }
          //[BUGFIX]-MOD-END by TCTNB.yugang.jia, 09/11/2013, FR-516039, CMAS Ergo
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            mAdapter.swapCursor(null);
        }

        private void showDialogAndMarkRead(CellBroadcastMessage cbm) {
            // show emergency alerts with the warning icon, but don't play alert tone
            Intent i = new Intent(getActivity(), CellBroadcastAlertDialog.class);
            ArrayList<CellBroadcastMessage> messageList = new ArrayList<CellBroadcastMessage>(1);
            messageList.add(cbm);
            i.putParcelableArrayListExtra(CellBroadcastMessage.SMS_CB_MESSAGE_EXTRA, messageList);
            startActivity(i);
        }

        private void showBroadcastDetails(CellBroadcastMessage cbm) {
            // show dialog with delivery date/time and alert details
            CharSequence details = CellBroadcastResources.getMessageDetails(getActivity(), cbm);
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.view_details_title)
                    .setMessage(details)
                    .setCancelable(true)
                    .show();
        }

        private final OnCreateContextMenuListener mOnCreateContextMenuListener =
                new OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v,
                            ContextMenuInfo menuInfo) {
                        menu.setHeaderTitle(R.string.message_options);
                        menu.add(0, MENU_VIEW_DETAILS, 0, R.string.menu_view_details);
                        menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
                    }
                };

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            Cursor cursor = mAdapter.getCursor();
            if (cursor != null && cursor.getPosition() >= 0) {
                switch (item.getItemId()) {
                    case MENU_DELETE:
                        confirmDeleteThread(cursor.getLong(cursor.getColumnIndexOrThrow(
                                Telephony.CellBroadcasts._ID)));
                        break;

                    case MENU_VIEW_DETAILS:
                        showBroadcastDetails(CellBroadcastMessage.createFromCursor(cursor));
                        break;

                    default:
                        break;
                }
            }
            return super.onContextItemSelected(item);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch(item.getItemId()) {
                case MENU_DELETE_ALL:
                    confirmDeleteThread(-1);
                    break;
                  //[FEATTURE]-MOD-BEGIN by TCTNB.Yugang.Jia,09/16/2013,FR-516039
                /* MODIFIED-BEGIN by yuxuan.zhang, 2016-05-06,BUG-1112693*/
                case MENU_PREFERENCES:
                    Intent intent = new Intent(getActivity(), CellBroadcastSettings.class);
                    startActivity(intent);
                    break;
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    //[FEATTURE]-MOD-END by TCTNB.Yugang.Jia
                default:
                    return true;
            }
            return false;
        }

        /**
         * Start the process of putting up a dialog to confirm deleting a broadcast.
         * @param rowId the row ID of the broadcast to delete, or -1 to delete all broadcasts
         */
        public void confirmDeleteThread(long rowId) {
            DeleteThreadListener listener = new DeleteThreadListener(rowId);
            confirmDeleteThreadDialog(listener, (rowId == -1), getActivity());
        }

        /**
         * Build and show the proper delete broadcast dialog. The UI is slightly different
         * depending on whether there are locked messages in the thread(s) and whether we're
         * deleting a single broadcast or all broadcasts.
         * @param listener gets called when the delete button is pressed
         * @param deleteAll whether to show a single thread or all threads UI
         * @param context used to load the various UI elements
         */
        public static void confirmDeleteThreadDialog(DeleteThreadListener listener,
                boolean deleteAll, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
          //[FEATTURE]-MOD-BEGIN by TCTNB.Yugang.Jia,09/16/2013,FR-516039
            builder.setTitle(deleteAll ? R.string.confirm_dialog_title_deleteall
                            : R.string.confirm_dialog_title_delete)
                    .setCancelable(true)
                    .setPositiveButton(R.string.button_delete, listener)
                    .setNegativeButton(R.string.button_cancel, null)
                    .setMessage(deleteAll ? R.string.confirm_delete_all_broadcasts
                            : R.string.confirm_delete_broadcast)
                    .show();
          //[FEATTURE]-MOD-END by TCTNB.Yugang.Jia,09/16/2013,FR-516039
        }

        public class DeleteThreadListener implements OnClickListener {
            private final long mRowId;

            public DeleteThreadListener(long rowId) {
                mRowId = rowId;
            }

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // delete from database on a background thread
                new CellBroadcastContentProvider.AsyncCellBroadcastTask(
                        getActivity().getContentResolver()).execute(
                        new CellBroadcastContentProvider.CellBroadcastOperation() {
                            @Override
                            public boolean execute(CellBroadcastContentProvider provider) {
                                if (mRowId != -1) {
                                    return provider.deleteBroadcast(mRowId);
                                } else {
                                    return provider.deleteAllBroadcasts();
                                }
                            }
                        });
                //[BUGFIX]-Add BEGIN-by TSCD qiang.li,02/03/2015,PR-889602
                // refer to bug#629643 to resolve this issue
                NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(CellBroadcastAlertService.NOTIFICATION_ID);
                //[BUGFIX]-Add END-by TSCD qiang.li

              //[BUGFIX]-Add-BEGIN by TSCD.fujun.yang,02/23/2015,932845,Unread CB message number display error in notification bar
                if(mRowId == -1){
                    CellBroadcastReceiverApp.clearNewMessageList();
                }
              //[BUGFIX]-Add-END by TSCD.fujun.yang
                dialog.dismiss();
            }
        }

      //[BUGFIX]-Mod-BEGIN by AMNJ.Bin.Huang, 2014/11/17, PR-842834
        static class SortedCursorLoader extends CursorLoader {

            public SortedCursorLoader(Context context, Uri uri, String[] projection,
                    String selection, String[] selectionArgs, String sortOrder) {
                super(context, uri, projection, selection, selectionArgs, sortOrder);
            }

            @Override
            public Cursor loadInBackground() {
                return new SortedCursorWrapper(super.loadInBackground());
            }

            class SortedCursorWrapper extends CursorWrapper {
                private int mPos = 0;
                ArrayList<SortEntry> mEntries = new ArrayList<SortEntry>(15);

                public SortedCursorWrapper(Cursor cursor) {
                    super(cursor);

                    if (cursor != null && cursor.getCount() != 0) {
                        int i = 0;
                        while (cursor.moveToNext()) {
                            SortEntry mEntry = new SortEntry();
                            mEntry.order = i;
                            mEntry.mRead = cursor.getInt(cursor
                                    .getColumnIndex(Telephony.CellBroadcasts.MESSAGE_READ));
                            if (!cursor.isNull(cursor
                                    .getColumnIndex(Telephony.CellBroadcasts.CMAS_MESSAGE_CLASS))) {
                                mEntry.isPresidential = cursor
                                       .getInt(cursor.getColumnIndex(Telephony.CellBroadcasts.CMAS_MESSAGE_CLASS)) == 0;
                            }

                            mEntry.date = cursor.getInt(cursor
                                    .getColumnIndex(Telephony.CellBroadcasts.DELIVERY_TIME));
                            mEntries.add(mEntry);
                            i++;
                        }

                        Collections.sort(mEntries, new SortComparator());
                    }
                }

                @Override
                public boolean move(int offset) {
                    return moveToPosition(mPos + offset);
                }
                @Override
                public boolean moveToFirst() {
                    return moveToPosition(0);
                }

                @Override
                public boolean moveToLast() {
                    return moveToPosition(getCount() - 1);
                }

                @Override
                public boolean moveToPosition(int position) {
                    if (position >= 0 && position < mEntries.size()) {
                        mPos = position;
                        int realOrder = mEntries.get(position).order;
                        return mCursor.moveToPosition(realOrder);
                    } else if (position < 0) {
                        mPos = -1;
                    } else {
                        mPos = mEntries.size();
                    }
                    return mCursor.moveToPosition(mPos);
                }

                @Override
                public boolean moveToNext() {
                    return moveToPosition(mPos + 1);
                }

                @Override
                public int getPosition() {
                    return mPos;
                }

                @Override
                public boolean moveToPrevious() {
                    return moveToPosition(mPos - 1);
                }
            }

            class SortComparator implements Comparator {

                @Override
                public int compare(Object lhs, Object rhs) {
                    int ret = 0;
                    SortEntry mLeftEntry = (SortEntry) lhs;
                    SortEntry mRightEntry = (SortEntry) rhs;
                    //Log.d("CMAS", "left: " + mLeftEntry);
                    //Log.d("CMAS", "right: " + mRightEntry);
                    if (mLeftEntry.isPresidential && !mRightEntry.isPresidential) {
                        ret = -1;
                        //Log.d("CMAS", "<isPresidential> is different");
                    } else if (!mLeftEntry.isPresidential && mRightEntry.isPresidential) {
                        ret = 1;
                        //Log.d("CMAS", "<isPresidential> is different");
                    } else {
                        // both are Presidential
                        // compare the read flag first, 0: unread, 1:read
                        if (mLeftEntry.mRead < mRightEntry.mRead) {
                            //Log.d("CMAS", "<mRead> is different");
                            ret = -1;
                        } else if (mLeftEntry.mRead > mRightEntry.mRead) {
                            ret = 1;
                            //Log.d("CMAS", "<mRead> is different");
                        } else {
                            // read is same, compare the time, bigger is later
                            if (mLeftEntry.date == mRightEntry.date) {
                                ret = 0;
                                //Log.d("CMAS", "<all are same>");
                            } else {
                                //Log.d("CMAS", "<date> is different");
                                ret = (mLeftEntry.date > mRightEntry.date ? -1 : 1);
                            }
                        }
                    }
                    //Log.d("CMAS", "compare result: " + ret);
                    return ret;
                }

            }
            class SortEntry {
                int mRead;
                boolean isPresidential;
                int date;
                int order;

                public String toString() {
                    return "read: " + mRead
                            + " isPresidential: " + (isPresidential ? "true" : "false")
                            + " date: " + date
                            + " order: " + order;
                }
            }
        }
      //[BUGFIX]-Mod-END by AMNJ.Bin.Huang, 2014/11/17, PR-842834
      //[BUGFIX]-Add-BEGIN by TCTNB.yugang.jia, 12/25/2013, PR-578732
        class CellBroadcastMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

            private View actionBarView = null;
            private TextView selectedMsgCount = null;
            //private ArrayList<Long> selectedMsgIds = null;//[BUGFIX]-Delete-by TSCD.tianming.lei,02/10/2015,PR-892380
            ActionMode mode;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    if(null ==  selectedMsgIds){
                        selectedMsgIds = new ArrayList<Long>();
                    }

                    this.mode = mode;
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(deleteListener);

                    if (actionBarView == null) {
                        actionBarView = LayoutInflater.from(getActivity()).inflate(
                                R.layout.cellbroadcast_message_list_multi_select_actionbar, null);
                    }

                    selectedMsgCount = (TextView) (actionBarView.findViewById(R.id.selected_msg_count));
                    TextView actiontext =(TextView)actionBarView.findViewById(R.id.action_bar_title);
                    actiontext.setText(R.string.action_bar_title_choose_messages);

                    mode.setCustomView(actionBarView);

                    return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
            View.OnClickListener deleteListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    int size = selectedMsgIds.size();
                    if (size > 0) {
                        String message;
                        if (size == 1) {
                            message = getActivity().getString(R.string.one_selected_message_to_be_deleted);
                        } else {
                            message = getActivity().getString(R.string.multi_selected_message_to_be_deleted, size);
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.confirm_dialog_title)
                                .setIconAttribute(android.R.attr.alertDialogIcon)
                                .setCancelable(true)
                                .setPositiveButton(
                                        R.string.button_delete,
                                        new DeleteMessageListener(selectedMsgIds
                                                .toArray(new Long[0])))
                                .setNegativeButton(R.string.button_cancel, null)
                                .setMessage(message)
                                .show();
                    }
                    mode.finish();
                }
            };
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                  // TODO Auto-generated method stub
                  return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selectedMsgIds = null;
                deleteButton.setVisibility(View.GONE);
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                    boolean checked) {
                ListView listView = getListView();
                selectedMsgCount.setText(Integer.toString(listView.getCheckedItemCount()));
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                long messageId = cursor
                        .getLong(cursor.getColumnIndex(Telephony.CellBroadcasts._ID));
                long date = cursor.getLong(cursor.getColumnIndex(Telephony.CellBroadcasts.DELIVERY_TIME));
                if (checked) {
                    /* MODIFIED-BEGIN by yuxuan.zhang, 2016-07-27,BUG-1112693*/
                	//[modify]-begin-by-chaobing.huang-21.12.2016-FR3674099
                    if(getResources().getBoolean(R.bool.def_cmasBroadcastAuthority)){
                        if(CellBroadcastMessage.createFromCursor(cursor).getServiceCategory() >=4370 && CellBroadcastMessage.createFromCursor(cursor).getServiceCategory() <= 4381){
                            listView.setItemChecked(position, false);
                        	return;
                        }
                    }
                    //[modify]-begin-by-chaobing.huang-21.12.2016-FR3674099
                    /* MODIFIED-END by yuxuan.zhang,BUG-1112693*/
                    selectedMsgIds.add(messageId);
                    messageCheckedStatus.add(date);
                } else {
                    selectedMsgIds.remove(messageId);
                    messageCheckedStatus.remove(date);
                }

            }

            public class DeleteMessageListener implements OnClickListener {
                private final Long[] mRowIds;

                public DeleteMessageListener(Long[] rowIds) {
                    mRowIds = rowIds;
                }

                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    // delete from database on a background thread
                    new CellBroadcastContentProvider.AsyncCellBroadcastTask(
                            getActivity().getContentResolver()).execute(
                            new CellBroadcastContentProvider.CellBroadcastOperation() {
                                @Override
                                public boolean execute(CellBroadcastContentProvider provider) {
                                    for (long id : mRowIds) {
                                        provider.deleteBroadcast(id);
                                    }
                                    return true;
                                }
                            });

                    dialog.dismiss();
                }
            }

        }
        //[BUGFIX]-Add-END by TCTNB.yugang.jia
    }
}
