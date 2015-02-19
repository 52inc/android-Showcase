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
public class CrashlyticsTree implements Timber.TaggedTree {
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<String>();

    public CrashlyticsTree(Context ctx){
        Fabric.with(ctx, new Crashlytics());
    }

    @Override public void v(String message, Object... args) {
        throwShade(Log.VERBOSE, formatString(message, args), null);
    }

    @Override public void v(Throwable t, String message, Object... args) {
        throwShade(Log.VERBOSE, formatString(message, args), t);
    }

    @Override public void d(String message, Object... args) {
        throwShade(Log.DEBUG, formatString(message, args), null);
    }

    @Override public void d(Throwable t, String message, Object... args) {
        throwShade(Log.DEBUG, formatString(message, args), t);
    }

    @Override public void i(String message, Object... args) {
        throwShade(Log.INFO, formatString(message, args), null);
    }

    @Override public void i(Throwable t, String message, Object... args) {
        throwShade(Log.INFO, formatString(message, args), t);
    }

    @Override public void w(String message, Object... args) {
        throwShade(Log.WARN, formatString(message, args), null);
    }

    @Override public void w(Throwable t, String message, Object... args) {
        throwShade(Log.WARN, formatString(message, args), t);
    }

    @Override public void e(String message, Object... args) {
        throwShade(Log.ERROR, formatString(message, args), null);
    }

    @Override public void e(Throwable t, String message, Object... args) {
        throwShade(Log.ERROR, formatString(message, args), t);
    }

    @Override
    public void tag(String tag) {
        NEXT_TAG.set(tag);
    }

    private void throwShade(int priority, String message, Throwable t) {
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

        String tag = createTag();
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

    private static String createTag() {
        String tag = NEXT_TAG.get();
        if (tag != null) {
            NEXT_TAG.remove();
            return tag;
        }

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length < 6) {
            throw new IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?");
        }
        tag = stackTrace[5].getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }
        return tag.substring(tag.lastIndexOf('.') + 1);
    }

    static String formatString(String message, Object... args) {
        // If no varargs are supplied, treat it as a request to log the string without formatting.
        return args.length == 0 ? message : String.format(message, args);
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