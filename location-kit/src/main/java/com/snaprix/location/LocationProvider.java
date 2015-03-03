package com.snaprix.location;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.snaprix.location.internal.LegacyLocationClient;
import com.snaprix.location.internal.LocationClient;
import com.snaprix.location.internal.PlayLocationClient;
import com.snaprix.location.utils.LocationKitLogger;
import com.snaprix.location.utils.MockLocation;
import com.snaprix.location.utils.PlayUtils;
import com.snaprix.location.utils.Time;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vladimirryabchikov on 7/3/14.
 */
public class LocationProvider {
    private boolean DEBUG = LocationKitLogger.isShowLogs();

    private static final String TAG = "LocationProvider";

    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_BALANCED = 2;
    public static final int PRIORITY_LOW = 3;

    private static final long LAST_LOCATION_LIFETIME_MILLIS = 15 * Time.MILLISECONDS.MINUTE;

    private static Location sLastLocation;

    private final Set<LocationReceiverProxy> mLocationReceivers;
    private final Set<LocationReceiverProxy> mLocationSubscribers;

    private LocationClient mPlayLocationClient;
    private LocationClient mLegacyLocationClient;

    private Context mContext;
    private ProviderCallbackProxy mCallbackProxy;

    public LocationProvider(){
        mLocationReceivers = new HashSet<>();
        mLocationSubscribers = new HashSet<>();
    }

    // connect(context: Context, callback: ProviderCallback, priority: int)
    /**
     *
     * @param context pass instance of activity when possible in order to
     *                give a Google Play services activity chance to resolve errors
     * @param callback
     * @param priority
     */
    public void connect(Context context, ProviderCallback callback, int priority){
        mContext = context;
        mCallbackProxy = new ProviderCallbackProxy(callback);

        if (DEBUG) Log.v(TAG, String.format("onResume requestPriority=%d", priority));

        if (PlayUtils.isAvailable(mContext)){
            enablePlayLocationClient(mContext, priority);
        } else {
            enableLegacyLocationManager(mContext, priority);
        }
    }

    // disconnect()

    /**
     * stop location updates
     *
     * could be called without previous call to connect()
     */
    public void disconnect(){
        if (mPlayLocationClient != null) {
            mPlayLocationClient.stopUpdates();
        }

        if (mLegacyLocationClient != null){
            mLegacyLocationClient.stopUpdates();
        }

        if (mCallbackProxy != null) {
            mCallbackProxy.destroy();
            mCallbackProxy = null;
        }
        mContext = null;
    }

    // requestLocation(receiver: LocationReceiver): LocationReceiver
    /**
     * if current location is not available, it will be delivered later
     * via LocationReceiver callback
     *
     * @param receiver
     * @return LocationReceiver waiting for first location or null if last location was delivered
     */
    public void requestLocation(LocationReceiver receiver){
        final LocationReceiverProxy proxy = new LocationReceiverProxy(receiver);

        if (DEBUG) Log.d(TAG, String.format("requestLocation isWorking=%b", isWorking()));
        if (!isWorking()) {
            mLocationReceivers.add(proxy);
            // location will be delivered after connect is called
            return;
        }

        final boolean hasLastLocation = hasLastLocation();
        if (DEBUG) Log.d(TAG, String.format("requestLocation sLastLocation=%s hasLastLocation=%b",
                sLastLocation, hasLastLocation));

        if (hasLastLocation){
            proxy.onLocationChanged(sLastLocation);
        } else {
            // location will be delivered when we receive the first one
            // if we send real location there is no need to keep receiver
            mLocationReceivers.add(proxy);
        }
    }

    // cancelRequest(receiver: LocationReceiver)
    public void cancelRequest(LocationReceiver receiver){
        final LocationReceiverProxy proxy = new LocationReceiverProxy(receiver);
        mLocationReceivers.remove(proxy);
    }

    public void subscribeForUpdates(LocationReceiver locationReceiver){
        final LocationReceiverProxy proxy = new LocationReceiverProxy(locationReceiver);
        mLocationSubscribers.add(proxy);

        if (hasLastLocation()){
            proxy.onLocationChanged(sLastLocation);
        }
    }

    public void unsubscribeFromUpdates(LocationReceiver locationReceiver){
        final LocationReceiverProxy proxy = new LocationReceiverProxy(locationReceiver);
        mLocationSubscribers.remove(proxy);
    }

    public void setPlayLocationClient(LocationClient client) {
        mPlayLocationClient = client;
    }

    public void setLegacyLocationClient(LocationClient client) {
        mLegacyLocationClient = client;
    }

