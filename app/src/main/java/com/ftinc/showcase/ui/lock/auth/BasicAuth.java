package com.ftinc.showcase.ui.lock.auth;

import android.util.Base64;

/**
 * Basic authenticator that compares the data in Base64 strings
 *
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock.auth
 * Created by drew.heavner on 3/2/15.
 */
public class BasicAuth implements Auth{
    @Override
    public boolean authenticate(byte[] input, byte[] stored) {
        String inputStr = Base64.encodeToString(input, Base64.DEFAULT);
        String storedStr = Base64.encodeToString(stored, Base64.DEFAULT);
        return inputStr.equals(storedStr);
    }
}
