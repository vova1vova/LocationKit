package com.snaprix.location.internal;

import android.location.Location;
import android.support.annotation.NonNull;

import com.snaprix.location.LocationReceiver;

/**
 * Created by vladimirryabchikov on 1/12/15.
 */
public interface LocationClient extends Client {
    // setLocationReceiver(receiver: Receiver)
    void setLocationReceiver(@NonNull LocationReceiver receiver);

    // mockLocation(l: Location)
    void mockLocation(Location l);
}