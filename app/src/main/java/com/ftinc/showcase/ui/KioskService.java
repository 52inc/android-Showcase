/*
 * Copyright (c)52apps 2014. All rights reserved.
 */

package com.ftinc.showcase.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ftinc.showcase.ShowcaseApp;
import com.r0adkll.deadskunk.preferences.BooleanPreference;
import com.r0adkll.deadskunk.preferences.IntPreference;
import com.r0adkll.deadskunk.preferences.StringPreference;
import com.r0adkll.deadskunk.utils.SecurePreferences;
import com.squareup.otto.Bus;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import com.ftinc.showcase.BuildConfig;
import com.ftinc.showcase.R;
import com.ftinc.showcase.data.model.ImmersiveRecoveryEvent;
import com.ftinc.showcase.ui.locks.Lockscreen;
import com.ftinc.showcase.ui.locks.PinLockscreen;
import com.ftinc.showcase.ui.widget.TouchedFrameLayout;
import com.ftinc.showcase.utils.qualifiers.AlarmSoundPath;
import com.ftinc.showcase.utils.qualifiers.LockTimeout;
import com.ftinc.showcase.utils.qualifiers.SirenEnabled;
import com.ftinc.showcase.utils.qualifiers.SirenMessage;
import com.ftinc.showcase.utils.qualifiers.VideoAudio;
import com.ftinc.showcase.utils.qualifiers.VideoConstraints;
import com.ftinc.showcase.utils.qualifiers.VideoLock;
import timber.log.Timber;

import static com.ftinc.showcase.ui.locks.Lockscreen.*;
import static com.ftinc.showcase.ui.locks.Lockscreen.Type.PIN;

/**
 * Created by drew.heavner on 1/31/14.
 */
