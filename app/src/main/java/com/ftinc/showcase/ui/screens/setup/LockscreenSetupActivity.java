package com.ftinc.showcase.ui.screens.setup;

import android.app.Activity;
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
import com.ftinc.showcase.ui.model.BaseActivity;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
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
public class LockscreenSetupActivity extends BaseActivity implements LockscreenSetupView{

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

    @InjectView(R.id.container)
    FrameLayout mContainer;

    @Inject
    SecurePreferences mSecPrefs;

    @Inject
    LockscreenSetupPresenter mPresenter;

    private Lockscreen mLockscreen;
    private String mInput;

    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */

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

        mPresenter.parseExtras(savedInstanceState);

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
     * TODO: Potentially move this to the {@link com.ftinc.showcase.ui.locks.Lockscreen} class
     * @param parent
     */
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

                    // FIXME: Why the hell did I implement this again???
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            storeCode(mType, encoded);
                        }
                    }, 500);
                    return Lockscreen.MATCH;
                }
            }

            return Lockscreen.SETUP;
        }
    };




    /***********************************************************************************************
     *
     * View Methods
     *
     */

    @Override
    public void setupUI(Lockscreen lockscreen) {

        // Now based on the type setup the lockscreen
        mLockscreen = lockscreen;
        if(mLockscreen != null){
            // Put the lockscreen in setup mode
            mLockscreen.setIsSetup(true);

            // Initialize Lockscreen
            mLockscreen.setContext(this);
            mLockscreen.setOnCheckInputListener(mInputCheckListener);

            // Generate View
            View layout = mLockscreen.createView(mContainer);
            layout.setBackground(null);
            if(layout instanceof ViewGroup) tintTextViews((ViewGroup) layout);
            mContainer.addView(layout);
            mLockscreen.onCreated();
            mLockscreen.reset("Please enter your code.");
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void showSnackBar(String text) {
        Snackbar.with(this)
                .text(text)
                .swipeToDismiss(true)
                .type(SnackbarType.MULTI_LINE)
                .show(this);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void closeKeyboard() {

    }

    /***********************************************************************************************
     *
     * Base Methods
     *
     */

    @Override
    protected Object[] getModules() {
        return new Object[]{
            new LockscreenSetupModule(this)
        };
    }
}
