package com.rodriguez.armin.fotomulta.controllers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.rodriguez.armin.fotomulta.R;
import com.rodriguez.armin.fotomulta.beans.Marker;
import com.rodriguez.armin.fotomulta.services.Constants;
import com.rodriguez.armin.fotomulta.services.GeofenceErrorMessages;
import com.rodriguez.armin.fotomulta.services.GeofenceTransitionsIntentService;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by armin on 31/08/16.
 */
public class GeofenceController implements ResultCallback<Status>
{

    private Context context;
    protected static final String TAG = "GeofenceController";
    protected GoogleApiClient mGoogleApiClient;
    private boolean mGeofencesAdded;
    private PendingIntent mGeofencePendingIntent;
    private SharedPreferences mSharedPreferences;

    public GeofenceController(Context context, GoogleApiClient mGoogleApiClient)
    {
        this.context = context;
        this.mGoogleApiClient = mGoogleApiClient;
        mGeofencePendingIntent = null;
        mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,Context.MODE_PRIVATE);
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
    }

    /**
     * Add geofence, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences.
     */
    public void addGeofence(Marker marker)
    {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(context, context.getResources().getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(marker),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Add geofence, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences.
     */
    public void addGeofences(ArrayList<Marker> markers)
    {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(context, context.getResources().getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Geofence> geofenceList = new ArrayList<>();

        for (Marker marker : markers)
        {
            geofenceList.add(createGeofenceObject(marker));
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(geofenceList),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Removes geofences
     */
    public void removeGeofences()
    {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(context, context.getResources().getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the geofence to be monitored.
     */
    private GeofencingRequest getGeofencingRequest(Marker marker) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofence(createGeofenceObject(marker));

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     */
    private GeofencingRequest getGeofencingRequest(ArrayList<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(geofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }


    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.

     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();

            Log.e(TAG, context.getResources().getString(mGeofencesAdded ? R.string.geofences_added :
                    R.string.geofences_removed));

        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(context,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences.
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    private Geofence createGeofenceObject(Marker marker)
    {
        return new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(String.valueOf(marker.getId()))

                // Set the circular region of this geofence.
                .setCircularRegion(marker.getLatLng().latitude, marker.getLatLng().longitude,
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build();
    }
}
