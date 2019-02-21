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

package com.android.cellbroadcastreceiver;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.telephony.CellBroadcastMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RelativeLayout;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.ImageView;
    
// add by deqin.tang for Defect 7143375 at2018-12-06 begin
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;
  // add by deqin.tang for Defect 7143375 at2018-12-06 end


// add by liang.zhang for Defect 6012946 at 2018-03-16 begin
import java.util.ArrayList;
import java.util.List;
// add by liang.zhang for Defect 6012946 at 2018-03-16 end

/**
 * This class manages the list item view for a single alert.
 */
//[BUGFIX]-mod-BEGIN by TCTNB.yugang.jia, 12/25/2013, PR-578732
public class CellBroadcastListItem extends RelativeLayout implements Checkable{
//[BUGFIX]-mod-END by TCTNB.yugang.jia, 12/25/2013, PR-578732

    private CellBroadcastMessage mCbMessage;

    private ImageView mEmergencyIconView;//[BUGFIX]-Mod- by TCTNB.yugang.jia,11/20/2013,PR-558059,
    private TextView mChannelView;
    private TextView mMessageView;
    private TextView mDateView;
    private boolean mChecked = false;//[BUGFIX]-add- by TCTNB.yugang.jia, 12/25/2013, PR-578732

    private SubscriptionManager mSubscriptionManager;//add by liang.zhang for Defect 6012946 at 2018-03-16

    // add by deqin.tang for Defect 7143375 at2018-12-06 begin
    private  Context mContext;
  // add by deqin.tang for Defect 7143375 at2018-12-06 end

    public CellBroadcastListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSubscriptionManager = SubscriptionManager.from(context);
         // add by deqin.tang for Defect 7143375 at2018-12-06 begin
        mContext = context;
         // add by deqin.tang for Defect 7143375 at2018-12-06 end
    }

    CellBroadcastMessage getMessage() {
        return mCbMessage;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mChannelView = (TextView) findViewById(R.id.channel);
        mDateView = (TextView) findViewById(R.id.date);
        mMessageView = (TextView) findViewById(R.id.message);
        mEmergencyIconView = (ImageView) findViewById(R.id.message_icon);//[BUGFIX]-Mod- by TCTNB.yugang.jia,11/20/2013,PR-558059,
    }
    
    // add by liang.zhang for Defect 6012946 at 2018-03-16 begin
    private int isLATAMMessage(CellBroadcastMessage message) {
        SubscriptionInfo subInfo = mSubscriptionManager.getActiveSubscriptionInfo(message.getSubId());
        if (subInfo!= null && subInfo.getMcc() == 716) {
        	return 1;
        } else if (subInfo!= null && subInfo.getMcc() == 730) {
        	return 2;
        } else if (subInfo!= null && subInfo.getMcc() == 334) {
        	return 3;
        }
        
        if (subInfo == null) {
        	List<SubscriptionInfo> subList = mSubscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info!= null && info.getMcc() == 716) {
        	        	return 1;
        	        } else if (info!= null && info.getMcc() == 730) {
        	        	return 2;
        	        } else if (info!= null && info.getMcc() == 334) {
        	        	return 3;
        	        }
        		}
        	}
        }
        return -1;
    }
    // add by liang.zhang for Defect 6012946 at 2018-03-16 end
    
    // add by liang.zhang for Defect 6234832 at 2018-04-24 begin
    private boolean isUAEMessage(CellBroadcastMessage message) {
    	SubscriptionInfo subInfo = mSubscriptionManager.getActiveSubscriptionInfo(message.getSubId());
        if (subInfo!= null && subInfo.getMcc() == 424) {
        	return true;
        }
        
        if (subInfo == null) {
        	List<SubscriptionInfo> subList = mSubscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info!= null && info.getMcc() == 424) {
        	        	return true;
        	        }
        		}
        	}
        }
        return false;
    }
    // add by liang.zhang for Defect 6234832 at 2018-04-24 end
    
	// add by liang.zhang for Defect 6369692 at 2018-06-07 begin
    private boolean isNewZealandMessage(CellBroadcastMessage message) {
    	SubscriptionInfo subInfo = mSubscriptionManager.getActiveSubscriptionInfo(message.getSubId());
        if (subInfo!= null && subInfo.getMcc() == 530) {
        	return true;
        }
        
        if (subInfo == null) {
        	List<SubscriptionInfo> subList = mSubscriptionManager.getActiveSubscriptionInfoList();
        	if (subList != null && subList.size() > 0) {
        		for (int i = 0; i < subList.size(); i++) {
        			SubscriptionInfo info = subList.get(i);
        			if (info!= null && info.getMcc() == 530) {
        	        	return true;
        	        }
        		}
        	}
        }
        return false;
    }
	// add by liang.zhang for Defect 6369692 at 2018-06-07 end
