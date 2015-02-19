package com.ftinc.showcase.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by r0adkll on 10/18/14.
 */
public class TouchedFrameLayout extends FrameLayout {

    /***********************************************************************************************
     *
     * Variables
     *
     */

    private OnTouchedEventListener mListener;

    /***********************************************************************************************
     *
     * Constructors
     *
     */

    public TouchedFrameLayout(Context context) {
        super(context);
    }

    public TouchedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // Call listener if available
        if(mListener != null)
            mListener.onTouchedEvent(ev);

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * Set the onTouchedEventListener to be called when the user
     * interfaces with content in this frame.
     *
     * @param listener      the listener
     */
    public void setOnTouchedEventListener(OnTouchedEventListener listener){
        mListener = listener;
    }

    /**
     * The Touched event listener to communicate to the parent
     * that the user is touching content in this layout so that
     * it can act upon it.
     */
    public static interface OnTouchedEventListener{
        public void onTouchedEvent(MotionEvent event);
    }

}
