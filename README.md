# LocationKit

Lightweight library for dealing with location APIs on Android (primarily Google Play Services)

How to use?

1 Add repository to resolve third party dependencies:
```
allprojects {
    repositories {
        maven { url 'https://raw.githubusercontent.com/vova1vova/android-m2-repository/master/' }
        jcenter()
    }
}
```
Add dependency:
```
compile 'com.snaprix:location-kit:1.0.17'
```
2 Add permissions
```
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

3 Connect and disconnect from provider in your activity/fragment
```
@Override
protected void onStart() {
    super.onStart();
    mLocationProvider.connect(this, new ProviderCallback() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onError() {

        }
    }, LocationProvider.PRIORITY_HIGH);
}

@Override
protected void onStop() {
    mLocationProvider.disconnect();
    super.onStop();
}
```

4 Request one-shot location update
```
mLocationProvider.requestLocation(new LocationReceiver() {
    @Override
    public void onLocationChanged(Location l) {
        // TODO something useful with location
    }
});
```

5 Or subscribe/unsubscribe from location updates
```
@Override
protected void onStart() {
    ...

    mLocationSubscriber = new LocationReceiver() {
        @Override
        public void onLocationChanged(Location l) {
            // TODO something useful with location
        }
    };
    mLocationProvider.subscribeForUpdates(mLocationSubscriber);
}

@Override
protected void onStop() {

    mLocationProvider.unsubscribeFromUpdates(mLocationSubscriber);
    
    ...
}
```