public class KioskService extends Service implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    /***********************************************************************************************
     *
     * Constants
     *
     */

    public static final String EXTRA_CONTENT_PATH = "fileContentPath";
    public static final String DEFAULT_ALARM_FILE = "siren.mp3";

    public static final long LOCKSCREEN_ANIM_DURATION = 300L;
    public static final long UNLOCKING_ANIM_DURATION = 600L;
    public static final long LOCKSCREEN_TIMEOUT = 5L * 1000L;

    /***********************************************************************************************
     *
     * Variables
     *
     */

    @Inject
    Bus mBus;

    @Inject
    WindowManager mWinMan;

    @Inject
    AudioManager mAudioMan;

    @Inject
    LayoutInflater mInflater;

    @Inject
    SecurePreferences mSecPrefs;

    @Inject @AlarmSoundPath
    StringPreference mAlarmSoundPath;

    @Inject @VideoLock
    IntPreference mVideoLock;

    @Inject @VideoAudio
    BooleanPreference mVideoAudioPref;

    @Inject @VideoConstraints
    BooleanPreference mVideoConstraints;

    @Inject @SirenEnabled
    BooleanPreference mSirenEnabled;

    @Inject @SirenMessage
    StringPreference mSirenMessage;

    @Inject @LockTimeout
    BooleanPreference mLockTimeout;

    private final Handler mHandler = new Handler();
    private GestureDetector mGesture;

    private MediaPlayer mPlayer;
    private SurfaceView mSurface;
    private SurfaceHolder mHolder;

    private RelativeLayout mLayout;
    private TouchedFrameLayout mLockContent;
    private Lockscreen mCurrentLock;
    private View mCurrentLockView;

    private MediaPlayer mAlarmPlayer;
    private View mAlarmView;
    private View mInstructionView;

    private String mVideoPath;

    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mVideoPath = intent.getExtras().getString(EXTRA_CONTENT_PATH);

        // Initialize the Lockscreen view
        initLockscreenView();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ShowcaseApp.get(this).inject(this);

        // Setup the gesture listener
        mGesture = new GestureDetector(this, mGestureListener);

        // Register the power state receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mPowerStateReceiver, filter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the powerstate receiver
        unregisterReceiver(mPowerStateReceiver);

        // Invalidate the Video Player
        if(mPlayer != null){
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
        }

        // Release the Alarm Player
        if(mAlarmPlayer != null){
            mAlarmPlayer.stop();
            mAlarmPlayer.reset();
            mAlarmPlayer.release();
        }

        // Invalidate the surface
        if(mHolder != null && mHolder.getSurface() != null)
            mHolder.getSurface().release();

        // Remove things from window manager
        if(mLayout != null){
            mWinMan.removeView(mLayout);
        }

        // Send Immersive recovery event
        mBus.post(new ImmersiveRecoveryEvent());

    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Initialize the Lockscreen View to be drawn
     * over the entire phone interface until the application deems it
     * UNWORTHY!!!!
     */
    private void initLockscreenView(){

        // Kill this service if we can't find the video path
        if(mVideoPath == null || mVideoPath.isEmpty()) {
            stopSelf();
            return;
        }

        // Check video constraints if setting is set
        if(mVideoConstraints.get()) {

            WindowManager window = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = window.getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);

            Integer width = size.x;
            Integer height = size.y;
            double deviceRatio = width.doubleValue() / height.doubleValue();

            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(mVideoPath);
            String vHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String vWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            Double vH = Double.valueOf(vHeight);
            Double vW = Double.valueOf(vWidth);
            double videoRatio = vW / vH;

            Timber.i("Ratio Check - [%d, %d] = %f - [%f, %f] = %f", width, height, deviceRatio, vW, vH, videoRatio);
            if(deviceRatio != videoRatio){
                Toast.makeText(this, "Video does not match the device screen ratio. Check the settings if you wish to override this.", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }
        }

        // Inflate lockscreen Layout
        mLayout = (RelativeLayout) mInflater.inflate(R.layout.service_kiosk, null, false);
        mAlarmView = mInflater.inflate(R.layout.layout_alarm_warning, null, false);

        mLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGesture.onTouchEvent(event);
            }
        });

        // Load Video Surface
        mLockContent = ButterKnife.findById(mLayout, R.id.overlay_content);
        mSurface  = ButterKnife.findById(mLayout, R.id.video_surface);

        // Setup the container touched event
        mLockContent.setOnTouchedEventListener(new TouchedFrameLayout.OnTouchedEventListener() {
            @Override
            public void onTouchedEvent(MotionEvent event) {
                // Check only if a lockscreen is active.
                if(mCurrentLock != null){
                    updateTimeout();
                }
            }
        });

        // Setup the video
        mHolder = mSurface.getHolder();
        mHolder.addCallback(this);

        // Setup the video media player
        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setLooping(true);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Get/Set Audio defaults
        if(!mVideoAudioPref.get())
            mPlayer.setVolume(0,0);

        // Setup the Siren Alarm player
        mAlarmPlayer = new MediaPlayer();
        mAlarmPlayer.setLooping(true);

        // Check for user-set alarm file
        String sirenFilePath = mAlarmSoundPath.get();

        // Ok, the user has selected an alarm file to play, attempt to use that one
        // first then resort to default as a fallback
        if(sirenFilePath != null){

            try {
                mAlarmPlayer.setDataSource(sirenFilePath);
                mAlarmPlayer.prepare();
            } catch (IOException e) {
                Timber.e(e, "Unable to load user selected alarm file, resorting to default");
                AssetFileDescriptor fd;
                try {
                    fd = getAssets().openFd(DEFAULT_ALARM_FILE);
                    mAlarmPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                    mAlarmPlayer.prepare();
                } catch (IOException e1) {
                    Timber.e(e1, "Unable to load default alarm sound.");
                }
            }

        }else {

            AssetFileDescriptor fd;
            try {
                fd = getAssets().openFd(DEFAULT_ALARM_FILE);
                mAlarmPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                mAlarmPlayer.prepare();
            } catch (IOException e) {
                Timber.e(e, "Unable to load default alarm sound.");
            }

        }

        // Create Layout Params
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                , PixelFormat.RGBA_8888);

        // Setup the system UI visibility for immersive sticky fullscreen mode
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // add to window manager
        mWinMan.addView(mLayout, params);


    }

    /**
     * The PowerState change BroadcastReceiver that receives broadcasts
     * when the user unplugs it from a power source. This will cause an alarm to
     * sound (if enabled) until the user plugs it back into a power source, or
     * until the admin of the app enters the pass code to disable the video all
     * together.
     *
     */
    private BroadcastReceiver mPowerStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case Intent.ACTION_POWER_CONNECTED:
                    Timber.i("Power Connected!");

                    // Merely pause the audio alarm
                    mAlarmPlayer.pause();
                    mLockContent.removeView(mAlarmView);

                    break;
                case Intent.ACTION_POWER_DISCONNECTED:

                    // Check preference to see if alarm is enabled
                    if(mSirenEnabled.get()) {

                        // Pull preferences for message
                        String prefMessage = mSirenMessage.get();
                        if (!TextUtils.isEmpty(prefMessage)) {
                            TextView msg = ButterKnife.findById(mAlarmView, R.id.msg);
                            msg.setText(prefMessage);
                        }

                        Timber.i("Power Disconnected!");
                        hideLockscreen();
                        mLockContent.addView(mAlarmView);

                        // Only raise the volume if not in debug mode
                        if(!BuildConfig.DEBUG) {
                            mAudioMan.setStreamVolume(AudioManager.STREAM_ALARM, mAudioMan.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                            mAlarmPlayer.setVolume(1, 1);
                        }

                        // Start playing the audio
                        mAlarmPlayer.start();

                    }
                    break;
            }
        }
    };


    /***********************************************************************************************
     *
     * Inner Classes and Interfaces
     *
     */


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // Set Display
        mPlayer.setDisplay(holder);

        // Set Data Source
        try {
            mPlayer.setDataSource(mVideoPath);
        } catch (IOException e) {
            Timber.e(e, "Error setting mPlayer data source");
            stopSelf();
        }

        // Prepare the video
        mPlayer.prepareAsync();
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) {}


    /***********************************************************************************************
     *
     *  Media Player Interfaces
     *
     */


    @Override
    public void onPrepared(MediaPlayer mp) {
        Timber.w("MediaPlayer::onPrepared(%d)", mp.getAudioSessionId());
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Timber.w("MediaPlayer::onError(%d, %d)", what, extra);

        // TODO: If a fatal error occurs, quit the service and display an error message to the user

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {}


    /***********************************************************************************************
     *
     * Lockscreen methods and interfaces
     *
     */

    /**
     * Show the lockscreen to the user based on the defined preset
     * that the user setup in the onboarding process or in the settings screen
     */
    private void showLockscreen(){

        // 1) Determine what lockscreen to show via preferences
        int ordinal = mVideoLock.get();
        Type type = Type.from(ordinal);
        switch (type){
            case PIN:
                mCurrentLock = new PinLockscreen();
                break;
            case PATTERN:

                break;
            case PASSWORD:

                break;
            case NONE:

                break;
        }

        // 2) Initialize and show the lockscreen to the user
        initLockscreen(type);

    }

    /**
     * Initialize the selected lockscreen to prepare it
     * for display over the video
     *
     * @param type      the lockscreen type
     */
    private void initLockscreen(final Type type){
        mCurrentLock.setContext(KioskService.this);
        mCurrentLock.setOnCheckInputListener(new OnCheckInputListener() {
            @Override
            public int checkInput(byte[] input) {
                String rawInput = Base64.encodeToString(input, Base64.DEFAULT);
                String secureInput = mSecPrefs.getString(type.getKey());

                Timber.i("Check Input [%s]:[%s]", rawInput, secureInput);

                if(rawInput.equals(secureInput)){
                    unlock();
                    return MATCH;
                }

                return MISMATCH;
            }
        });

        // Initialize and Show the lock
        mCurrentLockView = mCurrentLock.createView(mLockContent);
        mLockContent.addView(mCurrentLockView);
        mCurrentLock.onCreated();
        mCurrentLock.onAnimateIn(300);

        // Setup the timeout if enabled
        if(mLockTimeout.get()){
            updateTimeout();
        }

        // Animate Fade it in
        ObjectAnimator.ofFloat(mCurrentLockView, "alpha", 0, 1)
                .setDuration(300)
                .start();
    }

    /**
     * If there is a lockscreen that is being displayed, destroy it,
     * animated, then remove it from the scene
     *
     */
    private void hideLockscreen(){

        if(mCurrentLock != null){

            // Remove timeout tasks
            removeTimeout();

            // Shutdown the current lock
            mCurrentLock.onDestroy();
            mCurrentLock.onAnimateOut(LOCKSCREEN_ANIM_DURATION);

            // Animate it out
            ObjectAnimator anim = ObjectAnimator.ofFloat(mCurrentLockView, "alpha", 1, 0)
                    .setDuration(LOCKSCREEN_ANIM_DURATION);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLockContent.removeView(mCurrentLockView);
                    mCurrentLock = null;
                    mCurrentLockView = null;
                }
            });

            anim.start();

        }
    }

    /**
     * Remove the lockscreen and unlock the service by destroying
     * it with stopSelf()
     */
    private void unlock(){

        if(mCurrentLock != null) {

            mCurrentLock.onDestroy();
            mCurrentLock.onAnimateOut(UNLOCKING_ANIM_DURATION);
            ObjectAnimator anim = ObjectAnimator.ofFloat(mCurrentLockView, "alpha", 1, .20f)
                    .setDuration(UNLOCKING_ANIM_DURATION);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLockContent.removeView(mCurrentLockView);
                    mCurrentLock = null;
                    mCurrentLockView = null;
                    stopSelf();
                }
            });

            anim.start();

        }

    }

    /**
     * Update the timeout task to another {@link #LOCKSCREEN_TIMEOUT}
     * timeout duration
     */
    private void updateTimeout(){

        // Make sure previous tasks are removed
        removeTimeout();

        // Post the timeout task to occur after the specified time
        mHandler.postDelayed(mLockscreenTimeoutTask, LOCKSCREEN_TIMEOUT);

    }

    /**
     * Remove the timeout task
     */
    private void removeTimeout(){
        mHandler.removeCallbacks(mLockscreenTimeoutTask);
    }

    /**
     * The lockscreen timeout task
     */
    private Runnable mLockscreenTimeoutTask = new Runnable() {
        @Override
        public void run() {
            // When this task runs up, hide the lockscreen
            hideLockscreen();
        }
    };


    /**
     * The simple gesture detector
     */
    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(velocityY > 0){
                if(mCurrentLock == null) {
                    showLockscreen();
                }
            }else if(velocityY < 0){
                hideLockscreen();
            }
            return true;
        }
    };

}
