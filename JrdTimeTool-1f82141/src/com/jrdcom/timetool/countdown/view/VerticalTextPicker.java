
package com.jrdcom.timetool.countdown.view;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;

public class VerticalTextPicker extends View {

    private static final int TEXT_HORIZONTAL_MARGIN = 10;

    private float itemHeight;

    private float text_margin = 5;

    private float center_x = -1;
    private float center_y = -1;
    private float text_height;
    private float text_width;
    private float picker_text_canvas_height;

    private int TEXT_SIZE;

    private float VERTICAL_BACKGROUND_MARGIN = 5;

    private float HORIZONTAL_BACKGROUND_MARGIN = 20;

    private static final int SCROLL_MODE_NONE = 0;

    private static final int SCROLL_MODE_UP = -1;

    private static final int SCROLL_MODE_DOWN = 1;

    private static final int MOTION_STOP = 0;

    private static final int MOTION_SCROLL_ONE_ITEM = 1;

    private static final int MOTION_SCROLLING = 2;

    private static final int MOTION_ADJUST = 3;

    private static final int MOTION_PROGRESS = 4;

    public static final int ALIGN_RIGHT = 0;

    public static final int ALIGN_CENTER = 1;

    public static final int ALIGN_LEFT = 2;

    public static final int AMOUNT_THR = 3;

    public static final int AMOUNT_FIV = 5;

    public static final int AMOUNT_SEV = 7;

    public static final int SPINNER_POS_SINGLE = 0;

    public static final int SPINNER_POS_LEFT = 1;

    public static final int SPINNER_POS_CENTER = 2;

    public static final int SPINNER_POS_RIGHT = 3;

    private int SCROLL_DISTANCE;

    private Drawable mBackgroundFocused;

    private TextPaint mTextPaintLight;

    private TextPaint mTextPaintBig;

    private int mPreMovDistance;

    private int mTotalMovDistance;

    private int mTotalMovCount;

    private int mMotion;

    private int mCount;

    private long mPreTime;

    private boolean mToBottom = false;

    private int mAmountOfItems = AMOUNT_SEV;

    private int mTotalDistance;

    private Context mContext;

    private int mScrollMode;

    private boolean mWrapAround = true;

    private int mTotalAnimatedDistance;

    private int mDistanceOfEachAnimation;

    private String[] mTextList;

    private int mCurrentSelectedPos;

    private int mIsScroll;

    private OnChangedListener mListener;
    private String[] mText;

    private String mTitle;

    // add by haifeng.tang start,unit=dp or dip
    private float PAINT_OFFSET = 7;

    // add by haifeng.tang end

    public interface OnChangedListener {
        void onChanged(VerticalTextPicker spinner, int oldPos, int newPos, String[] items);
    }

    public VerticalTextPicker(Context context) {
        this(context, null);
    }

    public VerticalTextPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalTextPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMotion = MOTION_STOP;
        mContext = context;

