package com.agilaapp.projectfoxtrot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

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

}
