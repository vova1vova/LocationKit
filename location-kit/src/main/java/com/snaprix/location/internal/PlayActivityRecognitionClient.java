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
import com.google.android.gms.location.ActivityRecognition;
import com.snaprix.location.ProviderCallback;
import com.snaprix.location.utils.Time;

/**
 * Created by vladimirryabchikov on 1/12/15.
 */
public class PlayActivityRecognitionClient extends PlayClient {
    private static final long DETECTION_INTERVAL_MILLISECONDS = 20 * Time.MILLISECONDS.SECOND;

    private REQUEST_TYPE mType;

    private Intent mActivityRecognitionIntent;
    private PendingIntent mActivityRecognitionPendingIntent;

    // +add(context: Context, intent: Intent, errorResolveActivity: Activity)
    public static void add(Context context, Intent intent, @Nullable Activity errorResolveActivity) {
        PlayActivityRecognitionClient client = new PlayActivityRecognitionClient();
        client.setErrorResolveActivity(errorResolveActivity);

        client.mType = REQUEST_TYPE.ADD;
        client.mActivityRecognitionIntent = intent;

        client.connect(context);
    }

    // +remove(context: Context)
    public static void remove(Context context, Intent intent) {
        PlayActivityRecognitionClient client = new PlayActivityRecognitionClient();

        client.mType = REQUEST_TYPE.REMOVE;
        client.mActivityRecognitionIntent = intent;

        client.connect(context);
    }

    @Override
    public void startUpdates(Context context, @NonNull ProviderCallback callback) {
        // create intent before calling super type method
        // to have it, when onClientConnected will be called
        mActivityRecognitionPendingIntent = getActivityRecognitionPendingIntent(context);

        super.startUpdates(context, callback);
    }

    @Override
    protected void buildClient(GoogleApiClient.Builder builder) {
        super.buildClient(builder);
        builder.addApi(ActivityRecognition.API);
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

        switch (mType){
            case ADD: {
                // enable activity recognition
                PendingResult<Status> result = ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                        getGoogleApiClient(), DETECTION_INTERVAL_MILLISECONDS, mActivityRecognitionPendingIntent);
                result.setResultCallback(resultCallback);
                break;
            }
            case REMOVE: {
                // disable activity recognition
                PendingResult<Status> result = ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                        getGoogleApiClient(), mActivityRecognitionPendingIntent);
                result.setResultCallback(resultCallback);
                break;
            }
            default:
                throw new UnsupportedOperationException("unknown type " + mType);
        }
    }

    private void connect(Context context){
        // do not use this callbacks, use onClientConnected() method instead
        final ProviderCallback dummyListener = new ProviderCallback() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onError() {

            }
        };

        startUpdates(context, dummyListener);
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a activity recognition event occurs.
     */
    private PendingIntent getActivityRecognitionPendingIntent(Context context) {
        if (mActivityRecognitionIntent == null) {
            throw new NullPointerException("mActivityRecognitionIntent could not be null");
        }

        return PendingIntent.getService(
                context,
                0,
                mActivityRecognitionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Enum type for controlling the type of requested
    private enum REQUEST_TYPE {ADD, REMOVE}
}