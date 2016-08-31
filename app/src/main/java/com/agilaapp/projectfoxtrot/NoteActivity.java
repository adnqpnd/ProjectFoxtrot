package com.agilaapp.projectfoxtrot;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class NoteActivity extends AppCompatActivity {

    private static final String TAG = "NoteActivity";

    @BindView(R.id.recyclerViewChecklist)
    RecyclerView mRecyclerViewCheckList;

    @BindView(R.id.floatingButtonAddChecklist)
    FloatingActionButton mFloatingButtonAddChecklist;

    RecyclerView.LayoutManager mLayoutManager;

    Realm mRealm;
    RealmChangeListener<Realm> mRealmChangeListener;
    RealmResults<Checklist> mCheckListResults;
    NoteAdapter mNoteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        ButterKnife.bind(this);

        mRealm = Realm.getDefaultInstance();

        mCheckListResults = mRealm.where(Checklist.class).findAll().sort("id");

        mRecyclerViewCheckList.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(this,3);
        mRecyclerViewCheckList.setLayoutManager(mLayoutManager);

        mNoteAdapter = new NoteAdapter(mCheckListResults);
        mRecyclerViewCheckList.setAdapter(mNoteAdapter);

        mFloatingButtonAddChecklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NoteActivity.this, "Add item to list!!!", Toast.LENGTH_LONG).show();

                int primaryKey =  generateChecklistPrimaryKey(mRealm);
                final Checklist checklist = new Checklist();
                checklist.setId(primaryKey);
                checklist.setName("Note #" + primaryKey);

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealm(checklist);
                    }
                });
            }
        });

        mRealmChangeListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                mCheckListResults = mRealm.where(Checklist.class).findAll().sort("id");
                mNoteAdapter.notifyDataSetChanged();
            }
        };

        mRealm.addChangeListener(mRealmChangeListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(mRealmChangeListener);
        mRealm.close();
    }

    public int generateChecklistPrimaryKey(Realm realm){
        Log.d(TAG, "getChecklistNextKey: realm" + realm);
        RealmResults<Checklist> list =  realm.where(Checklist.class).findAll();

        if(list.size() > 0){
            return realm.where(Checklist.class).findAll().max("id").intValue() + 1;
        }else {
            return 1;
        }

    }

    public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
        private RealmResults<Checklist> mChecklists;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mNoteLabel;
            public Button mDeleteNoteButton;
            public RelativeLayout mRelativeLayoutNote;
            public ViewHolder(View v) {
                super(v);
                mNoteLabel = (TextView) v.findViewById(R.id.noteLabel);
                mDeleteNoteButton = (Button) v.findViewById(R.id.buttonDeleteNote);
                mRelativeLayoutNote = (RelativeLayout) v.findViewById(R.id.relativeLayoutNote);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public NoteAdapter(RealmResults<Checklist> checklists) {
            mChecklists = checklists;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_checklist, parent, false);
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final long primaryId = mChecklists.get(position).getId();
            holder.mNoteLabel.setText(mChecklists.get(position).getName());
            holder.mDeleteNoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("NoteAdapter", "onClick: delete note: " + primaryId);
                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Checklist checkList =  realm.where(Checklist.class).equalTo("id",primaryId).findFirst();
                            checkList.getItems().deleteAllFromRealm();
                            checkList.deleteFromRealm();
                        }
                    });
                }
            });
            holder.mRelativeLayoutNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick primaryId: " + primaryId);
                    Intent i = CheckListActivity.newInstance(NoteActivity.this,primaryId);
                    startActivity(i);
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
          return   mChecklists.size();
        }

    }

}
