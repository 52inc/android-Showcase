package com.ftinc.showcase.ui.screens.home;

import com.r0adkll.deadskunk.preferences.StringPreference;

import javax.inject.Singleton;

import com.ftinc.showcase.ui.UiModule;
import com.ftinc.showcase.utils.qualifiers.VideoLock;
import dagger.Module;
import dagger.Provides;

/**
 * Project: Kiosk
 * Package: co.ftinc.kiosk.ui.screens.home
 * Created by drew.heavner on 2/18/15.
 */
@Module(
    injects = HomeActivity.class,
    addsTo = UiModule.class,
    complete = false
)
public class HomeModule {

    private HomeView mView;

    public HomeModule(HomeView view){
        mView = view;
    }

    @Provides @Singleton
    HomeView provideView(){
        return mView;
    }

    @Provides @Singleton
    HomePresenter providePresenter(HomeView view,
                                   @VideoLock StringPreference vidLockPref){
        return new HomePresenterImpl(view, vidLockPref);
    }

}
