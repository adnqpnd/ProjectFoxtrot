package com.agilaapp.projectfoxtrot;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class AddItemActivity extends AppCompatActivity {

    @BindView(R.id.editTextTaskDescription)
    EditText mEditTextTaskDescription;

    @BindView(R.id.switchLocationAwareness)
    SwitchCompat mSwitchLocationAwareness;

    @BindView(R.id.editLocationName)
    EditText mEditLocationName;

    @BindView(R.id.linearLayoutPlaceName)
    LinearLayout mLinearLayoutPlaceName;

    @BindView(R.id.buttonAddTask)
    Button mButtonAddTask;

    @BindView(R.id.buttonCancelTask)
    Button mButtonCancelTask;

    private long checklistPrimaryKey;
    private Realm mRealm;

    private static final String TAG = AddItemActivity.class.getSimpleName();
    private static final String checklistId = "checklistId";

    public static Intent newInstance(Context context, long id) {
        Log.d(TAG, "newInstance id: " + id);
        Intent intent = new Intent(context, AddItemActivity.class);
        intent.putExtra(checklistId, id);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(checklistId)) {
            checklistPrimaryKey = getIntent().getLongExtra(checklistId, 0);
            Log.d(TAG, "onCreate checklistPrimaryKey: " + checklistPrimaryKey);
        } else {
            Log.d(TAG, "onCreate checklistPrimaryKey: none" + checklistPrimaryKey);
        }

        mRealm = Realm.getDefaultInstance();

        mSwitchLocationAwareness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mLinearLayoutPlaceName.getVisibility() == View.GONE) {
                        mLinearLayoutPlaceName.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mLinearLayoutPlaceName.getVisibility() == View.VISIBLE) {
                        mLinearLayoutPlaceName.setVisibility(View.GONE);
                    }
                }
            }
        });

        mEditTextTaskDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    if (mButtonAddTask.isEnabled()) {
                        mButtonAddTask.setEnabled(false);
                    }
                } else {
                    if (!mButtonAddTask.isEnabled()) {
                        mButtonAddTask.setEnabled(true);
                    }
                }

            }
        });

        mButtonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int primaryKey = generateItemPrimaryKey(mRealm);
                final Item item = new Item();
                item.setId(primaryKey);
                item.setLabel(mEditTextTaskDescription.getText().toString());
                item.setStatus(false);
                item.setPlaceSearch(mSwitchLocationAwareness.isChecked());
                item.setPlaceName(mEditLocationName.getText().toString());

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealm(item);

                        Checklist checklist = realm.where(Checklist.class).equalTo("id", checklistPrimaryKey).findFirst();
                        checklist.getItems().add(item);
                    }
                });

                finish();
            }
        });
    }

    public int generateItemPrimaryKey(Realm realm) {
        Log.d(TAG, "getChecklistNextKey: realm" + realm);
        RealmResults<Item> list = realm.where(Item.class).findAll();

        if (list.size() > 0) {
            return realm.where(Item.class).findAll().max("id").intValue() + 1;
        } else {
            return 1;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }


}
