/*
 * Copyright (c) 52apps 2014. All rights reserved.
 */

package com.ftinc.showcase.ui.model;

import android.app.Activity;

/**
 * This is the base 'View' interface for all MVP representations for this
 * UI
 *
 * Project: HealthyEating
 * Package: co.ftinc.healthyeating.ui.model
 * Created by drew.heavner on 12/5/14.
 */
public interface IBaseView {

    public Activity getActivity();

    public void showSnackBar(String text);

    public void showLoading();
    public void hideLoading();
    public void closeKeyboard();

}
