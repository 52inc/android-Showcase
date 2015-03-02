package com.ftinc.showcase.ui.lock.auth;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock
 * Created by drew.heavner on 2/27/15.
 */
public interface Auth {

    /**
     * Authenticate a given raw input against the raw stored data
     * created by a {@link com.ftinc.showcase.ui.lock.storage.Storage} interface
     *
     * @param input     the raw input to check with
     * @param stored    the raw data to check against
     * @return          true if authenticated, false otherwise
     */
    public boolean authenticate(byte[] input, byte[] stored);


}
