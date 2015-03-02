package com.ftinc.showcase.ui;

import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.WindowManager;

import javax.inject.Singleton;

import com.ftinc.showcase.ui.lock.storage.SecurePrefStorage;
import com.ftinc.showcase.ui.screens.settings.SettingsActivity;
import com.ftinc.showcase.ui.screens.setup.LockscreenSetupActivity;
import dagger.Module;
import dagger.Provides;

/**
 * Project: Kiosk
 * Package: co.ftinc.kiosk.ui
 * Created by drew.heavner on 2/18/15.
 */
@Module(
    injects = {
        SettingsActivity.SettingsFragment.class,
        SecurePrefStorage.class
    },
    library = true,
    complete = false
)
public class UiModule {

    @Provides @Singleton
    WindowManager provideWindowManager(Context ctx){
        return (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
    }

    @Provides @Singleton
    AudioManager provideAudioManager(Context ctx){
        return (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
    }

    @Provides @Singleton
    LayoutInflater provideLayoutInflater(Context ctx){
        return LayoutInflater.from(ctx);
    }

}
