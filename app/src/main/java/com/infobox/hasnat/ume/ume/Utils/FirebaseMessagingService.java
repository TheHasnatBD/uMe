package com.infobox.hasnat.ume.ume.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.infobox.hasnat.ume.ume.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "uMeHasnat";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "uMe Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Hearty365")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("New Friend Request")
                .setContentText("Hi user");

        int mNotificationId = (int) System.currentTimeMillis();
        notificationManager.notify(mNotificationId, notificationBuilder.build());


    }
}
