package com.ftinc.showcase.ui.screens.setup;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ftinc.showcase.ShowcaseApp;
import com.r0adkll.deadskunk.utils.SecurePreferences;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ftinc.showcase.R;
import com.ftinc.showcase.ui.locks.Lockscreen;
import com.ftinc.showcase.ui.locks.PinLockscreen;

/**
 * Created by r0adkll on 10/5/14.
 */
public class LockscreenSetupActivity extends ActionBarActivity {

    /***********************************************************************************************
     *
     * Constants
     *
     */

    public static final String EXTRA_LOCKSCREEN_TYPE = "extra_lockscreen_type";

    /***********************************************************************************************
     *
     * Variables
     *
     */

    @InjectView(R.id.container)     FrameLayout mContainer;

    @Inject
    SecurePreferences mSecPrefs;

    private String mType = "pin";
    private Lockscreen mLockscreen;

    private String mInput;

    /**
     * Called to create Activity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShowcaseApp.get(this).inject(this);
        setContentView(R.layout.activity_lockscreen_setup);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Load extra
        if(getIntent() != null){
            mType = getIntent().getStringExtra(EXTRA_LOCKSCREEN_TYPE);
        }

        // Exit if activity wasn't given a type
        if(mType == null)
            finish();

        // Now based on the type setup the lockscreen
        mLockscreen = createLockscreen(mType);
        if(mLockscreen != null){
            mLockscreen.setContext(this);
            mLockscreen.setOnCheckInputListener(mInputCheckListener);

            View layout = mLockscreen.createView(mContainer);
            layout.setBackground(null);
            if(layout instanceof ViewGroup) tintTextViews((ViewGroup) layout);
            mContainer.addView(layout);
            mLockscreen.onCreated();
            mLockscreen.reset("Please enter your code.");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLockscreen != null){
            mLockscreen.onDestroy();
        }
    }

    /**
     * Create the lockscreen based on the type
     *
     * @param type      the lockscreen type
     * @return          the created lockscreen
     */
    private Lockscreen createLockscreen(String type){
        switch (type){
            case "pin":
                return new PinLockscreen(true);
            case "pattern":
                break;
            case "password":
                break;
            case "gesture":
                break;
        }
        return null;
    }

    private void tintTextViews(ViewGroup parent){
        int color = getResources().getColor(R.color.textPrimary);
        int N = parent.getChildCount();
        for(int i=0; i<N; i++){
            View child = parent.getChildAt(i);
            if(child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            }else if(child instanceof ImageView){
                ((ImageView) child).getDrawable()
                        .setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }else if(child instanceof ViewGroup){
                tintTextViews((ViewGroup)child);
            }else if(child.getTag().equals("inverse")){
                child.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    /**
     * Store this code in the encoded Preferences
     *
     * @param type          the lockscreen type
     * @param encodedData   the encoded lockscreen password
     */
    private void storeCode(String type, String encodedData){

        // Store the encoded data
        mSecPrefs.put(type, encodedData);

        // Finish this activity
        finish();

    }

    /**
     * The Lockscreen's input check listener that reports the data of the lockscreen
     * input once it has been made in the Lockscreen object, here we will store the
     * data locally in Base64, then reset the lockscreen and wait for more input
     */
    private Lockscreen.OnCheckInputListener mInputCheckListener = new Lockscreen.OnCheckInputListener() {
        @Override
        public int checkInput(byte[] input) {
            final String encoded = Base64.encodeToString(input, Base64.DEFAULT);
            if(mInput == null){
                mInput = encoded;
                mLockscreen.reset(getString(R.string.lock_setup_reset_message));
            }else{
                if(mInput.equals(encoded)){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            storeCode(mType, encoded);
                        }
                    }, 500);
//                    storeCode(mType, encoded);
                    return Lockscreen.MATCH;
                }
            }

            return Lockscreen.SETUP;
        }
    };

}
