package com.ftinc.showcase.ui.lock.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eftimoff.patternview.PatternView;
import com.ftinc.showcase.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock.ui
 * Created by drew.heavner on 3/2/15.
 */
public class PatternUI extends LockUI implements PatternView.OnPatternDetectedListener {


    @InjectView(R.id.patternView)
    PatternView mPatternView;

    /**
     * Default Constructor
     */
    public PatternUI(){}


    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */


    @Override
    public void onCreate() {

        // Set Listener
        Timber.i("Setting up PatternView detection listener");
        mPatternView.setOnPatternDetectedListener(this);

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.layout_pattern_ui, parent, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onReset() {
        mPatternView.clearPattern();
    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

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

    /***********************************************************************************************
     *
     * Title Methods
     *
     */

    @Override
    public CharSequence getTitleText() {
        return getString(R.string.pattern_lock_title);
    }

    @Override
    public CharSequence getSetupText() {
        return getString(R.string.pattern_lock_setup_title);
    }

    @Override
    public CharSequence getConfirmationText() {
        return getString(R.string.pattern_lock_confirmation_title);
    }

    @Override
    public CharSequence getFailureText() {
        return getString(R.string.pattern_lock_failure_title);
    }

    /***********************************************************************************************
     *
     * Pattern Listeners
     *
     */

    @Override
    public void onPatternDetected() {
        Timber.i("onPatternDetected ");

    }
}
