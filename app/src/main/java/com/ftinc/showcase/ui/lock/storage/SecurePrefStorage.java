package com.ftinc.showcase.ui.lock.storage;

import android.content.Context;
import android.util.Base64;

import com.ftinc.kit.preferences.SecurePreferences;
import com.ftinc.showcase.ShowcaseApp;
import com.ftinc.showcase.ui.lock.LockType;

import javax.inject.Inject;

/**
 * Basic storage mechanism that stores the data in base64 strings using {@link android.util.Base64}
 * in the Secure AES-256 SharedPreference wrapper: {@link SecurePreferences}
 *
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock.storage
 * Created by drew.heavner on 3/2/15.
 */
public class SecurePrefStorage implements Storage{

    @Inject
    SecurePreferences mSecPrefs;

    /**
     * Constructor
     *
     * @param ctx       the context reference to inject with
     */
    public SecurePrefStorage(Context ctx){
        ShowcaseApp.get(ctx).inject(this);
    }


    @Override
    public void deposit(byte[] input, LockType type) {
        String encoded = Base64.encodeToString(input, Base64.DEFAULT);
        mSecPrefs.put(type.getKey(), encoded);
    }

    @Override
    public byte[] withdraw(LockType type) {
        String stored = mSecPrefs.getString(type.getKey());
        return Base64.decode(stored, Base64.DEFAULT);
    }
}
