package com.ftinc.showcase;

import android.app.Application;
import android.app.Service;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.r0adkll.postoffice.PostOffice;
import com.r0adkll.postoffice.model.Design;
import com.r0adkll.postoffice.model.Stamp;

import com.ftinc.showcase.utils.CrashlyticsTree;
import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;
import ollie.Ollie;
import timber.log.Timber;

/**
 * Created by drew.heavner on 2/27/14.
 */
public class ShowcaseApp extends Application {

    public static final String DB_NAME = "Showcase.db";
    public static final int DB_VERSION = 1;

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {

        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
            Timber.plant(new CrashlyticsTree(this));
        }else{
            Timber.plant(new CrashlyticsTree(this));
        }

        // Initialize PostOffice
        Stamp stamp = new Stamp.Builder(this)
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .setDesign(Design.MATERIAL_LIGHT)
                .setThemeColorResource(R.color.accent)
                .build();

        PostOffice.lick(stamp);

        // Intialize Ollie
        Ollie.with(this)
             .setName(DB_NAME)
             .setVersion(DB_VERSION)
             .setLogLevel(BuildConfig.DEBUG ? Ollie.LogLevel.FULL : Ollie.LogLevel.NONE)
             .init();

        buildObjectGraphAndInject();

    }

    public void buildObjectGraphAndInject(){
        objectGraph = ObjectGraph.create(new Object[]{
            new ShowcaseModule(this)
        });
        objectGraph.inject(this);
    }

    /**
     * Create a scoped object graph
     *
     * @param modules       the list of modules to add to the scope
     * @return              the scoped graph
     */
    public ObjectGraph createScopedGraph(Object... modules){
        return objectGraph.plus(modules);
    }

    /**
     * Inject an object with the object graph
     */
    public void inject(Object o){
        objectGraph.inject(o);
    }

    /**
     * Get a reference to the Application
     *
     * @param ctx       the context
     *
     * @return          the ChipperApp reference
     */
    public static ShowcaseApp get(Context ctx){
        return (ShowcaseApp) ctx.getApplicationContext();
    }

    /**
     * Get a reference to this application with a service
     * object
     *
     * @param ctx
     * @return
     */
    public static ShowcaseApp get(Service ctx){
        return (ShowcaseApp) ctx.getApplication();
    }

}
