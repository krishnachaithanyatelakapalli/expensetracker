package com.example.krishna.expendituremanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EntriesView extends AppCompatActivity {

    private static final String TAG = "EntriesView";
    /*
     * Elements from activity_entries_view.xml
     */
    TextView itemView, costView, transactionTypeView, dateView, descriptionView;
    ListView shareListView;
    Button entryEditBtn, entryDeleteBtn, entryCancelBtn;

    /*
     * Elements from context_delete_entry.xml
     */
    Button deleteCancelBtn, deleteDeleteBtn;
    ConstraintLayout delete_view;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entries_view);

        databaseHelper = new DatabaseHelper(this);

        init();

        String id = this.getIntent().getStringExtra("_id");
        String month = this.getIntent().getStringExtra("month");
        String accountName = this.getIntent().getStringExtra("accountName");

        delete_view.setVisibility(View.INVISIBLE);

        entryDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });

        entryCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        populateEntryDetails(id, month, accountName);
    }

    class ListCursorAdapter extends SimpleCursorAdapter {
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
        }

        ListCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }
    }

    private void init() {
        entryViewInit();
        deleteViewInit();
    }

    private void entryViewInit() {
        itemView = findViewById(R.id.entry_item);
        costView = findViewById(R.id.entry_cost);
        transactionTypeView = findViewById(R.id.entry_transaction_type);
        dateView = findViewById(R.id.entry_date);
        descriptionView = findViewById(R.id.entry_description);
        entryEditBtn = findViewById(R.id.entry_edit_btn);
        entryDeleteBtn = findViewById(R.id.entry_delete_btn);
        entryCancelBtn = findViewById(R.id.entry_cancel_btn);
        shareListView = findViewById(R.id.entry_share_list);
    }

    private void deleteViewInit() {
        delete_view = findViewById(R.id.entry_delete_view);
        deleteCancelBtn = findViewById(R.id.entry_delete_cancel_btn);
        deleteDeleteBtn = findViewById(R.id.entry_delete_delete_btn);
    }

    private void populateEntryDetails(String id, String month, String accountName) {
        String[] vals = new String[9];
        String[] entry_cols = {
                "_id",
                "_yearID",
                "_account_name",
                "_item",
                "_cost",
                "_date",
                "_transactionType",
                "_description",
                "_share"
        };

        String[] share_cols = {
                "_id",
                "_account_name",
                "_month",
                "_entryID",
                "_name",
                "_price"
        };

        String[] entry_args = {
                id
        };

        String[] share_sel = {
                "_account_name",
                "_month",
                "_entryID"
        };

        String[] share_args = {
                accountName,
                month,
                id
        };

        String[] share_projections = {
                "_name",
                "_price"
        };

        int[] toView = {
                R.id.entry_share_name,
                R.id.entry_share_cost
        };

        Cursor[] search_entry = databaseHelper.searchMonthSingle(month, entry_cols, share_cols, entry_cols[0], share_sel, entry_args, share_args);

        if (search_entry[0] != null) {
            for(int i = 0; i < vals.length; i++) {
                vals[i] = search_entry[0].getString(i);
            }
            search_entry[0].close();
        }

        itemView.setText(vals[3]);
        costView.setText(vals[4]);
        dateView.setText(vals[5]);
        transactionTypeView.setText(vals[6]);
        descriptionView.setText(vals[7]);

        if (search_entry[1] != null) {
            Log.i(TAG, "populateEntryDetails");
            ListCursorAdapter listCursorAdapter = new ListCursorAdapter(EntriesView.this, R.layout.single_share_entry,
                    search_entry[1], share_projections, toView, 0);

            shareListView.setAdapter(listCursorAdapter);
        }
    }

    private void delete() {
        delete_view.setVisibility(View.VISIBLE);
        delete_view.bringToFront();
        deleteBtnEnable();
        entryBtnDisable();

        deleteCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        deleteDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void edit() {

    }

    private void cancel() {
        delete_view.setVisibility(View.INVISIBLE);
        deleteBtnDisable();
        entryBtnEnable();
    }

    private void deleteBtnDisable() {
        deleteDeleteBtn.setClickable(false);
        deleteCancelBtn.setClickable(false);
    }

    private void deleteBtnEnable() {
        deleteCancelBtn.setClickable(true);
        deleteDeleteBtn.setClickable(true);
    }

    private void entryBtnDisable() {
        entryDeleteBtn.setClickable(false);
        entryCancelBtn.setClickable(false);
        entryEditBtn.setClickable(false);
    }

    private void entryBtnEnable() {
        entryEditBtn.setClickable(true);
        entryCancelBtn.setClickable(true);
        entryDeleteBtn.setClickable(true);
    }

    private void exit() {
        finish();
    }
}
