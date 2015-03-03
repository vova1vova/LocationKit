package com.snaprix.location.internal;

import android.content.Context;
import android.support.annotation.NonNull;

import com.snaprix.location.ProviderCallback;

/**
 * Created by vladimirryabchikov on 7/18/14.
 */
public interface Client {
    // startUpdates(context: Context, callback: ProviderCallback)
    /**
     * start receiving updates from this source
     * @param context
     * @param callback
     */
    void startUpdates(Context context, @NonNull ProviderCallback callback);

    // stopUpdates()
    /**
     * stop receiving updates from this source,
     * could be called without previous call to startUpdates
     */
    void stopUpdates();
}