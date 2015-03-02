package com.ftinc.showcase.ui.screens.setup;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ftinc.showcase.ShowcaseApp;
import com.ftinc.showcase.ui.lock.LockState;
import com.ftinc.showcase.ui.lock.Lockscreen;
import com.ftinc.showcase.ui.model.BaseActivity;
import com.ftinc.showcase.utils.qualifiers.VideoLock;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.r0adkll.deadskunk.preferences.IntPreference;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ftinc.showcase.R;

/**
 * Created by r0adkll on 10/5/14.
 */
public class LockscreenSetupActivity extends BaseActivity implements LockscreenSetupView, Lockscreen.LockscreenCallbacks {

    /***********************************************************************************************
     *
     * Constants
     *
     */

    public static final String EXTRA_LOCKSCREEN_TYPE = "extra_lockscreen_type";

    private static final long SUCCESS_DELAY = 500L;

    /***********************************************************************************************
     *
     * Variables
     *
     */

    @InjectView(R.id.container)
    FrameLayout mContainer;

    @Inject
    LockscreenSetupPresenter mPresenter;

    @Inject @VideoLock
    IntPreference mVideoLock;

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

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * TODO: Potentially move this to the {@link com.ftinc.showcase.ui.lock.Lockscreen} class
     * @param parent
     */
    private void tintTextViews(ViewGroup parent){
        int color = getResources().getColor(R.color.textLockSetup);
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


    /***********************************************************************************************
     *
     * Lockscreen Callbacks
     *
     */

    @Override
    public void onSuccess() {
        // At this point, we have a succesfully setup the lockscreen to the point of storage, so
        // we need to indicate this in the system preferences at some point
        mVideoLock.set(mPresenter.getType().ordinal());

        // Now exit the setup activity after a delay so we can see the success animation
        // TODO: Evaluate the merit of putting the delay in the Lockscreen mechanism itself
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, SUCCESS_DELAY);
    }

    @Override
    public void onFailure() {
        // Do Nothing
    }


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
            mLockscreen.setState(LockState.SETUP);

            // Initialize Lockscreen
            mLockscreen.setCallbacks(this);

            // Generate View
            View layout = mLockscreen.onCreateView(mContainer);
            if(layout instanceof ViewGroup) tintTextViews((ViewGroup) layout);

            mContainer.addView(layout);
            mLockscreen.onCreate();
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
