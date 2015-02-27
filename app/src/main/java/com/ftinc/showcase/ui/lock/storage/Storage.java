package com.ftinc.showcase.ui.lock.storage;

import com.ftinc.showcase.ui.lock.LockType;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock
 * Created by drew.heavner on 2/27/15.
 */
public interface Storage {

    /**
     * Deposit the input data into whatever storage method you desire
     *
     * @param input     the raw input data
     * @param type      the lock type used to create input data
     */
    public void deposit(byte[] input, LockType type);

    /**
     * Withdraw the previous input data for a given lock type
     *
     * @param type      the lock type used to store the data
     * @return          the raw input data stored in {@link #withdraw(LockType)}
     */
    public byte[] withdraw(LockType type);

}
