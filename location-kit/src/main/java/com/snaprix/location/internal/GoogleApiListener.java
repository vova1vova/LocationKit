package com.snaprix.location.internal;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by vladimirryabchikov on 1/12/15.
 */
interface GoogleApiListener extends
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
}