package com.ftinc.showcase.ui.screens.setup;

import android.os.Bundle;

import com.ftinc.showcase.ui.lock.LockType;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.screens.setup
 * Created by drew.heavner on 2/26/15.
 */
public interface LockscreenSetupPresenter {

    public void parseExtras(Bundle icicle);

    public void saveInstanceState(Bundle out);

    public LockType getType();

}
