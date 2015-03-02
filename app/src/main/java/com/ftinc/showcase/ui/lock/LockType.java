package com.ftinc.showcase.ui.lock;

import android.content.Context;

import com.ftinc.showcase.ui.lock.auth.BasicAuth;
import com.ftinc.showcase.ui.lock.storage.SecurePrefStorage;
import com.ftinc.showcase.ui.lock.ui.PinUI;

public enum LockType{
    NONE("None"),
    PIN("PIN"),
    PATTERN("Pattern"),
    PASSWORD("Password");

    // The proper name
    private final String mName;

    /**
     * Constructor
     * @param name      the proper name for this enum
     */
    LockType(String name){
        mName = name;
    }

    public static LockType from(int ordinal){
        return LockType.values()[ordinal];
    }

    /**
     * Create the lockscreen for this given type
     * @return      the lockscreen implementation for this type
     */
    public Lockscreen create(Context ctx){
        switch (this){
            case PIN:
                return new Lockscreen.Builder(ctx, this)
                        .ui(new PinUI())
                        .storage(new SecurePrefStorage(ctx))
                        .authenticator(new BasicAuth())
                        .build();

            case PATTERN:
                return null;
            case PASSWORD:
                return null;
            default:
                return null;
        }
    }

    /**
     * Get this lockscreen's storage key
     */
    public String getKey(){
        return String.format("lockscreen_key_%s", toString());
    }

    /**
     * Get the proper name for this enum
     * @return      the enum's proper name
     */
    public String getName(){
        return mName;
    }

}