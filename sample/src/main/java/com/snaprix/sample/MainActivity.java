package com.snaprix.sample;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.location.Location;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.snaprix.location.LocationProvider;
import com.snaprix.location.LocationReceiver;
import com.snaprix.location.ProviderCallback;


public class MainActivity extends ActionBarActivity {

    private LocationProvider mLocationProvider = new LocationProvider();
    private LocationReceiver mLocationSubscriber;

    private TextView mOneShotView;
    private TextView mSubscriptionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOneShotView = (TextView) findViewById(R.id.one_shot_location_view);
        mSubscriptionView = (TextView) findViewById(R.id.subsciption_location_view);
    }

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

        mLocationProvider.requestLocation(new LocationReceiver() {
            @Override
            public void onLocationChanged(Location l) {
                updateView(mOneShotView, l);
            }
        });

        mLocationSubscriber = new LocationReceiver() {
            @Override
            public void onLocationChanged(Location l) {
                updateView(mSubscriptionView, l);
            }
        };
        mLocationProvider.subscribeForUpdates(mLocationSubscriber);
    }

    @Override
    protected void onStop() {

        mLocationProvider.unsubscribeFromUpdates(mLocationSubscriber);
        mLocationProvider.disconnect();

        super.onStop();
    }

    private void updateView(final TextView view, Location l) {
        view.setText(l.toString());

        // value blinks on update
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            anim.setDuration(1000);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setAlpha(1f);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    view.setAlpha(1f);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.start();
        }
    }
}