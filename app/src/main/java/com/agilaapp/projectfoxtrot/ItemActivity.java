package com.agilaapp.projectfoxtrot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class ItemActivity extends AppCompatActivity {

    @BindView(R.id.editTextEditTaskDescription)
    EditText mEditTextEditTaskDescription;

    @BindView(R.id.switchEditLocationAwareness)
    SwitchCompat mSwitchEditLocationAwareness;

    @BindView(R.id.editTextEditLocationName)
    EditText mEditTextEditLocationName;

    @BindView(R.id.linearLayoutEditPlaceName)
    LinearLayout mLinearLayoutEditPlaceName;

    @BindView(R.id.buttonEditTask)
    Button mButtonEditTask;

    @BindView(R.id.buttonSaveEdit)
    Button mButtonSaveEdit;

    @BindView(R.id.buttonCancelEdit)
    Button mButtonCancelEdit;

    @BindView(R.id.recyclerViewPlaceSearched)
    RecyclerView mRecyclerViewPlaceSearched;

    private RecyclerView.LayoutManager mLayoutManager;
    private ItemPlaceListAdapter itemPlaceListAdapter;

    private long checklistItemPrimaryKey;
    private Item mItem;

    private static final String taskId = "taskId";

    private static final String TAG = ItemActivity.class.getSimpleName();

    private Realm mRealm;

    public static Intent newInstance(Context context, long id) {
        Log.d(TAG, "newInstance id: " + id);
        Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra(taskId, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        ButterKnife.bind(this);

        editMode(false);


        checklistItemPrimaryKey = getIntent().getLongExtra(taskId, 0);
        Log.d(TAG, "onCreate checklistItemPrimaryKey: " + checklistItemPrimaryKey);


        mRealm = Realm.getDefaultInstance();

        Item item = mRealm.where(Item.class).equalTo("id", checklistItemPrimaryKey).findFirst();

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerViewPlaceSearched.setLayoutManager(mLayoutManager);

        itemPlaceListAdapter = new ItemPlaceListAdapter(item.getPlaces());
        mRecyclerViewPlaceSearched.setAdapter(itemPlaceListAdapter);

        setupForm();

        mSwitchEditLocationAwareness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mLinearLayoutEditPlaceName.getVisibility() == View.GONE) {
                        mLinearLayoutEditPlaceName.setVisibility(View.VISIBLE);
                        mEditTextEditLocationName.setText(mItem.getPlaceName());
                    }
                } else {
                    if (mLinearLayoutEditPlaceName.getVisibility() == View.VISIBLE) {
                        mLinearLayoutEditPlaceName.setVisibility(View.GONE);
                    }
                }
            }
        });

        mButtonEditTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode(true);
            }
        });

        mButtonCancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode(false);
                setupForm();
            }
        });

        mButtonSaveEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Item item = new Item();
                item.setId(checklistItemPrimaryKey);
                item.setLabel(mEditTextEditTaskDescription.getText().toString());
                item.setStatus(false);
                item.setPlaceSearch(mSwitchEditLocationAwareness.isChecked());
                item.setPlaceName(mEditTextEditLocationName.getText().toString());

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(item);
                    }
                });

                editMode(false);

            }
        });

    }

    private void setupForm() {
        mItem = mRealm.where(Item.class).equalTo("id", checklistItemPrimaryKey).findFirst();
        Log.d(TAG, "onCreate: item " + mItem.toString());

        mEditTextEditTaskDescription.setText(mItem.getLabel());

        if (mItem.isPlaceSearch()) {
            mSwitchEditLocationAwareness.setChecked(mItem.isPlaceSearch());
            mLinearLayoutEditPlaceName.setVisibility(View.VISIBLE);
            mEditTextEditLocationName.setText(mItem.getPlaceName());
        }
    }

    private void editMode(boolean isEditMode) {
        mEditTextEditTaskDescription.setEnabled(isEditMode);
        mSwitchEditLocationAwareness.setEnabled(isEditMode);
        mEditTextEditLocationName.setEnabled(isEditMode);

        if (isEditMode) {
            mButtonEditTask.setVisibility(View.GONE);
            mButtonSaveEdit.setVisibility(View.VISIBLE);
            mButtonCancelEdit.setVisibility(View.VISIBLE);
        } else {
            mButtonEditTask.setVisibility(View.VISIBLE);
            mButtonSaveEdit.setVisibility(View.GONE);
            mButtonCancelEdit.setVisibility(View.GONE);
        }
    }

    public class ItemPlaceListAdapter extends RecyclerView.Adapter<ItemPlaceListAdapter.ViewHolder> {
        private List<Place> mPlaces;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextViewPlaceName;
            public LinearLayout mLinearLayoutPlace;

            public ViewHolder(View v) {
                super(v);
                mTextViewPlaceName = (TextView) v.findViewById(R.id.textViewPlaceName);
                mLinearLayoutPlace = (LinearLayout) v.findViewById(R.id.linearLayoutPlace);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public ItemPlaceListAdapter(List<Place> places) {
            mPlaces = places;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_place, parent, false);
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final String primaryId = mPlaces.get(position).getId();
            holder.mTextViewPlaceName.setText(mPlaces.get(position).getName());

            final double lat = mPlaces.get(position).getLatitude();
            final double lng = mPlaces.get(position).getLongitude();

            holder.mLinearLayoutPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ItemActivity.this, "lat:" + lat + " , long:" + lng, Toast.LENGTH_LONG).show();
                    goToMap(ItemActivity.this, lat, lng);
                }
            });
        }

        public void goToMap(Context context, double lat, double lon) {
            int zoomLevel = 15;
            String label = "?q=" + lat + "," + lon;
            String appUrl = "geo:" + lat + "," + lon + label + "&z=" + zoomLevel;

            // Log.d(PostHelper.class.getName(), "MapFeature appurl ---=" + appUrl);

            Uri intentUri = Uri.parse(appUrl);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                //Toast.makeText(context,"No App Avail",Toast.LENGTH_LONG).show();
                String webUrl = "https://www.google.com/maps?q=" + lat + "," + lon;
                //Log.d(PostHelper.class.getName(), "MapFeature weburl ---=" + webUrl);
                Uri webIntentUri = Uri.parse(webUrl);
                Intent webMapIntent = new Intent(Intent.ACTION_VIEW, webIntentUri);
                context.startActivity(webMapIntent);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mPlaces.size();
        }

    }

}
