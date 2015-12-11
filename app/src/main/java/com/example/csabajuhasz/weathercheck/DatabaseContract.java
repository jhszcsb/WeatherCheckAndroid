package com.example.csabajuhasz.weathercheck;

import android.provider.BaseColumns;

public class DatabaseContract {

    public DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static abstract class SubscriptionEntry implements BaseColumns {
        public static final String TABLE_NAME = "subscription";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_CITY = "city";
        public static final String COLUMN_NAME_TEMPERATURE = "temperature";
    }
}
