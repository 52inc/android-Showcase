package com.ftinc.showcase;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import com.ftinc.showcase.data.DataModule;
import com.ftinc.showcase.ui.KioskService;
import com.ftinc.showcase.ui.UiModule;
import dagger.Module;
import dagger.Provides;

/**
 * Project: Kiosk
 * Package: co.ftinc.kiosk
 * Created by drew.heavner on 2/18/15.
 */
@Module(
    includes = {
        UiModule.class,
        DataModule.class
    },
    injects = {
        ShowcaseApp.class,
        KioskService.class
    },
    library = true
)
public class ShowcaseModule {

    private Application mApp;

    /**
     * Constructor
     * @param app       the application
     */
    public ShowcaseModule(Application app){
        mApp = app;
    }

    @Provides @Singleton
    Application provideApplication(){
        return mApp;
    }

    @Provides @Singleton
    Context provideContext(){
        return mApp;
    }

    @Provides @Singleton
    Bus provideBus(){
        return new Bus(ThreadEnforcer.ANY);
    }

}
