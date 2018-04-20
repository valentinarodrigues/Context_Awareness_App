package com.example.aero.localife.create_profile;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.Toast;

import com.example.aero.localife.DatabaseHelperActivity;
import com.example.aero.localife.R;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static java.lang.String.*;

public class LocationCheckerServiceActivity extends Service implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static int LOCATION_PERMISSION = 111;
    private final int SERVICE_ID = 101;
    public GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    Notification notification = null;
    private String TAG = "LocationCheckerServiceActivity";
    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API)
                    .build();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        locationChecker();
        return START_STICKY;
    }

    private void locationChecker() {

        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100000);
        mLocationRequest.setFastestInterval(50000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        Log.d(TAG, "creating request");

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onResult(LocationSettingsResult result) {
                Log.d(TAG, "in on result");

                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        //...
                        Log.d(TAG, "in Success");

                        try {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient);
                            if (mLastLocation == null)
                                Log.d(TAG, "mLastLocation is null");
                            if (mGoogleApiClient == null)
                                Log.d(TAG, "mGoogleAPIClient is null");
                            else
                                Log.d(TAG, "nothing is null");


                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }

                        DatabaseHelperActivity databaseHelperActivity = new DatabaseHelperActivity(LocationCheckerServiceActivity.this);

                        if (mLastLocation != null) {

                            String bluetoothON = "ON";
                            Boolean rowExist = databaseHelperActivity.getProfileListEmptyStatus();
//                            AudioManager am;
//                            am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                            Toast.makeText(getApplicationContext(), "Latitude: " + valueOf(mLastLocation.getLatitude()) + ", " + "Longitude: " + valueOf(mLastLocation.getLongitude()), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Latitude: " + valueOf(mLastLocation.getLatitude()) + ", " + "Longitude: " + valueOf(mLastLocation.getLongitude()));
                            String latitude = valueOf(mLastLocation.getLatitude());
                            String longitude = valueOf(mLastLocation.getLongitude());

                            String serviceLatitudeSubString = latitude.substring(0, 7);
                            String serviceLongitudeSubString = longitude.substring(0, 7);

                            if (rowExist){
                                Log.i(TAG, "Inside here");

                                String matchedProfile = databaseHelperActivity.getProfileForLocationMatched(serviceLatitudeSubString, serviceLongitudeSubString);
                                String bluetoothStatus = databaseHelperActivity.getCurrentBluetoothValue(matchedProfile);

                                if (bluetoothStatus.equals(bluetoothON.trim())) {
                                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    bluetoothAdapter.enable();
//
//                                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                    Toast.makeText(LocationCheckerServiceActivity.this, matchedProfile + " is Activated!", Toast.LENGTH_LONG).show();
                                } else {
                                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    bluetoothAdapter.disable();
//                                    am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                    Toast.makeText(LocationCheckerServiceActivity.this, matchedProfile + " is not active!", Toast.LENGTH_LONG).show();
                                }
                            }
                                Notification.Builder builder = new Notification.Builder(getApplicationContext())
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle("Context-Aware")
                                        .setContentText("Latitude: "+serviceLatitudeSubString+", Longitude: "+serviceLongitudeSubString);
                                notification = builder.build();
                                startForeground(SERVICE_ID, notification);

                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(TAG, "in on resolution required");


                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "in on no option");

                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
