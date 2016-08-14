package com.agilaapp.projectfoxtrot;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class SearchPlacesService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = SearchPlacesService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private AppSharedPreference appSharedPreference;

    public SearchPlacesService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: ");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();

            appSharedPreference = AppSharedPreference.getInstance(getApplicationContext());
        }

        if(mLastLocation == null){
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: ");
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    protected void stopLocationUpdates() {

        Log.d(TAG, "stopLocationUpdates: ");
        
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null) {
            Toast.makeText(this,"Long: " + mLastLocation.getLongitude() + ", Lat: " + mLastLocation.getLatitude(),Toast.LENGTH_LONG).show();

            if(appSharedPreference.isLastLocationLatNotEmpty() && appSharedPreference.isLastLocationLongNotEmpty()){
                Log.d(TAG, "onLocationChanged: not appSharedPreference.isSearchEmpty() ");
                Location location1 = new Location("");
                location1.setLatitude(appSharedPreference.getLastLocationLat());
                location1.setLongitude(appSharedPreference.getLastLocationLong());

                Log.d(TAG, "onLocationChanged: appReference saved lat " + appSharedPreference.getLastLocationLat());
                Log.d(TAG, "onLocationChanged: appReference saved long " + appSharedPreference.getLastLocationLong());
                Log.d(TAG, "onLocationChanged: lat " + mLastLocation.getLatitude());
                Log.d(TAG, "onLocationChanged: long " + mLastLocation.getLongitude());

                Location location2 = new Location("");
                location2.setLatitude(mLastLocation.getLatitude());
                location2.setLongitude(mLastLocation.getLongitude());

                //TODO call api using long lat then save new location

                Log.d(TAG, "onLocationChanged: distance l1 to l2: " +  location1.distanceTo(location2));

                appSharedPreference.setLastLocationLat(Double.doubleToLongBits(mLastLocation.getLatitude()));
                appSharedPreference.setLastLocationLong(Double.doubleToLongBits(mLastLocation.getLongitude()));

            }else{
                Log.d(TAG, "onLocationChanged: appSharedPreference.isSearchEmpty() ");
                appSharedPreference.setLastLocationLat(Double.doubleToLongBits(mLastLocation.getLatitude()));
                appSharedPreference.setLastLocationLong(Double.doubleToLongBits(mLastLocation.getLongitude()));
                //TODO call api using long lat
            }

        }
    }
}
