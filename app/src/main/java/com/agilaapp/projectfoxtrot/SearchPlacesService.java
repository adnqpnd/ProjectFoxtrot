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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchPlacesService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = SearchPlacesService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private AppSharedPreference mAppSharedPreference;
    private NetworkManager mNetworkManager;

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
        }

        mAppSharedPreference = AppSharedPreference.getInstance(getApplicationContext());
        mNetworkManager = NetworkManager.getInstance();


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

            if(mAppSharedPreference.isLastLocationLatNotEmpty() && mAppSharedPreference.isLastLocationLongNotEmpty()){
                Log.d(TAG, "onLocationChanged: not appSharedPreference.isSearchEmpty() ");
                Location location1 = new Location("");
                location1.setLatitude(mAppSharedPreference.getLastLocationLat());
                location1.setLongitude(mAppSharedPreference.getLastLocationLong());

                Log.d(TAG, "onLocationChanged: appReference saved lat " + mAppSharedPreference.getLastLocationLat());
                Log.d(TAG, "onLocationChanged: appReference saved long " + mAppSharedPreference.getLastLocationLong());
                Log.d(TAG, "onLocationChanged: lat " + mLastLocation.getLatitude());
                Log.d(TAG, "onLocationChanged: long " + mLastLocation.getLongitude());

                Location location2 = new Location("");
                location2.setLatitude(mLastLocation.getLatitude());
                location2.setLongitude(mLastLocation.getLongitude());

                //TODO call api using long lat then save new location

                Log.d(TAG, "onLocationChanged: distance l1 to l2: " +  location1.distanceTo(location2));

                mAppSharedPreference.setLastLocationLat(Double.doubleToLongBits(mLastLocation.getLatitude()));
                mAppSharedPreference.setLastLocationLong(Double.doubleToLongBits(mLastLocation.getLongitude()));

                float distance = location1.distanceTo(location2);

                if(201 > 200){

                    Log.d(TAG, "onLocationChanged: request for new places");

                    RequestBody body = new FormBody.Builder()
                            .add("latitude",String.valueOf(mLastLocation.getLatitude()))
                            .add("longitude",String.valueOf(mLastLocation.getLongitude()))
                            .add("radius", "500")
                            .add("type", "Shopping_mall")
                            .build();

//                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                    String params = "{\"latitude\":"+mLastLocation.getLatitude()+",\"longitude\":"+mLastLocation.getLongitude()+",\"radius\":"+500+",\"type\":"+"bakery"+"}";
//                    RequestBody body = RequestBody.create(JSON, params);

                    Request request = new Request.Builder()
                            .url("https://foxtrot-app.herokuapp.com/api/places")
                            .post(body)
                            .build();

                    mNetworkManager.getOkHttpClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                throw new IOException("Unexpected code " + response);
                            }else {
                                try {
                                    String responseData = response.body().string();
                                    JSONArray jsonArray = new JSONArray(responseData);
                                    Log.d(TAG, "onLocationChanged new places onResponse: " + jsonArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }

            }else{
                Log.d(TAG, "onLocationChanged: appSharedPreference.isSearchEmpty() ");
                mAppSharedPreference.setLastLocationLat(Double.doubleToLongBits(mLastLocation.getLatitude()));
                mAppSharedPreference.setLastLocationLong(Double.doubleToLongBits(mLastLocation.getLongitude()));
                //TODO call api using long lat
            }

        }
    }
}
