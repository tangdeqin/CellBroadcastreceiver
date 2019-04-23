
package com.jrdcom.timetool.countdown.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.countdown.view.VerticalTextPicker.OnChangedListener;

public class CountDownPickerTcl extends LinearLayout {
    private int mCurrentHour = 0;

    private int mCurrentMinute = 0;

    private int mCurrentSecond = 0;

    private  VerticalTextPicker mHourPicker;

    private  VerticalTextPicker mMinutePicker;

    private  VerticalTextPicker mSecondPicker;

    private OnTimerChangedListener mOnTimerChangedListener;

    public interface OnTimerChangedListener {
        void onTimerChanged(CountDownPickerTcl view, int hour, int minute, int second);
    }

    public CountDownPickerTcl(Context context) {
        this(context, null);
    }

    public CountDownPickerTcl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownPickerTcl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MyLog.debug("start inflate layout", getClass());
        inflater.inflate(R.layout.countdown_picker_tclf, this, true);
    }
    
    
    
    // modified by haifeng.tang Pr 785760 
    private void initLayout() {
        MyLog.debug("inflate layout success", getClass());

        mHourPicker = (VerticalTextPicker) findViewById(R.id.timerpicker_hour);
        mHourPicker.setRange(0, 24);
        mHourPicker.setOnChangeListener(mChangedListener);

        mMinutePicker = (VerticalTextPicker) findViewById(R.id.timerpicker_minute);
        mMinutePicker.setRange(0, 59);
        mMinutePicker.setOnChangeListener(mChangedListener);

        mSecondPicker = (VerticalTextPicker) findViewById(R.id.timerpicker_second);
        mSecondPicker.setRange(0, 59);
        mSecondPicker.setOnChangeListener(mChangedListener);
        MyLog.debug("inflate layout end", getClass());

        init(0, 1, 0, null);
        if (!isEnabled()) {
            setEnabled(false);
        }
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // add by haifeng.tang Pr 785760  begin
        initLayout();
        // add by haifeng.tang Pr 785760  end
    }

    private OnChangedListener mChangedListener = new OnChangedListener() {
        public void onChanged(VerticalTextPicker spinner, int oldPos, int newPos, String[] items) {

            mCurrentHour = Integer.parseInt(mHourPicker.getCurrent());
            mCurrentMinute = Integer.parseInt(mMinutePicker.getCurrent());
            mCurrentSecond = Integer.parseInt(mSecondPicker.getCurrent());
            onTimerChanged();
        }
    };

    public void init(int hour, int minute, int second, OnTimerChangedListener onTimerChangedListener) {
        setCurrentHour(hour);
        setCurrentMinute(minute);
        setCurrentSecond(second);
        setOnTimerChangedListener(onTimerChangedListener);
    }

    public Integer getCurrentHour() {
        return mCurrentHour;
    }

    public Integer getCurrentMinute() {
        return mCurrentMinute;
    }

    public Integer getCurrentSecond() {
        return mCurrentSecond;
    }

    public void setCurrentHour(Integer currentHour) {
        mCurrentHour = currentHour;
        mHourPicker.setCurrent(pad(currentHour));
    }

    public void setCurrentMinute(Integer currentMinute) {
        mCurrentMinute = currentMinute;
        mMinutePicker.setCurrent(pad(currentMinute));
    }

    public void setCurrentSecond(Integer currentSecond) {
        mCurrentSecond = currentSecond;
        mSecondPicker.setCurrent(pad(currentSecond));
    }

    public void setOnTimerChangedListener(OnTimerChangedListener onTimerChangedListener) {
        mOnTimerChangedListener = onTimerChangedListener;
    }

    private void onTimerChanged() {
        if (mOnTimerChangedListener != null) {
            mOnTimerChangedListener.onTimerChanged(this, getCurrentHour(), getCurrentMinute(),
                    getCurrentSecond());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mMinutePicker.setEnabled(enabled);
        mHourPicker.setEnabled(enabled);
        mSecondPicker.setEnabled(enabled);
    }

    private String pad(int i) {
        return i < 10 ? "0" + i : String.valueOf(i);
    }
}
