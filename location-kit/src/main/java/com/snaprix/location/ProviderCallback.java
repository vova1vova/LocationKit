package com.snaprix.location;

/**
 * all methods are called from UI thread !!!
 *
 * Created by vladimirryabchikov on 1/11/15.
 */
public interface ProviderCallback {
    // onConnected()
    void onConnected();
    // onError()
    void onError();
}