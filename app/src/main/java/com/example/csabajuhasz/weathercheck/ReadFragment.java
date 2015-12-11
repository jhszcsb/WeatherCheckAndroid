package com.example.csabajuhasz.weathercheck;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ReadFragment extends Fragment {

    public ReadFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_read, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        // move floating action button to fragment_read

        /*FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        WeatherCheckDbHelper mDbHelper = new WeatherCheckDbHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseContract.SubscriptionEntry._ID,
                DatabaseContract.SubscriptionEntry.COLUMN_NAME_CITY,
                DatabaseContract.SubscriptionEntry.COLUMN_NAME_EMAIL,
                DatabaseContract.SubscriptionEntry.COLUMN_NAME_TEMPERATURE
        };

        String sortOrder = DatabaseContract.SubscriptionEntry._ID + " ASC";

        Cursor cursor = db.query(
                DatabaseContract.SubscriptionEntry.TABLE_NAME,  // The table to query
                projection,                                     // The columns to return
                null,                                           // The columns for the WHERE clause
                null,                                           // The values for the WHERE clause
                null,                                           // don't group the rows
                null,                                           // don't filter by row groups
                sortOrder                                       // The sort order
        );

        List<Subscription> subscriptions = new ArrayList<>();

        while (cursor.moveToNext()) {
            Subscription subscription = new Subscription(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.SubscriptionEntry._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SubscriptionEntry.COLUMN_NAME_CITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.SubscriptionEntry.COLUMN_NAME_EMAIL)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.SubscriptionEntry.COLUMN_NAME_TEMPERATURE))
            );
            subscriptions.add(subscription);
        }

        final ListView listView = (ListView) rootView.findViewById(R.id.display_data);

        ArrayAdapter<Subscription> adapter = new ArrayAdapter<Subscription>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, subscriptions);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                String itemValue = (String) listView.getItemAtPosition(position).toString();
                Toast.makeText(getActivity().getApplicationContext(),
                        "Position :" + itemPosition + "  Subscription : " + itemValue,
                        Toast.LENGTH_LONG).show();
            }
        });

        return rootView;
    }

}
