package com.snaprix.location;

import android.location.Location;

/**
 * Created by vladimirryabchikov on 1/11/15.
 */
public interface LocationReceiver {
    // onLocationChanged(l: Location)
    void onLocationChanged(Location l);
}