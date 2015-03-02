package com.ftinc.showcase.ui.lock.ui;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ftinc.showcase.ui.lock.LockState;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock
 * Created by drew.heavner on 2/27/15.
 */
public abstract class LockUI {

    /***********************************************************************************************
     *
     * Variables
     *
     */

    private Context mCtx;
    private UICallbacks mCallbacks;
    private LockState mState;

    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */

    /**
     * Called to initialize and setup everything needed
     * for the UI including View listeners, etc
     */
    public abstract void onCreate();

    /**
     * Called to shutdown anything that needs to be released, etc.
     */
    public abstract void onDestroy();

    /**
     * Called to initialize and create the view for this lock UI
     *
     * @param inflater      the layout inflater used to inflate layouts from R.layout
     * @param parent        the parent layout that the view created here will be placed into
     * @return              the lock Ui component
     */
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup parent);

    /***********************************************************************************************
     *
     * Abstract Helper Methods
     *
     */

    /**
     * Called to reset the UI to it's default state
     */
    public abstract void onReset();

    /**
     * Called when the UI has a successful input/match when calling it's
     * {@link com.ftinc.showcase.ui.lock.ui.LockUI.UICallbacks} callbacks
     *
     * Here the UI should illustrate/display the failure in some fashion
     */
    public abstract void onSuccess();

    /**
     * Called when the UI has a failed input/mismatch when calling it's
     * {@link com.ftinc.showcase.ui.lock.ui.LockUI.UICallbacks} callbacks.
     *
     * Here the UI should illustrate/display the failure in some fashion
     */
    public abstract void onFailure();

    /**
     * Animate your UI in for a given duration
     * @param duration      the duration of hte animation
     */
    public abstract void onAnimateIn(long duration);

    /**
     * Animate your UI out for a given duration
     * @param duration      the duration of the animation
     */
    public abstract void onAnimateOut(long duration);

    /**
     * Get the text used to display in the title when the lockscreen is used
     * when displaying a video. This is the text the user will see when trying to unlock a looping
     * video playing.
     *
     * @return      the main title text
     */
    public abstract CharSequence getTitleText();

    /**
     * Get the text used to display the initial title instructions to the user when they are
     * configuration a lockscreen from the settings/etc...
     *
     * @return      the main setup title text
     */
    public abstract CharSequence getSetupText();

    /**
     * Get the text used to prompt the user to re-enter there Lockscreen method depending
     * on the implemented UI.
     *
     * @return      the setup confirmation title text
     */
    public abstract CharSequence getConfirmationText();

    /**
     * Get the text used to prompt the user to re-try their lockscreen method due to a previously
     * incorrect attempt
     *
     * @return      the failure title text
     */
    public abstract CharSequence getFailureText();

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Submit input to the UI callback to be interfaced with the other portions of the
     * lockscreen system
     *
     * @param data      the data to submit/store/etc
     */
    protected void submitInput(byte[] data){
        if(mCallbacks != null) mCallbacks.onInput(data);
    }

    /**
     * Set the callbacks for this UI
     *
     * @param callbacks     the UI's callbacks
     */
    public void setUiCallbacks(UICallbacks callbacks){
        mCallbacks = callbacks;
    }

    /**
     * Set the Lock UI in setup mode or not
     * @param state         the setup flag from the Lockscreen parent
     */
    public void setState(LockState state){
        mState = state;
    }

    /**
     * Return whether or not the Lockscreen is in setup mode
     * @return      true if in setup, false otherwise
     */
    protected boolean isSetup(){
        return mState == LockState.SETUP || mState == LockState.CONFIRM;
    }

    /**
     * Set the Context reference for this UI component
     */
    public void setContext(Context ctx){
        mCtx = ctx;
    }

    /**
     * Get the context reference for this UI
     */
    protected Context getContext(){
        return mCtx;
    }

    protected Resources getResources(){
        return mCtx.getResources();
    }

    protected String getString(@StringRes int resId){
        return mCtx.getString(resId);
    }

    protected String getString(@StringRes int resId, Object... formatArgs){
        return mCtx.getString(resId, formatArgs);
    }

    protected int getColor(@ColorRes int resId){
        return getResources().getColor(resId);
    }

    /***********************************************************************************************
     *
     * Inner interfaces and classes
     *
     */

    /**
     * The callbacks this UI uses to communicate with the lockscreen, storage and auth systems
     */
    public static interface UICallbacks{
        public void onInput(byte[] data);
    }

}
