package com.ftinc.showcase.ui.lock;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftinc.showcase.R;
import com.ftinc.showcase.ui.lock.auth.Auth;
import com.ftinc.showcase.ui.lock.storage.Storage;
import com.ftinc.showcase.ui.lock.ui.LockUI;

import static com.ftinc.showcase.ui.lock.LockState.*;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock
 * Created by drew.heavner on 2/27/15.
 */
public class Lockscreen implements LockUI.UICallbacks {

    /***********************************************************************************************
     *
     * Variables
     *
     */

    @InjectView(R.id.title)
    TextView mTitle;

    @InjectView(R.id.content)
    RelativeLayout mContentFrame;

    /*
     * Upper level components
     */
    private Context mCtx;
    private LayoutInflater mInflater;
    private LockscreenCallbacks mCallbacks;

    /*
     * Interface methods that make the lockscreen work
     */
    private LockUI mUi;
    private Storage mStorage;
    private Auth mAuthenticator;

    /*
     * The type of lockscreen that this is setup to be
     */
    private LockType mType;

    /*
     * The state of the lockscreen
     */
    private LockState mState = LockState.LOCKED;

    /*
     * The staged input data in the setup process before the user input's again
     * to confirm their setup
     */
    private byte[] mStaged;

    /**
     * Hidden Internal Constructor
     *
     * @see com.ftinc.showcase.ui.lock.Lockscreen.Builder   to build this component
     * @param ctx       the context reference
     */
    private Lockscreen(Context ctx){
        mCtx = ctx;
        mInflater = LayoutInflater.from(mCtx);
    }

    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */

    /**
     * Called to signify the creation process for this lockscreen and prepare it for use
     */
    public void onCreate(){
        mUi.setUiCallbacks(this);
        mUi.onCreate();
    }

    /**
     * Called to destroy the lockscreen and finalize all it's resources
     */
    public void onDestroy(){
        mUi.onDestroy();
    }