    private void enablePlayLocationClient(Context context, int priority){
        int requestPriority;
        long intervalMilliseconds;
        long fastestIntervalMilliseconds;

        switch (priority){
            case PRIORITY_HIGH:
                requestPriority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                intervalMilliseconds = 15 * Time.MILLISECONDS.SECOND;
                fastestIntervalMilliseconds = 5 * Time.MILLISECONDS.SECOND;
                break;
            case PRIORITY_BALANCED:
                requestPriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                intervalMilliseconds = 30 * Time.MILLISECONDS.SECOND;
                fastestIntervalMilliseconds = 15 * Time.MILLISECONDS.SECOND;
                break;
            case PRIORITY_LOW:
                requestPriority = LocationRequest.PRIORITY_LOW_POWER;
                intervalMilliseconds = 60 * Time.MILLISECONDS.SECOND;
                fastestIntervalMilliseconds = 30 * Time.MILLISECONDS.SECOND;
                break;
            default:
                requestPriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                intervalMilliseconds = 30 * Time.MILLISECONDS.SECOND;
                fastestIntervalMilliseconds = 15 * Time.MILLISECONDS.SECOND;

                if (LocationKitLogger.getCrashHandler() != null) {
                    LocationKitLogger.getCrashHandler().logFatalException(new IllegalStateException(String.format("onResume unknown priority=%d", priority)));
                }
                break;
        }

        if (DEBUG) Log.v(TAG, String.format("enablePlayLocationClient requestPriority=%d intervalMilliseconds=%d fastestIntervalMilliseconds=%d",
                requestPriority, intervalMilliseconds, fastestIntervalMilliseconds));

        if (mPlayLocationClient == null) {
            Activity activity;
            if (context instanceof Activity){
                activity = (Activity) context;
            } else {
                activity = null;
            }
            mPlayLocationClient = new PlayLocationClient(activity);
        }

        // it's always instance of this class, just to call super type methods separately
        if (mPlayLocationClient instanceof PlayLocationClient) {
            PlayLocationClient client = (PlayLocationClient) mPlayLocationClient;
            client.setParams(requestPriority, intervalMilliseconds, fastestIntervalMilliseconds);
        } else {
            throw new RuntimeException("mPlayLocationClient must be instance of PlayLocationManager");
        }

        mPlayLocationClient.setLocationReceiver(new LocationReceiver() {
            @Override
            public void onLocationChanged(Location l) {
                onLocationReceived(l);
            }
        });
        mPlayLocationClient.startUpdates(context, mCallbackProxy);
    }

    private void enableLegacyLocationManager(Context context, int priority){
        long intervalMilliseconds;

        switch (priority){
            case PRIORITY_HIGH:
                intervalMilliseconds = 15 * Time.MILLISECONDS.SECOND;
                break;
            case PRIORITY_BALANCED:
                intervalMilliseconds = 30 * Time.MILLISECONDS.SECOND;
                break;
            case PRIORITY_LOW:
                intervalMilliseconds = 60 * Time.MILLISECONDS.SECOND;
                break;
            default:
                intervalMilliseconds = 30 * Time.MILLISECONDS.SECOND;
                if (LocationKitLogger.getCrashHandler() != null) {
                    LocationKitLogger.getCrashHandler().logFatalException(new IllegalStateException(String.format("onResume unknown priority=%d", priority)));
                }
                break;
        }

        if (mLegacyLocationClient == null) {
            mLegacyLocationClient = new LegacyLocationClient(context);
        }

        // it's always instance of this class, just to call super type methods separately
        if (mLegacyLocationClient instanceof LegacyLocationClient){
            LegacyLocationClient manager = (LegacyLocationClient) mLegacyLocationClient;
            manager.setIntervalMilliseconds(intervalMilliseconds);
        } else {
            throw new RuntimeException("mLegacyLocationClient should be instance of LegacyLocationManager");
        }

        mLegacyLocationClient.setLocationReceiver(new LocationReceiver() {
            @Override
            public void onLocationChanged(Location l) {
                onLocationReceived(l);
            }
        });
        mLegacyLocationClient.startUpdates(context, mCallbackProxy);

        Location mockLocation = MockLocation.MOSCOW;
        mLegacyLocationClient.mockLocation(mockLocation);
    }

    private void onLocationReceived(Location location) {
        if (DEBUG) Log.d(TAG, String.format("onLocationReceived location=%s", location));

        sLastLocation = location;

        for (LocationReceiverProxy r : mLocationReceivers){
            r.onLocationChanged(location);
        }
        mLocationReceivers.clear();


        for (LocationReceiverProxy r : mLocationSubscribers) {
            r.onLocationChanged(location);
        }
    }

    private boolean isWorking(){
        return mContext != null;
    }

    private boolean hasLastLocation(){
        return (sLastLocation != null
                && (System.currentTimeMillis() < sLastLocation.getTime() + LAST_LOCATION_LIFETIME_MILLIS));
    }



    /**
     * proxy object to make sure that all method calls are made on UI thread
     */
    private static class ProviderCallbackProxy implements ProviderCallback {
        private final ProviderCallback mCallback;
        private final Handler mUiHandler;

        private volatile boolean mIsWorking;

        private ProviderCallbackProxy(ProviderCallback callback) {
            mCallback = callback;
            mUiHandler = new Handler(Looper.getMainLooper());

            mIsWorking = true;
        }

        @Override
        public void onConnected() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mIsWorking) {
                        mCallback.onConnected();
                    }
                }
            });
        }

        @Override
        public void onError() {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mIsWorking) {
                        mCallback.onError();
                    }
                }
            });
        }

        void destroy(){
            mIsWorking = false;
        }
    }

    /**
     * proxy object to make sure that all method calls are made on UI thread
     */
    private static class LocationReceiverProxy implements LocationReceiver {
        private final LocationReceiver mReceiver;
        private final Handler mUiHandler;

        private LocationReceiverProxy(LocationReceiver receiver) {
            mReceiver = receiver;
            mUiHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onLocationChanged(final Location l) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mReceiver.onLocationChanged(l);
                }
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LocationReceiverProxy that = (LocationReceiverProxy) o;

            if (!mReceiver.equals(that.mReceiver)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return mReceiver.hashCode();
        }
    }
}