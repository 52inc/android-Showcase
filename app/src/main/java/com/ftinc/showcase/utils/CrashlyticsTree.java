/*
 * Copyright (c) 52apps 2014. All rights reserved.
 */

package com.ftinc.showcase.utils;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Crashlytics Timber Logging Tree
 *
 *
 */
public class CrashlyticsTree extends Timber.Tree {

    /**
     * Constructor
     * @param ctx
     */
    public CrashlyticsTree(Context ctx){
        Fabric.with(ctx, new Crashlytics());
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        throwShade(priority, tag, message, t);
    }

    private void throwShade(int priority, String tag, String message, Throwable t) {
        if (message == null || message.length() == 0) {
            if (t != null) {
                message = Log.getStackTraceString(t);
            } else {
                // Swallow message if it's null and there's no throwable.
                return;
            }
        } else if (t != null) {
            message += "\n" + Log.getStackTraceString(t);
        }

        if (message.length() < 4000) {
            log(priority, tag, message);
        } else {
            // It's rare that the message will be this large, so we're ok with the perf hit of splitting
            // and calling Log.println N times.  It's possible but unlikely that a single line will be
            // longer than 4000 characters: we're explicitly ignoring this case here.
            String[] lines = message.split("\n");
            for (String line : lines) {
                log(priority, tag, line);
            }
        }
    }

    /**
     * Convienence function for logging to the crashlytics server
     *
     * @param priority      the logging priority
     * @param tag           the log tag
     * @param message       the log message
     */
    private void log(int priority, String tag, String message){
        Crashlytics.log(priority, tag, message);
    }
}