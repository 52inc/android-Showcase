package com.ftinc.showcase.ui.screens.setup;

import android.content.Intent;
import android.os.Bundle;

import com.ftinc.showcase.ui.locks.Lockscreen;

import static com.ftinc.showcase.ui.locks.Lockscreen.Type.*;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.screens.setup
 * Created by drew.heavner on 2/26/15.
 */
public class LockscreenSetupPresenterImpl implements LockscreenSetupPresenter {

    private LockscreenSetupView mView;
    private Lockscreen.Type mType;

    /**
     * Constructor
     * @param mView
     */
    public LockscreenSetupPresenterImpl(LockscreenSetupView mView) {
        this.mView = mView;
    }

    /***********************************************************************************************
     *
     * Presenter Methods
     *
     */

    @Override
    public void parseExtras(Bundle icicle) {

        Intent intent = mView.getActivity().getIntent();
        if(intent != null){

            // Parse the lockscreen setup type here
            int typeOrdinal = intent.getIntExtra(LockscreenSetupActivity.EXTRA_LOCKSCREEN_TYPE, NONE.ordinal());
            mType = Lockscreen.Type.from(typeOrdinal);

        }

        if(icicle != null){

            // Parse the lockscreen setup type here
            int typeOrdinal = icicle.getInt(LockscreenSetupActivity.EXTRA_LOCKSCREEN_TYPE, NONE.ordinal());
            mType = Lockscreen.Type.from(typeOrdinal);

        }

        // Check type
        if(mType == NONE){
            mView.getActivity().finish();
            return;
        }

        // Inflate UI
        mView.setupUI(mType.create());

    }

    @Override
    public void saveInstanceState(Bundle out) {

    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */



}
