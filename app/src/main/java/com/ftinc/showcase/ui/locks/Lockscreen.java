package com.ftinc.showcase.ui.locks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ftinc.showcase.R;

/**
 * This is the lockscreen interface that provides a unified way of
 * supplying a lockscreen to the video service to provide a user with
 * the means of unlocking the video
 *
 * Created by r0adkll on 10/5/14.
 */
public abstract class Lockscreen {

    public static final int MATCH = 0;
    public static final int MISMATCH = 1;
    public static final int SETUP = 2;

    public static final String TYPE_NONE = "none";
    public static final String TYPE_PIN = "pin";
    public static final String TYPE_PATTERN = "pattern";
    public static final String TYPE_PASSCODE = "password";
    public static final String TYPE_CUSTOM_GESTURE = "custom gesture";

    /**
     * The application context reference
     */
    private Context mContext;

    /**
     * The input listener that the overriding class calls to
     * check it's newly gathered input against the encrypted input
     * in the secure store
     */
    private OnCheckInputListener mInputListener;

    /**
     * Set this lockscreens context reference
     *
     * @param ctx       the context reference
     */
    public void setContext(Context ctx){
        mContext = ctx;
    }

    /**
     * Set the input check listener
     */
    public void setOnCheckInputListener(OnCheckInputListener listener){
        mInputListener = listener;
    }

    /**
     * Generate the LockScreen UI to insert into the layout
     *
     * @param container     the container view to insert into
     * @return              the inflated view with dimmed background
     */
    public View createView(ViewGroup container){
        View layout = onCreateView(LayoutInflater.from(mContext), container);

        // Modify the background to transparent black, possibly with blur filter
        layout.setBackgroundColor(getContext().getResources().getColor(R.color.black80));

        // Return the modified layout
        return layout;
    }

    /**
     * Get the reference to the context
     * @return      the app context
     */
    protected Context getContext(){
        return mContext;
    }

    /**
     * Check the user's input against the secure store
     *
     * @param input     the input to check
     */
    protected int checkInput(byte[] input){
        if(mInputListener != null)
            return mInputListener.checkInput(input);

        return SETUP;
    }



    /**
     * Create/Inflate the view/ui for this lockscreen and return it to be overlayed on the
     * video
     *
     * @param inflater      the view inflater
     * @param container     the container of this view
     */
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container);

    /**
     * Called after the layout has been generated and added to
     * the container layout
     */
    public abstract void onCreated();

    /**
     * Called after the kiosk service fades the screen out and then reclaims it's resources
     */
    public abstract void onDestroy();

    /**
     * Call this to reset the lockscreen for another round of input
     */
    public abstract void reset(String message);

    /**
     * Start any special animation to add lockscreen components for pizazz
     * @param duration      the duration of the animation allowed
     */
    public void onAnimateIn(long duration){}

    /**
     * Start any special animation to remove the lockscreen components for pizzaz
     *
     * @param duration      the duration of the animation allowed
     */
    public void onAnimateOut(long duration){}


    public static interface OnCheckInputListener{
        public int checkInput(byte[] input);
    }

}
