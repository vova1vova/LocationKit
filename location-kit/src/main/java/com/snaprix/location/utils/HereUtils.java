package com.snaprix.location.utils;

/**
 * Created by vladimirryabchikov on 12/21/14.
 */
public class HereUtils {
    private static final String HERE_MAP_CLASS_NAME = "com.here.android.mapping.Map";

    /**
     * Are Here Maps supported in this device
     *
     * @return	true if Here Maps are available
     */
    public static boolean isHereMapsAvailable() {
        boolean available = true;
        try {
            Class.forName(HERE_MAP_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            available = false;
        }
        return available;
    }
}