        // add by hafieng.tang start
        mBackgroundFocused = mContext.getResources().getDrawable(R.drawable.countdown_setting);
        setBackgroundDrawable(mBackgroundFocused);
        initPaint();
        convertValue();
        initData();

    }

    /**
     * add by haifeng.tang 2014.8.25
     */

    private void initData() {
        mText = new String[9];
        mScrollMode = SCROLL_MODE_NONE;
        setAmount(AMOUNT_SEV);
    }

    // add by haifeng.tang
    private void initPaint() {

        mTextPaintLight = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaintLight.setColor(Color.LTGRAY);
        mTextPaintLight.setTextAlign(Paint.Align.CENTER);
        mTextPaintLight.setShadowLayer(1, 0, -1, R.color.timer_shadow_color);
        mTextPaintBig = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaintBig.setColor(Color.WHITE);
        mTextPaintBig.setShadowLayer(1, 0, -1, R.color.timer_shadow_color);
        mTextPaintBig.setTextAlign(Paint.Align.CENTER);

    }

    private void convertValue() {
        PAINT_OFFSET = dpToPx(PAINT_OFFSET);
        text_margin = dpToPx(text_margin);
        VERTICAL_BACKGROUND_MARGIN = dpToPx(VERTICAL_BACKGROUND_MARGIN);
        HORIZONTAL_BACKGROUND_MARGIN = dpToPx(HORIZONTAL_BACKGROUND_MARGIN);
    }

    // add by haifeng.tang
    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources()
                .getDisplayMetrics());
    }

    /*
     * Interface
     */
    public void setOnChangeListener(OnChangedListener listener) {
        mListener = listener;
    }

    /*
     * Set string list which spinner will use to display
     */
    public void setItems(String[] textList) {
        mTextList = textList;
        calculateTextPositions();
    }

    private void calculateItem() {
        MyLog.debug("calculate item", getClass());
        mTextPaintBig.setTextSize(TEXT_SIZE);
        Rect item = new Rect();
        MyLog.debug("mTextList[0]->" + mTextList[0], getClass());
        mTextPaintBig.getTextBounds(mTextList[0], 0, mTextList[0].length(), item);
        text_height = item.height();
        MyLog.debug("textHeight->" + text_height, getClass());
        text_width = item.width();
        MyLog.debug("text_width->" + text_width, getClass());
        float real_text_item_height = (picker_text_canvas_height - text_margin * 2f) / 3f;
        MyLog.debug("real_text_item_height->" + real_text_item_height, getClass());
        float real_text_item_width = getMeasuredWidth() - HORIZONTAL_BACKGROUND_MARGIN * 2;
        MyLog.debug("real_text_item_width->" + real_text_item_width, getClass());
        MyLog.debug("getMeasuredWidth->" + getMeasuredWidth(), getClass());
        boolean b = text_height > real_text_item_height || text_width > real_text_item_width;
        MyLog.debug("b->" + b, getClass());
        if (b) {
            TEXT_SIZE -= 1;
            mTextPaintBig.setTextSize(TEXT_SIZE);
            mTextPaintLight.setTextSize(TEXT_SIZE - PAINT_OFFSET);
            calculateItem();
        }

    }

    public void setRange(int start, int end) {
        mTextList = new String[end - start + 1];
        for (int i = 0, j = start; i <= end - start; i++, j++) {
            mTextList[i] = j < 10 ? ("0" + j) : String.valueOf(j);
        }
    }

    public void setRange(String[] str) {
        int len = str.length;
        mTextList = new String[str.length];
        for (int i = 0; i < len; i++) {
            mTextList[i] = str[i];
        }
    }

    public void setCurrent(String current) {
        if (mTextList == null || current == null) {
            return;
        }
        int selectedPos = 0;
        for (int i = 0; i < mTextList.length; i++) {
            if (mTextList[i] != null && mTextList[i].equals(current)) {
                selectedPos = i;
                break;
            }
        }
        setSelectedPos(selectedPos);
    }

    /*
     * Set default item which had been selected last time
     */
    public void setSelectedPos(int selectedPos) {
        mCurrentSelectedPos = selectedPos;
        calculateTextPositions();
        postInvalidate();
    }

    /*
     * Set if display all items in circulation mode default: false
     */

    public void setWrapAround(boolean wrap) {
        mWrapAround = wrap;
    }

    /*
     * Set Title on the selector,if do not set,no title default: null
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /*
     * Set amount of all the items to display default : 3
     */
    public void setAmount(int n) {
        switch (n) {
            case AMOUNT_FIV:
            case AMOUNT_SEV:
                mAmountOfItems = n;
                break;
            default:
                mAmountOfItems = AMOUNT_SEV;
        }
    }

    /*
     * return Item list
     */

    public String[] getTextList() {
        return mTextList;
    }

    /*
     * return the item with the index
     */
    public String getTextItem(int idx) {
        return mTextList[idx];
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (hasFocus()) {
            if ((keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
                mScrollMode = SCROLL_MODE_UP;
                mMotion = MOTION_SCROLL_ONE_ITEM;
                invalidate();

                return true;
            } else if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
                mScrollMode = SCROLL_MODE_DOWN;
                mMotion = MOTION_SCROLL_ONE_ITEM;
                invalidate();
                return true;
            }

            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                requestFocus();
                mPreMovDistance = y;
                mTotalMovDistance = 0;
                mTotalMovCount = 0;
                mMotion = MOTION_STOP;
                mDistanceOfEachAnimation = 0;
                break;

            case MotionEvent.ACTION_MOVE:

                int pos = y - mPreMovDistance;

                mTotalMovDistance += Math.abs(pos);

                if (pos < 0) {
                    if (mScrollMode != SCROLL_MODE_UP) {
                        mScrollMode = SCROLL_MODE_UP;
                    }
                    if (Math.abs(pos) >= SCROLL_DISTANCE)
                        mDistanceOfEachAnimation = SCROLL_DISTANCE;
                    else
                        mDistanceOfEachAnimation = Math.abs(pos);
                    invalidate();
                } else if (pos > 0) {
                    if (mScrollMode != SCROLL_MODE_DOWN) {
                        mScrollMode = SCROLL_MODE_DOWN;

                    }
                    if (pos >= SCROLL_DISTANCE)
                        mDistanceOfEachAnimation = SCROLL_DISTANCE;
                    else
                        mDistanceOfEachAnimation = pos;
                    invalidate();
                }
                mPreTime = new Date().getTime();
                mPreMovDistance = y;
                mTotalMovCount++;
                break;

            case MotionEvent.ACTION_UP:
                if (mTotalMovCount != 0) {
                    int speed = mTotalMovDistance / mTotalMovCount;

                    long curTime = new Date().getTime();
                    if (speed > 12 && curTime - mPreTime < 100) {
                        mMotion = MOTION_SCROLLING;
                        if (speed > 30)
                            mDistanceOfEachAnimation = 30;
                        else
                            mDistanceOfEachAnimation = speed;
                    } else {
                        mDistanceOfEachAnimation = 0;
                        mMotion = MOTION_ADJUST;
                    }

                } else
                    mMotion = MOTION_ADJUST;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:

                invalidate();
                break;
            default:
                invalidate();
                break;
        }
        return true;
    }

    protected int getMeasuredHeight_tcl() {
        return getMeasuredHeight();

    }

    /**
     * add by haifeng.tang 2014.8.20
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthsize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightsize = MeasureSpec.getSize(heightMeasureSpec);
        MyLog.debug("widthsize->" + widthsize, getClass());
        MyLog.debug("widthMode->" + widthMode, getClass());
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = 0;
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
                height = mBackgroundFocused.getIntrinsicHeight();
                break;
            case MeasureSpec.EXACTLY:
                height = heightsize;
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(mBackgroundFocused.getIntrinsicHeight(), heightsize);
                break;
        }
        int width = 0;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
                MyLog.debug("MeasureSpec.UNSPECIFIED", getClass());
                width = mBackgroundFocused.getIntrinsicWidth();
                break;
            case MeasureSpec.EXACTLY:
                MyLog.debug("MeasureSpec.EXACTLY", getClass());
                width = widthsize;
                break;
            case MeasureSpec.AT_MOST:
                MyLog.debug("MeasureSpec.AT_MOST", getClass());
                width = Math.min(mBackgroundFocused.getIntrinsicWidth(), widthsize);
                break;
        }

        MyLog.debug("with->" + width, getClass());

        setMeasuredDimension(width, height);
        calculateTextSize();
        MyLog.debug("onMesure", getClass());
        calculateItem();
    }

    /**
     * add by haifeng.tang for adjust screen
     */
    private void calculateTextSize() {
        picker_text_canvas_height = getMeasuredHeight() - VERTICAL_BACKGROUND_MARGIN * 2;
        itemHeight = picker_text_canvas_height / 3;
        center_x = getMeasuredWidth() / 2f;
        center_y = picker_text_canvas_height / 2 + VERTICAL_BACKGROUND_MARGIN;
        TEXT_SIZE = getMeasuredHeight() / 3;
        mTextPaintBig.setTextSize(TEXT_SIZE);
        mTextPaintLight.setTextSize(TEXT_SIZE - PAINT_OFFSET);
        SCROLL_DISTANCE = (int) itemHeight;
    }

    /* PR 564397- Neo Skunkworks - Paul Xu added - 001 End */

    @Override
    protected void onDraw(Canvas canvas) {

        if (mTextList == null || mTextList.length == 0) {
            return;
        }
        int nextScrollDistance = mTotalAnimatedDistance + mDistanceOfEachAnimation * mScrollMode;

        if (nextScrollDistance >= SCROLL_DISTANCE || nextScrollDistance <= -SCROLL_DISTANCE) {
            if (mScrollMode == SCROLL_MODE_UP) {
                int oldPos = mCurrentSelectedPos;
                int newPos = getNewIndex(1);

                if (newPos >= 0) {
                    mIsScroll = 1;
                    mCurrentSelectedPos = newPos;
                    mTotalAnimatedDistance += SCROLL_DISTANCE - mDistanceOfEachAnimation;
                    if (mListener != null) {
                        mListener.onChanged(this, oldPos, newPos, mTextList);
                    }
                }

                calculateTextPositions();
            } else if (mScrollMode == SCROLL_MODE_DOWN) {
                int oldPos = mCurrentSelectedPos;
                int newPos = getNewIndex(-1);
                if (newPos >= 0) {
                    mIsScroll = 1;
                    mCurrentSelectedPos = newPos;
                    mTotalAnimatedDistance += mDistanceOfEachAnimation - SCROLL_DISTANCE;
                    if (mListener != null) {
                        mListener.onChanged(this, oldPos, newPos, mTextList);
                    }
                }
                calculateTextPositions();
            }
        } else {
            if (mScrollMode == SCROLL_MODE_DOWN)
                mTotalAnimatedDistance += mDistanceOfEachAnimation;
            else
                mTotalAnimatedDistance -= mDistanceOfEachAnimation;
            mIsScroll = 0;
        }

        if ((mCurrentSelectedPos == 0 && mScrollMode == SCROLL_MODE_DOWN
                && mTotalAnimatedDistance > SCROLL_DISTANCE / 2 || mCurrentSelectedPos == mTextList.length - 1
                && mScrollMode == SCROLL_MODE_UP && mTotalAnimatedDistance < -SCROLL_DISTANCE / 2)
                && mWrapAround == false) {
            if (mScrollMode == SCROLL_MODE_UP)
                mTotalAnimatedDistance = -SCROLL_DISTANCE / 2;
            else
                mTotalAnimatedDistance = SCROLL_DISTANCE / 2;
            mToBottom = true;
        }
        canvas.save();
        canvas.clipRect(0, VERTICAL_BACKGROUND_MARGIN, getMeasuredWidth(), getMeasuredHeight()
                - VERTICAL_BACKGROUND_MARGIN);

        // modified haifeng.tang 2014.8.22 start

        float center_baseline = center_y + text_height / 2;

        int centerItemIndex = mAmountOfItems / 2;

        if (mWrapAround) {
            for (int i = 1; i <= mAmountOfItems / 2; i++) {
                drawText(canvas, mTextPaintLight, center_baseline - i * SCROLL_DISTANCE
                        + mTotalAnimatedDistance, mText[3 - i]);
                drawText(canvas, mTextPaintLight, center_baseline + i * SCROLL_DISTANCE
                        + mTotalAnimatedDistance, mText[3 + i]);
            }

            drawText(canvas, mTextPaintBig, center_baseline + mTotalAnimatedDistance, mText[3]);
        } else {
            drawText(canvas, mTextPaintBig, center_baseline + mTotalAnimatedDistance, mText[1]);
            drawText(canvas, mTextPaintLight, center_baseline - SCROLL_DISTANCE
                    + mTotalAnimatedDistance, mText[0]);
            drawText(canvas, mTextPaintLight, center_baseline + SCROLL_DISTANCE
                    + mTotalAnimatedDistance, mText[2]);
        }
        // modified haifeng.tang 2014.8.22 end

        canvas.restore();

        if (mMotion == MOTION_SCROLL_ONE_ITEM) {
            mDistanceOfEachAnimation = 4;
            if (mToBottom == true) {
                mMotion = MOTION_ADJUST;
                mToBottom = false;
                Adjust();
            } else {
                invalidate();
                mTotalDistance += 4;
            }
            if (mIsScroll == 1) {
                mMotion = MOTION_STOP;
                mTotalDistance = 0;
                mDistanceOfEachAnimation = 0;
            }
        } else if (mMotion == MOTION_SCROLLING) {
            if (mToBottom == true) {
                mMotion = MOTION_ADJUST;
                mCount = 0;
                mToBottom = false;
            } else {
                mCount++;
                if (mCount % 6 == 0) {
                    mDistanceOfEachAnimation -= 4;
                    mCount = 0;
                }

                if (mDistanceOfEachAnimation < 0) {
                    mDistanceOfEachAnimation = 0;
                    mMotion = MOTION_ADJUST;
                }
            }
            invalidate();
        } else if (mMotion > MOTION_SCROLLING) {
            Adjust();
        }
    }

    private void calculateTextPositions() {
        if (mWrapAround) {
            mText[0] = getTextToDraw(-3);
            mText[1] = getTextToDraw(-2);
            mText[2] = getTextToDraw(-1);
            mText[3] = getTextToDraw(0);
            mText[4] = getTextToDraw(1);
            mText[5] = getTextToDraw(2);
            mText[6] = getTextToDraw(3);
        } else {
            mText[0] = getTextToDraw(-1);
            mText[1] = getTextToDraw(0);
            mText[2] = getTextToDraw(1);
        }
    }

    private void drawText(Canvas canvas, Paint paint, float y, String text) {
        if (center_x == -1) {
            center_x = getMeasuredWidth() / 2;
        }
        canvas.drawText(text, center_x, y, paint);
    }

    private String getTextToDraw(int offset) {
        int index = getNewIndex(offset);
        if (index < 0) {
            return "";
        }
        return mTextList[index];
    }

    private int getNewIndex(int offset) {
        int index = mCurrentSelectedPos + offset;
        if (index < 0) {
            if (mWrapAround) {
                index += mTextList.length;
            } else {
                return -1;
            }
        } else if (index >= mTextList.length) {
            if (mWrapAround) {
                index -= mTextList.length;
            } else {
                return -1;
            }
        }
        return index;
    }

    private void Adjust() {
        if (Math.abs(mTotalAnimatedDistance) == 0
                || Math.abs(mTotalAnimatedDistance) == SCROLL_DISTANCE) {
            mDistanceOfEachAnimation = 0;
            mMotion = MOTION_STOP;
        } else if (mMotion == MOTION_ADJUST) {
            mDistanceOfEachAnimation = 1;
            mMotion = MOTION_PROGRESS;
            if (Math.abs(mTotalAnimatedDistance) <= SCROLL_DISTANCE / 2) {
                if (mTotalAnimatedDistance < 0)
                    mScrollMode = SCROLL_MODE_DOWN;
                else
                    mScrollMode = SCROLL_MODE_UP;
            } else {
                if (mTotalAnimatedDistance < 0)
                    mScrollMode = SCROLL_MODE_UP;
                else
                    mScrollMode = SCROLL_MODE_DOWN;
            }
        }

        invalidate();
    }

    public int getCurrentSelectedPos() {
        return mCurrentSelectedPos;
    }

    public String getCurrent() {
        if (mTextList == null) {
            return null;
        } else {
            return mTextList[mCurrentSelectedPos];
        }
    }
}
