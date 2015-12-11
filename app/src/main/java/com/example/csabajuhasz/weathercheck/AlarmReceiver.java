package com.example.csabajuhasz.weathercheck;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AlarmReceiver extends BroadcastReceiver {

    private NotificationManager myNotificationManager;
    private int notificationIdOne = 111;
    private int numMessagesOne = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. query all subscriptions
        List<Subscription> subscriptions = queryAllSubscriptions(context);
        // 2. check weather for subscriptions
        List<Subscription> alerts = checkWeatherForSubscriptions(subscriptions);
        // 3. display notification
        displayNotification(context, alerts);
    }

    private List<Subscription> queryAllSubscriptions(Context context) {
        WeatherCheckDbHelper mDbHelper = new WeatherCheckDbHelper(context);
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

        return subscriptions;
    }

    private List<Subscription> checkWeatherForSubscriptions(List<Subscription> subscriptions) {

        List<Subscription> alerts = new ArrayList<>();

        for(Subscription subscription : subscriptions) {

            String urlString =
                    "http://api.openweathermap.org/data/2.5/weather?q="
                            + subscription.getCity() +
                            "&appid=e4fae2a532adb3050d285b5e42afee1b";

            DownloadFromWebTask task = new DownloadFromWebTask();
            String currentTemperatureString = "";   // todo error handling
            try {
                currentTemperatureString = parseTemperature(task.execute(urlString).get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            double currentTemperature = formatTemperature(currentTemperatureString);
            // todo: error handling
            if(Double.compare(currentTemperature, subscription.getTemperature()) >= 0) {
                alerts.add(subscription);
            }
        }

        return alerts;
    }

    private void displayNotification(Context context, List<Subscription> subscriptions) {
        String notificationMessage = createNotificationMessage(subscriptions);
        createNotification(context, notificationMessage);
    }

    private String createNotificationMessage(List<Subscription> subscriptions) {
        StringBuffer msg = new StringBuffer();
        String eol = System.getProperty("line.separator");
        msg.append(eol + eol + "The weather is above the subscribed values in the following locations: " + eol + eol);
        for(Subscription subscription : subscriptions) {
            msg.append("Subscription: " + subscription.getCity() + " " + subscription.getTemperature() + " C" + eol);
        }
        return msg.toString();
    }

    private void createNotification(Context context, String notificationMessage) {
        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(context);

        mBuilder.setContentTitle("WeatherCheck Notification");
        mBuilder.setContentText("New weather notifications");
        mBuilder.setTicker("New Weather Updates Received!");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);

        // Increase notification number every time a new notification arrives
        mBuilder.setNumber(++numMessagesOne);

        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        //mBuilder.setDefaults(Notification.FLAG_AUTO_CANCEL);
        //mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MyNotification.class);
        resultIntent.putExtra("notificationId", notificationIdOne);
        resultIntent.putExtra("notificationMessage", notificationMessage);

        //This ensures that navigating backward from the Activity leads out of the app to Home page
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent
        stackBuilder.addParentStack(MyNotification.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_ONE_SHOT //can only be used once
                );
        // start the activity when the user clicks the notification text
        mBuilder.setContentIntent(resultPendingIntent);

        myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // pass the MyNotification object to the system
        myNotificationManager.notify(notificationIdOne, mBuilder.build());
    }





    // TODO: this is partly duplicated in MainActivity

    class DownloadFromWebTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (Exception e) {
                return "Unable to retrieve web page.";
            }
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            return "No result!";
        }
    }

    private String parseTemperature(String result) {
        JSONObject jsonObj;
        String temperature = "";
        try {
            jsonObj = new JSONObject(result);
            JSONObject main = jsonObj.getJSONObject("main");
            temperature = main.getString("temp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return temperature;
    }

    private double formatTemperature(String temperature) {
        double tempInCelsius = Double.parseDouble(temperature);
        tempInCelsius -= 273.15;
        return tempInCelsius;
    }

}