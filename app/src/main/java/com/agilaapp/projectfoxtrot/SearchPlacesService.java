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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.stetho.common.Utf8Charset;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;


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
    private int radius = 500;
    public final static String checkListIdExtra = "EXTRA_CHECKLIST_ID";
    private long checkListId;
    Realm mRealm;

    public SearchPlacesService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: ");
        mRealm = Realm.getDefaultInstance();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
        }

        mAppSharedPreference = AppSharedPreference.getInstance(getApplicationContext());
        mNetworkManager = NetworkManager.getInstance(this);


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
        mRealm.close();
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
        checkListId = intent.getLongExtra(checkListIdExtra, 0);
        Log.d(TAG, "onStartCommand: checkListIdcheckListId " + checkListId);
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

        if (checkListId != 0L) {
            Checklist checklist = mRealm.where(Checklist.class).equalTo("id", checkListId).findFirst();

            mLastLocation = location;
            if (mLastLocation != null) {
                Toast.makeText(this, "Long: " + mLastLocation.getLongitude() + ", Lat: " + mLastLocation.getLatitude(), Toast.LENGTH_LONG).show();

                if (mAppSharedPreference.isLastLocationLatNotEmpty() && mAppSharedPreference.isLastLocationLongNotEmpty()) {
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

                    Log.d(TAG, "onLocationChanged: distance l1 to l2: " + location1.distanceTo(location2));

                    mAppSharedPreference.setLastLocationLat(Double.doubleToLongBits(mLastLocation.getLatitude()));
                    mAppSharedPreference.setLastLocationLong(Double.doubleToLongBits(mLastLocation.getLongitude()));

                    float distance = location1.distanceTo(location2);

                    if (201 > 200) {

                        Log.d(TAG, "onLocationChanged: request for new places");

                        for (final Item item : checklist.getItems()) {
                            Log.d(TAG, "onLocationChanged: " + item);

                            if (item.isPlaceSearch() && !item.isDone()) {
                                String url = null;

                                try {
                                    url = "https://foxtrot-app.herokuapp.com/api/places?lat=" + mLastLocation.getLatitude()
                                            + "&lng=" + mLastLocation.getLongitude() + "&radius=" + radius + "&type=" + URLEncoder.encode(item.getPlaceName(), "utf-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }


                                Log.d(TAG, "onLocationChanged: url: " + url);

                                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                                        new Response.Listener<JSONArray>() {
                                            @Override
                                            public void onResponse(JSONArray response) {
                                                // display response
                                                Log.d("Response", item.getId() + ": " + response.toString());
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.d("Error.Response", "" + error);
                                            }
                                        }
                                );

                                mNetworkManager.addToRequestQueue(jsonObjectRequest);
                                }


                        }


                    }

                } else {
                    Log.d(TAG, "onLocationChanged: appSharedPreference.isSearchEmpty() ");
                    mAppSharedPreference.setLastLocationLat(Double.doubleToLongBits(mLastLocation.getLatitude()));
                    mAppSharedPreference.setLastLocationLong(Double.doubleToLongBits(mLastLocation.getLongitude()));
                    //TODO call api using long lat
                }

                }
        }
    }
}
