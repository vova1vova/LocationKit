package com.snaprix.location.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;

/**
 * Created by vladimirryabchikov on 1/11/15.
 */
public class PlayErrorHandler {
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    /**
     * handle error with Google Play services,
     * usually by showing error-dialog to the user
     *
     * @param result
     * @param activity
     */
    public void handleError(ConnectionResult result, Activity activity){
        if (HereUtils.isHereMapsAvailable()){
            // it's Nokia device
            // is ok that it does not have Play services
            return;
        }

        // Get the error dialog from Google Play services
        if (mResolvingError) {
            // Already attempting to resolve an error.
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(activity, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try next time
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

            } else {
                showErrorDialog(activity, result.getErrorCode());
                mResolvingError = true;
            }
        }
    }

    /* Creates a dialog for an error message */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showErrorDialog(Activity activity, int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);

        activity.getFragmentManager()
                .beginTransaction()
                .add(dialogFragment, "error_dialog_fragment")
                .commitAllowingStateLoss();
    }
}