// add by deqin.tang for Defect 7143375 at2018-12-06 begin
    public  Context getLocalLanguage(String local){
            Configuration cofig= getResources().getConfiguration(); 
            cofig.setLocale(new Locale(local));
            Context con = mContext.createConfigurationContext(cofig);
            return con;
    }
    // add by deqin.tang for Defect 7143375 at2018-12-06 end

    /**
     * Only used for header binding.
     * @param message the message contents to bind
     */
    public void bind(CellBroadcastMessage message) {
        mCbMessage = message;
        //[BUGFIX]-mod-BEGIN by TCTNB.yugang.jia, 12/25/2013, PR-578732
        //Drawable background = message.isRead() ?
        //        getResources().getDrawable(R.drawable.list_item_background_read) :
        //        getResources().getDrawable(R.drawable.list_item_background_unread);
        //
        //setBackground(background);
        updateBackground();
        //[BUGFIX]-mod-END by TCTNB.yugang.jia, 12/25/2013, PR-578732
        // add by liang.zhang for Defect 6012946 at 2018-03-16 begin
        int channelNameId = CellBroadcastResources.getDialogTitleResource(message);

          // add by deqin.tang for Defect 7143375 at2018-12-06 begin
         CellBroadcastResources.DialogTitleReturnForUAE mDialogTitleReturnForUAE =null;
         CharSequence stringCharSequence =null;
       // add by deqin.tang for Defect 7143375 at2018-12-06 end
        // add by liang.zhang for Defect 6234832 at 2018-04-24 begin
        if (isUAEMessage(message)) {
                // modify by deqin.tang for Defect 7143375 at2018-12-06 begin
        	//channelNameId = CellBroadcastResources.getDialogTitleResourceForUAE(message);
        	//if (channelNameId == -1) {
        		//channelNameId = CellBroadcastResources.getDialogTitleResource(message);
        	//}
               mDialogTitleReturnForUAE = CellBroadcastResources.getDialogTitleResourceForUAE(message);
               stringCharSequence= getLocalLanguage(mDialogTitleReturnForUAE.language).getText(mDialogTitleReturnForUAE.titleid);
               if(mDialogTitleReturnForUAE == null){
                    channelNameId = CellBroadcastResources.getDialogTitleResource(message);
               }
                // modify by deqin.tang for Defect 7143375 at2018-12-06 end

        }
        // add by liang.zhang for Defect 6234832 at 2018-04-24 end

        
        if (isLATAMMessage(message) == 1 || isLATAMMessage(message) == 2) {
        	channelNameId = CellBroadcastResources.getDialogTitleResourceForPeru(message);
        	if (channelNameId == -1) {
        		channelNameId = CellBroadcastResources.getDialogTitleResource(message);
        	}
        } else if (isLATAMMessage(message) == 3) {
        	channelNameId = CellBroadcastResources.getDialogTitleResourceForMexico(message);
        	if (channelNameId == -1) {
        		channelNameId = CellBroadcastResources.getDialogTitleResource(message);
        	}
        }
        // add by liang.zhang for Defect 6012946 at 2018-03-16 end
        
    	// add by liang.zhang for Defect 6369692 at 2018-06-07 begin
        if (isNewZealandMessage(message)) {
        	channelNameId = CellBroadcastResources.getDialogTitleResourceForNZ(message);
        	if (channelNameId == -1) {
        		channelNameId = CellBroadcastResources.getDialogTitleResource(message);
        	}
        }
    	// add by liang.zhang for Defect 6369692 at 2018-06-07 end

         // modify by deqin.tang for Defect 7143375 at2018-12-06 begin
        if(mDialogTitleReturnForUAE!=null){
            mChannelView.setText(stringCharSequence);
        }
        else{
                    mChannelView.setText(channelNameId);
        }
  //    mChannelView.setText(channelNameId);
         // modify by deqin.tang for Defect 7143375 at2018-12-06 end

        mDateView.setText(message.getDateString(getContext()));
        mMessageView.setText(formatMessage(message));
        //[BUGFIX]-Mod-BEGIN by TCTNB.yugang.jia,11/20/2013,PR-558059,
        //TODO add other emergency message
        if (message.isEmergencyAlertMessage()) {
            //PR 1054793 Added by fang.song begin
            if (CBSUtills.isShow4371AsNormal(message)){
                mEmergencyIconView.setImageResource(R.drawable.ic_common_message);
            } else {
                mEmergencyIconView.setImageResource(R.drawable.ic_emergency_message);
            }
            //PR 1054793 Added by fang.song end
        } else {
            mEmergencyIconView.setImageResource(R.drawable.ic_common_message);
        }
        //[BUGFIX]-Mod-END by TCTNB.yugang.jia
    }

    private static CharSequence formatMessage(CellBroadcastMessage message) {
        String body = message.getMessageBody();

        SpannableStringBuilder buf = new SpannableStringBuilder(body);

        // Unread messages are shown in bold
        if (!message.isRead()) {
            buf.setSpan(new StyleSpan(Typeface.BOLD), 0, buf.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return buf;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Speak the date first, then channel name, then message body
        event.getText().add(mCbMessage.getSpokenDateString(getContext()));
        mChannelView.dispatchPopulateAccessibilityEvent(event);
        mMessageView.dispatchPopulateAccessibilityEvent(event);
        return true;
    }
    //[BUGFIX]-mod-BEGIN by TCTNB.yugang.jia, 12/25/2013, PR-578732
    private void updateBackground() {
        int backgroundId;

        if (mChecked) {
            backgroundId = R.drawable.list_selected_holo_light;
        } else if (!mCbMessage.isRead()) {
            backgroundId = R.drawable.cellbroadcast_message_item_background_unread;
        } else {
            backgroundId = R.drawable.cellbroadcast_message_item_background_read;
        }
        Drawable background = mContext.getResources().getDrawable(backgroundId);
        setBackground(background);
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        updateBackground();
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        mChecked = !mChecked;
        updateBackground();
    }
    //[BUGFIX]-mod-END by TCTNB.yugang.jia, 12/25/2013, PR-578732
}
