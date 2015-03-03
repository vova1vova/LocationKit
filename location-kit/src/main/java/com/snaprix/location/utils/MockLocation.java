package com.snaprix.location.utils;

import android.location.Location;
import android.os.Build;
import android.os.SystemClock;

/**
 * helper class, contains various location objects
 *
 * Created by vladimirryabchikov on 4/21/14.
 */
public class MockLocation {
    public static Location MOSCOW;
    public static Location BRUSSELS;

    // do not change this code, it is the code that Location Services puts into the Location objects it sends out
    private static final String MOCK_PROVIDER = "fused";
    private static final float ACCURACY = 3.0f;

    static {
        MOSCOW = buildLocation(55.7558194, 37.6180175);
        BRUSSELS = buildLocation(50.8507357, 4.3517719);
    }

    private static Location buildLocation(double lat, double lng){
        Location l = new Location(MOCK_PROVIDER);
        l.setLatitude(lat);
        l.setLongitude(lng);
        l.setAccuracy(ACCURACY);
        l.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1){

        } else {
            l.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        return l;
    }
}
