package com.agilaapp.projectfoxtrot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);

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
    }

}
