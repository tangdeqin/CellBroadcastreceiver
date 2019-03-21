package com.example.user.viewmove1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.jar.Attributes;

/**
 * Created by user on 19-3-21.
 */

public class CustomView extends View {
    private int lastX;
    private int lastY;
    public CustomView(Context context){
        super(context);
    }
    public CustomView(Context context, AttributeSet attributes,int def){
        super(context, attributes,def);
    }
    public CustomView(Context context,AttributeSet attributeSet){
        super(context,attributeSet);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int offsetx = x - lastX;
                int offsety = y - lastY;
                layout(getLeft()+offsetx,getTop()+offsety,getRight()+offsetx,getBottom()+offsety);
        }
        return true;
    }
}
