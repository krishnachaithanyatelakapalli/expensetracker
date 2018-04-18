package com.example.krishna.expendituremanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DecimalFormat;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DBName = "Expenditure.db";
    private static int currentVersion = 1;
    
    public static final String Yearly = "Yearly";
    public static final String Monthly_Totals = "MonthlyTotals";
    public static final String Share_Table = "ShareTable";
    
    private HeaderData headerData = new HeaderData();

    private SQLiteDatabase ReadableDB = this.getReadableDatabase();
    private SQLiteDatabase WritableDB = this.getWritableDatabase();

    DatabaseHelper(Context context) {
        super(context, DBName, null, currentVersion);
        createNewYearTable(WritableDB);
        createNewMonthTable(WritableDB);
        createNewShareTable(WritableDB);
        Log.i(TAG, "DatabaseHelper: creating tables");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*
     * TABLE CREATION
     */

    private void createNewYearTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + Yearly + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_year TEXT, _account_name TEXT);";
        try {
            db.execSQL(createTable);
            Log.i(TAG, "createNewYearTable");
        } catch (SQLiteException e) {
            Log.e(TAG, "createNewYearTable: " + e.getMessage());
        }
    }

    private void createNewMonthTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + Monthly_Totals + " ( _id TEXT PRIMARY KEY, _account_name TEXT, _year INTEGER, " +
                headerData.getMonthColumns(0) + " TEXT," +
                headerData.getMonthColumns(1) + " TEXT," +
                headerData.getMonthColumns(2) + " TEXT," +
                headerData.getMonthColumns(3) + " TEXT," +
                headerData.getMonthColumns(4) + " TEXT," +
                headerData.getMonthColumns(5) + " TEXT," +
                headerData.getMonthColumns(6) + " TEXT," +
                headerData.getMonthColumns(7) + " TEXT," +
                headerData.getMonthColumns(8) + " TEXT," +
                headerData.getMonthColumns(9) + " TEXT," +
                headerData.getMonthColumns(10) + " TEXT," +
                headerData.getMonthColumns(11) + " TEXT);";
        try {
            db.execSQL(createTable);
            Log.i(TAG, "createNewMonthTable");
            for(int i = 0; i < 12; i++) {
                createNewExpenseTable(db, headerData.getMonth(i + 1));
            }
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void createNewExpenseTable(SQLiteDatabase db, String month) {
        String createTable = "CREATE TABLE IF NOT EXISTS "+ month + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_yearID REFERENCES " + Monthly_Totals + "(_id), _account_name TEXT, _date TEXT, _item TEXT, _transactionType TEXT, _cost TEXT, " +
                "_description TEXT, _share TEXT);";
        try {
            db.execSQL(createTable);
            Log.i(TAG, "createNewExpenseTable");
        }catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void createNewShareTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + Share_Table + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_account_name TEXT, _month TEXT, _entryID INTEGER, _name TEXT, _price TEXT);";
        try {
            db.execSQL(createTable);
        } catch (SQLiteException e) {
            Log.e(TAG, "createNewShareTable: " + e.getMessage());
        }
    }

    /*
     * INSERTION
     */

    public long insertNewExpenseEntry(int year, String accountName,
                                      String month, String date, String item, String transactionType, String cost, String description, String share) {
        String yearID = getYearId(year, accountName);

        long newId = -1;

        ContentValues contentValues = new ContentValues();
        contentValues.put("_yearID", yearID);
        contentValues.put("_account_name", accountName);
        contentValues.put("_date", date);
        contentValues.put("_item", item);
        contentValues.put("_transactionType", transactionType);
        contentValues.put("_cost", cost);
        contentValues.put("_description", description);
        contentValues.put("_share", share);

        try {
            newId = WritableDB.insert(month, null, contentValues);
            Log.d(TAG, "insertNewExpenseEntry");
            calculate_total_cost(""+year, month);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }

        return newId;
    }

    private void insertNewMonthEntry(int year, String accountName, String[] total_cost) {
        String insertNewMonth = "INSERT INTO " + Monthly_Totals + " (_id, _account_name, _year, " +
                headerData.getMonthColumns(0) + ", " +
                headerData.getMonthColumns(1) + ", " +
                headerData.getMonthColumns(2) + ", " +
                headerData.getMonthColumns(3) + ", " +
                headerData.getMonthColumns(4) + ", " +
                headerData.getMonthColumns(5) + ", " +
                headerData.getMonthColumns(6) + ", " +
                headerData.getMonthColumns(7) + ", " +
                headerData.getMonthColumns(8) + ", " +
                headerData.getMonthColumns(9) + ", " +
                headerData.getMonthColumns(10) + ", " +
                headerData.getMonthColumns(11) + ") " +
                " VALUES(\'" + accountName + "_" + year + "\', \'" + accountName + "\', \'" + year + "\', \'" +
                total_cost[0] + "\', \'" + total_cost[1] + "\', \'" + total_cost[2] + "\', \'" +
                total_cost[3] + "\', \'" + total_cost[4] + "\' ,\'" + total_cost[5] + "\', \'" +
                total_cost[6] + "\', \'" + total_cost[7] + "\', \'" + total_cost[8] + "\', \'" +
                total_cost[9] + "\', \'" + total_cost[10] + "\', \'" + total_cost[11] + "\');";
        try {
            WritableDB.execSQL(insertNewMonth);
            Log.i(TAG, "insertNewMonthEntry" + insertNewMonth);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void insertNewYearEntry(String year, String accountName) {
        String[] total_costs = {
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00",
                "$ 0.00"
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put("_year", year);
        contentValues.put("_account_name", accountName);

        try {
            long newID = WritableDB.insert(Yearly, null, contentValues);
            Log.i(TAG, "insertNewYearEntry");
            insertNewMonthEntry(Integer.parseInt(year), accountName, total_costs);
        } catch (SQLiteException e) {
            Log.e(TAG, "insertNewYearEntry: " + e.getMessage());
        }
    }

    public void insertNewShareEntry(String accountName, String month, int id, String name, String price) {
        String insertNewShare = "INSERT INTO " + Share_Table + "(_account_name, _month, _entryID, _name, _price) " +
                "VALUES(\'" + accountName + "\', \'" + month + "\', \'" + id + "\', \'" + name + "\', \'" + price + "\');";
        try {
            WritableDB.execSQL(insertNewShare);
        } catch (SQLiteException e) {
            Log.e(TAG, "insertNewShareEntry: " + e.getMessage());
        }
    }

    public Cursor searchYearly(String[] columns, String selection, String[] args, String orderBy) {
        Cursor searchYearly = ReadableDB.query(Yearly, columns, selection + " LIKE '%" + args[0] + "%'", null, null, null, orderBy);
        if (searchYearly.moveToNext()) {
            searchYearly.moveToFirst();
            return searchYearly;
        }
        return null;
    }

    public Cursor searchMonthly(String[] columns, String selection, String[] args) {
        Cursor searchMonthly = ReadableDB.query(Monthly_Totals, columns, selection + " LIKE '%" + args[0] + "%'", null, null, null, null);
        if (searchMonthly.moveToNext()) {
            searchMonthly.moveToFirst();
            return searchMonthly;
        }
        return null;
    }

    public Cursor searchMonth(String month, String[] columns, String[] selections, String[] args) {
        Cursor searchMonth = ReadableDB.query(month, columns, "(" + selections[0] + " LIKE '%" + args[0] + "%') AND (" + selections[1] + " LIKE '%" + args[1] + "%')", null,null,null, null);
        searchMonth.moveToFirst();
        if (searchMonth.moveToNext()) {
            searchMonth.moveToFirst();
            return searchMonth;
        }
        return null;
    }

    public Cursor[] searchMonthSingle(String month,
                                    String[] entry_cols, String[] share_cols,
                                    String entry_selection, String[] share_selections,
                                    String[] entry_args, String[] share_args) {
        Cursor[] response = new Cursor[2];

        Cursor searchMonthSingle = ReadableDB.query(month, entry_cols, entry_selection + " LIKE '%" + entry_args[0] + "%'", null, null, null, null);

        if (searchMonthSingle.moveToNext()) {
            searchMonthSingle.moveToFirst();
            response[0] = searchMonthSingle;
            response[1] = null;
            searchMonthSingle.moveToFirst();
            Log.d(TAG, "searchMonthSingle: " + searchMonthSingle.getString(8));
            searchMonthSingle.moveToFirst();
            if (searchMonthSingle.getString(8).equals("Sharing With")) {
                Cursor searchShare = ReadableDB.query(Share_Table, share_cols, "(" + share_selections[0] + " LIKE '%" + share_args[0] + "%') AND " +
                        "(" + share_selections[1] + " LIKE '%" + share_args[1] + "%') AND (" + share_selections[2] + " LIKE '%" + share_args[2] + "%')", null, null, null, null);
                searchShare.moveToFirst();
                response[1] = searchShare;
            }
            return response;
        }
        return null;
    }

    private void calculate_total_cost(String year, String month) {
        Log.d(TAG, "calculate_total_cost: [month] " + month + "; [year] " + year);
        Double curr, total_cost = 0.00;

        String[] expense_cols = {
                "_transactionType",
                "_cost"
        };

        Cursor expense = ReadableDB.query(month, expense_cols, null, null, null, null, null);
        while (expense.moveToNext()) {
            curr = Double.parseDouble(expense.getString(1).split(" ")[1]);
            if (expense.getString(0).equals("Dr")) {
                total_cost = total_cost + curr;
            }
            if (expense.getString(0).equals("Cr")) {
                total_cost = total_cost - curr;
            }
        }
        expense.close();
        total_cost = Double.parseDouble(new DecimalFormat("####.##").format(total_cost));

        String update = "UPDATE " + Monthly_Totals +
                " SET " + getMonthCols(month) + "=\'$ " + total_cost.toString() +
                "\' WHERE _year=\'" + year + "\';";
        try {
            Log.d(TAG, "calculate_total_cost: " + update);
            WritableDB.execSQL(update);
        } catch (SQLiteException e) {
            Log.e(TAG, "calculate_total_cost: " + e.getMessage());
        }
    }

    private String getMonthCols(String month) {
        return headerData.getMonthColumns(headerData.getIndex(month));
    }

    private String getYearId(int year, String accountName) {
        String result="";
        String[] args = {
                accountName,
                "" + year
        };

        String[] cols = {
                "_id",
                "_account_name",
                "_year"
        };

        Cursor search = ReadableDB.query(Monthly_Totals, cols,"(_account_name LIKE '%" + args[0] + "%') AND (_year LIKE '%" + "" + args[1] +"%')", null,null,null,null);
        search.moveToFirst();
        result = search.getString(0);
        Log.i(TAG, "getYearId: " + result);
        search.close();
        return result;
    }
}
