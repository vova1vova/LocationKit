package com.snaprix.location.internal;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.snaprix.location.ProviderCallback;
import com.snaprix.location.utils.LocationKitLogger;
import com.snaprix.location.utils.PlayErrorHandler;

/**
 * Created by vladimirryabchikov on 1/12/15.
 */
public class PlayClient implements Client {
    protected boolean SHOW_LOGS = LocationKitLogger.isShowLogs();
    protected final String TAG = getClass().getSimpleName();

    private final PlayErrorHandler mErrorHandler = new PlayErrorHandler();

    private Listener mListener;
    private GoogleApiClient mGoogleApiClient;
    private Activity mErrorResolveActivity;

    @Override
    public void startUpdates(Context context, @NonNull ProviderCallback callback) {
        mListener = new Listener(callback);

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
        builder.addConnectionCallbacks(mListener)
               .addOnConnectionFailedListener(mListener);
        buildClient(builder);
        mGoogleApiClient = builder.build();
        mGoogleApiClient.connect();
    }

    @Override
    public void stopUpdates() {
        if (isConnected()) {
            try {
                mGoogleApiClient.disconnect();
            } catch (Exception e) {
                // connection check is not enough, DeadObjectException still could happen
                // https://www.crashlytics.com/electrounion/android/apps/ru.multigo.multitoplivo/issues/54400046e3de5099ba10a31e
                if (SHOW_LOGS) e.printStackTrace();
            }
        }

        mListener.destroy();
        mErrorResolveActivity = null;
    }

    protected final void setErrorResolveActivity(Activity errorResolveActivity) {
        mErrorResolveActivity = errorResolveActivity;
    }

    protected final boolean isConnected(){
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    /**
     * check isConnected() before relying on this method
     * onClientConnected() is the single exception, when this check is redundant
     *
     * @return
     */
    protected final GoogleApiClient getGoogleApiClient() {
        if (!isConnected()){
            if (LocationKitLogger.getCrashHandler() != null) {
                LocationKitLogger.getCrashHandler().logFatalException(new IllegalStateException("getGoogleApiClient is called without prior check to isConnected(), isConnected() == false"));
            }
        }
        return mGoogleApiClient;
    }

    protected void buildClient(GoogleApiClient.Builder builder){

    }

    /**
     * method is called when mGoogleApiClient is connected
     * and user has not requested stopUpdates yet
     */
    protected void onClientConnected(){

    }




    private class Listener implements GoogleApiListener {
        private ProviderCallback mListener;

        private Listener(ProviderCallback listener) {
            mListener = listener;
        }

        @Override
        public void onConnected(Bundle bundle) {
            if (SHOW_LOGS) Log.d(TAG, "ClientListenerProxy Connected");

            // Register the listener to receive location updates
            if (isConnected()) {
                if (mListener != null) {

                    onClientConnected();
                    mListener.onConnected();
                } else {
                    // connection occurred after user requested stopUpdates,
                    // disconnect now, no data is necessary
                    mGoogleApiClient.disconnect();
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            if (SHOW_LOGS) Log.d(TAG, "ClientListenerProxy Disconnected. Please re-connect.");
            // Destroy the current location client
            mGoogleApiClient = null;

            if (mListener != null) {
                mListener.onError();
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            if (SHOW_LOGS) Log.d(TAG, String.format("ClientListenerProxy onConnectionFailed result=%s", result));

            if (mErrorResolveActivity != null) {
                mErrorHandler.handleError(result, mErrorResolveActivity);
            }

            if (mListener != null) {
                mListener.onError();
            }
        }

        void destroy(){
            mListener = null;
        }
    }
}