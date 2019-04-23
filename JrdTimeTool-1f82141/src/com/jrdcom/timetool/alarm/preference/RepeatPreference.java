/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.jrdcom.timetool.alarm.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import com.jrdcom.timetool.alarm.activity.SetAlarm;
import com.jrdcom.timetool.alarm.provider.Alarm;

public class RepeatPreference extends ListPreference {

    // Initial value that can be set with the values saved in the database.
    private Alarm.DaysOfWeek mDaysOfWeek = new Alarm.DaysOfWeek(0);
    // New value that will be set if a positive result comes back from the
    // dialog.
    private Alarm.DaysOfWeek mNewDaysOfWeek = new Alarm.DaysOfWeek(0);

    public RepeatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        String[] weekdays = new DateFormatSymbols().getWeekdays();
        
        String[] values;
        
        /*CR 566879- Neo Skunkworks - Paul Xu modified - 001 Begin*/
		if (isEsLanguage(context)) {
			
			values = new String[] { "Lunes", "Martes", "Miércoles", "Jueves",
					"Viernes", "Sábado", "Domingo", };

		} else {
			values = new String[] { weekdays[Calendar.MONDAY],
					weekdays[Calendar.TUESDAY], weekdays[Calendar.WEDNESDAY],
					weekdays[Calendar.THURSDAY], weekdays[Calendar.FRIDAY],
					weekdays[Calendar.SATURDAY], weekdays[Calendar.SUNDAY], };
		}
		/* CR 566879- Neo Skunkworks - Paul Xu modified - 001 End */
        setEntries(values);
        setEntryValues(values);
    }
    
    /*CR 566879- Neo Skunkworks - Paul Xu added - 001 Begin*/
    private boolean isEsLanguage(Context context){

		String language = context.getResources().getConfiguration().locale.getLanguage();
		
		if ("es".equals(language)) {
			return true;
		}
    	
    	return false;
    }
    /*CR 566879- Neo Skunkworks - Paul Xu added - 001 End*/

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            mDaysOfWeek.set(mNewDaysOfWeek);
            setSummary(mDaysOfWeek.toString(getContext(), true));
            callChangeListener(mDaysOfWeek);
            SetAlarm.mRepeatPref.setDaysOfWeek(mDaysOfWeek);// PR -596220 - Neo Skunworks - Soar Gao , add -001 //update the display of the repeat in setAlarm
        } else {
            mNewDaysOfWeek.set(mDaysOfWeek);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();

        builder.setMultiChoiceItems(
                entries, mDaysOfWeek.getBooleanArray(),
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which,
                            boolean isChecked) {
                        mNewDaysOfWeek.set(which, isChecked);
                    }
                });
    }

    public void setDaysOfWeek(Alarm.DaysOfWeek dow) {
        mDaysOfWeek.set(dow);
        mNewDaysOfWeek.set(dow);
        setSummary(dow.toString(getContext(), true));
    }

    public Alarm.DaysOfWeek getDaysOfWeek() {
        return mDaysOfWeek;
    }
}
