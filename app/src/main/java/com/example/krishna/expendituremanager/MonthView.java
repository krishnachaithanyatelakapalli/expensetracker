package com.example.krishna.expendituremanager;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MonthView extends AppCompatActivity {

    private static final String TAG = "MonthView";
    private String transaction_type = "Dr";
    private String share = "Not Sharing";
    private String date = "";
    private String month = "";

    /*
     * Elements in activity_month_view.xml
     */
    TextView monthName, monthTotalCost;
    Button createEntry;
    ListView monthExpenses;
    Cursor expenses;

    /*
     * Elements in context_add_new_entry.xml
     */
    Button save_btn, cancel_btn;
    EditText item_name, cost, description_box, share_name, share_cost;
    RadioButton tt_dr, tt_cr, not_share, share_with;
    DatePicker datePicker;
    ImageButton add_new_share;
    ConstraintLayout add_new_entry_layout;
    ListView shareViewList;

    /*
     * Elements in context_share_entry_edit.xml
     */
    ConstraintLayout entryShareEdit;
    EditText shareName, sharePrice;
    Button shareSave, shareDelete, shareCancel;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd");

    DatabaseHelper databaseHelper;
    HeaderData headerData = new HeaderData();

    String year, month_name, account_name;

    ArrayList<ShareList> shareLists;
    ShareListAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);

        databaseHelper = new DatabaseHelper(this);

        init();

        month_name = this.getIntent().getStringExtra("month");
        year = this.getIntent().getStringExtra("year");
        account_name = this.getIntent().getStringExtra("accountName");

        monthName.setText(month_name);

        add_new_entry_layout.setVisibility(View.INVISIBLE);
        entryShareEdit.setVisibility(View.INVISIBLE);

        createEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewEntry();
            }
        });

        populateMonthList();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        populateMonthList();
        populateTotalCost(month_name, year);
    }

    class ExpensesViewAdapter extends SimpleCursorAdapter {

        LinearLayout expenses_view;
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);

            final String id = cursor.getString(0);
            expenses_view = view.findViewById(R.id.expenses_view);

            expenses_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MonthView.this, EntriesView.class);
                    intent.putExtra("_id", id);
                    intent.putExtra("month", month_name.toUpperCase());
                    intent.putExtra("accountName", account_name);
                    startActivity(intent);
                }
            });
        }

        ExpensesViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }
    }

    class ShareListAdapter extends ArrayAdapter<ShareList> {

        ShareListAdapter(@NonNull Context context, int resource, ArrayList<ShareList> shareListArrayList) {
            super(context, resource, shareListArrayList);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final ShareList shareList = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_share_entry, parent, false);
            }

            TextView entry_share_name = convertView.findViewById(R.id.entry_share_name);
            TextView entry_share_cost = convertView.findViewById(R.id.entry_share_cost);
            LinearLayout entryShareView = convertView.findViewById(R.id.entry_share_view);

            if (shareList != null) {
                entry_share_cost.setText(shareList.getPrice());
                entry_share_name.setText(shareList.getName());
            }

            entryShareView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shareList != null) {
                        shareName.setText(shareList.getName());
                        sharePrice.setText(shareList.getPrice());
                    }
                    enableShareEdit();
                }
            });

            View.OnClickListener shareBtnListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.entry_share_edit_cancel:
                            disableShareEdit();
                            break;

                        case R.id.entry_share_edit_delete:
                            if (shareList != null && shareLists.get(position) != null) {
                                shareLists.remove(position);
                                disableShareEdit();
                                updateShareList();
                            }
                            break;

                        case R.id.entry_share_edit_save:
                            if (shareList != null) {
                                shareList.setName(shareName.getText().toString());
                                shareList.setPrice(sharePrice.getText().toString());
                                disableShareEdit();
                                updateShareList();
                            }
                            break;
                    }
                }
            };

            shareCancel.setOnClickListener(shareBtnListener);
            shareDelete.setOnClickListener(shareBtnListener);
            shareSave.setOnClickListener(shareBtnListener);

            return convertView;
        }
    }

    class ShareList {
        private String name;
        private String price;

        ShareList(String name, String price) {
            this.name = name;
            this.price = price;
        }

        void setName(String name) {
            this.name = name;
        }

        void setPrice(String price) {
            this.price = price;
        }

        String getName() {
            return this.name;
        }

        String getPrice() {
            return this.price;
        }
    }

    private void init() {
        monthViewInit();
        addNewEntryInit();
        editShareEntryInit();
    }

    private void monthViewInit() {
        monthName = findViewById(R.id.month_name);
        monthTotalCost = findViewById(R.id.month_total_cost);
        monthExpenses = findViewById(R.id.month_expenses);
        createEntry = findViewById(R.id.create_entry);
    }

    private void addNewEntryInit() {
        save_btn = findViewById(R.id.newEntry_save_btn);
        cancel_btn = findViewById(R.id.newEntry_cancel_btn);

        item_name = findViewById(R.id.newEntry_item);
        cost = findViewById(R.id.newEntry_cost);
        description_box = findViewById(R.id.newEntry_description);
        share_name = findViewById(R.id.newEntry_share_name);
        share_cost = findViewById(R.id.newEntry_share_cost);
        add_new_share = findViewById(R.id.newEntry_new_share);

        tt_dr = findViewById(R.id.newEntry_transactionType_dr);
        tt_cr = findViewById(R.id.newEntry_transactionType_cr);
        not_share = findViewById(R.id.newEntry_not_sharing);
        share_with = findViewById(R.id.newEntry_sharing_with);
        add_new_entry_layout = findViewById(R.id.add_new_entry);

        datePicker = findViewById(R.id.newEntry_date);
        shareViewList = findViewById(R.id.newEntry_share_list);
    }

    private void editShareEntryInit() {
        entryShareEdit = findViewById(R.id.entry_share_edit_layout);

        shareName = findViewById(R.id.entry_share_edit_name);
        sharePrice = findViewById(R.id.entry_share_edit_cost);

        shareSave = findViewById(R.id.entry_share_edit_save);
        shareDelete = findViewById(R.id.entry_share_edit_delete);
        shareCancel = findViewById(R.id.entry_share_edit_cancel);
    }

    private void disableShareEdit() {
        entryShareEdit.setVisibility(View.INVISIBLE);
        shareSave.setClickable(false);
        shareDelete.setClickable(false);
        shareCancel.setClickable(false);
        enablePromptElements();
    }

    private void enableShareEdit() {
        entryShareEdit.setVisibility(View.VISIBLE);
        entryShareEdit.bringToFront();
        shareCancel.setClickable(true);
        shareDelete.setClickable(true);
        shareSave.setClickable(true);
    }

    void populateMonthList() {
        String[] expense_projections = {
                "_item",
                "_date",
                "_transactionType",
                "_cost"
        };

        String[] expense_cols = {
                "_id",
                "_yearID",
                "_account_name",
                "_date",
                "_item",
                "_transactionType",
                "_cost"
        };

        int[] toViewExpense = {
                R.id.item,
                R.id.date,
                R.id.transactionType,
                R.id.cost
        };

        String[] selections = {
                "_yearID",
                "_account_name"
        };

        String[] args = {
                account_name + "_" + year,
                account_name
        };

        expenses = databaseHelper.searchMonth(month_name.toUpperCase(), expense_cols, selections, args);

        if (expenses != null) {
            ExpensesViewAdapter simpleCursorAdapter = new ExpensesViewAdapter(this,
                    R.layout.single_expenditure_list,
                    expenses,
                    expense_projections,
                    toViewExpense, 0);

            monthExpenses.setAdapter(simpleCursorAdapter);
            Log.i(TAG, "populateMonthList");
        }
    }

    private void populateTotalCost(String month, String year) {

        String[] columns = {
                "_" + month.toLowerCase()
        };

        String[] args = {
                year
        };

        Cursor costCursor = databaseHelper.searchMonthly(columns, "_year", args);
        if (costCursor != null) {
            monthTotalCost.setText(costCursor.getString(0));
            costCursor.close();
        }
    }

    private void createNewEntry() {
        shareLists = new ArrayList<>();
        updateShareList();
        enablePromptElements();
        setUpListeners();

        tt_dr.setChecked(true);
        not_share.setChecked(true);

        final LinearLayout share_view = findViewById(R.id.newEntry_share_view);
        share_view.setVisibility(View.INVISIBLE);

        not_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_view.setVisibility(View.INVISIBLE);
                share = "Not Sharing";
            }
        });

        share_with.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_view.setVisibility(View.VISIBLE);
                share = "Sharing With";
            }
        });

        tt_dr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction_type = "Dr";
            }
        });

        tt_cr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction_type = "Cr";
            }
        });

        Log.d(TAG, "createNewEntry:[share] " + share);
        month = headerData.getMonth(datePicker.getMonth() + 1);
        date = simpleDateFormat.format(new Date(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    month = headerData.getMonth(monthOfYear + 1);
                    date = simpleDateFormat.format(new Date(year, monthOfYear, dayOfMonth));
                }
            });
        }
    }

    private void setUpListeners() {
        View.OnClickListener new_entries_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            switch (v.getId()) {
                case R.id.newEntry_cancel_btn:
                    disablePromptElements();
                    for (int i = 0; i <= shareLists.size(); i++) {
                        shareLists.remove(0);
                    }
                    setListViewHeightBasedOnChildren(shareViewList);
                    break;

                case R.id.newEntry_save_btn:
                    if (item_name.getText().toString().equals("")) {
                        item_name.setError("Field should not be left empty");
                        break;
                    }
                    if (cost.getText().toString().equals("")) {
                        cost.setError("Field should not be left empty");
                        break;
                    }
                    makeExpenseEntry(account_name, month, item_name.getText().toString(),
                            new DecimalFormat("$ ####.##").format(Double.parseDouble(cost.getText().toString())),
                            date, transaction_type, description_box.getText().toString(), share);
                    disablePromptElements();
                    break;

                case R.id.newEntry_new_share:
                    Log.i(TAG, "pressed new share btn");
                    if (share_name.getText().toString().equals("")) {
                        share_name.setError("Field Empty");
                        break;
                    }
                    if (share_cost.getText().toString().equals("")) {
                        share_cost.setError("Field Empty");
                        break;
                    }
                    shareLists.add(new ShareList(share_name.getText().toString(), share_cost.getText().toString()));
                    arrayAdapter = new ShareListAdapter(MonthView.this, R.layout.single_share_entry,
                            shareLists);
                    shareViewList.setAdapter(arrayAdapter);
                    share_name.setText("");
                    share_cost.setText("");
                    setListViewHeightBasedOnChildren(shareViewList);
                    break;

                default:
                    Log.d(TAG, "onClick: " + share);
            }
            }
        };

        save_btn.setOnClickListener(new_entries_listener);
        cancel_btn.setOnClickListener(new_entries_listener);
        tt_dr.setOnClickListener(new_entries_listener);
        tt_cr.setOnClickListener(new_entries_listener);
        add_new_share.setOnClickListener(new_entries_listener);
    }

    private void updateShareList() {
        shareViewList.setAdapter(arrayAdapter);
    }

    private void disablePromptElements() {
        add_new_entry_layout.setVisibility(View.INVISIBLE);
        createEntry.setClickable(true);
        save_btn.setClickable(false);
        cancel_btn.setClickable(false);
        tt_cr.setClickable(false);
        tt_dr.setClickable(false);
    }

    private void enablePromptElements() {
        add_new_entry_layout.setVisibility(View.VISIBLE);
        add_new_entry_layout.bringToFront();
        createEntry.setClickable(false);
        save_btn.setClickable(true);
        cancel_btn.setClickable(true);
        tt_dr.setClickable(true);
        tt_cr.setClickable(true);
    }

    private void makeExpenseEntry(String accountName, String month, String item, String cost, String date, String transaction_type, String description, String share) {
        Log.d(TAG, "makeExpenseEntry: " + accountName);
        long entry_id = databaseHelper.insertNewExpenseEntry(2018, accountName, month, date, item, transaction_type, cost, description, share);
        if (share.equals("Sharing With")) {
            for(ShareList shareList: shareLists) {
                makeShareEntry(accountName, month, "" + entry_id, shareList.getName(), shareList.getPrice());
            }
        }
        populateMonthList();
        populateTotalCost(month_name, year);
    }

    private void makeShareEntry(String accountName, String month, String id, String name, String price) {
        databaseHelper.insertNewShareEntry(accountName, month, Integer.parseInt(id), name, price);
        Log.i(TAG, "makeShareEntry");
    }

    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;

        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ActionBar.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
