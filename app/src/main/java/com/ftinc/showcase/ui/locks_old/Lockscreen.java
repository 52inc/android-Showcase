package com.ftinc.showcase.ui.locks_old;

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

    /**
     * The Lockscreen type ENUM
     */
    public enum Type{
        NONE,
        PIN,
        PATTERN,
        PASSWORD;

        public static Type from(int ordinal){
            return Type.values()[ordinal];
        }

        /**
         * Create the lockscreen for this given type
         * @return      the lockscreen implementation for this type
         */
        public Lockscreen create(){
            switch (this){
                case PIN:
                    return new PinLockscreen();
                case PATTERN:
                    return null;
                case PASSWORD:
                    return null;
                default:
                    return null;
            }
        }

        /**
         * Get this lockscreen's storage key
         */
        public String getKey(){
            return String.format("lockscreen_key_%s", toString());
        }
    }

    /***********************************************************************************************
     *
     * Constants
     *
     */

    public static final int MATCH = 0;
    public static final int MISMATCH = 1;
    public static final int SETUP = 2;

    /***********************************************************************************************
     *
     * Variables
     *
     */

    /**
     * The application context reference
     */
    private Context mContext;

    private boolean mIsSetup = false;

    /**
     * The input listener that the overriding class calls to
     * check it's newly gathered input against the encrypted input
     * in the secure store
     */
    private OnCheckInputListener mInputListener;

    /**
     * Default Constructor
     */
    public Lockscreen(){}

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Return whether or not this lockscreen is being setup or not
     * @return      true if in setup mode
     */
    public boolean isSetup(){
        return mIsSetup;
    }

    /**
     * Set whether or not this lockscreen is in setup mode
     * @param val
     */
    public void setIsSetup(boolean val){
        mIsSetup = val;
    }

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
        if(!mIsSetup) layout.setBackgroundColor(getContext().getResources().getColor(R.color.black80));

        // Return the modified layout
        return layout;
    }

    /***********************************************************************************************
     *
     * Protected Methods
     *
     */

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

    /***********************************************************************************************
     *
     * Abstract Methods
     *
     */

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

    /***********************************************************************************************
     *
     * Inner Listeners and Methods
     *
     */

    public static interface OnCheckInputListener{
        public int checkInput(byte[] input);
    }

}
