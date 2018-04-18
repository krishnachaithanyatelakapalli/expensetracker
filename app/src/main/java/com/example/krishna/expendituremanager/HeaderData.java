package com.example.krishna.expendituremanager;

import android.util.Log;

public class HeaderData {
    private static final String TAG = "HeaderData";
    private static final String[] months = {
            "JANUARY",
            "FEBRUARY",
            "MARCH",
            "APRIL",
            "MAY",
            "JUNE",
            "JULY",
            "AUGUST",
            "SEPTEMBER",
            "OCTOBER",
            "NOVEMBER",
            "DECEMBER"
    };

    private static final String[] months_rep = {
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sept",
            "Oct",
            "Nov",
            "Dec"
    };

    private static final String[] months_columns = {
            "_january",
            "_february",
            "_march",
            "_april",
            "_may",
            "_june",
            "_july",
            "_august",
            "_september",
            "_october",
            "_november",
            "_december"
    };

    private static final String[] accounts = {
            "Checking",
            "Saving",
            "Credit_Card"
    };

    public static String getMonth(int index) {
        try {
            return months[index-1];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public String getMonthRep(int index) {
        try {
            return months_rep[index - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getMonthRep: " + e.getMessage());
        }
        return null;
    }

    public String getMonthColumns(int index) {
        try {
            return months_columns[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getMonthColumns: " + e.getMessage());
        }
        return null;
    }

    public String[] getAllMonthColumns() {
        return months_columns;
    }

    public int getIndex(String month) {
        int index = 0;
        for(int i = 0; i < months.length; i++) {
            if(months[i].equals(month)) {
                index = i;
            }
        }
        return index;
    }

    public String getAccount(int index) {
        try {
            return accounts[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getAccount: " + e.getMessage());
        }
        return null;
    }
}
