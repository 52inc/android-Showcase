package com.ftinc.showcase.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ftinc.showcase.BuildConfig;
import com.ftinc.showcase.ui.locks_old.Lockscreen;
import com.r0adkll.deadskunk.preferences.BooleanPreference;
import com.r0adkll.deadskunk.preferences.IntPreference;
import com.r0adkll.deadskunk.preferences.StringPreference;
import com.r0adkll.deadskunk.utils.SecurePreferences;

import javax.inject.Singleton;

import com.ftinc.showcase.utils.qualifiers.AlarmSoundName;
import com.ftinc.showcase.utils.qualifiers.AlarmSoundPath;
import com.ftinc.showcase.utils.qualifiers.LockTimeout;
import com.ftinc.showcase.utils.qualifiers.Onboarding;
import com.ftinc.showcase.utils.qualifiers.SirenEnabled;
import com.ftinc.showcase.utils.qualifiers.SirenMessage;
import com.ftinc.showcase.utils.qualifiers.VideoAudio;
import com.ftinc.showcase.utils.qualifiers.VideoConstraints;
import com.ftinc.showcase.utils.qualifiers.VideoLock;
import dagger.Module;
import dagger.Provides;

/**
 * Project: Kiosk
 * Package: co.ftinc.kiosk.data
 * Created by drew.heavner on 2/18/15.
 */
@Module(
    library = true,
    complete = false
)
public class DataModule {

    public static final String PREFS_SECURE_NAME = "Kiosk.secure";

    public static final String PREF_SHOW_ONBOARDING = "pref_show_onboarding";
    public static final String PREF_ALARM_SOUND_PATH = "pref_alarm_sound_file";
    public static final String PREF_ALARM_SOUND_NAME = "pref_alarm_sound_name";
    public static final String PREF_VIDEO_LOCK = "pref_video_lock";
    public static final String PREF_VIDEO_AUDIO_ENABLED = "pref_video_audio";
    public static final String PREF_SIREN_ENABLED = "pref_siren";
    public static final String PREF_SIREN_MESSAGE = "pref_siren_message";
    public static final String PREF_VIDEO_CONSTRAINTS = "pref_video_constraints";
    public static final String PREF_LOCK_TIMEOUT = "pref_lock_timeout";

    private static final String SAUCE = BuildConfig.SECURE_KEY;
    private static final String FLAVOR = BuildConfig.SECURE_SALT;

    @Provides @Singleton
    SecurePreferences provideSecurePreferences(Context ctx){
        return new SecurePreferences(ctx, PREFS_SECURE_NAME, SAUCE, FLAVOR, true);
    }

    @Provides @Singleton
    SharedPreferences provideDefaultPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Provides @Singleton @Onboarding
    BooleanPreference provideShowOnboardingPreference(SharedPreferences prefs){
        return new BooleanPreference(prefs, PREF_SHOW_ONBOARDING, true);
    }

    @Provides @Singleton @AlarmSoundName
    StringPreference provideAlarmSoundNamePreference(SharedPreferences prefs){
        return new StringPreference(prefs, PREF_ALARM_SOUND_NAME);
    }

    @Provides @Singleton @AlarmSoundPath
    StringPreference provideAlarmSoundPathPreference(SharedPreferences prefs){
        return new StringPreference(prefs, PREF_ALARM_SOUND_PATH);
    }

    @Provides @Singleton @VideoLock
    IntPreference provideVideoLockPreference(SharedPreferences prefs){
        return new IntPreference(prefs, PREF_VIDEO_LOCK, Lockscreen.Type.NONE.ordinal());
    }

    @Provides @Singleton @VideoAudio
    BooleanPreference provideVideoAudioPreference(SharedPreferences prefs){
        return new BooleanPreference(prefs, PREF_VIDEO_AUDIO_ENABLED, false);
    }

    @Provides @Singleton @VideoConstraints
    BooleanPreference provideVideoConstraintsPreference(SharedPreferences prefs){
        return new BooleanPreference(prefs, PREF_VIDEO_CONSTRAINTS, false);
    }

    @Provides @Singleton @SirenEnabled
    BooleanPreference provideSirenEnabledPreference(SharedPreferences prefs){
        return new BooleanPreference(prefs, PREF_SIREN_ENABLED, false);
    }

    @Provides @Singleton @SirenMessage
    StringPreference provideSirenMessagePreference(SharedPreferences prefs){
        return new StringPreference(prefs, PREF_SIREN_MESSAGE);
    }

    @Provides @Singleton @LockTimeout
    BooleanPreference provideLockTimeoutPreference(SharedPreferences prefs){
        return new BooleanPreference(prefs, PREF_LOCK_TIMEOUT, true);
    }

}
