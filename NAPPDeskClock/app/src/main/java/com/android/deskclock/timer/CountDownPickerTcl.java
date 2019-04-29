
package com.android.deskclock.timer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.text.format.DateUtils;

import com.android.deskclock.R;
import com.android.deskclock.FabContainer;
//import com.jrdcom.timetool.MyLog;
import com.android.deskclock.timer.VerticalTextPicker.OnChangedListener;
import static com.android.deskclock.FabContainer.FAB_SHRINK_AND_EXPAND;
import static com.android.deskclock.FabContainer.FAB_REQUEST_FOCUS;

import java.io.Serializable;
import java.util.Arrays;

public class CountDownPickerTcl extends LinearLayout {
    private int mCurrentHour = 0;

    private int mCurrentMinute = 0;

    private int mCurrentSecond = 0;

    private int mFabShowFlag = -1;

    private final int[] mInput = { 0, 0, 0, 0, 0, 0 };

    private FabContainer mFabContainer;

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
        //MyLog.debug("start inflate layout", getClass());
        inflater.inflate(R.layout.countdown_picker_tclf, this, true);
    }
    
    
    
    // modified by haifeng.tang Pr 785760 
    private void initLayout() {
        //MyLog.debug("inflate layout success", getClass());

        mHourPicker = (VerticalTextPicker) findViewById(R.id.timerpicker_hour);
        mHourPicker.setRange(0, 59);
        mHourPicker.setOnChangeListener(mChangedListener);

        mMinutePicker = (VerticalTextPicker) findViewById(R.id.timerpicker_minute);
        mMinutePicker.setRange(0, 59);
        mMinutePicker.setOnChangeListener(mChangedListener);

        mSecondPicker = (VerticalTextPicker) findViewById(R.id.timerpicker_second);
        mSecondPicker.setRange(0, 59);
        mSecondPicker.setOnChangeListener(mChangedListener);
        //MyLog.debug("inflate layout end", getClass());

        init(0, 0, 0, null);
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

  public void setFabContainer(FabContainer fabContainer) {
        mFabContainer = fabContainer;
    }
    private void updateFab() {
        mFabContainer.updateFab(FAB_SHRINK_AND_EXPAND);
    }

    private OnChangedListener mChangedListener = new OnChangedListener() {
        public void onChanged(VerticalTextPicker spinner, int oldPos, int newPos, String[] items) {

            mCurrentHour = Integer.parseInt(mHourPicker.getCurrent());
            mCurrentMinute = Integer.parseInt(mMinutePicker.getCurrent());
            mCurrentSecond = Integer.parseInt(mSecondPicker.getCurrent());
            onTimerChanged();
            
            if (hasValidInput()) {
                mFabShowFlag++;
                if(mFabShowFlag > 1){
                    mFabShowFlag = 1;
                }
                if (mFabShowFlag==0) {
                    updateFab();
                }
            }else{
               mFabShowFlag = -1;
               updateFab();
            }
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

    public void reset(){
         Arrays.fill(mInput, 0);
         mFabShowFlag = -1;
        setCurrentHour(0);
        setCurrentMinute(0);
        setCurrentSecond(0);

    }

    public boolean hasValidInput() {
        if(mCurrentSecond==0&&mCurrentMinute==0&&mCurrentHour==0){
            return false;
        }
        return true;
    }

    public long getTimeInMillis() {
        return mCurrentSecond * DateUtils.SECOND_IN_MILLIS
                + mCurrentMinute * DateUtils.MINUTE_IN_MILLIS
                + mCurrentHour * DateUtils.HOUR_IN_MILLIS;
    }

        /**
     * @return an opaque representation of the state of timer setup
     */
    public Serializable getState() {
        mInput[0] = mCurrentSecond % 10;
        mInput[1] = mCurrentSecond /10;
        mInput[2] = mCurrentMinute % 10;
        mInput[3] = mCurrentMinute /10;
        mInput[4] = mCurrentHour % 10;
        mInput[5] = mCurrentHour / 10;
        return Arrays.copyOf(mInput, mInput.length);
    }

    /**
     * @param state an opaque state of this view previously produced by {@link #getState()}
     */
    public void setState(Serializable state) {
        final int[] input = (int[]) state;
        if (input != null && mInput.length == input.length) {
            for (int i = 0; i < mInput.length; i++) {
                mInput[i] = input[i];
            }
        }
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