    /**
     * Create the lockscreen UI
     *
     * @param parent
     * @return
     */
    public View onCreateView(ViewGroup parent){

        // Inflate Base Layout and Inject it
        View layout = mInflater.inflate(R.layout.layout_lockscreen, parent, false);
        ButterKnife.inject(this, layout);

        // Set background to black80 if not setup
        if(mState != SETUP) layout.setBackgroundColor(mCtx.getResources().getColor(R.color.black80));

        // Inflate Lock UI layout and insert it
        View lockUi = mUi.onCreateView(mInflater, parent);
        mContentFrame.addView(lockUi);

        // Show the title depending on the mode
        switch (mState){
            case SETUP:
                showSetup();
                mTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case CONFIRM:
                showConfirmation();
                break;
            default:
                showTitle();
        }

        // Return the combined layout
        return layout;
    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Start any special animation to add lockscreen components for pizazz
     * @param duration      the duration of the animation allowed
     */
    public void onAnimateIn(long duration){
        mUi.onAnimateIn(duration);
    }

    /**
     * Start any special animation to remove the lockscreen components for pizzaz
     *
     * @param duration      the duration of the animation allowed
     */
    public void onAnimateOut(long duration){
        mUi.onAnimateOut(duration);
    }

    /**
     * Show the main display text on the lockscreen
     *
     */
    public void showTitle(){
        mTitle.setText(mUi.getTitleText());
    }

    /**
     * Show the setup display text on the lockscreen
     */
    public void showSetup(){
        mTitle.setText(mUi.getSetupText());
    }

    /**
     * Show the setup confirmation text on the lockscreen
     */
    public void showConfirmation(){
        mTitle.setText(mUi.getConfirmationText());
    }

    /**
     * Show the failure text
     */
    public void showFailure(){
        mTitle.setText(mUi.getFailureText());
    }

    /**
     * Set whether or not this lockscreen is being setup in the
     * {@link com.ftinc.showcase.ui.screens.setup.LockscreenSetupActivity}
     * @param state      the setup value
     */
    public void setState(LockState state){
        mState = state;
        if(mUi != null) mUi.setState(state);
    }

    /**
     * Set the lockscreen callbacks
     * @param callbacks
     */
    public void setCallbacks(LockscreenCallbacks callbacks){
        mCallbacks = callbacks;
    }

    /***********************************************************************************************
     *
     * Callback Interface Methods
     *
     */

    /**
     * Called upon submitted input from the UI portion of the lockscreen to indicate the user is
     * trying to submit lock data for authentication/storage
     *
     * @param data      the input data
     */
    @Override
    public void onInput(byte[] data) {

        switch (mState){
            case SETUP:
                // 1) Stage the data
                mStaged = data;

                // 2) Proceed state to confirm mode and update the title
                mState = CONFIRM;
                showConfirmation();

                // 3) Reset the Lock UI for another round of input
                mUi.onReset();

                break;
            case CONFIRM:

                // 1) Valid staged data, use authenticator to verify the data
                if(mAuthenticator.authenticate(data, mStaged)){
                    // 2a) Success! Notify UI
                    mUi.onSuccess();

                    // 3a) Store the input data
                    mStorage.deposit(data, mType);

                    // 4a)  Notify Listeners of setup completion
                    if(mCallbacks != null) mCallbacks.onSuccess();

                }else{

                    // 2b) Failure?! Notify UI to show failure and reset itself
                    mUi.onFailure();

                    // 3b) Show failure text
                    showFailure();

                    // 4b) Notify callbacks
                    if(mCallbacks != null) mCallbacks.onFailure();

                }

                break;
            default:

                // 1) Get data from storage
                byte[] stored = mStorage.withdraw(mType);

                // 2) Authenticate input against stored
                if(mAuthenticator.authenticate(data, stored)){

                    // 2a) Show success on UI
                    mUi.onSuccess();

                    // 3a) Signal listeners of match
                    if(mCallbacks != null) mCallbacks.onSuccess();

                }else{

                    // 2b) Show failure on UI and have it reset itself
                    mUi.onFailure();

                    // 3b) Show failure text
                    showFailure();

                    // 4b) Notify callbacks
                    if(mCallbacks != null) mCallbacks.onFailure();

                }



        }

    }

    /***********************************************************************************************
     *
     * Builder
     *
     */

    /**
     * Helper class to construct lockscreen with dynamic components using the method chaining
     * Builer paradigm
     */
    public static class Builder{

        // The lockscreen that is being built
        private Lockscreen mLock;

        /**
         * Constructor
         * @param ctx       the context reference
         * @param type      the lockscreen type, this is a required parameter
         */
        public Builder(Context ctx, LockType type){
            mLock = new Lockscreen(ctx);
            mLock.mType = type;
        }

        /**
         * Set the UI component of this lockscreen your building whether it be a Pin Code UI,
         * Pattern Ui, or Password UI
         *
         * @param ui    the {@link com.ftinc.showcase.ui.lock.ui.LockUI} component of this lockscreen
         * @return      self for chaining
         */
        public Builder ui(LockUI ui){
            mLock.mUi = ui;
            return this;
        }

        /**
         * Set the Storage component of this lockscreen that will store the input data once confirmed
         * from the UI component
         *
         * @param storage       the {@link com.ftinc.showcase.ui.lock.storage.Storage} component
         *                      for storing the lock data for later verification
         * @return              self for chaining
         */
        public Builder storage(Storage storage){
            mLock.mStorage = storage;
            return this;
        }

        /**
         * Set the Authenticator component of this lockscreen that will verify the input data against
         * the stored data in the {@link com.ftinc.showcase.ui.lock.storage.Storage} component
         *
         * @param auth      the {@link com.ftinc.showcase.ui.lock.auth.Auth} component for this lockscreen
         * @return          self for chaining
         */
        public Builder authenticator(Auth auth){
            mLock.mAuthenticator = auth;
            return this;
        }

        /**
         * Set whether or not this lockscreen is being setup right now to be stored in the
         * {@link com.ftinc.showcase.ui.lock.storage.Storage} component
         *
         * @param flag      the setup flag
         * @return          self for chaining
         */
        public Builder setup(boolean flag){
            mLock.setState(flag ? LockState.SETUP : LockState.LOCKED);
            return this;
        }

        /**
         * Build the Lockscreen with all the previously specified components
         *
         * @return      the built lockscreen
         * @throws      java.lang.UnsupportedOperationException if all the components haven't been
         *              specified
         */
        public Lockscreen build(){
            // Validate all the components of the lockscreen
            if(mLock.mUi == null ||
                    mLock.mStorage == null ||
                    mLock.mAuthenticator == null){
                throw new UnsupportedOperationException("Lockscreen UI, Storage, or Authenticator has not been setup");
            }

            // Set the setup flag in the UI so it can appropriate generate itself on the mode
            // TODO: Make this solution prettier
            mLock.mUi.setState(mLock.mState);
            mLock.mUi.setContext(mLock.mCtx);

            // Return the compiled lock
            return mLock;
        }

    }

    /***********************************************************************************************
     *
     * Callbacks
     *
     */

    public static interface LockscreenCallbacks{
        public void onSuccess();
        public void onFailure();
    }

}
