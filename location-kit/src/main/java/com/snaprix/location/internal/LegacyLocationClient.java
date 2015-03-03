package com.snaprix.location.internal;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.snaprix.location.LocationReceiver;
import com.snaprix.location.ProviderCallback;

/**
 * Created by vladimirryabchikov on 7/10/14.
 */
public class LegacyLocationClient implements LocationClient {
    private LocationManager locationManager = null;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    private boolean mGpsRunning = false;
    private boolean mNetworkRunning = false;

    private long mIntervalMilliseconds;

    private ProviderCallback mListener;
    private LocationReceiver mLocationReceiver;

    public LegacyLocationClient(Context context) {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex){
            // it's ok to not have gps provider
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            // it's ok to not have network provider
        }
    }

    @Override
    public void startUpdates(Context context, @NonNull ProviderCallback callback) {
        mListener = callback;

        if (locationManager != null) {
            if (!mGpsRunning && gps_enabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mIntervalMilliseconds, 50, mGpsListener);
                mGpsRunning = true;
            }
            if (!mNetworkRunning && network_enabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 25, mNetworkListener);
                mNetworkRunning = true;
            }

            mListener.onConnected();
        }
    }

    @Override
    public void stopUpdates(){
        if (mGpsRunning) {
            locationManager.removeUpdates(mGpsListener);
            mGpsRunning = false;
        }
        if (mNetworkRunning) {
            locationManager.removeUpdates(mNetworkListener);
            mNetworkRunning = false;
        }

        mLocationReceiver = null;
    }

    @Override
    public void setLocationReceiver(LocationReceiver locationReceiver) {
        mLocationReceiver = locationReceiver;
    }

    @Override
    public void mockLocation(Location l) {
        if (mLocationReceiver != null) {
            mLocationReceiver.onLocationChanged(l);
        }
    }

    public void setIntervalMilliseconds(long intervalMilliseconds) {
        mIntervalMilliseconds = intervalMilliseconds;
    }

    // Define a listener that responds to location updates
    LocationListener mGpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (network_enabled) {
                // we recv GPS location -> disable network
                network_enabled = false;
                if (mNetworkRunning) {
                    locationManager.removeUpdates(mNetworkListener);
                    mNetworkRunning = false;
                }
            }

            if (mLocationReceiver != null){
                mLocationReceiver.onLocationChanged(location);
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    // Define a listener that responds to location updates
    LocationListener mNetworkListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mLocationReceiver != null){
                mLocationReceiver.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };
}