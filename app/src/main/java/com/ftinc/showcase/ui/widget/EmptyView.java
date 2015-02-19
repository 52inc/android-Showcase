/*
 * Copyright (c) 52apps 2015. All rights reserved.
 */

package com.ftinc.showcase.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftinc.fontloader.FontLoader;
import com.ftinc.fontloader.Types;
import com.r0adkll.deadskunk.utils.Utils;

import com.ftinc.showcase.R;

/**
 * Project: Kiosk
 * Package: co.ftinc.kiosk.ui.widget
 * Created by drew.heavner on 2/19/15.
 */
public class EmptyView extends RelativeLayout {

    /***********************************************************************************************
     *
     * Variables
     *
     */

    private ImageView mIcon;
    private TextView mMessage;
    private ProgressBar mLoading;
    private LinearLayout mContainer;

    private int mEmptyIcon = R.drawable.ic_launcher;
    private int mAccentColor;
    private String mEmptyMessage = "You currently don't have any items";

    /***********************************************************************************************
     *
     * Constructors
     *
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseAttributes(context, attrs, defStyleAttr);
        init();
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttributes(context, attrs, defStyleAttr);
        init();
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs, 0);
        init();
    }

    public EmptyView(Context context) {
        super(context);
        init();
    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Parse XML attributes
     *
     * @param attrs     the attributes to parse
     */
    private void parseAttributes(Context context, AttributeSet attrs, int defStyle){
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.EmptyView, defStyle, 0);
        if (a == null) {
            mAccentColor = context.getResources().getColor(R.color.empty_layout_default);
            return;
        }

        // Parse attributes
        mEmptyMessage = a.getString(R.styleable.EmptyView_ev_emptyMessage);
        mAccentColor = a.getColor(R.styleable.EmptyView_ev_accentColor, context.getResources().getColor(R.color.empty_layout_default));
        mEmptyIcon = a.getResourceId(R.styleable.EmptyView_ev_emptyIcon, R.drawable.ic_launcher);
    }

    /**
     * Initialize the Empty Layout
     */
    private void init(){

        // Create the Empty Layout
        mContainer = new LinearLayout(getContext());
        mIcon = new ImageView(getContext());
        mMessage = new TextView(getContext());

        // Setup the layout
        LayoutParams containerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.setGravity(Gravity.CENTER);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        containerParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Setup the Icon
        int size = (int) Utils.dpToPx(getContext(), 64);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
        int padding = getResources().getDimensionPixelSize(R.dimen.half_padding);
        mIcon.setPadding(0, 0, 0, padding);
        mIcon.setColorFilter(mAccentColor, PorterDuff.Mode.SRC_IN);
        mIcon.setImageResource(mEmptyIcon);

        // Setup the message
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mMessage.setTextColor(mAccentColor);
        mMessage.setGravity(Gravity.CENTER);
        mMessage.setText(mEmptyMessage);
        FontLoader.applyTypeface(mMessage, Types.ROBOTO_MEDIUM);

        // Add to the layout
        mContainer.addView(mIcon, iconParams);
        mContainer.addView(mMessage, msgParams);

        // Setup loading indicator
        LayoutParams loadParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLoading = new ProgressBar(getContext());
        mLoading.setIndeterminate(true);
        mLoading.setVisibility(View.GONE);

        // Add to view
        addView(mContainer, containerParams);
        addView(mLoading, loadParams);
    }

    public void setAccentColor(int colorResId){
        mAccentColor = getResources().getColor(colorResId);
        mIcon.setColorFilter(mAccentColor, PorterDuff.Mode.SRC_IN);
        mMessage.setTextColor(mAccentColor);
    }

    public void setIcon(int resId){
        mIcon.setImageResource(resId);
    }

    public void setEmptyMessage(CharSequence message){
        mMessage.setText(message);
    }

    public void setLoading(boolean val){
        mLoading.setVisibility(val ? View.VISIBLE : View.GONE);
        mContainer.setVisibility(val ? View.GONE : View.VISIBLE);
    }

}
