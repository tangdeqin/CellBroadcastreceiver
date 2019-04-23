/************************************************************************************************************/
/*                                                                                           Date : 04/2013 */
/*                                      PRESENTATION                                                        */
/*                        Copyright (c) 2012 JRD Communications, Inc.                                       */
/************************************************************************************************************/
/*                                                                                                          */
/*              This material is company confidential, cannot be reproduced in any                          */
/*              form without the written permission of JRD Communications, Inc.                             */
/*                                                                                                          */
/*==========================================================================================================*/
/*   Author :  KUi Wang                                                                                     */
/*   Role :    JrdTimeTool                                                                                  */
/*   Reference documents : None                                                                             */
/*==========================================================================================================*/
/* Comments :                                                                                               */
/*     file    :                                                                                            */
/*     Labels  :                                                                                            */
/*==========================================================================================================*/
/* Modifications   (month/day/year)                                                                         */
/*==========================================================================================================*/
/* date    | author       |FeatureID                                 |modification                          */
/*=========|==============|==========================================|======================================*/

/*==========================================================================================================*/
/* Problems Report(PR/CR)                                                                                   */
/*==========================================================================================================*/
/* date    | author       | PR #                                     |                                      */
/*=========|==============|==========================================|======================================*/
/* 04/17/13 |Kui Wang     |PR439915-kuiwang-001                      | the gesture title display abnormal   */
/*=========|==============|==========================================|======================================*/
package com.jrdcom.timetool;

import android.content.Context;

import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
public class GesturesAlarmPreference extends SwitchPreference{

	public GesturesAlarmPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		TextView title = (TextView)view.findViewById(com.android.internal.R.id.title);
		if (title != null) {
			title.setSingleLine(false);
		}
	}
}
