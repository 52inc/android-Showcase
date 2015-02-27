package com.ftinc.showcase.ui.lock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ftinc.showcase.R;
import com.ftinc.showcase.ui.lock.auth.Auth;
import com.ftinc.showcase.ui.lock.storage.Storage;
import com.ftinc.showcase.ui.lock.ui.LockUI;

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
    FrameLayout mContentFrame;

    /*
     * Upper level components
     */
    private Context mCtx;
    private LayoutInflater mInflater;

    /*
     * Interface methods that make the lockscreen work
     */
    private LockUI mUi;
    private Storage mStorage;
    private Auth mAuthenticator;

    private boolean mIsSetup = false;

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

        // Inflate Lock UI layout and insert it
        View lockUi = mUi.onCreateView(mInflater, parent);
        mContentFrame.addView(lockUi);

        // Show the title depending on the mode
        if(mIsSetup) showSetup();
        else showTitle();

        // Return the combined layout
        return layout;
    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

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
     * Set whether or not this lockscreen is being setup in the
     * {@link com.ftinc.showcase.ui.screens.setup.LockscreenSetupActivity}
     * @param flag      the setup value
     */
    public void setIsSetup(boolean flag){
        mIsSetup = flag;
        if(mUi != null) mUi.setIsSetup(mIsSetup);
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
         */
        public Builder(Context ctx){
            mLock = new Lockscreen(ctx);
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
            mLock.setIsSetup(flag);
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
            mLock.mUi.setIsSetup(mLock.mIsSetup);
            mLock.mUi.setContext(mLock.mCtx);

            // Return the compiled lock
            return mLock;
        }

    }

}
