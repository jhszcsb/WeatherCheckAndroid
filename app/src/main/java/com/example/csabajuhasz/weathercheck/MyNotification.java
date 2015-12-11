package com.example.csabajuhasz.weathercheck;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class MyNotification extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_main);
        CharSequence s = "Inside the activity of MyNotification one ";
        CharSequence subscriptionMessage = "";
        int id=0;

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            s = "error";
        }
        else {
            //id = extras.getInt("notificationId");
            subscriptionMessage = extras.getString("notificationMessage");
        }
        TextView t = (TextView) findViewById(R.id.text1);
        s = s+"with id = "+id + subscriptionMessage;
        t.setText(s);
        NotificationManager myNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // remove the notification with the specific id
        myNotificationManager.cancel(id);

        // todo: automatically remove notification from notification menu after viewing it
    }

}