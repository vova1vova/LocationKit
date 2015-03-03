package com.snaprix.location.internal;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.snaprix.location.LocationReceiver;
import com.snaprix.location.ProviderCallback;
import com.snaprix.location.utils.MockLocation;
import com.snaprix.location.utils.Time;

/**
 * Created by vladimirryabchikov on 7/18/14.
 */
public class PlayLocationClient extends PlayClient implements LocationClient {
    private LocationReceiver mLocationReceiver;

    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;

    private boolean mHasParams;
    private int mRequestPriority;
    private long mIntervalMilliseconds;
    private long mFastestIntervalMilliseconds;

    public PlayLocationClient(@Nullable Activity errorResolveActivity) {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (mLocationReceiver != null) {
                    mLocationReceiver.onLocationChanged(location);
                }
            }
        };

        setErrorResolveActivity(errorResolveActivity);
    }

    @Override
    public void startUpdates(Context context, @NonNull ProviderCallback callback) {
        // create request object before calling super type method
        // to have it, when onClientConnected will be called
        if (mHasParams) {
            mLocationRequest = LocationRequest.create()
                    .setPriority(mRequestPriority)
                    .setInterval(mIntervalMilliseconds)
                    .setFastestInterval(mFastestIntervalMilliseconds);
        } else {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(30 * Time.MILLISECONDS.SECOND)
                    .setFastestInterval(15 * Time.MILLISECONDS.SECOND);
        }

        super.startUpdates(context, callback);
    }

    @Override
    protected void buildClient(GoogleApiClient.Builder builder) {
        super.buildClient(builder);
        builder.addApi(LocationServices.API);
    }


    @Override
    public void stopUpdates(){
        if (isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(), mLocationListener);
        }

        mLocationReceiver = null;
        mHasParams = false;

        super.stopUpdates();
    }

    @Override
    public void setLocationReceiver(LocationReceiver receiver) {
        mLocationReceiver = receiver;
    }

    @Override
    public void mockLocation(Location l) {
        if (isConnected()) {
            LocationServices.FusedLocationApi.setMockMode(getGoogleApiClient(), true);
            LocationServices.FusedLocationApi.setMockLocation(getGoogleApiClient(), l);
        }
    }

    public void setParams(int requestPriority, long intervalMilliseconds, long fastestIntervalMilliseconds){
        mRequestPriority = requestPriority;
        mIntervalMilliseconds = intervalMilliseconds;
        mFastestIntervalMilliseconds = fastestIntervalMilliseconds;

        mHasParams = true;
    }

    @Override
    protected void onClientConnected() {
        super.onClientConnected();

        LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(), mLocationRequest, mLocationListener);
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
        if (lastLocation != null) {
            mLocationListener.onLocationChanged(lastLocation);
        }

        if (SHOW_LOGS) {
            final boolean IS_EMULATOR = Build.FINGERPRINT.contains("generic");

            Log.v(TAG, String.format("onConnected IS_EMULATOR=%b fingerprint=%s",
                    IS_EMULATOR, Build.FINGERPRINT));

            // mock location only in debug mode,
            // because this way to detect emulator not accurate
            // and sometimes hit another devices
            if (IS_EMULATOR){
                mockLocation(MockLocation.MOSCOW);
            }
        }
    }
}