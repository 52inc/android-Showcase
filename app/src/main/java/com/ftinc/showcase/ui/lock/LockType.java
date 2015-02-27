package com.ftinc.showcase.ui.lock;

public enum LockType{
    NONE,
    PIN,
    PATTERN,
    PASSWORD;

    public static LockType from(int ordinal){
        return LockType.values()[ordinal];
    }

    /**
     * Create the lockscreen for this given type
     * @return      the lockscreen implementation for this type
     */
    public Lockscreen create(){
        switch (this){
            case PIN:
                return null;
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
}