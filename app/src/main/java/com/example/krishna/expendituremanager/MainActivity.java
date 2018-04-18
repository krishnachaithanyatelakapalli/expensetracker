package com.example.krishna.expendituremanager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    DatabaseHelper databaseHelper;
    HeaderData headerData = new HeaderData();

    /*
     * Elements from activity_main.xml
     */
    Cursor listCursor, expense_cursor;
    ListView month_list, monthlyView;
    YearViewAdapter monthCursorAdapter;
    MonthViewAdapter simpleExpenseCursorAdapter;
    Spinner account_spinner;
    ImageButton addNewEntry;
    ImageButton accountEdit;

    /*
     * Elements from context_main.xml
     */
    TextView emptyView;

    /*
     * Elements from context_add_new_year.xml
     */
    Button yearCreate;
    Button cancel;
    Spinner accountPicker;
    ConstraintLayout add_new_year;

    String accountName = headerData.getAccount(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);

        init();

        addNewEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewYear();
            }
        });

        accountEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        account_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setAccountName(position);
                populateYearList();
                populateMonthList(databaseHelper.getWritableDatabase());
                Log.d(TAG, "onItemSelected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        monthlyView = findViewById(R.id.month_list);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        populateYearList();
    }

    class YearViewAdapter extends SimpleCursorAdapter {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);

            month_list = view.findViewById(R.id.month_view);
            TextView year = view.findViewById(R.id.year_name);
            final String Year = year.getText().toString();

            try {
                populateMonthList(db);
                simpleExpenseCursorAdapter.setYear(Year);
            } catch (NullPointerException e) {
                Log.e(TAG, "bindView: " + e.getMessage());
            }

        }

        YearViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }
    }

    class MonthViewAdapter extends SimpleCursorAdapter {
        String year;
        String month;

        void setYear(String year) {
            this.year = year;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);

            Log.d(TAG, "bindView: " + cursor.getString(1));

            final TextView[] textViews = {
                    view.findViewById(R.id.jan_month),
                    view.findViewById(R.id.feb_month),
                    view.findViewById(R.id.mar_month),
                    view.findViewById(R.id.apr_month),
                    view.findViewById(R.id.may_month),
                    view.findViewById(R.id.jun_month),
                    view.findViewById(R.id.jul_month),
                    view.findViewById(R.id.aug_month),
                    view.findViewById(R.id.sept_month),
                    view.findViewById(R.id.oct_month),
                    view.findViewById(R.id.nov_month),
                    view.findViewById(R.id.dec_month)
            };

            View.OnClickListener viewClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.jan_month:
                            month = textViews[0].getText().toString();
                            break;
                        case R.id.feb_month:
                            month = textViews[1].getText().toString();
                            break;
                        case R.id.mar_month:
                            month = textViews[2].getText().toString();
                            break;
                        case R.id.apr_month:
                            month = textViews[3].getText().toString();
                            break;
                        case R.id.may_month:
                            month = textViews[4].getText().toString();
                            break;
                        case R.id.jun_month:
                            month = textViews[5].getText().toString();
                            break;
                        case R.id.jul_month:
                            month = textViews[6].getText().toString();
                            break;
                        case R.id.aug_month:
                            month = textViews[7].getText().toString();
                            break;
                        case R.id.sept_month:
                            month = textViews[8].getText().toString();
                            break;
                        case R.id.oct_month:
                            month = textViews[9].getText().toString();
                            break;
                        case R.id.nov_month:
                            month = textViews[10].getText().toString();
                            break;
                        case R.id.dec_month:
                            month = textViews[11].getText().toString();
                            break;
                    }
                    Intent intent = new Intent(MainActivity.this, MonthView.class);
                    intent.putExtra("month", month);
                    intent.putExtra("year", year);
                    intent.putExtra("accountName", accountName);
                    startActivity(intent);
                }
            };

            for (TextView textView : textViews) {
                textView.setOnClickListener(viewClickListener);
            }
        }

        MonthViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }
    }

    private void init() {
        mainInit();
        addNewYearInit();
    }

    private void mainInit() {
        account_spinner = findViewById(R.id.account_spinner);
        addNewEntry = findViewById(R.id.create);
        emptyView = findViewById(R.id.emptyList);
        accountEdit = findViewById(R.id.account_edit);
    }

    private void addNewYearInit() {
        cancel = findViewById(R.id.new_year_cancel);
        yearCreate = findViewById(R.id.new_year_create);
        accountPicker = findViewById(R.id.account_picker);
        add_new_year = findViewById(R.id.add_new_year);
        add_new_year.setVisibility(View.INVISIBLE);
    }

    private void populateYearList() {
        String[] yearly_projections = {
                "_year"
        };

        int[] toViewMonthly = {
                R.id.year_name
        };

        String[] yearly_columns = {
                "_id",
                "_year",
                "_account_name"
        };

        String[] args = {
                accountName
        };

        listCursor = databaseHelper.searchYearly(yearly_columns, yearly_columns[2], args, yearly_columns[1]);

        if (listCursor != null) {
            emptyView.setVisibility(View.INVISIBLE);
        }

        monthCursorAdapter = new YearViewAdapter(MainActivity.this,
                R.layout.single_year_list,
                listCursor,
                yearly_projections,
                toViewMonthly,0);
        if (monthlyView != null) {
            monthlyView.setAdapter(monthCursorAdapter);
        }

        Log.i(TAG, "populateYearList");
    }

    private void populateMonthList(SQLiteDatabase db) {
        String[] monthly_projections = headerData.getAllMonthColumns();

        int[] toViewMonthly= {
                R.id.jan_total_cost,
                R.id.feb_total_cost,
                R.id.mar_total_cost,
                R.id.apr_total_cost,
                R.id.may_total_cost,
                R.id.jun_total_cost,
                R.id.jul_total_cost,
                R.id.aug_total_cost,
                R.id.sept_total_cost,
                R.id.oct_total_cost,
                R.id.nov_total_cost,
                R.id.dec_total_cost
        };

        String[] monthly_columns = {
                "_id",
                "_account_name",
                "_year",
                headerData.getMonthColumns(0),
                headerData.getMonthColumns(1),
                headerData.getMonthColumns(2),
                headerData.getMonthColumns(3),
                headerData.getMonthColumns(4),
                headerData.getMonthColumns(5),
                headerData.getMonthColumns(6),
                headerData.getMonthColumns(7),
                headerData.getMonthColumns(8),
                headerData.getMonthColumns(9),
                headerData.getMonthColumns(10),
                headerData.getMonthColumns(11)
        };

        String[] args = {
                accountName
        };

        expense_cursor = databaseHelper.searchMonthly(monthly_columns, monthly_columns[1], args);

        if (expense_cursor != null) {
            Log.i(TAG, "populateMonthList: cursor not empty");
            simpleExpenseCursorAdapter = new MonthViewAdapter(MainActivity.this,
                    R.layout.single_month_list,
                    expense_cursor,
                    monthly_projections,
                    toViewMonthly,0);
            if (month_list != null) {
                month_list.setAdapter(simpleExpenseCursorAdapter);
            }
        }

        Log.i(TAG, "populateMonthList " + this.accountName);
    }

    private void setAccountName(int index){
        this.accountName = headerData.getAccount(index);
        Log.d(TAG, "setAccountName: " + accountName);
    }

    private void createNewYear() {
        add_new_year.setVisibility(View.VISIBLE);
        add_new_year.bringToFront();
        accountPicker.setClickable(true);
        yearCreate.setClickable(true);
        cancel.setClickable(true);

        accountPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setAccountName(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final NumberPicker yearPicker = findViewById(R.id.year_picker);
        yearPicker.setMinValue(2016);
        yearPicker.setMaxValue(2020);
        yearPicker.setWrapSelectorWheel(false);

        yearCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = yearPicker.getValue();
                databaseHelper.insertNewYearEntry("" + year, accountName);
                populateYearList();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_new_year.setVisibility(View.INVISIBLE);
                disablePromptElements();
            }
        });
    }

    private void disablePromptElements() {
        accountPicker.setClickable(false);
        yearCreate.setClickable(false);
        cancel.setClickable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        populateYearList();
        populateMonthList(databaseHelper.getReadableDatabase());
    }
}
