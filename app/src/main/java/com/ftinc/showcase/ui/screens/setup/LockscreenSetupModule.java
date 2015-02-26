package com.ftinc.showcase.ui.screens.setup;

import com.ftinc.showcase.ui.UiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.screens.setup
 * Created by drew.heavner on 2/26/15.
 */
@Module(
    injects = LockscreenSetupActivity.class,
    addsTo = UiModule.class,
    complete = false
)
public class LockscreenSetupModule {

    private LockscreenSetupView mView;

    public LockscreenSetupModule(LockscreenSetupView view){
        mView = view;
    }

    @Provides @Singleton
    LockscreenSetupView provideView(){
        return mView;
    }

    @Provides @Singleton
    LockscreenSetupPresenter providePresenter(LockscreenSetupView view){
        return new LockscreenSetupPresenterImpl(view);
    }

}
