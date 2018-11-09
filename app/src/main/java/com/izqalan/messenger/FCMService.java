package com.izqalan.messenger;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String CHANNEL_ID = "com.izqalan.messenger";


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // get payload from incoming notification messages
        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();

        // foreground notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(notification_title)
                .setContentText(notification_message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        String from_user_id = remoteMessage.getData().get("from_user_id"); // need this becase ProfileActivity need user_id to launch

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id", from_user_id);

        // opens up profile page when click the notification
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(resultPendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = (int) System.currentTimeMillis();

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());

    }


}
