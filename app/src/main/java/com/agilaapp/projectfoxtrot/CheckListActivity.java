package com.agilaapp.projectfoxtrot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CheckListActivity extends AppCompatActivity{
    private static final String TAG = CheckListActivity.class.getSimpleName();
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final String checklistId = "checklistId";
    private long checklistPrimaryKey;

    @BindView(R.id.floatingButtonChecklist)
    FloatingActionButton mFloatingButtonChecklist;

    @BindView(R.id.switchChecklist)
    SwitchCompat mSwitchCompat;

    @BindView(R.id.textViewLat)
    TextView textViewLat;

    @BindView(R.id.textViewLong)
    TextView textViewLong;

    @BindView(R.id.recyclerViewChecklist)
    RecyclerView recyclerViewCheckList;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Realm mRealm;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChecklistAdapter checklistAdapter;

    Checklist mChecklist;


    private AppSharedPreference mAppSharedPreference;
    private RealmChangeListener<Realm> mRealmChangeListener;

    public static Intent newInstance(Context context, long id){
        Log.d(TAG, "newInstance id: " + id);
        Intent intent = new Intent(context,CheckListActivity.class);
        intent.putExtra(checklistId,id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(checklistId)) {
            checklistPrimaryKey = getIntent().getLongExtra(checklistId,0);
            Log.d(TAG, "onCreate checklistPrimaryKey: " + checklistPrimaryKey);
        }else {
            Log.d(TAG, "onCreate checklistPrimaryKey: none" + checklistPrimaryKey);
        }

        mAppSharedPreference = AppSharedPreference.getInstance(getApplicationContext());

        mRealm = Realm.getDefaultInstance();


//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }

        recyclerViewCheckList.setHasFixedSize(true);


        //RealmResults<Item> rr = mRealm.where(Item.class).findAll();
        mChecklist = mRealm.where(Checklist.class).equalTo("id", checklistPrimaryKey).findFirst();

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerViewCheckList.setLayoutManager(mLayoutManager);

        checklistAdapter = new ChecklistAdapter(mChecklist.getItems());
        recyclerViewCheckList.setAdapter(checklistAdapter);

        mFloatingButtonChecklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(CheckListActivity.this, "Add item to list!!!", Toast.LENGTH_LONG).show();
//
//                int primaryKey = generateItemPrimaryKey(mRealm);
//                final Item item = new Item();
//                item.setId(primaryKey);
//                item.setLabel("Task #" + primaryKey);
//                item.setStatus(false);
//
//                mRealm.executeTransactionAsync(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        realm.copyToRealm(item);
//
//                        Checklist checklist = realm.where(Checklist.class).equalTo("id", checklistPrimaryKey).findFirst();
//                        checklist.getItems().add(item);
//                    }
//                });
                Intent intent = AddItemActivity.newInstance(CheckListActivity.this, checklistPrimaryKey);
                startActivity(intent);
            }
        });

        if(mAppSharedPreference.getSearch())
            mSwitchCompat.setChecked(true);
        else
            mSwitchCompat.setChecked(false);

        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(CheckListActivity.this, "Enable!!!", Toast.LENGTH_LONG).show();
                    mAppSharedPreference.setSearch(true);
                    startService( new Intent(CheckListActivity.this,SearchPlacesService.class));
                } else {
                    Toast.makeText(CheckListActivity.this, "Disable!!!", Toast.LENGTH_LONG).show();
                    mAppSharedPreference.setSearch(false);
                    stopService(new Intent(CheckListActivity.this,SearchPlacesService.class));
                }
            }
        });

        mRealmChangeListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                mChecklist = mRealm.where(Checklist.class).equalTo("id", checklistPrimaryKey).findFirst();
                checklistAdapter.notifyDataSetChanged();
            }
        };

        mRealm.addChangeListener(mRealmChangeListener);

        //createLocationRequest();
    }

//    @Override
//    protected void onStart() {
//        mGoogleApiClient.connect();
//        super.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        mGoogleApiClient.disconnect();
//        super.onStop();
//    }

//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//        }
//
//        Log.d(TAG, "onConnected: ");
//
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                mGoogleApiClient);
//
//        Log.d(TAG, "onConnected: mLastLocation " + mLastLocation);
//
//        if (mLastLocation != null) {
//            Log.d(TAG, "onConnected: lat " + mLastLocation.getLatitude());
//            Log.d(TAG, "onConnected: long " + mLastLocation.getLongitude());
//        }
//
//        startLocationUpdates();
//    }
//
//    protected void startLocationUpdates() {
//        Log.d(TAG, "startLocationUpdates: ");
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopLocationUpdates();
//    }
//
//    protected void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(
//                mGoogleApiClient, this);
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.d(TAG, "onConnectionSuspended: ");
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.d(TAG, "onConnectionFailed: ");
//    }
//
//    protected void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000);
//        mLocationRequest.setFastestInterval(5000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                .addLocationRequest(mLocationRequest);
//
//        PendingResult<LocationSettingsResult> result =
//                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
//                        builder.build());
//
//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            @Override
//            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
//                final Status status = locationSettingsResult.getStatus();
//                final LocationSettingsStates locationSettingStates = locationSettingsResult.getLocationSettingsStates();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.SUCCESS:
//                        // All location settings are satisfied. The client can
//                        // initialize location requests here.
//                        break;
//                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        // Location settings are not satisfied, but this can be fixed
//                        // by showing the user a dialog.
//                        try {
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            status.startResolutionForResult(
//                                    CheckListActivity.this,
//                                    REQUEST_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException e) {
//                            // Ignore the error.
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        // Location settings are not satisfied. However, we have no way
//                        // to fix the settings so we won't show the dialog.
//                        break;
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        mLastLocation = location;
//        if (mLastLocation != null) {
//            Log.d(TAG, "onLocationChanged: lat " + mLastLocation.getLatitude());
//            Log.d(TAG, "onLocationChanged: long " + mLastLocation.getLongitude());
//            textViewLat.setText(String.valueOf(mLastLocation.getLatitude()));
//            textViewLong.setText(String.valueOf(mLastLocation.getLongitude()));
//        }
//    }

    public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {
        private List<Item> mItems;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextViewTodo;
            public CheckBox mCheckboxTodo;

            public ViewHolder(View v) {
                super(v);
                mTextViewTodo = (TextView) v.findViewById(R.id.textViewTodo);
                mCheckboxTodo = (CheckBox) v.findViewById(R.id.checkBoxTodo);

            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public ChecklistAdapter(List<Item> items) {
            mItems = items;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_todo, parent, false);
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final long primaryId = mItems.get(position).getId();
            holder.mTextViewTodo.setText(mItems.get(position).getLabel());
            holder.mCheckboxTodo.setChecked(mItems.get(position).isDone());
            holder.mCheckboxTodo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Item item = realm.where(Item.class).equalTo("id", primaryId).findFirst();
                            item.setStatus(isChecked);
                        }
                    });
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return   mItems.size();
        }

    }
}
