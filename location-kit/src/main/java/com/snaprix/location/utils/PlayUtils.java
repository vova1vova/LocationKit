package com.snaprix.location.utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by vladimirryabchikov on 8/31/13.
 */
public class PlayUtils {
    public static int getPlayServicesStatus(Context context){
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
    }

    public static boolean isAvailable(Context context){
        int result = getPlayServicesStatus(context);
        switch (result) {
            case ConnectionResult.SUCCESS:
                return true;
            default:
                return false;
        }
    }
}