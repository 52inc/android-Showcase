package com.ftinc.showcase.ui.lock.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ftinc.showcase.R;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock.ui
 * Created by drew.heavner on 2/27/15.
 */
public class PinUI extends LockUI {

    /***********************************************************************************************
     *
     * Variables
     *
     */



    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent) {
        return null;
    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    @Override
    public void onReset() {

    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure() {

    }

    @Override
    public void onAnimateIn(long duration) {

    }

    @Override
    public void onAnimateOut(long duration) {

    }

    @Override
    public CharSequence getTitleText() {
        return getString(R.string.pin_lock_title);
    }

    @Override
    public CharSequence getSetupText() {
        return getString(R.string.pin_lock_setup_title);
    }

    @Override
    public CharSequence getConfirmationText() {
        return getString(R.string.pin_lock_confirmation_title);
    }

}
