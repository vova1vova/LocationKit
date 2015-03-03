package com.snaprix.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.snaprix.location.internal.PlayGeofenceClient;

import java.util.List;

/**
 * Created by vladimirryabchikov on 1/12/15.
 */
public class GeofenceProvider {
    // +add(context: Context, geofences: Geofence[*], intent: Intent, errorResolveActivity: Activity)
    public static void add(Context context, List<Geofence> geofences, Intent intent, @Nullable Activity errorResolveActivity) {
        PlayGeofenceClient.add(context, geofences, intent, errorResolveActivity);
    }

    // +remove(context: Context, ids: String[*])
    public static void remove(Context context, List<String> ids) {
        PlayGeofenceClient.remove(context, ids);
    }
}