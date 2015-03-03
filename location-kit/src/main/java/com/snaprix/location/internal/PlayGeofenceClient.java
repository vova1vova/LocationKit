package com.snaprix.location.internal;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.snaprix.location.ProviderCallback;

import java.util.List;

/**
 * Created by vladimirryabchikov on 7/18/14.
 */
public class PlayGeofenceClient extends PlayClient {
    private static final String TAG = "GeofenceLocationManager";

    private REQUEST_TYPE mType;

    private List<Geofence> mAddGeofenceList;
    private Intent mTransitionIntent;
    private PendingIntent mTransitionPendingIntent;
    private List<String> mRemoveGeofenceList;


    // +add(context: Context, geofences: Geofence[*], intent: Intent, errorResolveActivity: Activity)
    public static void add(Context context, List<Geofence> geofences, Intent intent, @Nullable Activity errorResolveActivity) {
        PlayGeofenceClient client = new PlayGeofenceClient();
        client.setErrorResolveActivity(errorResolveActivity);
        client.mType = REQUEST_TYPE.ADD;
        client.mAddGeofenceList = geofences;
        client.mTransitionIntent = intent;
        client.connect(context);
    }

    // +remove(context: Context, ids: String[*])
    public static void remove(Context context, List<String> ids) {
        PlayGeofenceClient client = new PlayGeofenceClient();
        client.mType = REQUEST_TYPE.REMOVE;
        client.mRemoveGeofenceList = ids;

        client.connect(context);
    }



    private PlayGeofenceClient() {

    }

    @Override
    public void startUpdates(Context context, @NonNull ProviderCallback callback) {
        // create intent before calling super type method
        // to have it, when onClientConnected will be called
        mTransitionPendingIntent = createTransitionPendingIntent(context);

        super.startUpdates(context, callback);
    }

    @Override
    protected void buildClient(GoogleApiClient.Builder builder) {
        super.buildClient(builder);
        builder.addApi(LocationServices.API);
    }

    @Override
    protected void onClientConnected() {
        super.onClientConnected();

        final ResultCallback<Status> resultCallback = new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (SHOW_LOGS) Log.v(TAG, String.format("resultCallback onResult status=%s",
                        status));

                // If adding was successful
                if (status.isSuccess()) {
                    /*
                     * Handle successful addition of geofences here.
                     * You can send out a broadcast intent or update the UI.
                     */
                } else {
                    // If adding failed
                    /*
                     * Report errors here.
                     * You can log the error using Log.e() or update
                     * the UI.
                     */
                }

                /*
                 * Disconnect the location client regardless of the
                 * request status, and indicate that a request is no
                 * longer in progress
                 */
                stopUpdates();
            }
        };

        switch (mType) {
            case ADD: {
                PendingIntent pendingIntent = getTransitionPendingIntent();
                GeofencingRequest request = new GeofencingRequest.Builder()
                        .addGeofences(mAddGeofenceList)
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .build();
                PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(getGoogleApiClient(), request, pendingIntent);
                result.setResultCallback(resultCallback);
                break;
            }
            case REMOVE: {
                PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(getGoogleApiClient(), mRemoveGeofenceList);
                result.setResultCallback(resultCallback);
                break;
            }
            default:
                throw new IllegalStateException(String.format("onClientConnected mType=%s", mType));
        }
    }

    private void connect(Context context){
        startUpdates(context, new ProviderCallback() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void disconnect(){
        stopUpdates();
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    @Nullable
    private PendingIntent createTransitionPendingIntent(Context context) {
        if (mTransitionIntent == null) {
            // it's ok to not have intent, because it's needed only in ADD request
            return null;
        }

        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                context,
                0,
                mTransitionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private PendingIntent getTransitionPendingIntent(){
        if (mTransitionPendingIntent == null) {
            throw new NullPointerException("mTransitionPendingIntent == null");
        }

        return mTransitionPendingIntent;
    }


    // Enum type for controlling the type of requested
    private enum REQUEST_TYPE {ADD, REMOVE}
}