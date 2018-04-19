package com.example.krishna.expendituremanager;

import android.content.Context;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
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
     * Elements from context_edit_entry.xml
     */
    Button edit_save_btn, edit_cancel_btn;
    EditText edit_item_name, edit_item_cost, edit_description_box, edit_share_name, edit_share_cost;
    RadioButton edit_tt_dr, edit_tt_cr, edit_not_share, edit_share_with;
    DatePicker edit_datePicker;
    ImageButton edit_add_new_share;
    ConstraintLayout edit_entry_layout;
    ListView edit_shareViewList;

    /*
     * Elements from context_delete_entry.xml
     */
    Button deleteCancelBtn, deleteDeleteBtn;
    ConstraintLayout delete_view;

    DatabaseHelper databaseHelper;

    String edit_transactionType = "";
    String edit_share = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entries_view);

        databaseHelper = new DatabaseHelper(this);

        init();

        final String id = this.getIntent().getStringExtra("_id");
        final String month = this.getIntent().getStringExtra("month");
        final String accountName = this.getIntent().getStringExtra("accountName");

        delete_view.setVisibility(View.INVISIBLE);
        edit_entry_layout.setVisibility(View.INVISIBLE);

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

        entryEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit(id, month, accountName);
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
        editEntryInit();
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

    private void editEntryInit() {
        edit_save_btn = findViewById(R.id.edit_entry_save_btn);
        edit_cancel_btn = findViewById(R.id.edit_entry_cancel_btn);

        edit_item_name = findViewById(R.id.edit_entry_item);
        edit_item_cost = findViewById(R.id.edit_entry_cost);
        edit_description_box = findViewById(R.id.edit_entry_description);
        edit_share_name = findViewById(R.id.edit_entry_share_name);
        edit_share_cost = findViewById(R.id.edit_entry_share_cost);
        edit_add_new_share = findViewById(R.id.edit_entry_new_share);

        edit_tt_dr = findViewById(R.id.edit_entry_transactionType_dr);
        edit_tt_cr = findViewById(R.id.edit_entry_transactionType_cr);
        edit_not_share = findViewById(R.id.edit_entry_not_sharing);
        edit_share_with = findViewById(R.id.edit_entry_sharing_with);
        edit_entry_layout = findViewById(R.id.edit_entry);

        edit_datePicker = findViewById(R.id.edit_entry_date);
        edit_shareViewList = findViewById(R.id.edit_entry_share_list);
    }

    private void deleteViewInit() {
        delete_view = findViewById(R.id.entry_delete_view);
        deleteCancelBtn = findViewById(R.id.entry_delete_cancel_btn);
        deleteDeleteBtn = findViewById(R.id.entry_delete_delete_btn);
    }

    private Cursor[] getEntryDetails(String id, String month, String accountName) {
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


        return databaseHelper.searchMonthSingle(month, entry_cols, share_cols, entry_cols[0], share_sel, entry_args, share_args);
    }

    private void populateEntryDetails(String id, String month, String accountName) {
        String[] share_projections = {
                "_name",
                "_price"
        };

        int[] toView = {
                R.id.entry_share_name,
                R.id.entry_share_cost
        };

        Cursor[] response = getEntryDetails(id, month, accountName);

        if (response[0] != null) {
            response[0].moveToFirst();
            itemView.setText(response[0].getString(3));
            costView.setText(response[0].getString(4));
            dateView.setText(response[0].getString(5));
            transactionTypeView.setText(response[0].getString(6));
            descriptionView.setText(response[0].getString(7));
        }

        if (response[1] != null) {
            Log.i(TAG, "populateEntryDetails");
            ListCursorAdapter listCursorAdapter = new ListCursorAdapter(EntriesView.this, R.layout.single_share_entry,
                    response[1], share_projections, toView, 0);

            shareListView.setAdapter(listCursorAdapter);
        }
    }

    private void populateEditLayout(String id, String month, String accountName) {
        String[] share_projections = {
                "_name",
                "_price"
        };

        int[] toView = {
                R.id.entry_share_name,
                R.id.entry_share_cost
        };

        Cursor[] response = getEntryDetails(id, month, accountName);

        if (response[0] != null) {
            edit_item_name.setText(response[0].getString(3));
            edit_item_cost.setText(response[1].getString(4));

            edit_description_box.setText(response[0].getString(7));

            if (response[0].getString(6).equals("Dr")) {
                edit_tt_dr.setChecked(true);
            } else {
                edit_tt_cr.setChecked(true);
            }

            if (response[0].getString(8).equals("Not Sharing")) {
                edit_not_share.setChecked(true);
            } else if (response[0].getString(8).equals("Sharing With")) {
                edit_share_with.setChecked(true);
            }
        }

        if (response[1] != null) {
            Log.i(TAG, "populateEditLayout");
            ListCursorAdapter listCursorAdapter = new ListCursorAdapter(EntriesView.this, R.layout.single_share_entry,
                    response[1], share_projections, toView, 0);

            edit_shareViewList.setAdapter(listCursorAdapter);
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

    private void edit(String id, String month, String accountName) {
        enableEditLayout();
        setupListeners();
        populateEditLayout(id, month, accountName);
    }

    private void enableEditLayout() {
        edit_entry_layout.setVisibility(View.VISIBLE);
        edit_entry_layout.bringToFront();

        edit_item_name.setClickable(true);
        edit_item_cost.setClickable(true);
        edit_tt_dr.setClickable(true);
        edit_tt_cr.setClickable(true);
        edit_share_with.setClickable(true);
        edit_not_share.setClickable(true);
        edit_description_box.setClickable(true);
        edit_datePicker.setClickable(true);
    }

    private void disableEditLayout() {
        edit_entry_layout.setVisibility(View.INVISIBLE);

        edit_item_name.setClickable(false);
        edit_item_cost.setClickable(false);
        edit_tt_dr.setClickable(false);
        edit_tt_cr.setClickable(false);
        edit_share_with.setClickable(false);
        edit_not_share.setClickable(false);
        edit_description_box.setClickable(false);
        edit_datePicker.setClickable(false);
    }

    private void setupListeners() {
        View.OnClickListener editEntryListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.edit_entry_transactionType_dr:
                        edit_transactionType = "Dr";
                        break;

                    case R.id.edit_entry_transactionType_cr:
                        edit_transactionType = "Cr";
                        break;

                    case R.id.edit_entry_sharing_with:
                        edit_share = "Sharing With";
                        break;

                    case R.id.edit_entry_not_sharing:
                        edit_share = "Not Sharing";
                        break;

                    case R.id.edit_entry_save_btn:

                        break;

                    case R.id.edit_entry_cancel_btn:
                        disableEditLayout();
                        break;
                }
            }
        };

        edit_tt_dr.setOnClickListener(editEntryListener);
        edit_tt_cr.setOnClickListener(editEntryListener);
        edit_share_with.setOnClickListener(editEntryListener);
        edit_not_share.setOnClickListener(editEntryListener);
        edit_save_btn.setOnClickListener(editEntryListener);
        edit_cancel_btn.setOnClickListener(editEntryListener);
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
