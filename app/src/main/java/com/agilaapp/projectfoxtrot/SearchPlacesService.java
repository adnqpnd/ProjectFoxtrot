package com.agilaapp.projectfoxtrot;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;


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

    private final IBinder mBinder = new LocalBinder();

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
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        checkListId = intent.getLongExtra(checkListIdExtra, 0);
        Log.d(TAG, "onBind: checkListIdcheckListId " + checkListId);
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public void printHelloWorld() {
        Log.d(TAG, "printHelloWorld: ");
    }

    public class LocalBinder extends Binder {
        SearchPlacesService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SearchPlacesService.this;
        }
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

        Log.d(TAG, "onLocationChanged: ");

        //if (checkListId != 0L) {
        // Checklist checklist = mRealm.where(Checklist.class).equalTo("id", checkListId).findFirst();
        RealmResults<Checklist> checklists = mRealm.where(Checklist.class).equalTo("enableSearchPlace", true).findAll();

        Log.d(TAG, "onLocationChanged: checklists size " + checklists.size());

        for (Checklist checklist : checklists) {
            final long checkListPrimaryKey = checklist.getId();
            mLastLocation = location;
            if (mLastLocation != null) {
                Toast.makeText(this, "Long: " + mLastLocation.getLongitude() + ", Lat: " + mLastLocation.getLatitude(), Toast.LENGTH_LONG).show();

                if (checklist.getLatitude() != null && checklist.getLongitude() != null) {
                    Log.d(TAG, "onLocationChanged: not appSharedPreference.isSearchEmpty() ");
                    Location location1 = new Location("");
                    location1.setLatitude(checklist.getLatitude());
                    location1.setLongitude(checklist.getLongitude());

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
                        searchForPlaces(checklist);
                    }

                } else {
                    Log.d(TAG, "onLocationChanged: appSharedPreference.isSearchEmpty() ");
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Checklist checklist = realm.where(Checklist.class).equalTo("id", checkListPrimaryKey).findFirst();
                            checklist.setLatitude(mLastLocation.getLatitude());
                            checklist.setLongitude(mLastLocation.getLongitude());
                        }
                    });

                    //TODO call api using long lat
                        searchForPlaces(checklist);
                    }

                }
            }
        // }
    }

    private void searchForPlaces(Checklist checklist) {
        for (final Item item : checklist.getItems()) {
            Log.d(TAG, "onLocationChanged: " + item);

            final long itemId = item.getId();

            if (item.isPlaceSearch() && !item.isDone()) {
                String url = null;

                try {
                    url = "https://foxtrot-app.herokuapp.com/api/places?lat=" + mLastLocation.getLatitude()
                            + "&lng=" + mLastLocation.getLongitude() + "&radius=" + radius + "&name=" + URLEncoder.encode(item.getPlaceName(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                Log.d(TAG, "onLocationChanged: url: " + url);

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(final JSONArray response) {
                                // display response
                                Log.d("Response", itemId + ": " + response.toString());

                                //   item.setPlaces(placeList);

                                mRealm.executeTransactionAsync(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {


                                        Item itemSelected = realm.where(Item.class).equalTo("id", itemId).findFirst();
                                        itemSelected.getPlaces().deleteAllFromRealm();

                                        for (int i = 0; i < response.length(); i++) {
                                            try {
                                                JSONObject jsonObject = response.getJSONObject(i);
                                                Place place = new Place();
                                                place.setId(jsonObject.getString("place_id"));
                                                place.setName(jsonObject.getString("name"));
                                                place.setLatitude(jsonObject.getDouble("lat"));
                                                place.setLongitude(jsonObject.getDouble("lng"));

                                                realm.copyToRealmOrUpdate(place);
                                                itemSelected.getPlaces().add(place);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    }
                                });
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
}
