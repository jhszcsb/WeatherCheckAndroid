package com.example.csabajuhasz.weathercheck;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

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

public class CreateFragment extends Fragment {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    public CreateFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_create, container, false);

        return rootView;
    }


    /** Called when the user clicks the Send button */
    public void sendCity(View view) {

        //displayNotification();

        EditText editText = (EditText) getView().findViewById(R.id.edit_message);
        String city = editText.getText().toString().trim();
        EditText editTextEmail = (EditText) getView().findViewById(R.id.edit_email);
        String email = editTextEmail.getText().toString();
        EditText editTextTemperature = (EditText) getView().findViewById(R.id.edit_temperature);
        String temp = editTextTemperature.getText().toString();
        double temperature;
        if("".equals(temp.trim())) {
            temperature = 0.0;
        }
        else {
            temperature = Double.parseDouble(temp);
        }

        String urlString =
                        "http://api.openweathermap.org/data/2.5/weather?q="
                        + city +
                        "&appid=e4fae2a532adb3050d285b5e42afee1b";
        new DownloadFromWebTask().execute(urlString);

        WeatherCheckDbHelper mDbHelper = new WeatherCheckDbHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SubscriptionEntry.COLUMN_NAME_CITY, city);
        values.put(DatabaseContract.SubscriptionEntry.COLUMN_NAME_EMAIL, email);
        values.put(DatabaseContract.SubscriptionEntry.COLUMN_NAME_TEMPERATURE, temperature);
        long newRowId = db.insert(DatabaseContract.SubscriptionEntry.TABLE_NAME, null, values);

        setAlarm();
    }



    private void setAlarm() {
        alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intent, 0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        5 * 1000, alarmIntent);

        // todo 0: set up an alarm starting button

        // todo 1: set alarm periodically

        // http://developer.android.com/training/scheduling/alarms.html

        /*

        // Hopefully your alarm will have a lower frequency than this!
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        AlarmManager.INTERVAL_HALF_HOUR,
        AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);

         */


        // todo 2: set up an alarm cancelling button
        /*

        Canceling:

        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        */
    }

    class DownloadFromWebTask extends AsyncTask<String, Void, String> {

        TextView textView = (TextView) getView().findViewById(R.id.display_message);

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (Exception e) {
                return "Unable to retrieve web page.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String temperature = "";
            temperature = parseTemperature(result, temperature);
            temperature = formatTemperature(temperature);
            displayTemperature(temperature);
        }

        private String parseTemperature(String result, String temperature) {
            JSONObject jsonObj;
            try {
                jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                temperature = main.getString("temp");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return temperature;
        }

        private String formatTemperature(String temperature) {
            try {
                double tempInCelsius = Double.parseDouble(temperature);
                tempInCelsius -= 273.15;
                NumberFormat df = DecimalFormat.getInstance();
                df.setMinimumFractionDigits(2);
                df.setMaximumFractionDigits(2);
                df.setRoundingMode(RoundingMode.DOWN);
                temperature = df.format(tempInCelsius);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return temperature;
        }

        private void displayTemperature(String temperature) {
            if("".equals(temperature.trim())) {
                textView.setText("No temperature available for the given city!");
            } else {
                textView.setText("The temperature is: " + temperature + " degrees (Celsius)");
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
}